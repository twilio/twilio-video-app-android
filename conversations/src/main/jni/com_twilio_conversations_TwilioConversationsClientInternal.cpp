#include <jni.h>
#include "webrtc/api/java/jni/jni_helpers.h"

#include "com_twilio_conversations_TwilioConversationsClientInternal.h"

#include "TSCoreSDK.h"
#include "TSCoreSDKTypes.h"
#include "TSCEndpoint.h"
#include "TSCSession.h"
#include "TSCLogger.h"

static std::shared_ptr<TwilioCommon::AccessManager> getNativeAccessMgrFromJava(JNIEnv* jni,
                                                                               jobject j_accessMgr);

using namespace twiliosdk;
using namespace webrtc_jni;

JNIEXPORT jlong JNICALL Java_com_twilio_conversations_TwilioConversationsClientInternal_nativeCreateEndpoint
        (JNIEnv *env, jobject obj, jobject j_accessMgr, jobjectArray optionsArray, jlong nativeEndpointObserver) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "createEndpoint");

    TSCOptions options;

    int size = env->GetArrayLength(optionsArray);
    int i = 0;
    while (i < size) {
        jstring jKey = (jstring)env->GetObjectArrayElement(optionsArray, i); i++;
        jstring jValue = (jstring)env->GetObjectArrayElement(optionsArray, i); i++;
        std::string key = JavaToStdString(env, jKey);
        std::string value = JavaToStdString(env, jValue);
        options[key] = value;
        env->DeleteLocalRef(jKey);
        env->DeleteLocalRef(jValue);
    }

    if (!nativeEndpointObserver) {
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelError, "nativeEndpointObserver is null");
        return 0;
    }

    TSCEndpointObserverPtr *endpointObserver = reinterpret_cast<TSCEndpointObserverPtr *>(nativeEndpointObserver);

    // Grab the shared_ptr to access manager
    std::shared_ptr<TwilioCommon::AccessManager> accessManager =
            getNativeAccessMgrFromJava(env, j_accessMgr);

    if (accessManager == NULL) {
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelError, "AccessManager is null");
        return 0;
    }

    if (accessManager->getToken().empty()) {
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelError, "token is null");
        return 0;
    }

    TS_CORE_LOG_DEBUG("access token is:%s", accessManager->getToken().c_str());

    TSCEndpointPtr *endpoint = new TSCEndpointPtr();
    *endpoint = TSCSDK::instance()->createEndpoint(options, accessManager.get(), *endpointObserver);

    // Release control of our local copy of the access manager shared_ptr
    accessManager.reset();

    return jlongFromPointer(endpoint);
}

static std::shared_ptr<TwilioCommon::AccessManager> getNativeAccessMgrFromJava(JNIEnv* jni,
                                                                               jobject j_accessMgr) {
    jclass j_accessManagerClass = GetObjectClass(jni, j_accessMgr);
    jmethodID getNativeHandleId = GetMethodID(jni, j_accessManagerClass, "getNativeHandle", "()J");

    jlong j_am = jni->CallLongMethod(j_accessMgr, getNativeHandleId);

    // Recreate a reference to the share_ptr of AccessManager by dereferencing the ptr
    // to the shared_ptr
    return *(reinterpret_cast<std::shared_ptr<TwilioCommon::AccessManager> *>(j_am));
}

/*
 * Class:     com_twilio_conversations_ConversationsClient
 * Method:    listen
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_twilio_conversations_TwilioConversationsClientInternal_nativeListen
        (JNIEnv *env, jobject obj, jlong nativeEndpoint) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "listen");
    reinterpret_cast<TSCEndpointPtr *>(nativeEndpoint)->get()->registerEndpoint(true, true);
}

/*
 * Class:     com_twilio_conversations_ConversationsClient
 * Method:    unlisten
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_twilio_conversations_TwilioConversationsClientInternal_nativeUnlisten
        (JNIEnv *env, jobject obj, jlong nativeEndpoint) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "unlisten");
    reinterpret_cast<TSCEndpointPtr *>(nativeEndpoint)->get()->unregisterEndpoint();
}


/*
 * Class:     com_twilio_conversations_ConversationsClient
 * Method:    reject
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_com_twilio_conversations_TwilioConversationsClientInternal_nativeReject
        (JNIEnv *env, jobject obj, jlong nativeEndpoint, jlong nativeSession) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "reject");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    reinterpret_cast<TSCEndpointPtr *>(nativeEndpoint)->get()->reject(*session);
}

/*
 * Class:     com_twilio_conversations_ConversationsClient
 * Method:    freeNativeHandle
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_twilio_conversations_TwilioConversationsClientInternal_nativeFreeHandle
        (JNIEnv *env, jobject obj, jlong nativeEndpoint) {
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "freeNativeHandle");
    TSCEndpointPtr *endpoint = reinterpret_cast<TSCEndpointPtr *>(nativeEndpoint);
    if (endpoint != nullptr) {
        TSCSDK::instance()->destroyEndpoint(*endpoint);
        endpoint->reset();
        delete endpoint;
    }
}
