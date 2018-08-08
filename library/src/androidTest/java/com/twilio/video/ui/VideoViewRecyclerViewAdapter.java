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

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.VideoScaleType;
import com.twilio.video.VideoView;
import com.twilio.video.test.R;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * RecyclerView adapter that renders local video tracks to a VideoView and TextView.
 */
public class VideoViewRecyclerViewAdapter
        extends RecyclerView.Adapter<VideoViewRecyclerViewAdapter.VideoViewHolder> {
    private static final String TAG = "VideoViewRecAdapter";

    private final List<LocalVideoTrack> localVideoTracks;
    private final Map<VideoViewHolder, LocalVideoTrack> viewHolderMap = new HashMap<>();

    public VideoViewRecyclerViewAdapter(@NonNull List<LocalVideoTrack> localVideoTracks) {
        this.localVideoTracks = localVideoTracks;
    }

    @Override
    public void onViewAttachedToWindow(VideoViewHolder holder) {
        Log.d(TAG, "onViewAttachedToWindow");
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(VideoViewHolder holder) {
        Log.d(TAG, "onViewDetachedFromWindow");
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewRecycled(VideoViewHolder holder) {
        Log.d(TAG, "onViewRecycled");
        super.onViewRecycled(holder);
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.video_view_item, null);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder");
        LocalVideoTrack localVideoTrack = localVideoTracks.get(position);

        // Remove renderer from previous video track
        if (viewHolderMap.containsKey(holder)) {
            viewHolderMap.get(holder).removeRenderer(holder.frameCountProxyRendererListener);
        }

        // Update view holder
        holder.trackNameTextView.setText(localVideoTrack.getName());
        holder.frameCountProxyRendererListener.videoView.setVideoScaleType(
                VideoScaleType.ASPECT_FILL);
        localVideoTrack.addRenderer(holder.frameCountProxyRendererListener);
        viewHolderMap.put(holder, localVideoTrack);
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount");
        return localVideoTracks.size();
    }

    /*
     * View holder that hosts the video view proxy and a text view
     */
    public class VideoViewHolder extends RecyclerView.ViewHolder {
        public final TextView trackNameTextView;
        public final FrameCountProxyRendererListener frameCountProxyRendererListener;

        VideoViewHolder(View itemView) {
            super(itemView);
            this.frameCountProxyRendererListener =
                    new FrameCountProxyRendererListener(
                            (VideoView) itemView.findViewById(R.id.video_view));
            this.trackNameTextView = itemView.findViewById(R.id.track_name_text_view);
        }
    }
}
