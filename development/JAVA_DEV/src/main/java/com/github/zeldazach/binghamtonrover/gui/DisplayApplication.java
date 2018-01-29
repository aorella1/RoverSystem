package com.github.zeldazach.binghamtonrover.gui;

import com.github.zeldazach.binghamtonrover.controller.ControllerHandler;
import com.github.zeldazach.binghamtonrover.controller.ControllerState;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class DisplayApplication extends Application {

    public static final String WINDOW_TITLE = "Binghamton Rover Base Station";

    /**
     * These need to be anything with a 16:9 ratio.
     */
    public static final int XBOX_VIEW_WIDTH = 800, XBOX_VIEW_HEIGHT = 450;

    public static final int CAMERA_VIEW_WIDTH = 400, CAMERA_VIEW_HEIGHT = 400;

    /**
     * Yes, this is hardcoded. It's used in the calculation of the offset of the joystick "pressed" image.
     */
    private static final double JOYSTICK_OFFSET_RATIO = 0.02125;

    @Override
    public void start(Stage primary) throws Exception {
        primary.setTitle(WINDOW_TITLE);

        VBox root = buildRoot();

        // We do not specify a width and a height. This will cause the window to be sized automatically,
        // which is exactly what we want.
        primary.setScene(new Scene(root));
        primary.show();
    }

    /**
     * Builds the GUI, returning the root container.
     * @return The root container of the GUI.
     */
    private VBox buildRoot() {
        VBox root = new VBox();

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));
        root.setSpacing(10);

        StackPane cameraView = buildCameraView();
        root.getChildren().add(cameraView);

        Canvas xboxView = buildXboxView();
        root.getChildren().add(xboxView);

        return root;
    }

    /**
     * Builds the camera feed view. This is a placeholder; for now it simply creates a rectangle.
     * @return The camera feed view.
     */
    private StackPane buildCameraView() {
        StackPane cameraView = new StackPane();
        cameraView.setAlignment(Pos.CENTER);

        Rectangle rect = new Rectangle(CAMERA_VIEW_WIDTH, CAMERA_VIEW_HEIGHT);
        rect.setFill(Color.AQUAMARINE);

        Text rectLabel = new Text("Camera Feed");

        cameraView.getChildren().addAll(rect, rectLabel);

        return cameraView;
    }

    private Canvas buildXboxView() {
        Canvas xboxCanvas = new Canvas(XBOX_VIEW_WIDTH, XBOX_VIEW_HEIGHT);

        xboxCanvas.getGraphicsContext2D().setFill(Color.BLUE);
        xboxCanvas.getGraphicsContext2D().fillRect(0, 0, XBOX_VIEW_WIDTH, XBOX_VIEW_HEIGHT);

        AnimationTimer loCanvasTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                ControllerState loState = ControllerHandler.getInstance().getState();

                GraphicsContext loCtx = xboxCanvas.getGraphicsContext2D();

                loCtx.setFill(Color.BLACK);
                loCtx.fillRect(0, 0, xboxCanvas.getWidth(), xboxCanvas.getHeight());

                drawImage(xboxCanvas, loState.buttonA ? "a_pressed" : "a_unpressed");

                if (loState.buttonB)    drawImage(xboxCanvas, "b_pressed");
                else                    drawImage(xboxCanvas, "b_unpressed");

                if (loState.buttonX)    drawImage(xboxCanvas, "x_pressed");
                else                    drawImage(xboxCanvas, "x_unpressed");

                if (loState.buttonY)    drawImage(xboxCanvas, "y_pressed");
                else                    drawImage(xboxCanvas, "y_unpressed");

                if (loState.buttonSelect)   drawImage(xboxCanvas, "view_pressed");
                else                        drawImage(xboxCanvas, "view_unpressed");

                if (loState.buttonMode)     drawImage(xboxCanvas, "xbox_pressed");
                else                        drawImage(xboxCanvas, "xbox_unpressed");

                if (loState.buttonStart)    drawImage(xboxCanvas, "menu_pressed");
                else                        drawImage(xboxCanvas, "menu_unpressed");

                if (loState.buttonLBumper)  drawImage(xboxCanvas, "lb_pressed");
                else                        drawImage(xboxCanvas, "lb_unpressed");

                if (loState.buttonRBumper)  drawImage(xboxCanvas, "rb_pressed");
                else                        drawImage(xboxCanvas, "rb_unpressed");

                double joystickOffset = JOYSTICK_OFFSET_RATIO * XBOX_VIEW_WIDTH;

                drawImage(xboxCanvas, "js_left_background");
                if (loState.lStickX != 0.0 || loState.lStickY != 0.0) {
                    drawImage(xboxCanvas, "js_left_pressed", Math.floor(loState.lStickX * joystickOffset),
                            Math.floor(loState.lStickY * joystickOffset));
                } else {
                    drawImage(xboxCanvas, "js_left_unpressed");
                }

                drawImage(xboxCanvas, "js_right_background");
                if (loState.rStickX != 0.0 || loState.rStickY != 0.0) {
                    drawImage(xboxCanvas, "js_right_pressed", Math.floor(loState.rStickX * joystickOffset),
                            Math.floor(loState.rStickY * joystickOffset));
                } else {
                    drawImage(xboxCanvas, "js_right_unpressed");
                }

                drawImage(xboxCanvas, "dpad_unpressed");
                if (loState.dpad == 0.25)   drawImage(xboxCanvas, "dpad_pressed_up");
                if (loState.dpad == 0.50)   drawImage(xboxCanvas, "dpad_pressed_right");
                if (loState.dpad == 0.75)   drawImage(xboxCanvas, "dpad_pressed_down");
                if (loState.dpad == 1.00)   drawImage(xboxCanvas, "dpad_pressed_left");

                drawImage(xboxCanvas, "lt_unpressed");
                loCtx.setGlobalAlpha((loState.lTrigger + 1.0) / 2.0);
                drawImage(xboxCanvas, "lt_pressed");
                loCtx.setGlobalAlpha(1.0);

                drawImage(xboxCanvas, "rt_unpressed");
                loCtx.setGlobalAlpha((loState.rTrigger + 1.0) / 2.0);
                drawImage(xboxCanvas, "rt_pressed");
                loCtx.setGlobalAlpha(1.0);
            }
        };

        loCanvasTimer.start();

        return xboxCanvas;
    }

    private Map<String, Image> controllerImageMap = new HashMap<>();

    private void drawImage(Canvas aoCanvas, String asImageName) {
        drawImage(aoCanvas, asImageName, 0, 0);
    }

    private void drawImage(Canvas aoCanvas, String asImageName, double x, double y) {
        Image loImage;

        if (!controllerImageMap.containsKey(asImageName)) {
            loImage = new Image("xbox/" + asImageName + ".png");
            controllerImageMap.put(asImageName, loImage);
        } else {
            loImage = controllerImageMap.get(asImageName);
        }

        aoCanvas.getGraphicsContext2D().drawImage(loImage, x, y, aoCanvas.getWidth(), aoCanvas.getHeight());
    }
}
