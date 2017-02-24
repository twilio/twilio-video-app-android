#include <jni.h>
#include "video/platform_info.h"

#ifndef VIDEO_ANDROID_COM_TWILIO_VIDEO_PLATFORMINFO_H_
#define VIDEO_ANDROID_COM_TWILIO_VIDEO_PLATFORMINFO_H_

#ifdef __cplusplus
extern "C" {
#endif

namespace twilio_video_jni {

struct PlatformInfoContext {
    twilio::video::PlatformInfo platform_info;
};

JNIEXPORT jlong JNICALL Java_com_twilio_video_PlatformInfo_nativeCreate
    (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jstring, jstring);


JNIEXPORT void JNICALL Java_com_twilio_video_PlatformInfo_nativeRelease
    (JNIEnv *, jobject, jlong);

}

#ifdef __cplusplus
}
#endif

#endif // VIDEO_ANDROID_COM_TWILIO_VIDEO_PLATFORMINFO_H_
