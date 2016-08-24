#include "com_twilio_video_MediaFactory.h"
#include "com_twilio_video_LocalMedia.h"

#include "webrtc/api/java/jni/jni_helpers.h"
#include "webrtc/voice_engine/include/voe_base.h"
#include "webrtc/api/java/jni/androidvideocapturer_jni.h"
#include "webrtc/modules/audio_device/android/opensles_player.h"
#include "webrtc/api/java/jni/classreferenceholder.h"

namespace twilio_video_jni {

static bool media_jvm_set = false;

std::shared_ptr<twilio::media::MediaFactory> getMediaFactory(jlong media_factory_handle) {
    MediaFactoryContext *media_factory_context =
            reinterpret_cast<MediaFactoryContext *>(media_factory_handle);

    return media_factory_context->getMediaFactory();
}

JNIEXPORT jlong JNICALL Java_com_twilio_video_MediaFactory_nativeCreate(JNIEnv *jni,
                                                                        jobject j_media_factory,
                                                                        jobject context) {
    // Setup media related Android device objects
    if (!media_jvm_set) {
        bool failure = false;
        failure |= webrtc::OpenSLESPlayer::SetAndroidAudioDeviceObjects(webrtc_jni::GetJVM(),
                                                                        context);
        failure |= webrtc::VoiceEngine::SetAndroidObjects(webrtc_jni::GetJVM(), context);
        failure |= webrtc_jni::AndroidVideoCapturerJni::SetAndroidObjects(jni, context);

        if (failure) {
            return 0;
        } else {
            media_jvm_set = true;
        }
    }

    // TODO: Set encoder and decoder options
    twilio::media::MediaOptions media_options;
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
