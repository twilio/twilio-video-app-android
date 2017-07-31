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

#include "android_participant_observer.h"
#include "com_twilio_video_RemoteParticipant.h"
#include "class_reference_holder.h"
#include "logging.h"
#include "webrtc/sdk/android/src/jni/classreferenceholder.h"

namespace twilio_video_jni {

jobject createJavaWebRtcVideoTrack(JNIEnv *env,
                                   std::shared_ptr<twilio::media::RemoteVideoTrack> remote_video_track) {
    jclass j_webrtc_video_track_class = webrtc_jni::FindClass(env, "org/webrtc/VideoTrack");
    jmethodID j_webrtc_video_track_ctor_id = webrtc_jni::GetMethodID(env,
                                                                     j_webrtc_video_track_class,
                                                                     "<init>",
                                                                     "(J)V");
    jobject j_webrtc_video_track = env->NewObject(j_webrtc_video_track_class,
                                                  j_webrtc_video_track_ctor_id,
                                                  webrtc_jni::jlongFromPointer(
                                                          remote_video_track->getWebRtcTrack()));
    CHECK_EXCEPTION(env) << "Failed to create org.webrtc.VideoTrack";

    return j_webrtc_video_track;
}

AndroidParticipantObserver::AndroidParticipantObserver(JNIEnv *env,
                                                       jobject j_remote_participant,
                                                       jobject j_remote_participant_observer,
                                                       std::map<std::shared_ptr<twilio::media::RemoteAudioTrack>, jobject>& remote_audio_track_map,
                                                       std::map<std::shared_ptr<twilio::media::RemoteVideoTrack>, jobject>& remote_video_track_map) :
        j_remote_participant_(env, j_remote_participant),
        j_remote_participant_observer_(env, j_remote_participant_observer),
        j_remote_participant_observer_class_(env,
                                             webrtc_jni::GetObjectClass(env, *j_remote_participant_observer_)),
        remote_audio_track_map_(remote_audio_track_map),
        remote_video_track_map_(remote_video_track_map),
        j_remote_audio_track_class_(env, twilio_video_jni::FindClass(env,
                                                                     "com/twilio/video/RemoteAudioTrack")),
        j_remote_video_track_class_(env, twilio_video_jni::FindClass(env,
                                                                     "com/twilio/video/RemoteVideoTrack")),
        j_on_audio_track_added_(
                webrtc_jni::GetMethodID(env,
                                        *j_remote_participant_observer_class_,
                                        "onAudioTrackAdded",
                                        "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteAudioTrack;)V")),
        j_on_audio_track_removed_(
                webrtc_jni::GetMethodID(env,
                                        *j_remote_participant_observer_class_,
                                        "onAudioTrackRemoved",
                                        "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteAudioTrack;)V")),
        j_on_subscribed_to_audio_track_(
                webrtc_jni::GetMethodID(env,
                                        *j_remote_participant_observer_class_,
                                        "onSubscribedToAudioTrack",
                                        "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteAudioTrack;)V")),
        j_on_unsubscribed_from_audio_track_(
                webrtc_jni::GetMethodID(env,
                                        *j_remote_participant_observer_class_,
                                        "onUnsubscribedFromAudioTrack",
                                        "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteAudioTrack;)V")),
        j_on_video_track_added_(
                webrtc_jni::GetMethodID(env,
                                        *j_remote_participant_observer_class_,
                                        "onVideoTrackAdded",
                                        "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteVideoTrack;)V")),
        j_on_video_track_removed_(
                webrtc_jni::GetMethodID(env,
                                        *j_remote_participant_observer_class_,
                                        "onVideoTrackRemoved",
                                        "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteVideoTrack;)V")),
        j_on_subscribed_to_video_track_(
                webrtc_jni::GetMethodID(env,
                                        *j_remote_participant_observer_class_,
                                        "onSubscribedToVideoTrack",
                                        "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteVideoTrack;)V")),
        j_on_unsubscribed_from_video_track_(
                webrtc_jni::GetMethodID(env,
                                        *j_remote_participant_observer_class_,
                                        "onUnsubscribedFromVideoTrack",
                                        "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteVideoTrack;)V")),
        j_on_audio_track_enabled_(
                webrtc_jni::GetMethodID(env,
                                        *j_remote_participant_observer_class_,
                                        "onAudioTrackEnabled",
                                        "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteAudioTrack;)V")),
        j_on_audio_track_disabled_(
                webrtc_jni::GetMethodID(env,
                                        *j_remote_participant_observer_class_,
                                        "onAudioTrackDisabled",
                                        "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteAudioTrack;)V")),
        j_on_video_track_enabled_(
                webrtc_jni::GetMethodID(env,
                                        *j_remote_participant_observer_class_,
                                        "onVideoTrackEnabled",
                                        "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteVideoTrack;)V")),
        j_on_video_track_disabled_(
                webrtc_jni::GetMethodID(env,
                                        *j_remote_participant_observer_class_,
                                        "onVideoTrackDisabled",
                                        "(Lcom/twilio/video/RemoteParticipant;Lcom/twilio/video/RemoteVideoTrack;)V")),
        j_audio_track_ctor_id_(
                webrtc_jni::GetMethodID(env,
                                        *j_remote_audio_track_class_,
                                        "<init>",
                                        "(Ljava/lang/String;Ljava/lang/String;ZZ)V")),
        j_video_track_ctor_id_(
                webrtc_jni::GetMethodID(env,
                                        *j_remote_video_track_class_,
                                        "<init>",
                                        "(Ljava/lang/String;Ljava/lang/String;ZZ)V")) {
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kDebug,
                      "AndroidMediaObserver");
}

