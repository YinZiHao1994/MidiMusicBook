package yin.source.com.midimusicbook.exception;

/**
 * @class MidiFileException
 * A MidiFileException is thrown when an error_avator occurs
 * while parsing the Midi File.  The constructore takes
 * the file offset (in bytes) where the error_avator occurred,
 * and a string describing the error_avator.
 */
public class MidiFileException extends RuntimeException {
    public MidiFileException(String s) {
        super(s);
    }

    public MidiFileException(String s, int offset) {
        super(s + " at offset " + offset);
    }
}
