package com.twilio.video.internal;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public final class Logger {
    public static final int INHERIT = Log.ASSERT + 1;

    private static final Map<Class<?>, Logger> loggers = new HashMap<Class<?>, Logger>();

    private static int globalLevel = Log.ERROR;

    public static Logger getLogger(Class<?> cls) {
        if (!loggers.containsKey(cls)) {
            synchronized (loggers) {
                if (!loggers.containsKey(cls)) {
                    loggers.put(cls, new Logger(cls.getSimpleName()));
                }
            }
        }
        return loggers.get(cls);
    }

    public static void setLogLevel(int level) {
        Logger.globalLevel = level;
    }

    public static int getLogLevel() {
        return Logger.globalLevel;
    }

    private final String name;
    private int level = INHERIT;

    private Logger(final String name) {
        this.name = name;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isVerboseEnabled() {
        return level <= Log.VERBOSE || (level == INHERIT && globalLevel <= Log.VERBOSE);
    }

    public boolean isDebugEnabled() {
        return level <= Log.DEBUG || (level == INHERIT && globalLevel <= Log.DEBUG);
    }

    public boolean isInfoEnabled() {
        return level <= Log.INFO || (level == INHERIT && globalLevel <= Log.INFO);
    }

    public boolean isWarnEnabled() {
        return level <= Log.WARN || (level == INHERIT && globalLevel <= Log.WARN);
    }

    public boolean isErrorEnabled() {
        return level <= Log.ERROR || (level == INHERIT && globalLevel >= Log.ERROR);
    }

    public void v(final String msg, final Throwable t) {
        if (isVerboseEnabled()) {
            Log.v(this.name, msg, t);
        }
    }

    public void v(final String msg) {
        if (isVerboseEnabled()) {
            Log.v(this.name, msg);
        }
    }

    public void d(final String msg, final Throwable t) {
        if (isDebugEnabled()) {
            Log.d(this.name, msg, t);
        }
    }

    public void d(final String msg) {
        if (isDebugEnabled()) {
            Log.d(this.name, msg);
        }
    }

    public void i(final String msg, final Throwable t) {
        if (isInfoEnabled()) {
            Log.i(this.name, msg, t);
        }
    }

    public void i(final String msg) {
        if (isInfoEnabled()) {
            Log.i(this.name, msg);
        }
    }

    public void w(final String msg, final Throwable t) {
        if (isWarnEnabled()) {
            Log.w(this.name, msg, t);
        }
    }

    public void w(final String msg) {
        if (isWarnEnabled()) {
            Log.w(this.name, msg);
        }
    }

    public void e(final String msg, final Throwable t) {
        if (isErrorEnabled()) {
            Log.e(this.name, msg, t);
        }
    }

    public void e(final String msg) {
        if (isErrorEnabled()) {
            Log.e(this.name, msg);
        }
    }
}
