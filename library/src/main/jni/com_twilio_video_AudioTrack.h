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

#ifndef VIDEO_ANDROID_COM_TWILIO_VIDEO_AUDIOTRACK_H_
#define VIDEO_ANDROID_COM_TWILIO_VIDEO_AUDIOTRACK_H_

#include <jni.h>
#include "twilio/media/track.h"
#include "audio_sink_adapter.h"

namespace twilio_video_jni {

class AudioTrackContext {
public:
    AudioTrackContext(std::shared_ptr<twilio::media::AudioTrack> audio_track)
    : audio_track_(audio_track) { }

    virtual ~AudioTrackContext();

    void addSink(JNIEnv *env, jobject j_audio_sink);
    void removeSink(jobject j_audio_sink);
private:
    std::shared_ptr<twilio::media::AudioTrack> audio_track_;
    std::map<jobject, std::unique_ptr<AudioSinkAdapter>> audio_sink_map_;
};

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL
Java_com_twilio_video_AudioTrack_nativeAddSink(JNIEnv *env,
                                               jobject instance,
                                               jlong native_audio_track_handle,
                                               jobject j_audio_sink);
JNIEXPORT void JNICALL
Java_com_twilio_video_AudioTrack_nativeRemoveSink(JNIEnv *env,
                                                  jobject instance,
                                                  jlong native_audio_track_handle,
                                                  jobject j_audio_sink);
#ifdef __cplusplus
}
#endif

}

#endif // VIDEO_ANDROID_COM_TWILIO_VIDEO_AUDIOTRACK_H_
