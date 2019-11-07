package syk.gcs.cameraview;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

public class CameraView {
    public VBox ui;
    public CameraViewController controller;

    public CameraView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CameraView.fxml"));
            ui = loader.load();
            controller = loader.getController();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
