#include "com_twilio_video_LocalMedia.h"
#include "com_twilio_video_LocalAudioTrack.h"
#include "com_twilio_video_LocalVideoTrack.h"
#include "com_twilio_video_VideoCapturerDelegate.h"
#include "webrtc/api/android/jni/jni_helpers.h"
#include "class_reference_holder.h"
#include "video/logger.h"
#include "android_video_capturer.h"

namespace twilio_video_jni {

std::shared_ptr<twilio::media::LocalMedia> getLocalMedia(jlong local_media_handle) {
    LocalMediaContext* local_media_context =
            reinterpret_cast<LocalMediaContext *>(local_media_handle);

    return local_media_context->getLocalMedia();
}

cricket::AudioOptions getAudioOptions(jobject j_audio_options) {
    JNIEnv* jni = webrtc_jni::AttachCurrentThreadIfNeeded();
    cricket::AudioOptions audio_options = cricket::AudioOptions();

    if (!webrtc_jni::IsNull(jni, j_audio_options)) {
        jclass audio_options_class = jni->GetObjectClass(j_audio_options);
        jfieldID echo_cancellation_field =
                jni->GetFieldID(audio_options_class, "echoCancellation", "Z");
        jfieldID auto_gain_control_field =
                jni->GetFieldID(audio_options_class, "autoGainControl", "Z");
        jfieldID noise_suppression_field =
                jni->GetFieldID(audio_options_class, "noiseSuppression", "Z");
        jfieldID highpass_filter_field =
                jni->GetFieldID(audio_options_class, "highpassFilter", "Z");
        jfieldID stereo_swapping_field =
                jni->GetFieldID(audio_options_class, "stereoSwapping", "Z");
        jfieldID audio_jitter_buffer_fast_accelerate_field =
                jni->GetFieldID(audio_options_class, "audioJitterBufferFastAccelerate", "Z");
        jfieldID typing_detection_field =
                jni->GetFieldID(audio_options_class, "typingDetection", "Z");

        audio_options.echo_cancellation =
                rtc::Optional<bool>(jni->GetBooleanField(j_audio_options,
                                                         echo_cancellation_field));
        audio_options.auto_gain_control =
                rtc::Optional<bool>(jni->GetBooleanField(j_audio_options,
                                                         auto_gain_control_field));
        audio_options.noise_suppression =
                rtc::Optional<bool>(jni->GetBooleanField(j_audio_options,
                                                         noise_suppression_field));
        audio_options.highpass_filter =
                rtc::Optional<bool>(jni->GetBooleanField(j_audio_options,
                                                         highpass_filter_field));
        audio_options.stereo_swapping =
                rtc::Optional<bool>(jni->GetBooleanField(j_audio_options,
                                                         stereo_swapping_field));
        audio_options.audio_jitter_buffer_fast_accelerate =
                rtc::Optional<bool>(jni->GetBooleanField(j_audio_options,
                                                         audio_jitter_buffer_fast_accelerate_field));
        audio_options.typing_detection =
                rtc::Optional<bool>(jni->GetBooleanField(j_audio_options,
                                                         typing_detection_field));
    }

    return audio_options;
}

bool javaIsScreencast(jobject j_video_capturer) {
    JNIEnv *jni = webrtc_jni::GetEnv();
    jmethodID j_video_capturer_is_screencast_id = webrtc_jni::GetMethodID(jni,
                                                                          jni->GetObjectClass(j_video_capturer),
                                                                          "isScreencast",
                                                                          "()Z");
    return jni->CallBooleanMethod(j_video_capturer, j_video_capturer_is_screencast_id);
}

jobject createJavaVideoCapturerDelegate(jobject j_video_capturer) {
    JNIEnv *jni = webrtc_jni::GetEnv();
    jclass j_video_capturer_delegate_class = twilio_video_jni::FindClass(jni,
                                                                         "com/twilio/video/VideoCapturerDelegate");
    jmethodID j_video_capturer_delegate_ctor_id = webrtc_jni::GetMethodID(jni,
                                                                     j_video_capturer_delegate_class,
                                                                     "<init>",
                                                                     "(Lcom/twilio/video/VideoCapturer;)V");
    return jni->NewObject(j_video_capturer_delegate_class,
                          j_video_capturer_delegate_ctor_id,
                          j_video_capturer);
}

twilio::media::MediaConstraints* convertVideoConstraints(jobject j_video_constraints) {
    twilio::media::MediaConstraints* video_constraints = new twilio::media::MediaConstraints();
    JNIEnv* env = webrtc_jni::GetEnv();

    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "Parsing video constraints");

