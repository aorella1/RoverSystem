package BinghamtonRover.Video;



import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import javafx.scene.image.Image;
import org.bytedeco.javacpp.opencv_core.*;
//import org.opencv.imgproc.Imgproc;
//import org.bytedeco.javacpp.presets.opencv_imgproc;
//import org.opencv.videoio.VideoCapture;
import org.bytedeco.javacpp.opencv_videoio.VideoCapture;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static BinghamtonRover.Video.Utils.mat2Image;

public class VideoFeedWorkerRunnable implements Runnable {

    private OpenCVFrameGrabber feed;

    VideoFeedWorkerRunnable(OpenCVFrameGrabber feed) {
        this.feed = feed;
    }

    /**
     * This thread calls the startVideoFeed() method from WebCamServer.
     * We will probably want to take the method body and just place it in here,
     * and eliminate the method from the WebCamServer.
     */
    @Override
    public void run() {
        OpenCVvideofeed();
    }


    private static int gnCameraID = 0;
    private VideoCapture coVideoCapture = new VideoCapture();
    private ScheduledExecutorService coTimer;

    /**
     * This Method accesses the camera on the machine and executes a Runnable to grab a frame
     * every 33ms. The Frame is stored in a Mat Object, which I plans to convert to a frame
     * and then display onto a canvas
     */
    private void OpenCVvideofeed()
    {

        //open the camera
        coVideoCapture.open(gnCameraID);

        //Open a Canvas
        CanvasFrame cFrame = new CanvasFrame("Server Live Feed", CanvasFrame.getDefaultGamma()/feed.getGamma());

        Runnable loRunnableFrameGrabber = () -> {
            // OpenCV VideoCapture Grabs a frame
            // Then convert it to FX Image
            // Then update the image onto the imageView of the GUI
            Mat loMatFrame = grabMatFrame();

            //This method comes from the Util Class, converts the mat object to Image
            //Image loImageToShow = mat2Image(loMatFrame);

            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            Frame frame = converter.convert(loMatFrame);
            if (cFrame.isVisible()) {
                cFrame.showImage(frame);
            }
            else System.exit(0);
        };

        //Spawn OR run the thread frameGrabber every 33 ms, 30 times a second
        coTimer = Executors.newSingleThreadScheduledExecutor();
        coTimer.scheduleAtFixedRate(loRunnableFrameGrabber, 0, 33, TimeUnit.MILLISECONDS);
    }


    /**
     * This Method accesses the camera and grab a frame and store it into a openCV Mat object
     * @return MatFrame, a Mat Object storing the current frame captured from the camera.
     */
    private Mat grabMatFrame()
    {
        Mat loMatFrame = new Mat();

        if (coVideoCapture.isOpened())
        {
            try
            {
                //If our VideoCapture is opened, read the current frame of the camera
                coVideoCapture.read(loMatFrame);

                //If what we read is not empty, then convert the color to Gray Scale
                if(loMatFrame.empty())
                {
                    //Imgproc dosent seems to be compatible with javacpp library
//                    Imgproc.cvtColor(loMatFrame, loMatFrame, Imgproc.COLOR_BGR2GRAY);
                    System.out.println("The Mat Frame is empty");
                }
            }
            catch (NullPointerException aoException)
            {
                System.out.println("Exception thrown during the image elaboration: " + aoException);
                aoException.printStackTrace();
                System.exit(1);
            }
        }
        return loMatFrame;
    }

}
