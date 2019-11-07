package syk.gcs.messageview;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

public class MessageView {
    public VBox ui;
    public MessageViewController controller;

    public MessageView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MessageView.fxml"));
            ui = loader.load();
            controller = loader.getController();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
