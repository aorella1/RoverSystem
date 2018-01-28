package BinghamtonRover.GuiMain;

import org.apache.commons.lang3.Validate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;

/**
 * A Thread handling the connection between the Video serve and the client.
 * The server will send data to the client via output stream. The server
 * Will send an integer indicating the length of the string and then send
 * the data.
 */
class VideoConnection //extends Thread
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


    /**
     * This method will instantiate a data output stream and send the data (image) through it to
     * the client. if the client side run into problems the socket connection will be broken and
     * ioStream will not be created correctly. The method will catch a socket exception and, ideally
     * The VideoConnection class will remove it self from the list of connections in the server,
     * but for now, the system will just exit if the socket exception is thrown.
     * @param data, the byte array data converted from the image frame.
     */
    public void sendData(byte[] data) {

        try {
            BufferedOutputStream loDataOut = new BufferedOutputStream(coClientSocket.getOutputStream());
            BufferedInputStream loDataIn = new BufferedInputStream(coClientSocket.getInputStream());

            //first send the expected length of the byte stream
            loDataOut.write(data.length);
            loDataOut.flush();

            loDataOut.write(data);
            loDataOut.flush();
        }
        catch(SocketException e){
            System.out.println("A socket connection is probably broken");
            System.exit(0);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * This Method Generates a random string of specified length from a set of characters
     * @param anLength, the length of the random string
     * @return a random string of specified length
     */
    private String randStr(int anLength){

        Random r = new Random();
        StringBuilder loSB = new StringBuilder(anLength);

        while(loSB.length() < loSB.capacity()){
            loSB.append(gsCharacters.charAt(r.nextInt(gsCharacters.length())));
        }

        return loSB.toString();

    }

}
