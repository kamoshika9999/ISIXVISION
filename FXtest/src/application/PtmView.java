package application;

import java.awt.Rectangle;
import java.net.URL;
import java.util.ResourceBundle;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Window;

public class PtmView {
	//クラス変数
	public static Mat ptmSrcMat;
	public static Mat arg_ptmMat;
	private Mat tmp_ptmMat;

	public static int arg_detectionCnt;

	public static boolean arg_gauusianCheck;
	public static double arg_gauusianSliderX;
	public static double arg_gauusianSliderY;
	public static double arg_gauusianSliderA;

	public static boolean arg_dilateCheck;
	public static double arg_dilateSliderN;

	public static boolean arg_erodeCheck;
	public static double arg_erodeSliderN;

	public static boolean arg_threshholdCheck;
	public static boolean arg_threshhold_Inverse;
	public static double arg_threshholdSlider;

	public static boolean arg_cannyCheck;
	public static double arg_cannyThresh1;
	public static double arg_cannyThresh2;

	public static double arg_ptmThreshSliderN;
	public static double arg_zoomValue_slider;
	public static Rectangle arg_rectsDetection;
	private Rectangle tmp_rectsDetection;
	public static boolean confimFlg = false;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button detectionAreaSet;

    @FXML
    private Slider gauusianSliderX;

    @FXML
    private CheckBox dilateCheck;

    @FXML
    private Slider dilateSliderN;

    @FXML
    private Slider gauusianSliderY;

    @FXML
    private Slider gauusianSliderA;

    @FXML
    private CheckBox threshholdCheck;

    @FXML
    private Slider threshholdSlider;

    @FXML
    private CheckBox threshhold_Inverse;

    @FXML
    private CheckBox gauusianCheck;

    @FXML
    private CheckBox erodeCheck;

    @FXML
    private Slider erodeSliderN;

    @FXML
    private CheckBox cannyCheck;

    @FXML
    private Slider cannyThresh1;
    @FXML
    private Slider cannyThresh2;

    @FXML
    private Label threshholdLabel1;

    @FXML
    private Label threshholdLabel11;

    @FXML
    private Spinner<Integer> ptm_sp;

    @FXML
    private Slider ptmThreshSliderN;

    @FXML
    private Label threshLabel;

    @FXML
    private TextArea ptmInfo;

    @FXML
    private Button ptmConfirm;

    @FXML
    private Button ptmCancel;

    @FXML
    private Button ptmTest;

    @FXML
    private Button ptmReturn;

    @FXML
    private Button patternSet;

    @FXML
    private Button move_up_btn;

    @FXML
    private Button move_left_btn;

    @FXML
    private Button move_right_btn;

    @FXML
    private Button move_down_btn;

    @FXML
    private Slider zoomValue_slider;

    @FXML
    private Slider move_speed_slider;

    @FXML
    private Label zoomLabel;

    @FXML
    private ImageView ptmMainView;

    @FXML
    private ImageView ptmMainViewDst;

    @FXML
    private ImageView ptmSubView;
    @FXML
    private ImageView ptmSubViewDst;


	private Rectangle draggingRect = new Rectangle(0,0,1,1);
	private Rectangle2D vRect;
	private double viewOrgZoom;
	private int fpsCnt;
	private long fpsEnd;
	private long fpsFirst;
	private boolean testFlg;
	double rt;
    @FXML
    void onCheckBtn(ActionEvent event) {

    }

    @FXML
    void onDetectionAreaSet(ActionEvent event) {
    	if( draggingRect.getWidth() > 10 ) {
    		tmp_rectsDetection = (Rectangle) draggingRect.clone();
    	}

    	draggingRect.width =0;

    	rePaint();
    }

    @FXML
    void onDragDone(MouseEvent event) {

    	Platform.runLater(() ->threshLabel.setText(
    			String.format("%.1f",this.ptmThreshSliderN.getValue())));

        rePaint();
    }

