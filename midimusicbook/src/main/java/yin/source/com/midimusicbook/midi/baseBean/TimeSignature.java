package yin.source.com.midimusicbook.midi.baseBean;

import java.io.Serializable;

import yin.source.com.midimusicbook.exception.MidiFileException;

/**
 * @class TimeSignature
 * The TimeSignature class represents
 * - The time signature of the song, such as 4/4, 3/4, or 6/8 time, and
 * - The number of pulses per quarter note // 每个四分之一音符的脉冲数
 * - The number of microseconds per quarter note // 每个四分之一音符的微秒数
 * <p/>
 * In midi files, all time is measured in "pulses".  Each note has
 * a start time (measured in pulses), and a duration (measured in
 * pulses).  This class is used mainly to convert pulse durations
 * (like 120, 240, etc) into note durations (half, quarter, eighth, etc).
 * 在midi文件中，所有的时间都是以“脉冲”计量的。每一个音符有一个开始时间(以脉冲为单位)和一个音长(以脉冲为单位)。
 * 这个类主要是用来将脉冲音长(比如120、240等等)转化为音符音长(半拍、四分之一拍、八分之一拍等等)
 */

public class TimeSignature implements Serializable {
    /**
     * Numerator of the time signature
     * 拍子记号的分子
     */
    private int numerator;
    /**
     * Denominator of the time signature
     * 拍子记号的分母
     */
    private int denominator;
    /**
     * Number of pulses per quarter note
     * 每个四分之一音符的脉冲数
     */
    private int pulsesOfQuarterNote;
    /**
     * Number of pulses per measure 每次测量的脉冲数(猜测是每一小节的脉冲数)
     */
    private int measure;
    /**
     * Number of microseconds per quarter note
     */
    private int tempo;// 每个四分之一小节的微秒数

    /**
     * Create a new time signature, with the given numerator,
     * denominator, pulses per quarter note, and tempo.
     */
    public TimeSignature(int numerator, int denominator, int pulsesOfQuarterNote, int tempo) {
        if (numerator <= 0 || denominator <= 0 || pulsesOfQuarterNote <= 0) {
            throw new MidiFileException("Invalid time signature", 0);
        }

        /**
         *  Midi File gives wrong time signature sometimes 有时候Midi文件会提供错误的拍子记号
         */
        if (numerator == 5) {
            numerator = 4;
        }
        this.numerator = numerator;
        this.denominator = denominator;
        this.pulsesOfQuarterNote = pulsesOfQuarterNote;
        this.tempo = tempo;
        int beat;
        if (denominator < 4)
            //pulsesOfQuarterNote 每个四分之一音符的脉冲数
            beat = pulsesOfQuarterNote * 2;
        else
            beat = pulsesOfQuarterNote / (denominator / 4);
        //measure 每次测量的脉冲数
        measure = numerator * beat;
//        Log.i("TimeS numerator:", numerator + "");
//        Log.i("TimeS denominator:", denominator + "");
//        Log.i("TimeS pulsesOfQuarterNote:", pulsesOfQuarterNote + "");
//        Log.i("TimeS tempo:", tempo + "");
//        Log.i("TimeS beat:", beat + "");
//        Log.i("TimeS measure:", measure + "");
    }

    /**
     * Convert a note duration into a stem duration.  Dotted durations
     * are converted into their non-dotted equivalents.
     * 将音符持续时间转换为句子持续时间。 虚线持续时间被转换成它们的非虚线等价物。
     */
    public static NoteDuration GetStemDuration(NoteDuration dur) {
        if (dur == NoteDuration.DottedHalf)
            return NoteDuration.Half;
        else if (dur == NoteDuration.DottedQuarter)
            return NoteDuration.Quarter;
        else if (dur == NoteDuration.DottedEighth)
            return NoteDuration.Eighth;
        else
            return dur;
    }

    /**
     * Get the numerator of the time signature // 拍子记号的分子
     */
    public int getNumerator() {
        return numerator;
    }

    /**
     * Get the denominator of the time signature
     */ // 拍子记号的分母
    public int getDenominator() {
        return denominator;
    }

    /**
     * Get the number of pulses per quarter note
     */
    public int getQuarter() {
        return pulsesOfQuarterNote;
    }

    /**
     * Get the number of pulses per measure
     */ // 每一小节的节拍数量
    public int getMeasure() {
        return measure;
    }

    /**
     * Get the number of microseconds per quarter note
     */ // 每个四分之一音符的微秒数
    public int getTempo() {
        return tempo;
    }

    /**
     * Return which measure the given time (in pulses) belongs to.
     * 返回所给的时间所属的小节
     */
    public int GetMeasure(int time) {
        return time / measure;
    }

    /**
     * Given a duration in pulses, return the closest note duration.
     * 给定脉冲持续时间，返回最近的音符持续时间。
     */
    public NoteDuration GetNoteDuration(int duration) {
        int whole = pulsesOfQuarterNote * 4;

        /**
         1       = 32/32
         3/4     = 24/32
         1/2     = 16/32
         3/8     = 12/32
         1/4     =  8/32
         3/16    =  6/32
         1/8     =  4/32 =    8/64
         triplet         = 5.33/64
         1/16    =  2/32 =    4/64
         1/32    =  1/32 =    2/64
         **/

        if (duration >= 28 * whole / 32)
            return NoteDuration.Whole;
        else if (duration >= 20 * whole / 32)
            return NoteDuration.DottedHalf;
        else if (duration >= 14 * whole / 32)
            return NoteDuration.Half;
        else if (duration >= 10 * whole / 32)
            return NoteDuration.DottedQuarter;
        else if (duration >= 7 * whole / 32)
            return NoteDuration.Quarter;
        else if (duration >= 5 * whole / 32)
            return NoteDuration.DottedEighth;
        else if (duration >= 6 * whole / 64)
            return NoteDuration.Eighth;
        else if (duration >= 5 * whole / 64)
            return NoteDuration.Triplet;
        else if (duration >= 3 * whole / 64)
            return NoteDuration.Sixteenth;
        else
            return NoteDuration.ThirtySecond;
    }

    /**
     * Return the time period (in pulses) the the given duration spans // 返回拍子乐节,在给定的范围
     */
    public int DurationToTime(NoteDuration dur) {
        int eighth = pulsesOfQuarterNote / 2;
        int sixteenth = eighth / 2;

        switch (dur) {
            case Whole:
                return pulsesOfQuarterNote * 4;
            case DottedHalf:
                return pulsesOfQuarterNote * 3;
            case Half:
                return pulsesOfQuarterNote * 2;
            case DottedQuarter:
                return 3 * eighth;
            case Quarter:
                return pulsesOfQuarterNote;
            case DottedEighth:
                return 3 * sixteenth;
            case Eighth:
                return eighth;
            case Triplet:
                return pulsesOfQuarterNote / 3;
            case Sixteenth:
                return sixteenth;
            case ThirtySecond:
                return sixteenth / 2;
            default:
                return 0;
        }
    }

    @Override
    public String toString() {
        return String.format("TimeSignature=%1$s/%2$s quarter=%3$s tempo=%4$s",
                numerator, denominator, pulsesOfQuarterNote, tempo);
    }

}
