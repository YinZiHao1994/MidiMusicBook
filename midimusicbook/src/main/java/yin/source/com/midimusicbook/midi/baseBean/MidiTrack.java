package yin.source.com.midimusicbook.midi.baseBean;

import java.util.ArrayList;

/**
 * @class MidiTrack
 * The MidiTrack takes as input the raw MidiEvents for the track, and gets:
 * - The list of midi notes in the track. // 音轨中的音符列表
 * - The first instrument used in the track. // 音轨中使用的第一件乐器
 * <p/>
 * For each NoteOn event in the midi file, a new MidiNote is created
 * and added to the track, using the AddNote() method.
 * 为midi文件中的每一个音符开时间，一个新的MidiNote被创建并被使用AddNote方法添加到音轨中
 * <p/>
 * The NoteOff() method is called when a NoteOff event is encountered,
 * in order to update the duration of the MidiNote.
 * 当遇到一个NoteOff事件后NoteOff方法被调用,主要是为了更新MidiNote的音长
 */
public class MidiTrack {

    private int tracknum;
    /**
     * The track number // 音轨数
     */
    private ArrayList<MidiNote> notes;
    /**
     * List of Midi notes // midiNote列表
     */
    private int instrument;
    /**
     * Instrument for this track // 当前音轨的所属乐器
     */
    private ArrayList<MidiEvent> lyrics;
    /**
     * The lyrics in this track // 当前音轨的歌词
     */

    /**
     * Create an empty MidiTrack.  Used by the Clone method // 通过克隆的方式创建一个空MidiTrack
     */
    public MidiTrack(int tracknum) {
        this.tracknum = tracknum;
        notes = new ArrayList<MidiNote>(20);
        instrument = 0;
    }

    /**
     * Create a MidiTrack based on the Midi events.  Extract the NoteOn/NoteOff
     * events to gather the list of MidiNotes. // 根据Midi Events创建一个MidiTrack，将提炼出的音符开或者关的事件收集到MidiNotes中
     */
    public MidiTrack(ArrayList<MidiEvent> events, int trackNum) {
        this.tracknum = trackNum;// 音轨
        notes = new ArrayList<>(events.size());// midi调子
        instrument = 0;

        for (MidiEvent mevent : events) {
            if (mevent.EventFlag == MidiFile.EventNoteOn && mevent.Velocity > 0) {// 音符开,音符力度大于0
                MidiNote note = new MidiNote(mevent.StartTime, mevent.Channel, mevent.Notenumber, 0);
                note.setVelocity(mevent.Velocity);
                AddNote(note);
            } else if (mevent.EventFlag == MidiFile.EventNoteOn && mevent.Velocity == 0) {
                // 音符开，音符力度等于0
                NoteOff(mevent.Channel, mevent.Notenumber, mevent.StartTime);
            } else if (mevent.EventFlag == MidiFile.EventNoteOff) {// 音符关闭的状态
                NoteOff(mevent.Channel, mevent.Notenumber, mevent.StartTime);
            } else if (mevent.EventFlag == MidiFile.EventProgramChange) {// 事件为"乐器"
                instrument = mevent.Instrument;
            } else if (mevent.Metaevent == MidiFile.MetaEventLyric) {// 事件为"歌词"
                AddLyric(mevent);
                if (lyrics == null) {
                    lyrics = new ArrayList<MidiEvent>();
                }
                lyrics.add(mevent);
            }
        }
        if (notes.size() > 0 && notes.get(0).getChannel() == 9) {// 如果MidiNotes列表的长度大于零同时通道为9
            instrument = 128;  /* Percussion */  // 乐器为打击乐器
        }
    }

    public int trackNumber() {
        return tracknum;
    }

    public ArrayList<MidiNote> getNotes() {
        return notes;
    }

    public int getInstrument() {
        return instrument;
    }

    public void setInstrument(int value) {
        instrument = value;
    }

    public ArrayList<MidiEvent> getLyrics() {
        return lyrics;
    }

    public void setLyrics(ArrayList<MidiEvent> value) {
        lyrics = value;
    }


    public String getInstrumentName() {
        if (instrument >= 0 && instrument <= 128)
            return MidiFile.Instruments[instrument];
        else
            return "";
    }

    /**
     * Add a MidiNote to this track.  This is called for each NoteOn event
     */
    public void AddNote(MidiNote m) {
        notes.add(m);
    }

    /**
     * A NoteOff event occured.  Find the MidiNote of the corresponding
     * NoteOn event, and update the duration of the MidiNote.
     * 发生NoteOff事件。 找到相应的NoteOn事件的MidiNote，并更新MidiNote的持续时间。
     */
    public void NoteOff(int channel, int notenumber, int endtime) {
        for (int i = notes.size() - 1; i >= 0; i--) {
            MidiNote note = notes.get(i);
            if (note.getChannel() == channel && note.getNoteNumber() == notenumber &&
                    note.getDuration() == 0) {
                note.NoteOff(endtime);
                return;
            }
        }
    }

    /**
     * Add a lyric event to this track
     */
    public void AddLyric(MidiEvent mevent) {
        if (lyrics == null) {
            lyrics = new ArrayList<MidiEvent>();
        }
        lyrics.add(mevent);
    }

    /**
     * Return a deep copy clone of this MidiTrack.
     */
    public MidiTrack Clone() {
        MidiTrack track = new MidiTrack(trackNumber());
        track.instrument = instrument;
        for (MidiNote note : notes) {
            track.notes.add(note.Clone());
        }
        if (lyrics != null) {
            track.lyrics = new ArrayList<MidiEvent>();
            for (MidiEvent ev : lyrics) {
                track.lyrics.add(ev);
            }
        }
        return track;
    }

    @Override
    public String toString() {
        String result = "Track number=" + tracknum + " instrument=" + instrument + "\n";
        for (MidiNote n : notes) {
            result = result + n + "\n";
        }
        result += "End Track\n";
        return result;
    }
}
