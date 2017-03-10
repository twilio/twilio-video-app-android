#ifndef VIDEO_ANDROID_ANDROID_ROOM_OBSERVER_H_
#define VIDEO_ANDROID_ANDROID_ROOM_OBSERVER_H_

#include "webrtc/api/android/jni/jni_helpers.h"

#include "video/logger.h"
#include "video/room_observer.h"
#include "video/participant.h"

#include "com_twilio_video_Participant.h"
#include "com_twilio_video_Media.h"
#include "class_reference_holder.h"

#include <vector>

class AndroidRoomObserver: public twilio::video::RoomObserver {
public:
    AndroidRoomObserver(JNIEnv *env, jobject j_room_observer) :
        j_room_observer_(env, j_room_observer),
        j_room_observer_class_(env, webrtc_jni::GetObjectClass(env, *j_room_observer_)),
        j_twilio_exception_class_(env, twilio_video_jni::FindClass(env, "com/twilio/video/TwilioException")),
        j_twilio_exception_ctor_id_(
                webrtc_jni::GetMethodID(env,
                            *j_twilio_exception_class_,
                            "<init>",
                            "(ILjava/lang/String;Ljava/lang/String;)V")),
        j_participant_class_(
            env, env->FindClass("com/twilio/video/Participant")),
        j_array_list_class_(env, env->FindClass("java/util/ArrayList")),
        j_audio_track_class_(env, env->FindClass("com/twilio/video/AudioTrack")),
        j_video_track_class_(env, env->FindClass("com/twilio/video/VideoTrack")),
        j_media_class_(env, env->FindClass("com/twilio/video/Media")),
        j_on_connected_(
            webrtc_jni::GetMethodID(env,
                        *j_room_observer_class_,
                        "onConnected",
                        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;)V")),
        j_on_disconnected_(
            webrtc_jni::GetMethodID(env,
                        *j_room_observer_class_,
                        "onDisconnected",
                        "(Lcom/twilio/video/TwilioException;)V")),
        j_on_connect_failure_(
            webrtc_jni::GetMethodID(env,
                        *j_room_observer_class_,
                        "onConnectFailure",
                        "(Lcom/twilio/video/TwilioException;)V")),
        j_on_participant_connected_(
            webrtc_jni::GetMethodID(env,
                        *j_room_observer_class_,
                        "onParticipantConnected",
                        "(Lcom/twilio/video/Participant;)V")),
        j_on_participant_disconnected_(
            webrtc_jni::GetMethodID(env,
                        *j_room_observer_class_,
                        "onParticipantDisconnected",
                        "(Ljava/lang/String;)V")),
        j_on_recording_started_(
            webrtc_jni::GetMethodID(env,
                        *j_room_observer_class_,
                        "onRecordingStarted",
                        "()V")),
        j_on_recording_stopped_(
            webrtc_jni::GetMethodID(env,
                        *j_room_observer_class_,
                        "onRecordingStopped",
                        "()V")),
        j_get_handler_(
            webrtc_jni::GetMethodID(env,
                        *j_room_observer_class_,
                        "getHandler",
                        "()Landroid/os/Handler;")),
        j_participant_ctor_id_(
            webrtc_jni::GetMethodID(env,
                        *j_participant_class_,
                        "<init>",
                        "(Ljava/lang/String;Ljava/lang/String;Lcom/twilio/video/Media;J)V")),
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
        j_audio_track_ctor_id_(
            webrtc_jni::GetMethodID(env,
                                    *j_audio_track_class_,
                                    "<init>",
                                    "(Ljava/lang/String;Z)V")),
        j_video_track_ctor_id_(
            webrtc_jni::GetMethodID(env,
                                    *j_video_track_class_,
                                    "<init>",
                                    "(Lorg/webrtc/VideoTrack;)V")),
        j_media_ctor_id_(
            webrtc_jni::GetMethodID(env,
                                    *j_media_class_,
                                    "<init>",
                                    "(JLjava/util/List;Ljava/util/List;Landroid/os/Handler;)V"))
        {
        TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                           twilio::video::kTSCoreLogLevelDebug,
                           "AndroidRoomObserver");
    }

    ~AndroidRoomObserver() {
        TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                           twilio::video::kTSCoreLogLevelDebug,
                           "~AndroidRoomObserver");
    }

    void setObserverDeleted() {
        rtc::CritScope cs(&deletion_lock_);
        observer_deleted_ = true;
        TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                           twilio::video::kTSCoreLogLevelDebug,
                           "room observer deleted");
    }

