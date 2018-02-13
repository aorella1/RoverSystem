package BinghamtonRover.Monitors;

import BinghamtonRover.GuiMain.GuiController;
import eu.hansolo.medusa.Gauge;

import java.util.Observable;

/*
 * The Temperature Monitor should monitor the temperature around the rover.
 * The New json file shows that the temperature will probably be in Fahrenheit.
 */

public class TemperatureMonitor extends InformationObserver
{
    private Gauge coGauge;

    public TemperatureMonitor()
    {
        super();
    }

    public TemperatureMonitor(Gauge aoGauge)
    {
        super();

        coGauge = aoGauge;
    }

    @Override
    public void update(Observable o, Object arg)
    {

        long lfTemperature = (long)getJson(((FileUpdatingObservable) o).getCoFileToMonitor(), "temperature");

        if (coGauge != null) coGauge.setValue(lfTemperature);
//                updateTemperature("The current temperature is: " + lfTemperature + "°F" );
    }
}
