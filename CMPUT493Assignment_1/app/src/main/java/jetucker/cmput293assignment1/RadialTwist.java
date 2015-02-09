package jetucker.cmput293assignment1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.renderscript.Allocation;
import android.renderscript.Float2;
import android.renderscript.RenderScript;

import jetucker.cmput293assignment1.rs.ScriptC_twist;

/**
 * Created by Jesse on 2015-02-03.
 *
 * Twists the image within a user defined circle
 */
public class RadialTwist extends FilterBase
{
    private Point m_center = null;
    private float m_radius = 0.0f;
    private float m_twist = 0.0f;

    private static RenderScript s_Rs;
    private static ScriptC_twist s_twistScript;

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
    public RadialTwist(Point center, float radius, float twist, Context context)
    {
        Util.Assert(center != null);
        Util.Assert(radius > 0);
        Util.Assert(context != null);

        if(s_Rs == null)
        {
            s_Rs = RenderScript.create(context);
        }

        m_center = center;
        m_radius = radius;
        m_twist = twist;
    }

    @Override
    public Bitmap ApplyFilter(Bitmap bmp, ProgressListener listener)
    {
        Bitmap result = bmp.copy(Bitmap.Config.ARGB_8888, true);

        Allocation inAlloc = Allocation.createFromBitmap(s_Rs, bmp);
        Allocation outAlloc = Allocation.createFromBitmap(s_Rs, result);

        if(s_twistScript == null)
        {
            s_twistScript = new ScriptC_twist(s_Rs);
        }

        s_twistScript.set_m_center(new Float2(m_center.x, m_center.y));
        s_twistScript.set_m_radius(m_radius);
        s_twistScript.set_m_twist(m_twist);
        s_twistScript.set_m_source(inAlloc);
        s_twistScript.set_m_width(bmp.getWidth());
        s_twistScript.set_m_height(bmp.getHeight());

        s_twistScript.forEach_twist(inAlloc, outAlloc);

        outAlloc.copyTo(result);

        return result;
    }

    @Override
    protected void ApplyFilterAt(Bitmap source, Bitmap target, int x_center, int y_center)
    {
        Util.Fail("This does not make sense for a renderscript filter");
    }
}
