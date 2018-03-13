package com.github.zeldazach.binghamtonrover.input;

/**
 * The current state of the system keyboard.
 */
public class KeyboardState
{
    /**
     * Definitions of the different possible keys.
     */
    public enum Key
    {
        A,
        B,
        C,
        D,
        E,
        F,
        G,
        H,
        I,
        J,
        K,
        L,
        M,
        N,
        O,
        P,
        Q,
        R,
        S,
        T,
        U,
        V,
        W,
        X,
        Y,
        Z,

        N0,
        N1,
        N2,
        N3,
        N4,
        N5,
        N6,
        N7,
        N8,
        N9,

        DASH,
        EQUALS,
        LEFT_BRACKET,
        RIGHT_BRACKET,
        BACK_SLASH,
        SEMICOLON,
        QUOTE,
        COMMA,
        PERIOD,
        FORWARD_SLASH,

        TAB,
        LEFT_SHIFT,
        LEFT_CONTROL,
        LEFT_ALT,
        RIGHT_ALT,
        RIGHT_CONTROL,
        RIGHT_SHIFT,
        UP,
        DOWN,
        LEFT,
        RIGHT,
        ENTER,
        BACKSPACE,
        DELETE,

        HOME,
        END,
        PAGE_UP,
        PAGE_DOWN
    }

    /**
     * Holds the on/off values of all the above keys.
     * Each bit, from least to most significant, represents one of the keys above, in enum order.
     * For a Key enum value key, its bit value is (1 << key.ordinal()).
     */
    private long keys;

    /**
     * Sets the state of a specific key.
     * This is threadsafe.
     */
    public void setKey(Key key, boolean value)
    {
        // Our listener has to be on a separate thread.
        synchronized (this)
        {
            if (value)
            {
                keys |= 1 << key.ordinal();
            } else
            {
                keys &= ~(1 << key.ordinal());
            }
        }
    }

    /**
     * Gets the state of a specific key,
     * This is threadsafe.
     */
    public boolean getKey(Key key)
    {
        synchronized (this)
        {
            // If the key is pressed, its bit will be set, so the and result will be non-zero.
            return (keys & (1 << key.ordinal())) != 0;
        }
    }
}
