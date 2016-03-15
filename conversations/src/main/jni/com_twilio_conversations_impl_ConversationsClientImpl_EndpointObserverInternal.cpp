#include "com_twilio_conversations_impl_ConversationsClientImpl_EndpointObserverInternal.h"
#include "talk/app/webrtc/java/jni/jni_helpers.h"
#include "TSCoreSDKTypes.h"
#include "TSCoreError.h"
#include "TSCEndpoint.h"
#include "TSCEndpointObserver.h"
#include "TSCSession.h"
#include "TSCLogger.h"
#include <android/log.h>
#include <webrtc/base/criticalsection.h>

using namespace webrtc_jni;
using namespace twiliosdk;

#define TAG  "TwilioSDK(native)"

class EndpointObserverInternalWrapper: public TSCEndpointObserver
{
public:
    EndpointObserverInternalWrapper(JNIEnv* env,jobject obj, jobject j_endpoint_observer, jobject j_endpoint)
            :j_endpoint_observer_(env, j_endpoint_observer),
             j_endpoint_(env, j_endpoint),
             j_observer_class_(env, GetObjectClass(env, *j_endpoint_observer_)),
             j_registration_complete_(
                     GetMethodID(env,
                                 *j_observer_class_,
                                 "onRegistrationDidComplete",
                                 "(Lcom/twilio/conversations/impl/core/CoreError;)V")),
             j_unreg_complete_(
                     GetMethodID(env,
                                 *j_observer_class_,
                                 "onUnregistrationDidComplete",
                                 "(Lcom/twilio/conversations/impl/core/CoreError;)V")),
             j_state_change_(
                     GetMethodID(env,
                                 *j_observer_class_,
                                 "onStateDidChange",
                                 "(Lcom/twilio/conversations/impl/core/EndpointState;)V")),
             j_incoming_call_(
                     GetMethodID(env,
                                 *j_observer_class_,
                                 "onIncomingCallDidReceive",
                                 "(J[Ljava/lang/String;)V")),
             j_statetype_enum_(
                     env, env->FindClass("com/twilio/conversations/impl/core/EndpointState")),
             j_errorimpl_class_(
                     env, env->FindClass("com/twilio/conversations/impl/core/CoreErrorImpl")),
             j_errorimpl_ctor_id_(
                     GetMethodID( env,
                                  *j_errorimpl_class_,
                                  "<init>",
                                  "(Ljava/lang/String;ILjava/lang/String;)V")),
             deletion_in_progress_(false)
    {}

    virtual ~EndpointObserverInternalWrapper() {
        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                           kTSCoreLogLevelDebug,
                           "DESTROYING  EndpointObserverInternalWrapper");
    }

    void markForDeletion() {
        rtc::CritScope cs(&deletion_lock_);
        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                           kTSCoreLogLevelDebug,
                           "markForDeletion ");
        deletion_in_progress_ = true;
    }


