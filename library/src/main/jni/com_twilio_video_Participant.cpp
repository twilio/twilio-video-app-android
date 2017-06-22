#include "com_twilio_video_Participant.h"

#include "webrtc/sdk/android/src/jni/jni_helpers.h"
#include "webrtc/sdk/android/src/jni/classreferenceholder.h"

#include "video/logger.h"

namespace twilio_video_jni {

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
                              jobject j_handler) {
    ParticipantContext *participant_context = new ParticipantContext();
    participant_context->participant = participant;
    jstring j_sid = webrtc_jni::JavaStringFromStdString(env, participant->getSid());
    jstring j_identity =
            webrtc_jni::JavaStringFromStdString(env, participant->getIdentity());
    jobject j_audio_tracks = createParticipantAudioTracks(env,
                                                          participant_context,
                                                          j_array_list_class,
                                                          j_array_list_ctor_id,
                                                          j_array_list_add,
                                                          j_audio_track_class,
                                                          j_audio_track_ctor_id);
    jobject j_video_tracks = createParticipantVideoTracks(env,
                                                          participant_context,
                                                          j_array_list_class,
                                                          j_array_list_ctor_id,
                                                          j_array_list_add,
                                                          j_video_track_class,
                                                          j_video_track_ctor_id);

    // Create participant
    jlong j_participant_context = webrtc_jni::jlongFromPointer(participant_context);
    return env->NewObject(j_participant_class,
                          j_participant_ctor_id,
                          j_identity,
                          j_sid,
                          j_audio_tracks,
                          j_video_tracks,
                          j_handler,
                          j_participant_context);
}

jobject createParticipantAudioTracks(JNIEnv *env,
                                     ParticipantContext* participant_context,
                                     jclass j_array_list_class,
                                     jmethodID j_array_list_ctor_id,
                                     jmethodID j_array_list_add,
                                     jclass j_audio_track_class,
                                     jmethodID j_audio_track_ctor_id) {
    jobject j_audio_tracks = env->NewObject(j_array_list_class, j_array_list_ctor_id);

    const std::vector<std::shared_ptr<twilio::media::AudioTrack>> audio_tracks =
            participant_context->participant->getAudioTracks();

    // Add audio tracks to array list
    for (unsigned int i = 0; i < audio_tracks.size(); i++) {
        std::shared_ptr<twilio::media::AudioTrack> audio_track = audio_tracks[i];
        jobject j_audio_track =
                createJavaAudioTrack(env,
                                     audio_track,
                                     j_audio_track_class,
                                     j_audio_track_ctor_id);

        /*
         * We create a global reference to the java audio track so we can map audio track events
         * to the original java instance.
         */
        participant_context->audio_track_map
                .insert(std::make_pair(audio_track,
                                       webrtc_jni::NewGlobalRef(env, j_audio_track)));
        env->CallVoidMethod(j_audio_tracks, j_array_list_add, j_audio_track);
    }

    return j_audio_tracks;
}

jobject createParticipantVideoTracks(JNIEnv *env,
                                     ParticipantContext* participant_context,
                                     jclass j_array_list_class,
                                     jmethodID j_array_list_ctor_id,
                                     jmethodID j_array_list_add,
                                     jclass j_video_track_class,
                                     jmethodID j_video_track_ctor_id) {
    jobject j_video_tracks = env->NewObject(j_array_list_class, j_array_list_ctor_id);

    const std::vector<std::shared_ptr<twilio::media::VideoTrack>> video_tracks =
            participant_context->participant->getVideoTracks();

    // Add video tracks to array list
    for (unsigned int i = 0; i < video_tracks.size(); i++) {
        std::shared_ptr<twilio::media::VideoTrack> video_track = video_tracks[i];
        jobject j_video_track =
                createJavaVideoTrack(env,
                                     video_track,
                                     j_video_track_class,
                                     j_video_track_ctor_id);
        /*
         * We create a global reference to the java video track so we can map video track events
         * to the original java instance.
         */
        participant_context->video_track_map
                .insert(std::make_pair(video_track,
                                       webrtc_jni::NewGlobalRef(env, j_video_track)));
        env->CallVoidMethod(j_video_tracks, j_array_list_add, j_video_track);
    }

    return j_video_tracks;
}

