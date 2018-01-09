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
#include "com_twilio_video_TwilioException.h"

namespace twilio_video_jni {

AndroidLocalParticipantObserver::AndroidLocalParticipantObserver(JNIEnv *env,
                                                                 jobject j_local_participant,
                                                                 jobject j_local_participant_observer,
                                                                 std::map<std::string, jobject>& local_audio_track_map,
                                                                 std::map<std::string, jobject>& local_video_track_map,
                                                                 std::map<std::string, jobject>& local_data_track_map)
        : j_local_participant_(env, j_local_participant),
          j_local_participant_observer_(env, j_local_participant_observer),
          local_audio_track_map_(local_audio_track_map),
          local_video_track_map_(local_video_track_map),
          local_data_track_map_(local_data_track_map),
          j_local_participant_observer_class_(env,
                                              webrtc_jni::GetObjectClass(env, *j_local_participant_observer_)),
          j_published_audio_track_class_(env,
                                         twilio_video_jni::FindClass(env,
                                                                     "com/twilio/video/LocalAudioTrackPublication")),
          j_published_video_track_class_(env,
                                         twilio_video_jni::FindClass(env,
                                                                     "com/twilio/video/LocalVideoTrackPublication")),
          j_published_data_track_class_(env,
                                        twilio_video_jni::FindClass(env,
                                                                    "com/twilio/video/LocalDataTrackPublication")),
          j_twilio_exception_class_(env, twilio_video_jni::FindClass(env,
                                                                     "com/twilio/video/TwilioException")),
          j_on_published_audio_track_(webrtc_jni::GetMethodID(env,
                                                              *j_local_participant_observer_class_,
                                                              "onAudioTrackPublished",
                                                              "(Lcom/twilio/video/LocalParticipant;Lcom/twilio/video/LocalAudioTrackPublication;)V")),
          j_on_audio_track_publication_failed_(webrtc_jni::GetMethodID(env,
                                                                       *j_local_participant_observer_class_,
                                                                       "onAudioTrackPublicationFailed",
                                                                       "(Lcom/twilio/video/LocalParticipant;Lcom/twilio/video/LocalAudioTrack;Lcom/twilio/video/TwilioException;)V")),
          j_on_published_video_track_(webrtc_jni::GetMethodID(env,
                                                              *j_local_participant_observer_class_,
                                                              "onVideoTrackPublished",
                                                              "(Lcom/twilio/video/LocalParticipant;Lcom/twilio/video/LocalVideoTrackPublication;)V")),
          j_on_video_track_publication_failed_(webrtc_jni::GetMethodID(env,
                                                                       *j_local_participant_observer_class_,
                                                                       "onVideoTrackPublicationFailed",
                                                                       "(Lcom/twilio/video/LocalParticipant;Lcom/twilio/video/LocalVideoTrack;Lcom/twilio/video/TwilioException;)V")),
          j_on_published_data_track_(webrtc_jni::GetMethodID(env,
                                                             *j_local_participant_observer_class_,
                                                             "onDataTrackPublished",
                                                             "(Lcom/twilio/video/LocalParticipant;Lcom/twilio/video/LocalDataTrackPublication;)V")),
          j_on_data_track_publication_failed_(webrtc_jni::GetMethodID(env,
                                                                      *j_local_participant_observer_class_,
                                                                      "onDataTrackPublicationFailed",
                                                                      "(Lcom/twilio/video/LocalParticipant;Lcom/twilio/video/LocalDataTrack;Lcom/twilio/video/TwilioException;)V")),
          j_published_audio_track_ctor_id_(webrtc_jni::GetMethodID(env,
                                                                   *j_published_audio_track_class_,
                                                                   "<init>",
                                                                   "(Ljava/lang/String;Lcom/twilio/video/LocalAudioTrack;)V")),
          j_published_video_track_ctor_id_(webrtc_jni::GetMethodID(env,
                                                                   *j_published_video_track_class_,
                                                                   "<init>",
                                                                   "(Ljava/lang/String;Lcom/twilio/video/LocalVideoTrack;)V")),
          j_published_data_track_ctor_id_(webrtc_jni::GetMethodID(env,
                                                                  *j_published_data_track_class_,
                                                                  "<init>",
                                                                  "(Ljava/lang/String;Lcom/twilio/video/LocalDataTrack;)V")),
          j_twilio_exception_ctor_id_(
                  webrtc_jni::GetMethodID(env,
                                          *j_twilio_exception_class_,
                                          "<init>",
                                          kTwilioExceptionConstructorSignature)){
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

        jobject j_local_audio_track =
                local_audio_track_map_[local_audio_track_publication->getLocalTrack()->getTrackId()];
        jobject j_published_audio_track = createJavaLocalAudioTrackPublication(jni(),
                                                                               local_audio_track_publication,
                                                                               j_local_audio_track,
                                                                               *j_published_audio_track_class_,
                                                                               j_published_audio_track_ctor_id_);
        jni()->CallVoidMethod(*j_local_participant_observer_,
                              j_on_published_audio_track_,
                              *j_local_participant_,
                              j_published_audio_track);
        CHECK_EXCEPTION(jni()) << "Error calling onAudioTrackPublished";
    }
}

