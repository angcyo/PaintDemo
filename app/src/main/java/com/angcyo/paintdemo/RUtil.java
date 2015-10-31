package com.angcyo.paintdemo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;

/**
 * Created by angcyo on 15-09-08-008.
 */
public class RUtil {
    static String config = getSDPath() + File.separator + "config.ini";

    /**
     * 去除字符串左右的字符
     */
    public static String trimMarks(String des) {
        return trimMarks(des, 1);
    }

    /**
     * 去除字符串左右指定个数的字符
     */
    public static String trimMarks(String des, int count) {
        if (des == null || count < 0 || des.length() < count + 1) {
            return des;
        }
        return des.substring(count, des.length() - count);
    }

    /**
     * 返回现在的时间,不包含日期
     */
    public static String getNowTime() {
        return getNowTime("HH:mm:ss");
    }

    public static String getNowTime(String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(new Date());
    }

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return (str == null || str.trim().length() < 1);
    }

    /**
     * 判断网络是否可以用
     *
     * @param context the con
     * @return the boolean
     */
    public static boolean isNetOk(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo == null) {
            return false;
        }
        if (netInfo.isConnected()) {
            return true;
        }
        return false;
    }

// 可以使用Time类，更简单

    /**
     * 获取网络的名字
     *
     * @param context the context
     * @return the net type name
     */
    public static String getNetTypeName(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager
                    .getActiveNetworkInfo();
            if (activeNetworkInfo == null) {
                return "无网络";
            }
            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return activeNetworkInfo.getTypeName();
            } else {
                String typeName = activeNetworkInfo.getSubtypeName();
//                MyLog.i("网络名称:", typeName);
                if (typeName == null || typeName.length() == 0) {
                    return "未知网络";
                } else if (typeName.length() > 3) {
                    return activeNetworkInfo.getSubtypeName().substring(0, 4);
                } else {
                    return activeNetworkInfo.getSubtypeName().substring(0,
                            typeName.length());
                }
            }
        } else {
            return "无网络";
        }
    }

    /**
     * @return 按照HH:mm:ss 返回时间
     */
    public static String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

    /**
     * @return 按照yyyy-MM-dd 格式返回日期
     */
    public static String getDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(new Date());
    }

    /**
     * @return 按照yyyy-MM-dd HH:mm:ss 的格式返回日期和时间
     */
    public static String getDateAndTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }

    /**
     * @param pattern 格式
     * @return 返回日期
     */
    public static String getDate(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(new Date());
    }

    /**
     * @param pattern 格式
     * @return 按照指定格式返回时间
     */
    public static String getTime(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(new Date());
    }

    /**
     * @param pattern 指定的格式
     * @return 按照指定格式返回日期和时间
     */
    public static String getDateAndTime(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(new Date());
    }