    jclass video_constraints_class = env->GetObjectClass(j_video_constraints);
    jfieldID min_fps_field =
            env->GetFieldID(video_constraints_class, "minFps", "I");
    jfieldID max_fps_field =
            env->GetFieldID(video_constraints_class, "maxFps", "I");
    int min_fps =
            env->GetIntField(j_video_constraints, min_fps_field);
    int max_fps =
            env->GetIntField(j_video_constraints, max_fps_field);

    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "Video constraints minFps %d maxFps %d",
                       min_fps,
                       max_fps);

    jfieldID min_video_dimensions_field = webrtc_jni::GetFieldID(env,
                                                     video_constraints_class,
                                                     "minVideoDimensions",
                                                     "Lcom/twilio/video/VideoDimensions;");
    jfieldID max_video_dimensions_field = webrtc_jni::GetFieldID(env,
                                                     video_constraints_class,
                                                     "maxVideoDimensions",
                                                     "Lcom/twilio/video/VideoDimensions;");

    jobject j_min_video_dimensions = env->GetObjectField(j_video_constraints, min_video_dimensions_field);
    jclass min_video_dimensions_class = env->GetObjectClass(j_min_video_dimensions);
    jfieldID min_width_field =
            env->GetFieldID(min_video_dimensions_class, "width", "I");
    jfieldID min_height_field =
            env->GetFieldID(min_video_dimensions_class, "height", "I");
    int min_width =
            env->GetIntField(j_min_video_dimensions, min_width_field);
    int min_height =
            env->GetIntField(j_min_video_dimensions, min_height_field);

    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "Video constraints min width %d min height %d",
                       min_width,
                       min_height);

    jfieldID aspect_ratio_field =
            env->GetFieldID(
                    video_constraints_class,
                    "aspectRatio",
                    "Lcom/twilio/video/AspectRatio;");
    jobject j_aspect_ratio = env->GetObjectField(j_video_constraints, aspect_ratio_field);
    jclass aspect_ratio_class = env->GetObjectClass(j_aspect_ratio);
    jfieldID numerator_field =
            env->GetFieldID(aspect_ratio_class, "numerator", "I");
    jfieldID denominator_field =
            env->GetFieldID(aspect_ratio_class, "denominator", "I");
    int numerator_aspect_ratio =
            env->GetIntField(j_aspect_ratio, numerator_field);
    int denominator_aspect_ratio =
            env->GetIntField(j_aspect_ratio, denominator_field);

    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "Video aspect ratio %d:%d",
                       numerator_aspect_ratio,
                       denominator_aspect_ratio);

    jobject j_max_video_dimensions = env->GetObjectField(j_video_constraints,
                                                         max_video_dimensions_field);
    jclass max_video_dimensions_class = env->GetObjectClass(j_max_video_dimensions);
    jfieldID max_width_field =
            env->GetFieldID(max_video_dimensions_class, "width", "I");
    jfieldID max_height_field =
            env->GetFieldID(max_video_dimensions_class, "height", "I");
    int max_width =
            env->GetIntField(j_max_video_dimensions, max_width_field);
    int max_height =
            env->GetIntField(j_max_video_dimensions, max_height_field);

    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "Video constraints max width %d max height %d",
                       max_width,
                       max_height);


    if (max_fps > 0) {
        video_constraints->SetMandatory(twilio::media::MediaConstraints::kMaxFrameRate, max_fps);
    }
    if (min_fps > 0) {
        video_constraints->SetMandatory(twilio::media::MediaConstraints::kMinFrameRate, min_fps);
    }

    if (max_width > 0 && max_height > 0) {
        video_constraints->SetMandatory(twilio::media::MediaConstraints::kMaxWidth, max_width);
        video_constraints->SetMandatory(twilio::media::MediaConstraints::kMaxHeight, max_height);
    }
    if (min_width > 0 && min_height > 0) {
        video_constraints->SetMandatory(twilio::media::MediaConstraints::kMinWidth, min_width);
        video_constraints->SetMandatory(twilio::media::MediaConstraints::kMinHeight, min_height);
    }
    if ((numerator_aspect_ratio > 0) &&
        (denominator_aspect_ratio > 0)){
        double aspect_ratio = (double) numerator_aspect_ratio / denominator_aspect_ratio;

        video_constraints->SetMandatory(twilio::media::MediaConstraints::kMinAspectRatio,
                                        aspect_ratio);
        video_constraints->SetMandatory(twilio::media::MediaConstraints::kMaxAspectRatio,
                                        aspect_ratio);
    }

    return video_constraints;
}

