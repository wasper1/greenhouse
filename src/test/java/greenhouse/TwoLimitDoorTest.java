package greenhouse;

import com.google.common.util.concurrent.Uninterruptibles;
import greenhouse.device.Button;
import greenhouse.device.MotorDriver;
import greenhouse.device.PowerDriver;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class TwoLimitDoorTest {

    private Button openedSensor;
    private Button closedSensor;
    private PowerDriver powerDriver;
    private MotorDriver motorDriver;

    @Before
    public void setUp() {
        openedSensor = mock(Button.class);
        closedSensor = mock(Button.class);
        powerDriver = mock(PowerDriver.class);
        motorDriver = mock(MotorDriver.class);
    }

    @Test
    public void shouldStopMotorWhenOpenTimeout() throws Exception {
        TwoLimitDoor twoLimitDoor = new TwoLimitDoor(openedSensor, closedSensor, powerDriver, motorDriver, 1);

        twoLimitDoor.open();
        verify(powerDriver).setActive(true);
        verify(motorDriver).goForward();

        Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);

        verify(powerDriver).setActive(false);
        verify(motorDriver).stop();
    }

    @Test
    public void shouldDoNothingWhenIsOpened() throws Exception {
        when(openedSensor.isActive()).thenReturn(true);
        TwoLimitDoor twoLimitDoor = new TwoLimitDoor(openedSensor, closedSensor, powerDriver, motorDriver, 1);

        twoLimitDoor.open();

        verifyZeroInteractions(powerDriver);
        verifyZeroInteractions(motorDriver);
    }


    @Test
    public void shouldStopMotorWhenCloseTimeout() throws Exception {
        TwoLimitDoor twoLimitDoor = new TwoLimitDoor(openedSensor, closedSensor, powerDriver, motorDriver, 1);

        twoLimitDoor.close();
        verify(powerDriver).setActive(true);
        verify(motorDriver).goBackward();

        Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);

        verify(powerDriver).setActive(false);
        verify(motorDriver).stop();
    }

    @Test
    public void shouldDoNothingWhenIsClosed() throws Exception {
        when(closedSensor.isActive()).thenReturn(true);
        TwoLimitDoor twoLimitDoor = new TwoLimitDoor(openedSensor, closedSensor, powerDriver, motorDriver, 1);

        twoLimitDoor.close();

        verifyZeroInteractions(powerDriver);
        verifyZeroInteractions(motorDriver);
    }


    @Test
    public void shouldNotWaitWhenNoActiveProcessing() throws Exception {
        TwoLimitDoor twoLimitDoor = new TwoLimitDoor(openedSensor, closedSensor, powerDriver, motorDriver, 1);
        twoLimitDoor.waitUntilFinished();
    }

    @Test
    public void shouldWaitWhenOpening() throws Exception {
        TwoLimitDoor twoLimitDoor = new TwoLimitDoor(openedSensor, closedSensor, powerDriver, motorDriver, 1);

        twoLimitDoor.open();
        twoLimitDoor.waitUntilFinished();
        verify(powerDriver).setActive(false);
        verify(motorDriver).stop();
    }

    @Test
    public void shouldWaitWhenClosing() throws Exception {
        TwoLimitDoor twoLimitDoor = new TwoLimitDoor(openedSensor, closedSensor, powerDriver, motorDriver, 1);

        twoLimitDoor.close();
        twoLimitDoor.waitUntilFinished();

        verify(powerDriver).setActive(false);
        verify(motorDriver).stop();
    }
}