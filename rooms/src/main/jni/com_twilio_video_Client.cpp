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

JNIEXPORT void JNICALL Java_com_twilio_video_Client_nativeSetCoreLogLevel
    (JNIEnv *env, jobject instance, jint level) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "setCoreLogLevel");
    TSCoreLogLevel coreLogLevel = static_cast<TSCoreLogLevel>(level);
    TSCLogger::instance()->setLogLevel(coreLogLevel);
}

JNIEXPORT void JNICALL Java_com_twilio_video_Client_nativeSetModuleLevel
    (JNIEnv *env, jobject instance, jint module, jint level) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "setModuleLevel");
    TSCoreLogModule coreLogModule = static_cast<TSCoreLogModule>(module);
    TSCoreLogLevel coreLogLevel = static_cast<TSCoreLogLevel>(level);
    TSCLogger::instance()->setModuleLogLevel(coreLogModule, coreLogLevel);
}

JNIEXPORT jint JNICALL Java_com_twilio_video_Client_nativeGetCoreLogLevel
    (JNIEnv *env, jobject instance) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "getCoreLogLevel");
    return TSCLogger::instance()->getLogLevel();
}

JNIEXPORT jboolean JNICALL
Java_com_twilio_video_Client_nativeInitialize(JNIEnv *env, jobject instance, jobject context) {
    bool failure = false;

    // Setup media related Android device objects
    if (!media_jvm_set) {
        failure |= webrtc::OpenSLESPlayer::SetAndroidAudioDeviceObjects(GetJVM(), context);
        failure |= webrtc::VoiceEngine::SetAndroidObjects(GetJVM(), context);
        failure |= webrtc_jni::AndroidVideoCapturerJni::SetAndroidObjects(env, context);
        media_jvm_set = true;
    }

    return !failure;
}

class ClientDataContext {
public:
    ClientDataContext( std::unique_ptr<twilio::video::Client> client,
                   std::shared_ptr<twilio::media::MediaFactory> media_factory,
                   twilio::video::Invoker *invoker) {
        client_ = std::move(client);
        media_factory_ = media_factory;
        invoker_ = invoker;
    }

    virtual ~ClientDataContext(){
        delete invoker_;
    }

    twilio::video::Client &getClient() const {
        return *client_;
    }

    std::shared_ptr<twilio::media::MediaFactory> getMediaFactory() const {
        return media_factory_;
    }
 private:
    std::unique_ptr<twilio::video::Client> client_;
    std::shared_ptr<twilio::media::MediaFactory> media_factory_;
    twilio::video::Invoker *invoker_;
};

JNIEXPORT jlong JNICALL
Java_com_twilio_video_Client_nativeCreateClient(JNIEnv *env,
                                                jobject j_instance,
                                                jobject j_access_manager,
                                                jobject j_media_factory) {

    // Get native AccessManager shared_ptr from java object
    // Call the private method to get the native handle
    jclass j_access_mgr_class = webrtc_jni::GetObjectClass(env, j_access_manager);
    jmethodID j_getNativeHandle_id =
        webrtc_jni::GetMethodID(env, j_access_mgr_class, "getNativeHandle", "()J");
    jlong j_access_mgr_handle = env->CallLongMethod(j_access_manager, j_getNativeHandle_id);
    // Recreate a reference to the share_ptr of AccessManager by dereferencing the ptr
    // to the shared_ptr
    std::shared_ptr<TwilioCommon::AccessManager> access_manager =
        *(reinterpret_cast<std::shared_ptr<TwilioCommon::AccessManager> *>(j_access_mgr_handle));

    // TODO: Obtain media from j_media_factory once it gets implemented
    // Creating media here until then
    twilio::media::MediaOptions media_options;
    std::shared_ptr<twilio::media::MediaFactory> media_factory =
        twilio::media::MediaFactory::create(media_options);

    twilio::video::Invoker *invoker = new twilio::video::Invoker();

    std::unique_ptr<twilio::video::Client> client =
        twilio::video::Client::create(access_manager,
                                      media_factory,
                                      invoker);
    return jlongFromPointer(
        new ClientDataContext(std::move(client), media_factory, invoker));

}

JNIEXPORT jlong JNICALL
Java_com_twilio_video_Client_nativeConnect(JNIEnv *env,
                                           jobject j_instance,
                                           jlong j_client_data_context,
                                           jlong j_android_room_observer_handle,
                                           jstring j_name) {


    ClientDataContext* client_data_context =
        reinterpret_cast<ClientDataContext *>(j_client_data_context);

    std::shared_ptr<twilio::media::LocalMedia> local_media =
        client_data_context->getMediaFactory()->createLocalMedia();
    local_media->addAudioTrack(); // enabled, default constraints



    AndroidRoomObserver *android_room_observer =
        reinterpret_cast<AndroidRoomObserver *>(j_android_room_observer_handle);

    twilio::video::ConnectOptions::Builder connect_options_builder =
        twilio::video::ConnectOptions::Builder()
            .setCreateRoom(true)
            .setLocalMedia(local_media);
    if(!IsNull(env, j_name)) {
        std::string roomName = webrtc_jni::JavaToStdString(env, j_name);
        connect_options_builder.setRoomName(roomName);
    }

    std::unique_ptr<twilio::video::Room> room =
        client_data_context->getClient().connect(connect_options_builder.build(),
                                             android_room_observer);

    // TODO: instead of releasing ownership, create Room data context and wrap it up
    return jlongFromPointer(room.release());
}

JNIEXPORT jlong JNICALL
Java_com_twilio_video_Client_00024RoomListenerHandle_nativeCreate(JNIEnv *env,
                                                                       jobject instance,
                                                                       jobject object) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug,
                       "Create AndroidRoomObserver");
    AndroidRoomObserver *androidRoomObserver = new AndroidRoomObserver(env, object);
    return jlongFromPointer(androidRoomObserver);
}

JNIEXPORT void JNICALL
Java_com_twilio_video_Client_00024RoomListenerHandle_nativeFree(JNIEnv *env,
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
