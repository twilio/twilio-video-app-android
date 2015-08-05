#include "com_twilio_signal_impl_ConversationImpl_SessionObserverInternal.h"
#include "TSCoreSDKTypes.h"
#include "TSCoreError.h"
#include "TSCSession.h"
#include "TSCSessionObserver.h"
#include <twilio-jni/twilio-jni.h>
#include <android/log.h>

using namespace twiliosdk;

#define TAG  "TwilioSDK(native)"

class SessionObserverInternalWrapper : public TSCSessionObserverObject {
public:
	SessionObserverInternalWrapper(JNIEnv* env, jobject obj, jobject listener, jobject endpoint) {

	}
protected:
	virtual void onDidReceiveEvent(const TSCEventObjectRef& event) {
		__android_log_print(ANDROID_LOG_VERBOSE, TAG, "onDidReceiveEvent");
	}

	virtual void onStateDidChange(TSCSessionState state) {
		__android_log_print(ANDROID_LOG_VERBOSE, TAG, "onStateDidChange");
	}

	virtual void onStartDidComplete(const TSCErrorObjectRef& error) {
		__android_log_print(ANDROID_LOG_VERBOSE, TAG, "onStartDidComplete");
	}
	virtual void onStopDidComplete(const TSCErrorObjectRef& error) {
		__android_log_print(ANDROID_LOG_VERBOSE, TAG, "onStopDidComplete");
	}

	virtual void onParticipantDidConnect(const TSCParticipantObjectRef& participant,
										 const TSCErrorObjectRef& error) {
		__android_log_print(ANDROID_LOG_VERBOSE, TAG, "onParticipantDidConnect");
	}
	virtual void onParticipantDidDisconect(const TSCParticipantObjectRef& participant,
										   TSCDisconnectReason reason) {
		__android_log_print(ANDROID_LOG_VERBOSE, TAG, "onParticipantDidDisconect");
	}

	virtual void onMediaStreamDidAdd(TSCMediaStreamInfoObject* stream) {
		__android_log_print(ANDROID_LOG_VERBOSE, TAG, "onMediaStreamDidAdd");
	}
	virtual void onMediaStreamDidRemove(TSCMediaStreamInfoObject* stream) {
		__android_log_print(ANDROID_LOG_VERBOSE, TAG, "onMediaStreamDidRemove");
	}

	virtual void onDidReceiveSessionStatistics(TSCSessionStatisticsObject* statistics) {
		__android_log_print(ANDROID_LOG_VERBOSE, TAG, "onDidReceiveSessionStatistics");
	}
};



/*
 * Class:     com_twilio_signal_impl_ConversationImpl_SessionObserverInternal
 * Method:    wrapNativeObserver
 * Signature: (Lcom/twilio/signal/ConversationListener;Lcom/twilio/signal/Endpoint;)J
 */
JNIEXPORT jlong JNICALL Java_com_twilio_signal_impl_ConversationImpl_00024SessionObserverInternal_wrapNativeObserver
  (JNIEnv *env, jobject obj, jobject listener, jobject endpoint) {
	TSCSessionObserverObjectRef sessionObserver =
				TSCSessionObserverObjectRef(new SessionObserverInternalWrapper(env, obj, listener, endpoint));
	return (jlong)sessionObserver.release();
}

/*
 * Class:     com_twilio_signal_impl_ConversationImpl_SessionObserverInternal
 * Method:    freeNativeObserver
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_twilio_signal_impl_ConversationImpl_00024SessionObserverInternal_freeNativeObserver
  (JNIEnv *, jobject obj, jlong nativeSession){

}
