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

#ifndef VIDEO_ANDROID_COM_TWILIO_VIDEO_REMOTEAUDIOTRACK_H_
#define VIDEO_ANDROID_COM_TWILIO_VIDEO_REMOTEAUDIOTRACK_H_

#include <jni.h>
#include "twilio/media/track.h"
#include "twilio/media/data_track.h"
#include "com_twilio_video_AudioTrack.h"

#ifdef __cplusplus
extern "C" {
#endif

namespace twilio_video_jni {

static const char *const kRemoteAudioTrackConstructorSignature = "("
        "J"
        "Ljava/lang/String;"
        "Ljava/lang/String;"
        "Z"
        ")V";

class RemoteAudioTrackContext : public AudioTrackContext {
public:
    RemoteAudioTrackContext(std::shared_ptr<twilio::media::RemoteAudioTrack> remote_audio_track)
            : AudioTrackContext(remote_audio_track), remote_audio_track_(remote_audio_track) { }

    virtual ~RemoteAudioTrackContext() = default;

    std::shared_ptr<twilio::media::RemoteAudioTrack> getRemoteAudioTrack() {
        return remote_audio_track_;
    }
private:
    std::shared_ptr<twilio::media::RemoteAudioTrack> remote_audio_track_;
};

jobject
createJavaRemoteAudioTrack(JNIEnv *env,
                           std::shared_ptr<twilio::media::RemoteAudioTrack> remote_audio_track,
                           jclass j_remote_audio_track_class,
                           jmethodID j_remote_audio_track_ctor_id);

JNIEXPORT void JNICALL
Java_com_twilio_video_RemoteAudioTrack_nativeEnablePlayback(JNIEnv *env,
                                                            jobject instance,
                                                            jlong native_remote_audio_track_handle,
                                                            jboolean j_enable);

JNIEXPORT void JNICALL
Java_com_twilio_video_RemoteAudioTrack_nativeRelease(JNIEnv *env,
                                                     jobject instance,
                                                     jlong native_remote_audio_track_handle);

}

#ifdef __cplusplus
}
#endif

#endif // VIDEO_ANDROID_COM_TWILIO_VIDEO_REMOTEAUDIOTRACK_H_
