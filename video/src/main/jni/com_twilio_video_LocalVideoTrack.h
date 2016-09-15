#ifndef VIDEO_ANDROID_COM_TWILIO_VIDEO_LOCALVIDEOTRACK_H_
#define VIDEO_ANDROID_COM_TWILIO_VIDEO_LOCALVIDEOTRACK_H_

#include <jni.h>
#include "media/track.h"

#ifdef __cplusplus
extern "C" {
#endif

namespace twilio_video_jni {

class LocalVideoTrackContext {
public:
    LocalVideoTrackContext(std::shared_ptr<twilio::media::LocalVideoTrack> local_video_track)
            : local_video_track_(local_video_track) {

    }

    virtual ~LocalVideoTrackContext() {
        local_video_track_.reset();
    }

    std::shared_ptr<twilio::media::LocalVideoTrack> getLocalVideoTrack() {
        return local_video_track_;
    }
private:
    std::shared_ptr<twilio::media::LocalVideoTrack> local_video_track_;
};

std::shared_ptr<twilio::media::LocalVideoTrack> getLocalVideoTrack(jlong);
jobject createJavaLocalVideoTrack(std::shared_ptr<twilio::media::LocalVideoTrack> local_video_track,
                                  jobject j_video_capturer,
                                  jobject j_video_constraints);
JNIEXPORT jboolean JNICALL Java_com_twilio_video_LocalVideoTrack_nativeIsEnabled(JNIEnv *,
                                                                                 jobject,
                                                                                 jlong);
JNIEXPORT void JNICALL Java_com_twilio_video_LocalVideoTrack_nativeEnable(JNIEnv *,
                                                                          jobject,
                                                                          jlong,
                                                                          jboolean);
JNIEXPORT void JNICALL Java_com_twilio_video_LocalVideoTrack_nativeRelease(JNIEnv *,
                                                                           jobject,
                                                                           jlong);

}

#ifdef __cplusplus
}
#endif

#endif // VIDEO_ANDROID_COM_TWILIO_VIDEO_LOCALVIDEOTRACK_H_