jobject createJavaAudioTrack(JNIEnv *env,
                             std::shared_ptr<twilio::media::AudioTrack> audio_track,
                             jclass j_audio_track_class,
                             jmethodID j_audio_track_ctor_id) {
    jstring j_track_id = webrtc_jni::JavaStringFromStdString(env, audio_track->getTrackId());
    jboolean j_is_enabled = audio_track->isEnabled();
    // TODO: Use real track SID when ready
    jstring j_track_sid = j_track_id;

    return env->NewObject(j_audio_track_class,
                          j_audio_track_ctor_id,
                          j_track_id,
                          j_is_enabled,
                          j_track_sid);
}

jobject createJavaVideoTrack(JNIEnv *env,
                             std::shared_ptr<twilio::media::VideoTrack> video_track,
                             jclass j_video_track_class, jmethodID j_video_track_ctor_id) {
    jclass j_webrtc_video_track_class = webrtc_jni::FindClass(env, "org/webrtc/VideoTrack");
    jmethodID j_webrtc_video_track_ctor_id = webrtc_jni::GetMethodID(env,
                                                                     j_webrtc_video_track_class,
                                                                     "<init>",
                                                                     "(J)V");
    jboolean j_is_enabled = video_track->isEnabled();
    jobject j_webrtc_video_track = env->NewObject(j_webrtc_video_track_class,
                                                  j_webrtc_video_track_ctor_id,
                                                  webrtc_jni::jlongFromPointer(
                                                          video_track->getWebRtcTrack()));
    // TODO: Get real track sid
    jstring j_track_sid = webrtc_jni::JavaStringFromStdString(env, video_track->getTrackId());

    return env->NewObject(j_video_track_class,
                          j_video_track_ctor_id,
                          j_webrtc_video_track,
                          j_is_enabled,
                          j_track_sid);
}

JNIEXPORT void JNICALL
Java_com_twilio_video_Participant_nativeCreateParticipantListenerProxy(JNIEnv *env,
                                                                       jobject j_participant,
                                                                       jobject j_participant_listener_proxy,
                                                                       jlong j_participant_context) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "%s", func_name.c_str());
    ParticipantContext *participant_context =
            reinterpret_cast<ParticipantContext *>(j_participant_context);
    participant_context->android_participant_observer =
            std::make_shared<AndroidParticipantObserver>(env,
                                                         j_participant,
                                                         j_participant_listener_proxy,
                                                         participant_context->audio_track_map,
                                                         participant_context->video_track_map);
    participant_context->participant->setObserver(
            participant_context->android_participant_observer);
}

JNIEXPORT jboolean JNICALL
Java_com_twilio_video_Participant_nativeIsConnected(JNIEnv *env, jobject instance,
                                                    jlong j_participant_context) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "%s", func_name.c_str());
    ParticipantContext *participant_context =
            reinterpret_cast<ParticipantContext *>(j_participant_context);
    if (participant_context == nullptr || !participant_context->participant) {
        TS_CORE_LOG_WARNING("Participant object no longer exist");
        return false;
    }

    return participant_context->participant->isConnected();
}

JNIEXPORT void JNICALL
Java_com_twilio_video_Participant_nativeRelease(JNIEnv *env,
                                                jobject instance,
                                                jlong j_participant_context) {
    ParticipantContext *participant_context =
            reinterpret_cast<ParticipantContext *>(j_participant_context);

    // Delete the participant observer
    participant_context->android_participant_observer->setObserverDeleted();
    participant_context->android_participant_observer = nullptr;

    // Delete all remaining global references to AudioTracks
    for (auto it = participant_context->audio_track_map.begin() ;
         it != participant_context->audio_track_map.end() ; it++) {
        webrtc_jni::DeleteGlobalRef(env, it->second);
    }
    participant_context->audio_track_map.clear();

    // Delete all remaining global references to VideoTracks
    for (auto it = participant_context->video_track_map.begin() ;
         it != participant_context->video_track_map.end() ; it++) {
        webrtc_jni::DeleteGlobalRef(env, it->second);
    }
    participant_context->video_track_map.clear();

    // Now that all participant resources are deleted we delete participant context
    delete participant_context;
}

}