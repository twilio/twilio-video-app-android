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

#ifndef VIDEO_ANDROID_ANDROID_STATS_OBSERVER_H_
#define VIDEO_ANDROID_ANDROID_STATS_OBSERVER_H_

#include "webrtc/sdk/android/src/jni/jni_helpers.h"

#include "twilio/media/stats_observer.h"
#include "twilio/video/stats_report.h"
#include "class_reference_holder.h"
#include "logging.h"
#include "jni_utils.h"
#include "webrtc/modules/utility/include/helpers_android.h"

#include <memory>
#include <vector>

namespace twilio_video_jni {

class AndroidStatsObserver : public twilio::media::StatsObserver {
public:
    AndroidStatsObserver(JNIEnv *env, jobject j_stats_observer) :
            j_stats_observer_(env, webrtc::JavaParamRef<jobject>(j_stats_observer)),
            j_stats_observer_class_(env, webrtc::JavaParamRef<jclass>(GetObjectClass(env, j_stats_observer_.obj()))),
            j_array_list_class_(env, webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env, "java/util/ArrayList"))),
            j_stats_report_class_(env,
                                  webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env, "com/twilio/video/StatsReport"))),
            j_local_audio_track_stats_class_(env, webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env,
                                                                                                           "com/twilio/video/LocalAudioTrackStats"))),
            j_local_video_track_stats_class_(env, webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env,
                                                                                                           "com/twilio/video/LocalVideoTrackStats"))),
            j_remote_audio_track_stats_class_(env, webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env,
                                                                                                            "com/twilio/video/RemoteAudioTrackStats"))),
            j_remote_video_track_stats_class_(env, webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env,
                                                                                                            "com/twilio/video/RemoteVideoTrackStats"))),
            j_ice_candidate_stats_class_(env, webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env,
                                                                          "com/twilio/video/IceCandidateStats"))),
            j_ice_candidate_pair_stats_class_(env, webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env,
                                                                                                            "com/twilio/video/IceCandidatePairStats"))),
            j_ice_candidate_pair_state_class_(env, webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env,
                                                                                                            "com/twilio/video/IceCandidatePairState"))),
            j_video_dimensions_class_(env, webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(env,
                                                                                                    "com/twilio/video/VideoDimensions"))),
            j_on_stats_id_(
                    webrtc::GetMethodID(env,
                                        j_stats_observer_class_.obj(),
                                        "onStats",
                                        "(Ljava/util/List;)V")),
            j_array_list_ctor_id_(
                    webrtc::GetMethodID(env,
                                        j_array_list_class_.obj(),
                                        "<init>",
                                        "()V")),
            j_array_list_add_(
                    webrtc::GetMethodID(env,
                                        j_array_list_class_.obj(),
                                        "add",
                                        "(Ljava/lang/Object;)Z")),
            j_stats_report_ctor_id_(
                    webrtc::GetMethodID(env,
                                        j_stats_report_class_.obj(),
                                        "<init>",
                                        "(Ljava/lang/String;)V")),
            j_stats_report_add_local_audio_id_(
                    webrtc::GetMethodID(env,
                                        j_stats_report_class_.obj(),
                                        "addLocalAudioTrackStats",
                                        "(Lcom/twilio/video/LocalAudioTrackStats;)V")),
            j_stats_report_add_local_video_id_(
                    webrtc::GetMethodID(env,
                                        j_stats_report_class_.obj(),
                                        "addLocalVideoTrackStats",
                                        "(Lcom/twilio/video/LocalVideoTrackStats;)V")),
            j_stats_report_add_audio_id_(
                    webrtc::GetMethodID(env,
                                        j_stats_report_class_.obj(),
                                        "addAudioTrackStats",
                                        "(Lcom/twilio/video/RemoteAudioTrackStats;)V")),
            j_stats_report_add_video_id_(
                    webrtc::GetMethodID(env,
                                        j_stats_report_class_.obj(),
                                        "addVideoTrackStats",
                                        "(Lcom/twilio/video/RemoteVideoTrackStats;)V")),

            j_stats_report_add_ice_candidate_pair_id_(
                    webrtc::GetMethodID(env,
                                        j_stats_report_class_.obj(),
                                        "addIceCandidatePairStats",
                                        "(Lcom/twilio/video/IceCandidatePairStats;)V")),
            j_stats_report_add_ice_candidate_stats_(
                    webrtc::GetMethodID(env,
                                        j_stats_report_class_.obj(),
                                        "addIceCandidateStats",
                                        "(Lcom/twilio/video/IceCandidateStats;)V")),
            j_local_audio_track_stats_ctor_id_(
                    webrtc::GetMethodID(env,
                                        j_local_audio_track_stats_class_.obj(),
                                        "<init>",
                                        "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;DJIJII)V")),
            j_local_video_track_stats_ctor_id_(
                    webrtc::GetMethodID(env,
                                        j_local_video_track_stats_class_.obj(),
                                        "<init>",
                                        "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;DJIJLcom/twilio/video/VideoDimensions;Lcom/twilio/video/VideoDimensions;II)V")),
            j_audio_track_stats_ctor_id_(
                    webrtc::GetMethodID(env,
                                        j_remote_audio_track_stats_class_.obj(),
                                        "<init>",
                                        "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;DJIII)V")),
            j_video_track_stats_ctor_id_(
                    webrtc::GetMethodID(env,
                                        j_remote_video_track_stats_class_.obj(),
                                        "<init>",
                                        "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;DJILcom/twilio/video/VideoDimensions;I)V")),
            j_video_dimensions_ctor_id_(
                    webrtc::GetMethodID(env,
                                        j_video_dimensions_class_.obj(),
                                        "<init>",
                                        "(II)V")),
            j_ice_candidate_pair_stats_ctor_id_(
                    webrtc::GetMethodID(env,
                                        j_ice_candidate_pair_stats_class_.obj(),
                                        "<init>",
                                        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/twilio/video/IceCandidatePairState;Ljava/lang/String;Ljava/lang/String;JZZZJJDDDDJJJJJJJJJZLjava/lang/String;)V")),
            j_ice_candidate_stats_ctor_id_(
                         webrtc::GetMethodID(env,
                                             j_ice_candidate_stats_class_.obj(),
                                             "<init>",
                                             "(Ljava/lang/String;ZLjava/lang/String;ILjava/lang/String;Ljava/lang/String;ILjava/lang/String;Z)V")){
    }

    virtual ~AndroidStatsObserver() {
        VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                          twilio::LogLevel::kDebug,
                          "~AndroidStatsObserver");
    }

    void setObserverDeleted() {
        rtc::CritScope cs(&deletion_lock_);
        observer_deleted_ = true;
        VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                          twilio::LogLevel::kDebug,
                          "android stats observer deleted");
    }

