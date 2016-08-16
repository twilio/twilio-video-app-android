#include "com_twilio_video_Room.h"
#include "webrtc/api/java/jni/jni_helpers.h"

#include "video/TSCLogger.h"


JNIEXPORT jstring JNICALL Java_com_twilio_video_Room_nativeGetSid
        (JNIEnv *env, jobject j_instance, jlong j_native_handle) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());
    RoomContext *room_context = reinterpret_cast<RoomContext *>(j_native_handle);
    if (room_context == nullptr || !room_context->room) {
        return webrtc_jni::JavaStringFromStdString(env, std::string(""));
    }
    jstring result = webrtc_jni::JavaStringFromStdString(env, room_context->room->getSid());
    return result;
}

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeDisconnect
        (JNIEnv *env, jobject j_instance, jlong j_native_handle) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());
    RoomContext *room_context = reinterpret_cast<RoomContext *>(j_native_handle);
    room_context->room->disconnect();
}

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeRelease
    (JNIEnv *env, jobject j_instance, jlong j_native_handle) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());
    RoomContext *room_context = reinterpret_cast<RoomContext *>(j_native_handle);
    if (room_context == nullptr) {
        return;
    }
    delete room_context;
}

