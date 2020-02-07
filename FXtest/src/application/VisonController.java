package application;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class VisonController{

	//クラス変数
	public static boolean debugFlg = false;

	final int saveMax_all = 255;
	final int saveMax_ng = 200;
	int ngCnt = 0;
	int allSaveCnt = 0;

	public static Mat srcMat = new Mat();//保存画像を使用した設定に使用する為publicにしておく
	Mat dstframe = new Mat();//srcMatをカメラキャリブレーションデーターから変換したオブジェクトが入る
	private Mat glayMat;
    List<Rectangle> rects;
    Rectangle draggingRect;
    volatile boolean dragging;
	boolean moveDragingFlg;
	Point[] moveDraggingPoint = new Point[2];
	Point moveDraggingPointView = new Point();


    preSet pObj;
    public static VideoCapture capObj = new VideoCapture();
	public static ScheduledExecutorService timer;
	public static ScheduledExecutorService timer2;

	//穴面積判定用
	private long whiteAreaAverage;
	private long whiteAreaMax;
	private long whiteAreaMin;


	//シャッタートリガ用
	boolean shutterFlg = false;
	boolean offShutterFlg = false;
	double ngTriggerTime = 0;
	double triggerDelly = 0;

	// a flag to change the button behavior
	private boolean cameraActive = false;
	// the id of the camera to be used
	private static int cameraId = 0;
	private String infoText;
	Rectangle2D vRect;
	double viewOrgZoom;//imgORG ImageViewの拡大率
	int onSetVeri_n;
	boolean manualTrigger = false;
	boolean autoTrigger = false;
	boolean eventTrigger = false;
	//FPS計測用
	long fpsFirst = System.currentTimeMillis();//計測開始
	long fpsEnd;
	int fpsCnt=0;//カウント用
	double fps =0;
	String password = "7777";
	double framCnt = 0;
	VideoCapture source_video;
	//設定自動ロック用
	boolean settingModeFlg = false;
	long lockedTimer = 0;
	final long lockedTimerThresh = 1000 * 60 *5;
	//パターンマッチング用
	private boolean ptmSetStartFlg = false;
	public Mat[][] ptmImgMat = new Mat[4][4];//[presetNo][ptm1～ptm4]
	private TMpara tmpara = new TMpara();

	//保存画像を使用した設定用
	public static boolean saveImgUseFlg;//保存画像を使用した設定に使うフラグ   保存画像使用中はＰＬＣシャッタートリガ強制無効
	public static Mat saveImgMat;
	//連続画像保存によるタイムラグ緩和のロジックに使用
	private long savelockedTimer;
	private long savelockedTimerThresh = 500;
	private int NGsaveCnt = 0;

	//カメラキャリブレーション用
	private boolean cameraCalibFlg = false;
	private Mat cameraMatrix;//内部パラメータ
	private Mat distortionCoefficients;//歪み係数

	//インフォメーション
	private String initInfo2;

	@FXML
    private Spinner<Integer> dellySpinner;

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private AnchorPane aPane;
    @FXML
    private Label info1;
    @FXML
    private Button getInfoBtn;
    @FXML
    private ImageView imgORG;
    @FXML
    private ImageView imgGLAY;
    @FXML
    private Slider sliderDetecPara4;
    @FXML
    private Slider sliderDetecPara5;
    @FXML
    private Slider sliderDetecPara6;
    @FXML
    private Slider sliderDetecPara7;
    @FXML
    private Slider sliderDetecPara8;
    @FXML
    private Slider sliderDetecPara9;
    @FXML
    private TextField textFieldDetecPara4;
    @FXML
    private TextField textFieldDetecPara5;
    @FXML
    private TextField textFieldDetecPara6;
    @FXML
    private TextField textFieldDetecPara7;
    @FXML
    private TextField textFieldDetecPara8;
    @FXML
    private TextField textFieldDetecPara9;
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
    private Button testBtn;
    @FXML
    private Button okuri1_btn;
    @FXML
    private Button okuri2_btn;
    @FXML
    private Button okuri3_btn;
    @FXML
    private Button okuri4_btn;
    @FXML
    private Label okuri1_label;
    @FXML
    private Label okuri2_label;
    @FXML
    private Label okuri3_label;
    @FXML
    private Label okuri4_label;
    @FXML
    private Label okuri1_judg;
    @FXML
    private Label okuri2_judg;
    @FXML
    private Label okuri3_judg;
    @FXML
    private Label okuri4_judg;
    @FXML
    private Label judg;
    @FXML
    private Button preset1;
    @FXML
    private Button preset2;
    @FXML
    private Button preset3;
    @FXML
    private Button preset4;
    @FXML
    private Button stopTest;
    @FXML
    private Button trigBtn;
    @FXML
    private Button saveBtn;
    @FXML
    private Button loadBtn;
    @FXML
    private Label okuri1_n;
    @FXML
    private Label okuri2_n;
    @FXML
    private Label okuri3_n;
    @FXML
    private Label okuri4_n;
    @FXML
    private CheckBox gauusianCheck;
    @FXML
    private Slider gauusianSliderX;//sigmaX
    @FXML
    private Slider gauusianSliderY;//sigmaY
    @FXML
    private Slider gauusianSliderA;//アパーチャサイズ
    @FXML
    private CheckBox dilateCheck;
    @FXML
    private Slider dilateSliderN;
    @FXML
    private CheckBox threshholdCheck;
    @FXML
    private Slider threshholdSlider;
    @FXML
    private Label threshholdLabel;
    @FXML
    private Button setVeriBtn1;
    @FXML
    private Button setVeriBtn2;
    @FXML
    private Button setVeriBtn3;
    @FXML
    private Button setVeriBtn4;
    @FXML
    private CheckBox threshhold_Inverse;
    @FXML
    private Button aTriggerBtn;
    @FXML
    private Button triggerBtn;
    @FXML
    private Button outTriggerBtn;
    @FXML
    private ImageView imgGLAY1;
    @FXML
    private ImageView imgGLAY2;
    @FXML
    private ImageView imgGLAY3;
    @FXML
    private Label fpsLabel;
    @FXML
    private Circle triggerCCircle;
    @FXML
    private Button clearBtn;
    @FXML
    private Button ngImageBtn;
    @FXML
    private Label ngCounterLabel;
    @FXML
    private CheckBox imgSaveFlg;
    @FXML
    private Button demoMode;
	private boolean demoFlg;
    @FXML
    private TextArea info2;
    @FXML
    private Button lockBtn;
    @FXML
    private javafx.scene.shape.Rectangle lockShape1; //設定ロック用カーテン
    @FXML
    private javafx.scene.shape.Rectangle lockShape2; //設定ロック用カーテン
    @FXML
    private javafx.scene.shape.Rectangle lockShape3; //設定ロック用カーテン
    @FXML
    private javafx.scene.shape.Rectangle lockShape4; //設定ロック用カーテン
    @FXML
    private javafx.scene.shape.Rectangle lockShape5; //設定ロック用カーテン
    @FXML
    private ImageView imgNG; //最新のＮＧ画像のイメージビュー
    @FXML
    private CheckBox outTrigDisableChk;//True:NGが発生してもGPIOの状態を変えない
    @FXML
    private CheckBox settingMode;//True:設定モード中
    @FXML
    private Spinner<Integer> portNoSpin;//GPIOボード用シリアルポート番号選択
    @FXML
    private Label zoomLabel;//メインビューの表示倍率のラベル
    @FXML
    private Spinner<Integer> camIDspinner;//使用するカメラのＩＤ
    @FXML
    private TextField capW_text;//カメラの解像度の横幅
    @FXML
    private TextField capH_text;//カメラの解像度の縦幅
    @FXML
    private Circle GPIO_STATUS_PIN0;//シャッタートリガ信号の状態
    @FXML
    private Circle GPIO_STATUS_PIN1;//オールクリア信号の状態
    @FXML
    private Circle GPIO_STATUS_PIN3;//ＮＧ出力トリガの状態
    @FXML
    private CheckBox imgSaveFlg_all;//規定枚数画像保存 規定数超えた画像は自動削除
    @FXML
    private Button OKImageBtn;
    @FXML
    private Accordion accordion_1;
    @FXML
    private CheckBox throughImageChk;
    @FXML
    private Button GPIO_allRead;
    @FXML
    private Button shotImgBtn;
    @FXML
    private Button shotImgBtn_chess;
    @FXML
    private Button cameraCalib;
    @FXML
    private Button calibDataDel;
    @FXML
    private TextField adc_thresh_value;
    @FXML
    private CheckBox adc_flg;

    //パターンマッチング関係
    @FXML
    private TitledPane ptm_setting_accordion;
    @FXML
    private Button ptm_set_pt1;
    @FXML
    private Button ptm_set_pt2;
    @FXML
    private Button ptm_set_pt3;
    @FXML
    private Button ptm_set_pt4;
    @FXML
    private Button ptm_set_para1;
    @FXML
    private Button ptm_set_para2;
    @FXML
    private Button ptm_set_para3;
    @FXML
    private Button ptm_set_para4;
    @FXML
    private CheckBox ptm_pt1_enable;
    @FXML
    private CheckBox ptm_pt2_enable;
    @FXML
    private CheckBox ptm_pt3_enable;
    @FXML
    private CheckBox ptm_pt4_enable;
    @FXML
    private ImageView ptm_img1;
    @FXML
    private ImageView ptm_img2;
    @FXML
    private ImageView ptm_img3;
    @FXML
    private ImageView ptm_img4;
    @FXML
    private ImageView debugImg;//穴の面積判定用
    @FXML
    private Label whiteRatioLabel;//穴の面積判定用
    @FXML
    private Label blackRatioLabel;//穴の面積判定用
    @FXML
    private Spinner<Integer> whiteRatioMaxSp;
    @FXML
    private Spinner<Integer> whiteRatioMinSp;

	private templateMatching tm;



    @FXML
    /**
     * スライダーの値を対応するテキストフィールドに入力する
     * @param event
     */
    void onDragDone(MouseEvent event) {
    	Platform.runLater(() ->textFieldDetecPara4.setText(
    			String.valueOf(String.format("%.1f",sliderDetecPara4.getValue()))));
    	Platform.runLater(() ->textFieldDetecPara5.setText(
    			String.valueOf(String.format("%.1f",sliderDetecPara5.getValue()))));
    	Platform.runLater(() ->textFieldDetecPara6.setText(
    			String.valueOf(String.format("%.1f",sliderDetecPara6.getValue()))));
    	Platform.runLater(() ->textFieldDetecPara7.setText(
    			String.valueOf(String.format("%.1f",sliderDetecPara7.getValue()))));
    	Platform.runLater(() ->textFieldDetecPara8.setText(
    			String.valueOf(String.format("%.1f",sliderDetecPara8.getValue()))));
    	Platform.runLater(() ->textFieldDetecPara9.setText(
    			String.valueOf(String.format("%.1f",sliderDetecPara9.getValue()))));
    	Platform.runLater(() ->threshholdLabel.setText(
    			String.format("%.1f",threshholdSlider.getValue())));

    	eventTrigger = true;//onTest():ru()からrePaint()を呼び出す為にイベントトリガフラグをセット
    }

    @FXML
    /**
     * 現在のカメラの情報をテキストエリアに表示する
     * @param event
     */
    void onInfoBtnAction(ActionEvent event) {
		double wset = capObj.get(Videoio.CAP_PROP_FRAME_WIDTH);
		double hset = capObj.get(Videoio.CAP_PROP_FRAME_HEIGHT);
		Platform.runLater( () ->info2.appendText("\n"+ "カメラ解像度 WIDTH="+ wset+
				"\n カメラ解像度 HEIGHT= " +hset +"\n"));
    }
    @FXML
    /**
     * メインビュー内に格納されているイメージの移動
     * @param event
     */
    void onMoveBtn(ActionEvent event) {
    	int speed =  (int)(move_speed_slider.getValue())*20;//移動するピクセル数の設定
    	Object eventObject = event.getSource();//どの移動ボタンが押されたか判断する為のオブジェクト取得
    	vRect = this.imgORG.getViewport();//メインビューのサイズを取得
    	double xMin,yMin,xMax,yMax,width,height,imgWidth,imgHeight;
    	xMin = vRect.getMinX();
    	yMin = vRect.getMinY();
    	xMax = vRect.getMaxX();
    	yMax = vRect.getMaxY();
    	width = vRect.getWidth();
    	height = vRect.getHeight();
    	imgWidth = imgORG.getImage().getWidth();//メインビューに格納されているイメージのサイズ取得
    	imgHeight = imgORG.getImage().getHeight();


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
    	Platform.runLater(() ->imgORG.setViewport(vRect));
    	Platform.runLater(() ->imgGLAY1.setViewport(vRect));
    	Platform.runLater(() ->imgGLAY2.setViewport(vRect));
    	Platform.runLater(() ->imgGLAY3.setViewport(vRect));
    	Platform.runLater(() ->imgGLAY.setViewport(vRect));

    	eventTrigger = true;
    }

    @FXML
    void onZoomSlider(MouseEvent event) {
    	vRect = imgORG.getViewport();
    	Rectangle2D rect = imgORG.getViewport();
    	double imgWidth = imgORG.getImage().getWidth();//に格納されているイメージの幅
    	double imgHeight = imgORG.getImage().getHeight();//に格納されているイメージの幅


    	if( imgORG.getFitWidth() < imgWidth * zoomValue_slider.getValue() ) {
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
    			imgORG.getFitWidth() /viewOrgZoom,
    			imgORG.getFitHeight() /viewOrgZoom);

    	Platform.runLater(() ->zoomValue_slider.setValue( viewOrgZoom));
    	Platform.runLater(() ->zoomLabel.setText(String.format("%.2f",viewOrgZoom)));

    	Platform.runLater(() ->imgORG.setViewport(vRect));
    	Platform.runLater(() ->imgGLAY.setViewport(vRect));
    	Platform.runLater(() ->imgGLAY1.setViewport(vRect));
    	Platform.runLater(() ->imgGLAY2.setViewport(vRect));
    	Platform.runLater(() ->imgGLAY3.setViewport(vRect));
    }
    @FXML
    void onWheel(ScrollEvent e) {
    	Rectangle2D rect = imgORG.getViewport();
    	double zoomStep = 0.01;
    	double imgWidth = imgORG.getImage().getWidth();//に格納されているイメージの幅

    	if( e.getDeltaY() < 0) {
    		if(zoomValue_slider.getMin() < viewOrgZoom - zoomStep ) {
    			if( imgORG.getFitWidth() < imgWidth * (viewOrgZoom - zoomStep)) {
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
    			imgORG.getFitWidth() /viewOrgZoom,
    			imgORG.getFitHeight() /viewOrgZoom);
    	Platform.runLater(() ->imgORG.setViewport(vRect));
    	Platform.runLater(() ->imgGLAY.setViewport(vRect));
    	Platform.runLater(() ->imgGLAY1.setViewport(vRect));
    	Platform.runLater(() ->imgGLAY2.setViewport(vRect));
    	Platform.runLater(() ->imgGLAY3.setViewport(vRect));
    	Platform.runLater(() ->zoomLabel.setText(String.format("%.1f",viewOrgZoom)));
    }

    @FXML
    void onDemo(ActionEvent event) {
		if (capObj != null ) {
			Platform.runLater( () ->capObj.release());
		}
    	demoFlg = true;
    	srcMat = new Mat();
    }

    @FXML
    void onTest(ActionEvent event) throws InterruptedException {
    	demoFlg = false;
		// カメラがアクティブ状態の時は停止する
		if (this.cameraActive) {
			this.cameraActive = false;
			this.stopAcquisition();
			// 処理終了
			return;
		}

		// カメラ
		cameraId = pObj.cameraID;
		capObj.open(cameraId);
		int capwidth = Integer.valueOf(capW_text.getText()).intValue();
		int capHeight =Integer.valueOf(capH_text.getText()).intValue();
		boolean wset = capObj.set(Videoio.CAP_PROP_FRAME_WIDTH, capwidth);
		boolean hset = capObj.set(Videoio.CAP_PROP_FRAME_HEIGHT, capHeight);
		capObj.set(Videoio.CAP_PROP_BUFFERSIZE,3);
		//boolean wset = capture.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
		//boolean hset = capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
		Platform.runLater( () ->info2.appendText("カメラ解像度 WIDTH=" +
								capwidth +"(" + wset + ") HEIGHT=" + capHeight + "("+hset+")\n"));
		Platform.runLater( () ->info2.appendText("MAXフレームレート"+String.valueOf(capObj.get(Videoio.CAP_PROP_FPS))+"\n"));
		Platform.runLater( () ->info2.appendText("バッファサイズ " +
						String.valueOf(capObj.get(Videoio.CAP_PROP_BUFFERSIZE))+"\n"));

		// カメラが開いていない時
		if (capObj.isOpened() == false) {
			// エラーログを出力して処理を終了する
			Platform.runLater( () ->info2.appendText("カメラが接続されていません。\nデモモードで起動します。\n"));
			demoFlg = true;

		}else {
			// カメラが正常に開いている時
			this.cameraActive = true;

			//キャリブレーションデーター読み込み
			File calibFile = new File("./CameraCalibration.xml");
			if( calibFile.exists() ) {
				cameraCalibFlg = true;
				final Path filePath = Paths.get("./CameraCalibration.xml");
				final Map<String, Mat> calibrationMats = MatIO.loadMat(filePath);
				cameraMatrix = calibrationMats.get("CameraMatrix");//内部パラメータ
				distortionCoefficients = calibrationMats.get("DistortionCoefficients");//歪み係数
			}
		}
		source_video = new VideoCapture("./test.mp4" );//デモモード用動画
		double video_width = source_video.get( Videoio.CAP_PROP_FRAME_WIDTH ); // 横幅を取得
		double video_height = source_video.get( Videoio.CAP_PROP_FRAME_HEIGHT ); // 縦幅を取得
		double video_frame_count = source_video.get( Videoio.CAP_PROP_FRAME_COUNT ); // フレーム数を取得
		double video_fps = source_video.get( Videoio.CAP_PROP_FPS ); // フレームレートを取得
		if(demoFlg) {
			 Platform.runLater( () ->info2.appendText("demo動画の横幅 : " + video_width+"\n" ));
			 Platform.runLater( () ->info2.appendText("demo動画の縦幅 : " + video_height+"\n" ));
			 Platform.runLater( () ->info2.appendText("demo動画のフレーム数 : " + video_frame_count +"\n"));
			 Platform.runLater( () ->info2.appendText("demo動画のフレームカウント : " + video_fps+"\n" ));
		}

		//GPIOボードオープン
		Gpio.open(String.valueOf(pObj.portNo), pObj.adc_thresh,pObj.adcFlg);

		//デバッグコード

			//Gpio.readAll();

		 // メインクラス
		Runnable frameGrabber = new Runnable() {
			Mat realMat;
			@Override
			public void run() {
				//設定自動ロック用
				if( settingModeFlg && !saveImgUseFlg) {
					if( System.currentTimeMillis() - lockedTimer > lockedTimerThresh) {
						onSettingModeBtn(null);
					}
				}

				if( throughImageChk.isSelected() && !demoFlg ) {
					realMat = grabFrame();
					if( realMat.width() > 0 ) {
						Imgproc.putText(realMat, "CAMERA Through",
								new Point(5,50),
								Imgproc.FONT_HERSHEY_SIMPLEX,2.0,new Scalar(0,255,0),3);
						updateImageView(imgORG, Utils.mat2Image(realMat));
					}else {
						Platform.runLater( () ->info2.appendText("スルー画像の取得に失敗\n"));
					}

				}else {
			    	if( !manualTrigger && !autoTrigger) {
			    		Platform.runLater( () ->triggerCCircle.setFill(Color.LIGHTSLATEGRAY));
			    	}
			    	if( eventTrigger) {
			    		if( !dragging ) {
			    			eventTrigger = false;
			    		}
			    		rePaint();
			    	}

					if( (manualTrigger || autoTrigger) && !saveImgUseFlg) {//マニュアルトリガ又はオートトリガが有効であった場合
			    		manualTrigger = false;
				    	if( demoFlg ) {
				    		try {
				    			framCnt++;
				    			if(framCnt>=video_frame_count) {
				    				source_video.set(Videoio.CAP_PROP_POS_FRAMES,0);
				    				Platform.runLater( () ->info2.appendText(("demo動画ループ再生\n")));
				    				framCnt=0;
				    			}
				    			source_video.read(srcMat);


				    			if( srcMat == null) {
				    				Platform.runLater( () ->info2.appendText(( "demo動画が再生できません\n")));
				    			}else {
				    				rePaint();
				    			}

				    		}catch(Exception e) {
				    			Platform.runLater( () ->info2.appendText("demo動画エラー"+e.toString()+"\n"));
				    			return;
				    		}
						}else {
							srcMat = grabFrame();
					    	if( srcMat.width() !=0 ) {
					    		rePaint();
					    	}
						}
			    	}else if(shutterFlg && !demoFlg && !saveImgUseFlg) {
						srcMat = grabFrame();
				    	if( srcMat.width() > 0 ) {
				    		rePaint();
				    	}else {
				    		Platform.runLater( () ->info2.appendText(("カメラから画像の取得に失敗\n")));
				    	}
				    	shutterFlg = false;
					}

			    	if( eventTrigger ) {
			    		rePaint();
			    	}
				}
			}
		};
		timer = Executors.newSingleThreadScheduledExecutor();
		timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

		if( Gpio.openFlg ) {
			if( Gpio.OkSignalON() ) {
				Platform.runLater( () ->GPIO_STATUS_PIN3.setFill(Color.BLUE));
			}else {
				Platform.runLater( () ->GPIO_STATUS_PIN3.setFill(Color.RED));
			}
			//トリガクラス
			Runnable triggerLoop = new Runnable() {
				String rt = "-1";//nullを避ける為-1をいれておく
				long debugCnt = 0;

				@Override
				public void run() {

					try {
						//readIO ="nothing";
						if( debugFlg ) {
							System.out.println("GPIO READ/WRITE" + debugCnt);
							debugCnt++;
						}
						//オールクリア信号受信
						rt = Gpio.clearSignal();
						if( rt == "1" ) {
							Platform.runLater(() ->info2.appendText("PLCからクリア信号を受信しました"));
							onAllClear(null);
				    		Platform.runLater( () ->GPIO_STATUS_PIN1.setFill(Color.YELLOW));

						}else {
							Platform.runLater( () ->GPIO_STATUS_PIN1.setFill(Color.LIGHTGRAY));
						}
						//シャッター信号受信
						//readIO ="shutterSignal";
						rt = Gpio.shutterSignal();
						//Platform.runLater( () ->info1.setText(Gpio.useFlg  + String.valueOf(loopcnt)+  "  GPIO 0(SHUTTER TRIGGER) = " + rt));

						if( rt == "1") {
							if( !offShutterFlg) {//シャッタートリガがoffになるまでshutterFlgをtrueにしない
								//シャッター
								try {
									Thread.sleep( dellySpinner.getValue() );
								} catch (InterruptedException e) {
									e.printStackTrace();
								}

								shutterFlg = true;
								offShutterFlg = true;
					    		Platform.runLater( () ->GPIO_STATUS_PIN0.setFill(Color.YELLOW));
					    		//Platform.runLater( () ->info2.appendText("シャッターON"));
							}
						}else{
								offShutterFlg = false;
					    		Platform.runLater( () ->GPIO_STATUS_PIN0.setFill(Color.LIGHTGRAY));
						}
					}catch(NullPointerException e) {
						System.out.println("readIO" + " / " + e.toString());
					}
				}
			};
			timer2 = Executors.newSingleThreadScheduledExecutor();
			timer2.scheduleAtFixedRate(triggerLoop, 0, 10, TimeUnit.MILLISECONDS);
		}

		if( !Gpio.openFlg ) {
			Platform.runLater( () ->info2.appendText("\n---------------\n- GPIO異常 -\n----------------\n"));
		}
    	Platform.runLater( () ->info2.appendText("検査を開始しました\n"));

    }
	/**
	 * 開いているビデオストリームからフレームを取得する
	 * @return {@link Mat}
	 */
	private Mat grabFrame() {
		Mat frame = new Mat();
		if (capObj.isOpened()) {
			try {
				capObj.read(frame);
				if (frame.empty() == false) {
					//Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
				}

			} catch(Exception e) {
				Platform.runLater( () ->info2.appendText("Exception during the image elaboration: " + e +"\n"));
			}
		}
		if( cameraCalibFlg ) {
			Calib3d.undistort(frame, dstframe, cameraMatrix, distortionCoefficients);
			return dstframe;
		}
		return frame;
	}

    @FXML
    void onTestStop(ActionEvent event) throws Exception {
    	this.cameraActive = false;
		VisonController.timer.shutdown();
		VisonController.timer.awaitTermination(33, TimeUnit.MICROSECONDS);
		if( timer2 != null ) {
			VisonController.timer2.shutdown();
			VisonController.timer2.awaitTermination(10, TimeUnit.MICROSECONDS);
			Gpio.ngSignalON();
			Gpio.close();
		}
    	stopAcquisition();
    	Platform.runLater( () ->info2.appendText("検査を停止しました\n"));
    }
	 void stopAcquisition() {
		if (timer != null && timer.isShutdown() == false) {
			try {
				timer.shutdown();
				timer.awaitTermination(33, TimeUnit.MICROSECONDS);
			} catch(Exception e) {
				// log any exception
				Platform.runLater( () ->info2.appendText("Exception in stopping the frame capture, trying to release the camera now... " + e +"\n"));
			}
		}
		// @FIXME-[カメラを解放するだけで良い？]
		if (capObj != null ) {
			capObj.release();
		}
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

	/**
	 * On application close, stop the acquisition from the camera
	 */
	protected void setClosed() {
		this.stopAcquisition();
	}

	//ビューの移動
	private void moveView() {
    	vRect = imgORG.getViewport();//メインビューのサイズを取得
    	double xMin,yMin,width,height,imgWidth,imgHeight;
    	width = vRect.getWidth();
    	height = vRect.getHeight();
    	imgWidth = imgORG.getImage().getWidth();//メインビューに格納されているイメージのサイズ取得
    	imgHeight = imgORG.getImage().getHeight();

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
    	Platform.runLater(() ->imgORG.setViewport(vRect));
    	Platform.runLater(() ->imgGLAY1.setViewport(vRect));
    	Platform.runLater(() ->imgGLAY2.setViewport(vRect));
    	Platform.runLater(() ->imgGLAY3.setViewport(vRect));
    	Platform.runLater(() ->imgGLAY.setViewport(vRect));
	}

    @FXML
    void mouseDragged(MouseEvent e) { //imgORG上でドラッグ
    	double zoom = this.zoomValue_slider.getValue();
    	if( moveDragingFlg && e.isMiddleButtonDown() ) {
            moveDraggingPoint[1].x = e.getX();
            moveDraggingPoint[1].y = e.getY();
            moveView();
    		return;
    	}

    	if( !settingModeFlg ) return;
    	int x = draggingRect.x;
        int y = draggingRect.y;
        double mX = e.getX()/zoom;
        double mY = e.getY()/zoom;

        if( mX > srcMat.width() ) {
        	mX = srcMat.width()-1;
        }
        if( mY > srcMat.height() ) {
        	mY = srcMat.height()-1;
        }
        draggingRect.width = (int)(imgORG.getViewport().getMinX() + mX - x);
        draggingRect.height =(int)(imgORG.getViewport().getMinY() + mY - y);

        eventTrigger = true;
    }

    @FXML
    void mousePressed(MouseEvent e) { //imgORG上でマウスプレス
    	double zoom = this.zoomValue_slider.getValue();
    	if( e.isMiddleButtonDown() ) {
    		moveDragingFlg = true;
            moveDraggingPoint[0].x = e.getX();
            moveDraggingPoint[0].y = e.getY();
            moveDraggingPointView.x = imgORG.getViewport().getMinX();
            moveDraggingPointView.y = imgORG.getViewport().getMinY();
    		return;
    	}
    	if( !settingModeFlg ) return;
        draggingRect.x = (int)(imgORG.getViewport().getMinX() + e.getX()/(zoom));
        draggingRect.y = (int)(imgORG.getViewport().getMinY() + e.getY()/(zoom));
        dragging = true;

        eventTrigger = true;
    }

    @FXML
    void mouseReleased(MouseEvent e) { //imgORG マウスボタン離す
    	if( moveDragingFlg && e.isMiddleButtonDown() ) {
    		moveDragingFlg = false;
    		return;
    	}
    	if( !settingModeFlg ) return;
    	dragging = false;
    	eventTrigger = true;
    }


    private void rePaint() {
    	if( saveImgUseFlg ) {
    		srcMat = saveImgMat.clone();
    		autoTrigger = false;
    	}
    	if( srcMat.width() < 1) return;
    	try {
	    	if( this.triggerCCircle.getFill() != Color.YELLOW) {
	    		Platform.runLater( () ->this.triggerCCircle.setFill(Color.YELLOW));
	    	}else {
	    		Platform.runLater( () ->this.triggerCCircle.setFill(Color.GREEN));
	    	}

	    	fpsCnt++;
	    	if( fpsCnt == 30) {
	    		fpsEnd = System.currentTimeMillis();

	    		fps = fpsCnt/((fpsEnd - fpsFirst)/1000.0);
	    		Platform.runLater( () ->fpsLabel.setText(String.format("FPS=%.1f", fps)));

	    		fpsFirst = System.currentTimeMillis();
	    		fpsCnt=0;
	    	}


	    	parameter para = pObj.para[pObj.select];
	    	Mat	orgMat = srcMat.clone();//srcMatは不変にしておく
	    	Mat saveSrcMat = srcMat.clone();

	    	glayMat = new Mat();
	    	Imgproc.cvtColor(orgMat, glayMat, Imgproc.COLOR_BGR2GRAY);
	    	Mat ptnAreaMat = glayMat.clone();

	    	Mat tmp0Mat = glayMat.clone();
			if( settingMode.isSelected()) {
		    	Mat tmp1Mat = glayMat.clone();
		    	Mat tmp2Mat = glayMat.clone();
		    	Mat tmp3Mat = glayMat.clone();
		    	Mat fillterAftterMat = glayMat.clone();
		    	int selFlg = 0;
		    	//ガウシアン→２値化→膨張の順番
		    	if( gauusianCheck.isSelected() ) {
		    		double sigmaX = gauusianSliderX.getValue();
		    		double sigmaY = gauusianSliderY.getValue();
		    		int tmpValue =(int)gauusianSliderA.getValue();
		    		if( tmpValue % 2 == 0 ) {
		    			tmpValue++;
		    		}
		    		Size sz = new Size(tmpValue,tmpValue);
		    		Imgproc.GaussianBlur(tmp1Mat, tmp1Mat, sz, sigmaX,sigmaY);
		    		selFlg+=1;
		    	}
		    	if( threshholdCheck.isSelected()) {
		    		int type = threshhold_Inverse.isSelected()?Imgproc.THRESH_BINARY_INV:Imgproc.THRESH_BINARY;
		    		Imgproc.threshold(tmp1Mat, tmp2Mat, this.threshholdSlider.getValue(),255,type);
		    		selFlg+=2;
		    	}
		    	if( dilateCheck.isSelected() ) {
		    		int n = (int)dilateSliderN.getValue();

		    		if(threshholdCheck.isSelected()) {
			    		Imgproc.dilate(tmp2Mat, tmp3Mat, new Mat(),new Point(-1,-1),n);
			    	}else {
			    		Imgproc.dilate(tmp1Mat, tmp3Mat, new Mat(),new Point(-1,-1),n);
			    	}
		    		selFlg+=4;
		    	}
		    	switch(selFlg) {
		    	case 0://全てなし
		    		Imgproc.Canny(tmp1Mat, fillterAftterMat,sliderDetecPara6.getValue(),sliderDetecPara6.getValue()/2);
			        updateImageView(imgGLAY1, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY2, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY3, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY, Utils.mat2Image(fillterAftterMat));
		    		break;
		    	case 1://ガウシアンのみ
					Imgproc.Canny(tmp1Mat, fillterAftterMat,sliderDetecPara6.getValue(),sliderDetecPara6.getValue()/2);
			        updateImageView(imgGLAY1, Utils.mat2Image(tmp1Mat));
			        updateImageView(imgGLAY2, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY3, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY, Utils.mat2Image(fillterAftterMat));
		    		break;
		    	case 2://２値化のみ
					Imgproc.Canny(tmp2Mat, fillterAftterMat,sliderDetecPara6.getValue(),sliderDetecPara6.getValue()/2);
			        updateImageView(imgGLAY1, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY2, Utils.mat2Image(tmp2Mat));
			        updateImageView(imgGLAY3, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY, Utils.mat2Image(fillterAftterMat));
		    		break;
		    	case 3://ガウシアンと２値化あり
					Imgproc.Canny(tmp2Mat, fillterAftterMat,sliderDetecPara6.getValue(),sliderDetecPara6.getValue()/2);
			        updateImageView(imgGLAY1, Utils.mat2Image(tmp1Mat));
			        updateImageView(imgGLAY2, Utils.mat2Image(tmp2Mat));
			        updateImageView(imgGLAY3, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY, Utils.mat2Image(fillterAftterMat));
		    		break;
		    	case 4://膨張のみ
					Imgproc.Canny(tmp3Mat, fillterAftterMat,sliderDetecPara6.getValue(),sliderDetecPara6.getValue()/2);
			        updateImageView(imgGLAY1, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY2, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY3, Utils.mat2Image(tmp3Mat));
			        updateImageView(imgGLAY, Utils.mat2Image(fillterAftterMat));
		    		break;
		    	case 5://ガウシアンと膨張あり
					Imgproc.Canny(tmp3Mat, fillterAftterMat,sliderDetecPara6.getValue(),sliderDetecPara6.getValue()/2);
			        updateImageView(imgGLAY1, Utils.mat2Image(tmp1Mat));
			        updateImageView(imgGLAY2, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY3, Utils.mat2Image(tmp3Mat));
			        updateImageView(imgGLAY, Utils.mat2Image(fillterAftterMat));
		    		break;
		    	case 6://ガウシアン無し　他あり
		    		Imgproc.Canny(tmp3Mat, fillterAftterMat,sliderDetecPara6.getValue(),sliderDetecPara6.getValue()/2);
			        updateImageView(imgGLAY1, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY2, Utils.mat2Image(tmp2Mat));
			        updateImageView(imgGLAY3, Utils.mat2Image(tmp3Mat));
			        updateImageView(imgGLAY, Utils.mat2Image(fillterAftterMat));
		    		break;
		    	case 7://全てあり
					Imgproc.Canny(tmp3Mat, fillterAftterMat,sliderDetecPara6.getValue(),sliderDetecPara6.getValue()/2);
			        updateImageView(imgGLAY1, Utils.mat2Image(tmp1Mat));
			        updateImageView(imgGLAY2, Utils.mat2Image(tmp2Mat));
			        updateImageView(imgGLAY3, Utils.mat2Image(tmp3Mat));
			        updateImageView(imgGLAY, Utils.mat2Image(fillterAftterMat));
		    		break;
		    	}
			}

	        if (dragging && settingModeFlg) {
	            Imgproc.rectangle(orgMat,
	            		new Point(draggingRect.x,draggingRect.y),
	            		new Point(draggingRect.x+draggingRect.width,draggingRect.y+draggingRect.height),
	            		new Scalar(0,255,0),3);
	        }else{
	        	if(draggingRect.width >0 && draggingRect.height > 0 && settingModeFlg){
		        	Mat roi = glayMat.submat(
		        			draggingRect.y,
		        			draggingRect.y + draggingRect.height,
		        			draggingRect.x,
		        			draggingRect.x+draggingRect.width);
		        	if( gauusianCheck.isSelected() ) {
		        		double sigmaX = gauusianSliderX.getValue();
		        		double sigmaY = gauusianSliderY.getValue();
		        		int tmpValue =(int)gauusianSliderA.getValue();
		        		if( tmpValue % 2 == 0 ) {
		        			tmpValue++;
		        		}
		        		Size sz = new Size(tmpValue,tmpValue);
		        		Imgproc.GaussianBlur(roi, roi, sz, sigmaX,sigmaY);
		        	}
		        	if( threshholdCheck.isSelected()) {
		        		int type = threshhold_Inverse.isSelected()?Imgproc.THRESH_BINARY_INV:Imgproc.THRESH_BINARY;
		        		Imgproc.threshold(roi, roi, this.threshholdSlider.getValue(),255,type);
		        	}
		        	if( dilateCheck.isSelected()) {
		        		Imgproc.dilate(roi, roi, new Mat(),new Point(-1,-1),(int)dilateSliderN.getValue());
		        	}
		        	//穴検出
		        	if( !ptmSetStartFlg ) {
			            Mat circles = new Mat();
						Imgproc.HoughCircles(roi, circles, Imgproc.CV_HOUGH_GRADIENT,
								sliderDetecPara4.getValue(),
								sliderDetecPara5.getValue(),
								sliderDetecPara6.getValue(),
								sliderDetecPara7.getValue(),
								(int)sliderDetecPara8.getValue(),
								(int)sliderDetecPara9.getValue());
						if( circles.cols() > 0) {
							fncDrwCircles(roi,circles,
									orgMat.submat(
						        			draggingRect.y,
						        			draggingRect.y + draggingRect.height,
						        			draggingRect.x,
						        			draggingRect.x+draggingRect.width),
											true);

							Imgproc.putText(orgMat, String.valueOf(circles.cols()),
									new Point(draggingRect.x-25,draggingRect.y-6),
									Imgproc.FONT_HERSHEY_SIMPLEX, 2.0,new Scalar(0,0,255),2);
							//面積判定
							boolean ratioFlg = holeWhiteAreaCheck(
									roi,circles,whiteRatioMaxSp.getValue(),whiteRatioMinSp.getValue());
							if( ratioFlg ) {
								Imgproc.putText(orgMat,
										"WhiteArea OK  ave=" + String.format("%d",whiteAreaAverage) +
												" Max=" + String.format("%d",whiteAreaMax) +
												" Min=" + String.format("%d",whiteAreaMin),
										new Point(draggingRect.x+20,draggingRect.y-6),
										Imgproc.FONT_HERSHEY_SIMPLEX, 1.5,new Scalar(0,255,0),2);
							}else {
								Imgproc.putText(orgMat, "WhiteArea NG  ave=" + String.format("%d",whiteAreaAverage) +
										" Max=" + String.format("%d",whiteAreaMax) +
										" Min=" + String.format("%d",whiteAreaMin),
										new Point(draggingRect.x+20,draggingRect.y-6),
										Imgproc.FONT_HERSHEY_SIMPLEX, 1.5,new Scalar(0,255,255),2);
							}
						}
		        	}
		            Imgproc.rectangle(orgMat,
		            		new Point(draggingRect.x,draggingRect.y),
		            		new Point(draggingRect.x+draggingRect.width,draggingRect.y+draggingRect.height),
		            		new Scalar(0,255,0),4);
	        	}
	        }

	    	int hanteCnt=0;
	    	String fileString ="";
	    	boolean ngFlg;
			Scalar color;
	        for (int i=0;i<4;i++) {
				ngFlg = false;
	        	if( para.setFlg[i] ) {
		        	Rectangle r = para.rects[i];
		        	Mat roi = tmp0Mat.submat(new Rect(r.x,r.y,r.width,r.height));
		        	if( para.gauusianCheck[i] ) {
		        		double sigmaX = para.gauusianSliderX[i];
		        		double sigmaY = para.gauusianSliderY[i];
		        		int tmpValue =(int) para.gauusianSliderA[i];
		        		if( tmpValue % 2 == 0 ) {
		        			tmpValue++;
		        		}
		        		Size sz = new Size(tmpValue,tmpValue);
		        		Imgproc.GaussianBlur(roi, roi, sz, sigmaX,sigmaY);
		        	}
		        	if( para.threshholdCheck[i]) {
		        		int type = para.threshhold_Invers[i]?Imgproc.THRESH_BINARY_INV:Imgproc.THRESH_BINARY;
		        		Imgproc.threshold(roi, roi, para.threshhold[i],255,type);
		        	}
		        	if( para.dilateCheck[i]) {
		        		Imgproc.dilate(roi, roi, new Mat(),new Point(-1,-1),para.dilateSliderN[i]);
		        	}

					/*# ハフ変換で円検出する。
					第1引数(gray)：8bit、1チャンネルのグレースケール画像。
				 	第2引数(circles)：検出した円のベクトル(x,y,r)→ x, yは円の中心座標でrは半径
					第3引数(Imgproc.CV_HOUGH_GRADIENT)：２値化の手法
					第4引数(sliderDetecPara4)：画像分解能に対する投票分解能の比率の逆数
					第5引数(sliderDetecPara5)：円の中心同士の最小距離
					第6引数(sliderDetecPara6)：２値化のパラメータ1
					第7引数(sliderDetecPara7)：２値化のパラメータ2
					第8引数(sliderDetecPara8)：円の半径の最小値
					第9引数(sliderDetecPara9)：円の半径の最大値 */
		            Mat circles = new Mat();
					Imgproc.HoughCircles(roi, circles, Imgproc.CV_HOUGH_GRADIENT,
							para.circlePara4[i],
							para.circlePara5[i],
							para.circlePara6[i],
							para.circlePara7[i],
							(int)para.circlePara8[i],
							(int)para.circlePara9[i]);

					boolean areaFlg = true;
					if( circles.cols() > 0 && !settingMode.isSelected()) {
						fncDrwCircles(roi,circles, orgMat.submat(new Rect(r.x,r.y,r.width,r.height)),false);
						Imgproc.putText(orgMat, String.valueOf(circles.cols()),
								new Point(r.x-25,r.y-6),
								Imgproc.FONT_HERSHEY_SIMPLEX, 2.0,new Scalar(0,255,0),3);
						//面積判定
						areaFlg = holeWhiteAreaCheck(
								roi,circles,para.whiteAreaMax[i],para.whiteAreaMin[i]);
						if( areaFlg ) {
							Imgproc.putText(orgMat, "WhiteArea OK  ave=" + String.format("%d",whiteAreaAverage) +
									" Max=" + String.format("%d",whiteAreaMax) +
									" Min=" + String.format("%d",whiteAreaMin),
									new Point(r.x+20,r.y-6),
									Imgproc.FONT_HERSHEY_SIMPLEX, 1.5,new Scalar(0,255,0),2);
						}else {
							Imgproc.putText(orgMat, "WhiteArea NG  ave=" + String.format("%d",whiteAreaAverage) +
									" Max=" + String.format("%d",whiteAreaMax) +
									" Min=" + String.format("%d",whiteAreaMin),
									new Point(r.x+20,r.y-6),
									Imgproc.FONT_HERSHEY_SIMPLEX, 1.5,new Scalar(0,255,255),2);
						}
					}

					//判定
		            switch(i) {
		            	case 0:
		            		Platform.runLater( () ->okuri1_n.setText( String.format("%d個", circles.cols()) + infoText));
		            		if( circles.cols() == para.cntHoleTh[i] && areaFlg ) {
		            			Platform.runLater( () ->okuri1_judg.setText("OK"));
		            			Platform.runLater( () ->okuri1_judg.setTextFill( Color.GREEN));
		            			hanteCnt++;
		            		}else {
		            			Platform.runLater( () ->okuri1_judg.setText("NG"));
		            			Platform.runLater( () ->okuri1_judg.setTextFill( Color.RED));
		            			fileString += "1_okuri_";
		            			ngFlg = true;
		            		}
		            		break;
		            	case 1:
		            		Platform.runLater( () ->okuri2_n.setText(String.format("%d個", circles.cols()) + infoText));
		            		if( circles.cols() == para.cntHoleTh[i] && areaFlg ) {
		            			Platform.runLater( () ->okuri2_judg.setText("OK"));
		            			Platform.runLater( () ->okuri2_judg.setTextFill( Color.GREEN));
		            			hanteCnt++;
		            		}else {
		            			Platform.runLater( () ->okuri2_judg.setText("NG"));
		            			Platform.runLater( () ->okuri2_judg.setTextFill( Color.RED));
		            			fileString += "1_poke_";
		            			ngFlg = true;
		            		}
		            		break;
		            	case 2:
		            		Platform.runLater( () ->okuri3_n.setText( String.format("%d個", circles.cols()) + infoText));
		            		if( circles.cols() == para.cntHoleTh[i] && areaFlg ) {
		            			Platform.runLater( () ->okuri3_judg.setText("OK"));
		            			Platform.runLater( () ->okuri3_judg.setTextFill( Color.GREEN));
		            			hanteCnt++;
		            		}else {
		            			Platform.runLater( () ->okuri3_judg.setText("NG"));
		            			Platform.runLater( () ->okuri3_judg.setTextFill( Color.RED));
		            			fileString += "2_okuri_";
		            			ngFlg = true;			            		}
		            		break;
		            	case 3:
		            		Platform.runLater( () ->okuri4_n.setText( String.format("%d個", circles.cols()) + infoText));
		            		if( circles.cols() == para.cntHoleTh[i] && areaFlg ) {
		            			Platform.runLater( () ->okuri4_judg.setText("OK"));
		            			Platform.runLater( () ->okuri4_judg.setTextFill( Color.GREEN));
		            			hanteCnt++;
		            		}else {
		            			Platform.runLater( () ->okuri4_judg.setText("NG"));
		            			Platform.runLater( () ->okuri4_judg.setTextFill( Color.RED));
		            			fileString += "2_poke_";
		            			ngFlg = true;
		            		}
		            		break;
		            	}

					if( ngFlg ) {
						color = new Scalar(0,0,255);
					}else {
						color = new Scalar(255,0,0);
					}
					if( !settingMode.isSelected()) {
			            Imgproc.rectangle(orgMat,new Point(r.x, r.y),
			            		new Point(r.x+r.width, r.y+r.height),
			            		color,4);
					}
		        }else {
		        	hanteCnt++;
		        }

	        }


	        //最終判定
	        if( !saveImgUseFlg && !settingModeFlg) {
		        //パターンマッチング
		        boolean tmFlg = tm.detectPattern(ptnAreaMat,orgMat,false);

	        	if(hanteCnt==4 && tmFlg ) {
		        	Platform.runLater( () ->judg.setText("OK"));
		        	Platform.runLater( () ->judg.setTextFill(Color.GREEN));
		        	//画像保存
		        	if( this.imgSaveFlg_all.isSelected() ) {
		        		saveImgOK( saveSrcMat );
		        	}
		        }else {
		        	Platform.runLater( () ->judg.setText("NG"));
		        	Platform.runLater( () ->judg.setTextFill(Color.RED));
		        	//画像保存
		        	if( imgSaveFlg.isSelected() && ngCnt < saveMax_ng && !settingModeFlg) {
		        		saveImgNG( saveSrcMat,fileString);
		        		saveImgNG( orgMat,fileString);
		        	}else if( fileString != ""){
		        		final String infoText = fileString +"\n";
		        		Platform.runLater( () ->info2.appendText(infoText));
		        	}
		        	if( ngCnt < 999) ngCnt++;

		        	//出力トリガが無効で無い場合
		        	if( !outTrigDisableChk.isSelected() ){
		        		Platform.runLater(() ->aPane.setStyle("-fx-background-radius: 0;-fx-background-color: rgba(255,0,0,0.5);"));
		        		if( Gpio.openFlg) {
		        			while( Gpio.useFlg ) {
		        				System.out.println("rePaint() Gpio.useFlg=true");
		        			}
		        			if( Gpio.ngSignalON() ) {
					    		Platform.runLater( () ->GPIO_STATUS_PIN3.setFill(Color.YELLOW));
		        			}else {
					    		Platform.runLater( () ->GPIO_STATUS_PIN3.setFill(Color.RED));
		        			}
		        		}
		        	}

		        	Platform.runLater(() ->ngCounterLabel.setText(String.valueOf(ngCnt)));
		        	updateImageView(imgNG, Utils.mat2Image(orgMat));
		        }
	        }
	        updateImageView(imgORG, Utils.mat2Image(orgMat));
    	}catch(Exception e) {
    		Platform.runLater(() ->info2.appendText(e+"\n:検査設定がキャプチャーされた画像からはみ出しています。\n検査設定をやり直してください\n"));
    	}
    }

    /**
     * NG画像保存
     * @param imgMat
     * @param fileString
     */
    public void saveImgNG(Mat imgMat,String fileString) {
    	if( NGsaveCnt == 2) {
    		if( System.currentTimeMillis() - savelockedTimer < savelockedTimerThresh) {
    			return;
    		}else {
    			NGsaveCnt = 0;
    		}
    	}
		savelockedTimer = System.currentTimeMillis();
		NGsaveCnt++;

    	File folder = new File("./ng_image");
    	if( !folder.exists()) {
    		if( !folder.mkdir() ) {
    			Platform.runLater( () ->info2.appendText("ng_imageフォルダの作成に失敗"+"\n"));
    			Platform.runLater( () ->this.info1.setText("ng_imageフォルダの作成に失敗"));
    			return;
    		}
    	}
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSS");
        String fileName = fileString +"_" + sdf.format(timestamp) + "_" +String.valueOf(ngCnt);
        try {
        	Imgcodecs.imwrite(folder+"/" + fileName + ".jpeg", imgMat);
        	Platform.runLater( () ->info2.appendText(folder+"/"+ fileName +".jpeg"+"NG画像保存"+"\n"));
        }catch(Exception e) {
        	Platform.runLater( () ->info2.appendText("NG画像の保存に失敗"+e.toString()+"\n"));
        }
        Platform.runLater( () ->info2.appendText("NG画像ファイルを保存\n" + fileName +".jpeg\n"));

    }
    /**
     * OK画像保存
     * @param imgMat
     */
    public void saveImgOK(Mat imgMat) {
		if( System.currentTimeMillis() - savelockedTimer < savelockedTimerThresh) {
			return;
		}else {
			savelockedTimer = System.currentTimeMillis();
		}
		File folder = new File("./ok_image");
    	if( !folder.exists()) {
    		if( !folder.mkdir() ) {
    			Platform.runLater( () ->info2.appendText("ng_imageフォルダの作成に失敗"+"\n"));
    			return;
    		}
    	}
        allSaveCnt++;
        try {
        	Imgcodecs.imwrite(folder+"/" + allSaveCnt + ".jpeg", imgMat);
        }catch(Exception e) {
        	Platform.runLater( () ->info2.appendText("OK画像の保存に失敗"+e.toString()+"\n"));
        }

        if( allSaveCnt > saveMax_all+1 ) {
        	File f = new File(folder+"/"+(allSaveCnt-saveMax_all)+".jpeg");
        	Platform.runLater( () ->{if( !f.delete() ) {
        		System.out.println(f.toString()+"削除失敗");
        		}
        	});
        }
    }
    /**
     * 撮影画像保存
     * @param imgMat
     * @param fileString
     */
    public void SaveshotImg(Mat imgMat,String fileString) {
    	File folder = new File("./shot_image");
    	if( !folder.exists()) {
    		if( !folder.mkdir() ) {
    			Platform.runLater( () ->info2.appendText("shot_imageフォルダの作成に失敗"+"\n"));
    			Platform.runLater( () ->this.info1.setText("shot_imageフォルダの作成に失敗"));
    			return;
    		}
    	}
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSS");
        String fileName = fileString +"_" + sdf.format(timestamp) + "_" +String.valueOf(ngCnt);
        try {
        	Imgcodecs.imwrite(folder+"/" + fileName + ".jpeg", imgMat);
        	Platform.runLater( () ->info2.appendText(folder+"/"+ fileName +".jpeg"+"shot画像保存"+"\n"));
        }catch(Exception e) {
        	Platform.runLater( () ->info2.appendText("shot画像の保存に失敗"+e.toString()+"\n"));
        }
        Platform.runLater( () ->info2.appendText("shot画像ファイルを保存\n" + fileName +".jpeg\n"));

    }
    /**
     * チェスボード画像保存
     * @param imgMat
     * @param fileString
     */
    public void SavechessImg(Mat imgMat,String fileString) {
    	File folder = new File("./chess_image");
    	if( !folder.exists()) {
    		if( !folder.mkdir() ) {
    			Platform.runLater( () ->info2.appendText("chess_imageフォルダの作成に失敗"+"\n"));
    			return;
    		}
    	}
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSS");
        String fileName = fileString +"_" + sdf.format(timestamp) + "_" +String.valueOf(ngCnt);
        try {
        	Imgcodecs.imwrite(folder+"/" + fileName + ".jpeg", imgMat);
        	Platform.runLater( () ->info2.appendText(folder+"/"+ fileName +".jpeg"+"チェスボード画像保存"+"\n"));
        }catch(Exception e) {
        	Platform.runLater( () ->info2.appendText("チェスボード画像の保存に失敗"+e.toString()+"\n"));
        }
        Platform.runLater( () ->info2.appendText("チェスボード画像ファイルを保存\n" + fileName +".jpeg\n"));

    }

    /**
     * 検出円描画
     * @param circles
     * @param img
     */
    private String fncDrwCircles(Mat judgeAreaMat,Mat circles ,Mat img,boolean infoFlg) {
  	  double[] data,data2;
  	  double rho;
  	  Point pt = new Point();
  	  infoText = "";
  	  double radiusMax,radiusMin,radiusAve,distAve;
  	  radiusMax = 0;
  	  radiusMin = 9999;
  	  radiusAve = 0;
  	  distAve = 0;

  	  //ソーティング　Ｘ昇順
  	  for (int i = 0; i < circles.cols(); i++){
  	  	  for (int j = 1+i; j < circles.cols(); j++){
			data = circles.get(0, i);
			data2 = circles.get(0, j);
			if( data[0] > data2[0] ) {
				circles.put(0, i,data2);
				circles.put(0, j,data);
			}
  	  	  }
  	  }
  	  //最大、最小、平均、距離平均計算
  	  if( circles.cols() >= 2) {
	  	  for( int i= 0; i < circles.cols(); i++) {
	  		  double r = circles.get(0, i)[2];
	  		  radiusAve += r;

	  		  if( radiusMax < r ) radiusMax = r;
	  		  if( radiusMin > r ) radiusMin = r;

	  		  if( i != circles.cols()-1 ) {
	  			  distAve += circles.get(0, circles.cols()-1)[0] - circles.get(0, circles.cols()-2)[0];
	  		  }
	  	  }
  		  distAve /= (circles.cols()-1);
  	  	  radiusAve /= circles.cols();
  	  }else {
  		radiusAve = circles.get(0, 0)[2];
  		radiusMax = radiusAve;
  		radiusMin = radiusAve;
  		distAve = 0;
  	  }

  	  infoText += String.format("MAX=%.1f ,MIN=%.1f ,AVE=%.1f ,DistAve=%.1f ",
  			  radiusMax,radiusMin,radiusAve,distAve);

  	  for (int i = 0; i < circles.cols(); i++){
  	    data = circles.get(0, i);
  	    pt.x = data[0];
  	    pt.y = data[1];
  	    rho = data[2];
  	    Imgproc.circle(img, pt, (int) rho, new Scalar(0,255,0),5);
  	    Imgproc.arrowedLine(img, new Point(pt.x-50,pt.y-50),
  	    		new Point(pt.x-6,pt.y-6),new Scalar(0,255,0), 5);
  	  }

  	  if(infoFlg) {
  		 Platform.runLater(
                 () ->info1.setText(infoText));
  	  }

  	  return infoText;
  	}

    /**
     * 穴面積判定
     * @param judgeAreaMat
     * @param circles
     * @param threshholdAreaMax　白面積の上限
     * @param threshholdAreaMin　白面積の下限
     * @return
     */
    private boolean holeWhiteAreaCheck(Mat judgeAreaMat,Mat circles,int threshholdAreaMax,int threshholdAreaMin) {
      boolean result = true;
	  Mat roi = new Mat(1,1,CvType.CV_8U);
	  int whiteArea = 0;
	  final int offset = 5;

	  whiteAreaAverage = 0;
	  whiteAreaMax = 0;
	  whiteAreaMin = 99999;

	  for( int i= 0; i < circles.cols(); i++) {
		double[] v = circles.get(0, i);//[0]:X  [1]:Y  [2]:r
		int  x = (int)v[0];
		int  y = (int)v[1];
		int  r = (int)v[2];
		if( x-r-offset < 0 || y-r-offset<0 || x-r+r+r>judgeAreaMat.width() || y-r+r+r>judgeAreaMat.height() ) {
			result = false;
		}else {
	  		roi = judgeAreaMat.submat(new Rect( x-r-offset, y-r-offset, r+r+offset, r+r+offset));
	  		whiteArea = Core.countNonZero(roi);
	  		whiteAreaAverage += whiteArea;
	  		whiteAreaMax = whiteAreaMax < whiteArea?whiteArea:whiteAreaMax;
	  		whiteAreaMin = whiteAreaMin > whiteArea?whiteArea:whiteAreaMin;
	  		if( whiteArea > threshholdAreaMax || whiteArea < threshholdAreaMin)
	  			result = false;
		}
	  }
	  if( settingModeFlg ) {
	  	  int wa = whiteArea;
	  	  int ba = (int) (roi.total() - wa);
	  	  Platform.runLater(() ->whiteRatioLabel.setText( String.format("%d", wa)));
	  	  Platform.runLater(() ->blackRatioLabel.setText( String.format("%d", ba)));
	  	  updateImageView(debugImg, Utils.mat2Image(roi));
	  }

	  whiteAreaAverage = whiteAreaAverage / circles.cols();
	  return result;
    }

    /**
     * クリックで面積スピナーの値を一致させる
     * @param event
     */
    @FXML
    void onWhiteAreaLabelClicked(MouseEvent event) {
    	Platform.runLater( () ->whiteRatioMaxSp.setValueFactory(
				new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 99999,
				Integer.valueOf(whiteRatioLabel.getText()),
				5)));
    	Platform.runLater( () ->whiteRatioMinSp.setValueFactory(
				new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 99999,
						Integer.valueOf(whiteRatioLabel.getText()),
				5)));
    }
    /**
     * 穴の検出パラメーターを設定 ①送り穴～②ポケ穴の４ボタン
     * @param e
     */
    @FXML
    void onSetValue(ActionEvent e) {
    	parameter para = pObj.para[pObj.select];
    	Object eObject = e.getSource();

    	if(eObject == okuri1_btn) {
			if( para.setFlg[0] ) {
    			para.setFlg[0] = false;
			}else {
    			setPara(0);
    	    	onSettingModeBtn(null);
    		}
    	}else if(eObject == okuri2_btn) {
    		if(para.setFlg[1] ){
    			para.setFlg[1] = false;
    		}else {
    			setPara(1);
    	    	onSettingModeBtn(null);
    		}
    	}else if(eObject == okuri3_btn) {
    		if(para.setFlg[2]){
    			para.setFlg[2] = false;
    		}else {
    			setPara(2);
    	    	onSettingModeBtn(null);
    		}
    	}else if(eObject == okuri4_btn) {
    		if(para.setFlg[3]){
    			para.setFlg[3] = false;
    		}else {
    			setPara(3);
    	    	onSettingModeBtn(null);
    		}
    	}
    	setBtnPara();

    	eventTrigger = true;

    }


    private void setPara(int i) {
    	parameter para = pObj.para[pObj.select];
    	para.circlePara4[i] = sliderDetecPara4.getValue();
    	para.circlePara5[i] = sliderDetecPara5.getValue();
    	para.circlePara6[i] = sliderDetecPara6.getValue();
    	para.circlePara7[i] = sliderDetecPara7.getValue();
    	para.circlePara8[i] = sliderDetecPara8.getValue();
    	para.circlePara9[i] = sliderDetecPara9.getValue();

    	para.rects[i] = (Rectangle)draggingRect.clone();

    	para.setFlg[i] = true;
    	para.gauusianCheck[i] = gauusianCheck.isSelected();
    	para.gauusianSliderX[i] = gauusianSliderX.getValue();
    	para.gauusianSliderY[i] = gauusianSliderY.getValue();
    	para.gauusianSliderA[i] = gauusianSliderA.getValue();
    	para.dilateCheck[i] = dilateCheck.isSelected();
    	para.dilateSliderN[i] = (int)dilateSliderN.getValue();
    	//para.zoom = this.zoomValue_slider.getValue();
    	para.threshholdCheck[i] = threshholdCheck.isSelected();
    	para.threshhold[i] = threshholdSlider.getValue();
    	para.threshhold_Invers[i] = threshhold_Inverse.isSelected();
    	para.whiteAreaMax[i] = whiteRatioMaxSp.getValue().intValue();
    	para.whiteAreaMin[i] = whiteRatioMinSp.getValue().intValue();

    	settingMode.setSelected(false);//セッティングモードから抜ける
    	draggingRect = new Rectangle(0,0,1,1);
    }
    private void setBtnPara() {
    	parameter para = pObj.para[pObj.select];
		if( !para.setFlg[0] ) {
			Platform.runLater(() ->okuri1_btn.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null))));
			Platform.runLater(() ->okuri1_btn.setTextFill( Color.BLACK));
			Platform.runLater(() ->okuri1_label.setText("未設定"));
			Platform.runLater(() ->okuri1_n.setText("-"));
		}else {
			Platform.runLater(() ->okuri1_btn.setBackground(new Background(new BackgroundFill(Color.GREEN, null, null))));
			Platform.runLater(() ->okuri1_btn.setTextFill( Color.WHITE));
			Platform.runLater(() ->okuri1_label.setText("設定済"));
		}
		if(!para.setFlg[1] ){
			Platform.runLater(() ->okuri2_btn.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null))));
			Platform.runLater(() ->okuri2_btn.setTextFill( Color.BLACK));
			Platform.runLater(() ->okuri2_label.setText("未設定"));
			Platform.runLater(() ->okuri2_n.setText("-"));
		}else {
			Platform.runLater(() ->okuri2_btn.setBackground(new Background(new BackgroundFill(Color.GREEN, null, null))));
			Platform.runLater(() ->okuri2_btn.setTextFill( Color.WHITE));
			Platform.runLater(() ->okuri2_label.setText("設定済"));
		}
		if(!para.setFlg[2]){
			Platform.runLater(() ->okuri3_btn.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null))));
			Platform.runLater(() ->okuri3_btn.setTextFill( Color.BLACK));
			Platform.runLater(() ->okuri3_label.setText("未設定"));
			Platform.runLater(() ->okuri3_n.setText("-"));
		}else {
			Platform.runLater(() ->okuri3_btn.setBackground(new Background(new BackgroundFill(Color.GREEN, null, null))));
			Platform.runLater(() ->okuri3_btn.setTextFill( Color.WHITE));
			Platform.runLater(() ->okuri3_label.setText("設定済"));
		}
		if(!para.setFlg[3]){
			Platform.runLater(() ->okuri4_btn.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null))));
			Platform.runLater(() ->okuri4_btn.setTextFill( Color.BLACK));
			Platform.runLater(() ->okuri4_label.setText("未設定"));
			Platform.runLater(() ->okuri4_n.setText("-"));
		}else {
			Platform.runLater(() ->okuri4_btn.setBackground(new Background(new BackgroundFill(Color.GREEN, null, null))));
			Platform.runLater(() ->okuri4_btn.setTextFill( Color.WHITE));
			Platform.runLater(() ->okuri4_label.setText("設定済"));

		}

		para.rects[4] = (Rectangle)draggingRect.clone();
		para.viewRect[0] = imgORG.getViewport().getMinX();
		para.viewRect[1] = imgORG.getViewport().getMinY();
		para.viewRect[2] = imgORG.getViewport().getWidth();
		para.viewRect[3] = imgORG.getViewport().getHeight();
		para.circlePara4[4] = sliderDetecPara4.getValue();
		para.circlePara5[4] = sliderDetecPara5.getValue();
		para.circlePara6[4] = sliderDetecPara6.getValue();
		para.circlePara7[4] = sliderDetecPara7.getValue();
		para.circlePara8[4] = sliderDetecPara8.getValue();
		para.circlePara9[4] = sliderDetecPara9.getValue();
		para.gauusianCheck[4] = gauusianCheck.isSelected();
		para.gauusianSliderX[4] = gauusianSliderX.getValue();
		para.gauusianSliderY[4] = gauusianSliderY.getValue();
		para.gauusianSliderA[4] = gauusianSliderA.getValue();
		para.dilateCheck[4] = dilateCheck.isSelected();
		para.dilateSliderN[4] = (int)dilateSliderN.getValue();
		para.zoom = zoomValue_slider.getValue();
		para.threshholdCheck[4] = threshholdCheck.isSelected();
		para.threshhold[4] = threshholdSlider.getValue();
		para.threshhold_Invers[4] = threshhold_Inverse.isSelected();
		para.whiteAreaMax[4] = whiteRatioMaxSp.getValue();
		para.whiteAreaMin[4] = whiteRatioMinSp.getValue();


		setSlidbar();
    }
    private void setSlidbar() {
    	parameter para = pObj.para[pObj.select];
		Platform.runLater(() ->sliderDetecPara4.setValue(para.circlePara4[4]));
		Platform.runLater(() ->sliderDetecPara5.setValue(para.circlePara5[4]));
		Platform.runLater(() ->sliderDetecPara6.setValue(para.circlePara6[4]));
		Platform.runLater(() ->sliderDetecPara7.setValue(para.circlePara7[4]));
		Platform.runLater(() ->sliderDetecPara8.setValue(para.circlePara8[4]));
		Platform.runLater(() ->sliderDetecPara9.setValue(para.circlePara9[4]));
		Platform.runLater(() ->gauusianCheck.setSelected(para.gauusianCheck[4]));
		Platform.runLater(() ->gauusianSliderX.setValue(para.gauusianSliderX[4]));
		Platform.runLater(() ->gauusianSliderY.setValue(para.gauusianSliderY[4]));
		Platform.runLater(() ->gauusianSliderA.setValue(para.gauusianSliderA[4]));
		Platform.runLater(() ->dilateCheck.setSelected(para.dilateCheck[4]));
		Platform.runLater(() ->dilateSliderN.setValue(para.dilateSliderN[4]));
		Platform.runLater(() ->zoomValue_slider.setValue(para.zoom));
		Platform.runLater(() ->zoomLabel.setText(String.format("%.1f",para.zoom)));
		Platform.runLater(() ->threshholdCheck.setSelected(para.threshholdCheck[4]));
		Platform.runLater(() ->threshholdSlider.setValue(para.threshhold[4]));
    	Platform.runLater(() ->threshholdLabel.setText(String.format("%.1f",threshholdSlider.getValue())));
    	Platform.runLater(() ->threshhold_Inverse.setSelected(para.threshhold_Invers[4]));
    	Platform.runLater(() ->textFieldDetecPara4.setText(String.valueOf(String.format("%.1f",sliderDetecPara4.getValue()))));
    	Platform.runLater(() ->textFieldDetecPara5.setText(String.valueOf(String.format("%.1f",sliderDetecPara5.getValue()))));
    	Platform.runLater(() ->textFieldDetecPara6.setText(String.valueOf(String.format("%.1f",sliderDetecPara6.getValue()))));
    	Platform.runLater(() ->textFieldDetecPara7.setText(String.valueOf(String.format("%.1f",sliderDetecPara7.getValue()))));
    	Platform.runLater(() ->textFieldDetecPara8.setText(String.valueOf(String.format("%.1f",sliderDetecPara8.getValue()))));
    	Platform.runLater(() ->textFieldDetecPara9.setText(String.valueOf(String.format("%.1f",sliderDetecPara9.getValue()))));
    	//Platform.runLater(() ->matchTmempTHreshSlider.setValue(para.matchThreshValue[4]));
    	Platform.runLater(() ->threshholdLabel.setText(String.format("%.1f",threshholdSlider.getValue())));

    }

    @FXML
    void onSetVeri(ActionEvent e) {
    	parameter para = pObj.para[pObj.select];
    	Object obj = e.getSource();
    	onSetVeri_n=0;
    	if( obj == setVeriBtn1 && para.setFlg[0]) {
    		onSetVeri_n=0;
    	}else if( obj == setVeriBtn2 && para.setFlg[1]) {
    		onSetVeri_n=1;
	    }else if( obj == setVeriBtn3 && para.setFlg[2]) {
			onSetVeri_n=2;
		}else if( obj == setVeriBtn4 && para.setFlg[3]) {
			onSetVeri_n=3;
		}else {
			return;
		}

		Platform.runLater(() ->sliderDetecPara4.setValue(para.circlePara4[onSetVeri_n]));
		Platform.runLater(() ->sliderDetecPara5.setValue(para.circlePara5[onSetVeri_n]));
		Platform.runLater(() ->sliderDetecPara6.setValue(para.circlePara6[onSetVeri_n]));
		Platform.runLater(() ->sliderDetecPara7.setValue(para.circlePara7[onSetVeri_n]));
		Platform.runLater(() ->sliderDetecPara8.setValue(para.circlePara8[onSetVeri_n]));
		Platform.runLater(() ->sliderDetecPara9.setValue(para.circlePara9[onSetVeri_n]));
		Platform.runLater(() ->gauusianCheck.setSelected(para.gauusianCheck[onSetVeri_n]));
		Platform.runLater(() ->gauusianSliderX.setValue(para.gauusianSliderX[onSetVeri_n]));
		Platform.runLater(() ->gauusianSliderY.setValue(para.gauusianSliderY[onSetVeri_n]));
		Platform.runLater(() ->gauusianSliderA.setValue(para.gauusianSliderA[onSetVeri_n]));
		Platform.runLater(() ->dilateCheck.setSelected(para.dilateCheck[onSetVeri_n]));
		Platform.runLater(() ->dilateSliderN.setValue(para.dilateSliderN[onSetVeri_n]));
		Platform.runLater(() ->threshholdCheck.setSelected(para.threshholdCheck[onSetVeri_n]));
		Platform.runLater(() ->threshholdSlider.setValue(para.threshhold[onSetVeri_n]));
    	Platform.runLater(() ->threshholdLabel.setText(String.format("%.1f",threshholdSlider.getValue())));
    	Platform.runLater(() ->threshhold_Inverse.setSelected(para.threshhold_Invers[onSetVeri_n]));
    	Platform.runLater(() ->textFieldDetecPara4.setText(String.valueOf(String.format("%.1f",sliderDetecPara4.getValue()))));
    	Platform.runLater(() ->textFieldDetecPara5.setText(String.valueOf(String.format("%.1f",sliderDetecPara5.getValue()))));
    	Platform.runLater(() ->textFieldDetecPara6.setText(String.valueOf(String.format("%.1f",sliderDetecPara6.getValue()))));
    	Platform.runLater(() ->textFieldDetecPara7.setText(String.valueOf(String.format("%.1f",sliderDetecPara7.getValue()))));
    	Platform.runLater(() ->textFieldDetecPara8.setText(String.valueOf(String.format("%.1f",sliderDetecPara8.getValue()))));
    	Platform.runLater(() ->textFieldDetecPara9.setText(String.valueOf(String.format("%.1f",sliderDetecPara9.getValue()))));
    	//Platform.runLater(() ->matchTmempTHreshSlider.setValue(para.matchThreshValue[4]));
    	Platform.runLater(() ->threshholdLabel.setText(String.format("%.1f",threshholdSlider.getValue())));
		whiteRatioMaxSp.setValueFactory(
				new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 99999,para.whiteAreaMax[onSetVeri_n],5));
		whiteRatioMinSp.setValueFactory(
				new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 99999,para.whiteAreaMin[onSetVeri_n],5));


    	eventTrigger = true;
    }

    @FXML
    void onTrigger(ActionEvent e) {
    	Object obj = e.getSource();
    	if( obj == triggerBtn ) {
	    	Platform.runLater( () ->info2.appendText("ManualTrigger"+"\n"));
	    	manualTrigger = true;
    	}else if( obj == aTriggerBtn) {
	    	if( autoTrigger) {
	    		autoTrigger = false;
	    	}else {
	    		Platform.runLater( () ->info2.appendText("AutoTrigger"+"\n"));
		    	autoTrigger = true;
	    	}
    	}

    }
    @FXML
    void onKeypressed(KeyEvent e) {
    	//Platform.runLater( () ->info2.appendText("KeyPressTrigger"+"\n"));
    	//manualTrigger = true;
    }
    /**
     * シリアライズ
     * @throws IOException
     */
    public void saveAllPara() throws IOException{
		FileOutputStream fo = new FileOutputStream("./conf5.txt");
		ObjectOutputStream objOut = new ObjectOutputStream(fo);

    	parameter para = pObj.para[pObj.select];
		para.rects[4] =  (Rectangle)draggingRect.clone();
		para.viewRect[0] = imgORG.getViewport().getMinX();
		para.viewRect[1] = imgORG.getViewport().getMinY();
		para.viewRect[2] = imgORG.getViewport().getWidth();
		para.viewRect[3] = imgORG.getViewport().getHeight();
		para.circlePara4[4] = sliderDetecPara4.getValue();
		para.circlePara5[4] = sliderDetecPara5.getValue();
		para.circlePara6[4] = sliderDetecPara6.getValue();
		para.circlePara7[4] = sliderDetecPara7.getValue();
		para.circlePara8[4] = sliderDetecPara8.getValue();
		para.circlePara9[4] = sliderDetecPara9.getValue();
		para.gauusianCheck[4] = gauusianCheck.isSelected();
		para.gauusianSliderX[4] = gauusianSliderX.getValue();
		para.gauusianSliderY[4] = gauusianSliderY.getValue();
		para.gauusianSliderA[4] = gauusianSliderA.getValue();
		para.dilateCheck[4] = dilateCheck.isSelected();
		para.dilateSliderN[4] = (int)dilateSliderN.getValue();
		para.zoom = zoomValue_slider.getValue();
		para.threshholdCheck[4] = threshholdCheck.isSelected();
		para.threshhold[4] = threshholdSlider.getValue();
		para.threshhold_Invers[4] = threshhold_Inverse.isSelected();

		para.ptmEnable[0] = ptm_pt1_enable.isSelected();
		para.ptmEnable[1] = ptm_pt2_enable.isSelected();
		para.ptmEnable[2] = ptm_pt3_enable.isSelected();
		para.ptmEnable[3] = ptm_pt4_enable.isSelected();

		pObj.portNo = portNoSpin.getValue().intValue();
		pObj.delly = dellySpinner.getValue().intValue();
		pObj.cameraID = camIDspinner.getValue().intValue();
		pObj.adc_thresh = Integer.valueOf(adc_thresh_value.getText());
		pObj.cameraHeight = Integer.valueOf(capH_text.getText());
		pObj.cameraWidth = Integer.valueOf(capW_text.getText());
		pObj.adcFlg = adc_flg.isSelected();


		objOut.writeObject(pObj);
		objOut.flush();
		objOut.close();

		//パターンマッチング画像の保存 ptmImgMat[preSetNo][ptm1～ptm4]
		for(int i=0;i<4;i++) {
			for(int j=0;j<4;j++) {
				if( ptmImgMat[i][j] != null ) {
					savePtmImg(ptmImgMat[i][j],"ptm"+String.format("_%d_%d", i,j));
				}
			}
		}

		Platform.runLater( () ->info2.appendText("設定が保存されました。\n"));
    }
    /**
     * パターンマッチング画像の保存
     * @param imgMat
     * @param fileName
     */
    public void savePtmImg(Mat imgMat,String fileName) {

    	File folder = new File("./ptm_image");
    	if( !folder.exists()) {
    		if( !folder.mkdir() ) {
    			Platform.runLater( () ->info2.appendText("ptm_imageフォルダの作成に失敗"+"\n"));
    			return;
    		}
    	}
        try {
        	Imgcodecs.imwrite(folder+"/" + fileName + ".jpeg", imgMat);
        	Platform.runLater( () ->info2.appendText(folder+"/"+ fileName +".jpeg"+"PTM画像保存"+"\n"));
        }catch(Exception e) {
        	Platform.runLater( () ->info2.appendText("PTM画像の保存に失敗"+e.toString()+"\n"));
        }
        Platform.runLater( () ->info2.appendText("PTM画像ファイルを保存\n" + fileName +".jpeg\n"));

    }

    /**
     * デシリアライズ
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void loadAllPara(){
    	try {
	    	FileInputStream fi = new FileInputStream("./conf5.txt");
	    	ObjectInputStream objIn = new ObjectInputStream(fi);

	    	pObj = (preSet)objIn.readObject();
	    	objIn.close();
    	}catch(Exception e) {
    		System.out.println(e);
    		pObj = new preSet();
    	}

    	parameter para = pObj.para[pObj.select];
    	draggingRect = (Rectangle)para.rects[4].clone();

    	viewOrgZoom = para.zoom;
    	zoomValue_slider.setValue(viewOrgZoom);

    	imgORG.setViewport(new Rectangle2D(
    			para.viewRect[0],para.viewRect[1],para.viewRect[2],para.viewRect[3]));
    	imgGLAY.setViewport(new Rectangle2D(
    			para.viewRect[0],para.viewRect[1],para.viewRect[2],para.viewRect[3]));
    	imgGLAY1.setViewport(new Rectangle2D(
    			para.viewRect[0],para.viewRect[1],para.viewRect[2],para.viewRect[3]));
    	imgGLAY2.setViewport(new Rectangle2D(
    			para.viewRect[0],para.viewRect[1],para.viewRect[2],para.viewRect[3]));
    	imgGLAY3.setViewport(new Rectangle2D(
    			para.viewRect[0],para.viewRect[1],para.viewRect[2],para.viewRect[3]));
		whiteRatioMaxSp.setValueFactory(
				new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 99999,para.whiteAreaMax[4],5));
		whiteRatioMinSp.setValueFactory(
				new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 99999,para.whiteAreaMin[4],5));
    	setBtnPara();

    	portNoSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9,pObj.portNo,1));
    	dellySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 900,pObj.delly,5));
    	camIDspinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9,pObj.cameraID,1));
    	adc_thresh_value.setText( String.valueOf(pObj.adc_thresh));
    	capH_text.setText( String.valueOf(pObj.cameraHeight));
    	capW_text.setText( String.valueOf(pObj.cameraWidth));
    	adc_flg.setSelected(pObj.adcFlg);

    	//パターンマッチング部
    	loadPtmImg();
    	updateImageView(ptm_img1, Utils.mat2Image(ptmImgMat[pObj.select][0]));
    	updateImageView(ptm_img2, Utils.mat2Image(ptmImgMat[pObj.select][1]));
    	updateImageView(ptm_img3, Utils.mat2Image(ptmImgMat[pObj.select][2]));
    	updateImageView(ptm_img4, Utils.mat2Image(ptmImgMat[pObj.select][3]));
    	ptm_pt1_enable.setSelected(para.ptmEnable[0]);
    	ptm_pt2_enable.setSelected(para.ptmEnable[1]);
    	ptm_pt3_enable.setSelected(para.ptmEnable[2]);
    	ptm_pt4_enable.setSelected(para.ptmEnable[3]);
        //パターンマッチング用パラメータ設定
    	patternMatchParaSet();

		Platform.runLater( () ->info2.appendText("設定がロードされました。\n"));

    }

    private void patternMatchParaSet() {
    	parameter para = pObj.para[pObj.select];
        for(int i=0;i<tmpara.arrayCnt;i++) {
        	tmpara.matchCnt[i] = para.matchCnt[i];
        	tmpara.thresh[i] = para.matchThreshValue[i];
        	tmpara.paternMat[i] = ptmImgMat[pObj.select][i];
        	tmpara.ptmEnable[i] = para.ptmEnable[i];
        	tmpara.detectionRects[i] = para.para_rectsDetection[i];
        	tmpara.scale[i] = para.detectionScale[i];
        }
        tm = new templateMatching(tmpara);
    }

    /**
     * パターンマッチング画像のロード
     */
    private void loadPtmImg() {
    	for( int i=0;i<4;i++) {
    		for( int j=0;j<4;j++) {
    	    	Mat tmpMat = Imgcodecs.imread("./ptm_image/ptm"+String.format("_%d_%d", i,j)+".jpeg");
    	    	if( tmpMat.width() > 0 ) {
    	    		 ptmImgMat[i][j] = new Mat();
    	    		 ptmImgMat[i][j] = tmpMat.clone();

    	    	}else {
    	    		ptmImgMat[i][j] = new Mat(100,100,CvType.CV_8UC3);
    	    	}

    		}
    	}
    }

    @FXML
    void onPreset(ActionEvent e) {//プリセット選択
    	Object obj = e.getSource();

    	Platform.runLater(() ->preset1.setTextFill( Color.BLACK ));
    	Platform.runLater(() ->preset2.setTextFill( Color.BLACK ));
    	Platform.runLater(() ->preset3.setTextFill( Color.BLACK ));
    	Platform.runLater(() ->preset4.setTextFill( Color.BLACK ));

    	if(obj == preset1) {
    		pObj.select = 0;
    		Platform.runLater(() ->preset1.setTextFill( Color.BLUE ));
    	}else if(obj == preset2){
    		pObj.select = 1;
    		Platform.runLater(() ->preset2.setTextFill( Color.BLUE ));
    	}else if(obj == preset3){
    		pObj.select = 2;
    		Platform.runLater(() ->preset3.setTextFill( Color.BLUE ));
    	}else if(obj == preset4){
    		pObj.select = 3;
    		Platform.runLater(() ->preset4.setTextFill( Color.BLUE ));
    	}
    	draggingRect = (Rectangle)pObj.para[pObj.select].rects[4].clone();

    	setBtnPara();
    }
    @FXML
    void onCheckBtn(ActionEvent event) {
    	eventTrigger = true;
    }
    @FXML
    void onNgImageBtn(ActionEvent event) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("NgImageViewer.fxml"));
		AnchorPane root = null;
		try {
			root = (AnchorPane) loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Scene scene = new Scene(root);
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.setResizable(false);
		stage.showAndWait();
		if( saveImgUseFlg && !this.settingModeFlg) {
			onSettingModeBtn(null);
		}
		eventTrigger = true;
   	}
    @FXML
    void onOKImageBtn(ActionEvent event) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("OKImageViewer.fxml"));
		AnchorPane root = null;
		try {
			root = (AnchorPane) loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Scene scene = new Scene(root);
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.setResizable(false);
		stage.showAndWait();
		if( saveImgUseFlg && !this.settingModeFlg) {
			onSettingModeBtn(null);
		}
		eventTrigger = true;
    }
	@FXML
    void onAllClear(ActionEvent event) {
    	ngCnt = 0;
    	allSaveCnt = 0;
    	Platform.runLater(() ->ngCounterLabel.setText(String.valueOf(ngCnt)));
    	Platform.runLater(() ->aPane.setStyle("-fx-background-radius: 0;-fx-background-color: #a5abb094;"));

    	Platform.runLater( () ->FileClass.fileClass(new File("./ng_image/")) );
    	Platform.runLater( () ->FileClass.fileClass(new File("./ok_image/")) );

    	Platform.runLater(() ->this.imgNG.setImage(null));

    	Platform.runLater(() ->info2.clear());
    	Platform.runLater(() ->info2.setText(initInfo2));
    	Platform.runLater(() ->info2.appendText("NG/OK画像ファイルを全て削除しました。\n"));

		while( Gpio.useFlg ) {
			System.out.println("onAllClear() Gpio.useFlg=true");
		}
		Gpio.OkSignalON();
		if( !Gpio.openFlg ) {
			Platform.runLater( () ->info2.appendText("\n---------------\n- GPIO異常 -\n----------------\n"));
			Platform.runLater( () ->GPIO_STATUS_PIN3.setFill(Color.RED));
		}else {
			Platform.runLater( () ->GPIO_STATUS_PIN3.setFill(Color.BLUE));
		}

    }

    @FXML
    void onSettingModeBtn(ActionEvent event) {
    	if( settingModeFlg ) {

    		Platform.runLater(() ->this.accordion_1.setDisable(true));
        	settingModeFlg = false;
        	Platform.runLater(() ->settingMode.setSelected(false));
        	Platform.runLater(() ->info1.setText(""));
        	draggingRect = new Rectangle(1,1,1,1);
        	saveImgUseFlg = false;
        	updateImageView(imgGLAY, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
    	}else {

    		Platform.runLater(() ->this.accordion_1.setDisable(false));
        	settingModeFlg = true;
        	lockedTimer = System.currentTimeMillis();
        	Platform.runLater(() ->settingMode.setSelected(true));
    	}
    }
    @FXML
    void onOutTrigDisableChk(ActionEvent event) {
    	if( this.outTrigDisableChk.isSelected())
    		Platform.runLater(() ->aPane.setStyle("-fx-background-radius: 0;-fx-background-color: #a5abb094;"));
    }
    @FXML
    void onEventTrigger(ActionEvent event) {
    	eventTrigger = true;
    }

    @FXML
    void onGPIOAllRead(ActionEvent event) {
    	String rt = Gpio.readAll();
    	Platform.runLater(() ->this.info2.appendText("GPIO Read ALL::" + rt));
    }

    @FXML
    /**
     * 撮影ボタンによる撮影 ./shot_imageに入り当ソフトからは消せない
     * @param event
     */
    void onShotImg(ActionEvent event) {
    	SaveshotImg(srcMat, "shot");
    }

    @FXML
    void onShotImg_chess(ActionEvent event) {
    	SavechessImg(srcMat, "chessbord");
    }
    @FXML
    void onCalbdataDel(ActionEvent event) {
    	if( !cameraCalibFlg ) {
    		Platform.runLater(() ->info2.appendText("キャリブレーションデーターがありません"));
    		return;
    	}
		File calibFile = new File("./CameraCalibration.xml");
		if( calibFile.exists() ) {
			calibFile.delete();
		}
    	Platform.runLater(() ->info2.appendText("キャリブレーションデーター削除\n"));
    }

    /**
     * カメラキャリブレーション実行
     * @param event
     */
    @FXML
    void onCameraCalib(ActionEvent event) {
    	cameraCalibration  cb = new cameraCalibration();
    	if( cb.processer() ) {
			File calibFile = new File("./CameraCalibration.xml");
			if( calibFile.exists() ) {
				cameraCalibFlg = true;
				final Path filePath = Paths.get("./CameraCalibration.xml");
				final Map<String, Mat> calibrationMats = MatIO.loadMat(filePath);
				cameraMatrix = calibrationMats.get("CameraMatrix");//内部パラメータ
				distortionCoefficients = calibrationMats.get("DistortionCoefficients");//歪み係数
			}
			cameraCalibFlg = true;
        	Platform.runLater(() ->info2.appendText("キャリブレーションを実行しました。\n"));

    	}else {
    		Platform.runLater(() ->info2.appendText("キャリブレーションに失敗しました。\n"));
    	}

    	calibTestController.cameraMatrix = this.cameraMatrix;
    	calibTestController.distortionCoefficients = this.distortionCoefficients;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("calibTest.fxml"));
		AnchorPane root = null;
		try {
			root = (AnchorPane) loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Scene scene = new Scene(root);
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.setResizable(false);
		stage.showAndWait();

    }

    /**
     * パターンマッチングのアコーディオンを開いた時に発生するイベント
     * @param event
     */
    @FXML
    void onOpenPtmAccordion(MouseEvent event) {
    	ptmSetStartFlg = true;
    	System.out.println("open_ptm");

    }

    /**
     * パターンマッチングのパラメーター設定
     * @param event
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    @FXML
    void onPtmSetPara(ActionEvent e){
    	Button obj = (Button)e.getSource();
    	ImageView iv;
    	int selectBtn;
    	if( obj == ptm_set_para1 ) {
    		selectBtn = 0;
    		iv = ptm_img1;
    	}else if( obj == ptm_set_para2 ) {
    		selectBtn = 1;
    		iv = ptm_img2;
    	}else if( obj == ptm_set_para3 ) {
    		selectBtn = 2;
    		iv = ptm_img3;
    	}else if( obj == ptm_set_para4 ) {
    		selectBtn = 3;
    		iv = ptm_img4;
    	}else {
    		return;
    	}

    	parameter para = pObj.para[pObj.select];

		//パラメーターを渡す
		PtmView.ptmSrcMat = srcMat.clone();

		PtmView.arg_ptmMat = ptmImgMat[pObj.select][selectBtn].clone();
		PtmView.arg_detectionCnt = para.detectionCnt[selectBtn];

		PtmView.arg_gauusianCheck = para.para_gauusianCheck[selectBtn];
		PtmView.arg_gauusianSliderX = para.para_gauusianSliderX[selectBtn];
		PtmView.arg_gauusianSliderY = para.para_gauusianSliderY[selectBtn];
		PtmView.arg_gauusianSliderA = para.para_gauusianSliderA[selectBtn];

		PtmView.arg_dilateCheck = para.para_dilateCheck[selectBtn];
		PtmView.arg_dilateSliderN = para.para_dilateSliderN[selectBtn];

		PtmView.arg_erodeCheck = para.para_erodeCheck[selectBtn];
		PtmView.arg_erodeSliderN = para.para_erodeSliderN[selectBtn];

		PtmView.arg_threshholdCheck = para.threshholdCheck[selectBtn];
		PtmView.arg_threshhold_Inverse = para.threshhold_Invers[selectBtn];
		PtmView.arg_threshholdSlider = para.para_threshholdSlider[selectBtn];//2値化閾値

		PtmView.arg_cannyCheck = para.para_cannyCheck[selectBtn];
		PtmView.arg_cannyThresh1 = para.para_cannyThresh1[selectBtn];
		PtmView.arg_cannyThresh2 = para.para_cannyThresh2[selectBtn];

		PtmView.arg_ptmThreshSliderN = para.para_ptmThreshSliderN[selectBtn];//閾値
		PtmView.arg_zoomValue_slider = para.para_zoomValue_slider[selectBtn];
		PtmView.arg_rectsDetection =  para.para_rectsDetection[selectBtn];//検出範囲

		PtmView.arg_detectionScale = para.detectionScale[selectBtn];//検出倍率の逆数

		FXMLLoader loader = new FXMLLoader(getClass().getResource("ptmView.fxml"));
		AnchorPane root = null;
		try {
			root = (AnchorPane) loader.load();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		Scene scene = new Scene(root);
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.setResizable(false);

		//設定ウィンドウを開く
		stage.showAndWait();

		if( PtmView.confimFlg ) {
			ptmImgMat[pObj.select][selectBtn] = PtmView.arg_ptmMat;
			updateImageView(iv, Utils.mat2Image(ptmImgMat[pObj.select][selectBtn]));

			para.detectionCnt[selectBtn] = PtmView.arg_detectionCnt;

			para.para_gauusianCheck[selectBtn] = PtmView.arg_gauusianCheck;
			para.para_gauusianSliderX[selectBtn] = PtmView.arg_gauusianSliderX;
			para.para_gauusianSliderY[selectBtn]  = PtmView.arg_gauusianSliderY;
			para.para_gauusianSliderA[selectBtn] = PtmView.arg_gauusianSliderA;

			para.para_dilateCheck[selectBtn] = PtmView.arg_dilateCheck;
			para.para_dilateSliderN[selectBtn] = PtmView.arg_dilateSliderN;

			para.para_erodeCheck[selectBtn] = PtmView.arg_erodeCheck;
			para.para_erodeSliderN[selectBtn] = PtmView.arg_erodeSliderN;

			para.threshholdCheck[selectBtn] = PtmView.arg_threshholdCheck;
			para.threshhold_Invers[selectBtn] = PtmView.arg_threshhold_Inverse;
			para.para_threshholdSlider[selectBtn] = PtmView.arg_threshholdSlider;//2値化閾値

			para.para_cannyCheck[selectBtn] = PtmView.arg_cannyCheck;
			para.para_cannyThresh1[selectBtn] = PtmView.arg_cannyThresh1;
			para.para_cannyThresh2[selectBtn] = PtmView.arg_cannyThresh2;

			para.para_ptmThreshSliderN[selectBtn] = PtmView.arg_ptmThreshSliderN;//閾値
			para.para_zoomValue_slider[selectBtn] = PtmView.arg_zoomValue_slider;
			para.para_rectsDetection[selectBtn] =  PtmView.arg_rectsDetection;//検出範囲
			para.detectionScale[selectBtn] = PtmView.arg_detectionScale;//検出倍率の逆数
		}
        //パターンマッチング用パラメータ設定
    	patternMatchParaSet();

    }

    /**
     * 画像パターンの登録
     * @param event
     */
    @FXML
    void onPtmSetImage(ActionEvent e) {
    	if( saveImgUseFlg ) {
    		Platform.runLater(() ->info2.appendText("保存画像を使用してパターンの登録はできません\n"));
    		return;
    	}
    	if( srcMat.width() < 10 ) {
    		Platform.runLater(() ->info2.appendText("登録パターンが小さすぎます\n"));
    		return;
    	}
    	Mat roi = srcMat.submat(new Rect(draggingRect.x,draggingRect.y,draggingRect.width,draggingRect.height)).clone();

    	Object eObject = e.getSource();
    	if( eObject == this.ptm_set_pt1 ) {
    		updateImageView(ptm_img1, Utils.mat2Image(roi));
    		ptmImgMat[pObj.select][0] = roi;
    	}else if( eObject == this.ptm_set_pt2 ) {
    		updateImageView(ptm_img2, Utils.mat2Image(roi));
    		ptmImgMat[pObj.select][1] = roi;
    	}else if( eObject == this.ptm_set_pt3 ) {
    		updateImageView(ptm_img3, Utils.mat2Image(roi));
    		ptmImgMat[pObj.select][2] = roi;
    	}else if( eObject == this.ptm_set_pt4 ) {
    		updateImageView(ptm_img4, Utils.mat2Image(roi));
    		ptmImgMat[pObj.select][3] = roi;
    	}
        //パターンマッチング用パラメータ設定
    	patternMatchParaSet();

    }

    @FXML
    void onPtmEnabeChk(MouseEvent event) {
    	tm.tmpara.ptmEnable[0] = this.ptm_pt1_enable.isSelected();
    	tm.tmpara.ptmEnable[1] = this.ptm_pt2_enable.isSelected();
    	tm.tmpara.ptmEnable[2] = this.ptm_pt3_enable.isSelected();
    	tm.tmpara.ptmEnable[3] = this.ptm_pt4_enable.isSelected();
    }

    @FXML
    void initialize() {
        assert info1 != null : "fx:id=\"info1\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert getInfoBtn != null : "fx:id=\"getInfoBtn\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert imgORG != null : "fx:id=\"imgORG\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert imgGLAY != null : "fx:id=\"imgGLAY\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert sliderDetecPara4 != null : "fx:id=\"sliderDetecPara4\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert sliderDetecPara5 != null : "fx:id=\"sliderDetecPara5\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert sliderDetecPara6 != null : "fx:id=\"sliderDetecPara6\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert sliderDetecPara7 != null : "fx:id=\"sliderDetecPara7\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert sliderDetecPara8 != null : "fx:id=\"sliderDetecPara8\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert sliderDetecPara9 != null : "fx:id=\"sliderDetecPara9\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert textFieldDetecPara4 != null : "fx:id=\"textFieldDetecPara4\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert textFieldDetecPara5 != null : "fx:id=\"textFieldDetecPara5\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert textFieldDetecPara6 != null : "fx:id=\"textFieldDetecPara6\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert textFieldDetecPara7 != null : "fx:id=\"textFieldDetecPara7\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert textFieldDetecPara8 != null : "fx:id=\"textFieldDetecPara8\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert textFieldDetecPara9 != null : "fx:id=\"textFieldDetecPara9\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert move_up_btn != null : "fx:id=\"move_up_btn\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert move_left_btn != null : "fx:id=\"move_left_btn\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert move_right_btn != null : "fx:id=\"move_right_btn\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert move_down_btn != null : "fx:id=\"move_down_btn\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert zoomValue_slider != null : "fx:id=\"zoomValue_slider\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert move_speed_slider != null : "fx:id=\"move_speed_slider\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert testBtn != null : "fx:id=\"testBtn\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert okuri1_btn != null : "fx:id=\"okuri1_btn\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert okuri2_btn != null : "fx:id=\"okuri2_btn\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert okuri3_btn != null : "fx:id=\"okuri3_btn\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert okuri4_btn != null : "fx:id=\"okuri4_btn\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert okuri1_label != null : "fx:id=\"okuri1_label\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert okuri2_label != null : "fx:id=\"okuri2_label\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert okuri3_label != null : "fx:id=\"okuri3_label\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert okuri4_label != null : "fx:id=\"okuri4_label\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert okuri1_judg != null : "fx:id=\"okuri1_judg\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert okuri2_judg != null : "fx:id=\"okuri2_judg\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert okuri3_judg != null : "fx:id=\"okuri3_judg\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert okuri4_judg != null : "fx:id=\"okuri4_judg\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert judg != null : "fx:id=\"judg\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert preset1 != null : "fx:id=\"preset1\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert preset2 != null : "fx:id=\"preset2\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert preset3 != null : "fx:id=\"preset3\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert preset4 != null : "fx:id=\"preset4\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert stopTest != null : "fx:id=\"stopTest\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert trigBtn != null : "fx:id=\"trigBtn\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert saveBtn != null : "fx:id=\"saveBtn\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert loadBtn != null : "fx:id=\"loadBtn\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert okuri1_n != null : "fx:id=\"okuri1_n\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert okuri2_n != null : "fx:id=\"okuri2_n\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert okuri3_n != null : "fx:id=\"okuri3_n\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert okuri4_n != null : "fx:id=\"okuri4_n\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert gauusianCheck != null : "fx:id=\"gauusianCheck\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert dilateCheck != null : "fx:id=\"dilateCheck\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert gauusianSliderX != null : "fx:id=\"gauusianSliderX\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert dilateSliderN != null : "fx:id=\"dilateSliderN\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert gauusianSliderY != null : "fx:id=\"gauusianSliderY\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert gauusianSliderA != null : "fx:id=\"gauusianSliderA\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert threshholdCheck != null : "fx:id=\"threshholdCheck\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert threshholdSlider != null : "fx:id=\"threshholdSlider\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert threshholdLabel != null : "fx:id=\"threshholdLabel\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert setVeriBtn1 != null : "fx:id=\"setVeriBtn1\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert setVeriBtn2 != null : "fx:id=\"setVeriBtn2\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert setVeriBtn3 != null : "fx:id=\"setVeriBtn3\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert setVeriBtn4 != null : "fx:id=\"setVeriBtn4\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert threshhold_Inverse != null : "fx:id=\"threshhold_Inverse\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert triggerBtn != null : "fx:id=\"triggerBtn\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert outTriggerBtn != null : "fx:id=\"outTriggerBtn\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert aTriggerBtn != null : "fx:id=\"autoTriggerBtn\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert imgGLAY1 != null : "fx:id=\"imgGLAY1\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert imgGLAY2 != null : "fx:id=\"imgGLAY2\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert imgGLAY3 != null : "fx:id=\"imgGLAY3\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert fpsLabel != null : "fx:id=\"fpsLabel\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert triggerCCircle != null : "fx:id=\"triggerCCircle\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert clearBtn != null : "fx:id=\"clearBtn\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert ngImageBtn != null : "fx:id=\"ngImageBtn\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert ngCounterLabel != null : "fx:id=\"ngCounterLabel\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert imgSaveFlg != null : "fx:id=\"imgSaveFlg\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert demoMode != null : "fx:id=\"demoMode\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert info2 != null : "fx:id=\"info2\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert lockBtn != null : "fx:id=\"lockBtn\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert lockShape1 != null : "fx:id=\"lockShape1\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert lockShape4 != null : "fx:id=\"lockShape4\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert lockShape2 != null : "fx:id=\"lockShape2\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert lockShape3 != null : "fx:id=\"lockShape3\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert lockShape5 != null : "fx:id=\"lockShape5\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert imgNG != null : "fx:id=\"imgNG\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert outTrigDisableChk != null : "fx:id=\"outTrigDisableChk\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert settingMode != null : "fx:id=\"settingMode\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert portNoSpin != null : "fx:id=\"portNoSpin\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert zoomLabel != null : "fx:id=\"zoomLabel\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert dellySpinner != null : "fx:id=\"dellySpinner\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert camIDspinner != null : "fx:id=\"camIDspinner\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert capW_text != null : "fx:id=\"capW_text\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert capH_text != null : "fx:id=\"capH_text\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert GPIO_STATUS_PIN0 != null : "fx:id=\"GPIO_STATUS_PIN0\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert GPIO_STATUS_PIN1 != null : "fx:id=\"GPIO_STATUS_PIN1\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert GPIO_STATUS_PIN3 != null : "fx:id=\"GPIO_STATUS_PIN3\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert imgSaveFlg_all != null : "fx:id=\"imgSaveFlg_all\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert OKImageBtn != null : "fx:id=\"OKImageBtn\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert accordion_1 != null : "fx:id=\"accordion_1\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert throughImageChk != null : "fx:id=\"throughImageChk\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert throughImageChk != null : "fx:id=\"throughImageChk\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert shotImgBtn != null : "fx:id=\"shotImgBtn\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert shotImgBtn_chess != null : "fx:id=\"shotImgBtn_chess\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert cameraCalib != null : "fx:id=\"cameraCalib\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert calibDataDel != null : "fx:id=\"calibDataDel\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert adc_thresh_value != null : "fx:id=\"adc_thresh_value\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert adc_flg != null : "fx:id=\"adc_flg\" was not injected: check your FXML file 'Sample2.fxml'.";
        //パターンマッチング関係
        assert ptm_setting_accordion != null : "fx:id=\"ptm_setting_accordion\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert ptm_set_pt1 != null : "fx:id=\"ptm_set_pt1\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert ptm_set_pt2 != null : "fx:id=\"ptm_set_pt2\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert ptm_set_pt3 != null : "fx:id=\"ptm_set_pt3\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert ptm_set_pt4 != null : "fx:id=\"ptm_set_pt4\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert ptm_set_para1 != null : "fx:id=\"ptm_set_para1\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert ptm_set_para2 != null : "fx:id=\"ptm_set_para2\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert ptm_set_para3 != null : "fx:id=\"ptm_set_para3\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert ptm_set_para4 != null : "fx:id=\"ptm_set_para4\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert ptm_pt1_enable != null : "fx:id=\"ptm_pt1_enable\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert ptm_pt2_enable != null : "fx:id=\"ptm_pt2_enable\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert ptm_pt3_enable != null : "fx:id=\"ptm_pt3_enable\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert ptm_pt4_enable != null : "fx:id=\"ptm_pt4_enable\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert ptm_img1 != null : "fx:id=\"ptm_img1\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert ptm_img2 != null : "fx:id=\"ptm_img2\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert ptm_img3 != null : "fx:id=\"ptm_img3\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert ptm_img4 != null : "fx:id=\"ptm_img4\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert debugImg != null : "fx:id=\"debugImg\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert whiteRatioLabel != null : "fx:id=\"whiteRatioLabel\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert blackRatioLabel != null : "fx:id=\"blackRatioLabel\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert whiteRatioMaxSp != null : "fx:id=\"whiteRatioMaxSp\" was not injected: check your FXML file 'Sample2.fxml'.";
        assert whiteRatioMinSp != null : "fx:id=\"whiteRatioMinSp\" was not injected: check your FXML file 'Sample2.fxml'.";

        //クラス変数の初期化
        rects = Collections.synchronizedList(new ArrayList<>());
        draggingRect = new Rectangle(0, 0,1,1);
        moveDraggingPoint[0] = new Point();//ドラッグ移動始点
        moveDraggingPoint[1] = new Point();//ドラッグ移動終点
        dragging = false;
        moveDragingFlg = false;
		Platform.runLater(() ->aPane.setStyle("-fx-background-radius: 0;-fx-background-color: #a5abb094"));

		//イニシャルinfo2の内容保存
		initInfo2 = this.info2.getText();

		loadAllPara();

        updateImageView(imgGLAY1, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
        updateImageView(imgGLAY2, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
        updateImageView(imgGLAY3, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
        updateImageView(imgGLAY, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
    	Platform.runLater(() ->info1.setText(""));

        accordion_1.expandedPaneProperty().addListener(new
                ChangeListener<TitledPane>() {
                    public void changed(ObservableValue<? extends TitledPane> ov,
                        TitledPane old_val, TitledPane new_val) {
                    	if( new_val == ptm_setting_accordion) {
                    		onOpenPtmAccordion(null);
                    	}else if(old_val == ptm_setting_accordion ) {
                    		ptmSetStartFlg = false;
                    	}
                  }
            });

    	try {
	    	int fileCnt = FileClass.getFiles(new File("./ok_image")).length;
	    		allSaveCnt = fileCnt;
		}catch( java.lang.NullPointerException e) {
    		Platform.runLater(() ->info1.setText("ok_imageフォルダがありません"));
    	}

        try {
			onTest(null);
		} catch (InterruptedException e) {
			System.out.println(" void initialize() {onTest(null);::エラー");
			e.printStackTrace();
		}
        manualTrigger = true;
        try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

        onAllClear(null);



    }
}
