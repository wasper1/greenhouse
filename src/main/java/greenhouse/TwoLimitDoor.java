package greenhouse;

import com.google.common.util.concurrent.Uninterruptibles;
import greenhouse.device.Button;
import greenhouse.device.MotorDriver;
import greenhouse.device.PowerDriver;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

public class TwoLimitDoor implements Door {
    private final static Logger logger = Logger.getLogger(TwoLimitDoor.class);
    private Button openedSensor;
    private Button closedSensor;
    private PowerDriver powerDriver;
    private MotorDriver motorDriver;
    private Thread emergencyStop;
    private int timeout;

    public TwoLimitDoor(Button openedSensor, Button closedSensor, PowerDriver powerDriver, MotorDriver motorDriver, int timeout) {
        this.openedSensor = openedSensor;
        this.closedSensor = closedSensor;
        this.powerDriver = powerDriver;
        this.motorDriver = motorDriver;
        this.timeout = timeout;
    }

    @Override
    public boolean isOpened() {
        return openedSensor.isActive();
    }

    @Override
    public void open() {
        if (isOpened()) {
            logger.error("Trying to open opened doors");
            return;
        }
        removeAllListeners();
        openedSensor.setAction(() -> {
            motorDriver.stop();
            powerDriver.setActive(false);
            emergencyStop.interrupt();
        }, () -> {
        });

        emergencyStop = new Thread(() -> {
            for (int i = 0; i < timeout; i++) {
                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
            }
            motorDriver.stop();
            powerDriver.setActive(false);
            logger.error("Door opening emergency stop - timeout reached");
        });
        emergencyStop.start();

        motorDriver.goForward();
        powerDriver.setActive(true);
    }

    private void removeAllListeners() {
        openedSensor.removeAction();
        closedSensor.removeAction();
    }

    @Override
    public boolean isClosed() {
        return closedSensor.isActive();
    }

    @Override
    public void close() {
        if (isClosed()) {
            logger.error("Trying to close closed doors");
            return;
        }
        removeAllListeners();
        closedSensor.setAction(() -> {
            motorDriver.stop();
            powerDriver.setActive(false);
            emergencyStop.interrupt();
        }, () -> {
        });

        emergencyStop = new Thread(() -> {
            for (int i = 0; i < timeout; i++) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
            }
            motorDriver.stop();
            powerDriver.setActive(false);
            logger.error("Door closing emergency stop - timeout reached");
        });
        emergencyStop.start();

        motorDriver.goBackward();
        powerDriver.setActive(true);
    }
}
