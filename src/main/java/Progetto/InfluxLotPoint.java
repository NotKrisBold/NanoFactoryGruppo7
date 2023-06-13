package Progetto;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import java.time.Instant;

public class InfluxLotPoint {
    // You can generate an API token from the "API Tokens Tab" in the UI
    private static final String token = "Q7S9CEST4vR9o9itqIa2SsAac_Ct3WXYEjeCyY_Zh7vYgQAP9QgxVRV-fQusNAmrGJmJuhDLK4OtQ1dlTzkmXQ==";
    private static final String bucket = "NanoFactory";
    private static final String org = "NanoFactory";
    public static void pushLot(Lot lot) {
        InfluxDBClient client = InfluxDBClientFactory.create("http://localhost:8086", token.toCharArray());

        System.out.println("Ok sono connesso");

        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("Lot")
                .addField("blueBalls", lot.getBlueBalls())
                .addField("redBalls", lot.getRedBalls())
                .addField("avarageTime", lot.getAvarageTime())
                .time(Instant.now(), WritePrecision.NS);
        writeApi.writePoint(bucket, org, point);
    }
}
