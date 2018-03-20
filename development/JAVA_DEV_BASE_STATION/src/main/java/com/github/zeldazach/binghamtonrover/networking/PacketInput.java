package com.github.zeldazach.binghamtonrover.networking;

import com.github.zeldazach.binghamtonrover.input.ControllerState;
import com.github.zeldazach.binghamtonrover.input.KeyboardState;

import java.nio.ByteBuffer;

public class PacketInput extends Packet
{
    // These are defined according to the packet spec.
    // More info on values can be found in KeyboardState.
    private byte controllerDpad;
    private short controllerLeftStickX;
    private short controllerLeftStickY;
    private short controllerRightStickX;
    private short controllerRightStickY;
    private short controllerButtonsUnsigned;
    private short controllerLeftTrigger;
    private short controllerRightTrigger;
    private long keyboardButtonsUnsigned;

    public PacketInput()
    {

    }

    @Override
    public void writeToBuffer(ByteBuffer buff)
    {
        buff.put(controllerDpad);
        buff.putShort(controllerLeftStickX);
        buff.putShort(controllerLeftStickY);
        buff.putShort(controllerRightStickX);
        buff.putShort(controllerRightStickY);
        buff.putShort(controllerButtonsUnsigned);
        buff.putShort(controllerLeftTrigger);
        buff.putShort(controllerRightTrigger);
        buff.putLong(keyboardButtonsUnsigned);
    }

    @Override
    public void readFromBuffer(ByteBuffer buff)
    {
        throw new IllegalStateException("Input packets can only be sent from base station -> rover!");
    }

    /**
     * Sets the content of this packet according to the given keyboard state.
     * @param state The current keyboard state.
     */
    public void setKeyboard(KeyboardState state)
    {
        keyboardButtonsUnsigned = state.getRawKeys();
    }

    /**
     * Sets the content of this packet according to the given controller state.
     * @param state The current controller state.
     */
    public void setController(ControllerState state)
    {

    }
}
