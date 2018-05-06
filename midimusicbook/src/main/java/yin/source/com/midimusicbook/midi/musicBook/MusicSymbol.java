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

/** @class MusicSymbol // 乐谱
 * The MusicSymbol class represents music symbols that can be displayed
 * on a staff.  This includes:
 *  - Accidental symbols: sharp, flat, natural
 *  - Chord symbols: single notes or chords
 *  - Rest symbols: whole, half, quarter, eighth
 *  - Bar symbols, the vertical bars which delimit measures.
 *  - Treble and Bass clef symbols
 *  - Blank symbols, used for aligning notes in different staffs
 *  这个MusicSymbol类表示可以显示在一个五线谱谱表上的乐谱.它包括：
 *  临时的符号：半高音符号，降号，本位音
 *  和弦符号：单一的音符或者和弦
 *  休止符(停顿，句读)：完整的，一半的，四分之一的，八分之一的
 *  小节线：界定拍子的垂直小节线
 *  最高音和低音部谱号符
 *  空白符，用来对齐不同五线谱上的音符
 */

public interface MusicSymbol {

    /** Get the time (in pulses) this symbol occurs at.
     * This is used to determine the measure this symbol belongs to.
     * 获取当前符号出现的时间(脉冲单位).
     * 这个用来终止当前符号所属的小节(拍子).
     */
    public int getStartTime();

    /**
     * Get the minimum width (in pixels) needed to draw this symbol
     * 获取需要绘制符号的最小宽度(单位像素)
     */
    public int getMinWidth();

    /** Get/Set the width (in pixels) of this symbol. The width is set
     * in SheetMusic.AlignSymbols() to vertically align symbols.
     * 获取/设置这个符号的宽度(单位像素).这个宽度在SheetMusic的AlignSymbols()方法中垂直对齐符号.
     */
    public int getWidth();
    public void setWidth(int value);

    /** Get the number of pixels this symbol extends above the staff. Used
     *  to determine the minimum height needed for the staff (Staff.FindBounds).
     *  获取这个符号高出五线谱的像素数.用来确定符号需要的最小高度.
     */
    public int getAboveStaff();

    /** Get the number of pixels this symbol extends below the staff. Used
     *  to determine the minimum height needed for the staff (Staff.FindBounds).
     *  获取这个符号低于五线谱的像素数.用来确定符号需要的最小高度.
     */
    public int getBelowStaff();

    /** Draw the symbol.
     * @param ytop The ylocation (in pixels) where the top of the staff starts.
     * 绘制这个符号.
     *
     */
    public void Draw(Canvas canvas, Paint paint, int ytop);

}


