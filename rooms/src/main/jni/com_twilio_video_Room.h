#include <jni.h>
#include <memory>
#include "video/room.h"

#ifndef _Included_com_twilio_conversations_Room
#define _Included_com_twilio_conversations_Room

#ifdef __cplusplus
extern "C" {
#endif

struct RoomContext {
    std::unique_ptr<twilio::video::Room> room;
};

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeDisconnect
        (JNIEnv *, jobject, jlong);

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeRelease
    (JNIEnv *, jobject, jlong);

JNIEXPORT jlong JNICALL Java_com_twilio_video_Room_00024InternalListenerHandle_nativeCreate
    (JNIEnv *, jobject, jobject);

JNIEXPORT void JNICALL Java_com_twilio_video_Room_00024InternalListenerHandle_nativeFree
    (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif
