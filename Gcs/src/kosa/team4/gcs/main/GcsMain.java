package kosa.team4.gcs.main;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GcsMain extends Application {
	public static GcsMain instance;
	public Stage primaryStage;
	public BorderPane ui;
	public GcsMainController controller;

	@Override
	public void start(Stage primaryStage) throws Exception {
		instance = this;
		
		this.primaryStage = primaryStage;

		//실행시 하얀 백그라운드를 보이지 않도록 하기 위해서 필요
		primaryStage.setOpacity(0.0);

		FXMLLoader loader = new FXMLLoader(getClass().getResource("GcsMain.fxml"));
		ui = loader.load();
		controller = loader.getController();

		Scene scene = new Scene(ui);
		scene.getStylesheets().add(GcsMain.class.getResource("style_dark.css").toExternalForm());

		primaryStage.setTitle("Drone Ground Control Station");
		primaryStage.setScene(scene);

		primaryStage.setMaximized(true);

		primaryStage.show();

		//백그라운드 보여주기
		primaryStage.setOpacity(1.0);

		//윈도우 닫기 버튼을 클릭했을 때
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				System.exit(0);
			}
		});
	}

	@Override
	public void stop() {
		//Platform.exit();
		System.exit(0);
	}

	public static void main(String[] args) {
		launch(args);
	}
}