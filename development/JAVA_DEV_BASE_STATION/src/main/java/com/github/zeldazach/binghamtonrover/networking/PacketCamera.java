package com.github.zeldazach.binghamtonrover.networking;

import com.github.zeldazach.binghamtonrover.utils.Unsigned;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PacketCamera extends Packet
{
    // Maximum size of frame data included within a single packet.
    public static final int MAX_FRAME_DATA_SIZE = 40000;

    private short frameIDUnsigned;
    private byte sectionIndexUnsigned;
    private byte sectionCountUnsigned;
    private short sectionSizeUnsigned;

    private ByteBuffer sectionData;

    public PacketCamera()
    {

    }

    @Override
    public void writeToBuffer(ByteBuffer buff)
    {
        throw new IllegalStateException("BASE STATION MUST NOT SEND CAMERA PACKETS!");
    }

    @Override
    public void readFromBuffer(ByteBuffer buff)
    {
        buff.order(ByteOrder.BIG_ENDIAN);

        frameIDUnsigned = buff.getShort();
        sectionIndexUnsigned = buff.get();
        sectionCountUnsigned = buff.get();
        sectionSizeUnsigned = buff.getShort();

        sectionData = ByteBuffer.allocate(Unsigned.value(sectionSizeUnsigned));
        sectionData.put(buff);

        // Ready sectionData for reading.
        sectionData.flip();
    }

    public int getFrameID()
    {
        return Unsigned.value(frameIDUnsigned);
    }

    public int getSectionIndex()
    {
        return Unsigned.value(sectionIndexUnsigned);
    }

    public int getSectionCount()
    {
        return Unsigned.value(sectionCountUnsigned);
    }

    public int getSectionSize()
    {
        return Unsigned.value(sectionSizeUnsigned);
    }

    public ByteBuffer getSectionData()
    {
        return sectionData;
    }
}
