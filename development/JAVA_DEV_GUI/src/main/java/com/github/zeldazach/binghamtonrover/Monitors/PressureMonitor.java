package com.github.zeldazach.binghamtonrover.Monitors;

import com.github.zeldazach.binghamtonrover.GuiMain.GuiController;

import java.util.Observable;

/*
 * Pressure monitor monitors the air pressure.
 */

public class PressureMonitor extends InformationObserver
{
    private GuiController coController;

    public PressureMonitor()
    {
        super();
    }

    public PressureMonitor(GuiController loController)
    {
        super();

        coController=loController;
    }

    @Override
    public void update(Observable o, Object arg)
    {
        FileUpdatingObservable loObservable = (FileUpdatingObservable) o;

        double lfPressure = (double) getJson(loObservable.getCoFileToMonitor(), "pressure");

        if (coController != null) coController.updatePressure("The air pressure is: " + lfPressure + "stp" );
    }
}
