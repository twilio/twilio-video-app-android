#include <string>

#include "com_twilio_video_VideoPixelFormat.h"
#include "class_reference_holder.h"

#include "webrtc/api/java/jni/jni_helpers.h"

namespace twilio_video_jni {

jobject VideoPixelFormat::getJavaVideoPixelFormat(uint32_t fourcc) {
    JNIEnv* jni = webrtc_jni::AttachCurrentThreadIfNeeded();
    const char* j_video_pixel_format_sig = "Lcom/twilio/video/VideoPixelFormat;";
    jclass j_video_pixel_format_class = twilio_video_jni::FindClass(jni,
                                                                    "com/twilio/video/VideoPixelFormat");
    jfieldID j_video_pixel_format_field_id;

    switch(fourcc) {
        case cricket::FOURCC_ABGR:
            j_video_pixel_format_field_id = jni->GetStaticFieldID(j_video_pixel_format_class,
                                                                  "RGBA_8888",
                                                                  j_video_pixel_format_sig);
            CHECK_EXCEPTION(jni);
            break;
        case cricket::FOURCC_NV21:
            j_video_pixel_format_field_id = jni->GetStaticFieldID(j_video_pixel_format_class,
                                                                  "NV21",
                                                                  j_video_pixel_format_sig);
            CHECK_EXCEPTION(jni);
            break;
        default:
            break;
    }

    return jni->GetStaticObjectField(j_video_pixel_format_class,
                                     j_video_pixel_format_field_id);
}

JNIEXPORT jint JNICALL Java_com_twilio_video_VideoPixelFormat_nativeGetValue(JNIEnv *jni,
                                                                             jobject j_video_pixel_format,
                                                                             jstring j_video_pixel_format_name) {
    std::string name = webrtc_jni::JavaToStdString(jni, j_video_pixel_format_name);

    if (name == "NV21") {
        return VideoPixelFormat::kVideoPixelFormatNv21;
    } else if (name == "RGBA_8888") {
        return VideoPixelFormat::kVideoPixelFormatRgba8888;
    } else {
        FATAL() << "Failed to translate VideoPixelFormat to cricket fourcc";
    }
}

}
