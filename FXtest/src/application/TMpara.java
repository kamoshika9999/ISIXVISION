package application;

import java.awt.Rectangle;

import org.opencv.core.Mat;

public class TMpara implements Cloneable{
	public int arrayCnt = 4;
	public int[] matchCnt;
	public double[] thresh;
	public Mat[] ptnMat;//パターンマッチングの登録Mat
	public boolean[] ptmEnable;//パターンマッチング有効無効フラグ
	public int[] ptmDetectCnt;
	public Rectangle[] para_rectsDetection;//パターンマッチング検出エリア
	public double[] scale;//パターンマッチングの検出に使用するスケール倍率の逆数

	public TMpara() {
		matchCnt = new int[arrayCnt];
		thresh = new double[arrayCnt];
		ptnMat = new Mat[arrayCnt];
		ptmEnable = new boolean[arrayCnt];
		ptmDetectCnt = new int[arrayCnt];
		para_rectsDetection = new Rectangle[arrayCnt];
		scale = new double[arrayCnt];
	}

	public TMpara(int arg_arrayCnt) {
		arrayCnt = arg_arrayCnt;

		matchCnt = new int[arrayCnt];
		thresh = new double[arrayCnt];
		ptnMat = new Mat[arrayCnt];
		ptmEnable = new boolean[arrayCnt];
		ptmDetectCnt = new int[arrayCnt];
		para_rectsDetection = new Rectangle[arrayCnt];
		scale = new double[arrayCnt];
	}
    @Override
    public TMpara clone() { //基本的にはpublic修飾子を付け、自分自身の型を返り値とする
    	TMpara b=null;

        /*ObjectクラスのcloneメソッドはCloneNotSupportedExceptionを投げる可能性があるので、try-catch文で記述(呼び出し元に投げても良い)*/
        try {
            b=(TMpara)super.clone(); //親クラスのcloneメソッドを呼び出す(親クラスの型で返ってくるので、自分自身の型でのキャストを忘れないようにする)

            b.matchCnt = this.matchCnt;
            b.thresh = this.thresh;
            b.ptmEnable = this.ptmEnable;
            b.ptmDetectCnt = this.ptmDetectCnt;
            b.scale = this.scale;

            for(int i=0;i<this.arrayCnt;i++ ) {
                b.ptnMat[i] = (Mat)this.ptnMat[i].clone();
                b.para_rectsDetection[i] = (Rectangle)this.para_rectsDetection[i].clone();

            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return b;
    }

}
