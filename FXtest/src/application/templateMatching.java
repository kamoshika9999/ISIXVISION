package application;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class templateMatching {

	public TMResult[] resultValue;//検出結果が保存される
	public TMpara tmpara;
	public Mat result;

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
	 * @return  0:合格又は検出無効  1:検出個数不足 2:警報閾値未満有 3:検出個数過多
	 */
	public int detectPattern(Mat areaMat, Mat dstMat,boolean settingFlg,boolean patternDispChk) {
		int resultStatus = 0;
		Mat c_areaMat;//検出エリアMatクローン用
		TMpara c_tmpara;//検出用パラメータクローン用オブジェクト

		if( !settingFlg ) {
			c_tmpara = tmpara.clone();//毎回引数からクローンする　クローンしないと登録パターンと検出エリアが呼び出されるたびに小さくなる
		}else {
			c_tmpara = tmpara;
		}

		for(int n=0;n<c_tmpara.arrayCnt;n++) {
			resultValue[n].listClear();
			if( c_tmpara.ptmEnable[n] ) {
				int searchCnt = 1;

				if( !settingFlg) {
					c_areaMat = areaMat.clone();
				}else {
					c_areaMat = areaMat;//セッティングモード時は画像確認の為クローンしない
				}

				//フィルタ処理
				if(!settingFlg) {
			    	if( c_tmpara.ptm_fil_gauusianCheck[n] ) {//ガウシアン
			    		double sigmaX = c_tmpara.ptm_fil_gauusianX[n];
			    		double sigmaY = c_tmpara.ptm_fil_gauusianY[n];
			    		int tmpValue = (int) c_tmpara.ptm_fil_gauusianValue[n];
			    		if( tmpValue % 2 == 0 ) {
			    			tmpValue++;
			    		}
			    		Size sz = new Size(tmpValue,tmpValue);
			    		Imgproc.GaussianBlur(c_areaMat, c_areaMat, sz, sigmaX,sigmaY);
			    		Imgproc.GaussianBlur(c_tmpara.paternMat[n], c_tmpara.paternMat[n], sz, sigmaX,sigmaY);
			    	}
			    	if( c_tmpara.ptm_fil_threshholdCheck[n]) {//２値化
			    		int type = c_tmpara.ptm_fil_threshhold_Invers[n]?Imgproc.THRESH_BINARY_INV:Imgproc.THRESH_BINARY;
			    		Imgproc.threshold(c_areaMat, c_areaMat, c_tmpara.ptm_fil_threshholdValue[n],255,type);
			    		Imgproc.threshold(c_tmpara.paternMat[n], c_tmpara.paternMat[n], c_tmpara.ptm_fil_threshholdValue[n],255,type);
			    	}
			    	if( c_tmpara.ptm_fil_dilateCheck[n]) {//膨張
			    		int v = (int)c_tmpara.ptm_fil_dilateValue[n];
			    		Imgproc.dilate(c_areaMat, c_areaMat, new Mat(),new Point(-1,-1),v);
			    		Imgproc.dilate(c_tmpara.paternMat[n], c_tmpara.paternMat[n], new Mat(),new Point(-1,-1),v);
			    	}
			    	if( c_tmpara.ptm_fil_erodeCheck[n]) {//収縮
			    		int v = (int)c_tmpara.ptm_fil_erodeValue[n];
			    		Imgproc.erode(c_areaMat, c_areaMat, new Mat(),new Point(-1,-1),v);
			    		Imgproc.erode(c_tmpara.paternMat[n], c_tmpara.paternMat[n], new Mat(),new Point(-1,-1),v);

			    	}
			    	if( c_tmpara.ptm_fil_cannyCheck[n] ) {//Canny
			    		double thresh1 = c_tmpara.ptm_fil_cannyThresh1[n];
			    		double thresh2 = c_tmpara.ptm_fil_cannyThresh2[n];
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
				Imgproc.resize(c_tmpara.ptm_ptmMat_mask[n],c_tmpara.ptm_ptmMat_mask[n], new Size(),
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


				if( areaRoi.width() > c_tmpara.paternMat[n].width()
						&& areaRoi.height() > c_tmpara.paternMat[n].height() ) {

					//テンプレートマッチングの結果を格納するMatを準備
			    	result = new Mat(areaRoi.rows() - c_tmpara.paternMat[n].rows() + 1,
			    			areaRoi.cols() - c_tmpara.paternMat[n].cols() + 1, CvType.CV_32FC1);
			    	//テンプレートがカラーの場合グレースケールへ変換
			    	if(c_tmpara.paternMat[n].type() == CvType.CV_8UC3) {
			        	Imgproc.cvtColor(c_tmpara.paternMat[n], c_tmpara.paternMat[n], Imgproc.COLOR_BGR2GRAY);//グレースケール化
			    	}
				    //テンプレートマッチ実行（ SSD：マッチング最良値は極小値をとる）
			    	Imgproc.matchTemplate(areaRoi, c_tmpara.paternMat[n], result,
			    			Imgproc.TM_SQDIFF,c_tmpara.ptm_ptmMat_mask[n]);
			    	//そのままでは完全一致していないにも関わらず、極小値を正規化すると
			    	//resuleへマッチング最良値である0.0が格納されてしまう。
			    	//座標(0,0)へ0を格納してから正規化し前記を補正する
			    	Imgproc.rectangle(result, new Point(0,0),new Point(0,0),new Scalar(0,0,0),1);
			    	//Imgproc.rectangle(result, new Point(1,0),new Point(1,0),
			    		//	new Scalar(result.width()*result.height()*(255^2),0,0),1);
			    	//正規化
			    	Core.normalize(result,result,0.0,1.0,Core.NORM_MINMAX);
			    	//正規化後に座標(0,0)へ1.0を代入し、完全不一致とする
			    	Imgproc.rectangle(result, new Point(0,0),new Point(0,0),new Scalar(1,0,0),1);


			    	int tmpPtWidth =  (int)c_tmpara.paternMat[n].width();
			    	int tmpPtHeight = (int)c_tmpara.paternMat[n].height();
			    	int w1,h1;
			    	double IOU = 0;
			    	List<Double> finedPointThresh = new ArrayList<Double>();
					List<Point> finedPoint = new ArrayList<Point>();

					//*************************************************************************************************
					//Non Maximum Suppression処理 resultのスコアはTM_SQDIFFの為逆数を取る
					//*************************************************************************************************
					//検出結果Matをリストへ変換
					List<Point> result_point = new ArrayList<Point>();
					List<Double> result_value = new ArrayList<Double>();
					for( int y = 0;y<result.height();y++ ) {
						for( int x = 0;x<result.width();x++ ) {
							if( 1 - result.get(y,x)[0] > c_tmpara.matchingThresh[n] ) {
								result_point.add(new Point(x,y));
								result_value.add( 1 - result.get(y,x)[0] );//逆数を取って入力
							}
						}
					}

					while(true) {
	   					int result_size = result_point.size();
	   					if( result_size == 0) break;
						//スコアが一番高い矩形番号を抽出
	   					int max_No = 0;
		   					double max_value = 0;
		   					for( int i=0;i<result_value.size();i++) {
		   						if( result_value.get(i) > max_value ) {
		   							max_value = result_value.get(i);
		   							max_No = i;
		   						}
		   					}
	   					//スコアが一番高い矩形を出力へ移す
	   					double t_x = result_point.get(max_No).x;
	   					double t_y = result_point.get(max_No).y;
	   					finedPoint.add(new Point(t_x,t_y));
	   					finedPointThresh.add(result_value.get(max_No));
	   					result_point.remove(max_No);
	   					result_value.remove(max_No);
	   					//入力に残っている各矩形のIOUを計算　閾値以上を入力から削除する
	   					for(int i=0;i<result_size-1;i++){
							w1 = (int)(tmpPtWidth - Math.abs(result_point.get(i).x - t_x));
							w1 = w1<0?0:w1;
							h1 = (int)(tmpPtHeight - Math.abs(result_point.get(i).y - t_y));
							h1 = h1<0?0:h1;
	    					IOU = (double)(w1*h1) / (double)( tmpPtWidth * tmpPtHeight );
	    					if( IOU > 0.05 ) {
	    	   					result_point.remove(i);
	    	   					result_value.remove(i);
	    	   					result_size--;//リストをリムーブする為　もう少しましな方法は無いか？
	    	   					i--;//リストをリムーブする為　もう少しましな方法は無いか？
	    					}
	    				}
					}
					//*************************************************************************************************

					resultValue[n].cnt = finedPoint.size();
					//閾値降順に並べ替え
					double tmpRatio;
					Point tmpPoint;
					for(int i=0;i<finedPoint.size()-1;i++) {
						for( int j=i+1;j<finedPoint.size();j++) {
							if( finedPointThresh.get(i) < finedPointThresh.get(j) ) {

								tmpRatio = finedPointThresh.get(i);
								tmpPoint = finedPoint.get(i);

								finedPointThresh.set(i, finedPointThresh.get(j));
								finedPointThresh.set(j,tmpRatio);
								finedPoint.set(i, finedPoint.get(j));
								finedPoint.set(j, tmpPoint);
							}
						}
					}
					searchCnt = c_tmpara.matchingTreshDetectCnt[n]>finedPoint.size()?
										finedPoint.size() : c_tmpara.matchingTreshDetectCnt[n];

					//*************************************************************************************************
			    	//サブピクセル精度計測
					//方法:パラボラフィッテング
					//R(0):検出位置の類似度 R(-1),R(+0):検出位置隣の類似度
					//類似度の分布は２次関数となることを前提とする
					//*************************************************************************************************
		    		int index=0;
			    	while( index < searchCnt ) {
				    	try {//検出位置がMat resultの端にあった場合インディックスエラーとなるので例外で処理する
				    		double x,y;
				    		double r0,r1,r2;

				    		//ｘを求める {(R(-1) - R(+1)}/{2*R(-1) - 4*R(0) + 2*R(+1)}
				    		r0= 1 - result.get( (int)finedPoint.get(index).y, (int)finedPoint.get(index).x )[0];//R(0)
				    		r1= 1 - result.get( (int)finedPoint.get(index).y, (int)finedPoint.get(index).x-1 )[0];//R(-1)
				    		r2= 1 - result.get( (int)finedPoint.get(index).y, (int)finedPoint.get(index).x+1 )[0];//R(+1)
				    		x = finedPoint.get(index).x + (r1-r2)/(2*r1-4*r0+2*r2);
				    		//yを求める {(R(+1) - R(-1)}/{2*R(-1) - 4*R(0) + 2*R(+1)}
				    		r0= 1 - result.get( (int)finedPoint.get(index).y, (int)finedPoint.get(index).x )[0];//R(0)
				    		r1= 1 - result.get( (int)finedPoint.get(index).y-1, (int)finedPoint.get(index).x )[0];//R(-1)
				    		r2= 1 - result.get( (int)finedPoint.get(index).y+1, (int)finedPoint.get(index).x )[0];//R(+1)
				    		y = finedPoint.get(index).y + (r1-r2)/(2*r1-4*r0+2*r2);

				    		resultValue[n].x_subPixel.add(x);
				    		resultValue[n].y_subPixel.add(y);

				    	}catch(Exception e) {
				    		System.out.println("サブピクセル計測失敗　寸法測定分解能低下。\n検出位置がサーチ範囲の端にかかっています。");
				    		resultValue[n].x_subPixel.add(finedPoint.get(index).x );
				    		resultValue[n].y_subPixel.add(finedPoint.get(index).y);
				    	}
				    	index++;
			    	}
					//*************************************************************************************************

			    	for(int i=0;i<searchCnt;i++) {
	    		    	Point p= finedPoint.get(i);
	    		    	double ratio = finedPointThresh.get(i);

	    		    	if(patternDispChk)Imgproc.rectangle(dstRoi,new Point(p.x*c_tmpara.scale[n],p.y*c_tmpara.scale[n]),
	    		    			new Point((p.x+c_tmpara.paternMat[n].width())*c_tmpara.scale[n],
	    		    			(p.y+c_tmpara.paternMat[n].height())*c_tmpara.scale[n]),
	    		    			new Scalar(0,255,255),3);
	    		    	if(patternDispChk)Imgproc.putText(dstRoi,
								String.format("%.3f", ratio),
								new Point(p.x*c_tmpara.scale[n],p.y*c_tmpara.scale[n]),
								Imgproc.FONT_HERSHEY_SIMPLEX, 1.5,new Scalar(0,255,255),7);
	    		    	resultValue[n].x.add((int)((c_tmpara.detectionRects[n].x+p.x)*c_tmpara.scale[n]));//X座標格納
	    		    	resultValue[n].y.add((int)((c_tmpara.detectionRects[n].y+p.y)*c_tmpara.scale[n]));//y座標格納
	    		    	resultValue[n].ratio.add(ratio);//類似度格納
	    		    	resultValue[n].detectMax = resultValue[n].detectMax<ratio?ratio:resultValue[n].detectMax;
	    		    	resultValue[n].detectMin = resultValue[n].detectMin>ratio?ratio:resultValue[n].detectMin;
	    		    	resultValue[n].detectAve += finedPointThresh.get(i);

	    		    	//警報閾値未満判定
	    		    	if( finedPointThresh.get(i) < c_tmpara.matchingThresh_K[n] ) {
	    		    		resultStatus = 2;
	    		    	}

	    		    	//パターン中心計算
	    		    	resultValue[n].centerPositionX.add(
	    		    			(double)c_tmpara.detectionRects[n].x*c_tmpara.scale[n] +
	    		    			resultValue[n].x_subPixel.get(i)*c_tmpara.scale[n]+
	    		    			c_tmpara.paternMat[n].width()*c_tmpara.scale[n]/2);
	    		    	resultValue[n].centerPositionY.add(
	    		    			(double)c_tmpara.detectionRects[n].y*c_tmpara.scale[n] +
	    		    			resultValue[n].y_subPixel.get(i)*c_tmpara.scale[n]+
	    		    			c_tmpara.paternMat[n].height()*c_tmpara.scale[n]/2);
	    		    	
	    		    	/*
	    		    	System.out.println("length="+resultValue[n].centerPositionX.size());
	    		    	if(resultValue[n].centerPositionX.size()>0) {
		    		    	System.out.println("x[0]="+resultValue[n].centerPositionX.get(0)+
		    		    			" y[0]="+resultValue[n].centerPositionY.get(0));
	    		    	}
	    		    	if(resultValue[n].centerPositionX.size()>1) {
		    		    	System.out.println("x[1]="+resultValue[n].centerPositionX.get(1)+
		    		    			" y[1]="+resultValue[n].centerPositionY.get(1));
	    		    	}
	    		    	*/
	    		    	
	    		    	//クロスマーク表示
	    		    	int orgX = (int)( resultValue[n].centerPositionX.get(i).doubleValue() );
	    		    	int orgY = (int)( resultValue[n].centerPositionY.get(i).doubleValue() );
	    		    	if(patternDispChk)Imgproc.line(dstMat,new Point(orgX-20,orgY-20),new Point(orgX+20,orgY+20),
	    		    					new Scalar(0,200,200),3);
	    		    	if(patternDispChk)Imgproc.line(dstMat,new Point(orgX+20,orgY-20),new Point(orgX-20,orgY+20),
		    							new Scalar(0,200,200),3);
			   		 }
				}
				resultValue[n].detectAve /= (double)searchCnt;
				if( resultValue[n].cnt < c_tmpara.matchingTreshDetectCnt[n] ) {
					resultStatus = 1;
				}else if( resultValue[n].cnt > c_tmpara.matchingTreshDetectCnt[n] ) {
					resultStatus = 3;
				}
			}
		}

		return resultStatus;
	}
}
