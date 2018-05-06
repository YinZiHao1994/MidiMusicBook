package yin.source.com.midimusicbook.midi.baseBean;

/**
 * Created by ZB-OK on 2016/12/22.
 */

import android.app.Activity;
import android.net.Uri;
import android.util.Log;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import yin.source.com.midimusicbook.exception.MidiFileException;
import yin.source.com.midimusicbook.utils.IOUtil;

/**
 * @class Pair - A pair of ints
 */
class PairInt {
    public int low;
    public int high;
}


/**
 * MIDI file format.
 * <p/>
 * The Midi File format is described below.  The description uses
 * the following abbreviations.
 * 下面描述了Midi文件的样式.使用下面的缩写描述
 * <p/>
 * u1     - One byte // 一个byte
 * u2     - Two bytes (big endian) // 两个byte
 * u4     - Four bytes (big endian) // 4个byte
 * varlen - A variable length integer, that can be 1 to 4 bytes. The
 * integer ends when you encounter a byte that doesn't have
 * the 8th bit set (a byte less than 0x80).
 * 可变长度的整形，字节长度从一到四。当你遇到一个小于0x80的byte后，这个整形结束
 * len?   - The length of the data depends on some code
 * 数据的长度取决于一些代码
 * <p/>
 * <p/>
 * <p/>
 * The Midi files begins with the main Midi header
 * u4 = The four ascii characters 'MThd' // 4个ASCII码(MThd)
 * u4 = The length of the MThd header = 6 bytes MThd头的长度为4
 * u2 = 0 if the file contains a single track 单音轨为1，
 * 1 if the file contains one or more simultaneous tracks 有一个或多个同事的音轨为1
 * 2 if the file contains one or more independent tracks 有一个或多个独立的音轨为2
 * u2 = number of tracks // 音轨数量
 * u2 = if >  0, the number of pulses per quarter note // 每个四分之一音符的脉冲数量(大于0的时候)
 * if <= 0, then ???
 * <p/>
 * Next come the individual Midi tracks.  The total number of Midi
 * tracks was given above, in the MThd header.  Each track starts
 * with a header:接下来是独立的音轨.音轨的总数上边已经给了.每个音轨有一个头
 * <p/>
 * u4 = The four ascii characters 'MTrk' // 四个ASCII码(MTrk)
 * u4 = Amount of track data, in bytes. // 音轨数据的字节长度
 * <p/>
 * The track data consists of a series of Midi events.  Each Midi event
 * has the following format:音轨数据包含一系列的midi事件.每个midi事件有以下格式
 * <p/>
 * varlen  - The time between the previous event and this event, measured
 * in "pulses".  The number of pulses per quarter note is given
 * in the MThd header.前一个时间和当前时间的时间，脉冲为单位.每一个四分之一音符的的脉冲数在Mthd头中给了
 * u1      - The Event code, always betwee 0x80 and 0xFF 事件码，总是介于0x80和0xFF之间
 * len?    - The event data.  The length of this data is determined by the
 * event code.  The first byte of the event data is always < 0x80.
 * 这个数据的长度取决于事件码。数据的第一个字节总是小于0x80
 * <p/>
 * The event code is optional.  If the event code is missing, then it
 * defaults to the previous event code.  For example:
 * 事件码是可以选择的.如果事件码没有，那么默认是前一个事件码
 * <p/>
 * varlen, eventcode1, eventdata,
 * varlen, eventcode2, eventdata,
 * varlen, eventdata,  // eventcode is eventcode2
 * varlen, eventdata,  // eventcode is eventcode2
 * varlen, eventcode3, eventdata,
 * ....
 * <p/>
 * How do you know if the eventcode is there or missing? Well:你怎样知道事件码存在还是丢失
 * - All event codes are between 0x80 and 0xFF 所有的事件码都介于0x80和0xFF之间
 * - The first byte of eventdata is always less than 0x80. 事件数据的第一个字节总是小于0x80
 * So, after the varlen delta time, if the next byte is between 0x80 因此如果在时间之后接下来的字节介于0x80和0xFF之间，
 * and 0xFF, its an event code.  Otherwise, its event data. 他就是一个事件码。否则，他就是一个时间数据
 * <p/>
 * The Event codes and event data for each event code are shown below. 每一个事件的事件码和事件数据展示在下边
 * <p/>
 * Code:  u1 - 0x80 thru 0x8F - Note Off event. 0x80到0x8F音符关
 * 0x80 is for channel 1, 0x8F is for channel 16. 0x80是通道一，0x8F是通道16
 * Data:  u1 - The note number, 0-127.  Middle C is 60 (0x3C)  音符数字，0-127、中央C是60
 * u1 - The note velocity.  This should be 0  音符按键力度。应该为0
 * <p/>
 * Code:  u1 - 0x90 thru 0x9F - Note On event. 0x90到0x9F音符关
 * 0x90 is for channel 1, 0x9F is for channel 16. 0x90是通道一，0x9F是通道16
 * Data:  u1 - The note number, 0-127.  Middle C is 60 (0x3C) 音符数字，0-127、中央C是60
 * u1 - The note velocity, from 0 (no sound) to 127 (loud). 音符按键力度，从0到到127
 * A value of 0 is equivalent to a Note Off.按键力度为零等同于音符关闭
 * <p/>
 * Code:  u1 - 0xA0 thru 0xAF - Key Pressure 按键力度
 * Data:  u1 - The note number, 0-127. // 音符数字从0到127
 * u1 - The pressure. // 压力值
 * <p/>
 * Code:  u1 - 0xB0 thru 0xBF - Control Change // 控制变更(0xB0到0xBF)
 * Data:  u1 - The controller number // 控制器
 * u1 - The value // 控制值
 * <p/>
 * Code:  u1 - 0xC0 thru 0xCF - Program Change // 指令变更(0xC0到0xCF)
 * Data:  u1 - The program number. // 指令值
 * <p/>
 * Code:  u1 - 0xD0 thru 0xDF - Channel Pressure // 通道压力(0xD0到0xDF)
 * u1 - The pressure. // 压力值
 * <p/>
 * Code:  u1 - 0xE0 thru 0xEF - Pitch Bend // 音高的滑动(0xE0到0xEF)
 * Data:  u2 - Some data
 * <p/>
 * Code:  u1     - 0xFF - Meta Event // 元事件
 * Data:  u1     - Metacode // 元事件码
 * varlen - Length of meta event // 元事件的长度
 * u1[varlen] - Meta event data. // 元事件数据
 * <p/>
 * <p/>
 * The Meta Event codes are listed below: 元事件数据排列在下方
 * <p/>
 * Metacode: u1         - 0x0  Sequence Number // 序列号
 * varlen     - 0 or 2 // 0或者2
 * u1[varlen] - Sequence number 序列号
 * <p/>
 * Metacode: u1         - 0x1  Text // 文本
 * varlen     - Length of text // 文本长度
 * u1[varlen] - Text // 文本
 * <p/>
 * Metacode: u1         - 0x2  Copyright // 版权
 * varlen     - Length of text // 长度
 * u1[varlen] - Text // 文字
 * <p/>
 * Metacode: u1         - 0x3  Track Name // 音轨名称
 * varlen     - Length of name // 名称长度
 * u1[varlen] - Track Name // 音轨名
 * <p/>
 * Metacode: u1         - 0x58  Time Signature // 拍子记号
 * varlen     - 4 // 长度为四
 * u1         - numerator // 分子
 * u1         - log2(denominator) // 分母
 * u1         - clocks in metronome click // 节拍器中的时钟
 * u1         - 32nd notes in quarter note (usually 8)
 * <p/>
 * Metacode: u1         - 0x59  Key Signature // 音调符号
 * varlen     - 2 长度为2
 * u1         - if >= 0, then number of sharps // 如果大于等于0.然后高音数
 * if < 0, then number of flats * -1 // 如果小于0.然后平音*-1
 * u1         - 0 if major key 如果是大调则为0
 * 1 if minor key 如果是小调则为1
 * <p/>
 * Metacode: u1         - 0x51  Tempo // 拍子
 * varlen     - 3
 * u3         - quarter note length in microseconds 四人之一音符的长度(毫秒为到位)
 */


