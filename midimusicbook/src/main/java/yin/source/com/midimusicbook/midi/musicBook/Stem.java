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

import yin.source.com.midimusicbook.midi.baseBean.NoteDuration;

/**
 * @class Stem 符干
 * The Stem class is used by ChordSymbol to draw the stem portion of
 * the chord.  The stem has the following fields:
 * 这个Stem类被ChordSymbol用来绘制和弦的符干部分.符干具有以下成员变量
 * <p/>
 * duration  - The duration of the stem. 音长
 * direction - Either Up or Down 方向(向上或者向下)
 * side      - Either left or right 左边或者右边
 * top       - The topmost note in the chord 和弦中最顶端的音符
 * bottom    - The bottommost note in the chord 和弦中最底部的音符
 * end       - The note position where the stem ends.  This is usually 符干借书处的音符的位置
 * six notes past the last note in the chord.  For 8th/16th 在和弦最后的音符处通常有六个音符.八分音符和十六分音符
 * notes, the stem must extend even more.
 * <p/>
 * The SheetMusic class can change the direction of a stem after it
 * has been created.  The side and end fields may also change due to
 * the direction change.  But other fields will not change.
 * 当符干被创建后,SheetMusic类可以改变符干的方向.side和end也有可能因为方向的改变而改变.但是其他的成员变量不会改变
 */

public class Stem {
    public static final int Up = 1;      /* The stem points up */ // 符干向上
    public static final int Down = 2;      /* The stem points down */ // 符干向下
    public static final int LeftSide = 1;  /* The stem is to the left of the note */ // 音符左边的符干
    public static final int RightSide = 2; /* The stem is to the right of the note */ // 音符右边的符干

    private NoteDuration duration;// 符干音长
    /**
     * Duration of the stem.
     */
    private int direction;// 符干方向
    /**
     * Up, Down, or None
     */
    private WhiteNote top;// 和弦中最顶端的音符
    /**
     * Topmost note in chord
     */
    private WhiteNote bottom;// 和弦中最底端的音符
    /**
     * Bottommost note in chord
     */
    private WhiteNote end;// 符干结束处的位置
    /**
     * Location of end of the stem
     */
    private boolean notesoverlap;// 使和弦音符重叠
    /**
     * Do the chord notes overlap
     */
    private int side;// 音符的左边或者右边
    /**
     * Left side or right side of note
     */

    private Stem pair;// 如果pair!=null,这是一个水平的焊接在另一个和弦上的符干
    /**
     * If pair != null, this is a horizontal
     * beam stem to another chord
     */
    private int width_to_pair;// 对和弦的宽度(像素为单位)
    /**
     * The width (in pixels) to the chord pair
     */
    private boolean receiver_in_pair;
    /**
     * This stem is the receiver of a horizontal beam stem from another chord.
     * 这个符干是从另一个和弦衣服水平过渡的桥梁
     */

    /**
     * Get/Set the direction of the stem (Up or Down) 获取/设置符干的方向(向上或者向下)
     */
    public int getDirection() {
        return direction;
    }

    public void setDirection(int value) {
        ChangeDirection(value);
    }

    /**
     * Get the duration of the stem (Eigth, Sixteenth, ThirtySecond) 获取符干的音长(八分音符,十六分音符,30s)
     */
    public NoteDuration getDuration() {
        return duration;
    }

    /**
     * Get the top note in the chord. This is needed to determine the stem direction
     * 获取和弦音符中的顶部音符.这个用来确定符干的方向
     */
    public WhiteNote getTop() {
        return top;
    }

    /**
     * Get the bottom note in the chord. This is needed to determine the stem direction
     * 获取和弦音符中的底部音符.这个用来确定符干的方向
     */
    public WhiteNote getBottom() {
        return bottom;
    }

