package com.angcyo.paintdemo.paint;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by angcyo on 15-10-29-029.
 */
public class LineShape extends PaintShape {


    public LineShape(float mStartX, float mStartY, Paint mPaint) {
        super(mStartX, mStartY, mPaint);
    }

    public LineShape(Paint paint) {
        super(paint);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawLine(mStartX, mStartY, mEndX, mEndY, mPaint);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(PaintConfig.Shape.Line).append(FIELD_SEPARATOR);
        builder.append(getFieldString());
        return builder.toString();
    }
}
