package yin.source.com.midimusicbook.midi.musicBook;

import yin.source.com.midimusicbook.midi.baseBean.MidiFile;
import yin.source.com.midimusicbook.midi.baseBean.MidiOptions;

/**
 * Created by Yin on 2018/5/8.
 */
public interface MidiPlayController {

    void Rewind();

    void Stop();

    void Play();

    void FastForward();

    void Pause();

    void SetMidiFile(MidiFile file, MidiOptions opt);

}
