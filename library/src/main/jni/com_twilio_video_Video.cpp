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

#include "com_twilio_video_Video.h"
#include "webrtc/sdk/android/src/jni/jni_helpers.h"
#include "webrtc/rtc_base/refcount.h"
#include "webrtc/sdk/android/native_api/base/init.h"
#include "webrtc/modules/utility/include/jvm_android.h"
#include "webrtc/sdk/android/src/jni/classreferenceholder.h"
#include "webrtc/rtc_base/ssladapter.h"
#include "twilio/video/video.h"
#include "android_room_observer.h"
#include "com_twilio_video_ConnectOptions.h"
#include "com_twilio_video_Room.h"
#include "com_twilio_video_MediaFactory.h"
#include "class_reference_holder.h"
#include "logging.h"

namespace twilio_video_jni {

extern "C" jint JNIEXPORT JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                      twilio::LogLevel::kDebug,
                      "%s", func_name.c_str());
    webrtc::InitAndroid(jvm);
    twilio_video_jni::LoadGlobalClassReferenceHolder();
    RTC_CHECK(rtc::InitializeSSL()) << "Failed to InitializeSSL()";

    return JNI_VERSION_1_6;
}

extern "C" void JNIEXPORT JNICALL JNI_OnUnLoad(JavaVM *jvm, void *reserved) {
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                      twilio::LogLevel::kDebug,
                      "%s", func_name.c_str());
    webrtc::JVM::Uninitialize();
    RTC_CHECK(rtc::CleanupSSL()) << "Failed to CleanupSSL()";
    twilio_video_jni::FreeGlobalClassReferenceHolder();
    webrtc::jni::FreeGlobalClassReferenceHolder();
}

JNIEXPORT void JNICALL Java_com_twilio_video_Video_nativeSetCoreLogLevel
        (JNIEnv *env, jobject instance, jint level) {
    VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                      twilio::LogLevel::kDebug,
                      "setCoreLogLevel");
    twilio::LogLevel log_level = static_cast<twilio::LogLevel>(level);
    twilio::setLogLevel(log_level);
}

JNIEXPORT void JNICALL Java_com_twilio_video_Video_nativeSetModuleLevel
        (JNIEnv *env, jobject instance, jint module, jint level) {
    VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                      twilio::LogLevel::kDebug,
                      "setModuleLevel");
    twilio::LogModule log_module = static_cast<twilio::LogModule>(module);
    twilio::LogLevel log_level = static_cast<twilio::LogLevel>(level);
    twilio::setModuleLogLevel(log_module, log_level);
}

JNIEXPORT jint JNICALL Java_com_twilio_video_Video_nativeGetCoreLogLevel
        (JNIEnv *env, jobject instance) {
    VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                      twilio::LogLevel::kDebug,
                      "getCoreLogLevel");
    return static_cast<jint>(twilio::getLogLevel());
}

}
