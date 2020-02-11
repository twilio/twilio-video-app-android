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
#include "twilio/video/video.h"
#include "logging.h"
#include "com_twilio_video_LocalParticipant.h"
#include "com_twilio_video_LocalAudioTrack.h"
#include "com_twilio_video_LocalVideoTrack.h"
#include "com_twilio_video_LocalDataTrack.h"
#include "com_twilio_video_TwilioException.h"
#include "jni_utils.h"
#include "webrtc/modules/utility/include/helpers_android.h"

namespace twilio_video_jni {

AndroidLocalParticipantObserver::AndroidLocalParticipantObserver(JNIEnv *env,
                                                                 jobject j_local_participant,
                                                                 jobject j_local_participant_observer,
                                                                 std::map<std::string, jobject>& local_audio_track_map,
                                                                 std::map<std::string, jobject>& local_video_track_map,
                                                                 std::map<std::string, jobject>& local_data_track_map)
        : j_local_participant_(env, webrtc::JavaParamRef<jobject>(j_local_participant)),
          j_local_participant_observer_(env, webrtc::JavaParamRef<jobject>(j_local_participant_observer)),
          local_audio_track_map_(local_audio_track_map),
          local_video_track_map_(local_video_track_map),
          local_data_track_map_(local_data_track_map),
          j_local_participant_observer_class_(env,
                                              webrtc::JavaParamRef<jclass>(GetObjectClass(env, j_local_participant_observer_.obj()))),
          j_published_audio_track_class_(env,
                                         webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env, "com/twilio/video/LocalAudioTrackPublication"))),
          j_published_video_track_class_(env,
                                         webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env, "com/twilio/video/LocalVideoTrackPublication"))),
          j_published_data_track_class_(env,
                                        webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env, "com/twilio/video/LocalDataTrackPublication"))),
          j_twilio_exception_class_(env, webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env,
                                                                                                  "com/twilio/video/TwilioException"))),
          j_network_quality_level_class_(env, webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env,
                                                                                                       "com/twilio/video/NetworkQualityLevel"))),
          j_on_published_audio_track_(webrtc::GetMethodID(env,
                                                          j_local_participant_observer_class_.obj(),
                                                          "onAudioTrackPublished",
                                                          "(Lcom/twilio/video/LocalParticipant;Lcom/twilio/video/LocalAudioTrackPublication;)V")),
          j_on_audio_track_publication_failed_(webrtc::GetMethodID(env,
                                                                   j_local_participant_observer_class_.obj(),
                                                                   "onAudioTrackPublicationFailed",
                                                                   "(Lcom/twilio/video/LocalParticipant;Lcom/twilio/video/LocalAudioTrack;Lcom/twilio/video/TwilioException;)V")),
          j_on_published_video_track_(webrtc::GetMethodID(env,
                                                          j_local_participant_observer_class_.obj(),
                                                          "onVideoTrackPublished",
                                                          "(Lcom/twilio/video/LocalParticipant;Lcom/twilio/video/LocalVideoTrackPublication;)V")),
          j_on_video_track_publication_failed_(webrtc::GetMethodID(env,
                                                                   j_local_participant_observer_class_.obj(),
                                                                   "onVideoTrackPublicationFailed",
                                                                   "(Lcom/twilio/video/LocalParticipant;Lcom/twilio/video/LocalVideoTrack;Lcom/twilio/video/TwilioException;)V")),
          j_on_published_data_track_(webrtc::GetMethodID(env,
                                                         j_local_participant_observer_class_.obj(),
                                                         "onDataTrackPublished",
                                                         "(Lcom/twilio/video/LocalParticipant;Lcom/twilio/video/LocalDataTrackPublication;)V")),
          j_on_data_track_publication_failed_(webrtc::GetMethodID(env,
                                                                  j_local_participant_observer_class_.obj(),
                                                                  "onDataTrackPublicationFailed",
                                                                  "(Lcom/twilio/video/LocalParticipant;Lcom/twilio/video/LocalDataTrack;Lcom/twilio/video/TwilioException;)V")),

          j_on_network_quality_level_changed_(webrtc::GetMethodID(env,
                                                                 j_local_participant_observer_class_.obj(),
                                                                 "onNetworkQualityLevelChanged",
                                                                 "(Lcom/twilio/video/LocalParticipant;Lcom/twilio/video/NetworkQualityLevel;)V")),

          j_published_audio_track_ctor_id_(webrtc::GetMethodID(env,
                                                               j_published_audio_track_class_.obj(),
                                                               "<init>",
                                                               "(Ljava/lang/String;Lcom/twilio/video/LocalAudioTrack;)V")),
          j_published_video_track_ctor_id_(webrtc::GetMethodID(env,
                                                               j_published_video_track_class_.obj(),
                                                               "<init>",
                                                               "(Ljava/lang/String;Lcom/twilio/video/LocalVideoTrack;)V")),
          j_published_data_track_ctor_id_(webrtc::GetMethodID(env,
                                                              j_published_data_track_class_.obj(),
                                                              "<init>",
                                                              "(Ljava/lang/String;Lcom/twilio/video/LocalDataTrack;)V")),
          j_twilio_exception_ctor_id_(
                  webrtc::GetMethodID(env,
                                      j_twilio_exception_class_.obj(),
                                      "<init>",
                                      kTwilioExceptionConstructorSignature)){
    VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                      twilio::LogLevel::kDebug,
                      "AndroidLocalParticipantObserver");
}

