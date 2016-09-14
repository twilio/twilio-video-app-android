#include "com_twilio_video_Media.h"
#include "webrtc/api/java/jni/jni_helpers.h"
#include "webrtc/api/java/jni/classreferenceholder.h"
#include "android_media_observer.h"

jobject createJavaAudioTrack(JNIEnv *env,
                             std::shared_ptr<twilio::media::AudioTrack> audio_track,
                             jclass j_audio_track_class,
                             jmethodID j_audio_track_ctor_id) {
    jstring j_track_id = webrtc_jni::JavaStringFromStdString(env, audio_track->getTrackId());
    jboolean j_is_enabled = audio_track->isEnabled();

    return env->NewObject(j_audio_track_class, j_audio_track_ctor_id, j_track_id, j_is_enabled);
}

jobject createJavaVideoTrack(JNIEnv *env,
                             std::shared_ptr<twilio::media::VideoTrack> video_track,
                             jclass j_video_track_class, jmethodID j_video_track_ctor_id) {
    jclass j_webrtc_video_track_class = webrtc_jni::FindClass(env, "org/webrtc/VideoTrack");
    jmethodID j_webrtc_video_track_ctor_id = webrtc_jni::GetMethodID(env,
                                                                     j_webrtc_video_track_class,
                                                                     "<init>",
                                                                     "(J)V");
    jobject j_webrtc_video_track = env->NewObject(j_webrtc_video_track_class,
                                                  j_webrtc_video_track_ctor_id,
                                                  webrtc_jni::jlongFromPointer(video_track->getWebRtcTrack()));

    return env->NewObject(j_video_track_class,
                          j_video_track_ctor_id,
                          j_webrtc_video_track);
}

JNIEXPORT void JNICALL
Java_com_twilio_video_Media_nativeSetInternalListener(JNIEnv *env,
                                                      jobject j_instance,
                                                      jlong j_media_context,
                                                      jlong j_internal_listener) {
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "Set internal media listener");
    MediaContext *media_context = reinterpret_cast<MediaContext *>(j_media_context);
    AndroidMediaObserver *media_observer =
        reinterpret_cast<AndroidMediaObserver *>(j_internal_listener);
    media_context->media->attachObserver(media_observer);
}

JNIEXPORT void JNICALL
Java_com_twilio_video_Media_nativeRelease(JNIEnv *env, jobject j_instance,
                                          jlong j_native_media_context) {
    MediaContext *media_context = reinterpret_cast<MediaContext *>(j_native_media_context);
    delete media_context;
}

JNIEXPORT jlong JNICALL
Java_com_twilio_video_Media_00024InternalMediaListenerHandle_nativeCreate(JNIEnv *env,
                                                                          jobject instance,
                                                                          jobject j_object) {
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "Create AndroidMediaObserver");
    AndroidMediaObserver *android_media_observer = new AndroidMediaObserver(env, j_object);
    return webrtc_jni::jlongFromPointer(android_media_observer);
}

JNIEXPORT void JNICALL
Java_com_twilio_video_Media_00024InternalMediaListenerHandle_nativeRelease(JNIEnv *env,
                                                                           jobject instance,
                                                                           jlong nativeHandle) {
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "Free AndroidMediaObserver");
    AndroidMediaObserver *android_media_observer =
        reinterpret_cast<AndroidMediaObserver *>(nativeHandle);
    if (android_media_observer != nullptr) {
        android_media_observer->setObserverDeleted();
        delete android_media_observer;
    }
}