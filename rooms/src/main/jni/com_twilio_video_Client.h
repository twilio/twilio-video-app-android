#include <jni.h>

#ifndef _Included_com_twilio_conversations_Client
#define _Included_com_twilio_conversations_Client

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_com_twilio_video_Client_nativeSetCoreLogLevel
        (JNIEnv *, jobject, jint);

JNIEXPORT void JNICALL Java_com_twilio_video_Client_nativeSetModuleLevel
        (JNIEnv *, jobject, jint, jint);

JNIEXPORT jint JNICALL Java_com_twilio_video_Client_nativeGetCoreLogLevel
        (JNIEnv *, jobject);

JNIEXPORT jboolean JNICALL Java_com_twilio_video_Client_nativeInitialize
    (JNIEnv *, jobject, jobject);

JNIEXPORT jlong JNICALL Java_com_twilio_video_Client_nativeCreateClient
    (JNIEnv *, jobject, jobject, jobject);

JNIEXPORT jlong JNICALL Java_com_twilio_video_Client_nativeConnect
        (JNIEnv *, jobject, jlong, jlong, jobject);

JNIEXPORT jlong JNICALL Java_com_twilio_video_Client_00024RoomListenerHandle_nativeCreate
        (JNIEnv *, jobject, jobject);

JNIEXPORT void JNICALL Java_com_twilio_video_Client_00024RoomListenerHandle_nativeFree
        (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif
