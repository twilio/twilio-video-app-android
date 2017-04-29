#include "com_twilio_video_Room.h"
#include "webrtc/sdk/android/src/jni/jni_helpers.h"

#include "video/logger.h"
#include "video/video.h"
#include "media/media_factory.h"
#include "android_room_observer.h"
#include "android_stats_observer.h"
#include "com_twilio_video_ConnectOptions.h"
#include "com_twilio_video_MediaFactory.h"
#include "class_reference_holder.h"

namespace twilio_video_jni {

JNIEXPORT jlong JNICALL Java_com_twilio_video_Room_nativeConnect(
        JNIEnv *env,
        jobject j_instance,
        jobject j_connect_options,
        jlong j_media_factory_handle,
        jlong j_room_observer_handle) {

    RoomObserverContext *room_observer_context =
        reinterpret_cast<RoomObserverContext *>(j_room_observer_handle);

    std::unique_ptr<twilio::video::Room> room;

    // Get connect options
    jclass j_connect_options_class = webrtc_jni::GetObjectClass(env, j_connect_options);
    jmethodID j_createNativeObject_id =
        webrtc_jni::GetMethodID(env, j_connect_options_class,
                                "createNativeObject", "(J)J");
    jlong j_connect_options_handle =
        env->CallLongMethod(j_connect_options, j_createNativeObject_id, j_media_factory_handle);
    ConnectOptionsContext *connect_options_context =
        reinterpret_cast<ConnectOptionsContext *>(j_connect_options_handle);
    room = twilio::video::connect(connect_options_context->connect_options,
                                  room_observer_context->android_room_observer);
    delete connect_options_context;
    RoomContext *room_context = new RoomContext();
    room_context->room = std::move(room);

    return webrtc_jni::jlongFromPointer(room_context);
}


JNIEXPORT jboolean JNICALL Java_com_twilio_video_Room_nativeIsRecording
    (JNIEnv *env, jobject j_instance, jlong j_native_handle) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "%s", func_name.c_str());
    RoomContext *room_context = reinterpret_cast<RoomContext *>(j_native_handle);
    return room_context->room->isRecording();
}

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeDisconnect
    (JNIEnv *env, jobject j_instance, jlong j_native_handle) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "%s", func_name.c_str());
    RoomContext *room_context = reinterpret_cast<RoomContext *>(j_native_handle);
    room_context->room->disconnect();
}

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeGetStats
    (JNIEnv *env, jobject j_instance, jlong j_native_room_context,
     jlong j_native_stats_observer) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "%s", func_name.c_str());
    RoomContext *room_context = reinterpret_cast<RoomContext *>(j_native_room_context);
    StatsObserverContext *stats_observer_context =
        reinterpret_cast<StatsObserverContext *>(j_native_stats_observer);
    room_context->room->getStats(stats_observer_context->stats_observer);
}

twilio::video::NetworkChangeEvent getNetworkChangeEvent(jobject j_network_change_event) {
    JNIEnv *jni = webrtc_jni::AttachCurrentThreadIfNeeded();
    jclass j_network_change_event_class =
        twilio_video_jni::FindClass(jni, "com/twilio/video/Video$NetworkChangeEvent");
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

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeOnNetworkChange(JNIEnv *env,
                                                                        jobject j_instance,
                                                                        jlong j_room_context,
                                                                        jobject j_network_changed_event) {
    RoomContext *room_context = reinterpret_cast<RoomContext *>(j_room_context);
    twilio::video::NetworkChangeEvent networkChangeEvent =
        getNetworkChangeEvent(j_network_changed_event);
    room_context->room->onNetworkChange(networkChangeEvent);
}


JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeRelease
    (JNIEnv *env, jobject j_instance, jlong j_native_handle) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "%s", func_name.c_str());
    RoomContext *room_context = reinterpret_cast<RoomContext *>(j_native_handle);
    if (room_context == nullptr) {
        return;
    }
    delete room_context;
}

JNIEXPORT jlong JNICALL
Java_com_twilio_video_Room_00024InternalRoomListenerHandle_nativeCreate(JNIEnv *env,
                                                                        jobject instance,
                                                                        jobject object) {
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "Create AndroidRoomObserver");
    RoomObserverContext *room_observer_context = new RoomObserverContext();
    room_observer_context->android_room_observer =
        std::make_shared<AndroidRoomObserver>(env, object);
    return webrtc_jni::jlongFromPointer(room_observer_context);
}

JNIEXPORT void JNICALL
Java_com_twilio_video_Room_00024InternalRoomListenerHandle_nativeRelease(JNIEnv *env,
                                                                         jobject instance,
                                                                         jlong nativeHandle) {
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "Free AndroidRoomObserver");
    RoomObserverContext
        *room_observer_context = reinterpret_cast<RoomObserverContext *>(nativeHandle);
    if (room_observer_context != nullptr) {
        room_observer_context->android_room_observer->setObserverDeleted();
        delete room_observer_context;
    }
}

JNIEXPORT jlong JNICALL
Java_com_twilio_video_Room_00024InternalStatsListenerHandle_nativeCreate(JNIEnv *env,
                                                                         jobject instance,
                                                                         jobject object) {
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "Create AndroidStatsObserver");
    StatsObserverContext *stats_observer_context = new StatsObserverContext();
    stats_observer_context->stats_observer =
        std::make_shared<AndroidStatsObserver>(env, object);
    return webrtc_jni::jlongFromPointer(stats_observer_context);
}

JNIEXPORT void JNICALL
Java_com_twilio_video_Room_00024InternalStatsListenerHandle_nativeRelease(JNIEnv *env,
                                                                          jobject instance,
                                                                          jlong nativeHandle) {
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "Free AndroidStatsObserver");
    StatsObserverContext *stats_observer_context =
        reinterpret_cast<StatsObserverContext *>(nativeHandle);
    if (stats_observer_context != nullptr) {
        delete stats_observer_context;
    }
}
}