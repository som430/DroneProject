package syk.gcs.mapview;

import syk.common.MavJsonMessage;
import syk.gcs.dialog.AlertDialog;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.StringConverter;
import netscape.javascript.JSObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;

public class FlightMapController implements Initializable {
    //--------------------------------------------------------------------------------------
    private static Logger logger = LoggerFactory.getLogger(FlightMapController.class);
    //--------------------------------------------------------------------------------------
    @FXML private Button btnAddTakeoff;
    @FXML private Button btnAddLand;
    @FXML private Button btnAddRTL;
    @FXML private Button btnAddROI;
    @FXML private Button btnAddJump;
    @FXML private Button btnAddDelay;
    @FXML private Button btnAddAction;
    @FXML private Button btnRemoveMissionItem;
    @FXML private Button btnItemUp;
    @FXML private Button btnItemDown;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	btnAddTakeoff.setOnAction(btnAddTakeoffEventHandler);
    	btnAddLand.setOnAction(btnAddLandEventHandler);
    	btnAddRTL.setOnAction(btnAddRTLEventHandler);
    	btnAddROI.setOnAction(btnAddROIEventHandler);
    	btnAddJump.setOnAction(btnAddJumpEventHandler);
        btnAddDelay.setOnAction(btnAddDelayEventHandler);
        btnAddAction.setOnAction(btnAddActionEventHandler);
    	btnRemoveMissionItem.setOnAction(btnRemoveMissionItemEventHandler);

        btnItemUp.setOnAction(btnItemUpEventHandler);
        btnItemDown.setOnAction(btnItemDownEventHandler);
    	