//

    /**
     * 需要权限<uses-permission android:name="android.permission.READ_PHONE_STATE" >
     *
     * @param context 上下文
     * @return 返回手机号码
     */
    public static String getTelNumber(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getLine1Number();
    }

    /**
     * 取得device的IP address
     * <p/>
     * 需要权限 android.permission.ACCESS_WIFI_STATE
     *
     * @param context
     * @return 返回ip
     */
    public static String getWifiIp(Context context) {
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();

// 格式化IP address，例如：格式化前：1828825280，格式化后：192.168.1.109
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));
        return ip;
    }

    /**
     * 获取网络ip
     */
    public static String getIp(Context context) {
        if (isWifiConnected(context)) {
            return getWifiIp(context);
        } else {
            return getMobileIP();
        }
    }

    /**
     * Get IP For mobile
     */
    public static String getMobileIP() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ipaddress = inetAddress.getHostAddress().toString();
                        return ipaddress;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("getMobileIP", "Exception in Get IP Address: " + ex.toString());
        }
        return "";
    }

    /**
     * @return 获取device的os version
     */
    public static String getOsVersion() {
        String string = Build.VERSION.RELEASE;
        return string;
    }

    /**
     * @return 返回设备型号
     */
    public static String getDeviceName() {
        String string = Build.MODEL;
        return string;
    }

    /**
     * INTERNAL method that returns the device model name with correct capitalization.
     * Taken from: http://stackoverflow.com/a/12707479/1254846
     *
     * @return The device model name (i.e., "LGE Nexus 5")
     */
    public static String getDeviceModelName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    /**
     * INTERNAL method that capitalizes the first character of a string
     *
     * @param s The string to capitalize
     * @return The capitalized string
     */
    public static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    /**
     * @return 返回设备sdk版本
     */
    public static String getOsSdk() {
        int sdk = Build.VERSION.SDK_INT;
        return String.valueOf(sdk);
    }

    public static int getRandom() {
        Random random = new Random();
        return random.nextInt();
    }

    /**
     * 获取随机数
     *
     * @param n 最大范围
     * @return
     */
    public static int getRandom(int n) {
        Random random = new Random();
        return random.nextInt(n);
    }

    /**
     * 获取字符数组中随机的字符串
     *
     * @param context
     * @param resId
     * @return
     */
    public static String getRandomString(Context context, int resId) {
        String[] strings;
        strings = context.getResources().getStringArray(resId);

        return strings[getRandom(strings.length)];
    }

    /**
     * 从资源id获取字符串
     *
     * @param context 上下文
     * @param id      资源id
     * @return 字符串
     */
    public static String getStringForRes(Context context, int id) {
        if (context == null) {
            return "";
        }
        return context.getResources().getString(id);
    }

    /**
     * 返回app的版本名称.
     *
     * @param context
     * @return
     */
    public static String getAppVersionName(Context context) {
        String version = "unknown";
// 获取package manager的实例
        PackageManager packageManager = context.getPackageManager();
// getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(),
                    0);
            version = packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
// Log.i("版本名称:", version);
        return version;
    }

    /**
     * 返回app的版本代码.
     *
     * @param context
     * @return
     */
    public static int getAppVersionCode(Context context) {
// 获取package manager的实例
        PackageManager packageManager = context.getPackageManager();
// getPackageName()是你当前类的包名，0代表是获取版本信息
        int code = 1;
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(),
                    0);
            code = packInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
