package jetucker.cmput293assignment1;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Jesse on 2015-02-08.
 *
 * Wrapper around UndoSystem to allow it to be accessed async
 */
public final class AsyncUndo
{
    private UndoSystem m_undoSystem = null;
    private Lock m_syncLock = new ReentrantLock();
    private Queue<Bitmap> m_pendingBitmapsForWrite = new LinkedList<>();
    private PopImageCallback m_callback = null;
    private UndoPushTask m_undoTask = null;
    private boolean m_isClearPending = false;
    private AllWorkCompleteListener m_listener = null;

    public AsyncUndo(UndoSystem undoSystem, AllWorkCompleteListener listener)
    {
        m_undoSystem = undoSystem;
        m_listener = listener;
    }

    public void PushImage(Bitmap bmp)
    {
        try
        {
            m_syncLock.lock();

            if(m_undoTask == null)
            {
                m_undoTask = new UndoPushTask(bmp);
                m_undoTask.execute();
            }
            else
            {
                m_pendingBitmapsForWrite.add(bmp);
            }
        }
        finally
        {
            m_syncLock.unlock();
        }
    }

    public void Clear()
    {
        try
        {
            m_syncLock.lock();
            if(m_undoTask == null)
            {
                m_undoSystem.Clear();
            }
            else
            {
                m_isClearPending = true;
            }
        }
        finally
        {
            m_syncLock.unlock();
        }
    }

    public void PopImage(PopImageCallback callback)
    {
        try
        {
            m_syncLock.lock();

            if(m_undoTask == null)
            {
                // we can just callback right now
                callback.ImageReady(m_undoSystem.PopImage());
            }
            else
            {
                // save the callback for later
                m_callback = callback;
            }
        }
        finally
        {
            m_syncLock.unlock();
        }
    }

    public int GetUndoAvailable()
    {
        // screw it... this might not always be accurate cuz of race conditions
        return m_undoSystem.GetUndoAvailable();
    }

    interface AllWorkCompleteListener
    {
        void OnWorkComplete();
    }

    interface PopImageCallback
    {
        void ImageReady(Bitmap bmp);
    }

    private class UndoPushTask extends AsyncTask<Void, Void, Void>
    {
        Bitmap m_bmp;

        UndoPushTask(Bitmap bmp)
        {
            m_bmp = bmp;
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            m_undoSystem.AddImage(m_bmp);

            return null;
        }

        @Override
        protected void onPostExecute(Void v)
        {
            try
            {
                m_syncLock.lock();

                m_undoTask = null;

                if(m_pendingBitmapsForWrite.size() > 0)
                {
                    m_undoTask = new UndoPushTask(m_pendingBitmapsForWrite.remove());
                    m_undoTask.execute();
                }
                else
                {
                    // all undo's are stored
                    if(m_callback != null)
                    {
                        // we have a callback pending, call it
                        m_callback.ImageReady(m_undoSystem.PopImage());
                        m_callback = null; // call at most once
                    }

                    m_listener.OnWorkComplete();
                }

                if(m_isClearPending)
                {
                    m_undoSystem.Clear();
                    m_isClearPending = false;
                }
            }
            finally
            {
                m_syncLock.unlock();
            }
        }
    }
}
