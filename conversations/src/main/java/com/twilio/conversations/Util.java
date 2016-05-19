package com.twilio.conversations;

import android.os.Handler;
import android.os.Looper;

/*
 * Junk drawer of utility methods needed throughout SDK.
 */
final class Util {
    /*
     * Return a handler on the thread looper, or the main thread looper if the calling thread
     * does not have a looper. If neither are available this handler will return null.
     */
    public static Handler createCallbackHandler() {
        Handler handler = null;
        Looper looper;

        if((looper = Looper.myLooper()) != null) {
            handler = new Handler(looper);
        } else if((looper = Looper.getMainLooper()) != null) {
            handler = new Handler(looper);
        }
        return handler;
    }
}
