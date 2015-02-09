package jetucker.cmput293assignment1;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;

import jetucker.cmput293assignment1.rs.ScriptC_PartialBlockify;

/**
 * Created by Jesse on 2015-02-05.
 *
 * Partially pixelifies an image.
 */
public class PartialBlockify extends FilterBase
{
    private static RenderScript s_Rs;
    private static ScriptC_PartialBlockify s_script;

    private float m_magnitude = 0.0f;
    private float m_period = 1.0f;
    private float m_strength = 0.5f;

    PartialBlockify(Context context, float magnitude, float period, float strength)
    {
        Util.Assert(period >= 1.0f);
        Util.Assert(strength > 0.0f);
        Util.Assert(strength <= 1.0f);

        if(s_Rs == null)
        {
            s_Rs = RenderScript.create(context);
        }

        m_magnitude = magnitude;
        m_period = period;
        m_strength = strength;
    }

    @Override
    public Bitmap ApplyFilter(Bitmap bmp, ProgressListener listener)
    {
        Bitmap result = bmp.copy(Bitmap.Config.ARGB_8888, true);

        Allocation inAlloc = Allocation.createFromBitmap(s_Rs, bmp);
        Allocation outAlloc = Allocation.createFromBitmap(s_Rs, result);

        if(s_script == null)
        {
            s_script = new ScriptC_PartialBlockify(s_Rs);
        }

        s_script.set_m_source(inAlloc);
        s_script.set_m_width(bmp.getWidth());
        s_script.set_m_height(bmp.getHeight());
        s_script.set_m_magnitude(m_magnitude);
        s_script.set_m_period(m_period);
        s_script.set_m_strength(m_strength);

        s_script.forEach_partialBlockify(inAlloc, outAlloc);
        outAlloc.copyTo(result);

        return result;
    }

    @Override
    protected void ApplyFilterAt(Bitmap source, Bitmap target, int x_center, int y_center)
    {
        Util.Fail("This does not make sense for a renderscript");
    }
}
