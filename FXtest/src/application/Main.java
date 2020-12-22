package application;

import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


public class Main extends Application {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	@Override
	public void start(Stage primaryStage) {
		AnchorPane root;
		try {
			root = (AnchorPane)FXMLLoader.load(getClass().getResource("Sample5.fxml"));
			Scene scene = new Scene(root,1209,709);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setResizable(false);
		} catch(Exception e) {
			e.printStackTrace();
		}
		primaryStage.showingProperty().addListener((observable, oldValue, newValue) -> {
		    if (oldValue == true && newValue == false) {
				if( VisonController2.capObj != null ) {
					VisonController2.capObj.release();
				}
				if (VisonController2.timer != null && VisonController2.timer.isShutdown() == false) {
					try {
						if( VisonController2.timer != null ) {
							VisonController2.timer.shutdown();
							VisonController2.timer.awaitTermination(33, TimeUnit.MICROSECONDS);
						}
						if( VisonController2.timer2 != null ) {
							VisonController2.timer2.shutdown();
							VisonController2.timer2.awaitTermination(10, TimeUnit.MICROSECONDS);
						}
					} catch(Exception e) {
						// log any exception
						System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
					}
				}
				if( Gpio.ngSignalON() ) System.out.println("NG信号 ON");
				if( Gpio.close() ) System.out.println("GPIOシリアルポート　クローズ");
		        System.out.println("システムは正常にシャットダウンされました");
		    }
		});

	}

	public static void main(String[] args) {
		launch(args);
	}
}
