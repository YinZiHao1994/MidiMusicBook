package yin.source.com.midimusicbook.midi.baseBean;

import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * @class MidiOptions
 * The MidiOptions class contains the available options for
 * modifying the sheet music and sound.  These options are collected
 * from the SettingsActivity, and are passed to the SheetMusic and
 * MidiPlayer classes.
 */
public class MidiOptions implements Serializable {

    // The possible values for showNoteLetters  // 展示的音符字母可能的值
    public static final int NoteNameNone = 0;// 没有音符名称
    public static final int NoteNameLetter = 1;// 字母样式的音符名称
    public static final int NoteNameFixedDoReMi = 2;//
    public static final int NoteNameMovableDoReMi = 3;//
    public static final int NoteNameFixedNumber = 4;// 混合的音符名称
    public static final int NoteNameMovableNumber = 5;// 可以移动的音符名称

    public boolean showPiano;
    /**
     * Display the piano
     */ // 显示钢琴
    public boolean[] tracks;
    /**
     * Which tracks to display (true = display)
     */ // 显示那个音轨
    public int[] instruments;
    /**
     * Which instruments to use per track
     */ // 每条音轨使用的乐器
    public boolean useDefaultInstruments;
    /**
     * If true, don't change instruments
     */ // 是否使用默认的乐器
    public boolean scrollVert;
    /**
     * Whether to scroll vertically or horizontally
     */ // 乐谱垂直滚动还是水平滚动
    public boolean largeNoteSize;
    /**
     * Display large or small note sizes
     */ // 显示大写的还是小写的音符
    public boolean twoStaffs;
    /**
     * Combine tracks into two staffs ?
     */  // 把音轨合并成两条五线谱
    public int showNoteLetters;
    /**
     * Show the letters (A, A#, etc) next to the notes
     */ // 在音符下边显示字母
    public boolean showLyrics;
    /**
     * Show the lyrics under each note
     */ // 在每个音符下边显示歌词
    public boolean showMeasures;
    /**
     * Show the measure numbers for each staff
     */ // 显示小节数
    public int shifttime;
    /**
     * Shift note starttimes by the given amount
     */ // 根据所给的数量换把音符开始节拍
    public int transpose;
    /**
     * Shift note key up/down by given amount
     */ // 根据所给的数量换把按键的抬起按下
    public int key;
    /**
     * Use the given KeySignature (NoteScale)
     */ // 使用提供的键拍号(音符音阶)
    public TimeSignature time;
    /**
     * Use the given time signature (null for default)
     */ // 使用提供的拍子记号(默认为null)
    public TimeSignature defaultTime;
    /**
     * The default time signature
     */ // 默认的拍子记号
    public int combineInterval;
    /**
     * Combine notes within given time interval (msec)
     */ // 根据提供的节奏(拍子)间隔合并音符
    public int colorRightHandShade;
    /**
     * The color to use for shading
     */ // 右手阴影颜色
    public int colorLeftHandShade;
    /**
     * The color to use for shading the left hand piano
     */ // 左手阴影颜色

    public boolean[] mute;
    /**
     * Which tracks to mute (true = mute)
     */ // 哪条音轨不发声
    public int tempo;
    /**
     * The tempo, in microseconds per quarter note
     */ // 速度(拍子)，每四分之一音符的微秒数
    public int pauseTime;
    /**
     * Start the midi music at the given pause time
     */ // 在提供的暂停时刻开始midi音乐

    public boolean playMeasuresInLoop;
    /**
     * play the selected measures in a loop
     */ // 循环播放选中的拍子(小节)
    public int playMeasuresInLoopStart;
    /**
     * Start measure to play in loop
     */ // 循环播放开始拍子
    public int playMeasuresInLoopEnd;
    /**
     * End measure to play in loop
     */ // 循环播放结束拍子
    public int lastMeasure;

    /**
     * The last measure in the song
     */ // 歌曲中的最后一个拍子
    public MidiOptions() {
    }

