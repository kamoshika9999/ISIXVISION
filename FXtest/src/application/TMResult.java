package application;

import java.util.ArrayList;
import java.util.List;

public class TMResult {
	public List<Integer> x = new ArrayList<Integer>();
	public List<Integer> y =new ArrayList<Integer>();
	public List<Double> ratio =new ArrayList<Double>();
	public List<Integer> centerPositionX = new ArrayList<Integer>();
	public List<Integer> centerPositionY = new ArrayList<Integer>();

	public int cnt = 0;//検出数
	public double detectMax = 0;//一致率の最大値
	public double detectMin = 1.0;//一致率の最小値
	public double detectAve = 0;//一致率の平均

}
