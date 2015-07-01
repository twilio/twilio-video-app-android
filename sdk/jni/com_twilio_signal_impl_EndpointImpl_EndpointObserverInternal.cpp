#include "com_twilio_signal_impl_EndpointImpl_EndpointObserverInternal.h"
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
	EndpointObserverInternalWrapper(JNIEnv* env,jobject obj, jobject j_endpoint_listener)
		: j_start_listening_id_(tw_jni_get_method(env, j_endpoint_listener, "onStartListeningForInvites", "(Lcom/twilio/signal/Endpoint;)V")),
		  j_stop_listening_id_(tw_jni_get_method(env, j_endpoint_listener, "onStopListeningForInvites", "(Lcom/twilio/signal/Endpoint;)V")),
		  j_failed_to_start_id_(tw_jni_get_method(env, j_endpoint_listener, "onFailedToStartListening", "(Lcom/twilio/signal/Endpoint;ILjava/lang/String;)V")),
		  j_receive_conv_id_(tw_jni_get_method(env, j_endpoint_listener, "onReceiveConversationInvite", "(Lcom/twilio/signal/Endpoint;Lcom/twilio/signal/Invite;)V")) {
		j_endpoint_listener_ = env->NewGlobalRef(j_endpoint_listener);
	}

    virtual ~EndpointObserverInternalWrapper() {
    	if (j_endpoint_listener_ != NULL) {
    		//TODO - we should probably notify jobject that native handle is being destroyed
    		env_->DeleteGlobalRef(j_endpoint_listener_);
    		j_endpoint_listener_ = NULL;
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
    jobject j_endpoint_listener_;
    jmethodID j_start_listening_id_;
    jmethodID j_stop_listening_id_;
    jmethodID j_failed_to_start_id_;
    jmethodID j_receive_conv_id_;
    JNIEnv* env_;
};

/*
 * Class:     com_twilio_signal_impl_EndpointImpl_EndpointObserverInternal
 * Method:    wrapNativeObserver
 * Signature: (Lcom/twilio/signal/EndpointListener;)J
 */
JNIEXPORT jlong JNICALL Java_com_twilio_signal_impl_EndpointImpl_00024EndpointObserverInternal_wrapNativeObserver
  (JNIEnv *env, jobject obj, jobject j_endpoint_listener) {
	TSCEndpointObserverObjectRef endpointObserver =
				TSCEndpointObserverObjectRef(new EndpointObserverInternalWrapper(env, obj, j_endpoint_listener));
		return (jlong)endpointObserver.release();
}

/*
 * Class:     com_twilio_signal_impl_EndpointImpl_EndpointObserverInternal
 * Method:    freeNativeObserver
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_twilio_signal_impl_EndpointImpl_00024EndpointObserverInternal_freeNativeObserver
  (JNIEnv *, jobject, jlong);


