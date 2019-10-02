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

#include "com_twilio_video_ConnectOptions.h"
#include "com_twilio_video_IceOptions.h"
#include "com_twilio_video_PlatformInfo.h"
#include "com_twilio_video_LocalAudioTrack.h"
#include "com_twilio_video_LocalVideoTrack.h"
#include "com_twilio_video_EncodingParameters.h"

#include "webrtc/sdk/android/src/jni/jni_helpers.h"
#include "class_reference_holder.h"
#include "com_twilio_video_LocalDataTrack.h"
#include "jni_utils.h"
#include "webrtc/modules/utility/include/helpers_android.h"

namespace twilio_video_jni {

std::shared_ptr<twilio::media::AudioCodec> getAudioCodec(JNIEnv *env, jobject j_audio_codec) {
    jclass j_audio_codec_class = GetObjectClass(env, j_audio_codec);
    jmethodID j_name_method_id = webrtc::GetMethodID(env,
                                                     j_audio_codec_class,
                                                     "getName",
                                                     "()Ljava/lang/String;");
    jstring j_audio_codec_name = (jstring) env->CallObjectMethod(j_audio_codec, j_name_method_id);
    CHECK_EXCEPTION(env) << "Failed to get name of audio codec";
    std::string audio_codec_name = JavaToUTF8StdString(env, j_audio_codec_name);
    std::shared_ptr<twilio::media::AudioCodec> audio_codec;

    if (audio_codec_name == "isac") {
        audio_codec.reset(new twilio::media::IsacCodec());
    } else if (audio_codec_name == "opus") {
        audio_codec.reset(new twilio::media::OpusCodec());
    } else if (audio_codec_name == "PCMA") {
        audio_codec.reset(new twilio::media::PcmaCodec());
    } else if (audio_codec_name == "PCMU") {
        audio_codec.reset(new twilio::media::PcmuCodec());
    } else if (audio_codec_name == "G722") {
        audio_codec.reset(new twilio::media::G722Codec());
    } else {
        FATAL() << "Failed to get native audio codec for " << audio_codec_name;
    }

    return audio_codec;
}

std::shared_ptr<twilio::media::VideoCodec> getVideoCodec(JNIEnv *env, jobject j_video_codec) {
    jclass j_video_codec_class = GetObjectClass(env, j_video_codec);
    jmethodID j_name_method_id = webrtc::GetMethodID(env,
                                                     j_video_codec_class,
                                                     "getName",
                                                     "()Ljava/lang/String;");
    jstring j_video_codec_name = (jstring) env->CallObjectMethod(j_video_codec, j_name_method_id);
    CHECK_EXCEPTION(env) << "Failed to get name of video codec";
    std::string video_codec_name = JavaToUTF8StdString(env, j_video_codec_name);
    std::shared_ptr<twilio::media::VideoCodec> video_codec;

    if (video_codec_name == "H264") {
        video_codec.reset(new twilio::media::H264Codec());
    } else if (video_codec_name == "VP8") {
        jfieldID j_simulcast_field_id = GetFieldID(env,
                                                   j_video_codec_class,
                                                   "simulcast",
                                                   "Z");
        jboolean j_simulcast = env->GetBooleanField(j_video_codec, j_simulcast_field_id);
        CHECK_EXCEPTION(env) << "Failed to get simulcast field from Vp8Codec instance";
        video_codec.reset(new twilio::media::Vp8Codec(j_simulcast));
    } else if (video_codec_name == "VP9") {
        video_codec.reset(new twilio::media::Vp9Codec());
    } else {
        FATAL() << "Failed to get native video codec for " << video_codec_name;
    }

    return video_codec;
}

JNIEXPORT jlong JNICALL
Java_com_twilio_video_ConnectOptions_nativeCreate(JNIEnv *env,
                                                  jobject j_instance,
                                                  jstring j_access_token,
                                                  jstring j_room_name,
                                                  jobjectArray j_audio_tracks,
                                                  jobjectArray j_video_tracks,
                                                  jobjectArray j_data_tracks,
                                                  jobject j_ice_options,
                                                  jboolean j_enable_insights,
                                                  jboolean j_enable_automatic_subscription,
                                                  jboolean j_enable_network_quality,
                                                  jboolean j_enable_dominant_speaker,
                                                  jlong j_platform_info_handle,
                                                  jobjectArray j_preferred_audio_codecs,
                                                  jobjectArray j_preferred_video_codecs,
                                                  jstring j_region,
                                                  jobject j_encoding_parameters) {

    std::string access_token = JavaToUTF8StdString(env, j_access_token);
    twilio::video::ConnectOptions::Builder* builder =
            new twilio::video::ConnectOptions::Builder(access_token);

    if (!IsNull(env, j_room_name)) {
        std::string name = JavaToUTF8StdString(env, j_room_name);
        builder->setRoomName(name);
    }

    if (!IsNull(env, j_audio_tracks)) {
        jclass j_local_audio_track_class =
                twilio_video_jni::FindClass(env, "com/twilio/video/LocalAudioTrack");
        jmethodID j_local_audio_track_get_native_handle =
                webrtc::GetMethodID(env, j_local_audio_track_class, "getNativeHandle", "()J");

        std::vector<std::shared_ptr<twilio::media::LocalAudioTrack>> audio_tracks;
        int size = env->GetArrayLength(j_audio_tracks);
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                // Get local audio track handle
                jobject j_audio_track = (jobject) env->GetObjectArrayElement(j_audio_tracks, i);
                jlong j_audio_track_handle =
                        (jlong) env->CallLongMethod(j_audio_track,
                                                    j_local_audio_track_get_native_handle);
                // Get local audio track
                auto audio_track = getLocalAudioTrack(j_audio_track_handle);
                audio_tracks.push_back(audio_track);
            }
            builder->setAudioTracks(audio_tracks);
        }
    }

    if(!IsNull(env, j_video_tracks)) {
        jclass j_local_video_track_class =
                twilio_video_jni::FindClass(env, "com/twilio/video/LocalVideoTrack");
        jmethodID j_local_video_track_get_native_handle =
                webrtc::GetMethodID(env, j_local_video_track_class, "getNativeHandle", "()J");

        std::vector<std::shared_ptr<twilio::media::LocalVideoTrack>> video_tracks;
        int size = env->GetArrayLength(j_video_tracks);
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                // Get local video track handle
                jobject j_video_track = (jobject) env->GetObjectArrayElement(j_video_tracks, i);
                jlong j_video_track_handle =
                        (jlong) env->CallLongMethod(j_video_track,
                                                    j_local_video_track_get_native_handle);
                // Get local video track
                auto video_track = getLocalVideoTrack(j_video_track_handle);
                video_tracks.push_back(video_track);
            }
            builder->setVideoTracks(video_tracks);
        }
    }

    if(!IsNull(env, j_data_tracks)) {
        jclass j_local_data_track_class =
                twilio_video_jni::FindClass(env, "com/twilio/video/LocalDataTrack");
        jmethodID j_local_data_track_get_native_handle =
                webrtc::GetMethodID(env, j_local_data_track_class, "getNativeHandle", "()J");

        std::vector<std::shared_ptr<twilio::media::LocalDataTrack>> data_tracks;
        int size = env->GetArrayLength(j_data_tracks);
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                // Get local data track handle
                jobject j_data_track = (jobject) env->GetObjectArrayElement(j_data_tracks, i);
                jlong j_data_track_handle =
                        (jlong) env->CallLongMethod(j_data_track,
                                                    j_local_data_track_get_native_handle);
                // Get local data track
                auto data_track = getLocalDataTrack(j_data_track_handle);
                data_tracks.push_back(data_track);
            }
            builder->setDataTracks(data_tracks);
        }
    }

    if (!IsNull(env, j_preferred_audio_codecs)) {
        std::vector<std::shared_ptr<twilio::media::AudioCodec>> preferred_audio_codecs;
        int size = env->GetArrayLength(j_preferred_audio_codecs);
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                jobject j_audio_codec =
                        (jobject) env->GetObjectArrayElement(j_preferred_audio_codecs, i);
                std::shared_ptr<twilio::media::AudioCodec> audio_codec =
                        getAudioCodec(env, j_audio_codec);
                preferred_audio_codecs.push_back(audio_codec);
            }
            builder->setPreferredAudioCodecs(preferred_audio_codecs);
        }
    }

    if (!IsNull(env, j_preferred_video_codecs)) {
        std::vector<std::shared_ptr<twilio::media::VideoCodec>> preferred_video_codecs;
        int size = env->GetArrayLength(j_preferred_video_codecs);
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                jobject j_video_codec =
                        (jobject) env->GetObjectArrayElement(j_preferred_video_codecs, i);
                std::shared_ptr<twilio::media::VideoCodec> video_codec =
                        getVideoCodec(env, j_video_codec);
                preferred_video_codecs.push_back(video_codec);
            }
            builder->setPreferredVideoCodecs(preferred_video_codecs);
        }
    }

    if(!IsNull(env, j_region)){
        std::string region = JavaToUTF8StdString(env, j_region);
        builder->setRegion(region);
    }

    if (!IsNull(env, j_ice_options)) {
        twilio::media::IceOptions ice_options = IceOptions::getIceOptions(env, j_ice_options);
        builder->setIceOptions(ice_options);
    }

    if (!IsNull(env, j_encoding_parameters)) {
        twilio::media::EncodingParameters encoding_parameters =
                getEncodingParameters(env, j_encoding_parameters);
        builder->setEncodingParameters(encoding_parameters);
    }

    PlatformInfoContext *platform_info_context =
            reinterpret_cast<PlatformInfoContext *>(j_platform_info_handle);
    if (platform_info_context != nullptr) {
        builder->setPlatformInfo(platform_info_context->platform_info);
    }

    builder->enableInsights(j_enable_insights);
    builder->enableAutomaticSubscription(j_enable_automatic_subscription);
    builder->enableNetworkQuality(j_enable_network_quality);
    builder->enableDominantSpeaker(j_enable_dominant_speaker);

    return webrtc::NativeToJavaPointer(builder);
}

}
