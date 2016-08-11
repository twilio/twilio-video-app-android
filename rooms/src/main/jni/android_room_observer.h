#ifndef ROOMS_ANDROID_ANDROID_ROOM_OBSERVER_H
#define ROOMS_ANDROID_ANDROID_ROOM_OBSERVER_H

#include "webrtc/api/java/jni/jni_helpers.h"

#include "TSCLogger.h"
#include "room_observer.h"
#include "participant.h"

#include "com_twilio_video_Participant.h"

using namespace webrtc_jni;

class AndroidRoomObserver: public twilio::video::RoomObserver {
public:
    AndroidRoomObserver(JNIEnv *env, jobject j_room_observer) :
        j_room_observer_(env, j_room_observer),
        j_room_observer_class_(env, GetObjectClass(env, *j_room_observer_)),
        j_on_connected_(
            GetMethodID(env,
                        *j_room_observer_class_,
                        "onConnected",
                        "()V")),
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
                        "(J)V")),
        j_on_participant_disconnected_(
            GetMethodID(env,
                        *j_room_observer_class_,
                        "onParticipantDisconnected",
                        "(J)V")) {
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

            jni()->CallVoidMethod(*j_room_observer_, j_on_connected_);
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

            ParticipantDataContext *participant_dc = new ParticipantDataContext();
            participant_dc->participant = participant;
            jni()->CallVoidMethod(*j_room_observer_,
                                  j_on_participant_connected_,
                                  jlongFromPointer(participant_dc));
            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

    virtual void onParticipantDisconnected(twilio::video::Room *room,
                                           std::shared_ptr<const twilio::video::Participant> participant) {
        ScopedLocalRefFrame local_ref_frame(jni());
        std::string func_name = std::string(__FUNCTION__);
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());

        {
            rtc::CritScope cs(&deletion_lock_);

            if (!isObserverValid(func_name)) {
                return;
            }

            // TODO: Implement me

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
    jmethodID j_on_connected_;
    jmethodID j_on_disconnected_;
    jmethodID j_on_connect_failure_;
    jmethodID j_on_participant_connected_;
    jmethodID j_on_participant_disconnected_;

};

#endif //ROOMS_ANDROID_ANDROID_ROOM_OBSERVER_H
