package com.github.zeldazach.binghamtonrover.networking;

import com.github.zeldazach.binghamtonrover.BaseStation;

import java.io.IOException;

interface PacketHandler
{
    class PacketHandlerException extends Exception
    {
        PacketHandlerException(String message)
        {
            super(message);
        }
    }
    void handle(Packet packet) throws PacketHandlerException;
}

class PacketUnknownHandler implements PacketHandler {
    @Override
    public void handle(Packet packet) throws PacketHandlerException {
        if (packet != null) {
            throw new PacketHandlerException("Non-null mapped to PacketUnknownHandler");
        }
        System.err.println("Received Packet of non-mapped type!");
    }
}

class PacketHearbeatHandler implements PacketHandler {
    @Override
    public void handle(Packet packet) throws PacketHandlerException {
        PacketHeartbeat castPacket = (PacketHeartbeat) packet;
        PacketHeartbeat.Direction packetDirection = castPacket.getDirection();
        Manager manager = castPacket.getManager().orElseThrow(
                () -> new PacketHandlerException("Supplied packet is not a received packet"));
        if (PacketHeartbeat.Direction.PING == packetDirection) {
            try {
                manager.sendPacket(
                        new PacketHeartbeat(PacketHeartbeat.Direction.PONG),
                        BaseStation.roverAddress,
                        BaseStation.roverPort);
            } catch (IOException e) {
                System.err.println("Unable to respond to rover ping " + e.getMessage());
            }
        } else if (PacketHeartbeat.Direction.PONG == packetDirection) {
            manager.onHeartBeatReceive();
        } else {
            throw new PacketHandlerException("PacketHeartbeat with unknown Direction received");
        }
    }
}
