package guru.clevercoder.dronefleet;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by frankyn on 11/30/14.
 */
public class ArdroneSimulator {
    ArrayList<ArdroneAPI> droneFleet;
    ArrayList<ArrayList<LatLng> > droneFlightPlans;
    ArrayList<Integer> droneDestination;
    boolean simulationFinished;
    final ArdroneSimulator handle = this;

    public void simulationDrones (  ArrayList<ArdroneAPI> drones , ArrayList<ArrayList<LatLng> > flightPlans ) {
        droneFleet = drones;
        droneFlightPlans = flightPlans;
        droneDestination = new ArrayList<Integer>();
        for(int i = 0 ; i < drones.size(); ++ i ) droneDestination.add(0); // init all destination counters
        simulationFinished = false;
    }

    public double calculateDistanceToPoint(LatLng point1, LatLng point2) {
        double distance = Math.pow(point1.latitude - point2.latitude,2)
                + Math.pow(point1.longitude - point2.longitude,2);
        return Math.sqrt(distance);
    }

    public boolean checkCompletion ( ) {
        for(int i = 0 ; i < droneDestination.size() ; ++i ) {
            if ( droneFlightPlans.get(i).size()-1 != droneDestination.get(i) ) {
                return false;
            }
        }
        Log.i("TAG","Completed Simulation");
        return true;
    }

    public void simulationStep ( double dt ) {
        for(int i = 0 ; i < droneFleet.size(); ++ i ) {
            ArdroneAPI drone = droneFleet.get(i);
            LatLng currentCoord = drone.getCurrentCoord();

            if ( droneDestination.get(i) < droneFlightPlans.get(i).size()-1) {
                droneDestination.set(i,droneDestination.get(i)+1);
                LatLng destinationCoord = droneFlightPlans.get(i).get(droneDestination.get(i));

                // increment towards the next way point
                drone.setPosition(destinationCoord);
            } else
            if ( checkCompletion() ) {
                simulationFinished = true;
            }
        }
    }

    public void simulationRun ( ) {
        new Thread(new Runnable(){
            long lastRunTime;
            @Override
            public void run ( ) {
                try {
                    lastRunTime = System.nanoTime();

                    while (!simulationFinished) {
                        long nowRunTime = System.nanoTime();
                        double dt = (nowRunTime - lastRunTime) / 1E9;

                        handle.simulationStep(dt);
                        Thread.sleep(1000);
                    }
                } catch ( InterruptedException e ) {

                }
            }
        }).start();
    }
}
