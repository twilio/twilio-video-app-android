#include "com_twilio_video_LocalVideoTrack.h"
#include "webrtc/sdk/android/src/jni/jni_helpers.h"
#include "webrtc/sdk/android/src/jni/classreferenceholder.h"
#include "class_reference_holder.h"

namespace twilio_video_jni {

std::shared_ptr<twilio::media::LocalVideoTrack> getLocalVideoTrack(jlong local_video_track_handle) {
    LocalVideoTrackContext* video_track_context =
            reinterpret_cast<LocalVideoTrackContext *>(local_video_track_handle);

    return video_track_context->getLocalVideoTrack();
}

jobject createJavaLocalVideoTrack(std::shared_ptr<twilio::media::LocalVideoTrack> local_video_track,
                                  jboolean j_enabled,
                                  jobject j_video_capturer,
                                  jobject j_video_constraints,
                                  jobject j_media_factory) {
    JNIEnv *jni = webrtc_jni::GetEnv();
    jclass j_video_track_class = twilio_video_jni::FindClass(jni,
                                                             "com/twilio/video/LocalVideoTrack");
    jclass j_webrtc_video_track_class = webrtc_jni::FindClass(jni, "org/webrtc/VideoTrack");
    jmethodID j_webrtc_video_track_ctor_id = webrtc_jni::GetMethodID(jni,
                                                                     j_webrtc_video_track_class,
                                                                     "<init>",
                                                                     "(J)V");
    jmethodID j_video_track_ctor_id = webrtc_jni::GetMethodID(jni,
                                                              j_video_track_class,
                                                              "<init>",
                                                              "(JZLcom/twilio/video/VideoCapturer;Lcom/twilio/video/VideoConstraints;Lorg/webrtc/VideoTrack;Lcom/twilio/video/MediaFactory;)V");
    LocalVideoTrackContext* video_track_context =
            new LocalVideoTrackContext(local_video_track);
    jobject j_webrtc_video_track = jni->NewObject(j_webrtc_video_track_class,
                                                  j_webrtc_video_track_ctor_id,
                                                  webrtc_jni::jlongFromPointer(local_video_track->getWebRtcTrack()));
    CHECK_EXCEPTION(jni) << "Error creating org.webrtc.VideoTrack";
    jobject j_local_video_track = jni->NewObject(j_video_track_class,
                                                 j_video_track_ctor_id,
                                                 webrtc_jni::jlongFromPointer(video_track_context),
                                                 j_enabled,
                                                 j_video_capturer,
                                                 j_video_constraints,
                                                 j_webrtc_video_track,
                                                 j_media_factory);
    CHECK_EXCEPTION(jni) << "Error creating LocalVideoTrack";

    return j_local_video_track;
}

JNIEXPORT jboolean JNICALL Java_com_twilio_video_LocalVideoTrack_nativeIsEnabled(JNIEnv *jni,
                                                                                 jobject j_local_video_track,
                                                                                 jlong j_local_video_track_handle) {
    std::shared_ptr<twilio::media::LocalVideoTrack> local_video_track =
            getLocalVideoTrack(j_local_video_track_handle);

    return local_video_track->isEnabled();
}
JNIEXPORT void JNICALL Java_com_twilio_video_LocalVideoTrack_nativeEnable(JNIEnv *jni,
                                                                          jobject j_local_video_track,
                                                                          jlong j_local_video_track_handle,
                                                                          jboolean enabled) {
    std::shared_ptr<twilio::media::LocalVideoTrack> local_video_track =
            getLocalVideoTrack(j_local_video_track_handle);

    local_video_track->setEnabled(enabled);
}
JNIEXPORT void JNICALL Java_com_twilio_video_LocalVideoTrack_nativeRelease(JNIEnv *jni,
                                                                           jobject j_local_video_track,
                                                                           jlong local_video_track_handle) {
    LocalVideoTrackContext* local_video_track_context =
            reinterpret_cast<LocalVideoTrackContext *>(local_video_track_handle);

    delete local_video_track_context;
}

}
