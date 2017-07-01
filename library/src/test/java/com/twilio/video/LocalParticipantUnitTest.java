package com.twilio.video;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Random;

@RunWith(MockitoJUnitRunner.class)
public class LocalParticipantUnitTest {
    private static final int INT_MAX = 25;

    private final Random random = new Random();
    private LocalParticipant localParticipant;
    @Mock LocalAudioTrack mockAudioTrack;
    @Mock LocalVideoTrack mockVideoTrackOne;
    @Mock LocalVideoTrack mockVideoTrackTwo;

    @Before
    public void setup() {
        localParticipant = new LocalParticipant(random.nextLong(),
                String.valueOf(random.nextInt(INT_MAX)),
                String.valueOf(random.nextInt(INT_MAX)),
                Arrays.asList(mockAudioTrack),
                Arrays.asList(mockVideoTrackOne, mockVideoTrackTwo));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingAudioTracks() {
        localParticipant.getAudioTracks().add(mockAudioTrack);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingVideoTracks() {
        localParticipant.getVideoTracks().add(mockVideoTrackOne);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingPublishedAudioTracks() {
        localParticipant.getPublishedAudioTracks().add(mockAudioTrack);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingPublishedVideoTracks() {
        localParticipant.getPublishedVideoTracks().add(mockVideoTrackOne);
    }
}
