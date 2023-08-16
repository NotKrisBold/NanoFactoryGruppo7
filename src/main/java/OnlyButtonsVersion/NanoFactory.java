package OnlyButtonsVersion;

import Utilities.InfluxLotPoint;
import Utilities.Lot;
import org.iot.raspberry.grovepi.GrovePi;
import org.iot.raspberry.grovepi.pi4j.GrovePi4J;
import org.iot.raspberry.grovepi.sensors.analog.GroveLightSensor;
import org.iot.raspberry.grovepi.sensors.digital.GroveButton;
import org.iot.raspberry.grovepi.sensors.digital.GroveLed;
import org.iot.raspberry.grovepi.sensors.i2c.GroveRgbLcd;
import org.iot.raspberry.grovepi.sensors.listener.GroveButtonListener;
import org.iot.raspberry.grovepi.sensors.synch.SensorMonitor;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NanoFactory {
    public static final int LOT_SIZE = 6;

    public static void main(String[] args) throws Exception {
        Logger.getLogger("GrovePi").setLevel(Level.SEVERE);
        Logger.getLogger("RaspberryPi").setLevel(Level.SEVERE);

        GrovePi grovePi = new GrovePi4J();

        Queue<Lot> activeLots = new LinkedList<>();

        //Led
        GroveLed entryLed = new GroveLed(grovePi, 7);
        GroveLed exitLed = new GroveLed(grovePi, 8);

        //Buttons
        GroveButton entryButton = new GroveButton(grovePi, 1);
        entryButton.setButtonListener(new GroveButtonListener() {
            @Override
            public void onRelease() {

            }

            @Override
            public void onPress() {

            }

            @Override
            public void onClick() {
                activeLots.add(new Lot(LOT_SIZE));
            }
        });
        GroveButton leftButton = new GroveButton(grovePi, 1);
        leftButton.setButtonListener(new GroveButtonListener() {
            @Override
            public void onRelease() {

            }

            @Override
            public void onPress() {

            }

            @Override
            public void onClick() {
                Lot lot = activeLots.peek();
                try {
                    exitLed.set(true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (lot != null && !lot.done())
                    lot.leftIncrement();
            }
        });
        GroveButton rightButton = new GroveButton(grovePi, 1);
        rightButton.setButtonListener(new GroveButtonListener() {
            @Override
            public void onRelease() {

            }

            @Override
            public void onPress() {

            }

            @Override
            public void onClick() {
                Lot lot = activeLots.peek();
                try {
                    exitLed.set(true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (lot != null && !lot.done())
                    lot.rightIncrement();
            }
        });

        // LCD
        GroveRgbLcd lcd1 = grovePi.getLCD();

        lcd1.setRGB(255, 255, 255);

        while (true) {
            Lot currentLot = activeLots.peek();
            if (currentLot == null) {
                entryLed.set(false);
                lcd1.setText("No balls");
            } else {
                lcd1.setText("R: " + currentLot.getRightBalls() + "\nL: " + currentLot.getLeftBalls());
                if (currentLot.done()) {
                    activeLots.poll();
                    InfluxLotPoint.pushLotNormalVersion(currentLot);
                    entryLed.set(false);
                }
            }
            Thread.sleep(5);
            exitLed.set(false);
        }
    }
}
