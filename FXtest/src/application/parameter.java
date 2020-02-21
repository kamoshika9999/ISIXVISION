package application;

import java.awt.Rectangle;
import java.io.Serializable;

public class parameter implements Serializable {
	int arrySize;
	double zoom;
	double[] viewRect;
	int[] cntHoleTh;
    boolean[] gauusianCheck;//ガウシアンフィルタ
    double[] gauusianSliderX;//sigmaX
    double[] gauusianSliderY;//sigmaY
    double[] gauusianSliderA;//アパーチャサイズ
    boolean[] dilateCheck;//収縮フィルタ
    int[] dilateSliderN;//dilateの回数
    double[] threshhold;


	//[4]はドラッグ中の枠用
	double[]	circlePara4, //投票分解能
				circlePara5, //円最小距離
				circlePara6, //２値化パラ① CANYフィルタの大きい方の値
				circlePara7, //投票率
				circlePara8, //検出円最小
				circlePara9; //検出円最大
	Rectangle[] rects; //検出ウィンドウのサイズ
	boolean[] setFlg; //検出実行フラグ
	public int[] whiteAreaMax;
	public int[] whiteAreaMin;

	double[] matchThreshValue;
	String[] ptmFileName;//パターンマッチングの登録Mat
	boolean[] ptmEnable;//パターンマッチング有効無効フラグ
	int[] ptmDetectCnt;
	public Rectangle[] ptm_rectsDetection;
	double[] ptm_detectionScale;//パターンマッチングの検出に使用するスケール倍率の逆数
	public int[] ptm_detectionCnt;
    boolean[] ptm_threshholdCheck;
    boolean[] ptm_threshhold_Invers;
    public boolean[] ptm_gauusianCheck;
	public double[] ptm_gauusianSliderX;
	public double[] ptm_gauusianSliderY;
	public double[] ptm_gauusianSliderA;
	public boolean[] ptm_dilateCheck;
	public double[] ptm_dilateSliderN;
	public boolean[] ptm_erodeCheck;
	public double[] ptm_erodeSliderN;
	public boolean[] para_threshholdCheck;
	public double[] ptm_threshholdSlider;
	public boolean[] ptm_cannyCheck;
	public double[] ptm_cannyThresh1;
	public double[] ptm_cannyThresh2;
	public double[] ptm_threshValue;
	public double[] ptm_zoomValue_slider;


	public parameter() {
		arrySize = 5;
		zoom = 1.0;
		viewRect = new double[arrySize];
		cntHoleTh = new int[arrySize];
		cntHoleTh[0]=9;
		cntHoleTh[1]=3;
		cntHoleTh[2]=9;
		cntHoleTh[3]=3;

		circlePara4 = new double[arrySize];
		circlePara5 = new double[arrySize];
		circlePara6 = new double[arrySize];
		circlePara7 = new double[arrySize];
		circlePara8 = new double[arrySize];
		circlePara9 = new double[arrySize];
		rects = new Rectangle[arrySize];
		setFlg = new boolean[arrySize];

		gauusianCheck = new boolean[arrySize];
		gauusianSliderX = new double[arrySize];
		gauusianSliderY = new double[arrySize];
		gauusianSliderA = new double[arrySize];
		dilateCheck = new boolean[arrySize];
		dilateSliderN = new int[arrySize];

		ptm_threshholdCheck = new boolean[arrySize];
		threshhold = new double[arrySize];
		ptm_threshhold_Invers = new boolean[arrySize];

		matchThreshValue = new double[arrySize];
		ptmDetectCnt = new int[arrySize];
		ptmEnable = new boolean[arrySize];
		ptm_detectionCnt = new int[arrySize];
		ptm_detectionScale = new double[arrySize];

		ptm_gauusianCheck = new boolean[arrySize];
		ptm_gauusianSliderX = new double[arrySize];
		ptm_gauusianSliderY = new double[arrySize];
		ptm_gauusianSliderA = new double[arrySize];

		ptm_dilateCheck = new boolean[arrySize];
		ptm_dilateSliderN = new double[arrySize];

		ptm_erodeCheck = new boolean[arrySize];
		ptm_erodeSliderN = new double[arrySize];

		para_threshholdCheck = new boolean[arrySize];
		ptm_threshholdSlider = new double[arrySize];

		ptm_cannyCheck = new boolean[arrySize];
		ptm_cannyThresh1 = new double[arrySize];
		ptm_cannyThresh2 = new double[arrySize];

		ptm_threshValue = new double[arrySize];
		ptm_zoomValue_slider = new double[arrySize];
		ptm_rectsDetection = new Rectangle[arrySize];

		whiteAreaMax = new int[arrySize];
		whiteAreaMin = new int[arrySize];

		for(int i=0;i<arrySize;i++){
			circlePara4[i] = 2;
			circlePara5[i] = 10;
			circlePara6[i] = 100;
			circlePara7[i] = 10;
			circlePara8[i] = 3;
			circlePara9[i] = 20;
			rects[i] = new Rectangle(0,0,1,1);
			setFlg[i] = false;
			gauusianCheck[i] = false;
			gauusianSliderX[i] = 1;
			gauusianSliderY[i] = 1;
			gauusianSliderA[i] = 10;
			dilateCheck[i] = false;
			dilateSliderN[i] = 1;
			ptm_threshholdCheck[i] = false;
			threshhold[i] = 128;
			ptm_threshhold_Invers[i] = false;
			matchThreshValue[i] = 0.8;
			ptmEnable[i] = false;
			ptmDetectCnt[i] = 0;
			whiteAreaMax[i] = 3000;
			whiteAreaMin[i] = 3000;
			ptm_detectionScale[i] = 2;

			ptm_rectsDetection[i] = new Rectangle();
		}
	}
}
