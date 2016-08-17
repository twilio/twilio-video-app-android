#include "com_twilio_video_LocalMedia.h"

JNIEXPORT jlong JNICALL Java_com_twilio_video_LocalMedia_nativeCreate(JNIEnv * jni,
                                                                     jobject j_local_media) {
    return 1;
}

JNIEXPORT void JNICALL Java_com_twilio_video_LocalMedia_nativeRelease(JNIEnv * jni,
                                                                      jobject j_local_media,
                                                                      jlong local_media_handle) {
}