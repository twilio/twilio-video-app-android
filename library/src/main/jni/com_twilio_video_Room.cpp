#include "com_twilio_video_Room.h"

#include "com_twilio_video_MediaFactory.h"
#include "room_delegate.h"
#include "logging.h"

namespace twilio_video_jni {

JNIEXPORT jlong JNICALL Java_com_twilio_video_Room_nativeConnect(
        JNIEnv *env,
        jobject j_room,
        jobject j_connect_options,
        jobject j_room_listener,
        jobject j_stats_listener,
        jlong j_media_factory_handle) {
    RoomDelegate *room_delegate = new RoomDelegate(env,
                                                  j_connect_options,
                                                  j_media_factory_handle,
                                                  j_room,
                                                  j_room_listener,
                                                  j_stats_listener);
    room_delegate->connect();

    return webrtc_jni::jlongFromPointer(room_delegate);
}


JNIEXPORT jboolean JNICALL Java_com_twilio_video_Room_nativeIsRecording
    (JNIEnv *env, jobject j_instance, jlong j_native_handle) {
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kDebug,
                      "%s", func_name.c_str());
    RoomDelegate *room_delegate = reinterpret_cast<RoomDelegate *>(j_native_handle);
    return room_delegate->isRecording();
}

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeDisconnect
    (JNIEnv *env, jobject j_instance, jlong j_native_handle) {
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kDebug,
                      "%s", func_name.c_str());
    RoomDelegate *room_delegate = reinterpret_cast<RoomDelegate *>(j_native_handle);

    room_delegate->disconnect();
}

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeGetStats
    (JNIEnv *env, jobject j_instance, jlong j_native_room_context,
     jlong j_native_stats_observer) {
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kDebug,
                      "%s", func_name.c_str());
    RoomDelegate *room_delegate = reinterpret_cast<RoomDelegate *>(j_native_room_context);
    room_delegate->getStats();
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
    RoomDelegate *room_delegate = reinterpret_cast<RoomDelegate *>(j_room_context);
    twilio::video::NetworkChangeEvent network_change_event =
        getNetworkChangeEvent(j_network_changed_event);
    room_delegate->onNetworkChange(network_change_event);
}

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeReleaseRoom
        (JNIEnv *env, jobject j_instance, jlong j_native_handle) {
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kDebug,
                      "%s", func_name.c_str());
    RoomDelegate *room_delegate = reinterpret_cast<RoomDelegate *>(j_native_handle);
    room_delegate->release();
}

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeRelease
    (JNIEnv *env, jobject j_instance, jlong j_native_handle) {
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kDebug,
                      "%s", func_name.c_str());
    RoomDelegate *room_delegate = reinterpret_cast<RoomDelegate *>(j_native_handle);
    delete room_delegate;
}

}