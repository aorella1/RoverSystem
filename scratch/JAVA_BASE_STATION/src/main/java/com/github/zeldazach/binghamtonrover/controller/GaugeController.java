package com.github.zeldazach.binghamtonrover.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.awt.event.ActionEvent;

public class GaugeController {

    @FXML Button TempAvgBtn;
    @FXML Button PsurAvgBtn;

    private boolean TempAvgOn;
    private boolean PsurAvgOn;


    @FXML
    public void DispTempAvg() {
        if (!TempAvgOn) {
            TempAvgBtn.setText("Hide Average");
            TempAvgOn = true;
        }
        else {
            TempAvgBtn.setText("Show Average");
            TempAvgOn = false;
        }

    }

    @FXML
    public void DispPsurAvg(){
        if (!PsurAvgOn) {
            PsurAvgBtn.setText("Hide Average");
            PsurAvgOn = true;
        }
        else {
            PsurAvgBtn.setText("Show Average");
            PsurAvgOn = false;
        }
    }

}
