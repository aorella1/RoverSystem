package com.github.zeldazach.binghamtonrover.networking;

import java.nio.ByteBuffer;

// This is tad strange naming convention, so I'm proposing a change to
// this new name
public class PacketHeartbeat extends Packet
{
    public enum Direction
    {
        PING,
        PONG
    }

    private Direction direction;

    public PacketHeartbeat()
    {

    }

    public PacketHeartbeat(Direction d)
    {
        direction = d;
    }

    @Override
    public void writeToBuffer(ByteBuffer buff)
    {
        buff.put((byte) direction.ordinal());
    }

    // See other comment about readFromBuffer
    @Override
    public void readFromBuffer(ByteBuffer buff)
    {
        direction = Direction.values()[buff.get()];
    }

    public Direction getDirection()
    {
        return direction;
    }
}
