#include <jni.h>
#include "media/media.h"
#include "media/media_factory.h"

#ifndef ROOMS_ANDROID_COM_TWILIO_VIDEO_MEDIAFACTORY_H_
#define ROOMS_ANDROID_COM_TWILIO_VIDEO_MEDIAFACTORY_H_

#ifdef __cplusplus
extern "C" {
#endif

namespace twilio {
namespace media {

class MediaFactoryContext {
public:
    MediaFactoryContext(MediaOptions media_options, std::shared_ptr<MediaFactory> media_factory) :
            media_factory_(media_factory),
            media_options_(media_options) {

    }

    virtual ~MediaFactoryContext() {
        media_factory_.reset();
        if (media_options_.audio_device_module != nullptr) {
            delete media_options_.audio_device_module;
        }
        if (media_options_.video_encoder_factory != nullptr) {
            delete media_options_.video_encoder_factory;
        }
        if (media_options_.video_decoder_factory != nullptr) {
            delete media_options_.video_decoder_factory;
        }
    }

    std::shared_ptr<MediaFactory> getMediaFactory() {
        return media_factory_;
    }
private:
    MediaOptions media_options_;
    std::shared_ptr<MediaFactory> media_factory_;
};

JNIEXPORT jlong JNICALL Java_com_twilio_video_MediaFactory_nativeCreate(JNIEnv *, jobject, jobject);
JNIEXPORT jlong JNICALL Java_com_twilio_video_MediaFactory_nativeCreateLocalMedia(JNIEnv *,
                                                                                  jobject,
                                                                                  jlong);
JNIEXPORT void JNICALL Java_com_twilio_video_MediaFactory_nativeRelease(JNIEnv *, jobject, jlong);

}
}

#ifdef __cplusplus
}
#endif

#endif // ROOMS_ANDROID_COM_TWILIO_VIDEO_MEDIAFACTORY_H_
