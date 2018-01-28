package BinghamtonRover.GuiMain;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

import static BinghamtonRover.Video.Utils.mat2Image;

public class VideoClient extends Thread{

    private Socket clientSocket;
    private ClientGuiController coGuiController;

    // Load in OpenCV3 libraries
    static
    {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public VideoClient(ClientGuiController controller){
        this(controller, 3000);
    }

    public VideoClient(ClientGuiController controller, int Port)
    {
        coGuiController = controller;
        try {
            clientSocket = new Socket("localhost", Port);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }


    /**
     * This method first reads the expected length of the incoming data, then
     * read the data into a buffer. Then from the buffer, reconstruct the
     * frame sent, and then update the frame onto the GUI.
     */
    public void run()
    {
        try{

//            BufferedWriter dataOut = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            BufferedInputStream dataIn = new BufferedInputStream(clientSocket.getInputStream());
            //Allocate 2MB of space for the incoming data
            byte[] buffer;
            byte[] dataLengthBuf = new byte[4];

            while(true) {

                //Read the length of the incoming data
                dataIn.read(dataLengthBuf, 0, 4);
                ByteBuffer wrapped = ByteBuffer.wrap(dataLengthBuf); // big-endian by default
                int length = wrapped.getInt();
                buffer = new byte[length];


                while (dataIn.available() < length)
                {
                }

                int bytesRead = dataIn.read(buffer);
                System.out.println("Read " + bytesRead + " bytes From Server");

                ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
                BufferedImage bufImage = ImageIO.read(bis);
                WritableImage image = SwingFXUtils.toFXImage(bufImage, null);


//                Mat loMatFrame = new Mat();
//                loMatFrame.put(0,0, buffer);
                coGuiController.updateImageView(image);

                //String msg = new String(buffer, 0,bytesRead);
                //System.out.println(msg);
            }


        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
//        catch(InterruptedException e)
//        {
//            e.printStackTrace();
//        }
    }

    public static void main(String[] args)
    {
//        VideoClient client = new VideoClient(3000);
//        client.run();
    }
}
