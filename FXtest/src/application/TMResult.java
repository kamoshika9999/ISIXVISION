package application;

import java.util.ArrayList;
import java.util.List;

public class TMResult {
	public List<Integer> x = new ArrayList<Integer>();//ピクセル単位での検出位置リスト
	public List<Integer> y =new ArrayList<Integer>();
	public List<Double> x_subPixel = new ArrayList<Double>();//サブピクセル単位での検出位置リスト
	public List<Double> y_subPixel =new ArrayList<Double>();
	public List<Double> ratio =new ArrayList<Double>();//類似度のリスト
	public List<Double> centerPositionX = new ArrayList<Double>();//検出位置の重心リスト
	public List<Double> centerPositionY = new ArrayList<Double>();
	public List<Double> dispersion = new ArrayList<Double>();//分散


	public int cnt = 0;//検出数
	public double detectMax = 0;//類似度の最大値
	public double detectMin = 1.0;//類似度の最小値
	public double detectAve = 0;//類似度の平均
	public double dispersionMax = 0;//分散の最大値
	public double dispersionMin = 9999999;//分散の最小値
	public double dispersionAve=0;//分散の平均値

	public int resultStatus; //0:合格又は検出無効  1:検出個数不足 2:警報閾値未満有 3:検出個数過多

	public void listClear() {
		x.clear();
		y.clear();
		x_subPixel.clear();
		y_subPixel.clear();
		ratio.clear();
		centerPositionX.clear();
		centerPositionY.clear();

		cnt = 0;
		detectMax =0;
		detectMin = 1.0;
		detectAve = 0;
		dispersionMax = 0;
		dispersionMin = 9999999;
		dispersionAve = 0;

		resultStatus=0;

	}

}
