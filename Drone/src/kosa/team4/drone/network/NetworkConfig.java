package kosa.team4.drone.network;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class NetworkConfig {
    public String mqttBrokerConnStr;
    public String droneTopic;

    public NetworkConfig() {
        try {
            Properties properties = new Properties();
            properties.load(new FileReader(NetworkConfig.class.getResource("networkconfig.properties").getPath()));
            mqttBrokerConnStr = properties.getProperty("mqttBrokerConnStr");
            droneTopic = properties.getProperty("droneTopic");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
