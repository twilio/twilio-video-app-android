#include "talk/app/webrtc/java/jni/jni_helpers.h"
#include "talk/app/webrtc/mediastreaminterface.h"

#include "com_twilio_conversations_impl_ConversationImpl_SessionObserverInternal.h"

#include "TSCoreSDKTypes.h"
#include "TSCoreError.h"
#include "TSCSession.h"
#include "TSCLogger.h"
#include "TSCSessionObserver.h"
#include "TSCMediaStreamInfo.h"
#include "TSCMediaTrackInfo.h"
#include "com_twilio_conversations_impl_ConversationImpl.h"

using namespace webrtc;
using namespace webrtc_jni;
using namespace twiliosdk;


class SessionObserverInternalWrapper : public TSCSessionObserver {
public:
    SessionObserverInternalWrapper(JNIEnv* jni, jobject obj, jobject j_observer, jobject conversation) :
            j_observer_global_(
                    jni, j_observer),
            j_observer_class_(
                    jni, GetObjectClass(jni, j_observer)),
            j_session_state_changed_id(
                    GetMethodID(jni,
                                *j_observer_class_,
                                "onSessionStateChanged",
                                "(Lcom/twilio/conversations/impl/core/SessionState;)V")),
            j_start_completed_id(
                    GetMethodID(jni,
                                *j_observer_class_,
                                "onStartCompleted",
                                "(Lcom/twilio/conversations/impl/core/CoreError;)V")),
            j_stop_completed_id(
                    GetMethodID(jni,
                                *j_observer_class_,
                                "onStopCompleted",
                                "(Lcom/twilio/conversations/impl/core/CoreError;)V")),
            j_participant_connected_id(
                    GetMethodID(jni,
                                *j_observer_class_,
                                "onParticipantConnected",
                                "(Ljava/lang/String;Ljava/lang/String;Lcom/twilio/conversations/impl/core/CoreError;)V")),
            j_participant_disconnected_id(
                    GetMethodID(jni,
                                *j_observer_class_,
                                "onParticipantDisconnected",
                                "(Ljava/lang/String;Ljava/lang/String;Lcom/twilio/conversations/impl/core/DisconnectReason;)V")),
            j_media_stream_added_id(
                    GetMethodID(jni,
                                *j_observer_class_,
                                "onMediaStreamAdded",
                                "(Lcom/twilio/conversations/impl/core/MediaStreamInfo;)V")),
            j_media_stream_removed_id(
                    GetMethodID(jni,
                                *j_observer_class_,
                                "onMediaStreamRemoved",
                                "(Lcom/twilio/conversations/impl/core/MediaStreamInfo;)V")),
            j_video_track_added_id_(
                    GetMethodID(jni,
                                *j_observer_class_,
                                "onVideoTrackAdded",
                                "(Lcom/twilio/conversations/impl/core/TrackInfo;Lorg/webrtc/VideoTrack;)V")),
            j_video_track_removed_id_(
                    GetMethodID(jni,
                                *j_observer_class_,
                                "onVideoTrackRemoved",
                                "(Lcom/twilio/conversations/impl/core/TrackInfo;)V")),
            j_video_track_state_changed_id_(
                    GetMethodID(jni,
                                *j_observer_class_,
                                "onVideoTrackStateChanged",
                                "(Lcom/twilio/conversations/impl/core/TrackInfo;)V")),
            j_audio_track_added_id_(
                    GetMethodID(jni,
                                *j_observer_class_,
                                "onAudioTrackAdded",
                                "(Lcom/twilio/conversations/impl/core/TrackInfo;Lorg/webrtc/AudioTrack;)V")),
            j_audio_track_removed_id_(
                    GetMethodID(jni,
                                *j_observer_class_,
                                "onAudioTrackRemoved",
                                "(Lcom/twilio/conversations/impl/core/TrackInfo;)V")),
            j_audio_track_state_changed_id_(
                    GetMethodID(jni,
                                *j_observer_class_,
                                "onAudioTrackStateChanged",
                                "(Lcom/twilio/conversations/impl/core/TrackInfo;)V")),
            j_trackinfo_class_(
                    jni, jni->FindClass("com/twilio/conversations/impl/core/TrackInfoImpl")),
            j_trackorigin_class_(
                    jni, jni->FindClass("com/twilio/conversations/TrackOrigin")),
            j_sessionstate_enum_(
                    jni, jni->FindClass("com/twilio/conversations/impl/core/SessionState")),
            j_trackinfo_ctor_id_(
                    GetMethodID(jni,
                                *j_trackinfo_class_,
                                "<init>",
                                "(Ljava/lang/String;Ljava/lang/String;Lcom/twilio/conversations/TrackOrigin;I)V")),
            j_video_track_class_(
                    jni, jni->FindClass("org/webrtc/VideoTrack")),
            j_video_track_ctor_(
                    GetMethodID(jni,
                                *j_video_track_class_,
                                "<init>",
                                "(J)V")),
            j_audio_track_class_(
                    jni, jni->FindClass("org/webrtc/AudioTrack")),
            j_audio_track_ctor_(
                    GetMethodID(jni,
                                *j_audio_track_class_,
                                "<init>",
                                "(J)V")),
            j_errorimpl_class_(
                    jni, jni->FindClass("com/twilio/conversations/impl/core/CoreErrorImpl")),
            j_errorimpl_ctor_id_(
                    GetMethodID(jni,
                                *j_errorimpl_class_,
                                "<init>",
                                "(Ljava/lang/String;ILjava/lang/String;)V")),
            j_disreason_enum_(
                    jni, jni->FindClass("com/twilio/conversations/impl/core/DisconnectReason")),
            j_media_stream_info_class_(
                    jni, jni->FindClass("com/twilio/conversations/impl/core/MediaStreamInfoImpl")),
            j_media_stream_info_ctor_(
                    GetMethodID(jni,
                                *j_media_stream_info_class_,
                                "<init>",
                                "(IILjava/lang/String;)V"))
    {}

protected:

