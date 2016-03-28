#include "com_twilio_conversations_impl_ConversationImpl.h"
#include "talk/app/webrtc/java/jni/jni_helpers.h"
#include "TSCoreSDKTypes.h"
#include "TSCoreError.h"
#include "TSCLogger.h"
#include "TSCEndpoint.h"
#include "TSCSessionObserver.h"
#include "TSCVideoCaptureController.h"
#include "TSCSession.h"
#include "TSCAudioInputController.h"

#include <string>
#include <map>
#include <vector>


using namespace twiliosdk;
using namespace webrtc_jni;

#define TAG  "TwilioSDK(native)"


JNIEXPORT jlong JNICALL Java_com_twilio_conversations_impl_ConversationImpl_wrapOutgoingSession
        (JNIEnv *env, jobject obj, jlong nativeEndpoint, jlong nativeSessionObserver, jobjectArray participantList) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatformSDK, kTSCoreLogLevelDebug, "wrapOutgoingSession");
    TSCEndpointPtr *endpoint = reinterpret_cast<TSCEndpointPtr *>(nativeEndpoint);
    TSCOptions options;
    options.insert(std::pair<std::string,std::string>("audio","yes"));
    options.insert(std::pair<std::string,std::string>("video","yes"));

    TSCSessionObserverPtr *sessionObserver = reinterpret_cast<TSCSessionObserverPtr *>(nativeSessionObserver);
    if (sessionObserver == nullptr || !(*sessionObserver)) {
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatformSDK, kTSCoreLogLevelDebug, "sessionObserver was null");
        return 0;
    }

    TSCSessionPtr *outgoingSession = new TSCSessionPtr();
    *outgoingSession = endpoint->get()->createSession(options, *sessionObserver);

    if (outgoingSession == nullptr || !(*outgoingSession)) {
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatformSDK, kTSCoreLogLevelDebug, "outgoingSession was null");
        return 0;
    }

    int size = env->GetArrayLength(participantList);
    if (size == 0) {
        TS_CORE_LOG_DEBUG("no participants were provided");
        return 0;
    }

    std::vector<std::string> participants;
    for (int i=0; i < size; i++) {
        jstring value = (jstring)env->GetObjectArrayElement(participantList, i);
        const char *nativeString = env->GetStringUTFChars(value, 0);
        std::string participantStr(nativeString);
        env->ReleaseStringUTFChars(value, nativeString);
        participants.push_back(participantStr);
    }

    outgoingSession->get()->setParticipants(participants);
    return webrtc_jni::jlongFromPointer(outgoingSession);
}


JNIEXPORT void JNICALL Java_com_twilio_conversations_impl_ConversationImpl_start
        (JNIEnv *env, jobject obj, jlong nativeSession, jboolean j_enableAudio, jboolean j_muteAudio, jboolean j_enableVideo, jboolean j_pauseVideo)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatformSDK, kTSCoreLogLevelDebug, "start");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);

    bool enableAudio = j_enableAudio == JNI_TRUE ? true : false;
    bool muteAudio = j_muteAudio == JNI_TRUE ? true : false;
    bool enableVideo = j_enableVideo == JNI_TRUE ? true : false;
    bool pauseVideo = j_pauseVideo == JNI_TRUE ? true : false;

    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatformSDK, kTSCoreLogLevelDebug, "local media config: %s, %s, %s, %s",
                       (enableAudio ? "enabledAudio = true":"enabledAudio = false"),
                       (muteAudio ? "muteAudio = true":"muteAudio = false"),
                       (enableVideo ? "enableVideo = true":"enableVideo=false"),
                       (pauseVideo ? "pauseVideo=true":"pauseVideo=false"));

    session->get()->start(new TSCSessionMediaConstraintsObject(enableAudio, muteAudio, enableVideo, pauseVideo));
}


JNIEXPORT void JNICALL Java_com_twilio_conversations_impl_ConversationImpl_stop
        (JNIEnv *env, jobject obj, jlong nativeSession)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatformSDK, kTSCoreLogLevelDebug, "stop");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    session->get()->stop();
}

