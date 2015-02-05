package jetucker.cmput293assignment1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.renderscript.Allocation;
import android.renderscript.Float2;
import android.renderscript.RenderScript;
import android.widget.Filter;

import jetucker.cmput293assignment1.rs.ScriptC_bulge;
import jetucker.cmput293assignment1.rs.ScriptC_twist;

/**
 * Created by Jesse on 2015-02-05.
 */
public class Bulge extends FilterBase
{
    private Point m_center = null;
    private float m_radius = 0.0f;

    private RenderScript m_Rs;

    Bulge(Context context, Point center, float radius)
    {
        Util.Assert(center != null);
        Util.Assert(radius > 0);

        m_Rs = RenderScript.create(context);

        m_radius = radius;
        m_center = center;
    }

    @Override
    public Bitmap ApplyFilter(Bitmap bmp, ProgressListener listener)
    {
        Bitmap result = bmp.copy(Bitmap.Config.ARGB_8888, true);

        Allocation inAlloc = Allocation.createFromBitmap(m_Rs, bmp);
        Allocation outAlloc = Allocation.createFromBitmap(m_Rs, result);
        ScriptC_bulge script = new ScriptC_bulge(m_Rs);

        script.set_m_center(new Float2(m_center.x, m_center.y));
        script.set_m_radius(m_radius);
        script.set_m_source(inAlloc);
        script.set_m_width(bmp.getWidth());
        script.set_m_height(bmp.getHeight());

        script.forEach_bulge(inAlloc, outAlloc);

        outAlloc.copyTo(result);

        return result;
    }

    @Override
    protected void ApplyFilterAt(Bitmap source, Bitmap target, int x_center, int y_center)
    {
        Util.Fail("This does not make sense for a renderscript");
    }
}
