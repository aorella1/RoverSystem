package BinghamtonRover.Bluetooth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class BluetoothGUIController {

    private boolean cbServerStarted = false;
    private SampleSPPServer coServer;
    private ExecutorService coServerRunner;


    @FXML
    private TextArea coMessageBoard;
    @FXML
    private Button coStartServerBtn;

    @FXML
    public void updateText(String asTextToAppend){
        coMessageBoard.appendText("\n" + asTextToAppend);
    }

    @FXML
    private void StartServer(ActionEvent event)
    {
        //If the Server has not start, run the server
        if (!cbServerStarted) {
            coServer = new SampleSPPServer(this);
            coServerRunner = Executors.newSingleThreadExecutor();
            coServerRunner.execute(coServer);

            //Set ServerStarted flag and button text.
            this.coStartServerBtn.setText("Stop Server");
            ServerStarted();
        }
        else if(cbServerStarted)
        {
            this.coServerRunner.shutdown();
            System.out.println("Shutting down the server");

            //Set ServerStarted flag and button text.
            this.coStartServerBtn.setText("Start Server");
            ServerClosed();
        }


    }

    public void ApplicationCLosed()
    {
        this.coServerRunner.shutdown();
    }

    private void ServerStarted()
    {
        cbServerStarted = true;
    }

    private void ServerClosed()
    {
        cbServerStarted = false;
    }
}
