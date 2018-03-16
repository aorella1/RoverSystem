// This file provides utilities to assist with the PacketInput values.
#include <stdint.h>

namespace input
{

enum class Dpad : uint8_t
{
    DPAD_NONE = 0,
    DPAD_UP = 1,
    DPAD_UP_RIGHT = 2,
    DPAD_RIGHT = 3,
    DPAD_RIGHT_DOWN = 4,
    DPAD_DOWN = 5,
    DPAD_DOWN_LEFT = 6,
    DPAD_LEFT = 7,
    DPAD_LEFT_UP = 8
};

enum class ControllerButton : uint16_t
{
    A =                 0x0001,
    B =                 0x0002,
    X =                 0x0004,
    Y =                 0x0008,

    VIEW =              0x0010,
    XBOX =              0x0020,
    MENU =              0x0040,

    LEFT_BUMPER =       0x0080,
    RIGHT_BUMPER =      0x0100,
    LEFT_STICK_PRESS =  0x0200,
    RIGHT_STICK_PRESS = 0x0400
};

enum class KeyboardButton : uint64_t
{
    A =             0x0000000000000001,
    B =             0x0000000000000002,
    C =             0x0000000000000004,
    D =             0x0000000000000008,
    E =             0x0000000000000010,
    F =             0x0000000000000020,
    G =             0x0000000000000040,
    H =             0x0000000000000080,
    I =             0x0000000000000100,
    J =             0x0000000000000200,
    K =             0x0000000000000400,
    L =             0x0000000000000800,
    M =             0x0000000000001000,
    N =             0x0000000000002000,
    O =             0x0000000000004000,
    P =             0x0000000000008000,
    Q =             0x0000000000010000,
    R =             0x0000000000020000,
    S =             0x0000000000040000,
    T =             0x0000000000080000,
    U =             0x0000000000100000,
    V =             0x0000000000200000,
    W =             0x0000000000400000,
    X =             0x0000000000800000,
    Y =             0x0000000001000000,
    Z =             0x0000000002000000,

    N0 =            0x0000000004000000,
    N1 =            0x0000000008000000,
    N2 =            0x0000000010000000,
    N3 =            0x0000000020000000,
    N4 =            0x0000000040000000,
    N5 =            0x0000000080000000,
    N6 =            0x0000000100000000,
    N7 =            0x0000000200000000,
    N8 =            0x0000000400000000,
    N9 =            0x0000000800000000,

    DASH =          0x0000001000000000,
    EQUALS =        0x0000002000000000,
    LEFT_BRACKET =  0x0000004000000000,
    RIGHT_BRACKET = 0x0000008000000000,
    BACK_SLASH =    0x0000010000000000,
    SEMICOLON =     0x0000020000000000,
    QUOTE =         0x0000040000000000,
    COMMA =         0x0000080000000000,
    PERIOD =        0x0000100000000000,
    FORWARD_SLASH = 0x0000200000000000,

    TAB =           0x0000400000000000,
    LEFT_SHIFT =    0x0000800000000000,
    LEFT_CONTROL =  0x0001000000000000,
    LEFT_ALT =      0x0002000000000000,
    RIGHT_ALT =     0x0004000000000000,
    RIGHT_CONTROL = 0x0008000000000000,
    RIGHT_SHIFT =   0x0010000000000000,
    UP =            0x0020000000000000,
    DOWN =          0x0040000000000000,
    LEFT =          0x0080000000000000,
    RIGHT =         0x0100000000000000,
    ENTER =         0x0200000000000000,
    BACKSPACE =     0x0400000000000000,
    DELETE =        0x0800000000000000,

    HOME =          0x1000000000000000,
    END =           0x2000000000000000,
    PAGE_UP =       0x4000000000000000,
    PAGE_DOWN =     0x8000000000000000
};

bool get_controller_button(uint16_t encoding, ControllerButton button);
void set_controller_button(uint16_t* encoding, ControllerButton button, bool value);
bool get_keyboard_button(uint64_t encoding, KeyboardButton button);
void set_keyboard_button(uint64_t* encoding, KeyboardButton button, bool value);

} // namespace input