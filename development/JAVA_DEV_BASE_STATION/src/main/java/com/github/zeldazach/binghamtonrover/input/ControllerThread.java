package com.github.zeldazach.binghamtonrover.input;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.Pipe;

public class ControllerThread extends Thread {

    protected Pipe pipe;
    private FileChannel channel;

    public ControllerThread(FileChannel channel) throws IOException {
        super("ControllerThread");

        pipe = Pipe.open();
        pipe.source().configureBlocking(false);

        this.channel = channel;

        this.setDaemon(true);
    }

    @Override
    public void run() {
        try {
            ByteBuffer buffer = ByteBuffer.allocateDirect(8 * 5);
            while (this.isAlive()) {
                channel.read(buffer);
                buffer.flip();
                pipe.sink().write(buffer);
                buffer.flip();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