AndroidLocalParticipantObserver::~AndroidLocalParticipantObserver() {
    VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                      twilio::LogLevel::kDebug,
                      "~AndroidLocalParticipantObserver");
}

void AndroidLocalParticipantObserver::setObserverDeleted() {
    rtc::CritScope cs(&deletion_lock_);
    observer_deleted_ = true;
    VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                      twilio::LogLevel::kDebug,
                      "local participant observer deleted");
}

void AndroidLocalParticipantObserver::onAudioTrackPublished(twilio::video::LocalParticipant *local_participant,
                                                            std::shared_ptr<twilio::media::LocalAudioTrackPublication> local_audio_track_publication) {
    webrtc::jni::ScopedLocalRefFrame local_ref_frame(jni());
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                      twilio::LogLevel::kDebug,
                      "%s", func_name.c_str());

    {
        rtc::CritScope cs(&deletion_lock_);

        if (!isObserverValid(func_name)) {
            return;
        }

        jobject j_local_audio_track =
                local_audio_track_map_[getLocalAudioTrackHash(local_audio_track_publication->getLocalTrack())];
        jobject j_published_audio_track = createJavaLocalAudioTrackPublication(jni(),
                                                                               local_audio_track_publication,
                                                                               j_local_audio_track,
                                                                               j_published_audio_track_class_.obj(),
                                                                               j_published_audio_track_ctor_id_);
        jni()->CallVoidMethod(j_local_participant_observer_.obj(),
                              j_on_published_audio_track_,
                              j_local_participant_.obj(),
                              j_published_audio_track);
        CHECK_EXCEPTION(jni()) << "Error calling onAudioTrackPublished";
    }
}

void AndroidLocalParticipantObserver::onAudioTrackPublicationFailed(twilio::video::LocalParticipant *participant,
                                                                    std::shared_ptr<twilio::media::LocalAudioTrack> audio_track,
                                                                    const twilio::video::Error twilio_error) {
    webrtc::jni::ScopedLocalRefFrame local_ref_frame(jni());
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                      twilio::LogLevel::kDebug,
                      "%s", func_name.c_str());

    {
        rtc::CritScope cs(&deletion_lock_);

        if (!isObserverValid(func_name)) {
            return;
        }

        jobject j_local_audio_track = local_audio_track_map_[getLocalAudioTrackHash(audio_track)];
        jobject j_twilio_exception = createJavaTwilioException(jni(),
                                                               j_twilio_exception_class_.obj(),
                                                               j_twilio_exception_ctor_id_,
                                                               twilio_error);

        jni()->CallVoidMethod(j_local_participant_observer_.obj(),
                              j_on_audio_track_publication_failed_,
                              j_local_participant_.obj(),
                              j_local_audio_track,
                              j_twilio_exception);
        CHECK_EXCEPTION(jni()) << "Error calling onAudioTrackPublicationFailed";
    }
}

