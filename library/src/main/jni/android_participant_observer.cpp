#include "android_participant_observer.h"
#include "com_twilio_video_Participant.h"

namespace twilio_video_jni {

AndroidParticipantObserver::AndroidParticipantObserver(JNIEnv *env,
                                                       jobject j_participant,
                                                       jobject j_participant_observer,
                                                       std::map<std::shared_ptr<twilio::media::AudioTrack>, jobject>& audio_track_map,
                                                       std::map<std::shared_ptr<twilio::media::VideoTrack>, jobject>& video_track_map) :
        j_participant_(env, j_participant),
        j_participant_observer_(env, j_participant_observer),
        j_participant_observer_class_(env,
                                      webrtc_jni::GetObjectClass(env, *j_participant_observer_)),
        audio_track_map_(audio_track_map),
        video_track_map_(video_track_map),
        j_audio_track_class_(env, env->FindClass("com/twilio/video/AudioTrack")),
        j_video_track_class_(env, env->FindClass("com/twilio/video/VideoTrack")),
        j_on_audio_track_added_(
                webrtc_jni::GetMethodID(env,
                                        *j_participant_observer_class_,
                                        "onAudioTrackAdded",
                                        "(Lcom/twilio/video/Participant;Lcom/twilio/video/AudioTrack;)V")),
        j_on_audio_track_removed_(
                webrtc_jni::GetMethodID(env,
                                        *j_participant_observer_class_,
                                        "onAudioTrackRemoved",
                                        "(Lcom/twilio/video/Participant;Lcom/twilio/video/AudioTrack;)V")),
        j_on_video_track_added_(
                webrtc_jni::GetMethodID(env,
                                        *j_participant_observer_class_,
                                        "onVideoTrackAdded",
                                        "(Lcom/twilio/video/Participant;Lcom/twilio/video/VideoTrack;)V")),
        j_on_video_track_removed_(
                webrtc_jni::GetMethodID(env,
                                        *j_participant_observer_class_,
                                        "onVideoTrackRemoved",
                                        "(Lcom/twilio/video/Participant;Lcom/twilio/video/VideoTrack;)V")),
        j_on_audio_track_enabled_(
                webrtc_jni::GetMethodID(env,
                                        *j_participant_observer_class_,
                                        "onAudioTrackEnabled",
                                        "(Lcom/twilio/video/Participant;Lcom/twilio/video/AudioTrack;)V")),
        j_on_audio_track_disabled_(
                webrtc_jni::GetMethodID(env,
                                        *j_participant_observer_class_,
                                        "onAudioTrackDisabled",
                                        "(Lcom/twilio/video/Participant;Lcom/twilio/video/AudioTrack;)V")),
        j_on_video_track_enabled_(
                webrtc_jni::GetMethodID(env,
                                        *j_participant_observer_class_,
                                        "onVideoTrackEnabled",
                                        "(Lcom/twilio/video/Participant;Lcom/twilio/video/VideoTrack;)V")),
        j_on_video_track_disabled_(
                webrtc_jni::GetMethodID(env,
                                        *j_participant_observer_class_,
                                        "onVideoTrackDisabled",
                                        "(Lcom/twilio/video/Participant;Lcom/twilio/video/VideoTrack;)V")),
        j_audio_track_ctor_id_(
                webrtc_jni::GetMethodID(env,
                                        *j_audio_track_class_,
                                        "<init>",
                                        "(Ljava/lang/String;Z)V")),
        j_video_track_ctor_id_(
                webrtc_jni::GetMethodID(env,
                                        *j_video_track_class_,
                                        "<init>",
                                        "(Lorg/webrtc/VideoTrack;Z)V")) {
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "AndroidMediaObserver");
}

AndroidParticipantObserver::~AndroidParticipantObserver() {
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "~AndroidMediaObserver");
}

void AndroidParticipantObserver::setObserverDeleted() {
    rtc::CritScope cs(&deletion_lock_);
    observer_deleted_ = true;
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "participant observer deleted");
}

