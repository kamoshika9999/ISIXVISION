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
    boolean[] threshholdCheck;
    double[] threshhold;
    boolean[] threshhold_Invers;
	int[] matchCnt;
	double[] matchThreshValue;

	//[4]はドラッグ中の枠用
	double[]	circlePara4, //投票分解能
				circlePara5, //円最小距離
				circlePara6, //２値化パラ① CANYフィルタの大きい方の値
				circlePara7, //投票率
				circlePara8, //検出円最小
				circlePara9; //検出円最大
	Rectangle[] rects; //検出ウィンドウのサイズ
	boolean[] setFlg; //検出実行フラグ

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

		threshholdCheck = new boolean[arrySize];
		threshhold = new double[arrySize];
		threshhold_Invers = new boolean[arrySize];

		matchCnt = new int[arrySize];
		matchThreshValue = new double[arrySize];

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
			threshholdCheck[i] = false;
			threshhold[i] = 128;
			threshhold_Invers[i] = false;
			matchCnt[i] = 6;
			matchThreshValue[i] = 0.8;
		}
	}
}
