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
import org.iot.raspberry.grovepi.sensors.digital.GroveBuzzer;
import org.iot.raspberry.grovepi.sensors.digital.GroveUltrasonicRanger;
import org.iot.raspberry.grovepi.sensors.synch.SensorMonitor;

/**
 * Connect: 
 * D6 -> Ultrasonic Ranger Sensor 
 * D7 -> Buzzer
 *
 */
public class UltrasonicRanger {

	public static void main(String[] args) throws Exception {
		Logger.getLogger("GrovePi").setLevel(Level.WARNING);
		Logger.getLogger("RaspberryPi").setLevel(Level.WARNING);

		GrovePi grovePi = new GrovePi4J();

		GroveUltrasonicRanger ranger = new GroveUltrasonicRanger(grovePi, 8);
		SensorMonitor<Double> rangerMonitor = new SensorMonitor<>(ranger, 10);

		// You can generate an API token from the "API Tokens Tab" in the UI
		String token = "vUBc-eg1193lQY2jz3npRx4Iw4XRKZ4VNXx4CJpLfSOJPSGoGbTGN7fS3sOalb5eAytaMqi4W4s3AsSHsWA5gA==";
		String bucket = "rangerTest";
		String org = "rangerTest";

		InfluxDBClient client = InfluxDBClientFactory.create("http://169.254.10.236:8086", token.toCharArray());

		WriteApiBlocking writeApi = client.getWriteApiBlocking();

		rangerMonitor.start();
		while (true) {
			if(rangerMonitor.isValid()) {
				System.out.println(rangerMonitor.getValue().doubleValue());
				Point point = Point
						.measurement("distance")
						.addField("value", rangerMonitor.getValue().doubleValue())
						.time(Instant.now(), WritePrecision.NS);
				writeApi.writePoint(bucket, org, point);
			}
			Thread.sleep(2000);
		}
	}

}