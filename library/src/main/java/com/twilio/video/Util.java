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

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

/*
 * Junk drawer of utility methods needed throughout SDK.
 */
final class Util {
    /*
     * Return a handler on the thread looper, or the main thread looper if the calling thread
     * does not have a looper. If neither are available this handler will return null.
     */
    static Handler createCallbackHandler() {
        Handler handler = null;
        Looper looper;

        if((looper = Looper.myLooper()) != null) {
            handler = new Handler(looper);
        } else if((looper = Looper.getMainLooper()) != null) {
            handler = new Handler(looper);
        }
        if(handler == null) {
            throw new IllegalThreadStateException("This thread must be able to obtain a Looper");
        }

        return handler;
    }

    static boolean permissionGranted(Context context, String permission) {
        int permissionCheck = context.checkCallingOrSelfPermission(permission);

        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }
}
