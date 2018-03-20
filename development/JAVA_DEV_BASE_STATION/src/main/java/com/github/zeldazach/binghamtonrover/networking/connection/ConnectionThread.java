package com.github.zeldazach.binghamtonrover.networking.connection;

import com.github.zeldazach.binghamtonrover.gui.DisplayApplication;
import com.github.zeldazach.binghamtonrover.networking.NetworkManager;
import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.io.IOException;

public class ConnectionThread extends Thread
{

    public ConnectionThread()
    {
        this.setDaemon(true);
    }

    @Override
    public void run()
    {
        while (true)
        {
            long delay;
            try
            {
                delay = NetworkManager.getInstance().getConnectionManager().update();
            } catch (IOException e)
            {
                delay = 250;
                e.printStackTrace();
            }

            // Update color.
            final Color backgroundColor;
            switch (NetworkManager.getInstance().getConnectionManager().getState())
            {
                case CONNECTED:
                    backgroundColor = Color.WHITE;
                    break;
                case TROUBLED:
                    backgroundColor = Color.YELLOW;
                    break;
                case DISCONNECTED:
                    backgroundColor = Color.RED;
                    break;
                default:
                    backgroundColor = Color.GRAY;
            }
            Platform.runLater(() -> DisplayApplication.getInstance().setSceneBackground(backgroundColor));

            try
            {
                Thread.sleep(delay);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