/**
 * @class MidiFile
 * <p/>
 * The MidiFile class contains the parsed data from the Midi File.MidiFile类包含以下Midi文件中解析出来的数据
 * It contains:
 * - All the tracks in the midi file, including all MidiNotes per track. // midi文件中的所有音轨,包含每条音轨中的所有音符
 * - The time signature (e.g. 4/4, 3/4, 6/8) // 拍子记号(4/4,3/4,6/8)
 * - The number of pulses per quarter note. // 每一个四分之一音符的脉冲数
 * - The tempo (number of microseconds per quarter note). // 拍子(每一个四分之一音符的微秒数)
 * <p/>
 * The constructor takes a fileName as input, and upon returning,
 * contains the parsed data from the midi file. // 构造函数将文件名作为输入，返回时包含来自midi文件的解析数据。
 * <p/>
 * The methods readTrack() and ReadMetaEvent() are helper functions called
 * readTrack()和ReadMeteEvent()方法作为帮助函数在构造函数解析式被调用
 * by the constructor during the parsing.
 * <p/>
 * After the MidiFile is parsed and created, the user can retrieve the
 * tracks and notes by using the property Tracks and Tracks.Notes.
 * 在MidiFile被解析和创建后,用户可以通过合适的音轨和音符检索音轨和音符
 * <p/>
 * There are two methods for modifying the midi data based on the menu
 * options selected:
 * 现在有两种方法可以给予菜单选择项修改midi数据
 * <p/>
 * - ChangeMidiNotes() // 修改midi音符
 * Apply the menu options to the parsed MidiFile.  This uses the helper functions:
 * 使用菜单设置项去解析MidiFile.这个方法使用以下帮助函数
 * SplitTrack() // 分离音轨
 * CombineToTwoTracks() // 合并两条音轨
 * ShiftTime() // 时间偏移
 * Transpose() // 变调演奏、移调
 * RoundStartTimes() // 四舍五入开始时间
 * RoundDurations() // 四舍五入播放时长
 * <p/>
 * - ChangeSound()
 * Apply the menu options to the MIDI music data, and save the modified midi data
 * to a file, for playback. 将菜单选项应用到MIDI音乐数据,并将修改后的midi数据保存到一份文件中，用来重放
 */

public class MidiFile {

    /**
     * True if we've split each channel into a track  如果我们将每个频道分成一个轨道则true
     */


    /* The list of Midi Events */ // midi事件列表
    public static final byte EventNoteOff = (byte) 0x80;
    public static final byte EventNoteOn = (byte) 0x90;
    public static final byte EventKeyPressure = (byte) 0xA0;
    public static final byte EventControlChange = (byte) 0xB0;
    public static final byte EventProgramChange = (byte) 0xC0;
    public static final byte EventChannelPressure = (byte) 0xD0;
    public static final byte EventPitchBend = (byte) 0xE0;
    public static final byte SysexEvent1 = (byte) 0xF0;
    public static final byte SysexEvent2 = (byte) 0xF7;
    public static final byte MetaEvent = (byte) 0xFF;
    /* The list of Meta Events */ // meta事件列表
    public static final byte MetaEventSequence = (byte) 0x0;
    public static final byte MetaEventText = (byte) 0x1;
    public static final byte MetaEventCopyright = (byte) 0x2;
    public static final byte MetaEventSequenceName = (byte) 0x3;
    public static final byte MetaEventInstrument = (byte) 0x4;
    public static final byte MetaEventLyric = (byte) 0x5;
    public static final byte MetaEventMarker = (byte) 0x6;
    public static final byte MetaEventEndOfTrack = (byte) 0x2F;
    public static final byte MetaEventTempo = (byte) 0x51;
    public static final byte MetaEventSMPTEOffset = (byte) 0x54;
    public static final byte MetaEventTimeSignature = (byte) 0x58;
    public static final byte MetaEventKeySignature = (byte) 0x59;
    /* The Program Change event gives the instrument that should
     * be used for a particular channel.  The following table
     * maps each instrument number (0 thru 128) to an instrument
     * name.
     */
    public static String[] Instruments = {
            "Acoustic Grand Piano",
            "Bright Acoustic Piano",
            "Electric Grand Piano",
            "Honky-tonk Piano",
            "Electric Piano 1",
            "Electric Piano 2",
            "Harpsichord",
            "Clavi",
            "Celesta",
            "Glockenspiel",
            "Music Box",
            "Vibraphone",
            "Marimba",
            "Xylophone",
            "Tubular Bells",
            "Dulcimer",
            "Drawbar Organ",
            "Percussive Organ",
            "Rock Organ",
            "Church Organ",
            "Reed Organ",
            "Accordion",
            "Harmonica",
            "Tango Accordion",
            "Acoustic Guitar (nylon)",
            "Acoustic Guitar (steel)",
            "Electric Guitar (jazz)",
            "Electric Guitar (clean)",
            "Electric Guitar (muted)",
            "Overdriven Guitar",
            "Distortion Guitar",
            "Guitar harmonics",
            "Acoustic Bass",
            "Electric Bass (finger)",
            "Electric Bass (pick)",
            "Fretless Bass",
            "Slap Bass 1",
            "Slap Bass 2",
            "Synth Bass 1",
            "Synth Bass 2",
            "Violin",
            "Viola",
            "Cello",
            "Contrabass",
            "Tremolo Strings",
            "Pizzicato Strings",
            "Orchestral Harp",
            "Timpani",
            "String Ensemble 1",
            "String Ensemble 2",
            "SynthStrings 1",
            "SynthStrings 2",
            "Choir Aahs",
            "Voice Oohs",
            "Synth Voice",
            "Orchestra Hit",
            "Trumpet",
            "Trombone",
            "Tuba",
            "Muted Trumpet",
            "French Horn",
            "Brass Section",
            "SynthBrass 1",
            "SynthBrass 2",
            "Soprano Sax",
            "Alto Sax",
            "Tenor Sax",
            "Baritone Sax",
            "Oboe",
            "English Horn",
            "Bassoon",
            "Clarinet",
            "Piccolo",
            "Flute",
            "Recorder",
            "Pan Flute",
            "Blown Bottle",
            "Shakuhachi",
            "Whistle",
            "Ocarina",
            "Lead 1 (square)",
            "Lead 2 (sawtooth)",
            "Lead 3 (calliope)",
            "Lead 4 (chiff)",
            "Lead 5 (charang)",
            "Lead 6 (voice)",
            "Lead 7 (fifths)",
            "Lead 8 (bass + lead)",
            "Pad 1 (new age)",
            "Pad 2 (warm)",
            "Pad 3 (polysynth)",
            "Pad 4 (choir)",
            "Pad 5 (bowed)",
            "Pad 6 (metallic)",
            "Pad 7 (halo)",
            "Pad 8 (sweep)",
            "FX 1 (rain)",
            "FX 2 (soundtrack)",
            "FX 3 (crystal)",
            "FX 4 (atmosphere)",
            "FX 5 (brightness)",
            "FX 6 (goblins)",
            "FX 7 (echoes)",
            "FX 8 (sci-fi)",
            "Sitar",
            "Banjo",
            "Shamisen",
            "Koto",
            "Kalimba",
            "Bag pipe",
            "Fiddle",
            "Shanai",
            "Tinkle Bell",
            "Agogo",
            "Steel Drums",
            "Woodblock",
            "Taiko Drum",
            "Melodic Tom",
            "Synth Drum",
            "Reverse Cymbal",
            "Guitar Fret Noise",
            "Breath Noise",
            "Seashore",
            "Bird Tweet",
            "Telephone Ring",
            "Helicopter",
            "Applause",
            "Gunshot",
            "Percussion"
    };
    /**
     * 踏板事件 List
     */
    List<FootBoard> footBoards;
    private String fileName;
    /**
     * The Midi file name
     */
    private ArrayList<ArrayList<MidiEvent>> allEvents;
    /**
     * The raw MidiEvents, one list per track
     */
    private ArrayList<MidiTrack> tracks;
    /**
     * The tracks of the midifile that have notes
     */
    private short trackMode;// midi文件的音轨类型(格式0为单音轨文件，格式1为多音轨文件，格式2为多音序器音轨文件)
    /**
     * 0 (single track), 1 (simultaneous tracks) 2 (independent tracks)
     */
    private TimeSignature timeSignature;
    /**
     * The time signature 拍子记号
     */
    private int quarterNote;
    /**
     * The number of pulses per quarter note 每个四分之一音符的脉冲数
     */
    private int totalPulses;
    /* End Instruments */
    /**
     * The total length of the song, in pulses 歌曲的总长度，以脉冲为单位
     */
    private boolean trackPerChannel;

    /**
     * Create a new MidiFile from the byte[]
     */
    public MidiFile(byte[] rawData, String fileName) {
        this.fileName = fileName;
        // outHData(rawData);
        parse(rawData);
        // outHData(rawData);
        // count8090(rawData);
    }


    public MidiFile(Uri uri, String fileName, Activity activity) {
        this.fileName = fileName;
        byte[] byteDataByUri = IOUtil.getByteDataByUri(uri, activity);
        if (byteDataByUri == null) {
            throw new MidiFileException("getByteDataByUri return null");
        }
        parse(byteDataByUri);
    }

    public MidiFile(File file, String fileName) {
        this.fileName = fileName;
        byte[] byteDataByUri = IOUtil.getByteDataByFile(file);
        if (byteDataByUri == null) {
            throw new MidiFileException("getByteDataByUri return null");
        }
        parse(byteDataByUri);
    }

    /**
     * Return true if this track contains multiple channels.
     * If a MidiFile contains only one track, and it has multiple channels,
     * then we treat each channel as a separate track.
     */
    static boolean HasMultipleChannels(MidiTrack track) {
        int channel = track.getNotes().get(0).getChannel();
        for (MidiNote note : track.getNotes()) {
            if (note.getChannel() != channel) {
                return true;
            }
        }
        return false;
    }

