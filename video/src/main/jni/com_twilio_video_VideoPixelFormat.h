#ifndef VIDEO_ANDROID_COM_TWILIO_VIDEO_VIDEOPIXELFORMAT_H_
#define VIDEO_ANDROID_COM_TWILIO_VIDEO_VIDEOPIXELFORMAT_H_

#include <jni.h>
#include <stdint.h>

#include "webrtc/media/base/videocommon.h"

#ifdef __cplusplus
extern "C" {
#endif

namespace twilio_video_jni {

enum VideoPixelFormat {
    kVideoPixelFormatNv21 = cricket::FOURCC_NV21,
    kVideoPixelFormatRgba8888 = cricket::FOURCC_ABGR
};

JNIEXPORT jint JNICALL Java_com_twilio_video_VideoPixelFormat_nativeGetValue(JNIEnv *,
                                                                             jobject,
                                                                             jstring);

};

#ifdef __cplusplus
}
#endif

#endif // VIDEO_ANDROID_COM_TWILIO_VIDEO_VIDEOPIXELFORMAT_H_
