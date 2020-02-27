package application;

import java.io.Serializable;

public class preSet2 implements Serializable{
	parameter[][] para;//[0:1st.ショット,1:2nd.ショット][0～3:各ウィンドウ]
	int select;
	int portNo;
	int cameraID;
	int adc_thresh;
	int cameraWidth,cameraHeight;
	boolean adcFlg;

	boolean dimensionDispChk;
	boolean holeDispChk;
	boolean patternDispChk;


	public preSet2() {
		portNo = 4;

		para = new parameter[2][4];
		for(int i=0;i<4;i++) {
			para[0][i] = new parameter();
			para[1][i] = new parameter();
			select = 1;
			cameraID = 1;
			adc_thresh = 400;
			cameraWidth = 1920;
			cameraHeight = 1080;
			adcFlg = true;
		}
	}
}
