/*
 * Copyright (c) 2009-2011 Madhav Vaidyanathan
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
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

import yin.source.com.midimusicbook.R;
import yin.source.com.midimusicbook.midi.baseBean.MidiFile;
import yin.source.com.midimusicbook.midi.baseBean.MidiNote;
import yin.source.com.midimusicbook.midi.baseBean.MidiOptions;
import yin.source.com.midimusicbook.midi.baseBean.MidiTrack;


/**
 * @class Piano
 * <p/>
 * The Piano Control is the panel at the top that displays the
 * piano, and highlights the piano notes during playback.
 * The main methods are:
 * <p/>
 * setMidiFile() - Set the Midi file to use for shading.  The Midi file
 * is needed to determine which notes to shade.
 * <p/>
 * ShadeNotes() - Shade notes on the piano that occur at a given pulse time.
 */
public class Piano extends SurfaceView implements SurfaceHolder.Callback, MidiPlayer.MidiPlayerCallback {
    //每个八度音阶的白键个数
    public static final int KeysPerOctave = 7;
    //八度音阶数
    public static final int MaxOctave = 6;

    private static int WhiteKeyWidth;
    /**
     * Width of a single white key // 白键宽度
     */
    private static int WhiteKeyHeight;
    /**
     * Height of a single white key  // 白键高度
     */
    private static int BlackKeyWidth;
    /**
     * Width of a single black key   // 黑键宽度
     */
    private static int BlackKeyHeight;
    /**
     * Height of a single black key  // 黑键高度
     */
    private static int margin;
    /**
     * The top/left margin to the piano (0)  // 钢琴左边和顶部的外边缘
     */
    private static int BlackBorder;
    /**
     * The width of the black border around the keys // 键周围黑边的宽度
     */

    private static int[] blackKeyOffsets;
    /**
     * The x pixles of the black keys  // 黑键的x坐标
     */

    /* The colors for drawing black/gray lines */
    private int colorDarkGray, colorGray, colorLightGray, colorLeftHandShade, colorRightHandShade;

    private boolean useTwoColors;
    /**
     * If true, use two colors for highlighting
     */
    private ArrayList<MidiNote> notes;
    /**
     * The Midi notes for shading
     */
    private int maxShadeDuration;
    /**
     * The maximum duration we'll shade a note for // 最长的按下时间
     */
    private int showNoteLetters;
    /**
     * Display the letter for each piano note // 显示每个钢琴音的字母
     */
    private Paint paint;
    /**
     * The paint options for drawing
     */
    private boolean surfaceReady;
    /**
     * True if we can draw on the surface
     */
    private Bitmap bufferBitmap;
    /**
     * The bitmap for double-buffering // 双缓冲的为位图
     */
    private Canvas bufferCanvas;
    /**
     * The canvas for double-buffering // 双缓冲的画板
     */
    private MidiPlayer player;
    /**
     * Used to pause the player
     */
    private PianoListener mPianoListener;

    /**
     * Create a new Piano.
     */
    public Piano(Context context) {
        this(context, null);
    }

