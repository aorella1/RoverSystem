package com.github.zeldazach.binghamtonrover.input;

import com.github.zeldazach.binghamtonrover.BaseStation;
import com.github.zeldazach.binghamtonrover.gui.DisplayApplication;
import com.github.zeldazach.binghamtonrover.input.KeyboardState.Key;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.util.List;

import static com.github.zeldazach.binghamtonrover.input.ControllerState.clampStickValue;

/**
 * Listens to incoming controller and keyboard events.
 * Updates static controller and keyboard state objects.
 * Sends input packets when necessary.
 */
public class InputEventHandler
{
    /**
     * Minimum milliseconds between consecutive input packet sends.
     */
    private static final int INPUT_PACKET_SEND_INTERVAL = 200;

    private static InputEventHandler INSTANCE = null;

    public static void init() throws IOException
    {
        INSTANCE = new InputEventHandler();
    }

    public static InputEventHandler getInstance()
    {
        if (INSTANCE == null)
        {
            throw new IllegalStateException("InputEventHandler must be initialized first!");
        }

        return INSTANCE;
    }

    /**
     * Listener for keyboard events.
     */
    private class KeyboardListener implements EventHandler<KeyEvent>
    {
        /**
         * The value (pressed = true, unpressed = false) for this listener.
         */
        private boolean value;

        private KeyboardListener(boolean v)
        {
            value = v;
        }

        @Override
        public void handle(KeyEvent event)
        {
            if (event.getCode().ordinal() >= KeyCode.A.ordinal() && event.getCode().ordinal() <= KeyCode.Z.ordinal())
            {
                // We have an alpha key.
                // Set the correct key, since the enum members in KeyCode and in Key are A-Z.
                int offset = event.getCode().ordinal() - KeyCode.A.ordinal() + Key.A.ordinal();
                keyboardState.setKey(Key.values()[offset], value);
            } else if (event.getCode().ordinal() >= KeyCode.DIGIT0.ordinal()
                    && event.getCode().ordinal() <= KeyCode.DIGIT9.ordinal())
            {
                // We have a digit key.
                int offset = event.getCode().ordinal() - KeyCode.DIGIT0.ordinal() + Key.N0.ordinal();
                keyboardState.setKey(Key.values()[offset], value);
            } else
            {
                // We have to do these manually.

                switch (event.getCode())
                {
                    case UNDERSCORE:
                        keyboardState.setKey(Key.DASH, value);
                        break;
                    case EQUALS:
                        keyboardState.setKey(Key.EQUALS, value);
                        break;
                    case BRACELEFT:
                        keyboardState.setKey(Key.LEFT_BRACKET, value);
                        break;
                    case BRACERIGHT:
                        keyboardState.setKey(Key.RIGHT_BRACKET, value);
                        break;
                    case BACK_SLASH:
                        keyboardState.setKey(Key.BACK_SLASH, value);
                        break;
                    case SEMICOLON:
                        keyboardState.setKey(Key.SEMICOLON, value);
                        break;
                    case QUOTE:
                        keyboardState.setKey(Key.QUOTE, value);
                        break;
                    case COMMA:
                        keyboardState.setKey(Key.COMMA, value);
                        break;
                    case SLASH:
                        keyboardState.setKey(Key.FORWARD_SLASH, value);
                        break;
                    case TAB:
                        keyboardState.setKey(Key.TAB, value);
                        break;
                    case SHIFT:
                        keyboardState.setKey(Key.LEFT_SHIFT, value);
                        keyboardState.setKey(Key.RIGHT_SHIFT, value);
                        break;
                    case CONTROL:
                        keyboardState.setKey(Key.LEFT_CONTROL, value);
                        keyboardState.setKey(Key.RIGHT_CONTROL, value);
                        break;
                    case ALT:
                        keyboardState.setKey(Key.LEFT_ALT, value);
                        keyboardState.setKey(Key.RIGHT_ALT, value);
                        break;
                    case UP:
                        keyboardState.setKey(Key.UP, value);
                        break;
                    case DOWN:
                        keyboardState.setKey(Key.DOWN, value);
                        break;
                    case LEFT:
                        keyboardState.setKey(Key.LEFT, value);
                        break;
                    case RIGHT:
                        keyboardState.setKey(Key.RIGHT, value);
                        break;
                    case ENTER:
                        keyboardState.setKey(Key.ENTER, value);
                        break;
                    case BACK_SPACE:
                        keyboardState.setKey(Key.BACKSPACE, value);
                        break;
                    case DELETE:
                        keyboardState.setKey(Key.DELETE, value);
                        break;
                    case HOME:
                        keyboardState.setKey(Key.HOME, value);
                        break;
                    case END:
                        keyboardState.setKey(Key.END, value);
                        break;
                    case PAGE_UP:
                        keyboardState.setKey(Key.PAGE_UP, value);
                        break;
                    case PAGE_DOWN:
                        keyboardState.setKey(Key.PAGE_DOWN, value);
                }
            }
        }
    }

