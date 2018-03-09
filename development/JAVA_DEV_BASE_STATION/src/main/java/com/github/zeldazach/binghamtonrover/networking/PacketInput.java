package com.github.zeldazach.binghamtonrover.networking;

import java.nio.ByteBuffer;

public class PacketInput extends Packet
{
    // Controller and keyboard button index definitions
    public enum ControllerButton {
        A, B, X, Y,
        VIEW, XBOX, MENU,
        LB, RB,
        LSP, RSP
    }

    public enum KeyboardButton {
        A, B, C, D, E, F, G, H, I, J, K, L, M, 
    }


    private byte controllerDpad;
    private short controllerLeftStickX;
    private short controllerLeftStickY;
    private short controllerRightStickX;
    private short controllerRightStickY;
    private short controllerButtonsUnsigned;
    private short controllerLeftTrigger;
    private short controllerRightTrigger;
    private long keyboardButtonsUnsigned;

    public PacketInput() {

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

    public void setControllerDpad(byte controllerDpad)
    {
        this.controllerDpad = controllerDpad;
    }

    public void setControllerLeftStickX(short controllerLeftStickX)
    {
        this.controllerLeftStickX = controllerLeftStickX;
    }

    public void setControllerLeftStickY(short controllerLeftStickY)
    {
        this.controllerLeftStickY = controllerLeftStickY;
    }

    public void setControllerRightStickX(short controllerRightStickX)
    {
        this.controllerRightStickX = controllerRightStickX;
    }

    public void setControllerRightStickY(short controllerRightStickY)
    {
        this.controllerRightStickY = controllerRightStickY;
    }

    public void setControllerLeftTrigger(short controllerLeftTrigger)
    {
        this.controllerLeftTrigger = controllerLeftTrigger;
    }

    public void setControllerRightTrigger(short controllerRightTrigger)
    {
        this.controllerRightTrigger = controllerRightTrigger;
    }

    public void setControllerButton(int index, boolean value)
    {
        if (value)
            controllerButtonsUnsigned |= (1 << index);
        else
            controllerButtonsUnsigned &= ~(1 << index);
    }

    public void setKeyboardButton(int index, boolean value)
    {
        if (value)
            keyboardButtonsUnsigned |= (1 << index);
        else
            keyboardButtonsUnsigned &= ~(1 << index);
    }
}
