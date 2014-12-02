package guru.clevercoder.dronefleet;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by frankyn on 12/2/14.
 */
public class SimpleFlightCoordination {

        public ArrayList<ArrayList<LatLng>> generateFlightPlan ( ArrayList<ArdroneAPI> drones, ArrayList<LatLng> mapPoints) {
            // The array of arraylists that will contain the point assignments for each drone
            ArrayList< ArrayList<LatLng> > droneAssignments = new ArrayList<ArrayList<LatLng> >(drones.size());
            int totalPoints = mapPoints.size();
            int pointLength = totalPoints/drones.size();

            for ( int i = 0, offset = 0 ; i < drones.size(); ++ i ) {
                // Begin flightplan for Drone 'i'
                droneAssignments.add(new ArrayList<LatLng>());

                // Add points into Drone 'i's flightplan
                for(int b = 0; b < pointLength ; ++b, ++offset ) {
                    droneAssignments.get(i).add(mapPoints.get(offset));
                }
            }

            return droneAssignments;
        }

}
