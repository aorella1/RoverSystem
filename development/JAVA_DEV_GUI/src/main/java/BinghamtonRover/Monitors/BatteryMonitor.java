package BinghamtonRover.Monitors;

import BinghamtonRover.GuiMain.GuiController;

import java.util.Observable;


/*
 * The BatteryMonitor watches the percentage battery remain on the rover.
 * the battery status will be a number between 0-100.
 */

public class BatteryMonitor extends InformationObserver
{

    private GuiController coController;

    public BatteryMonitor(GuiController aoController)
    {
        super();
        coController = aoController;
    }

    @Override
    public void update(Observable o, Object arg)
    {
        FileUpdatingObservable loObservable = (FileUpdatingObservable) o;

        String lsBattery = getJson(loObservable.getCoFileToMonitor(), "batteryLevel").toString();

//        System.out.println("The current Battery percentage is: lbBattery" + lsBattery + "%");
        if (coController != null) coController.updateBattery("Battery: " + lsBattery + "%" );
        else System.out.println("Controller is null");
    }
}
