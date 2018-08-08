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

package com.twilio.video.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.VideoScaleType;
import com.twilio.video.VideoView;
import com.twilio.video.test.R;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * ListView adapter that renders local video tracks to a VideoView and TextView.
 */
public class VideoViewListViewAdapter extends BaseAdapter {
    private static final String TAG = "VideoViewListAdapter";

    private final List<LocalVideoTrack> localVideoTracks;
    private final Map<ViewHolder, LocalVideoTrack> viewHolderMap = new HashMap<>();
    public final Map<Integer, ViewHolder> viewHolderPositionMap = new HashMap<>();

    public VideoViewListViewAdapter(List<LocalVideoTrack> localVideoTracks) {
        this.localVideoTracks = localVideoTracks;
    }

    @Override
    public int getCount() {
        Log.d(TAG, "getCount");
        return localVideoTracks.size();
    }

    @Override
    public Object getItem(int i) {
        Log.d(TAG, "getItem");
        return localVideoTracks.get(i);
    }

    @Override
    public long getItemId(int i) {
        Log.d(TAG, "getItemId");
        return localVideoTracks.get(i).getName().hashCode();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Log.d(TAG, "getView");

        // Create view holder if needed
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            view = inflater.inflate(com.twilio.video.test.R.layout.video_view_item, null);
            TextView trackNameTextView = view.findViewById(R.id.track_name_text_view);
            VideoView videoView = view.findViewById(R.id.video_view);
            FrameCountProxyRendererListener frameCountProxyRendererListener =
                    new FrameCountProxyRendererListener(videoView);

            ViewHolder viewHolder =
                    new ViewHolder(trackNameTextView, frameCountProxyRendererListener);
            view.setTag(viewHolder);
        }

        // Remove renderer from previous video track
        ViewHolder holder = (ViewHolder) view.getTag();
        LocalVideoTrack localVideoTrack = localVideoTracks.get(i);
        if (viewHolderMap.containsKey(holder)) {
            viewHolderMap.get(holder).removeRenderer(holder.frameCountProxyRendererListener);
        }

        // Update view holder
        holder.trackNameTextView.setText(localVideoTrack.getName());
        holder.frameCountProxyRendererListener.videoView.setVideoScaleType(
                VideoScaleType.ASPECT_FILL);
        localVideoTrack.addRenderer(holder.frameCountProxyRendererListener);
        viewHolderMap.put(holder, localVideoTrack);
        viewHolderPositionMap.put(i, holder);

        return view;
    }

    public static class ViewHolder {
        private final TextView trackNameTextView;
        public final FrameCountProxyRendererListener frameCountProxyRendererListener;

        ViewHolder(
                TextView trackNameTextView,
                FrameCountProxyRendererListener frameCountProxyRendererListener) {
            this.trackNameTextView = trackNameTextView;
            this.frameCountProxyRendererListener = frameCountProxyRendererListener;
        }
    }
}
