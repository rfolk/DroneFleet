package guru.clevercoder.dronefleet;

import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.DialogInterface;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.ArrayList;


public class MapScreen extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        View.OnClickListener,
        ArdroneAPICallbacks
{

    private GoogleMap map;
    public static boolean myMapIsTouched = false;
    private MySupportMapFragment mapFragment;
    Projection projection;
    public double latitude;
    public double longitude;
    public PolylineOptions flightPathLine;
    public Polyline renderPathLine;
    public PolylineOptions normLineOpts;
    public Polyline normLine;
    public PolygonOptions flightPathShape;
    public Polygon renderPathShape;
    public PolygonOptions normShapeOpts;
    public Polygon normShape;
    private LocationClient mLocationClient;
    private final MapScreen handle = this;
    public boolean running = false;
    // Define a request code to send to Google Play services This code is returned in Activity.onActivityResult
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public ArrayList<Marker> droneMarkers;
    public ArrayList<ArdroneAPI> drones;

    private boolean mapIsDrawable;
    private boolean drawingLine;
    private boolean drawingShape;
    private Button drawLine;
    private Button drawShape;
    private Button go;
    private Bundle extras;
    private String resultingFrom;

    private ArrayList<LatLng> coordinates;
    private ArrayList<LatLng> normCoordinates;

    private int connectedDrones = 0;
    private int flightPlansReady = 0;

    private double leastViableDistance = (1E-5)/2;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.map_screen );

        mLocationClient = new LocationClient( this, this, this );

        initializeMap();

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

    // Initialize real drones and prepare them with provided path
    private void initReal ( ) {
        drones = new ArrayList<ArdroneAPI>();
        droneMarkers = new ArrayList<Marker>();

        // Initialize a drone object per drone
        drones.add( new ArdroneAPI("192.168.43.4", this ) );
        droneMarkers.add(handle.map.addMarker(new MarkerOptions().position(new LatLng(0, 0))
                .title(drones.get(0).toString())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.dronemarker))));

        drones.add( new ArdroneAPI("192.168.43.5", this ) );
        droneMarkers.add(handle.map.addMarker(new MarkerOptions().position(new LatLng(0,0))
                .title(drones.get(1).toString())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.dronemarker))));

        final ArrayList<ArdroneAPI> dronesStatic = drones;
        // Connect all drones
        new Thread( new Runnable() {
            public void run ( ) {
                for( int i = 0 ; i < dronesStatic.size() ; i ++ ) {
                    dronesStatic.get(i).connect();
                }
            }
        }).start();

    }

    private void initSim ( ) {
       // Init # of drones specified.

       // Wait for ID's associated with drone communication

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
                if ( mapIsDrawable == false ) {
                    CharSequence colors[] = new CharSequence[] {"Real Drones", "Simulator"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Environment");
                    builder.setItems(colors, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        // the user clicked on colors[which]
                            switch ( which ) {
                                case 1:
                                    initSim();
                                break;
                                case 0:
                                    initReal();
                                break;
                            }
                        }
                    });
                    builder.show();

                }
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
                    Log.i("Drag", "STARTED");
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
                    normLine.remove();
                    flightPathLine = null;
                    coordinates = null;
                    normCoordinates = null;
                }
                if ( renderPathShape != null )
                {
                    renderPathShape.remove();
                    normShape.remove();
                    flightPathShape = null;
                    coordinates = null;
                    normCoordinates = null;
                }

                flightPathLine = new PolylineOptions();
                normLineOpts = new PolylineOptions();
                flightPathShape = new PolygonOptions();
                normShapeOpts = new PolygonOptions();
                coordinates = new ArrayList<LatLng>();
                normCoordinates = new ArrayList<LatLng>();

                float x = motionEvent.getX();
                float y = motionEvent.getY();

                //Log.i("ON_DRAG", "X:" + String.valueOf(x));
                //Log.i("ON_DRAG", "Y:" + String.valueOf(y));

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

                //Log.i("ON_DRAG", "X:" + String.valueOf(x));
                //Log.i("ON_DRAG", "Y:" + String.valueOf(y));

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
                    distanceBetweenPointsLine();
                    normalizeLine();
                }
                else if ( drawingShape )
                {
                    flightPathShape.add(coordinates.get(0));
                    coordinates.add(coordinates.get(0));
                    flightPathShape
                            .strokeWidth(5)
                            .strokeColor(Color.RED)
                            .visible(true)
                            .zIndex(1000);
                    renderPathShape = map.addPolygon(flightPathShape);
                    distanceBetweenPointsShape();
                    normalizeShape();
                }
                Log.i("Array for Points Size:", " " + String.valueOf(coordinates.size()));
                if ( extras != null )
                {
                    //Log.i("Came from:", resultingFrom);
                }

                // Adding multiple points in map using polyline and arraylist

            }
        });
    }


    private boolean isGooglePlayServicesAvailable()
    {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if ( ConnectionResult.SUCCESS == resultCode )
        {
            // In debug mode, log the status
            //Log.d( "Location Updates", "Google Play Services are available." );
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

    public void coordinateFlight ( ) {
        Auction coordAuction = new Auction ( );
        flightPlansReady = 0;
        coordinates = new ArrayList<LatLng>();


        /* Large Field
        coordinates.add(new LatLng(32.279401,-106.746412));
        coordinates.add(new LatLng(32.279412,-106.746308));
        coordinates.add(new LatLng(32.27938,-106.746246));
        coordinates.add(new LatLng(32.279335,-106.746224));
        coordinates.add(new LatLng(32.279283,-106.746216));
        coordinates.add(new LatLng(32.279228,-106.746219));
        coordinates.add(new LatLng(32.279183,-106.746251));
        coordinates.add(new LatLng(32.279165,-106.746326));
        coordinates.add(new LatLng(32.279181,-106.746399));
        coordinates.add(new LatLng(32.279226,-106.746493));
        coordinates.add(new LatLng(32.279285,-106.746587));
        coordinates.add(new LatLng(32.279355,-106.74667));
        coordinates.add(new LatLng(32.279392,-106.746581));
        coordinates.add(new LatLng(32.279385,-106.746506));
        */

        /* Small Field Next to CS Dept. */
        coordinates.add(new LatLng(32.280979,-106.752577));
        coordinates.add(new LatLng(32.280978,-106.752541));
        coordinates.add(new LatLng(32.280869,-106.752542));
        coordinates.add(new LatLng(32.280859,-106.752587));
        coordinates.add(new LatLng(32.280876,-106.752635));
        coordinates.add(new LatLng(32.280916,-106.752668));
        coordinates.add(new LatLng(32.28095,-106.752659));
        coordinates.add(new LatLng(32.280971,-106.75262));

        ArrayList< ArrayList<LatLng> > flightPlans = coordAuction.auctionPoints ( drones , normCoordinates );

        for ( int i = 0 ; i < drones.size ( ) ; ++ i ) {
            drones.get(i).buildFlightPlan(flightPlans.get(i));
        }
    }

    // All flight plans are now set and ready to start
    public void startFlightPlan ( ) {
        for ( int i = 0 ; i < drones.size ( ) ; ++ i ) {
            drones.get(i).startFlightPlan();
        }
    }

    public void onDroneConnect(  ArdroneAPI drone ) {
        Log.i( "DroneMessage" , "Connnection Opened" );
        connectedDrones ++;
        // Wait for all drones to connect
        if ( connectedDrones == drones.size ( ) ) {
            coordinateFlight();
        }
    }

    public void onDroneDisconnect ( ArdroneAPI drone ) {
        Log.i( "DroneMessage" , "Connection Closed" );
    }

    public void onFlightPlanReady ( ArdroneAPI drone ) {
        Log.i( "DroneMessage" , "FlightPlan ready to begin" );
        flightPlansReady ++;

        // Wait for all drones to be ready with their FlightPlan
        if ( flightPlansReady == drones.size() ) {
            startFlightPlan();
        }
    }

    public void onFlightPlanComplete ( ArdroneAPI drone ) {
        // Completed
        Log.i( "DroneMessage" , "Finished Flight Plan" );
    }

    public void onFlightPlanError ( ArdroneAPI drone , String why ) {

    }

    public void onMissionEvent ( ArdroneAPI drone , MISSION_EVENTS event ) {
        switch ( event ) {
            case LANDING:
                // Finished Plan
                Log.i( "DroneMessage" , "Landing" );
            break;
            case WAY_POINT_REACHED:
                // Reached a Way Point
                Log.i( "DroneMessage" , "Reached a way point" );
            break;
        }
    }

    public void updateGPSMarkers ( ) {
        try {
            for (int i = 0; i < drones.size(); ++i) {
                droneMarkers.get(i).setPosition(drones.get(i).getCurrentCoord());
            }
        } finally {
            Log.i ("DroneMessage" , "Error retrieving GPS" );
        }
    }

    public void onDroneGPS ( ArdroneAPI drone ) {
        try {
            runOnUiThread(new Runnable ( ) {
                public void run ( ) {
                    handle.updateGPSMarkers();
                }
            });
        } catch ( Exception e ) {
            Log.i ( "DroneMessage" , "ERROR in GPS::"+e );
        } finally {
            Log.i ( "DroneMessage" , "Error in GPS" );
        }
     }

    private void distanceBetweenPointsShape()
    {
        for ( int i = 0; i < coordinates.size() - 1; ++i )
        {
            int v2 = i + 1;
            Log.d("ED ("+ i + "," + v2 + "): ", "" + distanceBetweenPoints(coordinates.get(i), coordinates.get(i + 1)));
        }
        Log.d("ED ("+ coordinates.size() + "," + 0 + "): ", "" + distanceBetweenPoints(coordinates.get(coordinates.size() - 1), coordinates.get(0)));
    }

    private void distanceBetweenPointsLine()
    {
        for ( int i = 0; i < coordinates.size() - 1; ++i )
        {
            int v2 = i + 1;
            Log.d("ED ("+ i + "," + v2 + "): ", "" + distanceBetweenPoints(coordinates.get(i), coordinates.get(i + 1)));
        }
    }

    private double distanceBetweenPoints( LatLng p1, LatLng p2 )
    {
        return Math.sqrt(Math.pow((p2.latitude - p1.latitude), 2) + Math.pow((p2.longitude - p1.longitude), 2));
    }

    private void normalizeShape()
    {
        normShapeOpts.add( coordinates.get( 0 ) );
        ArrayList<LatLng> temp = new ArrayList<LatLng>();
        temp.add( coordinates.get( 0 ) );
        boolean addPoint;
        for ( int i = 1; i < coordinates.size() - 1; ++i )
        {
            addPoint = true;
            for ( int j = 0; j < temp.size(); ++j )
            {
                if ( distanceBetweenPoints(coordinates.get(i), temp.get(j)) < leastViableDistance )
                {
                    addPoint = false;
                }
            }
            if ( addPoint == true )
            {
                temp.add( coordinates.get( i ) );
            }
        }
        for ( int i = 1; i < temp.size(); ++i )
        {
            normShapeOpts.add( temp.get( i ) );
            normCoordinates.add( temp.get( i ) );
        }
        normShapeOpts
                .strokeWidth(2)
                .strokeColor(Color.BLUE)
                .visible(true)
                .zIndex(1001);
        normShape = map.addPolygon( normShapeOpts );
    }

    private void normalizeLine()
    {
        normLineOpts.add( coordinates.get( 0 ) );
        ArrayList<LatLng> temp = new ArrayList<LatLng>();
        temp.add( coordinates.get( 0 ) );
        boolean addPoint;
        for ( int i = 1; i < coordinates.size() - 1; ++i )
        {
            addPoint = true;
            for ( int j = 0; j < temp.size(); ++j )
            {
                if ( distanceBetweenPoints(coordinates.get(i), temp.get(j)) < leastViableDistance )
                {
                    addPoint = false;
                }
            }
            if ( addPoint == true )
            {
                temp.add( coordinates.get( i ) );
                normCoordinates.add( temp.get( i ) );
            }
        }
        for ( int i = 1; i < temp.size(); ++i )
        {
            normLineOpts.add( temp.get( i ) );
        }
        normLineOpts
                .width(2)
                .color(Color.BLUE)
                .visible(true)
                .zIndex(1001);
        normLine = map.addPolyline( normLineOpts );
    }
}
