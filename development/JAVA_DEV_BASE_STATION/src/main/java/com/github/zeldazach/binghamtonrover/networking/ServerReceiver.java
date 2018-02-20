package com.github.zeldazach.binghamtonrover.networking;

import java.io.IOException;
import java.net.DatagramPacket;

public class ServerReceiver extends ServerThread
{
    ServerReceiver(Manager m)
    {
        super("UDP Receiver", m);

        // So the program actually stops...
        setDaemon(true);
    }

    @Override
    public void run()
    {
        DatagramPacket datagramPacket;

        while (!serverManager.isClosed())
        {
            datagramPacket = Packet.makeReceivingPacket();

            try
            {
                    serverManager.getSocket().receive(datagramPacket);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                continue;
            }


            serverManager.handlePacket(datagramPacket);
        }
    }
}