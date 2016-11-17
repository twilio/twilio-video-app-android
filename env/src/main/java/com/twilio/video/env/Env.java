package com.twilio.video.env;

import android.content.Context;

import com.getkeepsafe.relinker.ReLinker;

public class Env {

    public static void set(Context context, String name, String value, boolean overwrite) {
        ReLinker.loadLibrary(context, "env-jni");
        nativeSet(name, value, overwrite);
    }

    public static String get(Context context, String name) {
        ReLinker.loadLibrary(context, "env-jni");
        return nativeGet(name);
    }

    static native void nativeSet(String name, String value, boolean overwrite);

    static native String nativeGet(String name);

}
