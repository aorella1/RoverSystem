package BinghamtonRover.Monitors;

import BinghamtonRover.GuiMain.GuiController;

import java.util.Observable;

/*
 * The DistanceMonitor should monitor the distance that the rover have traveled.
 * the Json file provided the total distance the rover travelled
 */

public class DistanceMonitor extends InformationObserver
{

    private GuiController coController;

    public DistanceMonitor(GuiController aoController){
        super();
        coController = aoController;
    }

    @Override
    public void update(Observable o, Object arg)
    {
        FileUpdatingObservable loObservable = (FileUpdatingObservable) o;

        double lfDistance = (double) getJson(loObservable.getCoFileToMonitor(), "totalDistanceTraveled");

//        System.out.println("Total distance traveled is: " + lfDistance);
        if (coController != null) coController.updateDistance("Total distance traveled: " + lfDistance + " units" );
        else System.out.println("Controller is null");
    }
}
