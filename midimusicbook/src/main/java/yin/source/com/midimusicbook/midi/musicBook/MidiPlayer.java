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
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import yin.source.com.midimusicbook.exception.MidiFileException;
import yin.source.com.midimusicbook.midi.baseBean.MidiFile;
import yin.source.com.midimusicbook.midi.baseBean.MidiOptions;

/**
 * @class MidiPlayer
 * <p/>
 * The MidiPlayer is the panel at the top used to play the sound of the
 * midi file. It consists of:
 * <p/>
 * - The Rewind button - The play/pause button - The stop button - The
 * Fast Forward button - The Playback speed bar
 * <p/>
 * The sound of the midi file depends on - The MidiOptions (taken from
 * the menus) Which tracks are selected How much to transpose the keys by
 * What instruments to use per track - The tempo (from the Speed bar) -
 * The volume
 * <p/>
 * The MidiFile.ChangeSound() method is used to create a new midi file
 * with these options. The mciSendString() function is used for playing,
 * pausing, and stopping the sound.
 * <p/>
 * For shading the notes during playback, the method
 * SheetMusic.ShadeNotes() is used. It takes the current 'pulse time',
 * and determines which notes to shade.
 */
public class MidiPlayer implements MidiPlayController {

    final String tempSoundFile = "playing.mid";
    /**
     * The seekbar for controlling the playback speed
     */

    private PlayState playState;
    /**
     * The filename to play sound from // 临时音乐文件
     */

