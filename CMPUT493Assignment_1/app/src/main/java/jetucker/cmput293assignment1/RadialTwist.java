package jetucker.cmput293assignment1;

import android.graphics.Bitmap;
import android.graphics.Point;

/**
 * Created by Jesse on 2015-02-03.
 */
public class RadialTwist extends FilterBase
{
    Point m_center = null;
    float m_radius = 0.0f;
    float m_twist = 0.0f;

    /**
     * Creates a radial twist filter
     *
     * Only twists the pixels within the circle defined by center and radius.
     * The twist is more pronounced near the center and tappers off towards
     * the edge. There is a linear relationship between twist and radius. Ie.
     * the twist will be twice as strong in the center and zero at the edge and will
     * drop off linearly between the two radial points.
     *
     * @param center The center point of the twist
     * @param radius The radius of the transform, no pixels outside the circle
     * @param twist Measure, in radians, of how much twist is applied. This is the offset
     *              applied to the pixels at the middle of the radius, ie. pixels equidistant
     *              from the center and the edge of the circle.
     */
    public RadialTwist(Point center, float radius, float twist)
    {
        Util.Assert(center != null);
        Util.Assert(radius > 0);

        m_center = center;
        m_radius = radius;
        m_twist = twist;
    }

    @Override
    protected void ApplyFilterAt(Bitmap source, Bitmap target, int x, int y)
    {
        // TODO::JT convert to renderscript
        float radius = (float)Math.sqrt((x - m_center.x) * (x - m_center.x) + (y - m_center.y) * (y - m_center.y));
        if(radius < m_radius)
        {
            float twist = (m_radius - radius) / m_radius;

            float oldX = x;
            float oldY = y;

            float relative_x = x - m_center.x;
            float relative_y = y - m_center.y;

            float sin_twist = (float) Math.sin(twist);
            float cos_twist = (float) Math.cos(twist);

            float newX = relative_x * cos_twist + relative_y * sin_twist;
            float newY = -1.0f * relative_x * sin_twist + relative_y * cos_twist;

            newX += m_center.x;
            newY += m_center.y;

            target.setPixel(x, y, Sample(source, newX, newY));
        }
        else
        {
            // outside range, just set to original colour
            target.setPixel(x, y, source.getPixel(x, y));
        }
    }
}
