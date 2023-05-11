import java.time.Instant;
import java.util.List;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.*;
import com.influxdb.client.domain.Query;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;

public class InfluxDB2Example {
    public static void main(final String[] args) {

        // You can generate an API token from the "API Tokens Tab" in the UI
        String token = "Q7S9CEST4vR9o9itqIa2SsAac_Ct3WXYEjeCyY_Zh7vYgQAP9QgxVRV-fQusNAmrGJmJuhDLK4OtQ1dlTzkmXQ==";
        String bucket = "db";
        String org = "NanoFactory";

        InfluxDBClient client = InfluxDBClientFactory.create("http://localhost:8086", token.toCharArray());

        System.out.println("Ok sono connesso");

        WriteApiBlocking writeApi = client.getWriteApiBlocking();

        for (int i = 0; i < 100; i++) {
            System.out.println("Aggiungo un punto alla misura");
            Point point = Point
                    .measurement("piscina")
                    .addTag("modo", "auto")
                    .addField("perc_cloro", i * 23.0)
                    .addField("temp", (float)(20+i))
                    .time(Instant.now(), WritePrecision.NS);
            writeApi.writePoint(bucket, org, point);
        }
    }
}
