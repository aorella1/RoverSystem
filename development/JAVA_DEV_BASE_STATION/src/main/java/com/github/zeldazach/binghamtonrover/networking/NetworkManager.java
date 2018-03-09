package com.github.zeldazach.binghamtonrover.networking;

import com.github.zeldazach.binghamtonrover.networking.connection.ConnectionManager;
import com.github.zeldazach.binghamtonrover.networking.connection.ConnectionState;
import com.github.zeldazach.binghamtonrover.utils.Unsigned;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;

public class NetworkManager
{

    /**
     * Size of the buffer to read incoming packets.
     * It needs to be big enough to fit any single packet, and since the max size of a packet is the max size
     * of a UDP packet (which is < 65535), this value was chosen.
     */
    private static final int READ_BUFFER_SIZE = 65535;

    private static final int PROTOCOL_VERSION = 7;

    private static final int HEADER_SIZE = 5;

    private static NetworkManager INSTANCE = null;

    public static NetworkManager getInstance()
    {
        if (INSTANCE != null)
        {
            INSTANCE = new NetworkManager();
        }

        return INSTANCE;
    }

    private NetworkManager()
    {

    }

    private DatagramChannel datagramChannel = null;

    private ConnectionManager connectionManager = null;

    public ConnectionManager getConnectionManager()
    {
        return connectionManager;
    }

    private ByteBuffer packetReadBuffer = ByteBuffer.allocateDirect(READ_BUFFER_SIZE);

    /**
     * An internal representation of the type of a packet.
     * This is used for maximum size calculations as well as storing handler information.
     */
    private static class PacketTypeInfo
    {
        private int id;
        private PacketHandler handler;
        private int maxSize;
        private Class<? extends Packet> packetClass;

        private PacketTypeInfo(int i, PacketHandler h, int ms, Class<? extends Packet> pc)
        {
            id = i;
            handler = h;
            maxSize = ms;
            packetClass = pc;
        }
    }

    private HashMap<Integer, PacketTypeInfo> packetTypeMap = new HashMap<>();
    private HashMap<Class<? extends Packet>, PacketTypeInfo> reversePacketTypeMap = new HashMap<>();

    /**
     * Registers a new packet type and associated handler.
     * Must be called before start().
     *
     * @param typeID        The numeric id of the packet type.
     * @param packetClass   The class which represents an instance of this packet type.
     * @param packetHandler The handler for this packet type.
     * @param maxSize       The maximum size of a packet of this type.
     */
    public <T extends Packet> void registerPacketType(int typeID, Class<T> packetClass, PacketHandler packetHandler,
                                                      int maxSize)
    {
        if (datagramChannel != null)
        {
            throw new IllegalStateException("Must register packets before starting manager!");
        }

        // Avoid registry if another packet already exists with this key.
        if (packetTypeMap.containsKey(typeID))
        {
            throw new IllegalArgumentException("Packet type ID was already registered!");
        }

        // Register the given packet type.
        PacketTypeInfo typeInfo = new PacketTypeInfo(typeID, packetHandler, maxSize, packetClass);
        packetTypeMap.put(typeID, typeInfo);

        // Also register the reverse.
        reversePacketTypeMap.put(packetClass, typeInfo);
    }

    /**
     * Starts the networking. Puts the base station in the uninitialized connection state and starts pinging the rover.
     */
    public void start() throws IOException
    {
        // Open the datagram channel.
        datagramChannel = DatagramChannel.open(StandardProtocolFamily.INET);

        // Bind it to 0.0.0.0 at an arbitrary port 34343.
        // TODO: Can we make this work by passing null here?
        datagramChannel.bind(new InetSocketAddress("0.0.0.0", 34343));

        // Set the channel to use non-blocking mode.
        datagramChannel.configureBlocking(false);

        // Set up our connection manager.
        connectionManager = new ConnectionManager();
    }

    /**
     * Proxy to return the current connection state.
     */
    public ConnectionState getConnectionState()
    {
        return connectionManager.getState();
    }

    /**
     * Reads and processes packets until all available packets are read.
     * TODO: Do we want a timeout here? Is it possible to sit indefinitely?
     */
    public void poll() throws IOException
    {
        connectionManager.update();

        // Try to receive packets.
        while (true)
        {
            SocketAddress sender = datagramChannel.receive(packetReadBuffer);
            if (sender == null)
            {
                // No packets are available.
                break;
            }

            // Flip the buffer so we can read from it.
            packetReadBuffer.flip();

            // Read the header values.
            short versionUnsigned = packetReadBuffer.getShort();
            byte packetTypeUnsigned = packetReadBuffer.get();
            short timestampUnsigned = packetReadBuffer.getShort();

            if (PROTOCOL_VERSION != Unsigned.value(versionUnsigned))
            {
                // TODO: Report version mismatch.
            }

            int packetType = Unsigned.value(packetTypeUnsigned);
            if (!packetTypeMap.containsKey(packetType))
            {
                // That packet type doesn't exist!
                // TODO: Report unknown packet.
            }

            // TODO: Check timestamp for packets that are not camera packets.

            // Get information about the packet type.
            PacketTypeInfo typeInfo = packetTypeMap.get(packetType);

            // Get the default constructor.
            Constructor<? extends Packet> typeConstructor;
            try
            {
                typeConstructor = typeInfo.packetClass.getDeclaredConstructor();
            } catch (NoSuchMethodException e)
            {
                throw new IllegalStateException("Packet subtype " + typeInfo.packetClass.getSimpleName() + " must " +
                        "declare an empty constructor!");
            }

            // Make the constructor accessible, even if it is private. Not needed if the packet subtypes are
            // implemented correctly, but is a catch-all.
            typeConstructor.setAccessible(true);

            // Create the new instance.
            Packet packet;
            try
            {
                packet = typeConstructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
            {
                throw new IllegalStateException("Failed to create new instance of " +
                        typeInfo.packetClass.getSimpleName());
            }

            // Fill the packet.
            packet.readFromBuffer(packetReadBuffer);

            // Call the handler.
            typeInfo.handler.handle(packet);
        }
    }

    // Timestamp stuff
    private static final int MAX_TIMESTAMP = 65535;
    private int nextSendTimestamp = 0;

    /**
     * Attempts to send the given packet, regardless of connection state.
     *
     * @param p The packet to send. It must be filled completely.
     */
    public void send(Packet p) throws IOException
    {
        // Make sure we know about this packet type.
        if (!reversePacketTypeMap.containsKey(p.getClass()))
        {
            throw new IllegalArgumentException("Cannot send unrecognized packet class " + p.getClass().getSimpleName());
        }

        // Get the information about the packet type.
        PacketTypeInfo typeInfo = reversePacketTypeMap.get(p.getClass());

        // Only allocate as much as we might need.
        ByteBuffer sendBuffer = ByteBuffer.allocate(HEADER_SIZE + typeInfo.maxSize);

        // Write header information.
        sendBuffer.putShort((short) PROTOCOL_VERSION);
        sendBuffer.put((byte) typeInfo.id);
        sendBuffer.putShort((short) nextSendTimestamp);

        // Manually wrap the send timestamp.
        if (nextSendTimestamp == MAX_TIMESTAMP)
            nextSendTimestamp = 0;
        else
            nextSendTimestamp++;

        // Write the packet to the buffer. Flip for writing to the wire.
        p.writeToBuffer(sendBuffer);
        sendBuffer.flip();

        // Send the packet to the rover.
        datagramChannel.send(sendBuffer, new InetSocketAddress(connectionManager.getRoverAddress(),
                connectionManager.getRoverPort()));
    }
}
