#include <jni.h>

#ifndef _Included_com_twilio_conversations_CameraCapturer
#define _Included_com_twilio_conversations_CameraCapturer
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_com_twilio_conversations_CameraCapturer_nativeStopVideoSource
        (JNIEnv *, jobject, jlong);

JNIEXPORT void JNICALL Java_com_twilio_conversations_CameraCapturer_nativeRestartVideoSource
        (JNIEnv *, jobject, jlong);

JNIEXPORT jlong JNICALL
        Java_com_twilio_conversations_CameraCapturer_nativeCreateNativeCapturer(JNIEnv *env,
                                                                                jobject instance,
                                                                                jobject j_video_capturer,
                                                                                jobject j_egl_context);

JNIEXPORT void JNICALL
        Java_com_twilio_conversations_CameraCapturer_nativeDisposeCapturer(JNIEnv *env,
                                                                           jobject instance,
                                                                           jlong nativeVideoCapturerAndroid);

#ifdef __cplusplus
}
#endif
#endif
