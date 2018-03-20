package com.github.zeldazach.binghamtonrover.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class DisplayApplication extends Application
{

    private static final String WINDOW_TITLE = "Binghamton Rover Base Station";

    private static DisplayApplication INSTANCE = null;

    private static boolean RUNNING = false;

    /**
     * Used for waiting until startup.
     * Inspired by https://stackoverflow.com/questions/25873769/launch-javafx-application-from-another-class.
     */
    private static final CountDownLatch startupLatch = new CountDownLatch(1);

    public static DisplayApplication getInstance() {
        return INSTANCE;
    }

    public static boolean isRunning() {
        return RUNNING;
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
    private Scene scene;

    @Override
    public void start(Stage primaryStage) throws IOException
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("DisplayApplication.fxml"));
        Parent root = loader.load();

        controller = loader.getController();

        scene = new Scene(root);

        primaryStage.setTitle(WINDOW_TITLE);
        primaryStage.setScene(scene);
        primaryStage.show();

        stage = primaryStage;

        INSTANCE = this;
        RUNNING = true;
        startupLatch.countDown();
    }

    @Override
    public void stop() {
        RUNNING = false;
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

    // Delegate method.
    public void renderXboxState()
    {
        controller.renderXboxState();
    }

    public void setSceneBackground(Color color)
    {
        scene.setFill(color);
    }

}
