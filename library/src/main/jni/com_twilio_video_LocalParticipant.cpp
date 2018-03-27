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

#include "com_twilio_video_LocalParticipant.h"
#include "com_twilio_video_LocalAudioTrack.h"
#include "com_twilio_video_LocalVideoTrack.h"
#include "com_twilio_video_LocalDataTrack.h"
#include "com_twilio_video_EncodingParameters.h"

#include "android_local_participant_observer.h"
#include "class_reference_holder.h"
#include "jni_utils.h"

namespace twilio_video_jni {

struct LocalParticipantContext {
    std::shared_ptr<twilio::video::LocalParticipant> local_participant;
    std::shared_ptr<AndroidLocalParticipantObserver> android_local_participant_observer;
    std::map<std::string, jobject> local_audio_tracks_map;
    std::map<std::string, jobject> local_video_tracks_map;
    std::map<std::string, jobject> local_data_tracks_map;
};

jobject createJavaLocalAudioTrackPublication(JNIEnv *env,
                                             std::shared_ptr<twilio::media::LocalAudioTrackPublication> local_audio_track_publication,
                                             jobject j_local_audio_track,
                                             jclass j_published_audio_track_class,
                                             jmethodID j_published_audio_track_ctor_id) {
    jobject j_published_audio_track = env->NewObject(j_published_audio_track_class,
                                                     j_published_audio_track_ctor_id,
                                                     JavaUTF16StringFromStdString(env,
                                                                                         local_audio_track_publication->getTrackSid()),
                                                     j_local_audio_track);
    CHECK_EXCEPTION(env) << "Failed to create LocalAudioTrackPublication";

    return j_published_audio_track;
}

jobject createJavaLocalVideoTrackPublication(JNIEnv *env,
                                             std::shared_ptr<twilio::media::LocalVideoTrackPublication> local_video_track_publication,
                                             jobject j_local_video_track,
                                             jclass j_published_video_track_class,
                                             jmethodID j_published_video_track_ctor_id) {
    jobject j_published_video_track = env->NewObject(j_published_video_track_class,
                                                     j_published_video_track_ctor_id,
                                                     JavaUTF16StringFromStdString(env,
                                                                                         local_video_track_publication->getTrackSid()),
                                                     j_local_video_track);
    CHECK_EXCEPTION(env) << "Failed to create LocalVideoTrackPublication";

    return j_published_video_track;
}

jobject createJavaLocalDataTrackPublication(JNIEnv *env,
                                            std::shared_ptr<twilio::media::LocalDataTrackPublication> local_data_track_publication,
                                            jobject j_local_data_track,
                                            jclass j_published_data_track_class,
                                            jmethodID j_published_data_track_ctor_id) {
    jobject j_published_data_track = env->NewObject(j_published_data_track_class,
                                                    j_published_data_track_ctor_id,
                                                    JavaUTF16StringFromStdString(env,
                                                                                        local_data_track_publication->getTrackSid()),
                                                    j_local_data_track);
    CHECK_EXCEPTION(env) << "Failed to create LocalDataTrackPublication";

    return j_published_data_track;
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
                                                              j_local_participant_listener_proxy,
                                                              local_participant_context->local_audio_tracks_map,
                                                              local_participant_context->local_video_tracks_map,
                                                              local_participant_context->local_data_tracks_map);
    local_participant_context->local_participant->setObserver(
            local_participant_context->android_local_participant_observer);
}

