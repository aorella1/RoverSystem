package com.github.zeldazach.binghamtonrover.input;

import com.github.zeldazach.binghamtonrover.utils.Unsigned;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Controller
{
    private String name;
    private String devPath;

    private ControllerThread thread;

    Controller(String n, String dp)
    {
        name = n;
        devPath = dp;
    }

    public void open() throws IOException
    {
        if (thread != null)
        {
            throw new IllegalStateException("Controller already opened!");
        }

        thread = new ControllerThread(this, FileChannel.open(Paths.get(devPath)));
        thread.start();
    }

    public void close()
    {
        thread.interrupt();
    }

    public String getName()
    {
        return name;
    }
}
