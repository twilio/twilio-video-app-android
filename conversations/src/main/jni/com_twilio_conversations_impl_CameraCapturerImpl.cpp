#include <talk/app/webrtc/androidvideocapturer.h>
#include "com_twilio_conversations_impl_CameraCapturerImpl.h"
#include "talk/app/webrtc/java/jni/jni_helpers.h"
#include "TSCoreSDKTypes.h"
#include "TSCoreError.h"
#include "TSCLogger.h"
#include "TSCEndpoint.h"
#include "TSCSession.h"

using namespace twiliosdk;
using namespace webrtc_jni;

#define TAG  "TwilioSDK(native)"


JNIEXPORT void JNICALL Java_com_twilio_conversations_impl_CameraCapturerImpl_stopVideoSource
        (JNIEnv *env, jobject obj, jlong nativeSession)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "stopVideoSource");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    session->get()->stopVideoSource();
}


JNIEXPORT void JNICALL Java_com_twilio_conversations_impl_CameraCapturerImpl_restartVideoSource
        (JNIEnv *env, jobject obj, jlong nativeSession)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "stopVideoSource");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    session->get()->restartVideoSource();
}

JNIEXPORT void JNICALL
Java_com_twilio_conversations_impl_CameraCapturerImpl_disposeCapturer(JNIEnv *env, jobject instance,
                                                                      jlong nativeVideoCapturerAndroid) {
    delete reinterpret_cast<webrtc::AndroidVideoCapturerDelegate *>(nativeVideoCapturerAndroid);
}

