package application;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

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

public class OKimageController {
	//クラス変数
	File[] files;
	int files_pointer = 0;
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private ImageView ngImage;
    @FXML
    private Button nextBtn;
    @FXML
    private Button prevBtn;
    @FXML
    private Button closeBtn;
    @FXML
    private Label info1;
    @FXML
    private Button useImgSettingBtn;
	private Mat img;

    @FXML
    void onClose(ActionEvent event) {
		Scene scene = ((Node) event.getSource()).getScene();
		Window window = scene.getWindow();
		window.hide();
    }

    @FXML
    void onNext(ActionEvent event) {
    	if( files_pointer < files.length-1) {
    		files_pointer++;
	        img = Imgcodecs.imread( files[files_pointer].getPath());
	        Platform.runLater(() ->ngImage.setImage( Utils.mat2Image(img)));
	        Platform.runLater(() ->this.info1.setText(String.valueOf(files_pointer+1)+" / "+String.valueOf(files.length)));

    	}
    }

    @FXML
    void onPrev(ActionEvent event) {
    	if( files_pointer > 0 ) {
    		files_pointer--;
	        img = Imgcodecs.imread( files[files_pointer].getPath());
	        Platform.runLater(() ->ngImage.setImage( Utils.mat2Image(img)));
	        Platform.runLater(() ->this.info1.setText(String.valueOf(files_pointer+1)+" / "+String.valueOf(files.length)));

    	}
    }

    @FXML
    void onUseImageSetting(ActionEvent event) {
        if( files.length > 0 ) {
	    	VisonController.saveImgUseFlg = true;//現在表示中のイメージを使用して設定
	    	VisonController.saveImgMat = img;//Matを渡す
        }
		Scene scene = ((Node) event.getSource()).getScene();
		Window window = scene.getWindow();
		window.hide();

    }

    @FXML
    void initialize() {
        assert ngImage != null : "fx:id=\"ngImage\" was not injected: check your FXML file 'OKImageViewer.fxml'.";
        assert nextBtn != null : "fx:id=\"nextBtn\" was not injected: check your FXML file 'OKImageViewer.fxml'.";
        assert prevBtn != null : "fx:id=\"prevBtn\" was not injected: check your FXML file 'OKImageViewer.fxml'.";
        assert closeBtn != null : "fx:id=\"closeBtn\" was not injected: check your FXML file 'OKImageViewer.fxml'.";
        assert info1 != null : "fx:id=\"info1\" was not injected: check your FXML file 'OKImageViewer.fxml'.";
        assert useImgSettingBtn != null : "fx:id=\"useImgSettingBtn\" was not injected: check your FXML file 'OKImageViewer.fxml'.";

        //NGイメージの保存先 ./ng_image
        files = FileClass.getFiles(new File("./ok_image"));
        if( files == null ) {
        	return;
        }

        files_pointer = files.length-1;
        if( files.length  >0 ) {
	        img = Imgcodecs.imread( files[files_pointer].getPath());
	        Platform.runLater(() ->ngImage.setImage( Utils.mat2Image(img)));
	        Platform.runLater(() ->this.info1.setText(String.valueOf(files_pointer+1)+" / "+
	        																String.valueOf(files.length)));
        }else {
        	Platform.runLater(() ->this.info1.setText("画像がありません"));
        }
    }
}