    @FXML
    void onMoveBtn(ActionEvent event) {
    	int speed =  (int)(move_speed_slider.getValue())*20;//移動するピクセル数の設定
    	Object eventObject = event.getSource();//どの移動ボタンが押されたか判断する為のオブジェクト取得
    	vRect = this.ptmMainView.getViewport();//メインビューのサイズを取得
    	double xMin,yMin,xMax,yMax,width,height,imgWidth,imgHeight;
    	xMin = vRect.getMinX();
    	yMin = vRect.getMinY();
    	xMax = vRect.getMaxX();
    	yMax = vRect.getMaxY();
    	width = vRect.getWidth();
    	height = vRect.getHeight();
    	imgWidth = ptmMainView.getImage().getWidth();//メインビューに格納されているイメージのサイズ取得
    	imgHeight = ptmMainView.getImage().getHeight();


    	if( eventObject == move_up_btn) {//上移動
    		if( yMin - speed >= 0 ) {
    			yMin -= speed;
    		}else {
    			yMin = 0;//移動制限
    		}
    	}else if( eventObject == move_down_btn) {//下移動
    		if( yMax + speed <= imgHeight ) {
    			yMin += speed;
    		}else {
    			yMin = imgHeight - height;
    		}
    	}else if( eventObject == move_left_btn) {//左移動
    		if( xMin - speed  >= 0 ) {
    			xMin -= speed;
    		}else {
    			xMin = 0;
    		}
    	}else if( eventObject == move_right_btn) {//右移動
    		if( xMax + speed  <= imgWidth ) {
    			xMin += speed;
    		}else {
    			xMin = imgWidth - width;
    		}
    	}
    	vRect = new Rectangle2D( xMin,yMin,width,height);
    	Platform.runLater(() ->ptmMainView.setViewport(vRect));
    	Platform.runLater(() ->ptmMainViewDst.setViewport(vRect));

        rePaint();

    }

    @FXML
    void onPatternSet(ActionEvent event) {
    	if( this.draggingRect.getWidth() > 10 ) {
    		Mat roi = ptmSrcMat.submat(new Rect(draggingRect.x,draggingRect.y,draggingRect.width,draggingRect.height));
    		tmp_ptmMat = roi.clone();
            updateImageView(ptmSubView,Utils.mat2Image(tmp_ptmMat));
        	Platform.runLater(() ->ptmInfo.appendText("登録画像が更新されました\n"));
        	rePaint();
    	}
    }

    @FXML
    void onPtmCancel(ActionEvent event) {
    	confimFlg = false;
    	setSlider();
    }

    @FXML
    void onPtmConfirm(ActionEvent event) {
    	confimFlg = true;

    	arg_detectionCnt = ptm_sp.getValue().intValue();

    	arg_gauusianCheck = gauusianCheck.isSelected();
    	arg_gauusianSliderX = gauusianSliderX.getValue();
    	arg_gauusianSliderY = gauusianSliderY.getValue();
    	arg_gauusianSliderA = gauusianSliderA.getValue();

    	arg_dilateCheck = dilateCheck.isSelected();
    	arg_dilateSliderN = dilateSliderN.getValue();

    	arg_erodeCheck = erodeCheck.isSelected();
    	arg_erodeSliderN = erodeSliderN.getValue();

    	arg_threshholdCheck = threshholdCheck.isSelected();
    	arg_threshhold_Inverse = this.threshhold_Inverse.isSelected();
    	arg_threshholdSlider = threshholdSlider.getValue();;

    	arg_cannyCheck = cannyCheck.isSelected();
    	arg_cannyThresh1 = cannyThresh1.getValue();
    	arg_cannyThresh2 = cannyThresh2.getValue();

    	arg_ptmThreshSliderN = ptmThreshSliderN.getValue();
    	arg_zoomValue_slider = zoomValue_slider.getValue();

    	arg_ptmMat = tmp_ptmMat.clone();
    	arg_rectsDetection = (Rectangle)tmp_rectsDetection.clone();

    	Platform.runLater(() ->ptmInfo.appendText("確定されました\n"));

    }

    @FXML
    void onPtmReturn(ActionEvent event) {
		Scene scene = ((Node) event.getSource()).getScene();
		Window window = scene.getWindow();
		window.hide();
    }

