package com.github.zeldazach.binghamtonrover.gui;

import com.github.zeldazach.binghamtonrover.controller.KeyboardHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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


        //Update random values to the gauges
        Random rand = new Random();
        ScheduledExecutorService scheduler= Executors.newSingleThreadScheduledExecutor();
        Runnable randGauge  = () -> {
                Platform.runLater(() -> {
                    controller.updateTempGauges(rand.nextGaussian() * 15 + 205);
                    controller.updatePsurGauge(rand.nextGaussian() * 10 + 500);
                    controller.updateHumidGauge(rand.nextGaussian() * 5 + 45);
                    controller.updateWinsSpeedGauge(rand.nextGaussian() * 8 + 35);
                    controller.updateMethaneGauge(rand.nextGaussian() * 2 + 47);
                });
        };

        scheduler.scheduleAtFixedRate(randGauge,1000,1000, TimeUnit.MILLISECONDS);

//        Thread gaugeUpdatingThread = new Thread(randGauge);
//        gaugeUpdatingThread.run();

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
