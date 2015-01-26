package jetucker.cmput293assignment1;

import android.graphics.Bitmap;

/**
 * Base class from which other filters may derive.
 * Provides the functionality for iterating over the image
 * as well as providing updates to a listener. Sub-classes
 * determine how to actually modify each pixel.
 */
public abstract class FilterBase
{
    protected int m_size = 3;

    protected final int ALPHA = 24;
    protected final int RED = 16;
    protected final int GREEN = 8;
    protected final int BLUE = 0;
    protected final int MASK = 0xFF;

    public FilterBase(int size)
    {
        Util.Assert(size % 2 == 1 && size > 1, "Filter size must be an odd number greater than 1!");
        m_size = size;
    }

    public Bitmap ApplyFilter(Bitmap bmp, ProgressListener listener)
    {
        Bitmap result = bmp.copy(Bitmap.Config.ARGB_8888, true);

        // iterate over every pixel applying the filter
        for(int x = 0 ; x < bmp.getWidth() ; ++x)
        {
            for(int y = 0 ; y < bmp.getHeight(); ++y)
            {
                ApplyFilterAt(bmp, result, x, y);
            }

            // push updates every time we finish a column
            listener.Update((float)(x) / (float)(bmp.getWidth()));
        }

        return result;
    }

    /**
     * Apply the filter to target using source at position (x,y)
     */
    protected abstract void ApplyFilterAt(Bitmap source, Bitmap target, int x_center, int y_center);

    // I wish I had java 8... lambda's and the other function stuff...
    /**
     * Interface for a callback that will be updated as the filter progresses
     */
    public interface ProgressListener
    {
        void Update(float progress);
    }
}
