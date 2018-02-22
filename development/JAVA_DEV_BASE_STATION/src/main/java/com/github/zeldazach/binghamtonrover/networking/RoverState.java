package com.github.zeldazach.binghamtonrover.networking;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

/**
 *  This class is a singleton and contains the movement direction and the current camera being sent in the packets
 *  for the rover to the server.
 **/

public class RoverState {
    // RoverState is accessed from multiple threads so this ensures accurate information in accessed
    private ReentrantLock lock;

    // Variable for the current camera sent in packets to the server
    private int camera = 0;

    private MovementDirection movement = MovementDirection.STOP;
    private RoverState instance = null;


    public int getCamera() {
        lock.lock();
        int retVal = instance.camera;
        lock.unlock();
        return retVal;
    }

    public void setCamera(int v, Manager managerUpdate, String address, int port) {
        lock.lock();
        getInstance().camera = v%4;

        // Send a control packet
        try {
            PacketControl movement = new PacketControl(getInstance());
            managerUpdate.sendPacket(movement, address, port);
        }
        catch (IOException e) {
            System.out.println("Failed to send packet to rover: " + e.getMessage());
        }
        lock.unlock();
    }

    public MovementDirection getDirection() {
        lock.lock();
        MovementDirection retVal = getInstance().movement;
        lock.unlock();
        return retVal;
    }

    public void setDirection(MovementDirection v, Manager managerUpdate, String address, int port) {
        lock.lock();
        getInstance().movement = v;

        // Send a control packet
        try {
            PacketControl movement = new PacketControl(getInstance());
            managerUpdate.sendPacket(movement, address, port);
        }
        catch (IOException e) {
            System.out.println("Failed to send packet to rover: " + e.getMessage());
        }
        lock.unlock();
    }

    public RoverState getInstance() {
        lock.lock();
        if(instance == null) {
            instance = new RoverState();
        }
        lock.unlock();
        return instance;
    }

}
