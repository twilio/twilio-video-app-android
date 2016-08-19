#include <jni.h>
#include "media/media.h"

#ifndef ROOMS_ANDROID_COM_TWILIO_VIDEO_LOCALMEDIA_H_
#define ROOMS_ANDROID_COM_TWILIO_VIDEO_LOCALMEDIA_H_

#ifdef __cplusplus
extern "C" {
#endif

namespace twilio {
namespace media {

class LocalMediaContext {
public:
    LocalMediaContext(std::shared_ptr<LocalMedia> local_media) : local_media_(local_media) {

    }

    virtual ~LocalMediaContext() {
        local_media_.reset();
    }

    std::shared_ptr<LocalMedia> getLocalMedia() {
        return local_media_;
    }
private:
    std::shared_ptr<LocalMedia> local_media_;
};

JNIEXPORT jobject JNICALL Java_com_twilio_video_LocalMedia_nativeGetDefaultAudioOptions(JNIEnv *,
                                                                                        jobject);
JNIEXPORT jlong JNICALL Java_com_twilio_video_LocalMedia_nativeAddAudioTrack(JNIEnv *,
                                                                             jobject,
                                                                             jlong,
                                                                             jboolean,
                                                                             jobject);
JNIEXPORT void JNICALL Java_com_twilio_video_LocalMedia_nativeRelease(JNIEnv *, jobject, jlong);

}
}

#ifdef __cplusplus
}
#endif

#endif // ROOMS_ANDROID_COM_TWILIO_VIDEO_LOCALMEDIA_H_
