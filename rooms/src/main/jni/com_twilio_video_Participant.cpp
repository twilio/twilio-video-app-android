#include "com_twilio_video_Participant.h"

#include "webrtc/api/java/jni/jni_helpers.h"

#include "video/logger.h"

JNIEXPORT jboolean JNICALL
Java_com_twilio_video_Participant_nativeIsConnected(JNIEnv *env, jobject instance,
                                                    jlong j_participant_context) {
    std::string func_name = std::string(__FUNCTION__);
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());
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