package application;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Window;

public class calibTestController {
	//クラス変数
	File[] files;
	int files_pointer = 0;
	Mat img;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ImageView srcImg;

    @FXML
    private ImageView dstImg;

    @FXML
    private Button Prev;

    @FXML
    private Button Next;
    @FXML
    private Label info1;

    @FXML
    private Button ret;
	public static Mat cameraMatrix;
	public static Mat distortionCoefficients;

    @FXML
    void onNext(ActionEvent event) {
    	if( files_pointer < files.length-1) {
    		files_pointer++;
	        img = Imgcodecs.imread( files[files_pointer].getPath());
	        Platform.runLater(() ->srcImg.setImage( Utils.mat2Image(img)));
	        dstImgOut(img);
	        Platform.runLater(() ->this.info1.setText(String.valueOf(files_pointer+1)+" / "+String.valueOf(files.length)));
    	}
    }

    @FXML
    void onPrev(ActionEvent event) {
    	if( files_pointer > 0 ) {
    		files_pointer--;
	        img = Imgcodecs.imread( files[files_pointer].getPath());
	        Platform.runLater(() ->srcImg.setImage( Utils.mat2Image(img)));
	        dstImgOut(img);
	        Platform.runLater(() ->this.info1.setText(String.valueOf(files_pointer+1)+" / "+String.valueOf(files.length)));
    	}
    }

    @FXML
    void onRet(ActionEvent event) {
		Scene scene = ((Node) event.getSource()).getScene();
		Window window = scene.getWindow();
		window.hide();

    }

    private void dstImgOut(Mat srcMat) {
    	Mat dstMat = new Mat();
		Calib3d.undistort(srcMat, dstMat, cameraMatrix, distortionCoefficients);
		 Platform.runLater(() ->dstImg.setImage( Utils.mat2Image(dstMat)));

    }

    @FXML
    void initialize() {
        assert srcImg != null : "fx:id=\"srcImg\" was not injected: check your FXML file 'calibTest.fxml'.";
        assert dstImg != null : "fx:id=\"dstImg\" was not injected: check your FXML file 'calibTest.fxml'.";
        assert Prev != null : "fx:id=\"Prev\" was not injected: check your FXML file 'calibTest.fxml'.";
        assert Next != null : "fx:id=\"Next\" was not injected: check your FXML file 'calibTest.fxml'.";
        assert ret != null : "fx:id=\"ret\" was not injected: check your FXML file 'calibTest.fxml'.";
        assert info1 != null : "fx:id=\"info1\" was not injected: check your FXML file 'calibTest.fxml'.";
       //calibイメージの保存先 ./chess_image
        files = FileClass.getFiles(new File("./chess_image"));
        if( files.length == 0 ) {
        	return;
        }

        files_pointer = files.length-1;
        img = Imgcodecs.imread( files[files_pointer].getPath());
        Platform.runLater(() ->this.srcImg.setImage( Utils.mat2Image(img)));
        dstImgOut(img);
        Platform.runLater(() ->this.info1.setText(String.valueOf(files_pointer+1)+" / "+String.valueOf(files.length)));


    }
}

