package com.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MyClass {
    public static void main(String... args) {
        String string = "1|-15295269|5.5|FILL|1179.3857:292.67807|@1179.3857:292.67807";
        String sep = "|";

        String[] ss = string.split(sep);

        string += "|";
        string += "\\|";

        String[] ss1 = string.split("|");
        String[] ss2 = string.split("\\\\" + sep);

        System.out.println();

        testSocket();
    }

    private static void testSocket() {
        ServerSocket serverSocket;
        Socket socket;

        while (true) {
            try {
                serverSocket = new ServerSocket(8776);
                socket = serverSocket.accept();
//                socket = new Socket();
//                socket.connect(new InetSocketAddress("192.168.1.101", 8777));
                String data = new String("angcyo");
                while (true) {
                    socket.getOutputStream().write(data.getBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
