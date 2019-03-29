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

#include "audio_sink_adapter.h"
#include "logging.h"
#include "class_reference_holder.h"
#include "jni_utils.h"
#include "webrtc/modules/utility/include/helpers_android.h"

namespace twilio_video_jni {

AudioSinkAdapter::AudioSinkAdapter(JNIEnv *env, jobject j_audio_sink) :
        j_audio_sink_(env, webrtc::JavaParamRef<jobject>(j_audio_sink)),
        j_audio_sink_class_(env, webrtc::JavaParamRef<jclass>(GetObjectClass(env, j_audio_sink_.obj()))),
        j_byte_buffer_class_(env, webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env, "java/nio/ByteBuffer"))),
        j_render_sample_id_(webrtc::GetMethodID(env,
                                                j_audio_sink_class_.obj(),
                                                "renderSample",
                                                "(Ljava/nio/ByteBuffer;III)V")),
        j_byte_buffer_wrap_id_(webrtc::GetStaticMethodID(env,
                                                         j_byte_buffer_class_.obj(),
                                                         "wrap",
                                                         "([B)Ljava/nio/ByteBuffer;")){
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kDebug,
                      "AudioSinkAdapter");
}

AudioSinkAdapter::~AudioSinkAdapter() {
    setObserverDeleted();
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kDebug,
                      "~AudioSinkAdapter");
}

void AudioSinkAdapter::OnData(const void *audio_data,
                              int bits_per_sample,
                              int sample_rate,
                              size_t number_of_channels,
                              size_t number_of_frames) {
    webrtc::jni::ScopedLocalRefFrame local_ref_frame(jni());
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kDebug,
                      "%s", func_name.c_str());

    {
        rtc::CritScope cs(&deletion_lock_);

        if (!isObserverValid(func_name)) {
            return;
        }

        // Convert audio_data to ByteBuffer
        size_t bytes_per_frame = (bits_per_sample / 8) * number_of_channels;
        size_t block_length = bytes_per_frame * number_of_frames;
        jbyteArray audio_sample = jni()->NewByteArray((jsize) block_length);
        jni()->SetByteArrayRegion(audio_sample,
                                  0,
                                  (jsize) block_length,
                                  reinterpret_cast<const jbyte*>(audio_data));
        jobject j_audio_sample = jni()->CallStaticObjectMethod(j_byte_buffer_class_.obj(),
                                                               j_byte_buffer_wrap_id_,
                                                               audio_sample);
        // Invoke callback
        jni()->CallVoidMethod(j_audio_sink_.obj(),
                              j_render_sample_id_,
                              j_audio_sample,
                              2,
                              sample_rate,
                              number_of_channels);
        CHECK_EXCEPTION(jni()) << "Error calling renderSample";
    }
}

bool AudioSinkAdapter::isObserverValid(const std::string &callback_name) {
    if (observer_deleted_) {
        VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                          twilio::video::LogLevel::kWarning,
                          "audio sink adapter is marked for deletion, skipping %s callback",
                          callback_name.c_str());
        return false;
    };
    if (webrtc::IsNull(jni(), j_audio_sink_)) {
        VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                          twilio::video::LogLevel::kWarning,
                          "audio sink adapter reference has been destroyed, skipping %s callback",
                          callback_name.c_str());
        return false;
    }
    return true;
}

void AudioSinkAdapter::setObserverDeleted() {
    rtc::CritScope cs(&deletion_lock_);
    observer_deleted_ = true;
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kDebug,
                      "audio sink adapter deleted");
}

}
