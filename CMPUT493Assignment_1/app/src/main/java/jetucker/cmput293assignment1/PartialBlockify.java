package jetucker.cmput293assignment1;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;

import jetucker.cmput293assignment1.rs.ScriptC_PartialBlockify;

/**
 * Created by Jesse on 2015-02-05.
 */
public class PartialBlockify extends FilterBase
{
    private RenderScript m_Rs;
    private float m_magnitude = 0.0f;
    private float m_period = 1.0f;
    private float m_strength = 0.5f;

    PartialBlockify(Context context, float magnitude, float period, float strength)
    {
        Util.Assert(period >= 1.0f);
        Util.Assert(strength > 0.0f);
        Util.Assert(strength <= 1.0f);

        m_Rs = RenderScript.create(context);
        m_magnitude = magnitude;
        m_period = period;
        m_strength = strength;
    }

    @Override
    public Bitmap ApplyFilter(Bitmap bmp, ProgressListener listener)
    {
        Bitmap result = bmp.copy(Bitmap.Config.ARGB_8888, true);

        Allocation inAlloc = Allocation.createFromBitmap(m_Rs, bmp);
        Allocation outAlloc = Allocation.createFromBitmap(m_Rs, result);
        ScriptC_PartialBlockify script = new ScriptC_PartialBlockify(m_Rs);

        script.set_m_source(inAlloc);
        script.set_m_width(bmp.getWidth());
        script.set_m_height(bmp.getHeight());
        script.set_m_magnitude(m_magnitude);
        script.set_m_period(m_period);
        script.set_m_strength(m_strength);

        script.forEach_partialBlockify(inAlloc, outAlloc);
        outAlloc.copyTo(result);

        script.destroy();
        m_Rs.destroy();

        return result;
    }

    @Override
    protected void ApplyFilterAt(Bitmap source, Bitmap target, int x_center, int y_center)
    {
        Util.Fail("This does not make sense for a renderscript");
    }
}
