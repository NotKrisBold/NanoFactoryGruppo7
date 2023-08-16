package VecchieVersioni.LocalSimulation;

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
            System.out.println("Operatore sta caricando il lotto");
            Thread.sleep((long) (1000 + (Math.random() * (9000))));
            System.out.println("Lotto caricato");

            LocalDateTime start = LocalDateTime.now().minusHours(2);
            ZonedDateTime zonedDateTime = start.atZone(ZoneId.of("UTC"));

            System.out.println("Palline al tornello");
            double waitTime = 1000 + (Math.random() * (21000));
            Thread.sleep((long) waitTime);
            System.out.println("Palline passate");

            Boolean[] balls = new Boolean[LOT_SIZE];
            double totalTime = waitTime / 1000;
            int redBalls = 0;
            int blueBalls = 0;
            for (int j = 0; j < LOT_SIZE; j++) {
                balls[j] = new Random().nextBoolean(); //True = blue, False = red
                if (balls[j]) {
                    blueBalls++;
                    System.out.println("Pallina blu sul lift");
                } else {
                    redBalls++;
                    System.out.println("Pallina rossa sul lift");
                }

                Boolean previousSpeed;
                if (j == 0)
                    previousSpeed = false;
                else
                    previousSpeed = balls[j - 1];

                double time;
                if (previousSpeed) {
                    if (balls[j])
                        time = 2 + Math.random();
                    else
                        time = 6.5 + Math.random() * 3;
                } else if (balls[j])
                    time = 4 + Math.random() * 2;
                else
                    time = 8.8 + Math.random() * 4.4;
                totalTime += j + time;
                Thread.sleep((long)(time * 1000));
                System.out.println("Pallina arrivata alla fine, tempo impiegato: " + time);
            }

            int right = 0;
            int left = 0;
            for (int j = 0; j < LOT_SIZE; j++) {
                if (Math.random() * 11 > 7)
                    right++;
                else
                    left++;
            }

            System.out.println("Tutte le palline sono arrivate alla fine, tempo totale del lotto: " + totalTime);

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