    /**
     * Get/Set the location where the stem ends.  This is usually six notes
     * past the last note in the chord. See method CalculateEnd.
     * 获取/设置符干结束的位置......看CalculateEnd方法
     */
    public WhiteNote getEnd() {
        return end;
    }

    public void setEnd(WhiteNote value) {
        end = value;
    }

    /**
     * Set this Stem to be the receiver of a horizontal beam, as part
     * of a chord pair.  In Draw(), if this stem is a receiver, we
     * don't draw a curvy stem, we only draw the vertical line.
     * 如果这个符干是一个接受者,我们不画一个弯曲的符干,我们只画垂直的线
     */
    public boolean getReceiver() {
        return receiver_in_pair;
    }

    public void setReceiver(boolean value) {
        receiver_in_pair = value;
    }

    /**
     * Create a new stem.  The top note, bottom note, and direction are
     * needed for drawing the vertical line of the stem.  The duration is
     * needed to draw the tail of the stem.  The overlap boolean is true
     * if the notes in the chord overlap.  If the notes overlap, the
     * stem must be drawn on the right side.
     * 创建一个新的符干.顶部音符,底部音符,和方向是绘制符干的垂直线的必需的.音长是绘制符干的尾巴所必须的.
     * 如果和弦中的音符是重叠的overlap为true。如果音符重叠,这个符干必须画在右边.
     */
    public Stem(WhiteNote bottom, WhiteNote top,
                NoteDuration duration, int direction, boolean overlap) {

        this.top = top;
        this.bottom = bottom;
        this.duration = duration;
        this.direction = direction;
        this.notesoverlap = overlap;
        if (direction == Up || notesoverlap)
            side = RightSide;
        else
            side = LeftSide;
        end = CalculateEnd();
        pair = null;
        width_to_pair = 0;
        receiver_in_pair = false;
    }

    /**
     * Calculate the vertical position (white note key) where
     * the stem ends 计算符干结束位置
     */
    public WhiteNote CalculateEnd() {
        if (direction == Up) {
            WhiteNote w = top;
            w = w.Add(6);
            if (duration == NoteDuration.Sixteenth) {
                w = w.Add(2);
            } else if (duration == NoteDuration.ThirtySecond) {
                w = w.Add(4);
            }
            return w;
        } else if (direction == Down) {
            WhiteNote w = bottom;
            w = w.Add(-6);
            if (duration == NoteDuration.Sixteenth) {
                w = w.Add(-2);
            } else if (duration == NoteDuration.ThirtySecond) {
                w = w.Add(-4);
            }
            return w;
        } else {
            return null;  /* Shouldn't happen */
        }
    }

    /**
     * Change the direction of the stem.  This function is called by
     * ChordSymbol.MakePair().  When two chords are joined by a horizontal
     * beam, their stems must point in the same direction (up or down).
     * 改变符干的方向.这个方法被ChordSymbol的makePair方法调用.当两个和弦音符被水平的连接,他们的符干防线必须相同
     */
    public void ChangeDirection(int newdirection) {
        direction = newdirection;
        if (direction == Up || notesoverlap)
            side = RightSide;
        else
            side = LeftSide;
        end = CalculateEnd();
    }

    /**
     * Pair this stem with another Chord.  Instead of drawing a curvy tail,
     * this stem will now have to draw a beam to the given stem pair.  The
     * width (in pixels) to this stem pair is passed as argument.
     * 将此符干和另一个和弦音配对.将不得不根据所给的符干对画一个横梁而不是画一个弯曲的结尾.
     */
    public void SetPair(Stem pair, int width_to_pair) {
        this.pair = pair;
        this.width_to_pair = width_to_pair;
    }

    /**
     * Return true if this Stem is part of a horizontal beam.
     * 当这个符干是一个水平横梁的一部分则返回true
     */
    public boolean IsBeam() {
        return receiver_in_pair || (pair != null);
    }

