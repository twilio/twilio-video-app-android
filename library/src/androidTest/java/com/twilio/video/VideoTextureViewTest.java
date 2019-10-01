/*
 * Copyright (C) 2018 Twilio, Inc.
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

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import com.twilio.video.base.BaseVideoTest;
import com.twilio.video.testcategories.MediaTest;
import com.twilio.video.ui.VideoTextureViewListViewAdapter;
import com.twilio.video.ui.VideoTextureViewRecyclerViewAdapter;
import com.twilio.video.ui.VideoTextureViewTestActivity;
import com.twilio.video.util.FakeVideoCapturer;
import java.util.ArrayList;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediaTest
@RunWith(JUnitParamsRunner.class)
public class VideoTextureViewTest extends BaseVideoTest {

    private static final int FRAME_DELAY_MS = (int) TestUtils.FIVE_SECONDS;

    @Rule
    public ActivityTestRule<VideoTextureViewTestActivity> activityRule =
            new ActivityTestRule<>(VideoTextureViewTestActivity.class);

    private VideoTextureViewTestActivity videoViewTestActivity;
    private RelativeLayout relativeLayout;
    private LocalVideoTrack localVideoTrack;

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        // VideoTextureView is known to occasioncally hang on devices less than Lollipop
        assumeTrue(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT);
        videoViewTestActivity = activityRule.getActivity();
        relativeLayout =
                videoViewTestActivity.findViewById(com.twilio.video.R.id.relative_layout_container);
    }

    @After
    public void teardown() {
        if (localVideoTrack != null) {
            localVideoTrack.release();
        }
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void setVideoScaleType_canBeCalledBeforeViewInflated() {
        InstrumentationRegistry.getInstrumentation()
                .runOnMainSync(
                        () -> {
                            VideoTextureView videoView =
                                    new VideoTextureView(videoViewTestActivity);
                            VideoScaleType expectedVideoScaleType = VideoScaleType.ASPECT_BALANCED;

                            videoView.setVideoScaleType(expectedVideoScaleType);

                            assertEquals(expectedVideoScaleType, videoView.getVideoScaleType());
                        });
    }

    @Test
    @Parameters
    public void canSetVideoScaleType(final int width, final int height) {
        InstrumentationRegistry.getInstrumentation()
                .runOnMainSync(
                        () -> {
                            VideoTextureView videoView =
                                    new VideoTextureView(videoViewTestActivity);
                            RelativeLayout.LayoutParams layoutParams =
                                    new RelativeLayout.LayoutParams(width, height);
                            VideoScaleType expectedVideoScaleType = VideoScaleType.ASPECT_BALANCED;

                            videoView.setLayoutParams(layoutParams);
                            relativeLayout.addView(videoView);
                            videoView.setVideoScaleType(expectedVideoScaleType);

                            assertEquals(expectedVideoScaleType, videoView.getVideoScaleType());
                        });
    }

    @Ignore(
            "GSDK-1881: This test has been proven to be unreliable enough in FTL to skip in our"
                    + "test suite")
    @Test
    public void canBeRenderedInRecyclerView() throws InterruptedException {
        final int numItems = 100;
        final String recyclerViewContentDescription = "VideoTextureViewRecyclerView";

        // Create the recycler view
        final RecyclerView recyclerView = new RecyclerView(videoViewTestActivity);
        recyclerView.setContentDescription(recyclerViewContentDescription);
        RecyclerView.LayoutParams layoutParams =
                new RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.MATCH_PARENT);
        recyclerView.setLayoutParams(layoutParams);

        // Create a list of the same local video track
        localVideoTrack =
                LocalVideoTrack.create(videoViewTestActivity, true, new FakeVideoCapturer());
        List<LocalVideoTrack> videoTracks = new ArrayList<>(numItems);
        for (int i = 0; i < numItems; i++) {
            videoTracks.add(i, localVideoTrack);
        }

        // Create the adapter with the generated tracks
        final VideoTextureViewRecyclerViewAdapter videoViewRecyclerViewAdapter =
                new VideoTextureViewRecyclerViewAdapter(videoTracks);

        // Add the recycler view to the layout
        InstrumentationRegistry.getInstrumentation()
                .runOnMainSync(
                        () -> {
                            relativeLayout.addView(recyclerView);
                            recyclerView.setLayoutManager(
                                    new LinearLayoutManager(videoViewTestActivity));
                            recyclerView.setAdapter(videoViewRecyclerViewAdapter);
                            videoViewRecyclerViewAdapter.notifyDataSetChanged();
                        });

        // Scroll through each item and validate a frame was received
        for (int i = 0; i < numItems; i++) {
            onView(withContentDescription(recyclerViewContentDescription))
                    .perform(RecyclerViewActions.scrollToPosition(i));
            // Sleep to give recycler view time to scroll to position to prevent NPE
            TestUtils.blockingWait(250);
            VideoTextureViewRecyclerViewAdapter.VideoViewHolder videoViewHolder =
                    (VideoTextureViewRecyclerViewAdapter.VideoViewHolder)
                            recyclerView.findViewHolderForAdapterPosition(i);

            assertTrue(
                    videoViewHolder.frameCountProxyRendererListener.waitForFrame(FRAME_DELAY_MS));
        }
        // Clean up
        for (LocalVideoTrack track : videoTracks) {
            track.enable(false);
            track.release();
        }
        videoTracks.clear();
        localVideoTrack.release();
        localVideoTrack = null;
    }

    @Test
    public void canBeRenderedInListView() throws InterruptedException {
        final int numItems = 100;
        final String listViewContentDescription = "VideoTextureListView";

        // Create the list view
        final ListView listView = new ListView(videoViewTestActivity);
        listView.setContentDescription(listViewContentDescription);
        ListView.LayoutParams layoutParams =
                new ListView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.MATCH_PARENT);
        listView.setLayoutParams(layoutParams);

        // Create a list of the same local video track
        localVideoTrack =
                LocalVideoTrack.create(videoViewTestActivity, true, new FakeVideoCapturer());
        assertNotNull(localVideoTrack);
        List<LocalVideoTrack> videoTracks = new ArrayList<>(numItems);
        for (int i = 0; i < numItems; i++) {
            videoTracks.add(i, localVideoTrack);
        }

        // Create the adapter with the generated tracks
        final VideoTextureViewListViewAdapter videoViewListViewAdapter =
                new VideoTextureViewListViewAdapter(videoTracks);

        // Add the list view to the layout
        InstrumentationRegistry.getInstrumentation()
                .runOnMainSync(
                        () -> {
                            relativeLayout.addView(listView);
                            videoViewListViewAdapter.notifyDataSetChanged();
                            listView.setAdapter(videoViewListViewAdapter);
                        });

        // Scroll through each item and validate a frame was received
        for (int i = 0; i < numItems; i++) {
            final int position = i;
            InstrumentationRegistry.getInstrumentation()
                    .runOnMainSync(
                            () -> {
                                listView.smoothScrollToPosition(position);
                                listView.setSelection(position);
                            });
            VideoTextureViewListViewAdapter.ViewHolder viewHolder =
                    videoViewListViewAdapter.viewHolderPositionMap.get(position);
            assertNotNull(viewHolder);
            if (viewHolder.frameCountProxyRendererListener != null) {
                assertTrue(viewHolder.frameCountProxyRendererListener.waitForFrame(FRAME_DELAY_MS));
            }
        }
        // Clean up
        for (LocalVideoTrack track : videoTracks) {
            track.enable(false);
            track.release();
        }
        videoTracks.clear();
        localVideoTrack.release();
        localVideoTrack = null;
    }

    private Object[] parametersForCanSetVideoScaleType() {
        return new Object[] {
            new Object[] {
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT
            },
            new Object[] {
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT
            },
            new Object[] {
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT
            },
            new Object[] {
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT
            },
            new Object[] {96, 96}
        };
    }
}
