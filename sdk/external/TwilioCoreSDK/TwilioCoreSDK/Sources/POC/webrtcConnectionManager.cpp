#include <stdlib.h>
#if defined(POSIX)
#include <sys/time.h>
#endif

#ifdef DARWIN
#pragma GCC diagnostic ignored "-Wdeprecated-declarations"
#endif // DARWIN

#include <iostream>

#include "webrtcConnectionManager.h"
#include "twilioDeviceManager.h"
#include "twilioIdentityService.h"
#include "twilioUtils.h"
#include "twilioVideoCapturer.h"
#include "twilioscreencapturer.h"

namespace twiliosdk {

const std::string kEndOfICECandidates = "a=end-of-candidates";
const std::string kMidICECandidates = "a=mid:";
// c-tor
WebrtcConnectionManager::WebrtcConnectionManager(WebrtcConnectionManagerObserverInterface *observer)
    : observer_(observer),
      video_capturer_(NULL) {
    // create peer connection factory
    peer_connection_factory_ = webrtc::CreatePeerConnectionFactory();
    TwilioDeviceManager::instance().init((webrtc::PeerConnectionFactory*)peer_connection_factory_.get());

    // create set session description observer
    sdp_observer_ = new SetSessionDescriptionObserver(this);

    // Setup peer connection constraints
    peer_connection_constraints_.SetAllowDtlsSctpDataChannels();

    // Setup sdp offer constraints
    sdp_constraints_.SetMandatoryReceiveAudio(false);
    sdp_constraints_.SetMandatoryReceiveVideo(false);
}

void WebrtcConnectionManager::setCaptureFeedbackInterval(int64_t timeInMsec) {
    if (video_capturer_) {
        static_cast<TwilioVideoCapturer*>(video_capturer_)->setCaptureFeedbackInterval(timeInMsec);
    } else {
        LOG_ERROR_STREAM << "WebrtcConnectionManager: Capture not yet added! Cannot set feedback interval at this time." << std::endl;
    }
}

void WebrtcConnectionManager::setVideoConstraintsInt(TwilioSdkVideoConstraints key, int value, TwilioSdkConstraintType type) {
    switch (key) {
        case MIN_WIDTH: {
            type ? video_constraints_.SetOptionalMinWidth(value) : video_constraints_.SetMandatoryMinWidth(value);
            break;
        }                       
        case MAX_WIDTH: {
            type ? video_constraints_.SetOptionalMaxWidth(value) : video_constraints_.SetMandatoryMaxWidth(value);
            break;
        }                       
        case MIN_HEIGHT: {
            type ? video_constraints_.SetOptionalMinHeight(value) : video_constraints_.SetMandatoryMinHeight(value);
            break;
        }                       
        case MAX_HEIGHT: {
            type ? video_constraints_.SetOptionalMaxHeight(value) : video_constraints_.SetMandatoryMaxHeight(value);
            break;
        }                       
        case MIN_FRAMERATE: {
            type ? video_constraints_.SetOptionalMinFrameRate(value) : video_constraints_.SetMandatoryMinFrameRate(value);
            break;
        }                       
        case MAX_FRAMERATE: {
            type ? video_constraints_.SetOptionalMaxFrameRate(value) : video_constraints_.SetMandatoryMaxFrameRate(value);
            break;
        }                       
        default: {
            LOG_INFO("WebrtcConnectionManager: Unknown video constraint with value of type int");
            break;
        }
    }
}

void WebrtcConnectionManager::setVideoConstraintsDouble(TwilioSdkVideoConstraints key, double value, TwilioSdkConstraintType type) {
    switch (key) {
        case MIN_ASPECT_RATIO: {
            type ? video_constraints_.SetOptionalMinAspectRatio(value) : video_constraints_.SetMandatoryMinAspectRatio(value);
            break;
        }
        case MAX_ASPECT_RATIO: {
            type ? video_constraints_.SetOptionalMaxAspectRatio(value) : video_constraints_.SetMandatoryMaxAspectRatio(value);
            break;
        }
        default: {
            LOG_INFO("WebrtcConnectionManager: Unknown media constraint with value of type double");
            break;
        }
    }
}

// init peer connection
bool WebrtcConnectionManager::initConnection(TwilioSdkInitParams *params) {
    bool result = false;
    std::string audioLabel;
    std::string videoLabel;
    std::string streamLabel;

    // initialize ICE servers
    webrtc::PeerConnectionInterface::IceServer server;
    if ( params->stun[0] != '\0' ) {
        server.uri = params->stun;
        ice_servers_.push_back(server);
    }
    if ( params->turn[0] != '\0' ) {
        server.uri = params->turn;
        server.username = params->user;
        server.password = params->password;
        ice_servers_.push_back(server);
    }

    LOG_INFO("WebrtcConnectionManager: Creating PeerConnection");
    peer_connection_ = peer_connection_factory_->CreatePeerConnection(
            ice_servers_, &peer_connection_constraints_, NULL,
            new TwilioIdentityService(), this);

    // generate audio/video/stream labels
    // generate audio/video/stream labels
    if (TwilioUtils::generateUniqueId(params->user, audioLabel) &&
            TwilioUtils::generateUniqueId(params->user, videoLabel) &&
            TwilioUtils::generateUniqueId(params->user, streamLabel) ) {
        // create the audio track
        audio_track_ = peer_connection_factory_->CreateAudioTrack(
                audioLabel,
                peer_connection_factory_->CreateAudioSource(NULL));

        //will be owned by track, so no need to own
        video_capturer_ = new TwilioVideoCapturer();
        
        static_cast<TwilioVideoCapturer*>(video_capturer_)->SignalCaptureFeedbackAvailable.connect(observer_,
                    &WebrtcConnectionManagerObserverInterface::onCaptureFeedbackAvailable);
        //video_capturer_ = new TwilioScreenCapturer();
        // create the video source
        video_source_ = peer_connection_factory_->CreateVideoSource(video_capturer_, &video_constraints_);
        const cricket::VideoFormat *format = video_capturer_->GetCaptureFormat();
        if (format) {
            best_format_.width = format->width;
            best_format_.height = format->height;
            best_format_.interval = format->interval;
            best_format_.fourcc = format->fourcc;
        }
        // create the video track
        video_track_ = peer_connection_factory_->CreateVideoTrack(
                videoLabel,
                video_source_);
        // create the local media stream
        stream_ = peer_connection_factory_->CreateLocalMediaStream(streamLabel);

        // add the audio track to the stream
        stream_->AddTrack(audio_track_);
        LOG_INFO("WebrtcConnectionManager: Added audio track");

        // add the video track to the stream
        stream_->AddTrack(video_track_);
        LOG_INFO("WebrtcConnectionManager: Added video track");

        // add the stream to the connection
        if ( !peer_connection_->AddStream(stream_, NULL) ) {
            LOG_ERROR("WebrtcConnectionManager: Adding stream to PeerConnection failed");
        } else {
            LOG_INFO("WebrtcConnectionManager: Added stream to PeerConnection");
            observer_->onCaptureAdded(audioLabel, TYPE_AUDIO_INPUT);

            if (video_capturer_->IsRunning()) {
                observer_->onCaptureAdded(videoLabel, TYPE_VIDEO_INPUT);
                // add our local video track
                addVideoTrackRenderer(streamLabel, videoLabel, video_track_);
                observer_->onSourceAdded(videoLabel, TYPE_VIDEO_INPUT);
            }
            result = true;
        }
    }
    // return the result
    return result;
}

// close and cleanup peer connection
void WebrtcConnectionManager::stopConnection() {
    // close/cleanup peer connection, stream, tracks
    if (video_capturer_) {
        video_capturer_->Stop();
        video_capturer_ = NULL;
    }
    if (peer_connection_.get()) {
        peer_connection_->Close();

        video_renderers_.clear();
        audio_renderers_.clear();

        if (stream_.get() ) {
            peer_connection_->RemoveStream(stream_.get());

            if (audio_track_.get()) {
                stream_->RemoveTrack(audio_track_.get());
            }

            if (video_track_.get()) {
                stream_->RemoveTrack(video_track_.get());
            }

            stream_.release();
        }
        audio_track_.release();
        video_track_.release();
        peer_connection_.release();
    }
}

void WebrtcConnectionManager::startPlayback(const std::string& source_id) {
    // find source - Video only so far
    for (VideoRendererMap::iterator it = video_renderers_.begin();
            it != video_renderers_.end(); ++it) {
        if (it->second->id() == source_id) {
            it->second->start();
            it->second->SignalDataAvailable.connect(observer_,
                                                    &WebrtcConnectionManagerObserverInterface::onSourceDataAvailable);
            break;
        }
    }
}

void WebrtcConnectionManager::stopPlayback(const std::string& source_id) {
    // find source - Video only so far
    for (VideoRendererMap::iterator it = video_renderers_.begin();
            it != video_renderers_.end(); ++it) {
        if (it->second->id() == source_id) {
            it->second->stop();
            it->second->SignalDataAvailable.disconnect(observer_);
            break;
        }
    }
}

void WebrtcConnectionManager::startCapture(TwilioSdkMediaType type) {
    if (TYPE_VIDEO_INPUT == type && !video_track_.get()) {
        return;
    }
    if (TYPE_AUDIO_INPUT == type && !audio_track_.get()) {
        return;
    }
    switch (type) {
        case TYPE_VIDEO_INPUT:
            //Start capturing
            video_capturer_->Start(best_format_);
            if (video_capturer_->IsRunning()) {
                video_track_->set_enabled(true);
                observer_->onCaptureAdded(video_track_->id(), TYPE_VIDEO_INPUT);
                observer_->onSourceAdded(video_track_->id(), TYPE_VIDEO_INPUT);
            }
            break;
        case TYPE_AUDIO_INPUT:
            audio_track_->set_enabled(true);
            observer_->onCaptureAdded(audio_track_->id(), TYPE_AUDIO_INPUT);
            break;
        default:
            break;
    }
}

void WebrtcConnectionManager::stopCapture(TwilioSdkMediaType type) {
    if (TYPE_VIDEO_INPUT == type && !video_track_.get()) {
        return;
    }
    if (TYPE_AUDIO_INPUT == type && !audio_track_.get()) {
        return;
    }
    switch (type) {
        case TYPE_VIDEO_INPUT:
            // Stop capturing
            video_track_->set_enabled(false);
            if (video_capturer_) {
                video_capturer_->Stop();
            }
            observer_->onCaptureRemoved(video_track_->id(), TYPE_VIDEO_INPUT);
            stopPlayback(video_track_->id());
            observer_->onSourceRemoved(video_track_->id());
            break;
        case TYPE_AUDIO_INPUT:
            audio_track_->set_enabled(false);
            observer_->onCaptureRemoved(audio_track_->id(), TYPE_AUDIO_INPUT);
            break;
        default:
            break;
    }
}

bool WebrtcConnectionManager::isCapturing(TwilioSdkMediaType type) {
    switch (type) {
        case TYPE_VIDEO_INPUT:
            return video_track_.get() && video_track_->enabled() &&
                    video_capturer_ && video_capturer_->IsRunning();
            break;
        case TYPE_AUDIO_INPUT:
            return audio_track_.get() && audio_track_->enabled();
            break;
        default:
            return false;
            break;
    }
}

void WebrtcConnectionManager::setVolume(int level) {
    LOG_DEBUG_STREAM << "WebrtcConnectionManager: Set volume level to " << level << std::endl;
    ((webrtc::PeerConnectionFactory*)peer_connection_factory_.get())->channel_manager()->SetOutputVolume(level);
}

void WebrtcConnectionManager::getVolume(int &level) {
    ((webrtc::PeerConnectionFactory*)peer_connection_factory_.get())->channel_manager()->GetOutputVolume(&level);
}

// create SDP offer
void WebrtcConnectionManager::createOffer() {
    LOG_INFO("WebrtcConnectionManager: Creating offer");
    peer_connection_->CreateOffer(this, &sdp_constraints_);
}

// for debugging purposes
void WebrtcConnectionManager::printSDP(const webrtc::SessionDescriptionInterface *sdp) {
    std::string temp;
    sdp->ToString(&temp);
    LOG_DEBUG(temp);
}

void WebrtcConnectionManager::addRemoteCandidate(const std::string& candidates) {
    LOG_INFO("WebrtcConnectionManager: Got remote ICE candidates");

    std::string::size_type start = 0;
    std::string::size_type end = start;
    std::vector<std::string> candidatesList;
    while ((end = candidates.find("\r\n", start)) != std::string::npos) {
        candidatesList.push_back(candidates.substr(start, end - start));
        start = end + 2;
    }
    candidatesList.push_back(candidates.substr(start));

    int media_idx = 0;
    std::string media;
    for (unsigned i = 0; i < candidatesList.size(); ++i) {
        std::string data = candidatesList[i];
        if (data.empty()) {
            continue;
        }
        if (data == kEndOfICECandidates) {
            // end of candidates
        } else if (data.find(kMidICECandidates) != std::string::npos) {
            // candidate media data
            std::string::size_type separator = data.find(" ");
            media = data.substr(kMidICECandidates.size(), separator - kMidICECandidates.size());
            media_idx = atoi(data.substr(separator + 1).c_str());
        } else {
            webrtc::SdpParseError e;
            LOG_DEBUG_STREAM << media_idx << " " << media << " " << data << std::endl;
            webrtc::IceCandidateInterface* ice =
                    webrtc::CreateIceCandidate(media, media_idx, data, &e);
            media_idx = 0;
            media.clear();
            if (ice) {
                peer_connection_->AddIceCandidate(ice);
            } else {
                LOG_ERROR_STREAM << "WebrtcConnectionManager: ICE candidate parse error: "
                        << e.line << ": " << e.description << std::endl;
                observer_->onSdpCreationFailed(e.description);
            }
        }
    }
}

void WebrtcConnectionManager::setTrickleEnabled(webrtc::SessionDescriptionInterface *description) {
    if (nullptr == description) {
        return;
    }

    const cricket::ContentInfos& contents = description->description()->contents();
    for (cricket::ContentInfos::const_iterator it = contents.begin(); it != contents.end(); ++it) {
        const cricket::TransportDescription* transport_desc =
            description->description()->GetTransportDescriptionByName(it->name);

        if (nullptr != transport_desc) {
            const_cast<cricket::TransportDescription*>(transport_desc)->AddOption("trickle");
        }
    }
}

bool WebrtcConnectionManager::isTrickleEnabled(webrtc::SessionDescriptionInterface *description) {
    bool result = false;
    if (nullptr == description) {
        return result;
    }

    const cricket::ContentInfos& contents = description->description()->contents();
    for (cricket::ContentInfos::const_iterator it = contents.begin(); it != contents.end(); ++it) {
        const cricket::TransportDescription* transport_desc =
            description->description()->GetTransportDescriptionByName(it->name);

        if (nullptr != transport_desc) {
            result |= const_cast<cricket::TransportDescription*>(transport_desc)->HasOption("trickle");
        }
    }

    return result;
}

// callback invoked when offer/answer is generated
void WebrtcConnectionManager::OnSuccess(webrtc::SessionDescriptionInterface *description) {
    LOG_INFO("WebrtcConnectionManager: Setting local description");
    sdp_observer_->sdpType(webrtc::SessionDescriptionInterface::kOffer);
    peer_connection_->SetLocalDescription(sdp_observer_, description);
    std::string sdp;
    description->ToString(&sdp);
    observer_->onSdpCreated(sdp);
}

// callback invoked when offer generation failed
void WebrtcConnectionManager::OnFailure(const std::string& error) {
    LOG_ERROR_STREAM << "WebrtcConnectionManager: Failed to create SDP: " << error << std::endl;
    observer_->onSdpCreationFailed(error);
}

void WebrtcConnectionManager::addTrickleIceSupport(std::string& offer) {
    const webrtc::SessionDescriptionInterface *local_sdp = peer_connection_->local_description();
    local_sdp->ToString(&offer);
    webrtc::SessionDescriptionInterface *sdp =
            webrtc::CreateSessionDescription(local_sdp->type(), offer);
    if (sdp) {
        setTrickleEnabled(sdp);
        peer_connection_->SetLocalDescription(sdp_observer_, sdp);
        sdp->ToString(&offer);
    }
}

// Handle an in bound offer
void WebrtcConnectionManager::onOfferReceived(const std::string& offer, bool& remote_supports_trickle) {
	LOG_INFO("WebrtcConnectionManager: Setting remote description");
    // let set SDP observer know we need to kick answer generation
    sdp_observer_->sdpType(webrtc::SessionDescriptionInterface::kPrAnswer);
    webrtc::SdpParseError e;
    webrtc::SessionDescriptionInterface *sdp;

    if (!(sdp = webrtc::CreateSessionDescription(webrtc::SessionDescriptionInterface::kOffer,
                                                 offer, &e))) {
        LOG_ERROR_STREAM << "WebrtcConnectionManager: SDP parse error: " << e.line << ": " << e.description << std::endl;
        observer_->onSdpCreationFailed(e.description);
    } else {
        remote_supports_trickle = isTrickleEnabled(sdp);
        peer_connection_->SetRemoteDescription(sdp_observer_, sdp);
    }
}

// Set the remote description and create an answer
void WebrtcConnectionManager::onAnswerReceived(const std::string& answer) {
    LOG_INFO("WebrtcConnectionManager: Setting remote description");

    const std::string type = webrtc::SessionDescriptionInterface::kAnswer;
    sdp_observer_->sdpType(type.c_str());

    webrtc::SdpParseError e;
    webrtc::SessionDescriptionInterface *sdp;

    if (!(sdp = webrtc::CreateSessionDescription(type, answer, &e))) {
        LOG_ERROR_STREAM << "WebrtcConnectionManager: SDP parse error: " << e.line << ": " << e.description << std::endl;
        observer_->onSdpCreationFailed(e.description);
    } else {
        peer_connection_->SetRemoteDescription(sdp_observer_, sdp);
    }
}

// called when ICE connection status changes (1 = checking, 2 = connected)
void WebrtcConnectionManager::OnIceConnectionChange(webrtc::PeerConnectionInterface::IceConnectionState state) {
    LOG_INFO_STREAM << "WebrtcConnectionManager: ICE connection state changed to: " << state << std::endl;
    if (webrtc::PeerConnectionInterface::kIceConnectionConnected == state) {
        observer_->onIceConnected();
    }
}

// called when ICE gathering state changes (2 = complete)
void WebrtcConnectionManager::OnIceGatheringChange(webrtc::PeerConnectionInterface::IceGatheringState state) {
    LOG_INFO_STREAM << "WebrtcConnectionManager: ICE gathering state changed to: " << state << std::endl;
}

// called every time new ICE candidate is discovered; this is designed for
// "ICE trickling", which we do not use since SIP signaling cannot handle it
void WebrtcConnectionManager::OnIceCandidate(const webrtc::IceCandidateInterface *candidate) {

    std::string sdpCandidate;
    candidate->ToString(&sdpCandidate);
    std::stringstream stream;
    // append media info
    stream << kMidICECandidates << candidate->sdp_mid() << " " << candidate->sdp_mline_index() << "\r\n";
    stream << sdpCandidate;
    sdpCandidate = stream.str();
    LOG_INFO_STREAM << "WebrtcConnectionManager: Got local ICE candidate " << candidate->sdp_mid() << " id: " <<
            candidate->sdp_mline_index() << std::endl;
    LOG_DEBUG(sdpCandidate);
    observer_->onIceCandidate(sdpCandidate);
}

// called when ICE candidate gathering is complete
void WebrtcConnectionManager::OnIceComplete() {
    LOG_INFO("WebrtcConnectionManager: ICE candidate discovery complete");
    //printSDP(peer_connection_->local_description());
    std::string sdpCandidate = kEndOfICECandidates;
    observer_->onIceCandidate(sdpCandidate);

    std::string sdp;
    peer_connection_->local_description()->ToString(&sdp);
    observer_->onLocalSdpFinished(sdp);
}

// not sure what is this used for
void WebrtcConnectionManager::OnRenegotiationNeeded() {
    LOG_INFO("WebrtcConnectionManager: Renegotiation is needed");
}

/**
 * PeerConnectionObserver callbacks
 */
void WebrtcConnectionManager::OnError() {
    LOG_ERROR("WebrtcConnectionManager: PeerConnection error");
}

void WebrtcConnectionManager::OnSignalingChange(
        webrtc::PeerConnectionInterface::SignalingState new_state) {
    LOG_INFO_STREAM << "WebrtcConnectionManager: Signal state change: " << new_state << std::endl;
}

void WebrtcConnectionManager::OnStateChange(
        webrtc::PeerConnectionObserver::StateType state_changed) {
    LOG_INFO_STREAM << "WebrtcConnectionManager: PeerConnection state change: " << state_changed << std::endl;
}

void WebrtcConnectionManager::OnAddStream(
        webrtc::MediaStreamInterface *stream) {
    LOG_INFO_STREAM << "WebrtcConnectionManager: Stream added: " << stream->label() << std::endl;

    for (unsigned int i = 0; i < stream->GetVideoTracks().size(); ++i) {
        std::string source_label;
        TwilioUtils::generateUniqueId(stream->label(), source_label);
        addVideoTrackRenderer(stream->label(), source_label, stream->GetVideoTracks()[i]);
        observer_->onSourceAdded(source_label, TYPE_VIDEO_OUTPUT);
    }

    for (unsigned int i = 0; i < stream->GetAudioTracks().size(); ++i) {
        std::string source_label;
        TwilioUtils::generateUniqueId(stream->label(), source_label);
        audio_renderers_.insert(
                AudioRendererPair(stream->label(), source_label));
        observer_->onSourceAdded(source_label, TYPE_AUDIO_OUTPUT);
    }
}

void WebrtcConnectionManager::OnRemoveStream(
        webrtc::MediaStreamInterface *stream) {
    LOG_INFO("WebrtcConnectionManager: Stream removed");
    // process all video tracks
    std::pair <VideoRendererMap::iterator, VideoRendererMap::iterator> video_ret =
            video_renderers_.equal_range(stream->label());

    for (VideoRendererMap::iterator it = video_ret.first; it != video_ret.second; ++it) {
        observer_->onSourceRemoved(it->second->id());
    }
    video_renderers_.erase(stream->label());
    // process all audio tracks
    std::pair <AudioRendererMap::iterator, AudioRendererMap::iterator> audio_ret =
            audio_renderers_.equal_range(stream->label());

    for (AudioRendererMap::iterator it = audio_ret.first; it != audio_ret.second; ++it) {
        observer_->onSourceRemoved(it->second);
    }
    audio_renderers_.erase(stream->label());
}

void WebrtcConnectionManager::OnDataChannel(webrtc::DataChannelInterface *dataChannel) {
    LOG_INFO_STREAM << "WebrtcConnectionManager: Remote peer opened data channel: " << dataChannel->label() << std::endl;
    observer_->onSourceAdded(dataChannel->label(), TYPE_DATA);
}

void WebrtcConnectionManager::addVideoTrackRenderer(const std::string& stream_label,
                                                    const std::string& source_id,
                                                    talk_base::scoped_refptr<webrtc::VideoTrackInterface>& videoTrack) {
    VideoRendererPair pair(stream_label,
                           new TwilioVideoTrackRenderer(source_id, videoTrack));
    pair.second->SignalVideoStarted.connect(observer_, &WebrtcConnectionManagerObserverInterface::onSourceVideoTrack);
    video_renderers_.insert(pair);
}
}  // namespace twiliosdk
