package greenhouse;

import com.pi4j.io.gpio.GpioFactory;
import greenhouse.device.Button;
import greenhouse.device.MotorDriver;
import greenhouse.device.PowerDriver;
import greenhouse.device.THSensor;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Greenhouse {
    private final static Logger logger = Logger.getLogger(Greenhouse.class);
    private String propertiesFileName;

    public static void main(String[] args) {
        if (args.length != 1) {
            logger.error("You must specify argument with path to config file");
            System.exit(1);
        }
        Greenhouse greenhouse = new Greenhouse(args[0]);
        greenhouse.routine();
    }

    private Greenhouse(String propertiesFileName) {
        this.propertiesFileName = propertiesFileName;
    }

    private Properties loadProperties(String filePath) {
        Properties prop = new Properties();
        try {
            InputStream input = new FileInputStream(filePath);
            prop.load(input);
            input.close();

        } catch (IOException ex) {
            logger.error("Properties load error ", ex);
            System.exit(1);
        }
        return prop;
    }

    private void routine() {
        Properties properties = loadProperties(propertiesFileName);

        THSensor thSensor = new THSensor(properties.getProperty("THSensorPin"));
        Button openedSensor = new Button(properties.getProperty("frontDoor.openedSensorPin"));
        Button closedSensor = new Button(properties.getProperty("frontDoor.closedSensorPin"));
        PowerDriver powerDriver = new PowerDriver(properties.getProperty("frontDoor.powerDriverPin"));
        MotorDriver motorDriver = new MotorDriver(properties.getProperty("frontDoor.motorDriverAPin"), properties.getProperty("frontDoor.motorDriverBPin"));
        int openTime = Integer.parseInt(properties.getProperty("frontDoor.openSensorlessTime"));
        int timeout = Integer.parseInt(properties.getProperty("frontDoor.moveTimeout"));

        double maxTemperature = Double.parseDouble(properties.getProperty("doorOpenTemperature"));
        double minTemperature = Double.parseDouble(properties.getProperty("doorCloseTemperature"));

        Door door = new OneLimitDoor(openTime, closedSensor, powerDriver, motorDriver, timeout);

        thSensor.readData();
        double temperature = thSensor.getTemperature();
        double humidity = thSensor.getHumidity();
        logger.info("Temperature=" + temperature + "Humidity=" + humidity);
        if (temperature > maxTemperature && door.isClosed()) {
            door.open();
        }
        if (temperature < minTemperature && door.isOpened()) {
            door.close();
        }
        door.waitUntilFinished();
        logger.debug("All tasks finished - shutdown");
        GpioFactory.getInstance().shutdown();
    }
}
