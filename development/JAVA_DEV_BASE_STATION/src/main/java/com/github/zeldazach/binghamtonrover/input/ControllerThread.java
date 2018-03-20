package com.github.zeldazach.binghamtonrover.input;

import com.github.zeldazach.binghamtonrover.networking.InputEventHandler;
import com.github.zeldazach.binghamtonrover.utils.Unsigned;

import javax.xml.bind.annotation.XmlType;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.Pipe;

public class ControllerThread extends Thread
{

    /**
     * The size of each controller event.
     */
    private static final int EVENT_SIZE = 8;

    /**
     * The default size of the event input buffer.
     */
    private static final int DEFAULT_BUFFER_SIZE = EVENT_SIZE * 5;

    private FileChannel channel;
    private Controller controller;

    public ControllerThread(Controller controller, FileChannel channel)
    {
        super("ControllerThread");

        this.channel = channel;
        this.controller = controller;

        this.setDaemon(true);
    }

    @Override
    public void run()
    {
        try
        {
            ByteBuffer buffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
            while (this.isAlive())
            {
                channel.read(buffer);
                buffer.order(ByteOrder.nativeOrder());
                buffer.flip();

                int timestampUnsigned = buffer.getInt();
                short value = buffer.getShort();
                byte typeUnsigned = buffer.get();
                byte numberUnsigned = buffer.get();

                ControllerEvent event = new ControllerEvent(controller, Unsigned.value(timestampUnsigned),
                        value, Unsigned.value(typeUnsigned), Unsigned.value(numberUnsigned));

                InputEventHandler.getInstance().handleControllerEvent(event);

                buffer.flip();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