    /**
     * The chosen controller.
     * May be null, in which case there is no controller.
     */
    private Controller controller;

    private KeyboardState keyboardState = new KeyboardState();

    /**
     * Two listeners, for key press and key release.
     */
    private KeyboardListener downListener, upListener;

    /**
     * Note: the application instance must already be started!
     */
    private InputEventHandler() throws IOException
    {
        List<Controller> controllers = ControllerManager.queryControllers();
        if (controllers.isEmpty())
        {
            // TODO: Log that we are not using a controller.
            BaseStation.LOGGER.info("No controller was found.");
        } else
        {
            // TODO: Log that we are using the first controller. Also log its name.
            BaseStation.LOGGER.info("Using controller " + controllers.get(0).getName());
            controller = controllers.get(0);
        }

        // One listener for key press and one for key release.
        downListener = new KeyboardListener(true);
        upListener = new KeyboardListener(false);

        DisplayApplication.getInstance().getStage().getScene().setOnKeyPressed(downListener);
        DisplayApplication.getInstance().getStage().getScene().setOnKeyReleased(upListener);

        // We need to open the controller, if we have one.
        if (controller != null)
        {
            controller.open();
        }
    }

    public void handleControllerEvent(ControllerEvent event)
    {
        ControllerState state = ControllerState.getInstance();

        short value = event.getValue();

        if (event.getType() == ControllerEvent.Type.AXIS)
        {
            switch (event.getNumber())
            {
                case 0:
                    state.lStickX = clampStickValue(value);
                    break;
                case 1:
                    state.lStickY = clampStickValue(value);
                    break;
                case 2:
                    state.lTrigger = value;
                    break;
                case 3:
                    state.rStickX = clampStickValue(value);
                    break;
                case 4:
                    state.rStickY = clampStickValue(value);
                    break;
                case 5:
                    state.rTrigger = value;
                    break;
                case 6:
                    // Dpad x axis.
                    // For now, we don't do combos.
                    // TODO: Support combos.
                    if (value == 0)
                    {
                        state.dpad = ControllerState.Dpad.NONE;
                    } else if (value < 0)
                    {
                        state.dpad = ControllerState.Dpad.LEFT;
                    } else
                    {
                        state.dpad = ControllerState.Dpad.RIGHT;
                    }
                    break;
                case 7:
                    // Dpad y axis.
                    if (value == 0)
                    {
                        state.dpad = ControllerState.Dpad.NONE;
                    } else if (value < 0)
                    {
                        state.dpad = ControllerState.Dpad.UP;
                    } else
                    {
                        state.dpad = ControllerState.Dpad.DOWN;
                    }
                default:
                    //TODO: Log unknown controller axis.
                    break;
            }
        } else
        {
            switch (event.getNumber())
            {
                case 0:
                    state.buttonA = (value != 0);
                    break;
                case 1:
                    state.buttonB = (value != 0);
                    break;
                case 2:
                    state.buttonX = (value != 0);
                    break;
                case 3:
                    state.buttonY = (value != 0);
                    break;
                case 4:
                    state.buttonLBumper = (value != 0);
                    break;
                case 5:
                    state.buttonRBumper = (value != 0);
                    break;
                case 6:
                    state.buttonView = (value != 0);
                    break;
                case 7:
                    state.buttonMenu = (value != 0);
                    break;
                case 8:
                    state.buttonXbox = (value != 0);
                    break;
                case 9:
                    state.buttonLThumb = (value != 0);
                    break;
                case 10:
                    state.buttonRThumb = (value != 0);
                    break;
                default:
                    // TODO: Log unknown button.
                    break;
            }
        }

        Platform.runLater(() -> DisplayApplication.getInstance().renderXboxState());
    }

    // Called periodically to update the input packet sending.
    public void update()
    {

    }

    public KeyboardState getKeyboardState()
    {
        return keyboardState;
    }
}
