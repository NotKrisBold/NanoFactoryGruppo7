package NormalVersion;

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

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NanoFactory {
    public static final int LOT_SIZE = 6;

    public static volatile boolean started = false;
    public static boolean acquisitionOn = false;

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

        //LightSensors
        GroveLightSensor left = new GroveLightSensor(grovePi, 2);
        GroveLightSensor right = new GroveLightSensor(grovePi, 3);

        //Monitors
        SensorMonitor<Double> leftMonitor = new SensorMonitor<>(left, 10);
        SensorMonitor<Double> rightMonitor = new SensorMonitor<>(right, 10);


        // LCD
        GroveRgbLcd lcd1 = grovePi.getLCD();

        lcd1.setRGB(255, 255, 255);

        Lot currentLot = new Lot(LOT_SIZE);

        leftMonitor.start();
        rightMonitor.start();
        Thread.sleep(1000);

        while (true) {
            if (!started) {
                Lot lot = activeLots.poll();
                if (lot != null) {
                    currentLot = lot;
                    acquisitionOn = true;
                    started = true;
                    entryLed.set(true);
                }
            }

            if (acquisitionOn) {
                if (leftMonitor.getValue() < 500) {
                    lcd1.setText("left");
                    lcd1.setRGB(0, 255, 0);
                    exitLed.set(true);
                    currentLot.leftIncrement();
                }

                if (rightMonitor.getValue() < 500) {
                    lcd1.setText("right");
                    lcd1.setRGB(255, 0, 0);
                    exitLed.set(true);
                    currentLot.rightIncrement();
                }


                lcd1.setText("R: " + currentLot.getRightBalls() + "\nL: " + currentLot.getLeftBalls());

                if (currentLot.done()) {
                    InfluxLotPoint.pushLotNormalVersion(currentLot);
                    acquisitionOn = false;
                    started = false;
                    entryLed.set(false);
                }

                Thread.sleep(5);
                exitLed.set(false);
                lcd1.setRGB(255, 255, 255);
            }

            Thread.sleep(5);
        }
    }
}
