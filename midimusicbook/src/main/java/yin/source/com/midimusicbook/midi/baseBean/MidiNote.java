package yin.source.com.midimusicbook.midi.baseBean;

import android.support.annotation.NonNull;


/**
 * @class MidiNote
 * A MidiNote contains
 * <p/>
 * pulsesOfStartTime - The time (measured in pulses) when the note is pressed.
 * channel   - The channel the note is from.  This is used when matching
 * NoteOff events with the corresponding NoteOn event.
 * The channels for the NoteOn and NoteOff events must be
 * the same.
 * noteNumber - The note number, from 0 to 127.  Middle C is 60.
 * duration  - The time duration (measured in pulses) after which the
 * note is released.
 * <p/>
 * A MidiNote is created when we encounter a NoteOff event.  The duration
 * is initially unknown (set to 0).  When the corresponding NoteOff event
 * is found, the duration is set by the method NoteOff().
 * 当我们遇到一个NoteOff事件时，创建一个MidiNote。 持续时间最初未知（设置为0）。
 * 当发现对应的NoteOff事件时，持续时间由方法NoteOff（）设置。
 */
public class MidiNote implements Comparable<MidiNote> {


    private boolean havePlayed;//此音符是否已经播放的标识

    private boolean haveReadyPlayed;//此音符是否已经 准备播放的标识

//    private int pulsesOfQuarterNote;
//    /**
//     * Number of pulses per quarter note
//     * 每个四分之一音符的脉冲数(此属性应该从 TimeSignature 中获取)
//     */

    private int pulsesOfStartTime;// 开始时间,脉冲形式
    /**
     * The start time, in pulses
     */

    private int startTimeInMilliSecond;//毫秒为单位的开始时间

    private int channel;// 通道
    /**
     * The channel
     */
    private int noteNumber;// 调子,从0到127
    /**
     * The note, from 0 to 127. Middle C is 60
     */
    private int duration;// 不算踏板的音符本身持续的时间,脉冲格式

    private int durationInMilliSecond;//毫秒为单位的持续时间

    //踏板作用脉冲时间，从音符结束之后开始算起的额外时间，单位:脉冲
    private int pedalActionTime = 0;

    //踏板作用时间，从音符结束之后开始算起的额外时间，单位:毫秒
    private int pedalActionTimeInMilliS = 0;

    private int velocity;// 力度

//    private int tempo;// 每个四分音符的微秒数(此属性应该从 TimeSignature 中获取)


    /**
     * The duration, in pulses
     */
    /* Create a new MidiNote.  This is called when a NoteOn event is
     * encountered in the MidiFile.
     */
    public MidiNote(int pulsesOfStartTime, int channel, int noteNumber, int duration) {
        this.pulsesOfStartTime = pulsesOfStartTime;
        this.channel = channel;
        this.noteNumber = noteNumber;
        this.duration = duration;
    }

//    public int getTempo() {
//        return tempo;
//    }
//
//    public void setTempo(int tempo) {
//        this.tempo = tempo;
//    }



    public int getPedalActionTimeInMilliS() {
        return pedalActionTimeInMilliS;
    }

    public void setPedalActionTimeInMilliS(int pedalActionTimeInMilliS) {
        this.pedalActionTimeInMilliS = pedalActionTimeInMilliS;
    }

    public boolean isHavePlayed() {
        return havePlayed;
    }

    public void setHavePlayed(boolean havePlayed) {
        this.havePlayed = havePlayed;
    }

//    public int getPulsesOfQuarterNote() {
//        return pulsesOfQuarterNote;
//    }
//
//    public void setPulsesOfQuarterNote(int pulsesOfQuarterNote) {
//        this.pulsesOfQuarterNote = pulsesOfQuarterNote;
//    }

    public int getDurationInMilliSecond() {
        return durationInMilliSecond;
    }

    public void setDurationInMilliSecond(int durationInSecond) {
        this.durationInMilliSecond = durationInSecond;
    }

    public int getStartTimeInMilliSecond() {
        return startTimeInMilliSecond;
    }

    public void setStartTimeInMilliSecond(int startTimeInMilliSecond) {
        this.startTimeInMilliSecond = startTimeInMilliSecond;
    }

    public int getPedalActionTime() {
        return pedalActionTime;
    }

    public void setPedalActionTime(int pedalActionTime) {
        this.pedalActionTime = pedalActionTime;
    }

    public int getVelocity() {
        return velocity;
    }

    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    public int getPulsesOfStartTime() {
        return pulsesOfStartTime;
    }

    public void setPulsesOfStartTime(int value) {
        pulsesOfStartTime = value;
    }

    public int getEndTime() {
        return pulsesOfStartTime + duration;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int value) {
        channel = value;
    }

    public int getNoteNumber() {
        return noteNumber;
    }

    public void setNoteNumber(int value) {
        noteNumber = value;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int value) {
        duration = value;
    }

    public boolean isHaveReadyPlayed() {
        return haveReadyPlayed;
    }

    public void setHaveReadyPlayed(boolean haveReadyPlayed) {
        this.haveReadyPlayed = haveReadyPlayed;
    }

    /* A NoteOff event occurs for this note at the given time.
         * Calculate the note duration based on the noteoff event.
         */
    public void NoteOff(int endtime) {
        duration = endtime - pulsesOfStartTime;
    }


    public MidiNote Clone() {
        return new MidiNote(pulsesOfStartTime, channel, noteNumber, duration);
    }

//    @Override
//    public String toString() {
//        String[] scale = new String[]{"A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#"};
//        return String.format("MidiNote channel=%1$s number=%2$s %3$s start=%4$s duration=%5$s",
//                channel, noteNumber, scale[(noteNumber + 3) % 12], pulsesOfStartTime, duration);
//
//    }


    @Override
    public String toString() {
        return "MidiNote{" +
                ", havePlayed=" + havePlayed +
                ", pulsesOfStartTime=" + pulsesOfStartTime +
                ", startTimeInMilliSecond=" + startTimeInMilliSecond +
                ", channel=" + channel +
                ", noteNumber=" + noteNumber +
                ", duration=" + duration +
                ", durationInMilliSecond=" + durationInMilliSecond +
                ", pedalActionTime=" + pedalActionTime +
                ", pedalActionTimeInMilliS=" + pedalActionTimeInMilliS +
                ", velocity=" + velocity +
                '}';
    }

    @Override
    public int compareTo(@NonNull MidiNote o) {
        return getPulsesOfStartTime() - o.getPulsesOfStartTime();
    }
}
