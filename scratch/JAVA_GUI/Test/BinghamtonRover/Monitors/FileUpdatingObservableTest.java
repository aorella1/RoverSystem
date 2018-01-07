package BinghamtonRover.Monitors;

import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class FileUpdatingObservableTest {

    private FileUpdatingObservable coFUO;

    @Before
    public void setup(){
        FileUpdatingObservable coFUO = new FileUpdatingObservable("TestResources/DummyURL");
    }

    @Test
    public void CostructorTest_Null() {

        coFUO = new FileUpdatingObservable("TestResources/DummyURL");


        assertThrows(NullPointerException.class,
                () ->
                {
                        coFUO = new FileUpdatingObservable(null);
                });
        assertThrows(NullPointerException.class,
                () ->
                {
                    coFUO = new FileUpdatingObservable("DOES_NOT_EXIST");
                });
    }

    @Test
    public void startFileMonitoringThread() {
        fail("This test has not been implemented");
    }

    @Test
    public void addObserver() {
        fail("This test has not been implemented");
    }

    @Test
    public void fileWasUpdated() {
        fail("This test has not been implemented");
    }

    @Test
    public void getCoFileToMonitor() {
        fail("This test has not been implemented");
    }

}