    public Piano(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Piano(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        WhiteKeyWidth = 0;
        blackKeyOffsets = null;
        paint = new Paint();
        paint.setTextSize(10.0f);
        paint.setAntiAlias(false);

        colorDarkGray = ContextCompat.getColor(context, R.color.dark_gray);
        colorGray = ContextCompat.getColor(context, R.color.gray);
        colorLightGray = ContextCompat.getColor(context, R.color.light_gray);
        colorLeftHandShade = ContextCompat.getColor(context, R.color.left_hand_shade_color);
        colorRightHandShade = ContextCompat.getColor(context, R.color.right_hand_shade_color);
        showNoteLetters = MidiOptions.NoteNameNone;

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
    }


    /**
     * Set the measured width and height
     */
    @Override
    protected void onMeasure(int widthspec, int heightspec) {
        super.onMeasure(widthspec, heightspec);
        int screenwidth = MeasureSpec.getSize(widthspec);
        int screenheight = MeasureSpec.getSize(heightspec);
        margin = 0;

        WhiteKeyWidth = (screenwidth - margin * 2) / (1 + KeysPerOctave * MaxOctave);
        int totalWhiteKeyWith = WhiteKeyWidth * KeysPerOctave * MaxOctave;

        //左右黑边宽度
        BlackBorder = (screenwidth - totalWhiteKeyWith) / 2;
        WhiteKeyHeight = WhiteKeyWidth * 5;
        BlackKeyWidth = WhiteKeyWidth / 2;
        BlackKeyHeight = WhiteKeyHeight * 5 / 9;


        int width = margin * 2 + BlackBorder * 2 + totalWhiteKeyWith;
        int height = margin * 2 + BlackBorder * 3 + WhiteKeyHeight;
        setMeasuredDimension(width, height);
    }


    @Override
    protected void onSizeChanged(int newwidth, int newheight, int oldwidth, int oldheight) {
        super.onSizeChanged(newwidth, newheight, oldwidth, oldheight);
        blackKeyOffsets = new int[]{
                WhiteKeyWidth - BlackKeyWidth / 2,
                WhiteKeyWidth + BlackKeyWidth / 2,
                2 * WhiteKeyWidth - BlackKeyWidth / 2,
                2 * WhiteKeyWidth + BlackKeyWidth / 2,
                4 * WhiteKeyWidth - BlackKeyWidth / 2,
                4 * WhiteKeyWidth + BlackKeyWidth / 2,
                5 * WhiteKeyWidth - BlackKeyWidth / 2,
                5 * WhiteKeyWidth + BlackKeyWidth / 2,
                6 * WhiteKeyWidth - BlackKeyWidth / 2,
                6 * WhiteKeyWidth + BlackKeyWidth / 2
        };
        bufferBitmap = Bitmap.createBitmap(newwidth, newheight, Bitmap.Config.ARGB_8888);
        bufferCanvas = new Canvas(bufferBitmap);
    }

    /**
     * Set the MidiFile to use.
     * Save the list of midi notes. Each midi note includes the note Number
     * and StartTime (in pulses), so we know which notes to shade given the
     * current pulse time.
     */
    public void setMidiFile(MidiFile midifile, MidiOptions options, MidiPlayer player) {
        if (midifile == null) {
            notes = null;
            useTwoColors = false;
            return;
        }
        this.player = player;
        player.addMidiPlayerCallback(this);
        ArrayList<MidiTrack> tracks = midifile.ChangeMidiNotes(options);
        MidiTrack track = MidiFile.CombineToSingleTrack(tracks);
        notes = track.getNotes();

        for (MidiNote midiNote : notes) {
            Log.i("Piano--midiNote", midiNote.toString());
        }
        maxShadeDuration = midifile.getTime().getQuarter() * 2;

        /* We want to know which track the note came from.
         * Use the 'channel' field to store the track.
         */
        for (int tracknum = 0; tracknum < tracks.size(); tracknum++) {
            for (MidiNote note : tracks.get(tracknum).getNotes()) {
                note.setChannel(tracknum);
            }
        }

        /* When we have exactly two tracks, we assume this is a piano song,
         * and we use different colors for highlighting the left hand and
         * right hand notes.
         */
        useTwoColors = false;
        if (tracks.size() == 2) {
            useTwoColors = true;
        }
        showNoteLetters = options.showNoteLetters;
    }

    /**
     * Set the colors to use for shading
     */
    public void setShadeColors(int c1, int c2) {
        colorLeftHandShade = c1;
        colorRightHandShade = c2;
    }

    /**
     * Draw the outline of a 12-note (7 white note) piano octave
     */
    private void DrawOctaveOutline(Canvas canvas) {
        int right = WhiteKeyWidth * KeysPerOctave;

        // Draw the bounding rectangle, from C to B
        paint.setColor(colorDarkGray);
        canvas.drawLine(0, 0, 0, WhiteKeyHeight, paint);
        canvas.drawLine(right, 0, right, WhiteKeyHeight, paint);
        // canvas.drawLine(0, 0, right, 0, paint);
        canvas.drawLine(0, WhiteKeyHeight, right, WhiteKeyHeight, paint);
        paint.setColor(colorLightGray);
        canvas.drawLine(right - 1, 0, right - 1, WhiteKeyHeight, paint);
        canvas.drawLine(1, 0, 1, WhiteKeyHeight, paint);

        // Draw the line between E and F
        paint.setColor(colorDarkGray);
        canvas.drawLine(3 * WhiteKeyWidth, 0, 3 * WhiteKeyWidth, WhiteKeyHeight, paint);
        paint.setColor(colorLightGray);
        canvas.drawLine(3 * WhiteKeyWidth - 1, 0, 3 * WhiteKeyWidth - 1, WhiteKeyHeight, paint);
        canvas.drawLine(3 * WhiteKeyWidth + 1, 0, 3 * WhiteKeyWidth + 1, WhiteKeyHeight, paint);

        // Draw the sides/bottom of the black keys
        for (int i = 0; i < 10; i += 2) {
            int x1 = blackKeyOffsets[i];
            int x2 = blackKeyOffsets[i + 1];

            paint.setColor(colorDarkGray);
            canvas.drawLine(x1, 0, x1, BlackKeyHeight, paint);
            canvas.drawLine(x2, 0, x2, BlackKeyHeight, paint);
            canvas.drawLine(x1, BlackKeyHeight, x2, BlackKeyHeight, paint);
            paint.setColor(colorGray);
            canvas.drawLine(x1 - 1, 0, x1 - 1, BlackKeyHeight + 1, paint);
            canvas.drawLine(x2 + 1, 0, x2 + 1, BlackKeyHeight + 1, paint);
            canvas.drawLine(x1 - 1, BlackKeyHeight + 1, x2 + 1, BlackKeyHeight + 1, paint);
            paint.setColor(colorLightGray);
            canvas.drawLine(x1 - 2, 0, x1 - 2, BlackKeyHeight + 2, paint);
            canvas.drawLine(x2 + 2, 0, x2 + 2, BlackKeyHeight + 2, paint);
            canvas.drawLine(x1 - 2, BlackKeyHeight + 2, x2 + 2, BlackKeyHeight + 2, paint);
        }

        // Draw the bottom-half of the white keys
        for (int i = 1; i < KeysPerOctave; i++) {
            if (i == 3) {
                continue;  // we draw the line between E and F above
            }
            paint.setColor(colorDarkGray);
            canvas.drawLine(i * WhiteKeyWidth, BlackKeyHeight, i * WhiteKeyWidth,
                    WhiteKeyHeight, paint);
            paint.setColor(colorGray);
            canvas.drawLine(i * WhiteKeyWidth - 1, BlackKeyHeight + 1, i * WhiteKeyWidth - 1,
                    WhiteKeyHeight, paint);
            paint.setColor(colorLightGray);
            canvas.drawLine(i * WhiteKeyWidth + 1, BlackKeyHeight + 1, i * WhiteKeyWidth + 1,
                    WhiteKeyHeight, paint);
        }

    }

    /**
     * Draw an outline of the piano for 6 octaves
     */
    private void DrawOutline(Canvas canvas) {
        for (int octave = 0; octave < MaxOctave; octave++) {
            canvas.translate(octave * WhiteKeyWidth * KeysPerOctave, 0);
            DrawOctaveOutline(canvas);
            canvas.translate(-(octave * WhiteKeyWidth * KeysPerOctave), 0);
        }
    }

    /* Draw the Black keys */
    private void DrawBlackKeys(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        for (int octave = 0; octave < MaxOctave; octave++) {
            canvas.translate(octave * WhiteKeyWidth * KeysPerOctave, 0);
            for (int i = 0; i < 10; i += 2) {
                int x1 = blackKeyOffsets[i];
                int x2 = blackKeyOffsets[i + 1];
                paint.setColor(colorDarkGray);
                canvas.drawRect(x1, 0, x1 + BlackKeyWidth, BlackKeyHeight, paint);
                paint.setColor(colorGray);
                canvas.drawRect(x1 + 1, BlackKeyHeight - BlackKeyHeight / 8,
                        x1 + 1 + BlackKeyWidth - 2,
                        BlackKeyHeight - BlackKeyHeight / 8 + BlackKeyHeight / 8,
                        paint);
            }
            canvas.translate(-(octave * WhiteKeyWidth * KeysPerOctave), 0);
        }
        paint.setStyle(Paint.Style.STROKE);
    }

    /* Draw the black border area surrounding the piano keys.
     * Also, draw gray outlines at the bottom of the white keys.
     */
    private void DrawBlackBorder(Canvas canvas) {
        int PianoWidth = WhiteKeyWidth * KeysPerOctave * MaxOctave;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(colorDarkGray);
        canvas.drawRect(margin, margin, margin + PianoWidth + BlackBorder * 2,
                margin + BlackBorder - 2, paint);
        canvas.drawRect(margin, margin, margin + BlackBorder,
                margin + WhiteKeyHeight + BlackBorder * 3, paint);
        canvas.drawRect(margin, margin + BlackBorder + WhiteKeyHeight,
                margin + BlackBorder * 2 + PianoWidth,
                margin + BlackBorder + WhiteKeyHeight + BlackBorder * 2, paint);
        canvas.drawRect(margin + BlackBorder + PianoWidth, margin,
                margin + BlackBorder + PianoWidth + BlackBorder,
                margin + WhiteKeyHeight + BlackBorder * 3, paint);

        paint.setColor(colorGray);
        canvas.drawLine(margin + BlackBorder, margin + BlackBorder - 1,
                margin + BlackBorder + PianoWidth,
                margin + BlackBorder - 1, paint);

        canvas.translate(margin + BlackBorder, margin + BlackBorder);

        // Draw the gray bottoms of the white keys  
        for (int i = 0; i < KeysPerOctave * MaxOctave; i++) {
            canvas.drawRect(i * WhiteKeyWidth + 1, WhiteKeyHeight + 2,
                    i * WhiteKeyWidth + 1 + WhiteKeyWidth - 2,
                    WhiteKeyHeight + 2 + BlackBorder / 2, paint);
        }
        canvas.translate(-(margin + BlackBorder), -(margin + BlackBorder));
    }

    /**
     * Draw the note letters (A, A#, Bb, etc) underneath each white note
     * 画音符字母，在每个白键下边
     */
    private void DrawNoteLetters(Canvas canvas) {
        String[] letters = {"C", "D", "E", "F", "G", "A", "B"};
        String[] numbers = {"1", "3", "5", "6", "8", "10", "12"};
        String[] names;
        if (showNoteLetters == MidiOptions.NoteNameLetter) {
            names = letters;
        } else if (showNoteLetters == MidiOptions.NoteNameFixedNumber) {
            names = numbers;
        } else {
            return;
        }
        canvas.translate(margin + BlackBorder, margin + BlackBorder);
        paint.setColor(Color.WHITE);
        for (int octave = 0; octave < MaxOctave; octave++) {
            for (int i = 0; i < KeysPerOctave; i++) {
                canvas.drawText(names[i],
                        (octave * KeysPerOctave + i) * WhiteKeyWidth + WhiteKeyWidth / 3,
                        WhiteKeyHeight + BlackBorder + 4, paint);
            }
        }
        canvas.translate(-(margin + BlackBorder), -(margin + BlackBorder));
        paint.setColor(Color.BLACK);
    }


    /**
     * Obtain the drawing canvas and call onDraw()
     * 获取画板并调用onDraw方法
     */
    private void draw() {
        if (!surfaceReady) {
            return;
        }
        SurfaceHolder holder = getHolder();
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            return;
        }

        if (!surfaceReady || bufferBitmap == null) {
            return;
        }
        if (WhiteKeyWidth == 0) {
            return;
        }
        paint.setAntiAlias(false);
        bufferCanvas.translate(margin + BlackBorder, margin + BlackBorder);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        bufferCanvas.drawRect(0, 0, 0 + WhiteKeyWidth * KeysPerOctave * MaxOctave,
                WhiteKeyHeight, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(colorDarkGray);
        DrawBlackKeys(bufferCanvas);
        DrawOutline(bufferCanvas);
        bufferCanvas.translate(-(margin + BlackBorder), -(margin + BlackBorder));
        DrawBlackBorder(bufferCanvas);
        canvas.drawBitmap(bufferBitmap, 0, 0, paint);
        if (showNoteLetters != MidiOptions.NoteNameNone) { // 如果需要画音符字母
            DrawNoteLetters(canvas);
        }

        holder.unlockCanvasAndPost(canvas);
    }

    /* Shade the given note with the given brush.
     * We only draw notes from notenumber 24 to 96.
     * (Middle-C is 60).
     */
    private void ShadeOneNote(Canvas canvas, int notenumber, int color) {
        int octave = notenumber / 12;
        int notescale = notenumber % 12;

        octave -= 2;
        if (octave < 0 || octave >= MaxOctave)
            return;

        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.translate(octave * WhiteKeyWidth * KeysPerOctave, 0);
        int x1, x2, x3;

        int bottomHalfHeight = WhiteKeyHeight - (BlackKeyHeight + 3);

        /* notescale goes from 0 to 11, from C to B. */
        switch (notescale) {
            case 0: /* C */
                x1 = 2;
                x2 = blackKeyOffsets[0] - 2;
                canvas.drawRect(x1, 0, x1 + x2 - x1, 0 + BlackKeyHeight + 3, paint);
                canvas.drawRect(x1, BlackKeyHeight + 3, x1 + WhiteKeyWidth - 3,
                        BlackKeyHeight + 3 + bottomHalfHeight, paint);
                break;
            case 1: /* C# */
                x1 = blackKeyOffsets[0];
                x2 = blackKeyOffsets[1];
                canvas.drawRect(x1, 0, x1 + x2 - x1, 0 + BlackKeyHeight, paint);
                if (color == colorDarkGray) {
                    paint.setColor(colorGray);
                    canvas.drawRect(x1 + 1, BlackKeyHeight - BlackKeyHeight / 8,
                            x1 + 1 + BlackKeyWidth - 2,
                            BlackKeyHeight - BlackKeyHeight / 8 + BlackKeyHeight / 8,
                            paint);
                }
                break;
            case 2: /* D */
                x1 = WhiteKeyWidth + 2;
                x2 = blackKeyOffsets[1] + 3;
                x3 = blackKeyOffsets[2] - 2;
                canvas.drawRect(x2, 0, x2 + x3 - x2, 0 + BlackKeyHeight + 3, paint);
                canvas.drawRect(x1, BlackKeyHeight + 3, x1 + WhiteKeyWidth - 3,
                        BlackKeyHeight + 3 + bottomHalfHeight, paint);
                break;
            case 3: /* D# */
                x1 = blackKeyOffsets[2];
                x2 = blackKeyOffsets[3];
                canvas.drawRect(x1, 0, x1 + BlackKeyWidth, 0 + BlackKeyHeight, paint);
                if (color == colorDarkGray) {
                    paint.setColor(colorGray);
                    canvas.drawRect(x1 + 1, BlackKeyHeight - BlackKeyHeight / 8,
                            x1 + 1 + BlackKeyWidth - 2,
                            BlackKeyHeight - BlackKeyHeight / 8 + BlackKeyHeight / 8,
                            paint);
                }
                break;
            case 4: /* E */
                x1 = WhiteKeyWidth * 2 + 2;
                x2 = blackKeyOffsets[3] + 3;
                x3 = WhiteKeyWidth * 3 - 1;
                canvas.drawRect(x2, 0, x2 + x3 - x2, 0 + BlackKeyHeight + 3, paint);
                canvas.drawRect(x1, BlackKeyHeight + 3, x1 + WhiteKeyWidth - 3,
                        BlackKeyHeight + 3 + bottomHalfHeight, paint);
                break;
            case 5: /* F */
                x1 = WhiteKeyWidth * 3 + 2;
                x2 = blackKeyOffsets[4] - 2;
                x3 = WhiteKeyWidth * 4 - 2;
                canvas.drawRect(x1, 0, x1 + x2 - x1, 0 + BlackKeyHeight + 3, paint);
                canvas.drawRect(x1, BlackKeyHeight + 3, x1 + WhiteKeyWidth - 3,
                        BlackKeyHeight + 3 + bottomHalfHeight, paint);
                break;
            case 6: /* F# */
                x1 = blackKeyOffsets[4];
                x2 = blackKeyOffsets[5];
                canvas.drawRect(x1, 0, x1 + BlackKeyWidth, 0 + BlackKeyHeight, paint);
                if (color == colorDarkGray) {
                    paint.setColor(colorGray);
                    canvas.drawRect(x1 + 1, BlackKeyHeight - BlackKeyHeight / 8,
                            x1 + 1 + BlackKeyWidth - 2,
                            BlackKeyHeight - BlackKeyHeight / 8 + BlackKeyHeight / 8,
                            paint);
                }
                break;
            case 7: /* G */
                x1 = WhiteKeyWidth * 4 + 2;
                x2 = blackKeyOffsets[5] + 3;
                x3 = blackKeyOffsets[6] - 2;
                canvas.drawRect(x2, 0, x2 + x3 - x2, 0 + BlackKeyHeight + 3, paint);
                canvas.drawRect(x1, BlackKeyHeight + 3, x1 + WhiteKeyWidth - 3,
                        BlackKeyHeight + 3 + bottomHalfHeight, paint);
                break;
            case 8: /* G# */
                x1 = blackKeyOffsets[6];
                x2 = blackKeyOffsets[7];
                canvas.drawRect(x1, 0, x1 + BlackKeyWidth, 0 + BlackKeyHeight, paint);
                if (color == colorDarkGray) {
                    paint.setColor(colorGray);
                    canvas.drawRect(x1 + 1, BlackKeyHeight - BlackKeyHeight / 8,
                            x1 + 1 + BlackKeyWidth - 2,
                            BlackKeyHeight - BlackKeyHeight / 8 + BlackKeyHeight / 8,
                            paint);
                }
                break;
            case 9: /* A */
                x1 = WhiteKeyWidth * 5 + 2;
                x2 = blackKeyOffsets[7] + 3;
                x3 = blackKeyOffsets[8] - 2;
                canvas.drawRect(x2, 0, x2 + x3 - x2, 0 + BlackKeyHeight + 3, paint);
                canvas.drawRect(x1, BlackKeyHeight + 3, x1 + WhiteKeyWidth - 3,
                        BlackKeyHeight + 3 + bottomHalfHeight, paint);
                break;
            case 10: /* A# */
                x1 = blackKeyOffsets[8];
                x2 = blackKeyOffsets[9];
                canvas.drawRect(x1, 0, x1 + BlackKeyWidth, 0 + BlackKeyHeight, paint);
                if (color == colorDarkGray) {
                    paint.setColor(colorGray);
                    canvas.drawRect(x1 + 1, BlackKeyHeight - BlackKeyHeight / 8,
                            x1 + 1 + BlackKeyWidth - 2,
                            BlackKeyHeight - BlackKeyHeight / 8 + BlackKeyHeight / 8,
                            paint);
                }
                break;
            case 11: /* B */
                x1 = WhiteKeyWidth * 6 + 2;
                x2 = blackKeyOffsets[9] + 3;
                x3 = WhiteKeyWidth * KeysPerOctave - 1;
                canvas.drawRect(x2, 0, x2 + x3 - x2, 0 + BlackKeyHeight + 3, paint);
                canvas.drawRect(x1, BlackKeyHeight + 3, x1 + WhiteKeyWidth - 3,
                        BlackKeyHeight + 3 + bottomHalfHeight, paint);
                break;
            default:
                break;
        }
        canvas.translate(-(octave * WhiteKeyWidth * KeysPerOctave), 0);
    }

    /**
     * Find the MidiNote with the startTime closest to the given time.
     * Return the index of the note.  Use a binary search method.
     * 找到开始节拍与所给节拍最近的音符
     * 返回这个音符的下表.使用二分查找方法
     */
    private int FindClosestStartTime(int pulseTime) {
        int left = 0;
        int right = notes.size() - 1;
        while (right - left > 1) {
            int i = (right + left) / 2;
            if (notes.get(left).getPulsesOfStartTime() == pulseTime)
                break;
            else if (notes.get(i).getPulsesOfStartTime() <= pulseTime)
                left = i;
            else
                right = i;
        }
        while (left >= 1 && (notes.get(left - 1).getPulsesOfStartTime() == notes.get(left).getPulsesOfStartTime())) {
            left--;
        }
        return left;
    }

    /**
     * Return the next StartTime that occurs after the MidiNote
     * at offset i, that is also in the same track/channel.
     */
    private int NextStartTimeSameTrack(int i) {
        int start = notes.get(i).getPulsesOfStartTime();
        int end = notes.get(i).getEndTime();
        int track = notes.get(i).getChannel();

        while (i < notes.size()) {
            if (notes.get(i).getChannel() != track) {
                i++;
                continue;
            }
            if (notes.get(i).getPulsesOfStartTime() > start) {
                return notes.get(i).getPulsesOfStartTime();
            }
            end = Math.max(end, notes.get(i).getEndTime());
            i++;
        }
        return end;
    }


    /**
     * Return the next StartTime that occurs after the MidiNote
     * at offset i.  If all the subsequent notes have the same
     * StartTime, then return the largest EndTime.
     */
    private int NextStartTime(int i) {
        int start = notes.get(i).getPulsesOfStartTime();
        int end = notes.get(i).getEndTime();

        while (i < notes.size()) {
            if (notes.get(i).getPulsesOfStartTime() > start) {
                return notes.get(i).getPulsesOfStartTime();
            }
            end = Math.max(end, notes.get(i).getEndTime());
            i++;
        }
        return end;
    }


    /**
     * Find the Midi notes that occur in the current time.
     * Shade those notes on the piano displayed.
     * Un-shade the those notes played in the previous time.
     * 找到当前节拍中的音符。
     * 将那些音符投影在钢琴上
     * 取消那些在之前节拍的音符
     */
    public void ShadeNotes(int currentPulseTime, int prevPulseTime) {
        if (notes == null || notes.size() == 0 || !surfaceReady || bufferBitmap == null) {
            return;
        }
        SurfaceHolder holder = getHolder();
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            return;
        }

        bufferCanvas.translate(margin + BlackBorder, margin + BlackBorder);
        /* Loop through the Midi notes.
         * Unshade notes where StartTime <= prevPulseTime < next StartTime
         * Shade notes where StartTime <= currentPulseTime < next StartTime
         */
        int lastShadedIndex = FindClosestStartTime(prevPulseTime - maxShadeDuration * 2);
        for (int i = lastShadedIndex; i < notes.size(); i++) {
            // Log.i("Piano", notes.get(i).getNumber() + ":" + i);
            int start = notes.get(i).getPulsesOfStartTime();
            int end = notes.get(i).getEndTime();
            int notenumber = notes.get(i).getNoteNumber();
            int nextStart = NextStartTime(i);
            int nextStartTrack = NextStartTimeSameTrack(i);
            end = Math.max(end, nextStartTrack);
            end = Math.min(end, start + maxShadeDuration - 1);

            /* If we've past the previous and current times, we're done. */
            if ((start > prevPulseTime) && (start > currentPulseTime)) {
                break;
            }

            /* If shaded notes are the same, we're done */
            if ((start <= currentPulseTime) && (currentPulseTime < nextStart) &&
                    (currentPulseTime < end) &&
                    (start <= prevPulseTime) && (prevPulseTime < nextStart) &&
                    (prevPulseTime < end)) {
                break;
            }

            /* If the note is in the current time, shade it */
            if ((start <= currentPulseTime) && (currentPulseTime < end)) {
                if (useTwoColors) {
                    if (notes.get(i).getChannel() == 1) {
                        ShadeOneNote(bufferCanvas, notenumber, colorRightHandShade);
                        if (mPianoListener != null)
                            mPianoListener.pianoKey(notenumber, 1);
                    } else {
                        ShadeOneNote(bufferCanvas, notenumber, colorLeftHandShade);
                        if (mPianoListener != null)
                            mPianoListener.pianoKey(notenumber, 2);
                    }
                } else {
                    ShadeOneNote(bufferCanvas, notenumber, colorLeftHandShade);
                }
            }

            /* If the note is in the previous time, un-shade it, draw it white. */
            else if ((start <= prevPulseTime) && (prevPulseTime < end)) {
                int num = notenumber % 12;
                if (num == 1 || num == 3 || num == 6 || num == 8 || num == 10) {
                    ShadeOneNote(bufferCanvas, notenumber, colorDarkGray);
                } else {
                    ShadeOneNote(bufferCanvas, notenumber, Color.WHITE);
                }
            }
        }
        bufferCanvas.translate(-(margin + BlackBorder), -(margin + BlackBorder));
        canvas.drawBitmap(bufferBitmap, 0, 0, paint);
        holder.unlockCanvasAndPost(canvas);
    }


    /**
     * TODO ??
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        draw();
    }

    /**
     * Surface is ready for shading the notes
     */
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceReady = true;
        // setWillNotDraw(false);
        draw();
    }

    /**
     * Surface has been destroyed
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceReady = false;
    }

    /**
     * When the Piano is touched, pause the midi player
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (player != null) {
                player.pause();
            }
        }
        return super.onTouchEvent(event);
    }

    public void setPianoListener(PianoListener pianoListener) {
        mPianoListener = pianoListener;
    }

    @Override
    public void onSheetNeedShadeNotes(int currentPulseTime, int prevPulseTime, int gradualScroll) {

    }

    @Override
    public void onPianoNeedShadeNotes(int currentPulseTime, int prevPulseTime) {
        ShadeNotes(currentPulseTime, prevPulseTime);
    }

    public interface PianoListener {
        void pianoKey(int position, int channel);
    }
}

