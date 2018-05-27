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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import yin.source.com.midimusicbook.R;

/** @class ClefSymbol 谱号符号
 * A ClefSymbol represents either a Treble or Bass Clef image.
 * The clef can be either normal or small size.  Normal size is
 * used at the beginning of a new staff, on the left side.  The
 * small symbols are used to show clef changes within a staff.
 * 一个谱号表现最高音或者低音部图片
 * 这个谱号可以是正常的尺寸或者小尺寸.当一个小节从左边开始的时候使用正常的尺寸，
 * 在一个小节中显示谱号变化的时候使用小符号
 */

public class ClefSymbol implements MusicSymbol {
    public static Bitmap treble;  /** The treble clef image */ // 最高音谱号图片
    private static Bitmap bass;    /** The bass clef image */ // 低音部谱号图片

    private int starttime;        /** Start time of the symbol */ // 谱号开始时间
    private boolean smallsize;    /** True if this is a small clef, false otherwise */ // 是否是一个小尺寸谱号
    private Clef clef;            /** The clef, Treble or Bass */ // 谱号，最高音部或者低音部
    private int width;

    /**
     * Create a new ClefSymbol, with the given clef, starttime, and size
     * 根据提供的谱号，开始时间和尺寸创建一个新的ClefSymbol对象
     */
    public ClefSymbol(Context context, Clef clef, int starttime, boolean small) {
        LoadImages(context);
        this.clef = clef;
        this.starttime = starttime;
        smallsize = small;
        width = getMinWidth();
    }

    /**
     * Set the Treble/Bass clef images into memory.
     * 将高音谱和低音的谱号图片放进内存
     */
    public static void LoadImages(Context context) {
        if (treble == null || bass == null) {
            Resources res = context.getResources();
            treble = BitmapFactory.decodeResource(res, R.drawable.treble);
            bass = BitmapFactory.decodeResource(res, R.drawable.bass);
        }
    }

    /** Get the time (in pulses) this symbol occurs at.
     * This is used to determine the measure this symbol belongs to.
     * 获取这个符号开始出现的时间(脉冲为单位).
     * 用来中断这个符号所属的五线谱
     */
    public int getStartTime() { return starttime; }

    /**
     * Get the minimum width (in pixels) needed to draw this symbol
     * 获取需要绘制的符号的最小的宽度
     */
    public int getMinWidth() { 
        if (smallsize)
            return MusicBook.NoteWidth * 2;
        else
            return MusicBook.NoteWidth * 3;
    } 

    /** Get/Set the width (in pixels) of this symbol. The width is set
     * in SheetMusic.AlignSymbols() to vertically align symbols.
     * 获取/设置符号的宽度.这个宽度在SheetMusic的AlignSymbols方法中设置用来垂直对齐符号
     */
    public int getWidth() { return width; }
    public void setWidth(int value){ width = value; }

    /** Get the number of pixels this symbol extends above the staff. Used
     *  to determine the minimum height needed for the staff (Staff.FindBounds).
     *  获取这个符号高数五线谱表的像素数.用来中断五线谱所需的最小高度
     */
    public int getAboveStaff() { 
        if (clef == Clef.Treble && !smallsize)
            return MusicBook.NoteHeight * 2;
        else
            return 0;
    }

    /** Get the number of pixels this symbol extends below the staff. Used
     *  to determine the minimum height needed for the staff (Staff.FindBounds).
     */
    public int getBelowStaff() {
        if (clef == Clef.Treble && !smallsize)
            return MusicBook.NoteHeight * 2;
        else if (clef == Clef.Treble && smallsize)
            return MusicBook.NoteHeight;
        else
            return 0;
    }

    /** Draw the symbol.
     * @param ytop The ylocation (in pixels) where the top of the staff starts.
     */
    public 
    void Draw(Canvas canvas, Paint paint, int ytop) {
        canvas.translate(getWidth() - getMinWidth(), 0);
        int y = ytop;
        Bitmap image;
        int height;

        /* Get the image, height, and top y pixel, depending on the clef
         * and the image size.
         */
        if (clef == Clef.Treble) {
            image = treble;
            if (smallsize) {
                height = MusicBook.StaffHeight + MusicBook.StaffHeight/4;
            } else {
                height = 3 * MusicBook.StaffHeight/2 + MusicBook.NoteHeight/2;
                y = ytop - MusicBook.NoteHeight;
            }
        }
        else {
            image = bass;
            if (smallsize) {
                height = MusicBook.StaffHeight - 3* MusicBook.NoteHeight/2;
            } else {
                height = MusicBook.StaffHeight - MusicBook.NoteHeight;
            }
        }

        /* Scale the image width to match the height */
        int imgwidth = image.getWidth() * height / image.getHeight();
        Rect src = new Rect(0, 0, image.getWidth(), image.getHeight());
        Rect dest = new Rect(0, y, 0 + imgwidth, y + height);
        canvas.drawBitmap(image, src, dest, paint);
        canvas.translate(-(getWidth() - getMinWidth()), 0);
    }

    public String toString() {
        return String.format("ClefSymbol clef=%1$s small=%2$s width=%3$s",
                             clef, smallsize, width);
    }
}


