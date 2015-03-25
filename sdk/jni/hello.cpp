#include "TSCoreSDK.h"
#include <twilio-jni/twilio-jni.h>
#include <string.h>
#include <jni.h>
#include "TSCoreSDKTypes.h"

using namespace twiliosdk;
extern "C" {

	JNIEXPORT jstring JNICALL
    Java_com_twilio_signal_Test_initCore(JNIEnv *env, jobject obj)
    {
		TSCSDK* tscSdk = TSCSDK::instance();

		return env->NewStringUTF("Hello from C++ over JNI!");
    }

	JNIEXPORT jboolean JNICALL
	Java_com_twilio_signal_Test_isCoreInitialized(JNIEnv *env, jobject obj)
	{
		TSCSDK* tscSdk = TSCSDK::instance();
		if (tscSdk->isInitialized())
		{
			return JNI_TRUE;
		}
		return JNI_FALSE;
	}


	JNIEXPORT jboolean JNICALL
	Java_com_twilio_signal_Test_login(JNIEnv *env, jobject obj)
	{
		return JNI_FALSE;
	}
}
