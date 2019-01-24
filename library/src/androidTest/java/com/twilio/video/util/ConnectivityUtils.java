package com.twilio.video.util;

import static org.junit.Assert.fail;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ConnectivityUtils {
    private static final int HTTP_TIMEOUT = 5;
    private static final int MAX_NETWORK_RETRIES = 5;

    public static void enableWifi(Context context, boolean enable) {
        final CountDownLatch requestLatch = new CountDownLatch(1);
        new Thread(
                        () -> {
                            WifiManager wifiManager =
                                    (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                            ConnectivityManager connectivityManager =
                                    (ConnectivityManager)
                                            context.getSystemService(Context.CONNECTIVITY_SERVICE);
                            if (!wifiManager.setWifiEnabled(enable)) {
                                fail("Unable to change wifi state");
                            }
                            if (enable) {
                                // Wait for the wifi to connect
                                while (true) {
                                    NetworkInfo networkInfo =
                                            connectivityManager.getActiveNetworkInfo();
                                    if (networkInfo != null
                                            && networkInfo.getType()
                                                    == ConnectivityManager.TYPE_WIFI
                                            && networkInfo.isConnected()) {
                                        break;
                                    }
                                }

                                /*
                                 * While the Wifi may be established data connectivity may still not be available.
                                 * Attempt an HTTP request up to the specified timeout.
                                 */
                                InputStream is = null;
                                for (int i = 0; i < MAX_NETWORK_RETRIES; i++) {
                                    try {
                                        is = new URL("https://google.com").openStream();

                                        int ptr = 0;
                                        StringBuilder buffer = new StringBuilder();

                                        while ((ptr = is.read()) != -1) {
                                            buffer.append((char) ptr);
                                        }
                                        if (buffer.length() > 0) {
                                            requestLatch.countDown();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } finally {
                                        if (is != null) {
                                            try {
                                                is.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            }
                        })
                .run();

        try {
            requestLatch.await(HTTP_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail(e.getMessage());
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
