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
import java.util.Iterator;
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
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
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
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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
import javafx.stage.Modality;
import javafx.stage.Stage;


public class VisonController2{

	//デバッグフラグ
	public static boolean debugFlg = false;

	//ログデーター
	public logData logdata = new logData();
	//検査したカウント数
	public int shotCnt = 0;
	//シャッター間隔が4秒以上あいた時にセットされる
	boolean shutterSignal4secInterval=false;
	//シャッター間隔が16秒以上あいた時にセットされる
	boolean intervalTime16secFlg;

	//目標輝度設定値
	double autoGain_target = 137;//#55 137 その他100

	//保存される画像の最大数
	final int saveMax_all = 255;
	final int saveMax_ng = 400;
	int allSaveCnt = 0;
	//判定ＮＧとなった回数
	int ngCnt = 0;
	//NG信号を継続発信させるフラグ
	boolean ngSignalKeizokuFlg = false;;


	public static Mat srcMat = new Mat();//保存画像を使用した設定に使用する為publicにしておく
	Mat dstframe = new Mat();//srcMatをカメラキャリブレーションデーターから変換したオブジェクトが入る
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
    //照明キャリブレーション用
	private static ScheduledExecutorService timerCalib;
	private boolean calibLiteFlg;//キャリブレーション中か？
	Mat realMat;//スルー画像


	//穴面積判定用
	private long whiteAreaAverage;
	private long whiteAreaMax;
	private long whiteAreaMin;

	//シャッタートリガ用
	boolean shutterFlg = false;
	boolean offShutterFlg = false;
	double ngTriggerTime = 0;
	double triggerDelly = 0;

	//カメラキャプチャ異常フラグ
	boolean cameraCaptureFlag;

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
	double framCnt = 0;
	VideoCapture source_video;
	//設定自動ロック用
	boolean settingModeFlg = false;
	long lockedTimer = 0;
	final long lockedTimerThresh = 1000 * 60 *15;
	//パターンマッチング用
	public Mat[][] ptm_ImgMat = new Mat[4][parameter.ptm_arrySize];//[presetNo][ptm1～ptm4]
	private TMpara ptm_tmpara;
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

	//2022.08.16 クリア信号ノイズ入力回避用
	boolean biginClearSignal = false;
	long clearSignalTime = System.currentTimeMillis();
	long exeAllClearTime = System.currentTimeMillis();//オールクリアが信号で実行された時刻を保持



	//インフォメーション
	private String initInfo2;

	//チャート
	private Tab[] chartTab_P2;
	private Tab[] chartTab_F;
	private Tab[] chartTab_E;
	private JFreeChart[] chart_P2;
	private JFreeChart[] chart_F;
	private JFreeChart[] chart_E;
	public Mat[][] dim_ImgMat = new Mat[4][5];//[presetNo][dim1～dim5]
	private TMpara dim_tmpara = new TMpara(5);
	private templateMatching dim_templateMatchingObj;
	private double[] P2_sum = new double[2];
	private double[] F_sum = new double[2];
	private double[] E_sum = new double[2];
	//寸法外れ判定用 5ショットの平均 2022.08.16追加 E寸は2023.10.06追加
	private int hantei_cnt=0;
	private double[][] P2_hantei = new double[2][5];
	private double[][] F_hantei = new double[2][5];
	private double[][] E_hantei = new double[2][5];
	private boolean sunpou_hantei_NG_5Shot = false;
	private boolean sunpou_hantei_NG_now = false;

	//トリガディレイ
	@FXML
    private Spinner<Integer> dellySpinner;

    @FXML
    private Label sokuteiTimeLabel;//rePaint()計測時間表示

    @FXML
    private CheckBox camera_revers_chk;//カメラ上下反転

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

    @FXML
    private Label presetText;//選択されている品種

    //パターンマッチング関係
    @FXML
    private TitledPane ptm_setting_accordion;
    @FXML
    private Button ptm_set_para;
    @FXML
    private CheckBox ptm_pt_enable;
    @FXML
    private CheckBox ptm_disableChk;
    @FXML
    private Spinner<Integer> ptm_selectNo;
    @FXML
    private TextArea ptm_textArea;
    @FXML
    private ImageView ptm_img;

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
    //イメージビュー
    @FXML
    private ImageView dim_okuriImg_1;
    @FXML
    private ImageView dim_poke_1;
    @FXML
    private ImageView dim_okuriImg_2;
    @FXML
    private ImageView dim_poke_2;
    @FXML
    private ImageView dim_edge_1;//E寸用
    //詳細設定ボタン
    @FXML
    private Button dim_set_para1;
    @FXML
    private Button dim_set_para2;
    @FXML
    private Button dim_set_para3;
    @FXML
    private Button dim_set_para4;
    @FXML
    private Button dim_set_para5;//E寸用
    //測定有効無効チェックボックス
    @FXML
    private CheckBox dim_1_enable;
    @FXML
    private CheckBox dim_2_enable;
    //寸法オフセットテキストフィールド
    @FXML
    private TextField dim_offset_F_1;
    @FXML
    private TextField dim_offset_P2_1;
    @FXML
    private TextField dim_offset_E_1;
    @FXML
    private TextField dim_offset_F_2;
    @FXML
    private TextField dim_offset_P2_2;
    //穴間隔換算距離反映ボタン
    @FXML
    private Button dimSettingBtn;
    //換算距離オフセットテキストフィールド
    @FXML
    private TextField dimSetting_offset;
    //換算距離表示ラベル
    @FXML
    private Label dimSettingLabel;
    //E寸異常警告ラベル
    @FXML
    private Label E_warningLabel;

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
    @FXML
    private ToggleButton  trgTimingCalib;
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
	private XYSeriesCollection[] dataset_E;

	private double holeDist_DimSetting;

	private int targetSetParaNO = 1;

    @FXML
    private Button setting_csv_out;

    @FXML
    private Button calib_btn;//照明キャリブレーションボタン
    @FXML
    private Label calib_label;//キャリブレーション用輝度平均表示
    @FXML
    private TextField targetLuminance;//目標輝度設定値

    @FXML
    private ComboBox<String> selectPreSetCB;//品種選択用

    @FXML
    private CheckBox autoGainChk;//オートゲインチェックボックス　※デフォルトはselected状態
    @FXML
    private TextField autoGainText;//オートゲインテキスト
    @FXML
    private TitledPane TiledPaneHardSET;//設定アコーディオンの初期ペイン

	private boolean trgCalibFlg;//トリガタイミング自動調整中はTrue
	private long trgCalibTime;//トリガタイミング自動調整開始時刻
	private int trgCalibCnt;//トリガタイミング自動調整測定ショット数

	private boolean autoGainEnable;

	private int DispersionErrorFlg = 0;//分散閾値NG連続回数

	private boolean DispersionErrorNG;


    /**
     * 品種の選択
     * @param event
     */
    @FXML
    void onSelectPreset(ActionEvent event) {
    	System.out.println("品種の選択コンボボックス操作");

    }

