package com.github.zeldazach.binghamtonrover.networking;

import com.github.zeldazach.binghamtonrover.BaseStation;

import java.io.IOException;

public interface PacketHandler
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
