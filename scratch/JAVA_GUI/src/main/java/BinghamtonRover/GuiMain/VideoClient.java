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

    public void run()
    {
        try{

//            BufferedWriter dataOut = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            BufferedInputStream dataIn = new BufferedInputStream(clientSocket.getInputStream());

            while(true) {

                //Allocate 2MB of space for the incoming data
                byte[] buffer = new byte[4194304/2];
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
