package yin.source.com.midimusicbook.midi.baseBean;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;

import yin.source.com.midimusicbook.R;


/**
 * 小节线对象
 * Created by Yin on 2017/8/4.
 */

public class BarIndicating {

    private Point startPoint;

    private Point point;
    //第几小节
    private int barNum;
    private int width;
    private int height;
    private Paint paint;
    private boolean isBarStart;
    private Context context;

    public BarIndicating(Context context, int barNum, boolean isBarStart, Point startPoint, int viewWidth) {
        this.context = context;
        this.barNum = barNum;
        this.startPoint = startPoint;
        this.point = new Point(startPoint.x, startPoint.y);
        this.width = isBarStart ? (int) (viewWidth * 2 / 5.0) : (int) (viewWidth / 5.0);
        this.height = isBarStart ? 5 : 3;
        this.isBarStart = isBarStart;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize((float) ((viewWidth - width) / 2.0));
//        int color = ContextCompat.getColor(context, R.color.colorAccent);
        int color = ContextCompat.getColor(context, R.color.red);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
    }

    public void moveFromStartPoint(int dy) {
        point.y = startPoint.y + dy;
    }

    public void drawSelf(Canvas canvas) {
        RectF rect = new RectF(point.x, point.y - height, width + point.x, point.y);
        canvas.drawRect(rect, paint);
        if (isBarStart) {
            canvas.drawText(String.valueOf(barNum), (float) (width + width / 8.0), point.y, paint);
        }
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public Point getPoint() {
        return point;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "BarIndicating{" +
                "startPoint=" + startPoint +
                ", point=" + point +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
