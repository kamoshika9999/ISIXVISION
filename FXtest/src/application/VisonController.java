package application;

import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.Stroke;
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

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
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

	public int shotCnt = 0; //オールクリアしてからのカウント数

	final int saveMax_all = 255;
	final int saveMax_ng = 400;
	int ngCnt = 0;
	int allSaveCnt = 0;

	public static Mat srcMat = new Mat();//保存画像を使用した設定に使用する為publicにしておく
	Mat dstframe = new Mat();//srcMatをカメラキャリブレーションデーターから変換したオブジェクトが入る
	private Mat mainViewGlayMat;
    List<Rectangle> rects;
    Rectangle draggingRect;
    volatile boolean dragging;
	boolean moveDragingFlg;
	Point[] moveDraggingPoint = new Point[2];
	Point moveDraggingPointView = new Point();


    public static preSet pObj;
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
	public Mat[][] ptm_ImgMat = new Mat[4][4];//[presetNo][ptm1～ptm4]
	private TMpara ptm_tmpara = new TMpara();
	private templateMatching ptm_templateMatchingObj;

	//保存画像を使用した設定用
	public static boolean saveImgUseFlg;//保存画像を使用した設定に使うフラグ   保存画像使用中はＰＬＣシャッタートリガ強制無効
	public static Mat saveImgMat;
	//連続画像保存によるタイムラグ緩和のロジックに使用
	private long savelockedTimer;
	private long savelockedTimerThresh = 1000;
	private int NGsaveCnt = 0;

	//カメラキャリブレーション用
	private boolean cameraCalibFlg = false;
	private Mat cameraMatrix;//内部パラメータ
	private Mat distortionCoefficients;//歪み係数

	//インフォメーション
	private String initInfo2;

	//チャート
	private Tab[] chartTab_P2;
	private Tab[] chartTab_F;
	private JFreeChart[] chart_P2;
	private JFreeChart[] chart_F;
	public Mat[][] dim_ImgMat = new Mat[4][4];//[presetNo][dim1～dim4]
	private TMpara dim_tmpara = new TMpara();
	private templateMatching dim_templateMatchingObj;
	private double[] P2_sum = new double[2];
	private double[] F_sum = new double[2];
	private double[] E_sum = new double[2];

	@FXML
    private Spinner<Integer> dellySpinner;
  @FXML
    private Spinner<Integer> dellySpinner2;
    @FXML
    private CheckBox trigger_2nd_chk;
    @FXML
    private ToggleButton para_12st_shot;
    @FXML
    private ToggleButton para_12st_shot1;
    @FXML
    private ToggleButton para_12st_shot11;


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
    private Slider gauusianSliderK;//アパーチャサイズ
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
    private CheckBox FilterViewMode;//True:フィルタビュー有効
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
    @FXML
    private Button ReTestBtn;//retest_imageフォルダに入っている画像を再テストする
    @FXML
    private Button holeAnalysisBtn;//穴検出解析ダイアログを開く

    @FXML
    private Button paraInitBtn;//設定初期化



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
    private CheckBox ptm_disableChk;
    //穴判定関係
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
    @FXML
    private Button whiteAreaBtn;

    //寸法測定用
    @FXML
    private ImageView dim_okuriImg_1;
    @FXML
    private Button dim_set_para1;
    @FXML
    private CheckBox dim_1_enable;
    @FXML
    private ImageView dim_poke_1;
    @FXML
    private Button dim_set_para2;
    @FXML
    private TextField dim_offset_F_1;
    @FXML
    private TextField dim_offset_P2_1;
    @FXML
    private TextField dim_offset_E_1;
    @FXML
    private ImageView dim_okuriImg_2;
    @FXML
    private Button dim_set_para3;
    @FXML
    private Button dim_set_para4;
    @FXML
    private ImageView dim_poke_2;
    @FXML
    private CheckBox dim_2_enable;
    @FXML
    private TextField dim_offset_F_2;
    @FXML
    private TextField dim_offset_P2_2;
    @FXML
    private TextField dim_offset_E_2;
    @FXML
    private Button dimSettingBtn;
    @FXML
    private TextField dimSetting_offset;
    @FXML
    private Label dimSettingLabel;
    @FXML
    private Button dimensionBtn;
    @FXML
    private TableView<Dim_itemValue> dim_table;
    @FXML
    private TableColumn dim_table_item;
    @FXML
    private TableColumn dim_table_P2;
    @FXML
    private TableColumn dim_table_F;
    @FXML
    private TableColumn dim_table_E;

    //カメラ関係
    @FXML
    private Button getExproBtn;
    @FXML
    private Button setExproBtn;
    @FXML
    private Button getGainBtn;
    @FXML
    private Button setGainBtn;
    @FXML
    private TextField cameraExpro;
    @FXML
    private TextField cameraGain;
    //ショット数
    @FXML
    private Label count_label;

    @FXML
    private CheckBox dimensionDispChk;
    @FXML
    private CheckBox holeDispChk;
    @FXML
    private CheckBox patternDispChk;
    //チャート
    @FXML
    private TabPane dataTabpane;
    @FXML
    private Label dimChartValueLable;

	private XYSeriesCollection[] dataset_P2;
	private XYSeriesCollection[] dataset_F;
	private double holeDist_DimSetting;

	private int targetSetParaNO = 1;




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
		double exp = capObj.get(Videoio.CAP_PROP_EXPOSURE);
		double gain = capObj.get(Videoio.CAP_PROP_GAIN);
		Platform.runLater( () ->info2.appendText("\n"+ "カメラ解像度 WIDTH="+ wset+
				"\n カメラ解像度 HEIGHT= " +hset + "\n"+
				"\n 露出="+exp+"\n"+
				"\n ゲイン="+gain+"\n"
				));
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
    	double zoomStep = 0.02;
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
			//露出の取得
			onGetExpro(null);

			//ゲインの取得
			onGetGain(null);

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
		source_video = new VideoCapture("./test4.mp4" );//デモモード用動画
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
        draggingRect.width = 1;
        draggingRect.height = 1;
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
    	}
    	if( srcMat.width() < 1) return;


    	//サブピクセル化
    	//Mat tmpMat = new Mat();
    	//Imgproc.getRectSubPix(srcMat, new Size(1920,1080), new Point(1920/2,1080/2), tmpMat);
    	//System.out.println("SUBPixelSize="+tmpMat.width()+" : "+tmpMat.height());

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
	    		Platform.runLater( () ->fpsLabel.setText(String.format("FPS=%.3f", fps)));

	    		fpsFirst = System.currentTimeMillis();
	    		fpsCnt=0;
	    	}


	    	parameter para = pObj.para[pObj.select];
	    	Mat	mainViewMat = srcMat.clone();//srcMatは不変にしておく
	    	Mat saveSrcMat = srcMat.clone();

	    	mainViewGlayMat = new Mat();
	    	Imgproc.cvtColor(mainViewMat, mainViewGlayMat, Imgproc.COLOR_BGR2GRAY);
	    	//Imgproc.equalizeHist(mainViewGlayMat, mainViewGlayMat);//コントラスト均等化

	    	Mat ptnAreaMat = mainViewGlayMat.clone();
	    	Mat holeDetectAreaMat = mainViewGlayMat.clone();//各穴のパラメータに従った判定のベースMat
	    	Mat tmp1Mat = new Mat();
	    	Mat tmp2Mat = new Mat();
	    	Mat tmp3Mat = new Mat();
	    	Mat tmp4mat = new Mat();
	    	Mat fillterAftterMat = mainViewGlayMat.clone();//セッティングモード時判定のベースMat
	    	int filterUselFlg = 0;

	    	//ガウシアン→２値化→膨張の順番
	    	if( gauusianCheck.isSelected() ) {
	    		double sigmaX = gauusianSliderX.getValue();
	    		double sigmaY = gauusianSliderY.getValue();
	    		int tmpValue =(int)gauusianSliderK.getValue();
	    		if( tmpValue % 2 == 0 ) {
	    			tmpValue++;
	    		}
	    		Size sz = new Size(tmpValue,tmpValue);
	    		Imgproc.GaussianBlur(fillterAftterMat, fillterAftterMat, sz, sigmaX,sigmaY);
	    		tmp1Mat = fillterAftterMat.clone();
	    		filterUselFlg+=1;
	    	}
	    	if( threshholdCheck.isSelected()) {
	    		int type = threshhold_Inverse.isSelected()?Imgproc.THRESH_BINARY_INV:Imgproc.THRESH_BINARY;
	    		Imgproc.threshold(fillterAftterMat, fillterAftterMat, this.threshholdSlider.getValue(),255,type);
	    		tmp2Mat = fillterAftterMat.clone();
	    		filterUselFlg+=2;
	    	}
	    	if( dilateCheck.isSelected() ) {
	    		int n = (int)dilateSliderN.getValue();
	    		Imgproc.dilate(fillterAftterMat, fillterAftterMat, new Mat(),new Point(-1,-1),n);
	    		tmp3Mat = fillterAftterMat.clone();
	    		filterUselFlg+=4;
	    	}
    		Imgproc.Canny(fillterAftterMat, tmp4mat,sliderDetecPara6.getValue(),sliderDetecPara6.getValue()/2);

	    	if( FilterViewMode.isSelected()) {
	    		switch(filterUselFlg) {
		    	case 0://全てなし
			        updateImageView(imgGLAY1, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY2, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY3, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY, Utils.mat2Image(tmp4mat));
		    		break;
		    	case 1://ガウシアンのみ
			        updateImageView(imgGLAY1, Utils.mat2Image(tmp1Mat));
			        updateImageView(imgGLAY2, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY3, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY, Utils.mat2Image(tmp4mat));
		    		break;
		    	case 2://２値化のみ
			        updateImageView(imgGLAY1, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY2, Utils.mat2Image(tmp2Mat));
			        updateImageView(imgGLAY3, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY, Utils.mat2Image(tmp4mat));
		    		break;
		    	case 3://ガウシアンと２値化あり
			        updateImageView(imgGLAY1, Utils.mat2Image(tmp1Mat));
			        updateImageView(imgGLAY2, Utils.mat2Image(tmp2Mat));
			        updateImageView(imgGLAY3, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY, Utils.mat2Image(tmp4mat));
		    		break;
		    	case 4://膨張のみ
			        updateImageView(imgGLAY1, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY2, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY3, Utils.mat2Image(tmp3Mat));
			        updateImageView(imgGLAY, Utils.mat2Image(tmp4mat));
		    		break;
		    	case 5://ガウシアンと膨張あり
			        updateImageView(imgGLAY1, Utils.mat2Image(tmp1Mat));
			        updateImageView(imgGLAY2, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY3, Utils.mat2Image(tmp3Mat));
			        updateImageView(imgGLAY, Utils.mat2Image(tmp4mat));
		    		break;
		    	case 6://ガウシアン無し　他あり
			        updateImageView(imgGLAY1, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY2, Utils.mat2Image(tmp2Mat));
			        updateImageView(imgGLAY3, Utils.mat2Image(tmp3Mat));
			        updateImageView(imgGLAY, Utils.mat2Image(tmp4mat));
		    		break;
		    	case 7://全てあり
			        updateImageView(imgGLAY1, Utils.mat2Image(tmp1Mat));
			        updateImageView(imgGLAY2, Utils.mat2Image(tmp2Mat));
			        updateImageView(imgGLAY3, Utils.mat2Image(tmp3Mat));
			        updateImageView(imgGLAY, Utils.mat2Image(tmp4mat));
		    		break;
		    	}
	    	}

	        if (dragging && settingModeFlg) {
	            Imgproc.rectangle(mainViewMat,
	            		new Point(draggingRect.x,draggingRect.y),
	            		new Point(draggingRect.x+draggingRect.width,draggingRect.y+draggingRect.height),
	            		new Scalar(0,255,0),2);
	        }else{
	    		if( srcMat.width() < draggingRect.x+draggingRect.width) {
	    			draggingRect.width = srcMat.width() - draggingRect.x;
	    		}
	    		if( srcMat.height() < draggingRect.y+draggingRect.height) {
	    			draggingRect.height = srcMat.height() - draggingRect.y;
	    		}
	    		if(draggingRect.width >0 && draggingRect.height > 0 && settingModeFlg){
	        		Mat fillterAftterMatRoi = fillterAftterMat.submat(
		        			draggingRect.y,
		        			draggingRect.y + draggingRect.height,
		        			draggingRect.x,
		        			draggingRect.x+draggingRect.width);
		        	//穴検出
		        	if( !ptmSetStartFlg ) {
			            Mat resultCircles = new Mat();
						Imgproc.HoughCircles(fillterAftterMatRoi, resultCircles, Imgproc.CV_HOUGH_GRADIENT,
								sliderDetecPara4.getValue(),
								sliderDetecPara5.getValue(),
								sliderDetecPara6.getValue(),
								sliderDetecPara7.getValue(),
								(int)sliderDetecPara8.getValue(),
								(int)sliderDetecPara9.getValue());
						if( resultCircles.cols() > 0) {
							fncDrwCircles(fillterAftterMatRoi,resultCircles,
									mainViewMat.submat(
						        			draggingRect.y,
						        			draggingRect.y + draggingRect.height,
						        			draggingRect.x,
						        			draggingRect.x+draggingRect.width),
											true);

							Imgproc.putText(mainViewMat, String.valueOf(resultCircles.cols()),
									new Point(draggingRect.x-25,draggingRect.y-6),
									Imgproc.FONT_HERSHEY_SIMPLEX, 2.0,new Scalar(128,255,128),7);

							//面積判定
							boolean holeAreaJudgFlg = holeWhiteAreaCheck(
									fillterAftterMatRoi,resultCircles,whiteRatioMaxSp.getValue(),whiteRatioMinSp.getValue());
							if( holeAreaJudgFlg ) {
								Imgproc.putText(mainViewMat,
										"WhiteArea OK  ave=" + String.format("%d",whiteAreaAverage) +
												" Max=" + String.format("%d",whiteAreaMax) +
												" Min=" + String.format("%d",whiteAreaMin),
										new Point(draggingRect.x+20,draggingRect.y-6),
										Imgproc.FONT_HERSHEY_SIMPLEX, 1.5,new Scalar(128,255,128),7);
							}else {
								Imgproc.putText(mainViewMat, "WhiteArea NG  ave=" + String.format("%d",whiteAreaAverage) +
										" Max=" + String.format("%d",whiteAreaMax) +
										" Min=" + String.format("%d",whiteAreaMin),
										new Point(draggingRect.x+20,draggingRect.y-6),
										Imgproc.FONT_HERSHEY_SIMPLEX, 1.5,new Scalar(0,0,255),7);
							}
						}
		        	}
		            Imgproc.rectangle(mainViewMat,
		            		new Point(draggingRect.x,draggingRect.y),
		            		new Point(draggingRect.x+draggingRect.width,draggingRect.y+draggingRect.height),
		            		new Scalar(0,255,0),2);
	        	}
	        }

	    	int judgCnt=0;
	    	shotCnt++;
	    	String fileString = "x"+String.valueOf(shotCnt)+"x";//ショット数を入れる
	    	Platform.runLater( () ->this.count_label.setText(String.valueOf(shotCnt)));//ショット数を更新

	    	boolean ngFlg;
			Scalar color;
	        for (int i=0;i<4;i++) {
				ngFlg = false;
	        	if( para.hole_DetectFlg[i] ) {
		        	Rectangle r = para.hole_rects[i];//検査範囲
		        	Mat holeDetectAreaMatroi = holeDetectAreaMat.submat(new Rect(r.x,r.y,r.width,r.height));
		        	if( para.hole_fil_gauusianCheck[i] ) {
		        		double sigmaX = para.hole_fil_gauusianX[i];
		        		double sigmaY = para.hole_fil_gauusianY[i];
		        		int tmpValue =(int) para.hole_fil_gauusianValue[i];
		        		if( tmpValue % 2 == 0 ) {
		        			tmpValue++;
		        		}
		        		Size sz = new Size(tmpValue,tmpValue);
		        		Imgproc.GaussianBlur(holeDetectAreaMatroi, holeDetectAreaMatroi, sz, sigmaX,sigmaY);
		        	}
		        	if( para.hole_fil_threshholdCheck[i]) {
		        		int type = para.hole_fil_threshhold_Invers[i]?Imgproc.THRESH_BINARY_INV:Imgproc.THRESH_BINARY;
		        		Imgproc.threshold(holeDetectAreaMatroi, holeDetectAreaMatroi, para.hole_fil_threshhold[i],255,type);
		        	}
		        	if( para.hole_fil_dilateCheck[i]) {
		        		Imgproc.dilate(holeDetectAreaMatroi, holeDetectAreaMatroi, new Mat(),new Point(-1,-1),para.hole_fil_dilateValue[i]);
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
					Imgproc.HoughCircles(holeDetectAreaMatroi, circles, Imgproc.CV_HOUGH_GRADIENT,
							para.hole_circlePara4[i],
							para.hole_circlePara5[i],
							para.hole_circlePara6[i],
							para.hole_circlePara7[i],
							(int)para.hole_circlePara8[i],
							(int)para.hole_circlePara9[i]);

					boolean holeAreaFlg = true;
					if( circles.cols() > 0 && !FilterViewMode.isSelected()) {

						if( holeDispChk.isSelected() ) {
							fncDrwCircles(holeDetectAreaMatroi,circles, mainViewMat.submat(new Rect(r.x,r.y,r.width,r.height)),false);
							Imgproc.putText(mainViewMat, String.valueOf(circles.cols()),
									new Point(r.x-25,r.y-6),
									Imgproc.FONT_HERSHEY_SIMPLEX, 2.0,new Scalar(0,255,0),7);
						}
						//面積判定
						holeAreaFlg = holeWhiteAreaCheck(
								holeDetectAreaMatroi,circles,para.hole_whiteAreaMax[i],para.hole_whiteAreaMin[i]);
						if( holeDispChk.isSelected() ) {
							if( holeAreaFlg ) {
									Imgproc.putText(mainViewMat, "WhiteArea OK  ave=" + String.format("%d",whiteAreaAverage) +
										" Max=" + String.format("%d",whiteAreaMax) +
										" Min=" + String.format("%d",whiteAreaMin),
										new Point(r.x+20,r.y-6),
										Imgproc.FONT_HERSHEY_SIMPLEX, 1.5,new Scalar(0,255,0),7);
							}else {
								Imgproc.putText(mainViewMat, "WhiteArea NG  ave=" + String.format("%d",whiteAreaAverage) +
										" Max=" + String.format("%d",whiteAreaMax) +
										" Min=" + String.format("%d",whiteAreaMin),
										new Point(r.x+20,r.y-6),
										Imgproc.FONT_HERSHEY_SIMPLEX, 1.5,new Scalar(0,0,255),7);
							}
						}

					}

					//判定
		            switch(i) {
		            	case 0:
		            		Platform.runLater( () ->okuri1_n.setText( String.format("%d個", circles.cols()) + infoText));
		            		if( circles.cols() == para.hole_cntHoleTh[i] && holeAreaFlg ) {
		            			Platform.runLater( () ->okuri1_judg.setText("OK"));
		            			Platform.runLater( () ->okuri1_judg.setTextFill( Color.GREEN));
		            			judgCnt++;
		            		}else {
		            			Platform.runLater( () ->okuri1_judg.setText("NG"));
		            			Platform.runLater( () ->okuri1_judg.setTextFill( Color.RED));
		            			//fileString += "1_okuri_";
		            			ngFlg = true;
		            		}
		            		break;
		            	case 1:
		            		Platform.runLater( () ->okuri2_n.setText(String.format("%d個", circles.cols()) + infoText));
		            		if( circles.cols() == para.hole_cntHoleTh[i] && holeAreaFlg ) {
		            			Platform.runLater( () ->okuri2_judg.setText("OK"));
		            			Platform.runLater( () ->okuri2_judg.setTextFill( Color.GREEN));
		            			judgCnt++;
		            		}else {
		            			Platform.runLater( () ->okuri2_judg.setText("NG"));
		            			Platform.runLater( () ->okuri2_judg.setTextFill( Color.RED));
		            			//fileString += "1_poke_";
		            			ngFlg = true;
		            		}
		            		break;
		            	case 2:
		            		Platform.runLater( () ->okuri3_n.setText( String.format("%d個", circles.cols()) + infoText));
		            		if( circles.cols() == para.hole_cntHoleTh[i] && holeAreaFlg ) {
		            			Platform.runLater( () ->okuri3_judg.setText("OK"));
		            			Platform.runLater( () ->okuri3_judg.setTextFill( Color.GREEN));
		            			judgCnt++;
		            		}else {
		            			Platform.runLater( () ->okuri3_judg.setText("NG"));
		            			Platform.runLater( () ->okuri3_judg.setTextFill( Color.RED));
		            			//fileString += "2_okuri_";
		            			ngFlg = true;			            		}
		            		break;
		            	case 3:
		            		Platform.runLater( () ->okuri4_n.setText( String.format("%d個", circles.cols()) + infoText));
		            		if( circles.cols() == para.hole_cntHoleTh[i] && holeAreaFlg ) {
		            			Platform.runLater( () ->okuri4_judg.setText("OK"));
		            			Platform.runLater( () ->okuri4_judg.setTextFill( Color.GREEN));
		            			judgCnt++;
		            		}else {
		            			Platform.runLater( () ->okuri4_judg.setText("NG"));
		            			Platform.runLater( () ->okuri4_judg.setTextFill( Color.RED));
		            			//fileString += "2_poke_";
		            			ngFlg = true;
		            		}
		            		break;
		            	}

					if( ngFlg ) {
						color = new Scalar(0,0,255);
					}else {
						color = new Scalar(255,0,0);
					}
					if( !FilterViewMode.isSelected() && holeDispChk.isSelected() ) {
			            Imgproc.rectangle(mainViewMat,new Point(r.x, r.y),
			            		new Point(r.x+r.width, r.y+r.height),
			            		color,4);
					}
		        }else {
		        	judgCnt++;
		        }

	        }


	        //パターンマッチング
        	boolean tmFlg;
        	if( !ptm_disableChk.isSelected() ) {//パターンマッチングが強制的に無効になっているか？
        		tmFlg = ptm_templateMatchingObj.detectPattern(ptnAreaMat,mainViewMat
        											,false,patternDispChk.isSelected());
        	}else {
        		tmFlg = true;
        	}


        	//寸法測定

        	boolean dimFlg;
       		dimFlg = dim_templateMatchingObj.detectPattern(ptnAreaMat,mainViewMat
        											,false,dimensionDispChk.isSelected());
       		if( dimFlg ) {
        	for(int g=0;g<2;g++) {
        		if( ((g==0 && dim_1_enable.isSelected()) || ((g==1) && dim_2_enable.isSelected())) && shotCnt>0) {
        			double P2 = 0.0;
        			double F = 0.0;
        			if( dim_templateMatchingObj.resultValue[g*2].cnt == 1 &&
        					dim_templateMatchingObj.resultValue[g*2+1].cnt == 1) {
		        		double p2_x0 = dim_templateMatchingObj.resultValue[g*2].centerPositionX.get(0);
		        		double p2_x1 = dim_templateMatchingObj.resultValue[g*2+1].centerPositionX.get(0);
		        		//System.out.println("X0 = " + String.format("%.4f", p2_x0));
		        		//System.out.println("X1 = " + String.format("%.4f", p2_x1));
		        		P2 = Math.abs(p2_x0 - p2_x1)*para.dimPixel_mm+para.dim_offset_P2[g];
		        		P2_sum[g] += P2;
		        		double f_y0 = dim_templateMatchingObj.resultValue[g*2].centerPositionY.get(0);
		        		double f_y1 = dim_templateMatchingObj.resultValue[g*2+1].centerPositionY.get(0);
		        		F = Math.abs(f_y0 - f_y1)*para.dimPixel_mm+para.dim_offset_F[g];
		        		F_sum[g] += F;
        			}
        			final int g2 =g;

        			final double _P2 =Double.valueOf(String.format("%.3f",P2)).doubleValue();
        			final double _F = Double.valueOf(String.format("%.3f",F)).doubleValue();
        			final double P2_ave = P2_sum[g]/shotCnt;
        			final double F_ave = F_sum[g]/shotCnt;
        			final double _P2_ave = Double.valueOf(String.format("%.3f",P2_ave)).doubleValue();
        			final double _F_ave = Double.valueOf(String.format("%.3f",F_ave)).doubleValue();

        			final double P2_final = P2;
        			final double F_final = F;
        			Platform.runLater( () ->dataset_P2[g2].getSeries(0).add(shotCnt,P2_final));
	        		Platform.runLater( () ->dataset_F[g2].getSeries(0).add(shotCnt,F_final));
	        		//寸法表示テーブルの更新
	        		Platform.runLater( () ->dim_table.getItems().get(g2*2).P2Property().set(_P2));
	        		Platform.runLater( () ->dim_table.getItems().get(g2*2).FProperty().set(_F));
	        		Platform.runLater( () ->dim_table.getItems().get(g2*2+1).P2Property().set(_P2_ave));
	        		Platform.runLater( () ->dim_table.getItems().get(g2*2+1).FProperty().set(_F_ave));


	        		//軸の設定更新
	        		Platform.runLater( () ->((NumberAxis)((XYPlot)chart_P2[g2].getPlot()).getDomainAxis()).
																setRange(shotCnt<=200?0:shotCnt-200,shotCnt));
	        		Platform.runLater( () ->((NumberAxis)((XYPlot)chart_F[g2].getPlot()).getDomainAxis()).
	        													setRange(shotCnt<=200?0:shotCnt-200,shotCnt));
	        		Platform.runLater( () ->((NumberAxis)((XYPlot)chart_P2[g2].getPlot()).getRangeAxis()).
							setRange(1.8,2.2));
	        		Platform.runLater( () ->((NumberAxis)((XYPlot)chart_F[g2].getPlot()).getRangeAxis()).
							setRange(11.3,11.7));
        		}
        	}
       		}else {
       			Platform.runLater( () ->this.info2.appendText("寸法測定に失敗しました\n"));
       			Platform.runLater( () ->this.info2.appendText("登録領域が画像端ギリギリすぎると推定します。\n"));
       		}

	        if( !saveImgUseFlg && !settingModeFlg && shotCnt>0 ) {
		        //最終判定
	        	if(judgCnt==4 && tmFlg ) {
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
		        	if( imgSaveFlg.isSelected() && shotCnt > ngCnt+3 && ngCnt < saveMax_ng && !settingModeFlg) {
		        		saveImgNG( saveSrcMat,fileString);
		        		saveImgNG( mainViewMat,"_"+fileString);
		        	}else if( fileString != ""){
		        		//final String infoText = fileString +"\n";
		        		//Platform.runLater( () ->info2.appendText(infoText));
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
		        	updateImageView(imgNG, Utils.mat2Image(mainViewMat));
		        }
	        }

	        //Core.flip(mainViewMat, mainViewMat, 1);
	        updateImageView(imgORG, Utils.mat2Image(mainViewMat));
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH' h 'mm' m 'ss' s'");
        String fileName = fileString +"_" + sdf.format(timestamp) + "_" +String.valueOf(ngCnt);
        try {
        	Imgcodecs.imwrite(folder+"/" + fileName + ".png", imgMat);
        	Platform.runLater( () ->info2.appendText(folder+"/"+ fileName +".png"+"NG画像保存"+"\n"));
        }catch(Exception e) {
        	Platform.runLater( () ->info2.appendText("NG画像の保存に失敗"+e.toString()+"\n"));
        }
        Platform.runLater( () ->info2.appendText("NG画像ファイルを保存\n" + fileName +".png\n"));

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
        	Imgcodecs.imwrite(folder+"/" + allSaveCnt + ".png", imgMat);
        }catch(Exception e) {
        	Platform.runLater( () ->info2.appendText("OK画像の保存に失敗"+e.toString()+"\n"));
        }

        if( allSaveCnt > saveMax_all+1 ) {
        	File f = new File(folder+"/"+(allSaveCnt-saveMax_all)+".png");
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
        	Imgcodecs.imwrite(folder+"/" + fileName + ".png", imgMat);
        	Platform.runLater( () ->info2.appendText(folder+"/"+ fileName +".png"+"shot画像保存"+"\n"));
        }catch(Exception e) {
        	Platform.runLater( () ->info2.appendText("shot画像の保存に失敗"+e.toString()+"\n"));
        }
        Platform.runLater( () ->info2.appendText("shot画像ファイルを保存\n" + fileName +".png\n"));

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
        	Imgcodecs.imwrite(folder+"/" + fileName + ".png", imgMat);
        	Platform.runLater( () ->info2.appendText(folder+"/"+ fileName +".png"+"チェスボード画像保存"+"\n"));
        }catch(Exception e) {
        	Platform.runLater( () ->info2.appendText("チェスボード画像の保存に失敗"+e.toString()+"\n"));
        }
        Platform.runLater( () ->info2.appendText("チェスボード画像ファイルを保存\n" + fileName +".png\n"));

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
  		  holeDist_DimSetting = distAve;
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
  	    Imgproc.circle(img, pt, (int) rho, new Scalar(0,255,0),4);
  	    Imgproc.arrowedLine(img, new Point(pt.x-50,pt.y-50),
  	    		new Point(pt.x-6,pt.y-6),new Scalar(0,255,0), 4);
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
	  final int offset = 0;

	  whiteAreaAverage = 0;
	  whiteAreaMax = 0;
	  whiteAreaMin = 99999;

	  for( int i= 0; i < circles.cols(); i++) {
		double[] v = circles.get(0, i);//[0]:X  [1]:Y  [2]:r
		int  x = (int)v[0];
		int  y = (int)v[1];
		int  r = (int)v[2];
		if( x-r-offset < 0 || y-r-offset<0 || x-r-offset+r+r+offset>judgeAreaMat.width() || y-r-offset+r+r+offset>judgeAreaMat.height() ) {
			result = false;
			Platform.runLater(() ->info2.appendText("穴検出エリアが狭すぎ、面積判定ができません。\n"));
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
	  	  Platform.runLater(() ->whiteRatioLabel.setText( String.format("%d", whiteAreaMax)));
	  	  Platform.runLater(() ->blackRatioLabel.setText( String.format("%d", whiteAreaMin)));
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
    	int max = (int)(Double.valueOf(whiteRatioLabel.getText())*1.2);
    	int min = (int)(Double.valueOf(blackRatioLabel.getText())*0.8);
    	Platform.runLater( () ->whiteRatioMaxSp.setValueFactory(
				new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 99999,
				max,
				5)));
    	Platform.runLater( () ->whiteRatioMinSp.setValueFactory(
				new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 99999,
				min,
				5)));
    	eventTrigger = true;
    }
    @FXML
    void onWhiteAreaBtn(ActionEvent event) {
    	onWhiteAreaLabelClicked(null);
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
			if( para.hole_DetectFlg[0] ) {
    			para.hole_DetectFlg[0] = false;
			}else {
    			setPara(0);
    	    	//onSettingModeBtn(null);
    		}
    	}else if(eObject == okuri2_btn) {
    		if(para.hole_DetectFlg[1] ){
    			para.hole_DetectFlg[1] = false;
    		}else {
    			setPara(1);
    	    	//onSettingModeBtn(null);
    		}
    	}else if(eObject == okuri3_btn) {
    		if(para.hole_DetectFlg[2]){
    			para.hole_DetectFlg[2] = false;
    		}else {
    			setPara(2);
    	    	//onSettingModeBtn(null);
    		}
    	}else if(eObject == okuri4_btn) {
    		if(para.hole_DetectFlg[3]){
    			para.hole_DetectFlg[3] = false;
    		}else {
    			setPara(3);
    	    	//onSettingModeBtn(null);
    		}
    	}
    	setBtnPara();

    	eventTrigger = true;

    }


    private void setPara(int i) {
    	parameter para = pObj.para[pObj.select];
    	para.hole_circlePara4[i] = sliderDetecPara4.getValue();
    	para.hole_circlePara5[i] = sliderDetecPara5.getValue();
    	para.hole_circlePara6[i] = sliderDetecPara6.getValue();
    	para.hole_circlePara7[i] = sliderDetecPara7.getValue();
    	para.hole_circlePara8[i] = sliderDetecPara8.getValue();
    	para.hole_circlePara9[i] = sliderDetecPara9.getValue();

    	para.hole_rects[i] = (Rectangle)draggingRect.clone();

    	para.hole_DetectFlg[i] = true;
    	para.hole_fil_gauusianCheck[i] = gauusianCheck.isSelected();
    	para.hole_fil_gauusianX[i] = gauusianSliderX.getValue();
    	para.hole_fil_gauusianY[i] = gauusianSliderY.getValue();
    	para.hole_fil_gauusianValue[i] = gauusianSliderK.getValue();
    	para.hole_fil_dilateCheck[i] = dilateCheck.isSelected();
    	para.hole_fil_dilateValue[i] = (int)dilateSliderN.getValue();
    	para.hole_fil_threshholdCheck[i] = threshholdCheck.isSelected();
    	para.hole_fil_threshhold[i] = threshholdSlider.getValue();
    	para.hole_fil_threshhold_Invers[i] = threshhold_Inverse.isSelected();
    	para.hole_whiteAreaMax[i] = whiteRatioMaxSp.getValue().intValue();
    	para.hole_whiteAreaMin[i] = whiteRatioMinSp.getValue().intValue();

    }
    private void setBtnPara() {
    	parameter para = pObj.para[pObj.select];
		if( !para.hole_DetectFlg[0] ) {
			Platform.runLater(() ->okuri1_btn.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null))));
			Platform.runLater(() ->okuri1_btn.setTextFill( Color.BLACK));
			Platform.runLater(() ->okuri1_n.setText("-"));
		}else {
			Platform.runLater(() ->okuri1_btn.setBackground(new Background(new BackgroundFill(Color.GREEN, null, null))));
			Platform.runLater(() ->okuri1_btn.setTextFill( Color.WHITE));
		}
		if(!para.hole_DetectFlg[1] ){
			Platform.runLater(() ->okuri2_btn.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null))));
			Platform.runLater(() ->okuri2_btn.setTextFill( Color.BLACK));
			Platform.runLater(() ->okuri2_n.setText("-"));
		}else {
			Platform.runLater(() ->okuri2_btn.setBackground(new Background(new BackgroundFill(Color.GREEN, null, null))));
			Platform.runLater(() ->okuri2_btn.setTextFill( Color.WHITE));
		}
		if(!para.hole_DetectFlg[2]){
			Platform.runLater(() ->okuri3_btn.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null))));
			Platform.runLater(() ->okuri3_btn.setTextFill( Color.BLACK));
			Platform.runLater(() ->okuri3_n.setText("-"));
		}else {
			Platform.runLater(() ->okuri3_btn.setBackground(new Background(new BackgroundFill(Color.GREEN, null, null))));
			Platform.runLater(() ->okuri3_btn.setTextFill( Color.WHITE));
		}
		if(!para.hole_DetectFlg[3]){
			Platform.runLater(() ->okuri4_btn.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null))));
			Platform.runLater(() ->okuri4_btn.setTextFill( Color.BLACK));
			Platform.runLater(() ->okuri4_n.setText("-"));
		}else {
			Platform.runLater(() ->okuri4_btn.setBackground(new Background(new BackgroundFill(Color.GREEN, null, null))));
			Platform.runLater(() ->okuri4_btn.setTextFill( Color.WHITE));
		}
		Platform.runLater(() ->dimensionDispChk.setSelected(pObj.dimensionDispChk));
		Platform.runLater(() ->holeDispChk.setSelected(pObj.holeDispChk));
		Platform.runLater(() ->patternDispChk.setSelected(pObj.patternDispChk));

		Platform.runLater(() ->this.dim_1_enable.setSelected(para.dim_1_enable));
		Platform.runLater(() ->this.dim_2_enable.setSelected(para.dim_2_enable));


		para.hole_rects[4] = (Rectangle)draggingRect.clone();
		para.hole_viewRect[0] = imgORG.getViewport().getMinX();
		para.hole_viewRect[1] = imgORG.getViewport().getMinY();
		para.hole_viewRect[2] = imgORG.getViewport().getWidth();
		para.hole_viewRect[3] = imgORG.getViewport().getHeight();
		para.hole_circlePara4[4] = sliderDetecPara4.getValue();
		para.hole_circlePara5[4] = sliderDetecPara5.getValue();
		para.hole_circlePara6[4] = sliderDetecPara6.getValue();
		para.hole_circlePara7[4] = sliderDetecPara7.getValue();
		para.hole_circlePara8[4] = sliderDetecPara8.getValue();
		para.hole_circlePara9[4] = sliderDetecPara9.getValue();
		para.hole_fil_gauusianCheck[4] = gauusianCheck.isSelected();
		para.hole_fil_gauusianX[4] = gauusianSliderX.getValue();
		para.hole_fil_gauusianY[4] = gauusianSliderY.getValue();
		para.hole_fil_gauusianValue[4] = gauusianSliderK.getValue();
		para.hole_fil_dilateCheck[4] = dilateCheck.isSelected();
		para.hole_fil_dilateValue[4] = (int)dilateSliderN.getValue();
		para.hole_zoom = zoomValue_slider.getValue();
		para.hole_fil_threshholdCheck[4] = threshholdCheck.isSelected();
		para.hole_fil_threshhold[4] = threshholdSlider.getValue();
		para.hole_fil_threshhold_Invers[4] = threshhold_Inverse.isSelected();
		para.hole_whiteAreaMax[4] = whiteRatioMaxSp.getValue();
		para.hole_whiteAreaMin[4] = whiteRatioMinSp.getValue();


		setSlidbar();
    }
    private void setSlidbar() {
    	parameter para = pObj.para[pObj.select];
		Platform.runLater(() ->sliderDetecPara4.setValue(para.hole_circlePara4[4]));
		Platform.runLater(() ->sliderDetecPara5.setValue(para.hole_circlePara5[4]));
		Platform.runLater(() ->sliderDetecPara6.setValue(para.hole_circlePara6[4]));
		Platform.runLater(() ->sliderDetecPara7.setValue(para.hole_circlePara7[4]));
		Platform.runLater(() ->sliderDetecPara8.setValue(para.hole_circlePara8[4]));
		Platform.runLater(() ->sliderDetecPara9.setValue(para.hole_circlePara9[4]));
		Platform.runLater(() ->gauusianCheck.setSelected(para.hole_fil_gauusianCheck[4]));
		Platform.runLater(() ->gauusianSliderX.setValue(para.hole_fil_gauusianX[4]));
		Platform.runLater(() ->gauusianSliderY.setValue(para.hole_fil_gauusianY[4]));
		Platform.runLater(() ->gauusianSliderK.setValue(para.hole_fil_gauusianValue[4]));
		Platform.runLater(() ->dilateCheck.setSelected(para.hole_fil_dilateCheck[4]));
		Platform.runLater(() ->dilateSliderN.setValue(para.hole_fil_dilateValue[4]));
		Platform.runLater(() ->zoomValue_slider.setValue(para.hole_zoom));
		Platform.runLater(() ->zoomLabel.setText(String.format("%.1f",para.hole_zoom)));
		Platform.runLater(() ->threshholdCheck.setSelected(para.hole_fil_threshholdCheck[4]));
		Platform.runLater(() ->threshholdSlider.setValue(para.hole_fil_threshhold[4]));
    	Platform.runLater(() ->threshholdLabel.setText(String.format("%.1f",threshholdSlider.getValue())));
    	Platform.runLater(() ->threshhold_Inverse.setSelected(para.hole_fil_threshhold_Invers[4]));
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
    	if( obj == setVeriBtn1 && para.hole_DetectFlg[0]) {
    		onSetVeri_n=0;
    	}else if( obj == setVeriBtn2 && para.hole_DetectFlg[1]) {
    		onSetVeri_n=1;
	    }else if( obj == setVeriBtn3 && para.hole_DetectFlg[2]) {
			onSetVeri_n=2;
		}else if( obj == setVeriBtn4 && para.hole_DetectFlg[3]) {
			onSetVeri_n=3;
		}else {
			draggingRect = new Rectangle(0,0,1,1);
			eventTrigger = true;
			return;
		}

    	Rectangle r = para.hole_rects[onSetVeri_n];
    	draggingRect = (Rectangle)r.clone();


		Platform.runLater(() ->sliderDetecPara4.setValue(para.hole_circlePara4[onSetVeri_n]));
		Platform.runLater(() ->sliderDetecPara5.setValue(para.hole_circlePara5[onSetVeri_n]));
		Platform.runLater(() ->sliderDetecPara6.setValue(para.hole_circlePara6[onSetVeri_n]));
		Platform.runLater(() ->sliderDetecPara7.setValue(para.hole_circlePara7[onSetVeri_n]));
		Platform.runLater(() ->sliderDetecPara8.setValue(para.hole_circlePara8[onSetVeri_n]));
		Platform.runLater(() ->sliderDetecPara9.setValue(para.hole_circlePara9[onSetVeri_n]));
		Platform.runLater(() ->gauusianCheck.setSelected(para.hole_fil_gauusianCheck[onSetVeri_n]));
		Platform.runLater(() ->gauusianSliderX.setValue(para.hole_fil_gauusianX[onSetVeri_n]));
		Platform.runLater(() ->gauusianSliderY.setValue(para.hole_fil_gauusianY[onSetVeri_n]));
		Platform.runLater(() ->gauusianSliderK.setValue(para.hole_fil_gauusianValue[onSetVeri_n]));
		Platform.runLater(() ->dilateCheck.setSelected(para.hole_fil_dilateCheck[onSetVeri_n]));
		Platform.runLater(() ->dilateSliderN.setValue(para.hole_fil_dilateValue[onSetVeri_n]));
		Platform.runLater(() ->threshholdCheck.setSelected(para.hole_fil_threshholdCheck[onSetVeri_n]));
		Platform.runLater(() ->threshholdSlider.setValue(para.hole_fil_threshhold[onSetVeri_n]));
    	Platform.runLater(() ->threshholdLabel.setText(String.format("%.1f",threshholdSlider.getValue())));
    	Platform.runLater(() ->threshhold_Inverse.setSelected(para.hole_fil_threshhold_Invers[onSetVeri_n]));
    	Platform.runLater(() ->textFieldDetecPara4.setText(String.valueOf(String.format("%.1f",sliderDetecPara4.getValue()))));
    	Platform.runLater(() ->textFieldDetecPara5.setText(String.valueOf(String.format("%.1f",sliderDetecPara5.getValue()))));
    	Platform.runLater(() ->textFieldDetecPara6.setText(String.valueOf(String.format("%.1f",sliderDetecPara6.getValue()))));
    	Platform.runLater(() ->textFieldDetecPara7.setText(String.valueOf(String.format("%.1f",sliderDetecPara7.getValue()))));
    	Platform.runLater(() ->textFieldDetecPara8.setText(String.valueOf(String.format("%.1f",sliderDetecPara8.getValue()))));
    	Platform.runLater(() ->textFieldDetecPara9.setText(String.valueOf(String.format("%.1f",sliderDetecPara9.getValue()))));
    	//Platform.runLater(() ->matchTmempTHreshSlider.setValue(para.matchThreshValue[4]));
    	Platform.runLater(() ->threshholdLabel.setText(String.format("%.1f",threshholdSlider.getValue())));
		whiteRatioMaxSp.setValueFactory(
				new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 99999,para.hole_whiteAreaMax[onSetVeri_n],5));
		whiteRatioMinSp.setValueFactory(
				new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 99999,para.hole_whiteAreaMin[onSetVeri_n],5));


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

		pObj.dimensionDispChk =  dimensionDispChk.isSelected();
	    pObj.holeDispChk = holeDispChk.isSelected();
	    pObj.patternDispChk = patternDispChk.isSelected();

    	parameter para = pObj.para[pObj.select];
		para.hole_rects[4] =  (Rectangle)draggingRect.clone();
		para.hole_viewRect[0] = imgORG.getViewport().getMinX();
		para.hole_viewRect[1] = imgORG.getViewport().getMinY();
		para.hole_viewRect[2] = imgORG.getViewport().getWidth();
		para.hole_viewRect[3] = imgORG.getViewport().getHeight();
		para.hole_circlePara4[4] = sliderDetecPara4.getValue();
		para.hole_circlePara5[4] = sliderDetecPara5.getValue();
		para.hole_circlePara6[4] = sliderDetecPara6.getValue();
		para.hole_circlePara7[4] = sliderDetecPara7.getValue();
		para.hole_circlePara8[4] = sliderDetecPara8.getValue();
		para.hole_circlePara9[4] = sliderDetecPara9.getValue();
		para.hole_fil_gauusianCheck[4] = gauusianCheck.isSelected();
		para.hole_fil_gauusianX[4] = gauusianSliderX.getValue();
		para.hole_fil_gauusianY[4] = gauusianSliderY.getValue();
		para.hole_fil_gauusianValue[4] = gauusianSliderK.getValue();
		para.hole_fil_dilateCheck[4] = dilateCheck.isSelected();
		para.hole_fil_dilateValue[4] = (int)dilateSliderN.getValue();
		para.hole_zoom = zoomValue_slider.getValue();
		para.hole_fil_threshholdCheck[4] = threshholdCheck.isSelected();
		para.hole_fil_threshhold[4] = threshholdSlider.getValue();
		para.hole_fil_threshhold_Invers[4] = threshhold_Inverse.isSelected();

		para.ptm_Enable[0] = ptm_pt1_enable.isSelected();
		para.ptm_Enable[1] = ptm_pt2_enable.isSelected();
		para.ptm_Enable[2] = ptm_pt3_enable.isSelected();
		para.ptm_Enable[3] = ptm_pt4_enable.isSelected();

		para.dim_1_enable = dim_1_enable.isSelected();
		para.dim_2_enable = dim_2_enable.isSelected();
		para.dim_Enable[0] = dim_1_enable.isSelected();
		para.dim_Enable[1] = dim_1_enable.isSelected();
		para.dim_Enable[2] = dim_2_enable.isSelected();
		para.dim_Enable[3] = dim_2_enable.isSelected();

	    para.dim_offset_P2[0] = Double.valueOf(dim_offset_P2_1.getText());
	    para.dim_offset_F[0] = Double.valueOf(dim_offset_F_1.getText());
	    para.dim_offset_E[0] = Double.valueOf(dim_offset_E_1.getText());
	    para.dim_offset_P2[1] = Double.valueOf(dim_offset_P2_2.getText());
	    para.dim_offset_F[1] = Double.valueOf(dim_offset_F_2.getText());
	    para.dim_offset_E[1] = Double.valueOf(dim_offset_E_2.getText());

		//para.dimPixel_mm = dimSetting_offset.getText()

		pObj.portNo = portNoSpin.getValue().intValue();
		pObj.cameraID = camIDspinner.getValue().intValue();
		pObj.adc_thresh = Integer.valueOf(adc_thresh_value.getText());
		pObj.cameraHeight = Integer.valueOf(capH_text.getText());
		pObj.cameraWidth = Integer.valueOf(capW_text.getText());
		pObj.adcFlg = adc_flg.isSelected();

		para.delly = dellySpinner.getValue().intValue();
		para.trigger_2nd_chk = this.trigger_2nd_chk.isSelected();
		para.delly2 = dellySpinner2.getValue().intValue();

		objOut.writeObject(pObj);
		objOut.flush();
		objOut.close();

		//パターンマッチング画像の保存 ptmImgMat[preSetNo][ptm1～ptm4]
		for(int i=0;i<4;i++) {
			for(int j=0;j<4;j++) {
				if( ptm_ImgMat[i][j] != null ) {
					savePtmImg(ptm_ImgMat[i][j],"ptm"+String.format("_%d_%d", i,j));
				}
			}
		}
		//寸法測定用画像保存
		for(int i=0;i<4;i++) {
			for(int j=0;j<4;j++) {
				if( dim_ImgMat[i][j] != null ) {
					savePtmImg(dim_ImgMat[i][j],"dim"+String.format("_%d_%d", i,j));
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
        	Imgcodecs.imwrite(folder+"/" + fileName + ".png", imgMat);
        	Platform.runLater( () ->info2.appendText(folder+"/"+ fileName +".png"+"PTM画像保存"+"\n"));
        }catch(Exception e) {
        	Platform.runLater( () ->info2.appendText("PTM画像の保存に失敗"+e.toString()+"\n"));
        }
        Platform.runLater( () ->info2.appendText("PTM画像ファイルを保存\n" + fileName +".png\n"));

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
    	draggingRect = (Rectangle)para.hole_rects[4].clone();

    	viewOrgZoom = para.hole_zoom;
    	zoomValue_slider.setValue(viewOrgZoom);

    	imgORG.setViewport(new Rectangle2D(
    			para.hole_viewRect[0],para.hole_viewRect[1],para.hole_viewRect[2],para.hole_viewRect[3]));
    	imgGLAY.setViewport(new Rectangle2D(
    			para.hole_viewRect[0],para.hole_viewRect[1],para.hole_viewRect[2],para.hole_viewRect[3]));
    	imgGLAY1.setViewport(new Rectangle2D(
    			para.hole_viewRect[0],para.hole_viewRect[1],para.hole_viewRect[2],para.hole_viewRect[3]));
    	imgGLAY2.setViewport(new Rectangle2D(
    			para.hole_viewRect[0],para.hole_viewRect[1],para.hole_viewRect[2],para.hole_viewRect[3]));
    	imgGLAY3.setViewport(new Rectangle2D(
    			para.hole_viewRect[0],para.hole_viewRect[1],para.hole_viewRect[2],para.hole_viewRect[3]));
		whiteRatioMaxSp.setValueFactory(
				new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 99999,para.hole_whiteAreaMax[4],5));
		whiteRatioMinSp.setValueFactory(
				new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 99999,para.hole_whiteAreaMin[4],5));
    	setBtnPara();

    	portNoSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9,pObj.portNo,1));
    	dellySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 900,para.delly,1));
    	dellySpinner2.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 900,para.delly2,1));

    	camIDspinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9,pObj.cameraID,1));
    	adc_thresh_value.setText( String.valueOf(pObj.adc_thresh));
    	capH_text.setText( String.valueOf(pObj.cameraHeight));
    	capW_text.setText( String.valueOf(pObj.cameraWidth));
    	adc_flg.setSelected(pObj.adcFlg);

    	//パターンマッチング部
    	loadPtmImg();

        //パターンマッチング用パラメータ設定
    	ptm_patternMatchParaSet();


    	//寸法測定部
    	Platform.runLater( () ->dimSettingLabel.setText(String.format("%.0f μm/pixel", para.dimPixel_mm*1000)));
    	Platform.runLater( () ->dim_offset_P2_1.setText(String.valueOf(para.dim_offset_P2[0])));
    	Platform.runLater( () ->dim_offset_F_1.setText(String.valueOf(para.dim_offset_F[0])));
    	Platform.runLater( () ->dim_offset_E_1.setText(String.valueOf(para.dim_offset_E[0])));
    	Platform.runLater( () ->dim_offset_P2_2.setText(String.valueOf(para.dim_offset_P2[1])));
    	Platform.runLater( () ->dim_offset_F_2.setText(String.valueOf(para.dim_offset_F[1])));
    	Platform.runLater( () ->dim_offset_E_2.setText(String.valueOf(para.dim_offset_E[1])));

		Platform.runLater( () ->info2.appendText("設定がロードされました。\n"));

    }

    /**
     * パターンマッチング用パラメータ設定
     */
    private void ptm_patternMatchParaSet() {
    	parameter para = pObj.para[pObj.select];
        for(int i=0;i<ptm_tmpara.arrayCnt;i++) {
        	ptm_tmpara.matchingTreshDetectCnt[i] = para.ptm_fil_detectionCnt[i];
        	ptm_tmpara.matchingThresh[i] = para.ptm_threshValue[i];
        	ptm_tmpara.paternMat[i] = ptm_ImgMat[pObj.select][i];
        	ptm_tmpara.ptmEnable[i] = para.ptm_Enable[i];
        	ptm_tmpara.detectionRects[i] = para.ptm_rectsDetection[i];
        	ptm_tmpara.scale[i] = para.ptm_detectionScale[i];
        }
        ptm_tmpara.ptm_fil_gauusianCheck = para.ptm_fil_gauusianCheck;
        ptm_tmpara.ptm_fil_gauusianX = para.ptm_fil_gauusianX;
        ptm_tmpara.ptm_fil_gauusianY = para.ptm_fil_gauusianY;
        ptm_tmpara.ptm_fil_gauusianValue = para.ptm_fil_gauusianValue;
        ptm_tmpara.ptm_fil_threshholdCheck = para.ptm_fil_threshholdCheck;
        ptm_tmpara.ptm_fil_threshhold_Invers = para.ptm_fil_threshhold_Invers;
        ptm_tmpara.ptm_fil_threshholdValue = para.ptm_fil_threshholdValue;
        ptm_tmpara.ptm_fil_dilateCheck = para.ptm_fil_dilateCheck;
        ptm_tmpara.ptm_fil_dilateValue = para.ptm_fil_dilateValue;
        ptm_tmpara.ptm_fil_erodeCheck = para.ptm_fil_erodeCheck;
        ptm_tmpara.ptm_fil_erodeValue = para.ptm_fil_erodeValue;
        ptm_tmpara.ptm_fil_cannyCheck = para.ptm_fil_cannyCheck;
        ptm_tmpara.ptm_fil_cannyThresh1 = para.ptm_fil_cannyThresh1;
        ptm_tmpara.ptm_fil_cannyThresh2 = para.ptm_fil_cannyThresh2;
        ptm_tmpara.ptm_ptmMat_mask_rect = para.ptm_ptmMat_mask_rect;
        ptm_tmpara.createMaskMat();

        ptm_templateMatchingObj = new templateMatching(ptm_tmpara);
    }
    /**
     * 寸法測定用パターンマッチングパラメータ設定
     */
    private void dim_patternMatchParaSet() {
    	parameter para = pObj.para[pObj.select];
        for(int i=0;i<dim_tmpara.arrayCnt;i++) {
        	dim_tmpara.matchingTreshDetectCnt[i] = para.dim_fil_detectionCnt[i];
        	dim_tmpara.matchingThresh[i] = para.dim_threshValue[i];
        	dim_tmpara.paternMat[i] = dim_ImgMat[pObj.select][i];
        	dim_tmpara.ptmEnable[i] = para.dim_Enable[i];
        	dim_tmpara.detectionRects[i] = para.dim_rectsDetection[i];
        	dim_tmpara.scale[i] = para.dim_detectionScale[i];
        }
        dim_tmpara.ptm_fil_gauusianCheck = para.dim_fil_gauusianCheck;
        dim_tmpara.ptm_fil_gauusianX = para.dim_fil_gauusianX;
        dim_tmpara.ptm_fil_gauusianY = para.dim_fil_gauusianY;
        dim_tmpara.ptm_fil_gauusianValue = para.dim_fil_gauusianValue;
        dim_tmpara.ptm_fil_threshholdCheck = para.dim_fil_threshholdCheck;
        dim_tmpara.ptm_fil_threshhold_Invers = para.dim_fil_threshhold_Invers;
        dim_tmpara.ptm_fil_threshholdValue = para.dim_fil_threshholdValue;
        dim_tmpara.ptm_fil_dilateCheck = para.dim_fil_dilateCheck;
        dim_tmpara.ptm_fil_dilateValue = para.dim_fil_dilateValue;
        dim_tmpara.ptm_fil_erodeCheck = para.dim_fil_erodeCheck;
        dim_tmpara.ptm_fil_erodeValue = para.dim_fil_erodeValue;
        dim_tmpara.ptm_fil_cannyCheck = para.dim_fil_cannyCheck;
        dim_tmpara.ptm_fil_cannyThresh1 = para.dim_fil_cannyThresh1;
        dim_tmpara.ptm_fil_cannyThresh2 = para.dim_fil_cannyThresh2;
        dim_tmpara.ptm_ptmMat_mask_rect = para.dim_ptmMat_mask_rect;
        dim_tmpara.createMaskMat();

        dim_templateMatchingObj = new templateMatching(dim_tmpara);
    }

    /**
     * パターンマッチング画像のロード
     */
    private void loadPtmImg() {
    	File folder = new File("./ptm_image");
    	if( !folder.exists()) {
    		if( !folder.mkdir() ) {
    			Platform.runLater( () ->info2.appendText("ptm_imageフォルダの作成に失敗"+"\n"));
    			return;
    		}
    	}
    	for( int i=0;i<4;i++) {
    		for( int j=0;j<4;j++) {
    	    	Mat tmpMat = Imgcodecs.imread(
    	    			"./ptm_image/ptm"+String.format("_%d_%d", i,j)+".png",Imgcodecs.IMREAD_UNCHANGED);
    	    	if( tmpMat.width() > 0 &&  pObj.para[i].ptm_Enable[j]) {
    	    		 ptm_ImgMat[i][j] = new Mat();
    	    		 ptm_ImgMat[i][j] = tmpMat.clone();
    	    	}else {
    	    		ptm_ImgMat[i][j] = new Mat(100,100,CvType.CV_8UC3,new Scalar(0));
    	    	}
    		}
    	}
    	updateImageView(ptm_img1, Utils.mat2Image(ptm_ImgMat[pObj.select][0]));
    	updateImageView(ptm_img2, Utils.mat2Image(ptm_ImgMat[pObj.select][1]));
    	updateImageView(ptm_img3, Utils.mat2Image(ptm_ImgMat[pObj.select][2]));
    	updateImageView(ptm_img4, Utils.mat2Image(ptm_ImgMat[pObj.select][3]));
    	ptm_pt1_enable.setSelected(pObj.para[pObj.select].ptm_Enable[0]);
    	ptm_pt2_enable.setSelected(pObj.para[pObj.select].ptm_Enable[1]);
    	ptm_pt3_enable.setSelected(pObj.para[pObj.select].ptm_Enable[2]);
    	ptm_pt4_enable.setSelected(pObj.para[pObj.select].ptm_Enable[3]);

    	//寸法測定用画像
    	for( int i=0;i<4;i++) {
    		for( int j=0;j<4;j++) {
    	    	Mat tmpMat = Imgcodecs.imread(
    	    			"./ptm_image/dim"+String.format("_%d_%d", i,j)+".png",Imgcodecs.IMREAD_UNCHANGED);
    	    	if( tmpMat.width() > 0 && pObj.para[pObj.select].dim_rectsDetection[0].width>1) {
    	    		 dim_ImgMat[i][j] = new Mat();
    	    		 dim_ImgMat[i][j] = tmpMat.clone();
    	    	}else {
    	    		dim_ImgMat[i][j] = new Mat(100,100,CvType.CV_8UC3,new Scalar(0));
    	    	}
    		}
    	}
    	updateImageView(dim_okuriImg_1, Utils.mat2Image(dim_ImgMat[pObj.select][0]));
    	updateImageView(dim_poke_1, Utils.mat2Image(dim_ImgMat[pObj.select][1]));
    	updateImageView(dim_okuriImg_2, Utils.mat2Image(dim_ImgMat[pObj.select][2]));
    	updateImageView(dim_poke_2, Utils.mat2Image(dim_ImgMat[pObj.select][3]));

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
    	draggingRect = (Rectangle)pObj.para[pObj.select].hole_rects[4].clone();

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

    //ReTestボタン
    @FXML
    void onRetestBtn(ActionEvent event) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("ReTestImageViewer.fxml"));
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

    /**
     * 穴検出の解析
     * @param event
     */
    @FXML
    void onHoleAnalysisBtn(ActionEvent event) {

    }

	@FXML
    void onAllClear(ActionEvent event) {
    	ngCnt = 0;
    	allSaveCnt = 0;
    	Platform.runLater(() ->ngCounterLabel.setText(String.valueOf(ngCnt)));
    	Platform.runLater(() ->aPane.setStyle("-fx-background-radius: 0;-fx-background-color: #a5abb094;"));

    	Platform.runLater( () ->FileClass.fileClass(new File("./ng_image/")) );
    	//Platform.runLater( () ->FileClass.fileClass(new File("./ok_image/")) );

    	Platform.runLater(() ->this.imgNG.setImage(null));

    	Platform.runLater(() ->info2.clear());
    	Platform.runLater(() ->info2.setText(initInfo2));
    	//Platform.runLater(() ->info2.appendText("NG/OK画像ファイルを全て削除しました。\n"));
    	Platform.runLater(() ->info2.appendText("NG画像ファイルを全て削除しました。\n"));

    	//チャートデーターのクリア
    	for(int i=0;i<2;i++) {
    		int _i = i;
    		Platform.runLater( () ->dataset_F[_i].getSeries(0).clear());
    		Platform.runLater( () ->dataset_P2[_i].getSeries(0).clear());
    	}

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
		//寸法表示テーブルの更新
		Platform.runLater( () ->dim_table.getItems().get(0).P2Property().set(0.0));
		Platform.runLater( () ->dim_table.getItems().get(0).FProperty().set(0.0));
		Platform.runLater( () ->dim_table.getItems().get(0).EProperty().set(0.0));
		Platform.runLater( () ->dim_table.getItems().get(1).P2Property().set(0.0));
		Platform.runLater( () ->dim_table.getItems().get(1).FProperty().set(0.0));
		Platform.runLater( () ->dim_table.getItems().get(1).EProperty().set(0.0));
		P2_sum[0] = 0;P2_sum[1] = 0;
		F_sum[0] = 0;F_sum[1] = 0;
		E_sum[0] = 0;E_sum[1] = 0;

		shotCnt= 0;
    }

    @FXML
    void onSettingModeBtn(ActionEvent event) {
    	if( settingModeFlg ) {

    		Platform.runLater(() ->this.accordion_1.setDisable(true));
        	settingModeFlg = false;
        	Platform.runLater(() ->FilterViewMode.setSelected(false));
        	Platform.runLater(() ->info1.setText(""));
        	draggingRect = new Rectangle(1,1,1,1);
        	saveImgUseFlg = false;
        	updateImageView(imgGLAY, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
    	}else {
            //パターンマッチング用パラメータ設定
        	ptm_patternMatchParaSet();

    		Platform.runLater(() ->this.accordion_1.setDisable(false));
        	settingModeFlg = true;
        	lockedTimer = System.currentTimeMillis();
        	Platform.runLater(() ->FilterViewMode.setSelected(true));

    	}
    	eventTrigger = true;
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

		PtmView.arg_ptmMat = ptm_ImgMat[pObj.select][selectBtn].clone();

		PtmView.arg_ptmMat_mask_rect = para.ptm_ptmMat_mask_rect[selectBtn];
		PtmView.arg_ptm_templatRect = para.ptm_templatRect[selectBtn];

		PtmView.arg_detectionCnt = para.ptm_fil_detectionCnt[selectBtn];

		PtmView.arg_gauusianCheck = para.ptm_fil_gauusianCheck[selectBtn];
		PtmView.arg_gauusianSliderX = para.ptm_fil_gauusianX[selectBtn];
		PtmView.arg_gauusianSliderY = para.ptm_fil_gauusianY[selectBtn];
		PtmView.arg_gauusianSliderA = para.ptm_fil_gauusianValue[selectBtn];

		PtmView.arg_dilateCheck = para.ptm_fil_dilateCheck[selectBtn];
		PtmView.arg_dilateSliderN = para.ptm_fil_dilateValue[selectBtn];

		PtmView.arg_erodeCheck = para.ptm_fil_erodeCheck[selectBtn];
		PtmView.arg_erodeSliderN = para.ptm_fil_erodeValue[selectBtn];

		PtmView.arg_threshholdCheck = para.ptm_fil_threshholdCheck[selectBtn];
		PtmView.arg_threshhold_Inverse = para.ptm_fil_threshhold_Invers[selectBtn];
		PtmView.arg_threshholdSlider = para.ptm_fil_threshholdValue[selectBtn];//2値化閾値

		PtmView.arg_cannyCheck = para.ptm_fil_cannyCheck[selectBtn];
		PtmView.arg_cannyThresh1 = para.ptm_fil_cannyThresh1[selectBtn];
		PtmView.arg_cannyThresh2 = para.ptm_fil_cannyThresh2[selectBtn];

		PtmView.arg_ptmThreshSliderN = para.ptm_threshValue[selectBtn];//閾値
		PtmView.arg_zoomValue_slider = para.ptm_zoomValue_slider[selectBtn];
		PtmView.arg_rectsDetection =  para.ptm_rectsDetection[selectBtn];//検出範囲

		PtmView.arg_detectionScale = para.ptm_detectionScale[selectBtn];//検出倍率の逆数




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
			ptm_ImgMat[pObj.select][selectBtn] = PtmView.arg_ptmMat;
			para.ptm_ptmMat_mask_rect[selectBtn] = PtmView.arg_ptmMat_mask_rect;
			para.ptm_templatRect[selectBtn] = PtmView.arg_ptm_templatRect;

			updateImageView(iv, Utils.mat2Image(ptm_ImgMat[pObj.select][selectBtn]));

			para.ptm_fil_detectionCnt[selectBtn] = PtmView.arg_detectionCnt;

			para.ptm_fil_gauusianCheck[selectBtn] = PtmView.arg_gauusianCheck;
			para.ptm_fil_gauusianX[selectBtn] = PtmView.arg_gauusianSliderX;
			para.ptm_fil_gauusianY[selectBtn]  = PtmView.arg_gauusianSliderY;
			para.ptm_fil_gauusianValue[selectBtn] = PtmView.arg_gauusianSliderA;

			para.ptm_fil_dilateCheck[selectBtn] = PtmView.arg_dilateCheck;
			para.ptm_fil_dilateValue[selectBtn] = PtmView.arg_dilateSliderN;

			para.ptm_fil_erodeCheck[selectBtn] = PtmView.arg_erodeCheck;
			para.ptm_fil_erodeValue[selectBtn] = PtmView.arg_erodeSliderN;

			para.ptm_fil_threshholdCheck[selectBtn] = PtmView.arg_threshholdCheck;
			para.ptm_fil_threshhold_Invers[selectBtn] = PtmView.arg_threshhold_Inverse;
			para.ptm_fil_threshholdValue[selectBtn] = PtmView.arg_threshholdSlider;//2値化閾値

			para.ptm_fil_cannyCheck[selectBtn] = PtmView.arg_cannyCheck;
			para.ptm_fil_cannyThresh1[selectBtn] = PtmView.arg_cannyThresh1;
			para.ptm_fil_cannyThresh2[selectBtn] = PtmView.arg_cannyThresh2;

			para.ptm_threshValue[selectBtn] = PtmView.arg_ptmThreshSliderN;//閾値
			para.ptm_zoomValue_slider[selectBtn] = PtmView.arg_zoomValue_slider;
			para.ptm_rectsDetection[selectBtn] =  PtmView.arg_rectsDetection;//検出範囲
			para.ptm_detectionScale[selectBtn] = PtmView.arg_detectionScale;//検出倍率の逆数
		}
        //パターンマッチング用パラメータ設定
    	ptm_patternMatchParaSet();

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
    		ptm_ImgMat[pObj.select][0] = roi;
    	}else if( eObject == this.ptm_set_pt2 ) {
    		updateImageView(ptm_img2, Utils.mat2Image(roi));
    		ptm_ImgMat[pObj.select][1] = roi;
    	}else if( eObject == this.ptm_set_pt3 ) {
    		updateImageView(ptm_img3, Utils.mat2Image(roi));
    		ptm_ImgMat[pObj.select][2] = roi;
    	}else if( eObject == this.ptm_set_pt4 ) {
    		updateImageView(ptm_img4, Utils.mat2Image(roi));
    		ptm_ImgMat[pObj.select][3] = roi;
    	}
        //パターンマッチング用パラメータ設定
    	ptm_patternMatchParaSet();

    }

    @FXML
    void onPtmEnabeChk(MouseEvent event) {
    	ptm_templateMatchingObj.tmpara.ptmEnable[0] = this.ptm_pt1_enable.isSelected();
    	ptm_templateMatchingObj.tmpara.ptmEnable[1] = this.ptm_pt2_enable.isSelected();
    	ptm_templateMatchingObj.tmpara.ptmEnable[2] = this.ptm_pt3_enable.isSelected();
    	ptm_templateMatchingObj.tmpara.ptmEnable[3] = this.ptm_pt4_enable.isSelected();
    }

    @FXML
    void onDimEnabeChk(MouseEvent event) {
    	dim_templateMatchingObj.tmpara.ptmEnable[0] = this.dim_1_enable.isSelected();
    	dim_templateMatchingObj.tmpara.ptmEnable[1] = this.dim_1_enable.isSelected();
    	dim_templateMatchingObj.tmpara.ptmEnable[2] = this.dim_2_enable.isSelected();
    	dim_templateMatchingObj.tmpara.ptmEnable[3] = this.dim_2_enable.isSelected();

    }
    //寸法測定メソッド群
    //登録パターンなどの詳細設定
    @FXML
    void onDimSetPara(ActionEvent e) {
    	Button obj = (Button)e.getSource();
    	ImageView iv;
    	int selectBtn;
    	if( obj == dim_set_para1 ) {
    		selectBtn = 0;
    		iv = dim_okuriImg_1;
    	}else if( obj == dim_set_para2 ) {
    		selectBtn = 1;
    		iv = dim_poke_1;
    	}else if( obj == dim_set_para3 ) {
    		selectBtn = 2;
    		iv = dim_okuriImg_2;
    	}else if( obj == dim_set_para4 ) {
    		selectBtn = 3;
    		iv = dim_poke_2;
    	}else {
    		return;
    	}

    	parameter para = pObj.para[pObj.select];

		//パラメーターを渡す
		PtmView.ptmSrcMat = srcMat.clone();

		PtmView.arg_ptmMat = dim_ImgMat[pObj.select][selectBtn].clone();

		PtmView.arg_ptmMat_mask_rect = para.dim_ptmMat_mask_rect[selectBtn];
		PtmView.arg_ptm_templatRect = para.dim_templatRect[selectBtn];

		PtmView.arg_detectionCnt = para.dim_fil_detectionCnt[selectBtn];

		PtmView.arg_gauusianCheck = para.dim_fil_gauusianCheck[selectBtn];
		PtmView.arg_gauusianSliderX = para.dim_fil_gauusianX[selectBtn];
		PtmView.arg_gauusianSliderY = para.dim_fil_gauusianY[selectBtn];
		PtmView.arg_gauusianSliderA = para.dim_fil_gauusianValue[selectBtn];

		PtmView.arg_dilateCheck = para.dim_fil_dilateCheck[selectBtn];
		PtmView.arg_dilateSliderN = para.dim_fil_dilateValue[selectBtn];

		PtmView.arg_erodeCheck = para.dim_fil_erodeCheck[selectBtn];
		PtmView.arg_erodeSliderN = para.dim_fil_erodeValue[selectBtn];

		PtmView.arg_threshholdCheck = para.dim_fil_threshholdCheck[selectBtn];
		PtmView.arg_threshhold_Inverse = para.dim_fil_threshhold_Invers[selectBtn];
		PtmView.arg_threshholdSlider = para.dim_fil_threshholdValue[selectBtn];//2値化閾値

		PtmView.arg_cannyCheck = para.dim_fil_cannyCheck[selectBtn];
		PtmView.arg_cannyThresh1 = para.dim_fil_cannyThresh1[selectBtn];
		PtmView.arg_cannyThresh2 = para.dim_fil_cannyThresh2[selectBtn];

		PtmView.arg_ptmThreshSliderN = para.dim_threshValue[selectBtn];//閾値
		PtmView.arg_zoomValue_slider = para.dim_zoomValue_slider[selectBtn];
		PtmView.arg_rectsDetection =  para.dim_rectsDetection[selectBtn];//検出範囲

		PtmView.arg_detectionScale = para.dim_detectionScale[selectBtn];//検出倍率の逆数

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
			dim_ImgMat[pObj.select][selectBtn] = PtmView.arg_ptmMat;
			updateImageView(iv, Utils.mat2Image(dim_ImgMat[pObj.select][selectBtn]));
			para.dim_ptmMat_mask_rect[selectBtn] = PtmView.arg_ptmMat_mask_rect;
			para.dim_templatRect[selectBtn] = PtmView.arg_ptm_templatRect;

			para.dim_fil_detectionCnt[selectBtn] = PtmView.arg_detectionCnt;

			para.dim_fil_gauusianCheck[selectBtn] = PtmView.arg_gauusianCheck;
			para.dim_fil_gauusianX[selectBtn] = PtmView.arg_gauusianSliderX;
			para.dim_fil_gauusianY[selectBtn]  = PtmView.arg_gauusianSliderY;
			para.dim_fil_gauusianValue[selectBtn] = PtmView.arg_gauusianSliderA;

			para.dim_fil_dilateCheck[selectBtn] = PtmView.arg_dilateCheck;
			para.dim_fil_dilateValue[selectBtn] = PtmView.arg_dilateSliderN;

			para.dim_fil_erodeCheck[selectBtn] = PtmView.arg_erodeCheck;
			para.dim_fil_erodeValue[selectBtn] = PtmView.arg_erodeSliderN;

			para.dim_fil_threshholdCheck[selectBtn] = PtmView.arg_threshholdCheck;
			para.dim_fil_threshhold_Invers[selectBtn] = PtmView.arg_threshhold_Inverse;
			para.dim_fil_threshholdValue[selectBtn] = PtmView.arg_threshholdSlider;//2値化閾値

			para.dim_fil_cannyCheck[selectBtn] = PtmView.arg_cannyCheck;
			para.dim_fil_cannyThresh1[selectBtn] = PtmView.arg_cannyThresh1;
			para.dim_fil_cannyThresh2[selectBtn] = PtmView.arg_cannyThresh2;

			para.dim_threshValue[selectBtn] = PtmView.arg_ptmThreshSliderN;//閾値
			para.dim_zoomValue_slider[selectBtn] = PtmView.arg_zoomValue_slider;
			para.dim_rectsDetection[selectBtn] =  PtmView.arg_rectsDetection;//検出範囲
			para.dim_detectionScale[selectBtn] = PtmView.arg_detectionScale;//検出倍率の逆数
		}
		dim_patternMatchParaSet();
    }
    //測定設定 送り穴間距離
    @FXML
    void onDimSetting(ActionEvent event) {
    	if( holeDist_DimSetting > 0) {
	    	Double pixel_mm = 0.0;
	    	try {
	    		pixel_mm = 4 / holeDist_DimSetting + Double.valueOf(dimSetting_offset.getText())/1000.0;
	    	}catch( java.lang.NumberFormatException e) {
	    		pixel_mm = 0.0;
	    	}
	    	final Double tmp_pixel_mm = pixel_mm;
	    	Platform.runLater( () ->this.dimSettingLabel.setText(String.format("%.0f μm/pixel", tmp_pixel_mm*1000)));
	    	pObj.para[pObj.select].dimPixel_mm = pixel_mm;
    	}
    }

    @FXML
    /**
     * グラフ表示
     * @param event
     */
    void onDimensionBtn(ActionEvent event) {

    }

    @FXML
    /**
     * カメラ露出取得
     * @param event
     */
    void onGetExpro(ActionEvent event) {
		double exp = capObj.get(Videoio.CAP_PROP_EXPOSURE);
		Platform.runLater( () ->this.cameraExpro.setText(String.valueOf(exp)));
    }

    @FXML
    /**
     * カメラ露出設定
     * @param event
     */
    void onSetExpro(ActionEvent event) {
    	double exp = Double.valueOf(cameraExpro.getText());
    	capObj.set(Videoio.CAP_PROP_EXPOSURE,exp);
    }
    @FXML
    /**
     * カメラゲイン取得
     * @param event
     */
    void onGetGain(ActionEvent event) {
		double gain = capObj.get(Videoio.CAP_PROP_GAIN);
		Platform.runLater( () ->this.cameraGain.setText(String.valueOf(gain)));
    }

    @FXML
    /**
     * カメラゲイン設定
     * @param event
     */
    void onSetGain(ActionEvent event) {
    	double gain = Double.valueOf(cameraGain.getText());
    	capObj.set(Videoio.CAP_PROP_GAIN,gain);
    }

    @FXML
    /**
     * パターンマッチング強制無効/有効
     * @param event
     */
    void onPtm_disableChk(ActionEvent event) {

    }

    /**
     * 設定初期化
     * @param event
     */
    @FXML
    void onParaInitBtn(ActionEvent event) {
    	pObj = new preSet();
    }

    /**
     * 寸法オフセットのチェンジリスナー
     * @param event
     */
    @FXML
    void onDimOffsetChange(KeyEvent  e) {
    	String p2_offset_1 = dim_offset_P2_1.getText();
    	String f_offset_1 = dim_offset_F_1.getText();
    	String p2_offset_2 = dim_offset_P2_2.getText();
    	String f_offset_2 = dim_offset_F_2.getText();

    	try {
    		double d_p2_offset_1 = Double.valueOf(p2_offset_1);
    		double d_f_offset_1 = Double.valueOf(f_offset_1);
    		double d_p2_offset_2 = Double.valueOf(p2_offset_2);
    		double d_f_offset_2 = Double.valueOf(f_offset_2);

    		parameter para = pObj.para[pObj.select];
    		para.dim_offset_P2[0] = d_p2_offset_1;
    		para.dim_offset_P2[1] = d_p2_offset_2;
    		para.dim_offset_F[0] = d_f_offset_1;
    		para.dim_offset_F[1] = d_f_offset_2;
    	}catch(Exception e2) {
    		Platform.runLater( () ->this.info2.appendText("offsetは数値で入力してください\n"));
    	}
    }


    /**
     * セッティングモード時　パラメータを設定するショットパラを選択する
     * @param event
     */
    @FXML
    void onPara_12st_shot_change(ActionEvent event) {
    	if(targetSetParaNO == 1) {
    		targetSetParaNO = 2;
        	Platform.runLater( () ->para_12st_shot.setText("2nd."));
        	Platform.runLater( () ->para_12st_shot1.setText("2nd."));
        	Platform.runLater( () ->para_12st_shot11.setText("2nd."));
        	Platform.runLater( () ->para_12st_shot.setSelected(true));
        	Platform.runLater( () ->para_12st_shot1.setSelected(true));
        	Platform.runLater( () ->para_12st_shot11.setSelected(true));

    	}else{
    		targetSetParaNO = 1;
        	Platform.runLater( () ->para_12st_shot.setText("1st."));
        	Platform.runLater( () ->para_12st_shot1.setText("1st."));
        	Platform.runLater( () ->para_12st_shot11.setText("1st."));
        	Platform.runLater( () ->para_12st_shot.setSelected(false));
        	Platform.runLater( () ->para_12st_shot1.setSelected(false));
        	Platform.runLater( () ->para_12st_shot11.setSelected(false));
    	}


    }

    @FXML
    void initialize() {

        //クラス変数の初期化
        rects = Collections.synchronizedList(new ArrayList<>());
        draggingRect = new Rectangle(0, 0,1,1);
        moveDraggingPoint[0] = new Point();//ドラッグ移動始点
        moveDraggingPoint[1] = new Point();//ドラッグ移動終点
        dragging = false;
        moveDragingFlg = false;
		Platform.runLater(() ->aPane.setStyle("-fx-background-radius: 0;-fx-background-color: #a5abb094"));
        //チャート生成
        chart_P2 = new JFreeChart[2];
        chartTab_P2 = new Tab[2];
        chart_F = new JFreeChart[2];
        chartTab_F = new Tab[2];
        dataset_P2 = new XYSeriesCollection[2];
        dataset_F = new XYSeriesCollection[2];
        chartFact();
        //寸法表示用テーブル
        dim_table_item.setCellValueFactory(new PropertyValueFactory<Dim_itemValue,String>("item"));
        dim_table_P2.setCellValueFactory(new PropertyValueFactory<Dim_itemValue,Double>("P2"));
        dim_table_F.setCellValueFactory(new PropertyValueFactory<Dim_itemValue,Double>("F"));
        dim_table_E.setCellValueFactory(new PropertyValueFactory<Dim_itemValue,Double>("E"));
   		Platform.runLater( () ->dim_table.getItems().clear());
		Platform.runLater( () ->dim_table.getItems().add(new Dim_itemValue("①列",0.0,0.0,0.0)));
		Platform.runLater( () ->dim_table.getItems().add(new Dim_itemValue("ave.",0.0,0.0,0.0)));
		Platform.runLater( () ->dim_table.getItems().add(new Dim_itemValue("②列",0.0,0.0,0.0)));
		Platform.runLater( () ->dim_table.getItems().add(new Dim_itemValue("ave.",0.0,0.0,0.0)));

		//イニシャルinfo2の内容保存
		initInfo2 = this.info2.getText();

		loadAllPara();
		dim_patternMatchParaSet();

        updateImageView(imgGLAY1, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
        updateImageView(imgGLAY2, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
        updateImageView(imgGLAY3, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
        updateImageView(imgGLAY, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
    	Platform.runLater(() ->info1.setText(""));
    	Platform.runLater(() ->accordion_1.setDisable(true));

        accordion_1.expandedPaneProperty().addListener(new
                ChangeListener<TitledPane>() {
                    public void changed(ObservableValue<? extends TitledPane> ov,
                        TitledPane old_val, TitledPane new_val) {
                    	if( new_val == ptm_setting_accordion) {
                    		onOpenPtmAccordion(null);
                    	}else if(old_val == ptm_setting_accordion ) {
                    		ptmSetStartFlg = false;
                        	parameter para = pObj.para[pObj.select];
                        	para.ptm_Enable[0] = ptm_pt1_enable.isSelected();
                        	para.ptm_Enable[1] = ptm_pt2_enable.isSelected();
                        	para.ptm_Enable[2] = ptm_pt3_enable.isSelected();
                        	para.ptm_Enable[3] = ptm_pt4_enable.isSelected();
                        	ptm_patternMatchParaSet();
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
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

        onAllClear(null);
    }

    private void chartFact() {
        for(int i=0;i<2;i++) {
        	dataset_P2[i] = getChartData_P2();
	        chart_P2[i] = createInitChart(String.valueOf(i+1)+"列目 P2","(mm)","n",dataset_P2[i] ,1.8,2.2);
	        ChartViewer chV = new ChartViewer(chart_P2[i]);
	        chV.addChartMouseListener( new ChartMouseListenerFX() {
					@Override
					public void chartMouseClicked(ChartMouseEventFX e) {
						XYPlot xyplot = e.getChart().getXYPlot();
						double value = xyplot.getRangeCrosshairValue();
						Platform.runLater(() ->dimChartValueLable.setText( String.format("%.2f", value)));
					}

					@Override
					public void chartMouseMoved(ChartMouseEventFX e) {
					}
	        	}
	        );
			chartTab_P2[i] = new Tab(String.valueOf(i+1)+"列目 P2     ",chV);

			dataset_F[i] = getChartData_F();
	        chart_F[i] = createInitChart(String.valueOf(i+1)+"列目 F","(mm)","n",dataset_F[i],11.3,11.7);
	        ChartViewer chV2 = new ChartViewer(chart_F[i]);
	        chV2.addChartMouseListener( new ChartMouseListenerFX() {
					@Override
					public void chartMouseClicked(ChartMouseEventFX e) {
						XYPlot xyplot = e.getChart().getXYPlot();
						double value = xyplot.getRangeCrosshairValue();
						Platform.runLater(() ->dimChartValueLable.setText( String.format("%.2f", value)));
					}

					@Override
					public void chartMouseMoved(ChartMouseEventFX e) {
					}
	        	}
	        );
	        chartTab_F[i] = new Tab(String.valueOf(i+1)+"列目 F      ",chV2);

			dataTabpane.getTabs().add(chartTab_P2[i]);
	        dataTabpane.getTabs().add(chartTab_F[i]);
        }
    }
    /**
     * グラフの雛形作成
     * @param title
     * @param valueAxisLabel
     * @param categoryAxisLabel
     * @return
     */
    private JFreeChart createInitChart(String title,String valueAxisLabel,
    							String categoryAxisLabel,XYSeriesCollection dataset,double lower,double upper){
    	JFreeChart chart = ChartFactory.createXYLineChart(title,categoryAxisLabel,valueAxisLabel,
                dataset,//データーセット
                PlotOrientation.VERTICAL,//値の軸方向
                false,//凡例
                false,//tooltips
                false);//urls
        // 背景色を設定
    	chart.setBackgroundPaint(ChartColor.WHITE);

        // 凡例の設定
        //LegendTitle lt = chart.getLegend();
        //lt.setFrame(new BlockBorder(1d, 2d, 3d, 4d, ChartColor.WHITE));

        XYPlot plot = (XYPlot) chart.getPlot();
        // 背景色
        plot.setBackgroundPaint(ChartColor.gray);
        // 背景色 透明度
        plot.setBackgroundAlpha(0.5f);
        // 前景色 透明度
        plot.setForegroundAlpha(0.5f);
        // 縦線の色
        plot.setDomainGridlinePaint(ChartColor.white);
        // 横線の色
        plot.setRangeGridlinePaint(ChartColor.white);
        // カーソル位置で横方向の補助線をいれる
        plot.setDomainCrosshairVisible(true);
        // カーソル位置で縦方向の補助線をいれる
        plot.setRangeCrosshairVisible(true);
        // 横軸の設定
        NumberAxis xAxis = (NumberAxis)plot.getDomainAxis();
        xAxis.setAutoRange(false);
        xAxis.setRange(1,200);
        // 縦軸の設定
        NumberAxis yAxis = (NumberAxis)plot.getRangeAxis();
        yAxis.setAutoRange(false);
        yAxis.setRange(lower,upper);

        // プロットをつける
        XYLineAndShapeRenderer  renderer = new XYLineAndShapeRenderer(true,false);
        plot.setRenderer(renderer);
        //renderer.setDefaultShapesVisible(true);
        //renderer.setDefaultShapesFilled(true);
        //プロットのサイズ
        Stroke stroke = new BasicStroke(1.0f);
        renderer.setSeriesStroke(0, stroke);
		//色
        renderer.setSeriesPaint(0, ChartColor.BLUE);

        /*
        // プロットに値を付ける
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(2);
        XYItemLabelGenerator generator =
            new StandardXYItemLabelGenerator(
                StandardXYItemLabelGenerator.DEFAULT_ITEM_LABEL_FORMAT,
                format, format);
        renderer.setDefaultItemLabelGenerator(generator);
        renderer.setDefaultItemLabelsVisible(true);
         */
        return chart;

    }
    private XYSeriesCollection getChartData_P2(){
        XYSeriesCollection p2_dataset = new XYSeriesCollection();
        XYSeries p2_series = new XYSeries("P2");
        p2_dataset.addSeries(p2_series);

        return p2_dataset;
    }
    private XYSeriesCollection getChartData_F(){
        XYSeriesCollection f_dataset = new XYSeriesCollection();
        XYSeries f_series = new XYSeries("F");
        f_dataset.addSeries(f_series);

        return f_dataset;
    }
    private XYSeriesCollection getChartData_E(){
        XYSeriesCollection e_dataset = new XYSeriesCollection();
        XYSeries e_series = new XYSeries("E");
        e_dataset.addSeries(e_series);

        return e_dataset;
    }

}
