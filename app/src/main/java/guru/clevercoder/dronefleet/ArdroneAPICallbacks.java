package guru.clevercoder.dronefleet;

/**
 * Created by frankyn on 11/10/14.
 */
public interface ArdroneAPICallbacks {
    enum MISSION_EVENTS {
       WAY_POINT_REACHED,
       LANDING,
    };

    public void onDroneConnect ( ArdroneAPI drone );
    public void onDroneDisconnect ( ArdroneAPI drone );
    public void onFlightPlanComplete ( ArdroneAPI drone );
    public void onFlightPlanReady ( ArdroneAPI drone );
    public void onFlightPlanError ( ArdroneAPI drone , String why );
    public void onMissionEvent ( ArdroneAPI drone , MISSION_EVENTS event );
    public void onDroneGPS ( ArdroneAPI drone );

}
