package com.github.zeldazach.binghamtonrover.networking;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PacketControl extends Packet
{
    public enum MovementDirection
    {
        STOP,
        FORWARD,
        LEFT,
        RIGHT,
        BACKWARD
    }

    private float lStick;
    private float rStick;

    PacketControl(float lstick, float rstick)
    {
        super((byte) 1, 8);
        lStick = lstick;
        rStick = rstick;
    }

    @Override
    public void writeToBuffer(ByteBuffer buff)
    {
        System.out.println("" + lStick + " " + rStick);
        buff.order(ByteOrder.LITTLE_ENDIAN).putFloat(lStick);
        buff.order(ByteOrder.LITTLE_ENDIAN).putFloat(rStick);
    }

    /*
    TODO: THe point of this method is to make an appropriate packet object from
    a ByteBuffer... but since we won't ever do this for this packet type it's
    unnecessary, maybe we should throw an exception instead?
    */
    @Override
    public void readFromBuffer(ByteBuffer buff) {
        lStick = 0;
        rStick = 0;
    }
}