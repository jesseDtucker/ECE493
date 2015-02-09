package jetucker.cmput293assignment1;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;

/**
 * Created by Jesse on 2015-02-08.
 *
 * Handles the undo system. 
 */
public final class UndoSystem
{
    private static String TAG = "Undo System";

    private int m_undoLimit = 50; // TODO::JT use this!
    private Stack<Uri> m_images = new Stack<>();
    private File m_cacheFolder = null;
    private ContentResolver m_contentResolver = null;

    UndoSystem(int undoLimit, File cacheFolder, ContentResolver contentResolver)
    {
        Util.Assert(undoLimit > 0);
        m_undoLimit = undoLimit;
        m_cacheFolder = cacheFolder;
        m_contentResolver = contentResolver;

        Clear();
    }

    public void Clear()
    {
        File[] existingFiles = m_cacheFolder.listFiles(new ImageFilter());
        for(File f : existingFiles)
        {
            boolean isDeleted = f.delete();
            Util.Assert(isDeleted, "Failed to delete a temp file from the cache");
        }
    }

    public void AddImage(Bitmap bmp)
    {
        m_images.push(SaveBitmap(bmp));
    }

    public Bitmap PopImage()
    {
        Util.Assert(m_images.size() != 0);
        Bitmap result = null;
        boolean lastTry = false;

        // files in the cache folder can be deleted at any time by the system
        // as such we should try to undo to the last available file. If one exists
        while(result == null && !lastTry)
        {
            if(m_images.size() > 1)
            {
                result = Util.LoadBitmap(m_contentResolver, m_images.pop());
            }
            else
            {
                result = Util.LoadBitmap(m_contentResolver, m_images.peek());
                lastTry = true;
            }
        }

        return result;
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
