#ifndef VIDEO_ANDROID_ANDROID_STATS_OBSERVER_H_
#define VIDEO_ANDROID_ANDROID_STATS_OBSERVER_H_

#include "webrtc/sdk/android/src/jni/jni_helpers.h"

#include "video/stats_observer.h"
#include "video/stats_report.h"

#include <memory>
#include <vector>


class AndroidStatsObserver: public twilio::video::StatsObserver {
public:
    AndroidStatsObserver(JNIEnv *env, jobject j_stats_observer):
        j_stats_observer_(env, j_stats_observer),
        j_stats_observer_class_(env, webrtc_jni::GetObjectClass(env, *j_stats_observer_)),
        j_array_list_class_(env, env->FindClass("java/util/ArrayList")),
        j_stats_report_class_(env, env->FindClass("com/twilio/video/StatsReport")),
        j_local_audio_track_stats_class_(env, env->FindClass("com/twilio/video/LocalAudioTrackStats")),
        j_local_video_track_stats_class_(env, env->FindClass("com/twilio/video/LocalVideoTrackStats")),
        j_audio_track_stats_class_(env, env->FindClass("com/twilio/video/AudioTrackStats")),
        j_video_track_stats_class_(env, env->FindClass("com/twilio/video/VideoTrackStats")),
        j_video_dimensions_class_(env, env->FindClass("com/twilio/video/VideoDimensions")),
        j_on_stats_id_(
            webrtc_jni::GetMethodID(env,
                                    *j_stats_observer_class_,
                                    "onStats",
                                    "(Ljava/util/List;)V")),
        j_array_list_ctor_id_(
            webrtc_jni::GetMethodID(env,
                                    *j_array_list_class_,
                                    "<init>",
                                    "()V")),
        j_array_list_add_(
            webrtc_jni::GetMethodID(env,
                                    *j_array_list_class_,
                                    "add",
                                    "(Ljava/lang/Object;)Z")),
        j_stats_report_ctor_id_(
            webrtc_jni::GetMethodID(env,
                                    *j_stats_report_class_,
                                    "<init>",
                                    "(Ljava/lang/String;)V")),
        j_stats_report_add_local_audio_id_(
            webrtc_jni::GetMethodID(env,
                                    *j_stats_report_class_,
                                    "addLocalAudioTrackStats",
                                    "(Lcom/twilio/video/LocalAudioTrackStats;)V")),
        j_stats_report_add_local_video_id_(
            webrtc_jni::GetMethodID(env,
                                    *j_stats_report_class_,
                                    "addLocalVideoTrackStats",
                                    "(Lcom/twilio/video/LocalVideoTrackStats;)V")),
        j_stats_report_add_audio_id_(
            webrtc_jni::GetMethodID(env,
                                    *j_stats_report_class_,
                                    "addAudioTrackStats",
                                    "(Lcom/twilio/video/AudioTrackStats;)V")),
        j_stats_report_add_video_id_(
            webrtc_jni::GetMethodID(env,
                                    *j_stats_report_class_,
                                    "addVideoTrackStats",
                                    "(Lcom/twilio/video/VideoTrackStats;)V")),
        j_local_audio_track_stats_ctor_id_(
            webrtc_jni::GetMethodID(env,
                                    *j_local_audio_track_stats_class_,
                                    "<init>",
                                    "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;DJIJII)V")),
        j_local_video_track_stats_ctor_id_(
            webrtc_jni::GetMethodID(env,
                                    *j_local_video_track_stats_class_,
                                    "<init>",
                                    "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;DJIJLcom/twilio/video/VideoDimensions;Lcom/twilio/video/VideoDimensions;II)V")),
        j_audio_track_stats_ctor_id_(
            webrtc_jni::GetMethodID(env,
                                    *j_audio_track_stats_class_,
                                    "<init>",
                                    "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;DJIII)V")),
        j_video_track_stats_ctor_id_(
            webrtc_jni::GetMethodID(env,
                                    *j_video_track_stats_class_,
                                    "<init>",
                                    "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;DJILcom/twilio/video/VideoDimensions;I)V")),
        j_video_dimensions_ctor_id_(
            webrtc_jni::GetMethodID(env,
                                    *j_video_dimensions_class_,
                                    "<init>",
                                    "(II)V")) {
    }

