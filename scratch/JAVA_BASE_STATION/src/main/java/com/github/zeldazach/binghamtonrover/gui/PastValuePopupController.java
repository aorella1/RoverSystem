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
    private LineChart<Number, Number> PastValueChart;

//    private ArrayList<ChartData> Charts;

//    private TempData tempData = new TempData();
//    private PressureData psurData = new PressureData();
//    private HumidData humidData= new HumidData();
//    private WindSpeedData windSpeedData = new WindSpeedData();
//    private MethaneData methaneData = new MethaneData();



//    private int tempCount = 1, psurCount = 1, humidCount = 1, windSpeedCount = 1, methaneCount = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PastValueChart.getXAxis().setAutoRanging(true);
        PastValueChart.getYAxis().setAutoRanging(true);
        ((NumberAxis) PastValueChart.getXAxis()).setForceZeroInRange(false);

    }

    public LineChart<Number, Number> getLineChart(){
        return PastValueChart;
    }
}
