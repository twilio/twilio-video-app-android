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

#include "com_twilio_video_PlatformInfo.h"
#include "twilio/video/video.h"
#include "webrtc/sdk/android/src/jni/jni_helpers.h"
#include "logging.h"
#include "jni_utils.h"

namespace twilio_video_jni {

JNIEXPORT jlong JNICALL Java_com_twilio_video_PlatformInfo_nativeCreate(
        JNIEnv *env,
        jobject j_instance,
        jstring j_platform_name,
        jstring j_platform_version,
        jstring j_hw_device_manufacturer,
        jstring j_hw_device_model,
        jstring j_sdk_version,
        jstring j_hw_device_arch) {

    VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                      twilio::LogLevel::kDebug,
                      "Create PlatformInfo");

    PlatformInfoContext *context = new PlatformInfoContext();
    if (!IsNull(env, j_platform_name)) {
        context->platform_info.platformName =
                JavaToUTF8StdString(env, j_platform_name);
    }
    if (!IsNull(env, j_platform_version)) {
        context->platform_info.platformVersion =
                JavaToUTF8StdString(env, j_platform_version);
    }
    if (!IsNull(env, j_hw_device_manufacturer)) {
        context->platform_info.hwDeviceManufacturer =
                JavaToUTF8StdString(env, j_hw_device_manufacturer);
    }
    if (!IsNull(env, j_hw_device_model)) {
        context->platform_info.hwDeviceModel =
                JavaToUTF8StdString(env, j_hw_device_model);
    }
    if (!IsNull(env, j_sdk_version)) {
        context->platform_info.sdkVersion =
                JavaToUTF8StdString(env, j_sdk_version);
    }
    if (!IsNull(env, j_hw_device_arch)) {
        context->platform_info.hwDeviceArch =
                JavaToUTF8StdString(env, j_hw_device_arch);
    }
    return webrtc::NativeToJavaPointer(context);
}


JNIEXPORT void JNICALL Java_com_twilio_video_PlatformInfo_nativeRelease
        (JNIEnv *env, jobject j_instance, jlong j_native_handle) {

    VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                      twilio::LogLevel::kDebug,
                      "Free PlatformInfo");
    PlatformInfoContext *platform_info_context =
            reinterpret_cast<PlatformInfoContext *>(j_native_handle);
    if (platform_info_context != nullptr) {
        delete platform_info_context;
    }
}

}
