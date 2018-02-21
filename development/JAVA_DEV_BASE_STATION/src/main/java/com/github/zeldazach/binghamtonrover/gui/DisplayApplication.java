package com.github.zeldazach.binghamtonrover.gui;

import com.github.zeldazach.binghamtonrover.controller.KeyboardHandler;
import com.github.zeldazach.binghamtonrover.networking.ConnectionState;
import com.github.zeldazach.binghamtonrover.networking.Manager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import java.io.IOException;

public class DisplayApplication extends Application {

    private static final String WINDOW_TITLE = "Binghamton Rover Base Station";

    public static DisplayApplication INSTANCE = null;

    private DisplayApplicationController controller;

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

        updateConnection(Manager.getInstance().getState());

        INSTANCE = this;
    }

    // delegate method to get our Image View
    public ImageView getCameraImageView()
    {
        return controller.getCameraImageView();
    }

    // Method to update color of background
    public void updateConnection(ConnectionState s) {
        Platform.runLater(() -> {
            Paint p = Color.WHITE;

            switch (s) {
                case UNINITIALIZED:
                    p = Color.BLUE;
                    break;
                case CONNECTED:
                    p = Color.WHITE;
                    break;
                case TROUBLED:
                    p = Color.ORANGE;
                    break;
                case DISCONNECTED:
                    p = Color.RED;
                    break;
            }

            controller.updateBackgroundColor(p);
        });
    }

    // Delegate to handle camera connection loss warning.
    public void updateCameraState(boolean working)
    {
        Platform.runLater(() -> controller.updateCameraState(working));
    }

}
