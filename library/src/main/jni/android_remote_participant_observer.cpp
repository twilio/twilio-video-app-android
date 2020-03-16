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

#include "android_remote_participant_observer.h"
#include "com_twilio_video_NetworkQuality.h"
#include "com_twilio_video_RemoteParticipant.h"
#include "com_twilio_video_RemoteAudioTrack.h"
#include "com_twilio_video_RemoteDataTrack.h"
#include "com_twilio_video_TwilioException.h"
#include "class_reference_holder.h"
#include "logging.h"
#include "webrtc/modules/utility/include/helpers_android.h"
#include "webrtc/sdk/android/src/jni/classreferenceholder.h"
#include "jni_utils.h"

namespace twilio_video_jni {

jobject createJavaWebRtcVideoTrack(JNIEnv *env,
                                   std::shared_ptr<twilio::media::RemoteVideoTrack> remote_video_track) {
    webrtc::ScopedJavaLocalRef<jclass> j_webrtc_video_track_class = webrtc::GetClass(env, "tvi/webrtc/VideoTrack");
    jmethodID j_webrtc_video_track_ctor_id = webrtc::GetMethodID(env,
                                                                 j_webrtc_video_track_class.obj(),
                                                                 "<init>",
                                                                 "(J)V");
    jobject j_webrtc_video_track = env->NewObject(j_webrtc_video_track_class.obj(),
                                                  j_webrtc_video_track_ctor_id,
                                                  webrtc::NativeToJavaPointer(
                                                          remote_video_track->getWebRtcTrack()));
    CHECK_EXCEPTION(env) << "Failed to create tvi.webrtc.VideoTrack";

    return j_webrtc_video_track;
}

AndroidRemoteParticipantObserver::AndroidRemoteParticipantObserver(JNIEnv *env,
                                                                   jobject j_remote_participant,
                                                                   jobject j_remote_participant_observer,
                                                                   std::map<std::shared_ptr<twilio::media::RemoteAudioTrackPublication>, jobject>& remote_audio_track_publication_map,
                                                                   std::map<std::shared_ptr<twilio::media::RemoteAudioTrack>, jobject>& remote_audio_track_map,
                                                                   std::map<std::shared_ptr<twilio::media::RemoteVideoTrackPublication>, jobject>& remote_video_track_publication_map,
                                                                   std::map<std::shared_ptr<twilio::media::RemoteVideoTrack>, jobject>& remote_video_track_map,
                                                                   std::map<std::shared_ptr<twilio::media::RemoteDataTrackPublication>, jobject>& remote_data_track_publication_map,
                                                                   std::map<std::shared_ptr<twilio::media::RemoteDataTrack>, jobject>& remote_data_track_map) :
        j_remote_participant_(env, webrtc::JavaParamRef<jobject>(j_remote_participant)),
        j_remote_participant_observer_(env, webrtc::JavaParamRef<jobject>(j_remote_participant_observer)),
        j_remote_participant_observer_class_(env,
                                             webrtc::JavaParamRef<jclass>(GetObjectClass(env, j_remote_participant_observer_.obj()))),
        remote_audio_track_publication_map_(remote_audio_track_publication_map),
        remote_audio_track_map_(remote_audio_track_map),
        remote_video_track_publication_map_(remote_video_track_publication_map),
        remote_video_track_map_(remote_video_track_map),
        remote_data_track_publication_map_(remote_data_track_publication_map),
        remote_data_track_map_(remote_data_track_map),
        j_remote_audio_track_class_(env, webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env, "com/twilio/video/RemoteAudioTrack"))),
        j_remote_audio_track_publication_class_(env, webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env, "com/twilio/video/RemoteAudioTrackPublication"))),
        j_remote_video_track_class_(env, webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env, "com/twilio/video/RemoteVideoTrack"))),
        j_remote_video_track_publication_class_(env, webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env, "com/twilio/video/RemoteVideoTrackPublication"))),
        j_remote_data_track_class_(env, webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env, "com/twilio/video/RemoteDataTrack"))),
        j_remote_data_track_publication_class_(env, webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env, "com/twilio/video/RemoteDataTrackPublication"))),
        j_twilio_exception_class_(env, webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env, "com/twilio/video/TwilioException"))),
        j_on_audio_track_published_(
                webrtc::GetMethodID(env,
                                    j_remote_participant_observer_class_.obj(),
                                    "onAudioTrackPublished",
                                    "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteAudioTrackPublication;)V")),
        j_on_audio_track_unpublished_(
                webrtc::GetMethodID(env,
                                    j_remote_participant_observer_class_.obj(),
                                    "onAudioTrackUnpublished",
                                    "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteAudioTrackPublication;)V")),
        j_on_audio_track_subscribed_(
                webrtc::GetMethodID(env,
                                    j_remote_participant_observer_class_.obj(),
                                    "onAudioTrackSubscribed",
                                    "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteAudioTrackPublication;Lcom/twilio/video/RemoteAudioTrack;)V")),
        j_on_audio_track_subscription_failed_(
                webrtc::GetMethodID(env,
                                    j_remote_participant_observer_class_.obj(),
                                    "onAudioTrackSubscriptionFailed",
                                    "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteAudioTrackPublication;Lcom/twilio/video/TwilioException;)V")),
        j_on_audio_track_unsubscribed_(
                webrtc::GetMethodID(env,
                                    j_remote_participant_observer_class_.obj(),
                                    "onAudioTrackUnsubscribed",
                                    "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteAudioTrackPublication;Lcom/twilio/video/RemoteAudioTrack;)V")),
        j_on_video_track_published_(
                webrtc::GetMethodID(env,
                                    j_remote_participant_observer_class_.obj(),
                                    "onVideoTrackPublished",
                                    "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteVideoTrackPublication;)V")),
        j_on_video_track_unpublished_(
                webrtc::GetMethodID(env,
                                    j_remote_participant_observer_class_.obj(),
                                    "onVideoTrackUnpublished",
                                    "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteVideoTrackPublication;)V")),
        j_on_video_track_subscribed_(
                webrtc::GetMethodID(env,
                                    j_remote_participant_observer_class_.obj(),
                                    "onVideoTrackSubscribed",
                                    "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteVideoTrackPublication;Lcom/twilio/video/RemoteVideoTrack;)V")),
        j_on_video_track_subscription_failed_(
                webrtc::GetMethodID(env,
                                    j_remote_participant_observer_class_.obj(),
                                    "onVideoTrackSubscriptionFailed",
                                    "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteVideoTrackPublication;Lcom/twilio/video/TwilioException;)V")),
        j_on_video_track_unsubscribed_(
                webrtc::GetMethodID(env,
                                    j_remote_participant_observer_class_.obj(),
                                    "onVideoTrackUnsubscribed",
                                    "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteVideoTrackPublication;Lcom/twilio/video/RemoteVideoTrack;)V")),
        j_on_data_track_published_(
                webrtc::GetMethodID(env,
                                    j_remote_participant_observer_class_.obj(),
                                    "onDataTrackPublished",
                                    "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteDataTrackPublication;)V")),
        j_on_data_track_unpublished_(
                webrtc::GetMethodID(env,
                                    j_remote_participant_observer_class_.obj(),
                                    "onDataTrackUnpublished",
                                    "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteDataTrackPublication;)V")),
        j_on_data_track_subscribed_(
                webrtc::GetMethodID(env,
                                    j_remote_participant_observer_class_.obj(),
                                    "onDataTrackSubscribed",
                                    "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteDataTrackPublication;Lcom/twilio/video/RemoteDataTrack;)V")),
        j_on_data_track_subscription_failed_(
                webrtc::GetMethodID(env,
                                    j_remote_participant_observer_class_.obj(),
                                    "onDataTrackSubscriptionFailed",
                                    "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteDataTrackPublication;Lcom/twilio/video/TwilioException;)V")),
        j_on_data_track_unsubscribed_(
                webrtc::GetMethodID(env,
                                    j_remote_participant_observer_class_.obj(),
                                    "onDataTrackUnsubscribed",
                                    "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteDataTrackPublication;Lcom/twilio/video/RemoteDataTrack;)V")),
        j_on_audio_track_enabled_(
                webrtc::GetMethodID(env,
                                    j_remote_participant_observer_class_.obj(),
                                    "onAudioTrackEnabled",
                                    "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteAudioTrackPublication;)V")),
        j_on_audio_track_disabled_(
                webrtc::GetMethodID(env,
                                    j_remote_participant_observer_class_.obj(),
                                    "onAudioTrackDisabled",
                                    "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteAudioTrackPublication;)V")),
        j_on_video_track_enabled_(
                webrtc::GetMethodID(env,
                                    j_remote_participant_observer_class_.obj(),
                                    "onVideoTrackEnabled",
                                    "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteVideoTrackPublication;)V")),
        j_on_video_track_disabled_(
                webrtc::GetMethodID(env,
                                    j_remote_participant_observer_class_.obj(),
                                    "onVideoTrackDisabled",
                                    "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteVideoTrackPublication;)V")),
        j_audio_track_ctor_id_(
                webrtc::GetMethodID(env,
                                    j_remote_audio_track_class_.obj(),
                                    "<init>",
                                    kRemoteAudioTrackConstructorSignature)),
        j_audio_track_publication_ctor_id_(
                webrtc::GetMethodID(env,
                                    j_remote_audio_track_publication_class_.obj(),
                                    "<init>",
                                    "(ZZLjava/lang/String;Ljava/lang/String;)V")),
        j_video_track_ctor_id_(
                webrtc::GetMethodID(env,
                                    j_remote_video_track_class_.obj(),
                                    "<init>",
                                    "(Ltvi/webrtc/VideoTrack;Ljava/lang/String;Ljava/lang/String;Z)V")),
        j_video_track_publication_ctor_id_(
                webrtc::GetMethodID(env,
                                    j_remote_video_track_publication_class_.obj(),
                                    "<init>",
                                    "(ZZLjava/lang/String;Ljava/lang/String;)V")),
        j_data_track_ctor_id_(
                webrtc::GetMethodID(env,
                                    j_remote_data_track_class_.obj(),
                                    "<init>",
                                    "(ZZZIILjava/lang/String;Ljava/lang/String;J)V")),
        j_data_track_publication_ctor_id_(
                webrtc::GetMethodID(env,
                                    j_remote_data_track_publication_class_.obj(),
                                    "<init>",
                                    "(ZZLjava/lang/String;Ljava/lang/String;)V")),
        j_on_network_quality_level_changed_(
                webrtc::GetMethodID(env,
                                    j_remote_participant_observer_class_.obj(),
                                    "onNetworkQualityLevelChanged",
                                    "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/NetworkQualityLevel;)V")),
        j_twilio_exception_ctor_id_(
                webrtc::GetMethodID(env,
                                    j_twilio_exception_class_.obj(),
                                    "<init>",
                                    kTwilioExceptionConstructorSignature)) {
    VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                      twilio::LogLevel::kDebug,
                      "AndroidRemoteParticipantObserver");
}

