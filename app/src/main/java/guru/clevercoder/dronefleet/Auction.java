package guru.clevercoder.dronefleet;

/**
 * Created by frankyn on 11/11/14.
 */
import java.lang.Double;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.android.gms.maps.model.LatLng;
import android.util.Log;

public class Auction {

    public double totalDistance ( ArrayList<LatLng> points ) {
        double totalDistanceValue = 0;

        for ( int p = 0 ; p < points.size(); p++ ) {
            totalDistanceValue += calculateDistanceToPoint ( points.get(p),  points.get(((p+1)%points.size())) );
        }

        return totalDistanceValue;
    }

    public ArrayList<LatLng> getInitPoints ( ArrayList<ArdroneAPI> drones , ArrayList<LatLng> mapPoints ) {

        ArrayList<LatLng> initPoints = new ArrayList<LatLng>(drones.size());
        while(initPoints.size() < drones.size() ) initPoints.add(new LatLng(0,0));

        int minDrone = 0; // overall
        int minPoint = 0;
        double minDistance = Double.MAX_VALUE; // overall


        for ( int d = 0 ; d < drones.size(); d++ ) {
            for ( int p = 0 ; p < mapPoints.size(); p++ ) {
                double dist = calculateDistanceToPoint ( drones.get(d).getCurrentCoord(),  mapPoints.get(p) );
                if ( dist < minDistance ) {
                    minDrone = d;
                    minPoint = p;
                    minDistance = dist;
                }
            }
        }

        // Set the global minimum point to specific drone
        initPoints.set(minDrone,mapPoints.get(minPoint));

        // Find the other init points
        int initPointsFound = 1; // found one in the code above.
        int lastIndex = minPoint;

        // Used to equally space drones
        // Contains normalized values for the amount of points and drones.
       // double totalDistanceValue = totalDistance(mapPoints);
        int intermediateSpace = mapPoints.size()/drones.size();

        // Cycle through all drones from wherever the index is positioned.
        for ( int droneIndex = ((minDrone+1)%drones.size()) ; initPointsFound < drones.size() ; droneIndex=((++droneIndex)%drones.size()), initPointsFound++ ) {
            initPoints.set( droneIndex, mapPoints.get((lastIndex+intermediateSpace)%mapPoints.size()) );
        }

        return initPoints;
    }

    public ArrayList<ArrayList<LatLng>> auctionPoints ( ArrayList<ArdroneAPI> drones, ArrayList<LatLng> mapPoints) {
        ArrayList<LatLng> initPoints = getInitPoints ( drones, mapPoints );
        ArrayList<LatLng> points = new ArrayList<LatLng>(mapPoints);

        // The array of arraylists that will contain the point assignments for each drone
        ArrayList< ArrayList<LatLng> > droneAssignments = new ArrayList<ArrayList<LatLng> >(drones.size());
        while(droneAssignments.size() < drones.size() ) droneAssignments.add(new ArrayList<LatLng>());

        double [] droneCurrentTotalCost = new double[drones.size()];
        Arrays.fill(droneCurrentTotalCost, 0);


        // Assuming preprocessing has already been completed, assign each drone their initPoints
        // as their first assignment.
        // The point located clockwise from the initPoint will also be assigned to each drone to
        // ensure that all drones will travel in the same direction to avoid collision.
        for (int d = 0; d < drones.size(); d++) {
            LatLng droneInitPoint = initPoints.get(d);
            Log.i("TAG",""+droneInitPoint);
            LatLng droneDirectionPoint = points.get((points.indexOf(droneInitPoint) + 1) % points.size());

            assignPoint(d, droneAssignments.get(d), droneInitPoint, points, droneCurrentTotalCost, 0);
            assignPoint(d, droneAssignments.get(d), droneDirectionPoint, points, droneCurrentTotalCost,
                    calculateDistanceToPoint(droneInitPoint, droneDirectionPoint));
        }

        int totalRounds = points.size();
        // There are n auction rounds, where n is the number of points.
        for (int round = 1; round <= totalRounds; round++) {

            double globalMinBid = Double.MAX_VALUE;
            LatLng globalMinBidPoint = null;
            int droneWithGlobalMinBid = -1;
            // During each round, each drone bids on all points that have not yet been assigned.
            for (int d = 0; d < drones.size(); d++) {

                double minBid = Double.MAX_VALUE;
                LatLng minBidPoint = null;
                // Each bid consists of calculating the distance to each point.
                for (int p = 0; p < points.size(); p++) {
                    LatLng bidPoint = points.get(p);
                    double bid = bid(d, droneAssignments.get(d), points.get(p), droneCurrentTotalCost);

                    // We really only need to consider the point with the minimum bid, because there
                    // is only one point assigned each round.
                    if (bid < minBid) {
                        minBidPoint = bidPoint;
                        minBid = bid;
                    }
                } // End p

                if (minBid < globalMinBid) {
                    globalMinBid = minBid;
                    droneWithGlobalMinBid = d;
                    globalMinBidPoint = minBidPoint;
                }
            } // End drone

            // Finally assign one point to the drone with the lowest bid.
            assignPoint(droneWithGlobalMinBid, droneAssignments.get(droneWithGlobalMinBid), globalMinBidPoint, points,
                    droneCurrentTotalCost, globalMinBid);
        } // End round

        return droneAssignments;
    }


    /*
     * A drone bids by adding the current total cost and the distance from the last assigned point to
     * the new bid point.
     */
    public double bid(int drone, ArrayList<LatLng> droneAssignment, LatLng bidPoint,
                      double [] droneCurrentTotalCost) {
        LatLng lastWonPoint = droneAssignment.get(droneAssignment.size()-1);
        double distanceToBidPoint = calculateDistanceToPoint(lastWonPoint, bidPoint);
        return droneCurrentTotalCost[drone] + distanceToBidPoint;
    }


    /*
     * Calculate the Euclidean distance from one point to another.
     */
    public double calculateDistanceToPoint(LatLng point1, LatLng point2) {
        double distance = Math.pow(point1.latitude - point2.latitude,2)
                + Math.pow(point1.longitude - point2.longitude,2);
        return Math.sqrt(distance);
    }

    /*
     * Assign a point to a specific drone, remove the point from the set of points that have
     * not been assigned, and update the total cost incurred so far.
     */
    public void assignPoint(int drone, ArrayList<LatLng> droneAssignment, LatLng point, ArrayList<LatLng> points,
                            double [] droneCurrentTotalCost, double bid) {
        droneAssignment.add(point);
        points.remove(point);
        droneCurrentTotalCost[drone] = bid;
        return;
    }
}