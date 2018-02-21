package com.github.zeldazach.binghamtonrover.networking;

public class PacketUnknownHandler implements PacketHandler {
    @Override
    public void handle(Packet packet) throws PacketHandlerException {
        if (packet != null) {
            throw new PacketHandlerException("Non-null mapped to PacketUnknownHandler");
        }
        System.err.println("Received Packet of non-mapped type!");
    }
}
