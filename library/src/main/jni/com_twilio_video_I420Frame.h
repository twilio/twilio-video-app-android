#include <jni.h>

#ifndef VIDEO_ANDROID_COM_TWILIO_VIDEO_I420FRAME_H_
#define VIDEO_ANDROID_COM_TWILIO_VIDEO_I420FRAME_H_

#ifdef __cplusplus
extern "C" {
#endif

namespace twilio_video_jni {

JNIEXPORT void JNICALL Java_com_twilio_video_I420Frame_nativeRelease(JNIEnv *, jobject, jlong);

}

#ifdef __cplusplus
}
#endif

#endif // VIDEO_ANDROID_COM_TWILIO_VIDEO_I420FRAME_H_