    virtual ~AndroidStatsObserver() {
        TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                           twilio::video::kTSCoreLogLevelDebug,
                           "~AndroidStatsObserver");
    }

    void setObserverDeleted() {
        rtc::CritScope cs(&deletion_lock_);
        observer_deleted_ = true;
        TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                           twilio::video::kTSCoreLogLevelDebug,
                           "android stats observer deleted");
    }

protected:
    virtual void onStats(
            const std::vector<twilio::video::StatsReport> &stats_reports) {
        webrtc_jni::ScopedLocalRefFrame local_ref_frame(jni());
        std::string func_name = std::string(__FUNCTION__);
        TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                           twilio::video::kTSCoreLogLevelDebug,
                           "%s", func_name.c_str());

        {
            rtc::CritScope cs(&deletion_lock_);
            if (!isObserverValid(func_name)) {
                return;
            }
            // Create ArrayList<StatsReport>
            jobject j_stats_reports = jni()->NewObject(*j_array_list_class_, j_array_list_ctor_id_);
            for (auto const &stats_report : stats_reports) {
                webrtc_jni::ScopedLocalRefFrame stats_iteration_ref_frame(jni());
                jstring j_peerconnection_id =
                    webrtc_jni::JavaStringFromStdString(jni(), stats_report.peer_connection_id);
                jobject j_stats_report = jni()->NewObject(*j_stats_report_class_,
                                                          j_stats_report_ctor_id_,
                                                          j_peerconnection_id);
                processLocalAudioTrackStats(j_stats_report,
                                            stats_report.local_audio_track_stats);
                processLocalVideoTrackStats(j_stats_report,
                                            stats_report.local_video_track_stats);
                processAudioTrackStats(j_stats_report, stats_report.audio_track_stats);
                processVideoTrackStats(j_stats_report, stats_report.video_track_stats);

                jni()->CallBooleanMethod(j_stats_reports, j_array_list_add_, j_stats_report);
            }

            jni()->CallVoidMethod(*j_stats_observer_, j_on_stats_id_, j_stats_reports);
        }
    }

