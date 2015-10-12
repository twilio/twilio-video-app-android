#include "com_twilio_signal_impl_EndpointImpl_EndpointObserverInternal.h"
#include "webrtc/modules/utility/interface/helpers_android.h"
#include "talk/app/webrtc/java/jni/jni_helpers.h"
#include "TSCoreSDKTypes.h"
#include "TSCoreError.h"
#include "TSCEndpoint.h"
#include "TSCEndpointObserver.h"
#include "TSCSession.h"
#include "TSCLogger.h"
#include <twilio-jni/twilio-jni.h>
#include <android/log.h>

using namespace webrtc;
using namespace twiliosdk;

#define TAG  "TwilioSDK(native)"

class EndpointObserverInternalWrapper: public TSCEndpointObserverObject
{
public:
	EndpointObserverInternalWrapper(JNIEnv* env,jobject obj, jobject j_endpoint_observer, jobject j_endpoint)
		: j_registration_complete_(
				tw_jni_get_method(env, j_endpoint_observer, "onRegistrationDidComplete", "(Lcom/twilio/signal/impl/core/CoreError;)V")),
		  j_unreg_complete_(
				tw_jni_get_method(env, j_endpoint_observer, "onUnregistrationDidComplete", "(Lcom/twilio/signal/impl/core/CoreError;)V")),
		  j_state_change_(
				tw_jni_get_method(env, j_endpoint_observer, "onStateDidChange", "(Lcom/twilio/signal/impl/core/EndpointState;)V")),
		  j_incoming_call_(
				tw_jni_get_method(env, j_endpoint_observer, "onIncomingCallDidReceive", "(J[Ljava/lang/String;)V")),
		  j_statetype_enum_(
				env, FindClass(env, "com/twilio/signal/impl/core/EndpointState")),
		j_errorimpl_class_(
				env, FindClass(env, "com/twilio/signal/impl/core/CoreErrorImpl")),
		j_errorimpl_ctor_id_(
				GetMethodID( env, *j_errorimpl_class_, "<init>", "(Ljava/lang/String;ILjava/lang/String;)V")){
		j_endpoint_observer_ = env->NewGlobalRef(j_endpoint_observer);
		j_endpoint_ = env->NewGlobalRef(j_endpoint);
	}

    virtual ~EndpointObserverInternalWrapper() {
    	if (j_endpoint_observer_ != NULL) {
    		//TODO - we should probably notify jobject that native handle is being destroyed
    		env_->DeleteGlobalRef(j_endpoint_observer_);
    		j_endpoint_observer_ = NULL;
    	}
    	if (j_endpoint_ != NULL) {
    		env_->DeleteGlobalRef(j_endpoint_);
    		j_endpoint_ = NULL;
    	}

    }


protected:
    virtual void onRegistrationDidComplete(TSCErrorObject* error) {
    	JNIEnvAttacher jniAttacher;
    	TS_CORE_LOG_DEBUG("onRegistrationDidComplete");
    	/*
    	if (error != NULL) {
    		jstring str = stringToJString(jniAttacher.get(), error->getMessage());
    		jniAttacher.get()->CallVoidMethod(j_endpoint_observer_, j_state_change_, j_endpoint_, (jint)error->getCode(), str);
    		return;
    	}*/
    	jobject j_error = errorToJavaCoreErrorImpl(error);
    	jniAttacher.get()->CallVoidMethod(j_endpoint_observer_, j_registration_complete_, j_error);

    }
    virtual void onUnregistrationDidComplete(TSCErrorObject* error) {
    	JNIEnvAttacher jniAttacher;
    	TS_CORE_LOG_DEBUG("onUnregistrationDidComplete");
    	jobject j_error = errorToJavaCoreErrorImpl(error);
		jniAttacher.get()->CallVoidMethod(j_endpoint_observer_, j_unreg_complete_, j_error);
    }
    virtual void onStateDidChange(TSCEndpointState state){
    	JNIEnvAttacher jniAttacher;
    	TS_CORE_LOG_DEBUG("onStateDidChange");
    	const std::string state_type_enum = "com/twilio/signal/impl/core/EndpointState";
		jobject j_state_type =
				webrtc_jni::JavaEnumFromIndex(jniAttacher.get(),
						*j_statetype_enum_, state_type_enum, state);
		jniAttacher.get()->CallVoidMethod(j_endpoint_observer_, j_state_change_, j_state_type);
    }
    virtual void onIncomingCallDidReceive(TSCSession* session) {
    	JNIEnvAttacher jniAttacher;
    	TS_CORE_LOG_DEBUG("onIncomingCallDidReceive");
    	jlong j_session_id = webrtc_jni::jlongFromPointer(session);

    	//Get participants from session and put them into java string array
    	jobjectArray j_participants =
    			partToJavaPart(jniAttacher.get(), session->getParticipants());

    	jniAttacher.get()->CallVoidMethod(
    			j_endpoint_observer_, j_incoming_call_, j_session_id, j_participants);
    }


private:

    jstring stringToJString(JNIEnv * env, const std::string & nativeString) {
        return env->NewStringUTF(nativeString.c_str());
    }

    // Return a ErrorImpl
	jobject errorToJavaCoreErrorImpl(const TSCErrorObject* error) {
		JNIEnvAttacher jniAttacher;
		if (!error) {
			return NULL;
		}
		jstring j_domain = stringToJString(jniAttacher.get(), error->getDomain());
		jint j_error_id = (jint)error->getCode();
		jstring j_message = stringToJString(jniAttacher.get(), error->getMessage());
		return jniAttacher.get()->NewObject(
				*j_errorimpl_class_, j_errorimpl_ctor_id_,
				j_domain, j_error_id, j_message);
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

    //TODO - find better way to track life time of global reference
    jobject j_endpoint_observer_;
    jobject j_endpoint_;
    jmethodID j_registration_complete_;
    jmethodID j_unreg_complete_;
    jmethodID j_state_change_;
    jmethodID j_incoming_call_;
    JNIEnv* env_;
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
	TSCEndpointObserverObjectRef endpointObserver =
				TSCEndpointObserverObjectRef(new EndpointObserverInternalWrapper(env, obj, j_endpoint_observer, j_endpoint));
		return (jlong)endpointObserver.release();
}

/*
 * Class:     com_twilio_signal_impl_EndpointImpl_EndpointObserverInternal
 * Method:    freeNativeObserver
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_twilio_signal_impl_EndpointImpl_00024EndpointObserverInternal_freeNativeObserver
  (JNIEnv *, jobject, jlong);


