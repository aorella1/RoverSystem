package BinghamtonRover.GuiMain;

import BinghamtonRover.DemoRunner;
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


public class VideoClientGUI extends Application {

    public static void main(String[] args) throws MalformedURLException
    {

//        String lsFilePath = "BinghamtonRover/Monitors/python_output.log.json";
//        gsFile = (args.length > 0) ? new URL(args[0]) : DemoRunner.class.getClassLoader().getResource(lsFilePath);
        launch(args);
    }

    @Override
    public void start(Stage aoPrimaryStage) throws NullPointerException, IOException
    {

        //Get the path of the FXML file
        URL loFXMLPath = getClass().getClassLoader().getResource("BinghamtonRover/GuiMain/ClientGuiScene.fxml");
//        System.out.println(loFXMLPath.toString());

        //Start the GUI
        FXMLLoader loLoader = new FXMLLoader(loFXMLPath);
        Parent loRoot = loLoader.load();
        aoPrimaryStage.setTitle("Client Video Feed");
        aoPrimaryStage.setScene(new Scene(loRoot));
        aoPrimaryStage.show();

//        //Get the Controller and assign it to each kinds of monitor
//        GuiController loController = loLoader.getController();
//        Validate.notNull(loController,"ClientGuiController is null");



    }

    @Override
    public void stop() {
        System.exit(0);
    }
}
