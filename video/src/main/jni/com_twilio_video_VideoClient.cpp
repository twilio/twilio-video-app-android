#include "com_twilio_video_VideoClient.h"
#include "webrtc/api/java/jni/jni_helpers.h"

#include "webrtc/base/refcount.h"
#include "webrtc/voice_engine/include/voe_base.h"
#include "webrtc/modules/video_capture/video_capture_internal.h"
#include "webrtc/api/java/jni/androidvideocapturer_jni.h"
#include "webrtc/modules/audio_device/android/audio_manager.h"
#include "webrtc/modules/audio_device/android/opensles_player.h"
#include "webrtc/api/java/jni/classreferenceholder.h"

#include "common/AccessManager/AccessManager.h"
#include "video/logger.h"
#include "video/video.h"
#include "video/connect_options.h"
#include "video/client_options.h"
#include "media/media_factory.h"

#include "android_platform_info_provider.h"
#include "android_room_observer.h"
#include "com_twilio_video_ConnectOptions.h"
#include "com_twilio_video_Room.h"
#include "com_twilio_video_MediaFactory.h"
#include "class_reference_holder.h"

#include <memory>

namespace twilio_video_jni {

static bool media_jvm_set = false;

extern "C" jint JNIEXPORT JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "%s", func_name.c_str());
    jint ret = InitGlobalJniVariables(jvm);
    if (ret < 0) {
        TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                           twilio::video::kTSCoreLogLevelError,
                           "InitGlobalJniVariables() failed");
        return -1;
    }
    webrtc_jni::LoadGlobalClassReferenceHolder();
    twilio_video_jni::LoadGlobalClassReferenceHolder();

    return ret;
}

extern "C" void JNIEXPORT JNICALL JNI_OnUnLoad(JavaVM *jvm, void *reserved) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "%s", func_name.c_str());
    twilio_video_jni::FreeGlobalClassReferenceHolder();
    webrtc_jni::FreeGlobalClassReferenceHolder();
}

JNIEXPORT void JNICALL Java_com_twilio_video_VideoClient_nativeSetCoreLogLevel
        (JNIEnv *env, jobject instance, jint level) {
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "setCoreLogLevel");
    twilio::video::TSCoreLogLevel coreLogLevel = static_cast<twilio::video::TSCoreLogLevel>(level);
    twilio::video::Logger::instance()->setLogLevel(coreLogLevel);
}

JNIEXPORT void JNICALL Java_com_twilio_video_VideoClient_nativeSetModuleLevel
        (JNIEnv *env, jobject instance, jint module, jint level) {
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "setModuleLevel");
    twilio::video::TSCoreLogModule coreLogModule = static_cast<twilio::video::TSCoreLogModule>(module);
    twilio::video::TSCoreLogLevel coreLogLevel = static_cast<twilio::video::TSCoreLogLevel>(level);
    twilio::video::Logger::instance()->setModuleLogLevel(coreLogModule, coreLogLevel);
}

JNIEXPORT jint JNICALL Java_com_twilio_video_VideoClient_nativeGetCoreLogLevel
        (JNIEnv *env, jobject instance) {
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "getCoreLogLevel");
    return twilio::video::Logger::instance()->getLogLevel();
}

class ClientContext {
public:
    ClientContext(std::unique_ptr<twilio::video::Client> client) {
        client_ = std::move(client);
    }

    virtual ~ClientContext() {
    }

    twilio::video::Client &getClient() const {
        return *client_;
    }


private:
    std::unique_ptr<twilio::video::Client> client_;
};

JNIEXPORT jlong JNICALL
Java_com_twilio_video_VideoClient_nativeCreateClient(JNIEnv *env,
                                                     jobject j_instance,
                                                     jobject j_context,
                                                     jobject j_access_manager,
                                                     jlong media_factory_handle) {

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

    std::shared_ptr<twilio::media::MediaFactory> media_factory =
            twilio_video_jni::getMediaFactory(media_factory_handle);

    AndroidPlatformInfoProvider *android_platform_info_provider =
            new AndroidPlatformInfoProvider(env, j_context);

    std::unique_ptr<twilio::video::ClientOptions> client_options = twilio::video::ClientOptions::Builder()
        .setPlatformInfoProvider(android_platform_info_provider)
        .build();

    std::unique_ptr<twilio::video::Client> client =
            twilio::video::Client::create(access_manager,
                                          media_factory,
                                          std::move(client_options));
    return jlongFromPointer(
            new ClientContext(std::move(client)));

}

JNIEXPORT jlong JNICALL
Java_com_twilio_video_VideoClient_nativeConnect(JNIEnv *env,
                                                jobject j_instance,
                                                jlong j_client_context,
                                                jlong j_android_room_observer_handle,
                                                jobject j_connect_options) {


    ClientContext *client_context =
            reinterpret_cast<ClientContext *>(j_client_context);

    AndroidRoomObserver *android_room_observer =
            reinterpret_cast<AndroidRoomObserver *>(j_android_room_observer_handle);

    std::unique_ptr<twilio::video::Room> room;

    bool have_connect_options = !webrtc_jni::IsNull(env, j_connect_options);
    if (have_connect_options) {
        // Get connect options
        jclass j_connect_options_class = webrtc_jni::GetObjectClass(env, j_connect_options);
        jmethodID j_createNativeObject_id =
                webrtc_jni::GetMethodID(env, j_connect_options_class,
                                        "createNativeObject", "()J");
        jlong j_connect_options_handle =
                env->CallLongMethod(j_connect_options, j_createNativeObject_id);
        ConnectOptionsContext *connect_options_context =
                reinterpret_cast<ConnectOptionsContext *>(j_connect_options_handle);
        room = client_context->getClient().connect(connect_options_context->connect_options,
                                                   android_room_observer);
        delete connect_options_context;

    } else {
        room = client_context->getClient().connect(android_room_observer);
    }


    RoomContext *room_context = new RoomContext();
    room_context->room = std::move(room);
    return jlongFromPointer(room_context);
}

JNIEXPORT void JNICALL
Java_com_twilio_video_VideoClient_nativeRelease(JNIEnv *env,
                                                jobject j_instance,
                                                jlong j_client_context) {
    ClientContext *client_context = reinterpret_cast<ClientContext *>(j_client_context);
    if (client_context != nullptr) {
        delete client_context;
    }

}

}
