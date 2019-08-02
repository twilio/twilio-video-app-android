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

#ifndef VIDEO_ANDROID_ANDROID_ROOM_OBSERVER_H_
#define VIDEO_ANDROID_ANDROID_ROOM_OBSERVER_H_

#include "webrtc/sdk/android/src/jni/jni_helpers.h"
#include "twilio/video/room_observer.h"
#include "twilio/video/remote_participant.h"
#include "com_twilio_video_RemoteParticipant.h"
#include "com_twilio_video_LocalParticipant.h"

namespace twilio_video_jni {

class AndroidRoomObserver : public twilio::video::RoomObserver {
public:
    AndroidRoomObserver(JNIEnv *env,
                        jobject j_room,
                        jobject j_room_observer,
                        jobject j_connect_options,
                        jobject j_handler);
    ~AndroidRoomObserver();
    void setObserverDeleted();
protected:
    virtual void onConnected(twilio::video::Room *room);
    virtual void onDisconnected(const twilio::video::Room *room,
                                std::unique_ptr<twilio::video::TwilioError> twilio_error);
    virtual void onConnectFailure(const twilio::video::Room *room,
                                  const twilio::video::TwilioError twilio_error);
    virtual void onReconnecting(const twilio::video::Room *room,
                                const twilio::video::TwilioError twilio_error);
    virtual void onReconnected(const twilio::video::Room *room);
    virtual void onParticipantConnected(twilio::video::Room *room,
                                        std::shared_ptr<twilio::video::RemoteParticipant> remote_participant);
    virtual void onParticipantDisconnected(twilio::video::Room *room,
                                           std::shared_ptr<twilio::video::RemoteParticipant> remote_participant);
    virtual void onRecordingStarted(twilio::video::Room *room);
    virtual void onRecordingStopped(twilio::video::Room *room);
    virtual void onDominantSpeakerChanged(const twilio::video::Room *room, std::shared_ptr<twilio::video::RemoteParticipant> remote_participant);

private:
    JNIEnv *jni();

    bool isObserverValid(const std::string &callbackName);

    jobject createJavaParticipantList(
            const std::map<std::string, std::shared_ptr<twilio::video::RemoteParticipant>> participants);

    jobject getLocalAudioTracks();
    jobject getLocalVideoTracks();
    jobject getLocalDataTracks();

    bool observer_deleted_ = false;
    mutable rtc::CriticalSection deletion_lock_;

    const webrtc::ScopedJavaGlobalRef<jobject> j_room_;
    const webrtc::ScopedJavaGlobalRef<jobject> j_room_observer_;
    const webrtc::ScopedJavaGlobalRef<jobject> j_connect_options_;
    const webrtc::ScopedJavaGlobalRef<jobject> j_handler_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_room_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_room_observer_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_local_participant_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_twilio_exception_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_remote_participant_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_array_list_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_published_audio_track_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_remote_audio_track_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_remote_audio_track_publication_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_published_video_track_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_remote_video_track_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_remote_video_track_publication_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_published_data_track_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_remote_data_track_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_remote_data_track_publication_class_;
    jmethodID j_set_connected_;
    jmethodID j_on_connected_;
    jmethodID j_on_reconnecting_;
    jmethodID j_on_reconnected_;
    jmethodID j_on_disconnected_;
    jmethodID j_on_connect_failure_;
    jmethodID j_on_participant_connected_;
    jmethodID j_on_participant_disconnected_;
    jmethodID j_on_dominant_speaker_changed;
    jmethodID j_on_recording_started_;
    jmethodID j_on_recording_stopped_;
    jmethodID j_local_participant_ctor_id_;
    jmethodID j_participant_ctor_id_;
    jmethodID j_array_list_ctor_id_;
    jmethodID j_array_list_add_;
    jmethodID j_published_audio_track_ctor_id_;
    jmethodID j_audio_track_ctor_id_;
    jmethodID j_audio_track_publication_ctor_id_;
    jmethodID j_published_video_track_ctor_id_;
    jmethodID j_video_track_ctor_id_;
    jmethodID j_video_track_publication_ctor_id_;
    jmethodID j_data_track_ctor_id_;
    jmethodID j_published_data_track_ctor_id_;
    jmethodID j_data_track_publication_ctor_id_;
    jmethodID j_connect_options_get_audio_tracks_;
    jmethodID j_connect_options_get_video_tracks_;
    jmethodID j_connect_options_get_data_tracks_;
    jmethodID j_twilio_exception_ctor_id_;
    std::map<std::shared_ptr<twilio::video::RemoteParticipant>, jobject> remote_participants_;
};

}

#endif // VIDEO_ANDROID_ANDROID_ROOM_OBSERVER_H_
