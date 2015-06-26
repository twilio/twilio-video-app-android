#include "com_twilio_signal_impl_EndpointListenerInternal.h"
#include "TSCoreSDKTypes.h"
#include "TSCEndpoint.h"
#include "TSCEndpointObserver.h"
#include <twilio-jni/twilio-jni.h>
#include <android/log.h>

using namespace twiliosdk;

#define TAG  "SignalCore(native)"

class EndpointObserverInternalWrapper: public TSCEndpointObserverObject
{
public:
	EndpointObserverInternalWrapper(JNIEnv* env,jobject obj, jobject j_endpoint_observer)
		: j_start_listening_id_(tw_jni_get_method(env, j_endpoint_observer, "onStartListeningForInvites", "(Lcom/twilio/signal/Endpoint;)V")),
		  j_stop_listening_id_(tw_jni_get_method(env, j_endpoint_observer, "onStopListeningForInvites", "(Lcom/twilio/signal/Endpoint;)V")),
		  j_failed_to_start_id_(tw_jni_get_method(env, j_endpoint_observer, "onFailedToStartListening", "(Lcom/twilio/signal/Endpoint;ILjava/lang/String;)V")),
		  j_receive_conv_id_(tw_jni_get_method(env, j_endpoint_observer, "onReceiveConversationInvite", "(Lcom/twilio/signal/Endpoint;Lcom/twilio/signal/Invite;)V"))
	{
		j_endpoint_observer_ = env->NewGlobalRef(j_endpoint_observer);
	}

    virtual ~EndpointObserverInternalWrapper()
    {
    	if (j_endpoint_observer_ != NULL) {

    		env_->DeleteGlobalRef(j_endpoint_observer_);
    		j_endpoint_observer_ = NULL;
    	}
    }


protected:
    virtual void onRegistrationDidComplete(TSCErrorObject* error) {
    	JNIEnvAttacher jniAttacher;
    	__android_log_print(ANDROID_LOG_VERBOSE, TAG, "onRegistrationDidComplete");
    }
    virtual void onUnregistrationDidComplete(TSCErrorObject* error) {
    	JNIEnvAttacher jniAttacher;
    	__android_log_print(ANDROID_LOG_VERBOSE, TAG, "onUnregistrationDidComplete");
    }
    virtual void onStateDidChange(TSCEndpointState state){
    	JNIEnvAttacher jniAttacher;
    	__android_log_print(ANDROID_LOG_VERBOSE, TAG, "onStateDidChange");
    }
    virtual void onIncomingCallDidReceive(TSCIncomingSession* session) {
    	JNIEnvAttacher jniAttacher;
    	__android_log_print(ANDROID_LOG_VERBOSE, TAG, "onIncomingCallDidReceive");
    }


private:
    //TODO - find better way to track life time of global reference
    jobject j_endpoint_observer_;
    jmethodID j_start_listening_id_;
    jmethodID j_stop_listening_id_;
    jmethodID j_failed_to_start_id_;
    jmethodID j_receive_conv_id_;
    JNIEnv* env_;
};


/*
 * Class:     com_twilio_signal_impl_EndpointListenerInternal
 * Method:    wrapNativeObserver
 * Signature: (Lcom/twilio/signal/EndpointListener;)J
 */
JNIEXPORT jlong JNICALL Java_com_twilio_signal_impl_EndpointListenerInternal_wrapNativeObserver
  (JNIEnv *env, jobject obj, jobject j_endpoint_listener) {
	TSCEndpointObserverObjectRef endpointObserver =
			TSCEndpointObserverObjectRef(new EndpointObserverInternalWrapper(env, obj, j_endpoint_listener));
	return (jlong)endpointObserver.release();
}

/*
 * Class:     com_twilio_signal_impl_EndpointListenerInternal
 * Method:    freeNativeObserver
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_twilio_signal_impl_EndpointListenerInternal_freeNativeObserver
  (JNIEnv *, jobject obj, jlong nativeEndpointObserver) {
	//delete reinterpret_cast<TSCEndpointObserverObject*>(nativeEndpointObserver);
}
