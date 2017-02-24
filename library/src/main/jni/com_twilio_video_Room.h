#include <jni.h>
#include <memory>

#include "video/stats_observer.h"
#include "video/room.h"
#include "android_room_observer.h"

#ifndef _Included_com_twilio_conversations_Room
#define _Included_com_twilio_conversations_Room

#ifdef __cplusplus
extern "C" {
#endif

namespace twilio_video_jni {

struct RoomContext {
    std::unique_ptr<twilio::video::Room> room;
};

struct RoomObserverContext {
    std::shared_ptr<AndroidRoomObserver> android_room_observer;
};

struct StatsObserverContext {
    std::shared_ptr<twilio::video::StatsObserver> stats_observer;
};

JNIEXPORT jlong JNICALL Java_com_twilio_video_Room_nativeConnect
    (JNIEnv *, jobject, jobject, jlong, jlong);

JNIEXPORT jboolean JNICALL Java_com_twilio_video_Room_nativeIsRecording
    (JNIEnv *, jobject, jlong);

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeDisconnect
    (JNIEnv *, jobject, jlong);

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeGetStats
    (JNIEnv *, jobject, jlong, jlong);

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeOnNetworkChange
    (JNIEnv *, jobject, jlong, jobject);

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeRelease
    (JNIEnv *, jobject, jlong);

JNIEXPORT jlong JNICALL Java_com_twilio_video_Room_00024InternalRoomListenerHandle_nativeCreate
    (JNIEnv *, jobject, jobject);

JNIEXPORT void JNICALL Java_com_twilio_video_Room_00024InternalRoomListenerHandle_nativeRelease
    (JNIEnv *, jobject, jlong);

JNIEXPORT jlong JNICALL Java_com_twilio_video_Room_00024InternalStatsListenerHandle_nativeCreate
    (JNIEnv *, jobject, jobject);

JNIEXPORT void JNICALL Java_com_twilio_video_Room_00024InternalStatsListenerHandle_nativeRelease
    (JNIEnv *, jobject, jlong);

}

#ifdef __cplusplus
}
#endif
#endif
