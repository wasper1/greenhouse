package greenhouse;

import com.google.common.util.concurrent.Uninterruptibles;
import greenhouse.device.Button;
import greenhouse.device.MotorDriver;
import greenhouse.device.PowerDriver;
import org.apache.log4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class OneLimitDoor implements Door {
    private final static Logger logger = Logger.getLogger(OneLimitDoor.class);
    private final Button closedSensor;
    private final PowerDriver powerDriver;
    private final MotorDriver motorDriver;
    private Thread emergencyStop;
    private final int openTime;
    private final int timeout;
    private CountDownLatch latch = new CountDownLatch(0);

    public OneLimitDoor(int openTime, Button closedSensor, PowerDriver powerDriver, MotorDriver motorDriver, int timeout) {
        this.openTime = openTime;
        this.closedSensor = closedSensor;
        this.powerDriver = powerDriver;
        this.motorDriver = motorDriver;
        this.timeout = timeout;
    }

    @Override
    public boolean isOpened() {
        return !closedSensor.isActive();
    }

    @Override
    public boolean isClosed() {
        return closedSensor.isActive();
    }

    @Override
    public void open() {
        if (isOpened()) {
            logger.error("Trying to open opened doors");
            return;
        }
        removeAllListeners();
        latch = new CountDownLatch(1);
        Thread openingThread = new Thread(() -> {
            Uninterruptibles.sleepUninterruptibly(this.openTime, TimeUnit.SECONDS);
            motorDriver.stop();
            powerDriver.setActive(false);
            latch.countDown();
        });
        openingThread.start();

        motorDriver.goForward();
        powerDriver.setActive(true);
    }

    private void removeAllListeners() {
        closedSensor.removeAction();
    }

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
            motorDriver.stop();
            powerDriver.setActive(false);
            logger.error("Door closing emergency stop - timeout reached");
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
