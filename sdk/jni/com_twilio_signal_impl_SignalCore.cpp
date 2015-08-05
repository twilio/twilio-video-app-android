#include "TSCoreSDK.h"
#include <twilio-jni/twilio-jni.h>
#include <string.h>
#include <jni.h>
#include <android/log.h>

#include "TSCoreSDKTypes.h"
#include "TSCEndpoint.h"
#include "EndpointObserver.h"
//#include "webrtc/video_engine/include/vie_base.h"
#include "webrtc/voice_engine/include/voe_base.h"
#include "webrtc/modules/video_capture/video_capture_internal.h"
#include "webrtc/modules/video_render/video_render_internal.h"

#include "com_twilio_signal_impl_SignalCore.h"

#define TAG  "SignalCore(native)"

using namespace twiliosdk;

static JavaVM * cachedJVM = NULL;
static std::map<jint , TSCEndpointObjectRef> endpoints;
static EndpointObserver* eObserver;
jlong eObserverPointer;

TSCSDK* tscSdk = NULL;

JNIEXPORT jboolean JNICALL Java_com_twilio_signal_impl_SignalCore_initCore(JNIEnv *env, jobject obj, jobject context)
{
	bool failure = false;


	env->GetJavaVM(&cachedJVM);
	tscSdk = TSCSDK::instance();
	__android_log_print(ANDROID_LOG_VERBOSE, TAG, "SignalCore.initCore() called");
	LOG_W(TAG, "SignalCore.initCore() called");


	//failure |= webrtc::SetCaptureAndroidVM(cachedJVM, context);
	//failure |= webrtc::SetRenderAndroidVM(cachedJVM);

	LOG_W(TAG, "Calling DA Magic formula");
	//failure |= webrtc::VoiceEngine::SetAndroidObjects(cachedJVM, env, context);

	if (tscSdk != NULL && tscSdk->isInitialized())
	{
		return JNI_TRUE;
	}

	return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_twilio_signal_impl_SignalCore_isCoreInitialized(JNIEnv *env, jobject obj)
{
	if (tscSdk != NULL && tscSdk->isInitialized())
	{
		return JNI_TRUE;
	}
	return JNI_FALSE;
}


JNIEXPORT jboolean JNICALL Java_com_twilio_signal_impl_SignalCore_login(JNIEnv *env, jobject obj, jobjectArray credInfo, jobject config,jobject endpointObj)
{
	if(tscSdk != NULL) {

		__android_log_print(ANDROID_LOG_VERBOSE, TAG, "SignalCore.login()");

		if (!credInfo) {
			LOG_W("SIGNAL", "Creating endpoint but no credentials specified");
			return true;
		 }

		jobject cred = env->GetObjectArrayElement(credInfo, 0);

		jstring tokenObj, surlObj, turlObj, userNameObj, passwordObj;
		const char *tokenStr = NULL, *surlStr = NULL, *turlStr = NULL, *userNameStr = NULL, *passwordStr = NULL;

		if (cred) {
			tokenStr = tw_jni_fetch_string(env, cred, "capabilityToken", &tokenObj);

		}

		TSCOptions coreOptions;

		__android_log_print(ANDROID_LOG_VERBOSE, TAG, "Capability token");
		coreOptions.insert(std::make_pair("capability-token", tokenStr));


		eObserver = new EndpointObserver(env, config, endpointObj);
		eObserverPointer = (jlong)eObserver;

		TSCEndpointObserverObjectRef eObserverRef = TSCEndpointObserverObjectRef(eObserver);

		__android_log_print(ANDROID_LOG_VERBOSE, TAG, "eObserverPointer = %p", &eObserver);
		__android_log_print(ANDROID_LOG_VERBOSE, TAG, "eObserverRef = %p", &eObserverRef);
		TSCEndpointObjectRef endpoint = tscSdk->createEndpoint(coreOptions, eObserverRef);

		eObserver->setEndpoint(endpoint);

		if(endpoint == NULL) {
			LOG_W(TAG, "createEndPoint  failed");
			return JNI_FALSE;
		}
		LOG_D(TAG, "Log createEndPoint  succeeded");
		//register endpoint
		jclass classToAdd = env->GetObjectClass(endpointObj);
		jmethodID id = env->GetMethodID(classToAdd, "hashCode", "()I");
		jint val = env->CallIntMethod(endpointObj, id);
		endpoints[val] = endpoint;

		endpoint->registerEndpoint();

	}
	return JNI_TRUE;
}


JNIEXPORT jboolean JNICALL Java_com_twilio_signal_impl_SignalCore_logout
  (JNIEnv *env, jobject obj, jobject endpointObj) {

	__android_log_print(ANDROID_LOG_VERBOSE, TAG, "SignalCore.logout()");

	jclass classToAdd = env->GetObjectClass(endpointObj);
	jmethodID id = env->GetMethodID(classToAdd, "hashCode", "()I");
	jint val = env->CallIntMethod(endpointObj, id);

	TSCEndpointObjectRef endpoint = endpoints[val];

	if(endpoint) {
		__android_log_print(ANDROID_LOG_VERBOSE, TAG, "Unregistering Endpoint with hashCode %d", val);
		endpoint->unregisterEndpoint();
		endpoints[val] = NULL;
	} else
	{
		__android_log_print(ANDROID_LOG_VERBOSE, TAG, "Endpoint not found");
	}


	return JNI_TRUE;
}


JNIEXPORT jboolean JNICALL Java_com_twilio_signal_impl_SignalCore_acceptNative
  (JNIEnv *env, jobject obj, jobject endpointObj) {

	__android_log_print(ANDROID_LOG_VERBOSE, TAG, "SignalCore.acceptNative()");

	jclass classToAdd = env->GetObjectClass(endpointObj);
	jmethodID id = env->GetMethodID(classToAdd, "hashCode", "()I");
	jint val = env->CallIntMethod(endpointObj, id);

	TSCEndpointObjectRef endpoint = endpoints[val];

	if(endpoint) {
		__android_log_print(ANDROID_LOG_VERBOSE, TAG, "acceptNative for Endpoint with hashCode %d", val);
		endpoint->unregisterEndpoint();
		endpoints[val] = NULL;
	} else
	{
		__android_log_print(ANDROID_LOG_VERBOSE, TAG, "Endpoint not found");
	}


	return JNI_TRUE;
}
