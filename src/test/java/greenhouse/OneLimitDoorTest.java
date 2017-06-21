package greenhouse;

import com.google.common.util.concurrent.Uninterruptibles;
import greenhouse.device.Button;
import greenhouse.device.MotorDriver;
import greenhouse.device.PowerDriver;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class OneLimitDoorTest {

    private Button closedSensor;
    private PowerDriver powerDriver;
    private MotorDriver motorDriver;

    @Before
    public void setUp() {
        closedSensor = mock(Button.class);
        powerDriver = mock(PowerDriver.class);
        motorDriver = mock(MotorDriver.class);
    }

    @Test
    public void shouldStopMotorWhenAfterOpenTime() throws Exception {
        when(closedSensor.isActive()).thenReturn(true);
        Door door = new OneLimitDoor(1, closedSensor, powerDriver, motorDriver, 1);

        door.open();
        verify(powerDriver).setActive(true);
        verify(motorDriver).goForward();

        Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);

        verify(powerDriver).setActive(false);
        verify(motorDriver).stop();
    }

    @Test
    public void shouldDoNothingWhenIsOpened() throws Exception {
        when(closedSensor.isActive()).thenReturn(false);
        Door door = new OneLimitDoor(0, closedSensor, powerDriver, motorDriver, 1);

        door.open();

        verifyZeroInteractions(powerDriver);
        verifyZeroInteractions(motorDriver);
    }


    @Test
    public void shouldStopMotorWhenCloseTimeout() throws Exception {
        Door door = new OneLimitDoor(0, closedSensor, powerDriver, motorDriver, 1);

        door.close();
        verify(powerDriver).setActive(true);
        verify(motorDriver).goBackward();

        Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);

        verify(powerDriver).setActive(false);
        verify(motorDriver).stop();
    }

    @Test
    public void shouldDoNothingWhenIsClosed() throws Exception {
        when(closedSensor.isActive()).thenReturn(true);
        Door door = new OneLimitDoor(0, closedSensor, powerDriver, motorDriver, 1);

        door.close();

        verifyZeroInteractions(powerDriver);
        verifyZeroInteractions(motorDriver);
    }
}