package application;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVWriter;

public class logData {
	//保持データー数
	private int cnt;
	//日時
	public String initialDate;
	public List<String> date = new ArrayList<String>();//各データ用

	//寸法値
	public List<String> P2_1 = new ArrayList<String>();
	public List<String> P2_2 = new ArrayList<String>();
	public List<String> F_1 = new ArrayList<String>();
	public List<String> F_2 = new ArrayList<String>();
	public List<String> E_1 = new ArrayList<String>();
	public List<String> E_2 = new ArrayList<String>();

	//テンプレートマッチング
	public List<String> tmResult = new ArrayList<String>();

	//穴検出
	public List<String> holeCnt = new ArrayList<String>();

	//ログメッセージ
	public List<String> logMsg = new ArrayList<String>();
	/*
	public List<Integer> x = new ArrayList<Integer>();//ピクセル単位での検出位置リスト
	public List<Integer> y =new ArrayList<Integer>();
	public List<Double> x_subPixel = new ArrayList<Double>();//サブピクセル単位での検出位置リスト
	public List<Double> y_subPixel =new ArrayList<Double>();
	public List<Double> ratio =new ArrayList<Double>();//類似度のリスト
	public List<Double> centerPositionX = new ArrayList<Double>();//検出位置の重心リスト
	public List<Double> centerPositionY = new ArrayList<Double>();
	public List<Double> dispersion = new ArrayList<Double>();//分散

	<対象>public int cnt = 0;//検出数
	<対象>public double detectMax = 0;//類似度の最大値
	<対象>public double detectMin = 1.0;//類似度の最小値
	<対象>public double detectAve = 0;//類似度の平均
	<対象>public double dispersionMax = 0;//分散の最大値
	<対象>public double dispersionMin = 9999999;//分散の最小値
	<対象>public double dispersionAve=0;//分散の平均値
	<対象>public int resultStatusl; //0:合格又は検出無効  1:検出個数不足 2:警報閾値未満有 3:検出個数過多  4:分散警報閾値未満 5:分散閾値未満

	 */

	/**
	 * コンストラクタ
	 */
	public logData() {
		cnt = 0;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH'h 'mm'm 'ss's 'SSS'ms'");
        initialDate = sdf.format(timestamp);

		File folder = new File("./log");
    	if( !folder.exists()) {
    		if( !folder.mkdir() ) {
    			System.out.println("logフォルダの作成に失敗");
    			return;
    		}
    	}

	}

	/**
	 * ログデーター追加
	 * @param P2_log_
	 * @param F_log_
	 * @param E_log_
	 * @param resultValue_
	 * @param holeCnt_
	 */
	public void addData(double[] P2_log_,double[] F_log_,double[] E_log_,TMResult[] resultValue_,
																						Integer[] holeCnt_,String logMsg_) {
		P2_1.add(String.valueOf(P2_log_[0]));
		P2_2.add(String.valueOf(P2_log_[1]));
		F_1.add(String.valueOf(F_log_[0]));
		F_2.add(String.valueOf(F_log_[1]));
		E_1.add(String.valueOf(E_log_[0]));
		E_2.add(String.valueOf(E_log_[0]));

		for(int i=0;i<parameter.ptm_arrySize;i++) {
			tmResult.add(String.valueOf(resultValue_[i].cnt));
			tmResult.add(String.valueOf(resultValue_[i].detectMax));
			tmResult.add(String.valueOf(resultValue_[i].detectMin));
			tmResult.add(String.valueOf(resultValue_[i].detectAve));
			tmResult.add(String.valueOf(resultValue_[i].dispersionMax));
			tmResult.add(String.valueOf(resultValue_[i].dispersionMin));
			tmResult.add(String.valueOf(resultValue_[i].dispersionAve));
			tmResult.add(String.valueOf(resultValue_[i].resultStatus));
		}

		for(int i=0;i<holeCnt_.length;i++) {
			holeCnt.add(String.valueOf(holeCnt_[i]));
		}

		logMsg.add(logMsg_);

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH'h 'mm'm 'ss's 'SSS'ms'");
		date.add(sdf.format(timestamp));
		cnt++;
	}

