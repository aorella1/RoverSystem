package com.github.zeldazach.binghamtonrover.input;

public interface KeyboardStateListener
{
    void handle(KeyboardState state, KeyboardState.Key key, boolean value);
}
