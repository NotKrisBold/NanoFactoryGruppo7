package NormalVersion;

import Utilities.InfluxLotPoint;
import Utilities.Lot;
import org.iot.raspberry.grovepi.GrovePi;
import org.iot.raspberry.grovepi.pi4j.GrovePi4J;
import org.iot.raspberry.grovepi.sensors.digital.GroveLed;
import org.iot.raspberry.grovepi.sensors.digital.GroveUltrasonicRanger;
import org.iot.raspberry.grovepi.sensors.i2c.GroveRgbLcd;
import org.iot.raspberry.grovepi.sensors.synch.SensorMonitor;

import java.util.logging.Level;
import java.util.logging.Logger;

public class NanoFactory {
    public static final int LOT_SIZE = 6;

    public static boolean started = false;
    public static boolean acquisitionON = false;

    public static void main(String[] args) throws Exception {
        Logger.getLogger("GrovePi").setLevel(Level.WARNING);
        Logger.getLogger("RaspberryPi").setLevel(Level.WARNING);

        GrovePi grovePi = new GrovePi4J();

        // Led
        GroveLed entry = new GroveLed(grovePi, 7);
        GroveLed exit = new GroveLed(grovePi, 8);

        // UltrasonicRanger
        GroveUltrasonicRanger exitRangerLeft = new GroveUltrasonicRanger(grovePi, 2);
        GroveUltrasonicRanger exitRangerRight = new GroveUltrasonicRanger(grovePi, 3);
        GroveUltrasonicRanger entryRanger = new GroveUltrasonicRanger(grovePi, 4);

        // LCD
        GroveRgbLcd lcd1 = grovePi.getLCD();

        // SensorMonitor
        SensorMonitor<Double> exitRangerLeftMonitor = new SensorMonitor<>(exitRangerLeft, 1);
        SensorMonitor<Double> exitRangerRightMonitor = new SensorMonitor<>(exitRangerRight, 1);
        SensorMonitor<Double> entryRangerMonitor = new SensorMonitor<>(entryRanger, 1);

        exitRangerLeftMonitor.start();
        exitRangerRightMonitor.start();
        entryRangerMonitor.start();
        Thread.sleep(1000); //Sleep to allow monitor to start

        while (true) {
            double entryRangerInitial = entryRangerMonitor.getValue();
            double exitRangerRightInitial = exitRangerLeftMonitor.getValue();
            double exitRangerLeftInitial = exitRangerRightMonitor.getValue();

            Lot currentLot = new Lot(LOT_SIZE);

            //Aspettiamo che le palline passino dal sensore di start
            while (!started) {
                if (entryRangerMonitor.isValid()) {
                    if (entryRangerInitial > entryRangerMonitor.getValue()) {
                        currentLot.start();
                        started = true;
                        entry.set(true);
                        acquisitionON = true;
                    }
                }
                Thread.sleep(50);
            }

            //Palline passate
            while (acquisitionON) {
                if (exitRangerLeftMonitor.isValid()) {
                    if (exitRangerLeftInitial > exitRangerLeftMonitor.getValue()) {
                        exit.set(true);
                        currentLot.end();
                        currentLot.leftIncrement();
                    }
                }

                if (exitRangerRightMonitor.isValid()) {
                    if (exitRangerRightInitial > exitRangerRightMonitor.getValue()) {
                        exit.set(true);
                        currentLot.end();
                        currentLot.rightIncrement();
                    }
                }

                lcd1.setText("R: " + currentLot.getRightBalls() + "\nL: " + currentLot.getLeftBalls());

                if(currentLot.done()) {
                    InfluxLotPoint.pushLotNormalVersion(currentLot);
                    acquisitionON = false;
                    started = false;
                }

                Thread.sleep(50);
                exit.set(false);
            }
            entry.set(false);
            Thread.sleep(50);
        }
    }
}
