/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "com_twilio_video_VideoCapturerDelegate.h"
#include "com_twilio_video_VideoPixelFormat.h"
#include "class_reference_holder.h"
#include "webrtc/base/logging.h"
#include "libyuv/convert.h"
#include "webrtc/modules/utility/include/helpers_android.h"
#include "jni_utils.h"
#include "logging.h"

namespace twilio_video_jni {

jobject VideoCapturerDelegate::application_context_ = nullptr;

// static
int VideoCapturerDelegate::SetAndroidObjects(JNIEnv* jni,
                                             jobject appliction_context) {
    if (application_context_) {
        jni->DeleteGlobalRef(application_context_);
    }
    application_context_ = webrtc::NewGlobalRef(jni, appliction_context);

    return 0;
}

webrtc::VideoRotation jintToVideoRotation(jint rotation) {
    RTC_DCHECK(rotation == 0 || rotation == 90 || rotation == 180 ||
               rotation == 270);
    return static_cast<webrtc::VideoRotation>(rotation);
}

VideoCapturerDelegate::VideoCapturerDelegate(JNIEnv* jni,
                                             jobject j_video_capturer,
                                             jobject j_egl_context,
                                             jboolean is_screencast)
        : j_video_capturer_(jni, webrtc::JavaParamRef<jobject>(j_video_capturer)),
          j_video_capturer_class_(jni, webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(jni, "com/twilio/video/VideoCapturerDelegate"))),
          j_observer_class_(
                  jni,
                  webrtc::JavaParamRef<jclass>(twilio_video_jni::FindClass(jni,
                                                                           "com/twilio/video/VideoCapturerDelegate$NativeObserver"))),
          is_screencast_(is_screencast),
          surface_texture_helper_(webrtc::jni::SurfaceTextureHelper::create(
                  jni, "Camera SurfaceTextureHelper", webrtc::JavaParamRef<jobject>(j_egl_context))),
          capturer_(nullptr) {
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kInfo,
                      "VideoCapturerDelegate ctor");
    jobject j_frame_observer =
            jni->NewObject(j_observer_class_.obj(),
                           webrtc::GetMethodID(jni, j_observer_class_.obj(), "<init>", "(J)V"),
                           webrtc::NativeToJavaPointer(this));
    CHECK_EXCEPTION(jni) << "error during NewObject";
    jni->CallVoidMethod(
            j_video_capturer_.obj(),
            webrtc::GetMethodID(jni, j_video_capturer_class_.obj(), "initialize",
                                "(Lorg/webrtc/SurfaceTextureHelper;Landroid/content/"
                                        "Context;Lorg/webrtc/VideoCapturer$CapturerObserver;)V"),
            surface_texture_helper_
            ? surface_texture_helper_->GetJavaSurfaceTextureHelper().obj()
            : nullptr,
            application_context_, j_frame_observer);
    CHECK_EXCEPTION(jni) << "error during VideoCapturer.initialize()";
    thread_checker_.DetachFromThread();
}

VideoCapturerDelegate::~VideoCapturerDelegate() {
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kInfo,
                      "~VideoCapturerDelegate");
    jni()->CallVoidMethod(
            j_video_capturer_.obj(),
            webrtc::GetMethodID(jni(), j_video_capturer_class_.obj(), "dispose", "()V"));
    CHECK_EXCEPTION(jni()) << "error during VideoCapturer.dispose()";
}

