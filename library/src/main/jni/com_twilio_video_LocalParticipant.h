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

#ifndef VIDEO_ANDROID_COM_TWILIO_VIDEO_LOCALPARTICIPANT_H_
#define VIDEO_ANDROID_COM_TWILIO_VIDEO_LOCALPARTICIPANT_H_

#include <jni.h>
#include "video/local_participant.h"
#include "android_local_participant_observer.h"

#ifdef __cplusplus
extern "C" {
#endif

namespace twilio_video_jni {

static const char *const kLocalParticipantConstructorSignature = "("
        "J"
        "Ljava/lang/String;"
        "Ljava/lang/String;"
        "Ljava/util/List;"
        "Ljava/util/List;"
        "Ljava/util/List;"
        "Landroid/os/Handler;"
        ")V";

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
                                   jobject j_handler);

jobject createJavaLocalAudioTrackPublication(JNIEnv *env,
                                             std::shared_ptr<twilio::media::LocalAudioTrackPublication> local_audio_track_publication,
                                             jobject j_local_audio_track,
                                             jclass j_published_audio_track_class,
                                             jmethodID j_published_audio_track_ctor_id);

jobject createJavaLocalVideoTrackPublication(JNIEnv *env,
                                             std::shared_ptr<twilio::media::LocalVideoTrackPublication> local_video_track_publication,
                                             jobject j_local_video_track,
                                             jclass j_published_video_track_class,
                                             jmethodID j_published_video_track_ctor_id);

jobject createJavaLocalDataTrackPublication(JNIEnv *env,
                                            std::shared_ptr<twilio::media::LocalDataTrackPublication> local_data_track_publication,
                                            jobject j_local_data_track,
                                            jclass j_published_data_track_class,
                                            jmethodID j_published_data_track_ctor_id);

JNIEXPORT bool JNICALL Java_com_twilio_video_LocalParticipant_nativePublishAudioTrack(JNIEnv *,
                                                                                      jobject,
                                                                                      jlong,
                                                                                      jobject,
                                                                                      jlong);

JNIEXPORT bool JNICALL Java_com_twilio_video_LocalParticipant_nativePublishVideoTrack(JNIEnv *,
                                                                                      jobject,
                                                                                      jlong,
                                                                                      jobject,
                                                                                      jlong);

JNIEXPORT bool JNICALL Java_com_twilio_video_LocalParticipant_nativePublishDataTrack(JNIEnv *,
                                                                                     jobject,
                                                                                     jlong,
                                                                                     jobject,
                                                                                     jlong);

JNIEXPORT bool JNICALL Java_com_twilio_video_LocalParticipant_nativeUnpublishAudioTrack(JNIEnv *,
                                                                                        jobject,
                                                                                        jlong,
                                                                                        jlong);

JNIEXPORT bool JNICALL Java_com_twilio_video_LocalParticipant_nativeUnpublishVideoTrack(JNIEnv *,
                                                                                        jobject,
                                                                                        jlong,
                                                                                        jlong);

JNIEXPORT bool JNICALL Java_com_twilio_video_LocalParticipant_nativeUnpublishDataTrack(JNIEnv *,
                                                                                       jobject,
                                                                                       jlong,
                                                                                       jlong);

JNIEXPORT void JNICALL Java_com_twilio_video_LocalParticipant_nativeSetEncodingParameters(JNIEnv *,
                                                                                          jobject,
                                                                                          jlong,
                                                                                          jobject);

JNIEXPORT void JNICALL Java_com_twilio_video_LocalParticipant_nativeRelease(JNIEnv *,
                                                                            jobject,
                                                                            jlong);

}

#ifdef __cplusplus
}
#endif

#endif // VIDEO_ANDROID_COM_TWILIO_VIDEO_LOCALPARTICIPANT_H_
