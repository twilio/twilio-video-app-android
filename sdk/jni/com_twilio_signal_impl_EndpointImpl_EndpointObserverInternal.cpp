#include "com_twilio_signal_impl_EndpointImpl_EndpointObserverInternal.h"
#include "talk/app/webrtc/java/jni/jni_helpers.h"
#include "TSCoreSDKTypes.h"
#include "TSCoreError.h"
#include "TSCEndpoint.h"
#include "TSCEndpointObserver.h"
#include "TSCSession.h"
#include "TSCLogger.h"
#include <android/log.h>

using namespace webrtc_jni;
using namespace twiliosdk;

#define TAG  "TwilioSDK(native)"

class EndpointObserverInternalWrapper: public TSCEndpointObserver
{
public:
	EndpointObserverInternalWrapper(JNIEnv* env,jobject obj, jobject j_endpoint_observer, jobject j_endpoint)
		:j_endpoint_observer_(env, j_endpoint_observer),
		 j_endpoint_(env, j_endpoint),
		 j_observer_class_(env, GetObjectClass(env, *j_endpoint_observer_)),
		 j_registration_complete_(
				 GetMethodID(env, *j_observer_class_, "onRegistrationDidComplete", "(Lcom/twilio/signal/impl/core/CoreError;)V")),
		  j_unreg_complete_(
				 GetMethodID(env, *j_observer_class_, "onUnregistrationDidComplete", "(Lcom/twilio/signal/impl/core/CoreError;)V")),
		  j_state_change_(
				 GetMethodID(env, *j_observer_class_, "onStateDidChange", "(Lcom/twilio/signal/impl/core/EndpointState;)V")),
		  j_incoming_call_(
				 GetMethodID(env, *j_observer_class_, "onIncomingCallDidReceive", "(J[Ljava/lang/String;)V")),
		  j_statetype_enum_(
				env, env->FindClass("com/twilio/signal/impl/core/EndpointState")),
		j_errorimpl_class_(
				env, env->FindClass("com/twilio/signal/impl/core/CoreErrorImpl")),
		j_errorimpl_ctor_id_(
				GetMethodID( env, *j_errorimpl_class_, "<init>", "(Ljava/lang/String;ILjava/lang/String;)V"))
		{}


protected:
    virtual void onRegistrationDidComplete(TSCoreErrorCode code, const std::string &message) {

    	TS_CORE_LOG_DEBUG("onRegistrationDidComplete");
        jobject j_error = errorToJavaCoreErrorImpl(code, message);
    	jni()->CallVoidMethod(*j_endpoint_observer_, j_registration_complete_, j_error);

    }

    virtual void onUnregistrationDidComplete(TSCoreErrorCode code, const std::string &message) {

    	TS_CORE_LOG_DEBUG("onUnregistrationDidComplete");
        jobject j_error = errorToJavaCoreErrorImpl(code, message);
        jni()->CallVoidMethod(*j_endpoint_observer_, j_unreg_complete_, j_error);

    }
    virtual void onStateDidChange(TSCEndpointState state){

    	TS_CORE_LOG_DEBUG("onStateDidChange");
    	const std::string state_type_enum = "com/twilio/signal/impl/core/EndpointState";
		jobject j_state_type =
				webrtc_jni::JavaEnumFromIndex(jni(),
						*j_statetype_enum_, state_type_enum, state);
		jni()->CallVoidMethod(*j_endpoint_observer_, j_state_change_, j_state_type);

    }
    virtual void onIncomingCallDidReceive(TSCSession* session) {

    	TS_CORE_LOG_DEBUG("onIncomingCallDidReceive");
    	jlong j_session_id = webrtc_jni::jlongFromPointer(session);

    	//Get participants from session and put them into java string array
    	jobjectArray j_participants =
    			partToJavaPart(jni(), session->getParticipants());

    	jni()->CallVoidMethod(
    			*j_endpoint_observer_, j_incoming_call_, j_session_id, j_participants);

    }


private:
    JNIEnv* jni() {
    	return AttachCurrentThreadIfNeeded();
    }

    jstring stringToJString(JNIEnv * env, const std::string & nativeString) {
        return env->NewStringUTF(nativeString.c_str());
    }

    // Return a ErrorImpl
    jobject errorToJavaCoreErrorImpl(TSCoreErrorCode code, const std::string &message) {
        jstring j_domain = stringToJString(jni(), "signal.coresdk.domain.error");
        jint j_error_id = (jint)code;
        jstring j_message = stringToJString(jni(), message);
        return jni()->NewObject(
		*j_errorimpl_class_, j_errorimpl_ctor_id_, j_domain, j_error_id, j_message);
    }

    // Return Java array of participants
    jobjectArray partToJavaPart(JNIEnv *env, const std::vector<TSCParticipant> participants) {
        int size = participants.size();
        if (size == 0) {
            return NULL;
        }
        jobjectArray j_participants = (jobjectArray)env->NewObjectArray(
                            size,
                            env->FindClass("java/lang/String"),
                            env->NewStringUTF(""));
        for (int i=0; i<size; i++) {
            env->SetObjectArrayElement(
                            j_participants, i, stringToJString(env, participants[i].getAddress()));
        }
        return j_participants;
    }

    const ScopedGlobalRef<jobject> j_endpoint_observer_;
    const ScopedGlobalRef<jobject> j_endpoint_;
    const ScopedGlobalRef<jclass> j_observer_class_;
    jmethodID j_registration_complete_;
    jmethodID j_unreg_complete_;
    jmethodID j_state_change_;
    jmethodID j_incoming_call_;
    const ScopedGlobalRef<jclass> j_statetype_enum_;
    const ScopedGlobalRef<jclass> j_errorimpl_class_;
    const jmethodID j_errorimpl_ctor_id_;


};

/*
 * Class:     com_twilio_signal_impl_EndpointImpl_EndpointObserverInternal
 * Method:    wrapNativeObserver
 * Signature: (Lcom/twilio/signal/impl/core/EndpointObserver;Lcom/twilio/signal/Endpoint;)J
 */
JNIEXPORT jlong JNICALL Java_com_twilio_signal_impl_EndpointImpl_00024EndpointObserverInternal_wrapNativeObserver
  (JNIEnv *env, jobject obj, jobject j_endpoint_observer, jobject j_endpoint) {
	TSCEndpointObserver* endpointObserver =
				new EndpointObserverInternalWrapper(env, obj, j_endpoint_observer, j_endpoint);
		return (jlong)endpointObserver;
}

/*
 * Class:     com_twilio_signal_impl_EndpointImpl_EndpointObserverInternal
 * Method:    freeNativeObserver
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_twilio_signal_impl_EndpointImpl_00024EndpointObserverInternal_freeNativeObserver
  (JNIEnv *, jobject, jlong){

}


