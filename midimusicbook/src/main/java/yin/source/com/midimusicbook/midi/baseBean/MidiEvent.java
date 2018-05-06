package yin.source.com.midimusicbook.midi.baseBean;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @class MidiEvent
 * A MidiEvent represents a single event (such as EventNoteOn) in the
 * Midi file. It includes the delta time of the event.
 * MidiEvent表示Midi文件中的单个事件（例如EventNoteOn）。 它包括事件的增量时间。
 */
public class MidiEvent implements Comparator<MidiEvent> {

    public int DeltaTime;// 现在这个事件距离上一个事件的时间,单位:tick
    /**
     * The time between the previous event and this on
     */
    public int StartTime;// 这个事件开始的绝对时间,单位:tick
    /**
     * The absolute time this event occurs
     */
    public boolean HasEventflag;// 如果使用之前的事件标记则为false
    /**
     * False if this is using the previous eventflag
     */
    public byte EventFlag;// 事件类型
    /**
     * NoteOn, NoteOff, etc.  Full list is in class MidiFile
     */
    public byte Channel;// 这个事件出现的通道
    /**
     * The channel this event occurs on
     */
    public byte Notenumber;// 调子(音符)位置
    /**
     * The note number
     */
    public byte Velocity;// 力度
    /**
     * The volume of the note
     */
    public byte Instrument;// 乐器编号
    /**
     * The instrument
     */
    public byte KeyPressure;// 按键压力
    /**
     * The key pressure
     */
    public byte ChanPressure;// 通道触动压力
    /**
     * The channel pressure
     */
    public byte ControlNum;// 控制器号
    /**
     * The controller number
     */
    public byte ControlValue;// 控制器值
    /**
     * The controller value
     */
    public short PitchBend;// 音高
    /**
     * The pitch bend value
     */
    public byte Numerator;// 分子,对于拍子记号元事件
    /**
     * The numerator, for TimeSignature meta events
     */
    public byte Denominator;// 分母,对于拍子记号元事件
    /**
     * The denominator, for TimeSignature meta events
     */
    public int Tempo;// 拍子,对于拍子元事件
    /**
     * The tempo, for Tempo meta events
     */
    public byte Metaevent;// 元事件,如果事件标记为元事件
    /**
     * The metaevent, used if eventflag is MetaEvent
     */
    public int Metalength;// 元事件长度
    /**
     * The metaevent length
     */
    public byte[] Value;// 不成熟的值,对于系统和元事件
    /**
     * The raw byte value, for Sysex and meta events
     */

    public MidiEvent() {
    }

    /**
     * Return a copy of this event
     */
    public MidiEvent Clone() {
        MidiEvent mevent = new MidiEvent();
        mevent.DeltaTime = DeltaTime;
        mevent.StartTime = StartTime;
        mevent.HasEventflag = HasEventflag;
        mevent.EventFlag = EventFlag;
        mevent.Channel = Channel;
        mevent.Notenumber = Notenumber;
        mevent.Velocity = Velocity;
        mevent.Instrument = Instrument;
        mevent.KeyPressure = KeyPressure;
        mevent.ChanPressure = ChanPressure;
        mevent.ControlNum = ControlNum;
        mevent.ControlValue = ControlValue;
        mevent.PitchBend = PitchBend;
        mevent.Numerator = Numerator;
        mevent.Denominator = Denominator;
        mevent.Tempo = Tempo;
        mevent.Metaevent = Metaevent;
        mevent.Metalength = Metalength;
        mevent.Value = Value;
        return mevent;
    }

    /**
     * Compare two MidiEvents based on their start times.
     */
    public int compare(MidiEvent x, MidiEvent y) {
        if (x.StartTime == y.StartTime) {
            if (x.EventFlag == y.EventFlag) {
                return x.Notenumber - y.Notenumber;
            } else {
                return x.EventFlag - y.EventFlag;
            }
        } else {
            return x.StartTime - y.StartTime;
        }
    }

    @Override
    public String toString() {
        return "MidiEvent{" +
                "DeltaTime=" + DeltaTime +
                ", StartTime=" + StartTime +
                ", HasEventflag=" + HasEventflag +
                ", EventFlag=" + EventFlag +
                ", Channel=" + Channel +
                ", Notenumber=" + Notenumber +
                ", Velocity=" + Velocity +
                ", Instrument=" + Instrument +
                ", KeyPressure=" + KeyPressure +
                ", ChanPressure=" + ChanPressure +
                ", ControlNum=" + ControlNum +
                ", ControlValue=" + ControlValue +
                ", PitchBend=" + PitchBend +
                ", Numerator=" + Numerator +
                ", Denominator=" + Denominator +
                ", Tempo=" + Tempo +
                ", Metaevent=" + Metaevent +
                ", Metalength=" + Metalength +
                ", Value=" + Arrays.toString(Value) +
                '}';
    }
}
