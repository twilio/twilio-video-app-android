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

#include "com_twilio_video_LocalDataTrack.h"
#include "webrtc/sdk/android/src/jni/jni_helpers.h"
#include "class_reference_holder.h"
#include "jni_utils.h"

namespace twilio_video_jni {

static const char *const kLocalDataTrackConstructorSignature = "("
        "J"
        "Z"
        "Z"
        "Z"
        "I"
        "I"
        "Ljava/lang/String;"
        "Ljava/lang/String;"
        "Lcom/twilio/video/MediaFactory;"
        ")V";

std::shared_ptr<twilio::media::LocalDataTrack> getLocalDataTrack(jlong local_data_track_handle) {
    LocalDataTrackContext* data_track_context =
            reinterpret_cast<LocalDataTrackContext *>(local_data_track_handle);

    return data_track_context->local_data_track;
}

jobject createJavaLocalDataTrack(std::shared_ptr<twilio::media::LocalDataTrack> local_data_track,
                                 jobject j_media_factory) {
    JNIEnv *jni = webrtc_jni::GetEnv();
    jclass j_data_track_class = twilio_video_jni::FindClass(jni,
                                                            "com/twilio/video/LocalDataTrack");
    jmethodID j_data_track_ctor_id = webrtc_jni::GetMethodID(jni,
                                                             j_data_track_class,
                                                             "<init>",
                                                             kLocalDataTrackConstructorSignature);
    LocalDataTrackContext* data_track_context = new LocalDataTrackContext();
    data_track_context->local_data_track = local_data_track;
    jstring j_track_id = JavaUTF16StringFromStdString(jni, local_data_track->getTrackId());
    jstring j_name = JavaUTF16StringFromStdString(jni, local_data_track->getName());
    jobject j_local_data_track = jni->NewObject(j_data_track_class,
                                                j_data_track_ctor_id,
                                                webrtc_jni::jlongFromPointer(data_track_context),
                                                local_data_track->isEnabled(),
                                                local_data_track->isOrdered(),
                                                local_data_track->isReliable(),
                                                local_data_track->getMaxPacketLifeTime(),
                                                local_data_track->getMaxRetransmits(),
                                                j_track_id,
                                                j_name,
                                                j_media_factory);
    CHECK_EXCEPTION(jni) << "Error creating LocalDataTrack";

    return j_local_data_track;
}

JNIEXPORT void JNICALL Java_com_twilio_video_LocalDataTrack_nativeBufferSend(JNIEnv *jni,
                                                                             jobject j_local_data_track,
                                                                             jlong local_data_track_handle,
                                                                             jbyteArray j_message_buffer) {
    LocalDataTrackContext* local_data_track_context =
            reinterpret_cast<LocalDataTrackContext *>(local_data_track_handle);

    // Convert Java byte array to uint8_t*
    jsize message_size = jni->GetArrayLength(j_message_buffer);
    uint8_t* message_buffer = new uint8_t[message_size];
    jni->GetByteArrayRegion(j_message_buffer,
                            0,
                            message_size,
                            reinterpret_cast<jbyte*>(message_buffer));

    local_data_track_context->local_data_track->send(message_buffer,
                                                     (size_t) message_size);
}

JNIEXPORT void JNICALL Java_com_twilio_video_LocalDataTrack_nativeStringSend(JNIEnv *jni,
                                                                             jobject j_local_data_track,
                                                                             jlong local_data_track_handle,
                                                                             jstring j_message) {
    LocalDataTrackContext* local_data_track_context =
            reinterpret_cast<LocalDataTrackContext *>(local_data_track_handle);

    local_data_track_context->
            local_data_track->send(JavaToUTF8StdString(jni, j_message));
}

JNIEXPORT void JNICALL Java_com_twilio_video_LocalDataTrack_nativeRelease(JNIEnv *jni,
                                                                          jobject j_local_data_track,
                                                                          jlong local_data_track_handle) {
    LocalDataTrackContext* local_data_track_context =
            reinterpret_cast<LocalDataTrackContext *>(local_data_track_handle);

    delete local_data_track_context;
}

}
