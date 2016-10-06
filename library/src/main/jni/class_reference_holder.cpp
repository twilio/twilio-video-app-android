#include "class_reference_holder.h"
#include "webrtc/api/java/jni/jni_helpers.h"

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
    LoadClass(jni, "com/twilio/video/VideoCapturerDelegate");
    LoadClass(jni, "com/twilio/video/VideoPixelFormat");
    LoadClass(jni, "com/twilio/video/LocalAudioTrack");
    LoadClass(jni, "com/twilio/video/LocalVideoTrack");
    LoadClass(jni, "com/twilio/video/VideoClient$NetworkChangeEvent");
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

