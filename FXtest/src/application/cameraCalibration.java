package application;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point3;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class cameraCalibration {
	final String picFolderPathString = "./chess_image";
	final Path picFolderPath = Paths.get(picFolderPathString);

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	public static void main(String[] args) {
		cameraCalibration c = new cameraCalibration();
		System.out.println("開始しました。");
		c.processer();
		System.out.println("終了しました。");
	}
	public boolean processer() {
		if (!Files.exists(picFolderPath)) {
			System.err.println("The picture folder does not exist!");
			return false;
		}
		if (!Files.isDirectory(picFolderPath)) {
			System.err.println("The path is not a folder!");
			return false;
		}

		List<Mat> imagePoints = new ArrayList<>(); // 各撮影画像のコーナーの二次元座標を入れる。
		final Size patternSize = new Size(7, 10); // 探査するコーナーの数

		List<Mat> outputFindChessboardCorners = new ArrayList<>();
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(picFolderPath)) {
			for (Path path : ds) {
				System.out.println(path.toString());

				final Optional<Mat> outputMat = findChessboardCorners(path.toString(), imagePoints, patternSize);

				if (outputMat.isPresent()) {
					outputFindChessboardCorners.add(outputMat.get());
					System.out.println("successful to find corners.");
				} else {
					System.err.println("unsuccessful to find corners.");
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		List<Mat> objectPoints = getObjectPoints(outputFindChessboardCorners.size(), patternSize); // チェスボードのコーナーの三次元座標(z=0)を、撮影画像枚数分入れる。

		final Size imageSize = outputFindChessboardCorners.get(0).size();

		// 受け取るもの
		Mat cameraMatrix = new Mat(), distortionCoefficients = new Mat();
		List<Mat> rotationMatrixs = new ArrayList<>(), translationVectors = new ArrayList<>();

		System.out.println("ni: " + objectPoints.get(0).checkVector(3, CvType.CV_32F));
		System.out.println(objectPoints.get(0).dump());
		System.out.println("ni1: " + imagePoints.get(0).checkVector(2, CvType.CV_32F));
		System.out.println(imagePoints.get(0).dump());

		Calib3d.calibrateCamera(objectPoints, imagePoints, imageSize,
				cameraMatrix, distortionCoefficients, rotationMatrixs, translationVectors);

		System.out.println("CameraMatrix: " + cameraMatrix.dump());
		System.out.println("DistortionCoefficients: " + distortionCoefficients.dump());

        Map<String, Mat> exportMats = new HashMap<>();
        exportMats.put("CameraMatrix", cameraMatrix);
        exportMats.put("DistortionCoefficients", distortionCoefficients);
        final Path exportFilePath = Paths.get("./CameraCalibration.xml");
        MatIO.exportMat(exportMats, exportFilePath);

        return true;
	}

	public List<Mat> getObjectPoints(int size, Size patternSize) {
		List<Mat> objectPoints = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			objectPoints.add(getObjectPoint(patternSize));
		}
		return objectPoints;
	}

	public MatOfPoint3f getObjectPoint(Size patternSize) {
		MatOfPoint3f objectPoint = new MatOfPoint3f();

		List<Point3> objectPoint_ = new ArrayList<>();
		// final Size patternSize = new Size(7, 10); // 探査するコーナーの数
		for (int row = 0; row < patternSize.height; row++) {
			for (int col = 0; col < patternSize.width; col++) {
				objectPoint_.add(getPoint(row, col));
			}
		}

		objectPoint.fromList(objectPoint_);
		return objectPoint;
	}

	public Point3 getPoint(int row, int col) {
		final double REAL_HEIGHT = 4.0, REAL_WIDTH = 4.0;
		return new Point3(col * REAL_WIDTH, row * REAL_HEIGHT, 0.0); // 多分x, y, zはこういう感じ。
	}

	public Optional<Mat> findChessboardCorners(String picPathString, List<Mat> imagePoints, Size patternSize) {
		Mat inputMat = Imgcodecs.imread(picPathString);
		Mat mat = inputMat.clone();
		// final Size patternSize = new Size(6, 9); // 探査するコーナーの数
		MatOfPoint2f corners = new MatOfPoint2f(); // in, 検出されたコーナーの二次元座標のベクトルを受け取る。

		Imgproc.cvtColor(inputMat, inputMat, Imgproc.COLOR_BGR2GRAY);

		final boolean canFindChessboard = Calib3d.findChessboardCorners(inputMat, patternSize, corners);

		if (!canFindChessboard) {
			System.err.println("Cannot find Chessboard Corners.");
			return Optional.empty();
		}

		imagePoints.add(corners);

		Calib3d.drawChessboardCorners(mat, patternSize, corners, true);
    	File folder = new File("./chess_imageDst");
    	if( !folder.exists()) {
    		if( !folder.mkdir() ) {
    			System.out.println("chess_imageDstフォルダの作成に失敗"+"\n");
    		}
    	}

		Path picPath = Paths.get(picPathString);
		Path path = Paths.get("./chess_imageDst", picPath.getFileName().toString());
		Imgcodecs.imwrite(path.toString(), mat);

		return Optional.of(inputMat);
	}
}
/*
 * JavaでOpenCVのカメラキャリブレーション(CameraCalibration)をやった
Java
OpenCV
はじめに
カメラキャリブレーションというものが必要になったのでやってみました。
注意: 筆者は独学で行なっているため、不正確な記述が含まれることがあります。

カメラキャリブレーション(CameraCalibration)とは
空間の位置と画像上の位置を変換できる式がありますが、これはカメラのパラメーターが必要です。
カメラパラメーターには種類があり、大きく内部パラメーターと外部パラメーターに分けられます。
また、内部パラメーターに含まれるのかわからないですが、レンズ歪み係数(歪曲収差)というものもあります。

内部パラメーター
内部パラメーターとは、「カメラによって決定されるパラメーター」です。
・焦点距離 f
・画素の物理的な間隔 δu, δv
・正規化座標に対する画像座標における画像中心 cu, cv

外部パラメーター
外部パラメーターとは、「ワールド座標系に対するカメラの位置と姿勢のパラメーター」です。
・回転行列 R3
・平行移動ベクトル t3

カメラキャリブレーションとは
さて、カメラキャリブレーションとは、このカメラのパラメーターを求めることです。
この際に得られる、内部パラメーターと外部パラメーターを合わせて、カメラ行列または透視投影行列と言います。

手法としては、予め位置がわかっている空間点と、その画像上への投影点(つまり、画像上の座標)を行列の式に当てはめて決定するします。

具体的に言えば、キャリブレーションターゲット(チェスボードなど)を動かしながら写真を撮影します。
チェスボードの格子の長さが既知であるため、格子点間の距離がわかり、空間上の位置が定まります。(逆に言えば、ワールド座標系を定めます)
また、画像上でも格子点の座標を取得します。(画像座標の取得)
画像を複数枚撮影し、最小二乗法を用いて、パラメーターを求めます。

引用(参考)元: 「ディジタル画像処理[改訂新版]」, なお都合より表現を改変している。
参考: カメラキャリブレーションと3次元再構成 - OpenCV.jp

OpenCVでカメラキャリブレーションを行う
今回は比較的簡単だと思う、チェストボードによるカメラキャリブレーションを以下の大まかな手順で行いました。

コードについてはOpenCV_CameraCalibration - GitHubを参照してください。

ワールド座標系と空間上の格子点の座標を定める
画像上のチェストボードの格子点の座標を求める [Calib3d.findChessboardCorners()を利用]
Calib3d.calibrateCamera()を実行する
返された カメラ行列とレンズ歪み係数を保存する 概要はこのような感じである。
それぞれについて少し詳しく解説します。

ワールド座標系と空間上の格子点の座標を定める
ワールド座標系を、平面なキャリブレーションターゲット(チェストボード)の平面がZ=0となるように任意に定めます。
このときに格子点の間隔を測って、mm単位でPointを定めます。
ここで呼び出ししています。

List<Mat> objectPoints = getObjectPoints(outputFindChessboardCorners.size(), patternSize); // チェスボードのコーナーの三次元座標(z=0)を、撮影画像枚数分入れる。
OpenCV_CameraCalibration_L68 - GitHub

getObjectPoints()の中身は以下のようになっています。

    public List<Mat> getObjectPoints(int size, Size patternSize) {
        List<Mat> objectPoints = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            objectPoints.add(getObjectPoint(patternSize));
        }
        return objectPoints;
    }

    public MatOfPoint3f getObjectPoint(Size patternSize) {
        MatOfPoint3f objectPoint = new MatOfPoint3f();

        List<Point3> objectPoint_ = new ArrayList<>();
        // final Size patternSize = new Size(6, 9); // 探査するコーナーの数
        for (int row = 0; row < patternSize.height; row++) {
            for (int col = 0; col < patternSize.width; col++) {
                objectPoint_.add(getPoint(row, col));
            }
        }

        objectPoint.fromList(objectPoint_);
        return objectPoint;
    }

    public Point3 getPoint(int row, int col) {
        final double REAL_HEIGHT = 20.0, REAL_WIDTH = 20.0;
        return new Point3(col * REAL_WIDTH, row * REAL_HEIGHT, 0.0); // 多分x, y, zはこういう感じ。
    }
OpenCV_CameraCalibration_L96 - GitHub

画像上のチェストボードの格子点の座標を求める
自分で、findChessboardCorners()というメソッドを書きました。呼び出しは以下のようになっています。

        List<Mat> outputFindChessboardCorners = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(picFolderPath)) {
            for (Path path : ds) {
                System.out.println(path.toString());

                final Optional<Mat> outputMat = findChessboardCorners(path.toString(), imagePoints, patternSize);

                if (outputMat.isPresent()) {
                    outputFindChessboardCorners.add(outputMat.get());
                    System.out.println("successful to find corners.");
                } else {
                    System.err.println("unsuccessful to find corners.");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
OpenCV_CameraCalibration_L50 - GitHub

findChessboardCorners()の中身は以下のようになっています。
Calib3d.findChessboardCorners()というメソッドを利用しました。
なお、返却しているMatは、Calib3d.findChessboardCorners()が正常に実行できた画像が入っています。

    public Optional<Mat> findChessboardCorners(String picPathString, List<Mat> imagePoints, Size patternSize) {
        Mat inputMat = Imgcodecs.imread(picPathString);
        Mat mat = inputMat.clone();
        // final Size patternSize = new Size(6, 9); // 探査するコーナーの数
        MatOfPoint2f corners = new MatOfPoint2f(); // in, 検出されたコーナーの二次元座標のベクトルを受け取る。

        Imgproc.cvtColor(inputMat, inputMat, Imgproc.COLOR_BGR2GRAY);

        final boolean canFindChessboard = Calib3d.findChessboardCorners(inputMat, patternSize, corners);

        if (!canFindChessboard) {
            System.err.println("Cannot find Chessboard Corners.");
            return Optional.empty();
        }

        imagePoints.add(corners);

        Calib3d.drawChessboardCorners(mat, patternSize, corners, true);

        Path picPath = Paths.get(picPathString);
        Path folderPath = Paths.get("S:\\CameraCaliblation\\2018-12-31_output");
        Path path = Paths.get(folderPath.toString(), picPath.getFileName().toString());

        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
            try {
                System.out.println("There was no folder, so it is createing a folder. : " + folderPath.toString());
                Files.createDirectory(folderPath);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        Imgcodecs.imwrite(path.toString(), mat);

        return Optional.of(inputMat);
    }
Calib3d.calibrateCamera()を実行する
先程用意した空間上の格子点の座標と、先程求めた画像上のチェストボードの格子点の座標を引数として、Calib3d.calibrateCamera()を実行します。
また、このメソッドでカメラ行列とレンズ歪み係数の値が返されるので、それを格納する変数を作っておきます。

        // 受け取るもの
        Mat cameraMatrix = new Mat(), distortionCoefficients = new Mat();
        List<Mat> rotationMatrixs = new ArrayList<>(), translationVectors = new ArrayList<>();

        Calib3d.calibrateCamera(objectPoints, imagePoints, imageSize,
                cameraMatrix, distortionCoefficients, rotationMatrixs, translationVectors);
OpenCV_CameraCalibration_L81 - GitHub

返された カメラ行列とレンズ歪み係数を保存する
カメラ行列とレンズ歪み係数を取得できましたが、この処理を毎回行うのは面倒かつ無駄なので、行列の値を保存しましょう。
CやPythonならはOpenCV側でMatの値を入力・出力するものがあるのですが、Java版ではないので、自分で今回は作りました。
Javaの標準ライブラリにあるXMLの入出力を使いました。

        Map<String, Mat> exportMats = new HashMap<>();
        exportMats.put("CameraMatrix", cameraMatrix);
        exportMats.put("DistortionCoefficients", distortionCoefficients);
        final Path exportFilePath = Paths.get("S:\\CameraCaliblation\\CameraCalibration_2018-12-31.xml");
        MatIO.exportMat(exportMats, exportFilePath);
OpenCV_CameraCalibration_L87 - GitHub

MatIOの中身は、OpenCV_CameraCalibration_MatIO.java - GitHub を参照してください。

おわりに
一応、これを使って、ArUcoのARマーカーの姿勢推定 を行ったところ、正常に動作しました。
我が部の情報班の後輩にも理解&実行してもらい、成功しましたので良かったです。

参考リンク
・OpenCV - カメラキャリブレーションを行う方法 - Pynote
・カメラキャリブレーション (OpenCV 1.0) - OpenCV.jp
・OpenCVとVisual C++による画像処理と認識
・pythonとOpenCVでカメラキャリブレーション（1個のカメラの内部パラメータと歪み係数を求める）するコード（パクリ）
・カメラ校正
・OpenCV 2.4.11&C++によるカメラキャリブレーション - 技術的特異点
・Camera Calibration and 3D Reconstruction - OpenCV公式- 改めて公式サイト見ると良い説明ありますね。
・CC_Controller.java - opencv-java/camera-calibration(GitHub) - Java版のやり方に参考になりました。
・cameracalibration - opencv(GitHub)
*/
