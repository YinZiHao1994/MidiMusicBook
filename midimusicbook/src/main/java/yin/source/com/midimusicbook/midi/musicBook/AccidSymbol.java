/*
 * Copyright (c) 2007-2011 Madhav Vaidyanathan
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


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;


/**
 * @class AccidSymbol
 * An accidental (accid) symbol represents a sharp, flat, or natural
 * accidental that is displayed at a specific position (note and clef).音符和谱号
 */
public class AccidSymbol implements MusicSymbol {
    private Accid accid;
    /**
     * The accidental (sharp, flat, natural)
     */
    private WhiteNote whitenote;
    /**
     * 符号出现的地方(白键)
     * The white note where the symbol occurs
     */
    private Clef clef;
    /**
     * 这个符号所在的谱号
     * Which clef the symbols is in
     */
    private int width;
    /**
     * 符号的宽度
     * Width of symbol
     */

    /**
     * Create a new AccidSymbol with the given accidental, that is
     * displayed at the given note in the given clef.
     * 根据铺好中的音符中正在展示的accid创建一个新的AccidSymbol,
     */
    public AccidSymbol(Accid accid, WhiteNote note, Clef clef) {
        this.accid = accid;
        this.whitenote = note;
        this.clef = clef;
        width = getMinWidth();
    }

    /**
     * Return the white note this accidental is displayed at
     * 返回正在展示中的accidental的白键
     */
    public WhiteNote getNote() {
        return whitenote;
    }

    /**
     * Get the time (in pulses) this symbol occurs at.
     * Not used.  Instead, the StartTime of the ChordSymbol containing this
     * AccidSymbol is used.
     */
    public int getStartTime() {
        return -1;
    }

    /**
     * Get the minimum width (in pixels) needed to draw this symbol
     */
    public int getMinWidth() {
        return 3 * MusicBook.NoteHeight / 2;
    }

    /**
     * Get/Set the width (in pixels) of this symbol. The width is set
     * in SheetMusic.AlignSymbols() to vertically align symbols.
     */
    public int getWidth() {
        return width;
    }

    public void setWidth(int value) {
        width = value;
    }

    /**
     * Get the number of pixels this symbol extends above the staff. Used
     * to determine the minimum height needed for the staff (Staff.FindBounds).
     */
    public int getAboveStaff() {
        int dist = WhiteNote.Top(clef).Dist(whitenote) *
                MusicBook.NoteHeight / 2;
        if (accid == Accid.Sharp || accid == Accid.Natural)
            dist -= MusicBook.NoteHeight;
        else if (accid == Accid.Flat)
            dist -= 3 * MusicBook.NoteHeight / 2;

        if (dist < 0)
            return -dist;
        else
            return 0;
    }

    /**
     * Get the number of pixels this symbol extends below the staff. Used
     * to determine the minimum height needed for the staff (Staff.FindBounds).
     */
    public int getBelowStaff() {
        int dist = WhiteNote.Bottom(clef).Dist(whitenote) *
                MusicBook.NoteHeight / 2 +
                MusicBook.NoteHeight;
        if (accid == Accid.Sharp || accid == Accid.Natural)
            dist += MusicBook.NoteHeight;

        if (dist > 0)
            return dist;
        else
            return 0;
    }

    /**
     * Draw the symbol.
     *
     * @param ytop The ylocation (in pixels) where the top of the staff starts.
     */
    public void Draw(Canvas canvas, Paint paint, int ytop) {
        /* Align the symbol to the right */
        canvas.translate(getWidth() - getMinWidth(), 0);

        /* Store the y-pixel value of the top of the whitenote in ynote. */
        int ynote = ytop + WhiteNote.Top(clef).Dist(whitenote) *
                MusicBook.NoteHeight / 2;

        if (accid == Accid.Sharp)
            DrawSharp(canvas, paint, ynote);
        else if (accid == Accid.Flat)
            DrawFlat(canvas, paint, ynote);
        else if (accid == Accid.Natural)
            DrawNatural(canvas, paint, ynote);

        canvas.translate(-(getWidth() - getMinWidth()), 0);
    }