private:
    JNIEnv *jni() {
        return webrtc_jni::AttachCurrentThreadIfNeeded();
    }

    bool isObserverValid(const std::string &callbackName) {
        if (observer_deleted_) {
            TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                               twilio::video::kTSCoreLogLevelWarning,
                               "android stats observer is marked for deletion, skipping %s callback",
                               callbackName.c_str());
            return false;
        };
        if (webrtc_jni::IsNull(jni(), *j_stats_observer_)) {
            TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                               twilio::video::kTSCoreLogLevelWarning,
                               "android stats observer reference has been destroyed, skipping %s callback",
                               callbackName.c_str());
            return false;
        }
        return true;
    }

    void processLocalAudioTrackStats(jobject j_stats_report,
                                     const std::vector<twilio::media::LocalAudioTrackStats> &local_audio_tracks_stats) {
        for(auto const &track_stats : local_audio_tracks_stats) {
            webrtc_jni::ScopedLocalRefFrame local_ref_frame(jni());
            jstring j_track_id =
                webrtc_jni::JavaStringFromStdString(jni(), track_stats.track_id);
            jstring j_codec =
                webrtc_jni::JavaStringFromStdString(jni(), track_stats.codec);
            jstring j_ssrc =
                webrtc_jni::JavaStringFromStdString(jni(), track_stats.ssrc);
            jobject j_local_audio_track_stats =
                jni()->NewObject(*j_local_audio_track_stats_class_,
                                 j_local_audio_track_stats_ctor_id_,
                                 j_track_id,
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
        for(auto const &track_stats : local_video_tracks_stats) {
            webrtc_jni::ScopedLocalRefFrame local_ref_frame(jni());
            jstring j_track_id =
                webrtc_jni::JavaStringFromStdString(jni(), track_stats.track_id);
            jstring j_codec =
                webrtc_jni::JavaStringFromStdString(jni(), track_stats.codec);
            jstring j_ssrc =
                webrtc_jni::JavaStringFromStdString(jni(), track_stats.ssrc);
            jobject j_capture_dimensions =
                jni()->NewObject(*j_video_dimensions_class_,
                                 j_video_dimensions_ctor_id_,
                                 track_stats.capture_dimensions.width,
                                 track_stats.capture_dimensions.height);
            jobject j_sent_dimensions =
                jni()->NewObject(*j_video_dimensions_class_,
                                 j_video_dimensions_ctor_id_,
                                 track_stats.dimensions.width,
                                 track_stats.dimensions.height);
            jobject j_local_video_track_stats =
                jni()->NewObject(*j_local_video_track_stats_class_,
                                 j_local_video_track_stats_ctor_id_,
                                 j_track_id,
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

    void processAudioTrackStats(jobject j_stats_report,
                                const std::vector<twilio::media::AudioTrackStats> &audio_tracks_stats) {
        for(auto const &track_stats : audio_tracks_stats) {
            webrtc_jni::ScopedLocalRefFrame local_ref_frame(jni());
            jstring j_track_id =
                webrtc_jni::JavaStringFromStdString(jni(), track_stats.track_id);
            jstring j_codec_name =
                webrtc_jni::JavaStringFromStdString(jni(), track_stats.codec);
            jstring j_ssrc =
                webrtc_jni::JavaStringFromStdString(jni(), track_stats.ssrc);
            jobject j_audio_track_stats =
                jni()->NewObject(*j_audio_track_stats_class_,
                                 j_audio_track_stats_ctor_id_,
                                 j_track_id,
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

    void processVideoTrackStats(jobject j_stats_report,
                                const std::vector<twilio::media::VideoTrackStats> &video_tracks_stats) {
        for(auto const &track_stats : video_tracks_stats) {
            webrtc_jni::ScopedLocalRefFrame local_ref_frame(jni());
            jstring j_track_id =
                webrtc_jni::JavaStringFromStdString(jni(), track_stats.track_id);
            jstring j_codec_name =
                webrtc_jni::JavaStringFromStdString(jni(), track_stats.codec);
            jstring j_ssrc =
                webrtc_jni::JavaStringFromStdString(jni(), track_stats.ssrc);
            jobject j_received_dimensions =
                jni()->NewObject(*j_video_dimensions_class_,
                                 j_video_dimensions_ctor_id_,
                                 track_stats.dimensions.width,
                                 track_stats.dimensions.height);
            jobject j_video_track_stats =
                jni()->NewObject(*j_video_track_stats_class_,
                                 j_video_track_stats_ctor_id_,
                                 j_track_id,
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

    bool observer_deleted_ = false;
    mutable rtc::CriticalSection deletion_lock_;

    const webrtc_jni::ScopedGlobalRef<jobject> j_stats_observer_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_stats_observer_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_array_list_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_stats_report_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_local_audio_track_stats_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_local_video_track_stats_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_audio_track_stats_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_video_track_stats_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_video_dimensions_class_;
    jmethodID j_on_stats_id_;
    jmethodID j_array_list_ctor_id_;
    jmethodID j_array_list_add_;
    jmethodID j_stats_report_ctor_id_;
    jmethodID j_stats_report_add_local_audio_id_;
    jmethodID j_stats_report_add_local_video_id_;
    jmethodID j_stats_report_add_audio_id_;
    jmethodID j_stats_report_add_video_id_;
    jmethodID j_local_audio_track_stats_ctor_id_;
    jmethodID j_local_video_track_stats_ctor_id_;
    jmethodID j_audio_track_stats_ctor_id_;
    jmethodID j_video_track_stats_ctor_id_;
    jmethodID j_video_dimensions_ctor_id_;

};

#endif // VIDEO_ANDROID_ANDROID_STATS_OBSERVER_H_
