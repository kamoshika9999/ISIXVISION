package application;

import java.awt.Rectangle;

import org.opencv.core.Mat;

public class TMpara implements Cloneable{
	public int arrayCnt = 4;
	public int[] matchCnt;
	public double[] thresh;
	public Mat[] paternMat;//パターンマッチングの登録Mat
	public boolean[] ptmEnable;//パターンマッチング有効無効フラグ
	public int[] ptmDetectCnt;
	public Rectangle[] detectionRects;//パターンマッチング検出エリア
	public double[] scale;//パターンマッチングの検出に使用するスケール倍率の逆数

	public TMpara() {
		matchCnt = new int[arrayCnt];
		thresh = new double[arrayCnt];
		paternMat = new Mat[arrayCnt];
		ptmEnable = new boolean[arrayCnt];
		ptmDetectCnt = new int[arrayCnt];
		detectionRects = new Rectangle[arrayCnt];
		scale = new double[arrayCnt];
	}

	public TMpara(int arg_arrayCnt) {
		arrayCnt = arg_arrayCnt;

		matchCnt = new int[arrayCnt];
		thresh = new double[arrayCnt];
		paternMat = new Mat[arrayCnt];
		ptmEnable = new boolean[arrayCnt];
		ptmDetectCnt = new int[arrayCnt];
		detectionRects = new Rectangle[arrayCnt];
		scale = new double[arrayCnt];
	}
    @Override
    public TMpara clone() { //基本的にはpublic修飾子を付け、自分自身の型を返り値とする
    	TMpara b = new TMpara();

        /*ObjectクラスのcloneメソッドはCloneNotSupportedExceptionを投げる可能性があるので、try-catch文で記述(呼び出し元に投げても良い)*/
        try {

            b.matchCnt = this.matchCnt;
            b.thresh = this.thresh;
            b.ptmEnable = this.ptmEnable;
            b.ptmDetectCnt = this.ptmDetectCnt;
            b.scale = this.scale;

            for(int i=0;i<this.arrayCnt;i++ ) {
                b.paternMat[i] = (Mat)this.paternMat[i].clone();
                b.detectionRects[i] = (Rectangle)this.detectionRects[i].clone();

            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return b;
    }

}
