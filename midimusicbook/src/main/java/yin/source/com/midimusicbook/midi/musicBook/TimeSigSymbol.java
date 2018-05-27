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


/**
 * @class TimeSigSymbol 拍子记号符号
 * A TimeSigSymbol represents the time signature at the beginning
 * of the staff. We use pre-made images for the numbers, instead of
 * drawing strings.
 * 一个TimeSigSymbol代表了五线谱开始位置的拍子符号.我们使用提前制作的图片表示数字而不是画字符串
 */

public class TimeSigSymbol implements MusicSymbol {
    private static Bitmap[] images;// 每一个数字的代表图片
    /**
     * The images for each number
     */
    private int numerator;// 分子
    /**
     * The numerator
     */
    private int denominator;// 分母
    /**
     * The denominator
     */
    private int width;// 宽度(单位像素)
    /**
     * The width in pixels
     */
    private boolean candraw;// 我们是否可以画拍子符号
    /**
     * True if we can draw the time signature
     */

    /**
     * Create a new TimeSigSymbol
     * 创建一个TimeSigSymbol
     */
    public TimeSigSymbol(Context context, int numer, int denom) {
        LoadImages(context);
        numerator = numer;// 分子
        denominator = denom;// 分母
        if (numer >= 0 && numer < images.length && images[numer] != null &&
                denom >= 0 && denom < images.length && images[numer] != null) {
            candraw = true;
        } else {
            candraw = false;
        }
        width = getMinWidth();
    }

    /**
     * Load the images into memory.
     */
    public static void LoadImages(Context context) {
        if (images != null) {
            return;
        }
        images = new Bitmap[13];
        Resources res = context.getResources();
        images[2] = BitmapFactory.decodeResource(res, R.drawable.two);
        images[3] = BitmapFactory.decodeResource(res, R.drawable.three);
        images[4] = BitmapFactory.decodeResource(res, R.drawable.four);
        images[6] = BitmapFactory.decodeResource(res, R.drawable.six);
        images[8] = BitmapFactory.decodeResource(res, R.drawable.eight);
        images[9] = BitmapFactory.decodeResource(res, R.drawable.nine);
        images[12] = BitmapFactory.decodeResource(res, R.drawable.twelve);
    }

    /**
     * Get the time (in pulses) this symbol occurs at.
     * 获取当前符号出现时的时间(脉冲形式)
     */
    public int getStartTime() {
        return -1;
    }

    /**
     * Get the minimum width (in pixels) needed to draw this symbol
     * 获取需要绘制的符号的最小宽度(以像素为单位)
     */
    public int getMinWidth() {
        if (candraw)
            return images[2].getWidth() * MusicBook.NoteHeight * 2 / images[2].getHeight();
        else
            return 0;
    }

    /**
     * Get/Set the width (in pixels) of this symbol. The width is set
     * in SheetMusic.AlignSymbols() to vertically align symbols.
     * 获取/设置当前符号的宽度(像素为单位).这个宽度在SheetMusic的AlignSymbols方法中设置用来垂直对齐符号
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
     * Draw the symbol.画符号
     *
     * @param ytop The ylocation (in pixels) where the top of the staff starts.
     */
    public void Draw(Canvas canvas, Paint paint, int ytop) {
        if (!candraw)
            return;

        canvas.translate(getWidth() - getMinWidth(), 0);
        Bitmap numer = images[numerator];
        Bitmap denom = images[denominator];

        /* Scale the image width to match the height */ // 缩放图片的宽度使其匹配音符高度
        int imgheight = MusicBook.NoteHeight * 2;
        int imgwidth = numer.getWidth() * imgheight / numer.getHeight();
        Rect src = new Rect(0, 0, numer.getWidth(), numer.getHeight());
        Rect dest = new Rect(0, ytop, imgwidth, ytop + imgheight);
        canvas.drawBitmap(numer, src, dest, paint);

        src = new Rect(0, 0, denom.getWidth(), denom.getHeight());
        dest = new Rect(0, ytop + MusicBook.NoteHeight * 2, imgwidth, ytop + MusicBook.NoteHeight * 2 + imgheight);
        canvas.drawBitmap(denom, src, dest, paint);
        canvas.translate(-(getWidth() - getMinWidth()), 0);
    }

    public String toString() {
        return String.format("TimeSigSymbol numerator=%1$s denominator=%2$s",
                numerator, denominator);
    }
}


