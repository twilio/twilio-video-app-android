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
    TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "wrapOutgoingSession");
    TSCEndpointPtr *endpoint = reinterpret_cast<TSCEndpointPtr *>(nativeEndpoint);
    TSCOptions options;
    options.insert(std::pair<std::string,std::string>("audio","yes"));
    options.insert(std::pair<std::string,std::string>("video","yes"));

    TSCSessionObserverPtr *sessionObserver = reinterpret_cast<TSCSessionObserverPtr *>(nativeSessionObserver);
    if (sessionObserver == nullptr || !(*sessionObserver)) {
        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "sessionObserver was null");
        return 0;
    }

    TSCSessionPtr *outgoingSession = new TSCSessionPtr();
    *outgoingSession = endpoint->get()->createSession(options, *sessionObserver);

    if (outgoingSession == nullptr || !(*outgoingSession)) {
        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "outgoingSession was null");
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
        std::string participantStr = JavaToStdString(env, value);
        participants.push_back(participantStr);
    }

    outgoingSession->get()->setParticipants(participants);
    return webrtc_jni::jlongFromPointer(outgoingSession);
}


JNIEXPORT void JNICALL Java_com_twilio_conversations_impl_ConversationImpl_start
        (JNIEnv *env, jobject obj, jlong nativeSession, jboolean j_enableAudio, jboolean j_muteAudio, jboolean j_enableVideo, jboolean j_pauseVideo, jobject j_video_constraints)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "start");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);

    bool enableAudio = j_enableAudio == JNI_TRUE ? true : false;
    bool muteAudio = j_muteAudio == JNI_TRUE ? true : false;
    bool enableVideo = j_enableVideo == JNI_TRUE ? true : false;
    bool pauseVideo = j_pauseVideo == JNI_TRUE ? true : false;

    TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "local media config: %s, %s, %s, %s",
                       (enableAudio ? "enabledAudio = true":"enabledAudio = false"),
                       (muteAudio ? "muteAudio = true":"muteAudio = false"),
                       (enableVideo ? "enableVideo = true":"enableVideo=false"),
                       (pauseVideo ? "pauseVideo=true":"pauseVideo=false"));

    if(webrtc_jni::IsNull(env, j_video_constraints)) {
        session->get()->start(
                new TSCSessionMediaConstraintsObject(enableAudio,
                                                     muteAudio,
                                                     enableVideo,
                                                     pauseVideo));
    } else {
        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "Parsing video constraints");

        jclass video_constraints_class = env->GetObjectClass(j_video_constraints);
        jfieldID min_fps_field =
                env->GetFieldID(video_constraints_class, "minFps", "I");
        jfieldID max_fps_field =
                env->GetFieldID(video_constraints_class, "maxFps", "I");
        int min_fps =
                env->GetIntField(j_video_constraints, min_fps_field);
        int max_fps =
                env->GetIntField(j_video_constraints, max_fps_field);

        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug,
                           "Video constraints minFps %d maxFps %d",
                            min_fps,
                            max_fps);

        jfieldID min_video_dimensions_field = GetFieldID(env,
                                                         video_constraints_class,
                                                         "minVideoDimensions",
                                                         "Lcom/twilio/conversations/VideoDimensions;");
        jfieldID max_video_dimensions_field = GetFieldID(env,
                                                         video_constraints_class,
                                                         "maxVideoDimensions",
                                                         "Lcom/twilio/conversations/VideoDimensions;");

        jobject j_min_video_dimensions = env->GetObjectField(j_video_constraints, min_video_dimensions_field);
        jclass min_video_dimensions_class = env->GetObjectClass(j_min_video_dimensions);
        jfieldID min_width_field =
                env->GetFieldID(min_video_dimensions_class, "width", "I");
        jfieldID min_height_field =
                env->GetFieldID(min_video_dimensions_class, "height", "I");
        int min_width =
                env->GetIntField(j_min_video_dimensions, min_width_field);
        int min_height =
                env->GetIntField(j_min_video_dimensions, min_height_field);

        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug,
                           "Video constraints min width %d min height %d",
                            min_width,
                            min_height);

        jobject j_max_video_dimensions = env->GetObjectField(j_video_constraints, max_video_dimensions_field);
        jclass max_video_dimensions_class = env->GetObjectClass(j_max_video_dimensions);
        jfieldID max_width_field =
                env->GetFieldID(max_video_dimensions_class, "width", "I");
        jfieldID max_height_field =
                env->GetFieldID(max_video_dimensions_class, "height", "I");
        int max_width =
                env->GetIntField(j_max_video_dimensions, max_width_field);
        int max_height =
                env->GetIntField(j_max_video_dimensions, max_height_field);

        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug,
                           "Video constraints max width %d max height %d",
                            max_width,
                            max_height);

        TSCConstraintsRef constraints = new TSCConstraintsObject();

        if (max_fps > 0) {
            constraints->SetMandatory(twiliosdk::TSCConstraints::kMaxFrameRate, max_fps);
        }
        if (min_fps > 0) {
            constraints->SetMandatory(twiliosdk::TSCConstraints::kMinFrameRate, min_fps);
        }

        if (max_width > 0 && max_height > 0) {
            constraints->SetMandatory(twiliosdk::TSCConstraints::kMaxWidth, max_width);
            constraints->SetMandatory(twiliosdk::TSCConstraints::kMaxHeight, max_height);
        }
        if (min_width > 0 && min_height > 0) {
            constraints->SetMandatory(twiliosdk::TSCConstraints::kMinWidth, min_width);
            constraints->SetMandatory(twiliosdk::TSCConstraints::kMinHeight, min_height);
        }

        session->get()->start(
                new TSCSessionMediaConstraintsObject(enableAudio,
                                                     muteAudio,
                                                     enableVideo,
                                                     pauseVideo),
                                                     constraints);
    }
}


