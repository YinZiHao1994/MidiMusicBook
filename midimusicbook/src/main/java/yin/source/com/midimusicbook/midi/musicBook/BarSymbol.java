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


/**
 * @class BarSymbol 小节线符号
 * The BarSymbol represents the vertical bars which delimit measures.
 * The starttime of the symbol is the beginning of the new
 * measure.
 * 这个BarSymbol代表着划分小节的垂直的小节线.这个符号的开始时间是这个新的小节的开始
 */
public class BarSymbol implements MusicSymbol {
    private int starttime;
    private int width;

    /**
     * Create a BarSymbol. The starttime should be the beginning of a measure.
     * 创建一个新的BarSymbol。starttime应该是一个小节的开始.
     */
    public BarSymbol(int starttime) {
        this.starttime = starttime;
        width = getMinWidth();
    }

    /**
     * Get the time (in pulses) this symbol occurs at.
     * This is used to determine the measure this symbol belongs to.
     * 互殴这个符号出现的时间(脉冲单位).
     * 这个用来确定这个符号所属的小节
     */
    public int getStartTime() {
        return starttime;
    }

    /**
     * Get the minimum width (in pixels) needed to draw this symbol
     * 获取需要绘制的符号的最小宽度(像素为单位)
     */
    public int getMinWidth() {
        return 2 * MusicBook.LineSpace;
    }

    /**
     * Get/Set the width (in pixels) of this symbol. The width is set
     * in SheetMusic.AlignSymbols() to vertically align symbols.
     * 获取/设置这个符号的宽度.这个宽度在SheetMusic的AlignSymbols方法中设置用来垂直对齐符号
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
     *
     */
    public int getAboveStaff() {
        return 0;
    }

    /**
     * Get the number of pixels this symbol extends below the staff. Used
     * to determine the minimum height needed for the staff (Staff.FindBounds).
     */
    public int getBelowStaff() {
        return 0;
    }

    /**
     * Draw a vertical bar.
     *
     * @param ytop The ylocation (in pixels) where the top of the staff starts.
     */
    public void Draw(Canvas canvas, Paint paint, int ytop) {
        int y = ytop;
        int yend = y + MusicBook.LineSpace * 4 + MusicBook.LineWidth * 4;
        paint.setStrokeWidth(1);
        canvas.drawLine(MusicBook.NoteWidth / 2, y, MusicBook.NoteWidth / 2, yend, paint);

    }

    public String toString() {
        return String.format("BarSymbol starttime=%1$s width=%2$s",
                starttime, width);
    }
}



