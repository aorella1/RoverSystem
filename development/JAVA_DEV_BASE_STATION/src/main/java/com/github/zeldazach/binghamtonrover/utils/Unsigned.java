package com.github.zeldazach.binghamtonrover.utils;

/**
 * Defines utility methods to assist with handling unsigned values.
 *
 * Whenever an unsigned value is needed, store it as its signed counterpart. For example, if an unsigned short is
 * needed, store it as a short. That way, if written to a buffer, it retains its bit pattern. If the value of that
 * variable is needed, use one of these methods to get it.
 */
public class Unsigned {

    public static short value(byte b) {
        // Grabs just the least-significant bits of the sign-extended value.
        return (short)(b & 0xFF);
    }

    public static int value(short s) {
        return s & 0xFFFF;
    }

    public static long value(int i) {
        return i & 0xFFFFFFFFL;
    }
}