    /**
     * Draw this stem.
     *
     * @param ytop     The y location (in pixels) where the top of the staff starts.
     * @param topstaff The note at the top of the staff.
     */
    public void Draw(Canvas canvas, Paint paint, int ytop, WhiteNote topstaff) {
        if (duration == NoteDuration.Whole)
            return;

        DrawVerticalLine(canvas, paint, ytop, topstaff);
        if (duration == NoteDuration.Quarter ||
                duration == NoteDuration.DottedQuarter ||
                duration == NoteDuration.Half ||
                duration == NoteDuration.DottedHalf ||
                receiver_in_pair) {

            return;
        }

        if (pair != null)
            DrawHorizBarStem(canvas, paint, ytop, topstaff);
        else
            DrawCurvyStem(canvas, paint, ytop, topstaff);
    }

    /**
     * Draw the vertical line of the stem
     * 绘制符干垂直的线段
     *
     * @param ytop     The y location (in pixels) where the top of the staff starts.
     * @param topstaff The note at the top of the staff.
     */
    private void DrawVerticalLine(Canvas canvas, Paint paint, int ytop, WhiteNote topstaff) {
        int xstart;
        if (side == LeftSide)
            xstart = MusicBook.LineSpace / 4 + 1;
        else
            xstart = MusicBook.LineSpace / 4 + MusicBook.NoteWidth;

        if (direction == Up) {
            int y1 = ytop + topstaff.Dist(bottom) * MusicBook.NoteHeight / 2
                    + MusicBook.NoteHeight / 4;

            int ystem = ytop + topstaff.Dist(end) * MusicBook.NoteHeight / 2;

            canvas.drawLine(xstart, y1, xstart, ystem, paint);
        } else if (direction == Down) {
            int y1 = ytop + topstaff.Dist(top) * MusicBook.NoteHeight / 2
                    + MusicBook.NoteHeight;

            if (side == LeftSide)
                y1 = y1 - MusicBook.NoteHeight / 4;
            else
                y1 = y1 - MusicBook.NoteHeight / 2;

            int ystem = ytop + topstaff.Dist(end) * MusicBook.NoteHeight / 2
                    + MusicBook.NoteHeight;

            canvas.drawLine(xstart, y1, xstart, ystem, paint);
        }
    }

