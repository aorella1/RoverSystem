package com.github.zeldazach.binghamtonrover.gui;

import BinghamtonRover.gui.Gauges;
import com.github.zeldazach.binghamtonrover.controller.ControllerHandler;
import com.github.zeldazach.binghamtonrover.controller.ControllerState;
import com.github.zeldazach.binghamtonrover.controller.KeyboardHandler;
import eu.hansolo.medusa.Gauge;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class DisplayApplication extends Application
{

    private static final String WINDOW_TITLE = "Binghamton Rover Base Station";

    public static DisplayApplication INSTANCE = null;

    /**
     * These need to be anything with a 16:9 ratio.
     */
    private static final int XBOX_VIEW_WIDTH = 450, XBOX_VIEW_HEIGHT = 243;

    private static final int CAMERA_VIEW_WIDTH = 400, CAMERA_VIEW_HEIGHT = 400;

    //Some Color Constant
    private static final Color EERIEBLACK = Color.rgb(0x19,0x19,0x19);

    /**
     * Yes, this is hardcoded. It's used in the calculation of the offset of the joystick "pressed" image.
     * TODO: Does this need to be hardcoded in the future?
     */
    private static final double JOYSTICK_OFFSET_RATIO = 0.02125;

    public ImageView cameraImageView;


    @Override
    public void start(Stage primary) throws IOException
    {
        INSTANCE = this;

        primary.setTitle(WINDOW_TITLE);

        VBox root = buildRoot();
        Scene scene = new Scene(root);

        // Register keyboard control
        KeyboardHandler.registerHandlers(scene);

        // We do not specify a width and a height. This will cause the window to be sized automatically,
        // which is exactly what we want.
        primary.setScene(scene);
        primary.show();
    }

    /**
     * Builds the GUI, returning the root container.
     * @return The root container of the GUI.
     */
    private VBox buildRoot() throws IOException
    {
        VBox root = new VBox();
        root.setBackground(new Background(new BackgroundFill(EERIEBLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));
        root.setSpacing(10);

        StackPane cameraView = buildCameraView();
        root.getChildren().add(cameraView);

        HBox GaugeView = buildGauges();
        root.getChildren().add(GaugeView);

        Canvas xboxView = buildXboxView();
        GaugeView.getChildren().add(xboxView);


        return root;
    }

    private HBox buildGauges() throws IOException
    {
        URL FXMLurl = getClass().getClassLoader().getResource("gui/Gauges.fxml");
        FXMLLoader loader = new FXMLLoader(FXMLurl);

        HBox GaugeView = loader.load();
        //add gauges
        VBox GaugeBox = (VBox) GaugeView.lookup("#GaugeBox");

        StackPane TempGauge = (StackPane) GaugeBox.lookup("#TempGauge");
        TempGauge.getChildren().addAll(Gauges.TEMPERATURE_GAUGE, Gauges.TEMPERATURE_GRAPH);

        StackPane PsurGauge = (StackPane) GaugeBox.lookup("#PsurGauge");
        PsurGauge.getChildren().addAll(Gauges.PRESSURE_GAUGE, Gauges.PRESSURE_GRAPH);

        Gauges.TEMPERATURE_GRAPH.setVisible(false);
        Gauges.PRESSURE_GRAPH.setVisible(false);
        Gauges.StartAnimation();
        return GaugeView;
    }

    /**
     * Builds the camera feed view. This is a placeholder; for now it simply creates a rectangle.
     * @return The camera feed view.
     */
    private StackPane buildCameraView()
    {
        StackPane cameraView = new StackPane();

        //add a temporary image to cameraView so it don't collapse
        URL imgURL = getClass().getClassLoader().getResource("gui/mars.jpg");
        BufferedImage img = null;
        try {
            img = ImageIO.read(imgURL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        cameraImageView = new ImageView();
        cameraImageView.setImage(SwingFXUtils.toFXImage(img, null));

        cameraView.getChildren().add(cameraImageView);

        return cameraView;
    }

    private void renderXboxState(Canvas xboxCanvas) {
        double joystickOffset = JOYSTICK_OFFSET_RATIO * XBOX_VIEW_WIDTH;
        ControllerState controllerState = ControllerHandler.getInstance().getControllerState();
        GraphicsContext graphicsContext = xboxCanvas.getGraphicsContext2D();

        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillRect(0, 0, xboxCanvas.getWidth(), xboxCanvas.getHeight());

        drawImage(xboxCanvas, (controllerState.buttonA) ? "a_pressed" : "a_unpressed");
        drawImage(xboxCanvas, (controllerState.buttonB) ? "b_pressed" : "b_unpressed");
        drawImage(xboxCanvas, (controllerState.buttonX) ? "x_pressed" : "x_unpressed");
        drawImage(xboxCanvas, (controllerState.buttonY) ? "y_pressed" : "y_unpressed");
        drawImage(xboxCanvas, (controllerState.buttonSelect) ? "view_pressed" : "view_unpressed");
        drawImage(xboxCanvas, (controllerState.buttonMode) ? "xbox_pressed" : "xbox_unpressed");
        drawImage(xboxCanvas, (controllerState.buttonStart) ? "menu_pressed" : "menu_unpressed");
        drawImage(xboxCanvas, (controllerState.buttonLBumper) ? "lb_pressed" : "lb_unpressed");
        drawImage(xboxCanvas, (controllerState.buttonRBumper) ? "rb_pressed" : "rb_unpressed");

        drawImage(xboxCanvas, "js_left_background");
        if (controllerState.lStickX != 0.0 || controllerState.lStickY != 0.0)
        {
            drawImage(xboxCanvas, "js_left_pressed", Math.floor(controllerState.lStickX * joystickOffset),
                    Math.floor(controllerState.lStickY * joystickOffset));
        }
        else
        {
            drawImage(xboxCanvas, "js_left_unpressed");
        }

        drawImage(xboxCanvas, "js_right_background");
        if (controllerState.rStickX != 0.0 || controllerState.rStickY != 0.0)
        {
            drawImage(xboxCanvas, "js_right_pressed", Math.floor(controllerState.rStickX * joystickOffset),
                    Math.floor(controllerState.rStickY * joystickOffset));
        }
        else
        {
            drawImage(xboxCanvas, "js_right_unpressed");
        }

        drawImage(xboxCanvas, "dpad_unpressed");
        if (controllerState.dpad == 0.25) drawImage(xboxCanvas, "dpad_pressed_up");
        if (controllerState.dpad == 0.50) drawImage(xboxCanvas, "dpad_pressed_right");
        if (controllerState.dpad == 0.75) drawImage(xboxCanvas, "dpad_pressed_down");
        if (controllerState.dpad == 1.00) drawImage(xboxCanvas, "dpad_pressed_left");

        drawImage(xboxCanvas, "lt_unpressed");
        graphicsContext.setGlobalAlpha((controllerState.lTrigger + 1.0) / 2.0);
        drawImage(xboxCanvas, "lt_pressed");
        graphicsContext.setGlobalAlpha(1.0);

        drawImage(xboxCanvas, "rt_unpressed");
        graphicsContext.setGlobalAlpha((controllerState.rTrigger + 1.0) / 2.0);
        drawImage(xboxCanvas, "rt_pressed");
        graphicsContext.setGlobalAlpha(1.0);
    }

    private Canvas buildXboxView()
    {
        Canvas xboxCanvas = new Canvas(XBOX_VIEW_WIDTH, XBOX_VIEW_HEIGHT);

        xboxCanvas.getGraphicsContext2D().setFill(Color.BLUE);
        xboxCanvas.getGraphicsContext2D().fillRect(0, 0, XBOX_VIEW_WIDTH, XBOX_VIEW_HEIGHT);

        ControllerHandler.getInstance().getControllerState().addObserver((obs, o) -> Platform.runLater(() -> renderXboxState(xboxCanvas)));

        // Render this now to show the initial state.
        renderXboxState(xboxCanvas);

        return xboxCanvas;
    }

    private Map<String, Image> controllerImageMap = new HashMap<>();

    private void drawImage(Canvas canvas, String imageName)
    {
        drawImage(canvas, imageName, 0, 0);
    }

    private void drawImage(Canvas canvas, String imageName, double x, double y)
    {
        Image image;

        if (!controllerImageMap.containsKey(imageName))
        {
            image = new Image("xbox/" + imageName + ".png");
            controllerImageMap.put(imageName, image);
        }
        else
        {
            image = controllerImageMap.get(imageName);
        }

        canvas.getGraphicsContext2D().drawImage(image, x, y, canvas.getWidth(), canvas.getHeight());
    }
}