JNIEXPORT void JNICALL Java_com_twilio_conversations_impl_ConversationImpl_stop
        (JNIEnv *env, jobject obj, jlong nativeSession)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "stop");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    session->get()->stop();
}

JNIEXPORT void JNICALL Java_com_twilio_conversations_impl_ConversationImpl_setExternalCapturer
        (JNIEnv *env, jobject obj, jlong nativeSession, jlong nativeCapturer)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "setExternalCapturer");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    TSCVideoCaptureControllerPtr videoCaptureController = session->get()->getVideoCaptureController();
    if(videoCaptureController != nullptr) {
        videoCaptureController->setExternalVideoCapturer(reinterpret_cast<cricket::VideoCapturer *>(nativeCapturer));
    } else {
        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "videoCapturerController was null");
    }
}

JNIEXPORT void JNICALL Java_com_twilio_conversations_impl_ConversationImpl_setSessionObserver
        (JNIEnv *, jobject, jlong nativeSession, jlong nativeSessionObserver)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "setSessionObserver");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    TSCSessionObserverPtr *sessionObserver = reinterpret_cast<TSCSessionObserverPtr *>(nativeSessionObserver);
    session->get()->setSessionObserver(*sessionObserver);
}

JNIEXPORT jboolean JNICALL Java_com_twilio_conversations_impl_ConversationImpl_enableVideo
        (JNIEnv *, jobject, jlong nativeSession, jboolean enabled, jboolean paused)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "enableVideo");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    return (session->get()->enableVideo((bool)enabled, (bool)paused) ? JNI_TRUE : JNI_FALSE);
}

JNIEXPORT void JNICALL Java_com_twilio_conversations_impl_ConversationImpl_freeNativeHandle
        (JNIEnv *env, jobject obj, jlong nativeSession) {
    TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "freeNativeHandle: Session");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    if (session != nullptr) {
        session->reset();
        delete session;
    }
}

JNIEXPORT jboolean JNICALL Java_com_twilio_conversations_impl_ConversationImpl_mute
        (JNIEnv *, jobject, jlong nativeSession, jboolean on)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "mute");
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
    TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "isMuted");
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
    TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "inviteParticipants");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    int size = env->GetArrayLength(participantList);
    if (size == 0) {
        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "no participants were provided");
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
    TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "getConversationSid");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);

    return JavaStringFromStdString(env, session->get()->getConversationSid());
}

JNIEXPORT jboolean JNICALL Java_com_twilio_conversations_impl_ConversationImpl_enableAudio
        (JNIEnv *, jobject, jlong nativeSession, jboolean j_enabled, jboolean j_muted)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "enableAudio");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    bool enabled = (j_enabled == JNI_TRUE) ? true : false;
    bool muted = (j_muted) ? true : false;
    if (session) {
        return session->get()->enableAudio(enabled, muted) ? JNI_TRUE : JNI_FALSE;
    }
    return JNI_FALSE;
}
