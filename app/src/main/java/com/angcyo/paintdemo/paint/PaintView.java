package com.angcyo.paintdemo.paint;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.angcyo.paintdemo.socket.ClientSocket;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;
import java.util.Vector;

/**
 * Created by angcyo on 15-10-29-029.
 */
public class PaintView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener, ClientSocket.OnDataChangeListener {
    public static final String TAG = "PaintView";
    public static final String BDC_CAPTURE = "bdc_capture";//
    public static final String CAPTURE_PATH = "file_path";
    public static final String CAPTURE_CODE = "capture_code";

    public static boolean isRunning = true;
    public static volatile boolean isChange = true;//先画一次
    public static volatile boolean isSendToServer = true;//是否是发送出去
    SurfaceHolder mSurfaceHolder;
    @ColorInt
    int mViewBGCol;
    float downX, downY;
    float moveX, moveY;
    Paint mFpsPaint;
    float fpsCount = 0;
    long startTime = 0;
    float fps;
    Vector<PaintShape> mShapes;//所有需要绘制的数据
    Vector<PaintShape> mBackShapes;//所有需要绘制的数据
    PaintShape mShape;
    volatile Rect mDirtyRect;
    Bitmap captureBmp;
    LocalBroadcastManager localBroadcastManager;
    private boolean isCapture = false;

    private ClientSocket mClientSocket;

    public PaintView(Context context) {
        this(context, null);
    }

    public PaintView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaintView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mViewBGCol = PaintConfig.getInstance().getSurfaceBackgroundColor();
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        setOnTouchListener(this);

        mShapes = new Vector<>();
//
        mFpsPaint = new Paint();
        mFpsPaint.setColor(Color.RED);
//        mFpsPaint.setStrokeWidth(20f);
        mFpsPaint.setTextSize(30f);
        mFpsPaint.setAntiAlias(true);
//        setBackgroundColor(mViewBGCol);

        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        Log.e(TAG, "surfaceCreated");
//        setBackgroundColor(mViewBGCol);
        isRunning = true;
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
//        mDirtyRect = new Rect((int) downX - 10, (int) downY - 10, (int) downX + 10, (int) downY + 10);
//        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        isChange = true;
        PaintConfig.getInstance().setSurfaceWidth(width);
        PaintConfig.getInstance().setSurfaceHeight(height);

        doDraw();
//        Log.e(TAG, "surfaceChanged");

//        Canvas canvas = mSurfaceHolder.lockCanvas();
//        canvas.drawLine(2.0f, 2.0f, 500.0f, 500f, mFpsPaint);
//        mSurfaceHolder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
//        Log.e(TAG, "surfaceDestroyed");
        isRunning = false;

