#include <jni.h>
#include <memory>
#include <video/stats_observer.h>
#include "video/room.h"

#ifndef _Included_com_twilio_conversations_Room
#define _Included_com_twilio_conversations_Room

#ifdef __cplusplus
extern "C" {
#endif

struct RoomContext {
    std::unique_ptr<twilio::video::Room> room;
};

struct StatsObserverContext {
    std::shared_ptr<twilio::video::StatsObserver> stats_observer;
};

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeDisconnect
        (JNIEnv *, jobject, jlong);

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeGetStats
    (JNIEnv *, jobject, jlong, jlong);

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

#ifdef __cplusplus
}
#endif
#endif
