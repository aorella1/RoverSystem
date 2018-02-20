package com.github.zeldazach.binghamtonrover.networking;


import com.github.zeldazach.binghamtonrover.gui.DisplayApplication;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class HeartbeatThread extends ServerThread
{
    private static final int CONNECTION_HEARTBEAT_NORMAL_DELAY = 1000;
    private static final int CONNECTION_HEARTBEAT_URGENT_DELAY = 250;
    private static final int CONNECTION_NORMAL_TIMEOUT = 3000;
    private static final int CONNECTION_URGENT_TIMEOUT = 6000;

    public HeartbeatThread(Manager manager)
    {
        super("Heartbeat", manager);
    }

    // To synchronize the receiver with this thread.
    public ReentrantLock lock = new ReentrantLock();

    public long last_heartbeat_send = 0;
    public long last_heartbeat_receive = 0;


    @Override
    public void run() {
        while (!serverManager.isClosed())
        {
            ConnectionState state;

            lock.lock();
            state = serverManager.getState();
            lock.unlock();

            if (state == ConnectionState.UNINITIALIZED ||
                    state == ConnectionState.DISCONNECTED)
            {
                // We are waiting for response from the rover.

                if (System.currentTimeMillis() - last_heartbeat_send >= CONNECTION_HEARTBEAT_NORMAL_DELAY) {
                    // Send a new heartbeat.
                    System.out.println("> Searching for rover...");

                    PacketHeartbeat heartbeat = new PacketHeartbeat(PacketHeartbeat.Direction.PING);
                    try
                    {
                        serverManager.sendPacketNoRepeat(heartbeat);

                        last_heartbeat_send = System.currentTimeMillis();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            } else if (state == ConnectionState.CONNECTED) {
                // We are actively beating.

                if (System.currentTimeMillis() - last_heartbeat_receive >= CONNECTION_NORMAL_TIMEOUT) {
                    // We have not received a heartbeat in too long.

                    serverManager.setState(ConnectionState.TROUBLED);
                    System.out.println("> Switch to troubled.");
                    continue;
                }

                if (System.currentTimeMillis() - last_heartbeat_send >= CONNECTION_HEARTBEAT_NORMAL_DELAY) {
                    // Send a new heartbeat.

                    PacketHeartbeat heartbeat = new PacketHeartbeat(PacketHeartbeat.Direction.PING);
                    try
                    {
                        serverManager.sendPacketNoRepeat(heartbeat);

                        last_heartbeat_send = System.currentTimeMillis();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            } else if (state == ConnectionState.TROUBLED) {
                // We are beating quickly.

                if (System.currentTimeMillis() - last_heartbeat_receive >= CONNECTION_URGENT_TIMEOUT) {
                    // Disconnected.

                    serverManager.setState(ConnectionState.DISCONNECTED);
                    System.out.println("> Switch to disconnected.");
                    continue;
                }

                if (System.currentTimeMillis() - last_heartbeat_send >= CONNECTION_HEARTBEAT_URGENT_DELAY) {
                    // Send a new heartbeat.

                    PacketHeartbeat heartbeat = new PacketHeartbeat(PacketHeartbeat.Direction.PING);
                    try
                    {
                        serverManager.sendPacketNoRepeat(heartbeat);

                        last_heartbeat_send = System.currentTimeMillis();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
