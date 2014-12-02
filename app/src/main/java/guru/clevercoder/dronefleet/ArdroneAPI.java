package guru.clevercoder.dronefleet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.lang.Double;
import java.util.ArrayList;
import java.util.Arrays;

/*
    GMAP LatLng class is used:
 */

import com.google.android.gms.maps.model.LatLng;

public class ArdroneAPI {
    private static int NAVCONTROL_PORT = 5556;
    private static int MAVLINK_PORT = 14551;
    private DatagramSocket navControl;
    private DatagramSocket mavData;
    private InetAddress ipAddress;
    private int ctrlSeqNum = 0;
    private int mavSeqNum = 0;
    private int wayPointsReached = 0;
    private LatLng droneCoord = new LatLng(0,0);
    private ArrayList<LatLng> flightPlan;
    private int flightPlanId = 0;
    private boolean sendingFlightPlan = false;


    private int flightPlanLastSend = -1;
    public final ArdroneAPI handle;
    public ArdroneAPICallbacks callbacks;

    private boolean connected = false;
    private boolean flightPlanned = false;

    public short systemId = 255;
    public short componentId = 190;
    public int targetSystem = 1;
    public int targetComponent = 190;
    public int mavVersion = 1;
    public long lastTime = 0L;
    public short currentSequenceNumber = 0;
    public short[][] lastSequenceNumber = new short[256][256];

    /*
        TODO: Create a timer for when the drone is sent in autonomous mode
     */

    public ArdroneAPI(String ip,ArdroneAPICallbacks callbackClass) {
        // Set internal handle
        handle = this;
        callbacks = callbackClass;

        try {

            ipAddress = InetAddress.getByName(ip);
            navControl = new DatagramSocket();
            mavData = new DatagramSocket();
            flightPlan = null;

            for ( int i = 0 ; i < 255; i ++ ) {
                for ( int b = 0; b < 255; b ++ ) {
                    lastSequenceNumber[i][b] = -1;
                }
            }
        } catch (UnknownHostException e) {
            System.err.println(e);
        } catch (SocketException e) {
            System.err.println(e);
        }
    }

    public ArdroneAPI(ArdroneAPICallbacks callbackClass){
        handle=this;
        callbacks = callbackClass;

    }

    // After elapsed time has passed 5 seconds we send a heartbeat :)
    public void checkElapsedTime () {
        try {
            long nowTime = System.nanoTime();

            long elapsedTime = (long)((nowTime - lastTime) / 1E9);

            // If elapsedTime is longer than 5 seconds send a HeartBeat
            if ( elapsedTime > 5 ) {
                MavLink.MSG_HEARTBEAT heartbeat = new MavLink.MSG_HEARTBEAT(systemId, componentId, 0, MavLink.MAV_TYPE_GCS, MavLink.MAV_MODE_FLAG_GUIDED_ENABLED, MavLink.MAV_MODE_MANUAL_ARMED, MavLink.MAV_STATE_ACTIVE, 3);
                DatagramPacket sendPacket = new DatagramPacket(heartbeat.encode(), heartbeat.encode().length, ipAddress, MAVLINK_PORT);
                mavData.send(sendPacket);
            }

            lastTime = nowTime;
        } catch ( IOException e ) {

        }
    }

    public void close() {
        if ( !isConnected ( ) ) {
            return;
        }
        try {
            navControl.close();
            mavData.close();
            connected = false;
            callbacks.onDroneDisconnect(this);
        } finally {
            System.err.println("Err");
        }
    }

