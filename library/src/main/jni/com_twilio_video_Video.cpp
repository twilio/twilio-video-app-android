#include "com_twilio_video_Video.h"
#include "webrtc/sdk/android/src/jni/jni_helpers.h"

#include "webrtc/base/refcount.h"
#include "webrtc/voice_engine/include/voe_base.h"
#include "webrtc/modules/audio_device/android/audio_manager.h"
#include "webrtc/modules/audio_device/android/opensles_player.h"
#include "webrtc/sdk/android/src/jni/classreferenceholder.h"
#include "webrtc/base/ssladapter.h"

#include "video/logger.h"
#include "video/video.h"
#include "android_room_observer.h"
#include "com_twilio_video_ConnectOptions.h"
#include "com_twilio_video_Room.h"
#include "com_twilio_video_MediaFactory.h"
#include "class_reference_holder.h"

namespace twilio_video_jni {

extern "C" jint JNIEXPORT JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "%s", func_name.c_str());
    jint ret = webrtc_jni::InitGlobalJniVariables(jvm);
    if (ret < 0) {
        TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                           twilio::video::kTSCoreLogLevelError,
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
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "%s", func_name.c_str());
    twilio_video_jni::FreeGlobalClassReferenceHolder();
    webrtc_jni::FreeGlobalClassReferenceHolder();
    RTC_CHECK(rtc::CleanupSSL()) << "Failed to CleanupSSL()";
}

JNIEXPORT void JNICALL Java_com_twilio_video_Video_nativeSetCoreLogLevel
        (JNIEnv *env, jobject instance, jint level) {
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "setCoreLogLevel");
    twilio::video::TSCoreLogLevel coreLogLevel = static_cast<twilio::video::TSCoreLogLevel>(level);
    twilio::video::Logger::instance()->setLogLevel(coreLogLevel);
}

JNIEXPORT void JNICALL Java_com_twilio_video_Video_nativeSetModuleLevel
        (JNIEnv *env, jobject instance, jint module, jint level) {
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "setModuleLevel");
    twilio::video::TSCoreLogModule coreLogModule = static_cast<twilio::video::TSCoreLogModule>(module);
    twilio::video::TSCoreLogLevel coreLogLevel = static_cast<twilio::video::TSCoreLogLevel>(level);
    twilio::video::Logger::instance()->setModuleLogLevel(coreLogModule, coreLogLevel);
}

JNIEXPORT jint JNICALL Java_com_twilio_video_Video_nativeGetCoreLogLevel
        (JNIEnv *env, jobject instance) {
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "getCoreLogLevel");
    return twilio::video::Logger::instance()->getLogLevel();
}

}