    @FXML
    void onPtmTest(ActionEvent event) {
    	fpsFirst = System.currentTimeMillis();
    	double fps = 0;
    	for(int i=0; i <= 29;i++) {
	    	fpsCnt++;
	    	if( fpsCnt == 30) {
	    		fpsEnd = System.currentTimeMillis();

	    		fps = fpsCnt/((fpsEnd - fpsFirst)/1000.0);

	    		fpsFirst = System.currentTimeMillis();
	    		fpsCnt=0;
	    	}
    		rePaint();
    	}
    	double fps_ = fps;
		Platform.runLater( () ->ptmInfo.appendText(String.format("FPS=%.1f", fps_)+"\n"));
    	Platform.runLater( () ->ptmInfo.appendText("FPS計測終了\n"));

    }

    @FXML
    void onWheel(ScrollEvent e) {
    	viewOrgZoom = zoomValue_slider.getValue();
    	Rectangle2D rect = ptmMainView.getViewport();
    	double zoomStep = 0.05;
    	double zoomOrg = viewOrgZoom;
    	double imgWidth = ptmMainView.getImage().getWidth();//に格納されているイメージの幅
    	double imgHeight = ptmMainView.getImage().getHeight();

    	if( e.getDeltaY() < 0) {
    		if(zoomValue_slider.getMin() < viewOrgZoom - zoomStep ) {
    			if( ptmMainView.getFitWidth() < imgWidth * (viewOrgZoom - zoomStep)) {
    				viewOrgZoom -= zoomStep;
    			}
    		}else {
    			viewOrgZoom = zoomValue_slider.getMin();
    		}
    	}else {
    		if(zoomValue_slider.getMax() > viewOrgZoom + zoomStep) {
    			viewOrgZoom +=zoomStep;
   			}else {
   				viewOrgZoom = zoomValue_slider.getMax();
   			}
    	}
    	Platform.runLater(() ->zoomValue_slider.setValue( viewOrgZoom));


    	double moveX = (rect.getWidth() / zoomOrg - rect.getWidth() / viewOrgZoom)/2;
    	double moveY = (rect.getHeight() / zoomOrg - rect.getHeight() / viewOrgZoom)/2;
    	double minX,minY;
    	if( rect.getMinX() + moveX < 0 ) {
    		minX = 0;
    	}else if( rect.getMaxX()+moveX > imgWidth) {
    		minX = imgWidth - rect.getWidth();
    	}else{
    		minX = rect.getMinX() + moveX;
    	}

    	if( rect.getMinY() + moveY < 0) {
    		minY = 0;
    	}else if( rect.getMaxY()+moveY > imgHeight) {
    		minY = imgHeight - rect.getHeight();
    	}else{
    		minY = rect.getMinY() + moveY;
    	}

    	vRect = new Rectangle2D( minX,minY,
    			ptmMainView.getFitWidth() /viewOrgZoom,
    			ptmMainView.getFitHeight() /viewOrgZoom);
    	Platform.runLater(() ->ptmMainView.setViewport(vRect));
    	Platform.runLater(() ->ptmMainViewDst.setViewport(vRect));
    	Platform.runLater(() ->zoomLabel.setText(String.format("%.1f",viewOrgZoom)));

    	rePaint();


    }

    @FXML
    void onZoomSlider(MouseEvent event) {
    	vRect = ptmMainView.getViewport();
    	double zoom = zoomValue_slider.getValue();
    	double zoomedWidth,zoomedHeight;
    	double imgWidth = ptmMainView.getImage().getWidth();//格納されているイメージの幅
    	double imgHeight = ptmMainView.getImage().getHeight();
    	Rectangle2D rect = ptmMainView.getViewport();

    	double minX = rect.getMinX();
    	double minY = rect.getMinY();
		if( ptmMainView.getFitWidth() < imgWidth * zoom) {
		    	zoomedWidth = ptmMainView.getFitWidth() / zoom;
		    	zoomedHeight = ptmMainView.getFitHeight() / zoom;
		}else {
			zoomedWidth = imgWidth;
			zoomedHeight = imgHeight;
			minX = 0;
			minY = 0;
		}

		vRect = new Rectangle2D( minX,minY,
    				zoomedWidth,zoomedHeight);

		double zoomCalc = ptmMainView.getFitWidth() / zoomedWidth;

    	Platform.runLater(() ->ptmMainView.setViewport(vRect));
    	Platform.runLater(() ->ptmMainViewDst.setViewport(vRect));
    	Platform.runLater(() ->zoomLabel.setText(String.format("%.1f",zoomCalc)));
    	Platform.runLater(() ->zoomValue_slider.setValue( zoomCalc ));

    	rePaint();

    }
    @FXML
    void mouseDragged(MouseEvent e) { //ptmMainView上でドラッグ
    	double zoom = zoomValue_slider.getValue();
        int x = (int)(draggingRect.getX());
        int y = (int)(draggingRect.getY());
        draggingRect.setSize((int)(ptmMainView.getViewport().getMinX() + e.getX()/(zoom)) - x,
        				(int)(ptmMainView.getViewport().getMinY() + e.getY()/(zoom) - y));
        rePaint();
    }

