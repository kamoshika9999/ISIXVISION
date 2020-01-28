
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
	static boolean useFlg = false;
	static boolean adcFlg = true;
	static int adcThresh;
	static final int sleepTime = 30; //バッファパージ前のスレッドスリープタイム(mSec)
	static int debugSleepTime = 50;

	/**
	 * GPIOボード用シリアルポートオープン
	 * @param p
	 * @return true:オープン成功  false:オープン失敗
	 * @throws InterruptedException
	 *
	 */
	public static boolean open(String p,int adc_thresh){
		adcThresh = adc_thresh;
		//デバッグコード-----
		if( VisonController.debugFlg ) {
			try {
				Thread.sleep(debugSleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			openFlg = true;
            System.out.println("Debug:::Port " + p + " opened successfully...");
			return true;
		}
		//-------------------
		if( useFlg ) return false;
		useFlg = true;

		openFlg=false;
	    port = new SerialPort( "COM"+p );
	    try {
	        if(port.openPort() == true){
	            System.out.println("Port " + p + " opened successfully...");
	            port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);//フロー制御無し
	            port.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
	            		SerialPort.PARITY_NONE);
	 	       	openFlg=true;//ポートオープン 成功
	        }else{
	        	openFlg = false;//ポートオープン 失敗
	        	useFlg = false;
	            return false;
	        }
	        useFlg =false;
	        return true; //例外無く終了

	    }catch(Exception e) {
	    	useFlg = false;
	    	return false;//例外発生し、ポートオープン失敗 openFlgはfalse
	    }
	}

	/**
	 * GPIOボード用シリアルポートクローズ
	 * @return true:ポートクローズ成功  false:ポートクローズ失敗
	 * @throws InterruptedException
	 */
	public static boolean close() {
		//デバッグコード---------
		if( VisonController.debugFlg ) {
			try {
				Thread.sleep(debugSleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
            System.out.println("Debug:::Port  closeed successfully...");
			return true;
		}
		//-----------------------
		if( useFlg ) return false;
		useFlg = true;

		if( !openFlg ) {
			useFlg = false;
			return false; //ポートが開いていない場合はportオブジェクトがnullの為リターン
		}
		try {
	        if(port.closePort() == true){//ポートクローズ
	            System.out.println("Port " + port.toString() + " closeed successfully...");
	        }else{
				useFlg = false;
	            return false;
	        }
	    }catch(Exception e) {
	    	e.printStackTrace();
			useFlg = false;
	    	return false;
	    }
		useFlg = false;
	    return true;
	}

	/**
	 * PLCからオールクリア信号を受信する
	 * 判定が有効化になれば自動的にPLCから送信される
	 * @return '1':オールクリア信号受信 '0':非受信
	 * @throws InterruptedException
	 */
	public static String clearSignal(){
		//デバッグコード----
		if( VisonController.debugFlg ) {
			try {
				Thread.sleep(debugSleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Debug:::clear signal OFF");
			return "0";
		}
		//------------------
		if( useFlg ) return "0";
		useFlg = true;

		if( !openFlg ) {
			useFlg = false;
			return "PortNotOpen";
		}
		String rt;
		try {
	        port.writeString("\r");

	        if( adcFlg ) {

                port.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
                port.writeString("adc read 1\r");
                Thread.sleep(sleepTime);
		        rt = port.readString();
		        if( rt != null)  {
			        String tmpStr[] = rt.split("\n\r");
			        String tmprt ="0";
			        for( int i=0;i<tmpStr.length;i++ ) {
			        	if( tmpStr[i].matches(".*read.*") ) {
			        		tmprt = tmpStr[i+1];
			        		break;
			        	}
			        }
			        if( Integer.valueOf(tmprt) > adcThresh){
			        	rt="1";
			        }else {
			        	rt ="0";
			        }
		        }else {
		        	rt ="0";
		        }

	        }else {
	        	port.writeString("gpio read 1\r");
		        rt = port.readString();
		        //返り値のフォーマット\n\r>\n\r>gpio read 0\n\r1\n\r>
		        if( rt != null)  {
			        String tmpStr[] = rt.split("\n\r");
			        String tmprt ="0";
			        for( int i=0;i<tmpStr.length;i++ ) {
			        	if( tmpStr[i].matches(".*read.*") ) {
			        		tmprt = tmpStr[i+1];
			        		break;
			        	}
			        }
			        rt=tmprt;
		        }else {
		        	rt ="0";
		        }
		        Thread.sleep(sleepTime );
		        port.purgePort(SerialPort.PURGE_RXCLEAR & SerialPort.PURGE_TXCLEAR);
	        }
		}catch(Exception e) {
			e.printStackTrace();
			useFlg = false;
			return "clearSignal Read error";
		}

		useFlg = false;
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
		if( useFlg ) return "0";
		useFlg = true;

		if( !openFlg ) {
			useFlg = false;
			return "PortNotOpen";
		}
		String rt = "0";
		try {
	        port.writeString("\r");
	        if( adcFlg ) {
                //Thread.sleep(sleepTime);
                port.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
                port.writeString("adc read 0\r");
                Thread.sleep(sleepTime);
		        rt = port.readString();
		        if( rt != null)  {
			        String tmpStr[] = rt.split("\n\r");
			        String tmprt ="0";
			        for( int i=0;i<tmpStr.length;i++ ) {
			        	if( tmpStr[i].matches(".*read.*") ) {
			        		tmprt = tmpStr[i+1];
			        		System.out.println("adc read 0"+ tmprt);
			        		break;
			        	}
			        }
			        if( Integer.valueOf(tmprt) > adcThresh){
			        	rt="1";
			        }else {
			        	rt ="0";
			        }
		        }else {
		        	rt ="null";
		        }

	        }else {
		        port.writeString("gpio read 0\r");
		        //返り値のフォーマット\n\r>\n\r>gpio read 0\n\r1\n\r>
		        rt = port.readString();
		        if( rt != null ) {
		        	System.out.println(rt);
		        	String tmpStr[] = rt.split("\n\r");
		        	String tmprt = "0";
			        for( int i=0;i< tmpStr.length;i++ ) {
			        	if( tmpStr[i].matches(".*read.*") ) {
			        		tmprt = tmpStr[i+1];
			        		break;
			        	}
			        }
			        rt = tmprt;
		        }else {
		        	rt="0";
		        }
		        Thread.sleep(sleepTime );
		        port.purgePort(SerialPort.PURGE_RXCLEAR & SerialPort.PURGE_TXCLEAR);
	        }
		}catch(Exception e) {
			e.printStackTrace();
			useFlg = false;
			return "shutterSignal Read error";
		}
		//System.out.println("シャッターFLG = "+rt);
		useFlg = false;
		return rt;//読み込みに成功したら'1'又は'0'を返す

	}

	/**
	 * PLCへOKシグナルを継続送信
	 * GPIO 1へ'1'をセットした後は、ngSignalON()メソッドでクリアされるまで'1'が保持される
	 * @return true:GPIO 1へセット成功 false:セット失敗
	 * @throws InterruptedException
	 */
	public static boolean OkSignalON(){
		//デバッグコード----
		if( VisonController.debugFlg ) {
			try {
				Thread.sleep(debugSleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	        System.out.println("Info: <gpio set 3> Command sent...");
	        return true;
		}
		//------------------
		if( useFlg ) return false;
		useFlg = true;

		if( !openFlg ) {
			useFlg = false;
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
			useFlg = false;
			return false;
		}
		useFlg = false;
        return true;
	}

	/**
	 * PLCへNGシグナル送信
	 * GPIO 1を'0'にセットするとPLC側では画像処理判定NGと判断し設備をシーケンスに従って止める
	 * 設備停止有効化はスリッターが回りだしてから10秒後となる
	 * @return
	 * @throws InterruptedException
	 */
	public static boolean ngSignalON() {
		//デバッグコード----
		if( VisonController.debugFlg ) {
			try {
				Thread.sleep(debugSleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	        System.out.println("Debug:::Info: <gpio clear 3> Command sent...");
			return true;
		}
		//------------------
		if( useFlg ) return false;
		useFlg = true;

		if( !openFlg ) {
			useFlg = false;
			return false;
		}
		try {
	        port.writeString("\r");
	        port.writeString("gpio clear 3\r");//IO 3クリア 出力ゼロ
	        System.out.println("Info: <gpio clear 3> Command sent...");

	        Thread.sleep(sleepTime );
	        port.purgePort(SerialPort.PURGE_RXCLEAR & SerialPort.PURGE_TXCLEAR);
		}catch(Exception e) {
			e.printStackTrace();
			useFlg = false;
			return false;
		}

		useFlg = false;
        return true;
	}

	/**
	 * 全ポート読み出し
	 * @return
	 */
	public static String readAll(){
		if( useFlg ) return "erroe";
		useFlg = true;

		if( !openFlg ) {
			useFlg = false;
			return "PortNotOpen";
		}
		String rt = "error";
		try {
	        port.writeString("\r");
	        port.writeString("gpio readall\r");

	        rt = port.readString();


	        Thread.sleep(sleepTime*10);
	        port.purgePort(SerialPort.PURGE_RXCLEAR & SerialPort.PURGE_TXCLEAR);
		}catch(Exception e) {
			e.printStackTrace();
			useFlg = false;
			return "All Read error";
		}

		useFlg = false;
		return rt;//読み込みに成功したら'1'又は'0'を返す

	}

}