AndroidRemoteParticipantObserver::~AndroidRemoteParticipantObserver() {
    VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                      twilio::LogLevel::kDebug,
                      "~AndroidRemoteParticipantObserver");
}

void AndroidRemoteParticipantObserver::setObserverDeleted() {
    rtc::CritScope cs(&deletion_lock_);
    observer_deleted_ = true;
    VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                      twilio::LogLevel::kDebug,
                      "participant observer deleted");
}

void AndroidRemoteParticipantObserver::onAudioTrackPublished(twilio::video::RemoteParticipant *remote_participant,
                                                             std::shared_ptr<twilio::media::RemoteAudioTrackPublication> remote_audio_track_publication) {
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

        jobject j_audio_track_publication = createJavaRemoteAudioTrackPublication(jni(),
                                                                                  remote_audio_track_publication,
                                                                                  j_remote_audio_track_publication_class_.obj(),
                                                                                  j_audio_track_publication_ctor_id_);
        /*
         * We create a global reference to the java audio track so we can map audio track events
         * to the original java instance.
         */
        remote_audio_track_publication_map_.insert(std::make_pair(remote_audio_track_publication,
                                                                  webrtc::jni::NewGlobalRef(jni(),
                                                                                            j_audio_track_publication)));
        jni()->CallVoidMethod(j_remote_participant_observer_.obj(),
                              j_on_audio_track_published_,
                              j_remote_participant_.obj(),
                              j_audio_track_publication);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidRemoteParticipantObserver::onAudioTrackUnpublished(twilio::video::RemoteParticipant *remote_participant,
                                                               std::shared_ptr<twilio::media::RemoteAudioTrackPublication> remote_audio_track_publication) {
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

        // Notify developer
        auto publication_it = remote_audio_track_publication_map_.find(remote_audio_track_publication);
        jobject j_audio_track_publication = publication_it->second;
        jni()->CallVoidMethod(j_remote_participant_observer_.obj(),
                              j_on_audio_track_unpublished_,
                              j_remote_participant_.obj(),
                              j_audio_track_publication);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";

        // We can remove audio track and delete the global reference after notifying developer
        remote_audio_track_publication_map_.erase(publication_it);
        webrtc::jni::DeleteGlobalRef(jni(), j_audio_track_publication);
        CHECK_EXCEPTION(jni()) << "error deleting global RemoteAudioTrackPublication reference";
    }
}

