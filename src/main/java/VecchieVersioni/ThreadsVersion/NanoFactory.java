package VecchieVersioni.ThreadsVersion;

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
    public static volatile boolean acquisitionON = false;

    public static void main(String[] args) throws Exception {
        Logger.getLogger("GrovePi").setLevel(Level.SEVERE);
        Logger.getLogger("RaspberryPi").setLevel(Level.SEVERE);

        GrovePi grovePi = new GrovePi4J();

        // Led
        GroveLed entry = new GroveLed(grovePi, 7);
        GroveLed exit = new GroveLed(grovePi, 8);

        // UltrasonicRanger
        GroveUltrasonicRanger exitRangerLeft = new GroveUltrasonicRanger(grovePi, 2);
        GroveUltrasonicRanger exitRangerRight = new GroveUltrasonicRanger(grovePi, 3);
        GroveUltrasonicRanger entryRanger = new GroveUltrasonicRanger(grovePi, 6);

        // SensorMonitor
        SensorMonitor<Double> entryRangerMonitor = new SensorMonitor<>(entryRanger, 5);

        // LCD
        GroveRgbLcd lcd1 = grovePi.getLCD();

        entryRangerMonitor.start();
        Thread.sleep(1000); //Sleep to allow monitor to start

        lcd1.setRGB(255, 255, 255);

        Lot currentLot = new Lot(LOT_SIZE);

        //Threads init
        RangerWorker leftWorker = new RangerWorker(exitRangerLeft);
        RangerWorker rightWorker = new RangerWorker(exitRangerRight);


        while (true) {
            if (entryRangerMonitor.isValid()) {
                // Read entry sensor value
                double entryValue = entryRangerMonitor.getValue();

                if (!started && entryValue < 10) {
                    // Ball detected at entry, start tracking
                    System.out.println("Entrate");
                    currentLot = new Lot(LOT_SIZE);
                    started = true;
                    acquisitionON = true;
                    entry.set(true);
                    Thread leftThread = new Thread(leftWorker);
                    Thread rightThread = new Thread(rightWorker);
                    leftThread.start();
                    rightThread.start();
                }
            }

            if (acquisitionON) {
                if (leftWorker.isBallPassed()) {
                    lcd1.setText("left");
                    lcd1.setRGB(0, 255, 0);
                    exit.set(true);
                    currentLot.end();
                    currentLot.leftIncrement();
                }

                if (rightWorker.isBallPassed()) {
                    lcd1.setText("right");
                    lcd1.setRGB(255, 0, 0);
                    exit.set(true);
                    currentLot.end();
                    currentLot.rightIncrement();
                }


                lcd1.setText("R: " + currentLot.getRightBalls() + "\nL: " + currentLot.getLeftBalls());

                if (currentLot.done()) {
                    //InfluxLotPoint.pushLotNormalVersion(currentLot);
                    acquisitionON = false;
                    started = false;
                }

                Thread.sleep(5);
                exit.set(false);
                lcd1.setRGB(255, 255, 255);
            }
            entry.set(false);
            Thread.sleep(5);
        }
    }
}