    /**
     * Draw a curvy stem tail.  This is only used for single chords, not chord pairs.
     * 绘制一个弯曲的符干尾巴.这个仅仅应用在单一的和弦，不适应于和弦对.
     *
     * @param ytop     The y location (in pixels) where the top of the staff starts.
     * @param topstaff The note at the top of the staff.
     */
    private void DrawCurvyStem(Canvas canvas, Paint paint, int ytop, WhiteNote topstaff) {
        Path bezierPath;
        paint.setStrokeWidth(2);

        int xstart = 0;
        if (side == LeftSide)
            xstart = MusicBook.LineSpace / 4 + 1;
        else
            xstart = MusicBook.LineSpace / 4 + MusicBook.NoteWidth;

        if (direction == Up) {
            int ystem = ytop + topstaff.Dist(end) * MusicBook.NoteHeight / 2;

            if (duration == NoteDuration.Eighth ||
                    duration == NoteDuration.DottedEighth ||
                    duration == NoteDuration.Triplet ||
                    duration == NoteDuration.Sixteenth ||
                    duration == NoteDuration.ThirtySecond) {

                bezierPath = new Path();
                bezierPath.moveTo(xstart, ystem);
                bezierPath.cubicTo(xstart, ystem + 3 * MusicBook.LineSpace / 2,
                        xstart + MusicBook.LineSpace * 2, ystem + MusicBook.NoteHeight * 2,
                        xstart + MusicBook.LineSpace / 2, ystem + MusicBook.NoteHeight * 3);
                canvas.drawPath(bezierPath, paint);

            }
            ystem += MusicBook.NoteHeight;

            if (duration == NoteDuration.Sixteenth ||
                    duration == NoteDuration.ThirtySecond) {

                bezierPath = new Path();
                bezierPath.moveTo(xstart, ystem);
                bezierPath.cubicTo(xstart, ystem + 3 * MusicBook.LineSpace / 2,
                        xstart + MusicBook.LineSpace * 2, ystem + MusicBook.NoteHeight * 2,
                        xstart + MusicBook.LineSpace / 2, ystem + MusicBook.NoteHeight * 3);
                canvas.drawPath(bezierPath, paint);

            }

            ystem += MusicBook.NoteHeight;
            if (duration == NoteDuration.ThirtySecond) {
                bezierPath = new Path();
                bezierPath.moveTo(xstart, ystem);
                bezierPath.cubicTo(xstart, ystem + 3 * MusicBook.LineSpace / 2,
                        xstart + MusicBook.LineSpace * 2, ystem + MusicBook.NoteHeight * 2,
                        xstart + MusicBook.LineSpace / 2, ystem + MusicBook.NoteHeight * 3);
                canvas.drawPath(bezierPath, paint);

            }

        } else if (direction == Down) {
            int ystem = ytop + topstaff.Dist(end) * MusicBook.NoteHeight / 2 +
                    MusicBook.NoteHeight;

            if (duration == NoteDuration.Eighth ||
                    duration == NoteDuration.DottedEighth ||
                    duration == NoteDuration.Triplet ||
                    duration == NoteDuration.Sixteenth ||
                    duration == NoteDuration.ThirtySecond) {

                bezierPath = new Path();
                bezierPath.moveTo(xstart, ystem);
                bezierPath.cubicTo(xstart, ystem - MusicBook.LineSpace,
                        xstart + MusicBook.LineSpace * 2, ystem - MusicBook.NoteHeight * 2,
                        xstart + MusicBook.LineSpace, ystem - MusicBook.NoteHeight * 2 - MusicBook.LineSpace / 2);
                canvas.drawPath(bezierPath, paint);

            }
            ystem -= MusicBook.NoteHeight;

            if (duration == NoteDuration.Sixteenth ||
                    duration == NoteDuration.ThirtySecond) {

                bezierPath = new Path();
                bezierPath.moveTo(xstart, ystem);
                bezierPath.cubicTo(xstart, ystem - MusicBook.LineSpace,
                        xstart + MusicBook.LineSpace * 2, ystem - MusicBook.NoteHeight * 2,
                        xstart + MusicBook.LineSpace, ystem - MusicBook.NoteHeight * 2 - MusicBook.LineSpace / 2);
                canvas.drawPath(bezierPath, paint);

            }

            ystem -= MusicBook.NoteHeight;
            if (duration == NoteDuration.ThirtySecond) {
                bezierPath = new Path();
                bezierPath.moveTo(xstart, ystem);
                bezierPath.cubicTo(xstart, ystem - MusicBook.LineSpace,
                        xstart + MusicBook.LineSpace * 2, ystem - MusicBook.NoteHeight * 2,
                        xstart + MusicBook.LineSpace, ystem - MusicBook.NoteHeight * 2 - MusicBook.LineSpace / 2);
                canvas.drawPath(bezierPath, paint);

            }

        }
        paint.setStrokeWidth(1);

    }