    /**
     * Write a variable length number to the buffer at the given offset.
     * Return the number of bytes written.
     */
    static int VarlenToBytes(int num, byte[] buf, int offset) {
        byte b1 = (byte) ((num >> 21) & 0x7F);
        byte b2 = (byte) ((num >> 14) & 0x7F);
        byte b3 = (byte) ((num >> 7) & 0x7F);
        byte b4 = (byte) (num & 0x7F);

        if (b1 > 0) {
            buf[offset] = (byte) (b1 | 0x80);
            buf[offset + 1] = (byte) (b2 | 0x80);
            buf[offset + 2] = (byte) (b3 | 0x80);
            buf[offset + 3] = b4;
            return 4;
        } else if (b2 > 0) {
            buf[offset] = (byte) (b2 | 0x80);
            buf[offset + 1] = (byte) (b3 | 0x80);
            buf[offset + 2] = b4;
            return 3;
        } else if (b3 > 0) {
            buf[offset] = (byte) (b3 | 0x80);
            buf[offset + 1] = b4;
            return 2;
        } else {
            buf[offset] = b4;
            return 1;
        }
    }

    /**
     * Write a 4-byte integer to data[offset : offset+4]
     */
    private static void IntToBytes(int value, byte[] data, int offset) {
        data[offset] = (byte) ((value >> 24) & 0xFF);
        data[offset + 1] = (byte) ((value >> 16) & 0xFF);
        data[offset + 2] = (byte) ((value >> 8) & 0xFF);
        data[offset + 3] = (byte) (value & 0xFF);
    }

    /**
     * Calculate the track length (in bytes) given a list of Midi events
     */
    private static int GetTrackLength(ArrayList<MidiEvent> events) {
        int len = 0;
        byte[] buf = new byte[1024];
        for (MidiEvent mevent : events) {
            len += VarlenToBytes(mevent.DeltaTime, buf, 0);
            len += 1;  /* for eventflag */
            switch (mevent.EventFlag) {
                case EventNoteOn:
                    len += 2;
                    break;
                case EventNoteOff:
                    len += 2;
                    break;
                case EventKeyPressure:
                    len += 2;
                    break;
                case EventControlChange:
                    len += 2;
                    break;
                case EventProgramChange:
                    len += 1;
                    break;
                case EventChannelPressure:
                    len += 1;
                    break;
                case EventPitchBend:
                    len += 2;
                    break;

                case SysexEvent1:
                case SysexEvent2:
                    len += VarlenToBytes(mevent.Metalength, buf, 0);
                    len += mevent.Metalength;
                    break;
                case MetaEvent:
                    len += 1;
                    len += VarlenToBytes(mevent.Metalength, buf, 0);
                    len += mevent.Metalength;
                    break;
                default:
                    break;
            }
        }
        return len;
    }

    /**
     * Copy len bytes from src to dest, at the given offsets
     */
    private static void ArrayCopy(byte[] src, int srcoffset, byte[] dest, int destoffset, int len) {
        for (int i = 0; i < len; i++) {
            dest[destoffset + i] = src[srcoffset + i];
        }
    }

    /**
     * Write the given list of Midi events to a stream/file.
     * This method is used for sound playback, for creating new Midi files
     * with the tempo, transpose, etc changed.
     * <p/>
     * Return true on success, and false on error.
     */
    private static void WriteEvents(FileOutputStream file, ArrayList<ArrayList<MidiEvent>> allevents,
                                    int trackmode, int quarter) throws IOException {

        byte[] buf = new byte[16384];

        /* Write the MThd, len = 6, track mode, number tracks, quarter note */
        file.write("MThd".getBytes("US-ASCII"), 0, 4);
        IntToBytes(6, buf, 0);
        file.write(buf, 0, 4);
        buf[0] = (byte) (trackmode >> 8);
        buf[1] = (byte) (trackmode & 0xFF);
        file.write(buf, 0, 2);
        buf[0] = 0;
        buf[1] = (byte) allevents.size();
        file.write(buf, 0, 2);
        buf[0] = (byte) (quarter >> 8);
        buf[1] = (byte) (quarter & 0xFF);
        file.write(buf, 0, 2);

        for (ArrayList<MidiEvent> list : allevents) {
            /* Write the MTrk header and track length */
            file.write("MTrk".getBytes("US-ASCII"), 0, 4);
            int len = GetTrackLength(list);
            IntToBytes(len, buf, 0);
            file.write(buf, 0, 4);

            for (MidiEvent mevent : list) {
                int varlen = VarlenToBytes(mevent.DeltaTime, buf, 0);
                file.write(buf, 0, varlen);

                if (mevent.EventFlag == SysexEvent1 ||
                        mevent.EventFlag == SysexEvent2 ||
                        mevent.EventFlag == MetaEvent) {
                    buf[0] = mevent.EventFlag;
                } else {
                    buf[0] = (byte) (mevent.EventFlag + mevent.Channel);
                }
                file.write(buf, 0, 1);

                if (mevent.EventFlag == EventNoteOn) {
                    buf[0] = mevent.Notenumber;
                    buf[1] = mevent.Velocity;
                    file.write(buf, 0, 2);
                } else if (mevent.EventFlag == EventNoteOff) {
                    buf[0] = mevent.Notenumber;
                    buf[1] = mevent.Velocity;
                    file.write(buf, 0, 2);
                } else if (mevent.EventFlag == EventKeyPressure) {
                    buf[0] = mevent.Notenumber;
                    buf[1] = mevent.KeyPressure;
                    file.write(buf, 0, 2);
                } else if (mevent.EventFlag == EventControlChange) {
                    buf[0] = mevent.ControlNum;
                    buf[1] = mevent.ControlValue;
                    file.write(buf, 0, 2);
                } else if (mevent.EventFlag == EventProgramChange) {
                    buf[0] = mevent.Instrument;
                    file.write(buf, 0, 1);
                } else if (mevent.EventFlag == EventChannelPressure) {
                    buf[0] = mevent.ChanPressure;
                    file.write(buf, 0, 1);
                } else if (mevent.EventFlag == EventPitchBend) {
                    buf[0] = (byte) (mevent.PitchBend >> 8);
                    buf[1] = (byte) (mevent.PitchBend & 0xFF);
                    file.write(buf, 0, 2);
                } else if (mevent.EventFlag == SysexEvent1) {
                    int offset = VarlenToBytes(mevent.Metalength, buf, 0);
                    ArrayCopy(mevent.Value, 0, buf, offset, mevent.Value.length);
                    file.write(buf, 0, offset + mevent.Value.length);
                } else if (mevent.EventFlag == SysexEvent2) {
                    int offset = VarlenToBytes(mevent.Metalength, buf, 0);
                    ArrayCopy(mevent.Value, 0, buf, offset, mevent.Value.length);
                    file.write(buf, 0, offset + mevent.Value.length);
                } else if (mevent.EventFlag == MetaEvent && mevent.Metaevent == MetaEventTempo) {
                    buf[0] = mevent.Metaevent;
                    buf[1] = 3;
                    buf[2] = (byte) ((mevent.Tempo >> 16) & 0xFF);
                    buf[3] = (byte) ((mevent.Tempo >> 8) & 0xFF);
                    buf[4] = (byte) (mevent.Tempo & 0xFF);
                    file.write(buf, 0, 5);
                } else if (mevent.EventFlag == MetaEvent) {
                    buf[0] = mevent.Metaevent;
                    int offset = VarlenToBytes(mevent.Metalength, buf, 1) + 1;
                    ArrayCopy(mevent.Value, 0, buf, offset, mevent.Value.length);
                    file.write(buf, 0, offset + mevent.Value.length);
                }
            }
        }
        file.close();
    }

    /**
     * Clone the list of MidiEvents
     */
    private static ArrayList<ArrayList<MidiEvent>> CloneMidiEvents(ArrayList<ArrayList<MidiEvent>> origlist) {
        ArrayList<ArrayList<MidiEvent>> newlist =
                new ArrayList<ArrayList<MidiEvent>>(origlist.size());
        for (int tracknum = 0; tracknum < origlist.size(); tracknum++) {
            ArrayList<MidiEvent> origevents = origlist.get(tracknum);
            ArrayList<MidiEvent> newevents = new ArrayList<MidiEvent>(origevents.size());
            newlist.add(newevents);
            for (MidiEvent mevent : origevents) {
                newevents.add(mevent.Clone());
            }
        }
        return newlist;
    }

    /**
     * Create a new Midi tempo event, with the given tempo
     */
    private static MidiEvent CreateTempoEvent(int tempo) {
        MidiEvent mevent = new MidiEvent();
        mevent.DeltaTime = 0;
        mevent.StartTime = 0;
        mevent.HasEventflag = true;
        mevent.EventFlag = MetaEvent;
        mevent.Metaevent = MetaEventTempo;
        mevent.Metalength = 3;
        mevent.Tempo = tempo;
        return mevent;
    }

    /**
     * Search the events for a ControlChange event with the same
     * channel and control number.  If a matching event is found,
     * update the control value.  Else, add a new ControlChange event.
     */
    private static void UpdateControlChange(ArrayList<MidiEvent> newevents, MidiEvent changeEvent) {
        for (MidiEvent mevent : newevents) {
            if ((mevent.EventFlag == changeEvent.EventFlag) &&
                    (mevent.Channel == changeEvent.Channel) &&
                    (mevent.ControlNum == changeEvent.ControlNum)) {

                mevent.ControlValue = changeEvent.ControlValue;
                return;
            }
        }
        newevents.add(changeEvent);
    }