        disconnectServer();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                mShape = generateShape(downX, downY);
                mShape.setmEndX(downX);
                mShape.setmEndY(downY);
                mShapes.add(mShape);
                mDirtyRect = new Rect((int) downX - 10, (int) downY - 10, (int) downX + 10, (int) downY + 10);
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = event.getX();
                moveY = event.getY();
                if (PaintConfig.getInstance().getCurrentShape() == PaintConfig.Shape.Point) {
                    PaintShape shape = generateShape(moveX, moveY);
                    shape.setmEndX(mShapes.lastElement().getmStartX());
                    shape.setmEndY(mShapes.lastElement().getmStartY());
                    mShapes.add(shape);
                } else {
                    mShape.setmEndX(moveX);
                    mShape.setmEndY(moveY);
                }
                int left = (int) Math.min(downX, moveX);
                int top = (int) Math.min(downY, moveY);
                int right = (int) Math.max(downX, moveX);
                int bottom = (int) Math.max(downY, moveY);
                mDirtyRect = new Rect(left - 10, top - 10, right + 10, bottom + 10);
                break;
            case MotionEvent.ACTION_UP:
                mDirtyRect = null;
                break;
        }
        isChange = true;
        isSendToServer = true;
        doDraw();
        return true;
    }

    private PaintShape generateShape(float downX, float downY) {
        PaintShape shape;
        Paint paint = new Paint();
        paint.setColor(PaintConfig.getInstance().getCurrentShapeColor());
        paint.setStrokeWidth(PaintConfig.getInstance().getCurrentShapeWidth());
        switch (PaintConfig.getInstance().getCurrentShape()) {
            case PaintConfig.Shape.Point:
                shape = new PointShape(downX, downY, paint);
                break;
            case PaintConfig.Shape.Line:
                shape = new LineShape(downX, downY, paint);
                break;
            case PaintConfig.Shape.Circle:
                paint.setStyle(Paint.Style.STROKE);
                shape = new CircleShape(downX, downY, paint);
                break;
            case PaintConfig.Shape.Rect:
                paint.setStyle(Paint.Style.STROKE);
                shape = new RectShape(downX, downY, paint);
                break;
            case PaintConfig.Shape.Image:
                shape = new ImageShape(downX, downY, paint);
                break;
            default:
                shape = new PointShape(downX, downY, paint);
                break;
        }
        return shape;
    }

    public void doDraw() {
        synchronized (this) {
            if (isChange) {
                //有变化重新绘制
                try {
                    mBackShapes = (Vector<PaintShape>) mShapes.clone();
                    if (isSendToServer) {
                        try {
                            ClientSocket.updateWriteData(mBackShapes);
                            isSendToServer = false;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                }
                Canvas canvas = null;
                try {
                    canvas = mSurfaceHolder.lockCanvas();
                    drawBg(canvas);
                    drawContent(canvas, mBackShapes);

                    try {
                        drawSvrContent(canvas, ClientSocket.getReadData());
                    } catch (Exception e) {
                    }
                } catch (Exception e) {
                } finally {
                    if (canvas != null) {
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
                isChange = false;
            }

            //截图
            if (isCapture) {
                try {
                    if (captureBmp != null) {
                        captureBmp.recycle();
                    }
                    captureBmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas bitCanvas = new Canvas(captureBmp);
                    drawBg(bitCanvas);
                    drawContent(bitCanvas, mBackShapes);
                    try {
                        drawSvrContent(bitCanvas, ClientSocket.getReadData());
                    } catch (Exception e) {
                    }
//                            drawFps(bitCanvas);
                    isCapture = false;
                    saveBitmap();
                } catch (Exception e) {

                }
            }
        }

    }

    private void disconnectServer() {
        if (mClientSocket != null) {
            mClientSocket.exit();
        }
    }

    public void connectServer() {
        if (mClientSocket == null) {
            mClientSocket = new ClientSocket();
            mClientSocket.setDataChangeListener(this);
        }
        new Thread(mClientSocket).start();
    }

    public void setClientSocket(ClientSocket clientSocket) {
        if (mClientSocket == null) {
            mClientSocket = clientSocket;
            mClientSocket.setDataChangeListener(this);
        }
    }

    private void saveBitmap() {
        Intent intent = new Intent(BDC_CAPTURE);
        Bundle bundle = new Bundle();
        try {
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + UUID.randomUUID() + ".jpg";
            File file = new File(filePath);
            FileOutputStream fos = new FileOutputStream(file);
            captureBmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            bundle.putInt(CAPTURE_CODE, 1);
            bundle.putString(CAPTURE_PATH, filePath);
        } catch (Exception e) {
            bundle.putInt(CAPTURE_CODE, 0);
        }
        intent.putExtras(bundle);
        localBroadcastManager.sendBroadcast(intent);
    }

    /**
     * 绘制本地图形
     */
    private void drawContent(Canvas canvas, Vector<PaintShape> shapes) {
        if (shapes == null) {
            return;
        }
        for (PaintShape shape : shapes) {
            shape.onDraw(canvas);
        }
    }

    /**
     * 绘制服务端发送过来的图形
     */
    private void drawSvrContent(Canvas canvas, Vector<PaintShape> shapes) {
        if (shapes == null) {
            return;
        }
        for (PaintShape shape : shapes) {
            shape.onDraw(canvas);
        }
    }

    private void drawBg(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//清屏
        canvas.drawColor(PaintConfig.getInstance().getSurfaceBackgroundColor());//绘制背景
    }

    private void drawFps(Canvas canvas) {
        fpsCount++;
        long nowTime = System.currentTimeMillis();
        if (nowTime - startTime > 1000) {
            fps = fpsCount / (nowTime - startTime) * 1000;
            startTime = nowTime;
            fpsCount = 0;
        }
        canvas.drawText(String.format("FPS:%.3f", fps), PaintConfig.getInstance().getSurfaceWidth() - 200, 40, mFpsPaint);
    }

    public void removeLastOne() {
        if (mShapes != null && mShapes.size() > 0) {
            mShapes.remove(mShapes.size() - 1);
            isChange = true;
            isSendToServer = true;
            doDraw();
        }
    }

    public void removeAll() {
        if (mShapes != null && mShapes.size() > 0) {
            mShapes.clear();
            isChange = true;
            isSendToServer = true;
            doDraw();
        }
    }

    /**
     * 截屏
     */
    public void capture() {
        isCapture = true;
        doDraw();
    }

    @Override
    public void onDataChange() {
        isChange = true;
        isSendToServer = false;
        doDraw();
    }
}
