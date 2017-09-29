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

#ifndef VIDEO_ANDROID_ANDROID_REMOTE_DATA_TRACK_OBSERVER_H_
#define VIDEO_ANDROID_ANDROID_REMOTE_DATA_TRACK_OBSERVER_H_

#include <jni.h>
#include "media/track.h"
#include "media/track_observer.h"
#include "webrtc/sdk/android/src/jni/jni_helpers.h"

namespace twilio_video_jni {

class AndroidRemoteDataTrackObserver : public twilio::media::RemoteDataTrackObserver {
public:
    AndroidRemoteDataTrackObserver(JNIEnv *env,
                                   jobject j_remote_data_track,
                                   jobject j_remote_data_track_listener);

    virtual ~AndroidRemoteDataTrackObserver();

    void setObserverDeleted();

protected:
    virtual void onMessage(twilio::media::RemoteDataTrack *track,
                           const std::string &message);

    virtual void onMessage(twilio::media::RemoteDataTrack *track,
                           const uint8_t *message,
                           size_t size);

private:
    JNIEnv *jni() {
        return webrtc_jni::AttachCurrentThreadIfNeeded();
    }

    bool isObserverValid(const std::string &callback_name);

    bool observer_deleted_ = false;
    mutable rtc::CriticalSection deletion_lock_;

    const webrtc_jni::ScopedGlobalRef<jobject> j_remote_data_track_;
    const webrtc_jni::ScopedGlobalRef<jobject> j_remote_data_track_listener_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_remote_data_track_listener_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_byte_buffer_class_;

    jmethodID j_on_string_message_;
    jmethodID j_on_buffer_message_;
    jmethodID j_byte_buffer_wrap_id_;
};

}

#endif // VIDEO_ANDROID_ANDROID_REMOTE_DATA_TRACK_OBSERVER_H_
