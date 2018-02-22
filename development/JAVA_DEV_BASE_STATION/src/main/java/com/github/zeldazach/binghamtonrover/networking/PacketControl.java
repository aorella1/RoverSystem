package com.github.zeldazach.binghamtonrover.networking;

import java.nio.ByteBuffer;

public class PacketControl extends Packet
{

    private MovementDirection direction;

    private int cameraSelected;

    PacketControl(RoverState redRover)
    {
        super((byte) 1, 1);
        direction = redRover.getDirection();
        cameraSelected = redRover.getCamera();
    }

    @Override
    public void writeToBuffer(ByteBuffer buff)
    {
        buff.put((byte) direction.ordinal());
        // Add the camera field in here
    }

    /*
    TODO: THe point of this method is to make an appropriate packet object from
    a ByteBuffer... but since we won't ever do this for this packet type it's
    unnecessary, maybe we should throw an exception instead?
    */
    @Override
    public void readFromBuffer(ByteBuffer buff)
    {
        // Add the camera field in here
        direction = MovementDirection.values()[buff.get()];
    }
}