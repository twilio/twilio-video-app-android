package com.twilio.video;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.util.FakeVideoCapturer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LocalMediaTest {
    private Context context;
    private LocalMedia localMedia;
    private FakeVideoCapturer fakeVideoCapturer;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        localMedia = LocalMedia.create(context);
        fakeVideoCapturer = new FakeVideoCapturer();
    }

    @After
    public void teardown() {
        localMedia.release();
    }

    @Test(expected = IllegalStateException.class)
    public void getLocalAudioTracks_shouldFailAfterRelease() {
        localMedia.release();
        localMedia.getLocalAudioTracks();
    }

    @Test(expected = IllegalStateException.class)
    public void getLocalVideoTracks_shouldFailAfterRelease() {
        localMedia.release();
        localMedia.getLocalVideoTracks();
    }

    @Test
    public void canAddEnabledAudioTrack() {
        boolean expectedEnabled = true;
        LocalAudioTrack localAudioTrack = localMedia.addAudioTrack(expectedEnabled);

        assertNotNull(localAudioTrack);
        assertEquals(expectedEnabled, localAudioTrack.isEnabled());
        assertEquals(1, localMedia.getLocalAudioTracks().size());
    }

    @Test
    public void canAddDisabledAudioTrack() {
        boolean expectedEnabled = false;
        LocalAudioTrack localAudioTrack = localMedia.addAudioTrack(expectedEnabled);

        assertNotNull(localAudioTrack);
        assertEquals(expectedEnabled, localAudioTrack.isEnabled());
        assertEquals(1, localMedia.getLocalAudioTracks().size());
    }

    @Test
    public void canAddAudioTrackWithOptions() {
        AudioOptions audioOptions = new AudioOptions.Builder()
                .echoCancellation(true)
                .autoGainControl(true)
                .typingDetection(true)
                .build();
        LocalAudioTrack localAudioTrack = localMedia.addAudioTrack(true, audioOptions);

        assertNotNull(localAudioTrack);
    }

    @Test
    public void canRemoveAudioTrack() {
        LocalAudioTrack localAudioTrack = localMedia.addAudioTrack(true);

        // Validate the track was added
        assertNotNull(localAudioTrack);
        assertEquals(1, localMedia.getLocalAudioTracks().size());

        // Now remove and validate it is gone
        assertTrue(localMedia.removeAudioTrack(localAudioTrack));
        assertEquals(0, localMedia.getLocalAudioTracks().size());
    }

    @Test
    public void removeAudioTrack_shouldReturnFalseForNull() {
        assertFalse(localMedia.removeAudioTrack(null));
    }

    @Test
    public void canAddMultipleAudioTracks() {
        int numAudioTracks = 5;
        boolean[] expectedEnabled = new boolean[]{ false, true, true, false, false };

        for (int i = 0 ; i < numAudioTracks ; i++) {
            LocalAudioTrack localAudioTrack = localMedia.addAudioTrack(expectedEnabled[i]);
            int expectedSize = i + 1;

            assertNotNull(localAudioTrack);
            assertEquals(expectedEnabled[i], localAudioTrack.isEnabled());
            assertEquals(expectedSize, localMedia.getLocalAudioTracks().size());
        }
    }

    @Test
    public void canAddAndRemoveMultipleAudioTracks() {
        int numAudioTracks = 5;
        LocalAudioTrack[] localAudioTracks = new LocalAudioTrack[numAudioTracks];

        for (int i = 0 ; i < numAudioTracks ; i++) {
            int expectedSize = i + 1;

            localAudioTracks[i] = localMedia.addAudioTrack(false);
            assertNotNull(localAudioTracks[i]);
            assertEquals(expectedSize, localMedia.getLocalAudioTracks().size());
        }

        for (int i = numAudioTracks - 1 ; i >= 0 ; i--) {
            int expectedSize = i;
            assertTrue(localMedia.removeAudioTrack(localAudioTracks[i]));
            assertEquals(expectedSize, localMedia.getLocalAudioTracks().size());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void addAudioTrack_shouldFailAfterRelease() {
        localMedia.release();
        localMedia.addAudioTrack(false);
    }

    @Test(expected = IllegalStateException.class)
    public void removeAudioTrack_shouldFailAfterRelease() {
        LocalAudioTrack localAudioTrack = localMedia.addAudioTrack(false);
        assertNotNull(localAudioTrack);
        localMedia.release();
        localMedia.removeAudioTrack(localAudioTrack);
    }

    @Test
    public void canAddEnabledVideoTrack() {
        boolean expectedEnabled = true;
        LocalVideoTrack localVideoTrack = localMedia.addVideoTrack(expectedEnabled,
                fakeVideoCapturer);

        assertNotNull(localVideoTrack);
        assertEquals(expectedEnabled, localVideoTrack.isEnabled());
        assertEquals(1, localMedia.getLocalVideoTracks().size());
        assertTrue(fakeVideoCapturer.isStarted());
    }

    @Test
    public void canAddDisabledVideoTrack() {
        boolean expectedEnabled = false;
        LocalVideoTrack localVideoTrack = localMedia.addVideoTrack(expectedEnabled,
                fakeVideoCapturer);

        assertNotNull(localVideoTrack);
        assertEquals(expectedEnabled, localVideoTrack.isEnabled());
        assertEquals(1, localMedia.getLocalVideoTracks().size());
    }

    @Test
    public void addLocalVideoTrack_shouldFailForInvalidConstraints() {
        VideoConstraints invalidVideoConstraints = new VideoConstraints.Builder()
                .minVideoDimensions(new VideoDimensions(1,2))
                .maxVideoDimensions(new VideoDimensions(10,20))
                .build();
        LocalVideoTrack localVideoTrack = localMedia.addVideoTrack(true,
                fakeVideoCapturer,
                invalidVideoConstraints);

        assertNull(localVideoTrack);
    }

    @Test
    public void addLocalVideoTrack_shouldRespectValidConstraints() {
        Integer expectedMinWidth = 320;
        Integer expectedMinHeight = 180;
        Integer expectedMaxWidth = 640;
        Integer expectedMaxHeight = 360;
        Integer expectedMinFps = 5;
        Integer expectedMaxFps = 15;
        VideoConstraints validVideoConstraints = new VideoConstraints.Builder()
                .minVideoDimensions(new VideoDimensions(expectedMinWidth, expectedMinHeight))
                .maxVideoDimensions(new VideoDimensions(expectedMaxWidth, expectedMaxHeight))
                .minFps(expectedMinFps)
                .maxFps(expectedMaxFps)
                .build();
        LocalVideoTrack localVideoTrack = localMedia.addVideoTrack(true,
                fakeVideoCapturer,
                validVideoConstraints);

        assertNotNull(localVideoTrack);
        assertTrue(fakeVideoCapturer.getCaptureWidth() >= expectedMinWidth);
        assertTrue(fakeVideoCapturer.getCaptureWidth() <= expectedMaxWidth);
        assertTrue(fakeVideoCapturer.getCaptureHeight() >= expectedMinHeight);
        assertTrue(fakeVideoCapturer.getCaptureHeight() <= expectedMaxHeight);
        assertTrue(fakeVideoCapturer.getCaptureFramerate() >= expectedMinFps);
        assertTrue(fakeVideoCapturer.getCaptureFramerate() <= expectedMaxFps);
    }

    @Test
    public void canRemoveVideoTrack() {
        LocalVideoTrack localVideoTrack = localMedia.addVideoTrack(true, fakeVideoCapturer);

        // Validate the track was added
        assertNotNull(localVideoTrack);
        assertEquals(1, localMedia.getLocalVideoTracks().size());

        // Now remove and validate it is gone
        assertTrue(localMedia.removeLocalVideoTrack(localVideoTrack));
        assertEquals(0, localMedia.getLocalVideoTracks().size());
        assertFalse(fakeVideoCapturer.isStarted());
    }

    @Test
    public void removeVideoTrack_shouldReturnFalseForNull() {
        assertFalse(localMedia.removeLocalVideoTrack(null));
    }

    @Test
    public void canAddMultipleVideoTracks() {
        int numVideoTracks = 5;
        boolean[] expectedEnabled = new boolean[]{ false, true, true, false, false };

        for (int i = 0 ; i < numVideoTracks ; i++) {
            LocalVideoTrack localVideoTrack = localMedia.addVideoTrack(expectedEnabled[i],
                    fakeVideoCapturer);
            int expectedSize = i + 1;

            assertNotNull(localVideoTrack);
            assertEquals(expectedEnabled[i], localVideoTrack.isEnabled());
            assertEquals(expectedSize, localMedia.getLocalVideoTracks().size());
        }
    }

    @Test
    public void canAddAndRemoveMultipleVideoTracks() {
        int numVideoTracks = 5;
        LocalVideoTrack[] localVideoTracks = new LocalVideoTrack[numVideoTracks];

        for (int i = 0 ; i < numVideoTracks ; i++) {
            int expectedSize = i + 1;

            localVideoTracks[i] = localMedia.addVideoTrack(false, fakeVideoCapturer);
            assertNotNull(localVideoTracks[i]);
            assertEquals(expectedSize, localMedia.getLocalVideoTracks().size());
        }

        for (int i = numVideoTracks - 1 ; i >= 0 ; i--) {
            int expectedSize = i;
            assertTrue(localMedia.removeLocalVideoTrack(localVideoTracks[i]));
            assertEquals(expectedSize, localMedia.getLocalVideoTracks().size());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void addVideoTrack_shouldFailAfterRelease() {
        localMedia.release();
        localMedia.addVideoTrack(false, new FakeVideoCapturer());
    }

    @Test(expected = IllegalStateException.class)
    public void removeVideoTrack_shouldFailAfterRelease() {
        LocalVideoTrack localVideoTrack = localMedia.addVideoTrack(false, fakeVideoCapturer);
        assertNotNull(localVideoTrack);
        localMedia.release();
        localMedia.removeLocalVideoTrack(localVideoTrack);
    }

    @Test
    public void canAddAudioAndVideoTrack() {
        LocalAudioTrack localAudioTrack = localMedia.addAudioTrack(true);
        LocalVideoTrack localVideoTrack = localMedia.addVideoTrack(true, fakeVideoCapturer);

        assertNotNull(localAudioTrack);
        assertNotNull(localVideoTrack);
        assertEquals(1, localMedia.getLocalAudioTracks().size());
        assertEquals(1, localMedia.getLocalVideoTracks().size());
    }

    @Test
    public void canAddMultipleAudioAndVideoTracks() {
        int numAudioTracks = 5;
        int numVideoTracks = 5;
        LocalAudioTrack[] localAudioTracks = new LocalAudioTrack[numAudioTracks];
        LocalVideoTrack[] localVideoTracks = new LocalVideoTrack[numVideoTracks];

        for (int i = 0 ; i < numAudioTracks ; i++) {
            int expectedSize = i + 1;

            localAudioTracks[i] = localMedia.addAudioTrack(false);
            assertNotNull(localAudioTracks[i]);
            assertEquals(expectedSize, localMedia.getLocalAudioTracks().size());
        }

        for (int i = 0 ; i < numVideoTracks ; i++) {
            int expectedSize = i + 1;

            localVideoTracks[i] = localMedia.addVideoTrack(false, fakeVideoCapturer);
            assertNotNull(localVideoTracks[i]);
            assertEquals(expectedSize, localMedia.getLocalVideoTracks().size());
        }

        for (int i = numAudioTracks - 1 ; i >= 0 ; i--) {
            int expectedSize = i;
            assertTrue(localMedia.removeAudioTrack(localAudioTracks[i]));
            assertEquals(expectedSize, localMedia.getLocalAudioTracks().size());
        }

        for (int i = numVideoTracks - 1 ; i >= 0 ; i--) {
            int expectedSize = i;
            assertTrue(localMedia.removeLocalVideoTrack(localVideoTracks[i]));
            assertEquals(expectedSize, localMedia.getLocalVideoTracks().size());
        }
    }

    @Test
    public void release_shouldBeIdempotent() {
        localMedia.release();
        localMedia.release();
    }
}
