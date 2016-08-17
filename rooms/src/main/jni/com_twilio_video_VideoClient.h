#include <jni.h>

#ifndef _Included_com_twilio_conversations_Client
#define _Included_com_twilio_conversations_Client

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_com_twilio_video_VideoClient_nativeSetCoreLogLevel
        (JNIEnv *, jobject, jint);

JNIEXPORT void JNICALL Java_com_twilio_video_VideoClient_nativeSetModuleLevel
        (JNIEnv *, jobject, jint, jint);

JNIEXPORT jint JNICALL Java_com_twilio_video_VideoClient_nativeGetCoreLogLevel
        (JNIEnv *, jobject);

JNIEXPORT jboolean JNICALL Java_com_twilio_video_VideoClient_nativeInitialize
    (JNIEnv *, jobject, jobject);

JNIEXPORT jlong JNICALL Java_com_twilio_video_VideoClient_nativeCreateClient
    (JNIEnv *, jobject, jobject, jobject);

JNIEXPORT jlong JNICALL Java_com_twilio_video_VideoClient_nativeConnect
        (JNIEnv *, jobject, jlong, jlong, jobject);


#ifdef __cplusplus
}
#endif
#endif
