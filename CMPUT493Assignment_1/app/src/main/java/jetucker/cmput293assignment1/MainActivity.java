package jetucker.cmput293assignment1;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener, GestureHelper.IGestureListener
{
    private static final String TAG = "Main Activity";

    static final int PICK_PHOTO = 100;
    static final int TAKE_PHOTO = 101;

    final float HOLD_TIME = 0.8f; // seconds;
    final float SWIPE_DISTANCE = 35.0f;

    private Bitmap m_selectedImage = null;
    private String m_selectedFilterName = "";
    private ProgressDialog m_progressDialog = null;
    private GestureHelper m_gestureHelper = null;
    private GestureOverlay m_gestureOverlay = null;
    private FilterTask m_filterTask = null;
    private Uri m_photoPath = null;
    private UndoSystem m_undo = null;
    private Menu m_menu = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] filterNames = getResources().getStringArray(R.array.filters);
        Util.Assert(filterNames.length > 0);
        m_selectedFilterName = filterNames[0];

        Spinner spinner = (Spinner) findViewById(R.id.filter_selector);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.filters, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        spinner.setSelection(0);

        Button filterControls = (Button) findViewById(R.id.apply_filter_btn);
        filterControls.setEnabled(false);

        m_gestureHelper = new GestureHelper(HOLD_TIME, SWIPE_DISTANCE, this);
        m_gestureOverlay = (GestureOverlay) findViewById(R.id.v_gestureOverlay);

        ImageView imgView = (ImageView) findViewById(R.id.image_view);
        imgView.setOnTouchListener(m_gestureHelper);

        m_undo = new UndoSystem(getCacheDir(), getContentResolver(), getApplicationContext());

        // we may have an image provided to us
        Intent intent = getIntent();
        Uri path = intent.getData();
        if(path != null)
        {
            SetSelectedImage(path);
        }
    }

    public void SelectImage(View v)
    {
        Intent photoPicker = new Intent(Intent.ACTION_PICK);
        photoPicker.setType("image/*");
        startActivityForResult(photoPicker, PICK_PHOTO);
    }

    private void TakePicture()
    {
        try
        {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,  // prefix
                    ".jpg",         // suffix
                    storageDir      // directory
            );

            m_photoPath = Uri.fromFile(image);

            if (image != null)
            {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                startActivityForResult(takePictureIntent, TAKE_PHOTO);
            }
        }
        catch (IOException e)
        {
            Log.e(TAG, e.getMessage());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case PICK_PHOTO:
                OnPickPhoto(resultCode, data);
                break;
            case TAKE_PHOTO:
                OnTakePhoto(resultCode);
                break;
            default:
                Util.Fail("Unhandled activity result" + ((Integer)(resultCode)).toString());
        }
    }

    private void OnTakePhoto(int resultCode)
    {
        if(resultCode == RESULT_OK)
        {
            Util.Assert(m_photoPath != null);
            SetSelectedImage(m_photoPath);

            AddToGallery(m_photoPath);
        }
    }

    private void AddToGallery(Uri path)
    {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(path);
        this.sendBroadcast(mediaScanIntent);
    }

    private void OnPickPhoto(int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            SetSelectedImage(data.getData());
        }
    }

    private void SetSelectedImage(Uri path)
    {
        m_photoPath = path;
        ImageView imgView = (ImageView) findViewById(R.id.image_view);

        m_selectedImage = Util.LoadBitmap(getContentResolver(), m_photoPath);

        if(m_selectedImage != null)
        {
            m_undo.Clear();

            imgView.setImageBitmap(m_selectedImage);
        }
        else
        {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Could not find image!",
                    Toast.LENGTH_LONG);
            toast.show();
        }

        UpdateUIElements();
    }

    private void UpdateUIElements()
    {
        Button filterControls = (Button) findViewById(R.id.apply_filter_btn);
        filterControls.setEnabled(m_selectedImage != null);

        m_menu.findItem(R.id.undoButton).setEnabled(m_selectedImage != null && m_undo.GetUndoAvailable() > 0);
        m_menu.findItem(R.id.discardButton).setEnabled(m_selectedImage != null);
        m_menu.findItem(R.id.saveButton).setEnabled(m_selectedImage != null);
    }

    public void ApplyFilter(View v)
    {
        Util.Assert(m_selectedImage != null, "This should not be an available option if no image is available!");

        LaunchFilter();
    }

    private void LaunchFilter()
    {
        LaunchFilter(null);
    }

    private void LaunchFilter(FilterBase filter)
    {
        if(m_filterTask != null || m_selectedImage == null)
        {
            return;
        }

        if(m_progressDialog == null)
        {
            m_progressDialog = new ProgressDialog(MainActivity.this);
        }
        m_progressDialog.setMessage("Processing Image...");
        m_progressDialog.setCancelable(false);
        m_progressDialog.setProgress(0);
        m_progressDialog.setMax(100);
        m_progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        m_progressDialog.show();

        m_filterTask = new FilterTask(m_selectedImage, filter);
        m_filterTask.execute(m_selectedFilterName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.undoButton).setEnabled(false);
        menu.findItem(R.id.saveButton).setEnabled(false);
        menu.findItem(R.id.discardButton).setEnabled(false);

        m_menu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        switch (id)
        {
            case R.id.action_settings:
                OpenSettings();
                return true;
            case R.id.saveButton:
                SaveImage();
                return true;
            case R.id.undoButton:
                Undo();
                return true;
            case R.id.cameraButton:
                TakePicture();
                return true;
            case R.id.discardButton:
                Discard();
                return true;
            default:
                Util.Fail("Unhandled option id : " + ((Integer)(id)).toString());
        }

        return super.onOptionsItemSelected(item);
    }

    private void Discard()
    {
        if(m_selectedImage == null)
        {
            return;
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Do you want to discard the image without saving?");
        // alert.setMessage("Message");

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                //Your action here
                m_selectedImage = null;
                ImageView imgView = (ImageView) findViewById(R.id.image_view);
                imgView.setImageBitmap(null);
                m_undo.Clear();

                UpdateUIElements();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton)
                {
                    // do nothing
                }
            });

        alert.show();
    }

    private void Undo()
    {
        Bitmap prev = m_undo.PopImage();
        if(prev != null)
        {
            m_selectedImage = prev;
            ImageView imgView = (ImageView) findViewById(R.id.image_view);
            imgView.setImageBitmap(prev);
        }

        UpdateUIElements();
    }

    private void SaveImage()
    {
        if(m_selectedImage == null)
        {
            return;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_" + timeStamp + ".png";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, imageFileName);

        Util.WriteBitmapToFile(m_selectedImage, image);

        AddToGallery(Uri.fromFile(image));
    }

    private void OpenSettings()
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        m_selectedFilterName = (String) parent.getItemAtPosition(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
        // Do Nothing
    }

    @Override
    public void OnHoldStart(GestureHelper.HoldInfo holdInfo)
    {
        m_gestureOverlay.SetHold(holdInfo);
    }

    @Override
    public void OnHoldContinue(GestureHelper.HoldInfo holdInfo)
    {
        m_gestureOverlay.SetHold(holdInfo);
    }

    @Override
    public void OnHoldEnd(GestureHelper.HoldInfo holdInfo)
    {
        m_gestureOverlay.Clear();

        if(m_selectedImage == null)
        {
            return;
        }

        Point center = FromViewToImg(holdInfo.CenterPoint);
        float radius = FromViewToImg(holdInfo.Radius());

        Bulge bulge = new Bulge(getApplicationContext(), center, radius);
        LaunchFilter(bulge);
    }

    @Override
    public void OnPinchStart(GestureHelper.PinchInfo pinchInfo)
    {
        m_gestureOverlay.SetPinch(pinchInfo);
    }

    @Override
    public void OnPinchContinue(GestureHelper.PinchInfo pinchInfo)
    {
        m_gestureOverlay.SetPinch(pinchInfo);
    }

    @Override
    public void OnPinchEnd(GestureHelper.PinchInfo pinchInfo)
    {
        m_gestureOverlay.Clear();

        if(m_selectedImage == null)
        {
            return;
        }

        Point center = FromViewToImg(pinchInfo.Center());
        float radius = FromViewToImg(pinchInfo.Radius());
        RadialTwist twist = new RadialTwist(center, radius, -1.0f * pinchInfo.Angle(), getApplicationContext());
        LaunchFilter(twist);
    }

    @Override
    public void OnSwipeStart(GestureHelper.SwipeInfo swipeInfo)
    {
        m_gestureOverlay.SetSwipe(swipeInfo);
    }

    @Override
    public void OnSwipeMove(GestureHelper.SwipeInfo swipeInfo)
    {
        m_gestureOverlay.SetSwipe(swipeInfo);
    }

    @Override
    public void OnSwipeEnd(GestureHelper.SwipeInfo swipeInfo)
    {
        m_gestureOverlay.Clear();

        // TODO::JT hook up strengths and distance
        PartialBlockify blocky = new PartialBlockify(getApplicationContext(), 5.0f, 5.0f, 0.5f);
        LaunchFilter(blocky);
    }

    @Override
    public void Cancel()
    {
        m_gestureOverlay.Clear();
    }

    private float FromViewToImg(float dist)
    {
        ImageView imgView = (ImageView) findViewById(R.id.image_view);
        float viewWidth = imgView.getWidth();
        float viewHeight = imgView.getHeight();
        float imgWidth = m_selectedImage.getWidth();
        float imgHeight = m_selectedImage.getHeight();

        float widthRatio = imgWidth / viewWidth;
        float heightRatio = imgHeight / viewHeight;

        float ratioToUse = Math.max(widthRatio, heightRatio);

        return dist * ratioToUse;
    }

    private Point FromViewToImg(Point p)
    {
        Point result = new Point();

        result.x = (int)FromViewToImg(p.x);
        result.y = (int)FromViewToImg(p.y);

        return result;
    }

    /**
     * Simple task for performing the bitmap filtering.
     * Will update the progress dialogue as it works.
     */
    private class FilterTask extends AsyncTask<String, Float, Bitmap>
    {
        Bitmap m_source = null;
        int m_filterSize = 0;
        FilterBase m_filter = null;

        public FilterTask(Bitmap bmp, FilterBase filter)
        {
            Util.Assert(bmp != null, "Cannot filter a null bitmap!");
            m_source = bmp;
            m_filter = filter;
        }

        private FilterBase SelectFilter(String name)
        {
            FilterBase result = null;
            switch (name)
            {
                case "Median Filter":
                    m_filterSize = Settings.GetMedianFilterSize(getApplicationContext());
                    result = (new MedianFilter(m_filterSize));
                    break;
                case "Mean Filter":
                    m_filterSize = Settings.GetMeanFilterSize(getApplicationContext());
                    result = (new MeanFilter(m_filterSize));
                    break;
                default:
                    Util.Fail("Unidentified filter : " + name);
            }

            return result;
        }

        @Override
        protected Bitmap doInBackground(String... params)
        {
            Bitmap result = null;
            Util.Assert(params.length == 1 || m_filter != null, "Filter task requires a filter or one filter name!");
            FilterUpdateListener listener = new FilterUpdateListener();

            if(m_filter == null)
            {
                m_filter = SelectFilter(params[0]);
            }

            // save the previous image before applying
            m_undo.AddImage(m_selectedImage);

            try
            {
                result = m_filter.ApplyFilter(m_source, listener);
            }
            catch(OutOfMemoryError ex)
            {
                // Note: this should never actually happen, but if it does
                // happen this is when it will happen. Bug that caused it
                // should be fixed. Leaving this code in to help
                // diagnose any missed issues.
                Handler handler = new Handler(Looper.getMainLooper());

                handler.post(new Runnable() {

                    @Override
                    public void run()
                    {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Out of memory, please restart application.",
                                Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
            }

            return result;
        }

        @Override
        protected void onProgressUpdate(Float... progress)
        {
            Util.Assert(progress.length == 1, "Only one progress update expected!");
            m_progressDialog.setProgress((int) (progress[0] * 100));
        }

        @Override
        protected void onPostExecute(Bitmap bmp)
        {

            if(bmp != null)
            {
                m_selectedImage = bmp;
                ImageView imgView = (ImageView) findViewById(R.id.image_view);
                imgView.setImageBitmap(m_selectedImage);
            }

            m_progressDialog.hide();
            m_filterTask = null;

            UpdateUIElements();
        }

        private class FilterUpdateListener implements FilterBase.ProgressListener
        {
            @Override
            public void Update(float progress)
            {
                publishProgress(progress);
            }
        }
    }
}
