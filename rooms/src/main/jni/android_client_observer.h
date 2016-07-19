#ifndef ROOMS_ANDROID_ANDROID_CLIENT_OBSERVER_H
#define ROOMS_ANDROID_ANDROID_CLIENT_OBSERVER_H

#include "webrtc/api/java/jni/jni_helpers.h"

#include "TSCLogger.h"
#include "TSCoreConstants.h"
#include "client_observer.h"


class AndroidClientObserver: public twilio::rooms::ClientObserver {
public:
    AndroidClientObserver(JNIEnv *env, jobject j_client_observer) :
        j_client_observer_(env, j_client_observer) {
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
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                           kTSCoreLogLevelDebug,
                           "onConnected");
    }

    virtual void onDisconnected(std::shared_ptr<const twilio::rooms::Room> room,
                                twilio::rooms::ClientError error_code = twilio::rooms::ClientError::kErrorUnknown) {
        ScopedLocalRefFrame local_ref_frame(jni());
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                           kTSCoreLogLevelDebug,
                           "onDisconnected");
    }

    virtual void onConnectFailure(std::string name_or_sid, twilio::rooms::ClientError error_code) {
        ScopedLocalRefFrame local_ref_frame(jni());
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                           kTSCoreLogLevelDebug,
                           "onConnectFailure");
    }

private:
    JNIEnv *jni() {
        return webrtc_jni::AttachCurrentThreadIfNeeded();
    }

    const webrtc_jni::ScopedGlobalRef <jobject> j_client_observer_;

    bool observer_deleted_;
    mutable rtc::CriticalSection deletion_lock_;
};

#endif //ROOMS_ANDROID_ANDROID_CLIENT_OBSERVER_H
