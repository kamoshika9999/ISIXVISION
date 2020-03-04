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
	 * @return  true:合格  false:不合格
	 */
	public boolean detectPattern(Mat areaMat, Mat dstMat,boolean settingFlg,boolean patternDispChk) {

		boolean resultFlg = true;
		Mat c_areaMat;//検出エリアMatクローン用
		TMpara c_tmpara;//検出用パラメータクローン用オブジェクト

		if( !settingFlg ) {
			c_tmpara = tmpara.clone();//毎回引数からクローンする　クローンしないと登録パターンと検出エリアが呼び出されるたびに小さくなる
		}else {
			c_tmpara = tmpara;
		}

		for(int n=0;n<c_tmpara.arrayCnt;n++) {
			if( c_tmpara.ptmEnable[n] ) {
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


				if( areaRoi.width() > c_tmpara.paternMat[n].width() && areaRoi.height() > c_tmpara.paternMat[n].height() ) {

			    	result = new Mat(areaRoi.rows() - c_tmpara.paternMat[n].rows() + 1,
			    			areaRoi.cols() - c_tmpara.paternMat[n].cols() + 1, CvType.CV_32FC1);
				    //テンプレートマッチ実行（TM_CCOEFF_NORMED：相関係数＋正規化）
			    	if(c_tmpara.paternMat[n].type() == CvType.CV_8UC3) {
			        	Imgproc.cvtColor(c_tmpara.paternMat[n], c_tmpara.paternMat[n], Imgproc.COLOR_BGR2GRAY);//グレースケール化
			    	}

			    	Imgproc.matchTemplate(areaRoi, c_tmpara.paternMat[n], result, Imgproc.TM_CCOEFF_NORMED);

			    	//結果から相関係数がしきい値以下を削除（０にする）
			    	//Imgproc.threshold(result, result,c_tmpara.matchingThresh[n],1.0, Imgproc.THRESH_TOZERO);

			    	int tmpPtWidth =  (int)((double)c_tmpara.paternMat[n].width() * (2.0/3.0));//テンプレート画像の2/3近傍チェック用
			    	int tmpPtHeight = (int)((double)c_tmpara.paternMat[n].height()* (2.0/3.0));
					double rt;
					boolean flg2;
			    	List<Double> finedPointThresh = new ArrayList<Double>();
					List<Point> finedPoint = new ArrayList<>();
			    	for (int i=0;i<result.rows();i++) {
			    		for (int j=0;j<result.cols();j++) {
			    			rt = result.get(i, j)[0];
			    			if ( rt > c_tmpara.matchingThresh[n]) {
			    				flg2 = false;
			    				//近傍に検出済パターンが無いか確認 テンプレート画像サイズ2/3近傍
			    				for( int k=0;k<finedPoint.size();k++) {
			    					Point p = finedPoint.get(k);
			    					if( p.x  + tmpPtWidth > j && p.x - tmpPtWidth < j &&
			    							p.y  + tmpPtHeight > i && p.y - tmpPtHeight < i) {
			    						if(rt > finedPointThresh.get(k)) {
				    						//近傍あり、閾値が上回る為入れ替え
				    						p.x = j;
				    						p.y = i;
				    						finedPointThresh.set(k,rt);
			    						}
				    					flg2 = true;
			    					}
			    				}
			    				//近傍に無い場合で見つかったのでリストに追加
			    				if( !flg2 ) {
			    					finedPoint.add(new Point(j,i));
			    					finedPointThresh.add(rt);
			    				}
			    			}
			    		}
			    	}
			    	/*
		            // 出力ファイルの作成
			    	try {
			            FileWriter f = new FileWriter("./gdata.csv", false);
			            PrintWriter p = new PrintWriter(new BufferedWriter(f));

			    		for(int y=0;y<result.rows();y++) {
			    			for(int x=0;x<result.cols();x++) {
			    				p.print(String.valueOf(x)+","+String.valueOf(y)+","+
			    						String.format("%.6f", result.get(y,x)[0]));
				                p.println();    // 改行
			    			}
			                p.println();    // 改行
			    		}

			            // ファイルに書き出し閉じる
			            p.close();

			            System.out.println("ファイル出力完了！");

			        } catch (IOException ex) {
			            ex.printStackTrace();
			        }
			        */

			    	//サブピクセル精度計測
		    		int _i=0;
			    	try {
				    	for(int i=0;i<finedPoint.size();i++) {
				    		_i = i;
				    		double x,y;
				    		double r0,r1,r2;

				    		//ｘを求める {(R(+1) - R(-1)}/{2*R(-1) - 4*R(0) + 2*R(+1)} * -1
				    		r0=result.get( (int)finedPoint.get(i).y, (int)finedPoint.get(i).x )[0];//R(0)
				    		r1=result.get( (int)finedPoint.get(i).y, (int)finedPoint.get(i).x-1 )[0];//R(-1)
				    		r2=result.get( (int)finedPoint.get(i).y, (int)finedPoint.get(i).x+1 )[0];//R(+1)
				    		x = finedPoint.get(i).x + (r2-r1)/(2*r1-4*r0+2*r2) * -1;
				    		//yを求める {(R(+1) - R(-1)}/{2*R(-1) - 4*R(0) + 2*R(+1)} * -1
				    		r0=result.get( (int)finedPoint.get(i).y, (int)finedPoint.get(i).x )[0];//R(0)
				    		r1=result.get( (int)finedPoint.get(i).y-1, (int)finedPoint.get(i).x )[0];//R(-1)
				    		r2=result.get( (int)finedPoint.get(i).y+1, (int)finedPoint.get(i).x )[0];//R(+1)
				    		y = finedPoint.get(i).y + (r2-r1)/(2*r1-4*r0+2*r2) * -1;

				    		resultValue[n].x_subPixel.add(x);
				    		resultValue[n].y_subPixel.add(y);

				    	}
			    	}catch(Exception e) {
			    		System.out.println("サブピクセル計測失敗　寸法測定分解能低下の為、計測しません");
			    		resultFlg = false;
			    		resultValue[n].x_subPixel.add(finedPoint.get(_i).x );
			    		resultValue[n].y_subPixel.add(finedPoint.get(_i).y);

			    	}

					resultValue[n].cnt = finedPoint.size();
			    	for(int i=0;i<finedPoint.size();i++) {
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
	    		    	resultValue[n].ratio.add(ratio);//マッチング度格納
	    		    	resultValue[n].detectMax = resultValue[n].detectMax<ratio?ratio:resultValue[n].detectMax;
	    		    	resultValue[n].detectMin = resultValue[n].detectMin>ratio?ratio:resultValue[n].detectMin;
	    		    	resultValue[n].detectAve += finedPointThresh.get(i);
	    		    	//パターン中心計算
	    		    	resultValue[n].centerPositionX.add(
	    		    			(double)c_tmpara.detectionRects[n].x*c_tmpara.scale[n] +
	    		    			resultValue[n].x_subPixel.get(i)*c_tmpara.scale[n]+
	    		    			c_tmpara.paternMat[n].width()*c_tmpara.scale[n]/2);
	    		    	resultValue[n].centerPositionY.add(
	    		    			(double)c_tmpara.detectionRects[n].y*c_tmpara.scale[n] +
	    		    			resultValue[n].y_subPixel.get(i)*c_tmpara.scale[n]+
	    		    			c_tmpara.paternMat[n].height()*c_tmpara.scale[n]/2);
	    		    	//十字マーク表示
	    		    	int orgX = (int)( resultValue[n].centerPositionX.get(i).doubleValue() );
	    		    	int orgY = (int)( resultValue[n].centerPositionY.get(i).doubleValue() );
	    		    	if(patternDispChk)Imgproc.line(dstMat,new Point(orgX-20,orgY-20),new Point(orgX+20,orgY+20),
	    		    					new Scalar(0,200,200),3);
	    		    	if(patternDispChk)Imgproc.line(dstMat,new Point(orgX+20,orgY-20),new Point(orgX-20,orgY+20),
		    							new Scalar(0,200,200),3);
			   		 }
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
