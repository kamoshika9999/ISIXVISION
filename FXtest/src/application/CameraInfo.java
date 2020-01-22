package application;

import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class CameraInfo {
	private double h;
	private double w;

	VideoCapture capture = new VideoCapture(0);

	public CameraInfo() {
		w = capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
		h = capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
	}

	public double getFrameHight() {
		return h;
	}

	public double getFrameWidth() {
		return w;
	}

}
