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

#include <map>

#include "com_twilio_video_LocalParticipant.h"
#include "com_twilio_video_LocalAudioTrack.h"
#include "com_twilio_video_LocalVideoTrack.h"

#include "android_local_participant_observer.h"
#include "class_reference_holder.h"

namespace twilio_video_jni {

struct LocalParticipantContext {
    std::shared_ptr<twilio::video::LocalParticipant> local_participant;
    std::shared_ptr<AndroidLocalParticipantObserver> android_local_participant_observer;
};

jobject createJavaPublishedAudioTrack(JNIEnv *env,
                                      std::shared_ptr<twilio::media::PublishedAudioTrack> published_audio_track,
                                      jclass j_published_audio_track_class,
                                      jmethodID j_published_audio_track_ctor_id) {
    jobject j_published_audio_track = env->NewObject(j_published_audio_track_class,
                                                     j_published_audio_track_ctor_id,
                                                     webrtc_jni::JavaStringFromStdString(env, published_audio_track->getSid()),
                                                     webrtc_jni::JavaStringFromStdString(env, published_audio_track->getTrackId()));
    CHECK_EXCEPTION(env) << "Failed to create PublishedAudioTrack";

    return j_published_audio_track;
}

jobject createJavaPublishedVideoTrack(JNIEnv *env,
                                      std::shared_ptr<twilio::media::PublishedVideoTrack> published_video_track,
                                      jclass j_published_video_track_class,
                                      jmethodID j_published_video_track_ctor_id) {
    jobject j_published_video_track = env->NewObject(j_published_video_track_class,
                                                     j_published_video_track_ctor_id,
                                                     webrtc_jni::JavaStringFromStdString(env, published_video_track->getSid()),
                                                     webrtc_jni::JavaStringFromStdString(env, published_video_track->getTrackId()));
    CHECK_EXCEPTION(env) << "Failed to create PublishedVideoTrack";

    return j_published_video_track;
}

void bindLocalParticipantListenerProxy(JNIEnv *env,
                                       jobject j_local_participant,
                                       jclass j_local_participant_class,
                                       LocalParticipantContext *local_participant_context) {
    jfieldID j_local_participant_listener_proxy_field = webrtc_jni::GetFieldID(env,
                                                                               j_local_participant_class,
                                                                               "localParticipantListenerProxy",
                                                                               "Lcom/twilio/video/LocalParticipant$Listener;");
    jobject j_local_participant_listener_proxy = webrtc_jni::GetObjectField(env,
                                                                            j_local_participant,
                                                                            j_local_participant_listener_proxy_field);

    local_participant_context->android_local_participant_observer =
            std::make_shared<AndroidLocalParticipantObserver>(env,
                                                              j_local_participant,
                                                              j_local_participant_listener_proxy);
    local_participant_context->local_participant->setObserver(
            local_participant_context->android_local_participant_observer);
}

jobject createPublishedAudioTracks(JNIEnv *env,
                                   LocalParticipantContext *local_participant_context,
                                   jclass j_array_list_class,
                                   jmethodID j_array_list_ctor_id,
                                   jmethodID j_array_list_add,
                                   jclass j_published_audio_track_class,
                                   jmethodID j_published_audio_track_ctor_id) {
    jobject j_published_audio_tracks = env->NewObject(j_array_list_class, j_array_list_ctor_id);
    const std::vector<std::shared_ptr<twilio::media::PublishedAudioTrack>> published_audio_tracks =
            local_participant_context->local_participant->getPublishedAudioTracks();

    // Add audio tracks to array list
    for (unsigned int i = 0; i < published_audio_tracks.size(); i++) {
        std::shared_ptr<twilio::media::PublishedAudioTrack> remote_audio_track = published_audio_tracks[i];
        jobject j_remote_audio_track =
                createJavaPublishedAudioTrack(env,
                                              remote_audio_track,
                                              j_published_audio_track_class,
                                              j_published_audio_track_ctor_id);
        env->CallBooleanMethod(j_published_audio_tracks, j_array_list_add, j_remote_audio_track);
    }

    return j_published_audio_tracks;
}

jobject createPublishedVideoTracks(JNIEnv *env,
                                   LocalParticipantContext *local_participant_context,
                                   jclass j_array_list_class,
                                   jmethodID j_array_list_ctor_id,
                                   jmethodID j_array_list_add,
                                   jclass j_published_video_track_class,
                                   jmethodID j_published_video_track_ctor_id) {
    jobject j_published_video_tracks = env->NewObject(j_array_list_class, j_array_list_ctor_id);
    const std::vector<std::shared_ptr<twilio::media::PublishedVideoTrack>> published_video_tracks =
            local_participant_context->local_participant->getPublishedVideoTracks();

    // Add video tracks to array list
    for (unsigned int i = 0; i < published_video_tracks.size(); i++) {
        std::shared_ptr<twilio::media::PublishedVideoTrack> remote_video_track = published_video_tracks[i];
        jobject j_remote_video_track =
                createJavaPublishedVideoTrack(env,
                                              remote_video_track,
                                              j_published_video_track_class,
                                              j_published_video_track_ctor_id);
        env->CallBooleanMethod(j_published_video_tracks, j_array_list_add, j_remote_video_track);
    }

    return j_published_video_tracks;
}

jobject createJavaLocalParticipant(JNIEnv *env,
                                   std::shared_ptr<twilio::video::LocalParticipant> local_participant,
                                   jclass j_local_participant_class,
                                   jmethodID j_local_participant_ctor_id,
                                   jobject j_local_audio_tracks,
                                   jobject j_local_video_tracks,
                                   jclass j_array_list_class,
                                   jmethodID j_array_list_ctor_id,
                                   jmethodID j_array_list_add,
                                   jclass j_published_audio_track_class,
                                   jmethodID j_published_audio_track_ctor_id,
                                   jclass j_published_video_track_class,
                                   jmethodID j_published_video_track_ctor_id,
                                   jobject j_handler) {
    LocalParticipantContext* local_participant_context = new LocalParticipantContext();
    local_participant_context->local_participant = local_participant;
    jstring j_sid = webrtc_jni::JavaStringFromStdString(env, local_participant->getSid());
    jstring j_identity = webrtc_jni::JavaStringFromStdString(env, local_participant->getIdentity());
    jobject j_published_audio_tracks = createPublishedAudioTracks(env,
                                                                  local_participant_context,
                                                                  j_array_list_class,
                                                                  j_array_list_ctor_id,
                                                                  j_array_list_add,
                                                                  j_published_audio_track_class,
                                                                  j_published_audio_track_ctor_id);
    jobject j_published_video_tracks = createPublishedVideoTracks(env,
                                                                  local_participant_context,
                                                                  j_array_list_class,
                                                                  j_array_list_ctor_id,
                                                                  j_array_list_add,
                                                                  j_published_video_track_class,
                                                                  j_published_video_track_ctor_id);

    // Create local participant
    jlong j_local_participant_context = webrtc_jni::jlongFromPointer(local_participant_context);
    jobject j_local_participant = env->NewObject(j_local_participant_class,
                                                 j_local_participant_ctor_id,
                                                 j_local_participant_context,
                                                 j_sid,
                                                 j_identity,
                                                 j_local_audio_tracks,
                                                 j_local_video_tracks,
                                                 j_published_audio_tracks,
                                                 j_published_video_tracks,
                                                 j_handler);

    CHECK_EXCEPTION(env) << "Failed to create LocalParticipant";

    // Bind the local participant listener proxy with the natie participant observer
    bindLocalParticipantListenerProxy(env,
                                      j_local_participant,
                                      j_local_participant_class,
                                      local_participant_context);

    return j_local_participant;
}

std::shared_ptr<twilio::video::LocalParticipant> getLocalParticipant(jlong local_participant_handle) {
    LocalParticipantContext* local_participant_context =
        reinterpret_cast<LocalParticipantContext *>(local_participant_handle);
    return local_participant_context->local_participant;
}

JNIEXPORT bool JNICALL Java_com_twilio_video_LocalParticipant_nativePublishAudioTrack(JNIEnv *jni,
                                                                                      jobject j_local_participant,
                                                                                      jlong j_local_participant_handle,
                                                                                      jlong j_audio_track_handle) {
    auto local_participant = getLocalParticipant(j_local_participant_handle);
    auto audio_track = getLocalAudioTrack(j_audio_track_handle);
    return local_participant->publishTrack(audio_track);
}

JNIEXPORT bool JNICALL Java_com_twilio_video_LocalParticipant_nativePublishVideoTrack(JNIEnv *jni,
                                                                                      jobject j_local_participant,
                                                                                      jlong j_local_participant_handle,
                                                                                      jlong j_video_track_handle) {
    auto local_participant = getLocalParticipant(j_local_participant_handle);
    auto video_track = getLocalVideoTrack(j_video_track_handle);
    return local_participant->publishTrack(video_track);
}

JNIEXPORT bool JNICALL Java_com_twilio_video_LocalParticipant_nativeUnpublishAudioTrack(JNIEnv *jni,
                                                                                        jobject j_local_participant,
                                                                                        jlong j_local_participant_handle,
                                                                                        jlong j_audio_track_handle) {
    auto local_participant = getLocalParticipant(j_local_participant_handle);
    auto audio_track = getLocalAudioTrack(j_audio_track_handle);
    return local_participant->unpublishTrack(audio_track);
}

JNIEXPORT bool JNICALL Java_com_twilio_video_LocalParticipant_nativeUnpublishVideoTrack(JNIEnv *jni,
                                                                                        jobject j_local_participant,
                                                                                        jlong j_local_participant_handle,
                                                                                        jlong j_video_track_handle) {
    auto local_participant = getLocalParticipant(j_local_participant_handle);
    auto video_track = getLocalVideoTrack(j_video_track_handle);
    return local_participant->unpublishTrack(video_track);
}

JNIEXPORT void JNICALL Java_com_twilio_video_LocalParticipant_nativeRelease(JNIEnv *jni,
                                                                            jobject j_local_participant,
                                                                            jlong j_local_participant_handle) {
    LocalParticipantContext* local_participant_context =
        reinterpret_cast<LocalParticipantContext *>(j_local_participant_handle);

    // Delete the local participant observer
    local_participant_context->android_local_participant_observer->setObserverDeleted();
    local_participant_context->android_local_participant_observer = nullptr;

    // Delete the local participant context
    delete local_participant_context;
}

}
