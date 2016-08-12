#include "com_twilio_video_Room.h"
#include "webrtc/api/java/jni/jni_helpers.h"

#include "video/TSCLogger.h"


JNIEXPORT jstring JNICALL Java_com_twilio_video_Room_nativeGetName
        (JNIEnv *env, jobject j_instance, jlong j_native_handle) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());
    RoomDataContext *room_dc = reinterpret_cast<RoomDataContext *>(j_native_handle);
    jstring result = webrtc_jni::JavaStringFromStdString(env, room_dc->room->getName());
    return result;
}

JNIEXPORT jstring JNICALL Java_com_twilio_video_Room_nativeGetSid
        (JNIEnv *env, jobject j_instance, jlong j_native_handle) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());
    RoomDataContext *room_dc = reinterpret_cast<RoomDataContext *>(j_native_handle);
    jstring result = webrtc_jni::JavaStringFromStdString(env, room_dc->room->getSid());
    return result;
}

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeDisconnect
        (JNIEnv *env, jobject j_instance, jlong j_native_handle) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());
    RoomDataContext *room_dc = reinterpret_cast<RoomDataContext *>(j_native_handle);
    room_dc->room->disconnect();

    // TODO: Should we delete room_dc at this point ?
    //delete room_dc;
}

JNIEXPORT jobject JNICALL
Java_com_twilio_video_Room_nativeGetState(JNIEnv *env, jobject j_instance, jlong j_native_handle) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());
    RoomDataContext *room_dc = reinterpret_cast<RoomDataContext *>(j_native_handle);
    twilio::video::Room::State state = room_dc->room->getState();
    std::string state_class = "com/twilio/video/RoomState";
    jclass j_state_class = env->FindClass("com/twilio/video/RoomState");
    jobject j_room_state = webrtc_jni::JavaEnumFromIndex(env, j_state_class, state_class, state);
    return j_room_state;
}

