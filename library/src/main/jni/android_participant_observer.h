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

#ifndef VIDEO_ANDROID_INCLUDE_ANDROID_PARTICIPANT_OBSERVER_H_
#define VIDEO_ANDROID_INCLUDE_ANDROID_PARTICIPANT_OBSERVER_H_

#include "webrtc/sdk/android/src/jni/jni_helpers.h"

#include "twilio/media/track.h"
#include "twilio/video/video.h"
#include "twilio/video/remote_participant_observer.h"

namespace twilio_video_jni {

class AndroidParticipantObserver : public twilio::video::RemoteParticipantObserver {
public:
    AndroidParticipantObserver(JNIEnv *env,
                               jobject j_remote_participant,
                               jobject j_remote_participant_observer,
                               std::map<std::shared_ptr<twilio::media::RemoteAudioTrackPublication>, jobject>& remote_audio_track_publication_map,
                               std::map<std::shared_ptr<twilio::media::RemoteAudioTrack>, jobject>& remote_audio_track_map,
                               std::map<std::shared_ptr<twilio::media::RemoteVideoTrackPublication>, jobject>& remote_video_track_publication_map,
                               std::map<std::shared_ptr<twilio::media::RemoteVideoTrack>, jobject>& remote_video_track_map,
                               std::map<std::shared_ptr<twilio::media::RemoteDataTrackPublication>, jobject>& remote_data_track_publication_map,
                               std::map<std::shared_ptr<twilio::media::RemoteDataTrack>, jobject>& remote_data_track_map);

    ~AndroidParticipantObserver();

    void setObserverDeleted();

protected:
    virtual void onAudioTrackPublished(twilio::video::RemoteParticipant *remote_participant,
                                       std::shared_ptr<twilio::media::RemoteAudioTrackPublication> remote_audio_track_publication);

    virtual void onAudioTrackUnpublished(twilio::video::RemoteParticipant *remote_participant,
                                         std::shared_ptr<twilio::media::RemoteAudioTrackPublication> remote_audio_track_publication);

    virtual void onVideoTrackPublished(twilio::video::RemoteParticipant *remote_participant,
                                       std::shared_ptr<twilio::media::RemoteVideoTrackPublication> remote_video_track_publication);

    virtual void onVideoTrackUnpublished(twilio::video::RemoteParticipant *remote_participant,
                                         std::shared_ptr<twilio::media::RemoteVideoTrackPublication> remote_video_track_publication);

    virtual void onDataTrackPublished(twilio::video::RemoteParticipant *remote_participant,
                                      std::shared_ptr<twilio::media::RemoteDataTrackPublication> remote_data_track_publication);

    virtual void onDataTrackUnpublished(twilio::video::RemoteParticipant *remote_participant,
                                        std::shared_ptr<twilio::media::RemoteDataTrackPublication> remote_data_track_publication);

    virtual void onAudioTrackEnabled(twilio::video::RemoteParticipant *remote_participant,
                                     std::shared_ptr<twilio::media::RemoteAudioTrackPublication> remote_audio_track_publication);

    virtual void onAudioTrackDisabled(twilio::video::RemoteParticipant *remote_participant,
                                      std::shared_ptr<twilio::media::RemoteAudioTrackPublication> remote_audio_track_publication);

    virtual void onVideoTrackEnabled(twilio::video::RemoteParticipant *remote_participant,
                                     std::shared_ptr<twilio::media::RemoteVideoTrackPublication> remote_video_track_publication);

    virtual void onVideoTrackDisabled(twilio::video::RemoteParticipant *remote_participant,
                                      std::shared_ptr<twilio::media::RemoteVideoTrackPublication> remote_video_track_publication);

    virtual void onAudioTrackSubscribed(twilio::video::RemoteParticipant *participant,
                                        std::shared_ptr<twilio::media::RemoteAudioTrackPublication> remote_audio_track_publication,
                                        std::shared_ptr<twilio::media::RemoteAudioTrack> remote_audio_track);

    virtual void onAudioTrackSubscriptionFailed(twilio::video::RemoteParticipant *participant,
                                                std::shared_ptr<twilio::media::RemoteAudioTrackPublication> publication,
                                                const twilio::video::TwilioError twilio_error);

    virtual void onAudioTrackUnsubscribed(twilio::video::RemoteParticipant *participant,
                                          std::shared_ptr<twilio::media::RemoteAudioTrackPublication> remote_audio_track_publication,
                                          std::shared_ptr<twilio::media::RemoteAudioTrack> remote_audio_track);