void VideoCapturerDelegate::Start(const cricket::VideoFormat& capture_format,
                                  AndroidVideoCapturer* capturer) {
    jclass j_video_capturer_delegate_class =
            twilio_video_jni::FindClass(jni(), "com/twilio/video/VideoCapturerDelegate");
    capture_pixel_format_ = capture_format.fourcc;
    jobject j_video_pixel_format =
            VideoPixelFormat::getJavaVideoPixelFormat(cricket::CanonicalFourCC(capture_pixel_format_));
    jni()->CallVoidMethod(j_video_capturer_.obj(),
                          webrtc::GetMethodID(jni(),
                                              j_video_capturer_delegate_class,
                                              "setVideoPixelFormat",
                                              "(Lcom/twilio/video/VideoPixelFormat;)V"),
                          j_video_pixel_format);

    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kInfo,
                      "VideoCapturerDelegate start");
    RTC_DCHECK(thread_checker_.CalledOnValidThread());
    {
        rtc::CritScope cs(&capturer_lock_);
        RTC_CHECK(capturer_ == nullptr);
        RTC_CHECK(invoker_.get() == nullptr);
        capturer_ = capturer;
        invoker_.reset(new rtc::GuardedAsyncInvoker());
    }
    jmethodID m =
            webrtc::GetMethodID(jni(), j_video_capturer_class_.obj(), "startCapture", "(III)V");
    jni()->CallVoidMethod(j_video_capturer_.obj(),
                          m,
                          capture_format.width,
                          capture_format.height,
                          capture_format.framerate());
    CHECK_EXCEPTION(jni()) << "error during VideoCapturer.startCapture";
}

void VideoCapturerDelegate::Stop() {
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kInfo,
                      "VideoCapturerDelegate stop");
    RTC_DCHECK(thread_checker_.CalledOnValidThread());
    {
        /*
         * TODO: Consider moving this block until *after* the call to stopCapturer.
         * stopCapturer should ensure that we get no more frames, and then we shouldn't need
         * the if (!capturer_) checks in OnMemoryBufferFrame and OnTextureFrame.
         */
        rtc::CritScope cs(&capturer_lock_);
        // Destroying |invoker_| will cancel all pending calls to |capturer_|.
        invoker_ = nullptr;
        capturer_ = nullptr;
    }
    jmethodID m = webrtc::GetMethodID(jni(), j_video_capturer_class_.obj(),
                                      "stopCapture", "()V");
    jni()->CallVoidMethod(j_video_capturer_.obj(), m);
    CHECK_EXCEPTION(jni()) << "error during VideoCapturer.stopCapture";
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kInfo,
                      "VideoCapturerDelegate stop done");
}

template <typename... Args>
void VideoCapturerDelegate::AsyncCapturerInvoke(
        const rtc::Location& posted_from,
        void (AndroidVideoCapturer::*method)(Args...),
        typename Identity<Args>::type... args) {
    rtc::CritScope cs(&capturer_lock_);
    if (!invoker_) {
        VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                          twilio::video::LogLevel::kWarning,
                          "%s() called from closed capturer", posted_from.function_name());
        return;
    }
    invoker_->AsyncInvoke<void>(posted_from,
                                rtc::Bind(method, capturer_, args...));
}

bool VideoCapturerDelegate::IsScreencast() {
    return is_screencast_;
}

