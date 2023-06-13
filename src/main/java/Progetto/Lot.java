package Progetto;

public class Lot {
    private long startTime;
    private final int size;
    private int cnt = 0; //Counter for balls arrived at the end
    private final double[] timeTaken; //Seconds taken to each ball
    private int blue = 0;
    private int red = 0;
    private int right = 0;
    private int left = 0;

    public Lot(int size){
        this.size = size;
        timeTaken = new double[size];
    }

    public void start(){
        startTime = System.currentTimeMillis();
    }

    public void end(){
        if(cnt >= size)
            return;
        timeTaken[cnt++] = (double) (System.currentTimeMillis() - startTime) / 1000;
    }

    public void rightIncrement(){
        right++;
    }

    public void leftIncrement(){
        left++;
    }

    public void setBalls(int blue){
        this.blue = blue;
        red = size - blue;
    }

    public int getBlueBalls() {
        return blue;
    }

    public int getRedBalls() {
        return red;
    }

    public int getRightBalls() {
        return right;
    }

    public int getLeftBalls() {
        return left;
    }

    public double getAverageTime() {
        double sum = 0;
        for(int i = 0; i < cnt; i++) {
            sum += timeTaken[i];
        }
        return sum / (double) cnt;
    }

    public boolean done(){
        return cnt == size;
    }
}
