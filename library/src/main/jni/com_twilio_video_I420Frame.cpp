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

#include "com_twilio_video_I420Frame.h"
#include "webrtc/media/base/videoframe.h"

namespace twilio_video_jni {

JNIEXPORT void JNICALL Java_com_twilio_video_I420Frame_nativeRelease(JNIEnv *jni,
                                                                     jobject j_i420_frame,
                                                                     jlong i420_frame_pointer) {
    delete reinterpret_cast<const cricket::VideoFrame*>(i420_frame_pointer);
}

}
