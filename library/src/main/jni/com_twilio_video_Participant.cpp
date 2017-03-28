#include "com_twilio_video_Participant.h"

#include "webrtc/sdk/android/src/jni/jni_helpers.h"

#include "com_twilio_video_Media.h"

#include "video/logger.h"

jobject createJavaParticipant(JNIEnv *env,
                              std::shared_ptr<twilio::video::Participant> participant,
                              jobject j_media,
                              jclass j_participant_class,
                              jmethodID j_particpant_ctor_id) {
    ParticipantContext *participant_context = new ParticipantContext();
    participant_context->participant = participant;
    jstring j_sid = webrtc_jni::JavaStringFromStdString(env, participant->getSid());
    jstring j_identity =
        webrtc_jni::JavaStringFromStdString(env, participant->getIdentity());

    // Create participant
    jlong j_participant_context = webrtc_jni::jlongFromPointer(participant_context);
    return env->NewObject(j_participant_class, j_particpant_ctor_id,
                         j_identity, j_sid, j_media, j_participant_context);
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
Java_com_twilio_video_Participant_nativeRelease(JNIEnv *env, jobject instance,
                                                jlong j_participant_context) {

    ParticipantContext *participant_context =
        reinterpret_cast<ParticipantContext *>(j_participant_context);
    delete participant_context;
}