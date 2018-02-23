package com.github.zeldazach.binghamtonrover.gui;

import com.github.zeldazach.binghamtonrover.controller.KeyboardHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Random;

public class DisplayApplication extends Application
{

    private static final String WINDOW_TITLE = "Binghamton Rover Base Station";

    public static DisplayApplication INSTANCE = null;

    private DisplayApplicationController controller;

    @Override
    public void start(Stage primaryStage) throws IOException
    {
        INSTANCE = this;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("DisplayApplication.fxml"));
        Parent root = loader.load();

        controller = loader.getController();

        Scene scene = new Scene(root);

        // Register keyboard control
        KeyboardHandler.registerHandlers(scene);

        primaryStage.setTitle(WINDOW_TITLE);
        primaryStage.setScene(scene);
        primaryStage.show();

        Random rand = new Random();
        Runnable randGauge  = () -> {
            while (true) {
                controller.updateTempGauges(rand.nextGaussian() * 15 + 205);
                controller.updatePsurGauge(rand.nextGaussian() * 10 + 500);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread t = new Thread(randGauge);
        t.start();

    }

    // delegate method to get our Image View
    public ImageView getCameraImageView()
    {
        return controller.getCameraImageView();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }
}
