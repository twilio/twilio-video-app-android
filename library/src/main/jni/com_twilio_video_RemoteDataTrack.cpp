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

#include "com_twilio_video_RemoteDataTrack.h"
#include "android_remote_data_track_observer.h"
#include "webrtc/sdk/android/src/jni/jni_helpers.h"
#include "jni_utils.h"

namespace twilio_video_jni {

struct RemoteDataTrackContext {
    std::shared_ptr<twilio::media::RemoteDataTrack> remote_data_track;
    std::shared_ptr<AndroidRemoteDataTrackObserver> android_remote_data_track_observer;
};

void bindRemoteDataTrackListenerProxy(JNIEnv *env,
                                      jobject j_remote_data_track,
                                      jclass j_remote_data_track_class,
                                      RemoteDataTrackContext *remote_data_track_context) {
    jfieldID j_remote_data_track_listener_proxy_field = webrtc_jni::GetFieldID(env,
                                                                               j_remote_data_track_class,
                                                                               "dataTrackListenerProxy",
                                                                               "Lcom/twilio/video/RemoteDataTrack$Listener;");
    jobject j_remote_data_track_listener_proxy = webrtc_jni::GetObjectField(env,
                                                                            j_remote_data_track,
                                                                            j_remote_data_track_listener_proxy_field);

    remote_data_track_context->android_remote_data_track_observer =
            std::make_shared<AndroidRemoteDataTrackObserver>(env,
                                                             j_remote_data_track,
                                                             j_remote_data_track_listener_proxy);
    remote_data_track_context->remote_data_track->setObserver(
            remote_data_track_context->android_remote_data_track_observer);
}

jobject createJavaRemoteDataTrack(JNIEnv *env,
                                  std::shared_ptr<twilio::media::RemoteDataTrack> remote_data_track,
                                  jclass j_remote_data_track_class,
                                  jmethodID j_remote_data_track_ctor_id) {
    RemoteDataTrackContext* remote_data_track_context = new RemoteDataTrackContext();
    remote_data_track_context->remote_data_track = remote_data_track;
    jboolean j_enabled = (jboolean) remote_data_track->isEnabled();
    jboolean j_ordered = (jboolean) remote_data_track->isOrdered();
    jboolean j_reliable = (jboolean) remote_data_track->isReliable();
    jint j_max_packet_life_time = (jint) remote_data_track->getMaxPacketLifeTime();
    jint j_max_retransmits = (jint) remote_data_track->getMaxRetransmits();
    jstring j_sid = JavaUTF16StringFromStdString(env, remote_data_track->getSid());
    jstring j_name = JavaUTF16StringFromStdString(env, remote_data_track->getName());
    jlong j_remote_data_track_context = webrtc_jni::jlongFromPointer(remote_data_track_context);
    jobject j_remote_data_track = env->NewObject(j_remote_data_track_class,
                                                 j_remote_data_track_ctor_id,
                                                 j_enabled,
                                                 j_ordered,
                                                 j_reliable,
                                                 j_max_packet_life_time,
                                                 j_max_retransmits,
                                                 j_sid,
                                                 j_name,
                                                 j_remote_data_track_context);
    CHECK_EXCEPTION(env) << "Failed to create RemoteDataTrack";

    // Bind the data track listener proxy with the native data track observer
    bindRemoteDataTrackListenerProxy(env,
                                     j_remote_data_track,
                                     j_remote_data_track_class,
                                     remote_data_track_context);

    return j_remote_data_track;
}


JNIEXPORT void JNICALL Java_com_twilio_video_RemoteDataTrack_nativeRelease(JNIEnv *env,
                                                                           jobject j_remote_data_track,
                                                                           jlong j_remote_data_track_context) {
    RemoteDataTrackContext *remote_data_track_context =
            reinterpret_cast<RemoteDataTrackContext *>(j_remote_data_track_context);

    // Delete the remote data track observer
    remote_data_track_context->android_remote_data_track_observer->setObserverDeleted();
    remote_data_track_context->android_remote_data_track_observer = nullptr;

    // Delete remote data track context
    delete remote_data_track_context;
}

}