std::vector<cricket::VideoFormat> VideoCapturerDelegate::GetSupportedFormats() {
    JNIEnv* jni = webrtc::jni::AttachCurrentThreadIfNeeded();
    jobject j_list_of_formats = jni->CallObjectMethod(
            j_video_capturer_.obj(),
            webrtc::GetMethodID(jni, j_video_capturer_class_.obj(), "getSupportedFormats",
                                "()Ljava/util/List;"));
    CHECK_EXCEPTION(jni) << "error during getSupportedFormats";

    // Extract Java List<CaptureFormat> to std::vector<cricket::VideoFormat>.
    jclass j_list_class = jni->FindClass("java/util/List");
    jclass j_video_format_class = twilio_video_jni::FindClass(jni, "com/twilio/video/VideoFormat");
    jclass j_video_dimensions_class =
            twilio_video_jni::FindClass(jni, "com/twilio/video/VideoDimensions");
    jclass j_video_pixel_format_class =
            twilio_video_jni::FindClass(jni,"com/twilio/video/VideoPixelFormat");

    const int size = jni->CallIntMethod(
            j_list_of_formats, webrtc::GetMethodID(jni, j_list_class, "size", "()I"));
    jmethodID j_get =
            webrtc::GetMethodID(jni, j_list_class, "get", "(I)Ljava/lang/Object;");
    jmethodID j_get_video_pixel_format_value =
            webrtc::GetMethodID(jni, j_video_pixel_format_class, "getValue", "()I");

    // VideoFormat fields
    jfieldID j_dimensions_field = GetFieldID(jni,
                                             j_video_format_class,
                                             "dimensions",
                                             "Lcom/twilio/video/VideoDimensions;");
    jfieldID j_framerate_field = GetFieldID(jni, j_video_format_class, "framerate", "I");
    jfieldID j_pixel_format_field =
            GetFieldID(jni, j_video_format_class, "pixelFormat",
                       "Lcom/twilio/video/VideoPixelFormat;");

    // VideoDimensions fields
    jfieldID j_width_field = GetFieldID(jni, j_video_dimensions_class, "width", "I");
    jfieldID j_height_field = GetFieldID(jni, j_video_dimensions_class, "height", "I");

    std::vector<cricket::VideoFormat> formats;
    formats.reserve(size);
    for (int i = 0; i < size; ++i) {
        jobject j_format = jni->CallObjectMethod(j_list_of_formats, j_get, i);
        jobject j_dimensions = GetObjectField(jni, j_format, j_dimensions_field);
        jobject j_pixel_format = GetObjectField(jni, j_format, j_pixel_format_field);
        jint j_framerate = GetIntField(jni, j_format, j_framerate_field);
        jint pixel_format = jni->CallIntMethod(j_pixel_format, j_get_video_pixel_format_value);
        int64_t frame_interval = cricket::VideoFormat::FpsToInterval(j_framerate);

        formats.emplace_back(GetIntField(jni, j_dimensions, j_width_field),
                             GetIntField(jni, j_dimensions, j_height_field),
                             frame_interval,
                             pixel_format);
    }
    CHECK_EXCEPTION(jni) << "error while extracting formats";
    return formats;

}

void VideoCapturerDelegate::OnCapturerStarted(bool success) {
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kInfo,
                      "VideoCapturerDelegate capture started: %s", (success ? "true" : "false"));
    AsyncCapturerInvoke(RTC_FROM_HERE, &AndroidVideoCapturer::OnCapturerStarted, success);
}

