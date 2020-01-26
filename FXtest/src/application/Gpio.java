
package application;

import jssc.SerialPort;  /* Calls the respective serial port */

/**
 *
 * @author Mamoru
 * PIN 0:シャッター用トリガ入力  PIN 1:NG出力トリガ PIN2:REDY出力信号
 */
public class Gpio {
	static SerialPort port;			//ポートオブジェクト保持用
	static boolean openFlg;			//ポートオープン成功失敗フラグ
	static final int sleepTime = 100; //バッファパージ前のスレッドスリープタイム(mSec)
	static int debugSleepTime = 50;

	/**
	 * GPIOボード用シリアルポートオープン
	 * @param p
	 * @return true:オープン成功  false:オープン失敗
	 * @throws InterruptedException
	 *
	 */
	public static boolean open(String p) throws InterruptedException {
		//デバッグコード-----
		if( VisonController.debugFlg ) {
			Thread.sleep(debugSleepTime);
			openFlg = true;
            System.out.println("Debug:::Port " + p + " opened successfully...");
			return true;
		}
		//-------------------
    	openFlg=false;
	    port = new SerialPort( "COM"+p );
	    try {
	        if(port.openPort() == true){
	            System.out.println("Port " + p + " opened successfully...");
	            port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);//フロー制御無し
	 	       	openFlg=true;//ポートオープン 成功
	        }else{
	        	openFlg = false;//ポートオープン 失敗
	            return false;
	        }
	        //Thread.sleep(sleepTime );
	        return true; //例外無く終了

	    }catch(Exception e) {
	    	return false;//例外発生し、ポートオープン失敗 openFlgはfalse
	    }
	}

	/**
	 * GPIOボード用シリアルポートクローズ
	 * @return true:ポートクローズ成功  false:ポートクローズ失敗
	 * @throws InterruptedException
	 */
	public static boolean close() throws InterruptedException {
		//デバッグコード---------
		if( VisonController.debugFlg ) {
			Thread.sleep(debugSleepTime);
            System.out.println("Debug:::Port  closeed successfully...");
			return true;
		}
		//-----------------------
		if( !openFlg ) {
			return false; //ポートが開いていない場合はportオブジェクトがnullの為リターン
		}
		try {
	        if(port.closePort() == true){//ポートクローズ
	            System.out.println("Port " + port.toString() + " closeed successfully...");
	        }else{
	            return false;
	        }
	    }catch(Exception e) {
	    	e.printStackTrace();
	    	return false;
	    }
	    return true;

	}

	/**
	 * PLCからオールクリア信号を受信する
	 * 判定が有効化になれば自動的にPLCから送信される
	 * @return '1':オールクリア信号受信 '0':非受信
	 * @throws InterruptedException
	 */
	public static String clearSignal() throws InterruptedException {
		//デバッグコード----
		if( VisonController.debugFlg ) {
			Thread.sleep(debugSleepTime);
			System.out.println("Debug:::clear signal OFF");
			return "0";
		}
		//------------------
		if( !openFlg ) {
			return "PortNotOpen";
		}
		String rt;
		try {
	        port.writeString("\r");
	        port.writeString("gpio read 1\r");

	        rt = port.readString();

	        Thread.sleep(sleepTime );
	        port.purgePort(SerialPort.PURGE_RXCLEAR & SerialPort.PURGE_TXCLEAR);
		}catch(Exception e) {
			e.printStackTrace();
			return "clearSignal Read error";
		}

		return rt;//読み込みに成功したら'1'又は'0'を返す

	}
	/**
	 * シャッター信号受信
	 * @return '1':シャッターON '0':シャッターOFF 'shutterSignal Read error':GPIOポート読み込み失敗
	 * @throws InterruptedException
	 */
	public static String shutterSignal(){
		//デバッグコード----
		if( VisonController.debugFlg ) {
			try {
				Thread.sleep(debugSleepTime);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			System.out.println("Debug:::shutter signal ON");
			return "1";
		}
		//------------------
		if( !openFlg ) {
			return "PortNotOpen";
		}
		String rt = "0";
		try {
	        port.writeString("\r");
	        port.writeString("gpio read 0\r");

	        rt = port.readString();

	        Thread.sleep(sleepTime );
	        port.purgePort(SerialPort.PURGE_RXCLEAR & SerialPort.PURGE_TXCLEAR);
		}catch(Exception e) {
			e.printStackTrace();
			return "shutterSignal Read error";
		}

		return rt;//読み込みに成功したら'1'又は'0'を返す

	}

	/**
	 * PLCへOKシグナルを継続送信
	 * GPIO 1へ'1'をセットした後は、ngSignalON()メソッドでクリアされるまで'1'が保持される
	 * @return true:GPIO 1へセット成功 false:セット失敗
	 * @throws InterruptedException
	 */
	public static boolean OkSignalON() throws InterruptedException {
		//デバッグコード----
		if( VisonController.debugFlg ) {
			Thread.sleep(debugSleepTime);
	        System.out.println("Info: <gpio set 3> Command sent...");
	        return true;
		}
		//------------------
		if( !openFlg ) {
			return false;
		}
        try {
			port.writeString("\r");
	        port.writeString("gpio set 3\r");
	        System.out.println("Info: <gpio set 3> Command sent...");

	        Thread.sleep(sleepTime );
	        port.purgePort(SerialPort.PURGE_RXCLEAR & SerialPort.PURGE_TXCLEAR);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

        return true;
	}

	/**
	 * PLCへNGシグナル送信
	 * GPIO 1を'0'にセットするとPLC側では画像処理判定NGと判断し設備をシーケンスに従って止める
	 * 設備停止有効化はスリッターが回りだしてから10秒後となる
	 * @return
	 * @throws InterruptedException
	 */
	public static boolean ngSignalON() throws InterruptedException {
		//デバッグコード----
		if( VisonController.debugFlg ) {
			Thread.sleep(debugSleepTime);
	        System.out.println("Debug:::Info: <gpio clear 3> Command sent...");
			return true;
		}
		//------------------
		if( !openFlg ) {
			return false;
		}		try {
	        port.writeString("\r");
	        port.writeString("gpio clear 3\r");//IO 3クリア 出力ゼロ
	        System.out.println("Info: <gpio clear 3> Command sent...");

	        Thread.sleep(sleepTime );
	        port.purgePort(SerialPort.PURGE_RXCLEAR & SerialPort.PURGE_TXCLEAR);
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}

        return true;
	}



}