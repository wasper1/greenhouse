package greenhouse.device;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class THSensorTest {

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenParsingError() throws Exception {
        THSensor thSensor = new THSensor("");
        thSensor.parseData("ERROR");
    }

    @Test
    public void shouldParseDataWhenParsingValidData() throws Exception {
        THSensor thSensor = new THSensor("");
        thSensor.parseData("1.0 2.0");
        assertEquals(thSensor.getTemperature(), 1.0, 0.001);
        assertEquals(thSensor.getHumidity(), 2.0, 0.001);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenCommandOutIsError() throws Exception {
        THSensor thSensor = spy(new THSensor("ERROR"));
        when(thSensor.getReadCommand()).thenReturn("echo");
        thSensor.readData();
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenCommandOutIsUnknown() throws Exception {
        THSensor thSensor = spy(new THSensor("TEST"));
        when(thSensor.getReadCommand()).thenReturn("echo");
        thSensor.readData();
    }

    @Test
    public void shouldParseDataWhenCommandOutIsValid() throws Exception {
        THSensor thSensor = spy(new THSensor("1.0 2.0"));
        when(thSensor.getReadCommand()).thenReturn("echo");
        thSensor.readData();
        assertEquals(thSensor.getTemperature(), 1.0, 0.001);
        assertEquals(thSensor.getHumidity(), 2.0, 0.001);
    }

}