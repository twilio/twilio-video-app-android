#ifndef VIDEO_ANDROID_COM_TWILIO_VIDEO_ROOM_H_
#define VIDEO_ANDROID_COM_TWILIO_VIDEO_ROOM_H_

#include <jni.h>
#include <memory>

#include "video/video.h"
#include "video/stats_observer.h"
#include "video/room.h"
#include "android_room_observer.h"
#include "android_stats_observer.h"
#include "com_twilio_video_ConnectOptions.h"

#ifdef __cplusplus
extern "C" {
#endif

namespace twilio_video_jni {

JNIEXPORT jlong JNICALL Java_com_twilio_video_Room_nativeConnect
        (JNIEnv *, jobject, jobject, jobject, jobject, jlong);

JNIEXPORT jboolean JNICALL Java_com_twilio_video_Room_nativeIsRecording
    (JNIEnv *, jobject, jlong);

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeDisconnect
    (JNIEnv *, jobject, jlong);

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeGetStats
    (JNIEnv *, jobject, jlong, jlong);

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeOnNetworkChange
    (JNIEnv *, jobject, jlong, jobject);

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeReleaseRoom
        (JNIEnv *, jobject, jlong);

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeRelease
    (JNIEnv *, jobject, jlong);

}

#ifdef __cplusplus
}
#endif
#endif // VIDEO_ANDROID_COM_TWILIO_VIDEO_ROOM_H_
