package com.angcyo.paintdemo.paint;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;

/**
 * Created by angcyo on 15-10-29-029.
 */
public abstract class PaintShape {
    public static final String FIELD_SHAPE = "shape";
    public static final String FIELD_PAINT_COL = "paint_col";
    public static final String FIELD_PAINT_WIDTH = "paint_width";
    public static final String FIELD_PAINT_STYLE = "paint_style";
    public static final String FIELD_STARTXY = "start_xy";
    public static final String FIELD_ENDXY = "end_xy";

    public static final String FIELD_SEPARATOR_REG = "\\|";//匹配用
    public static final String FIELD_SEPARATOR = "|";//分隔图形的字段
    public static final String SHAPE_SEPARATOR = "@";//分隔不同的图形
    public static final String VALUE_SEPARATOR = ":";//分隔字段

    protected float mStartX, mStartY, mEndX, mEndY;
    protected Paint mPaint;

    public PaintShape(Paint paint) {
        this.mPaint = paint;
    }

    public PaintShape(float mStartX, float mStartY, Paint mPaint) {
        this.mStartX = mStartX;
        this.mStartY = mStartY;
        this.mPaint = mPaint;
    }

    public static PaintShape generateShape(String string) throws Exception {
        PaintShape shape = null;
        if (!TextUtils.isEmpty(string)) {
            String[] fields = string.split(FIELD_SEPARATOR_REG);

            //解析画笔
            Paint paint = new Paint();
            paint.setColor(Integer.parseInt(fields[1]));
            paint.setStrokeWidth(Float.parseFloat(fields[2]));
            String style = fields[3];
            if (style.equalsIgnoreCase("FILL")) {
                paint.setStyle(Paint.Style.FILL);
            } else if (style.equalsIgnoreCase("STROKE")) {
                paint.setStyle(Paint.Style.STROKE);
            } else if (style.equalsIgnoreCase("FILL_AND_STROKE")) {
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
            }

            //解析图形
            String startXY, endXY;
            startXY = fields[4];
            endXY = fields[5];

            String[] sXY, eXY;
            sXY = startXY.split(VALUE_SEPARATOR);
            eXY = endXY.split(VALUE_SEPARATOR);

            switch (Integer.parseInt(fields[0])) {
                case PaintConfig.Shape.Point:
                    shape = new PointShape(Float.parseFloat(sXY[0]), Float.parseFloat(sXY[1]), paint);
                    break;
                case PaintConfig.Shape.Line:
                    shape = new LineShape(Float.parseFloat(sXY[0]), Float.parseFloat(sXY[1]), paint);
                    break;
                case PaintConfig.Shape.Circle:
                    shape = new CircleShape(Float.parseFloat(sXY[0]), Float.parseFloat(sXY[1]), paint);
                    break;
                case PaintConfig.Shape.Rect:
                    shape = new RectShape(Float.parseFloat(sXY[0]), Float.parseFloat(sXY[1]), paint);
                    break;
                case PaintConfig.Shape.Image:
                    shape = new ImageShape(Float.parseFloat(sXY[0]), Float.parseFloat(sXY[1]), paint);
                    break;
                default:
                    break;
            }

            shape.setmEndX(Float.parseFloat(eXY[0]));
            shape.setmEndY(Float.parseFloat(eXY[1]));
        }

        return shape;
    }

    public float getmStartX() {
        return mStartX;
    }

    public void setmStartX(float mStartX) {
        this.mStartX = mStartX;
    }

    public float getmStartY() {
        return mStartY;
    }

    public void setmStartY(float mStartY) {
        this.mStartY = mStartY;
    }

    public float getmEndX() {
        return mEndX;
    }

    public void setmEndX(float mEndX) {
        this.mEndX = mEndX;
    }

    public float getmEndY() {
        return mEndY;
    }

    public void setmEndY(float mEndY) {
        this.mEndY = mEndY;
    }

    public abstract void onDraw(Canvas canvas);

    protected String getFieldString() {
        StringBuilder builder = new StringBuilder();
        builder.append(mPaint.getColor()).append(FIELD_SEPARATOR);
        builder.append(mPaint.getStrokeWidth()).append(FIELD_SEPARATOR);
        builder.append(mPaint.getStyle()).append(FIELD_SEPARATOR);
        builder.append(mStartX).append(VALUE_SEPARATOR).append(mStartY).append(FIELD_SEPARATOR);
        builder.append(mEndX).append(VALUE_SEPARATOR).append(mEndY);
        return builder.toString();
    }

    @Override
    public String toString() {
        return getFieldString();
    }
}
