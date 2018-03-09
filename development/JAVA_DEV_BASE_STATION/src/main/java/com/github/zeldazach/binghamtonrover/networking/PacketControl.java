package com.github.zeldazach.binghamtonrover.networking;

import java.nio.ByteBuffer;

public class PacketControl extends Packet
{
    private byte selectedCameraUnsigned;

    public PacketControl(int selectedCamera)
    {
        selectedCameraUnsigned = (byte) selectedCamera;
    }

    public PacketControl()
    {

    }

    @Override
    public void writeToBuffer(ByteBuffer buff)
    {
        buff.put(selectedCameraUnsigned);
    }


    @Override
    public void readFromBuffer(ByteBuffer buff)
    {
        throw new IllegalStateException("Control packets are only base station -> rover!");
    }
}