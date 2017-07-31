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

#ifndef VIDEO_ANDROID_INCLUDE_ANDROID_LOCAL_PARTICIPANT_OBSERVER_H_
#define VIDEO_ANDROID_INCLUDE_ANDROID_LOCAL_PARTICIPANT_OBSERVER_H_

#include "webrtc/sdk/android/src/jni/jni_helpers.h"

#include "video/local_participant_observer.h"

namespace twilio_video_jni {

class AndroidLocalParticipantObserver : public twilio::video::LocalParticipantObserver {
public:
    AndroidLocalParticipantObserver(JNIEnv *env,
                                    jobject j_local_participant,
                                    jobject j_local_participant_observer);

    virtual ~AndroidLocalParticipantObserver();

    void setObserverDeleted();


protected:

    virtual void onAudioTrackPublished(twilio::video::LocalParticipant *local_participant,
                                       std::shared_ptr<twilio::media::PublishedAudioTrack> track);

    virtual void onVideoTrackPublished(twilio::video::LocalParticipant *local_participant,
                                       std::shared_ptr<twilio::media::PublishedVideoTrack> track);

private:
    JNIEnv *jni() {
        return webrtc_jni::AttachCurrentThreadIfNeeded();
    }

    bool isObserverValid(const std::string &callback_name);

    bool observer_deleted_ = false;
    mutable rtc::CriticalSection deletion_lock_;

    const webrtc_jni::ScopedGlobalRef<jobject> j_local_participant_;
    const webrtc_jni::ScopedGlobalRef<jobject> j_local_participant_observer_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_local_participant_observer_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_published_audio_track_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_published_video_track_class_;

    jmethodID j_on_published_audio_track_;
    jmethodID j_on_published_video_track_;
    jmethodID j_published_audio_track_ctor_id_;
    jmethodID j_published_video_track_ctor_id_;
};

}

#endif // VIDEO_ANDROID_INCLUDE_ANDROID_LOCAL_PARTICIPANT_OBSERVER_H_
