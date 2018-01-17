package BinghamtonRover;

import BinghamtonRover.GuiMain.GuiController;
import BinghamtonRover.Monitors.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.lang3.Validate;
import org.opencv.core.Core;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class DemoRunner extends Application
{
    private static URL gsFile;

    // Load in OpenCV3 libraries
    static
    {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) throws MalformedURLException
    {

        String lsFilePath = "BinghamtonRover/Monitors/python_output.log.json";
        gsFile = (args.length > 0) ? new URL(args[0]) : DemoRunner.class.getClassLoader().getResource(lsFilePath);
        launch(args);
    }

    @Override
    public void start(Stage aoPrimaryStage) throws NullPointerException, IOException
    {

        //Get the path of the FXML file
        URL loFXMLPath = getClass().getClassLoader().getResource("BinghamtonRover/GuiMain/guiScene.fxml");


        //Initialize a new array of Observer
        ArrayList<InformationObserver> laoObservers = new ArrayList<>();

        //Start the GUI
        FXMLLoader loLoader = new FXMLLoader(loFXMLPath);
        Parent loRoot = loLoader.load();
        aoPrimaryStage.setTitle("CameraFeed");
        aoPrimaryStage.setScene(new Scene(loRoot));
        aoPrimaryStage.show();

        //Get the Controller and assign it to each kinds of monitor
        GuiController loController = loLoader.getController();
        Validate.notNull(loController,"GuiController is null");

        laoObservers.add(new DistanceMonitor(loController));
        laoObservers.add(new BatteryMonitor(loController));
        laoObservers.add(new DirectionMonitor(loController));
        laoObservers.add(new PressureMonitor(loController));
        laoObservers.add(new TimeMonitor(loController));
        laoObservers.add(new CameraStatusMonitor(loController));
        laoObservers.add(new LocationMonitor(loController));
        laoObservers.add(new TemperatureMonitor(loController));

        FileUpdatingObservable loObservable = new FileUpdatingObservable(gsFile.getPath(), laoObservers);
        loObservable.startFileMonitoringThread();

    }

    @Override
    public void stop() {
        System.exit(0);
    }
}
