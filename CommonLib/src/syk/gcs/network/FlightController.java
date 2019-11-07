package syk.gcs.network;

import syk.common.MavJsonListener;
import syk.common.MavJsonMessage;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlightController {
    //---------------------------------------------------------------------------------
    private static Logger logger = LoggerFactory.getLogger(FlightController.class);
    //---------------------------------------------------------------------------------
    public FlightController() {
        receiveHeartbeat();
        receiveGlobalPositionInt();
        receiveAttitude();
        receiveVfrHud();
        receiveSysStatus();
        receiveGpsRawInt();
        receiveStatusText();
        receiveHomePosition();
    }
    //---------------------------------------------------------------------------------
    private Map<String, List<MavJsonListener>> jsonListeners = new HashMap<>();
    private MavJsonListener receiveMessageAllListener;
    private MavJsonListener sendMessageAllListener;

    public void addMavJsonListener(String msgid, MavJsonListener listener) {
        if(msgid.equals(MavJsonMessage.MAVJSON_MSG_ID_RECEIVE_MESSAGE_ALL)) {
            receiveMessageAllListener = listener;
            return;
        }
        if(msgid.equals(MavJsonMessage.MAVJSON_MSG_ID_SEND_MESSAGE_ALL)) {
            sendMessageAllListener = listener;
            return;
        }
        boolean isMsgid = jsonListeners.containsKey(msgid);
        if(isMsgid) {
            List<MavJsonListener> list = jsonListeners.get(msgid);
            list.add(listener);
        } else {
            List<MavJsonListener> list = new ArrayList<>();
            list.add(listener);
            jsonListeners.put(msgid, list);
        }
    }

    public void removeMavJsonListener(String msgid, MavJsonListener listener) {
        boolean isMsgid = jsonListeners.containsKey(msgid);
        if(isMsgid) {
            List<MavJsonListener> list = jsonListeners.get(msgid);
            list.remove(listener);
        }
    }
    //---------------------------------------------------------------------------------
    private String mqttBrokerConnStr;
    private String pubTopic;
    private String subTopic;
    private MqttClient mqttClient;
    public void mqttConnect(String mqttBrokerConnStr, String pubTopic, String subTopic) {
        this.mqttBrokerConnStr = mqttBrokerConnStr;
        this.pubTopic = pubTopic;
        this.subTopic = subTopic;
        try {
            if(mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
            }
            mqttClient = new MqttClient(mqttBrokerConnStr, MqttClient.generateClientId(), null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setConnectionTimeout(5);
            options.setAutomaticReconnect(true);
            mqttClient.connect(options);
            logger.info("FlightController MQTT Connected: " + mqttBrokerConnStr);
            mqttReceiveFromFlightController();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mqttDisconnect() {
        try {
            mqttClient.disconnect();
            mqttClient.close();
            mqttClient = null;
        } catch (Exception e) {
        }
    }
    //---------------------------------------------------------------------------------
    private ExecutorService mqttReceiveFromDronePool = Executors.newFixedThreadPool(1);
    private void mqttReceiveFromFlightController() {
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                byte[] payload = message.getPayload();
                String json = new String(payload);
                JSONObject jsonObject = new JSONObject(json);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if(receiveMessageAllListener != null) {
                            receiveMessageAllListener.receive(jsonObject);
                        }
                        List<MavJsonListener> list = jsonListeners.get(jsonObject.getString("msgid"));
                        List<MavJsonListener> copy = new ArrayList<>(list);
                        for(MavJsonListener listener : copy) {
                            listener.receive(jsonObject);
                        }
                    }
                };
                mqttReceiveFromDronePool.submit(runnable);
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
            @Override
            public void connectionLost(Throwable e) {
            }
        });

        try {
            mqttClient.subscribe(pubTopic);
            logger.info("FlightController MQTT Subscribed: " + pubTopic);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //---------------------------------------------------------------------------------
    public String mode;
    public boolean arm;
    private void receiveHeartbeat() {
        addMavJsonListener(
                MavJsonMessage.MAVJSON_MSG_ID_HEARTBEAT,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                        try {
                            mode = jsonMessage.getString("mode");
                            arm = jsonMessage.getBoolean("arm");

                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("msgid", "HEARTBEAT");
                            mqttSendToFlightController(jsonObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }
    //---------------------------------------------------------------------------------
    public double currLat;
    public double currLng;
    public double alt;
    public double heading;
    private void receiveGlobalPositionInt() {
        addMavJsonListener(
                MavJsonMessage.MAVJSON_MSG_ID_GLOBAL_POSITION_INT,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                        currLat = jsonMessage.getDouble("currLat");
                        currLng = jsonMessage.getDouble("currLng");
                        alt = jsonMessage.getDouble("alt");
                        heading = jsonMessage.getDouble("heading");
                    }
                });
    }
    //---------------------------------------------------------------------------------
    public double roll;
    public double pitch;
    public double yaw;
    private void receiveAttitude() {
    	addMavJsonListener(
                MavJsonMessage.MAVJSON_MSG_ID_ATTITUDE,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                        roll = jsonMessage.getDouble("roll");
                        pitch = jsonMessage.getDouble("pitch");
                        yaw = jsonMessage.getDouble("yaw");
                    }
                });
    }
    //---------------------------------------------------------------------------------
    public double airSpeed;
    public double groundSpeed;
    private void receiveVfrHud() {
    	addMavJsonListener(
                MavJsonMessage.MAVJSON_MSG_ID_VFR_HUD,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                        airSpeed = jsonMessage.getDouble("airSpeed");
                        groundSpeed = jsonMessage.getDouble("groundSpeed");
                    }
                });
    }
    //---------------------------------------------------------------------------------
    public double voltageBattery;
    public double currentBattery;
    public int batteryRemaining;
    private void receiveSysStatus() {
    	addMavJsonListener(
                MavJsonMessage.MAVJSON_MSG_ID_SYS_STATUS,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                        voltageBattery = jsonMessage.getDouble("voltageBattery");
                        currentBattery = jsonMessage.getDouble("currentBattery");
                        batteryRemaining = jsonMessage.getInt("batteryRemaining");
                    }
                });
    }
    //---------------------------------------------------------------------------------
    public String fixType;
    private void receiveGpsRawInt() {
    	addMavJsonListener(
                MavJsonMessage.MAVJSON_MSG_ID_GPS_RAW_INT,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                        fixType = jsonMessage.getString("fix_type");
                    }
                });
    }
    //---------------------------------------------------------------------------------
    public String text;
    private void receiveStatusText() {
    	addMavJsonListener(
                MavJsonMessage.MAVJSON_MSG_ID_STATUSTEXT,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                        text = jsonMessage.getString("text");
                    }
                });
    }
    //---------------------------------------------------------------------------------
    public double homeLat;
    public double homeLng;
    private void receiveHomePosition() {
    	addMavJsonListener(
                MavJsonMessage.MAVJSON_MSG_ID_HOME_POSITION,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                        homeLat = jsonMessage.getDouble("homeLat");
                        homeLng = jsonMessage.getDouble("homeLng");
                    }
                });
    }
    //---------------------------------------------------------------------------------
    private ExecutorService mqttSendToDronePool = Executors.newFixedThreadPool(1);
    private void mqttSendToFlightController(JSONObject jsonObject) {
        if(mqttClient != null && mqttClient.isConnected()) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        if(sendMessageAllListener != null) {
                            sendMessageAllListener.receive(jsonObject);
                        }
                        String json = jsonObject.toString();
                        mqttClient.publish(subTopic, json.getBytes(), 0, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            mqttSendToDronePool.submit(runnable);
        }
    }
    //---------------------------------------------------------------------------------
    public void sendArm(boolean arm) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_ARM);
        jsonObject.put("arm", arm);
        mqttSendToFlightController(jsonObject);
    }
    //---------------------------------------------------------------------------------
    public void sendTakeoff(float alt) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_TAKEOFF);
        jsonObject.put("alt", alt);
        mqttSendToFlightController(jsonObject);
    }
    //---------------------------------------------------------------------------------
    public void sendSetMode(String mode) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_SET_MODE);
        jsonObject.put("mode", mode);
        mqttSendToFlightController(jsonObject);
    }
    //---------------------------------------------------------------------------------
    public void sendSetPositionTargetGlobalInt(double lat, double lng, double alt) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_SET_POSITION_TARGET_GLOBAL_INT);
        jsonObject.put("lat", lat);
        jsonObject.put("lng", lng);
        jsonObject.put("alt", alt);
        mqttSendToFlightController(jsonObject);
    }
    //---------------------------------------------------------------------------------
    public void sendGetHomePosition() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_GET_HOME_POSITION);
        mqttSendToFlightController(jsonObject);
    }
    //---------------------------------------------------------------------------------
    public void sendMissionUpload(JSONArray jsonArray) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_MISSION_UPLOAD);
        jsonObject.put("items", jsonArray);
        mqttSendToFlightController(jsonObject);
    }    
    //---------------------------------------------------------------------------------
    public void sendMissionDownload() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_MISSION_DOWNLOAD);
        mqttSendToFlightController(jsonObject);
    }
    //---------------------------------------------------------------------------------
	public void sendMissionStart() {
		JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_MISSION_START);
        mqttSendToFlightController(jsonObject);
	}
	//---------------------------------------------------------------------------------
	public void sendFenceUpload(JSONArray jsonArray) {
		JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_FENCE_UPLOAD);
        jsonObject.put("points", jsonArray);
        mqttSendToFlightController(jsonObject);
	}
    //---------------------------------------------------------------------------------
	public void sendFenceDownload() {
		JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_FENCE_DOWNLOAD);
        mqttSendToFlightController(jsonObject);
	}
    //---------------------------------------------------------------------------------
	public void sendFenceEnable(boolean enable) {
		JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_FENCE_ENABLE);
        jsonObject.put("enable", (enable)?1:0);
        mqttSendToFlightController(jsonObject);
	}
    //---------------------------------------------------------------------------------
    public void sendFindControl(double velocityNorth, double velocityEast) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgid", MavJsonMessage.MAVJSON_MSG_ID_FINE_CONTROL);
        jsonObject.put("velocityNorth", velocityNorth);
        jsonObject.put("velocityEast", velocityEast);
        mqttSendToFlightController(jsonObject);
    }
}