std::map<std::string, jobject>
getLocalAudioTracksMap(JNIEnv *env, jobject j_local_audio_tracks) {
    std::map<std::string, jobject> local_audio_track_map;

    if (!webrtc_jni::IsNull(env, j_local_audio_tracks)) {
        jclass j_array_list_class = webrtc_jni::GetObjectClass(env, j_local_audio_tracks);
        jmethodID j_array_list_size = webrtc_jni::GetMethodID(env, j_array_list_class, "size",
                                                              "()I");
        jmethodID j_array_list_get = webrtc_jni::GetMethodID(env,
                                                             j_array_list_class,
                                                             "get",
                                                             "(I)Ljava/lang/Object;");
        const int size = env->CallIntMethod(j_local_audio_tracks, j_array_list_size);
        CHECK_EXCEPTION(env) << "Failed to call size() on local audio tracks list";

        for (int i = 0; i < size; i++) {
            jobject j_local_audio_track = env->CallObjectMethod(j_local_audio_tracks,
                                                                j_array_list_get,
                                                                i);
            CHECK_EXCEPTION(env) << "Failed to get local audio track from list";
            jclass j_local_audio_track_class = webrtc_jni::GetObjectClass(env, j_local_audio_track);
            jmethodID j_get_track_hash = webrtc_jni::GetMethodID(env,
                                                                 j_local_audio_track_class,
                                                                 "getNativeTrackHash",
                                                                 "()Ljava/lang/String;");
            jstring j_track_hash = (jstring) env->CallObjectMethod(j_local_audio_track,
                                                                 j_get_track_hash);
            std::string track_hash = JavaToUTF8StdString(env, j_track_hash);
            CHECK_EXCEPTION(env) << "Failed to get local audio track hash";

            // Add entry to map
            local_audio_track_map[track_hash] = webrtc_jni::NewGlobalRef(env, j_local_audio_track);
        }
    }

    return local_audio_track_map;
}

std::map<std::string, jobject>
getLocalVideoTracksMap(JNIEnv *env, jobject j_local_video_tracks) {
    std::map<std::string, jobject> local_video_track_map;

    if (!webrtc_jni::IsNull(env, j_local_video_tracks)) {
        jclass j_array_list_class = webrtc_jni::GetObjectClass(env, j_local_video_tracks);
        jmethodID j_array_list_size = webrtc_jni::GetMethodID(env, j_array_list_class, "size",
                                                              "()I");
        jmethodID j_array_list_get = webrtc_jni::GetMethodID(env,
                                                             j_array_list_class,
                                                             "get",
                                                             "(I)Ljava/lang/Object;");
        const int size = env->CallIntMethod(j_local_video_tracks, j_array_list_size);
        CHECK_EXCEPTION(env) << "Failed to call size() on local video tracks list";

        for (int i = 0; i < size; i++) {
            jobject j_local_video_track = env->CallObjectMethod(j_local_video_tracks,
                                                                j_array_list_get,
                                                                i);
            CHECK_EXCEPTION(env) << "Failed to get local video track from list";
            jclass j_local_video_track_class = webrtc_jni::GetObjectClass(env, j_local_video_track);
            jmethodID j_get_track_hash = webrtc_jni::GetMethodID(env,
                                                                 j_local_video_track_class,
                                                                 "getNativeTrackHash",
                                                                 "()Ljava/lang/String;");
            jstring j_track_hash = (jstring) env->CallObjectMethod(j_local_video_track,
                                                                   j_get_track_hash);
            std::string track_hash = JavaToUTF8StdString(env, j_track_hash);
            CHECK_EXCEPTION(env) << "Failed to get local video track hash";

            // Add entry to map
            local_video_track_map[track_hash] = webrtc_jni::NewGlobalRef(env, j_local_video_track);
        }
    }

    return local_video_track_map;
}

std::map<std::string, jobject>
getLocalDataTracksMap(JNIEnv *env, jobject j_local_data_tracks) {
    std::map<std::string, jobject> local_data_track_map;

    if (!webrtc_jni::IsNull(env, j_local_data_tracks)) {
        jclass j_array_list_class = webrtc_jni::GetObjectClass(env, j_local_data_tracks);
        jmethodID j_array_list_size = webrtc_jni::GetMethodID(env, j_array_list_class, "size",
                                                              "()I");
        jmethodID j_array_list_get = webrtc_jni::GetMethodID(env,
                                                             j_array_list_class,
                                                             "get",
                                                             "(I)Ljava/lang/Object;");
        const int size = env->CallIntMethod(j_local_data_tracks, j_array_list_size);
        CHECK_EXCEPTION(env) << "Failed to call size() on local data tracks list";

        for (int i = 0; i < size; i++) {
            jobject j_local_data_track = env->CallObjectMethod(j_local_data_tracks,
                                                                j_array_list_get,
                                                                i);
            CHECK_EXCEPTION(env) << "Failed to get local data track from list";
            jclass j_local_data_track_class = webrtc_jni::GetObjectClass(env, j_local_data_track);
            jmethodID j_get_track_hash = webrtc_jni::GetMethodID(env,
                                                                 j_local_data_track_class,
                                                                 "getNativeTrackHash",
                                                                 "()Ljava/lang/String;");
            jstring j_track_hash = (jstring) env->CallObjectMethod(j_local_data_track,
                                                                   j_get_track_hash);
            std::string track_hash = JavaToUTF8StdString(env, j_track_hash);
            CHECK_EXCEPTION(env) << "Failed to get local data track hash";

            // Add entry to map
            local_data_track_map[track_hash] = webrtc_jni::NewGlobalRef(env, j_local_data_track);
        }
    }

    return local_data_track_map;
}

