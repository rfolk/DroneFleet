package guru.clevercoder.dronefleet;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by Russell on 11/7/2014.
 */
public class MapWrapperLayout extends FrameLayout
{
    private OnDragListener mapOnDragListener;

    public MapWrapperLayout ( Context context )
    {
        super ( context );
    }

    public interface OnDragListener
    {
        public void OnDrag ( MotionEvent motionEvent );
    }

    @Override
    public boolean dispatchTouchEvent ( MotionEvent ev )
    {
        if ( mapOnDragListener != null )
        {
            mapOnDragListener.OnDrag( ev );
        }
        return super.dispatchTouchEvent( ev );
    }

    public void setOnDragListener ( OnDragListener mapOnDragListener )
    {
        this.mapOnDragListener = mapOnDragListener;
    }
}
