package syk.gcs.cameraview;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class CameraViewController implements Initializable {
	//---------------------------------------------------------------------------------
	private static Logger logger = LoggerFactory.getLogger(CameraViewController.class);
	//---------------------------------------------------------------------------------
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initCanvasLayer();
	}
	//---------------------------------------------------------------------------------
	@FXML private Canvas canvas;
	private double width;
	private double height;
	private GraphicsContext ctx;
	private void initCanvasLayer() {
		width = canvas.getWidth();
		height = canvas.getHeight();
		ctx = canvas.getGraphicsContext2D();
	}
	public void videoImage(byte[] image) {
		try {
			canvas.setVisible(true);
			BufferedImage bufferdImage = ImageIO.read(new ByteArrayInputStream(image));
			if (bufferdImage != null) {
				Image imgFx = SwingFXUtils.toFXImage(bufferdImage, null);
				if(imgFx != null) {
					Platform.runLater(()->{
						ctx.drawImage(imgFx, 0, 0, imgFx.getWidth(), imgFx.getHeight(), 0, 0, width, height);
					});
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