// Log.i("版本代码:", version);
        return code;
    }

    /**
     * 判断网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetworkOK(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = cm.getActiveNetworkInfo();

        if (info != null && info.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    /**
     * 判断wifi是否可用
     *
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI); // 返回WIFI
            // 的连接info
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 获取屏幕的宽度高度
     *
     * @param context
     * @param size
     */
    public static DisplayMetrics getDisplay(Context context, Point size) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getSize(size);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics;
    }

    /**
     * 格式化字节
     *
     * @param size
     * @return
     * @date 2014年12月3日
     */
    public static String formatSize(long size) {
        if (size / (1024 * 1024) > 0) {
            float tmpSize = (float) (size) / (float) (1024 * 1024);
            DecimalFormat df = new DecimalFormat("#.##");
            return "" + df.format(tmpSize) + "MB";
        } else if (size / 1024 > 0) {
            return "" + (size / (1024)) + "KB";
        } else
            return "" + size + "B";
    }

    public static String formatSize(String size) {

        if (size.substring(size.length() - 1).equalsIgnoreCase("B")
                || size.endsWith("-")) {
            return size;
        } else {
            return formatSize(Long.valueOf(size));
        }

    }

    /**
     * 是否分割kb单位
     *
     * @param size
     * @param isNewLine
     * @return
     * @date 2014年12月3日
     */
    public static String formatSize(long size, boolean isNewLine) {
        if (isNewLine == false) {
            return formatSize(size);
        }
        if (size / (1024 * 1024) > 0) {
            float tmpSize = (float) (size) / (float) (1024 * 1024);
            DecimalFormat df = new DecimalFormat("#.##");
            return "" + df.format(tmpSize) + "\nMB";
        } else if (size / 1024 > 0) {
            return "" + (size / (1024)) + "\nKB";
        } else
            return "" + size + "\nB";
    }

    public static String callMethodAndLine() {
        StringBuilder result = new StringBuilder();
        StackTraceElement thisMethodStack = (new Exception()).getStackTrace()[1];
        result.append(thisMethodStack.getClassName() + ".");
        result.append(thisMethodStack.getMethodName());
        result.append("(" + thisMethodStack.getFileName());
        result.append(":" + thisMethodStack.getLineNumber() + ")");
        return result.toString();
    }

    public static String callClassName() {
        StringBuilder result = new StringBuilder();
        StackTraceElement thisMethodStack = (new Exception()).getStackTrace()[1];
        result.append(thisMethodStack.getClassName());
        return result.toString();
    }

    public static String callMethodName() {
        StringBuilder result = new StringBuilder();
        StackTraceElement thisMethodStack = (new Exception()).getStackTrace()[1];
        result.append(thisMethodStack.getMethodName());
        return result.toString();
    }

    public static String callClassMethodName() {
        StringBuilder result = new StringBuilder();
        StackTraceElement thisMethodStack = (new Exception()).getStackTrace()[1];
        result.append(thisMethodStack.getClassName() + ".");
        result.append(thisMethodStack.getMethodName());
        return result.toString();
    }

    /**
     * 返回SD卡路径,如果没有返回默认的下载缓存路径
     *
     * @return the sD path
     */
    public static String getSDPath() {
        return isExternalStorageWritable() ? Environment
                .getExternalStorageDirectory().getPath() : Environment
                .getDownloadCacheDirectory().getPath();
    }

    /**
     * 判断是否有SD卡
     *
     * @return the boolean
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * 从配置文件中,获取值
     *
     * @param key    the key
     * @param config the config
     * @return the string
     */
    public static String get(String key, String config) {
        FileInputStream s;
        String value = "";
        try {
            s = new FileInputStream(config);
            Properties properties = new Properties();
            properties.load(s);
            value = properties.getProperty(key, "");
            s.close();
        } catch (Exception e) {
        }
        return value;
    }

    /**
     * 保存到配置文件 格式: key!value
     *
     * @param content the content
     * @param config  the config
     */
    public static void set(String content, String config) {
        Properties prop = new Properties();
        String[] values;
        InputStream fis = null;
        OutputStream fos = null;
        try {
            fis = new FileInputStream(config);
            fos = new FileOutputStream(config);
            prop.load(fis);
            values = content.split("!");
            String value = values.length == 1 ? "" : values[1].trim();
            prop.setProperty(values[0].trim(), value);
            prop.storeToXML(fos, " by angcyo", "utf-8");
        } catch (Exception e) {
        } finally {
            try {
                fis.close();
                fos.close();
            } catch (Exception e2) {
            }
        }
    }

    public static String get(String key) {
        return get(key, config);
    }

    public static void set(String content) {
        set(content, config);
    }

    /**
     * 打开网页
     *
     * @param context the context
     * @param url     the url
     */
    public static void openUrl(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        if (!url.toLowerCase().startsWith("http:") || !url.toLowerCase().startsWith("https:")) {
            url = "http:".concat(url);
        }

        Uri webPage = Uri.parse(url);
        Intent webIntent = new Intent(Intent.ACTION_VIEW, webPage);
        context.startActivity(webIntent);
    }

    /**
     * 检查是否有网络
     */
    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            return info.isAvailable();
        }
        return false;
    }

    /**
     * 检查是否是WIFI
     */
    public static boolean isWifi(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI)
                return true;
        }
        return false;
    }

    /**
     * 检查是否是移动网络
     */
    public static boolean isMobile(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE)
                return true;
        }
        return false;
    }

    private static NetworkInfo getNetworkInfo(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }
}
