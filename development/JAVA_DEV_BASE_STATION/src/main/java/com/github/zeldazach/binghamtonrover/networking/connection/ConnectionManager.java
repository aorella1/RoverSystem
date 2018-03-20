package com.github.zeldazach.binghamtonrover.networking.connection;

import com.github.zeldazach.binghamtonrover.networking.NetworkManager;
import com.github.zeldazach.binghamtonrover.networking.PacketHeartbeat;

import java.io.IOException;

public class ConnectionManager
{

    /**
     * Heartbeat send delay when in CONNECTED state, in milliseconds.
     */
    private static final int HEARTBEAT_CONNECTED_DELAY = 1000;

    /**
     * Heartbeat send delay when in TROUBLED state, in milliseconds.
     */
    private static final int HEARTBEAT_TROUBLED_DELAY = 250;

    /**
     * The time, in milliseconds, to wait since last receiving a heartbeat before switching to TROUBLED.
     */
    private static final int CONNECTED_TIMEOUT = 3000;

    /**
     * The time, in milliseconds, to wait since last receiving a heartbeat before switching to DISCONNECTED.
     */
    private static final int TROUBLED_TIMEOUT = 6000;

    /**
     * The current connection state.
     */
    private ConnectionState state;

    /**
     * The rover's IP address.
     * This is hardcoded.
     */
    private final String roverAddress = "149.125.83.106";

    /**
     * The rover's port.
     * For now, this is hardcoded.
     */
    private final int roverPort = 44444;

    /**
     * The UNIX millisecond time at which the last heartbeat packet was received.
     */
    private long lastHeartbeatReceiveTime;

    private long lastHeartbeatSendTime;

    public ConnectionManager()
    {
        state = ConnectionState.UNINITIALIZED;
    }

    public ConnectionState getState()
    {
        return state;
    }

    public String getRoverAddress()
    {
        return roverAddress;
    }

    public int getRoverPort()
    {
        return roverPort;
    }

    /**
     * Handles incoming heartbeat packets. This must only be called from the PacketHeartbeatHandler.
     *
     * @param hbp The incoming packet.
     */
    public void handleHeartbeat(PacketHeartbeat hbp)
    {
        // Update the last received time.
        lastHeartbeatReceiveTime = System.currentTimeMillis();

        // Update our state.
        switch (state) {
            case UNINITIALIZED:
            case DISCONNECTED:
            case TROUBLED:
                state = ConnectionState.CONNECTED;
        }
    }

    /**
     * Handles updating connection state based upon heartbeat timestamps. Must only be called from NetworkManager.
     * Returns the length of time, in milliseconds, before another update is necessary.
     */
    public long update() throws IOException
    {
        if (state == ConnectionState.CONNECTED) {
            // First make sure we don't need to switch states.
            if (System.currentTimeMillis() - lastHeartbeatReceiveTime > CONNECTED_TIMEOUT) {
                state = ConnectionState.TROUBLED;
                return ConnectionManager.HEARTBEAT_TROUBLED_DELAY;
            }

            // See if we need to send a heartbeat packet.
            if (System.currentTimeMillis() - lastHeartbeatSendTime > HEARTBEAT_CONNECTED_DELAY) {
                lastHeartbeatSendTime = System.currentTimeMillis();

                // Send a heartbeat, with direction PING.
                PacketHeartbeat hbp = new PacketHeartbeat(PacketHeartbeat.Direction.PING);
                NetworkManager.getInstance().send(hbp);
            }

            return ConnectionManager.HEARTBEAT_CONNECTED_DELAY;
        }

        if (state == ConnectionState.TROUBLED) {
            // Make sure we don't need to switch states.
            if (System.currentTimeMillis() - lastHeartbeatReceiveTime > TROUBLED_TIMEOUT) {
                state = ConnectionState.DISCONNECTED;
                return ConnectionManager.HEARTBEAT_CONNECTED_DELAY;
            }

            // Check if we need to send a heartbeat packet.
            if (System.currentTimeMillis() - lastHeartbeatSendTime > HEARTBEAT_TROUBLED_DELAY) {
                lastHeartbeatSendTime = System.currentTimeMillis();

                // Send a heartbeat.
                PacketHeartbeat hbp = new PacketHeartbeat(PacketHeartbeat.Direction.PING);
                NetworkManager.getInstance().send(hbp);
            }

            return ConnectionManager.HEARTBEAT_TROUBLED_DELAY;
        }

        // State is either UNINITIALIZED or DISCONNECTED. We do the same thing: send heartbeats at connected delay.
        if (System.currentTimeMillis() - lastHeartbeatSendTime > HEARTBEAT_CONNECTED_DELAY) {
            lastHeartbeatSendTime = System.currentTimeMillis();

            // Send a heartbeat.
            PacketHeartbeat hbp = new PacketHeartbeat(PacketHeartbeat.Direction.PING);
            NetworkManager.getInstance().send(hbp);
        }

        return ConnectionManager.HEARTBEAT_CONNECTED_DELAY;
    }
}
