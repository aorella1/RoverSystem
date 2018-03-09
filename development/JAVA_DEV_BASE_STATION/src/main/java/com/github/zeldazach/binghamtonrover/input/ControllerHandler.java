package com.github.zeldazach.binghamtonrover.input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ControllerHandler
{
    private static final String DEVICE_CLASS_DISCOVERY_PATH = "/sys/class/input";
    private static final String JOYSTICK_DEVICE_PREFIX = "js";

    private static ControllerHandler INSTANCE = null;

    public static ControllerHandler getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new ControllerHandler();
        }

        return INSTANCE;
    }

    private ControllerHandler()
    {

    }

    public void getControllers() throws IOException
    {
        // Search for available joysticks.
        List<Path> possibleJoysticks = new ArrayList<>();

        for (Path p : Files.newDirectoryStream(Paths.get(DEVICE_CLASS_DISCOVERY_PATH))) {
            // Must convert it to string to properly evaluate just the file name.
            if (p.getFileName().toString().startsWith(JOYSTICK_DEVICE_PREFIX)) {
                possibleJoysticks.add(p);
            }
        }
    }
}