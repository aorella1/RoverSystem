package com.github.zeldazach.binghamtonrover.networking;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class NetworkThread extends Thread
{
    /**
     * Size of the buffer to read incoming packets.
     * It needs to be big enough to fit any single packet, and since the max size of a packet is the max size
     * of a UDP packet (which is < 65535), this value was chosen.
     */
    private static final int READ_BUFFER_SIZE = 65535;

    private DatagramChannel datagramChannel;

    private ByteBuffer packetReadBuffer = ByteBuffer.allocateDirect(READ_BUFFER_SIZE);

    protected NetworkThread(DatagramChannel dc)
    {
        datagramChannel = dc;

        setDaemon(true);
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                packetReadBuffer.clear();
                datagramChannel.receive(packetReadBuffer);

                NetworkManager.getInstance().processPacket(packetReadBuffer);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}