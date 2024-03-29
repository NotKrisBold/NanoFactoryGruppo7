import Utilities.Lot;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import java.time.Instant;

public class PushTest {
    private static final String token = "Q7S9CEST4vR9o9itqIa2SsAac_Ct3WXYEjeCyY_Zh7vYgQAP9QgxVRV-fQusNAmrGJmJuhDLK4OtQ1dlTzkmXQ==";
    private static final String bucket = "NF";
    private static final String org = "NanoFactory";
    public static void main(String[] args) throws InterruptedException {
        InfluxDBClient client = InfluxDBClientFactory.create("http://169.254.10.236:8086", token.toCharArray());
        System.out.println("Ok sono connesso");

        //Creazione lotto esempio
        Lot lot = new Lot(6);
        lot.start();
        for(int i = 0; i < 6; i++) {
            if(i < 3)
                lot.rightIncrement();
            else
                lot.leftIncrement();
            Thread.sleep(1000);
        }
        lot.setBalls(2);

        System.out.println(lot.getAverageTime());

        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        Point point = Point.measurement("Lot")
                //.addField("lotCreation", lot.getLotCreation().toString())
                .addField("totalTimeTaken", lot.getTotalTimeTaken())
                .addField("avarageTime", lot.getAverageTime())
                .addField("leftBalls", lot.getLeftBalls())
                .addField("rightBalls", lot.getRightBalls())
                .time(Instant.now(), WritePrecision.NS);
        writeApi.writePoint(bucket, org, point);
    }
}
