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

#ifndef VIDEO_ANDROID_INCLUDE_COM_TWILIO_VIDEO_REMOTE_PARTICIPANT_H_
#define VIDEO_ANDROID_INCLUDE_COM_TWILIO_VIDEO_REMOTE_PARTICIPANT_H_

#include <jni.h>
#include <memory>
#include <map>
#include "twilio/video/remote_participant.h"
#include "android_participant_observer.h"

namespace twilio_video_jni {

#ifdef __cplusplus
extern "C" {
#endif

struct RemoteParticipantContext {
    std::shared_ptr<twilio::video::RemoteParticipant> remote_participant;
    std::map<std::shared_ptr<twilio::media::RemoteAudioTrackPublication>, jobject> remote_audio_track_publication_map;
    std::map<std::shared_ptr<twilio::media::RemoteAudioTrack>, jobject> remote_audio_track_map;
    std::map<std::shared_ptr<twilio::media::RemoteVideoTrackPublication>, jobject> remote_video_track_publication_map;
    std::map<std::shared_ptr<twilio::media::RemoteVideoTrack>, jobject> remote_video_track_map;
    std::map<std::shared_ptr<twilio::media::RemoteDataTrackPublication>, jobject> remote_data_track_publication_map;
    std::map<std::shared_ptr<twilio::media::RemoteDataTrack>, jobject> remote_data_track_map;
    std::shared_ptr<AndroidParticipantObserver> android_participant_observer;
};

jobject createJavaRemoteParticipant(JNIEnv *env,
                                    std::shared_ptr<twilio::video::RemoteParticipant> remote_participant,
                                    jclass j_remote_participant_class,
                                    jmethodID j_remote_participant_ctor_id,
                                    jclass j_array_list_class,
                                    jmethodID j_array_list_ctor_id,
                                    jmethodID j_array_list_add,
                                    jclass j_remote_audio_track_class,
                                    jclass j_remote_audio_track_publication_class,
                                    jmethodID j_remote_audio_track_ctor_id,
                                    jmethodID j_remote_audio_track_publication_ctor_id,
                                    jclass j_remote_video_track_class,
                                    jclass j_remote_video_track_publication_class,
                                    jmethodID j_remote_video_track_ctor_id,
                                    jmethodID j_remote_video_track_publication_ctor_id,
                                    jclass j_remote_data_track_class,
                                    jclass j_remote_data_track_publication_class,
                                    jmethodID j_remote_data_track_ctor_id,
                                    jmethodID j_remote_data_track_publication_ctor_id,
                                    jobject j_handler);

jobject createRemoteParticipantAudioTracks(JNIEnv *env,
                                           RemoteParticipantContext *remote_participant_context,
                                           jclass j_array_list_class,
                                           jmethodID j_array_list_ctor_id,
                                           jmethodID j_array_list_add,
                                           jclass j_remote_audio_track_class,
                                           jclass j_remote_audio_track_publication_class,
                                           jmethodID j_remote_audio_track_ctor_id,
                                           jmethodID j_remote_audio_track_publication_ctor_id);

jobject createRemoteParticipantVideoTracks(JNIEnv *env,
                                           RemoteParticipantContext *remote_participant_context,
                                           jclass j_array_list_class,
                                           jmethodID j_array_list_ctor_id,
                                           jmethodID j_array_list_add,
                                           jclass j_remote_video_track_class,
                                           jclass j_remote_video_track_publication_class,
                                           jmethodID j_remote_video_track_ctor_id,
                                           jmethodID j_remote_video_track_publication_ctor_id);

jobject createRemoteParticipantDataTracks(JNIEnv *env,
                                          RemoteParticipantContext *remote_participant_context,
                                          jclass j_array_list_class,
                                          jmethodID j_array_list_ctor_id,
                                          jmethodID j_array_list_add,
                                          jclass j_remote_data_track_class,
                                          jclass j_remote_data_track_publication_class,
                                          jmethodID j_remote_data_track_ctor_id,
                                          jmethodID j_remote_data_track_publication_ctor_id);

jobject createJavaRemoteVideoTrack(JNIEnv *env,
                                   std::shared_ptr<twilio::media::RemoteVideoTrack> remote_video_track,
                                   jobject j_webrtc_video_track,
                                   jclass j_remote_video_track_class,
                                   jmethodID j_remote_video_track_ctor_id);

jobject createJavaRemoteAudioTrackPublication(JNIEnv *env,
                                              std::shared_ptr<twilio::media::RemoteAudioTrackPublication> remote_audio_track_publication,
                                              jclass j_remote_audio_track_publication_class,
                                              jmethodID j_remote_audio_track_publication_ctor_id);

jobject createJavaRemoteVideoTrackPublication(JNIEnv *env,
                                              std::shared_ptr<twilio::media::RemoteVideoTrackPublication> remote_video_track_publication,
                                              jclass j_remote_video_track_publication_class,
                                              jmethodID j_remote_video_track_publication_ctor_id);

jobject createJavaRemoteDataTrackPublication(JNIEnv *env,
                                             std::shared_ptr<twilio::media::RemoteDataTrackPublication> remote_data_track_publication,
                                             jclass j_remote_data_track_publication_class,
                                             jmethodID j_remote_data_track_publication_ctor_id);

JNIEXPORT jboolean JNICALL Java_com_twilio_video_RemoteParticipant_nativeIsConnected
        (JNIEnv *, jobject, jlong);

JNIEXPORT void JNICALL Java_com_twilio_video_RemoteParticipant_nativeRelease
        (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif

}

#endif // VIDEO_ANDROID_INCLUDE_COM_TWILIO_VIDEO_REMOTE_PARTICIPANT_H_
