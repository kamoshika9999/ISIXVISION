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
	public parameter para = null;

	public TMpara() {
		matchingTreshDetectCnt = new int[arrayCnt];
		matchingThresh = new double[arrayCnt];
		paternMat = new Mat[arrayCnt];
		ptmEnable = new boolean[arrayCnt];
		detectionRects = new Rectangle[arrayCnt];
		scale = new double[arrayCnt];
	}

	public TMpara(int arg_arrayCnt) {
		arrayCnt = arg_arrayCnt;

		matchingTreshDetectCnt = new int[arrayCnt];
		matchingThresh = new double[arrayCnt];
		paternMat = new Mat[arrayCnt];
		ptmEnable = new boolean[arrayCnt];
		detectionRects = new Rectangle[arrayCnt];
		scale = new double[arrayCnt];
	}

	@Override
    public TMpara clone() {
    	TMpara b = new TMpara(this.arrayCnt);

        b.matchingTreshDetectCnt = this.matchingTreshDetectCnt;
        b.matchingThresh = this.matchingThresh;
        b.ptmEnable = this.ptmEnable;
        b.scale = this.scale;
        b.para = this.para;

        for(int i=0;i<this.arrayCnt;i++ ) {
            b.paternMat[i] = (Mat)this.paternMat[i].clone();
            b.detectionRects[i] = (Rectangle)this.detectionRects[i].clone();
        }
        return b;
    }
}
