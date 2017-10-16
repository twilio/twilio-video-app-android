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

#ifndef VIDEO_ANDROID_JNI_UTILS_H_
#define VIDEO_ANDROID_JNI_UTILS_H_

#include <jni.h>
#include <vector>

#ifdef __cplusplus
extern "C" {
#endif

namespace twilio_video_jni {

// Given a (UTF-16) jstring return a new UTF-8 native string.
std::string JavaToUTF8StdString(JNIEnv *jni, const jstring &j_string);

// Given a UTF-8 encoded |native| string return a new (UTF-16) jstring.
jstring JavaUTF16StringFromStdString(JNIEnv* jni, std::string const& string);

JNIEXPORT jstring JNICALL Java_com_twilio_video_JniUtils_nativeJavaUtf16StringToStdString(JNIEnv *,
                                                                                          jobject,
                                                                                          jstring);

}

#ifdef __cplusplus
}
#endif
#endif // VIDEO_ANDROID_JNI_UTILS_H_