jobject createLocalAudioTrackPublications(JNIEnv *env,
                                          LocalParticipantContext *local_participant_context,
                                          jclass j_array_list_class,
                                          jmethodID j_array_list_ctor_id,
                                          jmethodID j_array_list_add,
                                          jclass j_published_audio_track_class,
                                          jmethodID j_published_audio_track_ctor_id,
                                          jobject j_local_audio_tracks) {
    jobject j_published_audio_tracks = env->NewObject(j_array_list_class, j_array_list_ctor_id);
    const std::vector<std::shared_ptr<twilio::media::LocalAudioTrackPublication>> local_audio_track_publications =
            local_participant_context->local_participant->getLocalAudioTracks();

    // Map track ids to java LocalAudioTrack
    local_participant_context->local_audio_tracks_map =
            getLocalAudioTracksMap(env, j_local_audio_tracks);

    // Add audio tracks to array list
    for (unsigned int i = 0; i < local_audio_track_publications.size(); i++) {
        std::shared_ptr<twilio::media::LocalAudioTrackPublication> local_audio_track_publication =
                local_audio_track_publications[i];
        std::string local_audio_track_hash =
                getLocalAudioTrackHash(local_audio_track_publication->getLocalTrack());
        jobject j_local_audio_track =
                local_participant_context->local_audio_tracks_map[local_audio_track_hash];

        local_participant_context->local_audio_tracks_map.insert(
                std::make_pair(local_audio_track_hash,
                               webrtc_jni::NewGlobalRef(env, j_local_audio_track)));
        jobject j_local_audio_track_publication =
                createJavaLocalAudioTrackPublication(env,
                                                     local_audio_track_publication,
                                                     j_local_audio_track,
                                                     j_published_audio_track_class,
                                                     j_published_audio_track_ctor_id);
        env->CallBooleanMethod(j_published_audio_tracks, j_array_list_add, j_local_audio_track_publication);
    }

    return j_published_audio_tracks;
}

jobject createLocalVideoTrackPublications(JNIEnv *env,
                                          LocalParticipantContext *local_participant_context,
                                          jclass j_array_list_class,
                                          jmethodID j_array_list_ctor_id,
                                          jmethodID j_array_list_add,
                                          jclass j_published_video_track_class,
                                          jmethodID j_published_video_track_ctor_id,
                                          jobject j_local_video_tracks) {
    jobject j_published_video_tracks = env->NewObject(j_array_list_class, j_array_list_ctor_id);
    const std::vector<std::shared_ptr<twilio::media::LocalVideoTrackPublication>> local_video_track_publications =
            local_participant_context->local_participant->getLocalVideoTracks();

    // Map track ids to java LocalVideoTrack
    local_participant_context->local_video_tracks_map =
            getLocalVideoTracksMap(env, j_local_video_tracks);

    // Add video tracks to array list
    for (unsigned int i = 0; i < local_video_track_publications.size(); i++) {
        std::shared_ptr<twilio::media::LocalVideoTrackPublication> local_video_track_publication =
                local_video_track_publications[i];
        std::string local_video_track_hash =
                getLocalVideoTrackHash(local_video_track_publication->getLocalTrack());
        jobject j_local_video_track =
                local_participant_context->local_video_tracks_map[local_video_track_hash];

        local_participant_context->local_video_tracks_map.insert(
                std::make_pair(local_video_track_hash,
                               webrtc_jni::NewGlobalRef(env, j_local_video_track)));
        jobject j_local_video_track_publication =
                createJavaLocalVideoTrackPublication(env,
                                                     local_video_track_publication,
                                                     j_local_video_track,
                                                     j_published_video_track_class,
                                                     j_published_video_track_ctor_id);
        env->CallBooleanMethod(j_published_video_tracks, j_array_list_add, j_local_video_track_publication);
    }

    return j_published_video_tracks;
}

