package com.github.zeldazach.binghamtonrover.gui;

import javafx.scene.control.Label;

import java.text.DecimalFormat;

public class LabelWrapper {
    private Label label;
    DecimalFormat df;

    public LabelWrapper(Label label){
        this.label = label;
        df = new DecimalFormat("#.####");
    }

    public void update(double latitude, double longitude){

        label.setText( df.format(latitude)+ ", " + df.format(longitude));
    }
}
