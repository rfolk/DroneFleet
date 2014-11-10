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
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;


public class MapScreen extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        View.OnClickListener
{

    private GoogleMap map;
    public static boolean myMapIsTouched = false;
    private MySupportMapFragment mapFragment;
    Projection projection;
    public double latitude;
    public double longitude;
    public PolylineOptions flightPathLine;
    public Polyline renderPathLine;
    public PolygonOptions flightPathShape;
    public Polygon renderPathShape;
    private LocationClient mLocationClient;
    private final MapScreen handle = this;
    public boolean running = false;
    // Define a request code to send to Google Play services This code is returned in Activity.onActivityResult
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public ArdroneAPI drone1;
    public Marker marker;

    private boolean mapIsDrawable;
    private boolean drawingLine;
    private boolean drawingShape;
    private Button drawLine;
    private Button drawShape;
    private Button go;
    private Bundle extras;
    private String resultingFrom;

    private ArrayList<LatLng> coordinates;


    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.map_screen);

        extras = getIntent().getExtras();
        if ( extras != null )
        {
            resultingFrom = extras.getString("buttonPressed");
        }

        mLocationClient = new LocationClient( this, this, this );

        initializeMap();

        // Drone 1
        //drone1 = new ArdroneAPI("192.168.1.1");

        map.setMyLocationEnabled( true );

        mapIsDrawable = false;
        drawingLine = false;
        drawingShape = false;
        drawLine = ( Button ) findViewById( R.id.btn_drawLine);
        drawLine.setOnClickListener( this );
        drawShape = ( Button ) findViewById( R.id.btn_drawShape );
        drawShape.setOnClickListener( this );
        go = ( Button ) findViewById( R.id.btn_go );
        go.setOnClickListener( this );
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
        /*new Thread( new Runnable() {
            @Override
            public void run() {
                handle.drone1.connect ();
            }
        }).start();*/
    }

    /**
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop()
    {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        //drone1.close ( );
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


    /**
      * Track the click of buttons
      *
      * @param view
      */
    @Override
    public void onClick ( View view )
    {
        switch ( view.getId() )
        {
            case R.id.btn_drawLine:
                if ( mapIsDrawable == false )
                {
                    mapIsDrawable = true;
                    drawingLine = true;
                    map.getUiSettings().setAllGesturesEnabled(false);
                }
                else
                {
                    mapIsDrawable = false;
                    drawingLine = false;
                    map.getUiSettings().setAllGesturesEnabled(true);
                }
            break;
            case R.id.btn_drawShape:
                if ( mapIsDrawable == false )
                {
                    mapIsDrawable = true;
                    drawingShape = true;
                    map.getUiSettings().setAllGesturesEnabled(false);
                }
                else
                {
                    mapIsDrawable = false;
                    drawingShape = false;
                    map.getUiSettings().setAllGesturesEnabled(true);
                }
                break;
            case R.id.btn_go:
                Intent intent = new Intent( this, GoScreen.class );
                startActivity( intent );
                finish();
            break;
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
                if ( handle.mapIsDrawable )
                {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            onDragStart(motionEvent);
                            break;

                        case MotionEvent.ACTION_UP:
                            onDragStop();
                            break;

                        case MotionEvent.ACTION_MOVE:
                            onDragMove(motionEvent);
                            break;
                    }
                }
            }

            public void onDragStart ( MotionEvent motionEvent ) {
                if ( renderPathLine != null )
                {
                    renderPathLine.remove();
                    flightPathLine = null;
                    coordinates = null;
                }
                if ( renderPathShape != null )
                {
                    renderPathShape.remove();
                    flightPathShape = null;
                    coordinates = null;
                }

                flightPathLine = new PolylineOptions();
                flightPathShape = new PolygonOptions();
                coordinates = new ArrayList<LatLng>();

                float x = motionEvent.getX();
                float y = motionEvent.getY();

                Log.i("ON_DRAG", "X:" + String.valueOf(x));
                Log.i("ON_DRAG", "Y:" + String.valueOf(y));

                int x_co = Integer.parseInt(String.valueOf(Math.round(x)));
                int y_co = Integer.parseInt(String.valueOf(Math.round(y)));

                projection = map.getProjection();
                Point x_y_points = new Point(x_co, y_co);
                LatLng latLng = map.getProjection().fromScreenLocation(x_y_points);
                if ( drawingLine )
                {
                    flightPathLine.add( latLng );
                }
                else if ( drawingShape )
                {
                    flightPathShape.add( latLng );
                }
                coordinates.add(latLng);

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
                if ( drawingLine )
                {
                    flightPathLine.add( latLng );
                }
                else if ( drawingShape )
                {
                    flightPathShape.add( latLng );
                }
                coordinates.add(latLng);

            }

            public void onDragStop ( ) {
                // Create polyline options with existing LatLng ArrayList
                if ( drawingLine )
                {
                    flightPathLine
                            .width(5)
                            .color(Color.RED)
                            .visible(true)
                            .zIndex(1000);
                    renderPathLine = map.addPolyline(flightPathLine);
                }
                else if ( drawingShape )
                {
                    flightPathShape
                            .strokeWidth(5)
                            .strokeColor(Color.RED)
                            .visible(true)
                            .zIndex(1000);
                    renderPathShape = map.addPolygon(flightPathShape);
                }
                Log.i("Array for Points Size:", " " + String.valueOf(coordinates.size()));
                if ( extras != null )
                {
                    Log.i("Came from:", resultingFrom);
                }

                // Adding multiple points in map using polyline and arraylist

            }
        });

        LatLng latLng = new LatLng(0,0);
        marker = handle.map.addMarker(new MarkerOptions().position(latLng)
                .title("DRONE =====>(X)<====="));
        new Thread( new Runnable ( ) {
            public void run ( ) {
                try {

                    while ( handle.running ) {

                        LatLng latlng = drone1.getGPS ( );
                        handle.marker.setPosition ( latlng );
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
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
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
