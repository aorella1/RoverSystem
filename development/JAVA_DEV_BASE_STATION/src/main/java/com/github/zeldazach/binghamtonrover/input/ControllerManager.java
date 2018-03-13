package com.github.zeldazach.binghamtonrover.input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ControllerManager {
    public static List<Controller> queryControllers() throws IOException {
        List<Controller> controllers = new ArrayList<>();

        for (Path p : Files.newDirectoryStream(Paths.get("/sys/class/input"))) {
            // Must convert it to string to properly evaluate just the file name.
            if (p.getFileName().toString().startsWith("js")) {
                String name = Files.readAllLines(Paths.get(p.toString(), "device/name")).get(0);
                controllers.add(new Controller(name, "/dev/input/" + p.getFileName().toString()));
            }
        }

        return controllers;
    }
}
