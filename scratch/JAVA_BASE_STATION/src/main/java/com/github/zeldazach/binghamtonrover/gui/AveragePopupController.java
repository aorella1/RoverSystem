package com.github.zeldazach.binghamtonrover.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.net.URL;
import java.util.ResourceBundle;

public class AveragePopupController implements Initializable{

    @FXML
    private LineChart<Number, Number> AverageChart;

    private static XYChart.Series tempData;
    private static int tempCount = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AverageChart.getXAxis().setAutoRanging(true);
        AverageChart.getYAxis().setAutoRanging(true);

        tempData = new XYChart.Series();
        tempData.getData().add(new XYChart.Data<Number, Number>(tempCount++, 17));
        tempData.getData().add(new XYChart.Data<Number, Number>(tempCount++, 18));
        tempData.getData().add(new XYChart.Data<Number, Number>(tempCount++, 19));
        tempData.getData().add(new XYChart.Data<Number, Number>(tempCount++, 24));
        tempData.getData().add(new XYChart.Data<Number, Number>(tempCount++, 17));
        tempData.getData().add(new XYChart.Data<Number, Number>(tempCount++, 18));
        tempData.getData().add(new XYChart.Data<Number, Number>(tempCount++, 19));
        tempData.getData().add(new XYChart.Data<Number, Number>(tempCount++, 24));

        AverageChart.getData().add(tempData);

    }


    public void addTempData(double value){
        tempData.getData().add(new XYChart.Data<Number,Number>(tempCount++, value));
    }


}
