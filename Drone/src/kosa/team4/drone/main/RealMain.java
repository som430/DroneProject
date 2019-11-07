/*
java -Djava.library.path=/usr/lib/jni:/home/pi/opencv/opencv-3.4.5/build/lib -cp classes:lib/'*' companion.companion.RealMain
 */

package kosa.team4.drone.main;

import kosa.team4.drone.network.NetworkConfig;
import syk.drone.device.Camera;
import syk.drone.device.FlightController;


public class RealMain {
    public static void main(String[] args) {
        NetworkConfig networkConfig = new NetworkConfig();

        FlightController flightController = new FlightController();
        flightController.mavlinkConnectRxTx("/dev/ttyAMA0");
        flightController.mqttConnect(
                networkConfig.mqttBrokerConnStr,
                networkConfig.droneTopic +"/fc/pub",
                networkConfig.droneTopic +"/fc/sub"
        );

        Camera camera0 = new Camera();
        camera0.cameraConnect(0, 320, 240, 0);
        camera0.mattConnect(
                networkConfig.mqttBrokerConnStr,
                networkConfig.droneTopic + "/cam0/pub",
                networkConfig.droneTopic + "/cam0/sub"
        );

        Camera camera1 = new Camera();
        camera1.cameraConnect(1, 320, 240, 270);
        camera1.mattConnect(
                networkConfig.mqttBrokerConnStr,
                networkConfig.droneTopic +"/cam1/pub",
                networkConfig.droneTopic +"/cam1/sub"
        );
    }
}
