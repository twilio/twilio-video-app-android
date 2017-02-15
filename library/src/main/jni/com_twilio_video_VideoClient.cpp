#include "com_twilio_video_VideoClient.h"
#include "webrtc/api/android/jni/jni_helpers.h"

#include "webrtc/base/refcount.h"
#include "webrtc/voice_engine/include/voe_base.h"
#include "webrtc/modules/audio_device/android/audio_manager.h"
#include "webrtc/modules/audio_device/android/opensles_player.h"
#include "webrtc/api/android/jni/classreferenceholder.h"

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
    jint ret = webrtc_jni::InitGlobalJniVariables(jvm);
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

twilio::video::NetworkChangeEvent getNetworkChangeEvent(jobject j_network_change_event) {
    JNIEnv* jni = webrtc_jni::AttachCurrentThreadIfNeeded();
    jclass j_network_change_event_class =
            twilio_video_jni::FindClass(jni, "com/twilio/video/VideoClient$NetworkChangeEvent");
    jmethodID name_method_id = webrtc_jni::GetMethodID(jni,
                                                       j_network_change_event_class,
                                                       "name",
                                                       "()Ljava/lang/String;");
    jstring connection_event_name = (jstring) jni->CallObjectMethod(j_network_change_event,
                                                                    name_method_id);
    std::string name = webrtc_jni::JavaToStdString(jni, connection_event_name);
    twilio::video::NetworkChangeEvent network_changed_event = twilio::video::kConnectionChanged;

    if (name == "CONNECTION_LOST") {
        network_changed_event = twilio::video::NetworkChangeEvent::kConnectionLost;
    } else if (name == "CONNECTION_CHANGED") {
        network_changed_event = twilio::video::NetworkChangeEvent::kConnectionChanged;
    } else {
        FATAL() << "Network change event could not translated";
    }

    return network_changed_event;
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
                                                     jstring j_token,
                                                     jlong media_factory_handle) {

    std::string token = webrtc_jni::JavaToStdString(env, j_token);
    std::shared_ptr<twilio::media::MediaFactory> media_factory =
            twilio_video_jni::getMediaFactory(media_factory_handle);

    AndroidPlatformInfoProvider *android_platform_info_provider =
            new AndroidPlatformInfoProvider(env, j_context);

    std::unique_ptr<twilio::video::ClientOptions> client_options = twilio::video::ClientOptions::Builder()
        .setPlatformInfoProvider(android_platform_info_provider)
        .build();

    std::unique_ptr<twilio::video::Client> client =
            twilio::video::Client::create(token,
                                          media_factory,
                                          std::move(client_options));

    return webrtc_jni::jlongFromPointer(new ClientContext(std::move(client)));
}

JNIEXPORT jlong JNICALL
Java_com_twilio_video_VideoClient_nativeConnect(JNIEnv *env,
                                                jobject j_instance,
                                                jlong j_client_context,
                                                jlong j_room_observer_context,
                                                jobject j_connect_options) {


    ClientContext *client_context =
            reinterpret_cast<ClientContext *>(j_client_context);

    RoomObserverContext *room_observer_context =
            reinterpret_cast<RoomObserverContext *>(j_room_observer_context);

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
                                                   room_observer_context->android_room_observer);
        delete connect_options_context;

    } else {
        room = client_context->getClient().connect(room_observer_context->android_room_observer);
    }


    RoomContext *room_context = new RoomContext();
    room_context->room = std::move(room);

    return webrtc_jni::jlongFromPointer(room_context);
}

JNIEXPORT jlong JNICALL Java_com_twilio_video_VideoClient_nativeUpdateToken
        (JNIEnv *env, jobject j_instance, jlong j_client_context, jstring j_token) {
    ClientContext *client_context =
            reinterpret_cast<ClientContext *>(j_client_context);
    std::string token = webrtc_jni::JavaToStdString(env, j_token);

    client_context->getClient().updateToken(token);
}

JNIEXPORT void JNICALL Java_com_twilio_video_VideoClient_nativeOnNetworkChange(JNIEnv *env,
                                                                               jobject j_instance,
                                                                               jlong j_client_context,
                                                                               jobject j_network_changed_event) {
    ClientContext *client_context = reinterpret_cast<ClientContext *>(j_client_context);
    twilio::video::NetworkChangeEvent networkChangeEvent =
            getNetworkChangeEvent(j_network_changed_event);
    client_context->getClient().onNetworkChange(networkChangeEvent);
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
