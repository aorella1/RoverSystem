package com.github.zeldazach.binghamtonrover.networking;

import com.github.zeldazach.binghamtonrover.gui.DisplayApplication;
import javafx.application.Platform;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

public class PacketCameraHandler implements PacketHandler
{
    private static class FrameBuffer
    {
        // The maximum size of a single frame.
        private static final int FRAME_MAX_SIZE = 40000000;

        int frameID, remainingSections, bufferSize;
        // TODO: See if we can replace all allocate calls with allocateDirect. Does it have a speedup?
        ByteBuffer internalBuffer = ByteBuffer.allocate(FRAME_MAX_SIZE);

        FrameBuffer()
        {
            frameID = remainingSections = bufferSize = 0;
        }

        void push()
        {
            // Allocate a temporary array for the buffer.
            byte[] frame_buffer = new byte[bufferSize];

            // Prepare the internal buffer for reading.
            internalBuffer.position(0);

            // Copy only the frame bytes out of our buffer.
            internalBuffer.get(frame_buffer, 0, bufferSize);

            Image image = new Image(new ByteArrayInputStream(frame_buffer));
            Platform.runLater(() ->
            {
                if (DisplayApplication.INSTANCE != null)
                {
                    DisplayApplication.INSTANCE.getCameraImageView().setImage(image);
                }
            });
        }
    }

    private static class FrameBufferContainer
    {
        private int total_frames_received = 0;
        private int total_frames_dropped = 0;
        private int next_buffer;
        private FrameBuffer[] buffers;

        private int maxSize;

        FrameBufferContainer(int maxSize)
        {
            this.maxSize = maxSize;
            next_buffer = 0;
            buffers = new FrameBuffer[maxSize];

            for (int i = 0; i < maxSize; i++)
            {
                buffers[i] = new FrameBuffer();
            }
        }

        void update_buffer(PacketCamera packet)
        {
            int frameID = packet.getFrameID();
            int sectionCount = packet.getSectionCount();
            int sectionID = packet.getSectionIndex();
            ByteBuffer frameData = packet.getSectionData();
            int frameDataSize = packet.getSectionSize();

            // Search for a buffer that already has that timestamp.
            int foundBufferIdx = -1;
            for (int i = 0; i < maxSize; i++)
            {
                if (buffers[i].frameID == frameID)
                {
                    // Found it!
                    foundBufferIdx = i;
                    break;
                }
            }

            if (foundBufferIdx == -1)
            {
                // We did not find a buffer... it is a new frame!
                total_frames_received++;

                FrameBuffer ourBuffer = buffers[next_buffer];

                if (ourBuffer.frameID != 0)
                {
                    // It is not a new buffer... it already has a frame in it.

                    if (ourBuffer.remainingSections == 0)
                    {
                    } else
                    {
                        System.out.println("> Dropped frame with id " + ourBuffer.frameID + ", dropped perc " + total_frames_dropped / (double) total_frames_received);
                        total_frames_dropped++;
                    }
                }

                ourBuffer.frameID = frameID;
                ourBuffer.remainingSections = sectionCount - 1;

                // Copy the bytes into the proper location within our frame buffer.
                ourBuffer.internalBuffer.position(frameID * PacketCamera.MAX_FRAME_DATA_SIZE);
                ourBuffer.internalBuffer.put(frameData);

                ourBuffer.bufferSize = frameDataSize;

                if (ourBuffer.remainingSections == 0)
                {
                    ourBuffer.push();
                }

                // Reset our next_buffer.
                next_buffer = (next_buffer + 1) % maxSize;
            } else
            {
                // We found our buffer, let's update it!

                FrameBuffer ourBuffer = buffers[foundBufferIdx];

                ourBuffer.remainingSections--;

                // Copy the bytes into the proper location within our frame buffer.
                ourBuffer.internalBuffer.position(sectionID * PacketCamera.MAX_FRAME_DATA_SIZE);
                ourBuffer.internalBuffer.put(frameData);

                ourBuffer.bufferSize += frameDataSize;

                if (ourBuffer.remainingSections == 0)
                {
                    ourBuffer.push();
                }
            }
        }
    }

    private FrameBufferContainer frameBufferContainer = new FrameBufferContainer(10);

    @Override
    public void handle(Packet packet)
    {
        try
        {
            frameBufferContainer.update_buffer((PacketCamera) packet);
        } catch (IllegalArgumentException e)
        {
            System.out.println("Unable to add section to potential frame " + e.getMessage());
        }
    }
}