void AndroidLocalParticipantObserver::onVideoTrackPublished(twilio::video::LocalParticipant *local_participant,
                                                            std::shared_ptr<twilio::media::LocalVideoTrackPublication> local_video_track_publication) {
    webrtc::jni::ScopedLocalRefFrame local_ref_frame(jni());
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                      twilio::LogLevel::kDebug,
                      "%s", func_name.c_str());
    {
        rtc::CritScope cs(&deletion_lock_);

        if (!isObserverValid(func_name)) {
            return;
        }

        jobject j_local_video_track =
                local_video_track_map_[getLocalVideoTrackHash(local_video_track_publication->getLocalTrack())];
        jobject j_published_video_track = createJavaLocalVideoTrackPublication(jni(),
                                                                               local_video_track_publication,
                                                                               j_local_video_track,
                                                                               j_published_video_track_class_.obj(),
                                                                               j_published_video_track_ctor_id_);
        jni()->CallVoidMethod(j_local_participant_observer_.obj(),
                              j_on_published_video_track_,
                              j_local_participant_.obj(),
                              j_published_video_track);
        CHECK_EXCEPTION(jni()) << "Error calling onVideoTrackPublished";
    }
}

void AndroidLocalParticipantObserver::onVideoTrackPublicationFailed(twilio::video::LocalParticipant *participant,
                                                                    std::shared_ptr<twilio::media::LocalVideoTrack> video_track,
                                                                    const twilio::video::Error twilio_error) {
    webrtc::jni::ScopedLocalRefFrame local_ref_frame(jni());
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                      twilio::LogLevel::kDebug,
                      "%s", func_name.c_str());

    {
        rtc::CritScope cs(&deletion_lock_);

        if (!isObserverValid(func_name)) {
            return;
        }

        jobject j_local_video_track = local_video_track_map_[getLocalVideoTrackHash(video_track)];
        jobject j_twilio_exception = createJavaTwilioException(jni(),
                                                               j_twilio_exception_class_.obj(),
                                                               j_twilio_exception_ctor_id_,
                                                               twilio_error);

        jni()->CallVoidMethod(j_local_participant_observer_.obj(),
                              j_on_video_track_publication_failed_,
                              j_local_participant_.obj(),
                              j_local_video_track,
                              j_twilio_exception);
        CHECK_EXCEPTION(jni()) << "Error calling onVideoTrackPublicationFailed";
    }
}

void AndroidLocalParticipantObserver::onDataTrackPublished(twilio::video::LocalParticipant *local_participant,
                                                           std::shared_ptr<twilio::media::LocalDataTrackPublication> local_data_track_publication) {
    webrtc::jni::ScopedLocalRefFrame local_ref_frame(jni());
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                      twilio::LogLevel::kDebug,
                      "%s", func_name.c_str());
    {
        rtc::CritScope cs(&deletion_lock_);

        if (!isObserverValid(func_name)) {
            return;
        }

        jobject j_local_data_track =
                local_data_track_map_[getLocalDataTrackHash(local_data_track_publication->getLocalTrack())];
        jobject j_published_data_track = createJavaLocalDataTrackPublication(jni(),
                                                                             local_data_track_publication,
                                                                             j_local_data_track,
                                                                             j_published_data_track_class_.obj(),
                                                                             j_published_data_track_ctor_id_);
        jni()->CallVoidMethod(j_local_participant_observer_.obj(),
                              j_on_published_data_track_,
                              j_local_participant_.obj(),
                              j_published_data_track);
        CHECK_EXCEPTION(jni()) << "Error calling onDataTrackPublished";
    }
}

void AndroidLocalParticipantObserver::onDataTrackPublicationFailed(twilio::video::LocalParticipant *participant,
                                                                   std::shared_ptr<twilio::media::LocalDataTrack> data_track,
                                                                   const twilio::video::Error twilio_error) {
    webrtc::jni::ScopedLocalRefFrame local_ref_frame(jni());
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                      twilio::LogLevel::kDebug,
                      "%s", func_name.c_str());

    {
        rtc::CritScope cs(&deletion_lock_);

        if (!isObserverValid(func_name)) {
            return;
        }

        jobject j_local_data_track = local_data_track_map_[getLocalDataTrackHash(data_track)];
        jobject j_twilio_exception = createJavaTwilioException(jni(),
                                                               j_twilio_exception_class_.obj(),
                                                               j_twilio_exception_ctor_id_,
                                                               twilio_error);

        jni()->CallVoidMethod(j_local_participant_observer_.obj(),
                              j_on_data_track_publication_failed_,
                              j_local_participant_.obj(),
                              j_local_data_track,
                              j_twilio_exception);
        CHECK_EXCEPTION(jni()) << "Error calling onDataTrackPublicationFailed";
    }
}

