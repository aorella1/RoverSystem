package com.github.zeldazach.binghamtonrover.gui;

import eu.hansolo.medusa.Gauge;
import javafx.scene.chart.LineChart;

public class GaugeWrapper {

    private Gauge gauge;
    private ChartData chartData;

    public GaugeWrapper(Gauge gauge, LineChart<Number, Number> chart, String gaugeName){
        this.gauge = gauge;
        this.chartData = new ChartData(chart, gaugeName);
    }

    public void update(double value){
        gauge.setValue(value);
        chartData.addData(value);
    }

}
