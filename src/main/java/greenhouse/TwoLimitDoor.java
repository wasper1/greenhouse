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

    public void open() {
        if (openedSensor.isActive()) {
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
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
            }
            if (!openedSensor.isActive()) {
                motorDriver.stop();
                powerDriver.setActive(false);
                logger.error("Door opening emergency stop - timeout reached");
            }
        });
        emergencyStop.start();

        motorDriver.goForward();
        powerDriver.setActive(true);
    }

    private void removeAllListeners() {
        openedSensor.removeAction();
        closedSensor.removeAction();
    }

    public void close() {
        if (closedSensor.isActive()) {
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
            if (!closedSensor.isActive()) {
                motorDriver.stop();
                powerDriver.setActive(false);
                logger.error("Door closing emergency stop - timeout reached");
            }
        });
        emergencyStop.start();

        motorDriver.goBackward();
        powerDriver.setActive(true);
    }
}
