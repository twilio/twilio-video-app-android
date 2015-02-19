#include "twiliosdkWorker.h"
#include "twilioDeviceManager.h"
#include "twilioSipEndpoint.h"
#include "twilioLogger.h"

namespace twiliosdk {

TwilioSdkWorker::TwilioSdkWorker(const std::string &uuid, TwilioSdkInitParams *params,
                                 TwilioSdkObserverInterface *observer) :
                                                        uuid_(uuid),
                                                        initParams_(params),
                                                        callParams_(new TwilioSdkCallParams()),
                                                        observer_(observer),
                                                        initState_(UNINITIALIZED),
                                                        is_trickle_ice_(false),
                                                        local_sdp_finished_(false) {
    callParams_->localUser = params->alias;
    callParams_->callMode = UNDEFINED;
    callParams_->callState = TERMINATED;
}

TwilioSdkWorker::~TwilioSdkWorker() {
    sip_call_.reset(NULL);
    TwilioSipEndpoint::destroy();
    thread_->Stop();
    delete callParams_;
}

WebrtcConnectionManager* TwilioSdkWorker::getConnectionManager() {
    return connection_manager_.get();
}

// check whether SDK is initia
bool TwilioSdkWorker::initialized() const {
    return initState_ == INITIALIZED;
}

// post a message to SdkWorker
void TwilioSdkWorker::post(uint32 id, talk_base::MessageData *data) {
    thread_->Post(this, id, data, false);
}

// run the job on a thread
void TwilioSdkWorker::Run(talk_base::Thread *thread) {
    //initialize logger
    if (initParams_->enableLogger) {
        std::string logPath = "TwilioSDK.log";
        if (!initParams_->logFolder.empty()) {
            logPath = initParams_->logFolder + "/" + logPath;
        }
        TwilioLoggerConfig config(
#ifdef DEBUG
                LEVEL_DEBUG,
#else
                LEVEL_INFORMATION,
#endif
                logPath);
        TwilioLogger::init(config);
    }
    // initialize
    initStateTransition(INITIALIZING);
    // save UUID and thread we are running on
    thread_.reset(thread);

    changeCallState(new IdleState(this));
    // create connection manager and init peer connection
    connection_manager_.reset(new WebrtcConnectionManager(this));

     // use TLS for sip transport
     bool useTLS = false;
     TwilioSipEndpoint::init(useTLS);
     sip_account_.reset(new TwilioSipAccount(useTLS));
    
     // set the callbacks to monitor state
    sip_account_->subscribe(this);
    // start registration
    sip_account_->init(*initParams_);
    // got into message processing loop
    thread_->Run();
}

// handle messages
void TwilioSdkWorker::OnMessage(talk_base::Message *msg) {
    std::string message_name;
    switch (msg->message_id) {
        case MSG_START_CALL: message_name = "MSG_START_CALL"; break;
        case MSG_REMOTE_TRICKLE_SUPPORT: message_name = "MSG_REMOTE_TRICKLE_SUPPORT"; break;
        case MSG_SDP_CREATED: message_name = "MSG_SDP_CREATED"; break;
        case MSG_LOCAL_SDP_FINISHED: message_name = "MSG_LOCAL_SDP_FINISHED"; break;
        case MSG_SDP_CREATE_FAILED: message_name = "MSG_SDP_CREATE_FAILED"; break;
        case MSG_PRACK_RECEIVED: message_name = "MSG_PRACK_RECEIVED"; break;
        case MSG_ANSWER_RECEIVED: message_name = "MSG_ANSWER_RECEIVED"; break;
        case MSG_ANSWER_ACCEPT_FAILED: message_name = "MSG_ANSWER_ACCEPT_FAILED"; break;
        case MSG_ANSWER_ACCEPTED: message_name = "MSG_ANSWER_ACCEPTED"; break;
        case MSG_CALL_REJECTED: message_name = "MSG_CALL_REJECTED"; break;
        case MSG_RINGING: message_name = "MSG_RINGING"; break;
        case MSG_CALL_CONNECTING: message_name = "MSG_CALL_CONNECTING"; break;
        case MSG_USER_NOT_AVAILABLE: message_name = "MSG_USER_NOT_AVAILABLE"; break;
        case MSG_RECEIVE_CALL: message_name = "MSG_RECEIVE_CALL"; break;
        case MSG_OFFER_ACCEPTED: message_name = "MSG_OFFER_ACCEPTED"; break;
        case MSG_OFFER_ACCEPT_FAILED: message_name = "MSG_OFFER_ACCEPT_FAILED"; break;
        case MSG_REJECT: message_name = "MSG_REJECT"; break;
        case MSG_TERMINATE: message_name = "MSG_TERMINATE"; break;
        case MSG_TERMINATED: message_name = "MSG_TERMINATED"; break;
        case MSG_ICE_CANDIDATE: message_name = "MSG_ICE_CANDIDATE"; break;
        case MSG_SOURCE_ADDED: message_name = "MSG_SOURCE_ADDED"; break;
        case MSG_SOURCE_REMOVED: message_name = "MSG_SOURCE_REMOVED"; break;
        case MSG_START_PLAYBACK: message_name = "MSG_START_PLAYBACK"; break;
        case MSG_STOP_PLAYBACK: message_name = "MSG_STOP_PLAYBACK"; break;
        case MSG_CAPTURE_ADDED: message_name = "MSG_CAPTURE_ADDED"; break;
        case MSG_CAPTURE_REMOVED: message_name = "MSG_CAPTURE_REMOVED"; break;
        case MSG_START_CAPTURE: message_name = "MSG_START_CAPTURE"; break;
        case MSG_STOP_CAPTURE: message_name = "MSG_STOP_CAPTURE"; break;
        case MSG_SET_CURRENT_DEVICE: message_name = "MSG_SET_CURRENT_DEVICE"; break;
        case MSG_SET_VOLUME_LEVEL: message_name = "MSG_SET_VOLUME_LEVEL"; break;
        case MSG_SET_VIDEO_CONSTRAINTS_INT: message_name = "MSG_SET_VIDEO_CONSTRAINTS_INT"; break;
        case MSG_SET_VIDEO_CONSTRAINTS_DOUBLE: message_name = "MSG_SET_VIDEO_CONSTRAINTS_DOUBLE"; break;
        case MSG_SET_CAPTURE_FEEDBACK_INTERVAL: message_name = "MSG_SET_CAPTURE_FEEDBACK_INTERVAL"; break;
        default: break;
    }
    //LOG_INFO_STREAM << "SdkWorker: "<< call_state_->name() << " state received " << message_name << std::endl;
    LOG_INFO(message_name);
    call_state_->process(msg);
    delete msg->pdata;
}

void TwilioSdkWorker::onSdpCreationFailed(const std::string &error) {
    switch (callParams_->callState) {
        case INITIATING_CALL:
            post(MSG_SDP_CREATE_FAILED,
                 talk_base::WrapMessageData(error));
            break;
        case ACCEPTING_CALL:
            post(MSG_OFFER_ACCEPT_FAILED,
                 talk_base::WrapMessageData(error));
            break;
        case INCOMING_CALL:
            post(MSG_REJECT,
                 talk_base::WrapMessageData(error));
            break;
        case CONNECTING:
            post(MSG_ANSWER_ACCEPT_FAILED,
                 talk_base::WrapMessageData(error));
            break;
        default:
            break;
    }
}

void TwilioSdkWorker::onSdpCreated(const std::string& sdp) {
    post(MSG_SDP_CREATED, talk_base::WrapMessageData(sdp));
}

void TwilioSdkWorker::onIceCandidate(const std::string& candidate) {
    post(MSG_ICE_CANDIDATE, talk_base::WrapMessageData(candidate));
}

void TwilioSdkWorker::onLocalSdpFinished(const std::string& sdp) {
    post(MSG_LOCAL_SDP_FINISHED, talk_base::WrapMessageData(sdp));
}

void TwilioSdkWorker::onIceConnected() {
    notifyCallEvent(CONNECTED);
}

void TwilioSdkWorker::onAnswerAccepted() {
    post(MSG_ANSWER_ACCEPTED, NULL);
}

void TwilioSdkWorker::onSourceAdded(const std::string& source,
                                    const TwilioSdkMediaType type) {
    post(MSG_SOURCE_ADDED,
         talk_base::WrapMessageData(MediaSourceData(source, type)));
}

void TwilioSdkWorker::onSourceRemoved(const std::string& source) {
    post(MSG_SOURCE_REMOVED,
         talk_base::WrapMessageData(MediaSourceData(source,
                                                    TYPE_UNKNOWN_MEDIA)));
}

void TwilioSdkWorker::onSourceDataAvailable(const std::string& source) {
    // Pass this directly as we have too many events here
    if (observer_) {
        observer_->onSourceDataAvailable(source);
    }
}

void TwilioSdkWorker::onSourceVideoTrack(const std::string& source,
                                         const uint32 width, const uint32 height,
                                         uint8_t** buffer,
                                         TwilioSdkSourceFormat* desired_type) {
    // Pass this directly as we need buffer & type back
    if (observer_) {
        observer_->onSourceVideoTrack(source, width, height, buffer,
                                      *desired_type);
    }
}

void TwilioSdkWorker::onCaptureAdded(const std::string& source,
                                     const TwilioSdkMediaType type) {
    post(MSG_CAPTURE_ADDED,
         talk_base::WrapMessageData(MediaSourceData(source, type)));
}

void TwilioSdkWorker::onCaptureRemoved(const std::string& source,
                                       const TwilioSdkMediaType type) {
    post(MSG_CAPTURE_REMOVED,
         talk_base::WrapMessageData(MediaSourceData(source, type)));
}

void TwilioSdkWorker::onCaptureFeedbackAvailable(const uint32_t last_effective_width,
                                                 const uint32_t last_effective_height,
                                                 const uint32_t last_effective_framerate) {
    if (this->observer_) {
        this->observer_->onCaptureFeedbackAvailable(last_effective_width,
                                                    last_effective_height,
                                                    last_effective_framerate);
    }
}

void TwilioSdkWorker::onSipAccountIncomingCall(const int call_id) {
    sip_call_.reset(TwilioSipCall::get(call_id));
    sip_call_->subscribe(this);
    post(MSG_RECEIVE_CALL,
         talk_base::WrapMessageData(sip_call_->remoteOffer()));
}

void TwilioSdkWorker::onSipAccountStateChanged(const TwilioSdkInitState state) {
    LOG_INFO_STREAM << "SdkWorker: User state changed to " << state << std::endl;
    switch (initState_) {
        case INITIALIZING:
            initStateTransition(state);
            break;
        case LOGGINGIN:
            if (INITIALIZED == state || LOGGINGIN_ERROR == state) {
                initStateTransition(state);
            }
            break;
        default:
            break;
    }
}

void TwilioSdkWorker::onSipAccountRemoteTrickleSupported(const bool supported) {
    LOG_INFO_STREAM << "SdkWorker: remote Trickle ICE support: " << supported << std::endl;
    if (supported) {
        post(MSG_REMOTE_TRICKLE_SUPPORT, NULL);
    }
}

void TwilioSdkWorker::onSipCallStateChanged(const TwilioSdkCallState state) {
    LOG_INFO_STREAM << "SdkWorker: Call state changed to " << state << std::endl;
    switch (state) {
        case CONNECTING:
            post(MSG_CALL_CONNECTING, NULL);
            break;
        case CONNECTED:
            if (callParams_->callMode == INITIATE) {
                post(MSG_ANSWER_RECEIVED,
                     talk_base::WrapMessageData(sip_call_->remoteOffer()));
            }
            break;
        case USER_NOT_AVAILABLE:
            post(MSG_USER_NOT_AVAILABLE, NULL);
            break;
        case REJECTED:
            post(MSG_CALL_REJECTED, NULL);
            break;
        case TERMINATED:
            post(MSG_TERMINATED, NULL);
            break;
        case RINGING:
            post(MSG_RINGING, NULL);
            break;

        default:
            break;
    }
}

void TwilioSdkWorker::onSipInfoMethodReceived(const std::string& data) {
    connection_manager_->addRemoteCandidate(data);
}

void TwilioSdkWorker::onSipPrackMethodReceived() {
    post(MSG_PRACK_RECEIVED, NULL);
}

void TwilioSdkWorker::initStateTransition(TwilioSdkInitState state) {
    initState_ = state;
    if (observer_) {
        observer_->onInitStateChange(initState_);
    }
}

void TwilioSdkWorker::notifyCallEvent(TwilioSdkCallState state) {
    if (callParams_->callState == state) {
        return;
    }
    callParams_->callState = state;
    if (observer_) {
        LOG_INFO_STREAM << "SdkWorker: MSG_XXX Notify call event " << state << std::endl;
        observer_->onCallStateChange(callParams_);
    }
}

void TwilioSdkWorker::changeCallState(BaseState* state) {
    std::string old_state = call_state_.get() ? call_state_->name() : "(null)";
    std::string new_state = state ? state->name() : "(null)";
    LOG_INFO_STREAM << "SdkWorker: MSG_XXX Change call state from "
            << old_state << " to " << new_state << std::endl;
    call_state_.reset(state);
}

void TwilioSdkWorker::BaseState::process(talk_base::Message *msg) {
    switch (msg->message_id) {
        case MSG_SOURCE_ADDED: {
            MediaSourceData data = talk_base::UseMessageData<
                    MediaSourceData>(msg->pdata);
            if (worker_->observer_) {
                worker_->observer_->onSourceAdded(data.source_id_,
                                                  data.source_type_);
            }
            break;
        }
        case MSG_SOURCE_REMOVED: {
            MediaSourceData data = talk_base::UseMessageData<
                    MediaSourceData>(msg->pdata);
            if (worker_->observer_) {
                worker_->observer_->onSourceRemoved(data.source_id_);
            }
            break;
        }
        case MSG_START_PLAYBACK: {
            MediaSourceData data = talk_base::UseMessageData<
                    MediaSourceData>(msg->pdata);
            worker_->connection_manager_->startPlayback(data.source_id_);
            break;
        }
        case MSG_STOP_PLAYBACK: {
            MediaSourceData data = talk_base::UseMessageData<
                    MediaSourceData>(msg->pdata);
           worker_->connection_manager_->stopPlayback(data.source_id_);
            break;
        }
        case MSG_CAPTURE_ADDED: {
            MediaSourceData data = talk_base::UseMessageData<
                    MediaSourceData>(msg->pdata);
            if (worker_->observer_) {
                worker_->observer_->onCaptureAdded(data.source_id_,
                                                   data.source_type_);
            }
            break;
        }
        case MSG_CAPTURE_REMOVED: {
            MediaSourceData data = talk_base::UseMessageData<
                    MediaSourceData>(msg->pdata);
            if (worker_->observer_) {
                worker_->observer_->onCaptureRemoved(data.source_id_,
                                                     data.source_type_);
            }
            break;
        }
        case MSG_START_CAPTURE: {
            MediaSourceData data = talk_base::UseMessageData<
                    MediaSourceData>(msg->pdata);
            worker_->connection_manager_->startCapture(data.source_type_);
            break;
        }
        case MSG_STOP_CAPTURE: {
            MediaSourceData data = talk_base::UseMessageData<
                    MediaSourceData>(msg->pdata);
            worker_->connection_manager_->stopCapture(data.source_type_);
            break;
        }
        case MSG_SET_CURRENT_DEVICE: {
            MediaSourceData data = talk_base::UseMessageData<
                    MediaSourceData>(msg->pdata);
            bool device_changed =
                    TwilioDeviceManager::instance().setCurrentDevice(data.source_type_,
                                                                     data.source_id_);
            if (device_changed
                    && worker_->connection_manager_->isCapturing(data.source_type_)) {
                // restart capture on device change
                worker_->connection_manager_->stopCapture(data.source_type_);
                worker_->connection_manager_->startCapture(data.source_type_);
            }
            break;
        }
        case MSG_SET_VOLUME_LEVEL: {
            const int level = talk_base::UseMessageData<int>(msg->pdata);
            worker_->connection_manager_->setVolume(level);
            break;
        }
        case MSG_SET_VIDEO_CONSTRAINTS_INT: {
            VideoConstraintsData<int> data =
                    talk_base::UseMessageData<VideoConstraintsData<int>>(msg->pdata);
            worker_->connection_manager_->setVideoConstraintsInt(data.key_,
                                                                 data.value_,
                                                                 data.type_);
            break;
        }
        case MSG_SET_VIDEO_CONSTRAINTS_DOUBLE: {
            VideoConstraintsData<double> data =
                    talk_base::UseMessageData<VideoConstraintsData<double>>(msg->pdata);
            worker_->connection_manager_->setVideoConstraintsDouble(data.key_,
                                                                    data.value_,
                                                                    data.type_);
            break;
        }
        case MSG_SET_CAPTURE_FEEDBACK_INTERVAL: {
            worker_->connection_manager_->setCaptureFeedbackInterval(
                    talk_base::UseMessageData<int64_t>(msg->pdata));
            break;
        }
        default:
            LOG_WARN_STREAM << "SdkWorker: Unsupported message " << std::endl;
            break;
    }
}

void TwilioSdkWorker::IdleState::process(talk_base::Message *msg) {
    switch (msg->message_id) {
        case MSG_START_CALL: {
            worker_->callParams_->remoteUser =
                    talk_base::UseMessageData<std::string>(msg->pdata);
            worker_->callParams_->callMode = INITIATE;
            worker_->sip_account_->options(*worker_->initParams_,
                                           worker_->callParams_->remoteUser);
            // initialize webrtc peer connection
            if (!worker_->connection_manager_->initConnection(worker_->initParams_)) {
                LOG_ERROR("SdkWorker: Failed to initialize peer connection");
                worker_->notifyCallEvent(INITIATING_CALL_FAILED);
            } else {
                // start creating offer
                worker_->notifyCallEvent(INITIATING_CALL);
                worker_->connection_manager_->createOffer();
                worker_->changeCallState(new InitiatorStartState(worker_));
            }
            break;
        }
        case MSG_RECEIVE_CALL: {
            worker_->callParams_->callId = worker_->sip_call_->getId();
            worker_->callParams_->remoteUser = worker_->sip_call_->remoteUser();
            worker_->callParams_->callMode = ANSWER;
            // initialize webrtc peer connection
            if (!worker_->connection_manager_->initConnection(worker_->initParams_)) {
                worker_->notifyCallEvent(INITIATING_CALL_FAILED);
                worker_->sip_call_->reject();
                worker_->post(MSG_TERMINATE, NULL);
            } else {
                // indicate Ringing
                std::string sdp;
                worker_->sip_call_->answer(180, sdp);
                worker_->notifyCallEvent(INCOMING_CALL);
                sdp = talk_base::UseMessageData<std::string>(msg->pdata);
                // accept an offer and start generating answer
                worker_->connection_manager_->onOfferReceived(sdp, worker_->is_trickle_ice_);
                LOG_INFO_STREAM << "SdkWorker: remote Trickle ICE support: " << worker_->is_trickle_ice_ << std::endl;
                worker_->changeCallState(new ReceiverStartState(worker_));
            }
            break;
        }
        case MSG_TERMINATE: {
            if (worker_->sip_call_.get()) {
                worker_->notifyCallEvent(DISCONNECTING);
                worker_->sip_call_->hangup();
            } else {
                worker_->post(MSG_TERMINATED, NULL);
            }
            break;
        }
        case MSG_TERMINATED: {
            worker_->is_trickle_ice_ = false;
            worker_->local_sdp_finished_ = true;
            worker_->candidates_.clear();
            worker_->answer_.clear();
            worker_->offer_.clear();
            worker_->sip_call_.reset(NULL);
            worker_->connection_manager_->stopConnection();
            worker_->notifyCallEvent(TERMINATED);
            worker_->changeCallState(new IdleState(worker_));
            break;
        }
        default:
            BaseState::process(msg);
            break;
    }
}

void TwilioSdkWorker::InitiatorStartState::process(talk_base::Message *msg) {
    switch (msg->message_id) {
        case MSG_REMOTE_TRICKLE_SUPPORT: {
            worker_->is_trickle_ice_ = true;
            if (!worker_->offer_.empty()) {
                //If we already received initial sdp then resend it
                worker_->post(MSG_SDP_CREATED, talk_base::WrapMessageData(worker_->offer_));
            }
            worker_->changeCallState(new InitiatorTrickleStartState(worker_));
            break;
        }
        case MSG_SDP_CREATED: {
            // Just store SDP and wait for the remote options answer
            worker_->offer_ = talk_base::UseMessageData<std::string>(msg->pdata);
            break;
        }
        case MSG_ICE_CANDIDATE: {
            if (msg->pdata) {
                std::string candidate =
                        talk_base::UseMessageData<std::string>(msg->pdata);
                worker_->candidates_.append(candidate);
            }
            break;
        }
        case MSG_LOCAL_SDP_FINISHED: {
            worker_->offer_ = talk_base::UseMessageData<std::string>(msg->pdata);
            worker_->sip_call_.reset(new TwilioSipCall(worker_->sip_account_.get()));
            worker_->sip_call_->subscribe(worker_);
            worker_->sip_call_->call(*worker_->callParams_, worker_->offer_);
            worker_->changeCallState(new InitiatorConnectingState(worker_));
            break;
        }
        case MSG_SDP_CREATE_FAILED: {
            LOG_WARN_STREAM << talk_base::UseMessageData<std::string>(msg->pdata) << std::endl;
            worker_->notifyCallEvent(INITIATING_CALL_FAILED);
            worker_->post(MSG_TERMINATE, NULL);
            break;
        }
        case MSG_RINGING: {
            worker_->notifyCallEvent(RINGING);
            break;
        }
        case MSG_CALL_REJECTED: {
            worker_->notifyCallEvent(REJECTED);
            worker_->post(MSG_TERMINATED, NULL);
            break;
        }
        case MSG_USER_NOT_AVAILABLE: {
            worker_->notifyCallEvent(USER_NOT_AVAILABLE);
            worker_->post(MSG_TERMINATED, NULL);
            break;
        }
        default:
            IdleState::process(msg);
            break;
    }
}

void TwilioSdkWorker::InitiatorTrickleStartState::process(talk_base::Message *msg) {
    switch (msg->message_id) {
        case MSG_SDP_CREATED: {
            worker_->offer_ = talk_base::UseMessageData<std::string>(msg->pdata);
            worker_->connection_manager_->addTrickleIceSupport(worker_->offer_);
            worker_->sip_call_.reset(new TwilioSipCall(worker_->sip_account_.get()));
            worker_->sip_call_->subscribe(worker_);
            worker_->sip_call_->call(*worker_->callParams_, worker_->offer_);
            // Clear offer to be set with final
            worker_->offer_.clear();
            break;
        }
        case MSG_LOCAL_SDP_FINISHED: {
            // Just store SDP for further reInvites
            worker_->offer_ = talk_base::UseMessageData<std::string>(msg->pdata);
            break;
        }
        case MSG_CALL_CONNECTING: {
            worker_->notifyCallEvent(CONNECTING);
            worker_->connection_manager_->onAnswerReceived(worker_->sip_call_->remoteOffer());
            worker_->post(MSG_ICE_CANDIDATE, NULL);
            if (!worker_->offer_.empty()) {
                worker_->post(MSG_LOCAL_SDP_FINISHED, talk_base::WrapMessageData(worker_->offer_));
            }
            worker_->changeCallState(new InitiatorTricklingState(worker_));
            break;
        }
        default:
            InitiatorStartState::process(msg);
            break;
    }
}

void TwilioSdkWorker::InitiatorTricklingState::process(talk_base::Message *msg) {
    switch (msg->message_id) {
        case MSG_ANSWER_RECEIVED: {
            worker_->answer_ = talk_base::UseMessageData<std::string>(msg->pdata);
            if (!worker_->offer_.empty()) {
                // repost LOCAL_SDP_FINISHED if received previosly
                worker_->post(MSG_LOCAL_SDP_FINISHED, talk_base::WrapMessageData(worker_->offer_));
            }
            break;
        }
        case MSG_ICE_CANDIDATE: {
            if (msg->pdata) {
                std::string candidate =
                        talk_base::UseMessageData<std::string>(msg->pdata);
                worker_->candidates_.append(candidate);
            }
            worker_->sip_call_->info(worker_->candidates_);
            worker_->candidates_.clear();
            break;
        }
        case MSG_LOCAL_SDP_FINISHED: {
            worker_->offer_ = talk_base::UseMessageData<std::string>(msg->pdata);
            if (!worker_->answer_.empty()) {
                // we've already got an answer, send reINVITE
                worker_->sip_call_->reInvite(worker_->offer_);
                // we've provided answer previously
                worker_->post(MSG_ANSWER_ACCEPTED, NULL);
                worker_->changeCallState(new InitiatorConnectingState(worker_));
            }
            break;
        }
        default:
            IdleState::process(msg);
            break;
    }
}

void TwilioSdkWorker::InitiatorConnectingState::process(talk_base::Message *msg) {
    switch (msg->message_id) {
        case MSG_ANSWER_RECEIVED: {
             // set remote description and complete the setup
            worker_->connection_manager_->onAnswerReceived(
                    talk_base::UseMessageData<std::string>(msg->pdata));
            break;
        }
        case MSG_ANSWER_ACCEPTED: {
            worker_->notifyCallEvent(CONNECTED);
            worker_->changeCallState(new ConnectedState(worker_));
            break;
        }
        case MSG_ANSWER_ACCEPT_FAILED: {
            worker_->notifyCallEvent(CONNECTING_FAILED);
            worker_->post(MSG_TERMINATE, NULL);
            break;
        }
        case MSG_RINGING: {
            worker_->notifyCallEvent(RINGING);
            break;
        }
        case MSG_CALL_REJECTED: {
            worker_->notifyCallEvent(REJECTED);
            worker_->post(MSG_TERMINATED, NULL);
            break;
        }
        case MSG_USER_NOT_AVAILABLE: {
            worker_->notifyCallEvent(USER_NOT_AVAILABLE);
            worker_->post(MSG_TERMINATED, NULL);
            break;
        }
        default:
            IdleState::process(msg);
            break;
    }
}

void TwilioSdkWorker::ReceiverStartState::process(talk_base::Message *msg) {
    switch (msg->message_id) {
        case MSG_OFFER_ACCEPTED: {
            worker_->notifyCallEvent(ACCEPTING_CALL);
            if (worker_->is_trickle_ice_) {
                if (!worker_->offer_.empty()) {
                    worker_->post(MSG_SDP_CREATED, talk_base::WrapMessageData(worker_->offer_));
                }
                worker_->changeCallState(new ReceiverTrickleStartState(worker_));
            } else {
                if (!worker_->offer_.empty()) {
                    worker_->post(MSG_LOCAL_SDP_FINISHED, talk_base::WrapMessageData(worker_->offer_));
                }
                worker_->changeCallState(new ReceiverConnectingState(worker_));
            }
            break;
        }
        case MSG_REJECT: {
            worker_->notifyCallEvent(DISCONNECTING);
            worker_->sip_call_->reject();
            break;
        }
        case MSG_OFFER_ACCEPT_FAILED: {
            LOG_WARN_STREAM << talk_base::UseMessageData<std::string>(msg->pdata) << std::endl;
            worker_->notifyCallEvent(ACCEPTING_CALL_FAILED);
            worker_->post(MSG_TERMINATE, NULL);
            break;
        }
        case MSG_SDP_CREATED: {
            if (worker_->is_trickle_ice_) {
                // just save for future resending if trickel ice mode
                worker_->offer_ = talk_base::UseMessageData<std::string>(msg->pdata);
            }
            break;
        }
        case MSG_ICE_CANDIDATE: {
            if (msg->pdata) {
                std::string candidate =
                        talk_base::UseMessageData<std::string>(msg->pdata);
                worker_->candidates_.append(candidate);
            }
            break;
        }
        case MSG_LOCAL_SDP_FINISHED: {
            worker_->local_sdp_finished_ = true;
            // just save for future resending
            worker_->offer_ = talk_base::UseMessageData<std::string>(msg->pdata);
            break;
        }
        default:
            IdleState::process(msg);
            break;
    }
}

void TwilioSdkWorker::ReceiverTrickleStartState::process(talk_base::Message *msg) {
    switch (msg->message_id) {
        case MSG_SDP_CREATED: {
            std::string sdp = talk_base::UseMessageData<std::string>(msg->pdata);
            worker_->sip_call_->answer(183, sdp);
            break;
        }
        case MSG_ICE_CANDIDATE: {
            if (msg->pdata) {
                std::string candidate =
                        talk_base::UseMessageData<std::string>(msg->pdata);
                worker_->candidates_.append(candidate);
            }
            break;
        }
        case MSG_LOCAL_SDP_FINISHED: {
            worker_->local_sdp_finished_ = true;
            // just save for future resending
            worker_->offer_ = talk_base::UseMessageData<std::string>(msg->pdata);
            break;
        }
        case MSG_PRACK_RECEIVED: {
            worker_->post(MSG_ICE_CANDIDATE, NULL);
            worker_->changeCallState(new ReceiverTricklingState(worker_));
            break;
        }
        default:
            IdleState::process(msg);
            break;
    }
}

void TwilioSdkWorker::ReceiverTricklingState::process(talk_base::Message *msg) {
    switch (msg->message_id) {
        case MSG_ICE_CANDIDATE: {
            if (msg->pdata) {
                std::string candidate =
                        talk_base::UseMessageData<std::string>(msg->pdata);
                worker_->candidates_.append(candidate);
            }
            worker_->sip_call_->info(worker_->candidates_);
            worker_->candidates_.clear();
            //resend message if we've received it previous
            if (worker_->local_sdp_finished_) {
                worker_->post(MSG_LOCAL_SDP_FINISHED, talk_base::WrapMessageData(worker_->offer_));
            }
            break;
        }
        case MSG_LOCAL_SDP_FINISHED: {
            worker_->local_sdp_finished_ = true;
            worker_->offer_ = talk_base::UseMessageData<std::string>(msg->pdata);
            worker_->post(MSG_LOCAL_SDP_FINISHED, talk_base::WrapMessageData(worker_->offer_));
            worker_->changeCallState(new ReceiverConnectingState(worker_));
            break;
        }
        default:
            ReceiverTrickleStartState::process(msg);
            break;
    }
}

void TwilioSdkWorker::ReceiverConnectingState::process(talk_base::Message *msg) {
    switch (msg->message_id) {
        case MSG_LOCAL_SDP_FINISHED: {
            worker_->offer_ = talk_base::UseMessageData<std::string>(msg->pdata);
            if (worker_->is_trickle_ice_) {
                std::string sdp;
                worker_->sip_call_->answer(200, sdp);
                // set the offer for the subsequent 200 answers generation
                worker_->sip_call_->setLocalOffer(worker_->offer_);
            } else {
                worker_->sip_call_->answer(200, worker_->offer_);
            }
            worker_->changeCallState(new ConnectedState(worker_));
            break;
        }
        default:
            IdleState::process(msg);
            break;
    }
}

void TwilioSdkWorker::ConnectedState::process(talk_base::Message *msg) {
    switch (msg->message_id) {
        default:
            IdleState::process(msg);
            break;
    }
}

} // namespace twiliosdk
