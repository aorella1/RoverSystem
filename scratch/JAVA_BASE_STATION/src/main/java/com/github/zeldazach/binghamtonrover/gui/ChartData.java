package com.github.zeldazach.binghamtonrover.gui;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class ChartData {


    private XYChart.Series data;
    private LineChart<Number, Number> chart;
    private int dataCount;

    public ChartData(LineChart<Number, Number> chart, String gaugeName){
        data = new XYChart.Series();
        data.setName(gaugeName);
        dataCount = 0;

        chart.getYAxis().setAutoRanging(true);
        chart.getXAxis().setAutoRanging(true);
        ((NumberAxis)chart.getXAxis()).setForceZeroInRange(false);
        chart.setLegendVisible(false);
//        chart.setAnimated(false);

        this.chart = chart;
        this.chart.getData().add(data);
    }

    protected void addData(double value){
        data.getData().add(new XYChart.Data<Number, Number>(dataCount++, value));
        if(dataCount > 30){
            data.getData().remove(0,1);
        }
    }

}