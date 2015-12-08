#include "com_twilio_signal_impl_ConversationImpl.h"
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

#define TAG  "TwilioSDK(native)"


JNIEXPORT jlong JNICALL Java_com_twilio_signal_impl_ConversationImpl_wrapOutgoingSession
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
		const char *nativeString = env->GetStringUTFChars(value, 0);
		std::string participantStr(nativeString);
		env->ReleaseStringUTFChars(value, nativeString);
		participants.push_back(participantStr);
	}

	outgoingSession->get()->setParticipants(participants);
	return webrtc_jni::jlongFromPointer(outgoingSession);
}


JNIEXPORT void JNICALL Java_com_twilio_signal_impl_ConversationImpl_start
  (JNIEnv *env, jobject obj, jlong nativeSession)
{
	TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "start");
	TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
	session->get()->start();
}


JNIEXPORT void JNICALL Java_com_twilio_signal_impl_ConversationImpl_stop
  (JNIEnv *env, jobject obj, jlong nativeSession)
{
	TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "stop");
	TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
	session->get()->stop();
}

JNIEXPORT void JNICALL Java_com_twilio_signal_impl_ConversationImpl_setExternalCapturer
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

JNIEXPORT void JNICALL Java_com_twilio_signal_impl_ConversationImpl_setSessionObserver
  (JNIEnv *, jobject, jlong nativeSession, jlong nativeSessionObserver)
{
	TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "setSessionObserver");
	TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
	TSCSessionObserverPtr *sessionObserver = reinterpret_cast<TSCSessionObserverPtr *>(nativeSessionObserver);
	session->get()->setSessionObserver(*sessionObserver);
}

JNIEXPORT jboolean JNICALL Java_com_twilio_signal_impl_ConversationImpl_enableVideo
  (JNIEnv *, jobject, jlong nativeSession, jboolean enabled, jboolean paused)
{
	TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "enableVideo");
	TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
	return (session->get()->enableVideo((bool)enabled, (bool)paused) ? JNI_TRUE : JNI_FALSE);
}

JNIEXPORT void JNICALL Java_com_twilio_signal_impl_ConversationImpl_freeNativeHandle
  (JNIEnv *env, jobject obj, jlong nativeSession) {
	TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "freeNativeHandle: Session");
	TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
	if (session != nullptr) {
	    session->reset();
	    delete session;
	}
}

JNIEXPORT jboolean JNICALL Java_com_twilio_signal_impl_ConversationImpl_mute
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

JNIEXPORT jboolean JNICALL Java_com_twilio_signal_impl_ConversationImpl_isMuted
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
