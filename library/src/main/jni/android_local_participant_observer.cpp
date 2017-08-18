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

#include "android_local_participant_observer.h"
#include "class_reference_holder.h"
#include "video/video.h"
#include "logging.h"
#include "com_twilio_video_LocalParticipant.h"

namespace twilio_video_jni {

AndroidLocalParticipantObserver::AndroidLocalParticipantObserver(JNIEnv *env,
                                                                 jobject j_local_participant,
                                                                 jobject j_local_participant_observer)
        : j_local_participant_(env, j_local_participant),
          j_local_participant_observer_(env, j_local_participant_observer),
          j_local_participant_observer_class_(env,
                                              webrtc_jni::GetObjectClass(env, *j_local_participant_observer_)),
          j_published_audio_track_class_(env,
                                         twilio_video_jni::FindClass(env,
                                                                     "com/twilio/video/PublishedAudioTrack")),
          j_published_video_track_class_(env,
                                         twilio_video_jni::FindClass(env,
                                                                     "com/twilio/video/PublishedVideoTrack")),
          j_on_published_audio_track_(webrtc_jni::GetMethodID(env,
                                                              *j_local_participant_observer_class_,
                                                              "onPublishedAudioTrack",
                                                              "(Lcom/twilio/video/LocalParticipant;Lcom/twilio/video/PublishedAudioTrack;)V")),
          j_on_published_video_track_(webrtc_jni::GetMethodID(env,
                                                              *j_local_participant_observer_class_,
                                                              "onPublishedVideoTrack",
                                                              "(Lcom/twilio/video/LocalParticipant;Lcom/twilio/video/PublishedVideoTrack;)V")),
          j_published_audio_track_ctor_id_(webrtc_jni::GetMethodID(env,
                                                                   *j_published_audio_track_class_,
                                                                   "<init>",
                                                                   "(Ljava/lang/String;Ljava/lang/String;)V")),
          j_published_video_track_ctor_id_(webrtc_jni::GetMethodID(env,
                                                                   *j_published_video_track_class_,
                                                                   "<init>",
                                                                   "(Ljava/lang/String;Ljava/lang/String;)V")) {
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kDebug,
                      "AndroidLocalParticipantObserver");
}

AndroidLocalParticipantObserver::~AndroidLocalParticipantObserver() {
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kDebug,
                      "~AndroidLocalParticipantObserver");
}

void AndroidLocalParticipantObserver::setObserverDeleted() {
    rtc::CritScope cs(&deletion_lock_);
    observer_deleted_ = true;
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kDebug,
                      "local participant observer deleted");
}

void AndroidLocalParticipantObserver::onAudioTrackPublished(twilio::video::LocalParticipant *local_participant,
                                                            std::shared_ptr<twilio::media::LocalAudioTrackPublication> local_audio_track_publication) {
    webrtc_jni::ScopedLocalRefFrame local_ref_frame(jni());
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kDebug,
                      "%s", func_name.c_str());

    {
        rtc::CritScope cs(&deletion_lock_);

        if (!isObserverValid(func_name)) {
            return;
        }

        jobject j_published_audio_track = createJavaPublishedAudioTrack(jni(),
                                                                        local_audio_track_publication,
                                                                        *j_published_audio_track_class_,
                                                                        j_published_audio_track_ctor_id_);
        jni()->CallVoidMethod(*j_local_participant_observer_,
                              j_on_published_audio_track_,
                              *j_local_participant_,
                              j_published_audio_track);
        CHECK_EXCEPTION(jni()) << "Error calling onPublishedAudioTrack";
    }
}

void AndroidLocalParticipantObserver::onVideoTrackPublished(twilio::video::LocalParticipant *local_participant,
                                                            std::shared_ptr<twilio::media::LocalVideoTrackPublication> local_video_track_publication) {
    webrtc_jni::ScopedLocalRefFrame local_ref_frame(jni());
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kDebug,
                      "%s", func_name.c_str());
    {
        rtc::CritScope cs(&deletion_lock_);

        if (!isObserverValid(func_name)) {
            return;
        }

        jobject j_published_video_track = createJavaPublishedVideoTrack(jni(),
                                                                        local_video_track_publication,
                                                                        *j_published_video_track_class_,
                                                                        j_published_video_track_ctor_id_);
        jni()->CallVoidMethod(*j_local_participant_observer_,
                              j_on_published_video_track_,
                              *j_local_participant_,
                              j_published_video_track);
        CHECK_EXCEPTION(jni()) << "Error calling onPublishedVideoTrack";
    }
}

bool AndroidLocalParticipantObserver::isObserverValid(const std::string &callback_name) {
    if (observer_deleted_) {
        VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                          twilio::video::LogLevel::kWarning,
                          "local participant observer is marked for deletion, skipping %s callback",
                          callback_name.c_str());
        return false;
    };
    if (webrtc_jni::IsNull(jni(), *j_local_participant_observer_)) {
        VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                          twilio::video::LogLevel::kWarning,
                          "local participant observer reference has been destroyed, skipping %s callback",
                          callback_name.c_str());
        return false;
    }
    return true;
}

}