protected:
    virtual void onRegistrationDidComplete(TSCoreErrorCode code, const std::string message) {
        ScopedLocalRefFrame local_ref_frame(jni());

        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                           kTSCoreLogLevelDebug,
                           "onRegistrationDidComplete");
        jobject j_error = errorToJavaCoreErrorImpl(code, message);
        CHECK_EXCEPTION(jni()) << "error during NewObject";

        if (IsNull(jni(), *j_endpoint_observer_)) {
            TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                               kTSCoreLogLevelWarning,
                               "observer reference has been destroyed");
            return;
        }

        {
            rtc::CritScope cs(&deletion_lock_);
            TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                               kTSCoreLogLevelWarning,
                               "onRegistrationDidComplete before check");
            if (deletion_in_progress_) {
                TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                                   kTSCoreLogLevelDebug,
                                   "Observer is marked for deletion, skipping callback");
                return;
            };
            TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                               kTSCoreLogLevelWarning,
                               "onRegistrationDidComplete after check");
            jni()->CallVoidMethod(*j_endpoint_observer_, j_registration_complete_, j_error);
            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

    virtual void onUnregistrationDidComplete(TSCoreErrorCode code, const std::string message) {
        ScopedLocalRefFrame local_ref_frame(jni());

        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                           kTSCoreLogLevelDebug,
                           "onUnregistrationDidComplete");
        jobject j_error = errorToJavaCoreErrorImpl(code, message);
        CHECK_EXCEPTION(jni()) << "error during NewObject";

        if (IsNull(jni(), *j_endpoint_observer_)) {
            TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                               kTSCoreLogLevelWarning,
                               "observer reference has been destroyed");
            return;
        }
        {
            rtc::CritScope cs(&deletion_lock_);
            TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                               kTSCoreLogLevelWarning,
                               "onUnregistrationDidComplete before check");
            if (deletion_in_progress_) {
                TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                                   kTSCoreLogLevelDebug,
                                   "Observer is marked for deletion, skipping callback");
                return;
            };
            TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                               kTSCoreLogLevelWarning,
                               "onUnregistrationDidComplete after check");
            jni()->CallVoidMethod(*j_endpoint_observer_, j_unreg_complete_, j_error);
            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

    virtual void onStateDidChange(TSCEndpointState state){
        ScopedLocalRefFrame local_ref_frame(jni());

        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                           kTSCoreLogLevelDebug,
                           "onStateDidChange, new state:%d",
                           state);

        if (*j_endpoint_observer_ == NULL || IsNull(jni(), *j_endpoint_observer_)) {
            TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                               kTSCoreLogLevelWarning,
                               "observer reference has been destroyed");
            return;
        }
        const std::string state_type_enum = "com/twilio/conversations/impl/core/EndpointState";

        jobject j_state_type =
                webrtc_jni::JavaEnumFromIndex(jni(), *j_statetype_enum_, state_type_enum, state);

        CHECK_EXCEPTION(jni()) << "error during NewObject";

        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                           kTSCoreLogLevelDebug,
                           "onStateDidChange 3");

        {
            rtc::CritScope cs(&deletion_lock_);
            TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                               kTSCoreLogLevelWarning,
                               "onStateDidChange before check");
            if (deletion_in_progress_) {
                TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                                   kTSCoreLogLevelDebug,
                                   "Observer is marked for deletion, skipping callback");
                return;
            };
            TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                               kTSCoreLogLevelWarning,
                               "onStateDidChange after check");
            jni()->CallVoidMethod(*j_endpoint_observer_, j_state_change_, j_state_type);
            TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                               kTSCoreLogLevelDebug,
                               "onStateDidChange 4");
            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

    virtual void onIncomingCallDidReceive(const TSCSessionPtr &session) {
        ScopedLocalRefFrame local_ref_frame(jni());

        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                           kTSCoreLogLevelDebug,
                           "onIncomingCallDidReceive");
        TSCSessionPtr *newSession = new TSCSessionPtr();
        *newSession = session;
        jlong j_session_id = webrtc_jni::jlongFromPointer(newSession);

        //Get participants from session and put them into java string array
        jobjectArray j_participants =
                partToJavaPart(jni(), session->getParticipants());
        CHECK_EXCEPTION(jni()) << "error during NewObject";

        if (IsNull(jni(), *j_endpoint_observer_)) {
            TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                               kTSCoreLogLevelWarning,
                               "observer reference has been destroyed");
            return;
        }
        {
            rtc::CritScope cs(&deletion_lock_);
            TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                               kTSCoreLogLevelWarning,
                               "onIncomingCallDidReceive before check");
            if (deletion_in_progress_) {
                TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                                   kTSCoreLogLevelDebug,
                                   "Observer is marked for deletion, skipping callback");
                return;
            };
            TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                               kTSCoreLogLevelWarning,
                               "onIncomingCallDidReceive after check");
            jni()->CallVoidMethod(
                    *j_endpoint_observer_, j_incoming_call_, j_session_id, j_participants);
            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

