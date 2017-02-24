#include <jni.h>

#ifndef VIDEO_ANDROID_COM_TWILIO_VIDEO_VIDEO_CLIENT_H_
#define VIDEO_ANDROID_COM_TWILIO_VIDEO_VIDEO_CLIENT_H_

#ifdef __cplusplus
extern "C" {
#endif

namespace twilio_video_jni {

JNIEXPORT void JNICALL Java_com_twilio_video_VideoClient_nativeSetCoreLogLevel
        (JNIEnv *, jobject, jint);

JNIEXPORT void JNICALL Java_com_twilio_video_VideoClient_nativeSetModuleLevel
        (JNIEnv *, jobject, jint, jint);

JNIEXPORT jint JNICALL Java_com_twilio_video_VideoClient_nativeGetCoreLogLevel
        (JNIEnv *, jobject);

JNIEXPORT jlong JNICALL Java_com_twilio_video_VideoClient_nativeCreateClient
        (JNIEnv *, jobject, jobject, jstring, jlong);

}

#ifdef __cplusplus
}
#endif

#endif // VIDEO_ANDROID_COM_TWILIO_VIDEO_VIDEO_CLIENT_H_
