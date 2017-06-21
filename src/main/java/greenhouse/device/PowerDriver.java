package greenhouse.device;

import com.pi4j.io.gpio.*;

public class PowerDriver {
    private final GpioPinDigitalOutput pin;

    public PowerDriver(String pinNumber) {
        this(Integer.parseInt(pinNumber));
    }

    public PowerDriver(int pinNumber) {
        GpioController gpio = GpioFactory.getInstance();
        pin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByAddress(pinNumber), PinState.HIGH);
        pin.setShutdownOptions(true, PinState.HIGH);
    }

    public void setActive(boolean value) {
        if (value) {
            pin.low();
        } else {
            pin.high();
        }
    }
}