    virtual void onStateDidChange(TSCSessionState state) {
        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "onStateDidChange");

        const std::string session_state_class = "com/twilio/conversations/impl/core/SessionState";
        jobject j_session_state = webrtc_jni::JavaEnumFromIndex(
                jni(), *j_sessionstate_enum_, session_state_class, state);
        CHECK_EXCEPTION(jni()) << "error during NewObject";
        jni()->CallVoidMethod(
                *j_observer_global_, j_session_state_changed_id, j_session_state);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }

    virtual void onStartDidComplete(TSCoreErrorCode code, const std::string message) {
        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "onStartDidComplete");

        jobject j_error_obj = errorToJavaCoreErrorImpl(code, message);
        CHECK_EXCEPTION(jni()) << "error during NewObject";
        jni()->CallVoidMethod(*j_observer_global_, j_start_completed_id, j_error_obj);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }

    virtual void onStopDidComplete(TSCoreErrorCode code, const std::string message) {
        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "onStopDidComplete");

        jobject j_error_obj = errorToJavaCoreErrorImpl(code, message);
        CHECK_EXCEPTION(jni()) << "error during NewObject";
        jni()->CallVoidMethod(*j_observer_global_, j_stop_completed_id, j_error_obj);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }

    virtual void onParticipantDidConnect(const std::string participant,
                                         const std::string participantSid,
                                         TSCoreErrorCode code,
                                         const std::string message) {
        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                           kTSCoreLogLevelDebug,
                           "onParticipantDidConnect");

        jstring j_participant_identity = stringToJString(jni(), participant);
        jstring j_participant_sid = stringToJString(jni(), participantSid);

        jobject j_error_obj = errorToJavaCoreErrorImpl(code, message);
        CHECK_EXCEPTION(jni()) << "error during NewObject";
        jni()->CallVoidMethod(*j_observer_global_, j_participant_connected_id,
                              j_participant_identity, j_participant_sid, j_error_obj);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }

    virtual void onParticipantDidDisconnect(const std::string participant,
                                            const std::string participantSid,
                                            TSCDisconnectReason reason) {
        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                           kTSCoreLogLevelDebug,
                           "onParticipantDidDisconect");

        jstring j_participant_identity = stringToJString(jni(), participant);
        jstring j_participant_sid = stringToJString(jni(), participantSid);

        const std::string dis_reason_class =
                "com/twilio/conversations/impl/core/DisconnectReason";
        jobject j_reason = webrtc_jni::JavaEnumFromIndex(
                jni(), *j_disreason_enum_, dis_reason_class, reason);
        CHECK_EXCEPTION(jni()) << "error during NewObject";

        jni()->CallVoidMethod(*j_observer_global_, j_participant_disconnected_id,
                              j_participant_identity, j_participant_sid, j_reason);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }

    virtual void onMediaStreamDidAdd(TSCMediaStreamInfoObject* stream) {
        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "onMediaStreamDidAdd");

        jobject j_media_info = mediaStrInfoJavaMediaStrInfoImpl(stream);
        CHECK_EXCEPTION(jni()) << "error during NewObject";
        jni()->CallVoidMethod(
                *j_observer_global_, j_media_stream_added_id, j_media_info);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }

    virtual void onMediaStreamDidRemove(TSCMediaStreamInfoObject* stream) {
        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                           kTSCoreLogLevelDebug,
                           "onMediaStreamDidRemove");

        jobject j_media_info = mediaStrInfoJavaMediaStrInfoImpl(stream);
        CHECK_EXCEPTION(jni()) << "error during NewObject";
        jni()->CallVoidMethod(
                *j_observer_global_, j_media_stream_removed_id, j_media_info);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }

    virtual void onVideoTrackDidAdd(TSCVideoTrackInfoObject* trackInfo,
                                    VideoTrackInterface* videoTrack) {
        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "onVideoTrackDidAdd");

        jstring id = stringToJString(jni(), videoTrack->id());
        jobject j_track = jni()->NewObject(
                *j_video_track_class_, j_video_track_ctor_, jlongFromPointer(videoTrack), id);
        CHECK_EXCEPTION(jni()) << "error during NewObject";
        jobject j_trackinfo = TrackInfoToJavaTrackInfoImpl(trackInfo);
        CHECK_EXCEPTION(jni()) << "error during NewObject";
        jni()->CallVoidMethod(*j_observer_global_, j_video_track_added_id_, j_trackinfo, j_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        videoTrack->AddRef();
    }

    virtual void onVideoTrackDidRemove(TSCVideoTrackInfoObject* trackInfo) {
        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                           kTSCoreLogLevelDebug,
                           "onVideoTrackDidRemove");

        jobject j_trackinfo = TrackInfoToJavaTrackInfoImpl(trackInfo);
        CHECK_EXCEPTION(jni()) << "error during NewObject";
        jni()->CallVoidMethod(*j_observer_global_, j_video_track_removed_id_, j_trackinfo);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }

    virtual void onVideoTrackStateDidChange(TSCVideoTrackInfoObject* trackInfo) {
        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                           kTSCoreLogLevelDebug,
                           "onVideoTrackStateDidChange");

        jobject j_trackinfo = TrackInfoToJavaTrackInfoImpl(trackInfo);
        CHECK_EXCEPTION(jni()) << "error during NewObject";
        jni()->CallVoidMethod(*j_observer_global_, j_video_track_state_changed_id_, j_trackinfo);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }

    virtual void onAudioTrackDidAdd(TSCAudioTrackInfoObject *trackInfo,
                                    webrtc::AudioTrackInterface* audioTrack) {
        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "onAudioTrackDidAdd");

        jstring id = stringToJString(jni(), audioTrack->id());
        jobject j_track = jni()->NewObject(
                *j_audio_track_class_, j_audio_track_ctor_, jlongFromPointer(audioTrack), id);
        CHECK_EXCEPTION(jni()) << "error during NewObject";
        jobject j_trackinfo = TrackInfoToJavaTrackInfoImpl(trackInfo);
        CHECK_EXCEPTION(jni()) << "error during NewObject";
        jni()->CallVoidMethod(*j_observer_global_, j_audio_track_added_id_, j_trackinfo, j_track);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        audioTrack->AddRef();
    }

    virtual void onAudioTrackDidRemove(TSCAudioTrackInfoObject *trackInfo) {
        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                           kTSCoreLogLevelDebug,
                           "onAudioTrackDidRemove");

        jobject j_trackinfo = TrackInfoToJavaTrackInfoImpl(trackInfo);
        CHECK_EXCEPTION(jni()) << "error during NewObject";
        jni()->CallVoidMethod(*j_observer_global_, j_audio_track_removed_id_, j_trackinfo);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }

    virtual void onAudioTrackStateDidChange(TSCAudioTrackInfoObject* trackInfo) {
        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                           kTSCoreLogLevelDebug,
                           "onAudioTrackStateDidChange");

        jobject j_trackinfo = TrackInfoToJavaTrackInfoImpl(trackInfo);
        CHECK_EXCEPTION(jni()) << "error during NewObject";
        jni()->CallVoidMethod(*j_observer_global_, j_audio_track_state_changed_id_, j_trackinfo);
        CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
    }

    virtual void onDidReceiveSessionStatistics(TSCSessionStatisticsPtr statistics) {
        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                           kTSCoreLogLevelDebug,
                           "onDidReceiveSessionStatistics");
        // TODO: implement me
    }

    virtual void onDidReceiveConversationEvent(ConversationEvent *event) {
        TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK,
                           kTSCoreLogLevelDebug,
                           "onDidReceiveConversationEvent");
        // TODO: implement me
    }