    virtual void onVideoTrackSubscribed(twilio::video::RemoteParticipant *participant,
                                        std::shared_ptr<twilio::media::RemoteVideoTrackPublication> remote_video_track_publication,
                                        std::shared_ptr<twilio::media::RemoteVideoTrack> remote_video_track);

    virtual void onVideoTrackSubscriptionFailed(twilio::video::RemoteParticipant *participant,
                                                std::shared_ptr<twilio::media::RemoteVideoTrackPublication> publication,
                                                const twilio::video::TwilioError twilio_error);

    virtual void onVideoTrackUnsubscribed(twilio::video::RemoteParticipant *participant,
                                          std::shared_ptr<twilio::media::RemoteVideoTrackPublication> remote_video_track_publication,
                                          std::shared_ptr<twilio::media::RemoteVideoTrack> remote_video_track);

    virtual void onDataTrackSubscribed(twilio::video::RemoteParticipant *participant,
                                       std::shared_ptr<twilio::media::RemoteDataTrackPublication> remote_data_track_publication,
                                       std::shared_ptr<twilio::media::RemoteDataTrack> remote_data_track);

    virtual void onDataTrackSubscriptionFailed(twilio::video::RemoteParticipant *participant,
                                               std::shared_ptr<twilio::media::RemoteDataTrackPublication> publication,
                                               const twilio::video::TwilioError twilio_error);

    virtual void onDataTrackUnsubscribed(twilio::video::RemoteParticipant *participant,
                                         std::shared_ptr<twilio::media::RemoteDataTrackPublication> remote_data_track_publication,
                                         std::shared_ptr<twilio::media::RemoteDataTrack> remote_data_track);

private:
    JNIEnv *jni() {
        return webrtc::jni::AttachCurrentThreadIfNeeded();
    }

    bool isObserverValid(const std::string &callback_name);

    bool observer_deleted_ = false;
    mutable rtc::CriticalSection deletion_lock_;

    const webrtc::ScopedJavaGlobalRef<jobject> j_remote_participant_;
    const webrtc::ScopedJavaGlobalRef<jobject> j_remote_participant_observer_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_remote_participant_observer_class_;
    std::map<std::shared_ptr<twilio::media::RemoteAudioTrackPublication>, jobject>& remote_audio_track_publication_map_;
    std::map<std::shared_ptr<twilio::media::RemoteAudioTrack>, jobject>& remote_audio_track_map_;
    std::map<std::shared_ptr<twilio::media::RemoteVideoTrackPublication>, jobject>& remote_video_track_publication_map_;
    std::map<std::shared_ptr<twilio::media::RemoteVideoTrack>, jobject>& remote_video_track_map_;
    std::map<std::shared_ptr<twilio::media::RemoteDataTrackPublication>, jobject>& remote_data_track_publication_map_;
    std::map<std::shared_ptr<twilio::media::RemoteDataTrack>, jobject>& remote_data_track_map_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_remote_audio_track_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_remote_audio_track_publication_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_remote_video_track_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_remote_video_track_publication_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_remote_data_track_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_remote_data_track_publication_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_twilio_exception_class_;
    jmethodID j_on_audio_track_published_;
    jmethodID j_on_audio_track_unpublished_;
    jmethodID j_on_audio_track_subscribed_;
    jmethodID j_on_audio_track_subscription_failed_;
    jmethodID j_on_audio_track_unsubscribed_;
    jmethodID j_on_video_track_published_;
    jmethodID j_on_video_track_unpublished_;
    jmethodID j_on_video_track_subscribed_;
    jmethodID j_on_video_track_subscription_failed_;
    jmethodID j_on_video_track_unsubscribed_;
    jmethodID j_on_data_track_published_;
    jmethodID j_on_data_track_unpublished_;
    jmethodID j_on_data_track_subscribed_;
    jmethodID j_on_data_track_subscription_failed_;
    jmethodID j_on_data_track_unsubscribed_;
    jmethodID j_on_audio_track_enabled_;
    jmethodID j_on_audio_track_disabled_;
    jmethodID j_on_video_track_enabled_;
    jmethodID j_on_video_track_disabled_;
    jmethodID j_audio_track_ctor_id_;
    jmethodID j_audio_track_publication_ctor_id_;
    jmethodID j_video_track_ctor_id_;
    jmethodID j_video_track_publication_ctor_id_;
    jmethodID j_data_track_ctor_id_;
    jmethodID j_data_track_publication_ctor_id_;
    jmethodID j_twilio_exception_ctor_id_;
};

}

#endif // VIDEO_ANDROID_INCLUDE_ANDROID_PARTICIPANT_OBSERVER_H_
