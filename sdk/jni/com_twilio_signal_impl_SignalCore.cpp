#include "TSCoreSDK.h"
#include <twilio-jni/twilio-jni.h>
#include <string.h>
#include <jni.h>
#include <android/log.h>

#include "TSCoreSDKTypes.h"
#include "TSCEndpoint.h"
#include "EndpointObserver.h"

#include "com_twilio_signal_impl_SignalCore.h"

#define TAG  "SignalCore(native)"

using namespace twiliosdk;

static jobject mainSignalCore = NULL;
static JavaVM * cachedJVM = NULL;
static std::map<const char* , TSCEndpointObjectRef> endpoints;

TSCSDK* tscSdk = NULL;

JNIEXPORT jstring JNICALL
Java_com_twilio_signal_impl_SignalCore_initCore(JNIEnv *env, jobject obj)
{
	if (mainSignalCore) {
		LOG_W(TAG, "SignalCore.initCore() double-called");
		__android_log_print(ANDROID_LOG_VERBOSE, TAG, "SignalCore.initCore() double-called", 1);
		tw_jni_throw(env, "java/lang/IllegalArgumentException", "SignalCore is already initialized");
	} else {
		mainSignalCore = env->NewGlobalRef(obj);
		env->GetJavaVM(&cachedJVM);
		tscSdk = TSCSDK::instance();
		__android_log_print(ANDROID_LOG_VERBOSE, TAG, "SignalCore.initCore() called", 1);
		LOG_W(TAG, "SignalCore.initCore() called");
	}

	return env->NewStringUTF("SignalCore initialized!");
}

JNIEXPORT jboolean JNICALL
Java_com_twilio_signal_impl_SignalCore_isCoreInitialized(JNIEnv *env, jobject obj)
{
	if (tscSdk != NULL && tscSdk->isInitialized())
	{
		return JNI_TRUE;
	}
	return JNI_FALSE;
}


JNIEXPORT jboolean JNICALL
Java_com_twilio_signal_impl_SignalCore_login(JNIEnv *env, jobject obj, jobjectArray credInfo, jobject config)
{
	if(tscSdk != NULL) {

		if (!credInfo) {
			LOG_W("SIGNAL", "Creating endpoint but no credentials specified");
			return true;
		 }
		__android_log_print(ANDROID_LOG_VERBOSE, TAG, "tscSdk  initialized hashmap", 1);

		jobject cred = env->GetObjectArrayElement(credInfo, 0);

		jstring tokenObj, surlObj, turlObj, userNameObj, passwordObj;
		const char *tokenStr = NULL, *surlStr = NULL, *turlStr = NULL, *userNameStr = NULL, *passwordStr = NULL;

		if (cred) {
			tokenStr = tw_jni_fetch_string(env, cred, "capabilityToken", &tokenObj);
			surlStr = tw_jni_fetch_string(env, cred, "stunURL", &surlObj);
			turlStr = tw_jni_fetch_string(env, cred, "turnURL", &turlObj);
			userNameStr = tw_jni_fetch_string(env, cred, "userName", &userNameObj);
			passwordStr = tw_jni_fetch_string(env, cred, "password", &passwordObj);
		}

		TSCOptions coreOptions;

		coreOptions.insert(std::make_pair("capability-token", tokenStr));
		coreOptions.insert(std::make_pair("password", passwordStr));
		coreOptions.insert(std::make_pair("stun-url", surlStr));
		coreOptions.insert(std::make_pair("turn-url", turlStr));
		coreOptions.insert(std::make_pair("user-name", userNameStr));


		TSCEndpointObjectRef endpoint = tscSdk->createEndpoint(coreOptions, TSCEndpointObserverObjectRef(new EndpointObserver(env, config)));

		if(endpoint == NULL) {
			LOG_W(TAG, "createEndPoint  failed");
			return JNI_FALSE;
		}
		LOG_D(TAG, "Log createEndPoint  succeeded");
		endpoints.insert(std::make_pair(userNameStr, endpoint));
		endpoint->registerEndpoint();

	}
	return JNI_TRUE;
}


JNIEXPORT jboolean JNICALL Java_com_twilio_signal_impl_SignalCore_logout
  (JNIEnv *env, jobject obj, jstring userName) {

	__android_log_print(ANDROID_LOG_VERBOSE, TAG, "KUMKUMM ::::::::SignalCore.logout() called", 1);

	const char *nativeString = (env)->GetStringUTFChars(userName, 0);

	TSCEndpointObjectRef endpoint = endpoints[nativeString];
	if(endpoint) {
		__android_log_print(ANDROID_LOG_VERBOSE, TAG, "KUMKUMM ::::::::SignalCore.logout() endpoint found", 1);
	} else
	{
		__android_log_print(ANDROID_LOG_VERBOSE, TAG, "KUMKUMM ::::::::SignalCore.logout() endpoint not found", 1);
	}
	(env)->ReleaseStringUTFChars(userName, nativeString);

	return JNI_TRUE;
}