void AndroidRemoteParticipantObserver::onVideoTrackPublished(twilio::video::RemoteParticipant *remote_participant,
                                                             std::shared_ptr<twilio::media::RemoteVideoTrackPublication> remote_video_track_publication) {
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

        jobject j_video_track_publication = createJavaRemoteVideoTrackPublication(jni(),
                                                                                  remote_video_track_publication,
                                                                                  j_remote_video_track_publication_class_.obj(),
                                                                                  j_video_track_publication_ctor_id_);
        /*
         * We create a global reference to the java video track so we can map video track events
         * to the original java instance.
         */
        remote_video_track_publication_map_.insert(std::make_pair(remote_video_track_publication,
                                                                  webrtc::jni::NewGlobalRef(jni(),
                                                                                            j_video_track_publication)));
        jni()->CallVoidMethod(j_remote_participant_observer_.obj(),
                              j_on_video_track_published_,
                              j_remote_participant_.obj(),
                              j_video_track_publication);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidRemoteParticipantObserver::onVideoTrackUnpublished(twilio::video::RemoteParticipant *remote_participant,
                                                               std::shared_ptr<twilio::media::RemoteVideoTrackPublication> remote_video_track_publication) {
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

        // Notify developer
        auto publication_it = remote_video_track_publication_map_.find(remote_video_track_publication);
        jobject j_video_track_publication = publication_it->second;
        jni()->CallVoidMethod(j_remote_participant_observer_.obj(),
                              j_on_video_track_unpublished_,
                              j_remote_participant_.obj(),
                              j_video_track_publication);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";

        // We can remove video track and delete the global reference after notifying developer
        remote_video_track_publication_map_.erase(publication_it);
        webrtc::jni::DeleteGlobalRef(jni(), j_video_track_publication);
        CHECK_EXCEPTION(jni()) << "error deleting global RemoteVideoTrackPublication reference";
    }
}