JNIEXPORT void JNICALL Java_com_twilio_conversations_impl_ConversationImpl_setExternalCapturer
        (JNIEnv *env, jobject obj, jlong nativeSession, jlong nativeCapturer)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatformSDK, kTSCoreLogLevelDebug, "setExternalCapturer");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    TSCVideoCaptureControllerPtr videoCaptureController = session->get()->getVideoCaptureController();
    if(videoCaptureController != nullptr) {
        videoCaptureController->setExternalVideoCapturer(reinterpret_cast<cricket::VideoCapturer *>(nativeCapturer));
    } else {
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatformSDK, kTSCoreLogLevelDebug, "videoCapturerController was null");
    }
}

JNIEXPORT void JNICALL Java_com_twilio_conversations_impl_ConversationImpl_setSessionObserver
        (JNIEnv *, jobject, jlong nativeSession, jlong nativeSessionObserver)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatformSDK, kTSCoreLogLevelDebug, "setSessionObserver");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    TSCSessionObserverPtr *sessionObserver = reinterpret_cast<TSCSessionObserverPtr *>(nativeSessionObserver);
    session->get()->setSessionObserver(*sessionObserver);
}

JNIEXPORT jboolean JNICALL Java_com_twilio_conversations_impl_ConversationImpl_enableVideo
        (JNIEnv *, jobject, jlong nativeSession, jboolean enabled, jboolean paused)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatformSDK, kTSCoreLogLevelDebug, "enableVideo");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    return (session->get()->enableVideo((bool)enabled, (bool)paused) ? JNI_TRUE : JNI_FALSE);
}

JNIEXPORT void JNICALL Java_com_twilio_conversations_impl_ConversationImpl_freeNativeHandle
        (JNIEnv *env, jobject obj, jlong nativeSession) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatformSDK, kTSCoreLogLevelDebug, "freeNativeHandle: Session");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    if (session != nullptr) {
        session->reset();
        delete session;
    }
}

JNIEXPORT jboolean JNICALL Java_com_twilio_conversations_impl_ConversationImpl_mute
        (JNIEnv *, jobject, jlong nativeSession, jboolean on)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatformSDK, kTSCoreLogLevelDebug, "mute");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    TSCAudioInputControllerPtr audioInputCtrl = session->get()->getAudioInputController();
    if (audioInputCtrl) {
        return audioInputCtrl->setMuted(on) ? JNI_TRUE : JNI_FALSE;
    }
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_twilio_conversations_impl_ConversationImpl_isMuted
        (JNIEnv *, jobject, jlong nativeSession)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatformSDK, kTSCoreLogLevelDebug, "isMuted");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    TSCAudioInputControllerPtr audioInputCtrl = session->get()->getAudioInputController();
    if (audioInputCtrl) {
        return audioInputCtrl->isMuted() ? JNI_TRUE : JNI_FALSE;
    }
    return JNI_FALSE;
}

JNIEXPORT void JNICALL Java_com_twilio_conversations_impl_ConversationImpl_inviteParticipants
        (JNIEnv *env, jobject obj, jlong nativeSession, jobjectArray participantList)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatformSDK, kTSCoreLogLevelDebug, "inviteParticipants");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    int size = env->GetArrayLength(participantList);
    if (size == 0) {
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatformSDK, kTSCoreLogLevelDebug, "no participants were provided");
        return;
    }

    std::vector<std::string> participants;
    for (int i=0; i < size; i++) {
        jstring value = (jstring)env->GetObjectArrayElement(participantList, i);
        std::string participantStr = webrtc_jni::JavaToStdString(env, value);
        participants.push_back(participantStr);
    }
    session->get()->inviteParticipants(participants);
}

JNIEXPORT jstring JNICALL Java_com_twilio_conversations_impl_ConversationImpl_getConversationSid
        (JNIEnv *env, jobject obj, jlong nativeSession)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatformSDK, kTSCoreLogLevelDebug, "getConversationSid");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);

    return JavaStringFromStdString(env, session->get()->getConversationSid());
}

JNIEXPORT jboolean JNICALL Java_com_twilio_conversations_impl_ConversationImpl_enableAudio
        (JNIEnv *, jobject, jlong nativeSession, jboolean j_enabled, jboolean j_muted)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatformSDK, kTSCoreLogLevelDebug, "enableAudio");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    bool enabled = (j_enabled == JNI_TRUE) ? true : false;
    bool muted = (j_muted) ? true : false;
    if (session) {
        return session->get()->enableAudio(enabled, muted) ? JNI_TRUE : JNI_FALSE;
    }
    return JNI_FALSE;
}
