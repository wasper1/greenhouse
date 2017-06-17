package greenhouse.device;

import com.pi4j.io.gpio.*;

public class MotorDriver {
    private GpioPinDigitalOutput pinA;
    private GpioPinDigitalOutput pinB;

    public MotorDriver(String pinANumber, String pinBNumber) {
        this(Integer.parseInt(pinANumber), Integer.parseInt(pinBNumber));
    }

    public MotorDriver(int pinANumber, int pinBNumber) {
        GpioController gpio = GpioFactory.getInstance();
        pinA = gpio.provisionDigitalOutputPin(RaspiPin.getPinByAddress(pinANumber), PinState.HIGH);
        pinA.setShutdownOptions(true, PinState.HIGH);
        pinB = gpio.provisionDigitalOutputPin(RaspiPin.getPinByAddress(pinBNumber), PinState.HIGH);
        pinB.setShutdownOptions(true, PinState.HIGH);
    }

    public void stop() {
        pinA.high();
        pinB.high();
    }

    public void goForward() {
        pinA.high();
        pinB.low();
    }

    public void goBackward() {
        pinA.low();
        pinB.high();
    }

}