private:

    JNIEnv* jni() {
        return AttachCurrentThreadIfNeeded();
    }

    // Return a TrackInfoImpl for the VideoTrack
    jobject TrackInfoToJavaTrackInfoImpl(const TSCVideoTrackInfoObject *trackInfo) {
        jstring j_participant_address = stringToJString(jni(), trackInfo->getParticipantAddress());
        jstring j_track_id = stringToJString(jni(), trackInfo->getTrackId());
        const std::string state_class = "com/twilio/conversations/TrackOrigin";
        jobject j_origin = JavaEnumFromIndex(jni(),
                                             *j_trackorigin_class_,
                                             state_class,
                                             trackInfo->getStreamOrigin());
        CHECK_EXCEPTION(jni()) << "error during NewObject";
        int enabled = trackInfo->isEnabled() ? 1 : 0;
        return jni()->NewObject(
                *j_trackinfo_class_, j_trackinfo_ctor_id_,
                j_participant_address, j_track_id, j_origin, enabled);
    }

    // Return a TrackInfoImpl for the AudioTrack
    jobject TrackInfoToJavaTrackInfoImpl(const TSCAudioTrackInfoObject *trackInfo) {
        jstring j_participant_address = stringToJString(jni(), trackInfo->getParticipantAddress());
        jstring j_track_id = stringToJString(jni(), trackInfo->getTrackId());
        const std::string state_class = "com/twilio/conversations/TrackOrigin";
        jobject j_origin = JavaEnumFromIndex(jni(),
                                             *j_trackorigin_class_,
                                             state_class,
                                             trackInfo->getStreamOrigin());
        CHECK_EXCEPTION(jni()) << "error during NewObject";
        int enabled = trackInfo->isEnabled() ? 1 : 0;
        return jni()->NewObject(
                *j_trackinfo_class_, j_trackinfo_ctor_id_,
                j_participant_address, j_track_id, j_origin, enabled);
    }

    // Return a ErrorImpl
    jobject errorToJavaCoreErrorImpl(TSCoreErrorCode code, const std::string &message) {
        if (code == kTSCoreSuccess) {
            return nullptr;
        }

        jstring j_domain = stringToJString(jni(), "signal.coresdk.domain.error");
        jint j_error_id = (jint) code;
        jstring j_message = stringToJString(jni(), message);
        return jni()->NewObject(
                *j_errorimpl_class_, j_errorimpl_ctor_id_,
                j_domain, j_error_id, j_message);
    }

    // Return a MediaStreamInfoImpl
    jobject mediaStrInfoJavaMediaStrInfoImpl(const TSCMediaStreamInfoObject *stream) {
        if (!stream) {
            return NULL;
        }
        jstring j_address = stringToJString(jni(), stream->getParticipantAddress());
        jint j_streamId = (jint)stream->getStreamId();
        jint j_sessionId = (jint)stream->getSessionId();
        return jni()->NewObject(
                *j_media_stream_info_class_, j_media_stream_info_ctor_,
                j_sessionId, j_streamId, j_address);
    }

    jstring stringToJString(JNIEnv* env, const std::string& nativeString) {
        return JavaStringFromStdString(env, nativeString);
    }

    const ScopedGlobalRef<jobject> j_observer_global_;
    const ScopedGlobalRef<jclass> j_observer_class_;

    const jmethodID j_session_state_changed_id;
    const jmethodID j_start_completed_id;
    const jmethodID j_stop_completed_id;
    const jmethodID j_participant_connected_id;
    const jmethodID j_participant_disconnected_id;
    const jmethodID j_media_stream_added_id;
    const jmethodID j_media_stream_removed_id;
    const jmethodID j_video_track_added_id_;
    const jmethodID j_video_track_removed_id_;
    const jmethodID j_video_track_state_changed_id_;
    const jmethodID j_audio_track_added_id_;
    const jmethodID j_audio_track_removed_id_;
    const jmethodID j_audio_track_state_changed_id_;

    const ScopedGlobalRef<jclass> j_trackinfo_class_;
    const ScopedGlobalRef<jclass> j_trackorigin_class_;
    const ScopedGlobalRef<jclass> j_sessionstate_enum_;
    const jmethodID j_trackinfo_ctor_id_;
    const ScopedGlobalRef<jclass> j_video_track_class_;
    const jmethodID j_video_track_ctor_;
    const ScopedGlobalRef<jclass> j_audio_track_class_;
    const jmethodID j_audio_track_ctor_;
    const ScopedGlobalRef<jclass> j_errorimpl_class_;
    const jmethodID j_errorimpl_ctor_id_;
    const ScopedGlobalRef<jclass> j_disreason_enum_;
    const ScopedGlobalRef<jclass> j_media_stream_info_class_;
    const jmethodID j_media_stream_info_ctor_;
};

