#include "com_twilio_video_Media.h"
#include "webrtc/api/java/jni/jni_helpers.h"

#include "android_media_observer.h"

jobject createJavaAudioTrack(JNIEnv *env,
                             std::shared_ptr<twilio::media::AudioTrack> audio_track,
                             jclass j_audio_track_class, jmethodID j_audio_track_ctor_id) {
    // Create audio track context
    AudioTrackContext *audio_track_context = new AudioTrackContext();
    audio_track_context->audio_track = audio_track;
    jlong j_audio_track_context = webrtc_jni::jlongFromPointer(audio_track_context);
    // Get track info
    jstring j_track_id = webrtc_jni::JavaStringFromStdString(env, audio_track->getTrackId());
    jlong j_webrtc_track = webrtc_jni::jlongFromPointer(audio_track->getWebRtcTrack());
    jboolean j_is_enabled = audio_track->isEnabled();

    return env->NewObject(j_audio_track_class, j_audio_track_ctor_id,
                         j_audio_track_context, j_track_id, j_is_enabled, j_webrtc_track);
}

jobject createJavaVideoTrack(JNIEnv *env,
                             std::shared_ptr<twilio::media::VideoTrack> video_track,
                             jclass j_video_track_class, jmethodID j_video_track_ctor_id) {
    // Create video track context
    VideoTrackContext *video_track_context = new VideoTrackContext();
    video_track_context->video_track = video_track;
    jlong j_video_track_context = webrtc_jni::jlongFromPointer(video_track_context);

    jstring j_track_id = webrtc_jni::JavaStringFromStdString(env, video_track->getTrackId());
    jlong j_webrtc_track = webrtc_jni::jlongFromPointer(video_track->getWebRtcTrack());
    jboolean j_is_enabled = video_track->isEnabled();

    return env->NewObject(j_video_track_class, j_video_track_ctor_id,
                         j_video_track_context, j_track_id, j_is_enabled, j_webrtc_track);
}

JNIEXPORT void JNICALL
Java_com_twilio_video_Media_nativeSetInternalListener(JNIEnv *env,
                                                      jobject j_instance,
                                                      jlong j_media_context,
                                                      jlong j_internal_listener) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug,
                       "Set internal media listener");
    MediaContext *media_context = reinterpret_cast<MediaContext *>(j_media_context);
    AndroidMediaObserver *media_observer =
        reinterpret_cast<AndroidMediaObserver *>(j_internal_listener);
    media_context->media->attachObserver(media_observer);
}

JNIEXPORT void JNICALL
Java_com_twilio_video_AudioTrack_nativeRelease(JNIEnv *env, jobject j_instance,
                                               jlong j_audio_track_context) {

    AudioTrackContext *audio_track_context =
        reinterpret_cast<AudioTrackContext *>(j_audio_track_context);
    delete audio_track_context;
}

JNIEXPORT void JNICALL
Java_com_twilio_video_VideoTrack_nativeRelease(JNIEnv *env, jobject instance,
                                               jlong j_video_track_context) {

    VideoTrackContext *video_track_context =
        reinterpret_cast<VideoTrackContext *>(j_video_track_context);
    delete video_track_context;

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
        android_media_observer->setObserverDeleted();
        delete android_media_observer;
    }
}