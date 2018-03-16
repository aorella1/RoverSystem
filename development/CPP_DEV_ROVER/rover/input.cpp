#include "input.h"

namespace input
{

bool get_controller_button(uint16_t encoding, ControllerButton button)
{
    return (encoding & (uint16_t)button) != 0;
}

void set_controller_button(uint16_t* encoding, ControllerButton button, bool value)
{
    if (value)
    {
        *encoding |= (uint16_t)button;
    } else
    {
        *encoding &= ~(uint16_t)button;
    }
}

bool get_keyboard_button(uint64_t encoding, KeyboardButton button)
{
    return (encoding & (uint64_t)button) != 0;
}

void set_keyboard_button(uint64_t* encoding, KeyboardButton button, bool value)
{
    if (value)
    {
        *encoding |= (uint64_t)button;
    } else
    {
        *encoding &= ~(uint64_t)button;
    }
}

} // namespace input