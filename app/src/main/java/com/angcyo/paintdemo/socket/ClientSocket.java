package com.angcyo.paintdemo.socket;

import android.content.Intent;
import android.os.Bundle;

import com.angcyo.paintdemo.MainActivity;
import com.angcyo.paintdemo.paint.PaintShape;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Vector;

/**
 * Created by angcyo on 15-10-30-030.
 */
public class ClientSocket implements Runnable {
    private boolean isRunning = true;
    private boolean isReadWrite = true;
    private Socket mSocket;
    private ReadRunnable readRunnable;
    private WriteRunnable writeRunnable;
    private Vector<PaintShape> writeData;
    private Vector<PaintShape> readData;

    public ClientSocket() {
    }

    public ClientSocket(Socket mSocket) throws IOException {
        this.mSocket = mSocket;
        startReadWriteThread();
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
        readRunnable = new ReadRunnable(mSocket.getInputStream());
        writeRunnable = new WriteRunnable(mSocket.getOutputStream());
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

    public void updateWriteData(Vector newData) {
        synchronized (writeData) {
            writeData = (Vector<PaintShape>) newData.clone();
        }
    }

    private void updateReadData(Vector newData) {
        synchronized (readData) {
            readData = newData;
        }
    }

    public Vector<PaintShape> getReadData() {
        return readData;
    }

    /**
     * 数据读线程
     */
    private class ReadRunnable implements Runnable {

        BufferedReader bufferedReader;

        public ReadRunnable(InputStream inputStream) {
            this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            isReadWrite = true;
        }

        @Override
        public void run() {
            Vector<PaintShape> shapes = null;
            while (isReadWrite) {
                String line;
                try {
                    line = bufferedReader.readLine();
                    while (line != null) {
                        if (line.equalsIgnoreCase(SocketConfig.FLAG_SOCKET_READ_END)) {
                            if (shapes != null) {
                                updateReadData(shapes);
                            }
                            break;
                        }
                        try {
                            PaintShape shape = PaintShape.generateShape(line);
                            if (shapes == null) {
                                shapes = new Vector<>();
                            }
                            shapes.add(shape);
                        } catch (Exception e) {

                        }
                        line = bufferedReader.readLine();
                    }
                } catch (IOException e) {
                    if (shapes != null) {
                        shapes.clear();
                        shapes = null;
                    }
                }
            }
        }
    }

    /**
     * 数据写线程
     */
    private class WriteRunnable implements Runnable {
        BufferedWriter bufferedWriter;

        public WriteRunnable(OutputStream outputStream) {
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            isReadWrite = true;
        }

        @Override
        public void run() {
            while (isReadWrite) {
                try {
                    Vector<PaintShape> datas = (Vector<PaintShape>) writeData.clone();
                    for (PaintShape shape : datas) {
                        try {
                            bufferedWriter.write(shape.toString());
                            bufferedWriter.newLine();
                            bufferedWriter.flush();
                        } catch (IOException e) {
                            disconnect();
                        }
                    }
                    bufferedWriter.write(SocketConfig.FLAG_SOCKET_READ_END);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                } catch (Exception e) {
                }

            }
        }
    }
}
