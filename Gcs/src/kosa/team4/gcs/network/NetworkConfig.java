package kosa.team4.gcs.network;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.util.Properties;

public class NetworkConfig {
    //------------------------------------------------------------------------------------------
    private static Logger logger = LoggerFactory.getLogger(NetworkConfig.class);
    private static NetworkConfig instance = new NetworkConfig();
    public String mqttBrokerConnStr;
    public String droneTopic;
    public AnchorPane ui;
    public NetworkConfigController controller;
    //------------------------------------------------------------------------------------------
    private NetworkConfig() {
        try {
            Properties properties = new Properties();
            properties.load(new FileReader(NetworkConfig.class.getResource("networkconfig.properties").getPath()));
            mqttBrokerConnStr = properties.getProperty("mqttBrokerConnStr");
            droneTopic = properties.getProperty("droneTopic");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("NetworkConfig.fxml"));
            ui = loader.load();
            controller = loader.getController();

            controller.txtMqttBrokerConnStr.setText(mqttBrokerConnStr);
            controller.txtDroneTopic.setText(droneTopic);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    //------------------------------------------------------------------------------------------
    public static NetworkConfig getInstance() {
        return instance;
    }
}
