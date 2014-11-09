package guru.clevercoder.dronefleet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by Russell on 11/7/2014.
 */
public class MySupportMapFragment extends SupportMapFragment
{
    public View myOriginalContentView;
    public MapWrapperLayout myMapWrapperLayout;

    @Override
    public View onCreateView ( LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState )
    {
        myOriginalContentView = super.onCreateView( inflater, parent, savedInstanceState );
        myMapWrapperLayout = new MapWrapperLayout( getActivity() );
        myMapWrapperLayout.addView( myOriginalContentView );
        return myMapWrapperLayout;
    }

    @Override
    public View getView()
    {
        return myOriginalContentView;
    }

    public void setOnDragListener ( MapWrapperLayout.OnDragListener onDragListener )
    {
        myMapWrapperLayout.setOnDragListener( onDragListener );
    }
}