    public void sendMovement(String command, String arguments) {
        try {
            String message = command + "=" + ctrlSeqNum + "," + arguments + "\r";
            System.out.println(message);
            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, NAVCONTROL_PORT);
            navControl.send(sendPacket);
            ctrlSeqNum++;
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private void handleHeartBeat ( MavLink.MSG_HEARTBEAT msg ) {
        systemId = msg.getSystemId ( );
        componentId = msg.getComponentId ( );
        mavVersion = msg.getMavlink_version ( );
        targetSystem = 1;
        targetComponent = 0;
    }

    public boolean flightPlanned ( ) {
        return flightPlanned;
    }

    public void buildFlightPlan ( ArrayList<LatLng> points ) {
        if ( sendingFlightPlan ) {
            return;
        }
        try {
            flightPlan = points;
            flightPlanId = 0;
            flightPlanLastSend = -1;
            sendingFlightPlan = true;
            // Build flight plan
            // Send mission points

//            MavLink.MSG_HEARTBEAT heartbeat = new MavLink.MSG_HEARTBEAT(systemId, componentId, 0, MavLink.MAV_TYPE_GCS, MavLink.MAV_MODE_FLAG_GUIDED_ENABLED, MavLink.MAV_MODE_MANUAL_ARMED, MavLink.MAV_STATE_ACTIVE, 3);
//            DatagramPacket sendPacket = new DatagramPacket(heartbeat.encode(), heartbeat.encode().length, ipAddress, MAVLINK_PORT);
//            mavData.send(sendPacket);

            MavLink.MSG_MISSION_COUNT msg = new MavLink.MSG_MISSION_COUNT(systemId, componentId, points.size(), targetSystem, targetComponent );
            System.out.println(msg);

            DatagramPacket sendPacket = new DatagramPacket(msg.encode(), msg.encode().length, ipAddress, MAVLINK_PORT);
            mavData.send(sendPacket);
            System.out.println ( "Begin Flight Plan" );
        } catch ( IOException e ) {

        }
    }

    public void startFlightPlan ( ) {
        try {
            // Build

            //public MSG_COMMAND_LONG (short systemId, short componentId, float param1  , float param2  , float param3  , float param4  , float param5  , float param6  , float param7  , int command  , int target_system  , int target_component  , int confirmation  ) {

            MavLink.MSG_COMMAND_LONG commandLong = new MavLink.MSG_COMMAND_LONG(systemId, componentId, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, MavLink.MAV_CMD_NAV_TAKEOFF, targetSystem, targetComponent , 1 );

            DatagramPacket sendPacket = new DatagramPacket(commandLong.encode(), commandLong.encode().length, ipAddress, MAVLINK_PORT);
            mavData.send(sendPacket);

            System.out.println ( "Started Flight Plan" );
        } catch ( IOException e ) {

        }
    }

    private void lastSequenceNumber ( short seqIndex, short systemId, short componentId ) {
        short lastSeq = lastSequenceNumber[systemId][componentId];
        short expSeq = (lastSeq==-1?seqIndex:(short)((++lastSeq)%256));
        if(lastSeq==-1) callbacks.onDroneConnect(this); // used once

        if ( expSeq != lastSeq ) {
            System.out.println ( "Skipped " + (expSeq - lastSeq) + " packets" );
        }

        lastSequenceNumber[systemId][componentId] = expSeq;
    }

    private short nextSequenceNumber ( short systemId, short componentId ) {
        return ++lastSequenceNumber[systemId][componentId];
    }

    private void handleGlobalPositionInt ( MavLink.MSG_GLOBAL_POSITION_INT msg ) {
        droneCoord = new LatLng ((double)msg.getLat()/1E7,(double)msg.getLon()/1E7);
        callbacks.onDroneGPS(this);
    }

    public void setPosition ( LatLng pos ){
        droneCoord = pos;
        callbacks.onDroneGPS(this);
    }

    private void handleMissionRequest ( MavLink.MSG_MISSION_REQUEST msg ) {
        if ( !sendingFlightPlan ) {
            return;
        }
        System.out.println ( msg );
        if ( flightPlanLastSend != msg.getSeq() ) {
            flightPlanLastSend = msg.getSeq();
        }

        System.out.println ( "Sending Coordinate: " + msg.getSeq() );

        // Send the next way point item to the drone.

        // getNext item in flightPlan

        try {
            LatLng nextCoord = flightPlan.get(flightPlanLastSend);
            MavLink.MSG_MISSION_ITEM newMsg = new MavLink.MSG_MISSION_ITEM(systemId,
                    componentId,
                    0.0f,
                    0.75f, // Radius of accuracy
                    0.0f,
                    0.0f,
                    (float)(nextCoord.latitude),
                    (float)(nextCoord.longitude),
                    3.0f, // height in meters
                    flightPlanLastSend,
                    ((flightPlan.size()-1==flightPlanLastSend)?MavLink.MAV_CMD_NAV_LAND:MavLink.MAV_CMD_NAV_WAYPOINT),
                    targetSystem,
                    targetComponent,
                    MavLink.MAV_FRAME_GLOBAL,
                    (flightPlanLastSend==0?1:0),
                    1
            );

            System.out.println(newMsg);
            DatagramPacket sendPacket = new DatagramPacket(newMsg.encode(), newMsg.encode().length, ipAddress, MAVLINK_PORT);
            mavData.send(sendPacket);


        } catch ( IOException e ) {
            System.out.println ( "ERROR??SDFOJSDF" );
        }

    }

    private void handleMissionItemReached ( MavLink.MSG_MISSION_ITEM_REACHED msg ) {
        // A mission point has been reached
        callbacks.onMissionEvent ( this, ArdroneAPICallbacks.MISSION_EVENTS.WAY_POINT_REACHED );
        wayPointsReached++;

        if ( wayPointsReached == flightPlan.size() ){
            //Flight plan completed
            callbacks.onMissionEvent ( this, ArdroneAPICallbacks.MISSION_EVENTS.LANDING );
            callbacks.onFlightPlanComplete(this);
        }
    }

    private void handleMissionAck ( MavLink.MSG_MISSION_ACK msg ) {
        // Check to see if ACK is okay
        if ( msg.getType() != 0 ) {
            // Error occured
            System.out.println ( "ERROR" );
        } else
        if ( sendingFlightPlan ) {
            // We Recieved a message
            System.out.println ( "ACK" );
            // Finished Planning
            System.out.println ( "flightdone" );
            flightPlan.clear();
            flightPlanned = true;
            sendingFlightPlan = false;
            callbacks.onFlightPlanReady(this);
            return;
        }
    }

    public void handleMissionCurrent ( MavLink.MSG_MISSION_CURRENT msg ) {

    }

    public LatLng getCurrentCoord ( ) {
        return droneCoord;
    }

    public void readMavData() {
        try {
            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            mavData.receive(packet);
            MavLink.Message msg = MavLink.Message.decodeMessage(buf);
            lastSequenceNumber(msg.getSequenceIndex(),msg.getSystemId(),
                    msg.getComponentId());
            //System.out.println(msg);
            switch (msg.getMessageId()) {
                // Used to read heartbeat
                case MavLink.MSG_ID_HEARTBEAT:
                    handleHeartBeat((MavLink.MSG_HEARTBEAT) msg);
                    break;

                // Used for GPS coordinates
                case MavLink.MSG_ID_GLOBAL_POSITION_INT:
                    handleGlobalPositionInt((MavLink.MSG_GLOBAL_POSITION_INT) msg);
                    break;

                // Used to detect which point the drone is flying towards
                case MavLink.MSG_ID_MISSION_CURRENT:
                    handleMissionCurrent((MavLink.MSG_MISSION_CURRENT) msg);
                    break;

                // Used to detect which point the drone has reached
                case MavLink.MSG_ID_MISSION_ITEM_REACHED:
                    handleMissionItemReached((MavLink.MSG_MISSION_ITEM_REACHED) msg);
                    break;

                // Requesting a point
                case MavLink.MSG_ID_MISSION_REQUEST:
                    handleMissionRequest((MavLink.MSG_MISSION_REQUEST) msg);
                    break;

                // Mission ACK
                case MavLink.MSG_ID_MISSION_ACK:
                    handleMissionAck((MavLink.MSG_MISSION_ACK) msg );
                    break;

            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public void initMavLink() {
        try {
            // Start listening to the mavData port
            new Thread(new Runnable() {
                public void run() {
                    while (handle.connected) {
                        readMavData();
                    }
                }
            }).start();

            // Start a timer for a heartbeat when time out occurs
            new Thread(new Runnable() {
                public void run() {
                    try {
                        while (handle.connected) {
                            checkElapsedTime();
                            Thread.sleep(1000);
                        }
                    } catch ( Exception e ) {
                        // something went wrong.
                    }
                }
            }).start();

            // Send Init command to the drone
            MavLink.MSG_CHANGE_OPERATOR_CONTROL msg = new MavLink.MSG_CHANGE_OPERATOR_CONTROL(systemId, componentId, targetSystem, 0, mavVersion, null);
            DatagramPacket sendPacket = new DatagramPacket(msg.encode(), msg.encode().length, ipAddress, MAVLINK_PORT);
            mavData.send(sendPacket);
        } catch (IOException e) {
            System.out.println("error");
        }
    }

    public void initCtrlLink() {
        // Send set altitude max
        sendMovement("AT*CONFIG","\"control:altitude_max\",\"2000\"");

    }

    public boolean isConnected ( ) {
        return connected;
    }

    public void connect() {
        if ( isConnected ( ) ) {
            return;
        }

        connected = true;

        initMavLink();

        System.out.println("Started");

    }

    public void left() {
        sendMovement("AT*PCMD", "1,0,-1082130432,0,0");
    }

    public void right() {
        sendMovement("AT*PCMD", "1,0,1065353216,0,0");
    }

    public void front() {
        sendMovement("AT*PCMD", "1,-1082130432,0,0,0");
    }

    public void back() {
        sendMovement("AT*PCMD", "1,1065353216,0,0,0");
    }

    public void up() {
        sendMovement("AT*PCMD", "1,0,0,1065353216,0");
    }

    public void down() {
        sendMovement("AT*PCMD", "1,0,0,-1082130432,0");
    }

    public void tright() {
        sendMovement("AT*PCMD", "1,0,0,0,1065353216");
    }

    public void tleft() {
        sendMovement("AT*PCMD", "1,0,0,0,-1082130432");
    }

    public void liftOff ( ) {
        sendMovement("AT*REF", "290718208");
    }

    public void land ( ) {
        sendMovement("AT*REF", "290717696");
    }

    public String toString() {
        String ip = ipAddress.toString();
        int size = ip.length();

        return "Buddy: " + ip.substring(size-2,size-1);
    }
}

