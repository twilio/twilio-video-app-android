#include "com_twilio_video_Room.h"
#include "webrtc/api/java/jni/jni_helpers.h"

#include "video/logger.h"
#include "android_room_observer.h"


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

JNIEXPORT jlong JNICALL
Java_com_twilio_video_Room_00024InternalRoomListenerHandle_nativeCreate(JNIEnv *env,
                                                                          jobject instance,
                                                                          jobject object) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug,
                       "Create AndroidRoomObserver");
    AndroidRoomObserver *android_room_observer = new AndroidRoomObserver(env, object);
    return jlongFromPointer(android_room_observer);
}

JNIEXPORT void JNICALL
Java_com_twilio_video_Room_00024InternalRoomListenerHandle_nativeFree(JNIEnv *env,
                                                                        jobject instance,
                                                                        jlong nativeHandle) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug,
                       "Free AndroidRoomObserver");
    AndroidRoomObserver
        *android_room_observer = reinterpret_cast<AndroidRoomObserver *>(nativeHandle);
    if (android_room_observer != nullptr) {
        android_room_observer->setObserverDeleted();
        delete android_room_observer;
    }
}