void AndroidRemoteParticipantObserver::onDataTrackPublished(twilio::video::RemoteParticipant *remote_participant,
                                                            std::shared_ptr<twilio::media::RemoteDataTrackPublication> remote_data_track_publication) {
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

        jobject j_data_track_publication = createJavaRemoteDataTrackPublication(jni(),
                                                                                remote_data_track_publication,
                                                                                j_remote_data_track_publication_class_.obj(),
                                                                                j_data_track_publication_ctor_id_);
        /*
         * Create a global reference to the java data track so we can map data track events
         * to the original java instance.
         */
        remote_data_track_publication_map_.insert(std::make_pair(remote_data_track_publication,
                                                                 webrtc::jni::NewGlobalRef(jni(),
                                                                                           j_data_track_publication)));
        jni()->CallVoidMethod(j_remote_participant_observer_.obj(),
                              j_on_data_track_published_,
                              j_remote_participant_.obj(),
                              j_data_track_publication);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidRemoteParticipantObserver::onDataTrackUnpublished(twilio::video::RemoteParticipant *remote_participant,
                                                              std::shared_ptr<twilio::media::RemoteDataTrackPublication> remote_data_track_publication) {
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

        // Notify developer
        auto publication_it = remote_data_track_publication_map_.find(remote_data_track_publication);
        jobject j_data_track_publication = publication_it->second;
        jni()->CallVoidMethod(j_remote_participant_observer_.obj(),
                              j_on_data_track_unpublished_,
                              j_remote_participant_.obj(),
                              j_data_track_publication);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";

        // Remove data track and delete the global reference after notifying developer
        remote_data_track_publication_map_.erase(publication_it);
        webrtc::jni::DeleteGlobalRef(jni(), j_data_track_publication);
        CHECK_EXCEPTION(jni()) << "error deleting global RemoteDataTrackPublication reference";
    }
}

