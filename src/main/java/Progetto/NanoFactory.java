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

import java.util.logging.Level;
import java.util.logging.Logger;

public class NanoFactory {
    public static void main(String[] args) throws Exception {

        Logger.getLogger("GrovePi").setLevel(Level.WARNING);
        Logger.getLogger("RaspberryPi").setLevel(Level.WARNING);

        try (GrovePi grovePi = new GrovePi4J()) {

            // Rotatory
            GroveRotarySensor rotarySensorLift = new GroveRotarySensor(grovePi, 1);
            // Led
            GroveLed entry = new GroveLed(grovePi, 7);
            GroveLed exit = new GroveLed(grovePi, 8);
            // Buzzer
            GroveBuzzer groveBuzzer = new GroveBuzzer(grovePi, 4);
            // UltrasonicRanger
            GroveUltrasonicRanger ultrasonicRangerFirst = new GroveUltrasonicRanger(grovePi, 2);
            GroveUltrasonicRanger ultrasonicRangerSecond = new GroveUltrasonicRanger(grovePi, 3);
            // LCD
            GroveRgbLcd lcd1 = grovePi.getLCD();
            GroveRgbLcd lcd2 = grovePi.getLCD();

            // SensorMonitor
            SensorMonitor<GroveRotaryValue> rotatoryLiftSensor = new SensorMonitor<>(rotarySensorLift, 100);
            SensorMonitor<Double> doubleUltrasonicFirst = new SensorMonitor<>(ultrasonicRangerFirst, 100);
            SensorMonitor<Double> doubleUltrasonicSecond = new SensorMonitor<>(ultrasonicRangerSecond, 100);

            while(true) {

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
