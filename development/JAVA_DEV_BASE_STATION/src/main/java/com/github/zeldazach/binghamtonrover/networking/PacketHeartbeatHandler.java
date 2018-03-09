package com.github.zeldazach.binghamtonrover.networking;

public class PacketHeartbeatHandler implements PacketHandler
{

    @Override
    public void handle(Packet packet)
    {
        // Simply pass the packet along to the connection manager.
        PacketHeartbeat phb = (PacketHeartbeat) packet;
        NetworkManager.getInstance().getConnectionManager().handleHeartbeat(phb);
    }
}
