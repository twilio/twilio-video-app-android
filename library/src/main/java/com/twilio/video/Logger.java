/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;

class Logger {
    public static final int INHERIT = Log.ASSERT + 1;

    private static final Map<Class<?>, Logger> loggers = new HashMap<>();

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
        return level <= Log.ERROR || (level == INHERIT && globalLevel <= Log.ERROR);
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
