#include "com_twilio_video_LocalMedia.h"
#include "webrtc/api/java/jni/jni_helpers.h"

namespace twilio {
namespace media {

LocalMedia* getLocalMedia(jlong local_media_handle) {
    LocalMediaContext* local_media_context =
            reinterpret_cast<LocalMediaContext *>(local_media_handle);

    return local_media_context->getLocalMedia().get();
}

// TODO switch to cricket::AudioOptions extension
MediaConstraints* getAudioOptions(jobject j_audio_options) {
    // TODO: actually convert audio options
    return nullptr;
}

JNIEXPORT jobject JNICALL Java_com_twilio_video_LocalMedia_nativeGetDefaultAudioOptions(JNIEnv *jni,
                                                                                        jobject j_local_media) {
    // TODO: create default audio options
    return nullptr;
}

JNIEXPORT jlong JNICALL Java_com_twilio_video_LocalMedia_nativeAddAudioTrack(JNIEnv *jni,
                                                                             jobject j_local_media,
                                                                             jlong local_media_handle,
                                                                             jboolean enabled,
                                                                             jobject j_audio_options) {
    LocalMedia* local_media = getLocalMedia(local_media_handle);
    std::shared_ptr<AudioTrack> audio_track = local_media->addAudioTrack(enabled,
                                                                         getAudioOptions(j_audio_options));

    return webrtc_jni::jlongFromPointer(audio_track.get()->getWebrtcTrack());
}

JNIEXPORT void JNICALL Java_com_twilio_video_LocalMedia_nativeRelease(JNIEnv *jni,
                                                                      jobject j_local_media,
                                                                      jlong local_media_handle) {
    LocalMediaContext* local_media_context =
            reinterpret_cast<LocalMediaContext *>(local_media_handle);

    delete local_media_context;
}

}
}