jobject createLocalDataTrackPublications(JNIEnv *env,
                                          LocalParticipantContext *local_participant_context,
                                          jclass j_array_list_class,
                                          jmethodID j_array_list_ctor_id,
                                          jmethodID j_array_list_add,
                                          jclass j_published_data_track_class,
                                          jmethodID j_published_data_track_ctor_id,
                                          jobject j_local_data_tracks) {
    jobject j_published_data_tracks = env->NewObject(j_array_list_class, j_array_list_ctor_id);
    const std::vector<std::shared_ptr<twilio::media::LocalDataTrackPublication>> local_data_track_publications =
            local_participant_context->local_participant->getLocalDataTracks();

    // Map track ids to java LocalDataTrack
    local_participant_context->local_data_tracks_map =
            getLocalDataTracksMap(env, j_local_data_tracks);

    // Add data tracks to array list
    for (unsigned int i = 0; i < local_data_track_publications.size(); i++) {
        std::shared_ptr<twilio::media::LocalDataTrackPublication> local_data_track_publication =
                local_data_track_publications[i];
        std::string local_data_track_hash =
                getLocalDataTrackHash(local_data_track_publication->getLocalTrack());
        jobject j_local_data_track =
                local_participant_context->local_data_tracks_map[local_data_track_hash];

        local_participant_context->local_data_tracks_map.insert(
                std::make_pair(local_data_track_hash,
                               webrtc_jni::NewGlobalRef(env, j_local_data_track)));
        jobject j_local_data_track_publication =
                createJavaLocalDataTrackPublication(env,
                                                     local_data_track_publication,
                                                     j_local_data_track,
                                                     j_published_data_track_class,
                                                     j_published_data_track_ctor_id);
        env->CallBooleanMethod(j_published_data_tracks, j_array_list_add, j_local_data_track_publication);
    }

    return j_published_data_tracks;
}

jobject createJavaLocalParticipant(JNIEnv *env,
                                   std::shared_ptr<twilio::video::LocalParticipant> local_participant,
                                   jclass j_local_participant_class,
                                   jmethodID j_local_participant_ctor_id,
                                   jobject j_local_audio_tracks,
                                   jobject j_local_video_tracks,
                                   jobject j_local_data_tracks,
                                   jclass j_array_list_class,
                                   jmethodID j_array_list_ctor_id,
                                   jmethodID j_array_list_add,
                                   jclass j_published_audio_track_class,
                                   jmethodID j_published_audio_track_ctor_id,
                                   jclass j_published_video_track_class,
                                   jmethodID j_published_video_track_ctor_id,
                                   jclass j_published_data_track_class,
                                   jmethodID j_published_data_track_ctor_id,
                                   jobject j_handler) {
    LocalParticipantContext* local_participant_context = new LocalParticipantContext();
    local_participant_context->local_participant = local_participant;
    jstring j_sid = JavaUTF16StringFromStdString(env, local_participant->getSid());
    jstring j_identity = JavaUTF16StringFromStdString(env, local_participant->getIdentity());
    jobject j_published_audio_tracks = createLocalAudioTrackPublications(env,
                                                                         local_participant_context,
                                                                         j_array_list_class,
                                                                         j_array_list_ctor_id,
                                                                         j_array_list_add,
                                                                         j_published_audio_track_class,
                                                                         j_published_audio_track_ctor_id,
                                                                         j_local_audio_tracks);
    jobject j_published_video_tracks = createLocalVideoTrackPublications(env,
                                                                         local_participant_context,
                                                                         j_array_list_class,
                                                                         j_array_list_ctor_id,
                                                                         j_array_list_add,
                                                                         j_published_video_track_class,
                                                                         j_published_video_track_ctor_id,
                                                                         j_local_video_tracks);
    jobject j_published_data_tracks = createLocalDataTrackPublications(env,
                                                                       local_participant_context,
                                                                       j_array_list_class,
                                                                       j_array_list_ctor_id,
                                                                       j_array_list_add,
                                                                       j_published_data_track_class,
                                                                       j_published_data_track_ctor_id,
                                                                       j_local_data_tracks);

    // Create local participant
    jlong j_local_participant_context = webrtc_jni::jlongFromPointer(local_participant_context);
    jobject j_local_participant = env->NewObject(j_local_participant_class,
                                                 j_local_participant_ctor_id,
                                                 j_local_participant_context,
                                                 j_sid,
                                                 j_identity,
                                                 j_published_audio_tracks,
                                                 j_published_video_tracks,
                                                 j_published_data_tracks,
                                                 j_handler);

    CHECK_EXCEPTION(env) << "Failed to create LocalParticipant";

    // Bind the local participant listener proxy with the native participant observer
    bindLocalParticipantListenerProxy(env,
                                      j_local_participant,
                                      j_local_participant_class,
                                      local_participant_context);

    return j_local_participant;
}

