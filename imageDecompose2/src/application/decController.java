package application;
import application.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
public class decController {
	//imported image
	@FXML
	private ImageView originalImage;
	//image after doing canny edge detection
	@FXML
	private ImageView transformedImage;
	@FXML
	// the main stage
	private Stage stage;
	// canny threshold value
	@FXML
	private Slider threshold;
	// the JavaFX file chooser
	private FileChooser fileChooser;
	// support variables
	private Mat image;
	private Mat imgSource;
	private List<Mat> planes;
	private ScheduledExecutorService timer;
	// the final complex image
	protected void init()
	{
		this.fileChooser = new FileChooser();
		this.image = new Mat();
		this.planes = new ArrayList<>();
		//Timer to update canny for use with slider if transformed image has an image 
	}
	/**
	 * Load an image from disk
	 */
	@FXML
	protected void loadImage()
	{
		// show the open dialog window
		File file = this.fileChooser.showOpenDialog(this.stage);
		if (file != null)
		{
			this.image = Imgcodecs.imread(file.getAbsolutePath());
			// shows image
			this.updateImageView(originalImage, Utils.mat2Image(this.image));
			// read the image in gray scale
			this.image = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			this.imgSource = Imgcodecs.imread(file.getAbsolutePath());
			// set a fixed width
			this.originalImage.setFitWidth(500);
			// preserve image ratio
			this.originalImage.setPreserveRatio(true);
			// empty the image planes and the image views if it is not the first
			// loaded image
			if (!this.planes.isEmpty())
			{
				this.planes.clear();
				this.transformedImage.setImage(null);
			}
		}
	}
	/** Canny edge detection
	 * note to self stop being lazy and implement Gaussianblur
	 */
	@FXML
	protected void Canny() {
		Runnable frameGrabber = new Runnable() {
			@Override
			public void run() {
				Mat detectedEdges = new Mat();
				Mat grayImage = new Mat();
				Imgproc.cvtColor(imgSource, grayImage, Imgproc.COLOR_BGR2GRAY);
				// reduce noise with a 3x3 kernel
				Imgproc.blur(grayImage, detectedEdges, new Size(3, 3));
				Imgproc.threshold(grayImage,detectedEdges,50,255,Imgproc.THRESH_BINARY_INV);
				Imgproc.Canny(imgSource, detectedEdges, threshold.getValue(), threshold.getValue() * 50);
				Mat dest = new Mat();
				Core.add(dest, Scalar.all(0), dest);
				imgSource.copyTo(dest, detectedEdges);
				updateImageView(transformedImage, Utils.mat2Image(dest));
				transformedImage.setFitWidth(500);
				// preserve image ratio
				transformedImage.setPreserveRatio(true);
			}
		};
		this.timer = Executors.newSingleThreadScheduledExecutor();
		this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
	}
	/**
	 * Set the current stage (needed for the FileChooser modal window)
	 * 
	 * @param stage
	 *            the stage
	 */
	public void setStage(Stage stage)
	{
		this.stage = stage;
	}
	/**
	 * Update the {@link ImageView} in the JavaFX main thread
	 * 
	 * @param view
	 *            the {@link ImageView} to update
	 * @param image
	 *            the {@link Image} to show
	 */
	private void updateImageView(ImageView view, Image image)
	{
		Utils.onFXThread(view.imageProperty(), image);
	}
}