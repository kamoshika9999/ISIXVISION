package application;

//画像測定の基礎となるパラメータのグローバル定数
//穴数と寸法の閾値の値
public class baseParameterValue {
	public static final int poketHoleCntThreshold = 6;//#55=6 その他=3
	public static final int okuriHoleCntTheshold = 18;//#55=18 その他=9
	public static final double F_UpperLimit_dimensionTheshold = 11.65;
	public static final double F_LowerLimit_dimensionTheshold = 11.35;
	public static final double P2_UpperLimit_dimensionTheshold = 2.15;
	public static final double P2_LowerLimit_dimensionTheshold = 1.85;
	public static final double E_UpperLimit_dimensionTheshold = 1.85;
	public static final double E_LowerLimit_dimensionTheshold = 1.65;
}
