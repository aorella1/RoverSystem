package com.github.zeldazach.binghamtonrover.networking;

import com.github.zeldazach.binghamtonrover.BaseStation;
import com.github.zeldazach.binghamtonrover.controller.ControllerHandler;
import com.github.zeldazach.binghamtonrover.controller.ControllerState;

import java.io.IOException;
import java.util.Observer;
import java.util.Observable;

import static com.github.zeldazach.binghamtonrover.networking.PacketControl.MovementDirection;

public class ControllerUpdater implements Observer
{
    //  The observable that this observer is watching and updating
    private ControllerState controllerObservable = ControllerHandler.getInstance().getControllerState();

    //  Use the Manager to send packets to the rover
    private Manager managerUpdates;

    //  The previous status of the DPAD to compare to
    private float dpadState = controllerObservable.dpad;
    private float lStickState = controllerObservable.lStickY;
    private float rStickState = controllerObservable.rStickY;

    public int help=0;

    public ControllerUpdater(Manager manager)
    {
        managerUpdates = manager;
    }

    /**
     *  This observer object gets an update from the observable (the controller) when the buttons are pressed
     *  The value of the DPAD is checked and if it has changed then it sends packets to the rover.
     *  @param obs the observable object this observer is responsible for keeping track of
     *  @param arg I have no idea what this does a tutorial told me to have it
     */
    public void update(Observable obs, Object arg)
    {
        //  Make sure the observable notifying this observer is the correct one
        if (obs == controllerObservable)
        {
            //  Check the status of the DPAD (for DEBUGGING purposes
            // System.out.println("Previous:\t"+ dpadState + "\tCurrent:\t" + ((ControllerState)obs).dpad);
            float currentDpadState = ((ControllerState)obs).dpad;
            float lStickState = ((ControllerState)obs).lStickY;
            float rStickState = ((ControllerState)obs).rStickY;
            //If the states are different make a PacketControl and then send it

            help++;

            if (1 == 1)
            {

                try
                {
                    //System.out.println("lStickState = " + lStickState);

                    int low = (int) (lStickState * 7) + 8;
                    int hi = (int) (rStickState * 7) + 8;

                    int total = low + (hi * 16);

                    //System.out.println("low = " + low + " hi = " + hi);

                    Packet n_pack = new PacketControl(lStickState, rStickState);

                    managerUpdates.sendPacket(n_pack);

                    dpadState = currentDpadState;
                }
                catch (IOException e)
                {
                    //System.out.println("Failed to send packet to rover: " + e.getMessage());
                }

                System.out.println("wrote!");
            }

        }
    }
}