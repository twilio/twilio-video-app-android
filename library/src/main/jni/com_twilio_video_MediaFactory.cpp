#include "com_twilio_video_MediaFactory.h"
#include "com_twilio_video_LocalAudioTrack.h"
#include "com_twilio_video_LocalVideoTrack.h"

#include "webrtc/sdk/android/src/jni/jni_helpers.h"
#include "webrtc/voice_engine/include/voe_base.h"
#include "webrtc/modules/audio_device/android/opensles_player.h"
#include "webrtc/sdk/android/src/jni/classreferenceholder.h"
#include "webrtc/sdk/android/src/jni/androidmediadecoder_jni.h"
#include "webrtc/sdk/android/src/jni/androidmediaencoder_jni.h"
#include "com_twilio_video_VideoCapturerDelegate.h"
#include "class_reference_holder.h"
#include "logging.h"

namespace twilio_video_jni {

static bool media_jvm_set = false;

std::shared_ptr<twilio::media::MediaFactory> getMediaFactory(jlong media_factory_handle) {
    MediaFactoryContext *media_factory_context =
            reinterpret_cast<MediaFactoryContext *>(media_factory_handle);

    return media_factory_context->getMediaFactory();
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

    VIDEO_ANDROID_LOG(twilio::video::kTSCoreLogModulePlatform,
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

    VIDEO_ANDROID_LOG(twilio::video::kTSCoreLogModulePlatform,
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

    VIDEO_ANDROID_LOG(twilio::video::kTSCoreLogModulePlatform,
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

    VIDEO_ANDROID_LOG(twilio::video::kTSCoreLogModulePlatform,
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

    VIDEO_ANDROID_LOG(twilio::video::kTSCoreLogModulePlatform,
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
    if ((numerator_aspect_ratio > 0) && (denominator_aspect_ratio > 0)){
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

JNIEXPORT jlong JNICALL Java_com_twilio_video_MediaFactory_nativeCreate(JNIEnv *jni,
                                                                        jobject j_media_factory,
                                                                        jobject context,
                                                                        jobject j_egl_local_context,
                                                                        jobject j_egl_remote_context) {
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::video::kTSCoreLogModulePlatform,
                      twilio::video::kTSCoreLogLevelDebug,
                      "%s", func_name.c_str());
    // Setup media related Android device objects
    if (!media_jvm_set) {
        bool failure = false;

        failure |= webrtc::VoiceEngine::SetAndroidObjects(webrtc_jni::GetJVM(), context);
        failure |= VideoCapturerDelegate::SetAndroidObjects(jni, context);

        if (failure) {
            return 0;
        } else {
            media_jvm_set = true;
        }
    }

    twilio::media::MediaOptions media_options;
    webrtc_jni::MediaCodecVideoEncoderFactory* video_encoder_factory =
            new webrtc_jni::MediaCodecVideoEncoderFactory();
    webrtc_jni::MediaCodecVideoDecoderFactory* video_decoder_factory =
            new webrtc_jni::MediaCodecVideoDecoderFactory();
    // Enable use of textures for encoding
    video_encoder_factory->SetEGLContext(jni, j_egl_local_context);
    // Enable use of textures for decoding
    video_decoder_factory->SetEGLContext(jni, j_egl_remote_context);

    media_options.video_encoder_factory = video_encoder_factory;
    media_options.video_decoder_factory = video_decoder_factory;
    std::shared_ptr<twilio::media::MediaFactory> media_factory =
            twilio::media::MediaFactory::create(media_options);

    return webrtc_jni::jlongFromPointer(new MediaFactoryContext(media_options, media_factory));
}

JNIEXPORT jobject JNICALL Java_com_twilio_video_MediaFactory_nativeCreateAudioTrack(JNIEnv *jni,
                                                                                    jobject j_media_factory,
                                                                                    jlong media_factory_handle,
                                                                                    jboolean enabled,
                                                                                    jobject j_audio_options) {
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::video::kTSCoreLogModulePlatform,
                      twilio::video::kTSCoreLogLevelDebug,
                      "%s", func_name.c_str());
    std::shared_ptr<twilio::media::MediaFactory> media_factory =
            getMediaFactory(media_factory_handle);
    cricket::AudioOptions audio_options = getAudioOptions(j_audio_options);
    std::shared_ptr<twilio::media::LocalAudioTrack> local_audio_track =
            media_factory->createAudioTrack(enabled, audio_options);

    return local_audio_track == nullptr ?
           (nullptr) :
           (createJavaLocalAudioTrack(j_media_factory, local_audio_track));
}

JNIEXPORT jobject JNICALL Java_com_twilio_video_MediaFactory_nativeCreateVideoTrack(JNIEnv *jni,
                                                                                    jobject j_media_factory,
                                                                                    jlong media_factory_handle,
                                                                                    jboolean enabled,
                                                                                    jobject j_video_capturer,
                                                                                    jobject j_video_contraints,
                                                                                    jobject j_egl_context) {
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::video::kTSCoreLogModulePlatform,
                      twilio::video::kTSCoreLogLevelDebug,
                      "%s", func_name.c_str());
    std::shared_ptr<twilio::media::MediaFactory> media_factory =
            getMediaFactory(media_factory_handle);
    jobject j_video_capturer_delegate = createJavaVideoCapturerDelegate(j_video_capturer);
    bool is_screencast = javaIsScreencast(j_video_capturer);
    rtc::scoped_refptr<VideoCapturerDelegate> delegate =
            new rtc::RefCountedObject<VideoCapturerDelegate>(jni,
                                                             j_video_capturer_delegate,
                                                             j_egl_context,
                                                             is_screencast);
    cricket::VideoCapturer* capturer = new AndroidVideoCapturer(delegate);
    std::shared_ptr<twilio::media::LocalVideoTrack> video_track =
            media_factory->createVideoTrack(enabled,
                                            getVideoConstraints(j_video_contraints),
                                            capturer);

    return video_track == nullptr ?
           (nullptr) :
           (createJavaLocalVideoTrack(video_track,
                                      enabled,
                                      j_video_capturer,
                                      j_video_contraints,
                                      j_media_factory));
}

JNIEXPORT void JNICALL Java_com_twilio_video_MediaFactory_nativeRelease(JNIEnv *jni,
                                                                        jobject j_media_factory,
                                                                        jlong media_factory_handle) {
    std::string func_name = std::string(__FUNCTION__);
    VIDEO_ANDROID_LOG(twilio::video::kTSCoreLogModulePlatform,
                      twilio::video::kTSCoreLogLevelDebug,
                      "%s", func_name.c_str());
    if (media_factory_handle != 0) {
        MediaFactoryContext *media_factory_context =
                reinterpret_cast<MediaFactoryContext *>(media_factory_handle);

        delete media_factory_context;
    }
}

}
