#ifndef WEBRTCCONNECTIONAMANGER_H
#define WEBRTCCONNECTIONAMANGER_H

#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunused-parameter"
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
#endif

#include "talk/base/logging.h"
#include "talk/base/openssladapter.h"

#include "talk/app/webrtc/jsep.h"
#include "talk/app/webrtc/datachannelinterface.h"
#include "talk/app/webrtc/portallocatorfactory.h"
#include "talk/app/webrtc/videosourceinterface.h"

#include "talk/media/devices/devicemanager.h"
#include "talk/media/webrtc/webrtcvideoframe.h"

#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma clang diagnostic pop
#pragma GCC diagnostic pop
#endif

#include "twiliosdk.h"
#include "twilioLogger.h"
#include "twilioWebrtcConstraints.h"
#include "twilioVideoTrackRenderer.h"
#include "twilioVideoCapturer.h"
#include "twilioIdentityService.h"

namespace twiliosdk {

// forward class declaration
class SetSessionDescriptionObserver;

typedef std::multimap<std::string, talk_base::scoped_refptr<twiliosdk::TwilioVideoTrackRenderer> >
VideoRendererMap;

typedef std::pair<std::string, talk_base::scoped_refptr<twiliosdk::TwilioVideoTrackRenderer> >
VideoRendererPair;

typedef std::multimap<std::string,std::string>
AudioRendererMap; //Store source label so far

typedef std::pair<std::string, std::string>
AudioRendererPair;

// observer that receives notifications about important events
class WebrtcConnectionManagerObserverInterface : public sigslot::has_slots<> {
 public:
    virtual void onSdpCreationFailed(const std::string &error) = 0;

    virtual void onSdpCreated(const std::string &sdp) = 0;
    virtual void onIceCandidate(const std::string& candidate) = 0;
    virtual void onLocalSdpFinished(const std::string &sdp) = 0;
    virtual void onIceConnected() = 0;
	
    virtual void onAnswerAccepted() = 0;

    virtual void onSourceAdded(const std::string& source, const TwilioSdkMediaType type) = 0;
    virtual void onSourceRemoved(const std::string& source) = 0;
    virtual void onSourceDataAvailable(const std::string& source) = 0;
    virtual void onSourceVideoTrack(const std::string& source,
                                    const uint32 width,
                                    const uint32 height,
                                    uint8_t** buffer,
                                    TwilioSdkSourceFormat* desired_type) = 0;

    virtual void onCaptureAdded(const std::string& source,
                                const TwilioSdkMediaType type) = 0;
    virtual void onCaptureRemoved(const std::string& source,
                                  const TwilioSdkMediaType type) = 0;
    virtual void onCaptureFeedbackAvailable(const uint32_t last_effective_width, 
                                            const uint32_t last_effective_height, 
                                            const uint32_t last_effective_framerate) = 0;

