package greenhouse;

import com.google.common.util.concurrent.Uninterruptibles;
import greenhouse.device.Button;
import greenhouse.device.MotorDriver;
import greenhouse.device.PowerDriver;
import org.apache.log4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TwoLimitDoor implements Door {
    private final static Logger logger = Logger.getLogger(TwoLimitDoor.class);
    private final Button openedSensor;
    private final Button closedSensor;
    private final PowerDriver powerDriver;
    private final MotorDriver motorDriver;
    private Thread emergencyStop;
    private final int timeout;
    private CountDownLatch latch = new CountDownLatch(0);

    @SuppressWarnings("SameParameterValue")
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
        latch = new CountDownLatch(1);
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
                    logger.debug("Exit emergency thread due to trigger hit");
                    latch.countDown();
                    return;
                }
            }
            logger.error("Door opening emergency stop - timeout reached");
            motorDriver.stop();
            powerDriver.setActive(false);
            latch.countDown();
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
        latch = new CountDownLatch(1);
        closedSensor.setAction(() -> {
            motorDriver.stop();
            powerDriver.setActive(false);
            emergencyStop.interrupt();
        }, () -> {
        });

        emergencyStop = new Thread(() -> {
            for (int i = 0; i < timeout; i++) {
                if (Thread.currentThread().isInterrupted()) {
                    logger.debug("Exit emergency thread due to trigger hit");
                    latch.countDown();
                    return;
                }
                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
            }
            logger.error("Door closing emergency stop - timeout reached");
            motorDriver.stop();
            powerDriver.setActive(false);
            latch.countDown();
        });
        emergencyStop.start();

        motorDriver.goBackward();
        powerDriver.setActive(true);
    }

    @Override
    public void waitUntilFinished() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Wait error", e);
        }
    }
}
