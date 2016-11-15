package com.twilio.video.app.util;

public class Env {
    static {
        System.loadLibrary("env-jni");
    }

    public static void setEnv(String name, String value, boolean overwrite) {
        nativeSetEnv(name, value, overwrite);
    }

    public static String getEnv(String name) {
        return nativeGetEnv(name);
    }

    static native void nativeSetEnv(String name, String value, boolean overwrite);

    static native String nativeGetEnv(String name);

}
