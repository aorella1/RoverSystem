package com.github.zeldazach.binghamtonrover.networking;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

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

    private static final int CURRENT_VERSION = 3; // The current supported version.
    private InetAddress address;
    private int port;
    private int resendCount;
    private ServerReceiver receiver;
    private boolean started = false;
    private boolean closed = false;
    private char sendTimestamp = 0;
    private char receiveTimestamp = 0;
    private DatagramSocket socket;

    private Map<Integer, PacketHandler> handlers = new HashMap<>();
    private Map<Integer, Class<? extends Packet>> packetsByType = new HashMap<Integer, Class<? extends Packet>>() {{
        put(1, PacketControl.class);
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
        address = Inet4Address.getByName(_address);
        port = _port;
        resendCount = _resendCount;
        socket = new DatagramSocket(this.port, this.address);
        receiver = new ServerReceiver(this);
    }

    public void startServer() throws AlreadyStarted, SocketException
    {
        if (started)
        {
            throw new AlreadyStarted();
        }

        if (!socket.isBound())
        {
            socket.bind(new InetSocketAddress(address, port));
        }

        receiver.start();
        started = true;
        closed = false;
    }

    public void closeServer() throws AlreadyClosed, InterruptedException
    {
        if (closed)
        {
            throw new AlreadyClosed();
        }

        socket.close();
        receiver.join();
        started = false;
        closed = true;
    }

    public void setHandler(int packetType, PacketHandler handler)
    {
        handlers.put(packetType, handler);
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

        Packet packet = instantiatePacket(header);
        if (packet != null)
        {
            packet.readFromBuffer(buff);
        }

        // Now we need to get the handler for the packet type.
        getHandler(header.getType()).handle(packet);
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

    public InetAddress getAddress()
    {
        return address;
    }

    public int getPort()
    {
        return port;
    }

    public synchronized void sendPacket(Packet p, String roverAddress, int roverPort) throws IOException
    {
        InetAddress addr = InetAddress.getByName(roverAddress);

        for (int i = 0; i < resendCount; i++)
        {
            // We fill the packet header here, since the user of this API shouldn't have to touch it.
            ByteBuffer buff = packetToBuffer(p);

            DatagramPacket dp = new DatagramPacket(buff.array(), buff.limit(), addr, roverPort);
            socket.send(dp);
        }
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

    synchronized void incrementReceiveTimestamp()
    {
        receiveTimestamp++;
    }

    synchronized int getLastReceiveTimestamp()
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