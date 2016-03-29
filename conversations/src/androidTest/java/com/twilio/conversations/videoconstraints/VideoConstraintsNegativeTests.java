package com.twilio.conversations.videoconstraints;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.twilio.conversations.CameraCapturer;
import com.twilio.conversations.CameraCapturerFactory;
import com.twilio.conversations.CapturerErrorListener;
import com.twilio.conversations.CapturerException;
import com.twilio.conversations.LocalVideoTrack;
import com.twilio.conversations.LocalVideoTrackFactory;
import com.twilio.conversations.TwilioConversationsActivity;
import com.twilio.conversations.VideoConstraints;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class VideoConstraintsNegativeTests {

    @Rule
    public ActivityTestRule<TwilioConversationsActivity> mActivityRule = new ActivityTestRule<>(
            TwilioConversationsActivity.class);

    @Test(expected = NullPointerException.class)
    public void useInvalidMinVideoDimensions() {
        new VideoConstraints.Builder()
                .minVideoDimensions(null)
                .build();

    }

    @Test(expected = NullPointerException.class)
    public void useInvalidMaxVideoDimensions() {
        new VideoConstraints.Builder()
                .maxVideoDimensions(null)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void useNegativeMinFps() {
        new VideoConstraints.Builder()
                .minFps(-100)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void useNegativeMaxFps() {
        new VideoConstraints.Builder()
                .maxFps(-100)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void useInvalidFpsRange() {
        new VideoConstraints.Builder()
                .minFps(20)
                .maxFps(10)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void useInvalidDimensionsRange() {
        new VideoConstraints.Builder()
                .minVideoDimensions(VideoConstraints.HD_1080P_VIDEO_DIMENSIONS)
                .maxVideoDimensions(VideoConstraints.CIF_VIDEO_DIMENSIONS)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void localVideoTrackWithNullCameraCapturer() {
        CameraCapturer cameraCapturer = null;

        LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer);
    }

    @Test(expected = NullPointerException.class)
    public void localVideoTrackWithNullCameraCapturer2() {
        CameraCapturer cameraCapturer = null;

        VideoConstraints videoConstraints = new VideoConstraints.Builder()
                .build();

        LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer, videoConstraints);
    }

    @Test(expected = NullPointerException.class)
    public void localVideoTrackWithNullVideoConstraints() {
        ViewGroup viewGroup = new LinearLayout(mActivityRule.getActivity());
        CameraCapturer cameraCapturer = CameraCapturerFactory.
                createCameraCapturer(
                        mActivityRule.getActivity(),
                        CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA,
                        viewGroup,
                        new CapturerErrorListener() {
                            @Override
                            public void onError(CapturerException e) {

                            }
                        });

        assertNotNull(cameraCapturer);

        LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer, null);
    }

}
