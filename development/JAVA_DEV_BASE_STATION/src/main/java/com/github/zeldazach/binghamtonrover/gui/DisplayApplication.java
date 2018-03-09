package com.github.zeldazach.binghamtonrover.gui;

import com.github.zeldazach.binghamtonrover.input.KeyboardHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class DisplayApplication extends Application
{

    private static final String WINDOW_TITLE = "Binghamton Rover Base Station";

    private static DisplayApplication INSTANCE = null;

    /**
     * Used for waiting until startup.
     * Inspired by https://stackoverflow.com/questions/25873769/launch-javafx-application-from-another-class.
     */
    private static final CountDownLatch startupLatch = new CountDownLatch(1);

    public static DisplayApplication getInstance() {
        return INSTANCE;
    }

    /**
     * Called at the start of the program.
     * Will sleep until the INSTANCE is set up.
     */
    public static void waitForStart() {
        try
        {
            startupLatch.await();
        } catch (InterruptedException e)
        {
            throw new IllegalStateException("Application startup interrupted");
        }
    }

    private DisplayApplicationController controller;

    @Override
    public void start(Stage primaryStage) throws IOException
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("DisplayApplication.fxml"));
        Parent root = loader.load();

        controller = loader.getController();

        Scene scene = new Scene(root);

        // Register keyboard control
        KeyboardHandler.registerHandlers(scene);

        primaryStage.setTitle(WINDOW_TITLE);
        primaryStage.setScene(scene);
        primaryStage.show();

        INSTANCE = this;
        startupLatch.countDown();
    }

    // delegate method to get our Image View
    public ImageView getCameraImageView()
    {
        return controller.getCameraImageView();
    }

}
