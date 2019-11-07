package syk.gcs.network;

import syk.gcs.cameraview.ImageListener;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Camera {
    //----------------------------------------------------------------------------
	private static Logger logger = LoggerFactory.getLogger(Camera.class);
    //----------------------------------------------------------------------------
    public Camera() {
    }
    //----------------------------------------------------------------------------
    public ImageListener imageListener;
    public void mqttListenerSet(ImageListener listener) {
        this.imageListener = listener;
    }
    public void mqttListenerRemove() {
        this.imageListener = null;
    }
    //----------------------------------------------------------------------------
    public String mqttBrokerConnStr;
    public String pubTopic;
    public String subTopic;
    public MqttClient mqttClient;
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
            logger.info("Camera MQTT Connected: " + mqttBrokerConnStr);
            mqttReceiveFromCamera();
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
    //----------------------------------------------------------------------------
    public ExecutorService mqttReceiveFromCameraPool = Executors.newFixedThreadPool(1);
    public void mqttReceiveFromCamera() {
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                byte[] image = mqttMessage.getPayload();
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        if(imageListener != null) {
                            imageListener.receive(image);
                        }
                        //다음 이미지 요청(동기)
                        mqttSendToCamera(new byte[]{0});
                    }
                };
                mqttReceiveFromCameraPool.submit(task);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
            @Override
            public void connectionLost(Throwable throwable) {
            }
        });

        try {
            mqttClient.subscribe(pubTopic);
            logger.info("Camera MQTT Subscribed: " + pubTopic);
            mqttSendToCamera(new byte[]{0});
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------------------------
    public ExecutorService mqttSendToCameraPool = Executors.newFixedThreadPool(1);
    public void mqttSendToCamera(byte[] payload) {
        if(mqttClient != null && mqttClient.isConnected()) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        mqttClient.publish(subTopic, payload, 0, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            mqttSendToCameraPool.submit(runnable);
        }
    }
}