void AndroidRemoteParticipantObserver::onAudioTrackEnabled(twilio::video::RemoteParticipant *remote_participant,
                                                           std::shared_ptr<twilio::media::RemoteAudioTrackPublication> remote_audio_track_publication) {
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

        jobject j_remote_audio_track_publication = remote_audio_track_publication_map_[remote_audio_track_publication];
        jni()->CallVoidMethod(j_remote_participant_observer_.obj(),
                              j_on_audio_track_enabled_,
                              j_remote_participant_.obj(),
                              j_remote_audio_track_publication);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidRemoteParticipantObserver::onAudioTrackDisabled(twilio::video::RemoteParticipant *remote_participant,
                                                            std::shared_ptr<twilio::media::RemoteAudioTrackPublication> remote_audio_track_publication) {
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

        jobject j_remote_audio_track_publication = remote_audio_track_publication_map_[remote_audio_track_publication];
        jni()->CallVoidMethod(j_remote_participant_observer_.obj(),
                              j_on_audio_track_disabled_,
                              j_remote_participant_.obj(),
                              j_remote_audio_track_publication);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidRemoteParticipantObserver::onVideoTrackEnabled(twilio::video::RemoteParticipant *remote_participant,
                                                           std::shared_ptr<twilio::media::RemoteVideoTrackPublication> remote_video_track_publication) {
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

        jobject j_remote_video_track_publication = remote_video_track_publication_map_[remote_video_track_publication];
        jni()->CallVoidMethod(j_remote_participant_observer_.obj(),
                              j_on_video_track_enabled_,
                              j_remote_participant_.obj(),
                              j_remote_video_track_publication);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidRemoteParticipantObserver::onVideoTrackDisabled(twilio::video::RemoteParticipant *remote_participant,
                                                            std::shared_ptr<twilio::media::RemoteVideoTrackPublication> remote_video_track_publication) {
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

        jobject j_remote_video_track_publication = remote_video_track_publication_map_[remote_video_track_publication];
        jni()->CallVoidMethod(j_remote_participant_observer_.obj(),
                              j_on_video_track_disabled_,
                              j_remote_participant_.obj(),
                              j_remote_video_track_publication);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidRemoteParticipantObserver::onAudioTrackSubscribed(twilio::video::RemoteParticipant *participant,
                                                              std::shared_ptr<twilio::media::RemoteAudioTrackPublication> remote_audio_track_publication,
                                                              std::shared_ptr<twilio::media::RemoteAudioTrack> remote_audio_track) {
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

        jobject j_remote_audio_track_publication = remote_audio_track_publication_map_[remote_audio_track_publication];
        jobject j_remote_audio_track = createJavaRemoteAudioTrack(jni(),
                                                                  remote_audio_track,
                                                                  j_remote_audio_track_class_.obj(),
                                                                  j_audio_track_ctor_id_);
        remote_audio_track_map_.insert(std::make_pair(remote_audio_track_publication->getRemoteTrack(),
                                                      webrtc::jni::NewGlobalRef(jni(),
                                                                                j_remote_audio_track)));
        jni()->CallVoidMethod(j_remote_participant_observer_.obj(),
                              j_on_audio_track_subscribed_,
                              j_remote_participant_.obj(),
                              j_remote_audio_track_publication,
                              j_remote_audio_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidRemoteParticipantObserver::onAudioTrackSubscriptionFailed(twilio::video::RemoteParticipant *participant,
                                                                      std::shared_ptr<twilio::media::RemoteAudioTrackPublication> remote_audio_track_publication,
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

        jobject j_remote_audio_track_publication = remote_audio_track_publication_map_[remote_audio_track_publication];
        jobject j_twilio_exception = createJavaTwilioException(jni(),
                                                               j_twilio_exception_class_.obj(),
                                                               j_twilio_exception_ctor_id_,
                                                               twilio_error);
        jni()->CallVoidMethod(j_remote_participant_observer_.obj(),
                              j_on_audio_track_subscription_failed_,
                              j_remote_participant_.obj(),
                              j_remote_audio_track_publication,
                              j_twilio_exception);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidRemoteParticipantObserver::onAudioTrackUnsubscribed(twilio::video::RemoteParticipant *participant,
                                                                std::shared_ptr<twilio::media::RemoteAudioTrackPublication> remote_audio_track_publication,
                                                                std::shared_ptr<twilio::media::RemoteAudioTrack> remote_audio_track) {
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

        // Notify developer
        auto track_it = remote_audio_track_map_.find(remote_audio_track_publication->getRemoteTrack());
        jobject j_remote_audio_track = track_it->second;
        jobject j_remote_audio_track_publication = remote_audio_track_publication_map_[remote_audio_track_publication];

        jni()->CallVoidMethod(j_remote_participant_observer_.obj(),
                              j_on_audio_track_unsubscribed_,
                              j_remote_participant_.obj(),
                              j_remote_audio_track_publication,
                              j_remote_audio_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        remote_audio_track_map_.erase(track_it);
        webrtc::jni::DeleteGlobalRef(jni(), j_remote_audio_track);
        CHECK_EXCEPTION(jni()) << "error deleting global RemoteAudioTrack reference";
    }
}

void AndroidRemoteParticipantObserver::onVideoTrackSubscribed(twilio::video::RemoteParticipant *participant,
                                                        std::shared_ptr<twilio::media::RemoteVideoTrackPublication> remote_video_track_publication,
                                                        std::shared_ptr<twilio::media::RemoteVideoTrack> remote_video_track) {
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

        jobject j_remote_video_track_publication = remote_video_track_publication_map_[remote_video_track_publication];
        jobject j_webrtc_video_track = createJavaWebRtcVideoTrack(jni(), remote_video_track);
        jobject j_remote_video_track = createJavaRemoteVideoTrack(jni(),
                                                                  remote_video_track,
                                                                  j_webrtc_video_track,
                                                                  j_remote_video_track_class_.obj(),
                                                                  j_video_track_ctor_id_);
        remote_video_track_map_.insert(std::make_pair(remote_video_track_publication->getRemoteTrack(),
                                                      webrtc::jni::NewGlobalRef(jni(),
                                                                                j_remote_video_track)));
        jni()->CallVoidMethod(j_remote_participant_observer_.obj(),
                              j_on_video_track_subscribed_,
                              j_remote_participant_.obj(),
                              j_remote_video_track_publication,
                              j_remote_video_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidRemoteParticipantObserver::onVideoTrackSubscriptionFailed(twilio::video::RemoteParticipant *participant,
                                                                      std::shared_ptr<twilio::media::RemoteVideoTrackPublication> remote_video_track_publication,
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

        jobject j_remote_video_track_publication = remote_video_track_publication_map_[remote_video_track_publication];
        jobject j_twilio_exception = createJavaTwilioException(jni(),
                                                               j_twilio_exception_class_.obj(),
                                                               j_twilio_exception_ctor_id_,
                                                               twilio_error);
        jni()->CallVoidMethod(j_remote_participant_observer_.obj(),
                              j_on_video_track_subscription_failed_,
                              j_remote_participant_.obj(),
                              j_remote_video_track_publication,
                              j_twilio_exception);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidRemoteParticipantObserver::onVideoTrackUnsubscribed(twilio::video::RemoteParticipant *participant,
                                                                std::shared_ptr<twilio::media::RemoteVideoTrackPublication> remote_video_track_publication,
                                                                std::shared_ptr<twilio::media::RemoteVideoTrack> remote_video_track) {
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

        // Notify developer
        auto track_it = remote_video_track_map_.find(remote_video_track);
        jobject j_remote_video_track = track_it->second;
        jobject j_remote_video_track_publication = remote_video_track_publication_map_[remote_video_track_publication];
        jni()->CallVoidMethod(j_remote_participant_observer_.obj(),
                              j_on_video_track_unsubscribed_,
                              j_remote_participant_.obj(),
                              j_remote_video_track_publication,
                              j_remote_video_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        remote_video_track_map_.erase(track_it);
        webrtc::jni::DeleteGlobalRef(jni(), j_remote_video_track);
        CHECK_EXCEPTION(jni()) << "error deleting global RemoteVideoTrack reference";
    }
}

void AndroidRemoteParticipantObserver::onDataTrackSubscribed(twilio::video::RemoteParticipant *participant,
                                                             std::shared_ptr<twilio::media::RemoteDataTrackPublication> remote_data_track_publication,
                                                             std::shared_ptr<twilio::media::RemoteDataTrack> remote_data_track) {
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

        jobject j_remote_data_track_publication = remote_data_track_publication_map_[remote_data_track_publication];
        jobject j_remote_data_track = createJavaRemoteDataTrack(jni(),
                                                                remote_data_track,
                                                                j_remote_data_track_class_.obj(),
                                                                j_data_track_ctor_id_);
        remote_data_track_map_.insert(std::make_pair(remote_data_track_publication->getRemoteTrack(),
                                                     webrtc::jni::NewGlobalRef(jni(),
                                                                               j_remote_data_track)));
        jni()->CallVoidMethod(j_remote_participant_observer_.obj(),
                              j_on_data_track_subscribed_,
                              j_remote_participant_.obj(),
                              j_remote_data_track_publication,
                              j_remote_data_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidRemoteParticipantObserver::onDataTrackSubscriptionFailed(twilio::video::RemoteParticipant *participant,
                                                                     std::shared_ptr<twilio::media::RemoteDataTrackPublication> remote_data_track_publication,
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

        jobject j_remote_data_track_publication = remote_data_track_publication_map_[remote_data_track_publication];
        jobject j_twilio_exception = createJavaTwilioException(jni(),
                                                               j_twilio_exception_class_.obj(),
                                                               j_twilio_exception_ctor_id_,
                                                               twilio_error);
        jni()->CallVoidMethod(j_remote_participant_observer_.obj(),
                              j_on_data_track_subscription_failed_,
                              j_remote_participant_.obj(),
                              j_remote_data_track_publication,
                              j_twilio_exception);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidRemoteParticipantObserver::onDataTrackUnsubscribed(twilio::video::RemoteParticipant *participant,
                                                               std::shared_ptr<twilio::media::RemoteDataTrackPublication> remote_data_track_publication,
                                                               std::shared_ptr<twilio::media::RemoteDataTrack> remote_data_track) {
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

        // Notify developer
        auto track_it = remote_data_track_map_.find(remote_data_track);
        jobject j_remote_data_track = track_it->second;
        jobject j_remote_data_track_publication = remote_data_track_publication_map_[remote_data_track_publication];
        jni()->CallVoidMethod(j_remote_participant_observer_.obj(),
                              j_on_data_track_unsubscribed_,
                              j_remote_participant_.obj(),
                              j_remote_data_track_publication,
                              j_remote_data_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        remote_data_track_map_.erase(track_it);
        webrtc::DeleteGlobalRef(jni(), j_remote_data_track);
        CHECK_EXCEPTION(jni()) << "error deleting global RemoteDataTrack reference";
    }
}

void AndroidRemoteParticipantObserver::onNetworkQualityLevelChanged(twilio::video::RemoteParticipant *participant, twilio::video::NetworkQualityLevel level){
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

        jni()->CallVoidMethod(j_remote_participant_observer_.obj(),
                              j_on_network_quality_level_changed_,
                              j_remote_participant_.obj(),
                              createJavaNetworkQualityLevel(jni(), level));
        CHECK_EXCEPTION(jni()) << "Error calling onNetworkQualityLevelChanged";
    }
}

bool AndroidRemoteParticipantObserver::isObserverValid(const std::string &callbackName) {
    if (observer_deleted_) {
        VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                          twilio::LogLevel::kWarning,
                          "participant observer is marked for deletion, skipping %s callback",
                          callbackName.c_str());
        return false;
    };
    if (webrtc::IsNull(jni(), j_remote_participant_observer_)) {
        VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                          twilio::LogLevel::kWarning,
                          "participant observer reference has been destroyed, skipping %s callback",
                          callbackName.c_str());
        return false;
    }
    return true;
}

}
