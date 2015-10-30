package com.angcyo.paintdemo.socket;

/**
 * Created by angcyo on 15-10-30-030.
 */
public class SocketConfig {
    /**
     * 服务端ip
     */
    public static String SVR_IP = "192.168.1.100";
    /**
     * 服务端端口
     */
    public static int SVR_PORT = 8711;

    public static int READ_TIME_OUT = 3000;
    public static int CONNECT_TIME_OUT = 3000;

    public static String FLAG_SOCKET_READ_END = "!end!";//数据分隔标示

    public static boolean isServer = false;//当前app属于哪种状态
//    public static boolean isStartClient = false;//客户端启动连接

    public static String BDC_CONNECT_SERVER = "connect_server";//连上服务器,发送的广播
    public static String BDC_CONNECT_CLIENT = "connect_client";//客户端连上,发送的广播
    public static String KEY_SERVER_IP = "server_ip";
    public static String KEY_CLIENT_IP = "client_ip";
    public static String CLIENT_IP = "192.168.1.100";
}
