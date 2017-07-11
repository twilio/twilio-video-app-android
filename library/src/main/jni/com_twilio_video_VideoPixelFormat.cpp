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

#include <string>

#include "com_twilio_video_VideoPixelFormat.h"
#include "class_reference_holder.h"

#include "webrtc/sdk/android/src/jni/jni_helpers.h"

namespace twilio_video_jni {

jobject VideoPixelFormat::getJavaVideoPixelFormat(uint32_t fourcc) {
    JNIEnv* jni = webrtc_jni::AttachCurrentThreadIfNeeded();
    const char* j_video_pixel_format_sig = "Lcom/twilio/video/VideoPixelFormat;";
    jclass j_video_pixel_format_class = twilio_video_jni::FindClass(jni,
                                                                    "com/twilio/video/VideoPixelFormat");
    jfieldID j_video_pixel_format_field_id;

    switch(fourcc) {
        case cricket::FOURCC_ABGR:
            j_video_pixel_format_field_id = jni->GetStaticFieldID(j_video_pixel_format_class,
                                                                  "RGBA_8888",
                                                                  j_video_pixel_format_sig);
            CHECK_EXCEPTION(jni);
            break;
        case cricket::FOURCC_NV21:
            j_video_pixel_format_field_id = jni->GetStaticFieldID(j_video_pixel_format_class,
                                                                  "NV21",
                                                                  j_video_pixel_format_sig);
            CHECK_EXCEPTION(jni);
            break;
        default:
            break;
    }

    return jni->GetStaticObjectField(j_video_pixel_format_class,
                                     j_video_pixel_format_field_id);
}

JNIEXPORT jint JNICALL Java_com_twilio_video_VideoPixelFormat_nativeGetValue(JNIEnv *jni,
                                                                             jobject j_video_pixel_format,
                                                                             jstring j_video_pixel_format_name) {
    std::string name = webrtc_jni::JavaToStdString(jni, j_video_pixel_format_name);

    if (name == "NV21") {
        return VideoPixelFormat::kVideoPixelFormatNv21;
    } else if (name == "RGBA_8888") {
        return VideoPixelFormat::kVideoPixelFormatRgba8888;
    } else {
        FATAL() << "Failed to translate VideoPixelFormat to cricket fourcc";
        return -1;
    }
}

}
