#include "com_twilio_video_Room.h"
#include "webrtc/api/android/jni/jni_helpers.h"

#include "video/logger.h"
#include "android_room_observer.h"
#include "android_stats_observer.h"


JNIEXPORT jboolean JNICALL Java_com_twilio_video_Room_nativeIsRecording
        (JNIEnv *env, jobject j_instance, jlong j_native_handle) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "%s", func_name.c_str());
    RoomContext *room_context = reinterpret_cast<RoomContext *>(j_native_handle);
    return room_context->room->isRecording();
}

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeDisconnect
        (JNIEnv *env, jobject j_instance, jlong j_native_handle) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "%s", func_name.c_str());
    RoomContext *room_context = reinterpret_cast<RoomContext *>(j_native_handle);
    room_context->room->disconnect();
}

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeGetStats
    (JNIEnv *env, jobject j_instance, jlong j_native_room_context, jlong j_native_stats_observer) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "%s", func_name.c_str());
    RoomContext *room_context = reinterpret_cast<RoomContext *>(j_native_room_context);
    StatsObserverContext *stats_observer_context =
        reinterpret_cast<StatsObserverContext *>(j_native_stats_observer);
    room_context->room->getStats(stats_observer_context->stats_observer);
}

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeRelease
    (JNIEnv *env, jobject j_instance, jlong j_native_handle) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "%s", func_name.c_str());
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
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "Create AndroidRoomObserver");
    RoomObserverContext *room_observer_context = new RoomObserverContext();
    room_observer_context->android_room_observer =
            std::make_shared<AndroidRoomObserver>(env, object);
    return webrtc_jni::jlongFromPointer(room_observer_context);
}

JNIEXPORT void JNICALL
Java_com_twilio_video_Room_00024InternalRoomListenerHandle_nativeRelease(JNIEnv *env,
                                                                        jobject instance,
                                                                        jlong nativeHandle) {
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "Free AndroidRoomObserver");
    RoomObserverContext
        *room_observer_context = reinterpret_cast<RoomObserverContext *>(nativeHandle);
    if (room_observer_context != nullptr) {
        room_observer_context->android_room_observer->setObserverDeleted();
        delete room_observer_context;
    }
}

JNIEXPORT jlong JNICALL
Java_com_twilio_video_Room_00024InternalStatsListenerHandle_nativeCreate(JNIEnv *env,
                                                                        jobject instance,
                                                                        jobject object) {
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "Create AndroidStatsObserver");
    StatsObserverContext *stats_observer_context = new StatsObserverContext();
    stats_observer_context->stats_observer = std::make_shared<AndroidStatsObserver>(env, object);
    return webrtc_jni::jlongFromPointer(stats_observer_context);
}

JNIEXPORT void JNICALL
Java_com_twilio_video_Room_00024InternalStatsListenerHandle_nativeRelease(JNIEnv *env,
                                                                         jobject instance,
                                                                         jlong nativeHandle) {
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "Free AndroidStatsObserver");
    StatsObserverContext *stats_observer_context =
        reinterpret_cast<StatsObserverContext *>(nativeHandle);
    if (stats_observer_context != nullptr) {
        delete stats_observer_context;
    }
}