void AndroidLocalParticipantObserver::onAudioTrackPublicationFailed(twilio::video::LocalParticipant *participant,
                                                                    std::shared_ptr<twilio::media::LocalAudioTrack> audio_track,
                                                                    const twilio::video::TwilioError twilio_error) {
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

        jobject j_local_audio_track = local_audio_track_map_[audio_track->getTrackId()];
        jobject j_twilio_exception = createJavaTwilioException(jni(),
                                                               *j_twilio_exception_class_,
                                                               j_twilio_exception_ctor_id_,
                                                               twilio_error);

        jni()->CallVoidMethod(*j_local_participant_observer_,
                              j_on_audio_track_publication_failed_,
                              *j_local_participant_,
                              j_local_audio_track,
                              j_twilio_exception);
        CHECK_EXCEPTION(jni()) << "Error calling onAudioTrackPublicationFailed";
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

        jobject j_local_video_track =
                local_video_track_map_[local_video_track_publication->getLocalTrack()->getTrackId()];
        jobject j_published_video_track = createJavaLocalVideoTrackPublication(jni(),
                                                                               local_video_track_publication,
                                                                               j_local_video_track,
                                                                               *j_published_video_track_class_,
                                                                               j_published_video_track_ctor_id_);
        jni()->CallVoidMethod(*j_local_participant_observer_,
                              j_on_published_video_track_,
                              *j_local_participant_,
                              j_published_video_track);
        CHECK_EXCEPTION(jni()) << "Error calling onVideoTrackPublished";
    }
}

void AndroidLocalParticipantObserver::onVideoTrackPublicationFailed(twilio::video::LocalParticipant *participant,
                                                                    std::shared_ptr<twilio::media::LocalVideoTrack> video_track,
                                                                    const twilio::video::TwilioError twilio_error) {
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

        jobject j_local_video_track = local_video_track_map_[video_track->getTrackId()];
        jobject j_twilio_exception = createJavaTwilioException(jni(),
                                                               *j_twilio_exception_class_,
                                                               j_twilio_exception_ctor_id_,
                                                               twilio_error);

        jni()->CallVoidMethod(*j_local_participant_observer_,
                              j_on_video_track_publication_failed_,
                              *j_local_participant_,
                              j_local_video_track,
                              j_twilio_exception);
        CHECK_EXCEPTION(jni()) << "Error calling onVideoTrackPublicationFailed";
    }
}

void AndroidLocalParticipantObserver::onDataTrackPublished(twilio::video::LocalParticipant *local_participant,
                                                           std::shared_ptr<twilio::media::LocalDataTrackPublication> local_data_track_publication) {
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

        jobject j_local_data_track =
                local_data_track_map_[local_data_track_publication->getLocalTrack()->getTrackId()];
        jobject j_published_data_track = createJavaLocalDataTrackPublication(jni(),
                                                                             local_data_track_publication,
                                                                             j_local_data_track,
                                                                             *j_published_data_track_class_,
                                                                             j_published_data_track_ctor_id_);
        jni()->CallVoidMethod(*j_local_participant_observer_,
                              j_on_published_data_track_,
                              *j_local_participant_,
                              j_published_data_track);
        CHECK_EXCEPTION(jni()) << "Error calling onDataTrackPublished";
    }
}

void AndroidLocalParticipantObserver::onDataTrackPublicationFailed(twilio::video::LocalParticipant *participant,
                                                                   std::shared_ptr<twilio::media::LocalDataTrack> data_track,
                                                                   const twilio::video::TwilioError twilio_error) {
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

        jobject j_local_data_track = local_data_track_map_[data_track->getTrackId()];
        jobject j_twilio_exception = createJavaTwilioException(jni(),
                                                               *j_twilio_exception_class_,
                                                               j_twilio_exception_ctor_id_,
                                                               twilio_error);

        jni()->CallVoidMethod(*j_local_participant_observer_,
                              j_on_data_track_publication_failed_,
                              *j_local_participant_,
                              j_local_data_track,
                              j_twilio_exception);
        CHECK_EXCEPTION(jni()) << "Error calling onDataTrackPublicationFailed";
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