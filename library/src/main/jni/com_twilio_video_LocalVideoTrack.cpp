/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "com_twilio_video_LocalVideoTrack.h"
#include "webrtc/sdk/android/src/jni/jni_helpers.h"
#include "webrtc/sdk/android/src/jni/classreferenceholder.h"
#include "class_reference_holder.h"
#include "jni_utils.h"
#include "webrtc/modules/utility/include/helpers_android.h"

namespace twilio_video_jni {

static const char *const kLocalVideoTrackConstructorSignature = "("
        "J"
        "Z"
        "Lcom/twilio/video/VideoCapturer;"
        "Lcom/twilio/video/VideoConstraints;"
        "Lorg/webrtc/VideoTrack;"
        "Ljava/lang/String;"
        "Ljava/lang/String;"
        "Landroid/content/Context;"
        ")V";

std::shared_ptr<twilio::media::LocalVideoTrack> getLocalVideoTrack(jlong local_video_track_handle) {
    LocalVideoTrackContext* video_track_context =
            reinterpret_cast<LocalVideoTrackContext *>(local_video_track_handle);

    return video_track_context->getLocalVideoTrack();
}

std::string getLocalVideoTrackHash(std::shared_ptr<twilio::media::LocalVideoTrack> local_video_track) {
    std::size_t local_video_track_hash =
            std::hash<twilio::media::LocalVideoTrack *>{}(local_video_track.get());

    return std::to_string(local_video_track_hash);
}

jobject createJavaLocalVideoTrack(std::shared_ptr<twilio::media::LocalVideoTrack> local_video_track,
                                  jobject j_video_capturer,
                                  jobject j_video_constraints,
                                  jobject j_context) {
    JNIEnv *jni = webrtc::jni::GetEnv();
    jclass j_video_track_class = twilio_video_jni::FindClass(jni,
                                                             "com/twilio/video/LocalVideoTrack");
    jclass j_webrtc_video_track_class = webrtc::FindClass(jni, "org/webrtc/VideoTrack");
    jmethodID j_webrtc_video_track_ctor_id = webrtc::GetMethodID(jni,
                                                                 j_webrtc_video_track_class,
                                                                 "<init>",
                                                                 "(J)V");
    jmethodID j_video_track_ctor_id = webrtc::GetMethodID(jni,
                                                          j_video_track_class,
                                                          "<init>",
                                                          kLocalVideoTrackConstructorSignature);
    LocalVideoTrackContext* video_track_context =
            new LocalVideoTrackContext(local_video_track);
    jobject j_webrtc_video_track = jni->NewObject(j_webrtc_video_track_class,
                                                  j_webrtc_video_track_ctor_id,
                                                  webrtc::NativeToJavaPointer(local_video_track->getWebRtcTrack()));
    CHECK_EXCEPTION(jni) << "Error creating org.webrtc.VideoTrack";
    jstring j_name = JavaUTF16StringFromStdString(jni, local_video_track->getName());
    jstring j_track_hash = JavaUTF16StringFromStdString(jni,
                                                        getLocalVideoTrackHash(local_video_track));
    jobject j_local_video_track = jni->NewObject(j_video_track_class,
                                                 j_video_track_ctor_id,
                                                 webrtc::NativeToJavaPointer(video_track_context),
                                                 local_video_track->isEnabled(),
                                                 j_video_capturer,
                                                 j_video_constraints,
                                                 j_webrtc_video_track,
                                                 j_track_hash,
                                                 j_name,
                                                 j_context);
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

/*
 * Stub video sink interface used for testing capturers.
 */
class StubVideoSink : public rtc::VideoSinkInterface<webrtc::VideoFrame> {
    void OnFrame(const webrtc::VideoFrame &frame) override {}
};

// Use only for testing
JNIEXPORT jlong JNICALL Java_com_twilio_video_LocalVideoTrack_nativeAddRendererWithWants(JNIEnv *jni,
                                                                                         jobject j_local_video_track,
                                                                                         jlong j_local_video_track_handle,
                                                                                         jboolean j_rotation_applied) {
    std::shared_ptr<twilio::media::LocalVideoTrack> local_video_track =
            getLocalVideoTrack(j_local_video_track_handle);
    StubVideoSink *video_sink = new StubVideoSink();
    rtc::VideoSinkWants video_sink_wants;
    video_sink_wants.rotation_applied = j_rotation_applied;
    local_video_track->getWebRtcTrack()->AddOrUpdateSink(video_sink, video_sink_wants);

    return webrtc::NativeToJavaPointer(video_sink);
}

// Use only for testing
JNIEXPORT void JNICALL Java_com_twilio_video_LocalVideoTrack_nativeRemoveRendererWithWants(JNIEnv *jni,
                                                                                           jobject j_local_video_track,
                                                                                           jlong j_local_video_track_handle,
                                                                                           jlong j_native_video_sink_handle) {
    std::shared_ptr<twilio::media::LocalVideoTrack> local_video_track =
            getLocalVideoTrack(j_local_video_track_handle);
    StubVideoSink *video_sink = reinterpret_cast<StubVideoSink *>(j_native_video_sink_handle);

    // Remove and delete sink
    local_video_track->getWebRtcTrack()->RemoveSink(video_sink);
    delete video_sink;
}

JNIEXPORT void JNICALL Java_com_twilio_video_LocalVideoTrack_nativeRelease(JNIEnv *jni,
                                                                           jobject j_local_video_track,
                                                                           jlong local_video_track_handle) {
    LocalVideoTrackContext* local_video_track_context =
            reinterpret_cast<LocalVideoTrackContext *>(local_video_track_handle);

    delete local_video_track_context;
}

}
