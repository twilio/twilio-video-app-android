#include "com_twilio_signal_impl_ConversationImpl.h"
#include "TSCoreSDKTypes.h"
#include "TSCoreError.h"
#include "TSCEndpoint.h"
#include "TSCSessionObserver.h"
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
  (JNIEnv *env, jobject obj, jlong nativeEndpoint, jlong nativeSessionObserver, jobjectArray participantList, jobjectArray surfaces)
{

	jsize len = env->GetArrayLength(surfaces);
	ANativeWindow **windows = new ANativeWindow*[len];

	if (surfaces != 0) {
	 	// Intentionally skipping the first window to use it inside the core. 
		for(int i = 0; i < len; i++) {
			ANativeWindow* window = ANativeWindow_fromSurface(env, env->GetObjectArrayElement(surfaces, i));
			__android_log_print(ANDROID_LOG_DEBUG, TAG, "Got window %p", window);
			windows[i] = window;		
		}
    	}

	__android_log_print(ANDROID_LOG_DEBUG, TAG, "wrapOutgoingSession");
	TSCEndpointObject* endpoint = reinterpret_cast<TSCEndpointObject*>(nativeEndpoint);
	__android_log_print(ANDROID_LOG_DEBUG, TAG, "wrapOutgoingSession 1");
	TSCOptions options;
	options.insert(std::pair<std::string,std::string>("audio","yes"));
	options.insert(std::pair<std::string,std::string>("video","yes"));
	__android_log_print(ANDROID_LOG_DEBUG, TAG, "wrapOutgoingSession 2");

	TSCSessionObserverObjectRef observer =
			TSCSessionObserverObjectRef(reinterpret_cast<TSCSessionObserverObject*>(nativeSessionObserver));
	__android_log_print(ANDROID_LOG_DEBUG, TAG, "wrapOutgoingSession 4");
	TSCOutgoingSessionObjectRef outgoingSession = endpoint->createSession(options, observer);
	__android_log_print(ANDROID_LOG_DEBUG, TAG, "wrapOutgoingSession 5");
	if (outgoingSession.get() == NULL) {
		__android_log_print(ANDROID_LOG_DEBUG, TAG, "wrapOutgoingSession 6");
		return 0;
	}
	int size = env->GetArrayLength(participantList);
	__android_log_print(ANDROID_LOG_DEBUG, TAG, "wrapOutgoingSession 7");
	if (size == 0) {
		__android_log_print(ANDROID_LOG_DEBUG, TAG, "wrapOutgoingSession 8");
		return 0;
	}
	std::vector<TSCParticipant> participants;
	for (int i=0; i < size; i++) {
		jstring value = (jstring)env->GetObjectArrayElement(participantList, i);
		__android_log_print(ANDROID_LOG_DEBUG, TAG, "wrapOutgoingSession 9");
		const char *nativeString = env->GetStringUTFChars(value, 0);
		std::string participantStr(nativeString);
		__android_log_print(ANDROID_LOG_DEBUG, TAG, "wrapOutgoingSession 10: %s",nativeString);
		env->ReleaseStringUTFChars(value, nativeString);
		TSCParticipant participant(participantStr);
		participants.push_back(participant);
		__android_log_print(ANDROID_LOG_DEBUG, TAG, "wrapOutgoingSession 11");

	}

	outgoingSession->setWindows(windows);
	__android_log_print(ANDROID_LOG_DEBUG, TAG, "wrapOutgoingSession 12");
	outgoingSession->setParticipants(participants);
	TSCOptions options2;
	outgoingSession->start(options2);
	__android_log_print(ANDROID_LOG_DEBUG, TAG, "wrapOutgoingSession 13");
	return (jlong)outgoingSession.release();
}
