package guru.clevercoder.dronefleet;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


public class MainActivity extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener
{

    private GoogleMap map;
    public static boolean myMapIsTouched = false;
    private MySupportMapFragment mapFragment;
    Projection projection;
    public double latitude;
    public double longitude;
    public PolylineOptions flightPath;
    public Polyline renderPath;
    private LocationClient mLocationClient;
    private final MainActivity handle = this;
    public boolean running = false;
    // Define a request code to send to Google Play services This code is returned in Activity.onActivityResult
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public ArdroneAPI drone1;


    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        mLocationClient = new LocationClient( this, this, this );

        initializeMap();

        // Drone 1
        drone1 = new ArdroneAPI("192.168.1.1");

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
        drone1.connect ( );
    }

    /**
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop()
    {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        drone1.close ( );
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
        LatLng currentLocation = new LatLng( location.getLatitude(), location.getLongitude() );
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom( currentLocation, 29 );
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
            mapFragment = ( ( MySupportMapFragment ) getSupportFragmentManager().findFragmentById( R.id.google_map ) );
            map = mapFragment.getMap();

            if ( map == null )
            {
                Toast.makeText( getApplicationContext(), "Unable to create map.", Toast.LENGTH_SHORT ).show();
            }
        }
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        mapFragment.setOnDragListener( new MapWrapperLayout.OnDragListener() {

            @Override
            public void OnDrag ( MotionEvent motionEvent ) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        onDragStart ( motionEvent );
                        break;

                    case MotionEvent.ACTION_UP:
                        onDragStop ( );
                        break;

                    case MotionEvent.ACTION_MOVE:
                        onDragMove ( motionEvent );
                        break;
                }
            }

            public void onDragStart ( MotionEvent motionEvent ) {
                map.getUiSettings().setAllGesturesEnabled(false);
                if ( renderPath != null ) {
                    renderPath.remove();
                    flightPath = null;


                }

                flightPath = new PolylineOptions();

                float x = motionEvent.getX();
                float y = motionEvent.getY();

                Log.i("ON_DRAG", "X:" + String.valueOf(x));
                Log.i("ON_DRAG", "Y:" + String.valueOf(y));

                int x_co = Integer.parseInt(String.valueOf(Math.round(x)));
                int y_co = Integer.parseInt(String.valueOf(Math.round(y)));

                projection = map.getProjection();
                Point x_y_points = new Point(x_co, y_co);
                LatLng latLng = map.getProjection().fromScreenLocation(x_y_points);
                flightPath.add(latLng);

                latitude = latLng.latitude;
                longitude = latLng.longitude;

                Log.i("ON_DRAG", "lat:" + latitude);
                Log.i("ON_DRAG", "lon:" + longitude);



            }

            public void onDragMove ( MotionEvent motionEvent ) {
                float x = motionEvent.getX();
                float y = motionEvent.getY();

                Log.i("ON_DRAG", "X:" + String.valueOf(x));
                Log.i("ON_DRAG", "Y:" + String.valueOf(y));

                int x_co = Integer.parseInt(String.valueOf(Math.round(x)));
                int y_co = Integer.parseInt(String.valueOf(Math.round(y)));

                projection = map.getProjection();
                Point x_y_points = new Point(x_co, y_co);
                LatLng latLng = map.getProjection().fromScreenLocation(x_y_points);
                flightPath.add(latLng);

            }

            public void onDragStop ( ) {
                // Create polyline options with existing LatLng ArrayList
                flightPath
                        .width(5)
                        .color(Color.RED)
                        .visible(true)
                        .zIndex(1000);

                // Adding multiple points in map using polyline and arraylist
                renderPath = map.addPolyline(flightPath);
                map.getUiSettings().setAllGesturesEnabled(true);
            }
        });

        new Thread( new Runnable ( ) {
            public void run ( ) {
                try {
                    LatLng latLng = new LatLng(0,0);
                    Marker marker = handle.map.addMarker(new MarkerOptions().position(latLng)
                            .title("DRONE ====>(X)<====="));

                    while ( handle.running ) {

                        LatLng latlng = drone1.getGPS ( );
                        marker.setPosition ( latlng );
                        Thread.sleep(1000);
                    }
                } catch ( InterruptedException e ) {
                    System.err.println ( "ERROR" + e );
                }
            }

        }).start();
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
