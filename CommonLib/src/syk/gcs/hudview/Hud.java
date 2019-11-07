package syk.gcs.hudview;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;

public class Hud {
    public StackPane ui;
    public HudController controller;

    public Hud() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Hud.fxml"));
            ui = loader.load();
            controller = loader.getController();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
