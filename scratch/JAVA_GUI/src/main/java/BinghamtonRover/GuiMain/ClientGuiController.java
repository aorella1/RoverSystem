package BinghamtonRover.GuiMain;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ClientGuiController {

    @FXML private Button coStartClientBtn;
    @FXML private ImageView coVideoFeedView;

    private VideoClient coClient;
    private boolean cbClientActive = false;

    @FXML
    protected void startClient() {

        if(!cbClientActive)
        {
            coClient = new VideoClient(this);
            coClient.start();

            System.out.println("Client has started");
            coStartClientBtn.setText("Client Started");
            coStartClientBtn.setDisable(true);
            cbClientActive = true;
        }
    }

    public void updateImageView(Image aoImage)
    {
        coVideoFeedView.setImage(aoImage);
    }



}
