package BinghamtonRover.Monitors;

import BinghamtonRover.GuiMain.GuiController;

import java.util.Observable;

/*
 * Assume this class monitors the target's relative direction to the rover.
 */

public class DirectionMonitor extends InformationObserver
{
    private GuiController coController;

    public DirectionMonitor(GuiController aoController)
    {
        super();
        coController = aoController;
    }

    @Override
    public void update(Observable o, Object arg)
    {
        FileUpdatingObservable loObservable = (FileUpdatingObservable) o;

        String lsLatitude = (String) getJson(loObservable.getCoFileToMonitor(), "latitudeDirection");
        String lsLongitude = (String) getJson(loObservable.getCoFileToMonitor(), "longitudeDirection");

//        System.out.println("The target is at the " + lsLatitude + lsLongitude + " Direction");
        if (coController != null) coController.updateDirection("The target is at: " +  lsLatitude + lsLongitude  + " Direction" );
        else System.out.println("Controller is null");
    }
}
