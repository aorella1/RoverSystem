package com.github.zeldazach.binghamtonrover.networking;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Optional;


public abstract class Packet
{
    /**
     * This is called when the packet must be written to the network.
     * Children must implement this. Assume that the buffer has
     * enough space to hold the entire packet.
     * @param buff The buffer in which the packet must be written.
     */
    public abstract void writeToBuffer(ByteBuffer buff);
    public abstract void readFromBuffer(ByteBuffer buff);
}