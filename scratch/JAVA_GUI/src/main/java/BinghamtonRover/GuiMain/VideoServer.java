package BinghamtonRover.GuiMain;


import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A Server thread which will handle the video feed transmission to the other PC over the network
 */
public class VideoServer extends Thread
{

    private ServerSocket coServerSocket;
    private boolean cbServerOpen;
    private ArrayList<VideoConnection> caoConnections;

    public VideoServer()
    {
        this(3000); //Calls the Constructor with default port number
    }

    public VideoServer(int Port)
    {
        try
        {
            //Adapted from https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
            InetAddress addr;
            try (final DatagramSocket socket = new DatagramSocket()){
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
//                System.out.println("IP address is: " + socket.getLocalAddress().getHostAddress());
                addr = socket.getLocalAddress();

            }

            coServerSocket = new ServerSocket(Port, 10, addr);  //Create ServerSocket
            System.out.println("Server socket created, IP Address: " + coServerSocket.getInetAddress().getHostAddress());
            System.out.println("Server socket listening to port: " + coServerSocket.getLocalPort());

            //Initializing arrayList of connections
            caoConnections = new ArrayList<VideoConnection>();

        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * While running, the Server socket will listen to port 3000 and accept
     * clients' connection request. Server will put the connection created between
     * itself and the client into a VideoConnection class and add that connection to
     * an ArrayList. The Server will continues to listen for clients as it is
     * possible to have multiple client.
     */
    public void run()
    {
        //Create an executor to execute the Connection Thread(s)
        ExecutorService loConnectionExecutor = Executors.newSingleThreadExecutor();


        try
        {
            while (!Thread.interrupted())
            {
                //Server waits for a client connection. once a connection is established, the ServerSocket passes the
                //client socket to the VideoConnection class for it to handle. the Connection is added to the ArrayList.
                //An executor is used here to execute the new VideoConnection thread
                System.out.println("Waiting for client connection request... ...");
                VideoConnection loClientConnection = new VideoConnection(coServerSocket.accept());
                caoConnections.add(loClientConnection);

                System.out.println("Connection Established with a client.");
                System.out.println("There are " + caoConnections.size() + " connections in total");



                //try to use start method instead of an executor
//                loConnectionExecutor.execute(clientConnection);
//                loClientConnection.start();


            }
            //Close the socket
            coServerSocket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally {
            System.out.println("Server is closed");
        }
    }

    /**
     * This method will convert the passed Image frame into a byte array and then
     * Give this processed byte array to theVideoConnection class to handle
     * @param aoFrame, Parameter image.
     */
    public void sendFrame(Image aoFrame){

        //If there is no connections, don't do anything
        if(caoConnections.isEmpty())
        {
            //System.out.println("There is no active connection right now");
            return;
        }
        else{

            //Convert Image Object to Byte []
            byte[] byteFrame;
            try {
                BufferedImage bufImage = SwingFXUtils.fromFXImage(aoFrame, null);
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                ImageIO.write(bufImage, "png", bao);
                byteFrame = bao.toByteArray();



                for(VideoConnection connection: caoConnections){
                    connection.sendData(byteFrame);
                }
            }
            catch(IOException e){
                System.out.println("Problem converting Image to Byte[]");
                e.printStackTrace();
            }

//            //Convert the Mat Object to byte[]
//            byte[] labImgData = new byte[(int) aoMatFrame.total() * aoMatFrame.channels()];
//            aoMatFrame.get(0,0, labImgData);
//
//            for(VideoConnection connection: caoConnections){
//                connection.sendData(labImgData);
//            }
        }

        //If the caoConnections is not empty, send the image to every client
    }



//    private void getIPAddress() {
//        try
//        {
//            Enumeration e = NetworkInterface.getNetworkInterfaces();
//            while(e.hasMoreElements())
//            {
//                NetworkInterface n = (NetworkInterface) e.nextElement();
//                Enumeration ee = n.getInetAddresses();
//                while(ee.hasMoreElements())
//                {
//                    InetAddress i  = (InetAddress) ee.nextElement();
//                    System.out.println(i.getHostAddress());
//                }
//            }
//        }
//        catch(SocketException e)
//        {
//            e.printStackTrace();
//        }
//    }
//
//    public void openServer(){ cbServerOpen = true;}
//    public void closeServer(){ cbServerOpen = false;}


    public void interrupt(){
        super.interrupt();
        System.out.println("Server Thread Interrupted");
    }

    public static void main (String [] arg)
    {
        VideoServer Server = new VideoServer(3000);
        Server.run();
    }
}
