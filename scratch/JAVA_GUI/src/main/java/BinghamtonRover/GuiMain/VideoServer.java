package BinghamtonRover.GuiMain;


import org.apache.commons.lang3.Validate;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A Server thread which will handle the video feed transmission to the other PC over the network
 */
public class VideoServer extends Thread
{
    //Used for random strings
    private static String Characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxxyz0123456789";

    private ServerSocket serverSocket;
    private boolean serverOpen;

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
                //System.out.println(socket.getLocalAddress().getHostAddress());
                addr = socket.getLocalAddress();

            }

            serverSocket = new ServerSocket(Port, 10, addr);  //Create ServerSocket
            System.out.println("Server socket created, IP Address: " + serverSocket.getInetAddress().getHostAddress());
            System.out.println("Server socket listening to port: " + serverSocket.getLocalPort());
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void run()
    {
        openServer();   //Enable the while loop

        //Create an executor to execute the Connection Thread(s)
        ExecutorService loConnectionExecutor = Executors.newSingleThreadExecutor();

        while (serverOpen)
        {
            try
            {
                //Server waits for a client connection. once a connection is established, the ServerSocket passes the
                //client socket to the VideoConnection class for it to handle. An executor is used here
                //to execute the new VideoConnection thread
                System.out.println("Waiting for client connection request... ...");
                VideoConnection clientConnection = new VideoConnection(serverSocket.accept());

                System.out.println("Connection Established with a client.");
                loConnectionExecutor.execute(clientConnection);


            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * A Thread handling the connection between the Video Server and Client
     * As of now, this Thread should send packets of stuffs to the client.
     * but every packet should be sent only if client returns an acknowledgement
     * for receiving the last packet. Every packet right now will be Random
     * Strings. Might need to use a Queue in the future for the packets.
     */
    class VideoConnection extends Thread
    {
        private Socket coClientSocket;


        public VideoConnection(Socket clientSocket)
        {
            Validate.notNull(clientSocket);
            this.coClientSocket = clientSocket;
        }

        public void run()
        {

            try{

                BufferedOutputStream dataOut = new BufferedOutputStream(coClientSocket.getOutputStream());
                while(true){
                    dataOut.write(randStr(256).getBytes());
                    dataOut.flush();
                }

            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void getIPAddress() {
        try
        {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while(e.hasMoreElements())
            {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while(ee.hasMoreElements())
                {
                    InetAddress i  = (InetAddress) ee.nextElement();
                    System.out.println(i.getHostAddress());
                }
            }
        }
        catch(SocketException e)
        {
            e.printStackTrace();
        }
    }

    public void openServer(){ serverOpen = true;}
    public void closeServer(){ serverOpen = false;}

    private String randStr(int length){

        Random r = new Random();
        StringBuilder sb = new StringBuilder(length);

        while(sb.length() < sb.capacity()){
//            System.out.println("Current length: " + sb.length());
//            System.out.println("Target capacity: " + sb.capacity());
            sb.append(Characters.charAt(r.nextInt(Characters.length())));
        }

        return sb.toString();

    }

    public static void main (String [] arg)
    {
        VideoServer Server = new VideoServer(3000);
        Server.run();
    }
}
