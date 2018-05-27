/*
 * Copyright (c) 2007-2012 Madhav Vaidyanathan
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 */
package yin.source.com.midimusicbook.midi.musicBook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import yin.source.com.midimusicbook.midi.baseBean.ListInt;
import yin.source.com.midimusicbook.midi.baseBean.MidiEvent;
import yin.source.com.midimusicbook.midi.baseBean.MidiFile;
import yin.source.com.midimusicbook.midi.baseBean.MidiNote;
import yin.source.com.midimusicbook.midi.baseBean.MidiOptions;
import yin.source.com.midimusicbook.midi.baseBean.MidiTrack;
import yin.source.com.midimusicbook.midi.baseBean.NoteDuration;
import yin.source.com.midimusicbook.midi.baseBean.TimeSignature;

/**
 * @class BoxedInt
 **/
class BoxedInt {
    public int value;
}

/**
 * @class SheetMusic
 * <p/>
 * The SheetMusic Control is the main class for displaying the sheet music.
 * The SheetMusic class has the following public methods:
 * SheetMusic控制器是展示散页乐谱的主要类，SheetMusic类有以下的公共方法
 * <p/>
 * SheetMusic()
 * Create a new SheetMusic control from the given midi file and options.
 * 根据所给的midi文件和选项创建一个新的SheetMusic控制器
 * <p/>
 * onDraw()
 * Method called to draw the SheetMuisc
 * 绘制散页乐谱的方法
 * <p/>
 * shadeNotes()
 * Shade all the notes played at a given pulse time.
 * 阴影所有的在给定的时间播放的所有音符
 */
public class SheetMusic extends SurfaceView implements SurfaceHolder.Callback, ScrollAnimationListener, MidiPlayer.MidiPlayerCallback {

    /* Measurements used when drawing.  All measurements are in pixels. */
    public static final int LineWidth = 1;// 线的宽度
    /**
     * The width of a line
     */
    public static final int LeftMargin = 4;// 左边距
    /**
     * The left margin
     */
    public static final int LineSpace = 7;// 在乐谱中两条线之间的空隙
    /**
     * The space between lines in the staff
     */
    public static final int StaffHeight = LineSpace * 4 + LineWidth * 5;// 乐谱中5条线之间的高度
    /**
     * The height between the 5 horizontal lines of the staff
     */

    public static final int NoteHeight = LineSpace + LineWidth;// 一个完整音符的高度
    /**
     * The height of a whole note
     */
    public static final int NoteWidth = 3 * LineSpace / 2;// 一个完整音符的宽度
    /**
     * The width of a whole note
     */

    public static final int PageWidth = 800;// 每一页的宽度
    /**
     * The width of each page
     */
    public static final int PageHeight = 1050;// 每一页的高度(正在印刷的时候)
    /**
     * The height of each page (when printing)
     */
    public static final int TitleHeight = 14;// 第一页标题高度
    /**
     * Height of title on first page
     */

    public static final int ImmediateScroll = 1;// 立即的滚动
    public static final int GradualScroll = 2;// 平缓的滚动
    public static final int DontScroll = 3;//

    private ArrayList<Staff> staffs;// 需要展示的五线谱的数组
    /**
     * The array of staffs to display (from top to bottom)
     */
    private KeySignature mainkey;// 主要的音调符号
    /**
     * The main key signature
     */

    private String filename;// midi文件名称
    /**
     * The midi filename
     */
    private int numtracks;// 音轨的数量
    /**
     * The number of tracks
     */
    private float zoom;// 图像缩放比例
    /**
     * The zoom level to draw at (1.0 == 100%)
     */
//    private boolean scrollVert;// 是否是垂直滚动
    /**
     * Whether to scroll vertically or horizontally
     */
//    private int showNoteLetters;// 显示音符字母
    /**
     * Display the note letters
     */
    private int[] NoteColors;// 音符颜色集合
    /**
     * The note colors to use
     */
    private int shade1;// 用来变色的颜色1
    /**
     * The color for shading
     */
    private int shade2;// 用来变色的颜色2
    /**
     * The color for shading left-hand piano
     */
    private Paint paint;// 需要绘制的Paint
    /**
     * The paint for drawing
     */
    private boolean surfaceReady;// surface是否已经准备完毕
    /**
     * True if we can draw on the surface
     */
    private Bitmap bufferBitmap;// 用来画画的bitmap
    /**
     * The bitmap for drawing
     */
    private Canvas bufferCanvas;// 用来画画的画板
    /**
     * The canvas for drawing
     */
    private MidiPlayer player;// 用来暂停音乐
    /**
     * For pausing the music
     */
    private int playerHeight;// midi播放器的高度
//    /**
//     * Height of the midi player
//     */
//    private int screenwidth;// 屏幕宽度
//    /**
//     * The screen width
//     */
//    private int screenheight;// 屏幕高度
    /**
     * The screen height
     */

    /* fields used for scrolling */

    private int sheetwidth;// 散页乐谱的宽度(不考虑缩放)
    /**
     * The sheet music width (excluding zoom)
     */
    private int sheetheight;// 散页乐谱的高度(不考虑缩放)
    /**
     * The sheet music height (excluding zoom)
     */
    private int viewWidth;// 当前视图的宽度
    /**
     * The width of this view.
     */
    private int viewHeight;// 当前视图的高度
    /**
     * The height of this view.
     */
    private int bufferX;// bufferCanvas的左上坐标
    /**
     * The (left,top) of the bufferCanvas
     */
    private int bufferY;
    private int scrollX;// 滚动夹的左上坐标
    /**
     * The (left,top) of the scroll clip
     */
    private int scrollY;
    private ScrollAnimation scrollAnimation;// 滚动动画

    private Context context;
    private MidiOptions midiOptions;


    public SheetMusic(Context context) {
        this(context, null);
    }

