#include <jni.h>
#include "media/media.h"
#include "media/media_factory.h"

#ifndef VIDEO_ANDROID_COM_TWILIO_VIDEO_MEDIAFACTORY_H_
#define VIDEO_ANDROID_COM_TWILIO_VIDEO_MEDIAFACTORY_H_

#ifdef __cplusplus
extern "C" {
#endif

namespace twilio_video_jni {

class MediaFactoryContext {
public:
    MediaFactoryContext(twilio::media::MediaOptions media_options,
                        std::shared_ptr<twilio::media::MediaFactory> media_factory) :
            media_options_(media_options),
            media_factory_(media_factory) {

    }

    virtual ~MediaFactoryContext() {
        media_factory_.reset();
    }

    std::shared_ptr<twilio::media::MediaFactory> getMediaFactory() {
        return media_factory_;
    }
private:
    twilio::media::MediaOptions media_options_;
    std::shared_ptr<twilio::media::MediaFactory> media_factory_;
};

std::shared_ptr<twilio::media::MediaFactory> getMediaFactory(jlong);
JNIEXPORT jlong JNICALL Java_com_twilio_video_MediaFactory_nativeCreate(JNIEnv *,
                                                                        jobject,
                                                                        jobject,
                                                                        jobject,
                                                                        jobject);
JNIEXPORT jlong JNICALL Java_com_twilio_video_MediaFactory_nativeCreateLocalMedia(JNIEnv *,
                                                                                  jobject,
                                                                                  jlong);
JNIEXPORT void JNICALL Java_com_twilio_video_MediaFactory_nativeRelease(JNIEnv *, jobject, jlong);

}

#ifdef __cplusplus
}
#endif

#endif // VIDEO_ANDROID_COM_TWILIO_VIDEO_MEDIAFACTORY_H_
