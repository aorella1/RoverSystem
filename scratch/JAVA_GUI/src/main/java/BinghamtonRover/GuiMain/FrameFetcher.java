package BinghamtonRover.GuiMain;

import javafx.scene.image.Image;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import static BinghamtonRover.Video.Utils.mat2Image;

public class FrameFetcher {

    private static int gnCameraID = 0;
    private VideoCapture coVideoCapture = new VideoCapture();

    /**
     * This Method will access the camera, grab a frame and store it
     * in a Mat object. Convert the Mat object to Image and then return it.
     * @return MatFrame, the current captured frame from the camera
     */
    public Image grabFrame()
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
                    System.out.println("The Frame is empty");
                    //Imgproc.cvtColor(loMatFrame, loMatFrame, Imgproc.COLOR_BGR2GRAY);
                }
            }
            catch (NullPointerException aoException)
            {
                System.out.println("Exception thrown during the image elaboration: " + aoException);
                aoException.printStackTrace();
                System.exit(1);
            }
        }

        // This method comes from the Util Class and
        return mat2Image(loMatFrame);
    }

    public void enable(){
        //open the Camera for video capture
        coVideoCapture.open(gnCameraID);
    }

    public void disable(){
        //close the VideoCapture
        coVideoCapture.release();
    }

    public boolean isEnabled(){
        return coVideoCapture.isOpened();
    }

}