    /**
     * スライダーの値を対応するテキストフィールドに入力する
     * @param event
     */
    @FXML
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
		double exp_auto = capObj.get(Videoio.CAP_PROP_AUTO_EXPOSURE);//露出の自動調整
		double gain = capObj.get(Videoio.CAP_PROP_GAIN);
		Platform.runLater( () ->info2.appendText("\n"+ "カメラ解像度 WIDTH="+ wset+
				"\n カメラ解像度 HEIGHT= " +hset + "\n"+
				"\n 露出="+exp+"\n"+
				"\n 自動露出設定="+exp_auto+"\n"+
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

    /**
     * メインループ　初期設定からトリガ取得など
     * @param event
     * @throws InterruptedException
     */
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
			capObj.set(Videoio.CAP_PROP_AUTO_EXPOSURE,0);//2023.11.22追加露出自動設定オフ
			//ゲインの取得
			capObj.set(Videoio.CAP_PROP_GAIN,50);//起動時強制的にゲイン50%
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
		source_video = new VideoCapture("./test17.mp4" );//デモモード用動画
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
			    		//TriggerLoopから移動2022.11.10
			    		try {
							//PLCからのシャッター信号をオンディレーする
							Thread.sleep( dellySpinner.getValue() );//←コントラストを取得して自動調整のロジックをいれたい　最大-中間-最小の中間を求める
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						srcMat = grabFrame();
				    	if( srcMat.width() > 0 ) {
				    		rePaint();
				    		cameraCaptureFlag = true;//成功
				    	}else {
				    		Platform.runLater( () ->info2.appendText(("カメラから画像の取得に失敗\n")));
				    		cameraCaptureFlag = false;//失敗
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
			Gpio.ngSignalON();
			//Gpio.OkSignalON();//#55テストコード
			Platform.runLater( () ->GPIO_STATUS_PIN3.setFill(Color.RED));
			//Platform.runLater( () ->GPIO_STATUS_PIN3.setFill(Color.BLUE));//#55テストコード
			//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			//GPIOからのトリガ信号を受信するループ
			//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			Runnable triggerLoop = new Runnable() {
				String rt = "-1";//nullを避ける為-1をいれておく
				long debugCnt = 0;
				private long shutterSignalIntervalTime=System.currentTimeMillis();
				private boolean clealFlg = false;

				@Override
				public void run() {
					try {
						if( debugFlg ) {
							System.out.println("GPIO READ/WRITE" + debugCnt);
							debugCnt++;
						}
						//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
						//シャッター信号の間隔がn秒以上開いた場合
						//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
						long intervalTime = System.currentTimeMillis() - shutterSignalIntervalTime ;//シャッター間隔計算
						if( !shutterSignal4secInterval && 	intervalTime > 1000*4 && shotCnt>5850) {//ショットが5850を超えている場合
							shutterSignal4secInterval = true;
							shotCnt=0;
							autoGainEnable = false;
							Gpio.ngSignalON();
							Platform.runLater( () ->GPIO_STATUS_PIN3.setFill(Color.RED));
				    		//油付着の警告
				    		if(DispersionErrorNG) {
				    	        // ダイアログの表示
				    	        // Alertダイアログの利用
				    			/*
				    	        Alert alert = new Alert( AlertType.NONE , "" , ButtonType.OK ,
				    	                                                                           ButtonType.YES ,
				    	                                                                           ButtonType.NO ,
				    	                                                                           ButtonType.NEXT ,
				    	                                                                           ButtonType.PREVIOUS ,
				    	                                                                           ButtonType.FINISH ,
				    	                                                                           ButtonType.APPLY ,
				    	                                                                           ButtonType.CANCEL ,
				    	                                                                           ButtonType.CLOSE );
				    	        alert.setTitle( "油付着警告" );
				    	        alert.getDialogPane().setHeaderText( "油付着警告" );
				    	        alert.getDialogPane().setContentText( "油付着の可能性が高い画像が連続３回検出されました。無確認で継続生産を禁止します。！！操作は記録されます。！！" );
				    	        alert.showAndWait();
				    	        */
				    		}
							logdata.csvWrite();
							logdata.clear();
							//シャッタートリガ受信インジケーター色変更
				    		Platform.runLater( () ->GPIO_STATUS_PIN0.setFill(Color.WHITE));
				    		//メッセージを表示 2023.01.27
				    		Platform.runLater(() ->info2.appendText("シャッター間隔が4秒以上ありました。強制的にNG信号を発信します"));
						}else if(  !intervalTime16secFlg && intervalTime > 1000*16 && shotCnt>100 && shotCnt<=5850 ) {//シャッター間隔が16秒/超えショットが5850以下の場合
				    		Platform.runLater(() ->info2.appendText("シャッター間隔が16秒以上ありました。"));
				    		intervalTime16secFlg = true;//rePain内でクリアされる
						}else if(  !shutterSignal4secInterval && intervalTime > 1000*30) {//シャッター間隔が30秒超えている場合
							shutterSignal4secInterval = true;
							shotCnt=0;
							autoGainEnable = false;
							Gpio.ngSignalON();
							Platform.runLater( () ->GPIO_STATUS_PIN3.setFill(Color.RED));
							logdata.csvWrite();
							//シャッタートリガ受信インジケーター色変更
				    		Platform.runLater( () ->GPIO_STATUS_PIN0.setFill(Color.WHITE));
				    		Platform.runLater(() ->info2.appendText("シャッター間隔が30秒以上ありました。強制的にNG信号を発信します"));
				    		//油付着の警告
				    		if(DispersionErrorNG) {
				    	        // ダイアログの表示
				    	        // Alertダイアログの利用
				    			/*
				    	        Alert alert = new Alert( AlertType.NONE , "" , ButtonType.OK ,
				    	                                                                           ButtonType.YES ,
				    	                                                                           ButtonType.NO ,
				    	                                                                           ButtonType.NEXT ,
				    	                                                                           ButtonType.PREVIOUS ,
				    	                                                                           ButtonType.FINISH ,
				    	                                                                           ButtonType.APPLY ,
				    	                                                                           ButtonType.CANCEL ,
				    	                                                                           ButtonType.CLOSE );
				    	        alert.setTitle( "油付着警告" );
				    	        alert.getDialogPane().setHeaderText( "油付着警告" );
				    	        alert.getDialogPane().setContentText( "油付着の可能性が高い画像が連続３回検出されました。無確認で継続生産を禁止します。！！操作は記録されます。！！" );
				    	        alert.showAndWait();
				    	        */
				    			Platform.runLater(() ->info2.appendText("油付着の可能性が高い画像が連続３回検出されました。無確認で継続生産を禁止します。！！操作は記録されます。！！"));
				    		}
							logdata.clear();

						}
						//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
						//シャッター間隔４秒以上発生後はＮＧ信号発信　2023.01.27
						//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
						if( shutterSignal4secInterval ) {
							Gpio.ngSignalON();
							Platform.runLater( () ->GPIO_STATUS_PIN3.setFill(Color.RED));
						}
						//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
						//キャプチャ異常時はＮＧ信号発信　2023.03.27
						//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
						if( cameraCaptureFlag == false ) {
							Gpio.ngSignalON();
							Platform.runLater( () ->GPIO_STATUS_PIN3.setFill(Color.RED));
						}

						//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
						//照明キャリブレーション中はＮＧ信号発信
						//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
						if(	calibLiteFlg ) {
							Gpio.ngSignalON();
							Platform.runLater( () ->GPIO_STATUS_PIN3.setFill(Color.RED));
						}
						//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
						//オールクリア信号受信
						//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
						rt = Gpio.clearSignal();
						//2022.08.16 クリア信号ノイズ回避
						if( rt =="1" && !biginClearSignal
								&& System.currentTimeMillis() - exeAllClearTime > 1500) {//2022.11.23 クリア信号連続受信バグ修正
							clearSignalTime = System.currentTimeMillis();
							biginClearSignal = true;
						}
						if( rt =="0" && biginClearSignal ) biginClearSignal = false;
						if( rt =="1" && biginClearSignal ) {
							if( System.currentTimeMillis() - clearSignalTime < 50) {
								//コード内で指定されている時間クリア信号が継続しないとrtを１にしない
								rt = "0";
							}
						}

						if( rt == "1" && !clealFlg && biginClearSignal) {
							biginClearSignal = false;//クリア信号ノイズ回避用フラグリセット
							Platform.runLater(() ->info2.appendText("PLCからクリア信号を受信しました"));
							clealFlg = true;
							autoGainEnable = true;
							sunpou_hantei_NG_5Shot = false;
							sunpou_hantei_NG_now = false;
							onAllClear(null);
							exeAllClearTime = System.currentTimeMillis();//オールクリアが実行された時刻を更新
				    		Platform.runLater( () ->GPIO_STATUS_PIN1.setFill(Color.YELLOW));
						}else if( rt == "0" && clealFlg ){//2022.07.13 バグ修正
							clealFlg = false;
							Platform.runLater( () ->GPIO_STATUS_PIN1.setFill(Color.LIGHTGRAY));
						}
						//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
						//シャッター信号受信
						//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
						if( !clealFlg ) {
							rt = Gpio.shutterSignal();
							if( rt == "1") {
								if( !offShutterFlg) {//シャッタートリガがoffになるまでshutterFlgをtrueにしない
									shutterFlg = true;
									offShutterFlg = true;

									//トリガ間隔測定用に現在時刻を保存
									shutterSignalIntervalTime=System.currentTimeMillis();

									//シャッタートリガ受信インジケーター色変更
						    		Platform.runLater( () ->GPIO_STATUS_PIN0.setFill(Color.YELLOW));
								}
							}else{
									//シャッター信号　非受信
									offShutterFlg = false;
									//シャッタートリガ受信インジケーター色変更
						    		Platform.runLater( () ->GPIO_STATUS_PIN0.setFill(Color.LIGHTGRAY));
							}
						}
						//--------------------------------------------------------------------------------------------
					}catch(NullPointerException e) {
						System.out.println("readIO" + " / " + e.toString());
						cameraCaptureFlag = false;
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
				}else {
					cameraCaptureFlag = false;
					System.out.println("farame.empty() == true\n");
				}

			} catch(Exception e) {
				Platform.runLater( () ->info2.appendText("Exception during the image elaboration: " + e +"\n"));
				cameraCaptureFlag = false;
			}
		}
		if(camera_revers_chk.isSelected()) {//画像上下反転
			Core.flip(frame, frame, -1);
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
		VisonController2.timer.shutdown();
		VisonController2.timer.awaitTermination(33, TimeUnit.MICROSECONDS);
		if( timer2 != null ) {
			VisonController2.timer2.shutdown();
			VisonController2.timer2.awaitTermination(10, TimeUnit.MICROSECONDS);
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

    /**
     * 画像検査のメインメソッド
     * srcMatに保存された画像が検査対象となる
     * saveImgUseFlgがTrueの場合は保存画像が検査対象となる
     */
    private void rePaint() {

    	long startTime = System.currentTimeMillis() ;//計測時間測定開始

    	final int disableJudgeCnt = 30;//スタートから判定を無効にするショット数
    	Integer[] holeCnt_log = new Integer[4];
    	int kyouseiTeisshiNgCnt = 0;//穴検査が２領域以上NGであった場合停止させるフラグ
    	String logMsg = "";//明示的にnull文字列を設定
    	double[] P2_log = new double[2];
    	double[] F_log = new double[2];
    	double[] E_log = new double[2];

    	//保存画像を使う場合srcMatへクローンする
    	if( saveImgUseFlg ) {
    		srcMat = saveImgMat.clone();
    	}

    	//srcMatが正常に取得できていない場合処理を続行しない
    	if( srcMat.width() < 1) return;


    	try {
    		//動作を確認する為のインジケーター処理
	    	if( this.triggerCCircle.getFill() != Color.YELLOW) {
	    		Platform.runLater( () ->this.triggerCCircle.setFill(Color.YELLOW));
	    	}else {
	    		Platform.runLater( () ->this.triggerCCircle.setFill(Color.GREEN));
	    	}

	    	//フレームレート計算
	    	fpsCnt++;
	    	if( fpsCnt == 30) {
	    		fpsEnd = System.currentTimeMillis();

	    		fps = fpsCnt/((fpsEnd - fpsFirst)/1000.0);
	    		Platform.runLater( () ->fpsLabel.setText(String.format("FPS=%.3f",fps)));

	    		fpsFirst = System.currentTimeMillis();
	    		fpsCnt=0;
	    	}

	    	//品番毎の設定を参照変数に入れる
	    	parameter para = pObj.para[pObj.select];

	    	Mat	mainViewMat = srcMat.clone();//srcMatは不変にしておく
	    	Mat saveSrcMat = srcMat.clone();//画像保存用にオリジナルを保持させる
	    	Mat mainViewGlayMat = new Mat();//グレースケール変換用MAT

	    	//グレースケール変換
	    	Imgproc.cvtColor(mainViewMat, mainViewGlayMat, Imgproc.COLOR_BGR2GRAY);
	    	//Imgproc.equalizeHist(mainViewGlayMat, mainViewGlayMat);//コントラスト均等化

	    	Mat ptnAreaMat = mainViewGlayMat.clone();
	    	Mat holeDetectAreaMat = mainViewGlayMat.clone();//各穴のパラメータに従った判定のベースMat
	    	Mat tmp1Mat = new Mat();//ガウシアンフィルター適用後のMAT
	    	Mat tmp2Mat = new Mat();//2値化フィルター適用後のMAT
	    	Mat tmp3Mat = new Mat();//膨張フィルター適用後のMAT
	    	Mat fillterAftterMat = mainViewGlayMat.clone();//設定モード時判定のベースMat
	    	int filterUselFlg = 0;

	    	//----------------ここから設定モードの処理-----------------------------------------------------------------
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

	    	if( FilterViewMode.isSelected()) {
	    		switch(filterUselFlg) {
		    	case 0://全てなし
			        updateImageView(imgGLAY1, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY2, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY3, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY, Utils.mat2Image(fillterAftterMat));
		    		break;
		    	case 1://ガウシアンのみ
			        updateImageView(imgGLAY1, Utils.mat2Image(tmp1Mat));
			        updateImageView(imgGLAY2, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY3, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY, Utils.mat2Image(fillterAftterMat));
		    		break;
		    	case 2://２値化のみ
			        updateImageView(imgGLAY1, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY2, Utils.mat2Image(tmp2Mat));
			        updateImageView(imgGLAY3, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY, Utils.mat2Image(fillterAftterMat));
		    		break;
		    	case 3://ガウシアンと２値化あり
			        updateImageView(imgGLAY1, Utils.mat2Image(tmp1Mat));
			        updateImageView(imgGLAY2, Utils.mat2Image(tmp2Mat));
			        updateImageView(imgGLAY3, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY, Utils.mat2Image(fillterAftterMat));
		    		break;
		    	case 4://膨張のみ
			        updateImageView(imgGLAY1, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY2, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY3, Utils.mat2Image(tmp3Mat));
			        updateImageView(imgGLAY, Utils.mat2Image(fillterAftterMat));
		    		break;
		    	case 5://ガウシアンと膨張あり
			        updateImageView(imgGLAY1, Utils.mat2Image(tmp1Mat));
			        updateImageView(imgGLAY2, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY3, Utils.mat2Image(tmp3Mat));
			        updateImageView(imgGLAY, Utils.mat2Image(fillterAftterMat));
		    		break;
		    	case 6://ガウシアン無し　他あり
			        updateImageView(imgGLAY1, Utils.mat2Image(new Mat(1,1,CvType.CV_8U)));
			        updateImageView(imgGLAY2, Utils.mat2Image(tmp2Mat));
			        updateImageView(imgGLAY3, Utils.mat2Image(tmp3Mat));
			        updateImageView(imgGLAY, Utils.mat2Image(fillterAftterMat));
		    		break;
		    	case 7://全てあり
			        updateImageView(imgGLAY1, Utils.mat2Image(tmp1Mat));
			        updateImageView(imgGLAY2, Utils.mat2Image(tmp2Mat));
			        updateImageView(imgGLAY3, Utils.mat2Image(tmp3Mat));
			        updateImageView(imgGLAY, Utils.mat2Image(fillterAftterMat));
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
	        		Integer[] dummy_integer = new Integer[1];
		            int foundCircle_result_int = foundCircle(
		            		fillterAftterMatRoi,//検出領域のサブMAT
		            		mainViewMat,//結果の描画
		            		(int)sliderDetecPara5.getValue(),//穴間距離の閾値
		            		(int)sliderDetecPara9.getValue(),//穴径の最大値
		            		(int)sliderDetecPara8.getValue(),//穴径の最小値
		            		sliderDetecPara7.getValue(),//円形度
		            		-1,//穴数の閾値 設定領域の為「-1」と入れ区別する
		            		whiteRatioMaxSp.getValue(),//白面積の最大値
		            		whiteRatioMinSp.getValue(),//白面積の最小値
		            		true,//インフォメーションに表示する
		            		draggingRect.x,draggingRect.y,
		            		dummy_integer,0//ダミー変数を渡す
		            		);
		        }
	            Imgproc.rectangle(mainViewMat,
	            		new Point(draggingRect.x,draggingRect.y),
	            		new Point(draggingRect.x+draggingRect.width,draggingRect.y+draggingRect.height),
	            		new Scalar(0,255,0),2);
	        }
	        //------------------ここまでが設定モード-------------------------------------------------------------------

	    	int judgCnt=0;//穴検査の合格エリア数　カウント数4で合格
	    	shotCnt++;
	    	String fileString = "x"+String.valueOf(shotCnt)+"x";//ショット数を入れる
	    	Platform.runLater( () ->this.count_label.setText(String.valueOf(shotCnt)));//ショット数を更新

	    	boolean ngFlg;
			Scalar color;
			//---------------穴検出開始---------------------------------------------------------------------------------
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

		        	int foundCircle_result_int=0;
		    		if( !settingModeFlg ) {
		            foundCircle_result_int = foundCircle(
		            		holeDetectAreaMatroi,//検出領域のサブMAT
		            		mainViewMat,//結果の描画
		            		(int)para.hole_circlePara5[i],//穴間距離の閾値
		            		(int)para.hole_circlePara9[i],//穴径の最大値
		            		(int)para.hole_circlePara8[i],//穴径の最小値
		            		para.hole_circlePara7[i],//円形度
		            		para.hole_cntHoleTh[i],//穴数の閾値 -1の場合は設定領域を示す
		            		para.hole_whiteAreaMax[i],//白面積の最大値
		            		para.hole_whiteAreaMin[i],//白面積の最小値
		            		false,//インフォメーションに表示する
		            		r.x,r.y,//結果図形・文字描画時のmainVierMatの絶対位置(検出領域の左上と同値)
		            		holeCnt_log,//穴検出数※参照渡し
		            		i//検査領域のインデックス番号
		            		);
		    		}

					//判定
		    		//foundCircle_result_int 0:OK 1:面積 2:個数 3:面積と個数 4:未検出
		    		String[] circleResultMsg = {"OK","検出個数NG","面積NG","面積と検出個数NG","未検出"};
		            switch(i) {
		            	case 0:
		            		if( foundCircle_result_int == 0) {
		            			Platform.runLater( () ->okuri1_judg.setText("OK"));
		            			Platform.runLater( () ->okuri1_judg.setTextFill( Color.GREEN));
		            			judgCnt++;
		            		}else {
		            			Platform.runLater( () ->okuri1_judg.setText("NG"));
		            			Platform.runLater( () ->okuri1_judg.setTextFill( Color.RED));
		            			ngFlg = true;
		            			kyouseiTeisshiNgCnt++;
		            			logMsg += "穴検出エリア① " + circleResultMsg[foundCircle_result_int] + "\n";
		            		}
		            		break;
		            	case 1:
		            		if( foundCircle_result_int == 0 ) {
		            			Platform.runLater( () ->okuri2_judg.setText("OK"));
		            			Platform.runLater( () ->okuri2_judg.setTextFill( Color.GREEN));
		            			judgCnt++;
		            		}else {
		            			Platform.runLater( () ->okuri2_judg.setText("NG"));
		            			Platform.runLater( () ->okuri2_judg.setTextFill( Color.RED));
		            			ngFlg = true;
		            			kyouseiTeisshiNgCnt++;
		            			logMsg += "穴検出エリア② " + circleResultMsg[foundCircle_result_int] + "\n";
		            		}
		            		break;
		            	case 2:
		            		if( foundCircle_result_int == 0 ) {
		            			Platform.runLater( () ->okuri3_judg.setText("OK"));
		            			Platform.runLater( () ->okuri3_judg.setTextFill( Color.GREEN));
		            			judgCnt++;
		            		}else {
		            			Platform.runLater( () ->okuri3_judg.setText("NG"));
		            			Platform.runLater( () ->okuri3_judg.setTextFill( Color.RED));
		            			ngFlg = true;
		            			kyouseiTeisshiNgCnt++;
		            			logMsg += "穴検出エリア③ " + circleResultMsg[foundCircle_result_int] + "\n";
		            		}
		            		break;
		            	case 3:
		            		if( foundCircle_result_int == 0 ) {
		            			Platform.runLater( () ->okuri4_judg.setText("OK"));
		            			Platform.runLater( () ->okuri4_judg.setTextFill( Color.GREEN));
		            			judgCnt++;
		            		}else {
		            			Platform.runLater( () ->okuri4_judg.setText("NG"));
		            			Platform.runLater( () ->okuri4_judg.setTextFill( Color.RED));
		            			ngFlg = true;
		            			kyouseiTeisshiNgCnt++;
		            			logMsg += "穴検出エリア④ " + circleResultMsg[foundCircle_result_int] + "\n";
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
		        	judgCnt++;//判定しない時は合格ととしてカウント
		        }

	        }


	        //パターンマッチング
	        //return  0:合格又は検出無効  0x01:検出個数不足 0x02:警報閾値未満有 0x04:検出個数過多 0x08:分散警報閾値以上 0x10:分散閾値以上
        	int tmStatus;
        	if( !ptm_disableChk.isSelected() ) {//パターンマッチングが強制的に無効になっているか？
        		tmStatus = ptm_templateMatchingObj.detectPattern(ptnAreaMat,mainViewMat
        											,false,patternDispChk.isSelected());
        	}else {
        		tmStatus = 0;
        	}
        	if( (tmStatus & 0x01) != 0 ) {//検出個数不足
				Imgproc.putText(mainViewMat, "Count Error",
						new Point(200,1080/2+40),
						Imgproc.FONT_HERSHEY_SIMPLEX,4.0,new Scalar(0,0,255),6);
		        logMsg += "パターンマッチングの検出個数が閾値を下回っている領域があります。製品を確認してください\n";
        	}
        	if( (tmStatus & 0x02) != 0 ) {//パターンマッチング　警告レベル
				Imgproc.putText(mainViewMat, "Pattern Warning",
						new Point(1920/2,1080/2),
						Imgproc.FONT_HERSHEY_SIMPLEX,2.0,new Scalar(0,0,255),6);
		        logMsg += "パターンマッチングが警告レベルを下回っている領域があります\n";
        	}
        	if( (tmStatus & 0x04) != 0 ) {//検出個数過多
				Imgproc.putText(mainViewMat, "Count Error",
						new Point(10,1080/2+40),
						Imgproc.FONT_HERSHEY_SIMPLEX,4.0,new Scalar(0,0,255),6);
		        logMsg += "パターンマッチングの検出個数が閾値を上回っている領域があります。設定条件の見直しが必要です\n";
        	}
        	if( (tmStatus & 0x08) != 0 ) {//分散警告レベル
				Imgproc.putText(mainViewMat, "Dispersion Warning",
						new Point(1920/2,1080/2+40),
						Imgproc.FONT_HERSHEY_SIMPLEX,2.0,new Scalar(0,0,255),6);
		        logMsg += "分散が警告レベルを上回っている領域があります\n";
        	}
        	if( (tmStatus & 0x10) != 0 ) {//分散 閾値レベル
				Imgproc.putText(mainViewMat, "Dispersion Error",
						new Point(200,1080/2+60),
						Imgproc.FONT_HERSHEY_SIMPLEX,4.0,new Scalar(0,0,255),6);
		        logMsg += "分散が閾値レベルを上回っている領域があります\n";
		        DispersionErrorFlg++;
		        if(DispersionErrorFlg>=3) {
		        	DispersionErrorNG = true;
		        }
        	}
        	//分散閾値がOKの場合、変数クリア
        	if( DispersionErrorFlg > 0 && ((tmStatus & 0x10) == 0 )) {
        		DispersionErrorFlg=0;
        	}


        	//P2/F/E寸法測定
        	int dimStatus;
        	dimStatus = dim_templateMatchingObj.detectPattern(ptnAreaMat,mainViewMat
        											,false,dimensionDispChk.isSelected());
       		if( dimStatus == 0 ) {
	        	for(int g=0;g<2;g++) {
	        		if( ((g==0 && dim_1_enable.isSelected()) || ((g==1) && dim_2_enable.isSelected())) && shotCnt>0) {
	        			double P2 = 0.0;
	        			double F = 0.0;
	        			double E = 0.0;
	        			if( dim_templateMatchingObj.resultValue[g*2].cnt == 1 &&
	        					dim_templateMatchingObj.resultValue[g*2+1].cnt == 1) {
			        		double p2_x0 = dim_templateMatchingObj.resultValue[g*2].centerPositionX.get(0);
			        		double p2_x1 = dim_templateMatchingObj.resultValue[g*2+1].centerPositionX.get(0);
			        		//System.out.println("X0 = " + String.format("%.4f", p2_x0));
			        		//System.out.println("X1 = " + String.format("%.4f", p2_x1));
			        		P2 = Math.abs(p2_x0 - p2_x1)*(para.dimPixel_mm+para.dimPixel_mm_offset)+para.dim_offset_P2[g];

			        		//disableJudgeCntショット目から加算
			        		if( shotCnt > disableJudgeCnt ) {
			        			P2_sum[g] += P2;
			        		}
			        		double f_y0 = dim_templateMatchingObj.resultValue[g*2].centerPositionY.get(0);
			        		double f_y1 = dim_templateMatchingObj.resultValue[g*2+1].centerPositionY.get(0);
			        		F = Math.abs(f_y0 - f_y1)*(para.dimPixel_mm+para.dimPixel_mm_offset)+para.dim_offset_F[g];
			        		//disableJudgeCntショット目から加算
			        		if( shotCnt > disableJudgeCnt ) {
			        			F_sum[g] += F;
			        		}
			        		//E寸は１列目しか測定出来ない、２列目はロジック上のダミ変数
			        		double e_ｙ0 =  dim_templateMatchingObj.resultValue[4].centerPositionY.get(0);
			        		E = Math.abs(f_y0 - e_ｙ0)*(para.dimPixel_mm+para.dimPixel_mm_offset)+para.dim_offset_E[0];
			        		//disableJudgeCntショット目から加算
			        		if( shotCnt > disableJudgeCnt ) {
			        			E_sum[g] += E;
			        		}

	        			}else {
	        				P2 =0.0;
	        				F=0.0;
	        				E=0.0;
	        			}
	        			final int g2 =g;

	        			final double _P2 =Double.valueOf(String.format("%.3f",P2)).doubleValue();
	        			final double _F = Double.valueOf(String.format("%.3f",F)).doubleValue();
	        			final double _E = Double.valueOf(String.format("%.3f",E)).doubleValue();

	        			final double P2_ave = P2_sum[g]/(shotCnt-disableJudgeCnt);//disableJudgeCnt+1ショット目から加算の為
	        			final double F_ave = F_sum[g]/(shotCnt-disableJudgeCnt);//disableJudgeCnt+1ショット目から加算の為
	        			final double E_ave = E_sum[g]/(shotCnt-disableJudgeCnt);//disableJudgeCnt+1ショット目から加算の為
	        			final double _P2_ave = Double.valueOf(String.format("%.3f",P2_ave)).doubleValue();
	        			final double _F_ave = Double.valueOf(String.format("%.3f",F_ave)).doubleValue();
	        			final double _E_ave = Double.valueOf(String.format("%.3f",E_ave)).doubleValue();

	        			final double P2_final = P2;
	        			final double F_final = F;
	        			final double E_final = E;

	        			//ログデーター処理用
	        			P2_log[g] = P2;
	        			F_log[g] = F;
	        			if( g == 0 ) {
	        				E_log[g] =E;
	        			}else {
	        				E_log[g] = 0.0;//２列目は測定不能たの為0.0を入力
	        			}

	        			Platform.runLater( () ->dataset_P2[g2].getSeries(0).add(shotCnt,P2_final));
		        		Platform.runLater( () ->dataset_F[g2].getSeries(0).add(shotCnt,F_final));
		        		if( g2 == 0) {
		        			Platform.runLater( () ->dataset_E[g2].getSeries(0).add(shotCnt,E_final));
		        		}

		        		//寸法表示テーブルの更新
		        		Platform.runLater( () ->dim_table.getItems().get(g2*2).P2Property().set(_P2));
		        		Platform.runLater( () ->dim_table.getItems().get(g2*2).FProperty().set(_F));
		        		if( g2 == 0) {Platform.runLater( () ->dim_table.getItems().get(0).EProperty().set(_E));}
		        		Platform.runLater( () ->dim_table.getItems().get(g2*2+1).P2Property().set(_P2_ave));
		        		Platform.runLater( () ->dim_table.getItems().get(g2*2+1).FProperty().set(_F_ave));
		        		if( g2 == 0) {Platform.runLater( () ->dim_table.getItems().get(0+1).EProperty().set(_E_ave));}

		        		//軸の設定更新
		        		Platform.runLater( () ->((NumberAxis)((XYPlot)chart_P2[g2].getPlot()).getDomainAxis()).
																	setRange(shotCnt<=200?0:shotCnt-200,shotCnt));
		        		Platform.runLater( () ->((NumberAxis)((XYPlot)chart_F[g2].getPlot()).getDomainAxis()).
		        													setRange(shotCnt<=200?0:shotCnt-200,shotCnt));
		        		Platform.runLater( () ->((NumberAxis)((XYPlot)chart_P2[g2].getPlot()).getRangeAxis()).
								setRange(baseParameterValue.P2_LowerLimit_dimensionTheshold - 0.05,
												baseParameterValue.P2_UpperLimit_dimensionTheshold + 0.05));
		        		Platform.runLater( () ->((NumberAxis)((XYPlot)chart_F[g2].getPlot()).getRangeAxis()).
								setRange(baseParameterValue.F_LowerLimit_dimensionTheshold - 0.05,
												baseParameterValue.F_UpperLimit_dimensionTheshold + 0.05));
		        		Platform.runLater( () ->((NumberAxis)((XYPlot)chart_E[0].getPlot()).getRangeAxis()).
								setRange(baseParameterValue.E_LowerLimit_dimensionTheshold - 0.05,
												baseParameterValue.E_UpperLimit_dimensionTheshold + 0.05));

		        		//寸法外れ判定 2022.08.16
		        		P2_hantei[g][hantei_cnt] = P2;
		        		F_hantei[g][hantei_cnt] = F;
		        		E_hantei[g][hantei_cnt] = E;

		        		hantei_cnt++;
	        			if( hantei_cnt == 5 ) {
	        				hantei_cnt = 0;
	        			}
		        		if(shotCnt>50 ){
		        			double P2_tmp=0;
		        			double F_tmp=0;
		        			double E_tmp=0;
		        			double P2ave_tmp,Fave_tmp,Eave_tmp;
		        			for(int i=0;i<5;i++) {
		        				P2_tmp += P2_hantei[g][i];
		        				F_tmp += F_hantei[g][i];
		        				E_tmp += E_hantei[g][i];
		        			}
		        			P2ave_tmp = P2_tmp/5;
		        			Fave_tmp = F_tmp/5;
		        			Eave_tmp = E_tmp/5;

			        		if( P2ave_tmp <baseParameterValue.P2_LowerLimit_dimensionTheshold ||
			        				P2ave_tmp > baseParameterValue.P2_UpperLimit_dimensionTheshold ||
			        				Fave_tmp < baseParameterValue.F_LowerLimit_dimensionTheshold ||
			        				Fave_tmp > baseParameterValue.F_UpperLimit_dimensionTheshold) {
		        				sunpou_hantei_NG_now = true;
		        				Imgproc.putText(mainViewMat, "Dimension Warning",
		        						new Point(200,1080/5),
		        						Imgproc.FONT_HERSHEY_SIMPLEX,6.0,new Scalar(0,0,255),5);
		        				logMsg +="寸法警告\n";
		        			}else {
		        				sunpou_hantei_NG_now = false;
		        			}
			        		if( P2ave_tmp < baseParameterValue.P2_LowerLimit_dimensionTheshold ||
			        						P2ave_tmp > baseParameterValue.P2_UpperLimit_dimensionTheshold ||
			        						Fave_tmp < baseParameterValue.F_LowerLimit_dimensionTheshold ||
			        						Fave_tmp > baseParameterValue.F_UpperLimit_dimensionTheshold) {
		        				sunpou_hantei_NG_5Shot = true;
		        				logMsg +="寸法NG : ５ショットの平均が規格から外れました\n";
		        			}
			        		if( (Eave_tmp < baseParameterValue.E_LowerLimit_dimensionTheshold ||
	        						Eave_tmp > baseParameterValue.E_UpperLimit_dimensionTheshold) && g==0){
				        		//E寸警告ラベル表示
				        		Platform.runLater( () ->E_warningLabel.setVisible(true));
				        		logMsg +="E寸異常 : ５ショットのE寸平均が規格から外れました\n";
	        				}
		        		}

	        		}else {
	        			final int g2 =g;
	        			Platform.runLater( () ->dataset_P2[g2].getSeries(0).add(shotCnt,0.000f));
	        			Platform.runLater( () ->dataset_F[g2].getSeries(0).add(shotCnt,0.000f));
	        			if( g2 == 0) {
	        				Platform.runLater( () ->dataset_E[g2].getSeries(0).add(shotCnt,0.000f));
	        			}
	        			Platform.runLater( () ->dim_table.getItems().get(g2*2+1).P2Property().set(0.000f));
		        		Platform.runLater( () ->dim_table.getItems().get(g2*2+1).FProperty().set(0.000f));
		        		if( g2 == 0) {
		        		Platform.runLater( () ->dim_table.getItems().get(g2*2+1).EProperty().set(0.000f));
		        		}
		        		//寸法表示テーブルの更新
		        		Platform.runLater( () ->dim_table.getItems().get(g2*2).P2Property().set(0.000f));
		        		Platform.runLater( () ->dim_table.getItems().get(g2*2).FProperty().set(0.000f));
		        		if( g2 == 0) {
		        			Platform.runLater( () ->dim_table.getItems().get(g2*2).EProperty().set(0.000f));
		        		}

	        		}
	        	}

       		}else {
       			//sunpou_hantei_NG = true;
       			logMsg += "寸法測定に失敗しました\n";
       			for(int m=0;m<2;m++) {
	       			if( dim_templateMatchingObj.resultValue[m*2].cnt > 1 ||
	    					dim_templateMatchingObj.resultValue[m*2+1].cnt > 1) {
	    				logMsg += "寸法測定に失敗しました\n領域内の検出個数が1個ではありません。\n";
	       			}
       			}
       		}

	        if( !saveImgUseFlg && !settingModeFlg && shotCnt>0 ) {
		        //最終判定
	        	if( shotCnt < disableJudgeCnt+1 ) { //disableJudgeCnt+1ショットまでは判定無視
		        	Platform.runLater( () ->judg.setText( String.valueOf(disableJudgeCnt-shotCnt)) );
		        	Platform.runLater( () ->judg.setTextFill( Color.GREEN) );
	        	}else if(judgCnt==4 && (tmStatus==0 || tmStatus==2) &&!sunpou_hantei_NG_5Shot
	        			 && !sunpou_hantei_NG_now) {//judgCntが穴判定  tmStatusがテンプレートマッチング判定
		        	Platform.runLater( () ->judg.setText("OK") );
		        	Platform.runLater( () ->judg.setTextFill(Color.GREEN) );
		        	//画像保存
		        	if( this.imgSaveFlg_all.isSelected() ) {
		        		saveImgOK( saveSrcMat );
		        	}
		        }else {
		        	//ここから判定NGの場合-----------------------------------------------------------------------------------
		        	Platform.runLater( () ->judg.setText("NG"));
		        	Platform.runLater( () ->judg.setTextFill(Color.RED) );
		        	//画像保存
		        	if( !shutterSignal4secInterval && imgSaveFlg.isSelected() && ngCnt < saveMax_ng
		        			&& !settingModeFlg) {
		        		saveImgNG( saveSrcMat,fileString);
		        		saveImgNG( mainViewMat,"_"+fileString);
		        	}else if( fileString != ""){
		        		logMsg +=
		        				"!shutterSignal4secInterval = "+String.valueOf(!shutterSignal4secInterval)+"\n" +
		        				"imgSaveFlg.isSelected()=" +String.valueOf(imgSaveFlg.isSelected()) +"\n" +
		        				"ngCnt < saveMax_ng=" + String.valueOf(ngCnt < saveMax_ng) +"\n" +
		        				"!settingModeFlg="+String.valueOf(!settingModeFlg) +"\n"+
		        				"NG画像保存は保存されませんでした\n";

		        		//final String infoText = fileString +"\n";
		        		//Platform.runLater( () ->info2.appendText(infoText));
		        	}else {
		        		logMsg += "NG画像保存は保存されませんでした\n";
		        	}
		        	if( ngCnt > 500 || sunpou_hantei_NG_5Shot) {//2022.08.16寸法NGでも強制停止
		        		kyouseiTeisshiNgCnt = 2;//強制停止
		        	}else {
		        		ngCnt++;
		        	}

		        	//出力トリガが無効で無い かつ　(強制停止変数が1より大きい場合 か NG継続フラグがTrue)
		        	//NG信号の発信が失敗した場合の措置としてngSignalKeizokuFlgがTrueの場合継続してGpio.ngSignalON()が呼ばれるように変更2023.03.29
		        	if( !outTrigDisableChk.isSelected() && (kyouseiTeisshiNgCnt>1 || ngSignalKeizokuFlg)){//2023.10.17 論理式が間違っていたのを修正
	        			//Platform.runLater(() ->info2.appendText(sunpou_hantei_NG+"\n"));
		        		ngSignalKeizokuFlg = true;
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

		        		//寸法判定NGメッセージ表示
		        		if( sunpou_hantei_NG_5Shot ) {
		        			sunpou_hantei_NG_5Shot = false;
		        		}
		        	}

		        	Platform.runLater(() ->ngCounterLabel.setText(String.valueOf(ngCnt)));
		        	updateImageView(imgNG, Utils.mat2Image(mainViewMat));
		        	//ここまで判定NGの場合-----------------------------------------------------------------------------------
		        }
	        }

	        //Core.flip(mainViewMat, mainViewMat, 1);
	        updateImageView(imgORG, Utils.mat2Image(mainViewMat));

    	}catch(Exception e) {
    		logMsg += e+"\n:検査設定がキャプチャーされた画像からはみ出しています。\n検査設定をやり直してください\n";
    	}


    	//オートゲイン
    	if( autoGainChk.isSelected() && !manualTrigger &&
    			!saveImgUseFlg &&!demoFlg && shotCnt > disableJudgeCnt-5 && autoGainEnable ) {// disableJudgeCnt-20 から disableJudgeCnt-5へ変更2023.11.22
	    	Double luminanceAverage = 0.0;
	    	parameter para = pObj.para[pObj.select];
	    	int cnt = 0;
	    	for( int i=0;i<4;i++) {
		    	if( para.hole_DetectFlg[i] ) {
			    	Rectangle r = para.hole_rects[i];//検査範囲
			    	luminanceAverage += Core.mean(srcMat.submat(new Rect(r.x,r.y,r.width,r.height))).val[0];
			    	cnt++;
		    	}
	    	}
	    	if( cnt > 0 ) {
	    		luminanceAverage = luminanceAverage / cnt;
	        	int err = exeAutoGain(luminanceAverage);
	        	if( err == 2) {
	        		//照明異常強制停止
	        		Platform.runLater(() ->aPane.setStyle("-fx-background-radius: 0;-fx-background-color: rgba(0,0,0,0.5);"));
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
	        		logMsg += "!!!照明異常停止!!!\n";
	        		logMsg += "オートゲインの調整範囲を超えた為、強制的に停止\n";
	        		logMsg += "照明キャリブレーションが必要\n";
	        		setDefultGain();//ゲインをデフォルトにリセットする

	        	}
	    	}
    	}
    	if(intervalTime16secFlg) {
    		intervalTime16secFlg = false;
    		logMsg += "シャッター間隔が16秒以上ありました。\n";
    	}
        if( !saveImgUseFlg && !settingModeFlg && shotCnt>0 ) {
        	if( shotCnt > disableJudgeCnt ) { //disableJudgeCntショットまではログ無し
	       		//ログデーター処理
	       		logdata.addData(P2_log,F_log,E_log, ptm_templateMatchingObj.resultValue,
	       				holeCnt_log,
	       				String.valueOf(shotCnt) + "::" +logMsg);
        	}
        }
        if( logMsg != "" ) {
	        final String _logMsg_ = logMsg;
	    	Platform.runLater(() ->info2.appendText(String.valueOf(shotCnt) + "::" + _logMsg_));
        }

        //計測時間表示
        long elpsedTime = System.currentTimeMillis() - startTime;
        Platform.runLater(() ->this.sokuteiTimeLabel.setText(String.format("計測時間=%d msec",elpsedTime)));
    }

    /**
     * 目標輝度設定値変更
     * @param event
     */
    @FXML
    void onTargetLuminanceChange(KeyEvent event) {
    	autoGain_target = Double.valueOf( targetLuminance.getText());
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
        	//Platform.runLater( () ->info2.appendText(folder+"/"+ fileName +".png"+"NG画像保存"+"\n"));
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
     * 2021.11.17更新
     * ブロブによる円の検出
     * @param src_ 2値化済画像
     * @param dst_ 結果画像描画用MAT
     * @para holeLength_ 検出円間最小距離
     * @param max_diameter_ 検出円最大直径値　ピクセル
     * @param min_diameter_ 検出円最小直径値　ピクセル
     * @param circularity_ 円形度 0.0 - 1.0
     * @param circleCountThresh_ 検出円個数　判定個数
     * @param threshholdAreaMax_ 検出矩形の白面積 最大値閾値
     * @param threshholdAreaMin_ 検出矩形の白面積 最小値閾値
     * @para infoFlg_ インフォメーションテキストに値を表示させるか？
     * @para holeCnt_log_ integer[]型で穴検出数をログへ書き込み為の参照渡しの変数
     * @para index_ hokeCnt_log_のインデックスで使用
     * @return resultFlgBit 判定結果 0:合格 1:面積判定NG 2:個数ＮＧ 3:(面積判定、個数ＮＧ) 4:未検出
	 *                 ※0b00000000:OK  0b00000001:面積判定NG 0b00000010:検出個数NG  0b00000100:未検出
     */
    private int foundCircle(Mat src_,Mat dst_,int holeLength,int max_diameter_,int min_diameter_,double circularity_
    			,int circleCountThresh,double threshholdAreaMax,double threshholdAreaMin,
    			boolean infoFlg,int offset_x,int offset_y,
    			Integer[] holeCnt_log_,int index_
    			) {

		List<MatOfPoint> contours=new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();

		//輪郭抽出
		Imgproc.findContours(src_,contours,hierarchy,Imgproc.RETR_CCOMP,Imgproc.CHAIN_APPROX_SIMPLE);

		//ローカル変数宣言
		Iterator<MatOfPoint> iterator = contours.iterator();
	    MatOfPoint2f[] contoursPoly = new MatOfPoint2f[contours.size()];
        Rect[] boundRect = new Rect[contours.size()];
        Point[] centers = new Point[contours.size()];
        float[][] radius = new float[contours.size()][1];

        int cnt = 0;
        Mat roi = new Mat(1,1,CvType.CV_8U);
        int whiteArea = 0;
    	infoText = "";
      	double radiusMax,radiusMin,radiusAve,distAve;
      	radiusMax = 0;
      	radiusMin = 9999;
      	radiusAve = 0;
      	distAve = 0;
      	int	resultFlgBit = 0;

        whiteAreaAverage = 0;//クラス変数
	  	whiteAreaMax = 0;//クラス変数
	  	whiteAreaMin = 99999;//クラス変数

	  	//*************************************************************************************************************
	  	//円の取得
	  	//*************************************************************************************************************
	  	while (iterator.hasNext()){
    		MatOfPoint contour = iterator.next();
    		double area = Imgproc.contourArea(contour);
    		double arclength = Imgproc.arcLength(new MatOfPoint2f(contour.toArray()),true);
    		double circularity = 4 * Math.PI * area / (arclength * arclength);//円形度計算 4*PI*S/周長^2

    		if( circularity >= circularity_ &&
    				arclength/(2*Math.PI) >= min_diameter_ && arclength/(2*Math.PI) <= max_diameter_) {//円形度の比較
    			contoursPoly[cnt] = new MatOfPoint2f();
    			try {
    			Imgproc.approxPolyDP(new MatOfPoint2f(contour.toArray()), contoursPoly[cnt], 3, true);
	            boundRect[cnt] = Imgproc.boundingRect(new MatOfPoint(contoursPoly[cnt].toArray()));

	            //検出円の位置と直径の取得
	            centers[cnt] = new Point();
    			Imgproc.minEnclosingCircle(contoursPoly[cnt], centers[cnt], radius[cnt]);
    			cnt++;
    			}catch(Exception e){
    				System.out.println(e+"円検出エラー");
    			}
    		}
        }

	  	//穴検出数を参照渡しの変数へ格納
	  	holeCnt_log_[index_] = cnt;

	  	if( cnt == 0 ) {
	  		return 0b00000100;//未検出
	  	}

		//径と距離算出 X順で並び替え
		Point tmpPoint;
		float tmpRadius;
		Rect tmpBoundRect;
		for(int i=0;i<cnt;i++) {
			for(int j=i+1;j<cnt;j++) {
				if( centers[i].x > centers[j].x ) {
					tmpPoint = centers[i].clone();
					centers[i] = centers[j].clone();
					centers[j] = tmpPoint;

					tmpRadius = radius[i][0];
					radius[i][0] = radius[j][0];
					radius[j][0] = tmpRadius;

					tmpBoundRect = boundRect[i];
					boundRect[i] = boundRect[j];
					boundRect[j] = tmpBoundRect;
				}
			}
		}
		/*
		//穴間平均距離
		for(int i=0;i<cnt-1;i++) {
			distAve += centers[i+1].x - centers[i].x;

			if( holeLength > centers[i+1].x - centers[i].x ) {
				result = 4;
			}
		}
		*/
		//穴間平均距離を測定し次添え時の配列を詰める
		for(int i=0;i<cnt-1;i++) {

			if( holeLength > centers[i+1].x - centers[i].x ) {//穴間距離が閾値をした回っていいた場合[i]配列の穴を削除する
				for(int j=i+1;j<cnt-1;j++) {
					centers[j] = centers[j+1];
					radius[j] = radius[j+1];
					boundRect[j] = boundRect[j+1];
				}
				cnt--;
				i--;
			}else {
				distAve += centers[i+1].x - centers[i].x;
			}
		}
	  	//*************************************************************************************************************
	  	//検出円描画、検出矩形描画
	  	//*************************************************************************************************************

	  	for( int i=0; i<cnt; i++ ) {
		  	if(holeDispChk.isSelected()) {
	            Scalar color = new Scalar(0,255,255);
	            Imgproc.circle(dst_,
	            		new Point(centers[i].x+offset_x,centers[i].y+offset_y),
	            		(int) radius[i][0], color, 2);
	            Imgproc.rectangle(dst_,
	            		new Point(boundRect[i].tl().x+offset_x,boundRect[i].tl().y+offset_y),
	            		new Point(boundRect[i].br().x+offset_x,boundRect[i].br().y+offset_y),
	            		color, 2);
	        }
		  	//*************************************************************************************************************
	        //白面積判定
	        if(src_.width() < boundRect[i].br().x) {
	        	boundRect[i].br().x = src_.width() -1;
	        }
	        if( src_.height() < boundRect[i].br().y) {
	        	boundRect[i].br().y = src_.height() -1;
	        }
		  	roi = src_.submat(boundRect[i]);
	  		whiteArea = Core.countNonZero(roi);
	  		whiteAreaAverage += whiteArea;
	  		whiteAreaMax = whiteAreaMax < whiteArea?whiteArea:whiteAreaMax;
	  		whiteAreaMin = whiteAreaMin > whiteArea?whiteArea:whiteAreaMin;
	  		if( whiteArea > threshholdAreaMax || whiteArea < threshholdAreaMin) {
	  			resultFlgBit = resultFlgBit | 0b00000010;//面積判定NG
	  		}
  		}
		if( settingModeFlg ) {
			Platform.runLater(() ->whiteRatioLabel.setText( String.format("%d", whiteAreaMax)));
			Platform.runLater(() ->blackRatioLabel.setText( String.format("%d", whiteAreaMin)));
		  	updateImageView(debugImg, Utils.mat2Image(roi));
		}

  		whiteAreaAverage /= cnt;

        if(holeDispChk.isSelected()) {

        	if( resultFlgBit == 0  ) {
				Imgproc.putText(dst_,
						"WhiteArea OK  ave=" + String.format("%d",whiteAreaAverage) +
								" Max=" + String.format("%d",whiteAreaMax) +
								" Min=" + String.format("%d",whiteAreaMin),
						new Point(offset_x+20,offset_y-6),
						Imgproc.FONT_HERSHEY_SIMPLEX, 1.0,new Scalar(128,255,128),2);
			}else {
				Imgproc.putText(dst_, "WhiteArea NG  ave=" + String.format("%d",whiteAreaAverage) +
						" Max=" + String.format("%d",whiteAreaMax) +
						" Min=" + String.format("%d",whiteAreaMin),
						new Point(offset_x+20,offset_y-6),
						Imgproc.FONT_HERSHEY_SIMPLEX, 1.0,new Scalar(0,0,255),2);
			}
        }

		//個数判定
		if( cnt != circleCountThresh ) {//&& circleCountThresh != -1) {
			resultFlgBit = resultFlgBit | 0b00000001;
		}
        if(holeDispChk.isSelected()) {
			Imgproc.putText(dst_, String.valueOf(cnt),
					new Point(offset_x-60,offset_y-6),
					Imgproc.FONT_HERSHEY_SIMPLEX, 2.0,new Scalar(128,255,128),2);
        }

		//穴径(最大、最小、平均算出)
		for(int i=0;i<cnt;i++) {
			if( radius[i][0] > radiusMax ) {
				radiusMax = radius[i][0];
			}
			if( radius[i][0] < radiusMin ) {
				radiusMin = radius[i][0];
			}
			radiusAve += radius[i][0];
		}
		radiusAve /= cnt;

		if( cnt > 1 ) distAve /= cnt - 1;
		holeDist_DimSetting = distAve;

		whiteAreaAverage = whiteAreaAverage / cnt;
    	infoText += String.format("MAX=%.1f ,MIN=%.1f ,AVE=%.1f ,DistAve=%.1f ",
    			radiusMax,radiusMin,radiusAve,distAve);

    	if(infoFlg) {
    		Platform.runLater(
    				() ->info1.setText(infoText));
    	 }
	    return resultFlgBit;
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

		Platform.runLater(() ->this.autoGainChk.setSelected(para.autogain));


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

    	//寸法測定部
    	Platform.runLater( () ->dimSettingLabel.setText(String.format("%.0f μm/pixel", para.dimPixel_mm*1000)));
    	Platform.runLater( () ->dimSetting_offset.setText(String.format("%.0f", para.dimPixel_mm_offset*1000)));
    	Platform.runLater( () ->dim_offset_P2_1.setText(String.valueOf(para.dim_offset_P2[0])));
    	Platform.runLater( () ->dim_offset_F_1.setText(String.valueOf(para.dim_offset_F[0])));
    	Platform.runLater( () ->dim_offset_E_1.setText(String.valueOf(para.dim_offset_E[0])));
    	Platform.runLater( () ->dim_offset_P2_2.setText(String.valueOf(para.dim_offset_P2[1])));
    	Platform.runLater( () ->dim_offset_F_2.setText(String.valueOf(para.dim_offset_F[1])));

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
		FileOutputStream fo = new FileOutputStream("./conf403.txt");
		ObjectOutputStream objOut = new ObjectOutputStream(fo);

		pObj.dimensionDispChk =  dimensionDispChk.isSelected();
	    pObj.holeDispChk = holeDispChk.isSelected();
	    pObj.patternDispChk = patternDispChk.isSelected();
		pObj.camera_revers = camera_revers_chk.isSelected();

    	parameter para = pObj.para[pObj.select];
    	para.autogain = autoGainChk.isSelected();
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

		para.dim_1_enable = dim_1_enable.isSelected();
		para.dim_2_enable = dim_2_enable.isSelected();
		para.dim_Enable[0] = dim_1_enable.isSelected();
		para.dim_Enable[1] = dim_1_enable.isSelected();
		para.dim_Enable[2] = dim_2_enable.isSelected();
		para.dim_Enable[3] = dim_2_enable.isSelected();
		para.dim_Enable[4] = dim_1_enable.isSelected();


	    para.dim_offset_P2[0] = Double.valueOf(dim_offset_P2_1.getText());
	    para.dim_offset_F[0] = Double.valueOf(dim_offset_F_1.getText());
	    para.dim_offset_E[0] = Double.valueOf(dim_offset_E_1.getText());
	    para.dim_offset_P2[1] = Double.valueOf(dim_offset_P2_2.getText());
	    para.dim_offset_F[1] = Double.valueOf(dim_offset_F_2.getText());

		para.dimPixel_mm_offset = Double.valueOf(dimSetting_offset.getText());

		pObj.portNo = portNoSpin.getValue().intValue();
		pObj.cameraID = camIDspinner.getValue().intValue();
		pObj.adc_thresh = Integer.valueOf(adc_thresh_value.getText());
		pObj.cameraHeight = Integer.valueOf(capH_text.getText());
		pObj.cameraWidth = Integer.valueOf(capW_text.getText());
		pObj.adcFlg = adc_flg.isSelected();

		para.delly = dellySpinner.getValue().intValue();
		para.targetGain = Double.valueOf(this.targetLuminance.getText());
		para.exposur = Double.valueOf(cameraExpro.getText());//2022.11.22 露出設定の保存

		objOut.writeObject(pObj);
		objOut.flush();
		objOut.close();

		//パターンマッチング画像の保存 ptmImgMat[preSetNo][ptm1～ptm4]
    	int max_ptm_array = parameter.ptm_arrySize;
		for(int i=0;i<4;i++) {
			for(int j=0;j<max_ptm_array;j++) {
				if( ptm_ImgMat[i][j] != null ) {
					savePtmImg(ptm_ImgMat[i][j],"ptm"+String.format("_%d_%d", i,j));
				}
			}
		}
		//寸法測定用画像保存
		for(int i=0;i<4;i++) {
			for(int j=0;j<5;j++) {
				if( dim_ImgMat[i][j] != null ) {
					savePtmImg(dim_ImgMat[i][j],"dim"+String.format("_%d_%d", i,j));
				}
			}
		}

		Platform.runLater( () ->info2.appendText("設定が保存されました。\n"));
    }
    /**
     * CSVに設定を保存
     * @throws IOException
     */
    public void saveCsvAllPara() throws IOException{
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH' h 'mm' m 'ss' s'");
		String fileName = "./setting"  +"_" + sdf.format(timestamp) + ".csv";
    	FileOutputStream fo = new FileOutputStream(fileName);
		ObjectOutputStream objOut = new ObjectOutputStream(fo);

		pObj.dimensionDispChk =  dimensionDispChk.isSelected();
	    pObj.holeDispChk = holeDispChk.isSelected();
	    pObj.patternDispChk = patternDispChk.isSelected();
		pObj.camera_revers = camera_revers_chk.isSelected();

    	parameter para = pObj.para[pObj.select];//コードを見やすくする為参照を取得
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

		para.dim_1_enable = dim_1_enable.isSelected();
		para.dim_2_enable = dim_2_enable.isSelected();
		para.dim_Enable[0] = dim_1_enable.isSelected();
		para.dim_Enable[1] = dim_1_enable.isSelected();
		para.dim_Enable[2] = dim_2_enable.isSelected();
		para.dim_Enable[3] = dim_2_enable.isSelected();
		para.dim_Enable[4] = dim_1_enable.isSelected();


	    para.dim_offset_P2[0] = Double.valueOf(dim_offset_P2_1.getText());
	    para.dim_offset_F[0] = Double.valueOf(dim_offset_F_1.getText());
	    para.dim_offset_E[0] = Double.valueOf(dim_offset_E_1.getText());
	    para.dim_offset_P2[1] = Double.valueOf(dim_offset_P2_2.getText());
	    para.dim_offset_F[1] = Double.valueOf(dim_offset_F_2.getText());

		para.dimPixel_mm_offset = Double.valueOf(dimSetting_offset.getText());

		pObj.portNo = portNoSpin.getValue().intValue();
		pObj.cameraID = camIDspinner.getValue().intValue();
		pObj.adc_thresh = Integer.valueOf(adc_thresh_value.getText());
		pObj.cameraHeight = Integer.valueOf(capH_text.getText());
		pObj.cameraWidth = Integer.valueOf(capW_text.getText());
		pObj.adcFlg = adc_flg.isSelected();

		para.delly = dellySpinner.getValue().intValue();
		para.autogain = autoGainChk.isSelected();
		para.targetGain = Double.valueOf( targetLuminance.getText() );

		objOut.writeObject(pObj);//このメソッドを変更する必要がある
		objOut.flush();
		objOut.close();

		//パターンマッチング画像の保存 ptmImgMat[preSetNo][ptm1～ptm4]
    	int max_ptm_array = parameter.ptm_arrySize;
		for(int i=0;i<4;i++) {
			for(int j=0;j<max_ptm_array;j++) {
				if( ptm_ImgMat[i][j] != null ) {
					savePtmImg(ptm_ImgMat[i][j],"ptm"+String.format("_%d_%d", i,j));
				}
			}
		}
		//寸法測定用画像保存
		for(int i=0;i<4;i++) {
			for(int j=0;j<5;j++) {
				if( dim_ImgMat[i][j] != null ) {
					savePtmImg(dim_ImgMat[i][j],"dim"+String.format("_%d_%d", i,j));
				}
			}
		}

		Platform.runLater( () ->info2.appendText("設定がCSVへエクスポートされました。\n"));
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
	    	FileInputStream fi = new FileInputStream("./conf403.txt");
	    	ObjectInputStream objIn = new ObjectInputStream(fi);

	    	pObj = (preSet)objIn.readObject();
	    	objIn.close();
    	}catch(Exception e) {
    		System.out.println(e);
    		pObj = new preSet();
    	}

    	parameter para = pObj.para[pObj.select];

    	//選択されている品種の表示
    	Platform.runLater( () ->presetText.setText(pObj.presetNameText[pObj.select]));

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

    	camIDspinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9,pObj.cameraID,1));
    	Platform.runLater( () ->adc_thresh_value.setText( String.valueOf(pObj.adc_thresh)));
    	Platform.runLater( () ->capH_text.setText( String.valueOf(pObj.cameraHeight)));
    	Platform.runLater( () ->capW_text.setText( String.valueOf(pObj.cameraWidth)));
    	adc_flg.setSelected(pObj.adcFlg);

    	Platform.runLater( () ->camera_revers_chk.setSelected(pObj.camera_revers));

    	//パターンマッチング部
    	ptm_selectNo.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 49,0,1));
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

    	//2023.11.02追加
    	autoGain_target = para.targetGain;
    	Platform.runLater( () ->targetLuminance.setText(String.format("%.0f", autoGain_target)));
    	Platform.runLater( () ->autoGainChk.setSelected( para.autogain  ));
    	Platform.runLater( () ->dimSetting_offset.setText(String.format("%.0f", para.dimPixel_mm_offset )));

    	//2023.11.22 露出の設定
    	double exp = para.exposur;
    	capObj.set(Videoio.CAP_PROP_EXPOSURE,exp);
      	Platform.runLater( () ->cameraExpro.setText(String.format("%.0f", exp)));


    	//品種の選択コンボボックスのデーターロード
		Platform.runLater( () ->info2.appendText("設定がロードされました。\n"));

    }

    //設定をCSVファイルへ書き出す
    @FXML
    void onCSVout(ActionEvent event) {
    	try {
			saveCsvAllPara();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			Platform.runLater( () ->info2.appendText("CSV書き出し中に例外が発生\n"));
		}

    }

    /**
     * パターンマッチング用パラメータ設定
     */
    private void ptm_patternMatchParaSet() {
    	parameter para = pObj.para[pObj.select];
        for(int i=0;i<ptm_tmpara.arrayCnt;i++) {
        	ptm_tmpara.matchingTreshDetectCnt[i] = para.ptm_DetectCnt[i];//検出個数の判定数
        	ptm_tmpara.matchingThresh[i] = para.ptm_threshValue[i];//判定閾値
        	ptm_tmpara.matchingThresh_K[i] = para.ptm_threshValue_K[i];//警報閾値
        	ptm_tmpara.matchingDispersionThresh[i] = para.ptm_dispersionThreshValue[i];//分散判定閾値
        	ptm_tmpara.matchingDispersionThresh_K[i] = para.ptm_dispersionThreshValue_K[i];//分散警報閾値

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
     *パターンマッチング有効化チェックボックスのイベント
     * @param event
     */
    @FXML
    void onPtmEnabeChk(MouseEvent event) {
    	parameter para = pObj.para[pObj.select];
    	para.ptm_Enable[ptm_selectNo.getValue()] = ptm_pt_enable.isSelected();
    }

    /**
     * 寸法測定用パターンマッチングパラメータ設定
     * マッチングには、警報値と分散は不使用の為、設定していない
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
    		for( int j=0;j<parameter.ptm_arrySize;j++) {
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
    	updateImageView(ptm_img, Utils.mat2Image(ptm_ImgMat[pObj.select][ptm_selectNo.getValue()]));
    	ptm_pt_enable.setSelected(pObj.para[pObj.select].ptm_Enable[ptm_selectNo.getValue()]);

    	//寸法測定用画像
    	for( int i=0;i<4;i++) {
    		for( int j=0;j<5;j++) {
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
    	updateImageView(this.dim_edge_1, Utils.mat2Image(dim_ImgMat[pObj.select][4]));//E寸用

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

    	//パターンマッチング部
    	loadPtmImg();

        //パターンマッチング用パラメータ設定
    	ptm_patternMatchParaSet();

    	//選択されている品種の表示
    	Platform.runLater( () ->presetText.setText(pObj.presetNameText[pObj.select]));

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
    	ngSignalKeizokuFlg = false;
    	allSaveCnt = 0;
    	DispersionErrorNG = false;//パターンマッチングの分散NGが連続して発生した場合に立つフラグをクリアする

    	Platform.runLater(() ->ngCounterLabel.setText(String.valueOf(ngCnt)));
    	Platform.runLater(() ->aPane.setStyle("-fx-background-radius: 0;-fx-background-color: #a5abb094;"));

    	//Platform.runLater( () ->FileClass.fileClass(new File("./ng_image/")) );
    	//Platform.runLater( () ->FileClass.fileClass(new File("./ok_image/")) );

    	Platform.runLater(() ->this.imgNG.setImage(null));

    	Platform.runLater(() ->info2.clear());
    	Platform.runLater(() ->info2.setText(initInfo2));
    	//Platform.runLater(() ->info2.appendText("NG/OK画像ファイルを全て削除しました。\n"));
    	//Platform.runLater(() ->info2.appendText("NG画像ファイルを全て削除しました。\n"));

    	//チャートデーターのクリア
    	for(int i=0;i<2;i++) {
    		int _i = i;
    		Platform.runLater( () ->dataset_F[_i].getSeries(0).clear());
    		Platform.runLater( () ->dataset_P2[_i].getSeries(0).clear());
    		if( i == 0) {Platform.runLater( () ->dataset_E[_i].getSeries(0).clear());}
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
		for(int g=0;g<2;g++) {
			final int g2=g;
			Platform.runLater( () ->dim_table.getItems().get(g2*2).P2Property().set(0.0));
			Platform.runLater( () ->dim_table.getItems().get(g2*2).FProperty().set(0.0));
			if( g2 == 0) {
				Platform.runLater( () ->dim_table.getItems().get(g2*2).EProperty().set(0.0));
			}
			Platform.runLater( () ->dim_table.getItems().get(g2*2+1).P2Property().set(0.0));
			Platform.runLater( () ->dim_table.getItems().get(g2*2+1).FProperty().set(0.0));
			if( g2 == 0) {
				Platform.runLater( () ->dim_table.getItems().get(g2*2+1).EProperty().set(0.0));
			}
		}
		//E寸警告ラベル非表示
		Platform.runLater( () ->E_warningLabel.setVisible(false));

		P2_sum[0] = 0;P2_sum[1] = 0;
		F_sum[0] = 0;F_sum[1] = 0;
		E_sum[0] = 0;E_sum[1] = 0;

		//ログデーター処理用
		logdata.csvWrite();
		logdata.clear();

		shotCnt= 0;
		shutterSignal4secInterval=false;//NG画像保存開始
    	Platform.runLater( () ->judg.setText("-"));
    	Platform.runLater( () ->judg.setTextFill(Color.GREEN));
		Platform.runLater( () ->info2.appendText("\nクリア信号が受信されました\n"));

    }

	/**
	 * 設定モード切替
	 * @param event
	 */
    @FXML
    void onSettingModeBtn(ActionEvent event) {
    	if( !settingModeFlg) {//ロックするときはパスワード不要
		    FXMLLoader loader = new FXMLLoader(getClass().getResource("passwordDialog.fxml"));
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
			stage.initModality(Modality.WINDOW_MODAL);//モーダルダイアログにする為
			stage.initOwner(((Node)event.getSource()).getScene().getWindow() );//モーダルダイアログにする為
			stage.showAndWait();
			if( PasswordDialogController.flg == false ){//パスワードが不一致の場合
	    		Platform.runLater(() ->info2.appendText("*********************\n"));
	    		Platform.runLater(() ->info2.appendText("パスワードが違います\n"));
	    		Platform.runLater(() ->info2.appendText("*********************\n"));
	    		return;

			}
    	}

    	if( settingModeFlg ) {

    		Platform.runLater(() ->this.accordion_1.setDisable(true));
        	settingModeFlg = false;
        	Platform.runLater(() ->FilterViewMode.setSelected(false));
        	Platform.runLater(() ->info1.setText(""));
        	draggingRect = new Rectangle(1,1,1,1);
        	saveImgUseFlg = false;
        	offCalibLite();
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
    	onPtmSelectNoSP(ptm_selectNo.getValue());
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
    	int selectNo= this.ptm_selectNo.getValue();

    	parameter para = pObj.para[pObj.select];

		//パラメーターを渡す
		PtmView.arg_ptmSrcMat = srcMat.clone();

		PtmView.arg_ptmMat = ptm_ImgMat[pObj.select][selectNo].clone();

		PtmView.arg_ptmMat_mask_rect = para.ptm_ptmMat_mask_rect[selectNo];
		PtmView.arg_ptm_templatRect = para.ptm_templatRect[selectNo];

		PtmView.arg_detectionCnt = para.ptm_DetectCnt[selectNo];

		PtmView.arg_gauusianCheck = para.ptm_fil_gauusianCheck[selectNo];
		PtmView.arg_gauusianSliderX = para.ptm_fil_gauusianX[selectNo];
		PtmView.arg_gauusianSliderY = para.ptm_fil_gauusianY[selectNo];
		PtmView.arg_gauusianSliderA = para.ptm_fil_gauusianValue[selectNo];

		PtmView.arg_dilateCheck = para.ptm_fil_dilateCheck[selectNo];
		PtmView.arg_dilateSliderN = para.ptm_fil_dilateValue[selectNo];

		PtmView.arg_erodeCheck = para.ptm_fil_erodeCheck[selectNo];
		PtmView.arg_erodeSliderN = para.ptm_fil_erodeValue[selectNo];

		PtmView.arg_threshholdCheck = para.ptm_fil_threshholdCheck[selectNo];
		PtmView.arg_threshhold_Inverse = para.ptm_fil_threshhold_Invers[selectNo];
		PtmView.arg_threshholdSlider = para.ptm_fil_threshholdValue[selectNo];//2値化閾値

		PtmView.arg_cannyCheck = para.ptm_fil_cannyCheck[selectNo];
		PtmView.arg_cannyThresh1 = para.ptm_fil_cannyThresh1[selectNo];
		PtmView.arg_cannyThresh2 = para.ptm_fil_cannyThresh2[selectNo];

		PtmView.arg_ptmThreshSliderN = para.ptm_threshValue[selectNo];//判定閾値
		PtmView.arg_ptmThreshSliderN_K = para.ptm_threshValue_K[selectNo];//警報閾値

		PtmView.arg_ptmDispersionThreshSliderN = para.ptm_dispersionThreshValue[selectNo];//分散判定閾値
		PtmView.arg_ptmDispersionThreshSliderN_K = para.ptm_dispersionThreshValue_K[selectNo];//分散警報閾値

		PtmView.arg_zoomValue_slider = para.ptm_zoomValue_slider[selectNo];
		PtmView.arg_rectsDetection =  para.ptm_rectsDetection[selectNo];//検出範囲

		PtmView.arg_detectionScale = para.ptm_detectionScale[selectNo];//検出倍率の逆数

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
			ptm_ImgMat[pObj.select][selectNo] = PtmView.arg_ptmMat;
			para.ptm_ptmMat_mask_rect[selectNo] = PtmView.arg_ptmMat_mask_rect;
			para.ptm_templatRect[selectNo] = PtmView.arg_ptm_templatRect;

			updateImageView(ptm_img, Utils.mat2Image(ptm_ImgMat[pObj.select][selectNo]));

			para.ptm_DetectCnt[selectNo] = PtmView.arg_detectionCnt;

			para.ptm_fil_gauusianCheck[selectNo] = PtmView.arg_gauusianCheck;
			para.ptm_fil_gauusianX[selectNo] = PtmView.arg_gauusianSliderX;
			para.ptm_fil_gauusianY[selectNo]  = PtmView.arg_gauusianSliderY;
			para.ptm_fil_gauusianValue[selectNo] = PtmView.arg_gauusianSliderA;

			para.ptm_fil_dilateCheck[selectNo] = PtmView.arg_dilateCheck;
			para.ptm_fil_dilateValue[selectNo] = PtmView.arg_dilateSliderN;

			para.ptm_fil_erodeCheck[selectNo] = PtmView.arg_erodeCheck;
			para.ptm_fil_erodeValue[selectNo] = PtmView.arg_erodeSliderN;

			para.ptm_fil_threshholdCheck[selectNo] = PtmView.arg_threshholdCheck;
			para.ptm_fil_threshhold_Invers[selectNo] = PtmView.arg_threshhold_Inverse;
			para.ptm_fil_threshholdValue[selectNo] = PtmView.arg_threshholdSlider;//2値化閾値

			para.ptm_fil_cannyCheck[selectNo] = PtmView.arg_cannyCheck;
			para.ptm_fil_cannyThresh1[selectNo] = PtmView.arg_cannyThresh1;
			para.ptm_fil_cannyThresh2[selectNo] = PtmView.arg_cannyThresh2;

			para.ptm_threshValue[selectNo] = PtmView.arg_ptmThreshSliderN;//判定閾値
			para.ptm_threshValue_K[selectNo] = PtmView.arg_ptmThreshSliderN_K;//警報閾値

			para.ptm_dispersionThreshValue[selectNo] = PtmView.arg_ptmDispersionThreshSliderN;//分散判定閾値
			para.ptm_dispersionThreshValue_K[selectNo] = PtmView.arg_ptmDispersionThreshSliderN_K;//分散警報閾値

			para.ptm_zoomValue_slider[selectNo] = PtmView.arg_zoomValue_slider;
			para.ptm_rectsDetection[selectNo] =  PtmView.arg_rectsDetection;//検出範囲
			para.ptm_detectionScale[selectNo] = PtmView.arg_detectionScale;//検出倍率の逆数
		}
        //パターンマッチング用パラメータ設定
    	ptm_patternMatchParaSet();
    }

    @FXML
    void onDimEnabeChk(MouseEvent event) {
    	dim_templateMatchingObj.tmpara.ptmEnable[0] = this.dim_1_enable.isSelected();
    	dim_templateMatchingObj.tmpara.ptmEnable[1] = this.dim_1_enable.isSelected();
    	dim_templateMatchingObj.tmpara.ptmEnable[2] = this.dim_2_enable.isSelected();
    	dim_templateMatchingObj.tmpara.ptmEnable[3] = this.dim_2_enable.isSelected();
    	dim_templateMatchingObj.tmpara.ptmEnable[4] = this.dim_1_enable.isSelected();

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
    	}else if( obj == dim_set_para5 ) {
    		selectBtn = 4;
    		iv = dim_edge_1;
    	}else {
    		return;
    	}

    	parameter para = pObj.para[pObj.select];

		//パラメーターを渡す
		PtmView.arg_ptmSrcMat = srcMat.clone();

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
	    		pixel_mm = 4 / holeDist_DimSetting;
	    	}catch( java.lang.NumberFormatException e) {
	    		pixel_mm = 0.0;
	    	}
	    	final Double tmp_pixel_mm = pixel_mm;
	    	Platform.runLater( () ->this.dimSettingLabel.setText(String.format("%.3f μm/pixel", tmp_pixel_mm*1000)));
	    	pObj.para[pObj.select].dimPixel_mm = pixel_mm;
	    	pObj.para[pObj.select].dimPixel_mm_offset = Double.valueOf(dimSetting_offset.getText())/1000.0;//換算後のオフセットをテキストフィールドから取得
    	}
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
    	String e_offset_1 = dim_offset_E_1.getText();
    	String p2_offset_2 = dim_offset_P2_2.getText();
    	String f_offset_2 = dim_offset_F_2.getText();

    	try {
    		double d_p2_offset_1 = Double.valueOf(p2_offset_1);
    		double d_f_offset_1 = Double.valueOf(f_offset_1);
    		double d_e_offset_1 = Double.valueOf(e_offset_1);
    		double d_p2_offset_2 = Double.valueOf(p2_offset_2);
    		double d_f_offset_2 = Double.valueOf(f_offset_2);

    		parameter para = pObj.para[pObj.select];
    		para.dim_offset_P2[0] = d_p2_offset_1;
    		para.dim_offset_P2[1] = d_p2_offset_2;
    		para.dim_offset_F[0] = d_f_offset_1;
    		para.dim_offset_F[1] = d_f_offset_2;
    		para.dim_offset_E[0] = d_e_offset_1;
    	}catch(Exception e2) {
    		Platform.runLater( () ->this.info2.appendText("offsetは数値で入力してください\n"));
    	}
    }

