package jetucker.cmput293assignment1;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

/**
 * Created by Jesse on 2015-02-08.
 *
 * Handles the undo system. 
 */
public final class UndoSystem
{
    private LinkedList<Uri> m_images = new LinkedList<>();
    private File m_cacheFolder = null;
    private ContentResolver m_contentResolver = null;
    private Context m_context;

    UndoSystem(File cacheFolder, ContentResolver contentResolver, Context context)
    {
        m_cacheFolder = cacheFolder;
        m_contentResolver = contentResolver;
        m_context = context;

        Clear();
    }

    public void Clear()
    {
        File[] existingFiles = m_cacheFolder.listFiles(new ImageFilter());
        m_images = new LinkedList<>();
        for(File f : existingFiles)
        {
            boolean isDeleted = f.delete();
            Util.Assert(isDeleted, "Failed to delete a temp file from the cache");
        }
    }

    public void AddImage(Bitmap bmp)
    {
        m_images.addLast(SaveBitmap(bmp));
        LimitSize();
    }

    public Bitmap PopImage()
    {
        LimitSize();

        if(m_images.size() == 0)
        {
            return null;
        }

        Bitmap result = null;

        // files in the cache folder can be deleted at any time by the system
        // as such we should try to undo to the last available file. If one exists
        while(result == null && m_images.size() > 0)
        {
            result = Util.LoadBitmap(m_contentResolver, m_images.removeLast());
        }

        return result;
    }

    public int GetUndoAvailable()
    {
        return m_images.size();
    }

    private void LimitSize()
    {
        while(m_images.size() > Settings.GetUndoLimit(m_context))
        {
            // remove from front
            Uri img = m_images.removeFirst();
            File file = new File(img.getPath());
            boolean wasDeleted = file.delete();
            Util.Assert(wasDeleted);
        }
    }

    private Uri SaveBitmap(Bitmap bmp)
    {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_cache_" + timeStamp + ".png";
        File image = new File(m_cacheFolder, imageFileName);

        Util.WriteBitmapToFile(bmp, image);

        return Uri.fromFile(image);
    }

    class ImageFilter implements FileFilter
    {
        @Override
        public boolean accept(File pathname)
        {
            return pathname.getPath().contains(".png");
        }
    }
}
