package application;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Window;

public class PasswordDialogController {
	public static boolean flg = false;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private PasswordField pass_text;

    @FXML
    private Button OK_btn;

    @FXML
    void onOKbtn(ActionEvent event) {
    	if(pass_text.getText().matches("0662013437")) {
    		flg = true;
    	}else {
    		flg = false;
    	}
		Scene scene = ((Node) event.getSource()).getScene();
		Window window = scene.getWindow();
		window.hide();
    }

    @FXML
    void initialize() {
        assert pass_text != null : "fx:id=\"pass_text\" was not injected: check your FXML file 'passwordDialog.fxml'.";
        assert OK_btn != null : "fx:id=\"OK_btn\" was not injected: check your FXML file 'passwordDialog.fxml'.";
        flg = false;//表示される毎に毎回呼ばれる為、ここでフラグをリセットできる
    }
}
