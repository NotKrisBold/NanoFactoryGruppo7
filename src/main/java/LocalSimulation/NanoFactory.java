package LocalSimulation;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import java.time.*;
import java.util.Random;

public class NanoFactory {
    public static final int LOT_SIZE = 6;

    public static void main(String[] args) throws Exception {
        String token = "Q7S9CEST4vR9o9itqIa2SsAac_Ct3WXYEjeCyY_Zh7vYgQAP9QgxVRV-fQusNAmrGJmJuhDLK4OtQ1dlTzkmXQ==";
        String bucket = "NanoFactoryRS";
        String org = "NanoFactory";
        InfluxDBClient client = InfluxDBClientFactory.create("http://localhost:8086", token.toCharArray());

        for (int i = 0; i < 200; i++) {
            Thread.sleep((long) (1000 + (Math.random() * (9000))));
            LocalDateTime start = LocalDateTime.now().minusHours(2);
            ZonedDateTime zonedDateTime = start.atZone(ZoneId.of("UTC"));
            double waitTime = 1000 + (Math.random() * (21000));
            Thread.sleep((long) waitTime);
            Boolean[] balls = new Boolean[LOT_SIZE];
            double totalTime = waitTime / 1000;
            int redBalls = 0;
            int blueBalls = 0;
            for (int j = 0; j < LOT_SIZE; j++) {
                balls[j] = new Random().nextBoolean(); //True = blue, False = red
                if(balls[j])
                    blueBalls++;
                else
                    redBalls++;
                Boolean previousSpeed;
                if (j == 0)
                    previousSpeed = false;
                else
                    previousSpeed = balls[j - 1];

                if (previousSpeed) {
                    if (balls[j])
                        totalTime += 2 + Math.random();
                    else
                        totalTime += 6.5 + Math.random() * 3;
                } else if (balls[j])
                    totalTime += 4 + Math.random() * 2;
                else
                    totalTime += 8.8 + Math.random() * 4.4;
                totalTime += j;
                System.out.println(totalTime);
            }
            int right = 0;
            int left = 0;
            for (int j = 0; j < LOT_SIZE; j++) {
                if (Math.random() * 11 > 7)
                    right++;
                else
                    left++;
            }

            WriteApiBlocking writeApi = client.getWriteApiBlocking();

            Point point = Point.measurement("Lot")
                    .addField("totalTimeTaken", totalTime)
                    .addField("avarageTime", totalTime / LOT_SIZE)
                    .addField("blueBalls", blueBalls)
                    .addField("redBalls", redBalls)
                    .addField("leftBalls", left)
                    .addField("rightBalls", right)
                    .time(zonedDateTime.toInstant(), WritePrecision.NS);
            writeApi.writePoint(bucket, org, point);
        }
    }
}
