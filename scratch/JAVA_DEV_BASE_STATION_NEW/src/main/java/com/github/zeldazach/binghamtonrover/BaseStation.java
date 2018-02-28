package com.github.zeldazach.binghamtonrover;

import com.github.zeldazach.binghamtonrover.controller.ControllerHandler;
import com.github.zeldazach.binghamtonrover.gui.DisplayApplication;
import com.github.zeldazach.binghamtonrover.networking.Manager;
import com.github.zeldazach.binghamtonrover.networking.ControllerUpdater;
import javafx.application.Application;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;

public class BaseStation
{
    /**
     * The entry point for the base station control program.
     * @param args Command-line arguments.
     */
    public static void main(String[] args)
    {
        ControllerHandler.init();

        Manager networkManager = null;
        try
        {
            networkManager = new Manager("0.0.0.0", 34343, 5);
        }
        catch (SocketException e)
        {
            System.err.println("Failed to open network UDP socket: " + e.getMessage());
            System.exit(1);
        }
        catch (UnknownHostException e)
        {
            System.err.println("Failed to find the specified host: " + e.getMessage());
            System.exit(1);
        }

        try
        {
            networkManager.startServer();
        }
        catch (Manager.AlreadyStarted | IOException e1)
        {
            e1.printStackTrace();
            System.exit(1);
        }

        //  Create a ControllerUpdater object to send updates of the DPAD buttons to the rover
        try
        {
            ControllerUpdater sendDpad = new ControllerUpdater(networkManager);
            ControllerHandler.getInstance().getControllerState().addObserver(sendDpad);
        }
        catch (Exception e)
        {
            System.out.println("Failed to observe things: " + e.getMessage());

        }

        Application.launch(DisplayApplication.class);
    }
}
