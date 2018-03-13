package com.github.zeldazach.binghamtonrover.gui;

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
    private Stage stage;

    @Override
    public void start(Stage primaryStage) throws IOException
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("DisplayApplication.fxml"));
        Parent root = loader.load();

        controller = loader.getController();

        Scene scene = new Scene(root);

        primaryStage.setTitle(WINDOW_TITLE);
        primaryStage.setScene(scene);
        primaryStage.show();

        stage = primaryStage;

        INSTANCE = this;
        startupLatch.countDown();
    }

    // delegate method to get our Image View
    public ImageView getCameraImageView()
    {
        return controller.getCameraImageView();
    }

    public Stage getStage()
    {
        return stage;
    }

}
