package com.github.zeldazach.binghamtonrover.input;

public class ControllerEvent {

    /**
     * The bit of an event's type value which indicates if the event is for a button or an axis.
     */
    private static final short BUTTON_BIT = 0x01;

    public enum Type {
        BUTTON,
        AXIS
    }

    private long timestamp;
    private short value;
    private Type type;
    private short number;

    private Controller source;

    ControllerEvent(Controller src, long ts, short v, short t, short n) {
        source = src;
        timestamp = ts;
        value = v;
        // If the button bit is set, its a button. Otherwise, it must be an axis.
        type = (t & BUTTON_BIT) != 0 ? Type.BUTTON : Type.AXIS;
        number = n;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public short getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    public short getNumber() {
        return number;
    }

    public Controller getSource() {
        return source;
    }

}
