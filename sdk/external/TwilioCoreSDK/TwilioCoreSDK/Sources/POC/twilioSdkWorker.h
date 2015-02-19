#ifndef TWILIOSDKWORKER_H
#define TWILIOSDKWORKER_H

#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunused-parameter"
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
#endif

#include "talk/base/thread.h"

#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma clang diagnostic pop
#pragma GCC diagnostic pop
#endif

#include "twiliosdk.h"
#include "webrtcConnectionManager.h"
#include "twilioSipAccount.h"
#include "twilioSipCall.h"

namespace twiliosdk {

class MediaSourceData {
public:
    MediaSourceData(const std::string& source_id,
                    TwilioSdkMediaType source_type) :
                        source_id_(source_id),
                        source_type_(source_type) {
    }
public:
    std::string source_id_;
    TwilioSdkMediaType source_type_;
};

template <class T>
class VideoConstraintsData {
public:
    VideoConstraintsData(TwilioSdkVideoConstraints key,
                         T value,
                         TwilioSdkConstraintType type):
                             key_(key),
                             value_(value),
                             type_(type) {}
public:
    TwilioSdkVideoConstraints key_;
    T value_;
    TwilioSdkConstraintType type_;
};

template class VideoConstraintsData<int>;
template class VideoConstraintsData<double>;



class TwilioSdkWorker : public talk_base::Runnable,
        public talk_base::MessageHandler,
        public WebrtcConnectionManagerObserverInterface,
        public TwilioSipAccountObserverInterface,
        public TwilioSipCallObserverInterface {
public:
    // messages hadnled by the worker
    typedef enum sdkWorkerMessageId {
        MSG_START_CALL,
        MSG_REMOTE_TRICKLE_SUPPORT,
        MSG_SDP_CREATED,
        MSG_LOCAL_SDP_FINISHED,
        MSG_SDP_CREATE_FAILED,
        MSG_PRACK_RECEIVED,
        MSG_ANSWER_RECEIVED,
        MSG_ANSWER_ACCEPT_FAILED,
        MSG_ANSWER_ACCEPTED,
        MSG_CALL_REJECTED,
        MSG_RINGING,
        MSG_CALL_CONNECTING,
        MSG_USER_NOT_AVAILABLE,
        MSG_RECEIVE_CALL,
        MSG_OFFER_ACCEPTED,
        MSG_OFFER_ACCEPT_FAILED,
        MSG_REJECT,
        MSG_TERMINATE,
        MSG_TERMINATED,
        MSG_ICE_CANDIDATE,
        MSG_SOURCE_ADDED,
        MSG_SOURCE_REMOVED,
        MSG_START_PLAYBACK,
        MSG_STOP_PLAYBACK,
        MSG_CAPTURE_ADDED,
        MSG_CAPTURE_REMOVED,
        MSG_START_CAPTURE,
        MSG_STOP_CAPTURE,
        MSG_SET_CURRENT_DEVICE,
        MSG_SET_VOLUME_LEVEL,
        MSG_SET_VIDEO_CONSTRAINTS_INT,
        MSG_SET_VIDEO_CONSTRAINTS_DOUBLE,
        MSG_SET_CAPTURE_FEEDBACK_INTERVAL
    } SdkWorkerMessageId;

    TwilioSdkWorker(const std::string &uuid,
                    TwilioSdkInitParams *params,
                    TwilioSdkObserverInterface *observer);

    virtual ~TwilioSdkWorker();

    //TODO: rework once make more sync sdk methods
    WebrtcConnectionManager* getConnectionManager();

    // check whether SDK is initia
    bool initialized() const;

    // post a message to SdkWorker
    void post(uint32 id, talk_base::MessageData *data);

    // run the job on a thread
    virtual void Run(talk_base::Thread *thread);

    // handle messages
    virtual void OnMessage(talk_base::Message *msg);

    //* WebrtcConnectionManagerObserverInterface implementation
    virtual void onSdpCreationFailed(const std::string &error);

    virtual void onSdpCreated(const std::string &sdp);

    virtual void onIceCandidate(const std::string& candidate);

    virtual void onLocalSdpFinished(const std::string &sdp);

    virtual void onIceConnected();

    virtual void onAnswerAccepted();

    virtual void onSourceAdded(const std::string& source,
                               const TwilioSdkMediaType type);

    virtual void onSourceRemoved(const std::string& source);

    virtual void onSourceDataAvailable(const std::string& source);

    virtual void onSourceVideoTrack(const std::string& source,
                                    const uint32 width, const uint32 height,
                                    uint8_t** buffer,
                                    TwilioSdkSourceFormat* desired_type);

    virtual void onCaptureAdded(const std::string& source,
                                const TwilioSdkMediaType type);

    virtual void onCaptureRemoved(const std::string& source,
                                  const TwilioSdkMediaType type);

    virtual void onCaptureFeedbackAvailable(const uint32_t last_effective_width,
                                            const uint32_t last_effective_height,
                                            const uint32_t last_effective_framerate);

    //* TwilioSipAccountObserverInterface implementation
    virtual void onSipAccountIncomingCall(const int call_id);
    virtual void onSipAccountStateChanged(const TwilioSdkInitState state);
    virtual void onSipAccountRemoteTrickleSupported(const bool supported);

    //* TwilioSipCallObserverInterface implementation
    virtual void onSipCallStateChanged(const TwilioSdkCallState state);
    virtual void onSipInfoMethodReceived(const std::string& data);
    virtual void onSipPrackMethodReceived();

private:
    void initStateTransition(TwilioSdkInitState state);
    void notifyCallEvent(TwilioSdkCallState state);
private:

    // States that process callings
    class BaseState {
    public:
        BaseState(TwilioSdkWorker* worker) : worker_(worker) {}
        virtual ~BaseState() {}
        virtual std::string name() = 0;
        virtual void process(talk_base::Message *msg);
    protected:
        TwilioSdkWorker* worker_;
    };

    class IdleState : public BaseState {
    public:
        IdleState(TwilioSdkWorker* worker) : BaseState(worker) {}
        virtual std::string name() { return "Idle";}
        virtual void process(talk_base::Message *msg);
    };

    class InitiatorStartState : public IdleState {
     public:
        InitiatorStartState(TwilioSdkWorker* worker) : IdleState(worker) {}
        virtual std::string name() { return "InitiatorStart";}
        virtual void process(talk_base::Message *msg);
    };

    class InitiatorTrickleStartState : public InitiatorStartState {
     public:
        InitiatorTrickleStartState(TwilioSdkWorker* worker) : InitiatorStartState(worker) {}
        virtual std::string name() { return "InitiatorTrickleStart";}
        virtual void process(talk_base::Message *msg);
    };

    class InitiatorTricklingState : public IdleState {
     public:
        InitiatorTricklingState(TwilioSdkWorker* worker) : IdleState(worker) {}
        virtual std::string name() { return "InitiatorTrickling";}
        virtual void process(talk_base::Message *msg);
    };

    class InitiatorConnectingState : public IdleState {
     public:
        InitiatorConnectingState(TwilioSdkWorker* worker) : IdleState(worker) {}
        virtual std::string name() { return "InitiatorConnecting";}
        virtual void process(talk_base::Message *msg);
    };

    class ReceiverStartState : public IdleState {
     public:
        ReceiverStartState(TwilioSdkWorker* worker) : IdleState(worker) {}
        virtual std::string name() { return "ReceiverStart";}
        virtual void process(talk_base::Message *msg);
    };

    class ReceiverTrickleStartState : public IdleState {
     public:
        ReceiverTrickleStartState(TwilioSdkWorker* worker) : IdleState(worker) {}
        virtual std::string name() { return "ReceiverTrickleStart";}
        virtual void process(talk_base::Message *msg);
    };

    class ReceiverTricklingState : public ReceiverTrickleStartState {
     public:
        ReceiverTricklingState(TwilioSdkWorker* worker) : ReceiverTrickleStartState(worker) {}
        virtual std::string name() { return "ReceiverTrickling";}
        virtual void process(talk_base::Message *msg);
    };

    class ReceiverConnectingState : public IdleState {
     public:
        ReceiverConnectingState(TwilioSdkWorker* worker) : IdleState(worker) {}
        virtual std::string name() { return "ReceiverConnecting";}
        virtual void process(talk_base::Message *msg);
    };

    class ConnectedState : public IdleState {
     public:
        ConnectedState(TwilioSdkWorker* worker) : IdleState(worker) {}
        virtual std::string name() { return "Connected";}
        virtual void process(talk_base::Message *msg);
    };

    void changeCallState(BaseState* state);
private:
    talk_base::scoped_ptr<TwilioSipAccount> sip_account_;
    talk_base::scoped_ptr<TwilioSipCall> sip_call_;
    talk_base::scoped_ptr<WebrtcConnectionManager> connection_manager_;
    talk_base::scoped_ptr<talk_base::Thread> thread_;
    talk_base::scoped_ptr<BaseState> call_state_;
    std::string uuid_;

    TwilioSdkInitParams *initParams_;
    TwilioSdkCallParams *callParams_;

    TwilioSdkObserverInterface *observer_;
    TwilioSdkInitState initState_;
    std::string offer_;
    std::string answer_;
    std::string candidates_;
    bool is_trickle_ice_;
    bool local_sdp_finished_;
};

}  // namespace twiliosdk

#endif // TWILIOSDKWORKER_H
