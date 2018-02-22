package com.github.zeldazach.binghamtonrover.networking;

import com.github.zeldazach.binghamtonrover.BaseStation;
import com.github.zeldazach.binghamtonrover.gui.DisplayApplication;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.sql.Connection;
import java.util.*;

public class Manager
{
    static class ManagerException extends Exception
    {
        ManagerException(String message)
        {
            super(message);
        }
    }

    public static class AlreadyStarted extends ManagerException
    {
        AlreadyStarted()
        {
            super("Manager already started");
        }
    }

    public static class AlreadyClosed extends ManagerException
    {
        AlreadyClosed()
        {
            super("Manager already closed");
        }
    }

    private static Manager INSTANCE;

    public static Manager getInstance() {
        return INSTANCE;
    }

    private static final int CURRENT_VERSION = 5; // The current supported version.

    private static final String ROVER_ADDRESS = "192.168.1.100";
    private static final int ROVER_PORT = 44444;

    private ConnectionState state;
    private InetAddress address;
    private int port;
    private int resendCount;
    private ServerReceiver receiver;
    private HeartbeatThread heartbeat;
    private boolean started = false;
    private boolean closed = false;
    private char sendTimestamp = 0;
    private char receiveTimestamp = 0;
    private DatagramSocket socket;

    private Map<Integer, PacketHandler> handlers = new HashMap<Integer, PacketHandler>() {{
        put(null, new PacketUnknownHandler());
        put(0, new PacketHeartbeatHandler());
        put(2, new PacketCameraHandler());
    }};
    private Map<Integer, Class<? extends Packet>> packetsByType = new HashMap<Integer, Class<? extends Packet>>() {{
        put(0, PacketHeartbeat.class);
        put(1, PacketControl.class);
        put(2, PacketCamera.class);
    }};

    /**
     * Construct a Manager for sending and receiving packets
     * @param _address the address to bind to
     * @param _port the port to bind on
     * @param _resendCount how many times each packet should be sent
     * @throws SocketException when the socket can't be bound
     * @throws UnknownHostException when the address is invalid
     */
    public Manager(String _address, int _port, int _resendCount) throws SocketException, UnknownHostException
    {
        state = ConnectionState.UNINITIALIZED;
        address = Inet4Address.getByName(_address);
        port = _port;
        resendCount = _resendCount;
        socket = new DatagramSocket(this.port, this.address);
        receiver = new ServerReceiver(this);
        heartbeat = new HeartbeatThread(this);

        INSTANCE = this;
    }

    public void startServer() throws AlreadyStarted, IOException
    {
        if (started)
        {
            throw new AlreadyStarted();
        }

        if (!socket.isBound())
        {
            socket.bind(new InetSocketAddress(address, port));
        }

        started = true;
        closed = false;

        receiver.start();
        heartbeat.start();
    }

    private void closeServer() throws AlreadyClosed, InterruptedException
    {
        if (closed)
        {
            throw new AlreadyClosed();
        }

        socket.close();
        started = false;
        closed = true;

        receiver.join();
        heartbeat.join();
    }

    public boolean isClosed() {
        return closed;
    }

    private PacketHandler getHandler(int type)
    {
        return handlers.get(type);
    }

    /**
     * This is called whenever a datagram packet is received.
     * @param dp The packet that was just received.
     */
    protected void handlePacket(DatagramPacket dp)
    {
        ByteBuffer buff = ByteBuffer.wrap(dp.getData());

        PacketHeader header = new PacketHeader();
        header.readFromBuffer(buff);
        int timestamp = header.getTimestamp();
        int version = header.getVersion();
        int expectedTimestamp = getLastReceiveTimestamp();
        Packet packet = instantiatePacket(header);
        if (version != CURRENT_VERSION) {
            outputVersionMismatch(version);
            return;
        } else if (timestamp <= expectedTimestamp) {
            // Only output a timestamp mismatch if it is not a camera packet
            // and the timestamp is not zero (which is special).
            if (!(packet instanceof PacketCamera) && timestamp != 0) {
                outputTimestampMismatch(timestamp, expectedTimestamp);
                return;
            }
        }

        setReceiveTimestamp(header.getTimestamp());

        if (packet != null)
        {
            packet.setManager(this);
            packet.setHeader(header);
            packet.readFromBuffer(buff);
        }

        // Now we need to get the handler for the packet type.
        try {
            getHandler(header.getType()).handle(packet);
        } catch (PacketHandler.PacketHandlerException e) {
            e.printStackTrace();
        }
    }

    private Packet instantiatePacket(PacketHeader packetHeader)
    {
        // We assume the class has a default constructor.
        try
        {
            return packetsByType.get(packetHeader.getType()).getConstructor().newInstance();
        }
        catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public DatagramSocket getSocket()
    {
        return socket;
    }

    public ConnectionState getState()
    {
        return state;
    }

    public void setState(ConnectionState s)
    {
        state = s;

        if (DisplayApplication.INSTANCE != null) {
            DisplayApplication.INSTANCE.updateConnection(s);
        }
    }

    public HeartbeatThread getHeartbeat()
    {
        return heartbeat;
    }

    public synchronized void sendPacket(Packet p) throws IOException
    {
        InetAddress addr = InetAddress.getByName(ROVER_ADDRESS);

        for (int i = 0; i < resendCount; i++)
        {
            // We fill the packet header here, since the user of this API shouldn't have to touch it.
            ByteBuffer buff = packetToBuffer(p);

            DatagramPacket dp = new DatagramPacket(buff.array(), buff.limit(), addr, ROVER_PORT);
            socket.send(dp);
        }
    }

    public synchronized void sendPacketNoRepeat(Packet p) throws IOException
    {
        InetAddress addr = InetAddress.getByName(ROVER_ADDRESS);


        // We fill the packet header here, since the user of this API shouldn't have to touch it.
        ByteBuffer buff = packetToBuffer(p);

        DatagramPacket dp = new DatagramPacket(buff.array(), buff.limit(), addr, ROVER_PORT);
        socket.send(dp);

    }

    /**
     * Creates a buffer with the contents of the given packet, and readies it for sending.
     * This will flip the buffer so that it is ready to be read.
     * @param p The packet to serialize.
     * @return The buffer to write to the line, ready-to-go!
     */
    private ByteBuffer packetToBuffer(Packet p)
    {
        ByteBuffer buff = ByteBuffer.allocate(PacketHeader.SIZE + p.getSize());

        PacketHeader header = new PacketHeader((char) CURRENT_VERSION, p.getType(), sendTimestamp);
        sendTimestamp++;

        header.writeToBuffer(buff);
        p.writeToBuffer(buff);

        buff.flip();

        return buff;
    }

    private synchronized void setReceiveTimestamp(int timestamp)
    {
        receiveTimestamp = (char) timestamp;
    }

    private synchronized int getLastReceiveTimestamp()
    {
        return receiveTimestamp;
    }

    private void outputVersionMismatch(int version)
    {
        System.out.println(
                "Packet Received with incorrect version. Expected " + Manager.CURRENT_VERSION + " but got " + version);
    }

    private void outputTimestampMismatch(int actual, int expected)
    {
        System.out.println(
                "Packet Received with outdated timestamp. Expected at least" + expected + " but got " + actual);
    }
}
