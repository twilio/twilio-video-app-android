#include "com_twilio_video_VideoCapturerDelegate.h"
#include "com_twilio_video_VideoPixelFormat.h"
#include "class_reference_holder.h"
#include "libyuv/convert.h"

namespace twilio_video_jni {
    void VideoCapturerDelegate::Start(const cricket::VideoFormat& capture_format,
                                      webrtc::AndroidVideoCapturer* capturer) {
        JNIEnv* jni = webrtc_jni::AttachCurrentThreadIfNeeded();
        capture_pixel_format_ = capture_format.fourcc;
        jclass j_video_capturer_delegate_class = twilio_video_jni::FindClass(jni,
                                                                             "com/twilio/video/VideoCapturerDelegate");
        const char* j_video_pixel_format_sig = "Lcom/twilio/video/VideoPixelFormat;";
        jclass j_video_pixel_format_class = twilio_video_jni::FindClass(jni,
                                                                        "com/twilio/video/VideoPixelFormat");
        jfieldID j_video_pixel_format_field_id;

        switch(capture_pixel_format_) {
            case cricket::FOURCC_ABGR:
                j_video_pixel_format_field_id = jni->GetStaticFieldID(j_video_pixel_format_class,
                                                                      "RGBA_8888",
                                                                      j_video_pixel_format_sig);
                break;
            case cricket::FOURCC_NV21:
                j_video_pixel_format_field_id = jni->GetStaticFieldID(j_video_pixel_format_class,
                                                                      "NV21",
                                                                      j_video_pixel_format_sig);
                break;
            default:
                break;
        }

        jobject j_video_pixel_format = jni->GetStaticObjectField(j_video_pixel_format_class,
                                                                 j_video_pixel_format_field_id);
        jni->CallVoidMethod(*j_video_capturer_,
                            webrtc_jni::GetMethodID(jni,
                                                    j_video_capturer_delegate_class,
                                                    "setVideoPixelFormat",
                                                    "(Lcom/twilio/video/VideoPixelFormat;)V"),
                            j_video_pixel_format);
        webrtc_jni::AndroidVideoCapturerJni::Start(capture_format, capturer);
    }

    std::vector<cricket::VideoFormat> VideoCapturerDelegate::GetSupportedFormats() {
        JNIEnv* jni = webrtc_jni::AttachCurrentThreadIfNeeded();
        jobject j_list_of_formats = jni->CallObjectMethod(
                *j_video_capturer_,
                webrtc_jni::GetMethodID(jni, *j_video_capturer_class_, "getSupportedFormats",
                                        "()Ljava/util/List;"));
        CHECK_EXCEPTION(jni) << "error during getSupportedFormats";

        // Extract Java List<CaptureFormat> to std::vector<cricket::VideoFormat>.
        jclass j_list_class = jni->FindClass("java/util/List");
        jclass j_format_class =
                jni->FindClass("org/webrtc/CameraEnumerationAndroid$CaptureFormat");
        const int size = jni->CallIntMethod(
                j_list_of_formats, webrtc_jni::GetMethodID(jni, j_list_class, "size", "()I"));
        jmethodID j_get =
                webrtc_jni::GetMethodID(jni, j_list_class, "get", "(I)Ljava/lang/Object;");
        jfieldID j_width_field = webrtc_jni::GetFieldID(jni, j_format_class, "width", "I");
        jfieldID j_height_field = webrtc_jni::GetFieldID(jni, j_format_class, "height", "I");
        jfieldID j_max_framerate_field =
                webrtc_jni::GetFieldID(jni, j_format_class, "maxFramerate", "I");
        jfieldID j_format_field =
                webrtc_jni::GetFieldID(jni, j_format_class, "imageFormat", "I");

        std::vector<cricket::VideoFormat> formats;
        formats.reserve(size);
        for (int i = 0; i < size; ++i) {
            jobject j_format = jni->CallObjectMethod(j_list_of_formats, j_get, i);
            const int frame_interval = cricket::VideoFormat::FpsToInterval(
                    webrtc_jni::GetIntField(jni, j_format, j_max_framerate_field));
            formats.emplace_back(webrtc_jni::GetIntField(jni, j_format, j_width_field),
                                 webrtc_jni::GetIntField(jni, j_format, j_height_field),
                                 frame_interval,
                                 webrtc_jni::GetIntField(jni, j_format, j_format_field));
        }
        CHECK_EXCEPTION(jni) << "error while extracting formats";
        return formats;

    }

    void VideoCapturerDelegate::OnMemoryBufferFrame(void *video_frame, int length, int width,
                                                    int height, int rotation,
                                                    int64_t timestamp_ns) {
        rtc::scoped_refptr<webrtc::VideoFrameBuffer> buffer =
                buffer_pool_.CreateBuffer(width, height);

        switch (capture_pixel_format_) {
            /*
             * TODO: Need add support for padding
             */
            case cricket::FOURCC_ABGR: {
                const uint8_t *src_rgba = static_cast<uint8_t *>(video_frame);
                int rgba_stride = 4 * width;

                libyuv::ABGRToI420(
                        src_rgba, rgba_stride,
                        buffer->MutableData(webrtc::kYPlane), buffer->stride(webrtc::kYPlane),
                        buffer->MutableData(webrtc::kUPlane), buffer->stride(webrtc::kUPlane),
                        buffer->MutableData(webrtc::kVPlane), buffer->stride(webrtc::kVPlane),
                        width, height);
                break;
            }
            case cricket::FOURCC_NV21: {
                const uint8_t *y_plane = static_cast<uint8_t *>(video_frame);
                const uint8_t *vu_plane = y_plane + width * height;

                libyuv::NV21ToI420(
                        y_plane, width,
                        vu_plane, width,
                        buffer->MutableData(webrtc::kYPlane), buffer->stride(webrtc::kYPlane),
                        buffer->MutableData(webrtc::kUPlane), buffer->stride(webrtc::kUPlane),
                        buffer->MutableData(webrtc::kVPlane), buffer->stride(webrtc::kVPlane),
                        width, height);
                break;
            }
            default:
                break;
        }
        AsyncCapturerInvoke("OnIncomingFrame",
                            &webrtc::AndroidVideoCapturer::OnIncomingFrame,
                            buffer, rotation, timestamp_ns);
    }
}
