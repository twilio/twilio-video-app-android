#include "com_twilio_video_LocalMedia.h"
#include "webrtc/api/java/jni/jni_helpers.h"

namespace twilio {
namespace media {

LocalMedia* getLocalMedia(jlong local_media_handle) {
    LocalMediaContext* local_media_context =
            reinterpret_cast<LocalMediaContext *>(local_media_handle);

    return local_media_context->getLocalMedia().get();
}

JNIEXPORT jlong JNICALL Java_com_twilio_video_LocalMedia_nativeAddAudioTrack(JNIEnv *jni,
                                                                             jobject j_local_media,
                                                                             jlong local_media_handle) {
    LocalMedia* local_media = getLocalMedia(local_media_handle);
    std::shared_ptr<AudioTrack> audio_track = local_media->addAudioTrack();

    return webrtc_jni::jlongFromPointer(audio_track.get());
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