    /**
     * 照明キャリブレーション終了
     */
    void offCalibLite() {
    	if( calibLiteFlg ) {
	    	VisonController2.timerCalib.shutdown();
			try {
				VisonController2.timerCalib.awaitTermination(33, TimeUnit.MICROSECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			calibLiteFlg = false;
			Platform.runLater(() ->info2.appendText("**************************\n"));
			Platform.runLater(() ->info2.appendText("照明キャリブレーション終了\n"));
			Platform.runLater(() ->info2.appendText("**************************\n"));
			Platform.runLater( () ->throughImageChk.setSelected(false));
    	}
    }

    /**
     * USBカメラのゲインをデフォルトにする
     */
    void setDefultGain() {
    	capObj.set(Videoio.CAP_PROP_GAIN,50.0);
    }

    /**
     * USBカメラオートゲイン
     * @param luminanceAverage 穴認識領域の平均輝度　規格100±2 #55=137
     * @return 0:調整不要  1:調整実施 2:調整範囲オーバー
     */
    int exeAutoGain(double luminanceAverage) {
    	if( luminanceAverage >=autoGain_target-1 && luminanceAverage<=autoGain_target+1) {
        	Platform.runLater( () ->calib_label.setText(String.format("平均輝度=%.1f",luminanceAverage)));
    		return 0;
    	}
    	double gain = capObj.get(Videoio.CAP_PROP_GAIN);
    	//ゲインの調整幅20～80
    	if( gain <=20 || gain >=80) {
    		Platform.runLater( () ->calib_label.setText(String.format("平均輝度=%.1f",luminanceAverage)));
    		return 2;
    	}

    	if( luminanceAverage < autoGain_target) {
    		gain = gain + 0.5;
    	}else {
    		gain = gain - 0.5;
    	}
		capObj.set(Videoio.CAP_PROP_GAIN,gain);
		final double ga = gain;
		Platform.runLater( () ->this.info2.appendText(	String.format("照明ゲイン補正 %.1f \n", ga)));
		Platform.runLater( () ->this.autoGainText.setText(String.format("%.1f",ga)));

    	final double g = gain;
    	Platform.runLater( () ->autoGainText.setText(String.format("%.1f",g)));
    	Platform.runLater( () ->calib_label.setText(String.format("平均輝度=%.1f",luminanceAverage)));

    	return 1;
    }

    /**
     * 照明キャリブレーション開始
     * @param event
     */
    @FXML
    void onCalibLite(ActionEvent event) {
    	if( demoFlg ) return;

    	//現在のゲインを取得
    	//capObj.set(50.0);//強制的にゲインを50%にセットする。意図的に2021.11.16現在は実行しない
    	double gain = capObj.get(Videoio.CAP_PROP_GAIN);
    	if( gain<40 || gain>60) {
			Platform.runLater(() ->info2.appendText("初期ゲインを40～60の間に設定する必要があります\n"));
    		return;
    	}

    	if( calibLiteFlg ) {
    		throughImageChk.setSelected(false);//スルー画像表示フラグ無効
    		offCalibLite();
			return;
    	}

    	throughImageChk.setSelected(true);//スルー画像表示フラグ有効 RealMatがスレッドで自動取得される
       	calibLiteFlg = true;//キャリブレーション中フラグセット
		Platform.runLater(() ->info2.appendText("**************************\n"));
		Platform.runLater(() ->info2.appendText("照明キャリブレーション開始\n"));
		Platform.runLater(() ->info2.appendText("**************************\n"));
		Platform.runLater( () ->throughImageChk.setSelected(true));

		Runnable calibLiter = new Runnable() {
			private boolean judgFlg = false;

			@Override
			public void run() {
		    	if( realMat == null) {
					Platform.runLater(() ->info2.appendText("スルー画像準備中...\n"));
		    		return;
		    	}

				if( triggerCCircle.getFill() != Color.YELLOW) {
		    		Platform.runLater( () ->triggerCCircle.setFill(Color.YELLOW));
		    	}else {
		    		Platform.runLater( () ->triggerCCircle.setFill(Color.WHITE));
		    	}

		    	parameter para = pObj.para[pObj.select];
		    	Double luminanceAverage = 0.0;
		    	int cnt = 0;
		    	for( int i=0;i<4;i++) {
			    	if( para.hole_DetectFlg[i] ) {
				    	Rectangle r = para.hole_rects[i];//検査範囲
				    	luminanceAverage += Core.mean(realMat.submat(new Rect(r.x,r.y,r.width,r.height))).val[0];
				    	cnt++;
			    	}
		    	}

		    	if( cnt > 0 ){
		    		luminanceAverage = luminanceAverage / cnt;
		        	final double d = luminanceAverage;
		        	Platform.runLater( () ->calib_label.setText(
		        			String.format("平均輝度=%.1f",d)));
		        	if( d>=autoGain_target-2 && d<=autoGain_target+2 ) {
		        		if( !judgFlg )
		        			Platform.runLater( () ->calib_label.setText( "照明キャリブレーション合格"));
		        		judgFlg =true;
		        	}else {
		        		if(judgFlg) {
			        		Platform.runLater( () ->calib_label.setText( "照明強度要調整"));
		        			judgFlg = false;
		        		}
		        	}
		    	}else {
		        	Platform.runLater( () ->calib_label.setText( "領域設定不足"));
		    	}
		    }
		};

		timerCalib = Executors.newSingleThreadScheduledExecutor();
		timerCalib.scheduleAtFixedRate(calibLiter, 0, 100, TimeUnit.MILLISECONDS);


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
        chart_E = new JFreeChart[2];
        chartTab_E = new Tab[2];
        dataset_P2 = new XYSeriesCollection[2];
        dataset_F = new XYSeriesCollection[2];
        dataset_E = new XYSeriesCollection[2];

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
		//E寸警告ラベル非表示
		Platform.runLater( () ->E_warningLabel.setVisible(false));

		//イニシャルinfo2の内容保存
		initInfo2 = this.info2.getText();

		//パターンマッチング用オブジェクトの初期化
		ptm_tmpara = new TMpara(parameter.ptm_arrySize);

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
                        	ptm_patternMatchParaSet();//パターンマッチングのアコーディオンを閉じたときに変更を反映させる
                    	}
                  }
            });
        accordion_1.setExpandedPane(TiledPaneHardSET);

        //*************************************************************************************************************
        //パターンマッチング設定選択用スピナー　イベントリスナー
        ptm_selectNo.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            	onPtmSelectNoSP(Integer.valueOf(newValue));

        });
        //*************************************************************************************************************

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
        Platform.runLater( () ->GPIO_STATUS_PIN3.setFill(Color.YELLOW));

        loadAllPara();
    }

    /**
     * パターンマッチング　セレクタスピナーイベント
     * @param event
     */
    void onPtmSelectNoSP( int no ) {
    	parameter para = pObj.para[pObj.select];

    	updateImageView(ptm_img, Utils.mat2Image(ptm_ImgMat[pObj.select][no]));
    	Platform.runLater(() ->ptm_pt_enable.setSelected(para.ptm_Enable[no]));

    	Mat tmpMat = srcMat.clone();
        Imgproc.rectangle(tmpMat,
        		new Point(para.ptm_rectsDetection[no].getX(),para.ptm_rectsDetection[no].getY()),
        		new Point(para.ptm_rectsDetection[no].getWidth()+para.ptm_rectsDetection[no].getX(),
        				para.ptm_rectsDetection[no].getHeight()+para.ptm_rectsDetection[no].getY()),
        		new Scalar(255,0,0),3);
    	updateImageView(imgORG, Utils.mat2Image(tmpMat));

    	String info = "判定個数= " + para.ptm_DetectCnt[no] +"\n" +
    				"判定閾値= " + String.format("%.2f", para.ptm_threshValue[no]) + "\n" +
    				"警報閾値= " + String.format("%.2f", para.ptm_threshValue_K[no]) + "\n";

    	Platform.runLater(() ->this.ptm_textArea.setText(info));
    }

    private void chartFact() {
		dataset_E[0] = getChartData_E();
        chart_E[0] = createInitChart(String.valueOf(1)+"列目 E","(mm)","n",dataset_E[0],
        					baseParameterValue.E_LowerLimit_dimensionTheshold - 0.05,
        					baseParameterValue.E_UpperLimit_dimensionTheshold + 0.05);
        ChartViewer chV3 = new ChartViewer(chart_E[0]);
        chV3.addChartMouseListener( new ChartMouseListenerFX() {
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
        chartTab_E[0] = new Tab(String.valueOf(1)+"列目 E      ",chV3);
        dataTabpane.getTabs().add(chartTab_E[0]);

        for(int i=0;i<2;i++) {
        	dataset_P2[i] = getChartData_P2();
	        chart_P2[i] = createInitChart(String.valueOf(i+1)+"列目 P2","(mm)","n",dataset_P2[i] ,
	        			baseParameterValue.P2_LowerLimit_dimensionTheshold - 0.05,
	        			baseParameterValue.P2_UpperLimit_dimensionTheshold + 0.05);
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
	        chart_F[i] = createInitChart(String.valueOf(i+1)+"列目 F","(mm)","n",dataset_F[i],
	        					baseParameterValue.F_LowerLimit_dimensionTheshold - 0.05,
	        					baseParameterValue.F_UpperLimit_dimensionTheshold + 0.05);
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

    /**
     * トリガタイミング自動調整 2022.12.27
     * @param event
     */
    @FXML
    void onTrgTimingCalib(ActionEvent event) {
    	if( trgTimingCalib.isSelected() ) {
        	Platform.runLater(() ->info2.appendText("トリガタイミング自動調整が開始されました。\n"));
        	trgCalibCnt = 0;
    		trgCalibFlg = true;
    		trgCalibTime = System.currentTimeMillis();

    	}else {
    		trgCalibFlg = false;
        	Platform.runLater(() ->info2.appendText("トリガタイミング自動調整が終了しました。\n"));

        	if( trgCalibCnt == 10 ) {

        	}
    	}
    }

}
