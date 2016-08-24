#include "com_twilio_video_ConnectOptions.h"
#include "com_twilio_video_LocalMedia.h"

#include "webrtc/api/java/jni/jni_helpers.h"

namespace twilio_video_jni {

JNIEXPORT jlong JNICALL
Java_com_twilio_video_ConnectOptions_nativeCreate(JNIEnv *env,
                                                  jobject j_instance,
                                                  jstring j_name,
                                                  jobject j_local_media,
                                                  jobject j_ice_options) {

    twilio::video::ConnectOptions::Builder builder = twilio::video::ConnectOptions::Builder();

    if (!webrtc_jni::IsNull(env, j_name)) {
        std::string name = webrtc_jni::JavaToStdString(env, j_name);
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

    // TODO: Get ice options from j_ice_options
    // builder.setIceOptions();

    ConnectOptionsContext *data_context = new ConnectOptionsContext();
    data_context->connect_options = builder.build();
    return webrtc_jni::jlongFromPointer(data_context);
}

}