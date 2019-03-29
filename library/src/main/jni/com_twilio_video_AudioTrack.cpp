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

#include <algorithm>

#include "com_twilio_video_AudioTrack.h"
#include "audio_sink_adapter.h"
#include "logging.h"

namespace twilio_video_jni {

AudioTrackContext::~AudioTrackContext() {
    JNIEnv *jni = webrtc::AttachCurrentThreadIfNeeded();
    webrtc::AudioTrackInterface* webrtc_audio_track;

    // Clean up all audio sinks
    if ((webrtc_audio_track = audio_track_->getWebRtcTrack()) != nullptr) {
        for (auto it = audio_sink_map_.begin() ;
             it != audio_sink_map_.end() ; it++) {
            auto audio_sink = std::move(it->second);

            // Mark sink as deleted and remove
            audio_sink->setObserverDeleted();
            webrtc_audio_track->RemoveSink(audio_sink.get());

            // Delete the global reference to the Java AudioSink
            webrtc::jni::DeleteGlobalRef(jni, it->first);
        }
        audio_sink_map_.clear();
    }
}

void AudioTrackContext::addSink(JNIEnv *env, jobject j_audio_sink) {
    auto it = std::find_if(audio_sink_map_.begin(), audio_sink_map_.end(), [&](const auto& it){
        return env->IsSameObject(it.first, j_audio_sink);
    });
    if(it != audio_sink_map_.end()){
        VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                          twilio::video::LogLevel::kInfo,
                          "Trying to add duplicate Audio Sink. Skipping.");
       return;
    }
    // Create a global reference to Java AudioSink so the AudioSink can be removed later
    jobject j_global_audio_sink = webrtc::jni::NewGlobalRef(env, j_audio_sink);
    std::unique_ptr<AudioSinkAdapter> audio_sink(new AudioSinkAdapter(env, j_global_audio_sink));
    audio_track_->getWebRtcTrack()->AddSink(audio_sink.get());

    // Capture the global reference in the map
    audio_sink_map_[j_global_audio_sink] = std::move(audio_sink);
}

void AudioTrackContext::removeSink(jobject j_audio_sink) {
    JNIEnv *jni = webrtc::AttachCurrentThreadIfNeeded();

    /*
     * Lookup the Java AudioSink using IsSameObject because the jobject in the map is a global
     * reference.
     */
    auto it = std::find_if(audio_sink_map_.begin(), audio_sink_map_.end(), [&](const auto& it){
        return jni->IsSameObject(it.first, j_audio_sink);
    });

    if(it == audio_sink_map_.end()){
        VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                          twilio::video::LogLevel::kInfo,
                          "Removed audio sink that was not in collection");
        return;
    }

    std::unique_ptr<AudioSinkAdapter> audio_sink = std::move(it->second);

    // Mark the observer as deleted so no frames are incorrectly received after calling removeSink
    audio_sink->setObserverDeleted();

    webrtc::AudioTrackInterface* webrtc_audio_track;
    if ((webrtc_audio_track = audio_track_->getWebRtcTrack()) != nullptr) {
        webrtc_audio_track->RemoveSink(audio_sink.get());
    }
    audio_sink_map_.erase(it);
    webrtc::jni::DeleteGlobalRef(jni, it->first);
}

JNIEXPORT void JNICALL
Java_com_twilio_video_AudioTrack_nativeAddSink(JNIEnv *env,
                                               jobject instance,
                                               jlong native_audio_track_handle,
                                               jobject j_audio_sink) {
    AudioTrackContext* audio_track_context =
            reinterpret_cast<AudioTrackContext *>(native_audio_track_handle);

    audio_track_context->addSink(env, j_audio_sink);
}

JNIEXPORT void JNICALL
Java_com_twilio_video_AudioTrack_nativeRemoveSink(JNIEnv *env,
                                                  jobject instance,
                                                  jlong native_audio_track_handle,
                                                  jobject j_audio_sink) {
    AudioTrackContext* audio_track_context =
            reinterpret_cast<AudioTrackContext *>(native_audio_track_handle);

    audio_track_context->removeSink(j_audio_sink);
}
}
