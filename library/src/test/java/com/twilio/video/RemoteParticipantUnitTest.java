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

import android.os.Handler;
import java.util.Arrays;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RemoteParticipantUnitTest {
    private static final int INT_MAX = 25;

    private final Random random = new Random();
    private RemoteParticipant remoteParticipant;
    @Mock RemoteAudioTrackPublication mockRemoteAudioTrackPublication;
    @Mock RemoteVideoTrackPublication mockRemoteVideoTrackPublicationOne;
    @Mock RemoteVideoTrackPublication mockRemoteVideoTrackPublicationTwo;
    @Mock RemoteDataTrackPublication mockRemoteDataTrackPublication;
    @Mock Handler handler;

    @Before
    public void setup() {
        remoteParticipant =
                new RemoteParticipant(
                        String.valueOf(random.nextInt(INT_MAX)),
                        String.valueOf(random.nextInt(INT_MAX)),
                        Arrays.asList(mockRemoteAudioTrackPublication, null),
                        Arrays.asList(
                                mockRemoteVideoTrackPublicationOne,
                                mockRemoteVideoTrackPublicationTwo),
                        Arrays.asList(mockRemoteDataTrackPublication, null),
                        handler,
                        random.nextLong());
    }

    @Test(expected = NullPointerException.class)
    public void setListener_shouldNotAllowNull() throws Exception {
        remoteParticipant.setListener(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingAudioTracks() {
        remoteParticipant.getAudioTracks().add(mockRemoteAudioTrackPublication);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingVideoTracks() {
        remoteParticipant.getVideoTracks().add(mockRemoteVideoTrackPublicationOne);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingSubscribedAudioTracks() {
        remoteParticipant.getRemoteAudioTracks().add(mockRemoteAudioTrackPublication);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingSubscribedVideoTracks() {
        remoteParticipant.getRemoteVideoTracks().add(mockRemoteVideoTrackPublicationOne);
    }

    @Test
    public void networkQualityLevelShouldBeUnknownByDefault() {
        assertEquals(
                remoteParticipant.getNetworkQualityLevel(),
                NetworkQualityLevel.NETWORK_QUALITY_LEVEL_UNKNOWN);
    }

    @Test
    public void onNetworkQualityLevelChangedShouldInvokeCallbackOnNetworkQualityChange() {
        RemoteParticipant.Listener remoteParticipantListener =
                mock(RemoteParticipant.Listener.class);
        remoteParticipant.setListener(remoteParticipantListener);
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        NetworkQualityLevel networkQualityLevel = NetworkQualityLevel.NETWORK_QUALITY_LEVEL_FIVE;

        remoteParticipant.remoteParticipantListenerProxy.onNetworkQualityLevelChanged(
                remoteParticipant, networkQualityLevel);
        verify(handler).post(captor.capture());
        Runnable callback = captor.getValue();
        callback.run();

        assertEquals(networkQualityLevel, remoteParticipant.getNetworkQualityLevel());
    }

    @Test
    public void
            onNetworkQualityLevelChangedShouldInvokeCallbackOnNetworkQualityChangeWithAnotherQualityLevel() {
        RemoteParticipant.Listener remoteParticipantListener =
                mock(RemoteParticipant.Listener.class);
        remoteParticipant.setListener(remoteParticipantListener);
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);

        NetworkQualityLevel networkQualityLevel = NetworkQualityLevel.NETWORK_QUALITY_LEVEL_ZERO;
        remoteParticipant.remoteParticipantListenerProxy.onNetworkQualityLevelChanged(
                remoteParticipant, networkQualityLevel);
        verify(handler).post(captor.capture());
        Runnable callback = captor.getValue();
        callback.run();

        assertEquals(networkQualityLevel, remoteParticipant.getNetworkQualityLevel());
    }

    @Test
    public void onNetworkQualityLevelChangedShouldNotInvokeListenerIfItHasNotBeenSet() {
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);

        remoteParticipant.remoteParticipantListenerProxy.onNetworkQualityLevelChanged(
                remoteParticipant, NetworkQualityLevel.NETWORK_QUALITY_LEVEL_FIVE);
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
}
