package com.twilio.rooms;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings.Secure;

public final class PlatformInfo {
    private static final String PLATFORM_NAME = "Android";
    private static final Pattern IPV4_PATTERN =
            Pattern.compile(
                    "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

    private PlatformInfo(){}

    static String getPlatfomName() {
        return PLATFORM_NAME;
    }

    static String getPlatformVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    static String getHwDeviceManufacturer() {
        return android.os.Build.MANUFACTURER;
    }

    static String getHwDeviceModel() {
        return android.os.Build.MODEL;
    }

    static String getHwDeviceUUID(Context context) {
        return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
    }

    static String getHwDeviceIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en =
                 NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr =
                     intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() &&
                            isIPv4Address(inetAddress.getHostAddress())) {
                        String ipaddress = inetAddress .getHostAddress().toString();
                        return ipaddress;
                    }
                }
            }
        } catch (SocketException ex) {
            // TODO: Ignoring exception for now
        }
        return "";
    }

    static String getHwDeviceConnectionType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        String connType = "";
        if(info==null || !info.isConnected()) {
            connType = "No connection"; //not connected
        } else if(info.getType() == ConnectivityManager.TYPE_WIFI) {
            connType = info.getTypeName();
        } else if(info.getType() == ConnectivityManager.TYPE_MOBILE){
            connType = info.getSubtypeName();
        } else {
            connType = "Unable to detect connection type";
        }
        return connType;
    }

    static int getHwDeviceNumCores() {
        return Runtime.getRuntime().availableProcessors();
    }

    static double getTimeStamp() {
        return System.currentTimeMillis() / 1000L;
    }

    static String getRtcPlatformSdkVersion() {
        return TwilioConversationsClient.getVersion();
    }

    static String getOsArch() {
        return System.getProperty("os.arch");
    }

    private static boolean isIPv4Address(final String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }
}
