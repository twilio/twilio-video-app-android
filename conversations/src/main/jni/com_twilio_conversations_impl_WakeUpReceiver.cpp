#include <jni.h>
#include <string.h>

#include "TSCoreSDKTypes.h"
#include "TSCoreConstants.h"
#include "TSCoreSDK.h"
#include "TSCConfiguration.h"
#include "TSCLogger.h"
#include "com_twilio_conversations_impl_WakeUpReceiver.h"

#include "webrtc/api/java/jni/jni_helpers.h"
#include "webrtc/api/java/jni/classreferenceholder.h"

#define TAG  "TwilioSDK(native)"

using namespace twiliosdk;
using namespace webrtc_jni;

/*
* Class:     com_twilio_conversations_impl_WakeUpReceiver
* Method:    onApplicationWakeUp
* Signature: ()J
*/
JNIEXPORT void JNICALL Java_com_twilio_conversations_impl_WakeUpReceiver_nativeOnApplicationWakeUp
(JNIEnv *env, jobject) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "onApplicationWakeUp");
    TSCSDK* tscSdk = TSCSDK::instance();

    if (tscSdk != NULL) {
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "onShortWakeUp");
        tscSdk->onShortWakeUp();
        CHECK_EXCEPTION(env) << "error during onShortWakeUp";
    }
}