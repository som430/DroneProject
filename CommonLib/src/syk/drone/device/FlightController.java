package syk.drone.device;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import syk.common.MavJsonListener;
import syk.common.MavJsonMessage;
import syk.drone.mavlink.MAVLinkPacket;
import syk.drone.mavlink.MavLinkListener;
import syk.drone.mavlink.Messages.MAVLinkMessage;
import syk.drone.mavlink.Parser;
import syk.drone.mavlink.ardupilotmega.msg_fence_fetch_point;
import syk.drone.mavlink.ardupilotmega.msg_fence_point;
import syk.drone.mavlink.common.*;
import syk.drone.mavlink.enums.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlightController {
    //---------------------------------------------------------------------------------
    private static Logger logger = LoggerFactory.getLogger(FlightController.class);

    //#################################################################################
    // Pixhawk RxTx 통신
    //#################################################################################
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    public void mavlinkConnectTcp(String host, int port) {
        while(true) {
            try {
                socket = new Socket(host, port);
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                logger.info("FlightController TCP Connected: " + host + "," + port);
                break;
            } catch(Exception e) {
                e.printStackTrace();
                try { socket.close(); } catch(Exception e1) {}
                try { Thread.sleep(1000); } catch (InterruptedException e1) {}
            }
        }
        mavlinkInitCommon();
    }
    //---------------------------------------------------------------------------------
    private CommPort commPort;
    private SerialPort serialPort;
    public void mavlinkConnectRxTx(String rxtxSerialPort) {
        while(true) {
            try {
                System.setProperty("gnu.io.rxtx.SerialPorts", rxtxSerialPort);
                System.out.println(rxtxSerialPort);
                CommPortIdentifier commPortIdentifier = CommPortIdentifier.getPortIdentifier(rxtxSerialPort);
                System.out.println(commPortIdentifier);
                commPort = commPortIdentifier.open("FC", 5000);
                serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                inputStream = serialPort.getInputStream();
                outputStream = serialPort.getOutputStream();
                logger.info("FlightController RxTx Connected: " + rxtxSerialPort);
                break;
            } catch(Exception e) {
                e.printStackTrace();
                try { serialPort.close(); } catch(Exception e1) {}
                try { commPort.close(); } catch(Exception e1) {}
                try { Thread.sleep(1000); } catch (InterruptedException e1) {}
            }
        }
        mavlinkInitCommon();
    }
    //---------------------------------------------------------------------------------
    private void mavlinkInitCommon() {
        sendMavlinkRequestDataStream();

        receiveMavlinkHeartbeat();
        receiveMavlinkGlobalPositionInt();
        receiveMavlinkAttitude();
        receiveMavlinkVfrHud();
        receiveMavlinkSysStatus();
        receiveMavlinkGpsRawInt();
        receiveMavlinkStatusText();
        receiveMavlinkHomePosition();
        receiveMavlinkMissionRequest();
        receiveMavlinkMissionAck();
        receiveMavlinkMissionCount();
        receiveMavlinkMissionItemInt();
        receiveMavlinkMissionCurrent();

        mavlinkReceiveFromFc();
    }
    //---------------------------------------------------------------------------------
    private Map<Integer, MavLinkListener> mavlinkListeners = new HashMap<>();

    public void addMavlinkListener(int msgid, MavLinkListener listener) {
        mavlinkListeners.put(msgid, listener);
    }

    private void removeMavlinkListener(int msgid) {
        mavlinkListeners.remove(msgid);
    }
    //----------------------------------------------------------------------------
    private ExecutorService mavlinkReceiveFromFcPool = Executors.newFixedThreadPool(1);
    private Parser mavParser = new Parser();
    private void mavlinkReceiveFromFc() {
        Thread thread = new Thread() {
            byte[] buffer = new byte[500];
            @Override
            public void run() {
                try {
                    while (true) {
                        int readNum = inputStream.read(buffer);
                        if (readNum == -1) throw new Exception();
                        byte[] payload = Arrays.copyOf(buffer, readNum);
                        for (int i = 0; i < payload.length; i++) {
                            int unsignedByte = payload[i] & 0xff;
                            MAVLinkPacket mavLinkPacket = mavParser.mavlink_parse_char(unsignedByte);
                            if (mavLinkPacket != null) {
                                Runnable runnable = () -> {
                                    MavLinkListener mavLinkMessageListener = mavlinkListeners.get(mavLinkPacket.msgid);
                                    if (mavLinkMessageListener != null) {
                                        MAVLinkMessage mavLinkMessage = mavLinkPacket.unpack();
                                        mavLinkMessageListener.receive(mavLinkMessage);
                                    }
                                };
                                mavlinkReceiveFromFcPool.submit(runnable);
                            }
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }
    //----------------------------------------------------------------------------
    private ExecutorService mavlinkSendToFcPool = Executors.newFixedThreadPool(1);
    private void mavlinkSendToFc(MAVLinkMessage mavLinkMessage) {
        Runnable runnable = () -> {
            try {
                MAVLinkPacket mavLinkPacket = mavLinkMessage.pack();
                byte[] packet = mavLinkPacket.encodePacket();
                outputStream.write(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        mavlinkSendToFcPool.submit(runnable);
    }
    //----------------------------------------------------------------------------
    private String mode = "STABILIZE";
    private boolean arm = false;
    private void receiveMavlinkHeartbeat() {
        addMavlinkListener(
            msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT,
            new MavLinkListener() {
                @Override
                public void receive(MAVLinkMessage mavLinkMessage)  {
                    msg_heartbeat msg = (msg_heartbeat) mavLinkMessage;

                    switch((int)msg.custom_mode) {
                        case COPTER_MODE.COPTER_MODE_STABILIZE:
                            mode = "STABILIZE";
                            break;
                        case COPTER_MODE.COPTER_MODE_ALT_HOLD:
                            mode = "ALT_HOLD";
                            break;
                        case COPTER_MODE.COPTER_MODE_LOITER:
                            mode = "LOITER";
                            break;
                        case COPTER_MODE.COPTER_MODE_POSHOLD:
                            mode = "POSHOLD";
                            break;
                        case COPTER_MODE.COPTER_MODE_LAND:
                            mode = "LAND";
                            break;
                        case COPTER_MODE.COPTER_MODE_RTL:
                            mode = "RTL";
                            break;
                        case COPTER_MODE.COPTER_MODE_AUTO:
                            mode = "AUTO";
                            break;
                        case COPTER_MODE.COPTER_MODE_GUIDED:
                            mode = "GUIDED";
                            break;
                    }

                    arm = ((msg.base_mode & MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED)!=0)?true:false;

                    sendMavJsonHeartbeat();
                }
            }
        );
    }
    //----------------------------------------------------------------------------
    private double currLat;
    private double currLng;
    private double alt;
    private double heading;
    private void receiveMavlinkGlobalPositionInt() {
        addMavlinkListener(
                msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT,
                new MavLinkListener() {
                    @Override
                    public void receive(MAVLinkMessage mavLinkMessage)  {
                        msg_global_position_int msg = (msg_global_position_int) mavLinkMessage;
                        currLat = msg.lat / 10000000.0;
                        currLng = msg.lon / 10000000.0;
                        alt = msg.relative_alt / 1000.0;
                        heading = msg.hdg / 100;

                        sendMavJsonGlobalPositionInt();
                    }
                }
        );
    }
    //----------------------------------------------------------------------------
    private double roll;
    private double pitch;
    private double yaw;
    private void receiveMavlinkAttitude() {
        addMavlinkListener(
                msg_attitude.MAVLINK_MSG_ID_ATTITUDE,
                new MavLinkListener() {
                    @Override
                    public void receive(MAVLinkMessage mavLinkMessage)  {
                        msg_attitude msg = (msg_attitude) mavLinkMessage;
                        roll = ((int)(msg.roll * 180/Math.PI * 1000)) / 1000.0 ;
                        pitch = ((int)(msg.pitch * 180/Math.PI * 1000)) / 1000.0;
                        yaw = ((int)(msg.yaw * 180/Math.PI * 1000)) / 1000.0;

                        sendMavJsonAttitude();
                    }
                }
        );
    }
    //----------------------------------------------------------------------------
    private double airSpeed;
    private double groundSpeed;
    private void receiveMavlinkVfrHud() {
        addMavlinkListener(
                msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD,
                new MavLinkListener() {
                    @Override
                    public void receive(MAVLinkMessage mavLinkMessage)  {
                        msg_vfr_hud msg = (msg_vfr_hud) mavLinkMessage;
                        airSpeed = ((int)(msg.airspeed*10))/10.0;
                        groundSpeed = ((int)(msg.groundspeed*10))/10.0;

                        sendMavJsonVfrHud();
                    }
                }
        );
    }
    //----------------------------------------------------------------------------
    private double voltageBattery;
    private double currentBattery;
    private int batteryRemaining;
    private void receiveMavlinkSysStatus() {
        addMavlinkListener(
                msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS,
                new MavLinkListener() {
                    @Override
                    public void receive(MAVLinkMessage mavLinkMessage)  {
                        msg_sys_status msg = (msg_sys_status) mavLinkMessage;
                        voltageBattery = ((int)(msg.voltage_battery/1000.0*10))/10.0;
                        currentBattery = ((int)(msg.current_battery/100.0*10))/10.0;
                        batteryRemaining = (msg.battery_remaining>0)?msg.battery_remaining:0;

                        sendMavJsonSysStatus();
                    }
                }
        );
    }
    //----------------------------------------------------------------------------
    private String fix_type = "GPS No";
    private void receiveMavlinkGpsRawInt() {
        addMavlinkListener(
                msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT,
                new MavLinkListener() {
                    @Override
                    public void receive(MAVLinkMessage mavLinkMessage)  {
                        msg_gps_raw_int msg = (msg_gps_raw_int) mavLinkMessage;
                        if(msg.fix_type == 6) {
                            fix_type = "GPS Fixed";
                        } else {
                            fix_type = "GPS No";
                        }

                        sendMavJsonGpsRawInt();
                    }
                }
        );
    }
    //----------------------------------------------------------------------------
    private String text;
    private void receiveMavlinkStatusText() {
        addMavlinkListener(
                msg_statustext.MAVLINK_MSG_ID_STATUSTEXT,
                new MavLinkListener() {
                    @Override
                    public void receive(MAVLinkMessage mavLinkMessage)  {
                        msg_statustext msg = (msg_statustext) mavLinkMessage;
                        text = new String(msg.text).trim();

                        sendMavJsonStatusText();
                    }
                }
        );
    }
    //----------------------------------------------------------------------------
    private double homeLat;
    private double homeLng;
    private void receiveMavlinkHomePosition() {
    	addMavlinkListener(
                msg_home_position.MAVLINK_MSG_ID_HOME_POSITION,
                new MavLinkListener() {
                    @Override
                    public void receive(MAVLinkMessage mavLinkMessage)  {
                        msg_home_position msg = (msg_home_position) mavLinkMessage;
                        homeLat = msg.latitude / 10000000.0;
                        homeLng = msg.longitude / 10000000.0;
                        
                        sendMavJsonHomePosition();
                    }
                }
        );
    }
    //----------------------------------------------------------------------------
    private void receiveMavlinkMissionRequest() {
    	addMavlinkListener(
    			msg_mission_request.MAVLINK_MSG_ID_MISSION_REQUEST,
                new MavLinkListener() {
                    @Override
                    public void receive(MAVLinkMessage mavLinkMessage)  {
                    	int requestSeq = ((msg_mission_request) mavLinkMessage).seq;

                    	for(int i=0; i<missionItemsJSONArray.length(); i++) {
                    		JSONObject jsonObject = missionItemsJSONArray.getJSONObject(i);
                    		if(requestSeq == jsonObject.getInt("seq")) {
                               	msg_mission_item_int msg = new msg_mission_item_int();
                                msg.target_system = 1;
                                msg.target_component = MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1;
                                msg.seq = jsonObject.getInt("seq");
                                msg.command = jsonObject.getInt("command");
                                msg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
                                msg.autocontinue = 1;
                                msg.param1 = jsonObject.getFloat("param1");
                                msg.param2 = jsonObject.getFloat("param2");
                                msg.param3 = jsonObject.getFloat("param3");
                                msg.param4 = jsonObject.getFloat("param4");
                                msg.x = (int)(jsonObject.getFloat("x") * 10000000);
                                msg.y = (int)(jsonObject.getFloat("y") * 10000000);
                                msg.z = jsonObject.getFloat("z");
                                mavlinkSendToFc(msg);
                                break;
                    		}
                    	}
                    	
                    	//다음 순서: receiveMavlinkMissionAck() 모든 미션 아이템이 FC로 전달된 경우
                    }
                }
        );
    }
    //----------------------------------------------------------------------------
    private void receiveMavlinkMissionAck() {
    	addMavlinkListener(
    			msg_mission_ack.MAVLINK_MSG_ID_MISSION_ACK,
                new MavLinkListener() {
                    @Override
                    public void receive(MAVLinkMessage mavLinkMessage)  {
                    	msg_mission_ack msg = (msg_mission_ack) mavLinkMessage;
                    	if(msg.type == MAV_MISSION_RESULT.MAV_MISSION_ACCEPTED) {
                    		sendMavJsonMissionAck();
                    	}
                    }
                }
        );
    }
    //----------------------------------------------------------------------------
    private int missionCount;
    private void receiveMavlinkMissionCount() {
    	addMavlinkListener(
    			msg_mission_count.MAVLINK_MSG_ID_MISSION_COUNT,
                new MavLinkListener() {
                    @Override
                    public void receive(MAVLinkMessage mavLinkMessage)  {
                    	msg_mission_count msg = (msg_mission_count) mavLinkMessage;
                    	missionCount = msg.count;
                    	sendMavlinkMissionRequestInt(0);
                    }
                }
        );
    }
    //----------------------------------------------------------------------------
    private void receiveMavlinkMissionItemInt() {
    	addMavlinkListener(
    			msg_mission_item_int.MAVLINK_MSG_ID_MISSION_ITEM_INT,
                new MavLinkListener() {
                    @Override
                    public void receive(MAVLinkMessage mavLinkMessage)  {
                    	msg_mission_item_int msg = (msg_mission_item_int) mavLinkMessage;
                    	if(msg.seq == 0) {
                    		missionItemsJSONArray = new JSONArray();
                    	}
                    	
                    	JSONObject jsonObject = new JSONObject();
                    	jsonObject.put("seq", msg.seq);
        	    		jsonObject.put("command", msg.command);
        	    		jsonObject.put("param1", msg.param1);
        	    		jsonObject.put("param2", msg.param2);
        	    		jsonObject.put("param3", msg.param3);
        	    		jsonObject.put("param4", msg.param4);
        	    		jsonObject.put("x", msg.x);
        	    		jsonObject.put("y", msg.y);
        	    		jsonObject.put("z", msg.z);
        	    		missionItemsJSONArray.put(jsonObject);
                    	
                    	if(msg.seq < (missionCount-1)) {
                    		sendMavlinkMissionRequestInt(msg.seq+1);
                    	} else {
                            msg_mission_ack msg2 = new msg_mission_ack();
                            msg2.target_system = 1;
                            msg2.target_component = MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1;
                            msg2.type = MAV_MISSION_RESULT.MAV_MISSION_ACCEPTED;
                            mavlinkSendToFc(msg2);
                            
                            sendMavJsonMissionItems();
                    	}
                    }
                }
        );
    }
    //----------------------------------------------------------------------------
    private int missionCurrent;
    private void receiveMavlinkMissionCurrent() {
    	addMavlinkListener(
    			msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT,
                new MavLinkListener() {
    			    private int preSeq;
                    @Override
                    public void receive(MAVLinkMessage mavLinkMessage)  {
                    	msg_mission_current msg = (msg_mission_current) mavLinkMessage;
                        missionCurrent = msg.seq;

                    	sendMavJsonMissionCurrent();

                    	if(preSeq ==  msg.seq) return;
                    	preSeq = msg.seq;

                    	//MAV_CMD_DO_GRIPPER는 MAVLINK_MSG_ID_MISSION_CURRENT가 오지 않는다.(ArduCopter V3.6-dev, mavlink 1.0)
                        if (missionCount == msg.seq + 1) return;
                        JSONObject jsonObject = missionItemsJSONArray.getJSONObject(msg.seq + 1);
                        if (jsonObject.getInt("command") == MavJsonMessage.MAVJSON_MISSION_COMMAND_GRIPPER) {
                            int param1 = (int)jsonObject.getDouble("param1");
                            int param2 = (int)jsonObject.getDouble("param2");
                            double param3 = jsonObject.getDouble("param3");
                            for (Device device : listDevices) {
                                if (device.no == param1) {
                                    if (param2 == 0) {
                                        device.off();
                                    } else {
                                        device.on();
                                    }
                                }
                            }
                        }
                    }
                }
        );
    }
    //----------------------------------------------------------------------------
    private List<Device> listDevices = new ArrayList<>();
    public void addDevice(Device device) {
        listDevices.add(device);
    }
    //----------------------------------------------------------------------------
    private void sendMavlinkHeartbeat() {
        msg_heartbeat msg = new msg_heartbeat();
        msg.type = MAV_TYPE.MAV_TYPE_GCS;
        msg.autopilot = MAV_AUTOPILOT.MAV_AUTOPILOT_INVALID;
        mavlinkSendToFc(msg);
    }
    //----------------------------------------------------------------------------
    private void sendMavlinkRequestDataStream() {
        msg_request_data_stream msg = new msg_request_data_stream();
        msg.target_system = 1;
        msg.target_component = MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1;
        msg.req_message_rate = 4;
        msg.req_stream_id = MAV_DATA_STREAM.MAV_DATA_STREAM_ALL;
        msg.start_stop = 1;
        mavlinkSendToFc(msg);
    }
    //----------------------------------------------------------------------------
    private void sendMavlinkSetMode(int mode) {
        msg_set_mode msg = new msg_set_mode();
        msg.target_system = 1;
        msg.base_mode = MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED;
        msg.custom_mode = mode;
        mavlinkSendToFc(msg);
    }
    //----------------------------------------------------------------------------
    private void sendMavlinkArm(boolean arm) {
        if(arm==false && alt>1) {
            sendMavlinkSetMode(COPTER_MODE.COPTER_MODE_LAND);
            return;
        }

        sendMavlinkSetMode(COPTER_MODE.COPTER_MODE_STABILIZE);

        msg_command_long msg = new msg_command_long();
        msg.target_system = 1;
        msg.target_component = MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1;
        msg.command = MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM;
        if (arm) {
            msg.param1 = 1;
        } else {
            msg.param1 = 0;
        }
        mavlinkSendToFc(msg);
    }
    //----------------------------------------------------------------------------
    private void sendMavlinkTakeoff(float alt) {
        sendMavlinkSetMode(COPTER_MODE.COPTER_MODE_GUIDED);
        msg_command_long msg = new msg_command_long();
        msg.target_system = 1;
        msg.target_component = MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1;
        msg.param7 = alt;
        msg.command = MAV_CMD.MAV_CMD_NAV_TAKEOFF;
        mavlinkSendToFc(msg);
    }
    //----------------------------------------------------------------------------
    private void sendMavlinkSetPositionTargetGlobalInt(double lat, double lng, double alt) {
        sendMavlinkSetMode(COPTER_MODE.COPTER_MODE_GUIDED);
        msg_set_position_target_global_int msg = new msg_set_position_target_global_int();
        msg.target_system = 1;
        msg.target_component = MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1;
        msg.lat_int = (int)(lat * 10000000);
        msg.lon_int = (int)(lng * 10000000);
        msg.alt = (float)alt;
        msg.coordinate_frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT_INT;
        msg.type_mask = 65528;
        mavlinkSendToFc(msg);
    }
	//----------------------------------------------------------------------------
    private void sendMavlinkGetHomePosition() {
    	msg_command_long msg = new msg_command_long();
        msg.target_system = 1;
        msg.target_component = MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1;
        msg.command = MAV_CMD.MAV_CMD_GET_HOME_POSITION;
        mavlinkSendToFc(msg);
    }
    //----------------------------------------------------------------------------
    private void sendMavlinkMissionCount(int count) {
    	msg_mission_count msg = new msg_mission_count();
        msg.target_system = 1;
        msg.target_component = MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1;
        msg.count = count;
        mavlinkSendToFc(msg);
    }   
    //----------------------------------------------------------------------------
    private void sendMavlinkMissionRequestList() {
        msg_mission_request_list msg = new msg_mission_request_list();
        msg.target_system = 1;
        msg.target_component = MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1;
        mavlinkSendToFc(msg);
        //다음 순서: mavlinkListenerMissionCount() 에서 다운로드 받을 미션 아이템 총 수를 얻음
    }
    //----------------------------------------------------------------------------
    private void sendMavlinkMissionRequestInt(int seq) {
    	msg_mission_request_int msg = new msg_mission_request_int();
        msg.target_system = 1;
        msg.target_component = MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1;
        msg.seq = seq;
        mavlinkSendToFc(msg);
        //다음 순서: mavlinkListenerMissionItemInt() 에서 다운로드 받을 미션 아이템 총 수를 얻음
    }
    //----------------------------------------------------------------------------
    private void sendMavlinkMissionStart() {
    	msg_command_long msg = new msg_command_long();
        msg.target_system = 1;
        msg.target_component = MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1;
        msg.command = MAV_CMD.MAV_CMD_MISSION_START;
        mavlinkSendToFc(msg);
    }
    //----------------------------------------------------------------------------
    private void sendMavlinkParamSet(String paramId, float paramValue) {
    	msg_param_set msg = new msg_param_set();
        msg.target_system = 1;
        msg.target_component = MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1;
        msg.param_id = Arrays.copyOf(paramId.getBytes(), 16);
        msg.param_value = paramValue;
        msg.param_type = 9;
        mavlinkSendToFc(msg);
    }
    //----------------------------------------------------------------------------
    private void sendMavlinkFencePoint() {
    	for(int i=0; i<fencePointsJSONArray.length(); i++) {
	    	JSONObject jsonObject = fencePointsJSONArray.getJSONObject(i);
	    	msg_fence_point msg = new msg_fence_point();
	        msg.target_system = 1;
	        msg.target_component = MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1;
	        msg.idx = (short)jsonObject.getInt("idx");
	        msg.lat = jsonObject.getFloat("lat");
	        msg.lng = jsonObject.getFloat("lng");
	        msg.count = (short)fencePointsJSONArray.length();
	        mavlinkSendToFc(msg);
    	}
    	sendMavJsonFenceAck();
    }
    //----------------------------------------------------------------------------
    private Map<Short, JSONObject> fencePoints = new TreeMap<>();
    private void sendMavlinkFenceFetchPoint() {
    	addMavlinkListener(
    			msg_fence_point.MAVLINK_MSG_ID_FENCE_POINT,
                new MavLinkListener() {
                    @Override
                    public void receive(MAVLinkMessage mavLinkMessage)  {
                    	msg_fence_point msg = (msg_fence_point) mavLinkMessage;
                    	JSONObject jsonObject = new JSONObject();
                    	jsonObject.put("idx", msg.idx);
                    	jsonObject.put("lat", msg.lat);
                    	jsonObject.put("lng", msg.lng);
                    	fencePoints.put(msg.idx, jsonObject);
                    	if(fencePoints.size() == fenceTotal) {
                    		removeMavlinkListener(msg_fence_point.MAVLINK_MSG_ID_FENCE_POINT);
                    		fencePointsJSONArray = new JSONArray();
                    		for(JSONObject js : fencePoints.values()) {
                    			fencePointsJSONArray.put(js);
                    		}
                    		sendMavJsonFencePoints();
                    	}
                    }
                }
        );    	
    	
    	for(int i=0; i<fenceTotal; i++) {
	    	 msg_fence_fetch_point msg = new msg_fence_fetch_point();
	         msg.target_system = 1;
	         msg.target_component = MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1;
	         msg.idx = (short)i;
	         mavlinkSendToFc(msg);
    	}
    	//다음 순서: mavlinkListenerFencePoint()
    }
    //----------------------------------------------------------------------------
    private void sendMavlinkParmRequestRead(String paramId) {
    	msg_param_request_read msg = new msg_param_request_read();
        msg.target_system = 1;
        msg.target_component = MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1;
        msg.param_id = Arrays.copyOf(paramId.getBytes(), 16);
        msg.param_index = -1;
        mavlinkSendToFc(msg);
    }
    //----------------------------------------------------------------------------
    private void sendMavlinkSetPositionTargetLocalNedEncode(float velocityNorth, float velocityEast, float velocityDown) {
        msg_set_position_target_local_ned msg = new msg_set_position_target_local_ned();
        msg.target_system = 1;
        msg.target_component = MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1;
        msg.coordinate_frame = MAV_FRAME.MAV_FRAME_LOCAL_NED;
        msg.type_mask = 0b0000111111000111;
        msg.vx = velocityNorth;
        msg.vy = velocityEast;
        msg.vz = velocityDown;
        mavlinkSendToFc(msg);
    }
    //#################################################################################
    // GCS MQTT 통신
    //#################################################################################
    private MqttClient mqttClient;
    private String pubTopic;
    private String subTopic;
    public void mqttConnect(String mqttBrokerConnStr, String pubTopic, String subTopic) {
        this.pubTopic = pubTopic;
        this.subTopic = subTopic;
        while(true) {
            try {
                mqttClient = new MqttClient(mqttBrokerConnStr, MqttClient.generateClientId(), null);
                MqttConnectOptions options = new MqttConnectOptions();
                options.setConnectionTimeout(5);
                options.setAutomaticReconnect(true);
                mqttClient.connect(options);
                logger.info("FlightController MQTT Connected: " + mqttBrokerConnStr);
                break;
            } catch(Exception e) {
                e.printStackTrace();
                try { mqttClient.close(); } catch (Exception e1) {}
                try { Thread.sleep(1000); } catch (InterruptedException e1) {}
            }
        }

        receiveMavJsonHeartbeat();
        receiveMavJsonArm();
        receiveMavJsonTakeoff();
        receiveMavJsonSetMode();
        receiveMavJsonSetPositionTargetGlobalInt();
        receiveMavJsonGetHomePosition();
        receiveMavJsonMissionUpload();
        receiveMavJsonMissionDownload();
        receiveMavJsonMissionStart();
        receiveMavJsonFenceUpload();
        receiveMavJsonFenceDownload();
        receiveMavJsonFenceEnable();
        receiveMavJsonFineControl();

        mqttReceiveFromGcs();
    }
    //---------------------------------------------------------------------------------
    private Map<String, MavJsonListener> mavJsonListeners = new HashMap<>();

    private void addMavJsonListener(String msgid, MavJsonListener listener) {
        mavJsonListeners.put(msgid, listener);
    }

    private void removeMavJsonListener(String msgid) {
        mavJsonListeners.remove(msgid);
    }
    //---------------------------------------------------------------------------------
    private ExecutorService mqttReceiveFromGcsPool = Executors.newFixedThreadPool(1);
    private void mqttReceiveFromGcs() {
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                byte[] payload = mqttMessage.getPayload();
                Runnable runnable = () -> {
                    String json = new String(payload);
                    JSONObject jsonObject = new JSONObject(json);
                    String msgid = jsonObject.getString("msgid");
                    MavJsonListener jsonMessageListener = mavJsonListeners.get(msgid);
                    jsonMessageListener.receive(jsonObject);
                };
                mqttReceiveFromGcsPool.submit(runnable);
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
            @Override
            public void connectionLost(Throwable throwable) {
            }
        });

        try {
            mqttClient.subscribe(subTopic);
            logger.info("FlightController MQTT Subscribed: " + subTopic);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------------------------
    private ExecutorService mqttSendToGcsPool = Executors.newFixedThreadPool(1);
    private void mqttSendToGcs(JSONObject jsonMessage) {
        if(mqttClient != null && mqttClient.isConnected()) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        String json = jsonMessage.toString();
                        mqttClient.publish(pubTopic, json.getBytes(), 0, false);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            };
            mqttSendToGcsPool.submit(runnable);
        }
    }
    //----------------------------------------------------------------------------
    private void receiveMavJsonHeartbeat() {
        addMavJsonListener(
                MavJsonMessage.MAVJSON_MSG_ID_HEARTBEAT,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                        sendMavlinkHeartbeat();
                    }
                });
    }
    //----------------------------------------------------------------------------
    private void receiveMavJsonArm() {
        addMavJsonListener(
                MavJsonMessage.MAVJSON_MSG_ID_ARM,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                        boolean arm = jsonMessage.getBoolean("arm");
                        sendMavlinkArm(arm);
                    }
                });
    }
    //----------------------------------------------------------------------------
    private void receiveMavJsonTakeoff() {
        addMavJsonListener(
                MavJsonMessage.MAVJSON_MSG_ID_TAKEOFF,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                        float alt = jsonMessage.getFloat("alt");
                        sendMavlinkTakeoff(alt);
                    }
                });
    }
    //----------------------------------------------------------------------------
    private void receiveMavJsonSetMode() {
        addMavJsonListener(
                MavJsonMessage.MAVJSON_MSG_ID_SET_MODE,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                        String mode = jsonMessage.getString("mode");
                        if(mode.equals("LAND")) {
                            sendMavlinkSetMode(COPTER_MODE.COPTER_MODE_LAND);
                        } else if(mode.equals("RTL")) {
                            sendMavlinkSetMode(COPTER_MODE.COPTER_MODE_RTL);
                        } else if(mode.equals("GUIDED")) {
                            sendMavlinkSetMode(COPTER_MODE.COPTER_MODE_GUIDED);
                        } else if(mode.equals("STABILIZE")) {
                            sendMavlinkSetMode(COPTER_MODE.COPTER_MODE_STABILIZE);
                        } else if(mode.equals("AUTO")) {
                            sendMavlinkSetMode(COPTER_MODE.COPTER_MODE_AUTO);
                        }
                    }
                });
    }
    //----------------------------------------------------------------------------
    private void receiveMavJsonSetPositionTargetGlobalInt() {
        addMavJsonListener(
                MavJsonMessage.MAVJSON_MSG_ID_SET_POSITION_TARGET_GLOBAL_INT,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                        double lat = jsonMessage.getDouble("lat");
                        double lng = jsonMessage.getDouble("lng");
                        double alt = jsonMessage.getDouble("alt");
                        sendMavlinkSetPositionTargetGlobalInt(lat, lng, alt);
                    }
                });
    }
	//----------------------------------------------------------------------------
    private void receiveMavJsonGetHomePosition() {
        addMavJsonListener(
                MavJsonMessage.MAVJSON_MSG_ID_GET_HOME_POSITION,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                        sendMavlinkGetHomePosition();
                    }
                });
    }
    //----------------------------------------------------------------------------
    public JSONArray missionItemsJSONArray;
    private void receiveMavJsonMissionUpload() {
        addMavJsonListener(
                MavJsonMessage.MAVJSON_MSG_ID_MISSION_UPLOAD,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                        missionItemsJSONArray = jsonMessage.getJSONArray("items");
                        sendMavlinkMissionCount(missionItemsJSONArray.length());
                        //다음 순서: receiveMavlinkMissionRequest()에서 하나씩 FC로 미션 아이템 전송
                    }
                });
    }
    //----------------------------------------------------------------------------
    private void receiveMavJsonMissionDownload() {
    	addMavJsonListener(
    	        MavJsonMessage.MAVJSON_MSG_ID_MISSION_DOWNLOAD,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                        sendMavlinkMissionRequestList();
                    }
                });
    }
    //----------------------------------------------------------------------------
    private void receiveMavJsonMissionStart() {
    	addMavJsonListener(
    	        MavJsonMessage.MAVJSON_MSG_ID_MISSION_START,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                        sendMavlinkMissionStart();
                    }
                });
    }
    //----------------------------------------------------------------------------
    private JSONArray fencePointsJSONArray;
    private void receiveMavJsonFenceUpload() {
    	addMavJsonListener(
    	        MavJsonMessage.MAVJSON_MSG_ID_FENCE_UPLOAD,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                        fencePointsJSONArray = jsonMessage.getJSONArray("points");
                        sendMavlinkParamSet("FENCE_TYPE", 5);
                        sendMavlinkParamSet("FENCE_ACTION", 1);
                        sendMavlinkParamSet("FENCE_ALT_MAX", 50);
                        sendMavlinkParamSet("FENCE_MARGIN", 2);
                        sendMavlinkParamSet("FENCE_TOTAL", fencePointsJSONArray.length());
                        sendMavlinkFencePoint();
                    }
                });
    }
    //----------------------------------------------------------------------------  
    private int fenceTotal;
    private void receiveMavJsonFenceDownload() {
    	addMavJsonListener(MavJsonMessage.MAVJSON_MSG_ID_FENCE_DOWNLOAD,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                        addMavlinkListener(
                                msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE,
                                new MavLinkListener() {
                                    @Override
                                    public void receive(MAVLinkMessage mavLinkMessage)  {
                                        msg_param_value msg = (msg_param_value) mavLinkMessage;
                                        if(new String(msg.param_id).trim().equals("FENCE_TOTAL")) {
                                            removeMavlinkListener(msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE);
                                            fenceTotal = (int) msg.param_value;
                                            sendMavlinkFenceFetchPoint();
                                        }
                                    }
                                }
                        );
                        sendMavlinkParmRequestRead("FENCE_TOTAL");
                    }
                });
    }
    //----------------------------------------------------------------------------  
    private void receiveMavJsonFenceEnable() {
    	addMavJsonListener(
    	        MavJsonMessage.MAVJSON_MSG_ID_FENCE_ENABLE,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                        sendMavlinkParamSet("FENCE_ENABLE", jsonMessage.getFloat("enable"));
                    }
                });
    }
    //----------------------------------------------------------------------------
    private void receiveMavJsonFineControl() {
        addMavJsonListener(
                MavJsonMessage.MAVJSON_MSG_ID_FINE_CONTROL,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                        float velocityNorth = jsonMessage.getFloat("velocityNorth");
                        if(velocityNorth > 5) {
                            velocityNorth = 5;
                        } else if(velocityNorth < -5) {
                            velocityNorth = -5;
                        }

                        float velocityEast = jsonMessage.getFloat("velocityEast");
                        if(velocityEast > 5) {
                            velocityEast = 5;
                        } else if(velocityEast < -5) {
                            velocityEast = -5;
                        }

                        sendMavlinkSetPositionTargetLocalNedEncode(
                                velocityNorth,  //m/sec
                                velocityEast,   //m/sec
                                0    //m/sec
                        );
                    }
                });
    }
    //----------------------------------------------------------------------------
    private void sendMavJsonHeartbeat() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_HEARTBEAT);
        jsonObject.put("mode", mode);
        jsonObject.put("arm", arm);
        mqttSendToGcs(jsonObject);
    }
    //----------------------------------------------------------------------------
    private void sendMavJsonGlobalPositionInt() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_GLOBAL_POSITION_INT);
        jsonObject.put("currLat", currLat);
        jsonObject.put("currLng", currLng);
        jsonObject.put("alt", alt);
        jsonObject.put("heading", heading);
        mqttSendToGcs(jsonObject);
        //logger.info(jsonObject.toString());
    }
    //----------------------------------------------------------------------------
    private void sendMavJsonAttitude() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_ATTITUDE);
        jsonObject.put("roll", roll);
        jsonObject.put("pitch", pitch);
        jsonObject.put("yaw", yaw);
        mqttSendToGcs(jsonObject);
        //logger.info(jsonObject.toString());
    }
    //----------------------------------------------------------------------------
    private void sendMavJsonVfrHud() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_VFR_HUD);
        jsonObject.put("airSpeed", airSpeed);
        jsonObject.put("groundSpeed", groundSpeed);
        mqttSendToGcs(jsonObject);
        //logger.info(jsonObject.toString());
    }
    //----------------------------------------------------------------------------
    private void sendMavJsonSysStatus() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_SYS_STATUS);
        jsonObject.put("voltageBattery", voltageBattery);
        jsonObject.put("currentBattery", currentBattery);
        jsonObject.put("batteryRemaining", batteryRemaining);
        mqttSendToGcs(jsonObject);
        //logger.info(jsonObject.toString());
    }
    //----------------------------------------------------------------------------
    private void sendMavJsonGpsRawInt() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_GPS_RAW_INT);
        jsonObject.put("fix_type", fix_type);
        mqttSendToGcs(jsonObject);
        //logger.info(jsonObject.toString());
    }
    //----------------------------------------------------------------------------
    private void sendMavJsonStatusText() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_STATUSTEXT);
        jsonObject.put("text", text);
        mqttSendToGcs(jsonObject);
        //logger.info(jsonObject.toString());
    }
    //----------------------------------------------------------------------------
    private void sendMavJsonHomePosition() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_HOME_POSITION);
        jsonObject.put("homeLat", homeLat);
        jsonObject.put("homeLng", homeLng);
        mqttSendToGcs(jsonObject);
        //logger.info(jsonObject.toString());
    }
    //----------------------------------------------------------------------------
    private void sendMavJsonMissionAck() {
    	JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_MISSION_ACK);
        mqttSendToGcs(jsonObject);
        //logger.info(jsonObject.toString());
    }
    //----------------------------------------------------------------------------
    private void sendMavJsonMissionItems() {
    	JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_MISSION_ITEMS);
        jsonObject.put("items", missionItemsJSONArray);
        mqttSendToGcs(jsonObject);
        //logger.info(jsonObject.toString());
    }
    //----------------------------------------------------------------------------
    private void sendMavJsonMissionCurrent() {
    	JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_MISSION_CURRENT);
        jsonObject.put("seq", missionCurrent);
        mqttSendToGcs(jsonObject);
        //logger.info(jsonObject.toString());
    }
    //----------------------------------------------------------------------------
    private void sendMavJsonFenceAck() {
    	JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_FENCE_ACK);
        mqttSendToGcs(jsonObject);
        //logger.info(jsonObject.toString());
    }
    //----------------------------------------------------------------------------
    private void sendMavJsonFencePoints() {
    	JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_FENCE_POINTS);
        jsonObject.put("points", fencePointsJSONArray);
        mqttSendToGcs(jsonObject);
        //logger.info(jsonObject.toString());
    }
}
