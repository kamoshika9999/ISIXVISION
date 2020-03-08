package application;

import java.awt.Rectangle;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

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

	public boolean[] ptm_fil_gauusianCheck;
	public double[] ptm_fil_gauusianX;
	public double[] ptm_fil_gauusianY;
	public double[] ptm_fil_gauusianValue;
	public boolean[] ptm_fil_threshholdCheck;
	public boolean[] ptm_fil_threshhold_Invers;
	public double[] ptm_fil_threshholdValue;
	public boolean[] ptm_fil_dilateCheck;
	public double[] ptm_fil_dilateValue;
	public boolean[] ptm_fil_erodeCheck;
	public double[] ptm_fil_erodeValue;
	public boolean[] ptm_fil_cannyCheck;
	public double[] ptm_fil_cannyThresh1;
	public double[] ptm_fil_cannyThresh2;

	public Rectangle[] ptm_ptmMat_mask_rect;
	public Mat[] ptm_ptmMat_mask;


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
		ptm_fil_threshholdCheck = new boolean[arrayCnt];
		ptm_fil_threshhold_Invers = new boolean[arrayCnt];
		ptm_fil_threshholdValue = new double[arrayCnt];
		ptm_fil_dilateCheck = new boolean[arrayCnt];
		ptm_fil_dilateValue = new double[arrayCnt];
		ptm_fil_erodeCheck = new boolean[arrayCnt];
		ptm_fil_erodeValue = new double[arrayCnt];
		ptm_fil_cannyCheck = new boolean[arrayCnt];
		ptm_fil_cannyThresh1 = new double[arrayCnt];
		ptm_fil_cannyThresh2 = new double[arrayCnt];

		ptm_ptmMat_mask_rect = new Rectangle[arrayCnt];
		ptm_ptmMat_mask = new Mat[arrayCnt];


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
		ptm_fil_threshholdCheck = new boolean[arrayCnt];
		ptm_fil_threshhold_Invers = new boolean[arrayCnt];
		ptm_fil_threshholdValue = new double[arrayCnt];
		ptm_fil_dilateCheck = new boolean[arrayCnt];
		ptm_fil_dilateValue = new double[arrayCnt];
		ptm_fil_erodeCheck = new boolean[arrayCnt];
		ptm_fil_erodeValue = new double[arrayCnt];
		ptm_fil_cannyCheck = new boolean[arrayCnt];
		ptm_fil_cannyThresh1 = new double[arrayCnt];
		ptm_fil_cannyThresh2 = new double[arrayCnt];

		ptm_ptmMat_mask_rect = new Rectangle[arrayCnt];
		ptm_ptmMat_mask = new Mat[arrayCnt];

	}

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
		b.ptm_fil_threshholdCheck = this.ptm_fil_threshholdCheck;
		b.ptm_fil_threshhold_Invers = this.ptm_fil_threshhold_Invers;
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
            b.ptm_ptmMat_mask[i] = (Mat)this.ptm_ptmMat_mask[i].clone();
            b.ptm_ptmMat_mask_rect[i] = (Rectangle)this.ptm_ptmMat_mask_rect[i].clone();
            b.detectionRects[i] = (Rectangle)this.detectionRects[i].clone();
        }
        return b;
    }


    //テンプレートマッチング用マスクMat作成
    public void createMaskMat() {
		for(int i=0;i<ptm_ptmMat_mask_rect.length;i++) {
			ptm_ptmMat_mask[i] = new Mat(paternMat[i].height(),paternMat[i].width(),CvType.CV_8UC1,new Scalar(255));
			if(ptm_ptmMat_mask_rect[i] != null) {
		    	if( ptm_ptmMat_mask_rect[i].width > 1 && ptm_ptmMat_mask_rect[i].height > 1 ) {
			    	Imgproc.rectangle(ptm_ptmMat_mask[i],
			    			new Point(ptm_ptmMat_mask_rect[i].x,ptm_ptmMat_mask_rect[i].y),
			    			new Point(ptm_ptmMat_mask_rect[i].x+ptm_ptmMat_mask_rect[i].width,
			    					ptm_ptmMat_mask_rect[i].y+ptm_ptmMat_mask_rect[i].height),
			    			new Scalar(0),Imgproc.FILLED);
		    	}
			}
		}
    }
}
