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
    protected final int ALPHA = 24;
    protected final int RED = 16;
    protected final int GREEN = 8;
    protected final int BLUE = 0;
    protected final int MASK = 0xFF;

    public FilterBase()
    {

    }

    public Bitmap ApplyFilter(Bitmap bmp, ProgressListener listener)
    {
        Bitmap result = bmp.copy(Bitmap.Config.ARGB_8888, true);

        // iterate over every pixel applying the filter
        for (int x = 0; x < bmp.getWidth(); ++x)
        {
            for (int y = 0; y < bmp.getHeight(); ++y)
            {
                ApplyFilterAt(bmp, result, x, y);
            }

            // push updates every time we finish a column
            listener.Update((float) (x) / (float) (bmp.getWidth()));
        }

        return result;
    }

    private int GetColourAndClamp(Bitmap source, int x, int y)
    {
        x = Math.min(x, source.getWidth() - 1);
        x = Math.max(x, 0);
        y = Math.min(y, source.getHeight() - 1);
        y = Math.max(y, 0);

        return source.getPixel(x, y);
    }

    private int AveragePixels(int p1, int p2)
    {
        return WeightedAverage(p1, p2, 0.5f, 0.5f);
    }

    private int WeightedAverage(int p1, int p2, float p1Weight, float p2Weight)
    {
        float total = p1Weight + p2Weight;

        int red = (int)(((((p1 >> RED) & MASK) * p1Weight) + (((p2 >> RED) & MASK) * p2Weight)) / (total));
        int blue = (int)(((((p1 >> BLUE) & MASK) * p1Weight) + (((p2 >> BLUE) & MASK) * p2Weight)) / (total));
        int green = (int)(((((p1 >> GREEN) & MASK) * p1Weight) + (((p2 >> GREEN) & MASK) * p2Weight)) / (total));
        int alpha = (int)(((((p1 >> ALPHA) & MASK) * p1Weight) + (((p2 >> ALPHA) & MASK) * p2Weight)) / (total));

        return (alpha << ALPHA) | (red << RED) | (blue << BLUE) | (green << GREEN);
    }

    /**
     * Use bilinear interpolation to get the colour value at a given point in the source image
     */
    protected int Sample(Bitmap source, float x, float y)
    {
        int left = (int)Math.floor(x);
        int right = left + 1;
        int top = (int)Math.floor(y);
        int bottom = top + 1;

        int topLeft = GetColourAndClamp(source, left, top);
        int topRight = GetColourAndClamp(source, right, top);
        int bottomLeft = GetColourAndClamp(source, left, bottom);
        int bottomRight = GetColourAndClamp(source, right, bottom);

        int leftColour = AveragePixels(topLeft, bottomLeft);
        int rightColour = AveragePixels(topRight, bottomRight);

        float leftWeight = 1.0f - (x - (float)(left));
        float rightWeight = 1.0f - ((float)(right) - x);

        // sum is expected to be 1.0f
        Util.Assert(leftWeight + rightWeight > 0.99f && leftWeight + rightWeight < 1.01f);

        int horizontalColour = WeightedAverage(leftColour, rightColour, leftWeight, rightWeight);

        int topColour = AveragePixels(topLeft, topRight);
        int bottomColour = AveragePixels(bottomLeft, bottomRight);

        float topWeight = 1.0f - (y - (float)(top));
        float bottomWeight = 1.0f - ((float)(bottom) - y);

        // sum is expected to be 1.0f
        Util.Assert(topWeight + bottomWeight > 0.99f && topWeight + bottomWeight < 1.01f);

        int verticalColour = WeightedAverage(topColour, bottomColour, topWeight, bottomWeight);

        return AveragePixels(horizontalColour, verticalColour);
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
