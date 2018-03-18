package com.github.zeldazach.binghamtonrover.gui;

import com.github.zeldazach.binghamtonrover.networking.Manager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ResourceBundle;

public class PastValuePopupController implements Initializable{

    @FXML
    private LineChart<Number, Number> TempChart;
    @FXML
    private LineChart<Number, Number> HumidChart;
    @FXML
    private LineChart<Number, Number> WindSpeedChart;
    @FXML
    private LineChart<Number, Number> PressureChart;
    @FXML
    private LineChart<Number, Number> MethaneChart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        PastValueChart.getXAxis().setAutoRanging(true);
//        PastValueChart.getYAxis().setAutoRanging(true);
        ((NumberAxis) TempChart.getXAxis()).setForceZeroInRange(false);

    }

    public LineChart<Number, Number> getTempChart(){
        return TempChart;
    }

    public LineChart<Number, Number> getHumidChart(){ return HumidChart; }

    public LineChart<Number, Number> getWindSpeedChart() { return WindSpeedChart; }

    public LineChart<Number, Number> getPsurChart() { return PressureChart; }

    public LineChart<Number, Number> getMethaneChart() { return MethaneChart; }
}
