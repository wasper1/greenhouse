package greenhouse;

import greenhouse.device.Button;
import greenhouse.device.MotorDriver;
import greenhouse.device.PowerDriver;
import greenhouse.device.THSensor;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Greenhouse {
    private final static Logger logger = Logger.getLogger(Greenhouse.class);
    private String propertiesFileName;

    public static void main(String[] args) {
        logger.info("Greenhouse");
        Greenhouse greenhouse = new Greenhouse("greenhouse.properties");
        greenhouse.routine();
    }

    private Greenhouse(String propertiesFileName) {
        this.propertiesFileName = propertiesFileName;
    }

    private Properties loadProperties(String filename) {
        Properties prop = new Properties();
        try {
            InputStream input = this.getClass().getClassLoader().getResourceAsStream(filename);
            if (input == null) {
                logger.error("Unable to find properties " + filename);
                System.exit(1);
            }

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
    }
}
