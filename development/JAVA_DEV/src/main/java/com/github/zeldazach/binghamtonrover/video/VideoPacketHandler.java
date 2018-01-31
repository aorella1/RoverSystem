package com.github.zeldazach.binghamtonrover.video;

import com.github.zeldazach.binghamtonrover.networking.Packet;
import com.github.zeldazach.binghamtonrover.networking.PacketHandler;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class VideoPacketHandler implements PacketHandler {
    @Override
    public void handle(Packet packet) {
        ByteBuffer buff = ByteBuffer.allocate(packet.getSize());
        packet.writeToBuffer(buff);
        System.out.println(buff.array());
    }
}