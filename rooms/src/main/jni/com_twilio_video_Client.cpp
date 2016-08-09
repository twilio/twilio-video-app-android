#include "com_twilio_video_Client.h"
#include "webrtc/api/java/jni/jni_helpers.h"



#include "webrtc/base/refcount.h"
#include "webrtc/voice_engine/include/voe_base.h"
#include "webrtc/modules/video_capture/video_capture_internal.h"
#include "webrtc/api/java/jni/androidvideocapturer_jni.h"
#include "webrtc/modules/audio_device/android/audio_manager.h"
#include "webrtc/modules/audio_device/android/opensles_player.h"
#include "webrtc/api/java/jni/classreferenceholder.h"
#include "webrtc/media/engine/webrtcvideodecoderfactory.h"

#include "AccessManager/AccessManager.h"
#include "TSCLogger.h"
#include "video.h"
#include "media/media_factory.h"
#include "android_platform_info_provider.h"
#include "android_video_codec_manager.h"
#include "android_room_observer.h"
#include "connect_options.h"

#include <memory>

using namespace twiliosdk;
using namespace webrtc_jni;

static bool media_jvm_set = false;

extern "C" jint JNIEXPORT JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());
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
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());
    webrtc_jni::FreeGlobalClassReferenceHolder();
}

JNIEXPORT void JNICALL Java_com_twilio_video_RoomsClient_nativeSetCoreLogLevel
    (JNIEnv *env, jobject instance, jint level) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "setCoreLogLevel");
    TSCoreLogLevel coreLogLevel = static_cast<TSCoreLogLevel>(level);
    TSCLogger::instance()->setLogLevel(coreLogLevel);
}

JNIEXPORT void JNICALL Java_com_twilio_video_RoomsClient_nativeSetModuleLevel
    (JNIEnv *env, jobject instance, jint module, jint level) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "setModuleLevel");
    TSCoreLogModule coreLogModule = static_cast<TSCoreLogModule>(module);
    TSCoreLogLevel coreLogLevel = static_cast<TSCoreLogLevel>(level);
    TSCLogger::instance()->setModuleLogLevel(coreLogModule, coreLogLevel);
}

JNIEXPORT jint JNICALL Java_com_twilio_video_RoomsClient_nativeGetCoreLogLevel
    (JNIEnv *env, jobject instance) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "getCoreLogLevel");
    return TSCLogger::instance()->getLogLevel();
}

JNIEXPORT jboolean JNICALL
Java_com_twilio_video_RoomsClient_nativeInitialize(JNIEnv *env, jobject instance, jobject context) {
    bool failure = false;

    // Setup media related Android device objects
    if (!media_jvm_set) {
        failure |= webrtc::OpenSLESPlayer::SetAndroidAudioDeviceObjects(GetJVM(), context);
        failure |= webrtc::VoiceEngine::SetAndroidObjects(GetJVM(), context);
        failure |= webrtc_jni::AndroidVideoCapturerJni::SetAndroidObjects(env, context);
        media_jvm_set = true;
    }

    return failure ? JNI_FALSE : JNI_TRUE;
}

class ClientDataHolder {
public:
    ClientDataHolder(std::unique_ptr<twilio::video::Client> client,
                     std::shared_ptr<TwilioCommon::AccessManager> access_manager,
                     std::shared_ptr<twilio::media::MediaFactory> media_factory) {
        client_ = std::move(client);
        access_manager_ = access_manager;
        media_factory_ = media_factory;
    }

    std::unique_ptr<twilio::video::Client> client_;
    std::shared_ptr<TwilioCommon::AccessManager> access_manager_;
    std::shared_ptr<twilio::media::MediaFactory> media_factory_;

};

JNIEXPORT jlong JNICALL
Java_com_twilio_video_RoomsClient_nativeCreateClient(JNIEnv *env,
                                                     jobject instance,
                                                     jstring token_string) {
    // Create client
    std::string token = webrtc_jni::JavaToStdString(env, token_string);
    std::shared_ptr<TwilioCommon::AccessManager>
        access_manager = TwilioCommon::AccessManager::create(token, nullptr);

    // Create a MediaFactory
    twilio::media::MediaOptions media_options;
    std::shared_ptr<twilio::media::MediaFactory> media_factory =
        twilio::media::MediaFactory::create(media_options);

    twilio::video::Invoker *invoker = new twilio::video::Invoker();

    std::unique_ptr<twilio::video::Client> client =
        twilio::video::Client::create(access_manager,
                                      media_factory,
                                      invoker);
    // TODO: Once we have access manager and media factory properly implemented in java layer,
    // we won't be needing ClientDataHolder
    return jlongFromPointer(
        new ClientDataHolder(std::move(client), access_manager, media_factory));

}

JNIEXPORT jlong JNICALL
Java_com_twilio_video_RoomsClient_nativeConnect(JNIEnv *env,
                                                jobject instance,
                                                jlong nativeDataHolder,
                                                jlong android_room_observer_handle,
                                                jstring name) {


    ClientDataHolder* client_data_holder =
        reinterpret_cast<ClientDataHolder *>(nativeDataHolder);

    std::shared_ptr<twilio::media::LocalMedia> local_media =
        client_data_holder->media_factory_->createLocalMedia();
    local_media->addAudioTrack(); // enabled, default constraints



    AndroidRoomObserver *android_room_observer =
        reinterpret_cast<AndroidRoomObserver *>(android_room_observer_handle);

    twilio::video::ConnectOptions::Builder connect_options_builder =
        twilio::video::ConnectOptions::Builder().setCreateRoom(true).setLocalMedia(local_media);
    if(!IsNull(env, name)) {
        std::string roomName = webrtc_jni::JavaToStdString(env, name);
        connect_options_builder.setRoomName(roomName);
    }

    std::unique_ptr<twilio::video::Room> room =
        client_data_holder->client_->connect(connect_options_builder.build(),
                                             android_room_observer);

    return jlongFromPointer(room.release());
}

JNIEXPORT jlong JNICALL
Java_com_twilio_video_RoomsClient_00024RoomListenerHandle_nativeCreate(JNIEnv *env,
                                                                       jobject instance,
                                                                       jobject object) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug,
                       "Create AndroidRoomObserver");
    AndroidRoomObserver *androidRoomObserver = new AndroidRoomObserver(env, object);
    return jlongFromPointer(androidRoomObserver);
}

JNIEXPORT void JNICALL
Java_com_twilio_video_RoomsClient_00024RoomListenerHandle_nativeFree(JNIEnv *env,
                                                                     jobject instance,
                                                                     jlong nativeHandle) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug,
                       "Free AndroidRoomObserver");
    AndroidRoomObserver
        *androidRoomObserver = reinterpret_cast<AndroidRoomObserver *>(nativeHandle);
    if (androidRoomObserver != nullptr) {
        androidRoomObserver->setObserverDeleted();
        delete androidRoomObserver;
    }
}
