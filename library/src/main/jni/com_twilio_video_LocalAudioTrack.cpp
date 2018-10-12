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

#include "com_twilio_video_LocalAudioTrack.h"
#include "webrtc/sdk/android/src/jni/jni_helpers.h"
#include "class_reference_holder.h"
#include "jni_utils.h"
#include "webrtc/modules/utility/include/helpers_android.h"

namespace twilio_video_jni {

static const char *const kLocalAudioTrackConstructorSignature = "("
        "J"
        "Ljava/lang/String;"
        "Ljava/lang/String;"
        "Z"
        "Landroid/content/Context;"
        ")V";

std::shared_ptr<twilio::media::LocalAudioTrack> getLocalAudioTrack(jlong local_audio_track_handle) {
    LocalAudioTrackContext* local_audio_track_context =
            reinterpret_cast<LocalAudioTrackContext *>(local_audio_track_handle);

    return local_audio_track_context->getLocalAudioTrack();
}

std::string getLocalAudioTrackHash(std::shared_ptr<twilio::media::LocalAudioTrack> local_audio_track) {
    std::size_t local_audio_track_hash =
            std::hash<twilio::media::LocalAudioTrack *>{}(local_audio_track.get());

    return std::to_string(local_audio_track_hash);
}

jobject createJavaLocalAudioTrack(jobject j_context,
                                  std::shared_ptr<twilio::media::LocalAudioTrack> local_audio_track) {
    JNIEnv *jni = webrtc::jni::GetEnv();
    jclass j_local_audio_track_class = twilio_video_jni::FindClass(jni,
                                                                   "com/twilio/video/LocalAudioTrack");
    jmethodID j_local_audio_track_ctor_id = webrtc::GetMethodID(jni,
                                                                j_local_audio_track_class,
                                                                "<init>",
                                                                kLocalAudioTrackConstructorSignature);
    LocalAudioTrackContext* local_audio_track_context =
            new LocalAudioTrackContext(local_audio_track);
    jstring j_name = JavaUTF16StringFromStdString(jni, local_audio_track->getName());
    jstring j_track_hash = JavaUTF16StringFromStdString(jni,
                                                        getLocalAudioTrackHash(local_audio_track));
    jobject j_local_audio_track = jni->NewObject(j_local_audio_track_class,
                                                 j_local_audio_track_ctor_id,
                                                 webrtc::NativeToJavaPointer(local_audio_track_context),
                                                 j_track_hash,
                                                 j_name,
                                                 local_audio_track->isEnabled(),
                                                 j_context);
    CHECK_EXCEPTION(jni) << "Failed to create LocalAudioTrack instance";

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
