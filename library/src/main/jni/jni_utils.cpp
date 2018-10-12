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

#include "jni_utils.h"
#include <codecvt>
#include <locale>
#include "webrtc/sdk/android/src/jni/jni_helpers.h"

namespace twilio_video_jni {

/*
 * IsNull, GetObjectClass, GetFieldID, and GetObjectField were removed in WebRTC 67, but are 
 * still used in the SDK. These functions were ported from WebRTC 57.
 *
 * https://code.hq.twilio.com/client/twilio-webrtc/blob/twilio-webrtc-57/webrtc/sdk/android/src/jni/jni_helpers.cc
 */

bool IsNull(JNIEnv* jni, jobject obj) {
    return jni->IsSameObject(obj, nullptr);
}

jclass GetObjectClass(JNIEnv *jni, jobject object) {
    jclass c = jni->GetObjectClass(object);
    CHECK_EXCEPTION(jni) << "error during GetObjectClass";
    RTC_CHECK(c) << "GetObjectClass returned NULL";
    return c;
}

jfieldID GetFieldID(JNIEnv *jni, jclass c, const char *name, const char *signature) {
    jfieldID f = jni->GetFieldID(c, name, signature);
    CHECK_EXCEPTION(jni) << "error during GetFieldID";
    RTC_CHECK(f) << name << ", " << signature;
    return f;
}

jobject GetObjectField(JNIEnv *jni, jobject object, jfieldID id) {
    jobject o = jni->GetObjectField(object, id);
    CHECK_EXCEPTION(jni) << "error during GetObjectField";
    RTC_CHECK(!IsNull(jni, o)) << "GetObjectField returned NULL";
    return o;
}

jint GetIntField(JNIEnv *jni, jobject object, jfieldID id) {
    jint i = jni->GetIntField(object, id);
    CHECK_EXCEPTION(jni) << "error during GetIntField";
    return i;
}

std::string JavaToUTF8StdString(JNIEnv *jni, const jstring &j_string) {
    std::wstring_convert<std::codecvt_utf8_utf16<char16_t>, char16_t> convert;

    const jchar* jchars = jni->GetStringChars(j_string, NULL);
    CHECK_EXCEPTION(jni) << "Error during GetStringChars";
    size_t len = jni->GetStringLength(j_string);
    CHECK_EXCEPTION(jni) << "Error during GetStringLength";

    auto str = convert.to_bytes(std::u16string(reinterpret_cast<char16_t const*>(jchars), len));
    jni->ReleaseStringChars(j_string, jchars);
    CHECK_EXCEPTION(jni) << "Error during ReleaseStringChars";
    return str;
}

jstring JavaUTF16StringFromStdString(JNIEnv* jni, std::string const& string) {
    std::wstring_convert<std::codecvt_utf8_utf16<char16_t>, char16_t> convert;

    auto str = convert.from_bytes(string);
    jstring jstr = jni->NewString(reinterpret_cast<jchar const*>(str.c_str()), str.size());
    CHECK_EXCEPTION(jni) << "error during NewString";
    return jstr;
}

JNIEXPORT jstring JNICALL Java_com_twilio_video_JniUtils_nativeJavaUtf16StringToStdString(JNIEnv *env,
                                                                                          jobject instance,
                                                                                          jstring j_input_string) {
    std::string input_string = JavaToUTF8StdString(env, j_input_string);

    return JavaUTF16StringFromStdString(env, input_string);
}

}