    private MediaPlayer player;
    /**
     * For playing the audio // 播放器
     */
    private MidiFile midifile;
    /**
     * The midi file to play // midi文件
     */
    private MidiOptions options;
    /**
     * The sound options for playing the midi file // 播放设置选项
     */
    private double pulsesPerMsec;
    /**
     * The number of pulses per millisec // 每毫秒的节拍数
     */
    private Handler timer;
    /**
     * Timer used to update the sheet music while playing // 正在播放时用来更新乐谱的计时器
     */
    private long startTime;
    /**
     * Absolute time when music started playing (msec) // 当音乐开始播放时的绝对时间（单位微秒）
     */
    private double startPulseTime;
    /**
     * Time (in pulses) when music started playing // 音乐开始播放时的节拍
     */
    private double currentPulseTime;
    /**
     * Time (in pulses) music is currently at // 音乐现在的节拍
     */
    private double prevPulseTime;
    /**
     * Time (in pulses) music was last at // 当前音乐的前一个节拍
     */
    private Context context;
    private List<MidiPlayerCallback> midiPlayerCallbackList;
    /**
     * If we're paused, reshade the sheet music and piano.
     */
    Runnable ReShade = new Runnable() {
        public void run() {
            if (playState == PlayState.PAUSED || playState == PlayState.STOPPED) {

                if (midiPlayerCallbackList != null) {
                    for (MidiPlayerCallback midiPlayerCallback : midiPlayerCallbackList) {
                        midiPlayerCallback.onSheetNeedShadeNotes((int) currentPulseTime, -10, SheetMusic.ImmediateScroll);
                        midiPlayerCallback.onPianoNeedShadeNotes((int) currentPulseTime, (int) prevPulseTime);

                    }
                }
//                sheet.ShadeNotes((int) currentPulseTime, (int) -10, SheetMusic.ImmediateScroll);
//                piano.ShadeNotes((int) currentPulseTime, (int) prevPulseTime);
            }
        }
    };
    Runnable DoPlay = new Runnable() {
        public void run() {
//            context.getWindow().addFlags(
//                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            Log.i("MidiPlayer--", options.playMeasuresInLoop + "");
            /*
             * The startPulseTime is the pulse time of the midi file when we
             * first start playing the music. It's used during shading.
             */
            if (options.playMeasuresInLoop) {
                /*
                 * If we're playing measures in a loop, make sure the
                 * currentPulseTime is somewhere inside the loop measures.
                 */
                int measure = (int) (currentPulseTime / midifile.getTime()
                        .getMeasure());
                if ((measure < options.playMeasuresInLoopStart)
                        || (measure > options.playMeasuresInLoopEnd)) {
                    currentPulseTime = options.playMeasuresInLoopStart
                            * midifile.getTime().getMeasure();
                }
                startPulseTime = currentPulseTime;
                options.pauseTime = (int) (currentPulseTime - options.shifttime);
            } else if (playState == PlayState.PAUSED) {
                startPulseTime = currentPulseTime;
                options.pauseTime = (int) (currentPulseTime - options.shifttime);
            } else {
                options.pauseTime = 0;
                startPulseTime = options.shifttime;
                currentPulseTime = options.shifttime;
                prevPulseTime = options.shifttime
                        - midifile.getTime().getQuarter();
            }
            CreateMidiFile();
            playState = PlayState.PLAYING;
            PlaySound(tempSoundFile);
            startTime = SystemClock.uptimeMillis();

            timer.removeCallbacks(TimerCallback);
            timer.removeCallbacks(ReShade);
            timer.postDelayed(TimerCallback, 100);

            if (midiPlayerCallbackList != null) {
                for (MidiPlayerCallback midiPlayerCallback : midiPlayerCallbackList) {
//                    midiPlayerCallback.onPlayerPlay();
                    midiPlayerCallback.onSheetNeedShadeNotes((int) currentPulseTime, (int) prevPulseTime, SheetMusic.GradualScroll);
                    midiPlayerCallback.onPianoNeedShadeNotes((int) currentPulseTime, (int) prevPulseTime);
                }
            }

//            sheet.ShadeNotes((int) currentPulseTime, (int) prevPulseTime, SheetMusic.GradualScroll);
//            piano.ShadeNotes((int) currentPulseTime, (int) prevPulseTime);
            return;
        }
    };
    /**
     * The callback for the timer. If the midi is still playing, update the
     * currentPulseTime and shade the sheet music. If a stop or pause has been
     * initiated (by someone clicking the stop or pause button), then stop the
     * timer.
     */
    Runnable TimerCallback = new Runnable() {
        public void run() {
            if (midifile == null) {
                playState = PlayState.STOPPED;
                return;
            } else if (playState == PlayState.STOPPED || playState == PlayState.PAUSED) {
                /* This case should never happen */
                return;
            } else if (playState == PlayState.INIT_STOP) {
                return;
            } else if (playState == PlayState.PLAYING) {
                long msec = SystemClock.uptimeMillis() - startTime;
                Log.i("MidiPlayer--msec", msec + "");
                prevPulseTime = currentPulseTime;
                currentPulseTime = startPulseTime + msec * pulsesPerMsec;
                Log.i("MidiPlayer--current", currentPulseTime + "");
                /* If we're playing in a loop, stop and restart */
                if (options.playMeasuresInLoop) {
                    double nearEndTime = currentPulseTime + pulsesPerMsec * 10;
                    int measure = (int) (nearEndTime / midifile.getTime().getMeasure());
                    if (measure > options.playMeasuresInLoopEnd) {
                        RestartPlayMeasuresInLoop();
                        return;
                    }
                }

                /* stop if we've reached the end of the song */
                if (currentPulseTime > midifile.getTotalPulses()) {
                    DoStop();
                    return;
                }
                if (midiPlayerCallbackList != null) {
                    for (MidiPlayerCallback midiPlayerCallback : midiPlayerCallbackList) {
                        midiPlayerCallback.onSheetNeedShadeNotes((int) currentPulseTime, (int) prevPulseTime, SheetMusic.GradualScroll);
                        midiPlayerCallback.onPianoNeedShadeNotes((int) currentPulseTime, (int) prevPulseTime);
                    }
                }
//                sheet.ShadeNotes((int) currentPulseTime, (int) prevPulseTime, SheetMusic.GradualScroll);
//                piano.ShadeNotes((int) currentPulseTime, (int) prevPulseTime);
                timer.postDelayed(TimerCallback, 100);
                return;
            } else if (playState == PlayState.INIT_PAUSE) {
                long msec = SystemClock.uptimeMillis() - startTime;
                StopSound();

                prevPulseTime = currentPulseTime;
                currentPulseTime = startPulseTime + msec * pulsesPerMsec;

                if (midiPlayerCallbackList != null) {
                    for (MidiPlayerCallback midiPlayerCallback : midiPlayerCallbackList) {
                        midiPlayerCallback.onSheetNeedShadeNotes((int) currentPulseTime, (int) prevPulseTime, SheetMusic.ImmediateScroll);
                    }
                }
//                sheet.ShadeNotes((int) currentPulseTime, (int) prevPulseTime, SheetMusic.ImmediateScroll);
                playState = PlayState.PAUSED;
                timer.postDelayed(ReShade, 100);
                return;
            }
        }
    };