    /* Initialize the default settings/options for the given MidiFile */
    public MidiOptions(MidiFile midifile) {
        showPiano = true;
        int num_tracks = midifile.getTracks().size();// 音轨数量
        tracks = new boolean[num_tracks];// 显示那个音轨
        mute = new boolean[num_tracks];// 哪条音轨不发声 mute是静默的意思
        for (int i = 0; i < tracks.length; i++) {
            tracks[i] = true;// 音轨都显示
            mute[i] = false;// 音轨都发声
            if (midifile.getTracks().get(i).getInstrumentName().equals("Percussion")) {
                tracks[i] = false;// 如果是打击乐器音轨都不显示
                mute[i] = true;// 如果是打击乐器音轨都不发声
            }
        }
        useDefaultInstruments = true;// 使用默认的乐器
        instruments = new int[num_tracks];
        for (int i = 0; i < instruments.length; i++) {
            instruments[i] = midifile.getTracks().get(i).getInstrument(); // 每条音轨使用的乐器
        }
        scrollVert = true;// 五线谱垂直滚动
        largeNoteSize = true;// 大写的音符
        if (tracks.length != 2) {
            twoStaffs = true;// 如果没有两条音轨,把音轨合并成两条五线谱
        } else {
            twoStaffs = false;// 如果有两条音轨,不把音轨合并成两条五线谱
        }
        showNoteLetters = NoteNameNone;// 显示音符字母的样式=音符字母无样式
        showMeasures = false;// 显示拍子
        showLyrics = true;// 显示歌词
        shifttime = 0;// 换把音符开始节拍0
        transpose = 0;// 换把按键的抬起按下0
        time = null;// 拍子记号
        defaultTime = midifile.getTime();// 默认的拍子记号(分子、分母、节拍数)
        key = -1;// 使用提供的键拍号(音符音阶)
        combineInterval = 40;// 根据提供的节奏(拍子)间隔合并音符
        colorRightHandShade = Color.rgb(210, 205, 220);
        colorLeftHandShade = Color.rgb(150, 200, 220);


        tempo = midifile.getTime().getTempo();// 每四分之一节拍的微秒数
        pauseTime = 0;
        lastMeasure = midifile.EndTime() / midifile.getTime().getMeasure();
        playMeasuresInLoop = false;
        playMeasuresInLoopStart = 0;
        playMeasuresInLoopEnd = lastMeasure;
    }

