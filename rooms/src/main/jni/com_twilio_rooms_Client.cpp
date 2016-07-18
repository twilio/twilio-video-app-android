#include "com_twilio_rooms_Client.h"
#include "webrtc/api/java/jni/jni_helpers.h"

#include "AccessManager/AccessManager.h"
#include "TSCoreSDK.h"
#include "TSCLogger.h"
#include "TSCoreConstants.h"
#include "rooms.h"
#include "client_observer.h"

using namespace twiliosdk;
using namespace twilio;
using namespace webrtc_jni;


JNIEXPORT void JNICALL Java_com_twilio_rooms_RoomsClient_nativeSetCoreLogLevel
        (JNIEnv *, jobject, jint) {
    // TODO: implement me
}

JNIEXPORT void JNICALL Java_com_twilio_rooms_RoomsClient_nativeSetModuleLevel
        (JNIEnv *, jobject, jint, jint) {
    // TODO: implement me
}

JNIEXPORT jint JNICALL Java_com_twilio_rooms_RoomsClient_nativeGetCoreLogLevel
        (JNIEnv *, jobject) {
    return 0;
}

JNIEXPORT jlong JNICALL
Java_com_twilio_rooms_RoomsClient_nativeConnect(JNIEnv *env, jobject instance, jstring tokenString, jlong nativeClientObserver) {
    std::string token = webrtc_jni::JavaToStdString(env, tokenString);

    std::shared_ptr<TwilioCommon::AccessManager> access_manager = TwilioCommon::AccessManager::create(token,
                                                                                                      nullptr);

    // Create a MediaStack
    std::shared_ptr<media::MediaStack> stack(new media::MediaStack());

    // Create a TSCSDK - Needed for Resip global state
    twiliosdk::TSCSDK *sdk = new twiliosdk::TSCSDK();

    /**
    rooms::ClientObserver *clientObserver = reinterpret_cast<rooms::ClientObserver*>(nativeClientObserver);
    std::shared_ptr<rooms::ClientObserver> observer(clientObserver);

    std::unique_ptr<rooms::Client> client = rooms::Client::create(access_manager,
                                                                  observer,
                                                                  stack,
                                                                  sdk->getInvoker());

    // std::shared_ptr<rooms::RoomFuture> future = client->connect(nullptr);
    **/

    return 0;
}

class AndroidClientObserver: public rooms::ClientObserver {
public:
    AndroidClientObserver(JNIEnv* env, jobject j_client_observer) :
            j_client_observer_(env, j_client_observer) {}

    ~AndroidClientObserver() {}

    void setObserverDeleted() {
        rtc::CritScope cs(&deletion_lock_);
        observer_deleted_ = true;
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                           kTSCoreLogLevelDebug,
                           "client observer deleted");
    }

protected:
    virtual void onConnected(std::shared_ptr<rooms::Room> room) {
        ScopedLocalRefFrame local_ref_frame(jni());
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                           kTSCoreLogLevelDebug,
                           "onConnected");
    }

    virtual void onDisconnected(std::shared_ptr<const rooms::Room> room,
                                rooms::ClientError error_code = rooms::ClientError::kErrorUnknown) {
        ScopedLocalRefFrame local_ref_frame(jni());
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                           kTSCoreLogLevelDebug,
                           "onDisconnected");
    }

    virtual void onConnectFailure(std::string name_or_sid, rooms::ClientError error_code) {
        ScopedLocalRefFrame local_ref_frame(jni());
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                           kTSCoreLogLevelDebug,
                           "onConnectFailure");
    }

private:
    JNIEnv* jni() {
        return AttachCurrentThreadIfNeeded();
    }

    const ScopedGlobalRef<jobject> j_client_observer_;

    bool observer_deleted_;
    mutable rtc::CriticalSection deletion_lock_;
};

JNIEXPORT jlong JNICALL
Java_com_twilio_rooms_RoomsClient_00024ClientListenerHandle_nativeCreate(JNIEnv *env, jobject instance,
                                                                    jobject object) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "Create native ClientObserver");
    AndroidClientObserver* androidClientObserver = new AndroidClientObserver(env, object);
    return jlongFromPointer(androidClientObserver);
}

JNIEXPORT void JNICALL
Java_com_twilio_rooms_RoomsClient_00024ClientListenerHandle_nativeFree(JNIEnv *env, jobject instance,
                                                                  jlong nativeHandle) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "Free native ClientObserver");
    AndroidClientObserver *androidClientObserver = reinterpret_cast<AndroidClientObserver*>(nativeHandle);
    if(androidClientObserver != nullptr) {
        androidClientObserver->setObserverDeleted();
        delete androidClientObserver;
    }
}