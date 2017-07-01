#ifndef VIDEO_ANDROID_INCLUDE_COM_TWILIO_VIDEO_REMOTE_PARTICIPANT_H_
#define VIDEO_ANDROID_INCLUDE_COM_TWILIO_VIDEO_REMOTE_PARTICIPANT_H_

#include <jni.h>
#include <memory>
#include <map>
#include "video/participant.h"
#include "android_participant_observer.h"

namespace twilio_video_jni {

#ifdef __cplusplus
extern "C" {
#endif

struct RemoteParticipantContext {
    std::shared_ptr<twilio::video::Participant> participant;
    std::map<std::shared_ptr<twilio::media::AudioTrack>, jobject> audio_track_map;
    std::map<std::shared_ptr<twilio::media::VideoTrack>, jobject> video_track_map;
    std::shared_ptr<AndroidParticipantObserver> android_participant_observer;
};

jobject createJavaParticipant(JNIEnv *env,
                              std::shared_ptr<twilio::video::Participant> participant,
                              jclass j_participant_class,
                              jmethodID j_participant_ctor_id,
                              jclass j_array_list_class,
                              jmethodID j_array_list_ctor_id,
                              jmethodID j_array_list_add,
                              jclass j_audio_track_class,
                              jmethodID j_audio_track_ctor_id,
                              jclass j_video_track_class,
                              jmethodID j_video_track_ctor_id,
                              jobject j_handler);

jobject createParticipantAudioTracks(JNIEnv *env,
                                     RemoteParticipantContext *participant_context,
                                     jclass j_array_list_class,
                                     jmethodID j_array_list_ctor_id,
                                     jmethodID j_array_list_add,
                                     jclass j_audio_track_class,
                                     jmethodID j_audio_track_ctor_id);

jobject createParticipantVideoTracks(JNIEnv *env,
                                     RemoteParticipantContext *participant_context,
                                     jclass j_array_list_class,
                                     jmethodID j_array_list_ctor_id,
                                     jmethodID j_array_list_add,
                                     jclass j_video_track_class,
                                     jmethodID j_video_track_ctor_id);

jobject createJavaAudioTrack(JNIEnv *env,
                             std::shared_ptr<twilio::media::AudioTrack> audio_track,
                             jclass j_audio_track_class,
                             jmethodID j_audio_track_ctor_id);

jobject createJavaVideoTrack(JNIEnv *env,
                             std::shared_ptr<twilio::media::VideoTrack> video_track,
                             jclass j_video_track_class, jmethodID j_video_track_ctor_id);

JNIEXPORT jboolean JNICALL Java_com_twilio_video_RemoteParticipant_nativeIsConnected
        (JNIEnv *, jobject, jlong);

JNIEXPORT void JNICALL Java_com_twilio_video_RemoteParticipant_nativeRelease
        (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif

}

#endif // VIDEO_ANDROID_INCLUDE_COM_TWILIO_VIDEO_REMOTE_PARTICIPANT_H_
