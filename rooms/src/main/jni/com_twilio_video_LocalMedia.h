#include <jni.h>

#ifndef ROOMS_ANDROID_COM_TWILIO_VIDEO_LOCALMEDIA_H_
#define ROOMS_ANDROID_COM_TWILIO_VIDEO_LOCALMEDIA_H_

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_com_twilio_video_LocalMedia_nativeCreate(JNIEnv *, jobject);
JNIEXPORT void JNICALL Java_com_twilio_video_LocalMedia_nativeRelease(JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif

#endif // ROOMS_ANDROID_COM_TWILIO_VIDEO_LOCALMEDIA_H_
