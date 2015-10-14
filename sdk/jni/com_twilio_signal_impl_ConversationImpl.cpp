#include "com_twilio_signal_impl_ConversationImpl.h"
#include "TSCoreSDKTypes.h"
#include "TSCoreError.h"
#include "TSCLogger.h"
#include "TSCEndpoint.h"
#include "TSCSessionObserver.h"
#include "TSCVideoCaptureController.h"
#include "TSCSession.h"
#include "TSCParticipant.h"
#include <twilio-jni/twilio-jni.h>
#include <android/log.h>

#include <string>
#include <map>
#include <vector>


using namespace twiliosdk;

#define TAG  "TwilioSDK(native)"


JNIEXPORT jlong JNICALL Java_com_twilio_signal_impl_ConversationImpl_wrapOutgoingSession
  (JNIEnv *env, jobject obj, jlong nativeEndpoint, jlong nativeSessionObserver, jobjectArray participantList)
{

	TSCEndpointObject* endpoint = reinterpret_cast<TSCEndpointObject*>(nativeEndpoint);
	TSCOptions options;
	options.insert(std::pair<std::string,std::string>("audio","yes"));
	options.insert(std::pair<std::string,std::string>("video","yes"));

	TSCSessionObserverObjectRef sessionObserver =
			TSCSessionObserverObjectRef(reinterpret_cast<TSCSessionObserverObject*>(nativeSessionObserver));
	if (sessionObserver.get() == NULL) {
		TS_CORE_LOG_DEBUG("sessionObserver was null. Exiting");
		return 0;
	}

	TSCOutgoingSessionObjectRef outgoingSession = endpoint->createSession(options, sessionObserver);

	if (outgoingSession.get() == NULL) {
		TS_CORE_LOG_DEBUG("outgoingSession was null. Exiting");
		return 0;
	}

	int size = env->GetArrayLength(participantList);
	if (size == 0) {
		TS_CORE_LOG_DEBUG("no participants were provided");
		return 0;
	}

	std::vector<TSCParticipant> participants;
	for (int i=0; i < size; i++) {
		jstring value = (jstring)env->GetObjectArrayElement(participantList, i);
		const char *nativeString = env->GetStringUTFChars(value, 0);
		std::string participantStr(nativeString);
		env->ReleaseStringUTFChars(value, nativeString);
		TSCParticipant participant(participantStr);
		participants.push_back(participant);
	}

	outgoingSession->setParticipants(participants);
	return (jlong)outgoingSession.release();
}


JNIEXPORT void JNICALL Java_com_twilio_signal_impl_ConversationImpl_start
  (JNIEnv *env, jobject obj, jlong nativeSession)
{
	TS_CORE_LOG_DEBUG("start");
	TSCSessionObject* session = reinterpret_cast<TSCSessionObject*>(nativeSession);
	session->start();
}


JNIEXPORT void JNICALL Java_com_twilio_signal_impl_ConversationImpl_stop
  (JNIEnv *env, jobject obj, jlong nativeSession)
{
	TS_CORE_LOG_DEBUG("stop");
	TSCSessionObject* session = reinterpret_cast<TSCSessionObject*>(nativeSession);
	session->stop();
}


JNIEXPORT void JNICALL Java_com_twilio_signal_impl_ConversationImpl_setExternalCapturer
  (JNIEnv *env, jobject obj, jlong nativeSession, jlong nativeCapturer)
{
	TS_CORE_LOG_DEBUG("setExternalCapturer");
	TSCSessionObject* session = reinterpret_cast<TSCSessionObject*>(nativeSession);
	TSCVideoCaptureController* videoCaptureController = session->getVideoCaptureController();
	if(videoCaptureController != nullptr) {
		videoCaptureController->setExternalVideoCapturer(reinterpret_cast<cricket::VideoCapturer*>(nativeCapturer));
	} else {
		TS_CORE_LOG_DEBUG("videoCapturerController was null");
	}
}