protected:
    virtual void onConnected(twilio::video::Room *room) {
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

            jstring j_room_sid = webrtc_jni::JavaStringFromStdString(jni(), room->getSid());

            std::shared_ptr<twilio::video::LocalParticipant> local_participant =
                    room->getLocalParticipant();
            jstring j_local_participant_sid =
                    webrtc_jni::JavaStringFromStdString(jni(), local_participant->getSid());
            jstring j_local_participant_identity =
                    webrtc_jni::JavaStringFromStdString(jni(), local_participant->getIdentity());

            jobject j_participants = createJavaParticipantList(room->getParticipants());

            jni()->CallVoidMethod(*j_room_observer_,
                                  j_on_connected_,
                                  j_room_sid,
                                  j_local_participant_sid,
                                  j_local_participant_identity,
                                  j_participants);
            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

    virtual void onDisconnected(const twilio::video::Room *room,
                                std::unique_ptr<twilio::video::TwilioError> twilio_error) {
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

            jobject j_twilio_exception = nullptr;
            if (twilio_error != nullptr) {
                j_twilio_exception = createJavaRoomException(*twilio_error);
            }
            jni()->CallVoidMethod(*j_room_observer_, j_on_disconnected_, j_twilio_exception);
            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

    virtual void onConnectFailure(const twilio::video::Room *room,
                                  const twilio::video::TwilioError twilio_error) {
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

            jobject j_room_exception = createJavaRoomException(twilio_error);
            jni()->CallVoidMethod(*j_room_observer_, j_on_connect_failure_, j_room_exception);
            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

    virtual void onParticipantConnected(twilio::video::Room *room,
                                        std::shared_ptr<twilio::video::Participant> participant) {
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


            jobject j_media = createJavaMediaObject(participant->getMedia());

            // Create participant
            jobject j_participant = createJavaParticipant(jni(),
                                                          participant,
                                                          j_media,
                                                          *j_participant_class_,
                                                          j_participant_ctor_id_);

            jni()->CallVoidMethod(*j_room_observer_,
                                  j_on_participant_connected_,
                                  j_participant);
            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

    virtual void onParticipantDisconnected(twilio::video::Room *room,
                                           std::shared_ptr<twilio::video::Participant> participant) {
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

            jstring j_sid = webrtc_jni::JavaStringFromStdString(jni(), participant->getSid());
            jni()->CallVoidMethod(*j_room_observer_,
                                  j_on_participant_disconnected_,
                                  j_sid);

            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

    virtual void onRecordingStarted(twilio::video::Room *room) {
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

            jni()->CallVoidMethod(*j_room_observer_,
                                  j_on_recording_started_);

            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

    virtual void onRecordingStopped(twilio::video::Room *room) {
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

            jni()->CallVoidMethod(*j_room_observer_,
                                  j_on_recording_stopped_);

            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
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
                               "room observer is marked for deletion, skipping %s callback",
                               callbackName.c_str());
            return false;
        };
        if (webrtc_jni::IsNull(jni(), *j_room_observer_)) {
            TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                               twilio::video::kTSCoreLogLevelWarning,
                               "room observer reference has been destroyed, skipping %s callback",
                               callbackName.c_str());
            return false;
        }
        return true;
    }

    jobject createJavaRoomException(const twilio::video::TwilioError &twilio_error) {
        return jni()->NewObject(*j_twilio_exception_class_,
                                j_twilio_exception_ctor_id_,
                                twilio_error.getCode(),
                                webrtc_jni::JavaStringFromStdString(jni(), twilio_error.getMessage()),
                                webrtc_jni::JavaStringFromStdString(jni(), twilio_error.getExplanation()));
    }

    jobject createJavaMediaObject(std::shared_ptr<twilio::media::Media> media) {
        // Create media context
        MediaContext *media_context = new MediaContext();
        media_context->media = media;
        jlong j_media_context = webrtc_jni::jlongFromPointer(media_context);

        // Create ArrayList<AudioTrack>
        jobject j_audio_tracks = jni()->NewObject(*j_array_list_class_, j_array_list_ctor_id_);

        const std::vector<std::shared_ptr<twilio::media::AudioTrack>> audio_tracks =
                media->getAudioTracks();

        // Add audio tracks to array list
        for (int i = 0; i < audio_tracks.size(); i++) {
            jobject j_audio_track =
                    createJavaAudioTrack(jni(), audio_tracks[i],
                                         *j_audio_track_class_, j_audio_track_ctor_id_);
            jni()->CallVoidMethod(j_audio_tracks, j_array_list_add_, j_audio_track);
        }

        // Create ArrayList<VideoTracks>
        jobject j_video_tracks = jni()->NewObject(*j_array_list_class_, j_array_list_ctor_id_);

        const std::vector<std::shared_ptr<twilio::media::VideoTrack>> video_tracks =
                media->getVideoTracks();

        // Add audio tracks to array list
        for (int i = 0; i < video_tracks.size(); i++) {
            jobject j_video_track =
                    createJavaVideoTrack(jni(), video_tracks[i],
                                         *j_video_track_class_, j_video_track_ctor_id_);
            jni()->CallVoidMethod(j_video_tracks, j_array_list_add_, j_video_track);
        }

        jobject j_handler = jni()->CallObjectMethod(*j_room_observer_, j_get_handler_);

        // Create java media object
        return jni()->NewObject(
                *j_media_class_, j_media_ctor_id_, j_media_context,
                j_audio_tracks, j_video_tracks, j_handler);

    }

    jobject createJavaParticipantList(
            const std::map<std::string, std::shared_ptr<twilio::video::Participant>> participants) {

        // Create ArrayList<Participant>
        jobject j_participants = jni()->NewObject(*j_array_list_class_, j_array_list_ctor_id_);

        std::map<std::string, std::shared_ptr<twilio::video::Participant>>::const_iterator it;
        for (it = participants.begin(); it != participants.end(); ++it) {
            std::shared_ptr<twilio::video::Participant> participant = it->second;

            jobject j_media = createJavaMediaObject(participant->getMedia());


            jobject j_participant = createJavaParticipant(jni(),
                                                          participant,
                                                          j_media,
                                                          *j_participant_class_,
                                                          j_participant_ctor_id_);
            jni()->CallBooleanMethod(j_participants, j_array_list_add_, j_participant);
        }
        return j_participants;
    }

    bool observer_deleted_ = false;
    mutable rtc::CriticalSection deletion_lock_;

    const webrtc_jni::ScopedGlobalRef<jobject> j_room_observer_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_room_observer_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_twilio_exception_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_participant_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_array_list_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_audio_track_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_video_track_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_media_class_;
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
    jmethodID j_media_ctor_id_;
    jmethodID j_twilio_exception_ctor_id_;
};

#endif // VIDEO_ANDROID_ANDROID_ROOM_OBSERVER_H_
