package application;

import java.io.Serializable;

public class preSet implements Serializable{
	parameter[] para;
	int select;
	int portNo;
	int delly;
	int cameraID;
	int adc_thresh;
	int cameraWidth,cameraHeight;
	boolean adcFlg;

	boolean dimensionDispChk;
	boolean holeDispChk;
	boolean patternDispChk;


	public preSet() {
		portNo = 4;
		delly = 550;

		para = new parameter[4];
		for(int i=0;i<4;i++) {
			para[i] = new parameter();
			select = 1;
			cameraID = 1;
			adc_thresh = 400;
			cameraWidth = 1920;
			cameraHeight = 1080;
			adcFlg = true;
		}
	}
}