    @FXML
    void mousePressed(MouseEvent e) { //ptmMainView上でマウスプレス
    	double zoom = this.zoomValue_slider.getValue();
        draggingRect.setBounds((int)(ptmMainView.getViewport().getMinX() + e.getX()/(zoom)),
        					(int)(ptmMainView.getViewport().getMinY() + e.getY()/(zoom)), 0, 0);

        rePaint();
    }

    @FXML
    void mouseReleased(MouseEvent e) { //imgORG マウスボタン離す
        rePaint();
    }
    /**
	 * Update the {@link ImageView} in the JavaFX main thread
	 *
	 * @param view
	 *            the {@link ImageView} to update
	 * @param image
	 *            the {@link Image} to show
	 */
	private void updateImageView(ImageView view, Image image) {
		Utils.onFXThread(view.imageProperty(), image);
	}

	private void rePaint() {
		Mat tmpMat = ptmSrcMat.clone();
		Mat orgMat = ptmSrcMat.clone();

		Mat tmpMatPT = tmp_ptmMat.clone();

		Imgproc.rectangle(orgMat,
        		new Point(draggingRect.x,draggingRect.y),
        		new Point(draggingRect.x+draggingRect.width,draggingRect.y+draggingRect.height),
        		new Scalar(255,255,255),3);

		if( tmp_rectsDetection.getWidth() > 10 ) {
			Imgproc.rectangle(orgMat,
	        		new Point(tmp_rectsDetection.x,tmp_rectsDetection.y),
	        		new Point(tmp_rectsDetection.x+tmp_rectsDetection.width,tmp_rectsDetection.y+tmp_rectsDetection.height),
	        		new Scalar(255,0,0),2);
		}

		//フィルタ処理
    	Imgproc.cvtColor(tmpMat, tmpMat, Imgproc.COLOR_BGR2GRAY);//グレースケール化
    	Imgproc.cvtColor(tmpMatPT, tmpMatPT, Imgproc.COLOR_BGR2GRAY);//グレースケール化


    	if( gauusianCheck.isSelected() ) {//ガウシアン
    		double sigmaX = gauusianSliderX.getValue();
    		double sigmaY = gauusianSliderY.getValue();
    		int tmpValue =(int)gauusianSliderA.getValue();
    		if( tmpValue % 2 == 0 ) {
    			tmpValue++;
    		}
    		Size sz = new Size(tmpValue,tmpValue);
    		Imgproc.GaussianBlur(tmpMat, tmpMat, sz, sigmaX,sigmaY);
    		Imgproc.GaussianBlur(tmpMatPT, tmpMatPT, sz, sigmaX,sigmaY);
    	}
    	if( threshholdCheck.isSelected()) {//２値化
    		int type = threshhold_Inverse.isSelected()?Imgproc.THRESH_BINARY_INV:Imgproc.THRESH_BINARY;
    		Imgproc.threshold(tmpMat, tmpMat, this.threshholdSlider.getValue(),255,type);
    		Imgproc.threshold(tmpMatPT, tmpMatPT, this.threshholdSlider.getValue(),255,type);
    	}
    	if( dilateCheck.isSelected() ) {//膨張
    		int n = (int)dilateSliderN.getValue();
    		Imgproc.dilate(tmpMat, tmpMat, new Mat(),new Point(-1,-1),n);
    		Imgproc.dilate(tmpMatPT, tmpMatPT, new Mat(),new Point(-1,-1),n);
    	}
    	if( erodeCheck.isSelected() ) {//収縮
    		int n = (int)this.erodeSliderN.getValue();
    		Imgproc.erode(tmpMat, tmpMat, new Mat(),new Point(-1,-1),n);
    		Imgproc.erode(tmpMatPT, tmpMatPT, new Mat(),new Point(-1,-1),n);

    	}
    	if( cannyCheck.isSelected() ) {//Canny
    		double thresh1 = cannyThresh1.getValue();
    		double thresh2 = cannyThresh2.getValue();
    		Imgproc.Canny(tmpMat,tmpMat,thresh1,thresh2);
    		Imgproc.Canny(tmpMatPT,tmpMatPT,thresh1,thresh2);
    	}

    	//検出処理
    	//比較結果を格納するMatを生成
    	Mat roi = tmpMat.submat(new Rect(tmp_rectsDetection.x,tmp_rectsDetection.y,
    			tmp_rectsDetection.width,tmp_rectsDetection.height));
    	Mat orgroi = orgMat.submat(new Rect(tmp_rectsDetection.x,tmp_rectsDetection.y,
    			tmp_rectsDetection.width,tmp_rectsDetection.height));
    	int cnt = 0;
    	if( roi.width() > tmpMatPT.width() && roi.height() > tmpMatPT.height() ) {

	    	Mat result = new Mat(roi.rows() - tmpMatPT.rows() + 1, roi.cols() - tmpMatPT.cols() + 1, CvType.CV_32FC1);
    		//Mat result = new Mat();
	    	//テンプレートマッチ実行（TM_CCOEFF_NORMED：相関係数＋正規化）
	    	Imgproc.matchTemplate(roi, tmpMatPT, result, Imgproc.TM_CCOEFF_NORMED);
	    	//結果から相関係数がしきい値以下を削除（０にする）
	    	Imgproc.threshold(result, result,
	    			ptmThreshSliderN.getValue(),1.0, Imgproc.THRESH_TOZERO);

	    	boolean flg;

	    	for (int i=0;i<result.rows();i++) {
	    		flg = false;
	    		String rtStr ="";
	    		for (int j=0;j<result.cols();j++) {
	    			rt = result.get(i, j)[0];
	    			if ( rt > 0) {
	    				cnt++;
	    		    	Imgproc.rectangle(orgroi, new Point(j, i), new Point(j + tmpMatPT.cols(), i + tmpMatPT.rows()), new Scalar(255, 255, 0),3);
	    		    	rtStr = rtStr + String.format("%.2f", rt)+" : ";

	    		    	j += tmpMatPT.cols()/1.1;
	    		    	flg = true;
	    			}
	    		}
    			if( flg ) {
    				i += tmpMatPT.rows()/1.1;
    				String tmp = rtStr;
        			Platform.runLater( () ->ptmInfo.appendText("一致率="+tmp+"\n"));
    			}

	   		 }
    	}else {
    		System.out.println("サイズ不正");
    	}
    	//System.out.println("マッチングカウント=" + String.valueOf(cnt) );

		updateImageView(ptmMainView,Utils.mat2Image(orgMat));
		updateImageView(ptmMainViewDst,Utils.mat2Image(tmpMat));
		updateImageView(ptmSubViewDst,Utils.mat2Image(tmpMatPT));

	}

