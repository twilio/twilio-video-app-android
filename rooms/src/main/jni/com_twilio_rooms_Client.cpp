#include "com_twilio_rooms_Client.h"
#include "webrtc/api/java/jni/jni_helpers.h"

#include "AccessManager/AccessManager.h"
#include "TSCoreSDK.h"
#include "TSCLogger.h"
#include "TSCoreConstants.h"
#include "rooms.h"
#include "client_observer.h"

#include "webrtc/voice_engine/include/voe_base.h"
#include "webrtc/modules/video_capture/video_capture_internal.h"
#include "webrtc/api/java/jni/androidvideocapturer_jni.h"
#include "webrtc/modules/audio_device/android/audio_manager.h"
#include "webrtc/modules/audio_device/android/opensles_player.h"
#include "webrtc/api/java/jni/classreferenceholder.h"
#include "webrtc/media/engine/webrtcvideodecoderfactory.h"

#include <memory>

using namespace twiliosdk;
using namespace twilio;
using namespace webrtc_jni;
using cricket::WebRtcVideoDecoderFactory;
using cricket::WebRtcVideoEncoderFactory;

static bool media_jvm_set = false;

class AndroidClientObserver: public rooms::ClientObserver {
public:
    AndroidClientObserver(JNIEnv* env, jobject j_client_observer) :
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
        return webrtc_jni::AttachCurrentThreadIfNeeded();
    }

    const webrtc_jni::ScopedGlobalRef<jobject> j_client_observer_;

    bool observer_deleted_;
    mutable rtc::CriticalSection deletion_lock_;
};

class ClientDataHolder {
public:
    ClientDataHolder(std::unique_ptr<rooms::Client> client, std::shared_ptr<rooms::ClientObserver> client_observer, std::shared_ptr<TwilioCommon::AccessManager> access_manager, std::shared_ptr<media::MediaStack> media_stack) {
        client_ = std::move(client);
        client_observer_ = client_observer;
        access_manager_ = access_manager;
        media_stack_ = media_stack;
    }

private:
    std::unique_ptr<rooms::Client> client_;
    std::shared_ptr<rooms::ClientObserver> client_observer_;
    std::shared_ptr<TwilioCommon::AccessManager> access_manager_;
    std::shared_ptr<media::MediaStack> media_stack_;

};

extern "C" jint JNIEXPORT JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "JNI_OnLoad");
    jint ret = InitGlobalJniVariables(jvm);
    if (ret < 0) {
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                           kTSCoreLogLevelError,
                           "InitGlobalJniVariables() failed");
        return -1;
    }
    webrtc_jni::LoadGlobalClassReferenceHolder();

    return ret;
}

extern "C" void JNIEXPORT JNICALL JNI_OnUnLoad(JavaVM *jvm, void *reserved) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "JNI_OnUnload");
    webrtc_jni::FreeGlobalClassReferenceHolder();
}

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


JNIEXPORT jboolean JNICALL
Java_com_twilio_rooms_RoomsClient_nativeInitCore(JNIEnv *env, jobject instance, jobject context) {
    bool failure = false;

    if (!media_jvm_set) {
        failure |= webrtc::OpenSLESPlayer::SetAndroidAudioDeviceObjects(GetJVM(), context);
        failure |= webrtc::VoiceEngine::SetAndroidObjects(GetJVM(), context);
        failure |= webrtc_jni::AndroidVideoCapturerJni::SetAndroidObjects(env, context);
        media_jvm_set = true;
    }

    // TODO: Decide whether we need failure or success
    return failure ? JNI_FALSE : JNI_TRUE;
}

JNIEXPORT jlong JNICALL
Java_com_twilio_rooms_RoomsClient_nativeConnect(JNIEnv *env, jobject instance, jstring tokenString, jlong android_client_observer_handle) {

    twiliosdk::TSCSDK *sdk = new twiliosdk::TSCSDK();

    std::string token = webrtc_jni::JavaToStdString(env, tokenString);
    std::shared_ptr<TwilioCommon::AccessManager> access_manager = TwilioCommon::AccessManager::create(token, nullptr);

    AndroidClientObserver *android_client_observer = reinterpret_cast<AndroidClientObserver*>(android_client_observer_handle);
    std::shared_ptr<rooms::ClientObserver> client_observer(android_client_observer);

    std::shared_ptr<media::MediaStack> media_stack(new media::MediaStack());
    media_stack->start(media::MediaStackOptions());

    std::unique_ptr<rooms::Client> client = rooms::Client::create(access_manager,
                                                                  client_observer,
                                                                  media_stack,
                                                                  sdk->getInvoker());

    std::shared_ptr<rooms::RoomFuture> future = client->connect(nullptr);

    // TODO: properly delete holder
    new ClientDataHolder(std::move(client), client_observer, access_manager, media_stack);

    return 0;
}

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