    /**
     * Start the Midi music at the given pause time (in pulses).
     * Remove any NoteOn/NoteOff events that occur before the pause time.
     * For other events, change the delta-time to 0 if they occur
     * before the pause time.  Return the modified Midi Events.
     */
    private static ArrayList<ArrayList<MidiEvent>> StartAtPauseTime(ArrayList<ArrayList<MidiEvent>> list, int pauseTime) {
        ArrayList<ArrayList<MidiEvent>> newlist = new ArrayList<ArrayList<MidiEvent>>(list.size());
        for (int tracknum = 0; tracknum < list.size(); tracknum++) {
            ArrayList<MidiEvent> events = list.get(tracknum);
            ArrayList<MidiEvent> newevents = new ArrayList<MidiEvent>(events.size());
            newlist.add(newevents);

            boolean foundEventAfterPause = false;
            for (MidiEvent mevent : events) {

                if (mevent.StartTime < pauseTime) {
                    if (mevent.EventFlag == EventNoteOn ||
                            mevent.EventFlag == EventNoteOff) {

                        /* skip NoteOn/NoteOff event */
                    } else if (mevent.EventFlag == EventControlChange) {
                        mevent.DeltaTime = 0;
                        UpdateControlChange(newevents, mevent);
                    } else {
                        mevent.DeltaTime = 0;
                        newevents.add(mevent);
                    }
                } else if (!foundEventAfterPause) {
                    mevent.DeltaTime = (mevent.StartTime - pauseTime);
                    newevents.add(mevent);
                    foundEventAfterPause = true;
                } else {
                    newevents.add(mevent);
                }
            }
        }
        return newlist;
    }

    /**
     * Shift the starttime of the notes by the given amount.
     * This is used by the Shift Notes menu to shift notes left/right.
     */
    public static void
    ShiftTime(ArrayList<MidiTrack> tracks, int amount) {
        for (MidiTrack track : tracks) {
            for (MidiNote note : track.getNotes()) {
                note.setPulsesOfStartTime(note.getPulsesOfStartTime() + amount);
            }
        }
    }

    /**
     * Shift the note keys up/down by the given amount
     */
    public static void
    Transpose(ArrayList<MidiTrack> tracks, int amount) {
        for (MidiTrack track : tracks) {
            for (MidiNote note : track.getNotes()) {
                note.setNoteNumber(note.getNoteNumber() + amount);
                if (note.getNoteNumber() < 0) {
                    note.setNoteNumber(0);
                }
            }
        }
    }

    /* Find the highest and lowest notes that overlap this interval (starttime to endtime).
     * This method is used by SplitTrack to determine which staff (top or bottom) a note
     * should go to.
     *
     * For more accurate SplitTrack() results, we limit the interval/duration of this note
     * (and other notes) to one measure. We care only about high/low notes that are
     * reasonably close to this note.
     */
    private static void
    FindHighLowNotes(ArrayList<MidiNote> notes, int measurelen, int startindex,
                     int starttime, int endtime, PairInt pair) {

        int i = startindex;
        if (starttime + measurelen < endtime) {
            endtime = starttime + measurelen;
        }

        while (i < notes.size() && notes.get(i).getPulsesOfStartTime() < endtime) {
            if (notes.get(i).getEndTime() < starttime) {
                i++;
                continue;
            }
            if (notes.get(i).getPulsesOfStartTime() + measurelen < starttime) {
                i++;
                continue;
            }
            if (pair.high < notes.get(i).getNoteNumber()) {
                pair.high = notes.get(i).getNoteNumber();
            }
            if (pair.low > notes.get(i).getNoteNumber()) {
                pair.low = notes.get(i).getNoteNumber();
            }
            i++;
        }
    }

    /* Find the highest and lowest notes that start at this exact start time */
    private static void
    FindExactHighLowNotes(ArrayList<MidiNote> notes, int startindex, int starttime,
                          PairInt pair) {

        int i = startindex;

        while (notes.get(i).getPulsesOfStartTime() < starttime) {
            i++;
        }

        while (i < notes.size() && notes.get(i).getPulsesOfStartTime() == starttime) {
            if (pair.high < notes.get(i).getNoteNumber()) {
                pair.high = notes.get(i).getNoteNumber();
            }
            if (pair.low > notes.get(i).getNoteNumber()) {
                pair.low = notes.get(i).getNoteNumber();
            }
            i++;
        }
    }

    /* Split the given MidiTrack into two tracks, top and bottom.
     * The highest notes will go into top, the lowest into bottom.
     * This function is used to split piano songs into left-hand (bottom)
     * and right-hand (top) tracks.
     */
    public static ArrayList<MidiTrack> SplitTrack(MidiTrack track, int measurelen) {
        ArrayList<MidiNote> notes = track.getNotes();
        int count = notes.size();

        MidiTrack top = new MidiTrack(1);
        MidiTrack bottom = new MidiTrack(2);
        ArrayList<MidiTrack> result = new ArrayList<MidiTrack>(2);
        result.add(top);
        result.add(bottom);

        if (count == 0)
            return result;

        int prevhigh = 76; /* E5, top of treble staff */
        int prevlow = 45; /* A3, bottom of bass staff */
        int startindex = 0;

        for (MidiNote note : notes) {
            int high, low, highExact, lowExact;

            int number = note.getNoteNumber();
            high = low = highExact = lowExact = number;

            while (notes.get(startindex).getEndTime() < note.getPulsesOfStartTime()) {
                startindex++;
            }

            /* I've tried several algorithms for splitting a track in two,
             * and the one below seems to work the best:
             * - If this note is more than an octave from the high/low notes
             *   (that start exactly at this start time), choose the closest one.
             * - If this note is more than an octave from the high/low notes
             *   (in this note's time duration), choose the closest one.
             * - If the high and low notes (that start exactly at this starttime)
             *   are more than an octave apart, choose the closest note.
             * - If the high and low notes (that overlap this starttime)
             *   are more than an octave apart, choose the closest note.
             * - Else, look at the previous high/low notes that were more than an
             *   octave apart.  Choose the closeset note.
             */
            PairInt pair = new PairInt();
            pair.high = high;
            pair.low = low;
            PairInt pairExact = new PairInt();
            pairExact.high = highExact;
            pairExact.low = lowExact;

            FindHighLowNotes(notes, measurelen, startindex, note.getPulsesOfStartTime(), note.getEndTime(), pair);
            FindExactHighLowNotes(notes, startindex, note.getPulsesOfStartTime(), pairExact);

            high = pair.high;
            low = pair.low;
            highExact = pairExact.high;
            lowExact = pairExact.low;

            if (highExact - number > 12 || number - lowExact > 12) {
                if (highExact - number <= number - lowExact) {
                    top.AddNote(note);
                } else {
                    bottom.AddNote(note);
                }
            } else if (high - number > 12 || number - low > 12) {
                if (high - number <= number - low) {
                    top.AddNote(note);
                } else {
                    bottom.AddNote(note);
                }
            } else if (highExact - lowExact > 12) {
                if (highExact - number <= number - lowExact) {
                    top.AddNote(note);
                } else {
                    bottom.AddNote(note);
                }
            } else if (high - low > 12) {
                if (high - number <= number - low) {
                    top.AddNote(note);
                } else {
                    bottom.AddNote(note);
                }
            } else {
                if (prevhigh - number <= number - prevlow) {
                    top.AddNote(note);
                } else {
                    bottom.AddNote(note);
                }
            }

            /* The prevhigh/prevlow are set to the last high/low
             * that are more than an octave apart.
             */
            if (high - low > 12) {
                prevhigh = high;
                prevlow = low;
            }
        }

//        Collections.sort(top.getNotes(), track.getNotes().get(0));
//        Collections.sort(bottom.getNotes(), track.getNotes().get(0));

        //更改Comparator为Comparable后 —— by yin
        Collections.sort(top.getNotes());
        Collections.sort(bottom.getNotes());

        return result;
    }

    /**
     * Combine the notes in the given tracks into a single MidiTrack.
     * The individual tracks are already sorted.  To merge them, we
     * use a mergesort-like algorithm.
     */
    public static MidiTrack CombineToSingleTrack(ArrayList<MidiTrack> tracks) {
        /* Add all notes into one track */
        MidiTrack result = new MidiTrack(1);

        if (tracks.size() == 0) {
            return result;
        } else if (tracks.size() == 1) {
            MidiTrack track = tracks.get(0);
            for (MidiNote note : track.getNotes()) {
                result.AddNote(note);
            }
            return result;
        }

        int[] noteindex = new int[tracks.size() + 1];
        int[] notecount = new int[tracks.size() + 1];

        for (int tracknum = 0; tracknum < tracks.size(); tracknum++) {
            noteindex[tracknum] = 0;
            notecount[tracknum] = tracks.get(tracknum).getNotes().size();
        }
        MidiNote prevnote = null;
        while (true) {
            MidiNote lowestnote = null;
            int lowestTrack = -1;
            for (int tracknum = 0; tracknum < tracks.size(); tracknum++) {
                MidiTrack track = tracks.get(tracknum);
                if (noteindex[tracknum] >= notecount[tracknum]) {
                    continue;
                }
                MidiNote note = track.getNotes().get(noteindex[tracknum]);
                if (lowestnote == null) {
                    lowestnote = note;
                    lowestTrack = tracknum;
                } else if (note.getPulsesOfStartTime() < lowestnote.getPulsesOfStartTime()) {
                    lowestnote = note;
                    lowestTrack = tracknum;
                } else if (note.getPulsesOfStartTime() == lowestnote.getPulsesOfStartTime() && note.getNoteNumber() < lowestnote.getNoteNumber()) {
                    lowestnote = note;
                    lowestTrack = tracknum;
                }
            }
            if (lowestnote == null) {
                /* We've finished the merge */
                break;
            }
            noteindex[lowestTrack]++;
            if ((prevnote != null) && (prevnote.getPulsesOfStartTime() == lowestnote.getPulsesOfStartTime()) &&
                    (prevnote.getNoteNumber() == lowestnote.getNoteNumber())) {

                /* Don't add duplicate notes, with the same start time and number */
                if (lowestnote.getDuration() > prevnote.getDuration()) {
                    prevnote.setDuration(lowestnote.getDuration());
                }
            } else {
                result.AddNote(lowestnote);
                prevnote = lowestnote;
            }
        }

        return result;
    }

