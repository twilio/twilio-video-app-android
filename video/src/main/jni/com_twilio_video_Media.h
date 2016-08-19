#ifndef CONVERSATIONS_ANDROID_COM_TWILIO_VIDEO_MEDIA_H_H
#define CONVERSATIONS_ANDROID_COM_TWILIO_VIDEO_MEDIA_H_H
#include <jni.h>

#include "media/Media.h"
#include "media/track.h"

#ifdef __cplusplus
extern "C" {
#endif

struct MediaContext {
    std::shared_ptr<twilio::media::Media> media;
};

struct AudioTrackContext {
    std::shared_ptr<twilio::media::AudioTrack> audio_track;
};

struct VideoTrackContext {
    std::shared_ptr<twilio::media::VideoTrack> video_track;
};

/*
 * Class:     com_twilio_video_Media_InternalMediaListenerHandle
 * Method:    nativeCreate
 * Signature: (Ljava/lang/Object;)J
 */
JNIEXPORT jlong JNICALL Java_com_twilio_video_Media_00024InternalMediaListenerHandle_nativeCreate
    (JNIEnv *, jobject, jobject);

/*
 * Class:     com_twilio_video_Media_InternalMediaListenerHandle
 * Method:    nativeFree
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_twilio_video_Media_00024InternalMediaListenerHandle_nativeFree
    (JNIEnv *, jobject, jlong);


#ifdef __cplusplus
}
#endif

#endif //CONVERSATIONS_ANDROID_COM_TWILIO_VIDEO_MEDIA_H_H
