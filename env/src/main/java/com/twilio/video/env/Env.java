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
