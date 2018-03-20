package com.github.zeldazach.binghamtonrover;

import com.github.zeldazach.binghamtonrover.gui.DisplayApplication;
import com.github.zeldazach.binghamtonrover.networking.InputEventHandler;
import com.github.zeldazach.binghamtonrover.networking.NetworkManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.logging.Level;
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

        while (true)
        {
            inputEventHandler.update();

            Thread.sleep(250);

            if (!DisplayApplication.isRunning()) break;
        }
    }
}
