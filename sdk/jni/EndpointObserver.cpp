#include "EndpointObserver.h"
#include "com_twilio_signal_impl_SignalCore.h"

using namespace twiliosdk;

EndpointObserver::EndpointObserver(JNIEnv* env, jobject config)
{
	this->m_config = env->NewGlobalRef(config);
};

EndpointObserver::~EndpointObserver(){
	assert(this->m_config == NULL);
};

void EndpointObserver::destroy(JNIEnv* env) {

	__android_log_print(ANDROID_LOG_VERBOSE, "JNI SIGNAL", "EndpointObserver::destroy() called", 1);

     env->DeleteGlobalRef(this->m_config);
     this->m_config = NULL;
}


void EndpointObserver::setEndpoint(TSCEndpointObjectRef endpointObj) {

	this->endpoint = endpointObj;
}



void EndpointObserver::onRegistrationDidComplete(TSCErrorObject* error)
{
	__android_log_print(ANDROID_LOG_VERBOSE, "JNI SIGNAL", "Received notification:onRegistrationDidComplete", 1);

	JNIEnvAttacher attacher;
	JNIEnv *g_env = attacher.get();

	jobject callbacks = tw_jni_fetch_object(g_env, m_config, "callbacks", "Lcom/twilio/signal/impl/SignalCoreConfig$Callbacks;");
	jmethodID meth = tw_jni_get_method(g_env, callbacks, "onRegistrationComplete","()V");
	//jmethodID meth = tw_jni_get_method(g_env, callbacks, "onRegistrationComplete","(Lcom/twilio/signal/Endpoint;)V");
	g_env->CallVoidMethod(callbacks, meth);

}

void EndpointObserver::onUnregistrationDidComplete(TSCErrorObject* error)
{
	__android_log_print(ANDROID_LOG_VERBOSE, "JNI SIGNAL", "Received notification:onUnregistrationDidComplete", 1);
	JNIEnvAttacher attacher;
	JNIEnv *g_env = attacher.get();

	jobject callbacks = tw_jni_fetch_object(g_env, m_config, "callbacks", "Lcom/twilio/signal/impl/SignalCoreConfig$Callbacks;");
	jmethodID meth = tw_jni_get_method(g_env, callbacks, "onUnRegistrationComplete","()V");
	//jmethodID meth = tw_jni_get_method(g_env, callbacks, "onUnRegistrationComplete","(Lcom/twilio/signal/Endpoint;)V");
	g_env->CallVoidMethod(callbacks, meth);

}

void EndpointObserver::onStateDidChange(TSCEndpointState state) {
	__android_log_print(ANDROID_LOG_VERBOSE, "JNI SIGNAL", "Received notification:onStateDidChange", 1);
}


void EndpointObserver::onIncomingCallDidReceive(TSCIncomingSession* session) {
	__android_log_print(ANDROID_LOG_VERBOSE, "JNI SIGNAL", "Received notification:onIncomingCallDidReceive", 1);

	JNIEnvAttacher attacher;
	JNIEnv *g_env = attacher.get();

	jobject callbacks = tw_jni_fetch_object(g_env, m_config, "callbacks", "Lcom/twilio/signal/impl/SignalCoreConfig$Callbacks;");

	__android_log_print(ANDROID_LOG_VERBOSE, "JNI SIGNAL", "callbacks is not null notification:onIncomingCallDidReceive", 1);
	jmethodID meth = tw_jni_get_method(g_env, callbacks, "onIncomingCall","()V");
	//jmethodID meth = tw_jni_get_method(g_env, callbacks, "onIncomingCall","(Lcom/twilio/signal/Endpoint;)V");


	__android_log_print(ANDROID_LOG_VERBOSE, "JNI SIGNAL", "meth is not null notification:onIncomingCallDidReceive", 1);

	g_env->CallVoidMethod(callbacks, meth);

	__android_log_print(ANDROID_LOG_VERBOSE, "JNI SIGNAL", "accepting the call", 1);


	TSCIncomingSessionObjectRef incomingSession = dynamic_cast<TSCIncomingSessionObject*>(session);
	this->endpoint.get()->accept(incomingSession);

	//TSCIncomingSessionObjectRef sessionRef;
	//sessionRef.reset(session);
	//this->endpoint.get()->accept(TSCIncomingSessionObjectRef(session));
}