    public SheetMusic(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SheetMusic(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        bufferX = bufferY = scrollX = scrollY = 0;

    }

    /**
     * Find 2, 3, 4, or 6 chord symbols that occur consecutively (without any
     * rests or bars in between).  There can be BlankSymbols in between.
     * <p/>
     * The startIndex is the index in the symbols to start looking from.
     * <p/>
     * Store the indexes of the consecutive chords in chordIndexes.
     * Store the horizontal distance (pixels) between the first and last chord.
     * If we failed to find consecutive chords, return false.
     */
    private static boolean findConsecutiveChords(ArrayList<MusicSymbol> symbols, TimeSignature time,
                                                 int startIndex, int[] chordIndexes, BoxedInt horizDistance) {
        int i = startIndex;
        int numChords = chordIndexes.length;
        while (true) {
            horizDistance.value = 0;
            /* Find the starting chord */
            while (i < symbols.size() - numChords) {
                if (symbols.get(i) instanceof ChordSymbol) {
                    ChordSymbol c = (ChordSymbol) symbols.get(i);
                    if (c.getStem() != null) {
                        break;
                    }
                }
                i++;
            }
            if (i >= symbols.size() - numChords) {
                chordIndexes[0] = -1;
                return false;
            }
            chordIndexes[0] = i;
            boolean foundChords = true;
            for (int chordIndex = 1; chordIndex < numChords; chordIndex++) {
                i++;
                int remaining = numChords - 1 - chordIndex;
                while ((i < symbols.size() - remaining) &&
                        (symbols.get(i) instanceof BlankSymbol)) {

                    horizDistance.value += symbols.get(i).getWidth();
                    i++;
                }
                if (i >= symbols.size() - remaining) {
                    return false;
                }
                if (!(symbols.get(i) instanceof ChordSymbol)) {
                    foundChords = false;
                    break;
                }
                chordIndexes[chordIndex] = i;
                horizDistance.value += symbols.get(i).getWidth();
            }
            if (foundChords) {
                return true;
            }
            /* Else, start searching again from index i */
        }
    }

    /**
     * Connect chords of the same duration with a horizontal beam.
     * numChords is the number of chords per beam (2, 3, 4, or 6).
     * if startBeat is true, the first chord must start on a quarter note beat.
     */
    private static void createBeamedChords(ArrayList<ArrayList<MusicSymbol>> allsymbols, TimeSignature time,
                                           int numChords, boolean startBeat) {
        int[] chordIndexes = new int[numChords];
        ChordSymbol[] chords = new ChordSymbol[numChords];
        for (ArrayList<MusicSymbol> symbols : allsymbols) {
            int startIndex = 0;
            while (true) {
                BoxedInt horizDistance = new BoxedInt();
                horizDistance.value = 0;
                boolean found = findConsecutiveChords(symbols, time,
                        startIndex,
                        chordIndexes,
                        horizDistance);
                if (!found) {
                    break;
                }
                for (int i = 0; i < numChords; i++) {
                    chords[i] = (ChordSymbol) symbols.get(chordIndexes[i]);
                }
                if (ChordSymbol.CanCreateBeam(chords, time, startBeat)) {
                    ChordSymbol.CreateBeam(chords, horizDistance.value);
                    startIndex = chordIndexes[numChords - 1] + 1;
                } else {
                    startIndex = chordIndexes[0] + 1;
                }
                /* What is the value of startIndex here?
                 * If we created a beam, we start after the last chord.
                 * If we failed to create a beam, we start after the first chord.
                 */
            }
        }
    }

    /**
     * Connect chords of the same duration with a horizontal beam.
     * <p/>
     * We create beams in the following order:
     * - 6 connected 8th note chords, in 3/4, 6/8, or 6/4 time
     * - Triplets that start on quarter note beats
     * - 3 connected chords that start on quarter note beats (12/8 time only)
     * - 4 connected chords that start on quarter note beats (4/4 or 2/4 time only)
     * - 2 connected chords that start on quarter note beats
     * - 2 connected chords that start on any beat
     */
    private static void createAllBeamedChords(ArrayList<ArrayList<MusicSymbol>> allsymbols, TimeSignature time) {
        if ((time.getNumerator() == 3 && time.getDenominator() == 4) ||
                (time.getNumerator() == 6 && time.getDenominator() == 8) ||
                (time.getNumerator() == 6 && time.getDenominator() == 4)) {
            createBeamedChords(allsymbols, time, 6, true);
        }
        createBeamedChords(allsymbols, time, 3, true);
        createBeamedChords(allsymbols, time, 4, true);
        createBeamedChords(allsymbols, time, 2, true);
        createBeamedChords(allsymbols, time, 2, false);
    }

    /**
     * Get the width (in pixels) needed to display the key signature
     */
    public static int keySignatureWidth(Context context, KeySignature key) {
        ClefSymbol clefsym = new ClefSymbol(context, Clef.Treble, 0, false);
        int result = clefsym.getMinWidth();
        AccidSymbol[] keys = key.GetSymbols(Clef.Treble);
        for (AccidSymbol symbol : keys) {
            result += symbol.getMinWidth();
        }
        return result + SheetMusic.LeftMargin + 5;
    }

    /**
     * Get the lyrics for each track
     */
    private static ArrayList<ArrayList<LyricSymbol>> getLyrics(ArrayList<MidiTrack> tracks) {
        boolean hasLyrics = false;
        ArrayList<ArrayList<LyricSymbol>> result = new ArrayList<>();
        for (int tracknum = 0; tracknum < tracks.size(); tracknum++) {
            ArrayList<LyricSymbol> lyrics = new ArrayList<LyricSymbol>();
            result.add(lyrics);
            MidiTrack track = tracks.get(tracknum);
            if (track.getLyrics() == null) {
                continue;
            }
            hasLyrics = true;
            for (MidiEvent ev : track.getLyrics()) {
                try {
                    String text = new String(ev.Value, 0, ev.Value.length, "UTF-8");
                    LyricSymbol sym = new LyricSymbol(ev.StartTime, text);
                    lyrics.add(sym);
                } catch (UnsupportedEncodingException e) {
                }
            }
        }
        if (!hasLyrics) {
            return null;
        } else {
            return result;
        }
    }

    /**
     * Add the lyric symbols to the corresponding staffs
     */
    static void addLyricsToStaffs(ArrayList<Staff> staffs, ArrayList<ArrayList<LyricSymbol>> tracklyrics) {
        for (Staff staff : staffs) {
            ArrayList<LyricSymbol> lyrics = tracklyrics.get(staff.getTrack());
            staff.AddLyrics(lyrics);
        }
    }

    /**
     * Create a new SheetMusic View.创建一个SheetMusic控件.
     * MidiFile is the parsed midi file to display.MidiFile是midi文件解析后用来展示的.
     * SheetMusic Options are the menu options that were selected.
     * <p/>
     * - Apply all the Menu Options to the MidiFile tracks.将所有的设置应用于所有的MidiFile音轨
     * - Calculate the key signature.计算音调符号
     * - For each track, create a list of MusicSymbols (notes, rests, bars, etc)
     * 为每一条音轨,创建一个MusicSymbols的集合
     * - Vertically align the music symbols in all the tracks.垂直对齐所有音轨中的音乐符号
     * - Partition the music notes into horizontal staffs.将音乐音符划分到水平的五线谱中
     */
    public void init(MidiFile file, MidiOptions options) {
        midiOptions = options;
        if (options == null) {
            options = new MidiOptions(file);
        }
        zoom = 1.0f;

        filename = file.getFileName();
        setColors(null, options.colorRightHandShade, options.colorLeftHandShade);
        paint = new Paint();
        paint.setTextSize(12.0f);
        Typeface typeface = Typeface.create(paint.getTypeface(), Typeface.NORMAL);
        paint.setTypeface(typeface);
        paint.setColor(Color.BLACK);

        ArrayList<MidiTrack> tracks = file.ChangeMidiNotes(options);
        // SetNoteSize(options.largeNoteSize);
        TimeSignature time = file.getTime();// 拍子记号
        if (options.time != null) {
            time = options.time;
        }
        if (options.key == -1) {
            mainkey = getKeySignature(tracks);
        } else {
            mainkey = new KeySignature(options.key);
        }
        numtracks = tracks.size();

        int lastStart = file.EndTime() + options.shifttime;

        /** Create all the music symbols (notes, rests, vertical bars, and
         * clef changes).  The symbols variable contains a list of music
         * symbols for each track.  The list does not include the left-side
         * Clef and key signature symbols.  Those can only be calculated
         * when we create the staffs.
         * 创建所有的音乐符号(音符,休止符,垂直的小节线,以及谱号).symbols变量包含每一个音轨的音乐符号.
         * 这个集合不包括左侧谱号和音调符号.那些只能在我们创建五线谱的时候计算
         */
        ArrayList<ArrayList<MusicSymbol>> allSymbols = new ArrayList<>(numtracks);
        for (int trackNum = 0; trackNum < numtracks; trackNum++) {
            MidiTrack track = tracks.get(trackNum);
            ClefMeasures clefs = new ClefMeasures(track.getNotes(), time.getMeasure());
            ArrayList<ChordSymbol> chords = createChords(track.getNotes(), mainkey, time, clefs);
            allSymbols.add(CreateSymbols(chords, clefs, time, lastStart));
        }
        ArrayList<ArrayList<LyricSymbol>> lyrics = null;
        if (options.showLyrics) {
            lyrics = getLyrics(tracks);
        }
        /* Vertically align the music symbols */ // 垂直对齐音乐符号
        SymbolWidths widths = new SymbolWidths(allSymbols, lyrics);
        alignSymbols(allSymbols, widths, options);
        staffs = createStaffs(allSymbols, mainkey, options, time.getMeasure());
        createAllBeamedChords(allSymbols, time);
        if (lyrics != null) {
            addLyricsToStaffs(staffs, lyrics);
        }
        /* After making chord pairs, the stem directions can change,
         * which affects the staff height.  Re-calculate the staff height.
         * 和弦配对后,影响了五线谱的高度,符干方向有可能改变,重新计算五线谱的高度.
         */
        for (Staff staff : staffs) {
            staff.CalculateHeight();
        }
        zoom = 1.0f;
        scrollAnimation = new ScrollAnimation(this, options.scrollVert);
    }

    /**
     * Calculate the size of the sheet music width and height 计算散页乐谱的宽度和高度(不考虑因为适配屏幕所使用的缩放)
     * (without zoom scaling to fit the screen).  Store the result in
     * sheetwidth and sheetheight.将结果保存到sheetwidth和sheetheight中
     */
    private void calculateSize() {
        sheetwidth = 0;
        sheetheight = 0;
        if (staffs == null) {
            return;
        }
        for (Staff staff : staffs) {
            sheetwidth = Math.max(sheetwidth, staff.getWidth());
            sheetheight += (staff.getHeight());
        }
        sheetwidth += 2;
        sheetheight += LeftMargin;
    }

    /**
     * If this is the first size change, calculate the zoom level,
     * and create the bufferCanvas.  Otherwise, do nothing.
     */
    @Override
    protected void onSizeChanged(int newwidth, int newheight, int oldwidth, int oldheight) {
        viewWidth = newwidth;
        viewHeight = newheight;
        if (bufferCanvas != null) {
            callOnDraw();
            return;
        }
        calculateSize();
        if (midiOptions.scrollVert) {
            zoom = (float) ((newwidth - 2) * 1.0 / PageWidth);
        } else {
            zoom = (float) ((newheight + playerHeight) * 1.0 / sheetheight);
            if (zoom < 0.9)
                zoom = 0.9f;
            if (zoom > 1.1)
                zoom = 1.1f;
        }
        if (bufferCanvas == null) {
            createBufferCanvas(viewWidth, viewHeight);
        }
        callOnDraw();


        playerHeight = MidiPlayer.getPreferredSize(viewWidth, viewHeight).y;
    }


    /**
     * Get the best key signature given the midi notes in all the tracks.
     */
    private KeySignature getKeySignature(ArrayList<MidiTrack> tracks) {
        ListInt notenums = new ListInt();
        for (MidiTrack track : tracks) {
            for (MidiNote note : track.getNotes()) {
                notenums.add(note.getNoteNumber());
            }
        }
        return KeySignature.Guess(notenums);
    }

    /**
     * Create the chord symbols for a single track.
     *
     * @param midinotes The Midinotes in the track.
     * @param key       The Key Signature, for determining sharps/flats.
     * @param time      The Time Signature, for determining the measures.
     * @param clefs     The clefs to use for each measure.
     * @ret An array of ChordSymbols
     */
    private ArrayList<ChordSymbol> createChords(ArrayList<MidiNote> midinotes,
                                                KeySignature key,
                                                TimeSignature time,
                                                ClefMeasures clefs) {
        int i = 0;
        ArrayList<ChordSymbol> chords = new ArrayList<>();
        ArrayList<MidiNote> notegroup = new ArrayList<>(12);
        int len = midinotes.size();
        while (i < len) {
            int starttime = midinotes.get(i).getPulsesOfStartTime();
            Clef clef = clefs.GetClef(starttime);
            /* Group all the midi notes with the same start time
             * into the notes list.
             */
            notegroup.clear();
            notegroup.add(midinotes.get(i));
            i++;
            while (i < len && midinotes.get(i).getPulsesOfStartTime() == starttime) {
                notegroup.add(midinotes.get(i));
                i++;
            }
            /* Create a single chord from the group of midi notes with
             * the same start time.
             */
            ChordSymbol chord = new ChordSymbol(notegroup, key, time, clef, this);
            chords.add(chord);
        }
        return chords;
    }

    /**
     * Given the chord symbols for a track, create a new symbol list
     * that contains the chord symbols, vertical bars, rests, and
     * clef changes.
     * Return a list of symbols (ChordSymbol, BarSymbol, RestSymbol, ClefSymbol)
     */
    private ArrayList<MusicSymbol> CreateSymbols(ArrayList<ChordSymbol> chords, ClefMeasures clefs,
                                                 TimeSignature time, int lastStart) {
        ArrayList<MusicSymbol> symbols = new ArrayList<>();
        symbols = AddBars(context, chords, time, lastStart);
        symbols = AddRests(context, symbols, time);
        symbols = AddClefChanges(context, symbols, clefs, time);
        return symbols;
    }

    /**
     * Add in the vertical bars delimiting measures.
     * Also, add the time signature symbols.
     */
    private ArrayList<MusicSymbol> AddBars(Context context, ArrayList<ChordSymbol> chords, TimeSignature time, int lastStart) {
        ArrayList<MusicSymbol> symbols = new ArrayList<>();
        TimeSigSymbol timesig = new TimeSigSymbol(this.context, time.getNumerator(), time.getDenominator());
        symbols.add(timesig);
        /* The starttime of the beginning of the measure */
        int measuretime = 0;
        int i = 0;
        while (i < chords.size()) {
            if (measuretime <= chords.get(i).getStartTime()) {
                symbols.add(new BarSymbol(measuretime));
                measuretime += time.getMeasure();
            } else {
                symbols.add(chords.get(i));
                i++;
            }
        }
        /* Keep adding bars until the last StartTime (the end of the song) */
        while (measuretime < lastStart) {
            symbols.add(new BarSymbol(measuretime));
            measuretime += time.getMeasure();
        }
        /* Add the final vertical bar to the last measure */
        symbols.add(new BarSymbol(measuretime));
        return symbols;
    }

    /**
     * Add rest symbols between notes.  All times below are
     * measured in pulses.
     */
    private ArrayList<MusicSymbol> AddRests(Context context, ArrayList<MusicSymbol> symbols, TimeSignature time) {
        int prevtime = 0;
        ArrayList<MusicSymbol> result = new ArrayList<>(symbols.size());
        for (MusicSymbol symbol : symbols) {
            int starttime = symbol.getStartTime();
            RestSymbol[] rests = GetRests(time, prevtime, starttime);
            if (rests != null) {
                for (RestSymbol r : rests) {
                    result.add(r);
                }
            }
            result.add(symbol);
            /* Set prevtime to the end time of the last note/symbol. */
            if (symbol instanceof ChordSymbol) {
                ChordSymbol chord = (ChordSymbol) symbol;
                prevtime = Math.max(chord.getEndTime(), prevtime);
            } else {
                prevtime = Math.max(starttime, prevtime);
            }
        }
        return result;
    }

    /**
     * Return the rest symbols needed to fill the time interval between
     * start and end.  If no rests are needed, return nil.
     */
    private RestSymbol[] GetRests(TimeSignature time, int start, int end) {
        RestSymbol[] result;
        RestSymbol r1, r2;
        if (end - start < 0)
            return null;
        NoteDuration dur = time.GetNoteDuration(end - start);
        switch (dur) {
            case Whole:
            case Half:
            case Quarter:
            case Eighth:
                r1 = new RestSymbol(start, dur);
                result = new RestSymbol[]{r1};
                return result;
            case DottedHalf:
                r1 = new RestSymbol(start, NoteDuration.Half);
                r2 = new RestSymbol(start + time.getQuarter() * 2,
                        NoteDuration.Quarter);
                result = new RestSymbol[]{r1, r2};
                return result;
            case DottedQuarter:
                r1 = new RestSymbol(start, NoteDuration.Quarter);
                r2 = new RestSymbol(start + time.getQuarter(),
                        NoteDuration.Eighth);
                result = new RestSymbol[]{r1, r2};
                return result;
            case DottedEighth:
                r1 = new RestSymbol(start, NoteDuration.Eighth);
                r2 = new RestSymbol(start + time.getQuarter() / 2,
                        NoteDuration.Sixteenth);
                result = new RestSymbol[]{r1, r2};
                return result;
            default:
                return null;
        }
    }

    /**
     * The current clef is always shown at the beginning of the staff, on
     * the left side.  However, the clef can also change from measure to
     * measure. When it does, a Clef symbol must be shown to indicate the
     * change in clef.  This function adds these Clef change symbols.
     * This function does not add the main Clef Symbol that begins each
     * staff.  That is done in the Staff() contructor.
     */
    private ArrayList<MusicSymbol> AddClefChanges(Context context, ArrayList<MusicSymbol> symbols,
                                                  ClefMeasures clefs,
                                                  TimeSignature time) {
        ArrayList<MusicSymbol> result = new ArrayList<>(symbols.size());
        Clef prevclef = clefs.GetClef(0);
        for (MusicSymbol symbol : symbols) {
            /* A BarSymbol indicates a new measure */
            if (symbol instanceof BarSymbol) {
                Clef clef = clefs.GetClef(symbol.getStartTime());
                if (clef != prevclef) {
                    result.add(new ClefSymbol(this.context, clef, symbol.getStartTime() - 1, true));
                }
                prevclef = clef;
            }
            result.add(symbol);
        }
        return result;
    }

    /**
     * Notes with the same start times in different staffs should be
     * vertically aligned.  The SymbolWidths class is used to help
     * vertically align symbols.
     * <p/>
     * First, each track should have a symbol for every starttime that
     * appears in the Midi File.  If a track doesn't have a symbol for a
     * particular starttime, then add a "blank" symbol for that time.
     * <p/>
     * Next, make sure the symbols for each start time all have the same
     * width, across all tracks.  The SymbolWidths class stores
     * - The symbol width for each starttime, for each track
     * - The maximum symbol width for a given starttime, across all tracks.
     * <p/>
     * The method SymbolWidths.GetExtraWidth() returns the extra width
     * needed for a track to match the maximum symbol width for a given
     * starttime.
     */
    private void alignSymbols(ArrayList<ArrayList<MusicSymbol>> allsymbols, SymbolWidths widths, MidiOptions options) {
        // If we show measure numbers, increase bar symbol width
        if (options.showMeasures) {
            for (int track = 0; track < allsymbols.size(); track++) {
                ArrayList<MusicSymbol> symbols = allsymbols.get(track);
                for (MusicSymbol sym : symbols) {
                    if (sym instanceof BarSymbol) {
                        sym.setWidth(sym.getWidth() + NoteWidth);
                    }
                }
            }
        }
        for (int track = 0; track < allsymbols.size(); track++) {
            ArrayList<MusicSymbol> symbols = allsymbols.get(track);
            ArrayList<MusicSymbol> result = new ArrayList<>();
            int i = 0;
            /* If a track doesn't have a symbol for a starttime,
             * add a blank symbol.
             */
            for (int start : widths.getStartTimes()) {
                /* BarSymbols are not included in the SymbolWidths calculations */
                while (i < symbols.size() && (symbols.get(i) instanceof BarSymbol) &&
                        symbols.get(i).getStartTime() <= start) {
                    result.add(symbols.get(i));
                    i++;
                }
                if (i < symbols.size() && symbols.get(i).getStartTime() == start) {
                    while (i < symbols.size() &&
                            symbols.get(i).getStartTime() == start) {
                        result.add(symbols.get(i));
                        i++;
                    }
                } else {
                    result.add(new BlankSymbol(start, 0));
                }
            }
            /* For each starttime, increase the symbol width by
             * SymbolWidths.GetExtraWidth().
             */
            i = 0;
            while (i < result.size()) {
                if (result.get(i) instanceof BarSymbol) {
                    i++;
                    continue;
                }
                int start = result.get(i).getStartTime();
                int extra = widths.GetExtraWidth(track, start);
                int newwidth = result.get(i).getWidth() + extra;
                result.get(i).setWidth(newwidth);
                /* Skip all remaining symbols with the same starttime. */
                while (i < result.size() && result.get(i).getStartTime() == start) {
                    i++;
                }
            }
            allsymbols.set(track, result);
        }
    }

    /**
     * Given MusicSymbols for a track, create the staffs for that track.
     * Each Staff has a maxmimum width of PageWidth (800 pixels).
     * Also, measures should not span multiple Staffs.
     */
    private ArrayList<Staff> createStaffsForTrack(ArrayList<MusicSymbol> symbols, int measurelen,
                                                  KeySignature key, MidiOptions options, int track, int totaltracks) {
        int keysigWidth = keySignatureWidth(context, key);
        int startindex = 0;
        ArrayList<Staff> thestaffs = new ArrayList<Staff>(symbols.size() / 50);
        while (startindex < symbols.size()) {
            /* startindex is the index of the first symbol in the staff.
             * endindex is the index of the last symbol in the staff.
             */
            int endindex = startindex;
            int width = keysigWidth;
            int maxwidth;
            /* If we're scrolling vertically, the maximum width is PageWidth. */
            if (midiOptions.scrollVert) {
                maxwidth = SheetMusic.PageWidth;
            } else {
                maxwidth = 2000000;
            }
            while (endindex < symbols.size() &&
                    width + symbols.get(endindex).getWidth() < maxwidth) {
                width += symbols.get(endindex).getWidth();
                endindex++;
            }
            endindex--;
            /* There's 3 possibilities at this point:
             * 1. We have all the symbols in the track.
             *    The endindex stays the same.
             *
             * 2. We have symbols for less than one measure.
             *    The endindex stays the same.
             *
             * 3. We have symbols for 1 or more measures.
             *    Since measures cannot span multiple staffs, we must
             *    make sure endindex does not occur in the middle of a
             *    measure.  We count backwards until we come to the end
             *    of a measure.
             */
            if (endindex == symbols.size() - 1) {
                /* endindex stays the same */
            } else if (symbols.get(startindex).getStartTime() / measurelen ==
                    symbols.get(endindex).getStartTime() / measurelen) {
                /* endindex stays the same */
            } else {
                int endmeasure = symbols.get(endindex + 1).getStartTime() / measurelen;
                while (symbols.get(endindex).getStartTime() / measurelen ==
                        endmeasure) {
                    endindex--;
                }
            }
            if (midiOptions.scrollVert) {
                width = SheetMusic.PageWidth;
            }
            // int range = endindex + 1 - startindex;
            ArrayList<MusicSymbol> staffSymbols = new ArrayList<MusicSymbol>();
            for (int i = startindex; i <= endindex; i++) {
                staffSymbols.add(symbols.get(i));
            }
            Staff staff = new Staff(context, staffSymbols, key, options, track, totaltracks);
            thestaffs.add(staff);
            startindex = endindex + 1;
        }
        return thestaffs;
    }

    /**
     * Given all the MusicSymbols for every track, create the staffs
     * for the sheet music.  There are two parts to this:
     * <p/>
     * - Get the list of staffs for each track.
     * The staffs will be stored in trackstaffs as:
     * <p/>
     * trackstaffs[0] = { Staff0, Staff1, Staff2, ... } for track 0
     * trackstaffs[1] = { Staff0, Staff1, Staff2, ... } for track 1
     * trackstaffs[2] = { Staff0, Staff1, Staff2, ... } for track 2
     * <p/>
     * - Store the Staffs in the staffs list, but interleave the
     * tracks as follows:
     * <p/>
     * staffs = { Staff0 for track 0, Staff0 for track1, Staff0 for track2,
     * Staff1 for track 0, Staff1 for track1, Staff1 for track2,
     * Staff2 for track 0, Staff2 for track1, Staff2 for track2,
     * ... }
     */
    private ArrayList<Staff> createStaffs(ArrayList<ArrayList<MusicSymbol>> allsymbols, KeySignature key,
                                          MidiOptions options, int measurelen) {
        ArrayList<ArrayList<Staff>> trackStaffs = new ArrayList<>(allsymbols.size());
        int totalTracks = allsymbols.size();
        for (int track = 0; track < totalTracks; track++) {
            ArrayList<MusicSymbol> symbols = allsymbols.get(track);
            trackStaffs.add(createStaffsForTrack(symbols, measurelen, key, options, track, totalTracks));
        }
        /* Update the EndTime of each Staff. EndTime is used for playback */
        for (ArrayList<Staff> list : trackStaffs) {
            for (int i = 0; i < list.size() - 1; i++) {
                list.get(i).setEndTime(list.get(i + 1).getStartTime());
            }
        }
        /* Interleave the staffs of each track into the result array. */
        int maxStaffs = 0;
        for (int i = 0; i < trackStaffs.size(); i++) {
            if (maxStaffs < trackStaffs.get(i).size()) {
                maxStaffs = trackStaffs.get(i).size();
            }
        }
        ArrayList<Staff> result = new ArrayList<Staff>(maxStaffs * trackStaffs.size());
        for (int i = 0; i < maxStaffs; i++) {
            for (ArrayList<Staff> list : trackStaffs) {
                if (i < list.size()) {
                    result.add(list.get(i));
                }
            }
        }
        return result;
    }

    /**
     * Change the note colors for the sheet music, and redraw.
     * This is not currently used.
     */
    public void setColors(int[] newcolors, int newshade1, int newshade2) {
        if (NoteColors == null) {
            NoteColors = new int[12];
            for (int i = 0; i < 12; i++) {
                NoteColors[i] = Color.BLACK;
            }
        }
        if (newcolors != null) {
            for (int i = 0; i < 12; i++) {
                NoteColors[i] = newcolors[i];
            }
        }
        shade1 = newshade1;
        shade2 = newshade2;
    }

    /**
     * Get the color for a given note number. Not currently used.
     */
    public int NoteColor(int number) {
        return NoteColors[NoteScale.FromNumber(number)];
    }

    /**
     * Get the shade color
     */
    public int getShade1() {
        return shade1;
    }

    /**
     * Get the shade2 color
     */
    public int getShade2() {
        return shade2;
    }

    /**
     * Get whether to show note letters or not
     */
    public int getShowNoteLetters() {
        return (midiOptions == null ? 0 : midiOptions.showNoteLetters);
    }

    /**
     * Get the main key signature
     */
    public KeySignature getMainKey() {
        return mainkey;
    }

    /**
     * Create a bitmap/canvas to use for double-buffered drawing.
     * This is needed for shading the notes quickly.
     * Instead of redrawing the entire sheet music on every shade call,
     * we draw the sheet music to this bitmap canvas.  On subsequent
     * calls to ShadeNotes(), we only need to draw the delta (the
     * new notes to shade/unshade) onto the bitmap, and then draw the bitmap.
     * <p/>
     * We include the MidiPlayer height (since we hide the MidiPlayer
     * once the music starts playing). Also, we make the bitmap twice as
     * large as the scroll viewable area, so that we don't need to
     * refresh the bufferCanvas on every scroll change.
     *
     * @param width
     * @param height
     */
    void createBufferCanvas(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        if (bufferBitmap != null) {
            bufferCanvas = null;
            bufferBitmap.recycle();
            bufferBitmap = null;
        }
        if (midiOptions.scrollVert) {
            bufferBitmap = Bitmap.createBitmap(width,
                    (height + playerHeight) * 2,
                    Bitmap.Config.ARGB_8888);
        } else {
            bufferBitmap = Bitmap.createBitmap(width * 2,
                    (height + playerHeight) * 2,
                    Bitmap.Config.ARGB_8888);
        }
        bufferCanvas = new Canvas(bufferBitmap);
        drawToBuffer(scrollX, scrollY);
    }

    /**
     * Obtain the drawing canvas and call onDraw()
     */
    public void callOnDraw() {
        if (!surfaceReady) {
            return;
        }
        SurfaceHolder holder = getHolder();
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            return;
        }
//        onDraw(canvas);
        if (bufferBitmap == null) {
            createBufferCanvas(viewWidth, viewHeight);
        }


        //todo 去除条件后，保证每一次调用此方法都会重新绘制整个画面，使改变一些参数后能使画面配套生效。不确定无脑去除原条件后对性能会有什么影响暂时这么处理
//        if (!isScrollPositionInBuffer()) {
            drawToBuffer(scrollX, scrollY);
//        }
        // We want (scrollX - bufferX, scrollY - bufferY)
        // to be (0,0) on the canvas
        canvas.translate(-(scrollX - bufferX), -(scrollY - bufferY));
        canvas.drawBitmap(bufferBitmap, 0, 0, paint);
        canvas.translate(scrollX - bufferX, scrollY - bufferY);
        holder.unlockCanvasAndPost(canvas);
    }

