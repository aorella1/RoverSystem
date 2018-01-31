package com.github.zeldazach.binghamtonrover.video;

import org.bytedeco.javacv.*;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class DemoVideoFeedThread implements Runnable {

    private OpenCVFrameGrabber feed;
    private DemoWebCamServer server;

    DemoVideoFeedThread(DemoWebCamServer _server) throws FrameGrabber.Exception {
        server = _server;
        feed = OpenCVFrameGrabber.createDefault(0);
    }

    @Override
    public void run() {
        try {
            feed.start();
            Frame frame;

            // create a canvas frame to display the frames coming from the feed
            CanvasFrame cFrame = new CanvasFrame("Server Live Feed", CanvasFrame.getDefaultGamma()/feed.getGamma());

            // while there are frames to grab and while the canvase frame is visible, we update the canvas showing the most current frame grabbed.
            while((frame = feed.grab()) != null) {
                if (cFrame.isVisible()) cFrame.showImage(frame);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(Java2DFrameUtils.toBufferedImage(frame), "bmp", baos );
                byte[] data = {4};
                server.sendPacket(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
