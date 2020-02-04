package application;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class templateMatching {
	private Mat areaMat;
	private Rect[] detectRect;
	private Mat[] ptnMat;
	private Mat resultMat;
	private double[] threshhold;

	public TMResult[] resultValue;//検出結果が保存される

	/**
	 * コンストラクタ
	 * @param arg_areaMat
	 * @param arg_detectRect
	 * @param arg_ptnMat
	 * @param arg_resultMat
	 * @param arg_threshhold
	 */
	public templateMatching(
			Mat arg_areaMat,
			Rect[] arg_detectRect,
			Mat[] arg_ptnMat,
			Mat arg_resultMat,
			double[] arg_threshhold
			) {

		areaMat = arg_areaMat;
		detectRect = arg_detectRect;
		ptnMat = arg_ptnMat;
		resultMat = arg_resultMat;
		threshhold = arg_threshhold;

		resultValue = new TMResult[detectRect.length];
		for(int i=0;i<detectRect.length;i++)
			resultValue[i] = new TMResult();
	}

	public templateMatching(Mat srcMat, Mat dstMat, TMpara tmpara) {
		areaMat = srcMat;
		detectRect = rectangleToRect(tmpara.para_rectsDetection);
		ptnMat = tmpara.ptmMat;
		resultMat = dstMat;
		threshhold = tmpara.matchThreshValue;

		resultValue = new TMResult[detectRect.length];
		for(int i=0;i<detectRect.length;i++)
			resultValue[i] = new TMResult();
	}
    private Rect[] rectangleToRect(Rectangle[] rectangle) {
    	Rect[] rc = new Rect[rectangle.length];
    	for(int i=0;i<rectangle.length;i++) {
    		rc[i] = new Rect(rectangle[i].x,rectangle[i].y,rectangle[i].width,rectangle[i].height);
    	}
    	return rc;
    }




	/**
	 * パターン検出処理
	 * @return
	 */
	public Mat detectPattern() {
		for(int n=0;n<detectRect.length;n++) {
			//比較結果を格納するMatを生成
			Mat roi = areaMat.submat(detectRect[n]);
			Mat orgroi = resultMat.submat(detectRect[n]);

			boolean flg2;
			double rt;
			List<Point> finedPoint = new ArrayList<>();
			if( roi.width() > ptnMat[n].width() && roi.height() > ptnMat[n].height() ) {

		    	Mat result = new Mat(roi.rows() - ptnMat[n].rows() + 1, roi.cols() - ptnMat[n].cols() + 1, CvType.CV_32FC1);
			    	//テンプレートマッチ実行（TM_CCOEFF_NORMED：相関係数＋正規化）
		    	Imgproc.matchTemplate(roi, ptnMat[n], result, Imgproc.TM_CCOEFF_NORMED);
		    	//結果から相関係数がしきい値以下を削除（０にする）
		    	Imgproc.threshold(result, result,threshhold[n],1.0, Imgproc.THRESH_TOZERO);


		    	int tmpPtWidth =  ptnMat[n].width()/2;//テンプレート画像の1/2近傍チェック用
		    	int tmpPtHeight = ptnMat[n].height()/2;
		    	for (int i=0;i<result.rows();i++) {
		    		for (int j=0;j<result.cols();j++) {
		    			rt = result.get(i, j)[0];
		    			flg2=false;
		    			if ( rt > 0) {
		    				//近傍に検出エリアが無いか確認 テンプレート画像の1/2近傍
		    				if( !finedPoint.isEmpty() ) {
			    				for(Point p:finedPoint) {
			    					if( p.x  + tmpPtWidth > j && p.x - tmpPtWidth < j &&
			    							p.y  + tmpPtHeight > i && p.y - tmpPtHeight < i) {
			    						flg2 = true;
			    						break;
			    					}
			    				}
		    					if( !flg2 ){
		    						resultValue[n].cnt++;
		    						finedPoint.add(new Point(j,i));
		    	    		    	Imgproc.rectangle(orgroi,new Point(j,i),
		    	    		    			new Point(j+ptnMat[n].width(),i+ptnMat[n].height()),
		    	    		    			new Scalar(0,255,255),3);
									Imgproc.putText(orgroi,
											String.format("%.3f", rt),
											new Point(j,i),
											Imgproc.FONT_HERSHEY_SIMPLEX, 1.5,new Scalar(0,255,255),2);
		    	    		    	j = (int) (j + ptnMat[n].cols() + tmpPtWidth);
		    	    		    	resultValue[n].detectMax = resultValue[n].detectMax<rt?rt:resultValue[n].detectMax;
		    	    		    	resultValue[n].detectMin = resultValue[n].detectMin>rt?rt:resultValue[n].detectMin;
		    	    		    	resultValue[n].detectAve += rt;
		    	    		    	break;
		    					}
		    				}else {
		    					resultValue[n].cnt++;
								finedPoint.add(new Point(j,i));
			    		    	Imgproc.rectangle(orgroi,new Point(j,i),
			    		    			new Point(j+ptnMat[n].width(),i+ptnMat[n].height()),
			    		    			new Scalar(0,255,255),3);
								Imgproc.putText(orgroi,
										String.format("%.3f", rt),
										new Point(j,i),
										Imgproc.FONT_HERSHEY_SIMPLEX, 1.5,new Scalar(0,255,255),2);
			    		    	j = (int) (j + ptnMat[n].cols() + tmpPtWidth);
			    		    	resultValue[n].detectMax = resultValue[n].detectMax<rt?rt:resultValue[n].detectMax;
			    		    	resultValue[n].detectMin = resultValue[n].detectMin>rt?rt:resultValue[n].detectMin;
			    		    	resultValue[n].detectAve += rt;
		    				}
		    			}
		    		}
		   		 }
			}else {
				System.out.println("サイズ不正");
			}
			resultValue[n].detectAve /= (double)resultValue[n].cnt;
		}

		return resultMat;
	}

	/**
	 * パターン検出処理2 画像サイズ1/scaleで探索
	 * @param scale
	 * @return
	 */
	public Mat detectPattern2(double scale) {
		for(int n=0;n<detectRect.length;n++) {
			//エリア画像と登録画像を縮小
			Imgproc.resize(areaMat, areaMat, new Size(),1/scale,1/scale,Imgproc.INTER_AREA );
			Imgproc.resize(ptnMat[n], ptnMat[n], new Size(),1/scale,1/scale,Imgproc.INTER_AREA );

			//比較結果を格納するMatを生成
			Mat orgroi = resultMat.submat(detectRect[n]);//scale適用前に生成

			detectRect[n].x /= scale;
			detectRect[n].y /= scale;
			detectRect[n].width /= scale;
			detectRect[n].height /= scale;
			Mat roi = areaMat.submat(detectRect[n]);

			boolean flg2;
			double rt;
			List<Point> finedPoint = new ArrayList<>();
			if( roi.width() > ptnMat[n].width() && roi.height() > ptnMat[n].height() ) {

		    	Mat result = new Mat(roi.rows() - ptnMat[n].rows() + 1, roi.cols() - ptnMat[n].cols() + 1, CvType.CV_32FC1);
			    	//テンプレートマッチ実行（TM_CCOEFF_NORMED：相関係数＋正規化）
		    	Imgproc.matchTemplate(roi, ptnMat[n], result, Imgproc.TM_CCOEFF_NORMED);
		    	//結果から相関係数がしきい値以下を削除（０にする）
		    	Imgproc.threshold(result, result,threshhold[n],1.0, Imgproc.THRESH_TOZERO);


		    	int tmpPtWidth =  ptnMat[n].width()/2;//テンプレート画像の1/2近傍チェック用
		    	int tmpPtHeight = ptnMat[n].height()/2;
		    	for (int i=0;i<result.rows();i++) {
		    		for (int j=0;j<result.cols();j++) {
		    			rt = result.get(i, j)[0];
		    			flg2=false;
		    			if ( rt > 0) {
		    				//近傍に検出エリアが無いか確認 テンプレート画像の1/2近傍
		    				if( !finedPoint.isEmpty() ) {
			    				for(Point p:finedPoint) {
			    					if( p.x  + tmpPtWidth > j && p.x - tmpPtWidth < j &&
			    							p.y  + tmpPtHeight > i && p.y - tmpPtHeight < i) {
			    						flg2 = true;
			    						break;
			    					}
			    				}
		    					if( !flg2 ){
		    						resultValue[n].cnt++;
		    						finedPoint.add(new Point(j,i));
		    	    		    	Imgproc.rectangle(orgroi,new Point(j*scale,i*scale),
		    	    		    			new Point((j+ptnMat[n].width())*scale,(i+ptnMat[n].height())*scale),
		    	    		    			new Scalar(0,255,255),3);
									Imgproc.putText(orgroi,
											String.format("%.3f", rt),
											new Point(j*scale,i*scale),
											Imgproc.FONT_HERSHEY_SIMPLEX, 1.5,new Scalar(0,255,255),2);
		    	    		    	j = (int) (j + ptnMat[n].cols() + tmpPtWidth);
		    	    		    	resultValue[n].detectMax = resultValue[n].detectMax<rt?rt:resultValue[n].detectMax;
		    	    		    	resultValue[n].detectMin = resultValue[n].detectMin>rt?rt:resultValue[n].detectMin;
		    	    		    	resultValue[n].detectAve += rt;
		    	    		    	break;
		    					}
		    				}else {
		    					resultValue[n].cnt++;
								finedPoint.add(new Point(j,i));
			    		    	Imgproc.rectangle(orgroi,new Point(j*scale,i*scale),
			    		    			new Point((j+ptnMat[n].width())*scale,(i+ptnMat[n].height())*scale),
			    		    			new Scalar(0,255,255),3);
								Imgproc.putText(orgroi,
										String.format("%.3f", rt),
										new Point(j*scale,i*scale),
										Imgproc.FONT_HERSHEY_SIMPLEX, 1.5,new Scalar(0,255,255),2);
			    		    	j = (int) (j + ptnMat[n].cols() + tmpPtWidth);
			    		    	resultValue[n].detectMax = resultValue[n].detectMax<rt?rt:resultValue[n].detectMax;
			    		    	resultValue[n].detectMin = resultValue[n].detectMin>rt?rt:resultValue[n].detectMin;
			    		    	resultValue[n].detectAve += rt;
		    				}
		    			}
		    		}
		   		 }
			}else {
				System.out.println("サイズ不正");
			}
			resultValue[n].detectAve /= (double)resultValue[n].cnt;
		}

		return resultMat;
	}

}
