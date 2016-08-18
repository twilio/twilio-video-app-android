#include "com_twilio_video_ConnectOptions.h"
#include "webrtc/api/java/jni/jni_helpers.h"

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

    // TODO: Get local media from j_local_media, once LocalMedia is fully implemented in Java
    // builder.setLocalMedia();

    // TODO: Get ice options from j_ice_options
    // builder.setIceOptions();

    ConnectOptionsContext *data_context = new ConnectOptionsContext();
    data_context->connect_options = builder.build();
    return webrtc_jni::jlongFromPointer(data_context);
}