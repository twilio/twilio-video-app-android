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

#include <string.h>
#include <jni.h>
#include <stdlib.h>

JNIEXPORT void JNICALL
Java_com_twilio_video_env_Env_nativeSet(JNIEnv *env, jclass type, jstring j_name, jstring j_value, jboolean j_overwrite) {
    const char *name= (*env)->GetStringUTFChars(env, j_name, 0);
    const char *value= (*env)->GetStringUTFChars(env, j_value, 0);

    setenv(name, value, j_overwrite);

    (*env)->ReleaseStringUTFChars(env, j_name, name);
    (*env)->ReleaseStringUTFChars(env, j_value, value);
}

JNIEXPORT jstring JNICALL
Java_com_twilio_video_env_Env_nativeGet(JNIEnv *env, jclass type, jstring j_name) {
    const char *name= (*env)->GetStringUTFChars(env, j_name, 0);

    const char *value = getenv(name);

    (*env)->ReleaseStringUTFChars(env, j_name, name);

    return (*env)->NewStringUTF(env, value);
}