void VideoCapturerDelegate::OnMemoryBufferFrame(void *video_frame, int length, int width,
                                                int height, int rotation,
                                                int64_t timestamp_ns) {
    RTC_DCHECK(rotation == 0 || rotation == 90 || rotation == 180 ||
               rotation == 270);
    rtc::CritScope cs(&capturer_lock_);
    if (!capturer_) {
        VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                          twilio::video::LogLevel::kWarning,
                          "OnMemoryBufferFrame() called for closed capturer.");
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
        int tmp = adapted_height;
        adapted_height = adapted_width;
        adapted_width = tmp;

        tmp = rotated_height;
        rotated_height = rotated_width;
        rotated_width = tmp;
    }

    rtc::scoped_refptr<webrtc::VideoFrameBuffer> buffer =
            pre_scale_pool_.CreateBuffer(rotated_width, rotated_height);

    switch (capture_pixel_format_) {
        /*
         * TODO: Add support for padding
         */
        case cricket::FOURCC_ABGR: {
            const uint8_t *src_rgba = static_cast<uint8_t *>(video_frame);
            int rgba_stride = 4 * width;

            // Pre rotate buffer allocated only if rotation required to minimize memory footprint
            rtc::scoped_refptr<webrtc::VideoFrameBuffer> pre_rotate_buffer = buffer;
            if (capturer_->apply_rotation()) {
                pre_rotate_buffer = pre_rotate_pool_.CreateBuffer(crop_width, crop_height);
            }

            // Convert to I420
            libyuv::ABGRToI420(
                    src_rgba, rgba_stride,
                    (uint8 *) pre_rotate_buffer->GetI420()->DataY(),
                    pre_rotate_buffer->GetI420()->StrideY(),
                    (uint8 *) pre_rotate_buffer->GetI420()->DataU(),
                    pre_rotate_buffer->GetI420()->StrideU(),
                    (uint8 *) pre_rotate_buffer->GetI420()->DataV(),
                    pre_rotate_buffer->GetI420()->StrideV(),
                    crop_width, crop_height);

            /*
             * Rotation applied into original buffer if required. If rotation not required then the
             * pre rotation buffer is set to final buffer.
             */
            if (capturer_->apply_rotation()) {
                libyuv::I420Rotate((uint8 *) pre_rotate_buffer->GetI420()->DataY(),
                                   pre_rotate_buffer->GetI420()->StrideY(),
                                   (uint8 *) pre_rotate_buffer->GetI420()->DataU(),
                                   pre_rotate_buffer->GetI420()->StrideU(),
                                   (uint8 *) pre_rotate_buffer->GetI420()->DataV(),
                                   pre_rotate_buffer->GetI420()->StrideV(),
                                   (uint8 *) buffer->GetI420()->DataY(),
                                   buffer->GetI420()->StrideY(),
                                   (uint8 *) buffer->GetI420()->DataU(),
                                   buffer->GetI420()->StrideU(),
                                   (uint8 *) buffer->GetI420()->DataV(),
                                   buffer->GetI420()->StrideV(),
                                   crop_width, crop_height,
                                   static_cast<libyuv::RotationMode>(rotation));
            } else {
                buffer = pre_rotate_buffer;
            }
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
                    (uint8 *) buffer->GetI420()->DataY(), buffer->GetI420()->StrideY(),
                    // Swap U and V, since we have NV21, not NV12.
                    (uint8 *) buffer->GetI420()->DataV(), buffer->GetI420()->StrideV(),
                    (uint8 *) buffer->GetI420()->DataU(), buffer->GetI420()->StrideU(),
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
        scaled_buffer->ScaleFrom(*buffer->GetI420());
        buffer = scaled_buffer;
    }

    capturer_->OnFrame(webrtc::VideoFrame(buffer,
                                          capturer_->apply_rotation() ?
                                          webrtc::kVideoRotation_0 :
                                          static_cast<webrtc::VideoRotation>(rotation),
                                          translated_camera_time_us), width, height);
}

void VideoCapturerDelegate::OnTextureFrame(int width,
                                           int height,
                                           int rotation,
                                           int64_t timestamp_ns,
                                           const webrtc::jni::NativeHandleImpl& handle) {
    RTC_DCHECK(rotation == 0 || rotation == 90 || rotation == 180 ||
               rotation == 270);
    rtc::CritScope cs(&capturer_lock_);
    if (!capturer_) {
        VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                          twilio::video::LogLevel::kWarning,
                          "OnTextureFrame() called for closed capturer.");
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

    webrtc::jni::Matrix matrix = handle.sampling_matrix;

    matrix.Crop(crop_width / static_cast<float>(width),
                crop_height / static_cast<float>(height),
                crop_x / static_cast<float>(width),
                crop_y / static_cast<float>(height));

    if (capturer_->apply_rotation()) {
        if (rotation == webrtc::kVideoRotation_90 ||
            rotation == webrtc::kVideoRotation_270) {
            int tmp = adapted_height;
            adapted_height = adapted_width;
            adapted_width = tmp;
        }
        matrix.Rotate(static_cast<webrtc::VideoRotation>(rotation));
    }

    capturer_->OnFrame(webrtc::VideoFrame(
            surface_texture_helper_->CreateTextureFrame(
                    adapted_width, adapted_height,
                    webrtc::jni::NativeHandleImpl(handle.oes_texture_id, matrix)),
            capturer_->apply_rotation()
            ? webrtc::kVideoRotation_0
            : static_cast<webrtc::VideoRotation>(rotation),
            translated_camera_time_us),
                       width, height);
}

