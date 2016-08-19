#include "com_twilio_video_Media.h"
#include "webrtc/api/java/jni/jni_helpers.h"

#include "android_media_observer.h"

JNIEXPORT jlong JNICALL
Java_com_twilio_video_Media_00024InternalMediaListenerHandle_nativeCreate(JNIEnv *env,
                                                                          jobject instance,
                                                                          jobject j_object) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug,
                       "Create AndroidMediaObserver");
    AndroidMediaObserver *android_media_observer = new AndroidMediaObserver(env, j_object);
    return webrtc_jni::jlongFromPointer(android_media_observer);
}

JNIEXPORT void JNICALL
Java_com_twilio_video_Media_00024InternalMediaListenerHandle_nativeFree(JNIEnv *env,
                                                                        jobject instance,
                                                                        jlong nativeHandle) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug,
                       "Free AndroidMediaObserver");
    AndroidMediaObserver *android_media_observer =
        reinterpret_cast<AndroidMediaObserver *>(nativeHandle);
    if (android_media_observer != nullptr) {
        //android_media_observer->setObserverDeleted();
        delete android_media_observer;
    }
}