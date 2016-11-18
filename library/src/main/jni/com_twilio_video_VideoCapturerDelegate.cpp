#include "com_twilio_video_VideoCapturerDelegate.h"
#include "com_twilio_video_VideoPixelFormat.h"
#include "class_reference_holder.h"
#include "libyuv/convert.h"

namespace twilio_video_jni {
    void VideoCapturerDelegate::Start(const cricket::VideoFormat& capture_format,
                                      AndroidVideoCapturer* capturer) {
        JNIEnv* jni = webrtc_jni::AttachCurrentThreadIfNeeded();
        jclass j_video_capturer_delegate_class =
                twilio_video_jni::FindClass(jni, "com/twilio/video/VideoCapturerDelegate");
        capture_pixel_format_ = capture_format.fourcc;
        jobject j_video_pixel_format =
                VideoPixelFormat::getJavaVideoPixelFormat(capture_pixel_format_) ;
        jni->CallVoidMethod(*j_video_capturer_,
                            webrtc_jni::GetMethodID(jni,
                                                    j_video_capturer_delegate_class,
                                                    "setVideoPixelFormat",
                                                    "(Lcom/twilio/video/VideoPixelFormat;)V"),
                            j_video_pixel_format);
        AndroidVideoCapturerJni::Start(capture_format, capturer);
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
        jclass j_framerate_class = jni->FindClass(
                "org/webrtc/CameraEnumerationAndroid$CaptureFormat$FramerateRange");
        const int size = jni->CallIntMethod(
                j_list_of_formats, webrtc_jni::GetMethodID(jni, j_list_class, "size", "()I"));
        jmethodID j_get =
                webrtc_jni::GetMethodID(jni, j_list_class, "get", "(I)Ljava/lang/Object;");
        jfieldID j_framerate_field = webrtc_jni::GetFieldID(
                jni, j_format_class, "framerate",
                "Lorg/webrtc/CameraEnumerationAndroid$CaptureFormat$FramerateRange;");
        jfieldID j_width_field = webrtc_jni::GetFieldID(jni, j_format_class, "width", "I");
        jfieldID j_height_field = webrtc_jni::GetFieldID(jni, j_format_class, "height", "I");
        jfieldID j_max_framerate_field =
                webrtc_jni::GetFieldID(jni, j_framerate_class, "max", "I");
        jfieldID j_format_field =
                webrtc_jni::GetFieldID(jni, j_format_class, "imageFormat", "I");

        std::vector<cricket::VideoFormat> formats;
        formats.reserve(size);
        for (int i = 0; i < size; ++i) {
            jobject j_format = jni->CallObjectMethod(j_list_of_formats, j_get, i);
            jobject j_framerate = webrtc_jni::GetObjectField(jni, j_format, j_framerate_field);
            const int frame_interval = cricket::VideoFormat::FpsToInterval(
                    webrtc_jni::GetIntField(jni, j_framerate, j_max_framerate_field));
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
        RTC_DCHECK(rotation == 0 || rotation == 90 || rotation == 180 ||
                   rotation == 270);
        rtc::CritScope cs(&capturer_lock_);
        if (!capturer_) {
            LOG(LS_WARNING) << "OnMemoryBufferFrame() called for closed capturer.";
            return;
        }
        int adapted_width;
        int adapted_height;
        int crop_width;
        int crop_height;
        int crop_x;
        int crop_y;
        int64_t translated_camera_time_us;

        if (!capturer_->AdaptFrame(width, height,
                                   timestamp_ns / rtc::kNumNanosecsPerMicrosec,
                                   rtc::TimeMicros(),
                                   &adapted_width, &adapted_height,
                                   &crop_width, &crop_height, &crop_x, &crop_y,
                                   &translated_camera_time_us)) {
            return;
        }

        int rotated_width = crop_width;
        int rotated_height = crop_height;

        if (capturer_->apply_rotation() && (rotation == 90 || rotation == 270)) {
            std::swap(adapted_width, adapted_height);
            std::swap(rotated_width, rotated_height);
        }

        rtc::scoped_refptr<webrtc::VideoFrameBuffer> buffer =
                pre_scale_pool_.CreateBuffer(rotated_width, rotated_height);

        const uint8_t* y_plane = static_cast<const uint8_t*>(video_frame);
        const uint8_t* uv_plane = y_plane + width * height;

        // Can only crop at even pixels.
        crop_x &= ~1;
        crop_y &= ~1;
        int uv_width = (width + 1) / 2;

        switch (capture_pixel_format_) {
            /*
             * TODO: Need add support for padding
             */
            case cricket::FOURCC_ABGR: {
                const uint8_t *src_rgba = static_cast<uint8_t *>(video_frame);
                int rgba_stride = 4 * width;

                libyuv::ABGRToI420(
                        src_rgba, rgba_stride,
                        (uint8 *) buffer->DataY(), buffer->StrideY(),
                        (uint8 *) buffer->DataU(), buffer->StrideU(),
                        (uint8 *) buffer->DataV(), buffer->StrideV(),
                        width, height);
                break;
            }
            case cricket::FOURCC_NV21: {
                const uint8_t* y_plane = static_cast<const uint8_t*>(video_frame);
                const uint8_t* uv_plane = y_plane + width * height;

                // Can only crop at even pixels.
                crop_x &= ~1;
                crop_y &= ~1;
                int uv_width = (width + 1) / 2;

                libyuv::NV12ToI420Rotate(
                        y_plane + width * crop_y + crop_x, width,
                        uv_plane + uv_width * crop_y + crop_x, width,
                        (uint8 *) buffer->DataY(), buffer->StrideY(),
                        // Swap U and V, since we have NV21, not NV12.
                        (uint8 *) buffer->DataV(), buffer->StrideV(),
                        (uint8 *) buffer->DataU(), buffer->StrideU(),
                        crop_width, crop_height, static_cast<libyuv::RotationMode>(
                                capturer_->apply_rotation() ? rotation : 0));
                break;
            }
            default:
                break;
        }

        if (adapted_width != buffer->width() || adapted_height != buffer->height()) {
            rtc::scoped_refptr<webrtc::I420Buffer> scaled_buffer(
                    post_scale_pool_.CreateBuffer(adapted_width, adapted_height));
            scaled_buffer->ScaleFrom(buffer);
            buffer = scaled_buffer;
        }
        const cricket::WebRtcVideoFrame& frame = cricket::WebRtcVideoFrame(
                buffer, capturer_->apply_rotation()
                        ? webrtc::kVideoRotation_0
                        : static_cast<webrtc::VideoRotation>(rotation),
                translated_camera_time_us, 0);
        frame.video_frame_buffer()->AddRef();
        capturer_->OnFrame(
                frame,
                width, height);
    }

    JNIEXPORT void JNICALL
    Java_com_twilio_video_VideoCapturerDelegate_00024NativeObserver_nativeCapturerStarted(JNIEnv *env,
                                                                                     jobject instance,
                                                                                     jlong j_capturer,
                                                                                     jboolean j_success) {
        LOG(LS_INFO) << "NativeObserver_nativeCapturerStarted";
        reinterpret_cast<AndroidVideoCapturerJni*>(j_capturer)->OnCapturerStarted(
                j_success);
    }

    JNIEXPORT void JNICALL
    Java_com_twilio_video_VideoCapturerDelegate_00024NativeObserver_nativeOnByteBufferFrameCaptured(JNIEnv *jni,
                                                                                               jobject instance,
                                                                                               jlong j_capturer,
                                                                                               jbyteArray j_frame,
                                                                                               jint length,
                                                                                               jint width,
                                                                                               jint height,
                                                                                               jint rotation,
                                                                                               jlong timestamp) {
        jboolean is_copy = true;
        jbyte* bytes = jni->GetByteArrayElements(j_frame, &is_copy);
        reinterpret_cast<AndroidVideoCapturerJni*>(j_capturer)
                ->OnMemoryBufferFrame(bytes, length, width, height, rotation, timestamp);
        jni->ReleaseByteArrayElements(j_frame, bytes, JNI_ABORT);
    }

    JNIEXPORT void JNICALL
    Java_com_twilio_video_VideoCapturerDelegate_00024NativeObserver_nativeOnTextureFrameCaptured(JNIEnv *jni,
                                                                                            jobject instance,
                                                                                            jlong j_capturer,
                                                                                            jint width,
                                                                                            jint height,
                                                                                            jint oes_texture_id,
                                                                                            jfloatArray j_transform_matrix,
                                                                                            jint rotation,
                                                                                            jlong timestamp) {

        reinterpret_cast<AndroidVideoCapturerJni*>(j_capturer)
                ->OnTextureFrame(width, height, rotation, timestamp,
                                 webrtc_jni::NativeHandleImpl(jni, oes_texture_id, j_transform_matrix));
    }
}
