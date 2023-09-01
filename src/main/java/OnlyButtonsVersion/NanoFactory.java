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
        GroveButton entryButton = new GroveButton(grovePi, 2);
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
                try {
                    entryLed.set(true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        SensorMonitor<Boolean> entryMonitor = new SensorMonitor<>(entryButton, 10);

        GroveButton leftButton = new GroveButton(grovePi, 3);
        leftButton.setButtonListener(new GroveButtonListener() {
            @Override
            public void onRelease() {

            }

            @Override
            public void onPress() {

            }

            @Override
            public void onClick() {
                System.out.println("l");
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
        SensorMonitor<Boolean> leftMonitor = new SensorMonitor<>(leftButton, 10);

        GroveButton rightButton = new GroveButton(grovePi, 6);
        rightButton.setButtonListener(new GroveButtonListener() {
            @Override
            public void onRelease() {

            }

            @Override
            public void onPress() {

            }

            @Override
            public void onClick() {
                System.out.println("r");
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
        SensorMonitor<Boolean> rightMonitor = new SensorMonitor<>(rightButton, 10);

        // LCD
        GroveRgbLcd lcd1 = grovePi.getLCD();

        lcd1.setRGB(255, 255, 255);
        entryMonitor.start();
        leftMonitor.start();
        rightMonitor.start();
        Thread.sleep(1000);

        while (true) {
            Lot currentLot = activeLots.peek();
            if (currentLot == null) {
                entryLed.set(false);
                lcd1.setText("No balls");
            } else {
                if (currentLot.done()) {
                    activeLots.poll();
                    InfluxLotPoint.pushLotNormalVersion(currentLot);
                    entryLed.set(false);
                }
                lcd1.setText("R: " + currentLot.getRightBalls() + "\nL: " + currentLot.getLeftBalls());
            }
            Thread.sleep(5);
            exitLed.set(false);
        }
    }
}
