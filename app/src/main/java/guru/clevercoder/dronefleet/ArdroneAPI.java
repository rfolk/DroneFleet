package guru.clevercoder.dronefleet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import com.google.android.gms.maps.model.LatLng;

public class ArdroneAPI {
    private static int NAVCONTROL_PORT = 5556;
    private static int NAVDATA_PORT = 5554;
    private static int MAVLINK_PORT = 14551;
    private ByteBuffer tmpBuffer = ByteBuffer.allocate(1024 * 15);
    private DatagramSocket navControl;
    private DatagramSocket navData;
    private DatagramSocket mavData;
    private InetAddress ipAddress;
    private int controlSeqNum = 0;
    private int mavSeqNum = 0;
    private LatLng latlng;

    public boolean connected = true;


    public ArdroneAPI(String ip) {
        try {
            ipAddress = InetAddress.getByName(ip);
            navControl = new DatagramSocket();
            navData = new DatagramSocket();
            mavData = new DatagramSocket();

            //System.out.println("HELLO");
        } catch (UnknownHostException e) {
            System.err.println(e);
        } catch (SocketException e) {
            System.err.println(e);
        }
    }

    public void close() {
        try {
            sendMovement("AT*REF", "290717696");
            navControl.close();
            navData.close();
            mavData.close();

            connected = false;
        } finally {
            System.err.println("Err");
        }
    }

    public void sendMovement(String command, String arguments) {
        try {
            String message = command + "=" + controlSeqNum + "," + arguments + "\r";
            System.out.println(message);
            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, NAVCONTROL_PORT);
            navControl.send(sendPacket);
            controlSeqNum++;
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public void setGPS(MavLink.MSG_GLOBAL_POSITION_INT msg) {
        latlng = new LatLng ( ((double)msg.getLat()/1E7) , ((double)msg.getLon())/1E7 ) ;
    }

    public LatLng getGPS ( ) {
        return latlng;
    }


    public void readMavData() {
        try {
            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            mavData.receive(packet);
            MavLink.Message msg = MavLink.Message.decodeMessage(buf);
            switch (msg.getMessageId()) {
                case MavLink.MSG_ID_GLOBAL_POSITION_INT:
                    setGPS((MavLink.MSG_GLOBAL_POSITION_INT) msg);
                break;
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public void connect() {
        final ArdroneAPI handle = this;
        connected = true;
        new Thread(new Runnable() {
            public void run() {
                while (handle.connected) {
                    readMavData();
                }
            }
        }).start();
        //sendMovement("AT*CONFIG","\"control:altitude_max\",\"2000\"");
        //init_nav_data();
        // Send lift_off command

        //sendMovement("AT*REF", "290718208");
        startMavLink();
        System.out.println("Started");

    }

    public void startMavLink() {
        try {
            MavLink.MSG_CHANGE_OPERATOR_CONTROL msg = new MavLink.MSG_CHANGE_OPERATOR_CONTROL((short) 255, (short) 190, 1, 0x0, 1, null);
            DatagramPacket sendPacket = new DatagramPacket(msg.encode(), msg.encode().length, ipAddress, MAVLINK_PORT);
            mavData.send(sendPacket);
            System.out.println("SENT");
        } catch (IOException e) {
            System.out.println("error");
        }
    }

    public void readMavLink() {
        // Read mav_link data
        // Check for GPS coordinates only
        // MAVLINK_MSG_ID_GLOBAL_POSITION_INT = 73
        // MAVLINK_MSG_ID_MISSION_REQUEST = 40
        // MAV_COMP_ID_MISSIONPLANNER = 190
        // mavlink_msg_global_position_int_decode(&msg, &globalpos);
        // LOGI(TAG, "MAVLINK_MSG_ID_GLOBAL_POSITION_INT sysid=%d compid=%d timestamp=%zu lat=%d lon=%d alt=%d relalt=%d vx=%d vy=%d vz=%d hdg=%d", msg.sysid, msg.compid,
        //                globalpos.time_boot_ms,
        //                globalpos.lat,
        //                globalpos.lon,
        //                globalpos.alt,
        //                globalpos.relative_alt,
        //                globalpos.vx,
        //                globalpos.vy,
        //                globalpos.vz,
        //                globalpos.hdg);
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

}

