package application;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;

/**
 * 検出パラメーター群　シリアライズ化対応
 * @author Mamoru
 *
 */
public class parameter implements Serializable {
	//シャッタータイミング
	public boolean trigger_2nd_chk;
	public int delly;
	public int delly2;

	final int hole_arrySize = 5;
	double hole_zoom;
	double[] hole_viewRect;
	int[] hole_cntHoleTh;
    boolean[] hole_fil_gauusianCheck;//ガウシアンフィルタ
    double[] hole_fil_gauusianX;//sigmaX
    double[] hole_fil_gauusianY;//sigmaY
    double[] hole_fil_gauusianValue;//アパーチャサイズ
    boolean[] hole_fil_dilateCheck;//収縮フィルタ
    int[] hole_fil_dilateValue;//dilateの回数
    boolean[] hole_fil_threshholdCheck;
    boolean[] hole_fil_threshhold_Invers;
    double[] hole_fil_threshhold;


	//[4]はドラッグ中の枠用
	double[]	hole_circlePara4, //投票分解能
				hole_circlePara5, //円最小距離
				hole_circlePara6, //２値化パラ① CANYフィルタの大きい方の値
				hole_circlePara7, //投票率
				hole_circlePara8, //検出円最小
				hole_circlePara9; //検出円最大
	Rectangle[] hole_rects; //検出ウィンドウのサイズ
	boolean[] hole_DetectFlg; //検出実行フラグ
	public int[] hole_whiteAreaMax;
	public int[] hole_whiteAreaMin;

	static final int ptm_arrySize = 20;
	String[] ptm_FileName;//パターンマッチングの登録Mat
	Rectangle[] ptm_templatRect;//テンプレートの切り出し位置
	Rectangle[] ptm_ptmMat_mask_rect;////x,y:テンプレートからの相対位置 width,height:矩形サイズ

	boolean[] ptm_Enable;//パターンマッチング有効無効フラグ
	int[] ptm_DetectCnt;//検出数
	public Rectangle[] ptm_rectsDetection;//検出エリア
	double[] ptm_detectionScale;//パターンマッチングの検出に使用するスケール倍率の逆数
	public double[] ptm_zoomValue_slider;
	public double[] ptm_threshValue;//検出閾値
	public double[] ptm_threshValue_K;//警報閾値
	public double[] ptm_dispersionThreshValue;//分散閾値 0.0で無効
	public double[] ptm_dispersionThreshValue_K;//分散警報閾値 0.0で無効

	//パターンマッチングに適用されるフィルタ
	//public int[] ptm_fil_detectionCnt;
	public boolean[] ptm_fil_threshholdCheck;
	public boolean[] ptm_fil_threshhold_Invers;
	public double[] ptm_fil_threshholdValue;
    public boolean[] ptm_fil_gauusianCheck;
	public double[] ptm_fil_gauusianX;
	public double[] ptm_fil_gauusianY;
	public double[] ptm_fil_gauusianValue;
	public boolean[] ptm_fil_dilateCheck;
	public double[] ptm_fil_dilateValue;
	public boolean[] ptm_fil_erodeCheck;
	public double[] ptm_fil_erodeValue;
	public boolean[] ptm_fil_cannyCheck;
	public double[] ptm_fil_cannyThresh1;
	public double[] ptm_fil_cannyThresh2;

	//寸法測定
	public boolean dim_1_enable = false;
	public boolean dim_2_enable = false;
	public Double dimPixel_mm = 0.035;//ピクセル-mm換算
	public double[] dim_offset_P2 = new double[2];
	public double[] dim_offset_F = new double[2];
	public double[] dim_offset_E = new double[2];

	final int dim_arrySize = 4;
	String[] dim_FileName;//パターンマッチングの登録Mat
	Rectangle[] dim_templatRect;//テンプレートの切り出し位置
	Rectangle[] dim_ptmMat_mask_rect;////x,y:テンプレートからの相対位置 width,height:矩形サイズ

	int[] dim_DetectCnt;//検出数 1個固定
	public Rectangle[] dim_rectsDetection;//検出エリア
	double[] dim_detectionScale;//パターンマッチングの検出に使用するスケール倍率の逆数
	public double[] dim_zoomValue_slider;
	public double[] dim_threshValue;//検出閾値
	public Point[] dim_centerPosition;
	//パターンマッチングに適用されるフィルタ
	public int[] dim_fil_detectionCnt;
	public boolean[] dim_fil_threshholdCheck;
	public boolean[] dim_fil_threshhold_Invers;
	public double[] dim_fil_threshholdValue;
    public boolean[] dim_fil_gauusianCheck;
	public double[] dim_fil_gauusianX;
	public double[] dim_fil_gauusianY;
	public double[] dim_fil_gauusianValue;
	public boolean[] dim_fil_dilateCheck;
	public double[] dim_fil_dilateValue;
	public boolean[] dim_fil_erodeCheck;
	public double[] dim_fil_erodeValue;
	public boolean[] dim_fil_cannyCheck;
	public double[] dim_fil_cannyThresh1;
	public double[] dim_fil_cannyThresh2;
	public boolean[] dim_Enable;

