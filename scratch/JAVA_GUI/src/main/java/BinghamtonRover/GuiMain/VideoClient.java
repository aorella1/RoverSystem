package BinghamtonRover.GuiMain;

import java.io.*;
import java.net.Socket;

public class VideoClient extends Thread{

    private Socket clientSocket;


    public VideoClient(){
        this(3000);
    }

    public VideoClient(int Port)
    {
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

                byte[] buffer = new byte[512];
                int bytesRead = dataIn.read(buffer);
                System.out.println("Read " + bytesRead + " bytes From Server");
                String msg = new String(buffer, 0,bytesRead);
                System.out.println(msg);
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
        VideoClient client = new VideoClient(3000);
        client.run();
    }
}
