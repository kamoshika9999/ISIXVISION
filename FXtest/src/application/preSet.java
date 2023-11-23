package application;

import java.io.Serializable;

public class preSet implements Serializable{
	int presetMaxCount = 4;
	parameter[] para;
	int select;
	int portNo;
	int cameraID;
	int adc_thresh;
	int cameraWidth,cameraHeight;
	boolean adcFlg;

	boolean dimensionDispChk;
	boolean holeDispChk;
	boolean patternDispChk;

	boolean camera_revers;

	//String[] presetNameText = {"SO6L","(DEG)","(DAG)","④"};//#55
	String[] presetNameText = {"TO220","D2PACK","③","④"};//#51 #54 #56


	public preSet() {
		portNo = 4;

		para = new parameter[presetMaxCount];
		for(int i=0;i<presetMaxCount;i++) {
			para[i] = new parameter();
			select = 1;
			cameraID = 1;
			adc_thresh = 400;
			cameraWidth = 1920;
			cameraHeight = 1080;
			adcFlg = true;
			camera_revers = false;
		}
	}
}
