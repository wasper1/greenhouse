package greenhouse.device;

import com.google.common.annotations.VisibleForTesting;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class THSensor {
    private final static Logger logger = Logger.getLogger(THSensor.class);
    private final String READ_COMMAND = "readDTH11";
    private final int RETRY_COUNT = 5;
    private String pinNumber;
    private double temperature;
    private double humidity;

    public THSensor(String pinNumber) {
        this.pinNumber = pinNumber;
    }

    public void readData() {
        String data = null;
        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                Process process = new ProcessBuilder(getReadCommand(), pinNumber).start();
                BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                data = br.readLine();
            } catch (IOException e) {
                logger.error("Executing " + getReadCommand() + " fail", e);
            }
            if (!isError(data)) {
                break;
            } else {
                logger.info("Sensor returned invalid data in try " + i);
            }
        }
        parseData(data);
    }

    @VisibleForTesting
    String getReadCommand() {
        return READ_COMMAND;
    }

    @VisibleForTesting
    void parseData(String data) {
        if (isError(data)) {
            throw new RuntimeException("Reading data from THSensor failed");
        }
        String[] splitData = data.split(" ");
        temperature = Double.parseDouble(splitData[0]);
        humidity = Double.parseDouble(splitData[1]);
    }

    private boolean isError(String data) {
        return data == null || "ERROR".equals(data);
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }
}
