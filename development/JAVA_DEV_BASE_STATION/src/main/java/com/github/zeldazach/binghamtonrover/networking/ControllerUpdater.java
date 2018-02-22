package com.github.zeldazach.binghamtonrover.networking;

import com.github.zeldazach.binghamtonrover.BaseStation;
import com.github.zeldazach.binghamtonrover.controller.ControllerHandler;
import com.github.zeldazach.binghamtonrover.controller.ControllerState;

import java.util.Observer;
import java.util.Observable;


public class ControllerUpdater implements Observer
{
    //  The observable that this observer is watching and updating
    private ControllerState controllerObservable = ControllerHandler.getInstance().getControllerState();

    //  Use the Manager to send packets to the rover
    private Manager managerUpdates;

    //  Gets the port and address of the rover from the variables in the BaseStation class
    private static int port = BaseStation.roverPort;
    private static String address = BaseStation.roverAddress;

    //  The previous status of the DPAD to compare to
    private float dpadState = controllerObservable.dpad;

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
            //If the states are different make a PacketControl and then send it

            if (dpadState != currentDpadState)
            {
                RoverState roverState = new RoverState();
                //  When no buttons are pressed tell the rover to stop
                if (currentDpadState == 0)
                {
                    roverState.getInstance().setDirection(MovementDirection.STOP, managerUpdates, address, port);
                }

                //  Case 1 when only up is pushed
                if (currentDpadState == .25)
                {
                    roverState.getInstance().setDirection(MovementDirection.FORWARD, managerUpdates, address, port);
                }

                //  Case 2 when only right is pushed
                if (currentDpadState == .5)
                {
                    roverState.getInstance().setDirection(MovementDirection.RIGHT, managerUpdates, address, port);
                }

                //  Case 3 when only down is pushed
                if (currentDpadState == .75)
                {
                    roverState.getInstance().setDirection(MovementDirection.BACKWARD, managerUpdates, address, port);
                }

                //  Case 4 when only left is pushed
                if (currentDpadState == 1.0)
                {
                    roverState.getInstance().setDirection(MovementDirection.LEFT, managerUpdates, address, port);
                }

                //  When the left bumper is pressed the camera view will change
                if (((ControllerState)obs).buttonLBumper == true) {
                    roverState.getInstance().setCamera(roverState.getInstance().getCamera() + 1, managerUpdates, address, port);
                }

                //  Additions if we decide to allow pushing two directions on the DPAD at the same time
                /*
                if(currentDpadState == .125)
                {
                    PacketControl moveForward = new PacketControl(MovementDirection.FORWARD);
                    PacketControl moveLeft = new PacketControl(MovementDirection.LEFT);
                    managerUpdates.sendPacket(moveForward, address, port);
                    managerUpdates.sendPacket(moveLeft, address, port);
                }

                if(currentDpadState == .375)
                {
                    PacketControl moveForward = new PacketControl(MovementDirection.FORWARD);
                    PacketControl moveRight = new PacketControl(PacketControl.MovementDirection.RIGHT);
                    managerUpdates.sendPacket(moveForward, address, port);
                    managerUpdates.sendPacket(moveRight, address, port);
                }

                if(currentDpadState == .675)
                {
                    PacketControl moveBack = new PacketControl(PacketControl.MovementDirection.BACKWARD);
                    PacketControl moveRight = new PacketControl(PacketControl.MovementDirection.RIGHT);
                    managerUpdates.sendPacket(moveBack, address, port);
                    managerUpdates.sendPacket(moveRight, address, port);
                }

                if(currentDpadState == .875)
                {
                    PacketControl moveBack = new PacketControl(PacketControl.MovementDirection.BACKWARD);
                    PacketControl moveLeft = new PacketControl(MovementDirection.LEFT);
                    managerUpdates.sendPacket(moveBack, address, port);
                    managerUpdates.sendPacket(moveLeft, address, port);
                }*/

                dpadState = currentDpadState;

            }
        }
    }
}