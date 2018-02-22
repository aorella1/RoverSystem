package com.github.zeldazach.binghamtonrover.networking;

import com.github.zeldazach.binghamtonrover.BaseStation;

import java.io.IOException;

public class PacketHeartbeatHandler implements PacketHandler {
    @Override
    public void handle(Packet packet) throws PacketHandlerException {
        PacketHeartbeat castPacket = (PacketHeartbeat) packet;

        Manager manager = packet.getManager().orElseThrow(
                () -> new PacketHandlerException("No manager provided with packet!"));

        manager.getHeartbeat().last_heartbeat_receive = System.currentTimeMillis();

        if (manager.getState() == ConnectionState.DISCONNECTED ||
                manager.getState() == ConnectionState.UNINITIALIZED ||
                manager.getState() == ConnectionState.TROUBLED)
        {
            System.out.println("> Connected to rover.");

            manager.getHeartbeat().lock.lock();
            manager.setState(ConnectionState.CONNECTED);
            manager.getHeartbeat().lock.unlock();
        }
    }
}