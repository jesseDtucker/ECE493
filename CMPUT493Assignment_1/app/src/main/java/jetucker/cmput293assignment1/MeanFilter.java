package jetucker.cmput293assignment1;

import android.graphics.Bitmap;

public final class MeanFilter extends FilterBase
{
    public MeanFilter(int size)
    {
        super(size);
    }

    @Override
    protected void ApplyFilterAt(Bitmap source, Bitmap target, int x_center, int y_center)
    {
        int edgeSize = (m_size - 1) / 2;
        int left = x_center - edgeSize;
        int right = x_center + edgeSize;
        int top = y_center - edgeSize;
        int bottom = y_center + edgeSize;

        left = Math.max(left, 0);
        right = Math.min(right, source.getWidth() - 1);
        top = Math.max(top, 0);
        bottom = Math.min(bottom, source.getHeight() - 1);

        int pixelCount = (right - left + 1) * (bottom - top + 1);
        int redCount = 0;
        int blueCount = 0;
        int greenCount = 0;
        int alphaCount = 0;

        for(int x = left ; x <= right ; ++x)
        {
            for(int y = top ; y <= bottom ; ++y)
            {
                int color = source.getPixel(x, y);
                redCount += (color >> RED) & MASK;
                blueCount += (color >> BLUE) & MASK;
                greenCount += (color >> GREEN) & MASK;
                alphaCount += (color >> ALPHA) & MASK;
            }
        }

        int redAvg = redCount / pixelCount;
        int blueAvg = blueCount / pixelCount;
        int greenAvg = greenCount / pixelCount;
        int alphaAvg = alphaCount / pixelCount;

        int newColor = (alphaAvg << ALPHA) | (redAvg << RED) | (blueAvg << BLUE) | (greenAvg << GREEN);

        target.setPixel(x_center, y_center, newColor);
    }
}
