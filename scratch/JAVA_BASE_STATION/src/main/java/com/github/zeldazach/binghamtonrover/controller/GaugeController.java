package com.github.zeldazach.binghamtonrover.controller;

import BinghamtonRover.gui.Gauges;
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
            TempAvgBtn.setText("Hide Temp Avg");
            Gauges.TEMPERATURE_GAUGE.setOpacity(0.4);
            Gauges.TEMPERATURE_GRAPH.setVisible(true);
            TempAvgOn = true;
        }
        else {
            TempAvgBtn.setText("Show Temp Avg");
            Gauges.TEMPERATURE_GAUGE.setOpacity(1.0);
            Gauges.TEMPERATURE_GRAPH.setVisible(false);
            TempAvgOn = false;
        }

    }

    @FXML
    public void DispPsurAvg(){
        if (!PsurAvgOn) {
            PsurAvgBtn.setText("Hide Psur Avg");
            Gauges.PRESSURE_GAUGE.setOpacity(0.4);
            Gauges.PRESSURE_GRAPH.setVisible(true);
            PsurAvgOn = true;
        }
        else {
            PsurAvgBtn.setText("Show Psur Avg");
            Gauges.PRESSURE_GAUGE.setOpacity(1.0);
            Gauges.PRESSURE_GRAPH.setVisible(false);
            PsurAvgOn = false;
        }
    }


}
