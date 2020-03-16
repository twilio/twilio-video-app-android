/*
 * Copyright (C) 2020 Twilio, Inc.
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

#include "com_twilio_video_NetworkQuality.h"
#include "class_reference_holder.h"
#include "jni_utils.h"

namespace twilio_video_jni {

jobject createJavaNetworkQualityLevel(JNIEnv *env, twilio::video::NetworkQualityLevel level) {
    char const *network_quality_level_string;

    switch (level) {
        case twilio::video::kNetworkQualityLevelUnknown:
            network_quality_level_string = "NETWORK_QUALITY_LEVEL_UNKNOWN";
            break;
        case twilio::video::kNetworkQualityLevelZero:
            network_quality_level_string = "NETWORK_QUALITY_LEVEL_ZERO";
            break;
        case twilio::video::kNetworkQualityLevelOne:
            network_quality_level_string = "NETWORK_QUALITY_LEVEL_ONE";
            break;
        case twilio::video::kNetworkQualityLevelTwo:
            network_quality_level_string = "NETWORK_QUALITY_LEVEL_TWO";
            break;
        case twilio::video::kNetworkQualityLevelThree:
            network_quality_level_string = "NETWORK_QUALITY_LEVEL_THREE";
            break;
        case twilio::video::kNetworkQualityLevelFour:
            network_quality_level_string = "NETWORK_QUALITY_LEVEL_FOUR";
            break;
        case twilio::video::kNetworkQualityLevelFive:
            network_quality_level_string = "NETWORK_QUALITY_LEVEL_FIVE";
            break;
        default:
            FATAL() << "Unknown Network Quality Level. There is no corresponding Java enum value";
            break;
    }

    auto j_network_quality_level_class = twilio_video_jni::FindClass(env, "com/twilio/video/NetworkQualityLevel");
    jfieldID j_level_field = env->GetStaticFieldID(j_network_quality_level_class,
                                                   network_quality_level_string,
                                                   "Lcom/twilio/video/NetworkQualityLevel;");

    jobject j_network_quality_level = env->GetStaticObjectField(j_network_quality_level_class, j_level_field);
    CHECK_EXCEPTION(env) << "Error getting NetworkQualityLevel value";

    return j_network_quality_level;
}

twilio::video::NetworkQualityVerbosity getCoreNetworkQualityVerbosity(JNIEnv *env, jobject j_network_quality_verbosity) {
    auto core_verbosity = twilio::video::NetworkQualityVerbosity::kNone;

    jclass j_network_quality_verbosity_class = GetObjectClass(env, j_network_quality_verbosity);
    jfieldID j_verbosity_minimal_field = env->GetStaticFieldID(j_network_quality_verbosity_class,
                                                               "NETWORK_QUALITY_VERBOSITY_MINIMAL",
                                                               "Lcom/twilio/video/NetworkQualityVerbosity;");
    jobject j_verbosity_minimal = env->GetStaticObjectField(j_network_quality_verbosity_class, j_verbosity_minimal_field);
    CHECK_EXCEPTION(env) << "Error getting NETWORK_QUALITY_VERBOSITY_MINIMAL";

    if (env->IsSameObject(j_network_quality_verbosity, j_verbosity_minimal)) {
        core_verbosity = twilio::video::NetworkQualityVerbosity::kMinimal;
    }

    return core_verbosity;
}

twilio::video::NetworkQualityConfiguration getCoreNetworkQualityConfiguration(JNIEnv *env, jobject j_network_quality_config) {
    jclass j_network_quality_config_class = GetObjectClass(env, j_network_quality_config);

    // Get the Java local verbosity
    jfieldID j_local_verbosity_field_id = env->GetFieldID(j_network_quality_config_class,
                                                          "local",
                                                          "Lcom/twilio/video/NetworkQualityVerbosity;");
    jobject j_local_verbosity = env->GetObjectField(j_network_quality_config, j_local_verbosity_field_id);
    CHECK_EXCEPTION(env) << "Error getting local network quality verbosity";
    twilio::video::NetworkQualityVerbosity local_verbosity = getCoreNetworkQualityVerbosity(env, j_local_verbosity);

    // Get the Java remote verbosity
    jfieldID j_remote_verbosity_field_id = env->GetFieldID(j_network_quality_config_class,
                                                           "remote",
                                                           "Lcom/twilio/video/NetworkQualityVerbosity;");
    jobject j_remote_verbosity = env->GetObjectField(j_network_quality_config, j_remote_verbosity_field_id);
    CHECK_EXCEPTION(env) << "Error getting remote network quality verbosity";
    twilio::video::NetworkQualityVerbosity remote_verbosity = getCoreNetworkQualityVerbosity(env, j_remote_verbosity);

    return twilio::video::NetworkQualityConfiguration::Builder()
            .setLocalVerbosityLevel(local_verbosity)
            .setRemoteVerbosityLevel(remote_verbosity)
            .build();
}

}