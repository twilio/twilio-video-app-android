#include "com_twilio_video_PlatformInfo.h"
#include "video/logger.h"
#include "webrtc/api/android/jni/jni_helpers.h"

namespace twilio_video_jni {

JNIEXPORT jlong JNICALL Java_com_twilio_video_PlatformInfo_nativeCreate(
    JNIEnv *env,
    jobject j_instance,
    jstring j_platform_name,
    jstring j_platform_version,
    jstring j_hw_device_manufacturer,
    jstring j_hw_device_model,
    jstring j_sdk_version,
    jstring j_hw_device_arch) {

    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "Create PlatformInfo");

    PlatformInfoContext *context = new PlatformInfoContext();
    if (!webrtc_jni::IsNull(env, j_platform_name)) {
        context->platform_info.platformName =
            webrtc_jni::JavaToStdString(env, j_platform_name);
    }
    if (!webrtc_jni::IsNull(env, j_platform_version)) {
        context->platform_info.platformVersion =
            webrtc_jni::JavaToStdString(env, j_platform_version);
    }
    if (!webrtc_jni::IsNull(env, j_hw_device_manufacturer)) {
        context->platform_info.hwDeviceManufacturer =
            webrtc_jni::JavaToStdString(env, j_hw_device_manufacturer);
    }
    if (!webrtc_jni::IsNull(env, j_hw_device_model)) {
        context->platform_info.hwDeviceModel =
            webrtc_jni::JavaToStdString(env, j_hw_device_model);
    }
    if (!webrtc_jni::IsNull(env, j_sdk_version)) {
        context->platform_info.sdkVersion =
            webrtc_jni::JavaToStdString(env, j_sdk_version);
    }
    if (!webrtc_jni::IsNull(env, j_hw_device_arch)) {
        context->platform_info.hwDeviceArch =
            webrtc_jni::JavaToStdString(env, j_hw_device_arch);
    }
    return webrtc_jni::jlongFromPointer(context);
}


JNIEXPORT void JNICALL Java_com_twilio_video_PlatformInfo_nativeRelease
    (JNIEnv *env, jobject j_instance, jlong j_native_handle) {

    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "Free PlatformInfo");
    PlatformInfoContext *platform_info_context =
        reinterpret_cast<PlatformInfoContext *>(j_native_handle);
    if (platform_info_context != nullptr) {
        delete platform_info_context;
    }
}

}