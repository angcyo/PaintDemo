package com.angcyo.paintdemo.paint;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by angcyo on 15-10-29-029.
 */
public class PointShape extends PaintShape {


    public PointShape(float mStartX, float mStartY, Paint mPaint) {
        super(mStartX, mStartY, mPaint);
    }

    public PointShape(Paint paint) {
        super(paint);
    }

    @Override
    public void onDraw(Canvas canvas) {
        Paint paint = new Paint(mPaint);
        paint.setStrokeWidth(mPaint.getStrokeWidth());
        canvas.drawLine(mStartX, mStartY, mEndX, mEndY, paint);
        canvas.drawCircle(mStartX, mStartY, mPaint.getStrokeWidth() / 2, mPaint);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(PaintConfig.Shape.Point).append(FIELD_SEPARATOR);
        builder.append(getFieldString());
        return builder.toString();
    }
}
