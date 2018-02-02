package com.github.zeldazach.binghamtonrover.video;

import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class DemoVideoFeedThread implements Runnable {

    private OpenCVFrameGrabber feed;
    private DemoWebCamServer server;
    private int timeStamp = 0;

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

            // while there are frames to grab and while the canvas frame is visible, we update the canvas showing the most current frame grabbed.
            while((frame = feed.grab()) != null) {



                //testing for separating image
                BufferedImage bufImg = Java2DFrameUtils.toBufferedImage(frame);
                if (cFrame.isVisible()) cFrame.showImage(Java2DFrameUtils.toFrame(bufImg));

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                if(timeStamp == 65535) timeStamp = 0;
                baos.write(timeStamp);

                ByteArrayOutputStream baos3;
                //Original
                ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                ImageIO.write(bufImg, "bmp", baos2 );
                byte[] data = baos2.toByteArray();
                baos3 = new ByteArrayOutputStream(data.length);

                GZIPOutputStream gz = new GZIPOutputStream(baos3);
                gz.write(data);
                System.out.println(baos3.toByteArray().length);

                System.out.println(data.length);


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyPixles(BufferedImage from, BufferedImage to, int w_start, int h_start, int w_end, int h_end) {
        for (int x = w_start; x < w_end; x++) {
            for (int y = h_start; y < h_end; y++) {
                Color c = new Color(from.getRGB(x, y));
                to.setRGB(x, y, c.getRGB());
            }
        }
    }
}
