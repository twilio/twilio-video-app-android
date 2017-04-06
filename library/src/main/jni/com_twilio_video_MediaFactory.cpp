#include "com_twilio_video_MediaFactory.h"
#include "com_twilio_video_LocalMedia.h"

#include "webrtc/sdk/android/src/jni/jni_helpers.h"
#include "webrtc/voice_engine/include/voe_base.h"
#include "webrtc/modules/audio_device/android/opensles_player.h"
#include "webrtc/sdk/android/src/jni/classreferenceholder.h"
#include "webrtc/sdk/android/src/jni/androidmediadecoder_jni.h"
#include "webrtc/sdk/android/src/jni/androidmediaencoder_jni.h"
#include "com_twilio_video_VideoCapturerDelegate.h"

namespace twilio_video_jni {

static bool media_jvm_set = false;

std::shared_ptr<twilio::media::MediaFactory> getMediaFactory(jlong media_factory_handle) {
    MediaFactoryContext *media_factory_context =
            reinterpret_cast<MediaFactoryContext *>(media_factory_handle);

    return media_factory_context->getMediaFactory();
}

JNIEXPORT jlong JNICALL Java_com_twilio_video_MediaFactory_nativeCreate(JNIEnv *jni,
                                                                        jobject j_media_factory,
                                                                        jobject context,
                                                                        jobject j_egl_local_context,
                                                                        jobject j_egl_remote_context) {
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

    /*
     * TODO: Enable use of textures for encoding and decoding
     *
     * WebRTC represents an I420Frame decoded from a texture with a texture ID
     * and does not fill out the stride and plane data when surfaced to Java. This is more
     * efficient but causes problems for implementing a custom VideoRenderer because the pixel
     * data is unavailable in yuvPlanes. This problem will need to be revisited because
     * encoding/decoding from a texture provides performance gains. However, we do not want to
     * prohibit developers implementing custom renderers. For more information see GSDK-1108.
     */
    // video_encoder_factory->SetEGLContext(jni, j_egl_local_context);
    // video_decoder_factory->SetEGLContext(jni, j_egl_remote_context);

    media_options.video_encoder_factory = video_encoder_factory;
    media_options.video_decoder_factory = video_decoder_factory;
    std::shared_ptr<twilio::media::MediaFactory> media_factory =
            twilio::media::MediaFactory::create(media_options);

    return webrtc_jni::jlongFromPointer(new MediaFactoryContext(media_options, media_factory));
}

JNIEXPORT jlong JNICALL Java_com_twilio_video_MediaFactory_nativeCreateLocalMedia(JNIEnv *jni,
                                                                                  jobject j_media_factory,
                                                                                  jlong media_factory_handle) {
    jlong local_media_handle = 0;

    if (media_factory_handle != 0) {
        std::shared_ptr<twilio::media::MediaFactory> media_factory = getMediaFactory(media_factory_handle);

        // TODO: Support passing a name in
        std::shared_ptr<twilio::media::LocalMedia> local_media = media_factory->createLocalMedia("android-local-media");
        LocalMediaContext* local_media_context = new LocalMediaContext(local_media);
        local_media_handle = webrtc_jni::jlongFromPointer(local_media_context);
    }

    return local_media_handle;
}

JNIEXPORT void JNICALL Java_com_twilio_video_MediaFactory_nativeRelease(JNIEnv *jni,
                                                                        jobject j_media_factory,
                                                                        jlong media_factory_handle) {
    if (media_factory_handle != 0) {
        MediaFactoryContext *media_factory_context =
                reinterpret_cast<MediaFactoryContext *>(media_factory_handle);

        delete media_factory_context;
    }
}

}
