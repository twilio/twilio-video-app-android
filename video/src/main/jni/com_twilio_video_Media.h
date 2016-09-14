#ifndef VIDEO_ANDROID_COM_TWILIO_VIDEO_MEDIA_H_
#define VIDEO_ANDROID_COM_TWILIO_VIDEO_MEDIA_H_

#include <jni.h>

#include "media/media.h"
#include "media/track.h"

#ifdef __cplusplus
extern "C" {
#endif

struct MediaContext {
    std::shared_ptr<twilio::media::Media> media;
};

struct VideoTrackContext {
    std::shared_ptr<twilio::media::VideoTrack> video_track;
};

jobject createJavaAudioTrack(JNIEnv *env,
                             std::shared_ptr<twilio::media::AudioTrack> audio_track,
                             jclass j_audio_track_class,
                             jmethodID j_audio_track_ctor_id);

jobject createJavaVideoTrack(JNIEnv *env,
                             std::shared_ptr<twilio::media::VideoTrack> video_track,
                             jclass j_video_track_class, jmethodID j_video_track_ctor_id);

JNIEXPORT void JNICALL Java_com_twilio_video_Media_nativeSetInternalListener
    (JNIEnv *, jobject, jlong, jlong);

JNIEXPORT void JNICALL Java_com_twilio_video_VideoTrack_nativeRelease
    (JNIEnv *, jobject, jlong);

JNIEXPORT void JNICALL Java_com_twilio_video_Media_nativeRelease
    (JNIEnv *, jobject, jlong);

JNIEXPORT jlong JNICALL Java_com_twilio_video_Media_00024InternalMediaListenerHandle_nativeCreate
    (JNIEnv *, jobject, jobject);

JNIEXPORT void JNICALL Java_com_twilio_video_Media_00024InternalMediaListenerHandle_nativeRelease
    (JNIEnv *, jobject, jlong);


#ifdef __cplusplus
}
#endif

#endif // VIDEO_ANDROID_COM_TWILIO_VIDEO_MEDIA_H_
