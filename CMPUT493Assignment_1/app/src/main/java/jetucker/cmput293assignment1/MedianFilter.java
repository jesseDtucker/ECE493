package jetucker.cmput293assignment1;

import android.graphics.Bitmap;

import java.util.Arrays;

public class MedianFilter extends FilterBase
{
    public MedianFilter(int size)
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

        int[] pixels = new int[pixelCount];
        int[] redChannel = new int[pixelCount];
        int[] greenChannel = new int[pixelCount];
        int[] blueChannel = new int[pixelCount];
        int[] alphaChannel = new int[pixelCount];

        int width = right - left + 1;
        int height = bottom - top + 1;
        source.getPixels(pixels, 0, width, left, top, width, height);

        for(int i = 0 ; i < pixels.length ; ++i)
        {
            redChannel[i] = ((pixels[i] >> RED) & MASK);
            greenChannel[i] = ((pixels[i] >> GREEN) & MASK);
            blueChannel[i] = ((pixels[i] >> BLUE) & MASK);
            alphaChannel[i] = ((pixels[i] >> ALPHA) & MASK);
        }

        Arrays.sort(redChannel);
        Arrays.sort(greenChannel);
        Arrays.sort(blueChannel);
        Arrays.sort(alphaChannel);

        int medianRed = redChannel[redChannel.length / 2];
        int medianGreen = greenChannel[greenChannel.length / 2];
        int medianBlue = blueChannel[blueChannel.length / 2];
        int mediaAlpha = alphaChannel[alphaChannel.length / 2];

        int newColor = (mediaAlpha << ALPHA) | (medianRed << RED) | (medianBlue << BLUE) | (medianGreen << GREEN);

        target.setPixel(x_center, y_center, newColor);
    }
}
