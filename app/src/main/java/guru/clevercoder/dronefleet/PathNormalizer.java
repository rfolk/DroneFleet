package guru.clevercoder.dronefleet;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/**
 * Created by frankyn on 12/2/14.
 */
public class PathNormalizer {
    // If you want more points you can change this distance parameter
    final static double leastViableDistance = (1E-4)/2;

    private void distanceBetweenPointsShape(ArrayList<LatLng> coordinates) {
        for ( int i = 0; i < coordinates.size() - 1; ++i ) {
            int v2 = i + 1;
            Log.d("ED (" + i + "," + v2 + "): ", "" + distanceBetweenPoints(coordinates.get(i), coordinates.get(i + 1)));
        }
        Log.d("ED ("+ coordinates.size() + "," + 0 + "): ", "" + distanceBetweenPoints(coordinates.get(coordinates.size() - 1), coordinates.get(0)));
    }

    private void distanceBetweenPointsLine(ArrayList<LatLng> coordinates) {
        for ( int i = 0; i < coordinates.size() - 1; ++i ) {
            int v2 = i + 1;
            Log.d("ED ("+ i + "," + v2 + "): ", "" + distanceBetweenPoints(coordinates.get(i), coordinates.get(i + 1)));
        }
    }

    // General Purpose
    public double distanceBetweenPoints( LatLng p1, LatLng p2 ) {
        return Math.sqrt(Math.pow((p2.latitude - p1.latitude), 2) + Math.pow((p2.longitude - p1.longitude), 2));
    }

    public ArrayList<LatLng> normalizeShape(ArrayList<LatLng> coordinates) {
        //normShapeOpts.add( coordinates.get( 0 ) );
        ArrayList<LatLng> normCoordinates = new ArrayList<LatLng>();
        ArrayList<LatLng> temp = new ArrayList<LatLng>();
        temp.add( coordinates.get( 0 ) );
        normCoordinates.add( coordinates.get( 0 ) );
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
                normCoordinates.add( coordinates.get( i ) );
            }
        }
        /*
        for ( int i = 0; i < temp.size(); ++i )
        {
            normShapeOpts.add( temp.get( i ) );
        }
        normShapeOpts
                .strokeWidth(2)
                .strokeColor(Color.BLUE)
                .visible(true)
                .zIndex(1001);
        normShape = map.addPolygon( normShapeOpts );
        */

        return normCoordinates;
    }

    public ArrayList<LatLng> normalizeLine(ArrayList<LatLng> coordinates) {

        //PolylineOptions normLineOpts = new PolylineOptions();
        //normLineOpts.add( coordinates.get( 0 ) );

        ArrayList<LatLng> normCoordinates = new ArrayList<LatLng>();
        ArrayList<LatLng> temp = new ArrayList<LatLng>();
        temp.add( coordinates.get( 0 ) );
        normCoordinates.add( coordinates.get( 0 ) );
        boolean addPoint;
        for ( int i = 1; i < coordinates.size() - 1; ++i ) {
            addPoint = true;
            for ( int j = 0; j < temp.size(); ++j ) {
                if ( distanceBetweenPoints(coordinates.get(i), temp.get(j)) < leastViableDistance ) {
                    addPoint = false;
                }
            }
            if ( addPoint == true ) {
                temp.add( coordinates.get( i ) );
                normCoordinates.add( coordinates.get( i ) );
            }
        }
        /*
        for ( int i = 0; i < temp.size(); ++i )
        {
            normLineOpts.add( temp.get( i ) );
        }
        normLineOpts
                .width(2)
                .color(Color.BLUE)
                .visible(true)
                .zIndex(1001);
        normLine = map.addPolyline( normLineOpts );
        */

        return normCoordinates;
    }
}