	private void setSlider() {
		Platform.runLater(() ->zoomValue_slider.setValue(arg_zoomValue_slider));

		Platform.runLater(() ->gauusianCheck.setSelected(arg_gauusianCheck));
		Platform.runLater(() ->gauusianSliderX.setValue(arg_gauusianSliderX));
		Platform.runLater(() ->gauusianSliderY.setValue(arg_gauusianSliderY));
		Platform.runLater(() ->gauusianSliderA.setValue(arg_gauusianSliderA));

		Platform.runLater(() ->dilateCheck.setSelected(arg_dilateCheck));
		Platform.runLater(() ->dilateSliderN.setValue(arg_dilateSliderN));

		Platform.runLater(() ->erodeCheck.setSelected(arg_erodeCheck));
		Platform.runLater(() ->erodeSliderN.setValue(arg_erodeSliderN));

		Platform.runLater(() ->threshholdCheck.setSelected(arg_threshholdCheck));
		Platform.runLater(() ->threshhold_Inverse.setSelected(arg_threshhold_Inverse));
		Platform.runLater(() ->threshholdSlider.setValue(arg_threshholdSlider));

		Platform.runLater(() ->cannyCheck.setSelected(arg_cannyCheck));
		Platform.runLater(() ->cannyThresh1.setValue(arg_cannyThresh1));
		Platform.runLater(() ->cannyThresh2.setValue(arg_cannyThresh2));

		Platform.runLater(() ->ptmThreshSliderN.setValue(arg_ptmThreshSliderN));
		Platform.runLater(() ->threshLabel.setText(String.valueOf(arg_ptmThreshSliderN)));

		Platform.runLater(() ->ptm_sp.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,
				999,arg_detectionCnt,1)));


		rePaint();

	}
    @FXML
    void initialize() {
        assert detectionAreaSet != null : "fx:id=\"detectionAreaSet\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert gauusianSliderX != null : "fx:id=\"gauusianSliderX\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert dilateCheck != null : "fx:id=\"dilateCheck\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert dilateSliderN != null : "fx:id=\"dilateSliderN\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert gauusianSliderY != null : "fx:id=\"gauusianSliderY\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert gauusianSliderA != null : "fx:id=\"gauusianSliderA\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert threshholdCheck != null : "fx:id=\"threshholdCheck\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert threshholdSlider != null : "fx:id=\"threshholdSlider\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert threshhold_Inverse != null : "fx:id=\"threshhold_Inverse\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert gauusianCheck != null : "fx:id=\"gauusianCheck\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert erodeCheck != null : "fx:id=\"erodeCheck\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert erodeSliderN != null : "fx:id=\"erodeSliderN\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert cannyCheck != null : "fx:id=\"cannyCheck\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert cannyThresh1 != null : "fx:id=\"cannyThresh1\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert cannyThresh2 != null : "fx:id=\"cannyThresh1\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert threshholdLabel1 != null : "fx:id=\"threshholdLabel1\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert threshholdLabel11 != null : "fx:id=\"threshholdLabel11\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert ptm_sp != null : "fx:id=\"ptm_sp\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert ptmThreshSliderN != null : "fx:id=\"threshSliderN\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert threshLabel != null : "fx:id=\"threshLabel\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert ptmInfo != null : "fx:id=\"ptmInfo\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert ptmConfirm != null : "fx:id=\"ptmConfirm\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert ptmCancel != null : "fx:id=\"ptmCancel\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert ptmTest != null : "fx:id=\"ptmTest\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert ptmReturn != null : "fx:id=\"ptmReturn\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert patternSet != null : "fx:id=\"patternSet\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert move_up_btn != null : "fx:id=\"move_up_btn\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert move_left_btn != null : "fx:id=\"move_left_btn\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert move_right_btn != null : "fx:id=\"move_right_btn\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert move_down_btn != null : "fx:id=\"move_down_btn\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert zoomValue_slider != null : "fx:id=\"zoomValue_slider\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert move_speed_slider != null : "fx:id=\"move_speed_slider\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert zoomLabel != null : "fx:id=\"zoomLabel\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert ptmMainView != null : "fx:id=\"ptmMainView\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert ptmSubView != null : "fx:id=\"ptmSubView\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert ptmSubViewDst != null : "fx:id=\"ptmSubViewDst\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert ptmMainViewDst != null : "fx:id=\"ptmMainViewDst\" was not injected: check your FXML file 'ptmView.fxml'.";

        updateImageView(ptmMainView,Utils.mat2Image(ptmSrcMat));
        tmp_ptmMat = arg_ptmMat.clone();
        updateImageView(ptmSubView,Utils.mat2Image(tmp_ptmMat));

        if( arg_rectsDetection != null ) {
        	tmp_rectsDetection = (Rectangle) arg_rectsDetection.clone();
        }else {
        	tmp_rectsDetection = new Rectangle();
        }

        setSlider();



    }
}
