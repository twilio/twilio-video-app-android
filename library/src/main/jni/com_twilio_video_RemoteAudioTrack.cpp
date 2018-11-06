/*
 * Copyright (C) 2018 Twilio, Inc.
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

#include "com_twilio_video_RemoteAudioTrack.h"
#include "class_reference_holder.h"
#include "jni_utils.h"
#include "webrtc/sdk/android/src/jni/jni_helpers.h"

namespace twilio_video_jni {

jobject
createJavaRemoteAudioTrack(JNIEnv *env,
                           std::shared_ptr<twilio::media::RemoteAudioTrack> remote_audio_track,
                           jclass j_remote_audio_track_class,
                           jmethodID j_remote_audio_track_ctor_id) {
    RemoteAudioTrackContext* remote_audio_track_context =
            new RemoteAudioTrackContext(remote_audio_track);
    jstring j_sid = JavaUTF16StringFromStdString(env, remote_audio_track->getSid());
    jstring j_name = JavaUTF16StringFromStdString(env, remote_audio_track->getName());
    jboolean j_is_enabled = (jboolean) remote_audio_track->isEnabled();
    jobject j_remote_audio_track = env->NewObject(j_remote_audio_track_class,
                                                  j_remote_audio_track_ctor_id,
                                                  webrtc::NativeToJavaPointer(remote_audio_track_context),
                                                  j_sid,
                                                  j_name,
                                                  j_is_enabled);
    CHECK_EXCEPTION(env) << "Failed to create RemoteAudioTrack";

    return j_remote_audio_track;
}

JNIEXPORT void JNICALL
Java_com_twilio_video_RemoteAudioTrack_nativeEnablePlayback(JNIEnv *env,
                                                            jobject instance,
                                                            jlong native_remote_audio_track_handle,
                                                            jboolean j_enable) {
    RemoteAudioTrackContext* remote_audio_track_context =
            reinterpret_cast<RemoteAudioTrackContext *>(native_remote_audio_track_handle);

    remote_audio_track_context->getRemoteAudioTrack()->getWebRtcTrack()->set_enabled(j_enable);
}

JNIEXPORT void JNICALL
Java_com_twilio_video_RemoteAudioTrack_nativeRelease(JNIEnv *env,
                                                     jobject instance,
                                                     jlong native_remote_audio_track_handle) {
    RemoteAudioTrackContext* remote_audio_track_context =
            reinterpret_cast<RemoteAudioTrackContext *>(native_remote_audio_track_handle);

    delete remote_audio_track_context;
}

}