protected:
    virtual void onStats(
            const std::vector<twilio::media::StatsReport> &stats_reports) {
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
            // Create ArrayList<StatsReport>
            jobject j_stats_reports = jni()->NewObject(j_array_list_class_.obj(), j_array_list_ctor_id_);
            for (auto const &stats_report : stats_reports) {
                webrtc::jni::ScopedLocalRefFrame stats_iteration_ref_frame(jni());
                jstring j_peerconnection_id = JavaUTF16StringFromStdString(jni(),
                                                                           stats_report.peer_connection_id);
                jobject j_stats_report = jni()->NewObject(j_stats_report_class_.obj(),
                                                          j_stats_report_ctor_id_,
                                                          j_peerconnection_id);
                processLocalAudioTrackStats(j_stats_report,
                                            stats_report.local_audio_track_stats);
                processLocalVideoTrackStats(j_stats_report,
                                            stats_report.local_video_track_stats);
                processRemoteAudioTrackStats(j_stats_report, stats_report.remote_audio_track_stats);
                processRemoteVideoTrackStats(j_stats_report, stats_report.remote_video_track_stats);
                processIceCandidatePairStats(j_stats_report, stats_report.ice_candidate_pair_stats);
                processIceCandidateStats(j_stats_report, stats_report.ice_candidate_stats);

                jni()->CallBooleanMethod(j_stats_reports, j_array_list_add_, j_stats_report);
            }

            jni()->CallVoidMethod(j_stats_observer_.obj(), j_on_stats_id_, j_stats_reports);
        }
    }

