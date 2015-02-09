package jetucker.cmput293assignment1;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Util functions
 */
public class Util
{
    private static String TAG = "Util";

    // max width or height the application will accept. Anything
    // larger will be downsampled
    private static final int MAX_IMG_WIDTH = 2048;
    private static final int MAX_IMG_HEIGHT = 2048;

    // Android is dumb and doesn't actually respect asserts. This function
    // will demand the respect that assert deserves.
    public static void Assert(boolean check)
    {
        Assert(check, "Assertion Failed!");
    }

    public static void Assert(boolean check, String message)
    {
        // I consider asserts to be a sanity check for the programmer, so only use in
        // debug builds
        if (BuildConfig.DEBUG)
        {
            if (!check)
            {
                throw new AssertionError(message);
            }
        }
    }

    public static void Fail(String message)
    {
        if (BuildConfig.DEBUG)
        {
            throw new AssertionError(message);
        }
    }

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

    public static Bitmap LoadBitmap(ContentResolver resolver, Uri path)
    {
        Bitmap result = null;

        try
        {
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inJustDecodeBounds = true;

            // just getting the size first
            BitmapFactory.decodeStream(
                    resolver.openInputStream(path)
                    , null
                    , bitmapOptions);

            bitmapOptions.inSampleSize = CalculateInSampleSize(bitmapOptions, MAX_IMG_WIDTH, MAX_IMG_HEIGHT);
            bitmapOptions.inJustDecodeBounds = false;

            result = BitmapFactory.decodeStream(
                    resolver.openInputStream(path)
                    , null
                    , bitmapOptions);
        }
        catch (FileNotFoundException e)
        {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    public static void WriteBitmapToFile(Bitmap bmp, File file)
    {
        try
        {
            FileOutputStream outStream = new FileOutputStream(file);
            // try with resources requires API 19...
            try
            {
                boolean wasSuccess = bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                Util.Assert(wasSuccess, "Failed to compress image to file");
            }
            finally
            {
                outStream.close();
            }
        }
        catch (FileNotFoundException e)
        {
            Log.e(TAG, e.getMessage());
        }
        catch (IOException e)
        {
            Log.e(TAG, e.getMessage());
        }
    }

    public static int GetDistSquared(Point p1, Point p2)
    {
        int xSqrd = (p1.x - p2.x)* (p1.x - p2.x);
        int ySqrd = (p1.y - p2.y) * (p1.y - p2.y);
        return xSqrd + ySqrd;
    }

    public static float GetAngle(Point p1, Point p2)
    {
        return (float)Math.atan2(   p1.y - p2.y,
                                    p1.x - p2.x);
    }

    // Thanks to stack overflow: http://stackoverflow.com/questions/3365171/calculating-the-angle-between-two-lines-without-having-to-calculate-the-slope
    public static float AngleBetween2Lines(Point line1Point1, Point line1Point2, Point line2Point1, Point line2Point2)
    {
        float angle1 = GetAngle(line1Point1, line1Point2);
        float angle2 = GetAngle(line2Point1, line2Point2);
        return angle1-angle2;
    }
}
