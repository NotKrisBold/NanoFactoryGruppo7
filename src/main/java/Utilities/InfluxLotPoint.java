package Utilities;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class InfluxLotPoint {
    private static final String token = "Q7S9CEST4vR9o9itqIa2SsAac_Ct3WXYEjeCyY_Zh7vYgQAP9QgxVRV-fQusNAmrGJmJuhDLK4OtQ1dlTzkmXQ==";
    private static final String org = "NanoFactory";
    private static final InfluxDBClient client = InfluxDBClientFactory.create("http://169.254.10.236:8086", token.toCharArray());

    public static void pushLotNormalVersion(Lot lot){
        final String bucket = "NanoFactoryNV";

        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("Lot")
                .addField("totalTimeTaken", lot.getTotalTimeTaken())
                .addField("avarageTime", lot.getAverageTime())
                .addField("leftBalls", lot.getLeftBalls())
                .addField("rightBalls", lot.getRightBalls())
                .time(Instant.now(), WritePrecision.NS);
        writeApi.writePoint(bucket, org, point);
    }

    public static void pushLotRotatoryVersion(Lot lot) {
        final String bucket = "NanoFactoryRS";
        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("Lot")
                .addField("totalTimeTaken", lot.getTotalTimeTaken())
                .addField("avarageTime", lot.getAverageTime())
                .addField("blueBalls", lot.getBlueBalls())
                .addField("redBalls", lot.getRedBalls())
                .addField("leftBalls", lot.getLeftBalls())
                .addField("rightBalls", lot.getRightBalls())
                .time(Instant.now(), WritePrecision.NS);
        writeApi.writePoint(bucket, org, point);
    }
}