JNIEXPORT bool JNICALL Java_com_twilio_video_LocalParticipant_nativePublishAudioTrack(JNIEnv *jni,
                                                                                      jobject j_local_participant,
                                                                                      jlong j_local_participant_handle,
                                                                                      jobject j_local_audio_track,
                                                                                      jlong j_audio_track_handle) {
    LocalParticipantContext* local_participant_context =
            reinterpret_cast<LocalParticipantContext *>(j_local_participant_handle);
    std::shared_ptr<twilio::media::LocalAudioTrack> audio_track =
            getLocalAudioTrack(j_audio_track_handle);
    local_participant_context->local_audio_tracks_map.insert(
            std::make_pair(getLocalAudioTrackHash(audio_track),
                           webrtc_jni::NewGlobalRef(jni, j_local_audio_track)));
    return local_participant_context->local_participant->publishTrack(audio_track);
}

JNIEXPORT bool JNICALL Java_com_twilio_video_LocalParticipant_nativePublishVideoTrack(JNIEnv *jni,
                                                                                      jobject j_local_participant,
                                                                                      jlong j_local_participant_handle,
                                                                                      jobject j_local_video_track,
                                                                                      jlong j_video_track_handle) {
    LocalParticipantContext* local_participant_context =
            reinterpret_cast<LocalParticipantContext *>(j_local_participant_handle);
    std::shared_ptr<twilio::media::LocalVideoTrack> video_track =
            getLocalVideoTrack(j_video_track_handle);
    local_participant_context->local_video_tracks_map.insert(
            std::make_pair(getLocalVideoTrackHash(video_track),
                           webrtc_jni::NewGlobalRef(jni, j_local_video_track)));
    return local_participant_context->local_participant->publishTrack(video_track);
}

JNIEXPORT bool JNICALL Java_com_twilio_video_LocalParticipant_nativePublishDataTrack(JNIEnv *jni,
                                                                                     jobject j_local_participant,
                                                                                     jlong j_local_participant_handle,
                                                                                     jobject j_local_data_track,
                                                                                     jlong j_data_track_handle) {
    LocalParticipantContext* local_participant_context =
            reinterpret_cast<LocalParticipantContext *>(j_local_participant_handle);
    std::shared_ptr<twilio::media::LocalDataTrack> data_track =
            getLocalDataTrack(j_data_track_handle);
    local_participant_context->local_data_tracks_map.insert(
            std::make_pair(getLocalDataTrackHash(data_track),
                           webrtc_jni::NewGlobalRef(jni, j_local_data_track)));
    return local_participant_context->local_participant->publishTrack(data_track);
}

JNIEXPORT bool JNICALL Java_com_twilio_video_LocalParticipant_nativeUnpublishAudioTrack(JNIEnv *jni,
                                                                                        jobject j_local_participant,
                                                                                        jlong j_local_participant_handle,
                                                                                        jlong j_audio_track_handle) {
    LocalParticipantContext* local_participant_context =
            reinterpret_cast<LocalParticipantContext *>(j_local_participant_handle);
    std::shared_ptr<twilio::media::LocalAudioTrack> audio_track =
            getLocalAudioTrack(j_audio_track_handle);

    // Unpublish audio track
    bool result = local_participant_context->local_participant->unpublishTrack(audio_track);

    // Remove the local audio track and delete global ref
    auto it = local_participant_context->local_audio_tracks_map.find(
            getLocalAudioTrackHash(audio_track));
    jobject j_local_audio_track = it->second;
    local_participant_context->local_audio_tracks_map.erase(it);
    webrtc_jni::DeleteGlobalRef(jni, j_local_audio_track);

    return result;
}

