package jetucker.cmput293assignment1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.FileNotFoundException;

public class MainActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener
{
    static final int PICK_PHOTO = 100;
    // max width or height the application will accept. Anything
    // larger will be downsampled
    final int MAX_IMG_WIDTH = 2048;
    final int MAX_IMG_HEIGHT = 2048;
    Bitmap m_selectedImage = null;
    String m_selectedFilterName = "";
    ProgressDialog m_progressDialog = null;

    // Courtesy of : http://stackoverflow.com/questions/364985/algorithm-for-finding-the-smallest-power-of-two-thats-greater-or-equal-to-a-giv
    private static int NextLargestPowerOfTwo(int x)
    {
        if (x < 0)
            return 0;
        --x;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        return x + 1;
    }

    /**
     * Calculates the sample size needed to keep the provided image under
     * the maxWidth and maxHeight limits. Will always return a power of 2.
     */
    private static int CalculateInSampleSize(BitmapFactory.Options options,
                                             int maxWidth,
                                             int maxHeight)
    {
        // Raw height and width of image
        int height = options.outHeight;
        int width = options.outWidth;

        int widthSample = NextLargestPowerOfTwo(width / maxWidth);
        int heightSample = NextLargestPowerOfTwo(height / maxHeight);

        return Math.max(1, Math.max(widthSample, heightSample));
    }

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
    }

    public void SelectImage(View v)
    {
        Intent photoPicker = new Intent(Intent.ACTION_PICK);
        photoPicker.setType("image/*");
        startActivityForResult(photoPicker, PICK_PHOTO);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_PHOTO)
        {
            if (resultCode == RESULT_OK)
            {
                Uri imageUri = data.getData();
                ImageView imgView = (ImageView) findViewById(R.id.image_view);
                try
                {
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                    bitmapOptions.inJustDecodeBounds = true;

                    // just getting the size first
                    BitmapFactory.decodeStream(
                            getContentResolver().openInputStream(imageUri)
                            , null
                            , bitmapOptions);

                    bitmapOptions.inSampleSize = CalculateInSampleSize(bitmapOptions, MAX_IMG_WIDTH, MAX_IMG_HEIGHT);
                    bitmapOptions.inJustDecodeBounds = false;

                    m_selectedImage = BitmapFactory.decodeStream(
                            getContentResolver().openInputStream(imageUri)
                            , null
                            , bitmapOptions);

                    imgView.setImageBitmap(m_selectedImage);

                    Button filterControls = (Button) findViewById(R.id.apply_filter_btn);
                    filterControls.setEnabled(true);
                } catch (FileNotFoundException e)
                {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Could not find image!",
                            Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }
    }

    public void ApplyFilter(View v)
    {
        if (m_selectedImage == null)
        {
            Util.Fail("This should not be an available option if no image is available!");
            return;
        }

        m_progressDialog = new ProgressDialog(MainActivity.this);
        m_progressDialog.setMessage("Processing Image...");
        m_progressDialog.setCancelable(false);
        m_progressDialog.setProgress(0);
        m_progressDialog.setMax(100);
        m_progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        m_progressDialog.show();

        (new FilterTask(m_selectedImage)).execute(m_selectedFilterName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    /**
     * Simple task for performing the bitmap filtering.
     * Will update the progress dialogue as it works.
     */
    private class FilterTask extends AsyncTask<String, Float, Bitmap>
    {
        Bitmap m_source = null;
        int m_filterSize = 0;

        public FilterTask(Bitmap bmp)
        {
            Util.Assert(bmp != null, "Cannot filter a null bitmap!");
            m_source = bmp;
        }

        @Override
        protected Bitmap doInBackground(String... params)
        {
            Bitmap result = null;
            Util.Assert(params.length == 1, "Filter task only expects one filter to apply!");
            FilterUpdateListener listener = new FilterUpdateListener();

            Point pos = new Point(m_source.getWidth() / 2, m_source.getHeight() / 2);
            float radius = Math.min(m_source.getWidth(), m_source.getHeight()) / 2.0f;

            switch (params[0])
            {
                case "Median Filter":
                    m_filterSize = Settings.GetMedianFilterSize(getApplicationContext());
                    result = (new Bulge(getApplicationContext(), pos, radius)).ApplyFilter(m_source, listener);
                    break;
                case "Mean Filter":
                    m_filterSize = Settings.GetMeanFilterSize(getApplicationContext());
                    result = (new RadialTwist(pos, radius, (float)(Math.PI) / 8.0f, getApplicationContext())).ApplyFilter(m_source, listener);
                    break;
                default:
                    Util.Fail("Unidentified filter : " + params[0]);
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
            m_selectedImage = bmp;
            ImageView imgView = (ImageView) findViewById(R.id.image_view);
            imgView.setImageBitmap(m_selectedImage);

            m_progressDialog.hide();
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
