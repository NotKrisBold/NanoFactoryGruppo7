package RotatoryVersion;

import Utilities.Ball_Type;
import Utilities.InfluxLotPoint;
import Utilities.Lot;
import org.iot.raspberry.grovepi.GrovePi;
import org.iot.raspberry.grovepi.pi4j.GrovePi4J;
import org.iot.raspberry.grovepi.sensors.analog.GroveRotarySensor;
import org.iot.raspberry.grovepi.sensors.data.GroveRotaryValue;
import org.iot.raspberry.grovepi.sensors.digital.GroveLed;
import org.iot.raspberry.grovepi.sensors.digital.GroveUltrasonicRanger;
import org.iot.raspberry.grovepi.sensors.i2c.GroveRgbLcd;
import org.iot.raspberry.grovepi.sensors.synch.SensorMonitor;

import java.util.logging.Level;
import java.util.logging.Logger;

public class NanoFactoryRotatoryVersion {
    public static final int LOT_SIZE = 6;

    public static boolean started = false;
    public static boolean acquisitionON = false;

    public static void main(String[] args) throws Exception {
        Logger.getLogger("GrovePi").setLevel(Level.WARNING);
        Logger.getLogger("RaspberryPi").setLevel(Level.WARNING);

        GrovePi grovePi = new GrovePi4J();

        // Rotatory
        GroveRotarySensor rotatorySensorLift = new GroveRotarySensor(grovePi, 1);

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
        SensorMonitor<GroveRotaryValue> groveRotaryLiftSensorMonitor = new SensorMonitor<>(rotatorySensorLift, 1);
        SensorMonitor<Double> exitRangerLeftMonitor = new SensorMonitor<>(exitRangerLeft, 1);
        SensorMonitor<Double> exitRangerRightMonitor = new SensorMonitor<>(exitRangerRight, 1);
        SensorMonitor<Double> entryRangerMonitor = new SensorMonitor<>(entryRanger, 1);

        groveRotaryLiftSensorMonitor.start();
        exitRangerLeftMonitor.start();
        exitRangerRightMonitor.start();
        entryRangerMonitor.start();
        Thread.sleep(1000); //Sleep to allow monitor to start

        while (true) {
            GroveRotaryValue liftPrev = groveRotaryLiftSensorMonitor.getValue();
            long liftStartTime = System.currentTimeMillis(); // Initialize startTime
            double entryRangerInitial = entryRangerMonitor.getValue();
            double exitRangerRightInitial = exitRangerLeftMonitor.getValue();
            double exitRangerLeftInitial = exitRangerRightMonitor.getValue();

            Ball_Type prevLiftSpeed = Ball_Type.RED;
            Ball_Type liftSpeed = Ball_Type.RED;

            long blueSpeedTimeStart = 0;

            int blue = 0;
            int red = 0;
            Lot currentLot = new Lot(LOT_SIZE);

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

            while (acquisitionON) {
                if (groveRotaryLiftSensorMonitor.isValid()) {
                    GroveRotaryValue tmp = groveRotaryLiftSensorMonitor.getValue();
                    long liftEndTime = System.currentTimeMillis();
                    // Calculate the time elapsed since the previous reading
                    long elapsedTime = (liftEndTime - liftStartTime) * 1000;
                    double delta = Math.abs(tmp.getDegrees() - liftPrev.getDegrees());
                    if (delta >= 0.3) { //Errore di misura del sensore
                        // Calculate the rotation speed in degrees per second
                        double speed = delta / (double) elapsedTime;
                        if (speed * 10000 > 3) {
                            liftSpeed = Ball_Type.BLUE;
                            lcd1.setText("Lift BLUE speed");
                            lcd1.setRGB(0,0,255);
                        } else {
                            liftSpeed = Ball_Type.RED;
                            lcd1.setText("Lift RED speed");
                            lcd1.setRGB(255,0,0);
                        }

                        if (prevLiftSpeed.equals(Ball_Type.RED) && liftSpeed.equals(Ball_Type.BLUE)) {
                            blueSpeedTimeStart = System.currentTimeMillis();
                            blue++;
                        }

                        //650 Milliseconds
                        if(System.currentTimeMillis() - blueSpeedTimeStart > 650 && liftSpeed.equals(Ball_Type.BLUE)) {
                            blue++;
                            blueSpeedTimeStart = System.currentTimeMillis();
                        }

                        liftPrev = tmp;
                        liftStartTime = System.currentTimeMillis();
                        prevLiftSpeed = liftSpeed;
                    } else
                        lcd1.setText("Not moving");
                }

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

                if(currentLot.done()) {
                    if(liftSpeed.equals(Ball_Type.BLUE)){
                        blue += Math.round(System.currentTimeMillis() - blueSpeedTimeStart / 3.7);
                    }
                    if(blue > LOT_SIZE){
                        blue = LOT_SIZE;
                    }
                    if(LOT_SIZE - blue < red){
                        blue -= (blue - (LOT_SIZE - red));
                    }
                    currentLot.setBalls(blue);
                    InfluxLotPoint.pushLotRotatoryVersion(currentLot);
                    acquisitionON = false;
                }

                Thread.sleep(50);
                exit.set(false);
            }
            entry.set(false);
            Thread.sleep(50);
        }
    }
}
