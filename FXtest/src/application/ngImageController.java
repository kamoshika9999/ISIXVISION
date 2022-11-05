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

public class ngImageController {
	//クラス変数
	File[] files;
	int files_pointer = 0;
	Mat img;
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
    @FXML
    private Label shotCountLabele;
    @FXML
    private Label dateLabel;


    @FXML
    void onClose(ActionEvent event) {
		Scene scene = ((Node) event.getSource()).getScene();
		Window window = scene.getWindow();
		window.hide();
    }


    private String getShotCount(File f) {
    	String[] tmpStr = f.toString().split("x");
    	return tmpStr[1];

    }

    private String getDate(File f) {
    	String[] tmpStr = f.toString().split("_");
    	return tmpStr[ tmpStr.length-2];
    }

    @FXML
    void onNext(ActionEvent event) {
    	if( files_pointer < files.length-1 && files_pointer != -1) {
    		files_pointer++;
	        img = Imgcodecs.imread( files[files_pointer].getPath(),Imgcodecs.IMREAD_UNCHANGED);
	        Platform.runLater(() ->ngImage.setImage( Utils.mat2Image(img)));
	        Platform.runLater(() ->this.info1.setText(String.valueOf(files_pointer+1)+" / "+String.valueOf(files.length)));
	        Platform.runLater(() ->this.shotCountLabele.setText( getShotCount(files[files_pointer])));
	        Platform.runLater(() ->this.dateLabel.setText( getDate(files[files_pointer])));
    	}
    }

    @FXML
    void onPrev(ActionEvent event) {
    	if( files_pointer > 0 ) {
    		files_pointer--;
	        img = Imgcodecs.imread( files[files_pointer].getPath(),Imgcodecs.IMREAD_UNCHANGED);
	        Platform.runLater(() ->ngImage.setImage( Utils.mat2Image(img)));
	        Platform.runLater(() ->this.info1.setText(String.valueOf(files_pointer+1)+" / "+String.valueOf(files.length)));
	        Platform.runLater(() ->this.shotCountLabele.setText( getShotCount(files[files_pointer])));
	        Platform.runLater(() ->this.dateLabel.setText( getDate(files[files_pointer])));
    	}
    }
    @FXML
    void onUseImageSetting(ActionEvent event) {
        if( files.length > 0 ) {
	    	VisonController2.saveImgUseFlg = true;//現在表示中のイメージを使用して設定
	    	VisonController2.saveImgMat = img;//Matを渡す
        }
		Scene scene = ((Node) event.getSource()).getScene();
		Window window = scene.getWindow();
		window.hide();
    }

    private File[] sortFile(File[] f) {
    	File tmp;

    	if( f.length >0 ) {
	    	for(int i=0;i<f.length;i++) {
	    		for(int j=i+1;j<f.length;j++) {
	    			System.out.println(f[i]);
	    			if( !f[i].toString().contains("humbs") ) {
		    			int s1 = Integer.valueOf( f[i].toString().split("x")[1] );
		    			int s2 = Integer.valueOf( f[j].toString().split("x")[1] );
		    			if( s1 > s2 ) {
		    				tmp = f[i];
		    				f[i] = f[j];
		    				f[j] = tmp;
		    			}
	    			}
	    		}
	    	}
    	}
    	return f;

    }

    @FXML
    void initialize() {
        assert ngImage != null : "fx:id=\"ngImage\" was not injected: check your FXML file 'NgImageViewer.fxml'.";
        assert nextBtn != null : "fx:id=\"nextBtn\" was not injected: check your FXML file 'NgImageViewer.fxml'.";
        assert prevBtn != null : "fx:id=\"prevBtn\" was not injected: check your FXML file 'NgImageViewer.fxml'.";
        assert closeBtn != null : "fx:id=\"closeBtn\" was not injected: check your FXML file 'NgImageViewer.fxml'.";
        assert info1 != null : "fx:id=\"info1\" was not injected: check your FXML file 'NgImageViewer.fxml'.";
        assert useImgSettingBtn != null : "fx:id=\"useImgSettingBtn\" was not injected: check your FXML file 'NgImageViewer.fxml'.";
        assert shotCountLabele != null : "fx:id=\"shotCountLabele\" was not injected: check your FXML file 'NgImageViewer.fxml'.";
        assert dateLabel != null : "fx:id=\"dateLabel\" was not injected: check your FXML file 'NgImageViewer.fxml'.";

        //NGイメージの保存先 ./ng_image
        files = FileClass.getFiles(new File("./ng_image"));
        if( files == null ) {
        	return;
        }

	        files_pointer = files.length-1;
	        if( files.length  >0 ) {
	        	files = sortFile( files );
		        img = Imgcodecs.imread( files[files_pointer].getPath(),Imgcodecs.IMREAD_UNCHANGED);
		        Platform.runLater(() ->ngImage.setImage( Utils.mat2Image(img)));
		        Platform.runLater(() ->this.info1.setText(String.valueOf(files_pointer+1)+" / "+String.valueOf(files.length)));
		        Platform.runLater(() ->this.shotCountLabele.setText( getShotCount(files[files_pointer])));
		        Platform.runLater(() ->this.dateLabel.setText( getDate(files[files_pointer])));
	        }else {
	        	Platform.runLater(() ->this.info1.setText("画像がありません"));
	        }
    }
}
