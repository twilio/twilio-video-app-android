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

#ifndef VIDEO_ANDROID_AUDIO_SINK_ADAPTER_H_
#define VIDEO_ANDROID_AUDIO_SINK_ADAPTER_H_

#include <jni.h>
#include "webrtc/sdk/android/src/jni/jni_helpers.h"
#include "webrtc/api/mediastreaminterface.h"

namespace twilio_video_jni {


class AudioSinkAdapter : public webrtc::AudioTrackSinkInterface {
public:
    AudioSinkAdapter(JNIEnv *env, jobject j_audio_sink);
    ~AudioSinkAdapter();

    void OnData(const void *audio_data,
                int bits_per_sample,
                int sample_rate,
                size_t number_of_channels,
                size_t number_of_frames) override;

    void setObserverDeleted();

private:
    JNIEnv *jni() {
        return webrtc_jni::AttachCurrentThreadIfNeeded();
    }

    bool isObserverValid(const std::string &callback_name);

    bool observer_deleted_ = false;
    mutable rtc::CriticalSection deletion_lock_;

    const webrtc::ScopedJavaGlobalRef<jobject> j_audio_sink_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_audio_sink_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_byte_buffer_class_;
    jmethodID j_render_sample_id_;
    jmethodID j_byte_buffer_wrap_id_;
};
}

#endif // VIDEO_ANDROID_AUDIO_SINK_ADAPTER_H_
