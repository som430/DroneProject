package syk.gcs.messageview;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class MessageViewController implements Initializable {
	//---------------------------------------------------------------------------------
	private static Logger logger = LoggerFactory.getLogger(MessageViewController.class);
	//---------------------------------------------------------------------------------
	@FXML private Button btnReceiveMessage;
	@FXML private Button btnSendMessage;

	@FXML private TextField txtReceiveMsgId;
	@FXML private Button btnReceiveFilter;
	@FXML private Button btnReceiveShowHide;
	@FXML private Button btnReceiveClear;

	@FXML private TextField txtSendMsgId;
	@FXML private Button btnSendFilter;
	@FXML private Button btnSendShowHide;
	@FXML private Button btnSendClear;

	@FXML private ListView listReceiveMessage;
	@FXML private ListView listSendMessage;


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		btnReceiveMessage.setOnAction(btnReceiveMessageEventHandler);
		btnSendMessage.setOnAction(btnSendMessageEventHandler);

		btnReceiveFilter.setOnAction(btnReceiveFilterEventHandler);
		btnReceiveShowHide.setOnAction(btnReceiveShowHideEventHandler);
		btnReceiveClear.setOnAction(btnReceiveClearEventHandler);

		btnSendFilter.setOnAction(btnSendFilterEventHandler);
		btnSendShowHide.setOnAction(btnSendShowHideEventHandler);
		btnSendClear.setOnAction(btnSendClearEventHandler);

		filterReceiveMsgIds = txtReceiveMsgId.getText().split(",");
		filterSendMsgIds = txtSendMsgId.getText().split(",");

		listReceiveMessage.setOnMouseClicked(listReceiveMessageEventHandler);
		listSendMessage.setOnMouseClicked(listSendMessageEventHandler);
	}
	//---------------------------------------------------------------------------------
	private long countReceive;
	private String[] filterReceiveMsgIds = {};
	private boolean showHideReceiveMsg = true;
	public void addReceiveMessage(JSONObject jsonObject) {
		if(showHideReceiveMsg) {
			String newItem = "(" + countReceive++ + ") [" + jsonObject.getString("msgid") + "]  " + jsonObject.toString();
			if (filterReceiveMsgIds.length != 0) {
				for (String filterMsgId : filterReceiveMsgIds) {
					if (filterMsgId.equals(jsonObject.getString("msgid"))) {
						Platform.runLater(()->{
							//listReceiveMessage.getItems().add("(" + countReceive++ + ") [" + jsonObject.getString("msgid") + "]  " + jsonObject.toString());
							listReceiveMessage.getItems().add(newItem);
						});
					}
				}
			} else {
				Platform.runLater(()->{
					//listReceiveMessage.getItems().add("(" + countReceive++ + ") [" + jsonObject.getString("msgid") + "]  " + jsonObject.toString());
					listReceiveMessage.getItems().add(newItem);
				});
			}
			Platform.runLater(()-> {
				//listReceiveMessage.scrollTo(listReceiveMessage.getItems().size() - 1);
				listReceiveMessage.scrollTo(newItem);
			});
		}
	}
	//---------------------------------------------------------------------------------
	private long countSend;
	private String[] filterSendMsgIds = {};
	private boolean showHideSendMsg = true;
	public void addSendMessage(JSONObject jsonObject) {
		if(showHideSendMsg) {
			if (filterSendMsgIds.length != 0) {
				for (String filterMsgId : filterSendMsgIds) {
					if (filterMsgId.equals(jsonObject.getString("msgid"))) {
						Platform.runLater(()-> {
							listSendMessage.getItems().add("(" + countSend++ + ") [" + jsonObject.getString("msgid") + "]  " + jsonObject.toString());
						});
					}
				}
			} else {
				Platform.runLater(()-> {
					listSendMessage.getItems().add("(" + countSend++ + ") [" + jsonObject.getString("msgid") + "]  " + jsonObject.toString());
				});
			}
			Platform.runLater(()-> {
				listSendMessage.scrollTo(listSendMessage.getItems().size() - 1);
			});
		}
	}
	//---------------------------------------------------------------------------------
	@FXML private VBox receivePane;
	@FXML private VBox sendPane;
	private EventHandler<ActionEvent> btnReceiveMessageEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			receivePane.setVisible(true);
			sendPane.setVisible(false);
		}
	};
	//---------------------------------------------------------------------------------
	private EventHandler<ActionEvent> btnSendMessageEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			receivePane.setVisible(false);
			sendPane.setVisible(true);
		}
	};
	//---------------------------------------------------------------------------------
	private EventHandler<ActionEvent> btnReceiveFilterEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			if(txtReceiveMsgId.getText().trim().equals("")) {
				filterReceiveMsgIds = new String[] {};
			} else {
				filterReceiveMsgIds = txtReceiveMsgId.getText().split(",");
			}
		}
	};
	//---------------------------------------------------------------------------------
	private EventHandler<ActionEvent> btnReceiveShowHideEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			showHideReceiveMsg = !showHideReceiveMsg;
			if(showHideReceiveMsg) {
				btnReceiveShowHide.setText("Hide");
			} else {
				btnReceiveShowHide.setText("Show");
			}
		}
	};
	//---------------------------------------------------------------------------------
	private EventHandler<ActionEvent> btnReceiveClearEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			listReceiveMessage.getItems().clear();
		}
	};
	//---------------------------------------------------------------------------------
	private EventHandler<ActionEvent> btnSendFilterEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			if(txtSendMsgId.getText().trim().equals("")) {
				filterSendMsgIds = new String[] {};
			} else {
				filterSendMsgIds = txtSendMsgId.getText().split(",");
			}
		}
	};
	//---------------------------------------------------------------------------------
	private EventHandler<ActionEvent> btnSendShowHideEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			showHideSendMsg = !showHideSendMsg;
			if(showHideSendMsg) {
				btnSendShowHide.setText("Hide");
			} else {
				btnSendShowHide.setText("Show");
			}
		}
	};
	//---------------------------------------------------------------------------------
	private EventHandler<ActionEvent> btnSendClearEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			listSendMessage.getItems().clear();
		}
	};
	//---------------------------------------------------------------------------------
	private EventHandler<MouseEvent> listReceiveMessageEventHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			if(event.getClickCount() == 2) {
				String json = (String) listReceiveMessage.getSelectionModel().getSelectedItem();
				int start = json.indexOf("[") + 1;
				int end = json.indexOf("]");
				String msgId = json.substring(start, end);
				String str = txtReceiveMsgId.getText().trim();
				if (str.equals("")) {
					txtReceiveMsgId.setText(msgId);
				} else {
					if (!str.contains(msgId)) {
						txtReceiveMsgId.setText(txtReceiveMsgId.getText() + "," + msgId);
					}
				}
			}
		}
	};
	//---------------------------------------------------------------------------------
	private EventHandler<MouseEvent> listSendMessageEventHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			if(event.getClickCount() == 2) {
				String json = (String) listSendMessage.getSelectionModel().getSelectedItem();
				int start = json.indexOf("[") + 1;
				int end = json.indexOf("]");
				String msgId = json.substring(start, end);
				String str = txtSendMsgId.getText().trim();
				if (str.equals("")) {
					txtSendMsgId.setText(msgId);
				} else {
					if (!str.contains(msgId)) {
						txtSendMsgId.setText(txtSendMsgId.getText() + "," + msgId);
					}
				}
			}
		}
	};
}
