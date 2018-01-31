package com.github.zeldazach.binghamtonrover.networking;

import java.nio.ByteBuffer;

public class PacketVideo extends Packet {
    byte[] imageData;

    PacketVideo() { super((byte) 2, 1); }

    @Override
    public void writeToBuffer(ByteBuffer buff) { buff.put(ByteBuffer.wrap(imageData)); }

    @Override
    public void readFromBuffer(ByteBuffer buff) { imageData = buff.array(); }
}