/*
 * Class:     com_twilio_conversations_impl_ConversationImpl_SessionObserverInternal
 * Method:    wrapNativeObserver
 * Signature: (Lcom/twilio/conversations/SessionObserver;Lcom/twilio/conversations/Conversation;)J
 */
JNIEXPORT jlong JNICALL Java_com_twilio_conversations_impl_ConversationImpl_00024SessionObserverInternal_wrapNativeObserver
        (JNIEnv *env, jobject obj, jobject observer, jobject conversation) {
    TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "wrapNativeObserver");
    TSCSessionObserverPtr *sessionObserver = new TSCSessionObserverPtr();
    sessionObserver->reset(new SessionObserverInternalWrapper(env, obj, observer, conversation));
    return jlongFromPointer(sessionObserver);
}

/*
 * Class:     com_twilio_conversations_impl_ConversationImpl_SessionObserverInternal
 * Method:    freeNativeObserver
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_twilio_conversations_impl_ConversationImpl_00024SessionObserverInternal_freeNativeObserver
        (JNIEnv *env, jobject obj, jlong nativeSessionObserver){
    TS_CORE_LOG_MODULE(kTSCoreLogModuleSignalSDK, kTSCoreLogLevelDebug, "freeNativeObserver");
    TSCSessionObserverPtr *sessionObserver = reinterpret_cast<TSCSessionObserverPtr *>(nativeSessionObserver);
    if (sessionObserver != nullptr) {
        sessionObserver->reset();
        delete sessionObserver;
    }
}
