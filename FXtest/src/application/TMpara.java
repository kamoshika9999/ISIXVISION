package application;

import java.awt.Rectangle;

import org.opencv.core.Mat;

/*
int[] matchCnt;
double[] matchThreshValue;
String[] ptmFileName;//パターンマッチングの登録Mat
boolean[] ptmEnable;//パターンマッチング有効無効フラグ
int[] ptmDetectCnt;
public Rectangle[] para_rectsDetection;
double[] detectionScale;//パターンマッチングの検出に使用するスケール倍率の逆数

public templateMatching(
		Mat arg_areaMat,
		Rect[] arg_detectRect,
		Mat[] arg_ptnMat,
		Mat arg_resultMat,
		double[] arg_threshhold
		)
*/
public class TMpara {
	public final int arrayCnt = 4;
	public int[] matchCnt;
	public double[] matchThreshValue;
	public Mat[] ptmMat;//パターンマッチングの登録Mat
	public boolean[] ptmEnable;//パターンマッチング有効無効フラグ
	public int[] ptmDetectCnt;
	public Rectangle[] para_rectsDetection;
	public double[] detectionScale;//パターンマッチングの検出に使用するスケール倍率の逆数

	public TMpara() {
		matchCnt = new int[arrayCnt];
		matchThreshValue = new double[arrayCnt];
		ptmMat = new Mat[arrayCnt];
		ptmEnable = new boolean[arrayCnt];
		ptmDetectCnt = new int[arrayCnt];
		para_rectsDetection = new Rectangle[arrayCnt];
		detectionScale = new double[arrayCnt];
	}

}
