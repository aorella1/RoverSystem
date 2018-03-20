package com.github.zeldazach.binghamtonrover.gui;

import com.github.zeldazach.binghamtonrover.input.ControllerState;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class DisplayApplicationController
{
    @FXML
    private StackPane cameraView;

    @FXML
    private ImageView cameraImageView;

    @FXML
    private Canvas xboxView;

    private GraphicsContext xboxViewGraphicsContext;

    /**
     * Yes, this is hardcoded. It's used in the calculation of the offset of the joystick "pressed" image.
     * TODO: Does this need to be hardcoded in the future?
     */
    private static final double JOYSTICK_OFFSET_RATIO = 0.02125;


    public ImageView getCameraImageView()
    {
        return cameraImageView;
    }

    public void initialize()
    {
        // we can reuse our canvas context rather than continue to call upon this method each time
        xboxViewGraphicsContext = xboxView.getGraphicsContext2D();
        buildXboxView();
    }

    private void buildXboxView()
    {
        xboxViewGraphicsContext.setFill(Color.BLUE);
        xboxViewGraphicsContext.fillRect(0, 0, xboxView.getWidth(), xboxView.getHeight());

        // Render this now to show the initial state.
        renderXboxState();
    }

    private static final short MAX_AXIS_VALUE = 32767;

    public void renderXboxState()
    {
        double joystickOffset = JOYSTICK_OFFSET_RATIO * xboxView.getWidth();

        ControllerState controllerState = ControllerState.getInstance();

        xboxViewGraphicsContext.setFill(Color.BLACK);
        xboxViewGraphicsContext.fillRect(0, 0, xboxView.getWidth(), xboxView.getHeight());

        drawImage(controllerState.buttonA ? "a_pressed" : "a_unpressed");
        drawImage(controllerState.buttonB ? "b_pressed" : "b_unpressed");
        drawImage(controllerState.buttonX ? "x_pressed" : "x_unpressed");
        drawImage(controllerState.buttonY ? "y_pressed" : "y_unpressed");
        drawImage(controllerState.buttonView ? "view_pressed" : "view_unpressed");
        drawImage(controllerState.buttonXbox ? "xbox_pressed" : "xbox_unpressed");
        drawImage(controllerState.buttonMenu ? "menu_pressed" : "menu_unpressed");
        drawImage(controllerState.buttonLBumper ? "lb_pressed" : "lb_unpressed");
        drawImage(controllerState.buttonRBumper ? "rb_pressed" : "rb_unpressed");

        float lStickX = controllerState.lStickX / (float) MAX_AXIS_VALUE;
        float lStickY = controllerState.lStickY / (float) MAX_AXIS_VALUE;
        float rStickX = controllerState.rStickX / (float) MAX_AXIS_VALUE;
        float rStickY = controllerState.rStickY / (float) MAX_AXIS_VALUE;

        float lTrigger = controllerState.lTrigger / (float) MAX_AXIS_VALUE;
        float rTrigger = controllerState.rTrigger / (float) MAX_AXIS_VALUE;

        drawImage("js_left_background");
        if (controllerState.lStickX != 0 || controllerState.lStickY != 0)
        {
            drawImage("js_left_pressed", Math.floor(lStickX * joystickOffset),
                    Math.floor(lStickY * joystickOffset));
        } else
        {
            drawImage("js_left_unpressed");
        }

        drawImage("js_right_background");
        if (controllerState.rStickX != 0 || controllerState.rStickY != 0)
        {
            drawImage("js_right_pressed", Math.floor(rStickX * joystickOffset),
                    Math.floor(rStickY * joystickOffset));
        } else
        {
            drawImage("js_right_unpressed");
        }

        drawImage("dpad_unpressed");
        if (controllerState.dpad == ControllerState.Dpad.UP) drawImage("dpad_pressed_up");
        if (controllerState.dpad == ControllerState.Dpad.RIGHT) drawImage("dpad_pressed_right");
        if (controllerState.dpad == ControllerState.Dpad.DOWN) drawImage("dpad_pressed_down");
        if (controllerState.dpad == ControllerState.Dpad.LEFT) drawImage("dpad_pressed_left");

        drawImage("lt_unpressed");
        xboxViewGraphicsContext.setGlobalAlpha((lTrigger + 1.0) / 2.0);
        drawImage("lt_pressed");
        xboxViewGraphicsContext.setGlobalAlpha(1.0);

        drawImage("rt_unpressed");
        xboxViewGraphicsContext.setGlobalAlpha((rTrigger + 1.0) / 2.0);
        drawImage("rt_pressed");
        xboxViewGraphicsContext.setGlobalAlpha(1.0);

    }

    private Map<String, Image> controllerImageMap = new HashMap<>();

    private void drawImage(String imageName)
    {
        drawImage(imageName, 0, 0);
    }

    private void drawImage(String imageName, double x, double y)
    {
        Image image;

        if (!controllerImageMap.containsKey(imageName))
        {
            image = new Image("xbox/" + imageName + ".png");
            controllerImageMap.put(imageName, image);
        } else
        {
            image = controllerImageMap.get(imageName);
        }

        xboxViewGraphicsContext.drawImage(image, x, y, xboxView.getWidth(), xboxView.getHeight());
    }

}
