/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

// MESSAGE RC_CHANNELS_SCALED PACKING
package syk.drone.mavlink.common;
import syk.drone.mavlink.MAVLinkPacket;
import syk.drone.mavlink.Messages.MAVLinkMessage;
import syk.drone.mavlink.Messages.MAVLinkPayload;

/**
* The scaled values of the RC channels received: (-100%) -10000, (0%) 0, (100%) 10000. Channels that are inactive should be set to UINT16_MAX.
*/
public class msg_rc_channels_scaled extends MAVLinkMessage {

    public static final int MAVLINK_MSG_ID_RC_CHANNELS_SCALED = 34;
    public static final int MAVLINK_MSG_LENGTH = 22;
    private static final long serialVersionUID = MAVLINK_MSG_ID_RC_CHANNELS_SCALED;


      
    /**
    * Timestamp (time since system boot).
    */
    public long time_boot_ms;
      
    /**
    * RC channel 1 value scaled.
    */
    public short chan1_scaled;
      
    /**
    * RC channel 2 value scaled.
    */
    public short chan2_scaled;
      
    /**
    * RC channel 3 value scaled.
    */
    public short chan3_scaled;
      
    /**
    * RC channel 4 value scaled.
    */
    public short chan4_scaled;
      
    /**
    * RC channel 5 value scaled.
    */
    public short chan5_scaled;
      
    /**
    * RC channel 6 value scaled.
    */
    public short chan6_scaled;
      
    /**
    * RC channel 7 value scaled.
    */
    public short chan7_scaled;
      
    /**
    * RC channel 8 value scaled.
    */
    public short chan8_scaled;
      
    /**
    * Servo output port (set of 8 outputs = 1 port). Most MAVs will just use one, but this allows for more than 8 servos.
    */
    public short port;
      
    /**
    * Receive signal strength indicator. Values: [0-100], 255: invalid/unknown.
    */
    public short rssi;
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket(MAVLINK_MSG_LENGTH);
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_RC_CHANNELS_SCALED;
              
        packet.payload.putUnsignedInt(time_boot_ms);
              
        packet.payload.putShort(chan1_scaled);
              
        packet.payload.putShort(chan2_scaled);
              
        packet.payload.putShort(chan3_scaled);
              
        packet.payload.putShort(chan4_scaled);
              
        packet.payload.putShort(chan5_scaled);
              
        packet.payload.putShort(chan6_scaled);
              
        packet.payload.putShort(chan7_scaled);
              
        packet.payload.putShort(chan8_scaled);
              
        packet.payload.putUnsignedByte(port);
              
        packet.payload.putUnsignedByte(rssi);
        
        return packet;
    }

    /**
    * Decode a rc_channels_scaled message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
              
        this.time_boot_ms = payload.getUnsignedInt();
              
        this.chan1_scaled = payload.getShort();
              
        this.chan2_scaled = payload.getShort();
              
        this.chan3_scaled = payload.getShort();
              
        this.chan4_scaled = payload.getShort();
              
        this.chan5_scaled = payload.getShort();
              
        this.chan6_scaled = payload.getShort();
              
        this.chan7_scaled = payload.getShort();
              
        this.chan8_scaled = payload.getShort();
              
        this.port = payload.getUnsignedByte();
              
        this.rssi = payload.getUnsignedByte();
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_rc_channels_scaled(){
        msgid = MAVLINK_MSG_ID_RC_CHANNELS_SCALED;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_rc_channels_scaled(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_RC_CHANNELS_SCALED;
        unpack(mavLinkPacket.payload);        
    }

                          
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_RC_CHANNELS_SCALED - sysid:"+sysid+" compid:"+compid+" time_boot_ms:"+time_boot_ms+" chan1_scaled:"+chan1_scaled+" chan2_scaled:"+chan2_scaled+" chan3_scaled:"+chan3_scaled+" chan4_scaled:"+chan4_scaled+" chan5_scaled:"+chan5_scaled+" chan6_scaled:"+chan6_scaled+" chan7_scaled:"+chan7_scaled+" chan8_scaled:"+chan8_scaled+" port:"+port+" rssi:"+rssi+"";
    }
}
        