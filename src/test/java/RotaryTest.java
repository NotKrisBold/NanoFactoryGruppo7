import org.iot.raspberry.grovepi.GrovePi;
import org.iot.raspberry.grovepi.pi4j.GrovePi4J;
import org.iot.raspberry.grovepi.sensors.analog.GroveRotarySensor;
import org.iot.raspberry.grovepi.sensors.data.GroveRotaryValue;
import org.iot.raspberry.grovepi.sensors.digital.GroveButton;
import org.iot.raspberry.grovepi.sensors.listener.GroveButtonListener;
import org.iot.raspberry.grovepi.sensors.synch.SensorMonitor;

import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RotaryTest {
    public static void main(String[] args) throws Exception{
        System.out.println(LocalDateTime.now().toString());
        Logger.getLogger("GrovePi").setLevel(Level.WARNING);
        Logger.getLogger("RaspberryPi").setLevel(Level.WARNING);

        GrovePi grovePi = new GrovePi4J();

        // Rotatory
        GroveButton button = new GroveButton(grovePi, 2);
        button.setButtonListener(new GroveButtonListener() {
            @Override
            public void onRelease() {
                System.out.println("RELEASED");
            }

            @Override
            public void onPress() {
                System.out.println("PRESSED");
            }

            @Override
            public void onClick() {
                System.out.println("Operato arrivato");
            }
        });

        SensorMonitor<Boolean> buttonMonitore = new SensorMonitor<>(button, 100);
        buttonMonitore.start();

        while(true){
            //if(buttonMonitore.isValid())
                //System.out.println(buttonMonitore.getValue());

            Thread.sleep(50);
        }
    }
}
