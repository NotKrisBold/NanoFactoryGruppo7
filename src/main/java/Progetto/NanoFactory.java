package Progetto;

import org.iot.raspberry.grovepi.GrovePi;
import org.iot.raspberry.grovepi.pi4j.GrovePi4J;
import org.iot.raspberry.grovepi.sensors.analog.GroveRotarySensor;
import org.iot.raspberry.grovepi.sensors.data.GroveRotaryValue;
import org.iot.raspberry.grovepi.sensors.digital.GroveBuzzer;
import org.iot.raspberry.grovepi.sensors.digital.GroveLed;
import org.iot.raspberry.grovepi.sensors.digital.GroveUltrasonicRanger;
import org.iot.raspberry.grovepi.sensors.i2c.GroveRgbLcd;
import org.iot.raspberry.grovepi.sensors.synch.SensorMonitor;

import java.io.IOException;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NanoFactory {

    public static volatile boolean acquisitionON = true;

    public static void main(String[] args) throws Exception {
        Logger.getLogger("GrovePi").setLevel(Level.WARNING);
        Logger.getLogger("RaspberryPi").setLevel(Level.WARNING);

        GrovePi grovePi = new GrovePi4J();

        // Rotatory
        GroveRotarySensor rotatorySensorLift = new GroveRotarySensor(grovePi, 1);
        GroveRotarySensor rotatorySensorStart = new GroveRotarySensor(grovePi, 2);
        // Led
        GroveLed entry = new GroveLed(grovePi, 7);
        GroveLed exit = new GroveLed(grovePi, 8);

        // UltrasonicRanger
        GroveUltrasonicRanger ultrasonicRangerFirst = new GroveUltrasonicRanger(grovePi, 2);
        GroveUltrasonicRanger ultrasonicRangerSecond = new GroveUltrasonicRanger(grovePi, 3);
        // LCD
        GroveRgbLcd lcd1 = grovePi.getLCD();

        // SensorMonitor
        SensorMonitor<GroveRotaryValue> groveRotaryLiftSensorMonitor = new SensorMonitor<>(rotatorySensorLift, 10);
        SensorMonitor<GroveRotaryValue> groveRotaryStartSensorMonitor = new SensorMonitor<>(rotatorySensorStart, 10);
        SensorMonitor<Double> ultrasonicRangerFirstMonitor = new SensorMonitor<>(ultrasonicRangerFirst, 10);
        SensorMonitor<Double> ultrasonicRangerSecondMonitor = new SensorMonitor<>(ultrasonicRangerSecond, 10);

        groveRotaryStartSensorMonitor.start();
        groveRotaryLiftSensorMonitor.start();
        ultrasonicRangerFirstMonitor.start();
        ultrasonicRangerSecondMonitor.start();
        Thread.sleep(1000); //Sleep to allow monitor to start
        // Init
        GroveRotaryValue startPrev = groveRotaryStartSensorMonitor.getValue();
        GroveRotaryValue liftPrev = groveRotaryLiftSensorMonitor.getValue();
        long startTime = System.currentTimeMillis(); // Initialize startTime
        while (true){
            if (groveRotaryLiftSensorMonitor.isValid()) {
                GroveRotaryValue tmp = groveRotaryLiftSensorMonitor.getValue();
                long endTime = System.currentTimeMillis();
                // Calculate the time elapsed since the previous reading
                long elapsedTime = (endTime - startTime) * 1000;
                double delta = Math.abs(tmp.getDegrees() - liftPrev.getDegrees());
                if(delta >= 0.3) { //Errore di misura del sensore
                    // Calculate the rotation speed in degrees per second
                    double speed = delta / elapsedTime;
                    System.out.println("Velocity: " + speed);
                    liftPrev = tmp;
                    startTime = System.currentTimeMillis();
                }
                else
                    System.out.println("Lift not moving");
            }

            if (groveRotaryStartSensorMonitor.isValid()) {
                GroveRotaryValue tmp = groveRotaryStartSensorMonitor.getValue();
                // Se il valore attuale è diverso dal precedente le palline sono passate
                if (tmp.getDegrees() - startPrev.getDegrees() > 0.3){
                    System.out.println("Palline passate");
                    try {
                        // Led lampeggia
                        entry.set(true);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    startPrev = groveRotaryStartSensorMonitor.getValue();
                }
                try {
                    // Led spento
                    entry.set(false);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        }

            if (ultrasonicRangerFirstMonitor.isValid()) {
                if(ultrasonicRangerFirstMonitor.getValue() < 10) {
                    System.out.println(ultrasonicRangerFirstMonitor.getValue() + "cm");
                    exit.set(true);
                }
            }

            if (ultrasonicRangerSecondMonitor.isValid()) {
                if(ultrasonicRangerSecondMonitor.getValue() < 10) {
                    System.out.println(ultrasonicRangerSecondMonitor.getValue() + " cm");
                    exit.set(true);
                }
            }

            Thread.sleep(50);
            exit.set(false);
        }
    }
}