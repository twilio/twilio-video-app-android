#include "com_twilio_signal_impl_ConversationImpl.h"
#include "TSCoreSDKTypes.h"
#include "TSCoreError.h"
#include "TSCEndpoint.h"
#include "TSCSessionObserver.h"
#include "TSCOutgoingSession.h"
#include "TSCParticipant.h"
#include <twilio-jni/twilio-jni.h>
#include <android/log.h>

#include <string>
#include <map>
#include <vector>

using namespace twiliosdk;

#define TAG  "SignalCore(native)"

JNIEXPORT jlong JNICALL Java_com_twilio_signal_impl_ConversationImpl_wrapOutgoingSession
  (JNIEnv *env, jobject obj, jlong nativeEndpoint, jlong nativeSessionObserver, jobjectArray participantList)
{
	TSCEndpointObject* endpoint = reinterpret_cast<TSCEndpointObject*>(nativeEndpoint);
	TSCOptions options;
	options.insert(std::pair<std::string,std::string>("audio","yes"));
	options.insert(std::pair<std::string,std::string>("video","yes"));
	TSCOutgoingSessionObjectRef outgoingSession = endpoint->createSession(options, reinterpret_cast<TSCSessionObserverObject*>(nativeSessionObserver));
	if (outgoingSession.get() == NULL) {
		return 0;
	}
	int size = env->GetArrayLength(participantList);
	if (size == 0) {
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
	TSCOptions options2;
	outgoingSession->start(options2);

	return (jlong)outgoingSession.release();
}
