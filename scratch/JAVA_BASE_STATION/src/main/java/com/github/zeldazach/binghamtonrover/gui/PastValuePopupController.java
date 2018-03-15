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
    private XYChart.Series humidData;
    private XYChart.Series windSpeedData;
    private XYChart.Series methaneData;


    private int tempCount = 1, psurCount = 1, humidCount = 1, windSpeedCount = 1, methaneCount = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PastValueChart.getXAxis().setAutoRanging(true);
        ((NumberAxis)PastValueChart.getXAxis()).setForceZeroInRange(false);
        PastValueChart.getYAxis().setAutoRanging(true);

        tempData = new XYChart.Series();
        psurData = new XYChart.Series();
        humidData = new XYChart.Series();
        windSpeedData = new XYChart.Series();
        methaneData = new XYChart.Series();

        tempData.setName("Temperature");
        psurData.setName("Air Pressure");
        humidData.setName("Air Humidity");
        windSpeedData.setName("Wind Speed");
        methaneData.setName("Methane");


        PastValueChart.getData().addAll(tempData, psurData, humidData, windSpeedData, methaneData);

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

    public void addHumidData(double value){
        humidData.getData().add(new XYChart.Data<Number, Number>(humidCount++, value));
        if(humidCount > 30){
            humidData.getData().remove(0,1);
        }
    }

    public void addWindSpeedData(double value){
        windSpeedData.getData().add(new XYChart.Data<Number, Number>(windSpeedCount++, value));
        if(windSpeedCount > 30){
            windSpeedData.getData().remove(0,1);
        }
    }

    public void addMethanData(double value){
        methaneData.getData().add(new XYChart.Data<Number, Number>(methaneCount++, value));
        if(methaneCount > 30){
            methaneData.getData().remove(0,1);
        }
    }


}
