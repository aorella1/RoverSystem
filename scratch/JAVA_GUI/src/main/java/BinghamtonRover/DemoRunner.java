package BinghamtonRover;

import BinghamtonRover.GuiMain.GuiController;
import BinghamtonRover.Monitors.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
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
        System.out.println("The Python_output file path is at : " + gsFile);
        launch(args);
    }

    @Override
    public void start(Stage aoPrimaryStage) throws NullPointerException, IOException
    {

        //Get the path of the python file.
        URL loFilePath = getClass().getClassLoader().getResource("BinghamtonRover/Monitors/python_output.log.json");
        ArrayList<InformationObserver> laoObservers = new ArrayList<>();


        //System.out.println(GuiController.class.getResource("."));

        FXMLLoader loLoader = new FXMLLoader();
        GuiController loController = new GuiController();
        loLoader.setController(loController);

        Parent loRoot = loLoader.load(getClass().getClassLoader().getResource("BinghamtonRover/GuiMain/guiScene.fxml"));
        aoPrimaryStage.setTitle("CameraFeed");
        aoPrimaryStage.setScene(new Scene(loRoot, 700, 400));
        aoPrimaryStage.show();

        laoObservers.add(new DistanceMonitor());
        laoObservers.add(new BatteryMonitor());
        laoObservers.add(new DirectionMonitor());

        laoObservers.add(new PressureMonitor(loController));
        laoObservers.add(new TimeMonitor(loController));
        laoObservers.add(new CameraStatusMonitor(loController));
        laoObservers.add(new LocationMonitor(loController));
        laoObservers.add(new TemperatureMonitor(loController));

        FileUpdatingObservable loObservable = null;
        try {
            loObservable = new FileUpdatingObservable(loFilePath.getPath(), laoObservers);
        }
        catch(NullPointerException e){
            System.out.println("loFilePath Thrown NullPointerException");
            System.exit(1);
        }
        loObservable.startFileMonitoringThread();

        //System.exit(0);
    }
}
