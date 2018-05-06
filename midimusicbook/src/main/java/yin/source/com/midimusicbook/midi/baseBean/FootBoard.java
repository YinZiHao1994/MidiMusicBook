
package yin.source.com.midimusicbook.midi.baseBean;

/**
 * Created by ZB-OK on 2017/1/18.
 */
public class FootBoard {

    private int startTime;
    private int startTimeInMills;
    private int endTime;
    private int endTimeInMills;
    private int velocity;
    private TimeSignature timeSignature;

    public FootBoard(int startTime, TimeSignature timeSignature) {
        this.startTime = startTime;
        this.timeSignature = timeSignature;

        float quarterMilliSecond = timeSignature.getTempo() / 1000f;//每个四分音符的毫秒数
        this.startTimeInMills = (int) (quarterMilliSecond * startTime / timeSignature.getQuarter());
    }

    public int getStartTimeInMills() {
        return startTimeInMills;
    }

    public void setStartTimeInMills(int startTimeInMills) {
        this.startTimeInMills = startTimeInMills;
    }

    public int getEndTimeInMills() {
        return endTimeInMills;
    }

    public void setEndTimeInMills(int endTimeInMills) {
        this.endTimeInMills = endTimeInMills;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;

        float quarterMilliSecond = timeSignature.getTempo() / 1000f;//每个四分音符的毫秒数
        this.endTimeInMills = (int) (quarterMilliSecond * endTime / timeSignature.getQuarter());
    }

    public int getVelocity() {
        return velocity;
    }

    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    public TimeSignature getTimeSignature() {
        return timeSignature;
    }

    @Override
    public String toString() {
        return "FootBoard{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", velocity=" + velocity +
                '}';
    }

}