    /**
     * Draw a sharp symbol.
     *
     * @param ynote The pixel location of the top of the accidental's note.
     */
    public void DrawSharp(Canvas canvas, Paint paint, int ynote) {

        /* Draw the two vertical lines */
        int ystart = ynote - MusicBook.NoteHeight;
        int yend = ynote + 2 * MusicBook.NoteHeight;
        int x = MusicBook.NoteHeight / 2;
        paint.setStrokeWidth(1);
        canvas.drawLine(x, ystart + 2, x, yend, paint);
        x += MusicBook.NoteHeight / 2;
        canvas.drawLine(x, ystart, x, yend - 2, paint);

        /* Draw the slightly upwards horizontal lines */
        int xstart = MusicBook.NoteHeight / 2 - MusicBook.NoteHeight / 4;
        int xend = MusicBook.NoteHeight + MusicBook.NoteHeight / 4;
        ystart = ynote + MusicBook.LineWidth;
        yend = ystart - MusicBook.LineWidth - MusicBook.LineSpace / 4;
        paint.setStrokeWidth(MusicBook.LineSpace / 2);
        canvas.drawLine(xstart, ystart, xend, yend, paint);
        ystart += MusicBook.LineSpace;
        yend += MusicBook.LineSpace;
        canvas.drawLine(xstart, ystart, xend, yend, paint);
        paint.setStrokeWidth(1);
    }

    /**
     * Draw a flat symbol.
     *
     * @param ynote The pixel location of the top of the accidental's note.
     */
    public void DrawFlat(Canvas canvas, Paint paint, int ynote) {
        int x = MusicBook.LineSpace / 4;

        /* Draw the vertical line */
        paint.setStrokeWidth(1);
        canvas.drawLine(x, ynote - MusicBook.NoteHeight - MusicBook.NoteHeight / 2,
                x, ynote + MusicBook.NoteHeight, paint);

        /* Draw 3 bezier curves.
         * All 3 curves start and stop at the same points.
         * Each subsequent curve bulges more and more towards 
         * the topright corner, making the curve look thicker
         * towards the top-right.
         */
        Path bezierPath = new Path();
        bezierPath.moveTo(x, ynote + MusicBook.LineSpace / 4);
        bezierPath.cubicTo(x + MusicBook.LineSpace / 2, ynote - MusicBook.LineSpace / 2,
                x + MusicBook.LineSpace, ynote + MusicBook.LineSpace / 3,
                x, ynote + MusicBook.LineSpace + MusicBook.LineWidth + 1);
        canvas.drawPath(bezierPath, paint);

        bezierPath = new Path();
        bezierPath.moveTo(x, ynote + MusicBook.LineSpace / 4);
        bezierPath.cubicTo(x + MusicBook.LineSpace / 2, ynote - MusicBook.LineSpace / 2,
                x + MusicBook.LineSpace + MusicBook.LineSpace / 4,
                ynote + MusicBook.LineSpace / 3 - MusicBook.LineSpace / 4,
                x, ynote + MusicBook.LineSpace + MusicBook.LineWidth + 1);
        canvas.drawPath(bezierPath, paint);

        bezierPath = new Path();
        bezierPath.moveTo(x, ynote + MusicBook.LineSpace / 4);
        bezierPath.cubicTo(x + MusicBook.LineSpace / 2, ynote - MusicBook.LineSpace / 2,
                x + MusicBook.LineSpace + MusicBook.LineSpace / 2,
                ynote + MusicBook.LineSpace / 3 - MusicBook.LineSpace / 2,
                x, ynote + MusicBook.LineSpace + MusicBook.LineWidth + 1);
        canvas.drawPath(bezierPath, paint);

    }

    /**
     * Draw a natural symbol.
     *
     * @param ynote The pixel location of the top of the accidental's note.
     */
    public void DrawNatural(Canvas canvas, Paint paint, int ynote) {

        /* Draw the two vertical lines */
        int ystart = ynote - MusicBook.LineSpace - MusicBook.LineWidth;
        int yend = ynote + MusicBook.LineSpace + MusicBook.LineWidth;
        int x = MusicBook.LineSpace / 2;
        paint.setStrokeWidth(1);
        canvas.drawLine(x, ystart, x, yend, paint);
        x += MusicBook.LineSpace - MusicBook.LineSpace / 4;
        ystart = ynote - MusicBook.LineSpace / 4;
        yend = ynote + 2 * MusicBook.LineSpace + MusicBook.LineWidth -
                MusicBook.LineSpace / 4;
        canvas.drawLine(x, ystart, x, yend, paint);

        /* Draw the slightly upwards horizontal lines */
        int xstart = MusicBook.LineSpace / 2;
        int xend = xstart + MusicBook.LineSpace - MusicBook.LineSpace / 4;
        ystart = ynote + MusicBook.LineWidth;
        yend = ystart - MusicBook.LineWidth - MusicBook.LineSpace / 4;
        paint.setStrokeWidth(MusicBook.LineSpace / 2);
        canvas.drawLine(xstart, ystart, xend, yend, paint);
        ystart += MusicBook.LineSpace;
        yend += MusicBook.LineSpace;
        canvas.drawLine(xstart, ystart, xend, yend, paint);
        paint.setStrokeWidth(1);
    }


    public String toString() {
        return String.format(
                "AccidSymbol accid={0} whitenote={1} clef={2} width={3}",
                accid, whitenote, clef, width);
    }

}



