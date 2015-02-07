package jetucker.cmput293assignment1;

import android.graphics.Point;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Jesse on 2015-02-06.\
 *
 * Note: I realize that there is a gesture recognizer in Android. However, I wanted
 * to try my hand at the touch interpretation myself as practice. Plus, near as I could
 * tell there was no built in support for pinch input.
 */
public final class GestureHelper implements View.OnTouchListener
{
    private float m_holdTime = 0.0f;
    private float m_swipeDistanceSquared = 0.0f;
    private IGestureListener m_listener;
    private boolean m_isTrackingGesture = false;

    private HoldInfo m_holdInfo = null;
    private SwipeInfo m_swipeInfo = null;
    private PinchInfo m_pinchInfo = null;

    private Point m_lastDownPoint = null;
    
    GestureHelper(float holdTime, float swipeDistance, IGestureListener gestureListener)
    {
        Util.Assert(holdTime > 0.0f);
        Util.Assert(swipeDistance > 0.0f);
        Util.Assert(gestureListener != null);

        m_holdTime = holdTime;
        m_swipeDistanceSquared = swipeDistance * swipeDistance;
        m_listener = gestureListener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent e)
    {
        int action = MotionEventCompat.getActionMasked(e);
        if(action == MotionEvent.ACTION_DOWN)
        {
            m_lastDownPoint = new Point((int)(e.getX(0)), (int)(e.getY(0)));
        }

        if(m_isTrackingGesture)
        {
            TrackGesture(e);
        }
        else
        {
            OnTouch(e);
        }

        // if any of them are not null we are tracking a gesture
        m_isTrackingGesture =   m_holdInfo != null ||
                                m_swipeInfo != null ||
                                m_pinchInfo != null;

        return true;
    }
    
    // if not tracking
    private void OnTouch(MotionEvent e)
    {
        Point currentPoint = new Point((int)(e.getX(0)), (int)(e.getY(0)));
        int distanceSqrdFromLastDown = Util.GetDistSquared(currentPoint, m_lastDownPoint);

        long timeDown = e.getEventTime() - e.getDownTime();
        if(timeDown > m_holdTime * 1000)
        {
            // pointer has been down long enough for it to be a hold
            m_holdInfo = new HoldInfo();
            m_holdInfo.centerPoint = new Point();
            m_holdInfo.centerPoint.x = (int)(e.getX());
            m_holdInfo.centerPoint.y = (int)(e.getY());
            m_holdInfo.timeHeld = timeDown;

            m_listener.OnHoldStart(m_holdInfo);
        }
        else if(e.getPointerCount() == 2)
        {
            // 2 pointers means a pinch
            m_pinchInfo = new PinchInfo();
            m_pinchInfo.CurrentP1 = currentPoint;
            m_pinchInfo.CurrentP2 = new Point((int)(e.getX(1)), (int)(e.getY(1)));
            m_pinchInfo.StartP1 = m_pinchInfo.CurrentP1;
            m_pinchInfo.StartP2 = m_pinchInfo.CurrentP2;

            m_listener.OnPinchStart(m_pinchInfo);
        }
        else if(distanceSqrdFromLastDown > m_swipeDistanceSquared)
        {
            // moved far enough for it to be a swipe
            // Note: I don't care how long this takes to start, velocity is not important
            m_swipeInfo = new SwipeInfo();
            m_swipeInfo.CurrentPoint = currentPoint;
            m_swipeInfo.StartPoint = m_lastDownPoint;

            m_listener.OnSwipeStart(m_swipeInfo);
        }
    }
    
    private void TrackGesture(MotionEvent e)
    {
        if(m_holdInfo != null)
        {
            TrackHold(e);
        }
        else if(m_swipeInfo != null)
        {
            TrackSwipe(e);
        }
        else if(m_pinchInfo != null)
        {
            TrackPinch(e);
        }
    }

    private void TrackHold(MotionEvent e)
    {
        int action = MotionEventCompat.getActionMasked(e);
        Point currentPoint = new Point((int)(e.getX(0)), (int)(e.getY(0)));

        if(action == MotionEvent.ACTION_MOVE)
        {
            m_holdInfo.timeHeld = e.getEventTime() - e.getDownTime();
            m_listener.OnHoldContinue(m_holdInfo);
        }
        else if(action == MotionEvent.ACTION_UP)
        {
            m_listener.OnHoldEnd(m_holdInfo);
            m_holdInfo = null;
        }
    }

    private void TrackSwipe(MotionEvent e)
    {
        int action = MotionEventCompat.getActionMasked(e);

        if(action == MotionEvent.ACTION_MOVE)
        {
            m_swipeInfo.CurrentPoint = new Point((int)(e.getX(0)), (int)(e.getY(0)));
            m_listener.OnSwipeMove(m_swipeInfo);
        }
        else if(action == MotionEvent.ACTION_UP)
        {
            m_listener.OnSwipeEnd(m_swipeInfo);
            m_swipeInfo = null;
        }
    }

    private void TrackPinch(MotionEvent e)
    {
        int action = MotionEventCompat.getActionMasked(e);

        if(e.getPointerCount() >= 1)
        {
            m_pinchInfo.CurrentP1 = new Point((int)(e.getX(0)), (int)(e.getY(0)));
        }
        if(e.getPointerCount() >= 2)
        {
            m_pinchInfo.CurrentP2 = new Point((int)(e.getX(1)), (int)(e.getY(1)));
        }

        if(action == MotionEvent.ACTION_MOVE)
        {
            m_listener.OnPinchContinue(m_pinchInfo);
        }
        else if(action == MotionEvent.ACTION_UP || e.getPointerCount() < 2)
        {
            m_listener.OnPinchEnd(m_pinchInfo);
            m_pinchInfo = null;
        }
    }

    class HoldInfo
    {
        public long timeHeld = 0; // milliseconds
        public Point centerPoint;
    }

    class PinchInfo
    {
        public Point CurrentP1;
        public Point CurrentP2;
        public Point StartP1;
        public Point StartP2;
    }

    class SwipeInfo
    {
        public Point StartPoint;
        public Point CurrentPoint;
    }

    interface IGestureListener
    {
        void OnHoldStart(HoldInfo holdInfo);
        void OnHoldContinue(HoldInfo holdInfo);
        void OnHoldEnd(HoldInfo holdInfo);

        void OnPinchStart(PinchInfo pinchInfo);
        void OnPinchContinue(PinchInfo pinchInfo);
        void OnPinchEnd(PinchInfo pinchInfo);

        void OnSwipeStart(SwipeInfo swipeInfo);
        void OnSwipeMove(SwipeInfo swipeInfo);
        void OnSwipeEnd(SwipeInfo swipeInfo);

        void Cancel();
    }
}
