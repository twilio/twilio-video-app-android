package com.twilio.video;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings.Secure;

final class PlatformInfo {
    private static final String PLATFORM_NAME = "Android";
    private static long nativeHandle = 0;

    private PlatformInfo(){}

    static synchronized long getNativeHandle() {
        if (nativeHandle == 0) {
            nativeHandle = nativeCreate(
                PLATFORM_NAME,
                android.os.Build.VERSION.RELEASE,
                android.os.Build.MANUFACTURER,
                android.os.Build.MODEL,
                VideoClient.getVersion(),
                System.getProperty("os.arch"));
        }
        return nativeHandle;
    }

    static synchronized void release() {
        if (nativeHandle != 0) {
            nativeRelease(nativeHandle);
            nativeHandle = 0;
        }
    }

    private static native long nativeCreate(String platformName,
                                            String platformVersion,
                                            String hwDeviceManufacturer,
                                            String hwDeviceModel,
                                            String sdkVersion,
                                            String hwDeviceArch);

    private static native void nativeRelease(long nativeHandle);
}