    /**
     * Combine the notes in all the tracks given into two MidiTracks,
     * and return them.
     * <p/>
     * This function is intended for piano songs, when we want to display
     * a left-hand track and a right-hand track.  The lower notes go into
     * the left-hand track, and the higher notes go into the right hand
     * track.
     */
    public static ArrayList<MidiTrack> CombineToTwoTracks(ArrayList<MidiTrack> tracks, int measurelen) {
        MidiTrack single = CombineToSingleTrack(tracks);
        ArrayList<MidiTrack> result = SplitTrack(single, measurelen);

        ArrayList<MidiEvent> lyrics = new ArrayList<MidiEvent>();
        for (MidiTrack track : tracks) {
            if (track.getLyrics() != null) {
                lyrics.addAll(track.getLyrics());
            }
        }
        if (lyrics.size() > 0) {
            Collections.sort(lyrics, lyrics.get(0));
            result.get(0).setLyrics(lyrics);
        }
        return result;
    }

    /**
     * Check that the MidiNote start times are in increasing order.
     * This is for debugging purposes.  // 检查midi音符的开始时间是不是按增续排列的, 这是为了排除故障
     */
    private static void CheckStartTimes(ArrayList<MidiTrack> tracks) {
        for (MidiTrack track : tracks) {
            int prevtime = -1;// 上一个Midi音符的开始时间
            for (MidiNote note : track.getNotes()) {
                if (note.getPulsesOfStartTime() < prevtime) {
                    throw new MidiFileException("Internal parsing error", 0);
                }
                prevtime = note.getPulsesOfStartTime();
            }
        }
    }

    /**
     * In Midi Files, time is measured in pulses.  Notes that have
     * pulse times that are close together (like within 10 pulses)
     * will sound like they're the same chord.  We want to draw
     * these notes as a single chord, it makes the sheet music much
     * easier to read.  We don't want to draw notes that are close
     * together as two separate chords.
     * <p/>
     * The SymbolSpacing class only aligns notes that have exactly the same
     * start times.  Notes with slightly different start times will
     * appear in separate vertical columns.  This isn't what we want.
     * We want to align notes with approximately the same start times.
     * So, this function is used to assign the same starttime for notes
     * that are close together (timewise).
     */
    public static void
    RoundStartTimes(ArrayList<MidiTrack> tracks, int millisec, TimeSignature time) {
        /* Get all the starttimes in all tracks, in sorted order */
        ListInt starttimes = new ListInt();
        for (MidiTrack track : tracks) {
            for (MidiNote note : track.getNotes()) {
                starttimes.add(note.getPulsesOfStartTime());
            }
        }
        starttimes.sort();

        /* Notes within "millisec" milliseconds apart will be combined. */
        int interval = time.getQuarter() * millisec * 1000 / time.getTempo();

        /* If two starttimes are within interval millisec, make them the same */
        for (int i = 0; i < starttimes.size() - 1; i++) {
            if (starttimes.get(i + 1) - starttimes.get(i) <= interval) {
                starttimes.set(i + 1, starttimes.get(i));
            }
        }

        CheckStartTimes(tracks);

        /* Adjust the note starttimes, so that it matches one of the starttimes values */
        for (MidiTrack track : tracks) {
            int i = 0;

            for (MidiNote note : track.getNotes()) {
                while (i < starttimes.size() &&
                        note.getPulsesOfStartTime() - interval > starttimes.get(i)) {
                    i++;
                }

                if (note.getPulsesOfStartTime() > starttimes.get(i) &&
                        note.getPulsesOfStartTime() - starttimes.get(i) <= interval) {

                    note.setPulsesOfStartTime(starttimes.get(i));
                }
            }
//            Collections.sort(track.getNotes(), track.getNotes().get(0));
            Collections.sort(track.getNotes());
        }
    }

    /**
     * We want note durations to span up to the next note in general.
     * The sheet music looks nicer that way.  In contrast, sheet music
     * with lots of 16th/32nd notes separated by small rests doesn't
     * look as nice.  Having nice looking sheet music is more important
     * than faithfully representing the Midi File data.
     * <p/>
     * Therefore, this function rounds the duration of MidiNotes up to
     * the next note where possible.
     */
    public static void
    RoundDurations(ArrayList<MidiTrack> tracks, int quarternote) {

        for (MidiTrack track : tracks) {
            MidiNote prevNote = null;
            for (int i = 0; i < track.getNotes().size() - 1; i++) {
                MidiNote note1 = track.getNotes().get(i);
                if (prevNote == null) {
                    prevNote = note1;
                }

                /* Get the next note that has a different start time */
                MidiNote note2 = note1;
                for (int j = i + 1; j < track.getNotes().size(); j++) {
                    note2 = track.getNotes().get(j);
                    if (note1.getPulsesOfStartTime() < note2.getPulsesOfStartTime()) {
                        break;
                    }
                }
                int maxduration = note2.getPulsesOfStartTime() - note1.getPulsesOfStartTime();

                int dur = 0;
                if (quarternote <= maxduration)
                    dur = quarternote;
                else if (quarternote / 2 <= maxduration)
                    dur = quarternote / 2;
                else if (quarternote / 3 <= maxduration)
                    dur = quarternote / 3;
                else if (quarternote / 4 <= maxduration)
                    dur = quarternote / 4;


                if (dur < note1.getDuration()) {
                    dur = note1.getDuration();
                }

                /* Special case: If the previous note's duration
                 * matches this note's duration, we can make a notepair.
                 * So don't expand the duration in that case.
                 */
                if ((prevNote.getPulsesOfStartTime() + prevNote.getDuration() == note1.getPulsesOfStartTime()) &&
                        (prevNote.getDuration() == note1.getDuration())) {


                    dur = note1.getDuration();
                }
                note1.setDuration(dur);
                if (track.getNotes().get(i + 1).getPulsesOfStartTime() != note1.getPulsesOfStartTime()) {
                    prevNote = note1;
                }
            }
        }
    }

