package application;

import java.awt.Rectangle;

import org.opencv.core.Mat;

/**
 * テンプレートマッチングに使用する構造体クラス
 * @author Mamoru
 *
 */
public class TMpara implements Cloneable{
	public int arrayCnt = 4;
	public int[] matchingTreshDetectCnt;//マッチングパターン数の閾値
	public double[] matchingThresh;//テンプレートマッチングの閾値
	public Mat[] paternMat;//パターンマッチングの登録Mat
	public boolean[] ptmEnable;//パターンマッチング有効無効フラグ
	public Rectangle[] detectionRects;//パターンマッチング検出エリア
	public double[] scale;//パターンマッチングの検出に使用するスケール倍率の逆数

	boolean[]ptm_fil_gauusianCheck;
	double[] ptm_fil_gauusianX;
	double[] ptm_fil_gauusianY;
	double[] ptm_fil_gauusianValue;
	boolean[] hole_fil_threshholdCheck;
	boolean[] hole_fil_threshhold_Invers;
	double[] ptm_fil_threshholdValue;
	boolean[] ptm_fil_dilateCheck;
	double[] ptm_fil_dilateValue;
	boolean[] ptm_fil_erodeCheck;
	double[] ptm_fil_erodeValue;
	boolean[] ptm_fil_cannyCheck;
	double[] ptm_fil_cannyThresh1;
	double[] ptm_fil_cannyThresh2;


	public TMpara() {
		matchingTreshDetectCnt = new int[arrayCnt];
		matchingThresh = new double[arrayCnt];
		paternMat = new Mat[arrayCnt];
		ptmEnable = new boolean[arrayCnt];
		detectionRects = new Rectangle[arrayCnt];
		scale = new double[arrayCnt];

		ptm_fil_gauusianCheck = new boolean[arrayCnt];
		ptm_fil_gauusianX = new double[arrayCnt];
		ptm_fil_gauusianY = new double[arrayCnt];
		ptm_fil_gauusianValue = new double[arrayCnt];
		hole_fil_threshholdCheck = new boolean[arrayCnt];
		hole_fil_threshhold_Invers = new boolean[arrayCnt];
		ptm_fil_threshholdValue = new double[arrayCnt];
		ptm_fil_dilateCheck = new boolean[arrayCnt];
		ptm_fil_dilateValue = new double[arrayCnt];
		ptm_fil_erodeCheck = new boolean[arrayCnt];
		ptm_fil_erodeValue = new double[arrayCnt];
		ptm_fil_cannyCheck = new boolean[arrayCnt];
		ptm_fil_cannyThresh1 = new double[arrayCnt];
		ptm_fil_cannyThresh2 = new double[arrayCnt];
	}

	public TMpara(int arg_arrayCnt) {
		arrayCnt = arg_arrayCnt;

		matchingTreshDetectCnt = new int[arrayCnt];
		matchingThresh = new double[arrayCnt];
		paternMat = new Mat[arrayCnt];
		ptmEnable = new boolean[arrayCnt];
		detectionRects = new Rectangle[arrayCnt];
		scale = new double[arrayCnt];

		ptm_fil_gauusianCheck = new boolean[arrayCnt];
		ptm_fil_gauusianX = new double[arrayCnt];
		ptm_fil_gauusianY = new double[arrayCnt];
		ptm_fil_gauusianValue = new double[arrayCnt];
		hole_fil_threshholdCheck = new boolean[arrayCnt];
		hole_fil_threshhold_Invers = new boolean[arrayCnt];
		ptm_fil_threshholdValue = new double[arrayCnt];
		ptm_fil_dilateCheck = new boolean[arrayCnt];
		ptm_fil_dilateValue = new double[arrayCnt];
		ptm_fil_erodeCheck = new boolean[arrayCnt];
		ptm_fil_erodeValue = new double[arrayCnt];
		ptm_fil_cannyCheck = new boolean[arrayCnt];
		ptm_fil_cannyThresh1 = new double[arrayCnt];
		ptm_fil_cannyThresh2 = new double[arrayCnt];	}

	@Override
    public TMpara clone() {
    	TMpara b = new TMpara(this.arrayCnt);

        b.matchingTreshDetectCnt = this.matchingTreshDetectCnt;
        b.matchingThresh = this.matchingThresh;
        b.ptmEnable = this.ptmEnable;
        b.scale = this.scale;

        b.ptm_fil_gauusianCheck = this.ptm_fil_gauusianCheck;
		b.ptm_fil_gauusianX = this.ptm_fil_gauusianX;
		b.ptm_fil_gauusianY = this.ptm_fil_gauusianY;
		b.ptm_fil_gauusianValue = this.ptm_fil_gauusianValue;
		b.hole_fil_threshholdCheck = this.hole_fil_threshholdCheck;
		b.hole_fil_threshhold_Invers = this.hole_fil_threshhold_Invers;
		b.ptm_fil_threshholdValue = this.ptm_fil_threshholdValue;
		b.ptm_fil_dilateCheck = this.ptm_fil_dilateCheck;
		b.ptm_fil_dilateValue = this.ptm_fil_dilateValue;
		b.ptm_fil_erodeCheck = this.ptm_fil_erodeCheck;
		b.ptm_fil_erodeValue = this.ptm_fil_erodeValue;
		b.ptm_fil_cannyCheck = this.ptm_fil_cannyCheck;
		b.ptm_fil_cannyThresh1 = this.ptm_fil_cannyThresh1;
		b.ptm_fil_cannyThresh2 = this.ptm_fil_cannyThresh2;

        for(int i=0;i<this.arrayCnt;i++ ) {
            b.paternMat[i] = (Mat)this.paternMat[i].clone();
            b.detectionRects[i] = (Rectangle)this.detectionRects[i].clone();
        }
        return b;
    }
}
