#include <jni.h>
#include <stdint.h>

#ifndef VIDEO_ANDROID_COM_TWILIO_VIDEO_VIDEOCAPTURERDELEGATE_H_
#define VIDEO_ANDROID_COM_TWILIO_VIDEO_VIDEOCAPTURERDELEGATE_H_

#include "webrtc/base/refcount.h"
#include "android_video_capturer_jni.h"

#ifdef __cplusplus
extern "C" {
#endif

namespace twilio_video_jni {

    class VideoCapturerDelegate : public AndroidVideoCapturerJni {
    public:
        VideoCapturerDelegate(JNIEnv *jni, jobject j_video_capturer, jobject j_egl_context,
                              bool is_screencast) :
                AndroidVideoCapturerJni(jni,
                                        j_video_capturer,
                                        j_egl_context,
                                        is_screencast) { }

        void Start(const cricket::VideoFormat &capture_format,
                   AndroidVideoCapturer *capturer) override;

        std::vector<cricket::VideoFormat> GetSupportedFormats() override;

        void OnMemoryBufferFrame(void *video_frame,
                                 int length,
                                 int width,
                                 int height,
                                 int rotation,
                                 int64_t timestamp_ns) override;

    private:
        uint32_t capture_pixel_format_;
    };

JNIEXPORT void JNICALL
Java_com_twilio_video_VideoCapturerDelegate_00024NativeObserver_nativeCapturerStarted(JNIEnv *env,
                                                                                 jobject instance,
                                                                                 jlong nativeCapturer,
                                                                                 jboolean success);

JNIEXPORT void JNICALL
Java_com_twilio_video_VideoCapturerDelegate_00024NativeObserver_nativeOnByteBufferFrameCaptured(JNIEnv *env,
                                                                                           jobject instance,
                                                                                           jlong nativeCapturer,
                                                                                           jbyteArray data_,
                                                                                           jint length,
                                                                                           jint width,
                                                                                           jint height,
                                                                                           jint rotation,
                                                                                           jlong timeStamp);

JNIEXPORT void JNICALL
Java_com_twilio_video_VideoCapturerDelegate_00024NativeObserver_nativeOnTextureFrameCaptured(JNIEnv *env,
                                                                                        jobject instance,
                                                                                        jlong nativeCapturer,
                                                                                        jint width,
                                                                                        jint height,
                                                                                        jint oesTextureId,
                                                                                        jfloatArray transformMatrix_,
                                                                                        jint rotation,
                                                                                        jlong timestamp);
}

#ifdef __cplusplus
}
#endif

#endif // VIDEO_ANDROID_COM_TWILIO_VIDEO_VIDEOCAPTURERDELEGATE_H_
