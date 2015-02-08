package jetucker.cmput293assignment1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class GestureOverlay extends SurfaceView implements SurfaceHolder.Callback, Runnable
{
    private final float ALPHA = 0.3f;
    private final float FPS = 60.0f;
    private final String TAG = "GestureOverlay";

    private static final float FINGER_SIZE = 60.0f; // assumed radius of a finger, roughly speaking
    private static final float BORDER_WIDTH = 5.0f;
    private static final float LINE_WIDTH = 20.0f;

    private static final int FILL_COLOUR = 0x90959595;
    private static final int BORDER_COLOUR = 0X90555555;

    private Thread m_drawingThread = null;
    private boolean m_isDrawing = false;
    private SurfaceHolder m_surface = null;
    private Lock m_syncLock = new ReentrantLock();

    private GestureHelper.HoldInfo m_holdInfo = null;
    private GestureHelper.PinchInfo m_pinchInfo = null;
    private GestureHelper.SwipeInfo m_swipeInfo = null;

    private static Paint s_fill = null;
    private static Paint s_border = null;
    private static Paint s_line = null;

    // I love this feature!
    static
    {
        s_fill = new Paint();
        s_fill.setColor(FILL_COLOUR);
        s_fill.setStyle(Paint.Style.FILL);

        s_border = new Paint();
        s_border.setColor(BORDER_COLOUR);
        s_border.setStyle(Paint.Style.STROKE);
        s_border.setStrokeWidth(BORDER_WIDTH);

        s_line = new Paint();
        s_line.setColor(BORDER_COLOUR);
        s_line.setStyle(Paint.Style.STROKE);
        s_line.setStrokeWidth(LINE_WIDTH);
    }

    public GestureOverlay(Context context)
    {
        super(context);
        Setup();
    }

    public GestureOverlay(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        Setup();
    }

    public GestureOverlay(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        Setup();
    }

    private void Setup()
    {
        setZOrderOnTop(true);
        SurfaceHolder holder = this.getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSPARENT);
        this.setAlpha(ALPHA);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        m_surface = holder;
        m_isDrawing = true;
        m_drawingThread = new Thread(this);
        m_drawingThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        // Nothing to do here
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        m_isDrawing = false;
        if(m_drawingThread != null)
        {
            try
            {
                // allow it to finish drawing the current frame
                m_drawingThread.join();
                Clear();
            }
            catch (InterruptedException e)
            {
                Util.Fail("This shouldn't happen, not sure what a reasonable approach to handling this is..." + e.getMessage());
            }
            m_drawingThread = null;
        }
    }

    public void Clear()
    {
        try
        {
            m_syncLock.lock();
            m_pinchInfo = null;
            m_holdInfo = null;
            m_swipeInfo = null;
        }
        finally
        {
            m_syncLock.unlock();
        }
    }

    public void SetPinch(GestureHelper.PinchInfo pinchInfo)
    {
        try
        {
            m_syncLock.lock();
            m_pinchInfo = pinchInfo;
        }
        finally
        {
            m_syncLock.unlock();
        }
    }

    public void SetHold(GestureHelper.HoldInfo holdInfo)
    {
        try
        {
            m_syncLock.lock();
            m_holdInfo = holdInfo;
        }
        finally
        {
            m_syncLock.unlock();
        }
    }

    public void SetSwipe(GestureHelper.SwipeInfo swipeInfo)
    {
        try
        {
            m_syncLock.lock();
            m_swipeInfo = swipeInfo;
        }
        finally
        {
            m_syncLock.unlock();
        }
    }

    @Override
    public void run()
    {
        while(m_isDrawing)
        {
            // this is ugly but all it means is only draw if the surface is available
            // and the sync lock can be acquired (ie. no one is editing the stuff to be drawn)
            if(m_syncLock.tryLock())
            {
                try
                {
                    Canvas canvas = m_surface.lockCanvas();
                    if(canvas != null)
                    {
                        try
                        {
                            // clear canvas
                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                            if(m_holdInfo != null)
                            {
                                DrawHold(m_holdInfo, canvas);
                            }
                            else if(m_swipeInfo != null)
                            {
                                DrawSwipe(m_swipeInfo, canvas);
                            }
                            else if(m_pinchInfo != null)
                            {
                                DrawPinch(m_pinchInfo, canvas);
                            }
                        }
                        finally
                        {
                            m_surface.unlockCanvasAndPost(canvas);
                        }
                    }
                }
                finally
                {
                     m_syncLock.unlock();
                }
            }

            // then sleep for a bit
            try
            {
                Thread.sleep((long) (1000.0f / FPS));
            }
            catch (InterruptedException e)
            {
                // Don't care, just log it and move on
                Log.w(TAG, "Interrupted while sleeping!");
            }
        }
    }

    private static void DrawCircle(int x, int y, float radius, Canvas canvas)
    {
        canvas.drawCircle(x, y,
                radius,
                s_fill);

        canvas.drawCircle(x, y,
                radius,
                s_border);
    }

    private static void DrawPinch(GestureHelper.PinchInfo pinchInfo, Canvas canvas)
    {
        DrawCircle(pinchInfo.CurrentP1.x, pinchInfo.CurrentP1.y, FINGER_SIZE, canvas);
        DrawCircle(pinchInfo.CurrentP2.x, pinchInfo.CurrentP2.y, FINGER_SIZE, canvas);

        canvas.drawLine(pinchInfo.CurrentP1.x, pinchInfo.CurrentP1.y,
                pinchInfo.CurrentP2.x, pinchInfo.CurrentP2.y,
                s_line);

        Point curCenter = pinchInfo.Center();
        float radius = pinchInfo.Radius();

        canvas.drawCircle(curCenter.x, curCenter.y, radius, s_border);

        float angle = pinchInfo.Angle();
        angle = (float)Math.toDegrees(angle);
        angle = angle < -180.0f ? angle + 360.0f : angle;

        float originalAngle = Util.GetAngle(pinchInfo.StartP1, pinchInfo.StartP2);
        originalAngle = (float)Math.toDegrees(originalAngle);
        // this forces the arc to always draw to the top of the screen.
        originalAngle = originalAngle > 0.0f && originalAngle < 180.0f ? originalAngle - 180.0f : originalAngle;

        float left = curCenter.x - radius;
        float right = curCenter.x + radius;
        float top = curCenter.y - radius;
        float bottom = curCenter.y + radius;

        canvas.drawArc(new RectF(left, top, right, bottom), originalAngle, -angle, true, s_fill);
    }

    private static void DrawSwipe(GestureHelper.SwipeInfo swipeInfo, Canvas canvas)
    {
        canvas.drawLine(swipeInfo.StartPoint.x,
                swipeInfo.StartPoint.y,
                swipeInfo.CurrentPoint.x,
                swipeInfo.CurrentPoint.y,
                s_line);

        DrawCircle(swipeInfo.StartPoint.x,
                swipeInfo.StartPoint.y,
                FINGER_SIZE, canvas);

        DrawCircle(swipeInfo.CurrentPoint.x,
                swipeInfo.CurrentPoint.y,
                FINGER_SIZE, canvas);

        // TODO::JT strength
    }

    private static void DrawHold(GestureHelper.HoldInfo holdInfo, Canvas canvas)
    {
        DrawCircle(holdInfo.CenterPoint.x,
                holdInfo.CenterPoint.y,
                holdInfo.Radius(), canvas);
    }
}
