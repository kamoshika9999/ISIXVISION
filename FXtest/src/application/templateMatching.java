package application;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class templateMatching {
/*TMpara.java
 * 	public final int arrayCnt = 4;
	public int[] matchCnt;
	public double[] matchThreshValue;
	public Mat[] ptmMat;//パターンマッチングの登録Mat
	public boolean[] ptmEnable;//パターンマッチング有効無効フラグ
	public int[] ptmDetectCnt;
	public Rectangle[] para_rectsDetection;
	public double[] detectionScale;//パターンマッチングの検出に使用するスケール倍率の逆数

 */

	public TMResult[] resultValue;//検出結果が保存される
	public TMpara tmpara;

	/**
	 * コンストラクタ
	 * @param arg_areaMat
	 * @param arg_detectRect
	 * @param arg_ptnMat
	 * @param arg_resultMat
	 * @param arg_threshhold
	 */
	public templateMatching(TMpara arg_tmpara) {
		tmpara = arg_tmpara.clone();
		resultValue = new TMResult[arg_tmpara.arrayCnt];
		for(int i=0;i<arg_tmpara.arrayCnt;i++)
			resultValue[i] = new TMResult();
	}

	/**
	 * パターン検出処理
	 * @param srcMat
	 * @param dstMat
	 * @return
	 */
	public boolean detectPattern(Mat arg_srcMat, Mat dstMat) {
		//if( tmpara == null) return false;
		/*
		 * dstMat 結果が書き込まれる
		 */
		boolean resultFlg = true;
		TMpara ctmpara = tmpara.clone();//毎回引数からクローンする

		for(int n=0;n<ctmpara.arrayCnt;n++) {
			if( ctmpara.ptmEnable[n] ) {
				Mat areaMat = arg_srcMat.clone();//毎回引数からクローンする
				
				//エリア画像と登録画像を縮小
				Imgproc.resize(areaMat, areaMat, new Size(),
						1/ctmpara.scale[n],1/ctmpara.scale[n],Imgproc.INTER_AREA );
				Imgproc.resize(ctmpara.ptnMat[n],ctmpara.ptnMat[n], new Size(),
						1/ctmpara.scale[n],1/ctmpara.scale[n],Imgproc.INTER_AREA );

				//比較結果を格納するMatを生成
				Mat orgroi = null;
				orgroi = dstMat.submat(
						ctmpara.detectionRects[n].y,
						ctmpara.detectionRects[n].y+ctmpara.detectionRects[n].height,
						ctmpara.detectionRects[n].x,
						ctmpara.detectionRects[n].x+ctmpara.detectionRects[n].width
						);//ctmpara.scale[n]適用前に生成

				ctmpara.detectionRects[n].x /= ctmpara.scale[n];
				ctmpara.detectionRects[n].y /= ctmpara.scale[n];
				ctmpara.detectionRects[n].width /= ctmpara.scale[n];
				ctmpara.detectionRects[n].height /= ctmpara.scale[n];
				Mat roi = areaMat.submat(
						ctmpara.detectionRects[n].y,
						ctmpara.detectionRects[n].y+ctmpara.detectionRects[n].height,
						ctmpara.detectionRects[n].x,
						ctmpara.detectionRects[n].x+ctmpara.detectionRects[n].width
						);

				boolean flg2;
				double rt;
				List<Point> finedPoint = new ArrayList<>();
				if( roi.width() > ctmpara.ptnMat[n].width() && roi.height() > ctmpara.ptnMat[n].height() ) {

			    	Mat result = new Mat(roi.rows() - ctmpara.ptnMat[n].rows() + 1, roi.cols() - ctmpara.ptnMat[n].cols() + 1, CvType.CV_32FC1);
				    	//テンプレートマッチ実行（TM_CCOEFF_NORMED：相関係数＋正規化）
			    	Imgproc.matchTemplate(roi, ctmpara.ptnMat[n], result, Imgproc.TM_CCOEFF_NORMED);
			    	//結果から相関係数がしきい値以下を削除（０にする）
			    	Imgproc.threshold(result, result,ctmpara.thresh[n],1.0, Imgproc.THRESH_TOZERO);


			    	int tmpPtWidth =  ctmpara.ptnMat[n].width()/2;//テンプレート画像の1/2近傍チェック用
			    	int tmpPtHeight = ctmpara.ptnMat[n].height()/2;
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
			    	    		    	Imgproc.rectangle(orgroi,new Point(j*ctmpara.scale[n],i*ctmpara.scale[n]),
			    	    		    			new Point((j+ctmpara.ptnMat[n].width())*ctmpara.scale[n],(i+ctmpara.ptnMat[n].height())*ctmpara.scale[n]),
			    	    		    			new Scalar(0,255,255),3);
										Imgproc.putText(orgroi,
												String.format("%.3f", rt),
												new Point(j*ctmpara.scale[n],i*ctmpara.scale[n]),
												Imgproc.FONT_HERSHEY_SIMPLEX, 1.5,new Scalar(0,255,255),2);
			    	    		    	j = (int) (j + ctmpara.ptnMat[n].cols() + tmpPtWidth);
			    	    		    	resultValue[n].detectMax = resultValue[n].detectMax<rt?rt:resultValue[n].detectMax;
			    	    		    	resultValue[n].detectMin = resultValue[n].detectMin>rt?rt:resultValue[n].detectMin;
			    	    		    	resultValue[n].detectAve += rt;
			    	    		    	break;
			    					}
			    				}else {
			    					resultValue[n].cnt++;
									finedPoint.add(new Point(j,i));
				    		    	Imgproc.rectangle(orgroi,new Point(j*ctmpara.scale[n],i*ctmpara.scale[n]),
				    		    			new Point((j+ctmpara.ptnMat[n].width())*ctmpara.scale[n],(i+ctmpara.ptnMat[n].height())*ctmpara.scale[n]),
				    		    			new Scalar(0,255,255),3);
									Imgproc.putText(orgroi,
											String.format("%.3f", rt),
											new Point(j*ctmpara.scale[n],i*ctmpara.scale[n]),
											Imgproc.FONT_HERSHEY_SIMPLEX, 1.5,new Scalar(0,255,255),2);
				    		    	j = (int) (j + ctmpara.ptnMat[n].cols() + tmpPtWidth);
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
				if( resultValue[n].cnt < ctmpara.matchCnt[n] ) {
					resultFlg = false;
				}
			}
		}
		return resultFlg;
	}

}
