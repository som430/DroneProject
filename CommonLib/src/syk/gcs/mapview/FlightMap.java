package syk.gcs.mapview;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.SplitPane;

public class FlightMap {
    public SplitPane ui;
    public FlightMapController controller;

    public FlightMap() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FlightMap.fxml"));
            ui = loader.load();
            controller = loader.getController();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void setApiKey(String apiKey) {
        controller.initWebView(apiKey);
    }
}