JNIEXPORT bool JNICALL Java_com_twilio_video_LocalParticipant_nativeUnpublishVideoTrack(JNIEnv *jni,
                                                                                        jobject j_local_participant,
                                                                                        jlong j_local_participant_handle,
                                                                                        jlong j_video_track_handle) {
    LocalParticipantContext* local_participant_context =
            reinterpret_cast<LocalParticipantContext *>(j_local_participant_handle);
    std::shared_ptr<twilio::media::LocalVideoTrack> video_track =
            getLocalVideoTrack(j_video_track_handle);
    // Unpublish video track
    bool result = local_participant_context->local_participant->unpublishTrack(video_track);

    // Remove the local video track and delete global ref
    auto it = local_participant_context->local_video_tracks_map.find(
            getLocalVideoTrackHash(video_track));
    jobject j_local_video_track = it->second;
    local_participant_context->local_video_tracks_map.erase(it);
    webrtc_jni::DeleteGlobalRef(jni, j_local_video_track);

    return result;
}

JNIEXPORT bool JNICALL Java_com_twilio_video_LocalParticipant_nativeUnpublishDataTrack(JNIEnv *jni,
                                                                                       jobject j_local_participant,
                                                                                       jlong j_local_participant_handle,
                                                                                       jlong j_data_track_handle) {
    LocalParticipantContext* local_participant_context =
            reinterpret_cast<LocalParticipantContext *>(j_local_participant_handle);
    std::shared_ptr<twilio::media::LocalDataTrack> data_track =
            getLocalDataTrack(j_data_track_handle);
    // Unpublish data track
    bool result = local_participant_context->local_participant->unpublishTrack(data_track);

    // Remove the local data track and delete global ref
    auto it = local_participant_context->local_data_tracks_map.find(
            getLocalDataTrackHash(data_track));
    jobject j_local_video_track = it->second;
    local_participant_context->local_data_tracks_map.erase(it);
    webrtc_jni::DeleteGlobalRef(jni, j_local_video_track);

    return result;
}

JNIEXPORT void JNICALL Java_com_twilio_video_LocalParticipant_nativeSetEncodingParameters(JNIEnv *jni,
                                                                                          jobject j_local_participant,
                                                                                          jlong j_local_participant_handle,
                                                                                          jobject j_encoding_parameters) {
    LocalParticipantContext* local_participant_context =
            reinterpret_cast<LocalParticipantContext *>(j_local_participant_handle);
    twilio::media::EncodingParameters encoding_parameters = webrtc_jni::IsNull(jni, j_encoding_parameters) ?
            twilio::media::EncodingParameters() :
            getEncodingParameters(jni, j_encoding_parameters);

    // Set encoding parameters on native local participant
    local_participant_context->local_participant->setEncodingParameters(encoding_parameters);
}

JNIEXPORT void JNICALL Java_com_twilio_video_LocalParticipant_nativeRelease(JNIEnv *jni,
                                                                            jobject j_local_participant,
                                                                            jlong j_local_participant_handle) {
    LocalParticipantContext* local_participant_context =
        reinterpret_cast<LocalParticipantContext *>(j_local_participant_handle);

    // Delete the local participant observer
    local_participant_context->android_local_participant_observer->setObserverDeleted();
    local_participant_context->android_local_participant_observer = nullptr;

    // Delete all remaining global references to LocalAudioTracks
    for (auto it = local_participant_context->local_audio_tracks_map.begin() ;
         it != local_participant_context->local_audio_tracks_map.end() ; it++) {
        webrtc_jni::DeleteGlobalRef(jni, it->second);
    }
    local_participant_context->local_audio_tracks_map.clear();

    // Delete all remaining global references to LocalVideoTracks
    for (auto it = local_participant_context->local_video_tracks_map.begin() ;
         it != local_participant_context->local_video_tracks_map.end() ; it++) {
        webrtc_jni::DeleteGlobalRef(jni, it->second);
    }
    local_participant_context->local_video_tracks_map.clear();

    // Delete all remaining global references to LocalDataTracks
    for (auto it = local_participant_context->local_data_tracks_map.begin() ;
         it != local_participant_context->local_data_tracks_map.end() ; it++) {
        webrtc_jni::DeleteGlobalRef(jni, it->second);
    }
    local_participant_context->local_data_tracks_map.clear();

    // Delete the local participant context
    delete local_participant_context;
}

}
