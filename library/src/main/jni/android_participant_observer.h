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

#include "media/track.h"
#include "video/video.h"
#include "video/remote_participant_observer.h"

namespace twilio_video_jni {

class AndroidParticipantObserver : public twilio::video::RemoteParticipantObserver {
public:
    AndroidParticipantObserver(JNIEnv *env,
                               jobject j_remote_participant,
                               jobject j_remote_participant_observer,
                               std::map<std::shared_ptr<twilio::media::RemoteAudioTrack>, jobject>& remote_audio_track_map,
                               std::map<std::shared_ptr<twilio::media::RemoteVideoTrack>, jobject>& remote_video_track_map);

    ~AndroidParticipantObserver();

    void setObserverDeleted();

protected:
    virtual void onAudioTrackAdded(twilio::video::RemoteParticipant *remote_participant,
                                   std::shared_ptr<twilio::media::RemoteAudioTrack> remote_audio_track);

    virtual void onAudioTrackRemoved(twilio::video::RemoteParticipant *remote_participant,
                                     std::shared_ptr<twilio::media::RemoteAudioTrack> remote_audio_track);

    virtual void onVideoTrackAdded(twilio::video::RemoteParticipant *remote_participant,
                                   std::shared_ptr<twilio::media::RemoteVideoTrack> remote_video_track);

    virtual void onVideoTrackRemoved(twilio::video::RemoteParticipant *remote_participant,
                                     std::shared_ptr<twilio::media::RemoteVideoTrack> remote_video_track);

    virtual void onDataTrackAdded(twilio::video::RemoteParticipant *remote_participant,
                                  std::shared_ptr<twilio::media::DataTrack> track);

    virtual void onDataTrackRemoved(twilio::video::RemoteParticipant *remote_participant,
                                    std::shared_ptr<twilio::media::DataTrack> track);

    virtual void onAudioTrackEnabled(twilio::video::RemoteParticipant *remote_participant,
                                     std::shared_ptr<twilio::media::RemoteAudioTrack> remote_audio_track);

    virtual void onAudioTrackDisabled(twilio::video::RemoteParticipant *remote_participant,
                                      std::shared_ptr<twilio::media::RemoteAudioTrack> remote_audio_track);

    virtual void onVideoTrackEnabled(twilio::video::RemoteParticipant *remote_participant,
                                     std::shared_ptr<twilio::media::RemoteVideoTrack> remote_video_track);

    virtual void onVideoTrackDisabled(twilio::video::RemoteParticipant *remote_participant,
                                      std::shared_ptr<twilio::media::RemoteVideoTrack> remote_video_track);

    virtual void onAudioTrackSubscribed(twilio::video::RemoteParticipant *participant,
                                        std::shared_ptr<twilio::media::RemoteAudioTrack> remote_audio_track);

    virtual void onAudioTrackUnsubscribed(twilio::video::RemoteParticipant *participant,
                                          std::shared_ptr<twilio::media::RemoteAudioTrack> remote_audio_track);

    virtual void onVideoTrackSubscribed(twilio::video::RemoteParticipant *participant,
                                        std::shared_ptr<twilio::media::RemoteVideoTrack> remote_video_track);

    virtual void onVideoTrackUnsubscribed(twilio::video::RemoteParticipant *participant,
                                          std::shared_ptr<twilio::media::RemoteVideoTrack> remote_video_track);

private:
    JNIEnv *jni() {
        return webrtc_jni::AttachCurrentThreadIfNeeded();
    }

    bool isObserverValid(const std::string &callback_name);

    bool observer_deleted_ = false;
    mutable rtc::CriticalSection deletion_lock_;

    const webrtc_jni::ScopedGlobalRef<jobject> j_remote_participant_;
    const webrtc_jni::ScopedGlobalRef<jobject> j_remote_participant_observer_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_remote_participant_observer_class_;
    std::map<std::shared_ptr<twilio::media::RemoteAudioTrack>, jobject>& remote_audio_track_map_;
    std::map<std::shared_ptr<twilio::media::RemoteVideoTrack>, jobject>& remote_video_track_map_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_remote_audio_track_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_remote_video_track_class_;
    jmethodID j_on_audio_track_added_;
    jmethodID j_on_audio_track_removed_;
    jmethodID j_on_video_track_added_;
    jmethodID j_on_video_track_removed_;
    jmethodID j_on_audio_track_enabled_;
    jmethodID j_on_audio_track_disabled_;
    jmethodID j_on_video_track_enabled_;
    jmethodID j_on_video_track_disabled_;
    jmethodID j_audio_track_ctor_id_;
    jmethodID j_video_track_ctor_id_;
};

}

#endif //VIDEO_ANDROID_INCLUDE_ANDROID_PARTICIPANT_OBSERVER_H_
