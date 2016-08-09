#include "com_twilio_video_Room.h"
#include "webrtc/api/java/jni/jni_helpers.h"

#include "TSCLogger.h"
#include "room.h"


JNIEXPORT jstring JNICALL Java_com_twilio_rooms_Room_nativeGetName
        (JNIEnv *env, jobject instance, jlong nativeHandle) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());

    return nullptr;
}

JNIEXPORT jstring JNICALL Java_com_twilio_rooms_Room_nativeGetSid
        (JNIEnv *env, jobject instance, jlong nativeHandle) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());

    return nullptr;
}

JNIEXPORT void JNICALL Java_com_twilio_rooms_Room_nativeDisconnect
        (JNIEnv *env, jobject instance, jlong nativeHandle) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());

}

