package com.angcyo.paintdemo.paint;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by angcyo on 15-10-29-029.
 */
public class ImageShape extends PaintShape {


    public ImageShape(float mStartX, float mStartY, Paint mPaint) {
        super(mStartX, mStartY, mPaint);
    }

    public ImageShape(Paint paint) {
        super(paint);
    }

    @Override
    public void onDraw(Canvas canvas) {

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(PaintConfig.Shape.Image).append(FIELD_SEPARATOR);
        builder.append(getFieldString());
        return builder.toString();
    }
}
