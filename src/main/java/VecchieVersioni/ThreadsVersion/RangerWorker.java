package VecchieVersioni.ThreadsVersion;

import org.iot.raspberry.grovepi.sensors.digital.GroveUltrasonicRanger;

import java.io.IOException;

public class RangerWorker implements Runnable{
    private static final long REFRESH_RATE = 1;
    private final GroveUltrasonicRanger ranger;
    private boolean ballPassed;

    public RangerWorker(GroveUltrasonicRanger ranger) {
        this.ranger = ranger;
        ballPassed = false;
    }

    @Override
    public void run() {
        while(NanoFactory.acquisitionON){
            System.out.println(ballPassed);
            try {
                if(ranger.get() < 10){
                    ballPassed = true;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized boolean isBallPassed() {
        if(!ballPassed)
            return false;
        ballPassed = false;
        return true;
    }
}
