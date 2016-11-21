#include "com_twilio_video_VideoCapturerDelegate.h"
#include "com_twilio_video_VideoPixelFormat.h"
#include "class_reference_holder.h"
#include "libyuv/convert.h"

namespace twilio_video_jni {

jobject VideoCapturerDelegate::application_context_ = nullptr;

// static
int VideoCapturerDelegate::SetAndroidObjects(JNIEnv* jni,
                                             jobject appliction_context) {
    if (application_context_) {
        jni->DeleteGlobalRef(application_context_);
    }
    application_context_ = webrtc_jni::NewGlobalRef(jni, appliction_context);

    return 0;
}

VideoCapturerDelegate::VideoCapturerDelegate(JNIEnv* jni,
                                             jobject j_video_capturer,
                                             jobject j_egl_context,
                                             jboolean is_screencast)
        : j_video_capturer_(jni, j_video_capturer),
          j_video_capturer_class_(jni, twilio_video_jni::FindClass(jni, "com/twilio/video/VideoCapturerDelegate")),
          j_observer_class_(
                  jni,
                  twilio_video_jni::FindClass(jni,
                                              "com/twilio/video/VideoCapturerDelegate$NativeObserver")),
          surface_texture_helper_(webrtc_jni::SurfaceTextureHelper::create(
                  jni, "Camera SurfaceTextureHelper", j_egl_context)),
          capturer_(nullptr),
          is_screencast_(is_screencast) {
    LOG(LS_INFO) << "AndroidVideoCapturerJni ctor";
    jobject j_frame_observer =
            jni->NewObject(*j_observer_class_,
                           webrtc_jni::GetMethodID(jni, *j_observer_class_, "<init>", "(J)V"),
                           webrtc_jni::jlongFromPointer(this));
    CHECK_EXCEPTION(jni) << "error during NewObject";
    jni->CallVoidMethod(
            *j_video_capturer_,
            webrtc_jni::GetMethodID(jni, *j_video_capturer_class_, "initialize",
                                    "(Lorg/webrtc/SurfaceTextureHelper;Landroid/content/"
                                            "Context;Lorg/webrtc/VideoCapturer$CapturerObserver;)V"),
            surface_texture_helper_
            ? surface_texture_helper_->GetJavaSurfaceTextureHelper()
            : nullptr,
            application_context_, j_frame_observer);
    CHECK_EXCEPTION(jni) << "error during VideoCapturer.initialize()";
    thread_checker_.DetachFromThread();
}

VideoCapturerDelegate::~VideoCapturerDelegate() {
    LOG(LS_INFO) << "VideoCapturerDelegate dtor";
    jni()->CallVoidMethod(
            *j_video_capturer_,
            webrtc_jni::GetMethodID(jni(), *j_video_capturer_class_, "dispose", "()V"));
    CHECK_EXCEPTION(jni()) << "error during VideoCapturer.dispose()";
}

void VideoCapturerDelegate::Start(const cricket::VideoFormat& capture_format,
                                  AndroidVideoCapturer* capturer) {
    jclass j_video_capturer_delegate_class =
            twilio_video_jni::FindClass(jni(), "com/twilio/video/VideoCapturerDelegate");
    capture_pixel_format_ = capture_format.fourcc;
    jobject j_video_pixel_format =
            VideoPixelFormat::getJavaVideoPixelFormat(capture_pixel_format_) ;
    jni()->CallVoidMethod(*j_video_capturer_,
                        webrtc_jni::GetMethodID(jni(),
                                                j_video_capturer_delegate_class,
                                                "setVideoPixelFormat",
                                                "(Lcom/twilio/video/VideoPixelFormat;)V"),
                        j_video_pixel_format);

    LOG(LS_INFO) << "AndroidVideoCapturerJni start";
    RTC_DCHECK(thread_checker_.CalledOnValidThread());
    {
        rtc::CritScope cs(&capturer_lock_);
        RTC_CHECK(capturer_ == nullptr);
        RTC_CHECK(invoker_.get() == nullptr);
        capturer_ = capturer;
        invoker_.reset(new rtc::GuardedAsyncInvoker());
    }
    jmethodID m =
            webrtc_jni::GetMethodID(jni(), *j_video_capturer_class_, "startCapture", "(III)V");
    jni()->CallVoidMethod(*j_video_capturer_,
                          m,
                          capture_format.width,
                          capture_format.height,
                          capture_format.framerate());
    CHECK_EXCEPTION(jni()) << "error during VideoCapturer.startCapture";
}

void VideoCapturerDelegate::Stop() {
    LOG(LS_INFO) << "VideoCapturerDelegate stop";
    RTC_DCHECK(thread_checker_.CalledOnValidThread());
    {
        // TODO(nisse): Consider moving this block until *after* the call to
        // stopCapturer. stopCapturer should ensure that we get no
        // more frames, and then we shouldn't need the if (!capturer_)
        // checks in OnMemoryBufferFrame and OnTextureFrame.
        rtc::CritScope cs(&capturer_lock_);
        // Destroying |invoker_| will cancel all pending calls to |capturer_|.
        invoker_ = nullptr;
        capturer_ = nullptr;
    }
    jmethodID m = webrtc_jni::GetMethodID(jni(), *j_video_capturer_class_,
                                          "stopCapture", "()V");
    jni()->CallVoidMethod(*j_video_capturer_, m);
    CHECK_EXCEPTION(jni()) << "error during VideoCapturer.stopCapture";
    LOG(LS_INFO) << "VideoCapturerDelegate stop done";
}

template <typename... Args>
void VideoCapturerDelegate::AsyncCapturerInvoke(
        const rtc::Location& posted_from,
        void (AndroidVideoCapturer::*method)(Args...),
        typename Identity<Args>::type... args) {
    rtc::CritScope cs(&capturer_lock_);
    if (!invoker_) {
        LOG(LS_WARNING) << posted_from.function_name()
                        << "() called for closed capturer.";
        return;
    }
    invoker_->AsyncInvoke<void>(posted_from,
                                rtc::Bind(method, capturer_, args...));
}

bool VideoCapturerDelegate::IsScreencast() {
    return is_screencast_;
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

void VideoCapturerDelegate::OnCapturerStarted(bool success) {
    LOG(LS_INFO) << "AndroidVideoCapturerJni capture started: " << success;
    AsyncCapturerInvoke(RTC_FROM_HERE, &AndroidVideoCapturer::OnCapturerStarted, success);
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

void VideoCapturerDelegate::OnTextureFrame(int width,
                                             int height,
                                             int rotation,
                                             int64_t timestamp_ns,
                                             const webrtc_jni::NativeHandleImpl& handle) {
    RTC_DCHECK(rotation == 0 || rotation == 90 || rotation == 180 ||
               rotation == 270);
    rtc::CritScope cs(&capturer_lock_);
    if (!capturer_) {
        LOG(LS_WARNING) << "OnTextureFrame() called for closed capturer.";
        surface_texture_helper_->ReturnTextureFrame();
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
        surface_texture_helper_->ReturnTextureFrame();
        return;
    }

    webrtc_jni::Matrix matrix = handle.sampling_matrix;

    matrix.Crop(crop_width / static_cast<float>(width),
                crop_height / static_cast<float>(height),
                crop_x / static_cast<float>(width),
                crop_y / static_cast<float>(height));

    if (capturer_->apply_rotation()) {
        if (rotation == webrtc::kVideoRotation_90 ||
            rotation == webrtc::kVideoRotation_270) {
            std::swap(adapted_width, adapted_height);
        }
        matrix.Rotate(static_cast<webrtc::VideoRotation>(rotation));
    }

    capturer_->OnFrame(cricket::WebRtcVideoFrame(
            surface_texture_helper_->CreateTextureFrame(
                    adapted_width, adapted_height,
                    webrtc_jni::NativeHandleImpl(handle.oes_texture_id, matrix)),
            capturer_->apply_rotation()
            ? webrtc::kVideoRotation_0
            : static_cast<webrtc::VideoRotation>(rotation),
            translated_camera_time_us, 0),
                       width, height);
}

void VideoCapturerDelegate::OnOutputFormatRequest(int width,
                                                    int height,
                                                    int fps) {
    AsyncCapturerInvoke(RTC_FROM_HERE,
                        &AndroidVideoCapturer::OnOutputFormatRequest,
                        width, height, fps);
}

JNIEnv* VideoCapturerDelegate::jni() { return webrtc_jni::AttachCurrentThreadIfNeeded(); }

JNIEXPORT void JNICALL
Java_com_twilio_video_VideoCapturerDelegate_00024NativeObserver_nativeCapturerStarted(JNIEnv *env,
                                                                                 jobject instance,
                                                                                 jlong j_capturer,
                                                                                 jboolean j_success) {
    LOG(LS_INFO) << "NativeObserver_nativeCapturerStarted";
    reinterpret_cast<VideoCapturerDelegate*>(j_capturer)->OnCapturerStarted(
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
    reinterpret_cast<VideoCapturerDelegate*>(j_capturer)
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

    reinterpret_cast<VideoCapturerDelegate*>(j_capturer)
            ->OnTextureFrame(width, height, rotation, timestamp,
                             webrtc_jni::NativeHandleImpl(jni, oes_texture_id, j_transform_matrix));
}

}
