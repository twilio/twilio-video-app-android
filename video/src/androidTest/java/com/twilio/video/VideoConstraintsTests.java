package com.twilio.video;

import android.content.Context;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.activity.RoomsTestActivity;
import com.twilio.video.helper.CameraCapturerHelper;
import com.twilio.video.helper.OSLevelHelper;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class VideoConstraintsTests {

    @Rule
    public ActivityTestRule<RoomsTestActivity> activityRule = new ActivityTestRule<>(
            RoomsTestActivity.class);

    @Test(expected = NullPointerException.class)
    public void localVideoTrackWithNullVideoConstraints() {
//        CameraCapturer cameraCapturer = CameraCapturer.instance(
//                        activityRule.getActivity(),
//                        CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA,
//                        new CapturerErrorListener() {
//                            @Override
//                            public void onError(CapturerException e) {
//
//                            }
//                        });
//
//        assertNotNull(cameraCapturer);
//
//        new LocalVideoTrack(cameraCapturer, null);
    }

    @Test
    @Ignore
    public void startConversationWithInvalidVideoConstraints() throws InterruptedException {
        if(OSLevelHelper.requiresRuntimePermissions()) {
            return;
        }

        final CountDownLatch localVideoTrackFailedLatch = new CountDownLatch(1);

        LocalVideoTrack localVideoTrack = createLocalVideoTrackWithVideoConstraints(
                activityRule.getActivity(),
                new VideoConstraints.Builder()
                        .minVideoDimensions(new VideoDimensions(1,2))
                        .maxVideoDimensions(new VideoDimensions(10,20))
                        .build());

        LocalMedia localMedia = null;
//        LocalMedia localMedia = LocalMedia.instance(null, new LocalMedia.Listener() {
//            @Override
//            public void onLocalVideoTrackAdded(LocalMedia localMedia, LocalVideoTrack videoTrack) {
//                fail();
//            }
//
//            @Override
//            public void onLocalVideoTrackRemoved(LocalMedia localMedia, LocalVideoTrack videoTrack) {
//                fail();
//            }
//
//            @Override
//            public void onLocalVideoTrackError(LocalMedia localMedia, LocalVideoTrack track, VideoException exception) {
//                localVideoTrackFailedLatch.countDown();
//            }
//        });

//        localMedia.addLocalVideoTrack(localVideoTrack);

        assertTrue(localVideoTrackFailedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    @Ignore
    public void startConversationWithValidVideoConstraints() throws InterruptedException {
        if(OSLevelHelper.requiresRuntimePermissions()) {
            return;
        }

        final CountDownLatch localVideoTrackAddedLatch = new CountDownLatch(1);
        final CountDownLatch localVideoTrackRemovedLatch = new CountDownLatch(1);

        LocalVideoTrack localVideoTrack = createLocalVideoTrackWithVideoConstraints(
                activityRule.getActivity(),
                new VideoConstraints.Builder()
                        .minVideoDimensions(VideoDimensions.CIF_VIDEO_DIMENSIONS)
                        .maxVideoDimensions(VideoDimensions.VGA_VIDEO_DIMENSIONS)
                        .build());

        LocalMedia localMedia = null;
//        LocalMedia localMedia = LocalMedia.instance(null, new LocalMedia.Listener() {
//            @Override
//            public void onLocalVideoTrackAdded(LocalMedia localMedia, LocalVideoTrack videoTrack) {
//                localVideoTrackAddedLatch.countDown();
//            }
//
//            @Override
//            public void onLocalVideoTrackRemoved(LocalMedia localMedia, LocalVideoTrack videoTrack) {
//                localVideoTrackRemovedLatch.countDown();
//            }
//
//            @Override
//            public void onLocalVideoTrackError(LocalMedia localMedia,
//                                               LocalVideoTrack track,
//                                               VideoException exception) {
//                fail();
//            }
//        });

//        localMedia.addLocalVideoTrack(localVideoTrack);

        assertTrue(localVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));

        assertTrue(localVideoTrackRemovedLatch.await(20, TimeUnit.SECONDS));
    }

    private LocalVideoTrack createLocalVideoTrackWithVideoConstraints(Context context, VideoConstraints videoConstraints) throws InterruptedException {
//        CameraCapturer cameraCapturer = CameraCapturerHelper.createCameraCapturer(context, CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA);
//        return new LocalVideoTrack(cameraCapturer, videoConstraints);
        return null;
    }
}