    /**
     * Return true if the scrollX/scrollY is in the bufferBitmap
     */
    private boolean isScrollPositionInBuffer() {
        if ((scrollY < bufferY) || (scrollX < bufferX) ||
                (scrollY > bufferY + bufferBitmap.getHeight() / 3) ||
                (scrollX > bufferX + bufferBitmap.getWidth() / 3)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Draw the SheetMusic to the bufferCanvas, with the
     * given (left,top) corner.
     * <p/>
     * Scale the graphics by the current zoom factor.
     * Only draw Staffs which lie inside the buffer area.
     */
    private void drawToBuffer(int left, int top) {
        if (staffs == null) {
            return;
        }
        bufferX = left;
        bufferY = top;
        bufferCanvas.translate(-bufferX, -bufferY);
        Rect clip = new Rect(bufferX, bufferY, bufferX + bufferBitmap.getWidth(),
                bufferY + bufferBitmap.getHeight());
        // Scale both the canvas and the clip by the zoom factor
        clip.left = (int) (clip.left / zoom);
        clip.top = (int) (clip.top / zoom);
        clip.right = (int) (clip.right / zoom);
        clip.bottom = (int) (clip.bottom / zoom);
        bufferCanvas.scale(zoom, zoom);

        // Draw a white background
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        bufferCanvas.drawRect(clip.left, clip.top, clip.right, clip.bottom, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);

        // Draw the staffs in the clip area
        int ypos = 0;
        for (Staff staff : staffs) {
            if ((ypos + staff.getHeight() < clip.top) || (ypos > clip.bottom)) {
                /* Staff is not in the clip, don't need to draw it */
            } else {
                bufferCanvas.translate(0, ypos);
                staff.Draw(bufferCanvas, clip, paint);
                bufferCanvas.translate(0, -ypos);
            }
            ypos += staff.getHeight();
        }
        bufferCanvas.scale(1.0f / zoom, 1.0f / zoom);
        bufferCanvas.translate(bufferX, bufferY);
    }


    /**
     * Write the MIDI filename at the top of the page
     */
    private void DrawTitle(Canvas canvas) {
        int leftmargin = 20;
        int topmargin = 20;
        String title = filename;
        title = title.replace(".mid", "").replace("_", " ");
        canvas.translate(leftmargin, topmargin);
        canvas.drawText(title, 0, 0, paint);
        canvas.translate(-leftmargin, -topmargin);
    }

    /**
     * Return the number of pages needed to print this sheet music.
     * A staff should fit within a single page, not be split across two pages.
     * If the sheet music has exactly 2 tracks, then two staffs should
     * fit within a single page, and not be split across two pages.
     */
    public int GetTotalPages() {
        int num = 1;
        int currheight = TitleHeight;
        if (numtracks == 2 && (staffs.size() % 2) == 0) {
            for (int i = 0; i < staffs.size(); i += 2) {
                int heights = staffs.get(i).getHeight() + staffs.get(i + 1).getHeight();
                if (currheight + heights > PageHeight) {
                    num++;
                    currheight = heights;
                } else {
                    currheight += heights;
                }
            }
        } else {
            for (Staff staff : staffs) {
                if (currheight + staff.getHeight() > PageHeight) {
                    num++;
                    currheight = staff.getHeight();
                } else {
                    currheight += staff.getHeight();
                }
            }
        }
        return num;
    }

    /**
     * Draw the given page of the sheet music.
     * Page numbers start from 1.
     * A staff should fit within a single page, not be split across two pages.
     * If the sheet music has exactly 2 tracks, then two staffs should
     * fit within a single page, and not be split across two pages.
     */
    public void DrawPage(Canvas canvas, int pagenumber) {
        int leftmargin = 20;
        int topmargin = 20;
        //int rightmargin = 20;
        //int bottommargin = 20;
        //float scale = 1.0f;
        Rect clip = new Rect(0, 0, PageWidth + 40, PageHeight + 40);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawRect(clip.left, clip.top, clip.right, clip.bottom, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        int ypos = TitleHeight;
        int pagenum = 1;
        int staffnum = 0;
        if (numtracks == 2 && (staffs.size() % 2) == 0) {
            /* Skip the staffs until we reach the given page number */
            while (staffnum + 1 < staffs.size() && pagenum < pagenumber) {
                int heights = staffs.get(staffnum).getHeight() +
                        staffs.get(staffnum + 1).getHeight();
                if (ypos + heights >= PageHeight) {
                    pagenum++;
                    ypos = 0;
                } else {
                    ypos += heights;
                    staffnum += 2;
                }
            }
            /* Print the staffs until the height reaches PageHeight */
            if (pagenum == 1) {
                DrawTitle(canvas);
                ypos = TitleHeight;
            } else {
                ypos = 0;
            }
            for (; staffnum + 1 < staffs.size(); staffnum += 2) {
                int heights = staffs.get(staffnum).getHeight() +
                        staffs.get(staffnum + 1).getHeight();

                if (ypos + heights >= PageHeight)
                    break;
                canvas.translate(leftmargin, topmargin + ypos);
                staffs.get(staffnum).Draw(canvas, clip, paint);
                canvas.translate(-leftmargin, -(topmargin + ypos));
                ypos += staffs.get(staffnum).getHeight();
                canvas.translate(leftmargin, topmargin + ypos);
                staffs.get(staffnum + 1).Draw(canvas, clip, paint);
                canvas.translate(-leftmargin, -(topmargin + ypos));
                ypos += staffs.get(staffnum + 1).getHeight();
            }
        } else {
            /* Skip the staffs until we reach the given page number */
            while (staffnum < staffs.size() && pagenum < pagenumber) {
                if (ypos + staffs.get(staffnum).getHeight() >= PageHeight) {
                    pagenum++;
                    ypos = 0;
                } else {
                    ypos += staffs.get(staffnum).getHeight();
                    staffnum++;
                }
            }
            /* Print the staffs until the height reaches viewPageHeight */
            if (pagenum == 1) {
                DrawTitle(canvas);
                ypos = TitleHeight;
            } else {
                ypos = 0;
            }
            for (; staffnum < staffs.size(); staffnum++) {
                if (ypos + staffs.get(staffnum).getHeight() >= PageHeight)
                    break;
                canvas.translate(leftmargin, topmargin + ypos);
                staffs.get(staffnum).Draw(canvas, clip, paint);
                canvas.translate(-leftmargin, -(topmargin + ypos));
                ypos += staffs.get(staffnum).getHeight();
            }
        }
        /* Draw the page number */
        canvas.drawText("" + pagenumber, PageWidth - leftmargin, topmargin + PageHeight - 12, paint);
    }


    /**
     * Shade all the chords played at the given pulse time.
     * First, make sure the current scroll position is in the bufferBitmap.
     * Loop through all the staffs and call staff.Shade().
     * If scrollGradually is true, scroll gradually (smooth scrolling)
     * to the shaded notes.
     */
    public void ShadeNotes(int currentPulseTime, int prevPulseTime, int scrollType) {
        if (!surfaceReady || staffs == null) {
            return;
        }
        if (bufferCanvas == null) {
            createBufferCanvas(viewWidth, viewHeight);
        }

        /* If the scroll position is not in the bufferCanvas,
         * we need to redraw the sheet music into the bufferCanvas
         */
        if (!isScrollPositionInBuffer()) {
            drawToBuffer(scrollX, scrollY);
        }

        /* We're going to draw the shaded notes into the bufferCanvas.
         * Translate, so that (bufferX, bufferY) maps to (0,0) on the canvas
         */
        bufferCanvas.translate(-bufferX, -bufferY);

        /* Loop through each staff.  Each staff will shade any notes that
         * start at currentPulseTime, and unshade notes at prevPulseTime.
         */
        int x_shade = 0;
        int y_shade = 0;
        paint.setAntiAlias(true);
        bufferCanvas.scale(zoom, zoom);
        int ypos = 0;
        for (Staff staff : staffs) {
            bufferCanvas.translate(0, ypos);
            x_shade = staff.ShadeNotes(bufferCanvas, paint, shade1,
                    currentPulseTime, prevPulseTime, x_shade);
            bufferCanvas.translate(0, -ypos);
            ypos += staff.getHeight();
            if (currentPulseTime >= staff.getEndTime()) {
                y_shade += staff.getHeight();
            }
        }
        bufferCanvas.scale(1.0f / zoom, 1.0f / zoom);
        bufferCanvas.translate(bufferX, bufferY);

        /* We have the (x,y) position of the shaded notes.
         * Calculate the new scroll position.
         */
        if (currentPulseTime >= 0) {
            x_shade = (int) (x_shade * zoom);
            y_shade -= NoteHeight;
            y_shade = (int) (y_shade * zoom);
            if (scrollType == ImmediateScroll) {
                ScrollToShadedNotes(x_shade, y_shade, false);
            } else if (scrollType == GradualScroll) {
                ScrollToShadedNotes(x_shade, y_shade, true);
            } else if (scrollType == DontScroll) {
            }
        }

        /* If the new scrollX, scrollY is not in the buffer,
         * we have to call this method again.
         */
        if (scrollX < bufferX || scrollY < bufferY) {
            ShadeNotes(currentPulseTime, prevPulseTime, scrollType);
            return;
        }

        /* Draw the buffer canvas to the real canvas.
         * Translate canvas such that (scrollX,scrollY) within the
         * bufferCanvas maps to (0,0) on the real canvas.
         */
        SurfaceHolder holder = getHolder();
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            return;
        }
        canvas.translate(-(scrollX - bufferX), -(scrollY - bufferY));
        canvas.drawBitmap(bufferBitmap, 0, 0, paint);
        canvas.translate(scrollX - bufferX, scrollY - bufferY);
        holder.unlockCanvasAndPost(canvas);
    }

    /**
     * Scroll the sheet music so that the shaded notes are visible.
     * If scrollGradually is true, scroll gradually (smooth scrolling)
     * to the shaded notes. Update the scrollX/scrollY fields.
     */
    void ScrollToShadedNotes(int x_shade, int y_shade, boolean scrollGradually) {
        if (midiOptions.scrollVert) {
            int scrollDist = (int) (y_shade - scrollY);

            if (scrollGradually) {
                if (scrollDist > (zoom * StaffHeight * 8))
                    scrollDist = scrollDist / 2;
                else if (scrollDist > (NoteHeight * 4 * zoom))
                    scrollDist = (int) (NoteHeight * 4 * zoom);
            }
            scrollY += scrollDist;
        } else {

            int x_view = scrollX + viewWidth * 40 / 100;
            int xmax = scrollX + viewWidth * 65 / 100;
            int scrollDist = x_shade - x_view;

            if (scrollGradually) {
                if (x_shade > xmax)
                    scrollDist = (x_shade - x_view) / 3;
                else if (x_shade > x_view)
                    scrollDist = (x_shade - x_view) / 6;
            }

            scrollX += scrollDist;
        }
        checkScrollBounds();
    }

    /**
     * Return the pulseTime corresponding to the given point on the SheetMusic.
     * First, find the staff corresponding to the point.
     * Then, within the staff, find the notes/symbols corresponding to the point,
     * and return the StartTime (pulseTime) of the symbols.
     */
    public int PulseTimeForPoint(Point point) {
        Point scaledPoint = new Point((int) (point.x / zoom), (int) (point.y / zoom));
        int y = 0;
        for (Staff staff : staffs) {
            if (scaledPoint.y >= y && scaledPoint.y <= y + staff.getHeight()) {
                return staff.PulseTimeForPoint(scaledPoint);
            }
            y += staff.getHeight();
        }
        return -1;
    }


    /**
     * Check that the scrollX/scrollY position does not exceed
     * the bounds of the sheet music.
     */
    private void checkScrollBounds() {
        // Get the width/height of the scrollable area
        int scrollwidth = (int) (sheetwidth * zoom);
        int scrollheight = (int) (sheetheight * zoom);

        if (scrollX < 0) {
            scrollX = 0;
        }
        if (scrollX > scrollwidth - viewWidth / 2) {
            scrollX = scrollwidth - viewWidth / 2;
        }

        if (scrollY < 0) {
            scrollY = 0;
        }
        if (scrollY > scrollheight - viewHeight / 2) {
            scrollY = scrollheight - viewHeight / 2;
        }
    }


    /**
     * Handle touch/motion events to implement scrolling the sheet music.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        boolean result = scrollAnimation.onTouchEvent(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // If we touch while music is playing, stop the midi player 
                if (player != null && player.getPlayState() == MidiPlayer.PlayState.PLAYING) {
                    player.pause();
                    scrollAnimation.stopMotion();
                }
                return result;

            case MotionEvent.ACTION_MOVE:
                return result;

            case MotionEvent.ACTION_UP:
                return result;

            default:
                return false;
        }
    }


    /**
     * Update the scroll position. Callback by ScrollAnimation
     */
    public void scrollUpdate(int deltaX, int deltaY) {
        scrollX += deltaX;
        scrollY += deltaY;
        checkScrollBounds();
        callOnDraw();
    }

    /**
     * When the scroll is tapped, highlight the position tapped
     */
    public void scrollTapped(int x, int y) {
        if (player != null) {
            player.MoveToClicked(scrollX + x, scrollY + y);

            int i = PulseTimeForPoint(new Point(x, y));
            player.setCurrentPulseTime(i);
        }
    }

    public void setPlayer(MidiPlayer p) {
        player = p;
        player.addMidiPlayerCallback(this);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        callOnDraw();
    }

    /**
     * Surface is ready for shading the notes
     */
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceReady = true;
    }

    /**
     * Surface has been destroyed
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceReady = false;
    }

    @Override
    public String toString() {
        String result = "SheetMusic staffs=" + staffs.size() + "\n";
        for (Staff staff : staffs) {
            result += staff.toString();
        }
        result += "End SheetMusic\n";
        return result;
    }


//    @Override
//    public void onPlayerStop() {
//        ShadeNotes(-10, (int) player.getPrevPulseTime(), SheetMusic.DontScroll);
//        ShadeNotes(-10, (int) player.getCurrentPulseTime(), SheetMusic.DontScroll);
//    }
//
//    @Override
//    public void onPlayerRewind() {
//        double currentPulseTime = player.getCurrentPulseTime();
//        double prevPulseTime = player.getPrevPulseTime();
//        /* Remove any highlighted notes */
//        ShadeNotes(-10, (int) currentPulseTime, SheetMusic.DontScroll);
//        ShadeNotes((int) currentPulseTime, (int) prevPulseTime, SheetMusic.ImmediateScroll);
//    }
//
//    @Override
//    public void onPlayerFastForward() {
//        double currentPulseTime = player.getCurrentPulseTime();
//        double prevPulseTime = player.getPrevPulseTime();
//        /* Remove any highlighted notes */
//        ShadeNotes(-10, (int) currentPulseTime, SheetMusic.DontScroll);
//        ShadeNotes((int) currentPulseTime, (int) prevPulseTime, SheetMusic.ImmediateScroll);
//    }
//
//    @Override
//    public void onPlayerMoveToClicked() {
//        double currentPulseTime = player.getCurrentPulseTime();
//        double prevPulseTime = player.getPrevPulseTime();
//        /* Remove any highlighted notes */
//        ShadeNotes(-10, (int) currentPulseTime, SheetMusic.DontScroll);
//        ShadeNotes((int) currentPulseTime, (int) prevPulseTime, SheetMusic.DontScroll);
//    }
//
//    @Override
//    public void onPlayerRestartPlayMeasuresInLoop() {
//        double prevPulseTime = player.getPrevPulseTime();
//        ShadeNotes(-10, (int) prevPulseTime, SheetMusic.DontScroll);
//    }
//
//    @Override
//    public void onPlayerSetMidiFile() {
//        double currentPulseTime = player.getCurrentPulseTime();
//        ShadeNotes((int) currentPulseTime, (int) -1, SheetMusic.DontScroll);
//    }
//
//    @Override
//    public void onPlayerPlay() {
//        double prevPulseTime = player.getPrevPulseTime();
//        double currentPulseTime = player.getCurrentPulseTime();
//        ShadeNotes((int) currentPulseTime, (int) prevPulseTime, SheetMusic.GradualScroll);
//
//    }

    @Override
    public void onSheetNeedShadeNotes(int currentPulseTime, int prevPulseTime, int gradualScroll) {
        ShadeNotes(currentPulseTime, prevPulseTime, gradualScroll);
    }

    @Override
    public void onPianoNeedShadeNotes(int currentPulseTime, int prevPulseTime) {

    }
}

