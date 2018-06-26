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

final class PlatformInfo {
  private static final String PLATFORM_NAME = "Android";
  private static long nativeHandle = 0;

  private PlatformInfo() {}

  static synchronized long getNativeHandle() {
    if (nativeHandle == 0) {
      nativeHandle =
          nativeCreate(
              PLATFORM_NAME,
              android.os.Build.VERSION.RELEASE,
              android.os.Build.MANUFACTURER,
              android.os.Build.MODEL,
              Video.getVersion(),
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

  private static native long nativeCreate(
      String platformName,
      String platformVersion,
      String hwDeviceManufacturer,
      String hwDeviceModel,
      String sdkVersion,
      String hwDeviceArch);

  private static native void nativeRelease(long nativeHandle);
}
