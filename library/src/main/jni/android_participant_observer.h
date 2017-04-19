#ifndef VIDEO_ANDROID_INCLUDE_ANDROID_PARTICIPANT_OBSERVER_H_
#define VIDEO_ANDROID_INCLUDE_ANDROID_PARTICIPANT_OBSERVER_H_

#include "webrtc/sdk/android/src/jni/jni_helpers.h"

#include "media/track.h"
#include "video/logger.h"
#include "video/participant_observer.h"

namespace twilio_video_jni {

class AndroidParticipantObserver : public twilio::video::ParticipantObserver {
public:
    AndroidParticipantObserver(JNIEnv *env,
                               jobject j_participant,
                               jobject j_participant_observer,
                               std::map<std::shared_ptr<twilio::media::AudioTrack>, jobject>& audio_track_map,
                               std::map<std::shared_ptr<twilio::media::VideoTrack>, jobject>& video_track_map);

    ~AndroidParticipantObserver();

    void setObserverDeleted();

protected:
    virtual void onAudioTrackAdded(twilio::video::Participant *participant,
                                   std::shared_ptr<twilio::media::AudioTrack> track);

    virtual void onAudioTrackRemoved(twilio::video::Participant *participant,
                                     std::shared_ptr<twilio::media::AudioTrack> track);

    virtual void onVideoTrackAdded(twilio::video::Participant *participant,
                                   std::shared_ptr<twilio::media::VideoTrack> track);

    virtual void onVideoTrackRemoved(twilio::video::Participant *participant,
                                     std::shared_ptr<twilio::media::VideoTrack> track);

    virtual void onAudioTrackEnabled(twilio::video::Participant *participant,
                                     std::shared_ptr<twilio::media::AudioTrack> track);

    virtual void onAudioTrackDisabled(twilio::video::Participant *participant,
                                      std::shared_ptr<twilio::media::AudioTrack> track);

    virtual void onVideoTrackEnabled(twilio::video::Participant *participant,
                                     std::shared_ptr<twilio::media::VideoTrack> track);

    virtual void onVideoTrackDisabled(twilio::video::Participant *participant,
                                      std::shared_ptr<twilio::media::VideoTrack> track);

private:
    JNIEnv *jni() {
        return webrtc_jni::AttachCurrentThreadIfNeeded();
    }

    bool isObserverValid(const std::string &callback_name);

    bool observer_deleted_ = false;
    mutable rtc::CriticalSection deletion_lock_;

    const webrtc_jni::ScopedGlobalRef<jobject> j_participant_;
    const webrtc_jni::ScopedGlobalRef<jobject> j_participant_observer_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_participant_observer_class_;
    std::map<std::shared_ptr<twilio::media::AudioTrack>, jobject>& audio_track_map_;
    std::map<std::shared_ptr<twilio::media::VideoTrack>, jobject>& video_track_map_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_audio_track_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_video_track_class_;
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