AndroidParticipantObserver::~AndroidParticipantObserver() {
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kDebug,
                      "~AndroidMediaObserver");
}

void AndroidParticipantObserver::setObserverDeleted() {
    rtc::CritScope cs(&deletion_lock_);
    observer_deleted_ = true;
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kDebug,
                      "participant observer deleted");
}

void AndroidParticipantObserver::onAudioTrackAdded(twilio::video::RemoteParticipant *remote_participant,
                                                   std::shared_ptr<twilio::media::RemoteAudioTrack> remote_audio_track) {
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

        jobject j_audio_track = createJavaRemoteAudioTrack(jni(),
                                                           remote_audio_track,
                                                           *j_remote_audio_track_class_,
                                                           j_audio_track_ctor_id_);
        /*
         * We create a global reference to the java audio track so we can map audio track events
         * to the original java instance.
         */
        remote_audio_track_map_.insert(std::make_pair(remote_audio_track,
                                                      webrtc_jni::NewGlobalRef(jni(),
                                                                               j_audio_track)));
        jni()->CallVoidMethod(*j_remote_participant_observer_,
                              j_on_audio_track_added_,
                              *j_remote_participant_,
                              j_audio_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidParticipantObserver::onAudioTrackRemoved(twilio::video::RemoteParticipant *remote_participant,
                                                     std::shared_ptr<twilio::media::RemoteAudioTrack> remote_audio_track) {
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

        // Notify developer
        auto it = remote_audio_track_map_.find(remote_audio_track);
        jobject j_audio_track = it->second;
        jni()->CallVoidMethod(*j_remote_participant_observer_,
                              j_on_audio_track_removed_,
                              *j_remote_participant_,
                              j_audio_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";

        // We can remove audio track and delete the global reference after notifying developer
        remote_audio_track_map_.erase(it);
        webrtc_jni::DeleteGlobalRef(jni(), j_audio_track);
        CHECK_EXCEPTION(jni()) << "error deleting global AudioTrack reference";
    }
}

void AndroidParticipantObserver::onVideoTrackAdded(twilio::video::RemoteParticipant *remote_participant,
                                                   std::shared_ptr<twilio::media::RemoteVideoTrack> remote_video_track) {
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

        jobject j_remote_video_track =
                createJavaRemoteVideoTrack(jni(),
                                           remote_video_track,
                                           *j_remote_video_track_class_,
                                           j_video_track_ctor_id_);

        /*
         * We create a global reference to the java video track so we can map video track events
         * to the original java instance.
         */
        remote_video_track_map_.insert(std::make_pair(remote_video_track,
                                                      webrtc_jni::NewGlobalRef(jni(), j_remote_video_track)));
        jni()->CallVoidMethod(*j_remote_participant_observer_,
                              j_on_video_track_added_,
                              *j_remote_participant_,
                              j_remote_video_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidParticipantObserver::onVideoTrackRemoved(twilio::video::RemoteParticipant *remote_participant,
                                                     std::shared_ptr<twilio::media::RemoteVideoTrack> remote_video_track) {
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

        // Notify developer
        auto it = remote_video_track_map_.find(remote_video_track);
        jobject j_remote_video_track = it->second;
        jni()->CallVoidMethod(*j_remote_participant_observer_,
                              j_on_video_track_removed_,
                              *j_remote_participant_,
                              j_remote_video_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";

        // We can remove the video track and delete global reference after notifying developer
        remote_video_track_map_.erase(it);
        webrtc_jni::DeleteGlobalRef(jni(), j_remote_video_track);
        CHECK_EXCEPTION(jni()) << "error deleting global RemoteVideoTrack reference";
    }
}

void AndroidParticipantObserver::onDataTrackAdded(twilio::video::RemoteParticipant *remote_participant,
                                                  std::shared_ptr<twilio::media::DataTrack> track) {
    // TODO: Add DataTrack support
}

void AndroidParticipantObserver::onDataTrackRemoved(twilio::video::RemoteParticipant *remote_participant,
                                                    std::shared_ptr<twilio::media::DataTrack> track) {
    // TODO: Add DataTrack support
}

void AndroidParticipantObserver::onAudioTrackEnabled(twilio::video::RemoteParticipant *remote_participant,
                                                     std::shared_ptr<twilio::media::RemoteAudioTrack> remote_audio_track) {
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

        jobject j_remote_audio_track = remote_audio_track_map_[remote_audio_track];
        jni()->CallVoidMethod(*j_remote_participant_observer_,
                              j_on_audio_track_enabled_,
                              *j_remote_participant_,
                              j_remote_audio_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidParticipantObserver::onAudioTrackDisabled(twilio::video::RemoteParticipant *remote_participant,
                                                      std::shared_ptr<twilio::media::RemoteAudioTrack> remote_audio_track) {
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

        jobject j_remote_audio_track = remote_audio_track_map_[remote_audio_track];
        jni()->CallVoidMethod(*j_remote_participant_observer_,
                              j_on_audio_track_disabled_,
                              *j_remote_participant_,
                              j_remote_audio_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidParticipantObserver::onVideoTrackEnabled(twilio::video::RemoteParticipant *remote_participant,
                                                     std::shared_ptr<twilio::media::RemoteVideoTrack> remote_video_track) {
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

        jobject j_remote_video_track = remote_video_track_map_[remote_video_track];
        jni()->CallVoidMethod(*j_remote_participant_observer_,
                              j_on_video_track_enabled_,
                              *j_remote_participant_,
                              j_remote_video_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidParticipantObserver::onVideoTrackDisabled(twilio::video::RemoteParticipant *remote_participant,
                                                      std::shared_ptr<twilio::media::RemoteVideoTrack> remote_video_track) {
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

        jobject j_remote_video_track = remote_video_track_map_[remote_video_track];
        jni()->CallVoidMethod(*j_remote_participant_observer_,
                              j_on_video_track_disabled_,
                              *j_remote_participant_,
                              j_remote_video_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidParticipantObserver::onAudioTrackSubscribed(twilio::video::RemoteParticipant *participant,
                                                        std::shared_ptr<twilio::media::RemoteAudioTrack> remote_audio_track) {
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

        jobject j_remote_audio_track = remote_audio_track_map_[remote_audio_track];
        jni()->CallVoidMethod(*j_remote_participant_observer_,
                              j_on_subscribed_to_audio_track_,
                              *j_remote_participant_,
                              j_remote_audio_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidParticipantObserver::onAudioTrackUnsubscribed(twilio::video::RemoteParticipant *participant,
                                                          std::shared_ptr<twilio::media::RemoteAudioTrack> remote_audio_track) {
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

        // Notify developer
        jobject j_remote_audio_track = remote_audio_track_map_[remote_audio_track];
        jni()->CallVoidMethod(*j_remote_participant_observer_,
                              j_on_unsubscribed_from_audio_track_,
                              *j_remote_participant_,
                              j_remote_audio_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidParticipantObserver::onVideoTrackSubscribed(twilio::video::RemoteParticipant *participant,
                                                        std::shared_ptr<twilio::media::RemoteVideoTrack> remote_video_track) {
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

        jobject j_remote_video_track = remote_video_track_map_[remote_video_track];
        jobject j_webrtc_video_track = createJavaWebRtcVideoTrack(jni(), remote_video_track);
        jmethodID j_set_webrtc_track_method_id = webrtc_jni::GetMethodID(jni(),
                                                                         webrtc_jni::GetObjectClass(jni(), j_remote_video_track),
                                                                         "setWebRtcTrack",
                                                                         "(Lorg/webrtc/VideoTrack;)V");
        jni()->CallVoidMethod(j_remote_video_track,
                              j_set_webrtc_track_method_id,
                              j_webrtc_video_track);
        CHECK_EXCEPTION(jni()) << "Error setting WebRTC Video Track";
        jni()->CallVoidMethod(*j_remote_participant_observer_,
                              j_on_subscribed_to_video_track_,
                              *j_remote_participant_,
                              j_remote_video_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidParticipantObserver::onVideoTrackUnsubscribed(twilio::video::RemoteParticipant *participant,
                                                          std::shared_ptr<twilio::media::RemoteVideoTrack> remote_video_track) {
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

        // Notify developer
        jobject j_remote_video_track = remote_video_track_map_[remote_video_track];
        jni()->CallVoidMethod(*j_remote_participant_observer_,
                              j_on_unsubscribed_from_video_track_,
                              *j_remote_participant_,
                              j_remote_video_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

bool AndroidParticipantObserver::isObserverValid(const std::string &callbackName) {
    if (observer_deleted_) {
        VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                          twilio::video::LogLevel::kWarning,
                          "participant observer is marked for deletion, skipping %s callback",
                          callbackName.c_str());
        return false;
    };
    if (webrtc_jni::IsNull(jni(), *j_remote_participant_observer_)) {
        VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                          twilio::video::LogLevel::kWarning,
                          "participant observer reference has been destroyed, skipping %s callback",
                          callbackName.c_str());
        return false;
    }
    return true;
}

}