    /* Convert this MidiOptions object into a JSON string. */
    public String toJson() {
        try {
            JSONObject json = new JSONObject();
            JSONArray jsonTracks = new JSONArray();
            for (boolean value : tracks) {
                jsonTracks.put(value);
            }
            JSONArray jsonMute = new JSONArray();
            for (boolean value : mute) {
                jsonMute.put(value);
            }
            JSONArray jsonInstruments = new JSONArray();
            for (int value : instruments) {
                jsonInstruments.put(value);
            }
            if (time != null) {
                JSONObject jsonTime = new JSONObject();
                jsonTime.put("numerator", time.getNumerator());
                jsonTime.put("denominator", time.getDenominator());
                jsonTime.put("quarter", time.getQuarter());
                jsonTime.put("tempo", time.getTempo());
                json.put("time", jsonTime);
            }

            json.put("versionCode", 7);
            json.put("tracks", jsonTracks);
            json.put("mute", jsonMute);
            json.put("instruments", jsonInstruments);
            json.put("useDefaultInstruments", useDefaultInstruments);
            json.put("scrollVert", scrollVert);
            json.put("showPiano", showPiano);
            json.put("showLyrics", showLyrics);
            json.put("twoStaffs", twoStaffs);
            json.put("showNoteLetters", showNoteLetters);
            json.put("transpose", transpose);
            json.put("key", key);
            json.put("combineInterval", combineInterval);
            json.put("colorRightHandShade", colorRightHandShade);
            json.put("colorLeftHandShade", colorLeftHandShade);
            json.put("showMeasures", showMeasures);
            json.put("playMeasuresInLoop", playMeasuresInLoop);
            json.put("playMeasuresInLoopStart", playMeasuresInLoopStart);
            json.put("playMeasuresInLoopEnd", playMeasuresInLoopEnd);

            return json.toString();
        } catch (JSONException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    /* Initialize the options from a json string */
    public static MidiOptions fromJson(String jsonString) {
        if (jsonString == null) {
            return null;
        }
        MidiOptions options = new MidiOptions();
        try {
            JSONObject json = new JSONObject(jsonString);
            JSONArray jsonTracks = json.getJSONArray("tracks");
            options.tracks = new boolean[jsonTracks.length()];
            for (int i = 0; i < options.tracks.length; i++) {
                options.tracks[i] = jsonTracks.getBoolean(i);
            }

            JSONArray jsonMute = json.getJSONArray("mute");
            options.mute = new boolean[jsonMute.length()];
            for (int i = 0; i < options.mute.length; i++) {
                options.mute[i] = jsonMute.getBoolean(i);
            }

            JSONArray jsonInstruments = json.getJSONArray("instruments");
            options.instruments = new int[jsonInstruments.length()];
            for (int i = 0; i < options.instruments.length; i++) {
                options.instruments[i] = jsonInstruments.getInt(i);
            }

            if (json.has("time")) {
                JSONObject jsonTime = json.getJSONObject("time");
                int numer = jsonTime.getInt("numerator");
                int denom = jsonTime.getInt("denominator");
                int quarter = jsonTime.getInt("quarter");
                int tempo = jsonTime.getInt("tempo");
                options.time = new TimeSignature(numer, denom, quarter, tempo);
            }

            options.useDefaultInstruments = json.getBoolean("useDefaultInstruments");
            options.scrollVert = json.getBoolean("scrollVert");
            options.showPiano = json.getBoolean("showPiano");
            options.showLyrics = json.getBoolean("showLyrics");
            options.twoStaffs = json.getBoolean("twoStaffs");
            options.showNoteLetters = json.getInt("showNoteLetters");
            options.transpose = json.getInt("transpose");
            options.key = json.getInt("key");
            options.combineInterval = json.getInt("combineInterval");
            options.colorRightHandShade = json.getInt("colorRightHandShade");
            options.colorLeftHandShade = json.getInt("colorLeftHandShade");
            options.showMeasures = json.getBoolean("showMeasures");
            options.playMeasuresInLoop = json.getBoolean("playMeasuresInLoop");
            options.playMeasuresInLoopStart = json.getInt("playMeasuresInLoopStart");
            options.playMeasuresInLoopEnd = json.getInt("playMeasuresInLoopEnd");
        } catch (Exception e) {
            return null;
        }
        return options;
    }


    /**
     * 将保存的设置并入到MidiOptions
     * Merge in the saved options to this MidiOptions.
     */
    public void merge(MidiOptions saved) {
        if (saved.tracks.length == tracks.length) {
            for (int i = 0; i < tracks.length; i++) {
                tracks[i] = saved.tracks[i];
            }
        }
        if (saved.mute.length == mute.length) {
            for (int i = 0; i < mute.length; i++) {
                mute[i] = saved.mute[i];
            }
        }
        if (saved.instruments.length == instruments.length) {
            for (int i = 0; i < instruments.length; i++) {
                instruments[i] = saved.instruments[i];
            }
        }
        if (saved.time != null) {
            time = new TimeSignature(saved.time.getNumerator(), saved.time.getDenominator(),
                    saved.time.getQuarter(), saved.time.getTempo());
        }

        useDefaultInstruments = saved.useDefaultInstruments;
        scrollVert = saved.scrollVert;
        showPiano = saved.showPiano;
        showLyrics = saved.showLyrics;
        twoStaffs = saved.twoStaffs;
        showNoteLetters = saved.showNoteLetters;
        transpose = saved.transpose;
        key = saved.key;
        combineInterval = saved.combineInterval;
        colorRightHandShade = saved.colorRightHandShade;
        colorLeftHandShade = saved.colorLeftHandShade;
        showMeasures = saved.showMeasures;
        playMeasuresInLoop = saved.playMeasuresInLoop;
        playMeasuresInLoopStart = saved.playMeasuresInLoopStart;
        playMeasuresInLoopEnd = saved.playMeasuresInLoopEnd;
    }


    @Override
    public String toString() {
        String result = "MidiOptions: tracks: ";
        for (int i = 0; i < tracks.length; i++) {
            result += tracks[i] + ", ";
        }
        result += " Instruments: ";
        for (int i = 0; i < instruments.length; i++) {
            result += instruments[i] + ", ";
        }
        result += " scrollVert " + scrollVert;
        result += " twoStaffs " + twoStaffs;
        result += " transpose" + transpose;
        result += " key " + key;
        result += " combine " + combineInterval;
        result += " tempo " + tempo;
        result += " pauseTime " + pauseTime;
        if (time != null) {
            result += " time " + time.toString();
        }
        return result;
    }

    public MidiOptions copy() {
        MidiOptions options = new MidiOptions();
        options.tracks = new boolean[tracks.length];
        for (int i = 0; i < tracks.length; i++) {
            options.tracks[i] = tracks[i];
        }
        options.mute = new boolean[mute.length];
        for (int i = 0; i < mute.length; i++) {
            options.mute[i] = mute[i];
        }
        options.instruments = new int[instruments.length];
        for (int i = 0; i < instruments.length; i++) {
            options.instruments[i] = instruments[i];
        }
        options.defaultTime = defaultTime;
        options.time = time;
        options.useDefaultInstruments = useDefaultInstruments;
        options.scrollVert = scrollVert;
        options.showPiano = showPiano;
        options.showLyrics = showLyrics;
        options.twoStaffs = twoStaffs;
        options.showNoteLetters = showNoteLetters;
        options.transpose = transpose;
        options.key = key;
        options.combineInterval = combineInterval;
        options.colorRightHandShade = colorRightHandShade;
        options.colorLeftHandShade = colorLeftHandShade;
        options.showMeasures = showMeasures;
        options.playMeasuresInLoop = playMeasuresInLoop;
        options.playMeasuresInLoopStart = playMeasuresInLoopStart;
        options.playMeasuresInLoopEnd = playMeasuresInLoopEnd;
        options.lastMeasure = lastMeasure;
        options.tempo = tempo;
        options.pauseTime = pauseTime;

        options.shifttime = shifttime;
        options.largeNoteSize = largeNoteSize;
        return options;
    }
}