	//public void addHoleData()

	/**
	 * 保持データークリア
	 */
	public void clear() {
		cnt=0;
		tmResult.clear();
		date.clear();
		P2_1.clear();
		P2_2.clear();
		F_1.clear();
		F_2.clear();
		logMsg.clear();
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH'h 'mm'm 'ss's 'SSS'ms'");
        initialDate = sdf.format(timestamp);
	}

	/**
	 * 保持データー　最新の一行をコンソールへ表示
	 */
	public void outDisplay() {

	}

	/**
	 * 保持データーCSV書き込み
	 * @return
	 */
	public boolean csvWrite() {
		if(cnt>3) {
			final int headColummCount = 8 + 4 + parameter.ptm_arrySize*8 + 1;//ヘッダ列数
			String[] headStr = new String[headColummCount];
			headStr[0]="n"; //データー番号
			headStr[1]="date";//データー追加日時
			headStr[2]="P2_1";
			headStr[3]="P2_2";
			headStr[4]="F_1";
			headStr[5]="F_2";
			headStr[6]="E_1";
			headStr[7]="E_2";

			for(int i=0;i<4;i++) {
				headStr[8+i]="HoleCnt"+String.valueOf(i+1);
			}

			for(int i=0;i<parameter.ptm_arrySize;i++) {
				headStr[12 + i*8 + 0] = String.valueOf(i)+"_TM_cnt";
				headStr[12 + i*8 + 1] = String.valueOf(i)+"_TM_detectMax";
				headStr[12 + i*8 + 2] = String.valueOf(i)+"_TM_detectMin";
				headStr[12 + i*8 + 3] = String.valueOf(i)+"_TM_detectAve";
				headStr[12 + i*8 + 4] = String.valueOf(i)+"_TM_dispersionMax";
				headStr[12 + i*8 + 5] = String.valueOf(i)+"_TM_dispersionMin";
				headStr[12 + i*8 + 6] = String.valueOf(i)+"_TM_dispersionAve";
				headStr[12 + i*8 + 7] = String.valueOf(i)+"_TM_resultStatus";
			}

			headStr[headColummCount-1] = "logMessage";


	        CSVWriter writer;
	        System.out.println("cnt="+cnt+" List="+date.size());
			try {
				writer = new CSVWriter(new FileWriter("./log/"+initialDate + ".csv"));
		        writer.writeNext(headStr);  //ヘッダー書き込み

		        for(int i=0;i<cnt;i++) {
		        	String[] subStr= new String[headColummCount];
		        	subStr[0] = String.valueOf(i);
		        	subStr[1] = date.get(i);
		        	subStr[2] = P2_1.get(i);
		        	subStr[3] = P2_2.get(i);
		        	subStr[4] = F_1.get(i);
		        	subStr[5] = F_2.get(i);
		        	subStr[6] = E_1.get(i);
		        	subStr[7] = E_2.get(i);

		        	for(int j=0;j<4;j++) {
		        		subStr[8+j] = holeCnt.get(i*4+j);//2022.11.4修正
		        	}

		        	for(int j=0;j<parameter.ptm_arrySize;j++) {
		        		for(int k=0;k<8;k++) {
		        			subStr[12+j*8+k] = tmResult.get(i*(8*parameter.ptm_arrySize)+(j*8+k));
		        		}
		        	}

		        	//2022.12.27 ログの\nをアンダーバーへ変換
		        	subStr[headColummCount-1] = logMsg.get(i).replace("\n", "__");//最終行にログメッセージを追加

		        	writer.writeNext(subStr);
		        }

		        writer.close();

			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
    }
}
