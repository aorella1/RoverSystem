package BinghamtonRover.Bluetooth;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BluetoothClientGUI extends Application {

    //THIS CLASS IS NOT USED

    @Override
    public void start(Stage primaryStage) throws Exception{

//        System.out.println("loading FXML from: " + loFXMLurl.getPath());
        FXMLLoader loLoader= new FXMLLoader();
        Parent root = loLoader.load( getClass().getClassLoader().getResource("fxml/BluetoothGUI.fxml"));//change this to a client GUI
        primaryStage.setTitle("Bluetooth Client");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {

        System.exit(0);
    }

    public static void main (String args[])
    {
        launch(args);
    }

}
