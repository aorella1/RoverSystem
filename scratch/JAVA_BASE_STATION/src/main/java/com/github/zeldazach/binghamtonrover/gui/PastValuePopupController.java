package com.github.zeldazach.binghamtonrover.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.net.URL;
import java.util.ResourceBundle;

public class PastValuePopupController implements Initializable{

    @FXML
    private LineChart<Number, Number> PastValueChart;

    private XYChart.Series tempData;
    private XYChart.Series psurData;
    private int tempCount = 1, psurCount = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PastValueChart.getXAxis().setAutoRanging(true);
        ((NumberAxis)PastValueChart.getXAxis()).setForceZeroInRange(false);
        PastValueChart.getYAxis().setAutoRanging(true);

        tempData = new XYChart.Series();
        psurData = new XYChart.Series();

        tempData.setName("Temperature");
        psurData.setName("Air Pressure");

        PastValueChart.getData().addAll(tempData, psurData);

    }


    public void addTempData(double value){
        tempData.getData().add(new XYChart.Data<Number,Number>(tempCount++, value));
        if(tempCount > 30){
            tempData.getData().remove(0,1);
        }
    }

    public void addPsurData(double value){
        psurData.getData().add(new XYChart.Data<Number, Number>(psurCount++, value));
        if(psurCount > 30){
            psurData.getData().remove(0,1);
        }
    }


}
