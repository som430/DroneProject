package kosa.team4.gcs.network;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;

public class NetworkConfigController implements Initializable {
	//------------------------------------------------------------------------------------
	private static Logger logger = LoggerFactory.getLogger(NetworkConfigController.class);
	//------------------------------------------------------------------------------------
	@FXML public TextField txtMqttBrokerConnStr;
	@FXML public TextField txtDroneTopic;
	@FXML public Button btnApply;
	@FXML public Button btnCancel;
	//------------------------------------------------------------------------------------
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		btnApply.setOnAction(btnApplyEventHandler);
		btnCancel.setOnAction(btnCancelEventHandler);
	}
	//------------------------------------------------------------------------------------
	private EventHandler<ActionEvent> btnApplyEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			NetworkConfig networkConfig = NetworkConfig.getInstance();
			networkConfig.mqttBrokerConnStr = txtMqttBrokerConnStr.getText();
			networkConfig.droneTopic = txtDroneTopic.getText();

			try {
				PrintWriter pw = new PrintWriter(getClass().getResource("networkconfig.properties").getPath());
				pw.println("mqttBrokerConnStr=" + networkConfig.mqttBrokerConnStr);
				pw.println("droneTopic=" + networkConfig.droneTopic);
				pw.flush();
				pw.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}

			Stage dialog = (Stage)txtDroneTopic.getScene().getWindow();
			dialog.getScene().setRoot(new AnchorPane());
			dialog.close();
		}
	};
	//------------------------------------------------------------------------------------
	private EventHandler<ActionEvent> btnCancelEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			Stage dialog = (Stage)txtDroneTopic.getScene().getWindow();
			dialog.getScene().setRoot(new AnchorPane());
			dialog.close();
		}
	};
	//------------------------------------------------------------------------------------
}