    virtual ~WebrtcConnectionManagerObserverInterface () {}
};

// heavy lifter
class WebrtcConnectionManager: public webrtc::PeerConnectionObserver,
public webrtc::CreateSessionDescriptionObserver {

    friend class SetSessionDescriptionObserver;

 public:
    WebrtcConnectionManager(WebrtcConnectionManagerObserverInterface *observer);
    virtual ~WebrtcConnectionManager () {
        video_capturer_ = NULL;
        stopConnection();
        ice_servers_.clear();
    }

    bool initConnection(TwilioSdkInitParams *params);
    void stopConnection();

    void setVideoConstraintsInt(TwilioSdkVideoConstraints key, int value, TwilioSdkConstraintType type);
    void setVideoConstraintsDouble(TwilioSdkVideoConstraints key, double value, TwilioSdkConstraintType type);

    void createOffer();
    void onAnswerReceived(const std::string& answer);
    void onOfferReceived(const std::string& offer, bool& remote_supports_trickle);
    void printSDP(const webrtc::SessionDescriptionInterface *sdp);
    void addRemoteCandidate(const std::string& candidates);
    void addTrickleIceSupport(std::string& offer);

    void startPlayback(const std::string& source_id);
    void stopPlayback(const std::string& source_id);

    void startCapture(TwilioSdkMediaType type);
    void stopCapture(TwilioSdkMediaType type);
    bool isCapturing(TwilioSdkMediaType type);
    void setCaptureFeedbackInterval(int64_t timeInMsec);

    void setVolume(int level);
    void getVolume(int &level);

    // inherited from PeerConnection Observer
    virtual void OnError();
    virtual void OnSignalingChange(webrtc::PeerConnectionInterface::SignalingState new_state);
    virtual void OnStateChange(webrtc::PeerConnectionObserver::StateType state_changed);
    virtual void OnAddStream(webrtc::MediaStreamInterface *stream);
    virtual void OnRemoveStream(webrtc::MediaStreamInterface *stream);
    virtual void OnDataChannel(webrtc::DataChannelInterface *dataChannel);

    // ICE callbacks
    virtual void OnIceConnectionChange(webrtc::PeerConnectionInterface::IceConnectionState state);
    virtual void OnIceGatheringChange(webrtc::PeerConnectionInterface::IceGatheringState state);
    virtual void OnIceCandidate(const webrtc::IceCandidateInterface *candidate);
    virtual void OnRenegotiationNeeded();
    virtual void OnIceComplete();

    // inherited from CreateSessionDescriptionObserver
    virtual void OnSuccess(webrtc::SessionDescriptionInterface *desc);
    virtual void OnFailure(const std::string &error);

    // reference counting
    virtual int AddRef() {
        return 1;
    }

    // reference counting
    virtual int Release() {
        return 0;
    }

 private:

    void addVideoTrackRenderer(const std::string& stream_label,
                               const std::string& source_id,
                               talk_base::scoped_refptr<webrtc::VideoTrackInterface>& videoTrack);

    // helper for SetSessionDescriptionObserver
    void createAnswer() {
        peer_connection_->CreateAnswer(this, &sdp_constraints_);
    }

    void setTrickleEnabled(webrtc::SessionDescriptionInterface *description);
    bool isTrickleEnabled(webrtc::SessionDescriptionInterface *description);

 private:

    webrtc::PeerConnectionInterface::IceServers ice_servers_;

    TwilioWebrtcConstraints sdp_constraints_;
    TwilioWebrtcConstraints peer_connection_constraints_;
    TwilioWebrtcConstraints video_constraints_;

    talk_base::scoped_refptr<webrtc::PeerConnectionFactoryInterface> peer_connection_factory_;
    talk_base::scoped_refptr<webrtc::PeerConnectionInterface> peer_connection_;
    talk_base::scoped_refptr<webrtc::AudioTrackInterface> audio_track_;
    talk_base::scoped_refptr<webrtc::VideoTrackInterface> video_track_;
    talk_base::scoped_refptr<webrtc::VideoSourceInterface> video_source_;
    talk_base::scoped_refptr<webrtc::MediaStreamInterface> stream_;

    talk_base::scoped_refptr<SetSessionDescriptionObserver> sdp_observer_;
    WebrtcConnectionManagerObserverInterface *observer_;

    cricket::VideoCapturer *video_capturer_;
    cricket::VideoFormat best_format_;
    VideoRendererMap video_renderers_;
    AudioRendererMap audio_renderers_;
};

// simple observer to notify SDK of SDP validation failures
class SetSessionDescriptionObserver: public webrtc::SetSessionDescriptionObserver {
 public:
    SetSessionDescriptionObserver(WebrtcConnectionManager *webrtcConnectionManager)
     : connection_manager_(webrtcConnectionManager) { }

    virtual ~SetSessionDescriptionObserver() { }

    // called when session description is set successfully; when local SDP is
    // set we don't need to do anything since we still need to wait for ICE
    // candidate gathering to complete; however, if we set remote description
    // for offer we need to kick in answer creation or let the observer know
    // that the answer was accepted
    virtual void OnSuccess() {
        LOG_INFO("WebrtcConnectionManager: Session description was set successfully");
        if (type_ == webrtc::SessionDescriptionInterface::kPrAnswer) {
            connection_manager_->createAnswer();
        } else if (type_ == webrtc::SessionDescriptionInterface::kAnswer) {
            connection_manager_->observer_->onAnswerAccepted();
        }
    }

    // called when error ocurs during session description validation; notify
    // the observer to terminate call, cleanup, etc.
    virtual void OnFailure(const std::string &error) {
        connection_manager_->observer_->onSdpCreationFailed(error);
    }

    // reference counting
    virtual int AddRef() {
        return 1;
    }

    // reference counting
    virtual int Release() {
        return 0;
    }

    void sdpType(const char *type) {
        type_ = type;
    }

 private:
    std::string type_;
    WebrtcConnectionManager *connection_manager_;
};

}  // namespace twiliosdk

#endif // WEBRTCCONNECTIONAMANGER_H