twilio::media::MediaConstraints* getVideoConstraints(jobject j_video_contraints) {
    twilio::media::MediaConstraints* video_constraints = nullptr;

    if (!webrtc_jni::IsNull(webrtc_jni::GetEnv(), j_video_contraints)) {
        video_constraints = convertVideoConstraints(j_video_contraints);
    }

    return video_constraints;
}

JNIEXPORT jobject JNICALL Java_com_twilio_video_LocalMedia_nativeAddAudioTrack(JNIEnv *jni,
                                                                               jobject j_local_media,
                                                                               jlong local_media_handle,
                                                                               jboolean enabled,
                                                                               jobject j_audio_options) {
    std::shared_ptr<twilio::media::LocalMedia> local_media = getLocalMedia(local_media_handle);
    cricket::AudioOptions audio_options = getAudioOptions(j_audio_options);
    std::shared_ptr<twilio::media::LocalAudioTrack> local_audio_track =
            local_media->addAudioTrack(enabled, audio_options);

    return (local_audio_track == nullptr) ?
           (nullptr) :
           (createJavaLocalAudioTrack(local_audio_track));
}

JNIEXPORT jboolean JNICALL Java_com_twilio_video_LocalMedia_nativeRemoveAudioTrack(JNIEnv *jni,
                                                                                   jobject j_local_media,
                                                                                   jlong local_media_handle,
                                                                                   jstring track_id) {
    std::shared_ptr<twilio::media::LocalMedia> local_media = getLocalMedia(local_media_handle);

    return local_media->removeAudioTrack(webrtc_jni::JavaToStdString(jni, track_id));
}

JNIEXPORT jobject JNICALL Java_com_twilio_video_LocalMedia_nativeAddVideoTrack(JNIEnv *jni,
                                                                               jobject j_local_media,
                                                                               jlong local_media_handle,
                                                                               jboolean enabled,
                                                                               jobject j_video_capturer,
                                                                               jobject j_video_contraints,
                                                                               jobject j_egl_context) {
    std::shared_ptr<twilio::media::LocalMedia> local_media = getLocalMedia(local_media_handle);
    jobject j_video_capturer_delegate = createJavaVideoCapturerDelegate(j_video_capturer);
    bool is_screencast = javaIsScreencast(j_video_capturer);
    rtc::scoped_refptr<VideoCapturerDelegate> delegate =
            new rtc::RefCountedObject<VideoCapturerDelegate>(jni,
                                                             j_video_capturer_delegate,
                                                             j_egl_context,
                                                             is_screencast);
    cricket::VideoCapturer* capturer = new AndroidVideoCapturer(delegate);
    std::shared_ptr<twilio::media::LocalVideoTrack> video_track = local_media->addVideoTrack(enabled,
                                                                                             getVideoConstraints(j_video_contraints),
                                                                                             capturer);

    return (video_track == nullptr) ?
           (nullptr) :
           (createJavaLocalVideoTrack(video_track, j_video_capturer, j_video_contraints));
}

JNIEXPORT jboolean JNICALL Java_com_twilio_video_LocalMedia_nativeRemoveVideoTrack(JNIEnv *jni,
                                                                                   jobject j_local_media,
                                                                                   jlong local_media_handle,
                                                                                   jstring track_id) {
    std::shared_ptr<twilio::media::LocalMedia> local_media = getLocalMedia(local_media_handle);

    return local_media->removeVideoTrack(webrtc_jni::JavaToStdString(jni, track_id));
}

JNIEXPORT void JNICALL Java_com_twilio_video_LocalMedia_nativeRelease(JNIEnv *jni,
                                                                      jobject j_local_media,
                                                                      jlong local_media_handle) {
    LocalMediaContext* local_media_context =
            reinterpret_cast<LocalMediaContext *>(local_media_handle);

    delete local_media_context;
}

}