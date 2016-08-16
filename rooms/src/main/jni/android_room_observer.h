#ifndef ROOMS_ANDROID_ANDROID_ROOM_OBSERVER_H
#define ROOMS_ANDROID_ANDROID_ROOM_OBSERVER_H

#include "webrtc/api/java/jni/jni_helpers.h"

#include "video/TSCLogger.h"
#include "video/room_observer.h"
#include "video/participant.h"

#include "com_twilio_video_Participant.h"

using namespace webrtc_jni;

class AndroidRoomObserver: public twilio::video::RoomObserver {
public:
    AndroidRoomObserver(JNIEnv *env, jobject j_room_observer) :
        j_room_observer_(env, j_room_observer),
        j_room_observer_class_(env, GetObjectClass(env, *j_room_observer_)),
        j_participant_class_(
            env, env->FindClass("com/twilio/video/Participant")),
        j_on_connected_(
            GetMethodID(env,
                        *j_room_observer_class_,
                        "onConnected",
                        "(Ljava/lang/String;)V")),
        j_on_disconnected_(
            GetMethodID(env,
                        *j_room_observer_class_,
                        "onDisconnected",
                        "(I)V")),
        j_on_connect_failure_(
            GetMethodID(env,
                        *j_room_observer_class_,
                        "onConnectFailure",
                        "(I)V")),
        j_on_participant_connected_(
            GetMethodID(env,
                        *j_room_observer_class_,
                        "onParticipantConnected",
                        "(Lcom/twilio/video/Participant;)V")),
        j_on_participant_disconnected_(
            GetMethodID(env,
                        *j_room_observer_class_,
                        "onParticipantDisconnected",
                        "(Ljava/lang/String;)V")),
        j_participant_ctor_id_(
            GetMethodID(env,
                        *j_participant_class_,
                        "<init>",
                        "(Ljava/lang/String;Ljava/lang/String;JJ)V"))
        {
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                           kTSCoreLogLevelDebug,
                           "AndroidRoomObserver");
    }

    ~AndroidRoomObserver() {
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                           kTSCoreLogLevelDebug,
                           "~AndroidRoomObserver");
    }

    void setObserverDeleted() {
        rtc::CritScope cs(&deletion_lock_);
        observer_deleted_ = true;
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                           kTSCoreLogLevelDebug,
                           "room observer deleted");
    }

protected:
    virtual void onConnected(twilio::video::Room *room) {
        ScopedLocalRefFrame local_ref_frame(jni());
        std::string func_name = std::string(__FUNCTION__);
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());

        {
            rtc::CritScope cs(&deletion_lock_);

            if (!isObserverValid(func_name)) {
                return;
            }

            jstring j_room_sid = webrtc_jni::JavaStringFromStdString(jni(), room->getSid());

            jni()->CallVoidMethod(*j_room_observer_, j_on_connected_, j_room_sid);
            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

    virtual void onDisconnected(const twilio::video::Room *room,
                                twilio::video::ClientError error_code =
                                    twilio::video::ClientError::kErrorUnknown) {
        ScopedLocalRefFrame local_ref_frame(jni());
        std::string func_name = std::string(__FUNCTION__);
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());
        {
            rtc::CritScope cs(&deletion_lock_);

            if (!isObserverValid(func_name)) {
                return;
            }
            jint j_error_code = (jint)error_code;
            jni()->CallVoidMethod(*j_room_observer_, j_on_disconnected_, j_error_code);
            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

    virtual void onConnectFailure(const twilio::video::Room *room,
                                  twilio::video::ClientError error_code) {
        ScopedLocalRefFrame local_ref_frame(jni());
        std::string func_name = std::string(__FUNCTION__);
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());
        {
            rtc::CritScope cs(&deletion_lock_);

            if (!isObserverValid(func_name)) {
                return;
            }
            jint j_error_code = (jint)error_code;
            jni()->CallVoidMethod(*j_room_observer_, j_on_connect_failure_, j_error_code);
            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

    virtual void onParticipantConnected(twilio::video::Room *room,
                                        std::shared_ptr<twilio::video::Participant> participant) {
        ScopedLocalRefFrame local_ref_frame(jni());
        std::string func_name = std::string(__FUNCTION__);
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());

        {
            rtc::CritScope cs(&deletion_lock_);

            if (!isObserverValid(func_name)) {
                return;
            }

            ParticipantContext *participant_context = new ParticipantContext();
            participant_context->participant = participant;
            jstring j_sid = webrtc_jni::JavaStringFromStdString(jni(), participant->getSid());
            jstring j_identity =
                webrtc_jni::JavaStringFromStdString(jni(), participant->getIdentity());
            jlong j_media_context = 0;
            jlong j_participant_context = webrtc_jni::jlongFromPointer(participant_context);
            jobject j_participant =
                jni()->NewObject(*j_participant_class_, j_participant_ctor_id_,
                                 j_identity, j_sid, j_media_context, j_participant_context);

            jni()->CallVoidMethod(*j_room_observer_,
                                  j_on_participant_connected_,
                                  j_participant);
            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

    virtual void onParticipantDisconnected(twilio::video::Room *room,
                                           std::shared_ptr<twilio::video::Participant> participant) {
        ScopedLocalRefFrame local_ref_frame(jni());
        std::string func_name = std::string(__FUNCTION__);
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());

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

private:
    JNIEnv *jni() {
        return webrtc_jni::AttachCurrentThreadIfNeeded();
    }

    bool isObserverValid(const std::string &callbackName) {
        if (observer_deleted_) {
            TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                               kTSCoreLogLevelWarning,
                               "room observer is marked for deletion, skipping %s callback",
                               callbackName.c_str());
            return false;
        };
        if (IsNull(jni(), *j_room_observer_)) {
            TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                               kTSCoreLogLevelWarning,
                               "room observer reference has been destroyed, skipping %s callback",
                               callbackName.c_str());
            return false;
        }
        return true;
    }

    bool observer_deleted_ = false;
    mutable rtc::CriticalSection deletion_lock_;

    const webrtc_jni::ScopedGlobalRef<jobject> j_room_observer_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_room_observer_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_participant_class_;
    jmethodID j_on_connected_;
    jmethodID j_on_disconnected_;
    jmethodID j_on_connect_failure_;
    jmethodID j_on_participant_connected_;
    jmethodID j_on_participant_disconnected_;
    jmethodID j_participant_ctor_id_;

};

#endif //ROOMS_ANDROID_ANDROID_ROOM_OBSERVER_H
