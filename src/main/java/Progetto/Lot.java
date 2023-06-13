package Progetto;

public class Lot {
    private long startTime;
    private final int size;
    private final long[] timeTaken;
    private int blue = 0;
    private int red = 0;
    private int right = 0;
    private int left = 0;
    private int cnt = 0;

    public Lot(int size){
        this.size = size;
        timeTaken = new long[size];
    }

    public void start(){
        startTime = System.currentTimeMillis();
    }

    public void end(){
        if(cnt >= size)
            return;
        timeTaken[cnt++] = System.currentTimeMillis() - startTime;
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

    public long getAvarageTime(){
        long sum = 0;
        for(int i = 0; i < cnt; i++){
            sum += timeTaken[i];
        }
        return sum / (long) cnt;
    }

    public boolean done(){
        return cnt == size;
    }
}
