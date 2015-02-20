#include "TSCoreSDK.h"
#include <string.h>
#include <jni.h>

using namespace twiliosdk;
extern "C" {
	JNIEXPORT jstring JNICALL
    Java_com_example_Hellojnicpp_initCore(JNIEnv *env, jobject obj)
    {
		TSCSDK* tscSdk = TSCSDK::instance();

		return env->NewStringUTF("Hello from C++ over JNI!");
    }

	JNIEXPORT jboolean JNICALL
	Java_com_example_Hellojnicpp_isCoreInitialized(JNIEnv *env, jobject obj)
	{
		TSCSDK* tscSdk = TSCSDK::instance();
		if (tscSdk->isInitialized())
		{
			return JNI_TRUE;
		}
		return JNI_FALSE;
	}
}
