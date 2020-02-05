package application;

import java.awt.Rectangle;
import java.net.URL;
import java.util.ResourceBundle;

import org.opencv.core.Mat;
import org.opencv.core.Point;
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
	private boolean dragingFlg;
	boolean moveDragingFlg;
	Point[] moveDraggingPoint = new Point[2];
	Point moveDraggingPointView = new Point();
	private Rectangle draggingRect = new Rectangle(0,0,1,1);
	private Rectangle2D vRect;
	private double viewOrgZoom;
	private int fpsCnt;
	private long fpsEnd;
	private long fpsFirst;
	double rt;
	Rectangle[] rect = new Rectangle[1];
	Mat[] ptnMat = new Mat[1];
	double[] threshhold = new double[1];


	public static Mat ptmSrcMat; //メインビューに表示する画像 判定される画像
	public static Mat arg_ptmMat;//フォルダに保存されているテンプレート画像
	private Mat tmp_ptmMat;//決定される前のテンプレート画像 このクラス内でのみ判定に使われる画像

	public static int arg_detectionCnt;//探索する画像の数
	public static double arg_detectionScale;

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

	public static double arg_ptmThreshSliderN;//マッチングの閾値
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
    @FXML
    private Label detectCntLabel;
    @FXML
    private Label detectRationMax;
    @FXML
    private Label detectRationMin;
    @FXML
    private Label detectRationAve;
    @FXML
    private Slider scaleSlider;
    @FXML
    private Label scaleValue;
	private templateMatching tm;

    private void patternMatchParaSet() {
    	TMpara tmpara = new TMpara( 1 );

    	if( ptm_sp.getValue() == null ) {
    		tmpara.matchCnt[0] = 0;
    	}else {
    		tmpara.matchCnt[0] = ptm_sp.getValue();
    	}

    	tmpara.thresh[0] = threshholdSlider.getValue();
    	tmpara.paternMat[0] = tmp_ptmMat.clone();
    	tmpara.ptmEnable[0] = true;
    	tmpara.detectionRects[0] = tmp_rectsDetection;
    	tmpara.scale[0] = scaleSlider.getValue();

        tm = new templateMatching(tmpara);
    }

    @FXML
    void onCheckBtn(ActionEvent event) {

    }

    @FXML
    void onDetectionAreaSet(ActionEvent event) {
    	if( draggingRect.width > 10 ) {
    		tmp_rectsDetection = (Rectangle)draggingRect.clone();
    	}

    	draggingRect.width =0;
    	draggingRect.height =0;

    	rePaint();
    }

    @FXML
    void onDragDone(MouseEvent event) {

    	Platform.runLater(() ->threshLabel.setText(
    			String.format("%.1f",this.ptmThreshSliderN.getValue())));

        rePaint();
    }

	//ビューの移動
	private void moveView() {
    	vRect = ptmMainView.getViewport();//メインビューのサイズを取得
    	double xMin,yMin,width,height,imgWidth,imgHeight;
    	width = vRect.getWidth();
    	height = vRect.getHeight();
    	imgWidth = ptmMainView.getImage().getWidth();//メインビューに格納されているイメージのサイズ取得
    	imgHeight = ptmMainView.getImage().getHeight();

    	yMin = moveDraggingPointView.y+ (moveDraggingPoint[0].y - moveDraggingPoint[1].y)/viewOrgZoom;
    	xMin = moveDraggingPointView.x+ (moveDraggingPoint[0].x - moveDraggingPoint[1].x)/viewOrgZoom;

		if(  yMin > imgHeight  - height ) {
			yMin = imgHeight - height;//移動制限
		}else if( yMin < 0 ) {
			yMin = 0;
		}
		if(  xMin > imgWidth  - width ) {
			xMin = imgWidth - width;//移動制限
		}else if( xMin < 0 ) {
			xMin = 0;
		}
		if( height > imgHeight ) {
			yMin= 0;
		}
		if( width > imgWidth ) {
			xMin = 0;
		}


    	vRect = new Rectangle2D( xMin,yMin,width,height);
    	Platform.runLater(() ->ptmMainView.setViewport(vRect));

    	Rectangle2D vRect2 = new Rectangle2D(
    			xMin / scaleSlider.getValue(),
    			yMin / scaleSlider.getValue(),
    			width / scaleSlider.getValue(),
    			height / scaleSlider.getValue());
    	Platform.runLater(() ->ptmMainViewDst.setViewport(vRect2));
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
		if( height > imgHeight ) {
			yMin= 0;
		}
		if( width > imgWidth ) {
			xMin = 0;
		}

    	vRect = new Rectangle2D( xMin,yMin,width,height);
    	Platform.runLater(() ->ptmMainView.setViewport(vRect));

    	Rectangle2D vRect2 = new Rectangle2D(
    			xMin / scaleSlider.getValue(),
    			yMin / scaleSlider.getValue(),
    			width / scaleSlider.getValue(),
    			height / scaleSlider.getValue());
    	Platform.runLater(() ->ptmMainViewDst.setViewport(vRect2));
    }

    @FXML
    void onPatternSet(ActionEvent event) {
    	if( this.draggingRect.width > 10 ) {
    		Mat roi = ptmSrcMat.submat(
    				draggingRect.y,
    				draggingRect.y+draggingRect.height,
    				draggingRect.x,
    				draggingRect.x+draggingRect.width);
    		tmp_ptmMat = roi.clone();
            updateImageView(ptmSubView,Utils.mat2Image(tmp_ptmMat));
        	Platform.runLater(() ->ptmInfo.appendText("登録画像が更新されました\n"));
        	draggingRect.width =0;
        	draggingRect.height =0;
        	patternMatchParaSet();
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

    	arg_detectionScale = scaleSlider.getValue();

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
    	Platform.runLater( () ->ptmInfo.appendText("FPS計測開始.....\n"));

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
		Platform.runLater( () ->ptmInfo.appendText( String.format("FPS=%.1f", fps_) +"\n"));
    	Platform.runLater( () ->ptmInfo.appendText("FPS計測終了\n"));

    }

    @FXML
    void onWheel(ScrollEvent e) {
    	Rectangle2D rect = ptmMainView.getViewport();
    	double zoomStep = 0.01;
    	double imgWidth = ptmMainView.getImage().getWidth();//に格納されているイメージの幅

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


    	double minX = rect.getMinX();
    	double minY = rect.getMinY();

    	vRect = new Rectangle2D( minX,minY,
    			ptmMainView.getFitWidth() /viewOrgZoom,
    			ptmMainView.getFitHeight() /viewOrgZoom);
    	Platform.runLater(() ->ptmMainView.setViewport(vRect));
    	
    	Rectangle2D vRect2 = new Rectangle2D( minX / scaleSlider.getValue(),minY / scaleSlider.getValue(),
    			ptmMainViewDst.getFitWidth() /viewOrgZoom /scaleSlider.getValue(),
    			ptmMainViewDst.getFitHeight() /viewOrgZoom /scaleSlider.getValue());
    	Platform.runLater(() ->ptmMainViewDst.setViewport(vRect2));
    	
    	Platform.runLater(() ->zoomLabel.setText(String.format("%.2f",viewOrgZoom)));
    	Platform.runLater(() ->this.scaleValue.setText(String.format("%.2f",scaleSlider.getValue())));

    	rePaint();
    }

    @FXML
    void onZoomSlider(MouseEvent event) {
    	vRect = ptmMainView.getViewport();
    	Rectangle2D rect = ptmMainView.getViewport();
    	double imgWidth = ptmMainView.getImage().getWidth();//に格納されているイメージの幅
    	double imgHeight = ptmMainView.getImage().getHeight();


    	if( ptmMainView.getFitWidth() < imgWidth * zoomValue_slider.getValue() ) {
    		viewOrgZoom = zoomValue_slider.getValue();
    	}

    	double minX = rect.getMinX();
    	double minY = rect.getMinY();
		if( vRect.getHeight() > imgHeight ) {
			minY= 0;
		}
		if( vRect.getWidth() > imgWidth ) {
			minX = 0;
		}
    	vRect = new Rectangle2D( minX,minY,
    			ptmMainView.getFitWidth() /viewOrgZoom,
    			ptmMainView.getFitHeight() /viewOrgZoom);

    	Platform.runLater(() ->ptmMainView.setViewport(vRect));

    	Rectangle2D vRect2 = new Rectangle2D( minX / scaleSlider.getValue(),minY / scaleSlider.getValue(),
    			ptmMainViewDst.getFitWidth() /viewOrgZoom /scaleSlider.getValue(),
    			ptmMainViewDst.getFitHeight() /viewOrgZoom /scaleSlider.getValue());
    	Platform.runLater(() ->ptmMainViewDst.setViewport(vRect2));

    	Platform.runLater(() ->zoomValue_slider.setValue( viewOrgZoom));
    	Platform.runLater(() ->zoomLabel.setText(String.format("%.2f",viewOrgZoom)));
    	Platform.runLater(() ->this.scaleValue.setText(String.format("%.2f",scaleSlider.getValue())));
    	rePaint();
    }

    @FXML
    void mouseDragged(MouseEvent e) { //ptmMainView上でドラッグ
    	double zoom = zoomValue_slider.getValue();
    	if( moveDragingFlg && e.isMiddleButtonDown() ) {
            moveDraggingPoint[1].x = e.getX();
            moveDraggingPoint[1].y = e.getY();
            moveView();
    		return;
    	}

        int x = (int)(draggingRect.x);
        int y = (int)(draggingRect.y);

        double mX = e.getX()/zoom;
        double mY = e.getY()/zoom;

        if( mX > ptmSrcMat.width() ) {
        	mX = ptmSrcMat.width()-1;
        }
        if( mY > ptmSrcMat.height() ) {
        	mY = ptmSrcMat.height()-1;
        }
        draggingRect.width = (int)(ptmMainView.getViewport().getMinX() + mX - x);
        draggingRect.height =(int)(ptmMainView.getViewport().getMinY() + mY - y);
        rePaint();
    }

    @FXML
    void mousePressed(MouseEvent e) { //ptmMainView上でマウスプレス
    	double zoom = this.zoomValue_slider.getValue();
    	if( e.isMiddleButtonDown() ) {
    		moveDragingFlg = true;
            moveDraggingPoint[0].x = e.getX();
            moveDraggingPoint[0].y = e.getY();
            moveDraggingPointView.x = ptmMainView.getViewport().getMinX();
            moveDraggingPointView.y = ptmMainView.getViewport().getMinY();
    		return;
    	}
    	dragingFlg = true;
    	draggingRect.x = (int)(ptmMainView.getViewport().getMinX() + e.getX()/(zoom));
    	draggingRect.y = (int)(ptmMainView.getViewport().getMinY() + e.getY()/(zoom));

        rePaint();
    }

    @FXML
    void mouseReleased(MouseEvent e) { //imgORG マウスボタン離す
    	if( moveDragingFlg && e.isMiddleButtonDown() ) {
    		moveDragingFlg = false;
    		return;
    	}
    	if(draggingRect.width < 0 || draggingRect.height < 0)
    		draggingRect = new Rectangle(1,1,1,1);
    	dragingFlg = false;
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
		patternMatchParaSet();
	
		tm.tmpara.thresh[0] = this.ptmThreshSliderN.getValue();

		Mat areaMat = ptmSrcMat.clone();
		Mat orgMat = ptmSrcMat.clone();
		Mat ptarnMat = tm.tmpara.paternMat[0];

		Imgproc.rectangle(orgMat,
        		new Point(draggingRect.x,draggingRect.y),
        		new Point(draggingRect.x+draggingRect.width,draggingRect.y+draggingRect.height),
        		new Scalar(255,255,255),6);

		if( tmp_rectsDetection.width > 10 ) {
			Imgproc.rectangle(orgMat,
	        		new Point(tmp_rectsDetection.x,tmp_rectsDetection.y),
	        		new Point(tmp_rectsDetection.x+tmp_rectsDetection.width,tmp_rectsDetection.y+tmp_rectsDetection.height),
	        		new Scalar(255,0,0),6);
		}

		//フィルタ処理
    	Imgproc.cvtColor(areaMat, areaMat, Imgproc.COLOR_BGR2GRAY);//グレースケール化
    	Imgproc.cvtColor(ptarnMat, ptarnMat, Imgproc.COLOR_BGR2GRAY);//グレースケール化


    	if( gauusianCheck.isSelected() ) {//ガウシアン
    		double sigmaX = gauusianSliderX.getValue();
    		double sigmaY = gauusianSliderY.getValue();
    		int tmpValue =(int)gauusianSliderA.getValue();
    		if( tmpValue % 2 == 0 ) {
    			tmpValue++;
    		}
    		Size sz = new Size(tmpValue,tmpValue);
    		Imgproc.GaussianBlur(areaMat, areaMat, sz, sigmaX,sigmaY);
    		Imgproc.GaussianBlur(ptarnMat, ptarnMat, sz, sigmaX,sigmaY);
    	}
    	if( threshholdCheck.isSelected()) {//２値化
    		int type = threshhold_Inverse.isSelected()?Imgproc.THRESH_BINARY_INV:Imgproc.THRESH_BINARY;
    		Imgproc.threshold(areaMat, areaMat, this.threshholdSlider.getValue(),255,type);
    		Imgproc.threshold(ptarnMat, ptarnMat, this.threshholdSlider.getValue(),255,type);
    	}
    	if( dilateCheck.isSelected() ) {//膨張
    		int n = (int)dilateSliderN.getValue();
    		Imgproc.dilate(areaMat, areaMat, new Mat(),new Point(-1,-1),n);
    		Imgproc.dilate(ptarnMat, ptarnMat, new Mat(),new Point(-1,-1),n);
    	}
    	if( erodeCheck.isSelected() ) {//収縮
    		int n = (int)this.erodeSliderN.getValue();
    		Imgproc.erode(areaMat, areaMat, new Mat(),new Point(-1,-1),n);
    		Imgproc.erode(ptarnMat, ptarnMat, new Mat(),new Point(-1,-1),n);

    	}
    	if( cannyCheck.isSelected() ) {//Canny
    		double thresh1 = cannyThresh1.getValue();
    		double thresh2 = cannyThresh2.getValue();
    		Imgproc.Canny(areaMat,areaMat,thresh1,thresh2);
    		Imgproc.Canny(ptarnMat,ptarnMat,thresh1,thresh2);
    	}

    	if( !dragingFlg ) {

    		//テンプレート画像
    		ptnMat[0] = ptarnMat;
    		//検出エリア
    		rect[0] = new Rectangle(
    				tmp_rectsDetection.x,tmp_rectsDetection.y,
    				tmp_rectsDetection.width,tmp_rectsDetection.height);
    		//閾値
    		threshhold[0] = ptmThreshSliderN.getValue();

    		//テンプレートマッチング
    		boolean flg = tm.detectPattern(areaMat,orgMat);//実行し結果を表示用Matに上書き

	    	final int tmp_cnt = tm.resultValue[0].cnt;
	    	final double tmp_detectMax = tm.resultValue[0].detectMax;
	    	final double tmp_detectMin = tm.resultValue[0].detectMin;
	    	final double tmp_detectAve = tm.resultValue[0].detectAve;
	    	Platform.runLater(() ->detectCntLabel.setText(String.valueOf(tmp_cnt)));
	    	Platform.runLater(() ->detectRationMax.setText(String.format("%.3f",tmp_detectMax)));
	    	Platform.runLater(() ->detectRationMin.setText(String.format("%.3f",tmp_detectMin)));
	    	Platform.runLater(() ->detectRationAve.setText(String.format("%.3f",tmp_detectAve)));

    	}


		updateImageView(ptmMainView,Utils.mat2Image(orgMat));
		updateImageView(ptmMainViewDst,Utils.mat2Image(areaMat));
		updateImageView(ptmSubViewDst,Utils.mat2Image(ptarnMat));

	}

	private void setSlider() {
		Platform.runLater(() ->zoomValue_slider.setValue(arg_zoomValue_slider));
		Platform.runLater(() ->this.scaleSlider.setValue(arg_detectionScale));
    	Platform.runLater(() ->zoomLabel.setText(String.format("%.2f",zoomValue_slider.getValue())));
    	Platform.runLater(() ->this.scaleValue.setText(String.format("%.2f",scaleSlider.getValue())));


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
    	Platform.runLater(() ->threshLabel.setText(String.format("%.1f",this.ptmThreshSliderN.getValue())));

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
        assert detectCntLabel != null : "fx:id=\"detectCntLabel\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert detectRationMax != null : "fx:id=\"detectRationMax\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert detectRationMin != null : "fx:id=\"detectRationMin\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert detectRationAve != null : "fx:id=\"detectRationAve\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert scaleSlider != null : "fx:id=\"scaleSlider\" was not injected: check your FXML file 'ptmView.fxml'.";
        assert scaleValue != null : "fx:id=\"scaleValue\" was not injected: check your FXML file 'ptmView.fxml'.";

        moveDraggingPoint[0] = new Point();//ドラッグ移動始点
        moveDraggingPoint[1] = new Point();//ドラッグ移動終点
        moveDragingFlg = false;

        viewOrgZoom = arg_zoomValue_slider;
        if( viewOrgZoom == 0.0 ) viewOrgZoom = 0.3;
        vRect = new Rectangle2D( 0,0,ptmMainView.getFitWidth()/viewOrgZoom,
        										ptmMainView.getFitHeight()/viewOrgZoom);
    	ptmMainView.setViewport(vRect);
    	ptmMainViewDst.setViewport(vRect);

    	tmp_ptmMat = arg_ptmMat.clone();
    	updateImageView(ptmMainView,Utils.mat2Image(ptmSrcMat));
        updateImageView(ptmSubView,Utils.mat2Image(tmp_ptmMat));

        if( arg_rectsDetection != null ) {
        	tmp_rectsDetection = (Rectangle)arg_rectsDetection.clone();
        }else {
        	tmp_rectsDetection = new Rectangle();
        }

        setSlider();

    }
}
