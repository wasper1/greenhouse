package greenhouse.device;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class Button {
    private final GpioController gpio = GpioFactory.getInstance();
    private GpioPinDigitalInput pin;

    public Button(int pinNumber) {
        pin = gpio.provisionDigitalInputPin(RaspiPin.getPinByAddress(pinNumber),
                PinPullResistance.PULL_UP);
        pin.setShutdownOptions(true);
    }

    public void setAction(Runnable onActivation, Runnable onDeactivation) {
        pin.removeAllListeners();
        pin.addListener((GpioPinListenerDigital) event -> {
            if (event.getState().isHigh()) {
                onDeactivation.run();
            } else {
                onActivation.run();
            }
        });
    }

    public boolean isActive() {
        return pin.isLow();
    }
}
