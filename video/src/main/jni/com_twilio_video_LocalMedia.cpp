#include "com_twilio_video_LocalMedia.h"
#include "third_party/webrtc/webrtc/api/androidvideocapturer.h"
#include "third_party/webrtc/webrtc/api/java/jni/androidvideocapturer_jni.h"
#include "webrtc/api/java/jni/jni_helpers.h"

namespace twilio_video_jni {

twilio::media::LocalMedia* getLocalMedia(jlong local_media_handle) {
    LocalMediaContext* local_media_context =
            reinterpret_cast<LocalMediaContext *>(local_media_handle);

    return local_media_context->getLocalMedia().get();
}

// TODO switch to cricket::AudioOptions extension
twilio::media::MediaConstraints* getAudioOptions(jobject j_audio_options) {
    // TODO: actually convert audio options
    return nullptr;
}

twilio::media::MediaConstraints* getVideoConstraints(jobject j_video_contraints) {
    twilio::media::MediaConstraints* video_constraints = nullptr;

    if (webrtc_jni::IsNull(webrtc_jni::GetEnv(), j_video_contraints)) {
        video_constraints = twilio::media::MediaConstraints::defaultVideoConstraints();
    } else {
        // TODO: convert from java object to actual constraints
    }

    return video_constraints;
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
    twilio::media::LocalMedia* local_media = getLocalMedia(local_media_handle);
    std::shared_ptr<twilio::media::AudioTrack> audio_track = local_media->addAudioTrack(enabled,
                                                                         getAudioOptions(j_audio_options));

    return webrtc_jni::jlongFromPointer(audio_track.get()->getWebrtcTrack());
}

JNIEXPORT jboolean JNICALL Java_com_twilio_video_LocalMedia_nativeRemoveAudioTrack(JNIEnv *jni,
                                                                                   jobject j_local_media,
                                                                                   jlong local_media_handle,
                                                                                   jstring track_id) {
    twilio::media::LocalMedia* local_media = getLocalMedia(local_media_handle);

    return local_media->removeAudioTrack(webrtc_jni::JavaToStdString(jni, track_id));
}

JNIEXPORT jlong JNICALL Java_com_twilio_video_LocalMedia_nativeAddVideoTrack(JNIEnv *jni,
                                                                             jobject j_local_media,
                                                                             jlong local_media_handle,
                                                                             jboolean enabled,
                                                                             jobject j_video_capturer,
                                                                             jobject j_video_contraints) {
    twilio::media::LocalMedia* local_media = getLocalMedia(local_media_handle);
    rtc::scoped_refptr<webrtc::AndroidVideoCapturerDelegate> delegate =
            new rtc::RefCountedObject<webrtc_jni::AndroidVideoCapturerJni>(jni,
                                                                           j_video_capturer,
                                                                           nullptr);
    cricket::VideoCapturer* capturer = new webrtc::AndroidVideoCapturer(delegate);

    std::shared_ptr<twilio::media::VideoTrack> video_track = local_media->addVideoTrack(enabled,
                                                                                        getVideoConstraints(j_video_contraints),
                                                                                        capturer);

    return webrtc_jni::jlongFromPointer(video_track.get()->getWebrtcTrack());
}
JNIEXPORT jboolean JNICALL Java_com_twilio_video_LocalMedia_nativeRemoveVideoTrack(JNIEnv *jni,
                                                                                   jobject j_local_media,
                                                                                   jlong local_media_handle,
                                                                                   jstring track_id) {
    twilio::media::LocalMedia* local_media = getLocalMedia(local_media_handle);

    return local_media->removeVideoTrack(webrtc_jni::JavaToStdString(jni, track_id));
}

JNIEXPORT void JNICALL Java_com_twilio_video_LocalMedia_nativeRelease(JNIEnv *jni,
                                                                      jobject j_local_media,
                                                                      jlong local_media_handle) {
    LocalMediaContext* local_media_context =
            reinterpret_cast<LocalMediaContext *>(local_media_handle);

    delete local_media_context;
}

}