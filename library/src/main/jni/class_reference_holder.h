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