void AndroidLocalParticipantObserver::onNetworkQualityLevelChanged(twilio::video::LocalParticipant *participant, twilio::video::NetworkQualityLevel level){
    webrtc::jni::ScopedLocalRefFrame local_ref_frame(jni());
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                      twilio::LogLevel::kDebug,
                      "%s", func_name.c_str());

        {
        rtc::CritScope cs(&deletion_lock_);

        if (!isObserverValid(func_name)) {
            return;
        }

        jfieldID j_level_field = jni()->GetStaticFieldID(j_network_quality_level_class_.obj(),
                                                         "NETWORK_QUALITY_LEVEL_UNKNOWN",
                                                         "Lcom/twilio/video/NetworkQualityLevel;");


        if(level == twilio::video::kNetworkQualityLevelZero) {
            j_level_field = jni()->GetStaticFieldID(j_network_quality_level_class_.obj(),
                                                         "NETWORK_QUALITY_LEVEL_ZERO",
                                                         "Lcom/twilio/video/NetworkQualityLevel;");
        } else if(level == twilio::video::kNetworkQualityLevelOne) {
            j_level_field = jni()->GetStaticFieldID(j_network_quality_level_class_.obj(),
                                                    "NETWORK_QUALITY_LEVEL_ONE",
                                                    "Lcom/twilio/video/NetworkQualityLevel;");

        } else if(level == twilio::video::kNetworkQualityLevelTwo) {
            j_level_field = jni()->GetStaticFieldID(j_network_quality_level_class_.obj(),
                                                    "NETWORK_QUALITY_LEVEL_TWO",
                                                    "Lcom/twilio/video/NetworkQualityLevel;");

        } else if(level == twilio::video::kNetworkQualityLevelThree) {
            j_level_field = jni()->GetStaticFieldID(j_network_quality_level_class_.obj(),
                                                    "NETWORK_QUALITY_LEVEL_THREE",
                                                    "Lcom/twilio/video/NetworkQualityLevel;");

        } else if(level == twilio::video::kNetworkQualityLevelFour) {
            j_level_field = jni()->GetStaticFieldID(j_network_quality_level_class_.obj(),
                                                    "NETWORK_QUALITY_LEVEL_FOUR",
                                                    "Lcom/twilio/video/NetworkQualityLevel;");

        } else if(level == twilio::video::kNetworkQualityLevelFive) {
            j_level_field = jni()->GetStaticFieldID(j_network_quality_level_class_.obj(),
                                                    "NETWORK_QUALITY_LEVEL_FIVE",
                                                    "Lcom/twilio/video/NetworkQualityLevel;");
        } else {
            FATAL() << "Unknown quality level. There is no corresponding Java enum value";
        }

        jobject j_level = jni()->GetStaticObjectField(j_network_quality_level_class_.obj(),
                                                      j_level_field);

        jni()->CallVoidMethod(j_local_participant_observer_.obj(),
                j_on_network_quality_level_changed_,
                j_local_participant_.obj(),
                j_level);
        CHECK_EXCEPTION(jni()) << "Error calling onNetworkQualityLevelChanged";
    }
}

bool AndroidLocalParticipantObserver::isObserverValid(const std::string &callback_name) {
    if (observer_deleted_) {
        VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                          twilio::LogLevel::kWarning,
                          "local participant observer is marked for deletion, skipping %s callback",
                          callback_name.c_str());
        return false;
    };
    if (webrtc::IsNull(jni(), j_local_participant_observer_)) {
        VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                          twilio::LogLevel::kWarning,
                          "local participant observer reference has been destroyed, skipping %s callback",
                          callback_name.c_str());
        return false;
    }
    return true;
}

}