void VideoCapturerDelegate::OnFrameCaptured(
        JNIEnv* jni,
        int width,
        int height,
        int64_t timestamp_ns,
        webrtc::VideoRotation rotation,
        const webrtc::JavaRef<jobject>& j_video_frame_buffer) {
    rtc::CritScope cs(&capturer_lock_);
    if (!capturer_) {
        VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                          twilio::video::LogLevel::kWarning,
                          "OnFrameCaptured() called for closed capturer.");
        return;
    }
    int64_t camera_time_us = timestamp_ns / rtc::kNumNanosecsPerMicrosec;
    int64_t translated_camera_time_us =
            timestamp_aligner_.TranslateTimestamp(camera_time_us, rtc::TimeMicros());

    int adapted_width;
    int adapted_height;
    int crop_width;
    int crop_height;
    int crop_x;
    int crop_y;

    if (!capturer_->AdaptFrame(width, height, camera_time_us, rtc::TimeMicros(), &adapted_width,
                               &adapted_height, &crop_width, &crop_height, &crop_x,
                               &crop_y, &translated_camera_time_us)) {
        return;
    }

    rtc::scoped_refptr<webrtc::VideoFrameBuffer> buffer =
            webrtc::jni::AndroidVideoBuffer::Create(jni, j_video_frame_buffer)
                    ->CropAndScale(jni, crop_x, crop_y, crop_width, crop_height,
                                   adapted_width, adapted_height);

    // AdaptedVideoTrackSource handles applying rotation for I420 frames.
    if (capturer_->apply_rotation() && rotation != webrtc::kVideoRotation_0) {
        buffer = buffer->ToI420();
    }

    capturer_->OnFrame(webrtc::VideoFrame(buffer,
                                          static_cast<webrtc::VideoRotation>(rotation),
                                          translated_camera_time_us), width, height);
}

void VideoCapturerDelegate::OnOutputFormatRequest(int width,
                                                  int height,
                                                  int fps) {
    AsyncCapturerInvoke(RTC_FROM_HERE,
                        &AndroidVideoCapturer::OnOutputFormatRequest,
                        width, height, fps);
}

JNIEnv* VideoCapturerDelegate::jni() { return webrtc::jni::AttachCurrentThreadIfNeeded(); }

JNIEXPORT void JNICALL
Java_com_twilio_video_VideoCapturerDelegate_00024NativeObserver_nativeCapturerStarted(JNIEnv *env,
                                                                                      jobject instance,
                                                                                      jlong j_capturer,
                                                                                      jboolean j_success) {
    VIDEO_ANDROID_LOG(twilio::video::LogModule::kPlatform,
                      twilio::video::LogLevel::kInfo,
                      "NativeObserver_nativeCapturerStarted");
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
                             webrtc::jni::NativeHandleImpl(jni, oes_texture_id, webrtc::JavaParamRef<jfloatArray>(j_transform_matrix)));
}

JNIEXPORT void JNICALL
Java_com_twilio_video_VideoCapturerDelegate_00024NativeObserver_nativeOnFrameCaptured(JNIEnv *env,
                                                                                      jobject instance,
                                                                                      jlong native_capturer,
                                                                                      jint width,
                                                                                      jint height,
                                                                                      jlong timestamp_ns,
                                                                                      jint rotation,
                                                                                      jobject j_video_frame_buffer) {
    reinterpret_cast<VideoCapturerDelegate*>(native_capturer)
            ->OnFrameCaptured(env,
                              width,
                              height,
                              timestamp_ns,
                              jintToVideoRotation(rotation),
                              webrtc::JavaParamRef<jobject>(j_video_frame_buffer));

}

}
