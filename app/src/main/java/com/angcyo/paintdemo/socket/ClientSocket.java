package com.angcyo.paintdemo.socket;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.angcyo.paintdemo.MainActivity;
import com.angcyo.paintdemo.paint.PaintShape;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Vector;

/**
 * Created by angcyo on 15-10-30-030.
 */
public class ClientSocket implements Runnable {
    private static Vector<PaintShape> writeData;
    private static Vector<PaintShape> readData;
    private static Object lock = new Object();
    InputStream inputStream;
    OutputStream outputStream;
    private boolean isRunning = true;
    private boolean isReadWrite = true;
    private Socket mSocket;
    private ReadRunnable readRunnable;
    private WriteRunnable writeRunnable;

    public ClientSocket() {
    }

    public ClientSocket(Socket mSocket) throws IOException {
        this.mSocket = mSocket;
        startReadWriteThread();
    }

    public static void updateWriteData(Vector newData) {
        writeData = (Vector<PaintShape>) newData.clone();
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    private static void updateReadData(Vector newData) {
        readData = newData;
    }

    public static Vector<PaintShape> getReadData() {
        return readData;
    }

    @Override
    public void run() {
        while (isRunning) {
            //连接服务器
            if (mSocket == null) {
                try {
                    connect();
                } catch (IOException e) {
                    mSocket = null;
                    try {
                        Thread.sleep(100);
                        continue;
                    } catch (InterruptedException e1) {
                    }
                }
            }
            Thread.yield();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
        try {
            disconnect();
        } catch (IOException e) {
        }
    }

    public void exit() {
        isRunning = false;
    }

    /**
     * 连接服务器
     */
    private void connect() throws IOException {
        if (mSocket == null) {
            mSocket = new Socket();
            mSocket.setSoTimeout(SocketConfig.READ_TIME_OUT);
        }
        mSocket.connect(new InetSocketAddress(SocketConfig.SVR_IP, SocketConfig.SVR_PORT), SocketConfig.CONNECT_TIME_OUT);
        startReadWriteThread();
        sendBroadcast();
    }

    private void sendBroadcast() {
        Intent intent = new Intent(SocketConfig.BDC_CONNECT_SERVER);
        Bundle bundle = new Bundle();
        bundle.putString(SocketConfig.KEY_SERVER_IP, SocketConfig.SVR_IP);
        intent.putExtras(bundle);
        MainActivity.localBroadcastManager.sendBroadcast(intent);
    }

    private void startReadWriteThread() throws IOException {
        inputStream = mSocket.getInputStream();
        outputStream = mSocket.getOutputStream();

        readRunnable = new ReadRunnable();
        writeRunnable = new WriteRunnable();

        new Thread(readRunnable).start();
        new Thread(writeRunnable).start();
    }

    /**
     * 断开服务器
     */
    private void disconnect() throws IOException {
        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
            isReadWrite = false;
        }
    }

    /**
     * 处理收到的数据
     */
    private void handleData(String data) {
        if (TextUtils.isEmpty(data)) {
            return;
        }
        String[] shapes = data.split(PaintShape.SHAPE_SEPARATOR);
        Vector<PaintShape> paintShapes = new Vector<>();
        for (String shapeStr : shapes) {
            try {
                shapeStr = shapeStr.replaceAll(SocketConfig.FLAG_SOCKET_READ_END, "");
                PaintShape paintShape = PaintShape.generateShape(shapeStr);
                paintShapes.add(paintShape);
            } catch (Exception e) {

            }
        }
        updateReadData(paintShapes);
    }

    /**
     * 数据读线程
     */
    private class ReadRunnable implements Runnable {
        @Override
        public void run() {//读线程
            int len;
            byte[] bytes = new byte[1024];
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while (isReadWrite) {
                try {
                    len = inputStream.read(bytes);
                    line = new String(bytes, 0, len);
                    stringBuilder.append(line);
                    if (line.endsWith(SocketConfig.FLAG_SOCKET_READ_END)) {
                        StringBuffer buffer = new StringBuffer(stringBuilder.toString());
                        Log.e("收到数据: " + buffer.length(), buffer.toString());
                        handleData(buffer.toString());
                        stringBuilder = new StringBuilder();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 数据写线程
     */
    private class WriteRunnable implements Runnable {

        @Override
        public void run() {//写数据
            while (isReadWrite) {
                if (writeData == null || writeData.size() < 1) {
                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {

                        }
                    }
                }
//                Vector<PaintShape> datas = (Vector<PaintShape>) writeData.clone();
//                StringBuilder stringBuilder = new StringBuilder();
//                while (writeData.size() > 0) {
//                    PaintShape shape = writeData.remove(0);
//                    stringBuilder.append(shape.toString());
//                    stringBuilder.append(PaintShape.SHAPE_SEPARATOR);
//                    outputStream.write(shape.toString().getBytes());
//                }
//                stringBuilder.append(SocketConfig.FLAG_SOCKET_READ_END);
                try {
//                    outputStream.write(stringBuilder.toString().getBytes());
                    while (writeData.size() > 0) {
                        PaintShape shape = writeData.remove(0);
                        outputStream.write(shape.toString().getBytes());
                        outputStream.write(PaintShape.SHAPE_SEPARATOR.getBytes());
                    }
                    outputStream.write(SocketConfig.FLAG_SOCKET_READ_END.getBytes());
                    outputStream.flush();
                    writeData.clear();
                    writeData = null;
                } catch (Exception e) {
                    try {
                        disconnect();
                    } catch (IOException e1) {
                    }
                }

            }
        }
    }
}
