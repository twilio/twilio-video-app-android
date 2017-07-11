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

#include "webrtc/base/refcount.h"
#include "webrtc/voice_engine/include/voe_base.h"
#include "webrtc/modules/audio_device/android/audio_manager.h"
#include "webrtc/modules/audio_device/android/opensles_player.h"
#include "webrtc/sdk/android/src/jni/classreferenceholder.h"
#include "webrtc/base/ssladapter.h"

#include "video/video.h"
#include "android_room_observer.h"
#include "com_twilio_video_ConnectOptions.h"
#include "com_twilio_video_Room.h"
#include "com_twilio_video_MediaFactory.h"
#include "class_reference_holder.h"
#include "logging.h"

namespace twilio_video_jni {

extern "C" jint JNIEXPORT JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kDebug,
                      "%s", func_name.c_str());
    jint ret = webrtc_jni::InitGlobalJniVariables(jvm);
    if (ret < 0) {
        VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                           twilio::video::LogLevel::kError,
                           "InitGlobalJniVariables() failed");
        return -1;
    }
    RTC_CHECK(rtc::InitializeSSL()) << "Failed to InitializeSSL()";
    webrtc_jni::LoadGlobalClassReferenceHolder();
    twilio_video_jni::LoadGlobalClassReferenceHolder();

    return ret;
}

extern "C" void JNIEXPORT JNICALL JNI_OnUnLoad(JavaVM *jvm, void *reserved) {
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kDebug,
                      "%s", func_name.c_str());
    twilio_video_jni::FreeGlobalClassReferenceHolder();
    webrtc_jni::FreeGlobalClassReferenceHolder();
    RTC_CHECK(rtc::CleanupSSL()) << "Failed to CleanupSSL()";
}

JNIEXPORT void JNICALL Java_com_twilio_video_Video_nativeSetCoreLogLevel
        (JNIEnv *env, jobject instance, jint level) {
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kDebug,
                      "setCoreLogLevel");
    twilio::video::LogLevel log_level = static_cast<twilio::video::LogLevel>(level);
    twilio::video::setLogLevel(log_level);
}

JNIEXPORT void JNICALL Java_com_twilio_video_Video_nativeSetModuleLevel
        (JNIEnv *env, jobject instance, jint module, jint level) {
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kDebug,
                      "setModuleLevel");
    twilio::video::LogModule log_module = static_cast<twilio::video::LogModule>(module);
    twilio::video::LogLevel log_level = static_cast<twilio::video::LogLevel>(level);
    twilio::video::setModuleLogLevel(log_module, log_level);
}

JNIEXPORT jint JNICALL Java_com_twilio_video_Video_nativeGetCoreLogLevel
        (JNIEnv *env, jobject instance) {
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kDebug,
                      "getCoreLogLevel");
    return static_cast<jint>(twilio::video::getLogLevel());
}

}
