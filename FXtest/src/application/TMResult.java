package application;

import java.util.ArrayList;
import java.util.List;

public class TMResult {
	public List<Integer> x = new ArrayList<Integer>();
	public List<Integer> y =new ArrayList<Integer>();
	public List<Double> x_subPixel = new ArrayList<Double>();
	public List<Double> y_subPixel =new ArrayList<Double>();
	public List<Double> ratio =new ArrayList<Double>();
	public List<Double> centerPositionX = new ArrayList<Double>();
	public List<Double> centerPositionY = new ArrayList<Double>();


	public int cnt = 0;//検出数
	public double detectMax = 0;//一致率の最大値
	public double detectMin = 1.0;//一致率の最小値
	public double detectAve = 0;//一致率の平均

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
	}

}
