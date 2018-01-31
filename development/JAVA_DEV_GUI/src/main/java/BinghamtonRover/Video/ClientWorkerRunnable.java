package BinghamtonRover.Video;

import org.apache.commons.lang3.Validate;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;


import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

import static BinghamtonRover.Video.Utils.intToBytes;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;

public class ClientWorkerRunnable implements Runnable{
    private OpenCVFrameGrabber feed;
    private DatagramSocket serverSocket;
    private InetAddress addr;
    private int port;
//    //Every DatagramPacket is limited to 65507 bytes
//    private static int PACKET_LIMIT = 65507;

    ClientWorkerRunnable(OpenCVFrameGrabber f, DatagramSocket socket, InetAddress addr, int port){
        feed = f;
        serverSocket = socket;
        this.addr = addr;
        this.port = port;
    }



    /**
     * This method is still in the works. We are currently trying to figure out how to turn a frame
     * into a byte array, which should be serializable. We will then send the byte array over to the
     * client, who will then take the btyes, reassemble the frame, and display the frames one at a time.
     * Not sure if this is the best way to go about this, but that is the idea we had in mind.
     */
    @Override
    public void run() {
        opencv_core.IplImage img;
        try {
            Frame frame;
            //DataOutputStream dos = new DataOutputStream();
            DatagramPacket packet;

            while((frame = feed.grab()) != null) {
                //Grab a frame, convert it to type IplImage, then save it as a jpg file
                OpenCVFrameConverter frameToImg = new OpenCVFrameConverter.ToIplImage(); //put this outside the loop?
                img = (opencv_core.IplImage) frameToImg.convert(frame);
                cvSaveImage("frame.jpg", img);
                File file = new File("frame.jpg");

                //Read the jpg file and write it into a byte, then wrap this byte array into the Datagram Packet
                //first send a packet about the length of the each packet fragment
                try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                    ByteArrayOutputStream bos;

                    if(Integer.MAX_VALUE < file.length())
                        System.out.println("File too big for int");

                    //get the length of the file and figure out the size of each fragment of the packet
                    int dataLen = (int)file.length();
                    System.out.println("Sending " + dataLen + " amount of data");
                    int [] packetLens = Utils.getPacketsSize(dataLen);

                    //Send the size of each packet Fragment
                    packet = new DatagramPacket(intToBytes(packetLens), packetLens.length, addr, port);
                    serverSocket.send(packet);

                    for (int i = 0; i < packetLens.length; i++)
                    {

                        // Create a Datagram Packet for each length inside the array
                        // and read the file into each packet Fragment accordingly.
                        bos = new ByteArrayOutputStream(packetLens[i]);
//                        packet = new DatagramPacket(new byte[packetLens[i]], packetLens[i], addr, port);

//                        ByteBuffer dbuf = ByteBuffer.allocate(4);
//                        byte[] fragLength = dbuf.putInt(packetLens[i]).array();

                        while(bos.size() < packetLens[i])
                        {
                            bos.write(is.read());
                        }


                        byte [] fragPacket = bos.toByteArray();
                        packet = new DatagramPacket(fragPacket, fragPacket.length, addr, port);
                        serverSocket.send(packet);
                    }



//                    packet = new DatagramPacket(dbuf.array(), 4, addr, port);
//                    serverSocket.send(packet);

//                    int val = 0;
//                    byte [] imgData;
//                    while (val != -1)
//                    {
//                        bos = new ByteArrayOutputStream();
//                        bos.write();
//
//                        while(bos.size() < PACKET_LIMIT && (val = is.read()) != -1)
//                            bos.write(val);
//                        imgData = bos.toByteArray();
//                        System.out.println("imgData Fragment length: " + imgData.length);
//                        packet = new DatagramPacket(imgData, imgData.length, addr, port);
//                        serverSocket.send(packet);
//                        bos.flush();
//                        System.out.println("bos length:" + bos.size());
//                    }
//                    byte [] imgData = bos.toByteArray();
//                    System.out.println("imgData.length: " + imgData.length);


                }

                try
                {
                    //try to wait 5 ms to receive a client response for stop sending data
                    serverSocket.setSoTimeout(5);
                    serverSocket.receive(packet);
                    System.out.println("Received client response");
                    byte response = packet.getData()[0];
                    if(response == 0x01) {
                        System.out.println("Client has requested to stop");
                        break;
                    }

                }
                catch(SocketTimeoutException e)
                {
                }



            }
        }
        catch(SocketException e){
            e.printStackTrace();
            System.out.println("Client disconnected.");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
