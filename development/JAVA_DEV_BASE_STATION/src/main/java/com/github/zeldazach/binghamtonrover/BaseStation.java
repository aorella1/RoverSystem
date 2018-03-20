package com.github.zeldazach.binghamtonrover;

import com.github.zeldazach.binghamtonrover.gui.DisplayApplication;
import com.github.zeldazach.binghamtonrover.input.InputEventHandler;
import com.github.zeldazach.binghamtonrover.input.KeyboardState;
import com.github.zeldazach.binghamtonrover.input.KeyboardStateListener;
import com.github.zeldazach.binghamtonrover.networking.NetworkManager;
import com.github.zeldazach.binghamtonrover.networking.PacketControl;
import javafx.application.Application;

import java.io.IOException;
import java.util.logging.Logger;

public class BaseStation
{
    /**
     * The global logger.
     */
    public static final Logger LOGGER = Logger.getLogger("BaseStation");

    /**
     * The entry point for the base station control program.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) throws Exception
    {
        // Initialize the network manager.
        NetworkManager manager = NetworkManager.getInstance();

        // First, launch the JavaFX application in another thread.
        // Then we wait for it to start before starting networking.
        Thread applicationThread = new Thread(() -> Application.launch(DisplayApplication.class));
        applicationThread.setDaemon(true);
        applicationThread.start();

        // Wait for the application to start.
        DisplayApplication.waitForStart();

        // Init the input event handler.
        try
        {
            InputEventHandler.init();

        } catch (IOException e)
        {
            throw new RuntimeException("Failed to start: IO Exception while starting input handler", e);
        }

        // Get a handle for the input event handler.
        InputEventHandler inputEventHandler = InputEventHandler.getInstance();

        // Start the networking.
        try
        {
            manager.start();
        } catch (IOException e)
        {
            throw new RuntimeException("Failed to start: IO Exception while starting network manager", e);
        }

        KeyboardStateListener listener = new KeyboardStateListener()
        {
            private int currentCamera = 0;

            @Override
            public void handle(KeyboardState state, KeyboardState.Key key, boolean value)
            {
                if (key == KeyboardState.Key.N0 && currentCamera != 0)
                {
                    System.out.println("CHANGED CAMERA TO 0");
                    currentCamera = 0;
                } else if (key == KeyboardState.Key.N1 && currentCamera != 1)
                {
                    System.out.println("CHANGED CAMERA TO 1");
                    currentCamera = 1;
                } else
                {
                    return;
                }

                PacketControl packet = new PacketControl(currentCamera);
                try
                {
                    manager.send(packet);
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        };

        inputEventHandler.getKeyboardState().registerListener(listener);

        while (true)
        {
            inputEventHandler.update();

            Thread.sleep(250);

            if (!DisplayApplication.isRunning()) break;
        }
    }
}
