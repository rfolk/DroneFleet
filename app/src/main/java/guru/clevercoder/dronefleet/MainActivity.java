package guru.clevercoder.dronefleet;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;


public class MainActivity extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener
{

    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private LocationClient mLocationClient;

    // Define a request code to send to Google Play services This code is returned in Activity.onActivityResult
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;



    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        mLocationClient = new LocationClient( this, this, this );

        initializeMap();

        map.setMyLocationEnabled( true );
    }

    /**
      * Called when the Activity becomes visible.
      */
    @Override
    protected void onStart()
    {
        super.onStart();
        // Connect the client.
        if ( isGooglePlayServicesAvailable() )
        {
            mLocationClient.connect();
        }

    }

    /**
      * Called when the Activity is no longer visible.
      */
    @Override
    protected void onStop()
    {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }

    /**
      * Handle results returned to the FragmentActivity
      * by Google Play services
      */
    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data )
    {
        // Decide what to do based on the original request code
        switch ( requestCode )
        {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
            // If the result code is Activity.RESULT_OK, try to connect again
                switch ( resultCode )
                {
                    case Activity.RESULT_OK:
                        mLocationClient.connect();
                        break;
                }

        }
    }

    /**
      * Called by Location Services when the request to connect the
      * client finishes successfully. At this point, you can
      * request the current location or start periodic updates
      */
    @Override
    public void onConnected ( Bundle dataBundle )
    {
        // Display the connection status
        Toast.makeText( this, "Connected", Toast.LENGTH_SHORT ).show();
        Location location = mLocationClient.getLastLocation();
        LatLng latLng = new LatLng( location.getLatitude(), location.getLongitude() );
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom( latLng, 19 );
        map.animateCamera( cameraUpdate );
    }

    /**
      * Called by Location Services if the connection to the
      * location client drops because of an error.
      */
    @Override
    public void onDisconnected()
    {
        // Display the connection status
        Toast.makeText( this, "Disconnected. Reconnect.", Toast.LENGTH_SHORT ).show();
    }

    /**
      * Called by Location Services if the attempt to
      * Location Services fails.
      */
    @Override
    public void onConnectionFailed ( ConnectionResult connectionResult )
    {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if ( connectionResult.hasResolution() )
        {
            try
            {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult( this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */
            }
            catch ( IntentSender.SendIntentException e )
            {
                // Log the error
                e.printStackTrace();
            }
        }
        else
        {
            Toast.makeText( getApplicationContext(), "Location services are unavailable", Toast.LENGTH_LONG ).show();
        }
    }




    private void initializeMap()
    {
        if ( map == null )
        {
            mapFragment = ( ( SupportMapFragment ) getSupportFragmentManager().findFragmentById( R.id.google_map ) );
            map = mapFragment.getMap();

            if ( map == null )
            {
                Toast.makeText( getApplicationContext(), "Unable to create map.", Toast.LENGTH_SHORT ).show();
            }
        }
        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    }


    private boolean isGooglePlayServicesAvailable()
    {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable( this );
        // If Google Play services is available
        if ( ConnectionResult.SUCCESS == resultCode )
        {
            // In debug mode, log the status
            Log.d( "Location Updates", "Google Play Services are available." );
            return true;
        }
        else
        {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog( resultCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST );

            // If Google Play services can provide an error dialog
            if ( errorDialog != null )
            {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog( errorDialog );
                errorFragment.show( getSupportFragmentManager(), "Location Updates" );
            }
            return false;
        }
    }

}
