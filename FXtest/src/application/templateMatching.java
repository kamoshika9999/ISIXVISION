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
		resultValue = new TMResult[tmpara.arrayCnt];
		for(int i=0;i<tmpara.arrayCnt;i++)
			resultValue[i] = new TMResult();
	}

	/**
	 * パターン検出処理
	 * @param srcMat
	 * @param dstMat
	 * @return
	 */
	public boolean detectPattern(Mat srcMat, Mat dstMat) {
		boolean resultFlg = true;

		for(int n=0;n<tmpara.arrayCnt;n++) {
			if( tmpara.ptmEnable[n] ) {
				//エリア画像と登録画像を縮小
				Imgproc.resize(srcMat, srcMat, new Size(),
						1/tmpara.scale[n],1/tmpara.scale[n],Imgproc.INTER_AREA );
				Imgproc.resize(tmpara.ptnMat[n],tmpara.ptnMat[n], new Size(),
						1/tmpara.scale[n],1/tmpara.scale[n],Imgproc.INTER_AREA );

				//比較結果を格納するMatを生成
				Mat orgroi = null;
				orgroi = dstMat.submat(
						tmpara.para_rectsDetection[n].y,
						tmpara.para_rectsDetection[n].y+tmpara.para_rectsDetection[n].height,
						tmpara.para_rectsDetection[n].x,
						tmpara.para_rectsDetection[n].x+tmpara.para_rectsDetection[n].width
						);//tmpara.detectionScale[n]適用前に生成

				tmpara.para_rectsDetection[n].x /= tmpara.scale[n];
				tmpara.para_rectsDetection[n].y /= tmpara.scale[n];
				tmpara.para_rectsDetection[n].width /= tmpara.scale[n];
				tmpara.para_rectsDetection[n].height /= tmpara.scale[n];
				Mat roi = srcMat.submat(
						tmpara.para_rectsDetection[n].y,
						tmpara.para_rectsDetection[n].y+tmpara.para_rectsDetection[n].height,
						tmpara.para_rectsDetection[n].x,
						tmpara.para_rectsDetection[n].x+tmpara.para_rectsDetection[n].width
						);

				boolean flg2;
				double rt;
				List<Point> finedPoint = new ArrayList<>();
				if( roi.width() > tmpara.ptnMat[n].width() && roi.height() > tmpara.ptnMat[n].height() ) {

			    	Mat result = new Mat(roi.rows() - tmpara.ptnMat[n].rows() + 1, roi.cols() - tmpara.ptnMat[n].cols() + 1, CvType.CV_32FC1);
				    	//テンプレートマッチ実行（TM_CCOEFF_NORMED：相関係数＋正規化）
			    	Imgproc.matchTemplate(roi, tmpara.ptnMat[n], result, Imgproc.TM_CCOEFF_NORMED);
			    	//結果から相関係数がしきい値以下を削除（０にする）
			    	Imgproc.threshold(result, result,tmpara.thresh[n],1.0, Imgproc.THRESH_TOZERO);


			    	int tmpPtWidth =  tmpara.ptnMat[n].width()/2;//テンプレート画像の1/2近傍チェック用
			    	int tmpPtHeight = tmpara.ptnMat[n].height()/2;
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
			    	    		    	Imgproc.rectangle(orgroi,new Point(j*tmpara.scale[n],i*tmpara.scale[n]),
			    	    		    			new Point((j+tmpara.ptnMat[n].width())*tmpara.scale[n],(i+tmpara.ptnMat[n].height())*tmpara.scale[n]),
			    	    		    			new Scalar(0,255,255),3);
										Imgproc.putText(orgroi,
												String.format("%.3f", rt),
												new Point(j*tmpara.scale[n],i*tmpara.scale[n]),
												Imgproc.FONT_HERSHEY_SIMPLEX, 1.5,new Scalar(0,255,255),2);
			    	    		    	j = (int) (j + tmpara.ptnMat[n].cols() + tmpPtWidth);
			    	    		    	resultValue[n].detectMax = resultValue[n].detectMax<rt?rt:resultValue[n].detectMax;
			    	    		    	resultValue[n].detectMin = resultValue[n].detectMin>rt?rt:resultValue[n].detectMin;
			    	    		    	resultValue[n].detectAve += rt;
			    	    		    	break;
			    					}
			    				}else {
			    					resultValue[n].cnt++;
									finedPoint.add(new Point(j,i));
				    		    	Imgproc.rectangle(orgroi,new Point(j*tmpara.scale[n],i*tmpara.scale[n]),
				    		    			new Point((j+tmpara.ptnMat[n].width())*tmpara.scale[n],(i+tmpara.ptnMat[n].height())*tmpara.scale[n]),
				    		    			new Scalar(0,255,255),3);
									Imgproc.putText(orgroi,
											String.format("%.3f", rt),
											new Point(j*tmpara.scale[n],i*tmpara.scale[n]),
											Imgproc.FONT_HERSHEY_SIMPLEX, 1.5,new Scalar(0,255,255),2);
				    		    	j = (int) (j + tmpara.ptnMat[n].cols() + tmpPtWidth);
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
		}
		return resultFlg;
	}

}
