#include <jni.h>
#include <stdint.h>

#ifndef VIDEO_ANDROID_COM_TWILIO_VIDEO_VIDEOCAPTURERDELEGATE_H_
#define VIDEO_ANDROID_COM_TWILIO_VIDEO_VIDEOCAPTURERDELEGATE_H_

#include "webrtc/api/java/jni/androidvideocapturer_jni.h"

#ifdef __cplusplus
extern "C" {
#endif

namespace twilio_video_jni {

class VideoCapturerDelegate : public webrtc_jni::AndroidVideoCapturerJni {
public:
    VideoCapturerDelegate(JNIEnv* jni, jobject j_video_capturer, jobject j_egl_context) :
            webrtc_jni::AndroidVideoCapturerJni(jni,
                                                j_video_capturer,
                                                j_egl_context,
                                                false) {}

    void Start(const cricket::VideoFormat& capture_format,
               webrtc::AndroidVideoCapturer* capturer) override;
    std::vector<cricket::VideoFormat> GetSupportedFormats() override;

    void OnMemoryBufferFrame(void* video_frame,
                             int length,
                             int width,
                             int height,
                             int rotation,
                             int64_t timestamp_ns) override;

private:
    uint32_t capture_pixel_format_;
};

}

#ifdef __cplusplus
}
#endif

#endif // VIDEO_ANDROID_COM_TWILIO_VIDEO_VIDEOCAPTURERDELEGATE_H_
