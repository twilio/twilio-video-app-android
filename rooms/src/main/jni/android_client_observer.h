#ifndef ROOMS_ANDROID_ANDROID_CLIENT_OBSERVER_H
#define ROOMS_ANDROID_ANDROID_CLIENT_OBSERVER_H

#include "webrtc/api/java/jni/jni_helpers.h"

#include "TSCLogger.h"
#include "TSCoreConstants.h"
#include "client_observer.h"


class AndroidClientObserver: public twilio::rooms::ClientObserver {
public:
    AndroidClientObserver(JNIEnv *env, jobject j_client_observer) :
        j_client_observer_(env, j_client_observer),
        j_client_observer_class_(env, GetObjectClass(env, *j_client_observer_)),
        j_on_connected_(
            GetMethodID(env,
                        *j_client_observer_class_,
                        "onConnected",
                        "(Lcom/twilio/rooms/Room;)V")) {
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                           kTSCoreLogLevelDebug,
                           "AndroidClientObserver");
    }

    ~AndroidClientObserver() {
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                           kTSCoreLogLevelDebug,
                           "~AndroidClientObserver");
    }

    void setObserverDeleted() {
        rtc::CritScope cs(&deletion_lock_);
        observer_deleted_ = true;
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                           kTSCoreLogLevelDebug,
                           "client observer deleted");
    }

protected:
    virtual void onConnected(std::shared_ptr<twilio::rooms::Room> room) {
        ScopedLocalRefFrame local_ref_frame(jni());
        std::string func_name = std::string(__FUNCTION__);
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());

        {
            rtc::CritScope cs(&deletion_lock_);

            if (!isObserverValid(func_name)) {
                return;
            }

            jni()->CallVoidMethod(*j_client_observer_, j_on_connected_, nullptr);
            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

    virtual void onDisconnected(std::shared_ptr<const twilio::rooms::Room> room,
                                twilio::rooms::ClientError error_code = twilio::rooms::ClientError::kErrorUnknown) {
        ScopedLocalRefFrame local_ref_frame(jni());
        std::string func_name = std::string(__FUNCTION__);
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());
        // TODO: implement me
    }

    virtual void onConnectFailure(std::string name_or_sid, twilio::rooms::ClientError error_code) {
        ScopedLocalRefFrame local_ref_frame(jni());
        std::string func_name = std::string(__FUNCTION__);
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());
        // TODO: implement me
    }

private:
    JNIEnv *jni() {
        return webrtc_jni::AttachCurrentThreadIfNeeded();
    }

    bool isObserverValid(const std::string &callbackName) {
        if (observer_deleted_) {
            TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                               kTSCoreLogLevelWarning,
                               "client observer is marked for deletion, skipping %s callback",
                               callbackName.c_str());
            return false;
        };
        if (IsNull(jni(), *j_client_observer_)) {
            TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                               kTSCoreLogLevelWarning,
                               "client observer reference has been destroyed, skipping %s callback",
                               callbackName.c_str());
            return false;
        }
        return true;
    }

    bool observer_deleted_ = false;
    mutable rtc::CriticalSection deletion_lock_;

    const webrtc_jni::ScopedGlobalRef<jobject> j_client_observer_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_client_observer_class_;
    jmethodID j_on_connected_;

};

#endif //ROOMS_ANDROID_ANDROID_CLIENT_OBSERVER_H
