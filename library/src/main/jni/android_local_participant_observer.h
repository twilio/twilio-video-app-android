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

#include "twilio/media/track.h"
#include "twilio/media/data_track.h"
#include "twilio/video/local_participant_observer.h"

namespace twilio_video_jni {

class AndroidLocalParticipantObserver : public twilio::video::LocalParticipantObserver {
public:
    AndroidLocalParticipantObserver(JNIEnv *env,
                                    jobject j_local_participant,
                                    jobject j_local_participant_observer,
                                    std::map<std::string, jobject>& local_audio_track_map,
                                    std::map<std::string, jobject>& local_video_track_map,
                                    std::map<std::string, jobject>& local_data_track_map);

    virtual ~AndroidLocalParticipantObserver();

    void setObserverDeleted();


protected:

    virtual void onAudioTrackPublished(twilio::video::LocalParticipant *local_participant,
                                       std::shared_ptr<twilio::media::LocalAudioTrackPublication> local_audio_track_publication);

    virtual void onAudioTrackPublicationFailed(twilio::video::LocalParticipant *participant,
                                               std::shared_ptr<twilio::media::LocalAudioTrack> audio_track,
                                               const twilio::video::Error twilio_error);

    virtual void onVideoTrackPublished(twilio::video::LocalParticipant *local_participant,
                                       std::shared_ptr<twilio::media::LocalVideoTrackPublication> local_video_track_publication);

    virtual void onVideoTrackPublicationFailed(twilio::video::LocalParticipant *participant,
                                               std::shared_ptr<twilio::media::LocalVideoTrack> video_track,
                                               const twilio::video::Error twilio_error);

    virtual void onDataTrackPublished(twilio::video::LocalParticipant *local_participant,
                                      std::shared_ptr<twilio::media::LocalDataTrackPublication> local_data_track_publication);

    virtual void onDataTrackPublicationFailed(twilio::video::LocalParticipant *participant,
                                              std::shared_ptr<twilio::media::LocalDataTrack> data_track,
                                              const twilio::video::Error twilio_error);

    virtual void onNetworkQualityLevelChanged(twilio::video::LocalParticipant *participant, twilio::video::NetworkQualityLevel level);
private:
    JNIEnv *jni() {
        return webrtc::jni::AttachCurrentThreadIfNeeded();
    }

    bool isObserverValid(const std::string &callback_name);

    bool observer_deleted_ = false;
    mutable rtc::CriticalSection deletion_lock_;

    const webrtc::ScopedJavaGlobalRef<jobject> j_local_participant_;
    const webrtc::ScopedJavaGlobalRef<jobject> j_local_participant_observer_;
    std::map<std::string, jobject>& local_audio_track_map_;
    std::map<std::string, jobject>& local_video_track_map_;
    std::map<std::string, jobject>& local_data_track_map_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_local_participant_observer_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_published_audio_track_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_published_video_track_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_published_data_track_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_twilio_exception_class_;

    jmethodID j_on_published_audio_track_;
    jmethodID j_on_audio_track_publication_failed_;
    jmethodID j_on_published_video_track_;
    jmethodID j_on_video_track_publication_failed_;
    jmethodID j_on_published_data_track_;
    jmethodID j_on_data_track_publication_failed_;
    jmethodID j_on_network_quality_level_changed_;
    jmethodID j_published_audio_track_ctor_id_;
    jmethodID j_published_video_track_ctor_id_;
    jmethodID j_published_data_track_ctor_id_;
    jmethodID j_twilio_exception_ctor_id_;
};

}

#endif // VIDEO_ANDROID_INCLUDE_ANDROID_LOCAL_PARTICIPANT_OBSERVER_H_
