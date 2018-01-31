package BinghamtonRover.Video;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.Scanner;

import static BinghamtonRover.Video.Utils.BytesToInt;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;

public class Client {
    private String host;
    private int port;
    private DatagramSocket socket;
    private static int PACKET_LIMIT = 65507;

    /**
     * Client value constructor
     * @param h the host
     * @param p the port number
     */
    private Client(String h, int p){
        host = h;
        port = p;
        try {
            //Connect the client socket to the server address and port
            //The client can only receive and send packet to server address
            socket = new DatagramSocket(8080);
            socket.connect(InetAddress.getByName(host), port);
            System.out.println("Connecting to Server at IP: " + host + ", port: " + port);


        }
        catch(UnknownHostException e) {
            e.printStackTrace();
        }
        catch(SocketException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method is meant to connect a client to the Server.
     * @return clientSocket the socket corresponding to the Client's connection to the Server
     */
//    private Socket connectClient(){
////        Socket clientSocket = null;
//        try {
//            //this call automatically calls connect for us.
//            //so we do not need to call connect on it.
//            clientSocket = new Socket(host, port);
//        }
//        catch(ConnectException e){
//            System.out.println("Could not establish a socket for streaming feed. Exiting.");
//            System.exit(-1);
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//        return clientSocket;
//
//    }

    /**
     * This method displays a canvasFrame for the client, and tries to display each frame
     * sent by the server in real time. We are still working out how to serialize the
     * frames for successful transfer. The original idea of converting a frame to an image sort of worked,
     * but the byte array option will probably prove more efficient and better for playing with the frame data later on if needed.
     * @param clientSocket The socket corresponding to the client's connection with the Server
     */
    private static void displayFeed(DatagramSocket clientSocket){
        CanvasFrame canvas = new CanvasFrame("Client Webcam feed");
        try {
//            DataInputStream dis = new DataInputStream(new DataInputStream(clientSocket.getInputStream()));
            DatagramPacket initRequest = new DatagramPacket( new byte[] {0x01}, 1);
            clientSocket.send(initRequest);
            ByteArrayInputStream bis;

            while (clientSocket.isConnected()) {
                System.out.println("Socket is connected");
                File file = new File("sentFrame.jpg");
                try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))){

                    //First receive data for the length of the incoming data
                    byte[] buffer = new byte[4];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    System.out.println("waiting for fragLength packet");
                    clientSocket.receive(packet);


                    //Convert data and get the length of the incoming data
                    int[] fraglength = BytesToInt(packet.getData());
                    System.out.println("Expects " + fraglength[0] + " packages.");

//                    buffer = new byte[PACKET_LIMIT];
//                    data.setData(buffer);

                    //for every fragment length, crete a new datagram packet
                    //receive the data from the server, put it into the packet
                    //read the data from the packet using DataInputStream
                    //and write it to the file
                    for(int i = 0; i < fraglength[0]; i++) {
                        packet = new DatagramPacket(new byte[PACKET_LIMIT], PACKET_LIMIT);
                        clientSocket.receive(packet);
                        bis = new ByteArrayInputStream(packet.getData());
                        if(bis.available() <= 0) continue;

                        while( (bis.available() > 0))
                            os.write(bis.read());

                    }
                    os.flush();
                    os.close();
                } catch (EOFException e) {
                    e.printStackTrace();
                } catch(SocketException e){
                    e.printStackTrace();
                    System.out.println("Lost connection to server. Exiting now.");
                    System.exit(-1);
                }catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Lost connection to server. Exiting now.");
                    System.exit(-1);
                }
                opencv_core.IplImage img = cvLoadImage("sentFrame.jpg");
                OpenCVFrameConverter imgToFrame = new OpenCVFrameConverter.ToIplImage();
                canvas.showImage(imgToFrame.convert(img));
           }
        }
//        catch(IOException e){
//            e.printStackTrace();
//            System.out.println("Lost connection to server. Exiting now.");
//            System.exit(-1);
//        }
        catch(IOException e){
            e.printStackTrace();
        }
        catch(NullPointerException e){
            e.printStackTrace();
        }


    }

    public DatagramSocket getSocket(){
        return this.socket;
    }

    /**
     * This method simply scans for the server's host and port number, and then
     * the client creates a socket to try and establish a connect with the server.
     * This needs to be changed from TCP to UDP protocol, so that it's connectionless.
     * @param args command line arguments that are included when the program is run at the command line
     */
    public static void main(String[] args) {
        Scanner reader = new Scanner(System.in);
        System.out.println("Enter the server's host address: ");
        String host = reader.next();
        System.out.println("Enter the server's port: ");
        int port = reader.nextInt();
        Client client = new Client(host, port);
//        Socket clientSocket = client.connectClient();
        Client.displayFeed(client.getSocket());

    }
}
