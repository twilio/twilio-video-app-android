#ifndef VIDEO_ANDROID_ANDROID_ROOM_OBSERVER_H_
#define VIDEO_ANDROID_ANDROID_ROOM_OBSERVER_H_

#include "webrtc/sdk/android/src/jni/jni_helpers.h"
#include "video/room_observer.h"
#include "video/participant.h"
#include "com_twilio_video_Participant.h"
#include "com_twilio_video_LocalParticipant.h"

namespace twilio_video_jni {

class AndroidRoomObserver : public twilio::video::RoomObserver {
public:
    AndroidRoomObserver(JNIEnv *env, jobject j_room_observer);
    ~AndroidRoomObserver();
    void setObserverDeleted();
protected:
    virtual void onConnected(twilio::video::Room *room);
    virtual void onDisconnected(const twilio::video::Room *room,
                                std::unique_ptr<twilio::video::TwilioError> twilio_error);
    virtual void onConnectFailure(const twilio::video::Room *room,
                                  const twilio::video::TwilioError twilio_error);
    virtual void onParticipantConnected(twilio::video::Room *room,
                                        std::shared_ptr<twilio::video::Participant> participant);
    virtual void onParticipantDisconnected(twilio::video::Room *room,
                                           std::shared_ptr<twilio::video::Participant> participant);
    virtual void onRecordingStarted(twilio::video::Room *room);
    virtual void onRecordingStopped(twilio::video::Room *room);

private:
    JNIEnv *jni();

    bool isObserverValid(const std::string &callbackName);

    jobject createJavaRoomException(const twilio::video::TwilioError &twilio_error);

    jobject createJavaParticipantList(
            const std::map<std::string, std::shared_ptr<twilio::video::Participant>> participants);

    bool observer_deleted_ = false;
    mutable rtc::CriticalSection deletion_lock_;

    const webrtc_jni::ScopedGlobalRef<jobject> j_room_observer_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_room_observer_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_twilio_exception_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_participant_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_array_list_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_audio_track_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_video_track_class_;
    jmethodID j_on_connected_;
    jmethodID j_on_disconnected_;
    jmethodID j_on_connect_failure_;
    jmethodID j_on_participant_connected_;
    jmethodID j_on_participant_disconnected_;
    jmethodID j_on_recording_started_;
    jmethodID j_on_recording_stopped_;
    jmethodID j_get_handler_;
    jmethodID j_participant_ctor_id_;
    jmethodID j_array_list_ctor_id_;
    jmethodID j_array_list_add_;
    jmethodID j_audio_track_ctor_id_;
    jmethodID j_video_track_ctor_id_;
    jmethodID j_twilio_exception_ctor_id_;
};

}

#endif // VIDEO_ANDROID_ANDROID_ROOM_OBSERVER_H_
