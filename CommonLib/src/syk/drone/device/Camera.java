package syk.drone.device;

import org.eclipse.paho.client.mqttv3.*;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Camera {
    //----------------------------------------------------------------------------
	private static Logger logger = LoggerFactory.getLogger(Camera.class);
    //----------------------------------------------------------------------------
    private VideoCapture videoCapture;
    private double angle;
    public void cameraConnect(int cameraNo, int width, int height, double angle) {
        this.angle = angle;
        while (true) {
            try {
                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                videoCapture = new VideoCapture(cameraNo);
                videoCapture.set(org.opencv.videoio.Videoio.CV_CAP_PROP_FRAME_WIDTH, width); //320
                videoCapture.set(org.opencv.videoio.Videoio.CV_CAP_PROP_FRAME_HEIGHT, height); //240
                if(videoCapture.isOpened()) {
                    logger.info("Camera Connected: " + cameraNo);
                    break;
                } else {
                    throw new Exception("Camera is not opened");
                }
            } catch (Exception e) {
                e.printStackTrace();
                try { videoCapture.release(); } catch (Exception e1) {}
                try { Thread.sleep(1000); } catch (InterruptedException e1) {}
            }
        }
        captureFrame();
    }
    //----------------------------------------------------------------------------
    private byte[] image;
    private void captureFrame() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Mat srcMat = new Mat();
                Mat mat = new Mat();
                MatOfByte matOfByte = new MatOfByte();
                while (true) {
                    try {
                        if (videoCapture.isOpened()) {
                            if(angle == 0) {
                                videoCapture.read(mat);
                            } else {
                                videoCapture.read(srcMat);
                                Point center = new Point(srcMat.width()/2, srcMat.height()/2);
                                double scale = 1.0;
                                Mat mapMatrix = Imgproc.getRotationMatrix2D(center, angle, scale);
                                Imgproc.warpAffine(srcMat, mat, mapMatrix, new Size(srcMat.width(), srcMat.height()));
                            }
                            Imgcodecs.imencode(".jpg", mat, matOfByte);
                            image = matOfByte.toArray();
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
    }
    //----------------------------------------------------------------------------
    private MqttClient mqttClient;
    private String pubTopic;
    private String subTopic;
    public void mattConnect(String mqttBrokerConnStr, String pubTopic, String subTopic) {
        this.pubTopic = pubTopic;
        this.subTopic = subTopic;
        while (true) {
            try {
                mqttClient = new MqttClient(mqttBrokerConnStr, MqttClient.generateClientId(), null);
                MqttConnectOptions options = new MqttConnectOptions();
                options.setConnectionTimeout(5);
                options.setAutomaticReconnect(true);
                mqttClient.connect(options);
                logger.info("Camera MQTT Connected: " + mqttBrokerConnStr);
                break;
            } catch (Exception e) {
                e.printStackTrace();
                try { mqttClient.close(); } catch (Exception e1) {}
                try { Thread.sleep(1000); } catch (InterruptedException e1) {}

            }
        }
        mqttReceiveFromGcs();
    }
    //----------------------------------------------------------------------------
    private ExecutorService mqttReceiveFromGcsPool = Executors.newFixedThreadPool(1);
    private void mqttReceiveFromGcs() {
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                Runnable task = () -> {
                    if(image != null) {
                        mqttSendToGcs(image);
                    } else {
                        mqttSendToGcs(new byte[] {0});
                    }
                };
                mqttReceiveFromGcsPool.submit(task);
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
            logger.info("Camera MQTT subscribed: " + subTopic);
            mqttSendToGcs(new byte[]{0});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------------------------
    private ExecutorService mqttSendToGcsPool = Executors.newFixedThreadPool(1);
    private void mqttSendToGcs(byte[] image) {
        if(mqttClient != null && mqttClient.isConnected()) {
            Runnable runnable = () -> {
                try {
                    mqttClient.publish(pubTopic, image, 0, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            mqttSendToGcsPool.submit(runnable);
        }
    }
}
