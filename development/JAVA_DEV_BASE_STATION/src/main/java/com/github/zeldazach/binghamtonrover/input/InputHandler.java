package com.github.zeldazach.binghamtonrover.input;

import com.github.zeldazach.binghamtonrover.networking.PacketInput;

public class InputHandler
{
    /**
     * Minimum interval between two consecutive input packets, in milliseconds.
     */
    private static final int INPUT_PACKET_INTERVAL = 100;

    private static InputHandler INSTANCE = null;

    public static InputHandler getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new InputHandler();
        }

        return INSTANCE;
    }

    private InputHandler()
    {
    }

    /**
     * Last time an input packet was sent.
     */
    private long lastInputPacketSend = 0;

    /**
     * Polls input devices. Is called from the main loop.
     */
    public void poll()
    {
        if (System.currentTimeMillis() - lastInputPacketSend > INPUT_PACKET_INTERVAL) {
            lastInputPacketSend = System.currentTimeMillis();

            PacketInput packet = new PacketInput();


        }
    }
}
