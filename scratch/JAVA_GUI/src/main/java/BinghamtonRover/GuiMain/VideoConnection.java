package BinghamtonRover.GuiMain;

import org.apache.commons.lang3.Validate;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
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
    private static String gsCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";


    private Socket coClientSocket;


    public VideoConnection(Socket aoClientSocket)
    {
        System.out.println(aoClientSocket.isInputShutdown());
        Validate.notNull(aoClientSocket);
        this.coClientSocket = aoClientSocket;
    }

    public void run()
    {

        try{

            BufferedOutputStream loDataOut = new BufferedOutputStream(coClientSocket.getOutputStream());
            while(true){
                loDataOut.write(randStr(256).getBytes());
                loDataOut.flush();
            }

        }
        catch(SocketException e)
        {
            System.out.println("A Connection has been closed");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        try{
            coClientSocket.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private String randStr(int anLength){

        Random r = new Random();
        StringBuilder loSB = new StringBuilder(anLength);

        while(loSB.length() < loSB.capacity()){
            loSB.append(gsCharacters.charAt(r.nextInt(gsCharacters.length())));
        }

        return loSB.toString();

    }

}
