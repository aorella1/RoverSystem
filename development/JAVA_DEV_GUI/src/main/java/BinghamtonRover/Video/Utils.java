package BinghamtonRover.Video;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Provide general purpose methods for handling OpenCV-JavaFX data conversion.
 * Moreover, expose some "low level" methods for matching few JavaFX behavior.
 *
 * @author Luigi De Russis
 * @author Maximilian Zuleger
 * @version 1.0 (2016-09-17)
 * @since 1.0
 *
 */
public final class Utils
{
    /**
     * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
     *
     * @param frame the {@link Mat} representing the current frame
     * @return the {@link Image} to show
     */
    public static Image mat2Image(Mat frame)
    {
        try
        {
            return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
        }
        catch (Exception e)
        {
            System.err.println("Cannot convert the Mat obejct: " + e);
            return null;
        }
    }

    /**
     * Generic method for putting element running on a non-JavaFX thread on the
     * JavaFX thread, to properly update the UI
     *
     * @param property
     *            a {@link ObjectProperty}
     * @param value
     *            the value to set for the given {@link ObjectProperty}
     */
    public static <T> void onFXThread(final ObjectProperty<T> property, final T value)
    {
        Platform.runLater(() -> {
            property.set(value);
        });
    }

    /**
     * Supports the {@link mat2Image()} method
     *
     * @param original
     *            the {@link Mat} object in BGR or grayscale
     * @return the corresponding {@link BufferedImage}
     */
    private static BufferedImage matToBufferedImage(Mat original)
    {
        // init
        BufferedImage image = null;
        int width = original.width(), height = original.height(), channels = original.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        original.get(0, 0, sourcePixels);

        if (original.channels() > 1)
        {
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        }
        else
        {
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        }
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

        return image;
    }

    /**
     * This method will return an array of integer indicating the size of each fragment
     * packet of the image file during packet to be transmitted.
     * @param fileSize, the size of the original file
     * @return int array of DatagramPacket size
     */
    public static int[] getPacketsSize(int fileSize)
    {
        final int PACKET_LIMIT = 65507;
        int[] arrPacketLen = new int[(int) Math.ceil((double)fileSize/PACKET_LIMIT)];

        for(int i = 0; i < arrPacketLen.length; i++)
        {
            int fragLength = PACKET_LIMIT % fileSize;
            fileSize -= PACKET_LIMIT;

            arrPacketLen[i] = fragLength;
        }
        return arrPacketLen;
    }

    /**
     * This method takes in an integer and return an equivalent byte array
     * @param n integer
     * @return byte array representing the integer
     */
    public static byte[] intToBytes(int n)
    {
        ByteBuffer dbuf = ByteBuffer.allocate(4);
        return dbuf.putInt(n).array();
    }

    /**
     * This method takes a byte array and return an int array. This method
     * requires the byte array to have length that is divisible by 4
     * @param bytes, byte array
     * @return, the integer array, every integer is encoded by 4 bytes
     */
    public static int[] BytesToInt(byte[] bytes)
    {

        int [] arr = new int[bytes.length/4];
        for(int i = 0; i < bytes.length; i+=4) {
            ByteBuffer dbuf = ByteBuffer.wrap(bytes, i, 4);
            arr[i/4] = dbuf.getInt();
        }
        return arr;
    }

    /**
     * This method takes in an integer array, convert it to a byte array ans return it
     * @param arr, integer array
     * @return byte array converted from the integer array
     */
    public static byte[] intToBytes(int[] arr)
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(4*arr.length);
        ByteBuffer dbuf;
        for (int i = 0; i < arr.length; i++) {
            try {
                dbuf = ByteBuffer.allocate(4);
                bos.write(dbuf.putInt(arr[i]).array());
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
        return bos.toByteArray();
    }

}
