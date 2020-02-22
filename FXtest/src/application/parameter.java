package application;

import java.awt.Rectangle;
import java.io.Serializable;

/**
 * 検出パラメーター群　シリアライズ化対応
 * @author Mamoru
 *
 */
public class parameter implements Serializable {
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

	final int ptm_arrySize = 4;
	String[] ptm_FileName;//パターンマッチングの登録Mat
	boolean[] ptm_Enable;//パターンマッチング有効無効フラグ
	int[] ptm_DetectCnt;//検出数
	public Rectangle[] ptm_rectsDetection;//検出エリア
	double[] ptm_detectionScale;//パターンマッチングの検出に使用するスケール倍率の逆数
	public double[] ptm_zoomValue_slider;
	public double[] ptm_threshValue;//検出閾値

	//パターンマッチングに適用されるフィルタ
	public int[] ptm_fil_detectionCnt;
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

	public parameter() {
		hole_zoom = 1.0;
		hole_viewRect = new double[hole_arrySize];
		hole_cntHoleTh = new int[hole_arrySize];
		hole_cntHoleTh[0]=9;
		hole_cntHoleTh[1]=3;
		hole_cntHoleTh[2]=9;
		hole_cntHoleTh[3]=3;

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

		hole_fil_threshholdCheck = new boolean[hole_arrySize];
		hole_fil_threshhold = new double[hole_arrySize];
		hole_fil_threshhold_Invers = new boolean[hole_arrySize];

		hole_whiteAreaMax = new int[hole_arrySize];
		hole_whiteAreaMin = new int[hole_arrySize];

		ptm_DetectCnt = new int[ptm_arrySize];
		ptm_Enable = new boolean[ptm_arrySize];
		ptm_fil_detectionCnt = new int[ptm_arrySize];
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
		ptm_zoomValue_slider = new double[ptm_arrySize];
		ptm_rectsDetection = new Rectangle[ptm_arrySize];


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

			ptm_fil_threshholdValue[i] = 0;

			ptm_fil_cannyCheck[i] = false;
			ptm_fil_cannyThresh1[i] = 0;
			ptm_fil_cannyThresh2[i] = 0;

			ptm_threshValue[i] = 0.8;
			ptm_zoomValue_slider[i] = 0.3;


		}
	}
}
