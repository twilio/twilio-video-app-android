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
     env->DeleteGlobalRef(this->m_config);
     this->m_config = NULL;
   }

void EndpointObserver::onRegistrationDidComplete(TSCErrorObject* error)
{
	__android_log_print(ANDROID_LOG_VERBOSE, "JNI SIGNAL", "Received notification: onRegistrationDidComplete", 1);

	JNIEnvAttacher attacher;
	JNIEnv *g_env = attacher.get();

	jobject callbacks = tw_jni_fetch_object(g_env, m_config, "callbacks", "Lcom/twilio/signal/impl/SignalCoreConfig$Callbacks;");
	jmethodID meth = tw_jni_get_method(g_env, callbacks, "onRegistrationComplete","()V");
	g_env->CallVoidMethod(callbacks, meth);

}

void EndpointObserver::onUnregistrationDidComplete(TSCErrorObject* error)
{
	__android_log_print(ANDROID_LOG_VERBOSE, "JNI SIGNAL", "onUnregistrationDidComplete", 1);
	JNIEnvAttacher attacher;
	JNIEnv *g_env = attacher.get();

	jobject callbacks = tw_jni_fetch_object(g_env, m_config, "callbacks", "Lcom/twilio/signal/impl/SignalCoreConfig$Callbacks;");
	jmethodID meth = tw_jni_get_method(g_env, callbacks, "onUnRegistrationComplete","()V");
	g_env->CallVoidMethod(callbacks, meth);

}

void EndpointObserver::onStateDidChange(TSCEndpointState state) {
	__android_log_print(ANDROID_LOG_VERBOSE, "JNI SIGNAL", "onStateDidChange", 1);
}


void EndpointObserver::onIncomingCallDidReceive(TSCIncomingSession* session) {
	__android_log_print(ANDROID_LOG_VERBOSE, "JNI SIGNAL", "onIncomingCallDidReceive", 1);
}




