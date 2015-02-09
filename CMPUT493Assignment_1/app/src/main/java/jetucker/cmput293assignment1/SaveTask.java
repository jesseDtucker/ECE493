package jetucker.cmput293assignment1;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.File;

public final class SaveTask extends AsyncTask<Void, Void, Void>
{
    private Bitmap m_bmp = null;
    private File m_file = null;
    private OnSaveCompleteListener m_listener;

    public SaveTask(Bitmap bmp, File file, OnSaveCompleteListener listener)
    {
        m_bmp = bmp;
        m_file = file;
        m_listener = listener;
    }

    @Override
    protected void onPostExecute(Void bmp)
    {
        m_listener.OnComplete(m_file);
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        Util.WriteBitmapToFile(m_bmp, m_file);

        return null;
    }

    public interface OnSaveCompleteListener
    {
        void OnComplete(File savedFile);
    }
}