        //initWebView();
        initMissionTableView();
    }
    //--------------------------------------------------------------------------------------
    @FXML private WebView webView;
    @FXML private Slider zoomSlider;
    private WebEngine webEngine;
    /*private void initWebView() {
        webEngine = webView.getEngine();
        webEngine.getLoadWorker().stateProperty().addListener(webEngineLoadStateChangeListener);
        webEngine.load(FlightMapController.class.getResource("map/map.html").toExternalForm());
        zoomSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int value = newValue.intValue();
                jsproxy.call("setMapZoom", value);
            }
        });
    }*/
    public void initWebView(String apiKey) {
        webEngine = webView.getEngine();
        webEngine.getLoadWorker().stateProperty().addListener(webEngineLoadStateChangeListener);
        String html = "";
        html += "<!DOCTYPE html>";
        html += "<html>";
        html += "    <head>";
        html += "        <meta charset='utf-8'>";
        html += "        <style>";
        html += "           #map { height: 100%; }";
        html += "           html, body { height: 100%; margin: 0; padding: 0; }";
        html += "        </style>";
        html += "        <script src='" + FlightMapController.class.getResource("map/jsproxy.js").toExternalForm() + "'></script>";
        html += "        <script src='" + FlightMapController.class.getResource("map/uav.js").toExternalForm() + "'></script>";
        html += "        <script src='" + FlightMapController.class.getResource("map/map.js").toExternalForm() + "'></script>";
        html += "    </head>";
        html += "    <body>";
        html += "        <div id='map'></div>";
        html += "        <script src='https://maps.googleapis.com/maps/api/js?key=" + apiKey + "&libraries=geometry,drawing&callback=map.init' async defer></script>";
        html += "    </body>";
        html += "</html>";
        webEngine.loadContent(html);
        zoomSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int value = newValue.intValue();
                jsproxy.call("setMapZoom", value);
            }
        });
    }
    //--------------------------------------------------------------------------------------
    private JSObject jsproxy;
    private ChangeListener<Worker.State> webEngineLoadStateChangeListener =
            new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue<? extends Worker.State> observable,
                                    Worker.State oldValue,
                                    Worker.State newValue) {
                    if(newValue == Worker.State.SUCCEEDED) {
                        jsproxy = (JSObject) webEngine.executeScript("jsproxy");
                        jsproxy.setMember("java", FlightMapController.this);
                    }
                }
            };
    //--------------------------------------------------------------------------------------
    public void javascriptLog(String message) {
        logger.info(message);
    }
    //--------------------------------------------------------------------------------------
    public void setZoomSliderValue(int zoom) {
        zoomSlider.setValue(zoom);
    }
    //--------------------------------------------------------------------------------------
    @FXML private TableView<MissionItem> missionTableView;
    private void initMissionTableView() {
        missionTableView.setEditable(true);
        missionTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<MissionItem, Integer> column1 = new TableColumn<MissionItem, Integer>("SEQ");
        column1.setCellValueFactory(new PropertyValueFactory<MissionItem, Integer>("seq"));
        column1.setMinWidth(50);
        column1.setMaxWidth(50);
        column1.setSortable(false);
        column1.impl_setReorderable(false); //헤더를 클릭하면 멈춤 현상을 없애기 위해
        missionTableView.getColumns().add(column1);

        TableColumn<MissionItem, String> column2 = new TableColumn<MissionItem, String>("COMMAND");
        column2.setCellValueFactory(new PropertyValueFactory<MissionItem, String>("strCommand"));
        column2.setMinWidth(100);
        column2.setMaxWidth(100);
        column2.setSortable(false);
        column2.impl_setReorderable(false);
        missionTableView.getColumns().add(column2);

        TableColumn<MissionItem, Float> column3 = new TableColumn<MissionItem, Float>("P1");
        column3.setCellValueFactory(new PropertyValueFactory<MissionItem, Float>("param1"));
        column3.setMinWidth(50);
        column3.setMaxWidth(50);
        column3.setSortable(false);
        column3.impl_setReorderable(false);
        Callback<TableColumn<MissionItem, Float>, TableCell<MissionItem, Float>> floatCellFactory =
                new Callback<TableColumn<MissionItem, Float>, TableCell<MissionItem, Float>>() {
                    @Override
                    public TableCell<MissionItem, Float> call(TableColumn<MissionItem, Float> p) {
                        return new TextFieldTableCell(new StringConverter<Float>() {
                            @Override
                            public String toString(Float object) {
                                return object.toString();
                            }
                            @Override
                            public Float fromString(String string) {
                                return Float.parseFloat(string);
                            }
                        });
                    }
                };
        column3.setCellFactory(floatCellFactory);
        column3.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<MissionItem, Float>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<MissionItem, Float> event) {
                        MissionItem item = (MissionItem)event.getTableView().getItems().get(event.getTablePosition().getRow());
                        item.setParam1(event.getNewValue());
                        //missionMapSync();
                    }
                }
        );
        missionTableView.getColumns().add(column3);

        TableColumn<MissionItem, Float> column4 = new TableColumn<MissionItem, Float>("P2");
        column4.setCellValueFactory(new PropertyValueFactory<MissionItem, Float>("param2"));
        column4.setMinWidth(50);
        column4.setMaxWidth(50);
        column4.setSortable(false);
        column4.impl_setReorderable(false);
        column4.setCellFactory(floatCellFactory);
        column4.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<MissionItem, Float>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<MissionItem, Float> event) {
                        MissionItem item = (MissionItem)event.getTableView().getItems().get(event.getTablePosition().getRow());
                        item.setParam2(event.getNewValue());
                        //missionMapSync();
                    }
                }
        );
        missionTableView.getColumns().add(column4);

        TableColumn<MissionItem, Float> column5 = new TableColumn<MissionItem, Float>("P3");
        column5.setCellValueFactory(new PropertyValueFactory<MissionItem, Float>("param3"));
        column5.setMinWidth(50);
        column5.setMaxWidth(50);
        column5.setSortable(false);
        column5.impl_setReorderable(false);
        column5.setCellFactory(floatCellFactory);
        column5.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<MissionItem, Float>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<MissionItem, Float> event) {
                        MissionItem item = (MissionItem)event.getTableView().getItems().get(event.getTablePosition().getRow());
                        item.setParam3(event.getNewValue());
                        //missionMapSync();
                    }
                }
        );
        missionTableView.getColumns().add(column5);

        TableColumn<MissionItem, Float> column6 = new TableColumn<MissionItem, Float>("P4");
        column6.setCellValueFactory(new PropertyValueFactory<MissionItem, Float>("param4"));
        column6.setMinWidth(50);
        column6.setMaxWidth(50);
        column6.setSortable(false);
        column6.impl_setReorderable(false);
        column6.setCellFactory(floatCellFactory);
        column6.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<MissionItem, Float>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<MissionItem, Float> event) {
                        MissionItem item = (MissionItem)event.getTableView().getItems().get(event.getTablePosition().getRow());
                        item.setParam4(event.getNewValue());
                        //missionMapSync();
                    }
                }
        );
        missionTableView.getColumns().add(column6);

        TableColumn<MissionItem, Double> column7 = new TableColumn<>("LAT");
        column7.setCellValueFactory(new PropertyValueFactory<MissionItem, Double>("x"));
        column7.setMinWidth(100);
        column7.setMaxWidth(100);
        column7.setSortable(false);
        column7.impl_setReorderable(false);
        Callback<TableColumn<MissionItem, Double>, TableCell<MissionItem, Double>> doubleCellFactory =
                new Callback<TableColumn<MissionItem, Double>, TableCell<MissionItem, Double>>() {
                    @Override
                    public TableCell<MissionItem, Double> call(TableColumn<MissionItem, Double> p) {
                        return new TextFieldTableCell(new StringConverter<Double>() {
                            @Override
                            public String toString(Double object) {
                                return object.toString();
                            }
                            @Override
                            public Double fromString(String string) {
                                return Double.parseDouble(string);
                            }
                        });
                    }
                };
        column7.setCellFactory(doubleCellFactory);
        column7.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<MissionItem, Double>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<MissionItem, Double> event) {
                        MissionItem item = (MissionItem)event.getTableView().getItems().get(event.getTablePosition().getRow());
                        item.setX(event.getNewValue());
                        //missionMapSync();
                    }
                }
        );
        missionTableView.getColumns().add(column7);

        TableColumn<MissionItem, Double> column8 = new TableColumn<>("LNG");
        column8.setCellValueFactory(new PropertyValueFactory<MissionItem, Double>("y"));
        column8.setMinWidth(100);
        column8.setMaxWidth(100);
        column8.setSortable(false);
        column8.impl_setReorderable(false);
        column8.setCellFactory(doubleCellFactory);
        column8.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<MissionItem, Double>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<MissionItem, Double> event) {
                        MissionItem item = (MissionItem)event.getTableView().getItems().get(event.getTablePosition().getRow());
                        item.setY(event.getNewValue());
                        //missionMapSync();
                    }
                }
        );
        missionTableView.getColumns().add(column8);

        TableColumn<MissionItem, Float> column9 = new TableColumn<MissionItem, Float>("ALT");
        column9.setCellValueFactory(new PropertyValueFactory<MissionItem, Float>("z"));
        column9.setMinWidth(50);
        column9.setMaxWidth(50);
        column9.setSortable(false);
        column9.impl_setReorderable(false);
        column9.setCellFactory(floatCellFactory);
        column9.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<MissionItem, Float>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<MissionItem, Float> event) {
                        MissionItem item = (MissionItem)event.getTableView().getItems().get(event.getTablePosition().getRow());
                        item.setZ(event.getNewValue());
                        //missionMapSync();
                    }
                }
        );
        missionTableView.getColumns().add(column9);

        TableColumn<MissionItem, String> column10 = new TableColumn<MissionItem, String>("Desc");
        missionTableView.getColumns().add(column10);

        missionTableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<MissionItem>() {
            @Override
            public void onChanged(Change<? extends MissionItem> c) {
                List<Integer> list = missionTableView.getSelectionModel().getSelectedIndices();
                if(list.size() > 0) {
                    JSONArray jsonArray = new JSONArray();
                    for (int index : list) {
                        jsonArray.put(index);
                    }
                    String strIndices = jsonArray.toString();
                    jsproxy.call("missionMapSelectItem", strIndices);
                }
            }
        });
    }
	//--------------------------------------------------------------------------------------   
    private Map<String, MapListener> mapListeners = new HashMap<>();
    
    public void mapListenerAdd(String msgid, MapListener listener) {
    	mapListeners.put(msgid, listener);
    }

    public void mapListenerRemove(String msgid) {
    	mapListeners.remove(msgid);
    }
	//-------------------------------------------------------------------------------------- 
    public void receiveFromMap(String json) {
    	JSONObject jsonMessage = new JSONObject(json);
    	MapListener mapListener = mapListeners.get(jsonMessage.get("msgid"));
    	mapListener.receive(jsonMessage);
    }
    //--------------------------------------------------------------------------------------
    private List<MissionItem> missionItems;
    public void missionTableViewSync(String strMissionItems) {
    	missionItems = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(strMissionItems);
        for(int i=0; i<jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            MissionItem msg = new MissionItem();
            msg.setSeq(jsonObject.getInt("seq"));
            msg.setCommand(jsonObject.getInt("command"));
            msg.setParam1(jsonObject.getFloat("param1"));
            msg.setParam2(jsonObject.getFloat("param2"));
            msg.setParam3(jsonObject.getFloat("param3"));
            msg.setParam4(jsonObject.getFloat("param4"));
            msg.setX((int)(jsonObject.getDouble("x") * 10000000) / 10000000.0);
            msg.setY((int)(jsonObject.getDouble("y") * 10000000) / 10000000.0);
            msg.setZ(jsonObject.getFloat("z"));
            missionItems.add(msg);
        }
        missionTableView.setItems(FXCollections.observableList(missionItems));
        missionTableView.refresh();
        missionTableView.getSelectionModel().clearSelection();
        missionTableView.getSelectionModel().select(moveSelectedIndex);
    }    
	//--------------------------------------------------------------------------------------
    public void setCurrLocation(double currLat, double currLng, double heading) {
    	Platform.runLater(() -> {
            jsproxy.call("setCurrLocation", currLat, currLng, heading);
    	});
    }
	//--------------------------------------------------------------------------------------
    public void setHomePosition(double homeLat, double homeLng) {
    	Platform.runLater(() -> {
			jsproxy.call("setHomePosition", homeLat, homeLng);
		});
    }
	//--------------------------------------------------------------------------------------
    private String mode = "";
    public void setMode(String mode) {
    	this.mode = mode;
    	Platform.runLater(() -> {
    		if(mode.equals("GUIDED")) {
    			jsproxy.call("setMode", 4);
    		} else if(mode.equals("AUTO")) {
    			jsproxy.call("setMode", 3);
    		} else if(mode.equals("RTL")) {
    			jsproxy.call("setMode", 6);
    		} else if(mode.equals("LAND")) {
                jsproxy.call("setMode", 9);
            }
		});
    }    
	//--------------------------------------------------------------------------------------    
    public void manualMake(double alt) {
    	Platform.runLater(() -> {
    		jsproxy.call("manualMake", alt);
    	});
    }
    //-------------------------------------------------------------------------------------- 
    public void missionMake() {
    	Platform.runLater(() -> {   	
	        jsproxy.call("missionMake");
    	});    
    }
    //-------------------------------------------------------------------------------------- 
    public void missionClear() {
    	Platform.runLater(() -> {
    		List<MissionItem> missionItems = missionTableView.getItems();
    		List<MissionItem> copy = new ArrayList<>(missionItems);
    		for(int i=(copy.size()-1); i>0; i--) {
    			missionItems.remove(copy.get(i));
    		}
    		missionTableView.refresh();
    		jsproxy.call("missionClear");
    	}); 
    }
    //--------------------------------------------------------------------------------------
    public JSONArray getMissionItems() {
    	JSONArray jsonArray = new JSONArray();
    	
    	List<MissionItem> missionItems = missionTableView.getItems();
    	for(MissionItem mi : missionItems) {
    		JSONObject jsonObject = new JSONObject();
    		jsonObject.put("seq", mi.getSeq());
    		jsonObject.put("command", mi.getCommand());
    		jsonObject.put("param1", mi.getParam1());
    		jsonObject.put("param2", mi.getParam2());
    		jsonObject.put("param3", mi.getParam3());
    		jsonObject.put("param4", mi.getParam4());
    		jsonObject.put("x", mi.getX());
    		jsonObject.put("y", mi.getY());
    		jsonObject.put("z", mi.getZ());
    		jsonArray.put(jsonObject);
    	}
    	
    	return jsonArray;
    }
    //--------------------------------------------------------------------------------------
    public void setMissionItems(JSONArray jsonArray) {
    	missionItems = new ArrayList<>();
        for(int i=0; i<jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            MissionItem msg = new MissionItem();
            msg.setSeq(jsonObject.getInt("seq"));
            msg.setCommand(jsonObject.getInt("command"));
            msg.setParam1(jsonObject.getFloat("param1"));
            msg.setParam2(jsonObject.getFloat("param2"));
            msg.setParam3(jsonObject.getFloat("param3"));
            msg.setParam4(jsonObject.getFloat("param4"));

            if(msg.getStrCommand().equals("RTL")) {
            	msg.setX(jsonArray.getJSONObject(0).getDouble("x") / 10000000.0);
                msg.setY(jsonArray.getJSONObject(0).getDouble("y") / 10000000.0);
                msg.setZ(jsonObject.getFloat("z"));
            } else if(msg.getStrCommand().equals("DELAY") || msg.getStrCommand().equals("ACTION")) {
                //FC에서 모두 0으로 세팅해서 보내므로 재설정 필요
                msg.setX(missionItems.get(missionItems.size()-1).getX());
                msg.setY(missionItems.get(missionItems.size()-1).getY());
                msg.setZ(missionItems.get(missionItems.size()-1).getZ());
            } else {
                msg.setX(jsonObject.getDouble("x") / 10000000.0);
                msg.setY(jsonObject.getDouble("y") / 10000000.0);
                msg.setZ(jsonObject.getFloat("z"));
            }

            missionItems.add(msg);
        }
        
        Platform.runLater(() -> {
            missionTableView.setItems(FXCollections.observableList(missionItems));
            missionTableView.refresh();
            missionMapSync();
        });
    }

    public void setMissionItems(double lat, double lng) {
        ObservableList<MissionItem> list = missionTableView.getItems();
        if(list.size()>1) {
            list.remove(1, list.size());
        }

        MissionItem msg = new MissionItem();
        msg.setSeq(1);
        msg.setCommand(MavJsonMessage.MAVJSON_MISSION_COMMAND_WAYPOINT);
        msg.setX(lat);
        msg.setY(lng);
        msg.setZ(10);
        list.add(msg);

        Platform.runLater(() -> {
            missionMapSync();
        });
    }
    //--------------------------------------------------------------------------------------
    public void setMissionCurrent(int seq) {
    	Platform.runLater(() -> {
    		jsproxy.call("setMissionCurrent", seq);
    		if (mode.equals("AUTO")) {
                missionTableView.getSelectionModel().clearSelection();
                ObservableList<MissionItem> list = missionTableView.getItems();
                for(int i=0; i<list.size(); i++) {
                    MissionItem item = list.get(i);
                    if(item.getSeq() == seq) {
                        missionTableView.getSelectionModel().select(item);
                    }
                }
            }
    	});
    }
    //--------------------------------------------------------------------------------------
    public void missionMapSync() {
        JSONArray jsonArray = new JSONArray();
        for(MissionItem mi : missionItems) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("seq", mi.getSeq());
            jsonObject.put("command", mi.getCommand());
            jsonObject.put("param1", mi.getParam1());
            jsonObject.put("param2", mi.getParam2());
            jsonObject.put("param3", mi.getParam3());
            jsonObject.put("param4", mi.getParam4());
            jsonObject.put("x", mi.getX());
            jsonObject.put("y", mi.getY());
            jsonObject.put("z", mi.getZ());
            jsonArray.put(jsonObject);
        }
        String strMissionItems = jsonArray.toString();
        jsproxy.call("missionMapSync", strMissionItems);
    }    
    //--------------------------------------------------------------------------------------
    @FXML private Label lblInfo;
    private Thread infoLabelThread;
    public void showInfoLabel(String info) {
        if(infoLabelThread != null) {
            infoLabelThread.interrupt();
        }
        infoLabelThread = new Thread() {
            @Override
            public void run() {
                try {
                    Platform.runLater(()->{
                        lblInfo.setText(info);
                    });
                    Thread.sleep(3000);
                    Platform.runLater(()->{
                        lblInfo.setText("");
                    });
                } catch(Exception e) {}
            }
        };
        infoLabelThread.setDaemon(true);
        infoLabelThread.start();
    }	
	//--------------------------------------------------------------------------------------
    private EventHandler<ActionEvent> btnAddTakeoffEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
	        int selectedIndex = missionTableView.getSelectionModel().getSelectedIndex();
	        if(selectedIndex == -1) {
	            AlertDialog.showOkButton("알림", "미션 아이템이 선택되지 않았습니다.");
	            return;
	        }

	        MissionItem selectedMissionItem = missionTableView.getSelectionModel().getSelectedItem();

	        MissionItem missionItem = new MissionItem();
	        missionItem.setStrCommand("TAKEOFF");
	        missionItem.setX(selectedMissionItem.getX());
	        missionItem.setY(selectedMissionItem.getY());
	        missionItem.setZ(10);
	        missionItems.add(selectedIndex+1, missionItem);
	        for(int i=0; i<missionItems.size(); i++) {
	            missionItems.get(i).setSeq(i);
	        }
	        missionTableView.getSelectionModel().clearSelection();
	        missionTableView.getSelectionModel().select(selectedIndex+1);
	        missionTableView.refresh();
	        missionMapSync();
		}
	};
	//--------------------------------------------------------------------------------------
    private EventHandler<ActionEvent> btnAddLandEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
	        int selectedIndex = missionTableView.getSelectionModel().getSelectedIndex();
	        if(selectedIndex == -1) {
	            AlertDialog.showOkButton("알림", "미션 아이템이 선택되지 않았습니다.");
	            return;
	        }

	        MissionItem selectedMissionItem = missionTableView.getSelectionModel().getSelectedItem();
	        MissionItem missionItem = new MissionItem();
	        missionItem.setStrCommand("LAND");
	        missionItem.setX(selectedMissionItem.getX());
	        missionItem.setY(selectedMissionItem.getY());
	        missionItems.add(selectedIndex+1, missionItem);
	        for(int i=0; i<missionItems.size(); i++) {
	            missionItems.get(i).setSeq(i);
	        }
	        missionTableView.getSelectionModel().clearSelection();
	        missionTableView.getSelectionModel().select(selectedIndex+1);
	        missionTableView.refresh();
	        missionMapSync();
		}
	};
	//--------------------------------------------------------------------------------------
    private EventHandler<ActionEvent> btnAddRTLEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
	        int selectedIndex = missionTableView.getSelectionModel().getSelectedIndex();
	        if(selectedIndex == -1) {
	            AlertDialog.showOkButton("알림", "미션 아이템이 선택되지 않았습니다.");
	            return;
	        }

	        mapListenerAdd("getHomePosition", new MapListener() {
                @Override
                public void receive(JSONObject jsonMessage) {
                    MissionItem missionItem = new MissionItem();
                    missionItem.setStrCommand("RTL");
                    missionItem.setX(jsonMessage.getDouble("lat"));
                    missionItem.setY(jsonMessage.getDouble("lng"));
                    missionItems.add(selectedIndex+1, missionItem);
                    for(int i=0; i<missionItems.size(); i++) {
                        missionItems.get(i).setSeq(i);
                    }
                    missionTableView.getSelectionModel().clearSelection();
                    missionTableView.getSelectionModel().select(selectedIndex+1);
                    missionTableView.refresh();
                    missionMapSync();
                }
            });

            jsproxy.call("getHomePosition", selectedIndex+1);
		}
	};
	//--------------------------------------------------------------------------------------
    private EventHandler<ActionEvent> btnAddROIEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
	        int selectedIndex = missionTableView.getSelectionModel().getSelectedIndex();
	        if(selectedIndex == -1) {
	            AlertDialog.showOkButton("알림", "미션 아이템이 선택되지 않았습니다.");
	            return;
	        }

	        MissionItem selectedMissionItem = missionTableView.getSelectionModel().getSelectedItem();
	        MissionItem missionItem = new MissionItem();
	        missionItem.setStrCommand("ROI");
	        missionItem.setX(selectedMissionItem.getX());
	        missionItem.setY(selectedMissionItem.getY());
	        missionItems.add(selectedIndex+1, missionItem);
	        for(int i=0; i<missionItems.size(); i++) {
	            missionItems.get(i).setSeq(i);
	        }
	        missionTableView.getSelectionModel().clearSelection();
	        missionTableView.getSelectionModel().select(selectedIndex+1);
	        missionTableView.refresh();
	        missionMapSync();

	        jsproxy.call("roiMake", selectedIndex+1);
	        AlertDialog.showOkButton("알림", "맵에서 ROI 위치를 클릭하세요.");
		}
	};
	//--------------------------------------------------------------------------------------
    private EventHandler<ActionEvent> btnAddJumpEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
	        int selectedIndex = missionTableView.getSelectionModel().getSelectedIndex();
	        if(selectedIndex == -1) {
	            AlertDialog.showOkButton("알림", "미션 아이템이 선택되지 않았습니다.");
	            return;
	        }

	        MissionItem selectedMissionItem = missionTableView.getSelectionModel().getSelectedItem();
	        MissionItem missionItem = new MissionItem();
	        missionItem.setStrCommand("JUMP");
	        missionItem.setParam1(1);
	        missionItem.setParam2(-1);
	        missionItems.add(selectedIndex+1, missionItem);
	        for(int i=0; i<missionItems.size(); i++) {
	            missionItems.get(i).setSeq(i);
	        }
	        missionTableView.getSelectionModel().clearSelection();
	        missionTableView.getSelectionModel().select(selectedIndex+1);
	        missionTableView.refresh();
	        missionMapSync();
		}
	};
    //--------------------------------------------------------------------------------------
    private EventHandler<ActionEvent> btnAddDelayEventHandler = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            int selectedIndex = missionTableView.getSelectionModel().getSelectedIndex();
            if(selectedIndex == -1) {
                AlertDialog.showOkButton("알림", "미션 아이템이 선택되지 않았습니다.");
                return;
            }

            MissionItem selectedMissionItem = missionTableView.getSelectionModel().getSelectedItem();
            MissionItem missionItem = new MissionItem();
            missionItem.setStrCommand("DELAY");
            missionItem.setParam1(2); //a number from 1 to max number of Actuator on the vehicle
            missionItem.setParam2(0); //0=off, 1=on
            missionItem.setX(selectedMissionItem.getX());
            missionItem.setY(selectedMissionItem.getY());
            missionItem.setZ(selectedMissionItem.getZ());
            missionItems.add(selectedIndex+1, missionItem);

            for(int i=0; i<missionItems.size(); i++) {
                missionItems.get(i).setSeq(i);
            }
            missionTableView.getSelectionModel().clearSelection();
            missionTableView.getSelectionModel().select(selectedIndex+1);
            missionTableView.refresh();
            missionMapSync();
        }
    };
    //--------------------------------------------------------------------------------------
    private EventHandler<ActionEvent> btnAddActionEventHandler = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            int selectedIndex = missionTableView.getSelectionModel().getSelectedIndex();
            if(selectedIndex == -1) {
                AlertDialog.showOkButton("알림", "미션 아이템이 선택되지 않았습니다.");
                return;
            }

            MissionItem selectedMissionItem = missionTableView.getSelectionModel().getSelectedItem();
            MissionItem missionItem = new MissionItem();
            missionItem.setStrCommand("ACTION");
            missionItem.setParam1(1); //a number from 1 to max number of Actuator on the vehicle
            missionItem.setParam2(0); //0=off, 1=on
            missionItem.setX(selectedMissionItem.getX());
            missionItem.setY(selectedMissionItem.getY());
            missionItem.setZ(selectedMissionItem.getZ());
            missionItems.add(selectedIndex+1, missionItem);

            for(int i=0; i<missionItems.size(); i++) {
                missionItems.get(i).setSeq(i);
            }
            missionTableView.getSelectionModel().clearSelection();
            missionTableView.getSelectionModel().select(selectedIndex+1);
            missionTableView.refresh();
            missionMapSync();
        }
    };
	//--------------------------------------------------------------------------------------
    private EventHandler<ActionEvent> btnRemoveMissionItemEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
	        ObservableList<Integer> selectedIndices = missionTableView.getSelectionModel().getSelectedIndices();
	        List<Integer> list = new ArrayList<>(selectedIndices);
	        list.sort(new Comparator<Integer>() {
	            @Override
	            public int compare(Integer o1, Integer o2) {
	                return -o1.compareTo(o2);
	            }
	        });

	        for(int index : list) {
	            if(index != 0) {
	                missionItems.remove(index);
	            }
	        }

	        for(int i=0; i<missionItems.size(); i++) {
	            MissionItem missionItem = missionItems.get(i);
	            missionItem.setSeq(i);
	        }
	        missionTableView.refresh();
	        missionMapSync();
		}
	};
    //--------------------------------------------------------------------------------------
    private EventHandler<ActionEvent> btnItemUpEventHandler = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            ObservableList<MissionItem> list = missionTableView.getItems();
            MissionItem moveSelectedItem = missionTableView.getSelectionModel().getSelectedItem();
            moveSelectedIndex = missionTableView.getSelectionModel().getSelectedIndex();
            if(moveSelectedIndex <= 1) return;
            list.remove(moveSelectedItem);
            list.add(moveSelectedIndex-1, moveSelectedItem);
            moveSelectedIndex--;
            for(int i=0; i<missionItems.size(); i++) {
                missionItems.get(i).setSeq(i);
            }
            missionMapSync();
        }
    };
    //--------------------------------------------------------------------------------------
    private int moveSelectedIndex;
    private EventHandler<ActionEvent> btnItemDownEventHandler = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            ObservableList<MissionItem> list = missionTableView.getItems();
            MissionItem moveSelectedItem = missionTableView.getSelectionModel().getSelectedItem();
            moveSelectedIndex = missionTableView.getSelectionModel().getSelectedIndex();
            if(moveSelectedIndex == list.size()-1) return;
            if(moveSelectedIndex == list.size()-2) {
                list.remove(moveSelectedItem);
                list.add(moveSelectedItem);
            } else {
                list.remove(moveSelectedItem);
                list.add(moveSelectedIndex+1, moveSelectedItem);
            }
            moveSelectedIndex++;
            for(int i=0; i<missionItems.size(); i++) {
                missionItems.get(i).setSeq(i);
            }
            missionMapSync();
        }
    };
	//--------------------------------------------------------------------------------------
	public void writeMissionToFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Mission Files", "*.waypoints")
        );
        File selectedFile = fileChooser.showSaveDialog(null);
        if(selectedFile != null) {
            try {
                PrintWriter pw = new PrintWriter(selectedFile);
                pw.println("QGC WPL 110");
                for(int i=0; i<missionItems.size(); i++) {
                    MissionItem mi = missionItems.get(i);
                    pw.print(mi.getSeq() + "\t");
                    if(mi.getStrCommand().equals("HOME")) {
                        pw.print(1 + "\t");
                        pw.print(0 + "\t");
                    } else {
                        pw.print(0 + "\t");
                        pw.print(3 + "\t");
                    }
                    pw.print(mi.getCommand() + "\t");
                    pw.print(mi.getParam1() + "\t");
                    pw.print(mi.getParam2() + "\t");
                    pw.print(mi.getParam3() + "\t");
                    pw.print(mi.getParam4() + "\t");
                    pw.print(mi.getX() + "\t");
                    pw.print(mi.getY() + "\t");
                    pw.print(mi.getZ() + "\t");
                    pw.println(1);
                }
                pw.flush();
                pw.close();
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
	}
	//--------------------------------------------------------------------------------------
	public void readMissionFromFile() {
		FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Mission Files", "*.waypoints")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if(selectedFile != null) {
            try {
                FileReader fr = new FileReader(selectedFile);
                BufferedReader br = new BufferedReader(fr);
                String wplVersion = br.readLine();
                if(!wplVersion.equals("QGC WPL 110")) {
                    AlertDialog.showOkButton("알림", "잘못된 WPL 형식 파일입니다.");
                    return;
                }
                missionItems = new ArrayList<>();
                while(true) {
                    String line = br.readLine();
                    if(line == null) break;
                    String[] waypoint = line.split("\t");
                    MissionItem missionItem = new MissionItem();
                    missionItem.setSeq(Integer.parseInt(waypoint[0]));
                    missionItem.setCommand(Integer.parseInt(waypoint[3]));
                    missionItem.setParam1(Float.parseFloat(waypoint[4]));
                    missionItem.setParam2(Float.parseFloat(waypoint[5]));
                    missionItem.setParam3(Float.parseFloat(waypoint[6]));
                    missionItem.setParam4(Float.parseFloat(waypoint[7]));
                    if(missionItem.getStrCommand().equals("RTL")) {
                    	missionItem.setX(missionItems.get(0).getX());
                        missionItem.setY(missionItems.get(0).getY());
                    } else {
                        missionItem.setX(Double.parseDouble(waypoint[8]));
                        missionItem.setY(Double.parseDouble(waypoint[9]));
                    }
                    missionItem.setZ(Float.parseFloat(waypoint[10]));
                    missionItems.add(missionItem);
                }
                br.close();
                fr.close();
                missionTableView.setItems(FXCollections.observableList(missionItems));
                missionTableView.refresh();
                missionMapSync();
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
	}
    //-------------------------------------------------------------------------------------- 
	public void fenceMake() {
		Platform.runLater(()->{
			jsproxy.call("fenceMake");
		});
	}
	//-------------------------------------------------------------------------------------- 
	public void fenceClear() {
		Platform.runLater(()->{
			jsproxy.call("fenceClear");
		});
	}
	//-------------------------------------------------------------------------------------- 
	public void getFencePoints() {
		Platform.runLater(()->{
			jsproxy.call("getFencePoints");
		});
	}	
	//-------------------------------------------------------------------------------------- 
	public void fenceMapSync(JSONArray fencePoints) {
		String strFencePoints = fencePoints.toString();
        Platform.runLater(()-> {
            jsproxy.call("fenceMapSync", strFencePoints);
        });
	}	
}
