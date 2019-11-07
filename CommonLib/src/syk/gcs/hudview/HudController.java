package syk.gcs.hudview;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class HudController implements Initializable {
    //------------------------------------------------------------------------------
    private static Logger logger = LoggerFactory.getLogger(HudController.class);
    //------------------------------------------------------------------------------
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initCanvasLayer0();
        initCanvasLayer1();
        initCanvasLayer2();
        initCanvasLayer3();

        AnimationTimer animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                layer0Draw();
                layer1Draw();
                layer2Draw();
                layer3Draw();
            }
        };
        animationTimer.start();
    }
    //------------------------------------------------------------------------------
    @FXML private Canvas canvas0;
    private double width;
    private double height;
    private GraphicsContext ctx0;
    private LinearGradient skyLinearGradient;
    private LinearGradient groundLinearGradient;

    private void initCanvasLayer0() {
        width = canvas0.getWidth();
        height = canvas0.getHeight();
        ctx0 = canvas0.getGraphicsContext2D();
        skyLinearGradient = new LinearGradient(
                0, 0, 0, 1,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0.3, Color.rgb(0x00, 0x00, 0xFF)),
                new Stop(1.0, Color.rgb(0x87, 0xCE, 0xFA))
        );
        groundLinearGradient = new LinearGradient(
                0, 0, 0, 1,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(0xBC, 0xE5, 0x5C)),
                new Stop(0.5, Color.rgb(0x66, 0x4B, 0x00))
        );
    }
    //------------------------------------------------------------------------------
    private double tempRoll0;
    private double translateX0;
    private double translateY0;

    private void layer0Draw() {
        //원점 회전 복귀
        if(tempRoll0 != 0) {
            ctx0.rotate(-tempRoll0);
        }

        //원점 이동 복귀
        if(translateX0 != 0) {
            ctx0.translate(-translateX0, -translateY0);
        }

        //뷰 지우기
        ctx0.clearRect(0, 0, width, height);

        //원점 이동
        double tempPitch = pitch;
        double pitchDistance = height/2/45;
        translateX0 = width/2;
        translateY0 = height/2 + pitchDistance*tempPitch;
        ctx0.translate(translateX0, translateY0);

        //원점 회전
        tempRoll0 = roll;
        ctx0.rotate(tempRoll0);

        //SKY 배경을 드로잉
        ctx0.setFill(skyLinearGradient);
        ctx0.fillRect(-500, -500, 1000, 500);

        //Ground 배경을 드로잉
        ctx0.setFill(groundLinearGradient);
        ctx0.fillRect(-500, 0, 1000, 500);
    }
    //------------------------------------------------------------------------------
    @FXML private Canvas canvas1;
    private GraphicsContext ctx1;
    private void initCanvasLayer1() {
        ctx1 = canvas1.getGraphicsContext2D();
    }
    //------------------------------------------------------------------------------
    private void layer1Draw() {
    }
    //------------------------------------------------------------------------------
    @FXML
    public Button btnCamera;
    public boolean isVideoOn;
    public void videoOn() {
        Platform.runLater(()->{
            canvas1.setVisible(true);
            btnCamera.setText("영상 끄기");
            isVideoOn = true;
        });
    }
    public void videoOff() {
        Platform.runLater(()->{
            canvas1.setVisible(false);
            btnCamera.setText("영상 보기");
            isVideoOn = false;
        });
    }
    public void videoImage(byte[] image) {
        try {
            BufferedImage bufferdImage = ImageIO.read(new ByteArrayInputStream(image));
            if (bufferdImage != null) {
                Image imgFx = SwingFXUtils.toFXImage(bufferdImage, null);
                if(imgFx != null) {
                    Platform.runLater(()->{
                        ctx1.drawImage(imgFx, 0, 0, imgFx.getWidth(), imgFx.getHeight(), 0, 0, width, height);
                    });
                }
            }
        } catch (Exception e) {
            //프로그램을 종료하면 ImageIO.read()에서 예외 발행할 수 있음
            //e.printStackTrace();
        }
    }
    //------------------------------------------------------------------------------
    @FXML private Canvas canvas2;
    private GraphicsContext ctx2;
    private void initCanvasLayer2() {
        ctx2 = canvas2.getGraphicsContext2D();
    }
    //------------------------------------------------------------------------------
    private double tempRoll2;
    private double translateX2;
    private double translateY2;
    private void layer2Draw() {
        //원점 회전 복귀
        if(tempRoll2 != 0) {
            ctx2.rotate(-tempRoll2);
        }

        //원점 이동 복귀
        if(translateX2 != 0) {
            ctx2.translate(-translateX2, -translateY2);
        }

        //뷰 지우기
        ctx2.clearRect(0, 0, width, height);

        //원점 이동
        double tempPitch = pitch;
        double pitchDistance = height/2/45;
        translateX2 = width/2;
        translateY2 = height/2 + pitchDistance*tempPitch;
        ctx2.translate(translateX2, translateY2);

        //원점 회전
        tempRoll2 = roll;
        ctx2.rotate(tempRoll2);

        //pitch 0 선 그리기
        ctx2.setStroke(Color.GREEN);
        ctx2.setLineWidth(1.5);
        ctx2.strokeLine(-50, 0, 50, 0);
        ctx2.setTextBaseline(VPos.CENTER);
        ctx2.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        ctx2.setFill(Color.WHITE);
        ctx2.fillText("0", -70, 0);

        //pitch SKY 선 그리기
        ctx2.setStroke(Color.WHITE);
        ctx2.setFill(Color.WHITE);
        ctx2.setTextBaseline(VPos.CENTER);
        ctx2.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        for(int i=5; i<=(25+tempPitch); i+=5) {
            ctx2.strokeLine((i%2==0)?-50:-25, -(pitchDistance*i), (i%2==0)?50:25, -(pitchDistance*i));
            if(i%2==0) {
                ctx2.fillText(String.valueOf(i), -80, -(pitchDistance*i));
            }
        }
        //pitch Ground 선 그리기
        for(int i=5; i<=(25-tempPitch); i+=5) {
            ctx2.strokeLine((i%2==0)?-50:-25, (pitchDistance*i), (i%2==0)?50:25, (pitchDistance*i));
            if(i%2==0) {
                ctx2.fillText(String.valueOf(-i), -80, (pitchDistance*i));
            }
        }
    }
    //------------------------------------------------------------------------------
    @FXML private Canvas canvas3;
    private GraphicsContext ctx3;

    private void initCanvasLayer3() {
        ctx3 = canvas3.getGraphicsContext2D();
    }
    //------------------------------------------------------------------------------
    private double translateX3;
    private double translateY3;

    private void layer3Draw() {
        //원점 이동 복귀
        if(translateX3 != 0) {
            ctx3.translate(-translateX3, -translateY3);
        }
        //뷰 지우기
        ctx3.clearRect(0, 0, width, height);
        //원점 이동
        translateX3 = width/2;
        translateY3 = height/2;
        ctx3.translate(translateX3, translateY3);
        //UAV 선 그리기
        ctx3.setStroke(Color.RED);
        ctx3.setLineWidth(2);
        ctx3.strokeLine(0,0,-50,20);
        ctx3.strokeLine(0,0,50,20);
        ctx3.strokeLine(-80,0,-120,0);
        ctx3.strokeLine(80,0,120,0);
        //UAV 호 그리기
        ctx3.setStroke(Color.WHITE);
        ctx3.setLineWidth(1);
        ctx3.setLineDashes(1, 5);
        ctx3.strokeArc(-120, -120, 240, 240, 0, 180, ArcType.OPEN);
        //Yaw 수평선 그리기
        ctx3.setStroke(Color.WHITE);
        ctx3.setLineDashes(0, 0);
        ctx3.strokeLine(-175, -130, 175, -130);
        //Yaw 중앙 눈금 그리기
        ctx3.setStroke(Color.RED);
        ctx3.setLineWidth(3);
        ctx3.strokeLine(0, -130, 0, -135);
        //Yaw 눈금 그리기
        double yawDistance = width/2/60;
        ctx3.setLineWidth(1);
        ctx3.setStroke(Color.WHITE);
        for(int i=5; i<=60; i+=5) {
            ctx3.strokeLine((yawDistance*i), -130, (yawDistance*i), -135);
        }
        for(int i=5; i<=60; i+=5) {
            ctx3.strokeLine(-(yawDistance*i), -130, -(yawDistance*i), -135);
        }
        //숫자 그리기
        double tempYaw = yaw;
        ctx3.setFill(Color.RED);
        ctx3.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        ctx3.setTextAlign(TextAlignment.CENTER);
        ctx3.fillText(String.valueOf((int)tempYaw), 0, -140);
        ctx3.setFill(Color.WHITE);
        ctx3.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        ctx3.setTextAlign(TextAlignment.CENTER);
        for(int i=15; i<=60; i+=15) {
            double value = tempYaw+i;
            if(value >= 360) {
                value -= 360;
            }
            ctx3.fillText(String.valueOf((int)value), (yawDistance*i), -140);
        }
        for(int i=15; i<=60; i+=15) {
            double value = tempYaw-i;
            if(value<0) {
                value += 360;
            }
            ctx3.fillText(String.valueOf((int)value), -(yawDistance*i), -140);
        }
    }
    //------------------------------------------------------------------------------
    @FXML private Label lblStatusText;
    private Thread statusTextThread;
    public void setStatusText(String text) {
    	if(statusTextThread != null) {
            statusTextThread.interrupt();
        }
        statusTextThread = new Thread() {
            @Override
            public void run() {
                Platform.runLater(()->{
                    if(text.length() > 20) {
                        String line0 = text.substring(0, 20);
                        String line1 = text.substring(20);
                        lblStatusText.setText(line0 + "\n" + line1);
                    } else {
                        lblStatusText.setText(text);
                    }
                });
                try { Thread.sleep(3000); } catch(Exception e) {}
                Platform.runLater(()->{
                    lblStatusText.setText("");
                });
            }
        };
        statusTextThread.start();
    }
    //------------------------------------------------------------------------------
    @FXML private Label lblMode;
    public void setMode(String mode) {
        Platform.runLater(()->{
            lblMode.setText(mode);
        });
    }
    //------------------------------------------------------------------------------
    @FXML private Label lblArmed;
    public void setArm(boolean arm) {
        Platform.runLater(()->{
            if(arm) {
                lblArmed.setText("ARMED");
            } else {
                lblArmed.setText("DISARMED");
            }
        });
    }
    //------------------------------------------------------------------------------
    @FXML private Label lblAltitude;
    public void setAlt(double alt) {
        double altitude = (int)(alt * 10) / 10.0;
        Platform.runLater(()->{
            lblAltitude.setText(altitude + " m");
        });
    }
    //------------------------------------------------------------------------------
    private double roll;
    private double pitch;
    private double yaw;
    public void setRollPichYaw(double roll, double pich, double yaw) {
        this.roll = roll;
        this.pitch = pich;
        this.yaw = yaw;
    }
    //------------------------------------------------------------------------------
    @FXML private Label lblAirSpeed;
    @FXML private Label lblGroundSpeed;
    public void setSpeed(double airSpeed, double groundSpeed) {
        Platform.runLater(()->{
            lblAirSpeed.setText("AS " + airSpeed + "m/s");
            lblGroundSpeed.setText("GS " + groundSpeed + "m/s");
        });
    }
    //------------------------------------------------------------------------------
    @FXML private Label lblBattery;
    public void setBattery(double voltageBattery, double currentBattery, int batteryRemaining) {
        Platform.runLater(()->{
            lblBattery.setText(
                "BAT: " +
                voltageBattery + "V " +
                currentBattery + "A " +
                batteryRemaining + "%"
            );
        });
    }
    //------------------------------------------------------------------------------
    @FXML private Label lblGpsFixed;;
    public void setGpsFixed(String fixType) {
        Platform.runLater(()->{
            lblGpsFixed.setText(fixType);
        });
    }
}
