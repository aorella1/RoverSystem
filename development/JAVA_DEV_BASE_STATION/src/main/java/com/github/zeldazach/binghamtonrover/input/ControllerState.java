package com.github.zeldazach.binghamtonrover.input;

/**
 * ControllerState represents the state of the XBox input at a given moment in time.
 * It is not threadsafe, however this is never accessed from multiple threads.
 */
public class ControllerState
{
    /**
     * The threshold about 0 at which a stick movement will actually be processed.
     * This is fixed for now, but could be calibrated at runtime if necessary.
     */
    private static final short STICK_THRESHOLD = 2000;

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
     * Integer values (between -32767 and 32767) for "continuous" axes.
     * <p>
     * lTrigger and rTrigger are from -32767 (not pressed) to 32767 (fully pressed).
     */
    public short lStickX, lStickY, rStickX, rStickY, lTrigger = -32767, rTrigger = -32767;

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
    public static short clampStickValue(short value)
    {
        if (Math.abs(value) < STICK_THRESHOLD)
        {
            return 0;
        }

        return value;
    }
}