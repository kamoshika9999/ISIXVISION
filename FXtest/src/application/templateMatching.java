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
	 *  パターン検出処理
	 * @param areaMat 8UC1
	 * @param dstMat  8UC3
	 * @param settingFlg セッティングモード時 true
	 * @return  true:合格  false:不合格
	 */
	public boolean detectPattern(Mat areaMat, Mat dstMat,boolean settingFlg) {
		preSet pObj = VisonController.pObj;
		parameter para = pObj.para[pObj.select];

		boolean resultFlg = true;
		Mat c_areaMat;//検出エリアMatクローン用
		TMpara c_tmpara;//検出用パラメータクローン用オブジェクト

		if( !settingFlg ) {
			c_tmpara = tmpara.clone();//毎回引数からクローンする　クローンしないと登録パターンと検出エリアが呼び出されるたびに小さくなる
		}else {
			c_tmpara = tmpara;
		}

		for(int n=0;n<c_tmpara.arrayCnt;n++) {
			resultValue[n].cnt = 0;
			if( c_tmpara.ptmEnable[n] ) {
				if( !settingFlg) {
					c_areaMat = areaMat.clone();
				}else {
					c_areaMat = areaMat;//セッティングモード時は画像確認の為クローンしない
				}

				//フィルタ処理
				if(!settingFlg) {
			    	if( para.ptm_gauusianCheck[n] ) {//ガウシアン
			    		double sigmaX = para.ptm_gauusianSliderX[n];
			    		double sigmaY = para.ptm_gauusianSliderY[n];
			    		int tmpValue = (int) para.ptm_gauusianSliderA[n];
			    		if( tmpValue % 2 == 0 ) {
			    			tmpValue++;
			    		}
			    		Size sz = new Size(tmpValue,tmpValue);
			    		Imgproc.GaussianBlur(c_areaMat, c_areaMat, sz, sigmaX,sigmaY);
			    		Imgproc.GaussianBlur(c_tmpara.paternMat[n], c_tmpara.paternMat[n], sz, sigmaX,sigmaY);
			    	}
			    	if( para.ptm_threshholdCheck[n]) {//２値化
			    		int type = para.ptm_threshhold_Invers[n]?Imgproc.THRESH_BINARY_INV:Imgproc.THRESH_BINARY;
			    		Imgproc.threshold(c_areaMat, c_areaMat, para.ptm_threshholdSlider[n],255,type);
			    		Imgproc.threshold(c_tmpara.paternMat[n], c_tmpara.paternMat[n], para.ptm_threshholdSlider[n],255,type);
			    	}
			    	if( para.ptm_dilateCheck[n]) {//膨張
			    		int v = (int)para.ptm_dilateSliderN[n];
			    		Imgproc.dilate(c_areaMat, c_areaMat, new Mat(),new Point(-1,-1),v);
			    		Imgproc.dilate(c_tmpara.paternMat[n], c_tmpara.paternMat[n], new Mat(),new Point(-1,-1),v);
			    	}
			    	if( para.ptm_erodeCheck[n]) {//収縮
			    		int v = (int)para.ptm_erodeSliderN[n];
			    		Imgproc.erode(c_areaMat, c_areaMat, new Mat(),new Point(-1,-1),v);
			    		Imgproc.erode(c_tmpara.paternMat[n], c_tmpara.paternMat[n], new Mat(),new Point(-1,-1),v);
	
			    	}
			    	if( para.ptm_cannyCheck[n] ) {//Canny
			    		double thresh1 = para.ptm_cannyThresh1[n];
			    		double thresh2 = para.ptm_cannyThresh2[n];
			    		Imgproc.Canny(c_areaMat,c_areaMat,thresh1,thresh2);
			    		Imgproc.Canny(c_tmpara.paternMat[n],c_tmpara.paternMat[n],thresh1,thresh2);
			    	}
				}

				//比較結果を格納するMatを生成 ※ctmpara.scale[n]適用前に生成
				Mat dstRoi = dstMat.submat(
						c_tmpara.detectionRects[n].y,
						c_tmpara.detectionRects[n].y+c_tmpara.detectionRects[n].height,
						c_tmpara.detectionRects[n].x,
						c_tmpara.detectionRects[n].x+c_tmpara.detectionRects[n].width
						);

				//検出エリア画像と登録画像を縮小
				Imgproc.resize(c_areaMat, c_areaMat, new Size(),
						1/c_tmpara.scale[n],1/c_tmpara.scale[n],Imgproc.INTER_AREA );
				Imgproc.resize(c_tmpara.paternMat[n],c_tmpara.paternMat[n], new Size(),
						1/c_tmpara.scale[n],1/c_tmpara.scale[n],Imgproc.INTER_AREA );

				c_tmpara.detectionRects[n].x /= c_tmpara.scale[n];
				c_tmpara.detectionRects[n].y /= c_tmpara.scale[n];
				c_tmpara.detectionRects[n].width /= c_tmpara.scale[n];
				c_tmpara.detectionRects[n].height /= c_tmpara.scale[n];
				Mat areaRoi = c_areaMat.submat(
						c_tmpara.detectionRects[n].y,
						c_tmpara.detectionRects[n].y+c_tmpara.detectionRects[n].height,
						c_tmpara.detectionRects[n].x,
						c_tmpara.detectionRects[n].x+c_tmpara.detectionRects[n].width
						);

				boolean flg2;
				double rt;
				List<Point> finedPoint = new ArrayList<>();
				finedPoint.add(new Point(10000,10000));//冗長判定回避用のダミーデーター

				if( areaRoi.width() > c_tmpara.paternMat[n].width() && areaRoi.height() > c_tmpara.paternMat[n].height() ) {

			    	Mat result = new Mat(areaRoi.rows() - c_tmpara.paternMat[n].rows() + 1, areaRoi.cols() - c_tmpara.paternMat[n].cols() + 1, CvType.CV_32FC1);
				    //テンプレートマッチ実行（TM_CCOEFF_NORMED：相関係数＋正規化）
			    	if(c_tmpara.paternMat[n].type() == CvType.CV_8UC3) {
			        	Imgproc.cvtColor(c_tmpara.paternMat[n], c_tmpara.paternMat[n], Imgproc.COLOR_BGR2GRAY);//グレースケール化
			    	}
			    	Imgproc.matchTemplate(areaRoi, c_tmpara.paternMat[n], result, Imgproc.TM_CCOEFF_NORMED);
			    	//結果から相関係数がしきい値以下を削除（０にする）
			    	Imgproc.threshold(result, result,c_tmpara.matchingThresh[n],1.0, Imgproc.THRESH_TOZERO);


			    	int tmpPtWidth =  (int)((double)c_tmpara.paternMat[n].width() * (2.0/3.0));//テンプレート画像の2/3近傍チェック用
			    	int tmpPtHeight = (int)((double)c_tmpara.paternMat[n].height()* (2.0/3.0));
			    	for (int i=0;i<result.rows();i++) {
			    		for (int j=0;j<result.cols();j++) {
			    			rt = result.get(i, j)[0];
			    			if ( rt > 0) {
			    				//近傍に検出エリアが無いか確認 テンプレート画像の2/3近傍
				    			flg2=false;
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
		    	    		    	Imgproc.rectangle(dstRoi,new Point(j*c_tmpara.scale[n],i*c_tmpara.scale[n]),
		    	    		    			new Point((j+c_tmpara.paternMat[n].width())*c_tmpara.scale[n],(i+c_tmpara.paternMat[n].height())*c_tmpara.scale[n]),
		    	    		    			new Scalar(0,255,255),3);
									Imgproc.putText(dstRoi,
											String.format("%.3f", rt),
											new Point(j*c_tmpara.scale[n],i*c_tmpara.scale[n]),
											Imgproc.FONT_HERSHEY_SIMPLEX, 1.5,new Scalar(0,255,255),7);
		    	    		    	j = (int) (j + c_tmpara.paternMat[n].cols() + tmpPtWidth);
		    	    		    	resultValue[n].x.add(j);//X座標格納
		    	    		    	resultValue[n].y.add(i);//y座標格納
		    	    		    	resultValue[n].ratio.add(rt);//マッチング度格納
		    	    		    	resultValue[n].detectMax = resultValue[n].detectMax<rt?rt:resultValue[n].detectMax;
		    	    		    	resultValue[n].detectMin = resultValue[n].detectMin>rt?rt:resultValue[n].detectMin;
		    	    		    	resultValue[n].detectAve += rt;
		    	    		    	break;
		    					}
			    			}
			    		}
			   		 }
				}else {
					System.out.println("検出エリアサイズよりパターンサイズが大きく検査出来ない");
				}

				resultValue[n].detectAve /= (double)resultValue[n].cnt;
				if( resultValue[n].cnt != c_tmpara.matchingTreshDetectCnt[n] ) {
					resultFlg = false;
				}
			}
		}
		return resultFlg;
	}

}
