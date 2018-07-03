/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "com_twilio_video_Room.h"

#include "com_twilio_video_MediaFactory.h"
#include "room_delegate.h"
#include "logging.h"
#include "jni_utils.h"
#include "webrtc/sdk/android/src/jni/jni_helpers.h"
#include "webrtc/sdk/android/native_api/jni/scoped_java_ref.h"

namespace twilio_video_jni {

JNIEXPORT jlong JNICALL Java_com_twilio_video_Room_nativeConnect(
        JNIEnv *env,
        jobject j_room,
        jobject j_connect_options,
        jobject j_room_listener,
        jobject j_stats_listener,
        jlong j_media_factory_handle,
        jobject j_handler) {
    RoomDelegate *room_delegate = new RoomDelegate(env,
                                                   j_connect_options,
                                                   j_media_factory_handle,
                                                   j_room,
                                                   j_room_listener,
                                                   j_stats_listener,
                                                   j_handler);
    room_delegate->connect();

    return webrtc::NativeToJavaPointer(room_delegate);
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
    JNIEnv *jni = webrtc::jni::AttachCurrentThreadIfNeeded();
    jclass j_network_change_event_class =
            twilio_video_jni::FindClass(jni, "com/twilio/video/Video$NetworkChangeEvent");
    jmethodID name_method_id = webrtc::GetMethodID(jni,
                                                   j_network_change_event_class,
                                                   "name",
                                                   "()Ljava/lang/String;");
    jstring connection_event_name = (jstring) jni->CallObjectMethod(j_network_change_event,
                                                                    name_method_id);
    std::string name = JavaToUTF8StdString(jni, connection_event_name);
    twilio::video::NetworkChangeEvent network_changed_event =
            twilio::video::NetworkChangeEvent::kConnectionChanged;

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
