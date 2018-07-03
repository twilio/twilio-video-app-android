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

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import com.twilio.video.base.BaseVideoTest;
import com.twilio.video.test.R;
import com.twilio.video.ui.VideoViewListViewAdapter;
import com.twilio.video.ui.VideoViewRecyclerViewAdapter;
import com.twilio.video.ui.VideoViewTestActivity;
import com.twilio.video.util.FakeVideoCapturer;
import java.util.ArrayList;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class VideoViewTest extends BaseVideoTest {
    private static final int FRAME_DELAY_MS = 3500;

    @Rule
    public ActivityTestRule<VideoViewTestActivity> activityRule =
            new ActivityTestRule<>(VideoViewTestActivity.class);

    private VideoViewTestActivity videoViewTestActivity;
    private RelativeLayout relativeLayout;
    private LocalVideoTrack localVideoTrack;

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        videoViewTestActivity = activityRule.getActivity();
        relativeLayout = videoViewTestActivity.findViewById(R.id.relative_layout_container);
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
                        new Runnable() {
                            @Override
                            public void run() {
                                VideoView videoView = new VideoView(videoViewTestActivity);
                                VideoScaleType expectedVideoScaleType =
                                        VideoScaleType.ASPECT_BALANCED;

                                videoView.setVideoScaleType(expectedVideoScaleType);

                                assertEquals(expectedVideoScaleType, videoView.getVideoScaleType());
                            }
                        });
    }

    @Test
    @Parameters
    public void canSetVideoScaleType(final int width, final int height) {
        InstrumentationRegistry.getInstrumentation()
                .runOnMainSync(
                        new Runnable() {
                            @Override
                            public void run() {
                                VideoView videoView = new VideoView(videoViewTestActivity);
                                RelativeLayout.LayoutParams layoutParams =
                                        new RelativeLayout.LayoutParams(width, height);
                                VideoScaleType expectedVideoScaleType =
                                        VideoScaleType.ASPECT_BALANCED;

                                videoView.setLayoutParams(layoutParams);
                                relativeLayout.addView(videoView);
                                videoView.setVideoScaleType(expectedVideoScaleType);

                                assertEquals(expectedVideoScaleType, videoView.getVideoScaleType());
                            }
                        });
    }

    @Test
    public void canBeRenderedInRecyclerView() throws InterruptedException {
        final int numItems = 100;
        final String recyclerViewContentDescription = "VideoViewRecyclerView";

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
        final VideoViewRecyclerViewAdapter videoViewRecyclerViewAdapter =
                new VideoViewRecyclerViewAdapter(videoTracks);

        // Add the recycler view to the layout
        InstrumentationRegistry.getInstrumentation()
                .runOnMainSync(
                        new Runnable() {
                            @Override
                            public void run() {
                                relativeLayout.addView(recyclerView);
                                recyclerView.setLayoutManager(
                                        new LinearLayoutManager(videoViewTestActivity));
                                recyclerView.setAdapter(videoViewRecyclerViewAdapter);
                            }
                        });

        // Scroll through each item and validate a frame was received
        for (int i = 0; i < numItems; i++) {
            onView(withContentDescription(recyclerViewContentDescription))
                    .perform(RecyclerViewActions.scrollToPosition(i));
            VideoViewRecyclerViewAdapter.VideoViewHolder videoViewHolder =
                    (VideoViewRecyclerViewAdapter.VideoViewHolder)
                            recyclerView.findViewHolderForAdapterPosition(i);

            assertTrue(
                    videoViewHolder.frameCountProxyRendererListener.waitForFrame(FRAME_DELAY_MS));
        }
    }

    @Test
    public void canBeRenderedInListView() throws InterruptedException {
        final int numItems = 100;
        final String listViewContentDescription = "VideoListView";

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
        List<LocalVideoTrack> videoTracks = new ArrayList<>(numItems);
        for (int i = 0; i < numItems; i++) {
            videoTracks.add(i, localVideoTrack);
        }

        // Create the adapter with the generated tracks
        final VideoViewListViewAdapter videoViewListViewAdapter =
                new VideoViewListViewAdapter(videoTracks);

        // Add the list view to the layout
        InstrumentationRegistry.getInstrumentation()
                .runOnMainSync(
                        new Runnable() {
                            @Override
                            public void run() {
                                relativeLayout.addView(listView);
                                listView.setAdapter(videoViewListViewAdapter);
                            }
                        });

        // Scroll through each item and validate a frame was received
        for (int i = 0; i < numItems; i++) {
            final int position = i;
            InstrumentationRegistry.getInstrumentation()
                    .runOnMainSync(
                            new Runnable() {
                                @Override
                                public void run() {
                                    listView.smoothScrollToPosition(position);
                                    listView.setSelection(position);
                                }
                            });
            VideoViewListViewAdapter.ViewHolder viewHolder =
                    videoViewListViewAdapter.viewHolderPositionMap.get(position);
            assertTrue(viewHolder.frameCountProxyRendererListener.waitForFrame(FRAME_DELAY_MS));
        }
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
