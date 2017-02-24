#include "com_twilio_video_ConnectOptions.h"
#include "com_twilio_video_LocalMedia.h"
#include "com_twilio_video_IceOptions.h"
#include "com_twilio_video_PlatformInfo.h"

#include "webrtc/api/android/jni/jni_helpers.h"
#include "media/ice_options.h"

namespace twilio_video_jni {

JNIEXPORT jlong JNICALL
Java_com_twilio_video_ConnectOptions_nativeCreate(JNIEnv *env,
                                                  jobject j_instance,
                                                  jstring j_access_token,
                                                  jstring j_room_name,
                                                  jobject j_local_media,
                                                  jobject j_ice_options,
                                                  jlong j_platform_info_handle) {

    std::string access_token = webrtc_jni::JavaToStdString(env, j_access_token);
    twilio::video::ConnectOptions::Builder builder =
        twilio::video::ConnectOptions::Builder(access_token);

    if (!webrtc_jni::IsNull(env, j_room_name)) {
        std::string name = webrtc_jni::JavaToStdString(env, j_room_name);
        builder.setRoomName(name);
    }

    if (!webrtc_jni::IsNull(env, j_local_media)) {
        jclass j_local_media_class = webrtc_jni::GetObjectClass(env, j_local_media);
        jmethodID j_getNativeLocalMediaHandle_id =
                webrtc_jni::GetMethodID(env, j_local_media_class,
                                        "getNativeLocalMediaHandle", "()J");
        jlong local_media_handle = env->CallLongMethod(j_local_media,
                                                       j_getNativeLocalMediaHandle_id);
        std::shared_ptr<twilio::media::LocalMedia> local_media = getLocalMedia(local_media_handle);

        builder.setLocalMedia(local_media);
    }

    if (!webrtc_jni::IsNull(env, j_ice_options)) {
        twilio::media::IceOptions ice_options = IceOptions::getIceOptions(env, j_ice_options);
        builder.setIceOptions(ice_options);
    }

    PlatformInfoContext *platform_info_context =
        reinterpret_cast<PlatformInfoContext *>(j_platform_info_handle);
    if (platform_info_context != nullptr) {
        builder.setPlatformInfo(platform_info_context->platform_info);
    }

    twilio_video_jni::ConnectOptionsContext *data_context = new twilio_video_jni::ConnectOptionsContext;
    data_context->connect_options = builder.build();
    return webrtc_jni::jlongFromPointer(data_context);
}

}