import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.iot.raspberry.grovepi.GrovePi;
import org.iot.raspberry.grovepi.pi4j.GrovePi4J;
import org.iot.raspberry.grovepi.sensors.analog.GroveRotarySensor;
import org.iot.raspberry.grovepi.sensors.data.GroveRotaryValue;
import org.iot.raspberry.grovepi.sensors.digital.GroveBuzzer;
import org.iot.raspberry.grovepi.sensors.digital.GroveUltrasonicRanger;
import org.iot.raspberry.grovepi.sensors.synch.SensorMonitor;

/**
 * Connect:
 * D6 -> Ultrasonic Ranger Sensor
 * D7 -> Buzzer
 */
public class UltrasonicRanger {

    public static void main(String[] args) throws Exception {
        Logger.getLogger("GrovePi").setLevel(Level.WARNING);
        Logger.getLogger("RaspberryPi").setLevel(Level.WARNING);

        GrovePi grovePi = new GrovePi4J();

        GroveRotarySensor rotarySensor = new GroveRotarySensor(grovePi, 1);
        SensorMonitor<GroveRotaryValue> monitor = new SensorMonitor<>(rotarySensor, 100);
        monitor.start();
        Thread.sleep(1000);
        while (true){
            if(monitor.isValid())
                System.out.println(monitor.getValue().getDegrees());
            Thread.sleep(10);
        }
    }
}