private:
    JNIEnv* jni() {
        return AttachCurrentThreadIfNeeded();
    }

    jstring stringToJString(JNIEnv * env, const std::string & nativeString) {
        return JavaStringFromStdString(env, nativeString);
    }

    // Return a ErrorImpl
    jobject errorToJavaCoreErrorImpl(TSCoreErrorCode code, const std::string &message) {
        if(code == kTSCoreSuccess) {
            return nullptr;
        } else {
            jstring j_domain = stringToJString(jni(), "signal.coresdk.domain.error");
            jint j_error_id = (jint)code;
            jstring j_message = stringToJString(jni(), message);
            return jni()->NewObject(
                    *j_errorimpl_class_, j_errorimpl_ctor_id_, j_domain, j_error_id, j_message);
        }
    }

    // Return Java array of participants
    jobjectArray partToJavaPart(JNIEnv *env,
                                const std::vector<std::pair<std::string, std::string>> participants) {
        int size = participants.size();
        if (size == 0) {
            return NULL;
        }
        jobjectArray j_participants = (jobjectArray)env->NewObjectArray(
                size,
                env->FindClass("java/lang/String"),
                stringToJString(jni(), ""));
        CHECK_EXCEPTION(jni()) << "error during NewObject";
        for (int i=0; i<size; i++) {
            env->SetObjectArrayElement(
                    j_participants, i, stringToJString(env, participants[i].first));
        }
        return j_participants;
    }

    const ScopedGlobalRef<jobject> j_endpoint_observer_;
    const ScopedGlobalRef<jobject> j_endpoint_;
    const ScopedGlobalRef<jclass> j_observer_class_;
    jmethodID j_registration_complete_;
    jmethodID j_unreg_complete_;
    jmethodID j_state_change_;
    jmethodID j_incoming_call_;
    const ScopedGlobalRef<jclass> j_statetype_enum_;
    const ScopedGlobalRef<jclass> j_errorimpl_class_;
    const jmethodID j_errorimpl_ctor_id_;

    bool deletion_in_progress_;
    mutable rtc::CriticalSection deletion_lock_;
};

/*
 * Class:     com_twilio_conversations_impl_ConversationsClientImpl_EndpointObserverInternal
 * Method:    wrapNativeObserver
 * Signature: (Lcom/twilio/conversations/impl/core/EndpointObserver;Lcom/twilio/conversations/Endpoint;)J
 */
JNIEXPORT jlong JNICALL Java_com_twilio_conversations_impl_ConversationsClientImpl_00024EndpointObserverInternal_wrapNativeObserver
        (JNIEnv *env, jobject obj, jobject j_endpoint_observer, jobject j_endpoint) {
    TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "wrapNativeObserver: Endpoint");
    TSCEndpointObserverPtr *endpointObserver = new TSCEndpointObserverPtr();
    endpointObserver->reset(new EndpointObserverInternalWrapper(env, obj, j_endpoint_observer, j_endpoint));
    return jlongFromPointer(endpointObserver);
}

/*
 * Class:     com_twilio_conversations_impl_ConversationsClientImpl_EndpointObserverInternal
 * Method:    freeNativeObserver
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_twilio_conversations_impl_ConversationsClientImpl_00024EndpointObserverInternal_freeNativeObserver
        (JNIEnv *env, jobject obj, jlong nativeEndpointObserver){
    TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "freeNativeObserver: Endpoint");
    TSCEndpointObserverPtr *endpointObserver = reinterpret_cast<TSCEndpointObserverPtr *>(nativeEndpointObserver);
    if (endpointObserver != nullptr) {
        endpointObserver->reset();
        delete endpointObserver;
    }
}

JNIEXPORT void JNICALL Java_com_twilio_conversations_impl_ConversationsClientImpl_00024EndpointObserverInternal_markForDeletion
        (JNIEnv *env, jobject obj, jlong nativeEndpointObserver){
    TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "markForDeletion: Endpoint");
    TSCEndpointObserverPtr *endpointObserver = reinterpret_cast<TSCEndpointObserverPtr *>(nativeEndpointObserver);
    if (endpointObserver != nullptr) {
        EndpointObserverInternalWrapper* wrapper = static_cast<EndpointObserverInternalWrapper*>(endpointObserver->get());
        wrapper->markForDeletion();
    }
}

