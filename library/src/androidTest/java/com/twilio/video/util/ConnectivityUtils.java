package com.twilio.video.util;

import static org.junit.Assert.fail;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import java.lang.reflect.Method;

public class ConnectivityUtils {
    private static final int HTTP_TIMEOUT = 20;

    public static void enableWifi(Context context, boolean enable) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!wifiManager.setWifiEnabled(enable)) {
            fail("Unable to change wifi state");
        }
        if (enable) {
            // Wait for the wifi to connect
            while (true) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null
                        && networkInfo.getType() == ConnectivityManager.TYPE_WIFI
                        && networkInfo.isConnected()) {
                    break;
                }
                // Removed request to Google, relies on okhttp. Do we want to add okhttp to the
                // library?
            }
        }
    }

    public static boolean isMobileDataEnabled(Context context) {
        boolean enabled = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            enabled = Settings.Global.getInt(context.getContentResolver(), "mobile_data", 0) == 1;
        } else {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            try {
                Class cmClass = Class.forName(cm.getClass().getName());
                Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
                method.setAccessible(true); // Make the method callable
                // get the setting for "mobile data"
                enabled = (Boolean) method.invoke(cm);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
        return enabled;
    }
}
