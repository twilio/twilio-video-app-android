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

#include "class_reference_holder.h"
#include "webrtc/sdk/android/src/jni/jni_helpers.h"

namespace twilio_video_jni {

class ClassReferenceHolder {
public:
    explicit ClassReferenceHolder(JNIEnv* jni);
    ~ClassReferenceHolder();

    void FreeReferences(JNIEnv* jni);
    jclass GetClass(const std::string& name);

private:
    void LoadClass(JNIEnv* jni, const std::string& name);

    std::map<std::string, jclass> classes_;
};

static ClassReferenceHolder*class_reference_holder = nullptr;

void LoadGlobalClassReferenceHolder() {
    RTC_CHECK(class_reference_holder == nullptr);
    class_reference_holder = new ClassReferenceHolder(webrtc_jni::GetEnv());
}

void FreeGlobalClassReferenceHolder() {
    class_reference_holder->FreeReferences(webrtc_jni::AttachCurrentThreadIfNeeded());
    delete class_reference_holder;
    class_reference_holder = nullptr;
}

ClassReferenceHolder::ClassReferenceHolder(JNIEnv* jni) {
    LoadClass(jni, "com/twilio/video/TwilioException");
    LoadClass(jni, "com/twilio/video/VideoCapturerDelegate");
    LoadClass(jni, "com/twilio/video/VideoPixelFormat");
    LoadClass(jni, "com/twilio/video/LocalAudioTrack");
    LoadClass(jni, "com/twilio/video/LocalVideoTrack");
    LoadClass(jni, "com/twilio/video/Video$NetworkChangeEvent");
    LoadClass(jni, "com/twilio/video/VideoCapturerDelegate$NativeObserver");
    LoadClass(jni, "com/twilio/video/RemoteParticipant");
    LoadClass(jni, "java/util/ArrayList");
    LoadClass(jni, "com/twilio/video/RemoteAudioTrack");
    LoadClass(jni, "com/twilio/video/RemoteVideoTrack");
    LoadClass(jni, "com/twilio/video/StatsReport");
    LoadClass(jni, "com/twilio/video/LocalAudioTrackStats");
    LoadClass(jni, "com/twilio/video/LocalVideoTrackStats");
    LoadClass(jni, "com/twilio/video/RemoteAudioTrackStats");
    LoadClass(jni, "com/twilio/video/RemoteVideoTrackStats");
    LoadClass(jni, "com/twilio/video/VideoDimensions");
}

ClassReferenceHolder::~ClassReferenceHolder() {
    RTC_CHECK(classes_.empty()) << "Must call FreeReferences() before dtor!";
}

void ClassReferenceHolder::FreeReferences(JNIEnv* jni) {
    for (std::map<std::string, jclass>::const_iterator it = classes_.begin();
         it != classes_.end(); ++it) {
        jni->DeleteGlobalRef(it->second);
    }
    classes_.clear();
}

jclass ClassReferenceHolder::GetClass(const std::string& name) {
    std::map<std::string, jclass>::iterator it = classes_.find(name);
    RTC_CHECK(it != classes_.end()) << "Unexpected GetClass() call for: " << name;
    return it->second;
}

void ClassReferenceHolder::LoadClass(JNIEnv* jni, const std::string& name) {
    jclass localRef = jni->FindClass(name.c_str());
    CHECK_EXCEPTION(jni) << "error during FindClass: " << name;
    RTC_CHECK(localRef) << name;
    jclass globalRef = reinterpret_cast<jclass>(jni->NewGlobalRef(localRef));
    CHECK_EXCEPTION(jni) << "error during NewGlobalRef: " << name;
    RTC_CHECK(globalRef) << name;
    bool inserted = classes_.insert(std::make_pair(name, globalRef)).second;
    RTC_CHECK(inserted) << "Duplicate class name: " << name;
}

jclass FindClass(JNIEnv* jni, const char* name) {
    return class_reference_holder->GetClass(name);
}

}