    /**
     * Draw a horizontal beam stem, connecting this stem with the Stem pair.
     * 绘制一个水平的横梁符干,连接着这个符干和符干对
     *
     * @param ytop     The y location (in pixels) where the top of the staff starts.
     * @param topstaff The note at the top of the staff.
     */
    private void DrawHorizBarStem(Canvas canvas, Paint paint, int ytop, WhiteNote topstaff) {
        paint.setStrokeWidth(MusicBook.NoteHeight / 2);
        paint.setStrokeCap(Paint.Cap.BUTT);
        int xstart = 0;
        int xstart2 = 0;

        if (side == LeftSide)
            xstart = MusicBook.LineSpace / 4 + 1;
        else if (side == RightSide)
            xstart = MusicBook.LineSpace / 4 + MusicBook.NoteWidth;

        if (pair.side == LeftSide)
            xstart2 = MusicBook.LineSpace / 4 + 1;
        else if (pair.side == RightSide)
            xstart2 = MusicBook.LineSpace / 4 + MusicBook.NoteWidth;


        if (direction == Up) {
            int xend = width_to_pair + xstart2;
            int ystart = ytop + topstaff.Dist(end) * MusicBook.NoteHeight / 2;
            int yend = ytop + topstaff.Dist(pair.end) * MusicBook.NoteHeight / 2;

            if (duration == NoteDuration.Eighth ||
                    duration == NoteDuration.DottedEighth ||
                    duration == NoteDuration.Triplet ||
                    duration == NoteDuration.Sixteenth ||
                    duration == NoteDuration.ThirtySecond) {

                canvas.drawLine(xstart, ystart, xend, yend, paint);
            }
            ystart += MusicBook.NoteHeight;
            yend += MusicBook.NoteHeight;

            /* A dotted eighth will connect to a 16th note. */
            if (duration == NoteDuration.DottedEighth) {
                int x = xend - MusicBook.NoteHeight;
                double slope = (yend - ystart) * 1.0 / (xend - xstart);
                int y = (int) (slope * (x - xend) + yend);

                canvas.drawLine(x, y, xend, yend, paint);
            }

            if (duration == NoteDuration.Sixteenth ||
                    duration == NoteDuration.ThirtySecond) {

                canvas.drawLine(xstart, ystart, xend, yend, paint);
            }
            ystart += MusicBook.NoteHeight;
            yend += MusicBook.NoteHeight;

            if (duration == NoteDuration.ThirtySecond) {
                canvas.drawLine(xstart, ystart, xend, yend, paint);
            }
        } else {
            int xend = width_to_pair + xstart2;
            int ystart = ytop + topstaff.Dist(end) * MusicBook.NoteHeight / 2 +
                    MusicBook.NoteHeight;
            int yend = ytop + topstaff.Dist(pair.end) * MusicBook.NoteHeight / 2
                    + MusicBook.NoteHeight;

            if (duration == NoteDuration.Eighth ||
                    duration == NoteDuration.DottedEighth ||
                    duration == NoteDuration.Triplet ||
                    duration == NoteDuration.Sixteenth ||
                    duration == NoteDuration.ThirtySecond) {

                canvas.drawLine(xstart, ystart, xend, yend, paint);
            }
            ystart -= MusicBook.NoteHeight;
            yend -= MusicBook.NoteHeight;

            /* A dotted eighth will connect to a 16th note. */
            if (duration == NoteDuration.DottedEighth) {
                int x = xend - MusicBook.NoteHeight;
                double slope = (yend - ystart) * 1.0 / (xend - xstart);
                int y = (int) (slope * (x - xend) + yend);

                canvas.drawLine(x, y, xend, yend, paint);
            }

            if (duration == NoteDuration.Sixteenth ||
                    duration == NoteDuration.ThirtySecond) {

                canvas.drawLine(xstart, ystart, xend, yend, paint);
            }
            ystart -= MusicBook.NoteHeight;
            yend -= MusicBook.NoteHeight;

            if (duration == NoteDuration.ThirtySecond) {
                canvas.drawLine(xstart, ystart, xend, yend, paint);
            }
        }
        paint.setStrokeWidth(1);
    }

    @Override
    public String toString() {
        return String.format("Stem duration=%1$s direction=%2$s top=%3$s bottom=%4$s end=%5$s" +
                        " overlap=%6$s side=%7$s width_to_pair=%8$s receiver_in_pair=%9$s",
                duration, direction, top.toString(), bottom.toString(),
                end.toString(), notesoverlap, side, width_to_pair, receiver_in_pair);
    }

} 


