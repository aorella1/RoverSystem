package com.github.zeldazach.binghamtonrover.networking;

import com.github.zeldazach.binghamtonrover.gui.DisplayApplication;
import com.github.zeldazach.binghamtonrover.input.Controller;
import com.github.zeldazach.binghamtonrover.input.ControllerEvent;
import com.github.zeldazach.binghamtonrover.input.ControllerManager;
import com.github.zeldazach.binghamtonrover.input.ControllerState;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.util.List;

import static com.github.zeldazach.binghamtonrover.input.ControllerState.normalizeAxis;
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

        @Override
        public void handle(KeyEvent event)
        {

        }
    }

    /**
     * The chosen controller.
     * May be null, in which case there is no controller.
     */
    private Controller controller;

    private KeyboardListener listener;

    /**
     * Note: the application instance must already be started!
     */
    private InputEventHandler() throws IOException
    {
        List<Controller> controllers = ControllerManager.queryControllers();
        if (controllers.isEmpty())
        {
            // TODO: Log that we are not using a controller.
        } else
        {
            // TODO: Log that we are using the first controller. Also log its name.
            controller = controllers.get(0);
        }

        listener = new KeyboardListener();

        DisplayApplication.getInstance().getStage().getScene().setOnKeyPressed(listener);
        DisplayApplication.getInstance().getStage().getScene().setOnKeyReleased(listener);

        // We need to open the controller, if we have one.
        if (controller != null)
        {
            controller.open();
        }
    }

    private void pollController() throws IOException
    {
        ControllerState state = ControllerState.getInstance();

        for (ControllerEvent event : controller.poll())
        {
            short value = event.getValue();

            if (event.getType() == ControllerEvent.Type.AXIS)
            {
                switch (event.getNumber())
                {
                    case 0:
                        state.lStickX = clampStickValue(normalizeAxis(value));
                        break;
                    case 1:
                        state.lStickY = clampStickValue(-normalizeAxis(value));
                        break;
                    case 2:
                        state.lTrigger = normalizeAxis(value);
                        break;
                    case 3:
                        state.rStickX = clampStickValue(normalizeAxis(value));
                        break;
                    case 4:
                        state.rStickY = clampStickValue(-normalizeAxis(value));
                        break;
                    case 5:
                        state.rTrigger = normalizeAxis(value);
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
        }
    }

    /**
     * This is for reading the recent controller events.
     */
    public void poll() throws IOException
    {
        if (controller != null)
        {
            pollController();
        }
    }

}
