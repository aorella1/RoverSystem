package com.github.zeldazach.binghamtonrover.input;

/**
 * ControllerState represents the state of the XBox input at a given moment in time.
 * It is not threadsafe, however this is never accessed from multiple threads.
 */
public class ControllerState
{
    /**
     * The threshold about 0.0 at which a stick movement will actually be processed.
     * This is fixed for now, but could be calibrated at runtime if necessary.
     */
    private static final float STICK_THRESHOLD = 0.02f;

    /**
     * In the Linux joystick API, the maximum value of an axis.
     * The minimum value of an axis is simply -MAX_AXIS.
     */
    private static final short MAX_AXIS = 32767;

    private static ControllerState INSTANCE;

    public static ControllerState getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new ControllerState();
        }

        return INSTANCE;
    }

    /**
     * Simple buttons, have an off state (false) and an on state (true).
     * <p>
     * Select is the left-most middle button, mode is the glowing button with the logo, and start is the right-most middle button.
     * LThumb and RThumb are the left and right sticks when pressed.
     * <p>
     * TODO: Getters/Setters possibly?
     */
    public boolean buttonX, buttonY, buttonA, buttonB, buttonView, buttonXbox, buttonMenu;
    public boolean buttonLBumper, buttonRBumper, buttonLThumb, buttonRThumb;

    /**
     * Normalized values (between -1 and 1) for "continuous" axes.
     * <p>
     * lTrigger and rTrigger are from -1 (not pressed) to 1 (fully pressed).
     */
    public float lStickX, lStickY, rStickX, rStickY, lTrigger, rTrigger;

    public enum Dpad
    {
        NONE,
        UP,
        UP_RIGHT,
        RIGHT,
        RIGHT_DOWN,
        DOWN,
        DOWN_LEFT,
        LEFT,
        LEFT_UP
    }

    /**
     * The DPAD uses the values enumerated in the packet specification.
     */
    public Dpad dpad;

    /**
     * Clamps stick values under STICK_THRESHOLD to zero.
     *
     * @param value The stick value to evaluate.
     * @return 0 if the value is < STICK_THRESHOLD, and value otherwise.
     */
    public static float clampStickValue(float value)
    {
        if (Math.abs(value) < STICK_THRESHOLD)
        {
            return 0;
        }

        return value;
    }

    /**
     * Normalizes an axis value. Assumes the Linux joystick API.
     *
     * @param value The un-normalized axis value, from -32,767 to 32,767.
     * @return A value between 0 and 1, inclusive on both ends.
     */
    public static float normalizeAxis(short value)
    {
        return value / (float) MAX_AXIS;
    }
}