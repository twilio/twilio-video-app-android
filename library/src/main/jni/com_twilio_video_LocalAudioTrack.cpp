#include "com_twilio_video_LocalAudioTrack.h"
#include "webrtc/sdk/android/src/jni/jni_helpers.h"
#include "class_reference_holder.h"

namespace twilio_video_jni {

std::shared_ptr<twilio::media::LocalAudioTrack> getLocalAudioTrack(jlong local_audio_track_handle) {
    LocalAudioTrackContext* local_audio_track_context =
            reinterpret_cast<LocalAudioTrackContext *>(local_audio_track_handle);

    return local_audio_track_context->getLocalAudioTrack();
}

jobject createJavaLocalAudioTrack(std::shared_ptr<twilio::media::LocalAudioTrack> local_audio_track) {
    JNIEnv *jni = webrtc_jni::GetEnv();
    jclass j_local_audio_track_class = twilio_video_jni::FindClass(jni,
                                                                   "com/twilio/video/LocalAudioTrack");
    jmethodID j_local_audio_track_ctor_id = webrtc_jni::GetMethodID(jni,
                                                                    j_local_audio_track_class,
                                                                    "<init>",
                                                                    "(JLjava/lang/String;Z)V");
    LocalAudioTrackContext* local_audio_track_context =
            new LocalAudioTrackContext(local_audio_track);

    jobject j_local_audio_track = jni->NewObject(j_local_audio_track_class,
                                                 j_local_audio_track_ctor_id,
                                                 webrtc_jni::jlongFromPointer(local_audio_track_context),
                                                 webrtc_jni::JavaStringFromStdString(jni, local_audio_track->getTrackId()),
                                                 local_audio_track->isEnabled());
    CHECK_EXCEPTION(jni);

    return j_local_audio_track;
}

JNIEXPORT jboolean JNICALL Java_com_twilio_video_LocalAudioTrack_nativeIsEnabled(JNIEnv *jni,
                                                                                 jobject j_local_audio_track,
                                                                                 jlong local_audio_track_handle) {
    std::shared_ptr<twilio::media::LocalAudioTrack> local_audio_track =
            getLocalAudioTrack(local_audio_track_handle);

    return local_audio_track->isEnabled();
}

JNIEXPORT void JNICALL Java_com_twilio_video_LocalAudioTrack_nativeEnable(JNIEnv *jni,
                                                                          jobject j_local_audio_track,
                                                                          jlong local_audio_track_handle,
                                                                          jboolean enabled) {
    std::shared_ptr<twilio::media::LocalAudioTrack> local_audio_track =
            getLocalAudioTrack(local_audio_track_handle);

    local_audio_track->setEnabled(enabled);
}

JNIEXPORT void JNICALL Java_com_twilio_video_LocalAudioTrack_nativeRelease(JNIEnv *jni,
                                                                           jobject j_local_audio_track,
                                                                           jlong local_audio_track_handle) {
    LocalAudioTrackContext* local_audio_track_context =
            reinterpret_cast<LocalAudioTrackContext *>(local_audio_track_handle);

    delete local_audio_track_context;
}

}
