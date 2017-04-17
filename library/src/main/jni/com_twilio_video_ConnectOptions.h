#ifndef VIDEO_ANDROID_COM_TWILIO_VIDEO_CONNECT_OPTIONS_H_
#define VIDEO_ANDROID_COM_TWILIO_VIDEO_CONNECT_OPTIONS_H_

#include <jni.h>
#include "video/connect_options.h"

#ifdef __cplusplus
extern "C" {
#endif

namespace twilio_video_jni {

struct ConnectOptionsContext {
    ConnectOptionsContext() : connect_options(twilio::video::ConnectOptions::Builder("").build()) {}
    twilio::video::ConnectOptions connect_options;
};

JNIEXPORT jlong JNICALL Java_com_twilio_video_ConnectOptions_nativeCreate
        (JNIEnv *, jobject, jstring, jstring, jobject, jobject, jboolean, jlong);

}

#ifdef __cplusplus
}
#endif

#endif //  VIDEO_ANDROID_COM_TWILIO_VIDEO_CONNECT_OPTIONS_H_