    /**
     * Split the given track into multiple tracks, separating each
     * channel into a separate track. 将所给的音轨分离成多个音轨，将每一个通道分成独立的音轨
     */
    private static ArrayList<MidiTrack> SplitChannels(MidiTrack origtrack, ArrayList<MidiEvent> events) {

        /* Find the instrument used for each channel */
        int[] channelInstruments = new int[16];
        for (MidiEvent mevent : events) {
            if (mevent.EventFlag == EventProgramChange) {
                channelInstruments[mevent.Channel] = mevent.Instrument;
            }
        }
        channelInstruments[9] = 128; /* Channel 9 = Percussion */

        ArrayList<MidiTrack> result = new ArrayList<MidiTrack>();
        for (MidiNote note : origtrack.getNotes()) {
            boolean foundchannel = false;
            for (MidiTrack track : result) {
                if (note.getChannel() == track.getNotes().get(0).getChannel()) {
                    foundchannel = true;
                    track.AddNote(note);
                }
            }
            if (!foundchannel) {
                MidiTrack track = new MidiTrack(result.size() + 1);
                track.AddNote(note);
                track.setInstrument(channelInstruments[note.getChannel()]);
                result.add(track);
            }
        }
        ArrayList<MidiEvent> lyrics = origtrack.getLyrics();
        if (lyrics != null) {
            for (MidiEvent lyricEvent : lyrics) {
                for (MidiTrack track : result) {
                    if (lyricEvent.Channel == track.getNotes().get(0).getChannel()) {
                        track.AddLyric(lyricEvent);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Return true if the data starts with the header MTrk
     */
    public static boolean hasMidiHeader(byte[] data) {
        String s;
        try {
            s = new String(data, 0, 4, "US-ASCII");
            if (s.equals("MThd"))
                return true;
            else
                return false;
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

    /* Command-line program to print out a parsed Midi file. Used for debugging.
     * To run:
     * - Change main2 to main
     * - javac MidiFile.java
     * - java MidiFile file.mid
     *
     */
    public static void main2(String[] args) {
        /**
         if (args.length == 0) {
         System.out.println("Usage: MidiFile <fileName>");
         return;
         }
         String fileName = args[0];
         byte[] data;
         try {
         File info = new File(fileName);
         FileInputStream file = new FileInputStream(fileName);

         data = new byte[ (int)info.length() ];
         int offset = 0;
         int len = (int)info.length();
         while (true) {
         if (offset == len)
         break;
         int n = file.read(data, offset, len- offset);
         if (n <= 0)
         break;
         offset += n;
         }
         file.close();
         }
         catch(IOException e) {
         return;
         }

         MidiFile f = new MidiFile(data, "");
         System.out.print(f.toString());
         **/
    }

    /**
     * Return a String representation of a Midi event
     */
    private String EventName(int ev) {
        if (ev >= EventNoteOff && ev < EventNoteOff + 16)
            return "NoteOff";
        else if (ev >= EventNoteOn && ev < EventNoteOn + 16)
            return "NoteOn";
        else if (ev >= EventKeyPressure && ev < EventKeyPressure + 16)
            return "KeyPressure";
        else if (ev >= EventControlChange && ev < EventControlChange + 16)
            return "ControlChange";
        else if (ev >= EventProgramChange && ev < EventProgramChange + 16)
            return "ProgramChange";
        else if (ev >= EventChannelPressure && ev < EventChannelPressure + 16)
            return "ChannelPressure";
        else if (ev >= EventPitchBend && ev < EventPitchBend + 16)
            return "PitchBend";
        else if (ev == MetaEvent)
            return "MetaEvent";
        else if (ev == SysexEvent1 || ev == SysexEvent2)
            return "SysexEvent";
        else
            return "Unknown";
    }

    /**
     * Return a String representation of a meta-event
     */
    private String MetaName(int ev) {
        if (ev == MetaEventSequence)
            return "MetaEventSequence";
        else if (ev == MetaEventText)
            return "MetaEventText";
        else if (ev == MetaEventCopyright)
            return "MetaEventCopyright";
        else if (ev == MetaEventSequenceName)
            return "MetaEventSequenceName";
        else if (ev == MetaEventInstrument)
            return "MetaEventInstrument";
        else if (ev == MetaEventLyric)
            return "MetaEventLyric";
        else if (ev == MetaEventMarker)
            return "MetaEventMarker";
        else if (ev == MetaEventEndOfTrack)
            return "MetaEventEndOfTrack";
        else if (ev == MetaEventTempo)
            return "MetaEventTempo";
        else if (ev == MetaEventSMPTEOffset)
            return "MetaEventSMPTEOffset";
        else if (ev == MetaEventTimeSignature)
            return "MetaEventTimeSignature";
        else if (ev == MetaEventKeySignature)
            return "MetaEventKeySignature";
        else
            return "Unknown";
    }

    /**
     * Get the list of tracks
     */
    public ArrayList<MidiTrack> getTracks() {
        return tracks;
    }

    /**
     * Get the time signature
     */
    public TimeSignature getTime() {
        return timeSignature;
    }

    /**
     * Get the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Get the total length (in pulses) of the song
     */
    public int getTotalPulses() {
        return totalPulses;
    }

    public ArrayList<ArrayList<MidiEvent>> getAllEvents() {
        return allEvents;
    }

    private void count8090(byte[] rawdata) {
        int count1 = 0;
        int count2 = 0;
        for (int i = 0; i < rawdata.length; i++) {
            if ((rawdata[i] & 0xff) == 144)
                count1++;
            if ((rawdata[i] & 0xff) == 128)
                count2++;
        }
        System.out.println("count1:" + count1);
        System.out.println("count2:" + count2);
    }

    private void outHData(byte[] rawdata) {
        for (int i = 0; i < rawdata.length; i++) {
            if (i % 50 == 0)
                System.out.println("");
//            System.out.print(Integer.toHexString(rawdata[i] & 0xff) + "\t");
        }
    }

    /**
     * Parse the given Midi file, and return an instance of this MidiFile 解析所给的midi文件，并返回MidiFile类的实例
     * class.  After reading the midi file, this object will contain: // 读完midi文件后,这个对象将会包含：
     * - The raw list of midi events  // 未处理的midi事件列表
     * - The Time Signature of the song // 歌曲的拍子记号
     * - All the tracks in the song which contain notes. // 这个歌曲中包含音符的所有音轨
     * - The number, starttime, and duration of each note. // 每个音符的数字、开始时间、以及音长时间
     */
    private void parse(byte[] rawData) {
        String id;
        int len;
        tracks = new ArrayList<>();
        trackPerChannel = false;
        MidiFileReader file = new MidiFileReader(rawData);
        id = file.readAscii(4);
        if (!id.equals("MThd")) {
            throw new MidiFileException("Doesn't start with MThd", 0);
        }
        len = file.readInt();
        if (len != 6) {
            throw new MidiFileException("Bad MThd header", 4);
        }
        trackMode = (short) file.readShort();// midiFile文件音轨的格式类型
        int tracksNum = file.readShort();// 音轨数
        quarterNote = file.readShort();// MIDI事件的时间格式类型
        allEvents = new ArrayList<>();
        //左右手音轨标志，偶数右手，奇数左手
        int count = 0;
        // 读取所有的音轨块
        for (int trackIndex = 0; trackIndex < tracksNum; trackIndex++) {
            allEvents.add(readTrack(file));
            // Log.i("allEvents", allEvents.get(trackIndex).size() + "");
            MidiTrack track = new MidiTrack(allEvents.get(trackIndex), trackIndex);
            ArrayList<MidiNote> notes = track.getNotes();
            if (notes.size() > 0) {
                tracks.add(track);
                count++;
            }
        }
        // Log.i("allEvents", "----------------------------------------------------------------------------");


        /* Determine the time signature */ // 确定时间签名
        int tempoCount = 0;// 拍子数量
        long tempo = 0;// 拍子(每一个四分之一音符的毫秒数)
        int numer = 0;// 分子
        int denom = 0;// 分母
        for (ArrayList<MidiEvent> list : allEvents) {
            for (MidiEvent mevent : list) {
                if (mevent.Metaevent == MetaEventTempo) {// 音符毫秒数元事件
                    // Take average of all tempos
                    tempo += mevent.Tempo;
                    Log.i("MidiFile", mevent.Tempo + "");
                    tempoCount++;
                }
                if (mevent.Metaevent == MetaEventTimeSignature && numer == 0) {// 拍子记号
                    numer = mevent.Numerator;
                    denom = mevent.Denominator;
                }
            }
        }
        if (tempo == 0) {
            tempo = 500000; /* 500,000 microseconds = 0.05 sec */
        } else {
            tempo = tempo / tempoCount;
        }
        if (numer == 0) {
            numer = 4;
            denom = 4;
        }
        timeSignature = new TimeSignature(numer, denom, quarterNote, (int) tempo);

        footBoards = handleFootBoard();

        /* Get the length of the song in pulses */
        for (MidiTrack track : tracks) {
            MidiNote last = track.getNotes().get(track.getNotes().size() - 1);
            if (this.totalPulses < last.getPulsesOfStartTime() + last.getDuration()) {
                this.totalPulses = last.getPulsesOfStartTime() + last.getDuration();
            }
        }

        /* If we only have one track with multiple channels, then treat
         * each channel as a separate track.// 如果我们只有一条音轨却有多个通道，将每个通道视为一个独立的音轨
         */
        if (tracks.size() == 1 && HasMultipleChannels(tracks.get(0))) {
            tracks = SplitChannels(tracks.get(0), allEvents.get(tracks.get(0).trackNumber()));
            trackPerChannel = true;
        }

        CheckStartTimes(tracks);

        ArrayList<MidiNote> midiNotes;
        for (MidiTrack midiTrack : tracks) {
            midiNotes = midiTrack.getNotes();
            for (MidiNote midiNote : midiNotes) {
//                midiNote.setPulsesOfQuarterNote(timeSignature.getQuarter());
//                midiNote.setTempo(timeSignature.getTempo());
                for (FootBoard footBoard : footBoards) {
                    int notePulsesOfEndTime = midiNote.getPulsesOfStartTime() + midiNote.getDuration();
                    if (footBoard.getStartTime() < notePulsesOfEndTime && footBoard.getEndTime() > notePulsesOfEndTime) {
                        midiNote.setPedalActionTime(footBoard.getEndTime() - notePulsesOfEndTime);
                    }
                }
            }
        }
    }

    private List<FootBoard> handleFootBoard() {
        List<FootBoard> footBoards = new ArrayList<>();
        FootBoard footBoard;
        MidiEvent preMidiEvent = allEvents.get(0).get(0);
        MidiEvent currMidiEvent;
        int maxIndex = 0;
        int maxSize = 0;
        for (int i = 0; i < allEvents.size(); i++) {
            maxIndex = allEvents.get(i).size() > maxSize ? i : maxIndex;
            maxSize = allEvents.get(i).size() > maxSize ? allEvents.get(i).size() : maxSize;
        }
//        Log.i("allEvents", "----------------------------------------");

//        ArrayList<MidiEvent> midiEvents = allEvents.get(maxIndex);
        for (List<MidiEvent> midiEvents : allEvents) {

            for (int i = 0; i < midiEvents.size(); i++) {
                currMidiEvent = midiEvents.get(i);
                if (currMidiEvent.EventFlag == EventControlChange && currMidiEvent.ControlNum == '@') {
//                if (currMidiEvent.ControlValue != preMidiEvent.ControlValue) {
                    if (currMidiEvent.ControlValue > 30) {
                        footBoard = new FootBoard(currMidiEvent.StartTime, timeSignature);
                        footBoard.setVelocity(currMidiEvent.ControlValue);
                        footBoards.add(footBoard);
                    } else {
                        if (footBoards.size() > 0) {
                            footBoards.get(footBoards.size() - 1).setEndTime(currMidiEvent.StartTime);
                        }
                    }
                    preMidiEvent = currMidiEvent;
//                }
                }
            }

        }
        //todo 调试解析结果发现每一条有效数据伴随着一条开始时间大于零，结束时间等于零的多余数据，待解决
        return footBoards;
    }

    /**
     * Parse a single Midi track into a list of MidiEvents.
     * Entering this function, the file offset should be at the start of
     * the MTrk header.  Upon exiting, the file offset should be at the
     * start of the next MTrk header.
     */
    private ArrayList<MidiEvent> readTrack(MidiFileReader file) {
        ArrayList<MidiEvent> result = new ArrayList<MidiEvent>(20);
        int startTime = 0;
        String id = file.readAscii(4);

        if (!id.equals("MTrk")) {
            throw new MidiFileException("Bad MTrk header", file.getOffset() - 4);
        }
        int trackLen = file.readInt();
        int trackEnd = trackLen + file.getOffset();

        byte eventFlag = 0;
        int channel0 = 0;
        int channel1 = 0;
        while (file.getOffset() < trackEnd) {
            // If the midi file is truncated here, we can still recover.
            // Just return what we've parsed so far.
            int startoffset, deltaTime;
            byte peekevent;
            try {
                startoffset = file.getOffset();
                deltaTime = file.readVariableLength();
                startTime += deltaTime;
                peekevent = file.Peek();
            } catch (MidiFileException e) {
                return result;
            }

            MidiEvent midiEvent = new MidiEvent();
            result.add(midiEvent);
            midiEvent.DeltaTime = deltaTime;// 现在这个事件距离上一个事件的时间,单位:tick
            midiEvent.StartTime = startTime;// 这个事件开始的绝对时间,单位:tick

            // if (peekevent >= EventNoteOff) {
            if (peekevent < 0) {
                midiEvent.HasEventflag = true;// 如果使用之前的事件标记则为false
                eventFlag = file.readByte();
            }

            //Log.e("debug",  "offset " + startoffset +
            //                " event " + eventFlag + " " + EventName(eventFlag) +
            //                " start " + startTime + " delta " + midiEvent.DeltaTime);

            if (eventFlag >= EventNoteOn && eventFlag < EventNoteOn + 16) {// 声音开启的情况,状态位为"001",考虑16个通道
                midiEvent.EventFlag = EventNoteOn;// 状态"开"
                midiEvent.Channel = ((byte) (eventFlag - EventNoteOn));// 通道
                midiEvent.Notenumber = file.readByte();// 按哪个键
                midiEvent.Velocity = file.readByte();// 音速
            } else if (eventFlag >= EventNoteOff && eventFlag < EventNoteOff + 16) {// 声音关闭的情况,状态位为"000",考虑16个通道
                midiEvent.EventFlag = EventNoteOff;// 状态"关"
                midiEvent.Channel = ((byte) (eventFlag - EventNoteOff));// 通道
                midiEvent.Notenumber = file.readByte();// 按哪个键
                midiEvent.Velocity = file.readByte();// 音速
            } else if (eventFlag >= EventKeyPressure &&
                    eventFlag < EventKeyPressure + 16) {// 音键压力,状态位为"010",考虑16个通道
                midiEvent.EventFlag = EventKeyPressure;// 音键压力
                midiEvent.Channel = ((byte) (eventFlag - EventKeyPressure));// 通道
                midiEvent.Notenumber = file.readByte();// 按哪个键
                midiEvent.KeyPressure = file.readByte();// 键压力
            } else if (eventFlag >= EventControlChange &&
                    eventFlag < EventControlChange + 16) {// 控制变化,状态位为"011",考虑16个通道
                midiEvent.EventFlag = EventControlChange;// 控制变化
                midiEvent.Channel = ((byte) (eventFlag - EventControlChange));// 通道
                if (midiEvent.Channel == 0) {
                    channel0++;
                    Log.i("Control count0", channel0 + "");
                } else {
                    channel1++;
                    Log.i("Control count1", channel1 + "");
                }
                Log.i("Control Channel", midiEvent.Channel + "");
                midiEvent.ControlNum = file.readByte();// 控制器号
                Log.i("Control ControlNum", midiEvent.ControlNum + "");
                midiEvent.ControlValue = file.readByte();// 控制器值
                Log.i("Control ControlValue", midiEvent.ControlValue + "");
                Log.i("Control", "--------------------------------------------");
            } else if (eventFlag >= EventProgramChange &&
                    eventFlag < EventProgramChange + 16) {// 改字乐器,状态位为"100",考虑16个通道
                midiEvent.EventFlag = EventProgramChange;// 改字乐器
                midiEvent.Channel = ((byte) (eventFlag - EventProgramChange));// 通道
                midiEvent.Instrument = file.readByte();// 乐器编号
            } else if (eventFlag >= EventChannelPressure &&
                    eventFlag < EventChannelPressure + 16) {// 通道触动压力,状态位为"101",考虑16个通道
                midiEvent.EventFlag = EventChannelPressure;// 通道触动压力
                midiEvent.Channel = ((byte) (eventFlag - EventChannelPressure));// 通道
                midiEvent.ChanPressure = file.readByte();// 压力
            } else if (eventFlag >= EventPitchBend &&
                    eventFlag < EventPitchBend + 16) {// 弯音轮变化,状态位为"110",考虑16个通道 0xE0
                midiEvent.EventFlag = EventPitchBend;// 弯音轮变化
                midiEvent.Channel = ((byte) (eventFlag - EventPitchBend));// 通道
                midiEvent.PitchBend = (short) file.readShort();// 音高
            } else if (eventFlag == SysexEvent1) {// 状态位为"111",不考虑16个通道 0xf0 系统专用消息
                midiEvent.EventFlag = SysexEvent1;// 系统专用消息 厂商标识号
                midiEvent.Metalength = file.readVariableLength();
                midiEvent.Value = file.readBytes(midiEvent.Metalength);
            } else if (eventFlag == SysexEvent2) {// "1111 0111" 0xf7 系统专用消息的结束标记EOX
                midiEvent.EventFlag = SysexEvent2;// 系统专用结束标记
                midiEvent.Metalength = file.readVariableLength();
                midiEvent.Value = file.readBytes(midiEvent.Metalength);
            } else if (eventFlag == MetaEvent) {// "1111 1111" 0xFF 系统复位
                midiEvent.EventFlag = MetaEvent;
                midiEvent.Metaevent = file.readByte();
                midiEvent.Metalength = file.readVariableLength();
                midiEvent.Value = file.readBytes(midiEvent.Metalength);
                if (midiEvent.Metaevent == MetaEventTimeSignature) {
                    if (midiEvent.Metalength < 2) {
                        throw new MidiFileException(
                                "Meta Event Time Signature len == " + midiEvent.Metalength +
                                        " != 4", file.getOffset());
                    } else {
                        midiEvent.Numerator = ((byte) midiEvent.Value[0]);
                        midiEvent.Denominator = ((byte) Math.pow(2, midiEvent.Value[1]));
                    }
                } else if (midiEvent.Metaevent == MetaEventTempo) {
                    if (midiEvent.Metalength != 3) {
                        throw new MidiFileException(
                                "Meta Event Tempo len == " + midiEvent.Metalength +
                                        " != 3", file.getOffset());
                    }
                    midiEvent.Tempo = ((midiEvent.Value[0] & 0xFF) << 16) |
                            ((midiEvent.Value[1] & 0xFF) << 8) |
                            (midiEvent.Value[2] & 0xFF);
                } else if (midiEvent.Metaevent == MetaEventEndOfTrack) {
                    /* break;  */
                }
            } else {
                throw new MidiFileException("Unknown event " + midiEvent.EventFlag,
                        file.getOffset() - 1);
            }
        }
        return result;
    }

    /**
     * Write this Midi file to the given file.
     * If options is not null, apply those options to the midi events
     * before performing the write.
     * Return true if the file was saved successfully, else false.
     */
    public void ChangeSound(FileOutputStream destfile, MidiOptions options)
            throws IOException {
        Write(destfile, options);
    }

    public void Write(FileOutputStream destfile, MidiOptions options)
            throws IOException {
        ArrayList<ArrayList<MidiEvent>> newevents = allEvents;
        if (options != null) {
            newevents = ApplyOptionsToEvents(options);
        }
        WriteEvents(destfile, newevents, trackMode, quarterNote);
    }

    /**
     * Apply the following sound options to the midi events:
     * - The tempo (the microseconds per pulse)
     * - The instruments per track
     * - The note number (transpose value)
     * - The tracks to include
     * Return the modified list of midi events.
     */
    public ArrayList<ArrayList<MidiEvent>>
    ApplyOptionsToEvents(MidiOptions options) {
        int i;
        if (trackPerChannel) {
            return ApplyOptionsPerChannel(options);
        }

        /* A midifile can contain tracks with notes and tracks without notes.
         * The options.tracks and options.instruments are for tracks with notes.
         * So the track numbers in 'options' may not match correctly if the
         * midi file has tracks without notes. Re-compute the instruments, and
         * tracks to keep.
         */
        int num_tracks = allEvents.size();
        int[] instruments = new int[num_tracks];
        boolean[] keeptracks = new boolean[num_tracks];
        for (i = 0; i < num_tracks; i++) {
            instruments[i] = 0;
            keeptracks[i] = true;
        }
        for (int tracknum = 0; tracknum < tracks.size(); tracknum++) {
            MidiTrack track = tracks.get(tracknum);
            int realtrack = track.trackNumber();
            instruments[realtrack] = options.instruments[tracknum];
            if (!options.tracks[tracknum] || options.mute[tracknum]) {
                keeptracks[realtrack] = false;
            }
        }

        ArrayList<ArrayList<MidiEvent>> newevents = CloneMidiEvents(allEvents);

        /* Set the tempo at the beginning of each track */
        for (int tracknum = 0; tracknum < newevents.size(); tracknum++) {
            MidiEvent mevent = CreateTempoEvent(options.tempo);
            newevents.get(tracknum).add(0, mevent);
        }
        /* Change the note number (transpose), instrument, and tempo */
        for (int tracknum = 0; tracknum < newevents.size(); tracknum++) {
            for (MidiEvent mevent : newevents.get(tracknum)) {
                int num = mevent.Notenumber + options.transpose;
                if (num < 0)
                    num = 0;
                if (num > 127)
                    num = 127;
                mevent.Notenumber = (byte) num;
                if (!options.useDefaultInstruments) {
                    mevent.Instrument = (byte) instruments[tracknum];
                }
                mevent.Tempo = options.tempo;
            }
        }
        if (options.pauseTime != 0) {
            newevents = StartAtPauseTime(newevents, options.pauseTime);
        }
        /* Change the tracks to include */
        int count = 0;
        for (int tracknum = 0; tracknum < keeptracks.length; tracknum++) {
            if (keeptracks[tracknum]) {
                count++;
            }
        }
        ArrayList<ArrayList<MidiEvent>> result = new ArrayList<ArrayList<MidiEvent>>(count);
        i = 0;
        for (int tracknum = 0; tracknum < keeptracks.length; tracknum++) {
            if (keeptracks[tracknum]) {
                result.add(newevents.get(tracknum));
                i++;
            }
        }
        return result;
    }

    /**
     * Apply the following sound options to the midi events:
     * - The tempo (the microseconds per pulse)
     * - The instruments per track
     * - The note number (transpose value)
     * - The tracks to include
     * Return the modified list of midi events.
     * <p/>
     * This Midi file only has one actual track, but we've split that
     * into multiple fake tracks, one per channel, and displayed that
     * to the end-user.  So changing the instrument, and tracks to
     * include, is implemented differently than the ApplyOptionsToEvents() method:
     * <p/>
     * - We change the instrument based on the channel, not the track.
     * - We include/exclude channels, not tracks.
     * - We exclude a channel by setting the note volume/velocity to 0.
     */
    public ArrayList<ArrayList<MidiEvent>> ApplyOptionsPerChannel(MidiOptions options) {
        /* Determine which channels to include/exclude.
         * Also, determine the instruments for each channel.
         */
        int[] instruments = new int[16];
        boolean[] keepchannel = new boolean[16];
        for (int i = 0; i < 16; i++) {
            instruments[i] = 0;
            keepchannel[i] = true;
        }
        for (int tracknum = 0; tracknum < tracks.size(); tracknum++) {
            MidiTrack track = tracks.get(tracknum);
            int channel = track.getNotes().get(0).getChannel();
            instruments[channel] = options.instruments[tracknum];
            if (options.tracks[tracknum] == false || options.mute[tracknum] == true) {
                keepchannel[channel] = false;
            }
        }
        ArrayList<ArrayList<MidiEvent>> newevents = CloneMidiEvents(allEvents);
        /* Set the tempo at the beginning of each track */
        for (int tracknum = 0; tracknum < newevents.size(); tracknum++) {
            MidiEvent mevent = CreateTempoEvent(options.tempo);
            newevents.get(tracknum).add(0, mevent);
        }
        /* Change the note number (transpose), instrument, and tempo */
        for (int tracknum = 0; tracknum < newevents.size(); tracknum++) {
            for (MidiEvent mevent : newevents.get(tracknum)) {
                int num = mevent.Notenumber + options.transpose;
                if (num < 0)
                    num = 0;
                if (num > 127)
                    num = 127;
                mevent.Notenumber = (byte) num;
                if (!keepchannel[mevent.Channel]) {
                    mevent.Velocity = 0;
                }
                if (!options.useDefaultInstruments) {
                    mevent.Instrument = (byte) instruments[mevent.Channel];
                }
                mevent.Tempo = options.tempo;
            }
        }
        if (options.pauseTime != 0) {
            newevents = StartAtPauseTime(newevents, options.pauseTime);
        }
        return newevents;
    }

    /**
     * Apply the given sheet music options to the MidiNotes.
     * Return the midi tracks with the changes applied.
     */
    public ArrayList<MidiTrack> ChangeMidiNotes(MidiOptions options) {
        ArrayList<MidiTrack> newtracks = new ArrayList<>();
        for (int track = 0; track < tracks.size(); track++) {
            if (options.tracks[track]) {
                newtracks.add(tracks.get(track).Clone());
            }
        }
        /* To make the sheet music look nicer, we round the start times
         * so that notes close together appear as a single chord.  We
         * also extend the note durations, so that we have longer notes
         * and fewer rest symbols.
         */
        TimeSignature time = timeSignature;
        if (options.time != null) {
            time = options.time;
        }
        MidiFile.RoundStartTimes(newtracks, options.combineInterval, timeSignature);
        MidiFile.RoundDurations(newtracks, time.getQuarter());
        if (options.twoStaffs) {
            newtracks = MidiFile.CombineToTwoTracks(newtracks, timeSignature.getMeasure());
        }
        if (options.shifttime != 0) {
            MidiFile.ShiftTime(newtracks, options.shifttime);
        }
        if (options.transpose != 0) {
            MidiFile.Transpose(newtracks, options.transpose);
        }
        return newtracks;
    }

    /**
     * Guess the measure length.  We assume that the measure
     * length must be between 0.5 seconds and 4 seconds.
     * Take all the note start times that fall between 0.5 and
     * 4 seconds, and return the starttimes.
     */
    public ListInt GuessMeasureLength() {
        ListInt result = new ListInt();

        int pulses_per_second = (int) (1000000.0 / timeSignature.getTempo() * timeSignature.getQuarter());
        int minmeasure = pulses_per_second / 2;  /* The minimum measure length in pulses */
        int maxmeasure = pulses_per_second * 4;  /* The maximum measure length in pulses */

        /* Get the start time of the first note in the midi file. */
        int firstnote = timeSignature.getMeasure() * 5;
        for (MidiTrack track : tracks) {
            if (firstnote > track.getNotes().get(0).getPulsesOfStartTime()) {
                firstnote = track.getNotes().get(0).getPulsesOfStartTime();
            }
        }

        /* interval = 0.06 seconds, converted into pulses */
        int interval = timeSignature.getQuarter() * 60000 / timeSignature.getTempo();

        for (MidiTrack track : tracks) {
            int prevtime = 0;
            for (MidiNote note : track.getNotes()) {
                if (note.getPulsesOfStartTime() - prevtime <= interval)
                    continue;

                prevtime = note.getPulsesOfStartTime();

                int time_from_firstnote = note.getPulsesOfStartTime() - firstnote;

                /* Round the time down to a multiple of 4 */
                time_from_firstnote = time_from_firstnote / 4 * 4;
                if (time_from_firstnote < minmeasure)
                    continue;
                if (time_from_firstnote > maxmeasure)
                    break;

                if (!result.contains(time_from_firstnote)) {
                    result.add(time_from_firstnote);
                }
            }
        }
        result.sort();
        return result;
    }

    /**
     * Return the last start time
     */
    public int EndTime() {
        int lastStart = 0;
        for (MidiTrack track : tracks) {
            if (track.getNotes().size() == 0) {
                continue;
            }
            int last = track.getNotes().get(track.getNotes().size() - 1).getPulsesOfStartTime();
            lastStart = Math.max(last, lastStart);
        }
        return lastStart;
    }

    /**
     * Return true if this midi file has lyrics
     */
    public boolean hasLyrics() {
        for (MidiTrack track : tracks) {
            if (track.getLyrics() != null) {
                return true;
            }
        }
        return false;
    }

    public List<FootBoard> getFootBoards() {
        return footBoards;
    }

    @Override
    public String toString() {
        String result = "Midi File tracks=" + tracks.size() + " quarter=" + quarterNote + "\n";
        result += timeSignature.toString() + "\n";
        for (MidiTrack track : tracks) {
            result += track.toString();
        }
        return result;
    }

}  /* End class MidiFile */