    public MidiPlayer(Context context) {
        this.context = context;
        playState = PlayState.STOPPED;
        startTime = SystemClock.uptimeMillis();
        startPulseTime = 0;
        currentPulseTime = 0;
        prevPulseTime = -10;

        timer = new Handler();

        player = new MediaPlayer();
    }

    /**
     * Get the preferred width/height given the screen width/height
     */
    public static Point getPreferredSize(int screenwidth, int screenheight) {
        int height = (int) (5.0 * screenwidth / (2 + Piano.KeysPerOctave
                * Piano.MaxOctave));
        height = height * 2 / 3;
        Point result = new Point(screenwidth, height);
        return result;
    }

    /**
     * The MidiFile and/or SheetMusic has changed. stop any playback sound, and
     * store the current midifile and sheet music.
     */
    public void setMidiFile(MidiFile file, MidiOptions opt) {

        /*
         * If we're paused, and using the same midi file, redraw the highlighted
         * notes.
         */
        if ((file == midifile && midifile != null && playState == PlayState.PAUSED)) {
            options = opt;


            if (midiPlayerCallbackList != null) {
                for (MidiPlayerCallback midiPlayerCallback : midiPlayerCallbackList) {
//                    midiPlayerCallback.onPlayerSetMidiFile();
                    midiPlayerCallback.onSheetNeedShadeNotes((int) currentPulseTime, (int) -1, SheetMusic.DontScroll);
                }
            }
//            sheet.ShadeNotes((int) currentPulseTime, (int) -1, SheetMusic.DontScroll);

            /*
             * We have to wait some time (200 msec) for the sheet music to
             * scroll and redraw, before we can re-shade.
             */
            timer.removeCallbacks(TimerCallback);
            timer.postDelayed(ReShade, 500);
        } else {
            stop();
            midifile = file;
            options = opt;
        }
    }

    /**
     * Return the number of tracks selected in the MidiOptions. If the number of
     * tracks is 0, there is no sound to play.
     */
    private int numberTracks() {
        int count = 0;
        for (int i = 0; i < options.tracks.length; i++) {
            if (options.tracks[i] && !options.mute[i]) {
                count += 1;
            }
        }
        return count;
    }

