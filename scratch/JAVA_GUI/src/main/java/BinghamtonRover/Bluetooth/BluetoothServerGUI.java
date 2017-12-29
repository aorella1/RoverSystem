package BinghamtonRover.Bluetooth;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.net.URL;

public class BluetoothServerGUI extends Application {

    @Override
    public void start(Stage primaryStage){

        URL loFXMLPath = getClass().getClassLoader().getResource("BinghamtonRover/Bluetooth/BluetoothGUI.fxml");
        Validate.notNull(loFXMLPath, "The FXML Resource path is null.");
        System.out.println("bilibili");

        System.out.println("loading FXML from: " + loFXMLPath.getPath());
        FXMLLoader loLoader= new FXMLLoader();


        Parent root = null;
        try{
           root = loLoader.load(loFXMLPath);
        }
        catch(IOException e){
          e.printStackTrace();
            System.exit(1);
        }
        Validate.notNull(root);
        primaryStage.setTitle("Bluetooth");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() {

        System.exit(0);
    }

    public static void main (String args[])
    {
        launch(args);
    }

}