private:
    JNIEnv *jni() {
        return webrtc::jni::AttachCurrentThreadIfNeeded();
    }

    bool isObserverValid(const std::string &callbackName) {
        if (observer_deleted_) {
            VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                              twilio::LogLevel::kWarning,
                              "android stats observer is marked for deletion, skipping %s callback",
                              callbackName.c_str());
            return false;
        };
        if (IsNull(jni(), j_stats_observer_.obj())) {
            VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                              twilio::LogLevel::kWarning,
                              "android stats observer reference has been destroyed, skipping %s callback",
                              callbackName.c_str());
            return false;
        }
        return true;
    }

    void processLocalAudioTrackStats(jobject j_stats_report,
                                     const std::vector<twilio::media::LocalAudioTrackStats> &local_audio_tracks_stats) {
        for (auto const &track_stats : local_audio_tracks_stats) {
            webrtc::jni::ScopedLocalRefFrame local_ref_frame(jni());
            jstring j_track_sid =
                    JavaUTF16StringFromStdString(jni(), track_stats.track_sid);
            jstring j_codec =
                    JavaUTF16StringFromStdString(jni(), track_stats.codec);
            jstring j_ssrc =
                    JavaUTF16StringFromStdString(jni(), track_stats.ssrc);
            jobject j_local_audio_track_stats =
                    jni()->NewObject(j_local_audio_track_stats_class_.obj(),
                                     j_local_audio_track_stats_ctor_id_,
                                     j_track_sid,
                                     track_stats.packets_lost,
                                     j_codec,
                                     j_ssrc,
                                     track_stats.timestamp,
                                     track_stats.bytes_sent,
                                     track_stats.packets_sent,
                                     track_stats.round_trip_time,
                                     track_stats.audio_level,
                                     track_stats.jitter);
            jni()->CallVoidMethod(j_stats_report,
                                  j_stats_report_add_local_audio_id_,
                                  j_local_audio_track_stats);
        }
    }

    void processLocalVideoTrackStats(jobject j_stats_report,
                                     const std::vector<twilio::media::LocalVideoTrackStats> &local_video_tracks_stats) {
        for (auto const &track_stats : local_video_tracks_stats) {
            webrtc::jni::ScopedLocalRefFrame local_ref_frame(jni());
            jstring j_track_sid =
                    JavaUTF16StringFromStdString(jni(), track_stats.track_sid);
            jstring j_codec =
                    JavaUTF16StringFromStdString(jni(), track_stats.codec);
            jstring j_ssrc =
                    JavaUTF16StringFromStdString(jni(), track_stats.ssrc);
            jobject j_capture_dimensions =
                    jni()->NewObject(j_video_dimensions_class_.obj(),
                                     j_video_dimensions_ctor_id_,
                                     track_stats.capture_dimensions.width,
                                     track_stats.capture_dimensions.height);
            jobject j_sent_dimensions =
                    jni()->NewObject(j_video_dimensions_class_.obj(),
                                     j_video_dimensions_ctor_id_,
                                     track_stats.dimensions.width,
                                     track_stats.dimensions.height);
            jobject j_local_video_track_stats =
                    jni()->NewObject(j_local_video_track_stats_class_.obj(),
                                     j_local_video_track_stats_ctor_id_,
                                     j_track_sid,
                                     track_stats.packets_lost,
                                     j_codec,
                                     j_ssrc,
                                     track_stats.timestamp,
                                     track_stats.bytes_sent,
                                     track_stats.packets_sent,
                                     track_stats.round_trip_time,
                                     j_capture_dimensions,
                                     j_sent_dimensions,
                                     track_stats.capture_frame_rate,
                                     track_stats.frame_rate);
            jni()->CallVoidMethod(j_stats_report,
                                  j_stats_report_add_local_video_id_,
                                  j_local_video_track_stats);
        }
    }

    void processRemoteAudioTrackStats(jobject j_stats_report,
                                      const std::vector<twilio::media::RemoteAudioTrackStats> &audio_tracks_stats) {
        for (auto const &track_stats : audio_tracks_stats) {
            webrtc::jni::ScopedLocalRefFrame local_ref_frame(jni());
            jstring j_track_sid =
                    JavaUTF16StringFromStdString(jni(), track_stats.track_sid);
            jstring j_codec_name =
                    JavaUTF16StringFromStdString(jni(), track_stats.codec);
            jstring j_ssrc =
                    JavaUTF16StringFromStdString(jni(), track_stats.ssrc);
            jobject j_audio_track_stats =
                    jni()->NewObject(j_remote_audio_track_stats_class_.obj(),
                                     j_audio_track_stats_ctor_id_,
                                     j_track_sid,
                                     track_stats.packets_lost,
                                     j_codec_name,
                                     j_ssrc,
                                     track_stats.timestamp,
                                     track_stats.bytes_received,
                                     track_stats.packets_received,
                                     track_stats.audio_level,
                                     track_stats.jitter);
            jni()->CallVoidMethod(j_stats_report,
                                  j_stats_report_add_audio_id_,
                                  j_audio_track_stats);
        }
    }

    void processRemoteVideoTrackStats(jobject j_stats_report,
                                      const std::vector<twilio::media::RemoteVideoTrackStats> &video_tracks_stats) {
        for (auto const &track_stats : video_tracks_stats) {
            webrtc::jni::ScopedLocalRefFrame local_ref_frame(jni());
            jstring j_track_sid =
                    JavaUTF16StringFromStdString(jni(), track_stats.track_sid);
            jstring j_codec_name =
                    JavaUTF16StringFromStdString(jni(), track_stats.codec);
            jstring j_ssrc =
                    JavaUTF16StringFromStdString(jni(), track_stats.ssrc);
            jobject j_received_dimensions =
                    jni()->NewObject(j_video_dimensions_class_.obj(),
                                     j_video_dimensions_ctor_id_,
                                     track_stats.dimensions.width,
                                     track_stats.dimensions.height);
            jobject j_video_track_stats =
                    jni()->NewObject(j_remote_video_track_stats_class_.obj(),
                                     j_video_track_stats_ctor_id_,
                                     j_track_sid,
                                     track_stats.packets_lost,
                                     j_codec_name,
                                     j_ssrc,
                                     track_stats.timestamp,
                                     track_stats.bytes_received,
                                     track_stats.packets_received,
                                     j_received_dimensions,
                                     track_stats.frame_rate);
            jni()->CallVoidMethod(j_stats_report,
                                  j_stats_report_add_video_id_,
                                  j_video_track_stats);
        }
    }
    void processIceCandidateStats(jobject j_stats_report,
                                      const std::vector<twilio::media::IceCandidateStats> &ice_candidate_stats) {
        for (auto const &stats : ice_candidate_stats) {
            webrtc::jni::ScopedLocalRefFrame local_ref_frame(jni());

            jstring j_transport_id = JavaUTF16StringFromStdString(jni(), stats.transport_id);
            jstring j_ip = JavaUTF16StringFromStdString(jni(), stats.ip);
            jstring j_protocol= JavaUTF16StringFromStdString(jni(), stats.protocol);
            jstring j_candidate_type = JavaUTF16StringFromStdString(jni(), stats.candidate_type);
            jstring j_url = JavaUTF16StringFromStdString(jni(), stats.url);

            jobject j_ice_candidate_stats =
                    jni()->NewObject(j_ice_candidate_stats_class_.obj(),
                                     j_ice_candidate_stats_ctor_id_,
                                     j_transport_id,
                                     stats.is_remote,
                                     j_ip,
                                     (jint) stats.port,
                                     j_protocol,
                                     j_candidate_type,
                                     (jint) stats.priority,
                                     j_url,
                                     stats.deleted);

            jni()->CallVoidMethod(j_stats_report,
                                  j_stats_report_add_ice_candidate_stats_,
                                  j_ice_candidate_stats);
        }
    }

    void processIceCandidatePairStats(jobject j_stats_report,
                                      const std::vector<twilio::media::IceCandidatePairStats> &ice_candidate_pair_stats) {
        for (auto const &stats : ice_candidate_pair_stats) {
            webrtc::jni::ScopedLocalRefFrame local_ref_frame(jni());
            jstring j_transport_id =
                    JavaUTF16StringFromStdString(jni(), stats.transport_id);
            jstring j_local_candidate_id =
                    JavaUTF16StringFromStdString(jni(), stats.local_candidate_id);
            jstring j_remote_candidate_id =
                    JavaUTF16StringFromStdString(jni(), stats.remote_candidate_id);

            jobject state = NULL;
            jfieldID j_state_field = NULL;
            if (stats.state == twilio::media::kStateSucceeded) {
                j_state_field = jni()->GetStaticFieldID(j_ice_candidate_pair_state_class_.obj(),
                                                        "STATE_SUCCEEDED",
                                                        "Lcom/twilio/video/IceCandidatePairState;");
            } else if (stats.state == twilio::media::kStateCancelled) {
                j_state_field = jni()->GetStaticFieldID(j_ice_candidate_pair_state_class_.obj(),
                                                        "STATE_CANCELED",
                                                        "Lcom/twilio/video/IceCandidatePairState;");
            } else if (stats.state == twilio::media::kStateFailed) {
                j_state_field = jni()->GetStaticFieldID(j_ice_candidate_pair_state_class_.obj(),
                                                        "STATE_FAILED",
                                                        "Lcom/twilio/video/IceCandidatePairState;");
            } else if (stats.state == twilio::media::kStateFrozen) {
                j_state_field = jni()->GetStaticFieldID(j_ice_candidate_pair_state_class_.obj(),
                                                        "STATE_FROZEN",
                                                        "Lcom/twilio/video/IceCandidatePairState;");
            } else if (stats.state == twilio::media::kStateInProgress) {
                j_state_field = jni()->GetStaticFieldID(j_ice_candidate_pair_state_class_.obj(),
                                                        "STATE_IN_PROGRESS",
                                                        "Lcom/twilio/video/IceCandidatePairState;");
            } else if (stats.state == twilio::media::kStateWaiting) {
                j_state_field = jni()->GetStaticFieldID(j_ice_candidate_pair_state_class_.obj(),
                                                        "STATE_WAITING",
                                                        "Lcom/twilio/video/IceCandidatePairState;");
            } else {
                VIDEO_ANDROID_LOG(twilio::LogModule::kPlatform,
                                  twilio::LogLevel::kError,
                                  "invalid ice candidate pair state received");
                continue;
            }

            state = jni()->GetStaticObjectField(j_ice_candidate_pair_state_class_.obj(),
                                                j_state_field);
            jstring localCandidateIp = JavaUTF16StringFromStdString(jni(), stats.local_candidate_ip);
            jstring remoteCandidateIp = JavaUTF16StringFromStdString(jni(), stats.remote_candidate_ip);
            jstring relayProtocol = JavaUTF16StringFromStdString(jni(), stats.relay_protocol);


            jobject j_ice_candidate_pair_stats = jni()->NewObject(j_ice_candidate_pair_stats_class_.obj(), j_ice_candidate_pair_stats_ctor_id_,
                                                                  j_transport_id, j_local_candidate_id, j_remote_candidate_id,
                                                                  state, localCandidateIp, remoteCandidateIp,
                                                                  stats.priority, stats.nominated, stats.writable, stats.readable,
                                                                  stats.bytes_sent, stats.bytes_received, stats.total_round_trip_time,
                                                                  stats.current_round_trip_time, stats.available_outgoing_bitrate,
                                                                  stats.available_incoming_bitrate,
                                                                  stats.requests_received, stats.requests_sent, stats.responses_received,
                                                                  stats.retransmissions_received, stats.retransmissions_sent,
                                                                  stats.consent_requests_received, stats.consent_requests_sent,
                                                                  stats.consent_responses_received, stats.consent_responses_sent, stats.active_candidate_pair, relayProtocol);
            jni()->CallVoidMethod(j_stats_report, j_stats_report_add_ice_candidate_pair_id_, j_ice_candidate_pair_stats);
        }
    }


    bool observer_deleted_ = false;
    mutable rtc::CriticalSection deletion_lock_;

    const webrtc::ScopedJavaGlobalRef<jobject> j_stats_observer_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_stats_observer_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_array_list_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_stats_report_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_local_audio_track_stats_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_local_video_track_stats_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_remote_audio_track_stats_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_remote_video_track_stats_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_ice_candidate_stats_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_ice_candidate_pair_stats_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_ice_candidate_pair_state_class_;
    const webrtc::ScopedJavaGlobalRef<jclass> j_video_dimensions_class_;
    jmethodID j_on_stats_id_;
    jmethodID j_array_list_ctor_id_;
    jmethodID j_array_list_add_;
    jmethodID j_stats_report_ctor_id_;
    jmethodID j_stats_report_add_local_audio_id_;
    jmethodID j_stats_report_add_local_video_id_;
    jmethodID j_stats_report_add_audio_id_;
    jmethodID j_stats_report_add_video_id_;
    jmethodID j_stats_report_add_ice_candidate_pair_id_;
    jmethodID j_stats_report_add_ice_candidate_stats_;
    jmethodID j_local_audio_track_stats_ctor_id_;
    jmethodID j_local_video_track_stats_ctor_id_;
    jmethodID j_audio_track_stats_ctor_id_;
    jmethodID j_video_track_stats_ctor_id_;
    jmethodID j_video_dimensions_ctor_id_;
    jmethodID j_ice_candidate_pair_stats_ctor_id_;
    jmethodID j_ice_candidate_stats_ctor_id_;
};

}

#endif // VIDEO_ANDROID_ANDROID_STATS_OBSERVER_H_
