/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.os.Handler;
import com.twilio.video.util.Constants;
import java.util.Collections;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LocalParticipantUnitTest {
    private static final int INT_MAX = 25;
    private static final String REGION_US1 = "us1";

    private final Random random = new Random();
    private LocalParticipant localParticipant;
    @Mock Handler handler;
    @Mock LocalAudioTrackPublication mockLocalAudioTrackPublication;
    @Mock LocalVideoTrackPublication mockLocalVideoTrackPublicationOne;
    @Mock LocalDataTrackPublication mockLocalDataTrackPublication;
    @Mock LocalAudioTrack mockLocalAudioTrack;
    @Mock LocalVideoTrack mockLocalVideoTrack;
    @Mock LocalDataTrack mockLocalDataTrack;

    @Before
    public void setup() {
        localParticipant =
                new LocalParticipant(
                        random.nextLong(),
                        Constants.MOCK_PARTICIPANT_SID,
                        String.valueOf(random.nextInt(INT_MAX)),
                        REGION_US1,
                        Collections.singletonList(mockLocalAudioTrackPublication),
                        Collections.singletonList(mockLocalVideoTrackPublicationOne),
                        Collections.singletonList(mockLocalDataTrackPublication),
                        handler);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailWithNullSid() {
        new LocalParticipant(
                random.nextLong(),
                null,
                String.valueOf(random.nextInt(INT_MAX)),
                REGION_US1,
                Collections.singletonList(mockLocalAudioTrackPublication),
                Collections.singletonList(mockLocalVideoTrackPublicationOne),
                Collections.singletonList(mockLocalDataTrackPublication),
                handler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithEmptySid() {
        new LocalParticipant(
                random.nextLong(),
                "",
                REGION_US1,
                String.valueOf(random.nextInt(INT_MAX)),
                Collections.singletonList(mockLocalAudioTrackPublication),
                Collections.singletonList(mockLocalVideoTrackPublicationOne),
                Collections.singletonList(mockLocalDataTrackPublication),
                handler);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailWithNullIdentity() {
        new LocalParticipant(
                random.nextLong(),
                Constants.MOCK_PARTICIPANT_SID,
                null,
                REGION_US1,
                Collections.singletonList(mockLocalAudioTrackPublication),
                Collections.singletonList(mockLocalVideoTrackPublicationOne),
                Collections.singletonList(mockLocalDataTrackPublication),
                handler);
    }

    @Test
    public void shouldSucceedWithValidTrackSid() {
        new LocalParticipant(
                random.nextLong(),
                Constants.MOCK_PARTICIPANT_SID,
                String.valueOf(random.nextInt(INT_MAX)),
                REGION_US1,
                Collections.singletonList(mockLocalAudioTrackPublication),
                Collections.singletonList(mockLocalVideoTrackPublicationOne),
                Collections.singletonList(mockLocalDataTrackPublication),
                handler);
    }

    @Test(expected = NullPointerException.class)
    public void setListener_shouldNotAllowNull() {
        localParticipant.setListener(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingAudioTracks() {
        localParticipant.getAudioTracks().add(mockLocalAudioTrackPublication);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingVideoTracks() {
        localParticipant.getVideoTracks().add(mockLocalVideoTrackPublicationOne);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingPublishedAudioTracks() {
        localParticipant.getLocalAudioTracks().add(mockLocalAudioTrackPublication);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingPublishedVideoTracks() {
        localParticipant.getLocalVideoTracks().add(mockLocalVideoTrackPublicationOne);
    }

    @Test(expected = IllegalArgumentException.class)
    public void publishTrack_shouldNotAllowReleasedAudioTrack() {
        when(mockLocalAudioTrack.isReleased()).thenReturn(true);
        localParticipant.publishTrack(mockLocalAudioTrack);
    }

    @Test(expected = IllegalArgumentException.class)
    public void publishTrack_shouldNotAllowReleasedVideoTrack() {
        when(mockLocalVideoTrack.isReleased()).thenReturn(true);
        localParticipant.publishTrack(mockLocalVideoTrack);
    }

    @Test(expected = IllegalArgumentException.class)
    public void publishTrack_shouldNotAllowReleasedDataTrack() {
        when(mockLocalDataTrack.isReleased()).thenReturn(true);
        localParticipant.publishTrack(mockLocalDataTrack);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unpublishTrack_shouldNotAllowReleasedAudioTrack() {
        when(mockLocalAudioTrack.isReleased()).thenReturn(true);
        localParticipant.unpublishTrack(mockLocalAudioTrack);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unpublishTrack_shouldNotAllowReleasedVideoTrack() {
        when(mockLocalVideoTrack.isReleased()).thenReturn(true);
        localParticipant.unpublishTrack(mockLocalVideoTrack);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unpublishTrack_shouldNotAllowReleasedDataTrack() {
        when(mockLocalDataTrack.isReleased()).thenReturn(true);
        localParticipant.unpublishTrack(mockLocalDataTrack);
    }

    @Test(expected = NullPointerException.class)
    public void publshTrack_shouldNotAllowNull() {
        LocalVideoTrack videoTrack = null;
        localParticipant.publishTrack(videoTrack);

        LocalAudioTrack audioTrack = null;
        localParticipant.publishTrack(audioTrack);

        LocalDataTrack dataTrack = null;
        localParticipant.publishTrack(dataTrack);
    }

    @Test
    public void onNetworkQualityLevelChangedShouldInvokeCallbackOnNetworkQualityChange() {
        LocalParticipant.Listener localParticipantListener = mock(LocalParticipant.Listener.class);
        localParticipant.setListener(localParticipantListener);
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        NetworkQualityLevel networkQualityLevel = NetworkQualityLevel.NETWORK_QUALITY_LEVEL_FIVE;

        localParticipant.localParticipantListenerProxy.onNetworkQualityLevelChanged(
                localParticipant, networkQualityLevel);
        verify(handler).post(captor.capture());
        Runnable callback = captor.getValue();
        callback.run();

        assertEquals(networkQualityLevel, localParticipant.getNetworkQualityLevel());
    }

    @Test
    public void
            onNetworkQualityLevelChangedShouldInvokeCallbackOnNetworkQualityChangeWithAnotherQualityLevel() {
        LocalParticipant.Listener localParticipantListener = mock(LocalParticipant.Listener.class);
        localParticipant.setListener(localParticipantListener);
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);

        NetworkQualityLevel networkQualityLevel = NetworkQualityLevel.NETWORK_QUALITY_LEVEL_ZERO;
        localParticipant.localParticipantListenerProxy.onNetworkQualityLevelChanged(
                localParticipant, networkQualityLevel);
        verify(handler).post(captor.capture());
        Runnable callback = captor.getValue();
        callback.run();

        assertEquals(networkQualityLevel, localParticipant.getNetworkQualityLevel());
    }

    @Test
    public void onNetworkQualityLevelChangedShouldNotInvokeListenerIfItHasNotBeenSet() {
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);

        localParticipant.localParticipantListenerProxy.onNetworkQualityLevelChanged(
                localParticipant, NetworkQualityLevel.NETWORK_QUALITY_LEVEL_FIVE);
        verify(handler).post(captor.capture());
        Runnable callback = captor.getValue();
        try {
            callback.run();
        } catch (NullPointerException e) {
            fail("NullPointerException should not be thrown here!");
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    public void networkQualityLevelShouldBeUnknownByDefault() {
        assertEquals(
                localParticipant.getNetworkQualityLevel(),
                NetworkQualityLevel.NETWORK_QUALITY_LEVEL_UNKNOWN);
    }
}
