#include "com_twilio_signal_impl_ConversationImpl.h"
#include "TSCoreSDKTypes.h"
#include "TSCoreError.h"
#include "TSCEndpoint.h"
#include "TSCSessionObserver.h"
#include "TSCSession.h"
#include "TSCVideoSurface.h"
#include "TSCOutgoingSession.h"
#include "TSCParticipant.h"
#include <twilio-jni/twilio-jni.h>
#include <android/log.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <renderer.h>

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

	TSCSessionObserverObjectRef observer =
			TSCSessionObserverObjectRef(reinterpret_cast<TSCSessionObserverObject*>(nativeSessionObserver));
	TSCOutgoingSessionObjectRef outgoingSession = endpoint->createSession(options, observer);

	if (outgoingSession.get() == NULL) {
		__android_log_print(ANDROID_LOG_DEBUG, TAG, "outgoingSession was null. Exiting");
		return 0;
	}

	int size = env->GetArrayLength(participantList);
	if (size == 0) {
		__android_log_print(ANDROID_LOG_DEBUG, TAG, "no participants were provided");
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


JNIEXPORT void JNICALL Java_com_twilio_signal_impl_ConversationImpl_setVideoSurface
  (JNIEnv *env, jobject obj, jlong nativeSession, jlong nativeVideoSurface)
{
	__android_log_print(ANDROID_LOG_DEBUG, TAG, "setVideoSurface");
	TSCSessionObject* session = reinterpret_cast<TSCSessionObject*>(nativeSession);
	TSCVideoSurfaceObject* videoSurface = reinterpret_cast<TSCVideoSurfaceObject*>(nativeVideoSurface);
	session->setVideoSurface(videoSurface);	
}


JNIEXPORT void JNICALL Java_com_twilio_signal_impl_ConversationImpl_start
  (JNIEnv *env, jobject obj, jlong nativeSession)
{
	__android_log_print(ANDROID_LOG_DEBUG, TAG, "start");
	TSCSessionObject* session = reinterpret_cast<TSCSessionObject*>(nativeSession);
	session->start();	
}

