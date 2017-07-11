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

#ifndef VIDEO_ANDROID_ANDROID_CLASS_REFERENCE_HOLDER_H_
#define VIDEO_ANDROID_ANDROID_CLASS_REFERENCE_HOLDER_H_

#include <jni.h>
#include <map>
#include <string>

namespace twilio_video_jni {
    // LoadGlobalClassReferenceHolder must be called in JNI_OnLoad.
    void LoadGlobalClassReferenceHolder();
    // FreeGlobalClassReferenceHolder must be called in JNI_UnLoad.
    void FreeGlobalClassReferenceHolder();

    // Returns a global reference guaranteed to be valid for the lifetime of the process
    jclass FindClass(JNIEnv* jni, const char* name);
}

#endif // VIDEO_ANDROID_ANDROID_CLASS_REFERENCE_HOLDER_H_
