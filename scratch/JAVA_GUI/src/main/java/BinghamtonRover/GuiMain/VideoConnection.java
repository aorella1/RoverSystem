package BinghamtonRover.GuiMain;

import org.apache.commons.lang3.Validate;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

/**
 * A Thread handling the connection between the Video Server and Client
 * As of now, this Thread should send packets of stuffs to the client.
 * but every packet should be sent only if client returns an acknowledgement
 * for receiving the last packet. Every packet right now will be Random
 * Strings. Might need to use a Queue in the future for the packets.
 */
class VideoConnection extends Thread
{

    //Used for generating random strings
    private static String Characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxxyz0123456789";


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
}
