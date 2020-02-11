package com.twilio.video.compatibility;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;

public class VideoAndWebRtcSideBySideTest extends BaseCompatibilityTest {
    @Before
    public void setup() {
        super.setup();
    }

    @Test
    public void canExecuteVoiceAndVideoConnect() throws Throwable {
        assertVideoCanExecuteConnect();

        // Attempt to create peerconnection and dispose
        PeerConnectionFactory.InitializationOptions initializationOptions =
                PeerConnectionFactory.InitializationOptions.builder(context)
                        .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);
        PeerConnectionFactory peerConnectionFactory =
                PeerConnectionFactory.builder().createPeerConnectionFactory();
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        PeerConnection.RTCConfiguration rtcConfiguration =
                new PeerConnection.RTCConfiguration(iceServers);
        PeerConnection peerConnection =
                peerConnectionFactory.createPeerConnection(
                        rtcConfiguration,
                        new PeerConnection.Observer() {
                            @Override
                            public void onSignalingChange(
                                    PeerConnection.SignalingState signalingState) {}

                            @Override
                            public void onIceConnectionChange(
                                    PeerConnection.IceConnectionState iceConnectionState) {}

                            @Override
                            public void onIceConnectionReceivingChange(boolean b) {}

                            @Override
                            public void onIceGatheringChange(
                                    PeerConnection.IceGatheringState iceGatheringState) {}

                            @Override
                            public void onIceCandidate(IceCandidate iceCandidate) {}

                            @Override
                            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {}

                            @Override
                            public void onAddStream(MediaStream mediaStream) {}

                            @Override
                            public void onRemoveStream(MediaStream mediaStream) {}

                            @Override
                            public void onDataChannel(DataChannel dataChannel) {}

                            @Override
                            public void onRenegotiationNeeded() {}

                            @Override
                            public void onAddTrack(
                                    RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {}
                        });

        if (peerConnection != null) {
            peerConnection.close();
            peerConnection.dispose();
        }

        peerConnectionFactory.dispose();
    }
}
