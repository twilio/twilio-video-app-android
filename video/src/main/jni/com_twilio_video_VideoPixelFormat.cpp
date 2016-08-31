#include <string>

#include "com_twilio_video_VideoPixelFormat.h"

#include "webrtc/api/java/jni/jni_helpers.h"

namespace twilio_video_jni {

JNIEXPORT jint JNICALL Java_com_twilio_video_VideoPixelFormat_nativeGetValue(JNIEnv *jni,
                                                                             jobject j_video_pixel_format,
                                                                             jstring j_video_pixel_format_name) {
    std::string name = webrtc_jni::JavaToStdString(jni, j_video_pixel_format_name);

    if (name == "NV21") {
        return kVideoPixelFormatNv21;
    } else if (name == "RGBA_8888") {
        return kVideoPixelFormatRgba8888;
    } else {
        return cricket::FOURCC_ANY;
    }
}

}
