package com.github.zeldazach.binghamtonrover.input;

import com.github.zeldazach.binghamtonrover.utils.Unsigned;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    /**
     * The size of each controller event.
     */
    private static final int EVENT_SIZE = 8;

    /**
     * The default size of the event input buffer.
     */
    private static final int DEFAULT_BUFFER_SIZE = EVENT_SIZE * 5;

    private String name;
    private String devPath;

    private ControllerThread thread;
    private ByteBuffer buffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);

    Controller(String n, String dp) {
        name = n;
        devPath = dp;
    }

    public void open() throws IOException {
        if (thread != null) {
            throw new IllegalStateException("Controller already opened!");
        }

        thread = new ControllerThread(FileChannel.open(Paths.get(devPath)));
        thread.start();
    }

    public void close() {
        thread.interrupt();
    }

    public String getName() {
        return name;
    }

    public List<ControllerEvent> poll() throws IOException {
        if (thread == null) {
            throw new IllegalStateException("The controller must be opened before polling!");
        }

        List<ControllerEvent> events = new ArrayList<>();

        while (true) {
            int bread = thread.pipe.source().read(buffer);
            if (bread == 0) break;

            buffer.order(ByteOrder.nativeOrder());
            buffer.flip();

            for (int j = 0; j < bread / EVENT_SIZE; j++) {
                int timestampUnsigned = buffer.getInt();
                short value = buffer.getShort();
                byte typeUnsigned = buffer.get();
                byte numberUnsigned = buffer.get();

                ControllerEvent event = new ControllerEvent(this, Unsigned.value(timestampUnsigned),
                        value, Unsigned.value(typeUnsigned), Unsigned.value(numberUnsigned));

                events.add(event);
            }

            buffer.flip();
        }

        return events;
    }

}