    /**
     * Create a new midi file with all the MidiOptions incorporated. Save the
     * new file to playing.mid, and store this temporary filename in
     * tempSoundFile. // 使用所有的设置创建一个新的midi文件,保存新的文件为play
     */
    private void CreateMidiFile() {
        double inverse_tempo = 1.0 / midifile.getTime().getTempo();
//        double inverse_tempo_scaled = inverse_tempo * speedBar.getProgress() / 100.0;
        double inverse_tempo_scaled = inverse_tempo;
        // double inverse_tempo_scaled = inverse_tempo * 100.0 / 100.0;
        options.tempo = (int) (1.0 / inverse_tempo_scaled);
        pulsesPerMsec = midifile.getTime().getQuarter() * (1000.0 / options.tempo);
        try {
            FileOutputStream dest = context.openFileOutput(tempSoundFile, Context.MODE_PRIVATE);
            midifile.ChangeSound(dest, options);
            dest.close();
            checkFile(tempSoundFile);
        } catch (IOException e) {
            Toast toast = Toast.makeText(context,
                    "Error: Unable to create MIDI file for playing.",
                    Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void checkFile(String name) {
        try {
            FileInputStream in = context.openFileInput(name);
            byte[] data = new byte[4096];
            int total = 0, len = 0;
            while (true) {
                len = in.read(data, 0, 4096);
                if (len > 0)
                    total += len;
                else
                    break;
            }
            in.close();
            data = new byte[total];
            in = context.openFileInput(name);
            int offset = 0;
            while (offset < total) {
                len = in.read(data, offset, total - offset);
                if (len > 0)
                    offset += len;
            }
            in.close();
            MidiFile testmidi = new MidiFile(data, name);
        } catch (IOException e) {
            Toast toast = Toast.makeText(context,
                    "CheckFile: " + e.toString(), Toast.LENGTH_LONG);
            toast.show();
        } catch (MidiFileException e) {
            Toast toast = Toast.makeText(context,
                    "CheckFile midi: " + e.toString(), Toast.LENGTH_LONG);
            toast.show();
        }
    }

    /**
     * play the sound for the given MIDI file
     */
    private void PlaySound(String filename) {
        if (player == null)
            return;
        try {
            FileInputStream input = context.openFileInput(filename);
            player.reset();
            player.setDataSource(input.getFD());
            input.close();
            player.prepare();
            player.start();
        } catch (IOException e) {
            Toast toast = Toast.makeText(context,
                    "Error: Unable to play MIDI sound", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    /**
     * stop playing the MIDI music
     */
    private void StopSound() {
        if (player == null)
            return;
        player.stop();
        player.reset();
    }

    /**
     * The callback for the play button. If we're stopped or pause, then play
     * the midi file.
     */
    public void play() {
        if (midifile == null || numberTracks() == 0) {
            return;
        } else if (playState == PlayState.INIT_STOP || playState == PlayState.INIT_PAUSE || playState == PlayState.PLAYING) {
            return;
        }
        // playState is stopped or paused

        // Hide the midi player, wait a little for the view
        // to refresh, and then start playing
//        this.setVisibility(View.GONE);
        timer.removeCallbacks(TimerCallback);
        timer.postDelayed(DoPlay, 1000);
    }

    /**
     * The callback for pausing playback. If we're currently playing, pause the
     * music. The actual pause is done when the timer is invoked.
     */
    public void pause() {

        if (midifile == null || numberTracks() == 0) {
            return;
        } else if (playState == PlayState.PLAYING) {
            playState = PlayState.INIT_PAUSE;
            return;
        }
    }

    /**
     * The callback for the stop button. If playing, initiate a stop and wait
     * for the timer to finish. Then do the actual stop.
     */
    public void stop() {
        if (midifile == null || playState == PlayState.STOPPED) {
            return;
        }

        if (playState == PlayState.INIT_PAUSE || playState == PlayState.INIT_STOP || playState == PlayState.PLAYING) {
            /* Wait for timer to finish */
            playState = PlayState.INIT_STOP;
            DoStop();
        } else if (playState == PlayState.PAUSED) {
            DoStop();
        }
    }

    /**
     * Perform the actual stop, by stopping the sound, removing any shading, and
     * clearing the state.
     */
    void DoStop() {
        playState = PlayState.STOPPED;
        timer.removeCallbacks(TimerCallback);

        if (midiPlayerCallbackList != null) {
            for (MidiPlayerCallback midiPlayerCallback : midiPlayerCallbackList) {
//                midiPlayerCallback.onPlayerStop();
                midiPlayerCallback.onSheetNeedShadeNotes(-10, (int) prevPulseTime, SheetMusic.DontScroll);
                midiPlayerCallback.onSheetNeedShadeNotes(-10, (int) currentPulseTime, SheetMusic.DontScroll);
                midiPlayerCallback.onPianoNeedShadeNotes(-10, (int) prevPulseTime);
                midiPlayerCallback.onPianoNeedShadeNotes(-10, (int) currentPulseTime);

            }
        }

//        sheet.ShadeNotes(-10, (int) prevPulseTime, SheetMusic.DontScroll);
//        sheet.ShadeNotes(-10, (int) currentPulseTime, SheetMusic.DontScroll);
//        piano.ShadeNotes(-10, (int) prevPulseTime);
//        piano.ShadeNotes(-10, (int) currentPulseTime);
        startPulseTime = 0;
        currentPulseTime = 0;
        prevPulseTime = 0;
//        setVisibility(View.VISIBLE);
        StopSound();
    }

    /**
     * 倒带
     * <p>
     * Rewind the midi music back one measure. The music must be in the paused
     * state. When we resume in playPause, we start at the currentPulseTime. So
     * to rewind, just decrease the currentPulseTime, and re-shade the sheet
     * music.
     */
    public void Rewind() {
        if (midifile == null || playState != PlayState.PAUSED) {
            return;
        }

        if (midiPlayerCallbackList != null) {
            for (MidiPlayerCallback midiPlayerCallback : midiPlayerCallbackList) {
                midiPlayerCallback.onSheetNeedShadeNotes(-10, (int) currentPulseTime, SheetMusic.DontScroll);
                midiPlayerCallback.onPianoNeedShadeNotes(-10, (int) currentPulseTime);
            }
        }
//        /* Remove any highlighted notes */
//        sheet.ShadeNotes(-10, (int) currentPulseTime, SheetMusic.DontScroll);
//        piano.ShadeNotes(-10, (int) currentPulseTime);

        prevPulseTime = currentPulseTime;
        currentPulseTime -= midifile.getTime().getMeasure();
        if (currentPulseTime < options.shifttime) {
            currentPulseTime = options.shifttime;
        }

        if (midiPlayerCallbackList != null) {
            for (MidiPlayerCallback midiPlayerCallback : midiPlayerCallbackList) {
                midiPlayerCallback.onSheetNeedShadeNotes((int) currentPulseTime, (int) prevPulseTime, SheetMusic.ImmediateScroll);
                midiPlayerCallback.onPianoNeedShadeNotes((int) currentPulseTime, (int) prevPulseTime);
            }
        }
//        sheet.ShadeNotes((int) currentPulseTime, (int) prevPulseTime, SheetMusic.ImmediateScroll);
//        piano.ShadeNotes((int) currentPulseTime, (int) prevPulseTime);
    }

    /**
     * 快进
     * <p>
     * Fast forward the midi music by one measure. The music must be in the
     * paused/stopped state. When we resume in playPause, we start at the
     * currentPulseTime. So to fast forward, just increase the currentPulseTime,
     * and re-shade the sheet music.
     */
    public void FastForward() {
        if (midifile == null) {
            return;
        }
        if (playState != PlayState.PAUSED && playState != PlayState.STOPPED) {
            return;
        }
        playState = PlayState.PAUSED;
        if (midiPlayerCallbackList != null) {
            for (MidiPlayerCallback midiPlayerCallback : midiPlayerCallbackList) {
                midiPlayerCallback.onSheetNeedShadeNotes(-10, (int) currentPulseTime, SheetMusic.DontScroll);
                midiPlayerCallback.onPianoNeedShadeNotes(-10, (int) currentPulseTime);
            }
        }
//        /* Remove any highlighted notes */
//        sheet.ShadeNotes(-10, (int) currentPulseTime, SheetMusic.DontScroll);
//        piano.ShadeNotes(-10, (int) currentPulseTime);

        prevPulseTime = currentPulseTime;
        currentPulseTime += midifile.getTime().getMeasure();
        if (currentPulseTime > midifile.getTotalPulses()) {
            currentPulseTime -= midifile.getTime().getMeasure();
        }
        if (midiPlayerCallbackList != null) {
            for (MidiPlayerCallback midiPlayerCallback : midiPlayerCallbackList) {
                midiPlayerCallback.onSheetNeedShadeNotes((int) currentPulseTime, (int) prevPulseTime, SheetMusic.ImmediateScroll);
                midiPlayerCallback.onPianoNeedShadeNotes((int) currentPulseTime, (int) prevPulseTime);
            }
        }
//        sheet.ShadeNotes((int) currentPulseTime, (int) prevPulseTime, SheetMusic.ImmediateScroll);
//        piano.ShadeNotes((int) currentPulseTime, (int) prevPulseTime);
    }

    /**
     * Move the current position to the location clicked. The music must be in
     * the paused/stopped state. When we resume in playPause, we start at the
     * currentPulseTime. So, set the currentPulseTime to the position clicked.
     */
    public void MoveToClicked(int x, int y) {
        if (midifile == null) {
            return;
        }
        if (playState != PlayState.PAUSED && playState != PlayState.STOPPED) {
            return;
        }
        playState = PlayState.PAUSED;
        if (midiPlayerCallbackList != null) {
            for (MidiPlayerCallback midiPlayerCallback : midiPlayerCallbackList) {
                midiPlayerCallback.onSheetNeedShadeNotes(-10, (int) currentPulseTime, SheetMusic.DontScroll);
                midiPlayerCallback.onPianoNeedShadeNotes(-10, (int) currentPulseTime);
            }
        }
//        /* Remove any highlighted notes */
//        sheet.ShadeNotes(-10, (int) currentPulseTime, SheetMusic.DontScroll);
//        piano.ShadeNotes(-10, (int) currentPulseTime);

//        currentPulseTime = sheet.PulseTimeForPoint(new Point(x, y));
        prevPulseTime = currentPulseTime - midifile.getTime().getMeasure();
        if (currentPulseTime > midifile.getTotalPulses()) {
            currentPulseTime -= midifile.getTime().getMeasure();
        }
        if (midiPlayerCallbackList != null) {
            for (MidiPlayerCallback midiPlayerCallback : midiPlayerCallbackList) {
                midiPlayerCallback.onSheetNeedShadeNotes((int) currentPulseTime, (int) prevPulseTime, SheetMusic.DontScroll);
                midiPlayerCallback.onPianoNeedShadeNotes((int) currentPulseTime, (int) prevPulseTime);
            }
        }
//        sheet.ShadeNotes((int) currentPulseTime, (int) prevPulseTime, SheetMusic.DontScroll);
//        piano.ShadeNotes((int) currentPulseTime, (int) prevPulseTime);
    }

    /**
     * The "play Measures in a Loop" feature is enabled, and we've reached the
     * last measure. stop the sound, unshade the music, and then start playing
     * again.
     */
    private void RestartPlayMeasuresInLoop() {
        playState = PlayState.STOPPED;

        if (midiPlayerCallbackList != null) {
            for (MidiPlayerCallback midiPlayerCallback : midiPlayerCallbackList) {
//                midiPlayerCallback.onPlayerRestartPlayMeasuresInLoop();
                midiPlayerCallback.onSheetNeedShadeNotes(-10, (int) prevPulseTime, SheetMusic.DontScroll);
                midiPlayerCallback.onPianoNeedShadeNotes(-10, (int) prevPulseTime);
            }
        }

//        piano.ShadeNotes(-10, (int) prevPulseTime);
//        sheet.ShadeNotes(-10, (int) prevPulseTime, SheetMusic.DontScroll);
        currentPulseTime = 0;
        prevPulseTime = -1;
        StopSound();
        timer.postDelayed(DoPlay, 300);
    }

    public double getCurrentPulseTime() {
        return currentPulseTime;
    }

    public void setCurrentPulseTime(double currentPulseTime) {
        this.currentPulseTime = currentPulseTime;
    }

    public double getPrevPulseTime() {
        return prevPulseTime;
    }

    public void setPrevPulseTime(double prevPulseTime) {
        this.prevPulseTime = prevPulseTime;
    }

    public List<MidiPlayerCallback> getMidiPlayerCallbackList() {
        return midiPlayerCallbackList;
    }

    public void addMidiPlayerCallback(MidiPlayerCallback midiPlayerCallback) {
        if (midiPlayerCallbackList == null) {
            midiPlayerCallbackList = new ArrayList<>();
        }
        if (!midiPlayerCallbackList.contains(midiPlayerCallback)) {
            midiPlayerCallbackList.add(midiPlayerCallback);
        }
    }

    public PlayState getPlayState() {
        return playState;
    }

    public enum PlayState {
        STOPPED, PLAYING, PAUSED, INIT_STOP, INIT_PAUSE
    }

    public interface MidiPlayerCallback {

//        void onPlayerStop();

//        void onPlayerRewind();
//
//        void onPlayerFastForward();
//
//        void onPlayerMoveToClicked();
//
//        void onPlayerRestartPlayMeasuresInLoop();

//        void onPlayerSetMidiFile();

//        void onPlayerPlay();

        void onSheetNeedShadeNotes(int currentPulseTime, int prevPulseTime, int gradualScroll);

        void onPianoNeedShadeNotes(int currentPulseTime, int prevPulseTime);
    }

}
