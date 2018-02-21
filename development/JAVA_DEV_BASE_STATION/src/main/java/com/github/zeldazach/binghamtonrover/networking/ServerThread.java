package com.github.zeldazach.binghamtonrover.networking;

public abstract class ServerThread extends Thread
{
    Manager serverManager;

    ServerThread(String name, Manager m)
    {
        super(name);
        serverManager = m;

        this.setDaemon(true);
    }
}