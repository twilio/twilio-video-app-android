/*
 * Copyright (C) 2017 Twilio, inc.
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

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.twilio.video.ui.VideoViewListViewAdapter;
import com.twilio.video.ui.VideoViewRecyclerViewAdapter;
import com.twilio.video.ui.VideoViewTestActivity;
import com.twilio.video.test.R;
import com.twilio.video.util.FakeVideoCapturer;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class VideoViewTest {
    @Rule
    public ActivityTestRule<VideoViewTestActivity> activityRule =
            new ActivityTestRule<>(VideoViewTestActivity.class);
    private VideoViewTestActivity videoViewTestActivity;
    private RelativeLayout relativeLayout;

    @Before
    public void setup() {
        videoViewTestActivity = activityRule.getActivity();
        relativeLayout = (RelativeLayout) videoViewTestActivity.findViewById(R.id.container);
    }

    @After
    public void teardown() {
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void canBeRenderedInRecyclerView() throws InterruptedException {
        final int numItems = 100;
        final String recyclerViewContentDescription = "VideoViewRecyclerView";

        // Create the recycler view
        final RecyclerView recyclerView = new RecyclerView(videoViewTestActivity);
        recyclerView.setContentDescription(recyclerViewContentDescription);
        RecyclerView.LayoutParams layoutParams =
                new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.MATCH_PARENT);
        recyclerView.setLayoutParams(layoutParams);

        // Create a list of the same local video track
        LocalVideoTrack localVideoTrack = LocalVideoTrack.create(videoViewTestActivity,
                true,
                new FakeVideoCapturer());
        List<LocalVideoTrack> videoTracks = new ArrayList<>(numItems);
        for (int i = 0 ; i < numItems ; i++) {
            videoTracks.add(i, localVideoTrack);
        }

        // Create the adapter with the generated tracks
        final VideoViewRecyclerViewAdapter videoViewRecyclerViewAdapter =
                new VideoViewRecyclerViewAdapter(videoTracks);

        // Add the recycler view to the layout
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                relativeLayout.addView(recyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(videoViewTestActivity));
                recyclerView.setAdapter(videoViewRecyclerViewAdapter);
            }
        });

        // Scroll through each item and validate a frame was received
        for (int i = 0 ; i < numItems ; i++) {
            onView(withContentDescription(recyclerViewContentDescription))
                    .perform(RecyclerViewActions.scrollToPosition(i));
            VideoViewRecyclerViewAdapter.VideoViewHolder videoViewHolder =
                    (VideoViewRecyclerViewAdapter.VideoViewHolder) recyclerView
                            .findViewHolderForAdapterPosition(i);

            assertTrue(videoViewHolder.frameCountProxyRendererListener.waitForFrame(3000));
        }

        // Release video track
        localVideoTrack.release();
    }

    @Test
    public void canBeRenderedInListView() throws InterruptedException {
        final int numItems = 100;
        final String listViewContentDescription = "VideoListView";

        // Create the list view
        final ListView listView = new ListView(videoViewTestActivity);
        listView.setContentDescription(listViewContentDescription);
        ListView.LayoutParams layoutParams =
                new ListView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.MATCH_PARENT);
        listView.setLayoutParams(layoutParams);

        // Create a list of the same local video track
        LocalVideoTrack localVideoTrack = LocalVideoTrack.create(videoViewTestActivity,
                true,
                new FakeVideoCapturer());
        List<LocalVideoTrack> videoTracks = new ArrayList<>(numItems);
        for (int i = 0 ; i < numItems ; i++) {
            videoTracks.add(i, localVideoTrack);
        }

        // Create the adapter with the generated tracks
        final VideoViewListViewAdapter videoViewListViewAdapter =
                new VideoViewListViewAdapter(videoTracks);

        // Add the list view to the layout
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                relativeLayout.addView(listView);
                listView.setAdapter(videoViewListViewAdapter);
            }
        });

        // Scroll through each item and validate a frame was received
        for (int i = 0 ; i < numItems ; i++) {
            final int position = i;
            InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
                @Override
                public void run() {
                    listView.smoothScrollToPosition(position);
                    listView.setSelection(position);
                }
            });
            VideoViewListViewAdapter.ViewHolder viewHolder = videoViewListViewAdapter
                    .viewHolderPositionMap.get(position);
            assertTrue(viewHolder.frameCountProxyRendererListener.waitForFrame(3000));
        }

        // Release video track
        localVideoTrack.release();
    }
}