	public parameter() {
		delly = 550;
		delly2 = 50;

		hole_zoom = 1.0;
		hole_viewRect = new double[hole_arrySize];
		hole_cntHoleTh = new int[hole_arrySize];
		hole_cntHoleTh[0]=18;
		hole_cntHoleTh[1]=6;
		hole_cntHoleTh[2]=18;
		hole_cntHoleTh[3]=6;

		hole_circlePara4 = new double[hole_arrySize];
		hole_circlePara5 = new double[hole_arrySize];
		hole_circlePara6 = new double[hole_arrySize];
		hole_circlePara7 = new double[hole_arrySize];
		hole_circlePara8 = new double[hole_arrySize];
		hole_circlePara9 = new double[hole_arrySize];
		hole_rects = new Rectangle[hole_arrySize];
		hole_DetectFlg = new boolean[hole_arrySize];

		hole_fil_gauusianCheck = new boolean[hole_arrySize];
		hole_fil_gauusianX = new double[hole_arrySize];
		hole_fil_gauusianY = new double[hole_arrySize];
		hole_fil_gauusianValue = new double[hole_arrySize];
		hole_fil_dilateCheck = new boolean[hole_arrySize];
		hole_fil_dilateValue = new int[hole_arrySize];

		hole_fil_threshhold = new double[hole_arrySize];
		hole_fil_threshholdCheck  = new boolean[hole_arrySize];
		hole_fil_threshhold_Invers  = new boolean[hole_arrySize];

		hole_whiteAreaMax = new int[hole_arrySize];
		hole_whiteAreaMin = new int[hole_arrySize];

		ptm_fil_threshholdCheck = new boolean[hole_arrySize];
		ptm_fil_threshhold_Invers = new boolean[hole_arrySize];

		ptm_DetectCnt = new int[ptm_arrySize];
		ptm_Enable = new boolean[ptm_arrySize];
		//ptm_fil_detectionCnt = new int[ptm_arrySize];
		ptm_detectionScale = new double[ptm_arrySize];

		ptm_fil_gauusianCheck = new boolean[ptm_arrySize];
		ptm_fil_gauusianX = new double[ptm_arrySize];
		ptm_fil_gauusianY = new double[ptm_arrySize];
		ptm_fil_gauusianValue = new double[ptm_arrySize];

		ptm_fil_dilateCheck = new boolean[ptm_arrySize];
		ptm_fil_dilateValue = new double[ptm_arrySize];

		ptm_fil_erodeCheck = new boolean[ptm_arrySize];
		ptm_fil_erodeValue = new double[ptm_arrySize];

		ptm_fil_threshholdCheck = new boolean[ptm_arrySize];
		ptm_fil_threshhold_Invers = new boolean[ptm_arrySize];
		ptm_fil_threshholdValue = new double[ptm_arrySize];

		ptm_fil_cannyCheck = new boolean[ptm_arrySize];
		ptm_fil_cannyThresh1 = new double[ptm_arrySize];
		ptm_fil_cannyThresh2 = new double[ptm_arrySize];

		ptm_threshValue = new double[ptm_arrySize];
		ptm_threshValue_K = new double[ptm_arrySize];

		ptm_dispersionThreshValue = new double[ptm_arrySize];
		ptm_dispersionThreshValue_K = new double[ptm_arrySize];

		ptm_zoomValue_slider = new double[ptm_arrySize];
		ptm_rectsDetection = new Rectangle[ptm_arrySize];
		//マスク処理用
		ptm_templatRect = new Rectangle[ptm_arrySize];
		ptm_ptmMat_mask_rect = new Rectangle[ptm_arrySize];
		//--------------寸法測定------------------------
		//マスク処理用
		dim_templatRect = new Rectangle[dim_arrySize];
		dim_ptmMat_mask_rect = new Rectangle[dim_arrySize];

		dim_DetectCnt = new int[dim_arrySize];
		dim_fil_detectionCnt = new int[dim_arrySize];
		dim_detectionScale = new double[dim_arrySize];

		dim_fil_gauusianCheck = new boolean[dim_arrySize];
		dim_fil_gauusianX = new double[dim_arrySize];
		dim_fil_gauusianY = new double[dim_arrySize];
		dim_fil_gauusianValue = new double[dim_arrySize];

		dim_fil_dilateCheck = new boolean[dim_arrySize];
		dim_fil_dilateValue = new double[dim_arrySize];

		dim_fil_erodeCheck = new boolean[dim_arrySize];
		dim_fil_erodeValue = new double[dim_arrySize];

		dim_fil_threshholdCheck = new boolean[dim_arrySize];
		dim_fil_threshhold_Invers = new boolean[dim_arrySize];
		dim_fil_threshholdValue = new double[dim_arrySize];

		dim_fil_cannyCheck = new boolean[dim_arrySize];
		dim_fil_cannyThresh1 = new double[dim_arrySize];
		dim_fil_cannyThresh2 = new double[dim_arrySize];

		dim_threshValue = new double[dim_arrySize];
		dim_zoomValue_slider = new double[dim_arrySize];
		dim_rectsDetection = new Rectangle[dim_arrySize];
		dim_centerPosition = new Point[dim_arrySize];

		dim_Enable = new boolean[dim_arrySize];
		//------------------------------------------------

		for(int i=0;i<hole_arrySize;i++){
			hole_circlePara4[i] = 2;
			hole_circlePara5[i] = 10;
			hole_circlePara6[i] = 100;
			hole_circlePara7[i] = 10;
			hole_circlePara8[i] = 3;
			hole_circlePara9[i] = 20;
			hole_rects[i] = new Rectangle(0,0,1,1);
			hole_DetectFlg[i] = false;
			hole_fil_gauusianCheck[i] = false;
			hole_fil_gauusianX[i] = 1;
			hole_fil_gauusianY[i] = 1;
			hole_fil_gauusianValue[i] = 10;
			hole_fil_dilateCheck[i] = false;
			hole_fil_dilateValue[i] = 1;
			hole_fil_threshholdCheck[i] = false;
			hole_fil_threshhold[i] = 128;
			hole_fil_threshhold_Invers[i] = false;
			hole_whiteAreaMax[i] = 3000;
			hole_whiteAreaMin[i] = 3000;
		}
		for(int i=0;i<ptm_arrySize;i++) {
			ptm_Enable[i] = false;
			ptm_DetectCnt[i] = 4;
			ptm_detectionScale[i] = 3;
			ptm_rectsDetection[i] = new Rectangle();

			ptm_fil_threshholdCheck[i] = false;
			ptm_fil_threshhold_Invers[i] = false;
			ptm_fil_threshholdValue[i] = 128;
			ptm_fil_gauusianCheck[i] = false;
			ptm_fil_gauusianX[i] = 0;
			ptm_fil_gauusianY[i] = 0;
			ptm_fil_gauusianValue[i] = 0;

			ptm_fil_dilateCheck[i] = false;
			ptm_fil_dilateValue[i] = 0;

			ptm_fil_erodeCheck[i] = false;
			ptm_fil_erodeValue[i] = 0;

			ptm_fil_cannyCheck[i] = false;
			ptm_fil_cannyThresh1[i] = 0;
			ptm_fil_cannyThresh2[i] = 0;

			ptm_threshValue[i] = 0.8;
			ptm_threshValue_K[i] = 0.9;

			ptm_dispersionThreshValue[i] = 0.0;
			ptm_dispersionThreshValue_K[i] =0.0;
			ptm_zoomValue_slider[i] = 0.3;

			ptm_templatRect[i] = new Rectangle();
			ptm_ptmMat_mask_rect[i] = new Rectangle();
		}
		for(int i=0;i<dim_arrySize;i++) {
			dim_templatRect[i] = new Rectangle();
			dim_ptmMat_mask_rect[i] = new Rectangle();

			dim_DetectCnt[i] = 1;
			dim_detectionScale[i] = 3;
			dim_rectsDetection[i] = new Rectangle();

			dim_fil_threshholdCheck[i] = false;
			dim_fil_threshhold_Invers[i] = false;
			dim_fil_threshholdValue[i] = 128;
			dim_fil_gauusianCheck[i] = false;
			dim_fil_gauusianX[i] = 0;
			dim_fil_gauusianY[i] = 0;
			dim_fil_gauusianValue[i] = 0;

			dim_fil_dilateCheck[i] = false;
			dim_fil_dilateValue[i] = 0;

			dim_fil_erodeCheck[i] = false;
			dim_fil_erodeValue[i] = 0;

			dim_fil_threshholdValue[i] = 0;

			dim_fil_cannyCheck[i] = false;
			dim_fil_cannyThresh1[i] = 0;
			dim_fil_cannyThresh2[i] = 0;

			dim_threshValue[i] = 0.8;
			dim_zoomValue_slider[i] = 0.3;

			dim_Enable[i]=false;
		}
	}
}
