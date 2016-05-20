#include "com_twilio_conversations_ApplicationForegroundTracker.h"
#include "TSCoreSDKTypes.h"
#include "TSCoreConstants.h"
#include "TSCoreSDK.h"
#include "TSCConfiguration.h"
#include "TSCLogger.h"

#include "webrtc/api/java/jni/jni_helpers.h"
#include "webrtc/api/java/jni/classreferenceholder.h"

#define TAG  "TwilioSDK(native)"

using namespace twiliosdk;
using namespace webrtc_jni;

/*
* Class:     Java_com_twilio_conversations_ApplicationForegroundTracker_onApplicationForeground
* Method:    onApplicationForeground
* Signature: ()J
*/
JNIEXPORT void JNICALL Java_com_twilio_conversations_ApplicationForegroundTracker_nativeOnApplicationForeground
        (JNIEnv *env, jobject) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "onApplicationForeground");
    TSCSDK* tscSdk = TSCSDK::instance();

    if (tscSdk != NULL) {
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                           kTSCoreLogLevelDebug,
                           "onCompleteWakeUp");
        tscSdk->onCompleteWakeUp();
        CHECK_EXCEPTION(env) << "error during onCompleteWakeUp";
    }
}



/*
 * Class:     Java_com_twilio_conversations_ApplicationForegroundTracker_onApplicationBackground
 * Method:    onApplicationBackground
 * Signature: ()J
 */
JNIEXPORT void JNICALL Java_com_twilio_conversations_ApplicationForegroundTracker_nativeOnApplicationBackground
        (JNIEnv *env, jobject) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "onApplicationBackground");
    TSCSDK* tscSdk = TSCSDK::instance();

    if (tscSdk != NULL) {
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                           kTSCoreLogLevelDebug,
                           "onGoingToSleep");
        tscSdk->onGoingToSleep();
        CHECK_EXCEPTION(env) << "error during onGoingToSleep";
    }
}
