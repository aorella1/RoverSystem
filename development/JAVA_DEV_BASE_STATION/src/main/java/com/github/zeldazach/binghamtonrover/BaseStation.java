package com.github.zeldazach.binghamtonrover;

import com.github.zeldazach.binghamtonrover.gui.DisplayApplication;
import com.github.zeldazach.binghamtonrover.input.InputHandler;
import com.github.zeldazach.binghamtonrover.networking.NetworkManager;
import javafx.application.Application;

import java.io.IOException;

public class BaseStation
{
    /**
     * The entry point for the base station control program.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args)
    {
        // Initialize input by triggering the singleton construction.
        InputHandler inputHandler = InputHandler.getInstance();

        // Initialize the network manager.
        NetworkManager manager = NetworkManager.getInstance();

        // First, launch the JavaFX application in another thread.
        // Then we wait for it to start before starting networking.
        Thread applicationThread = new Thread(() -> Application.launch(DisplayApplication.class));
        applicationThread.setDaemon(true);
        applicationThread.start();

        // Wait for the application to start.
        DisplayApplication.waitForStart();

        // Start the networking.
        try
        {
            manager.start();
        } catch (IOException e)
        {
            throw new RuntimeException("Failed to start: IO Exception while starting network manager", e);
        }

        // Do our main loop.
        while (true)
        {
            try
            {
                inputHandler.poll();

                manager.poll();

                // TODO: Update GUI
            } catch (IOException e)
            {
                // TODO: Log network failure
            }
        }
    }
}
