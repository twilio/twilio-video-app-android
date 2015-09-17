#include <twilio-jni/twilio-jni.h>
#include "webrtc/modules/utility/interface/helpers_android.h"

#include "com_twilio_signal_impl_ConversationImpl_SessionObserverInternal.h"
#include "TSCoreSDKTypes.h"
#include "TSCoreError.h"
#include "TSCSession.h"
#include "TSCLogger.h"
#include "TSCSessionObserver.h"
#include "TSCMediaStreamInfo.h"

using namespace webrtc;
using namespace twiliosdk;


class SessionObserverInternalWrapper : public TSCSessionObserverObject {
public:
	SessionObserverInternalWrapper(JNIEnv* jni, jobject obj, jobject j_observer, jobject conversation)
			: j_participant_did_connect_id(tw_jni_get_method(jni, j_observer, "onConnectParticipant", "(Ljava/lang/String;)V")),
			j_video_added_for_participant_id(tw_jni_get_method(jni, j_observer, "onVideoAddedForParticipant", "(Ljava/lang/String;)V")),
			j_observer_global_(jni, j_observer),	
			j_observer_class_(jni, jni->GetObjectClass(*j_observer_global_)) {

	}
protected:
	virtual void onDidReceiveEvent(const TSCEventObjectRef& event) {
		TS_CORE_LOG_DEBUG("onDidReceiveEvent");
	}

	virtual void onStateDidChange(TSCSessionState state) {
		TS_CORE_LOG_DEBUG("onStateDidChange");
	}

	virtual void onStartDidComplete(const TSCErrorObjectRef& error) {
		TS_CORE_LOG_DEBUG("onStartDidComplete");
	}
	virtual void onStopDidComplete(const TSCErrorObjectRef& error) {
		TS_CORE_LOG_DEBUG("onStopDidComplete");
	}

	virtual void onParticipantDidConnect(const TSCParticipantObjectRef& participant,
										 const TSCErrorObjectRef& error) {
		TS_CORE_LOG_DEBUG("onParticipantDidConnect");
	    	JNIEnvAttacher jniAttacher;
    		jstring j_participant_address = stringToJString(jniAttacher.get(), participant->getAddress());
    		jniAttacher.get()->CallVoidMethod(*j_observer_global_, j_participant_did_connect_id, j_participant_address);
	}

	virtual void onParticipantDidDisconect(const TSCParticipantObjectRef& participant,
										   TSCDisconnectReason reason) {
		TS_CORE_LOG_DEBUG("onParticipantDidDisconect");
	}

	virtual void onMediaStreamDidAdd(TSCMediaStreamInfoObject* stream) {
		TS_CORE_LOG_DEBUG("onMediaStreamDidAdd");
	    	JNIEnvAttacher jniAttacher;

    		jstring j_participant_address = stringToJString(jniAttacher.get(), stream->getParticipantAddress());
    		jniAttacher.get()->CallVoidMethod(*j_observer_global_, j_video_added_for_participant_id, j_participant_address);
	}

	virtual void onMediaStreamDidRemove(TSCMediaStreamInfoObject* stream) {
		TS_CORE_LOG_DEBUG("onMediaStreamDidRemove");
	}

	virtual void onDidReceiveSessionStatistics(TSCSessionStatisticsObject* statistics) {
		TS_CORE_LOG_DEBUG("onDidReceiveSessionStatistics");
	}

private:

	jstring stringToJString(JNIEnv * env, const std::string & nativeString) {
		return env->NewStringUTF(nativeString.c_str());
	}



	const jmethodID j_participant_did_connect_id;
	const jmethodID j_video_added_for_participant_id;
	const ScopedGlobalRef<jobject> j_observer_global_;
	const ScopedGlobalRef<jclass> j_observer_class_;
};



/*
 * Class:     com_twilio_signal_impl_ConversationImpl_SessionObserverInternal
 * Method:    wrapNativeObserver
 * Signature: (Lcom/twilio/signal/ConversationObserver;Lcom/twilio/signal/Conversation;)J
 */
JNIEXPORT jlong JNICALL Java_com_twilio_signal_impl_ConversationImpl_00024SessionObserverInternal_wrapNativeObserver
  (JNIEnv *env, jobject obj, jobject observer, jobject conversation) {
	TS_CORE_LOG_DEBUG("wrapNativeObserver");
  	rtc::scoped_ptr<SessionObserverInternalWrapper> so(
		new SessionObserverInternalWrapper(env, obj, observer, conversation)
	);
  	return (jlong)so.release();
}

/*
 * Class:     com_twilio_signal_impl_ConversationImpl_SessionObserverInternal
 * Method:    freeNativeObserver
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_twilio_signal_impl_ConversationImpl_00024SessionObserverInternal_freeNativeObserver
  (JNIEnv *, jobject obj, jlong nativeSession){

}
