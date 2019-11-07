package exam01;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void init() throws Exception {
        System.out.println("init()");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("start()");
        primaryStage.setTitle("나의 JavaFX 윈도우");
        //primaryStage.setWidth(600);
        //primaryStage.setHeight(400);
        primaryStage.setMaximized(true);

        Scene scene = new Scene(new AnchorPane());
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("stop()");
    }

    public static void main(String[] args) {
        launch(args); //launch 실행시 자동으로 start() 실행
    }
}
