package com.angcyo.paintdemo.socket;

import android.content.Intent;
import android.os.Bundle;

import com.angcyo.paintdemo.MainActivity;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by angcyo on 15-10-30-030.
 */
public class ServerSocket implements Runnable {

    java.net.ServerSocket mServerSocket;
    Socket mClient;
    ClientSocket mClientSocket;

    private boolean isRunning = true;

    public ServerSocket() {
    }

    public void exit() {
        isRunning = false;
    }

    @Override
    public void run() {
        while (isRunning) {
            if (mClient == null) {
                try {
                    mClient = accept();//监听连接
                    SocketConfig.CLIENT_IP = mClient.getInetAddress().getHostAddress();
                    sendBroadcast();
                } catch (IOException e) {
                    mClient = null;
                    mServerSocket = null;
                }
            }
            if (mClientSocket == null && mClient != null) {
                try {
                    mClientSocket = new ClientSocket(mClient);
                } catch (IOException e) {
                    try {
                        mClient.close();
                    } catch (IOException e1) {
                    }
                    mClient = null;
                    mClientSocket = null;
                }
            }
        }
        disconnect();
    }

    private void sendBroadcast() {
        Intent intent = new Intent(SocketConfig.BDC_CONNECT_SERVER);
        Bundle bundle = new Bundle();
        bundle.putString(SocketConfig.KEY_SERVER_IP, SocketConfig.SVR_IP);
        intent.putExtras(bundle);
        MainActivity.localBroadcastManager.sendBroadcast(intent);
    }


    private void disconnect() {
        mClientSocket.exit();
        try {
            mClient.close();
        } catch (IOException e) {
        }
        try {
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mClientSocket = null;
        mClient = null;
        mServerSocket = null;
    }

    private Socket accept() throws IOException {
        if (mServerSocket == null) {
            mServerSocket = new java.net.ServerSocket(SocketConfig.SVR_PORT);
        }
        return mServerSocket.accept();
    }
}
