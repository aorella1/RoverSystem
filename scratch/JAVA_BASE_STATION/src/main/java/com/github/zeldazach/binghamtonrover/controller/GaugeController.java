package com.github.zeldazach.binghamtonrover.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

public class GaugeController {

    @FXML Button TempAvgBtn;
    @FXML Button PsurAvgBtn;

    @FXML Pane PsurGauge;
    @FXML Pane TempGauge;

    private boolean TempAvgOn;
    private boolean PsurAvgOn;

    @FXML
    public void DispTempAvg() {
        if (!TempAvgOn) {
            TempAvgBtn.setText("Hide Avg");
            TempAvgOn = true;
        }
        else {
            TempAvgBtn.setText("Show Avg");
            TempAvgOn = false;
        }

    }

    @FXML
    public void DispPsurAvg(){
        if (!PsurAvgOn) {
            PsurAvgBtn.setText("Hide Avg");
            PsurAvgOn = true;
        }
        else {
            PsurAvgBtn.setText("Show Avg");
            PsurAvgOn = false;
        }
    }

//    @FXML
//    public void OnPsurGaugeClick(){
//        PsurGauge.setOpacity(0.4);
//    }
//
//    @FXML
//    public void OnTempGaugeClick(){
//        PsurGauge.setOpacity(0.4);
//    }

}