void AndroidParticipantObserver::onAudioTrackAdded(twilio::video::Participant *participant,
                                                   std::shared_ptr<twilio::media::AudioTrack> track) {
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

        jobject j_audio_track = createJavaAudioTrack(jni(),
                                                     track,
                                                     *j_audio_track_class_,
                                                     j_audio_track_ctor_id_);
        /*
         * We create a global reference to the java audio track so we can map audio track events
         * to the original java instance.
         */
        audio_track_map_.insert(std::make_pair(track,
                                               webrtc_jni::NewGlobalRef(jni(), j_audio_track)));

        jni()->CallVoidMethod(*j_participant_observer_,
                              j_on_audio_track_added_,
                              *j_participant_,
                              j_audio_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidParticipantObserver::onAudioTrackRemoved(twilio::video::Participant *participant,
                                                     std::shared_ptr<twilio::media::AudioTrack> track) {
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

        // Notify developer
        auto it = audio_track_map_.find(track);
        jobject j_audio_track = it->second;
        jni()->CallVoidMethod(*j_participant_observer_,
                              j_on_audio_track_removed_,
                              *j_participant_,
                              j_audio_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";

        // We can remove audio track and delete the global reference after notifying developer
        audio_track_map_.erase(it);
        webrtc_jni::DeleteGlobalRef(jni(), j_audio_track);
        CHECK_EXCEPTION(jni()) << "error deleting global AudioTrack reference";
    }
}

void AndroidParticipantObserver::onVideoTrackAdded(twilio::video::Participant *participant,
                                                   std::shared_ptr<twilio::media::VideoTrack> track) {
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

        jobject j_video_track =
                createJavaVideoTrack(jni(), track, *j_video_track_class_, j_video_track_ctor_id_);

        /*
         * We create a global reference to the java video track so we can map video track events
         * to the original java instance.
         */
        video_track_map_.insert(std::make_pair(track,
                                               webrtc_jni::NewGlobalRef(jni(), j_video_track)));

        jni()->CallVoidMethod(*j_participant_observer_,
                              j_on_video_track_added_,
                              *j_participant_,
                              j_video_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidParticipantObserver::onVideoTrackRemoved(twilio::video::Participant *participant,
                                                     std::shared_ptr<twilio::media::VideoTrack> track) {
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

        // Notify developer
        auto it = video_track_map_.find(track);
        jobject j_video_track = it->second;
        jni()->CallVoidMethod(*j_participant_observer_,
                              j_on_video_track_removed_,
                              *j_participant_,
                              j_video_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";

        // We can remove the video track and delete global reference after notifying developer
        video_track_map_.erase(it);
        webrtc_jni::DeleteGlobalRef(jni(), j_video_track);
        CHECK_EXCEPTION(jni()) << "error deleting global VideoTrack reference";
    }
}

void AndroidParticipantObserver::onDataTrackAdded(twilio::video::Participant *participant,
                                                  std::shared_ptr<twilio::media::DataTrack> track) {
    // TODO: Add DataTrack support
}

void AndroidParticipantObserver::onDataTrackRemoved(twilio::video::Participant *participant,
                                                    std::shared_ptr<twilio::media::DataTrack> track) {
    // TODO: Add DataTrack support
}

void AndroidParticipantObserver::onAudioTrackEnabled(twilio::video::Participant *participant,
                                                     std::shared_ptr<twilio::media::AudioTrack> track) {
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

        jobject j_audio_track = audio_track_map_[track];
        jni()->CallVoidMethod(*j_participant_observer_,
                              j_on_audio_track_enabled_,
                              *j_participant_,
                              j_audio_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidParticipantObserver::onAudioTrackDisabled(twilio::video::Participant *participant,
                                                      std::shared_ptr<twilio::media::AudioTrack> track) {
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

        jobject j_audio_track = audio_track_map_[track];
        jni()->CallVoidMethod(*j_participant_observer_,
                              j_on_audio_track_disabled_,
                              *j_participant_,
                              j_audio_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidParticipantObserver::onVideoTrackEnabled(twilio::video::Participant *participant,
                                                     std::shared_ptr<twilio::media::VideoTrack> track) {
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

        jobject j_video_track = video_track_map_[track];
        jni()->CallVoidMethod(*j_participant_observer_,
                              j_on_video_track_enabled_,
                              *j_participant_,
                              j_video_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

void AndroidParticipantObserver::onVideoTrackDisabled(twilio::video::Participant *participant,
                                                      std::shared_ptr<twilio::media::VideoTrack> track) {
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

        jobject j_video_track = video_track_map_[track];

        jni()->CallVoidMethod(*j_participant_observer_,
                              j_on_video_track_disabled_,
                              *j_participant_,
                              j_video_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }
}

bool AndroidParticipantObserver::isObserverValid(const std::string &callbackName) {
    if (observer_deleted_) {
        TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                           twilio::video::kTSCoreLogLevelWarning,
                           "participant observer is marked for deletion, skipping %s callback",
                           callbackName.c_str());
        return false;
    };
    if (webrtc_jni::IsNull(jni(), *j_participant_observer_)) {
        TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                           twilio::video::kTSCoreLogLevelWarning,
                           "participant observer reference has been destroyed, skipping %s callback",
                           callbackName.c_str());
        return false;
    }
    return true;
}

}
