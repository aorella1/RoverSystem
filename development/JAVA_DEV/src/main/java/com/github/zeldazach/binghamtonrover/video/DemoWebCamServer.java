package com.github.zeldazach.binghamtonrover.video;
import org.bytedeco.javacv.*;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class DemoWebCamServer {
    private static final int port = 5001;
    private DatagramSocket socket;
    private InetAddress address;
    private char timestamp = 0;

    public DemoWebCamServer(String _address) throws SocketException, UnknownHostException {
        address = Inet4Address.getByName(_address);
        socket = new DatagramSocket(port, this.address);
    }

    public synchronized void sendPacket(byte[] data) throws IOException {
        // data size plus header size
        ByteBuffer img = ByteBuffer.wrap(data);
        ByteBuffer buff = ByteBuffer.allocate(data.length + 5);
        buff.putShort((short) 4);
        buff.put((byte) 2);
        buff.putShort((short) timestamp);
        buff.put(img);
        byte[] buffArray = buff.array();
        socket.send(new DatagramPacket(buffArray, buffArray.length, address, port));
        timestamp++;
    }

    /**
     * This method gets the server up and running, looking for clients that want access to the video feed.
     * A Thread is delegated to service each client, allowing for multiple clients to access the feed concurrently.
     * The server is printing out its port and host information, which a potential client must type in to try and get access.
     * Ideally, we want to ensure that the server doesn't prematurely die. Right now, a client can cause the server to crash
     * If an error occurs, which is unacceptable.
     * @param args command line arguments that are passed in when DemoWebCamServer is run
     */
    public static void main(String[] args) {
        try {

            DemoWebCamServer server = new DemoWebCamServer("0.0.0.0");
            try {
                Thread feedThread = new Thread(new DemoVideoFeedThread(server));
                feedThread.start();
                System.out.println("Server's port: " + port);
                feedThread.join();
            } catch (InterruptedException | FrameGrabber.Exception e) {
                e.printStackTrace();
            }
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
    }
}
