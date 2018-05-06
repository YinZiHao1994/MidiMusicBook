package yin.source.com.midimusicbook.midi.baseBean;



import java.io.UnsupportedEncodingException;

import yin.source.com.midimusicbook.exception.MidiFileException;

/** @class MidiFileReader
 * The MidiFileReader is used to read low-level binary data from a file.
 * This class can do the following:
 *
 * - Peek at the next byte in the file.
 * - Read a byte
 * - Read a 16-bit big endian short
 * - Read a 32-bit big endian int
 * - Read a fixed length ascii string (not null terminated)
 * - Read a "variable length" integer.  The format of the variable length
 *   int is described at the top of this file.
 * - skip ahead a given number of bytes
 * - Return the current offset.
 */

public class MidiFileReader {
    private byte[] data;       /** The entire midi file data */
    private int parse_offset;  /** The current offset while parsing */

    /** Create a new MidiFileReader for the given filename */
    /** Not used
     public MidiFileReader(String filename) {
     try {
     File info = new File(filename);
     FileInputStream file = new FileInputStream(filename);
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

     parse_offset = 0;
     }
     catch (IOException e) {
     throw new MidiFileException("Cannot open file " + filename, 0);
     }
     }
     **/

    /** Create a new MidiFileReader from the given data */
    public MidiFileReader(byte[] bytes) {
        data = bytes;
        parse_offset = 0;
    }

    /** Check that the given number of bytes doesn't exceed the file size */
    private void checkRead(int amount) {
        if (parse_offset + amount > data.length) {
            throw new MidiFileException("File is truncated", parse_offset);
        }
    }

    /** Read the next byte in the file, but don't increment the parse offset */
    public byte Peek() {
        checkRead(1);
        return data[parse_offset];
    }

    /** Read a byte from the file */
    public byte readByte() {
        checkRead(1);
        byte x = data[parse_offset];
        parse_offset++;
        return x;
    }

    /** Read the given number of bytes from the file */
    public byte[] readBytes(int amount) {
        checkRead(amount);
        byte[] result = new byte[amount];
        for (int i = 0; i < amount; i++) {
            result[i] = (byte)(data[i + parse_offset]);
        }
        parse_offset += amount;
        return result;
    }

    /** Read a 16-bit short from the file */
    public int readShort() {
        checkRead(2);
        int x = ((data[parse_offset] & 0xFF) << 8) |
                (data[parse_offset+1] & 0xFF);
        parse_offset += 2;
        return x;
    }

    /** Read a 32-bit int from the file */
    public int readInt() {
        checkRead(4);
        int x =  ((data[parse_offset] & 0xFF) << 24) |
                ((data[parse_offset+1] & 0xFF) << 16) |
                ((data[parse_offset+2] & 0xFF) << 8) |
                (data[parse_offset+3] & 0xFF);
        parse_offset += 4;
        return x;
    }

    /** Read an ascii String with the given length */
    public String readAscii(int len) {
        checkRead(len);
        String s ;
        try {
            s = new String(data, parse_offset, len, "US-ASCII");
        }
        catch (UnsupportedEncodingException e) {
            s = new String(data, parse_offset, len);
        }
        parse_offset += len;
        return s;
    }

    /** Read a variable-length integer (1 to 4 bytes). The integer ends
     * when you encounter a byte that doesn't have the 8th bit set
     * (a byte less than 0x80).
     */
    public int readVariableLength() {
        int result = 0;
        byte b;

        b = readByte();
        result = (int)(b & 0x7f);

        for (int i = 0; i < 3; i++) {
            if ((b & 0x80) != 0) {
                b = readByte();
                result = (int)( (result << 7) + (b & 0x7f) );
            }
            else {
                break;
            }
        }
        return (int)result;
    }

    /** skip over the given number of bytes */
    public void skip(int amount) {
        checkRead(amount);
        parse_offset += amount;
    }

    /** Return the current parse offset */
    public int getOffset() {
        return parse_offset;
    }

    /** Return the raw midi file byte data */
    public byte[] getData() {
        return data;
    }
}
