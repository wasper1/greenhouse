package greenhouse;

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
    }
}
