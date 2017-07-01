package com.twilio.video;

import android.os.Handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Random;

@RunWith(MockitoJUnitRunner.class)
public class RemoteParticipantUnitTest {
    private static final int INT_MAX = 25;

    private final Random random = new Random();
    private RemoteParticipant remoteParticipant;
    @Mock RemoteAudioTrack mockRemoteAudioTrack;
    @Mock RemoteVideoTrack mockRemoteVideoTrackOne;
    @Mock RemoteVideoTrack mockRemoteVideoTrackTwo;
    @Mock Handler handler;

    @Before
    public void setup() {
        remoteParticipant = new RemoteParticipant(String.valueOf(random.nextInt(INT_MAX)),
                String.valueOf(random.nextInt(INT_MAX)),
                Arrays.asList(mockRemoteAudioTrack),
                Arrays.asList(mockRemoteVideoTrackOne, mockRemoteVideoTrackTwo),
                handler,
                random.nextLong());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingAudioTracks() {
        remoteParticipant.getAudioTracks().add(mockRemoteAudioTrack);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingVideoTracks() {
        remoteParticipant.getVideoTracks().add(mockRemoteVideoTrackOne);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingSubscribedAudioTracks() {
        remoteParticipant.getSubscribedAudioTracks().add(mockRemoteAudioTrack);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingSubscribedVideoTracks() {
        remoteParticipant.getSubscribedVideoTracks().add(mockRemoteVideoTrackOne);
    }
}
