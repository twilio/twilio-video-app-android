#ifndef VIDEO_ANDROID_INCLUDE_COM_TWILIO_VIDEO_REMOTE_PARTICIPANT_H_
#define VIDEO_ANDROID_INCLUDE_COM_TWILIO_VIDEO_REMOTE_PARTICIPANT_H_

#include <jni.h>
#include <memory>
#include <map>
#include "video/remote_participant.h"
#include "android_participant_observer.h"

namespace twilio_video_jni {

#ifdef __cplusplus
extern "C" {
#endif

struct RemoteParticipantContext {
    std::shared_ptr<twilio::video::RemoteParticipant> remote_participant;
    std::map<std::shared_ptr<twilio::media::RemoteAudioTrack>, jobject> remote_audio_track_map;
    std::map<std::shared_ptr<twilio::media::RemoteVideoTrack>, jobject> remote_video_track_map;
    std::shared_ptr<AndroidParticipantObserver> android_participant_observer;
};

jobject createJavaRemoteParticipant(JNIEnv *env,
                                    std::shared_ptr<twilio::video::RemoteParticipant> remote_participant,
                                    jclass j_remote_participant_class,
                                    jmethodID j_remote_participant_ctor_id,
                                    jclass j_array_list_class,
                                    jmethodID j_array_list_ctor_id,
                                    jmethodID j_array_list_add,
                                    jclass j_remote_audio_track_class,
                                    jmethodID j_remote_audio_track_ctor_id,
                                    jclass j_remote_video_track_class,
                                    jmethodID j_remote_video_track_ctor_id,
                                    jobject j_handler);

jobject createRemoteParticipantAudioTracks(JNIEnv *env,
                                           RemoteParticipantContext *remote_participant_context,
                                           jclass j_array_list_class,
                                           jmethodID j_array_list_ctor_id,
                                           jmethodID j_array_list_add,
                                           jclass j_remote_audio_track_class,
                                           jmethodID j_remote_audio_track_ctor_id);

jobject createRemoteParticipantVideoTracks(JNIEnv *env,
                                           RemoteParticipantContext *remote_participant_context,
                                           jclass j_array_list_class,
                                           jmethodID j_array_list_ctor_id,
                                           jmethodID j_array_list_add,
                                           jclass j_remote_video_track_class,
                                           jmethodID j_remote_video_track_ctor_id);

jobject createJavaRemoteAudioTrack(JNIEnv *env,
                                   std::shared_ptr<twilio::media::RemoteAudioTrack> remote_audio_track,
                                   jclass j_remote_audio_track_class,
                                   jmethodID j_remote_audio_track_ctor_id);

jobject createJavaRemoteVideoTrack(JNIEnv *env,
                                   std::shared_ptr<twilio::media::RemoteVideoTrack> remote_video_track,
                                   jclass j_remote_video_track_class,
                                   jmethodID j_remote_video_track_ctor_id);

JNIEXPORT jboolean JNICALL Java_com_twilio_video_RemoteParticipant_nativeIsConnected
        (JNIEnv *, jobject, jlong);

JNIEXPORT void JNICALL Java_com_twilio_video_RemoteParticipant_nativeRelease
        (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif

}

#endif // VIDEO_ANDROID_INCLUDE_COM_TWILIO_VIDEO_REMOTE_PARTICIPANT_H_
