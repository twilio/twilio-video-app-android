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

package com.twilio.video.app.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;
import com.twilio.video.RemoteAudioTrack;
import com.twilio.video.RemoteAudioTrackPublication;
import com.twilio.video.RemoteAudioTrackStats;
import com.twilio.video.LocalAudioTrackStats;
import com.twilio.video.LocalVideoTrackStats;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.StatsReport;
import com.twilio.video.RemoteVideoTrackStats;
import com.twilio.video.app.R;
import com.twilio.video.app.model.StatsListItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StatsListAdapter extends RecyclerView.Adapter<StatsListAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.stats_track_name) TextView trackNameText;
        @BindView(R.id.stats_track_sid_value) TextView trackSidValueText;
        @BindView(R.id.stats_codec_value) TextView codecValueText;
        @BindView(R.id.stats_packets_value) TextView packetsValueText;
        @BindView(R.id.stats_bytes_title) TextView bytesTitleText;
        @BindView(R.id.stats_bytes_value) TextView bytesValueText;
        @BindView(R.id.stats_rtt_value) TextView rttValueText;
        @BindView(R.id.stats_jitter_value) TextView jitterValueText;
        @BindView(R.id.stats_audio_level_value) TextView audioLevelValueText;
        @BindView(R.id.stats_dimensions_value) TextView dimensionsValueText;
        @BindView(R.id.stats_framerate_value) TextView framerateValueText;
        @BindView(R.id.stats_rtt_row) TableRow rttTableRow;
        @BindView(R.id.stats_jitter_row) TableRow jitterTableRow;
        @BindView(R.id.stats_audio_level_row) TableRow audioLevelTableRow;
        @BindView(R.id.stats_dimensions_row) TableRow dimensionsTableRow;
        @BindView(R.id.stats_framerate_row) TableRow framerateTableRow;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private ArrayList<StatsListItem> statsListItems = new ArrayList<>();
    private Context context;
    private Handler handler;

    public StatsListAdapter(Context context) {
        this.context = context;
        handler = new Handler(Looper.getMainLooper());
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stats_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        StatsListItem item = statsListItems.get(position);
        holder.trackNameText.setText(item.trackName);
        holder.trackSidValueText.setText(item.trackSid);
        holder.codecValueText.setText(item.codec);
        holder.packetsValueText.setText(String.valueOf(item.packetsLost));
        holder.bytesValueText.setText(String.valueOf(item.bytes));
        if (item.isLocalTrack) {
            holder.bytesTitleText.setText(context.getString(R.string.stats_bytes_sent));
            holder.rttValueText.setText(String.valueOf(item.rtt));
            holder.rttTableRow.setVisibility(View.VISIBLE);
        } else {
            holder.rttTableRow.setVisibility(View.GONE);
            holder.bytesTitleText.setText(context.getString(R.string.stats_bytes_received));
        }
        if (item.isAudioTrack) {
            holder.jitterValueText.setText(String.valueOf(item.jitter));
            holder.audioLevelValueText.setText(String.valueOf(item.audioLevel));
            holder.dimensionsTableRow.setVisibility(View.GONE);
            holder.framerateTableRow.setVisibility(View.GONE);
            holder.jitterTableRow.setVisibility(View.VISIBLE);
            holder.audioLevelTableRow.setVisibility(View.VISIBLE);
        } else {
            holder.dimensionsValueText.setText(item.dimensions);
            holder.framerateValueText.setText(String.valueOf(item.framerate));
            holder.dimensionsTableRow.setVisibility(View.VISIBLE);
            holder.framerateTableRow.setVisibility(View.VISIBLE);
            holder.jitterTableRow.setVisibility(View.GONE);
            holder.audioLevelTableRow.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return statsListItems.size();
    }

    public void updateStatsData(List<StatsReport> statsReports,
                                List<RemoteParticipant> remoteParticipants,
                                Map<String, String> localVideoTrackNames){
        /*
         * Generate new items on a separate list to ensure statsListItems changes are only
         * performed on the UI thread to meet the threading requirement of RecyclerView.Adapter.
         */
        ImmutableList.Builder statsListItemsBuilder = new ImmutableList.Builder<StatsListItem>();

        // Generate stats items list from reports
        boolean localTracksAdded = false;
        for (StatsReport report : statsReports) {
            if (!localTracksAdded) {
                // go trough local tracks
                for (LocalAudioTrackStats localAudioTrackStats : report.getLocalAudioTrackStats()) {
                    StatsListItem item = new StatsListItem.Builder()
                            .baseTrackInfo(localAudioTrackStats)
                            .bytes(localAudioTrackStats.bytesSent)
                            .rtt(localAudioTrackStats.roundTripTime)
                            .jitter(localAudioTrackStats.jitter)
                            .audioLevel(localAudioTrackStats.audioLevel)
                            .trackName(context.getString(R.string.local_audio_track))
                            .isAudioTrack(true)
                            .isLocalTrack(true)
                            .build();
                    statsListItemsBuilder.add(item);
                }
                for (LocalVideoTrackStats localVideoTrackStats : report.getLocalVideoTrackStats()) {
                    String localVideoTrackName =
                            localVideoTrackNames.get(localVideoTrackStats.trackSid);
                    if (localVideoTrackName == null) {
                        localVideoTrackName = context.getString(R.string.local_video_track);
                    }
                    StatsListItem item = new StatsListItem.Builder()
                            .baseTrackInfo(localVideoTrackStats)
                            .bytes(localVideoTrackStats.bytesSent)
                            .rtt(localVideoTrackStats.roundTripTime)
                            .dimensions(localVideoTrackStats.dimensions.toString())
                            .framerate(localVideoTrackStats.frameRate)
                            .trackName(localVideoTrackName)
                            .isAudioTrack(false)
                            .isLocalTrack(true)
                            .build();
                    statsListItemsBuilder.add(item);
                }
                localTracksAdded = true;
            }
            int trackCount = 0;
            for (RemoteAudioTrackStats remoteAudioTrackStats : report.getRemoteAudioTrackStats()) {
                String trackName =
                        getParticipantName(remoteAudioTrackStats.trackSid, true, remoteParticipants) +
                                " " + context.getString(R.string.audio_track) + " " + trackCount;
                StatsListItem item = new StatsListItem.Builder()
                        .baseTrackInfo(remoteAudioTrackStats)
                        .bytes(remoteAudioTrackStats.bytesReceived)
                        .jitter(remoteAudioTrackStats.jitter)
                        .audioLevel(remoteAudioTrackStats.audioLevel)
                        .trackName(trackName)
                        .isAudioTrack(true)
                        .isLocalTrack(false)
                        .build();
                statsListItemsBuilder.add(item);
                trackCount++;
            }
            trackCount = 0;
            for (RemoteVideoTrackStats remoteVideoTrackStats : report.getRemoteVideoTrackStats()) {
                String trackName =
                        getParticipantName(remoteVideoTrackStats.trackSid, false, remoteParticipants) +
                                " " + context.getString(R.string.video_track) + " " + trackCount;
                StatsListItem item = new StatsListItem.Builder()
                        .baseTrackInfo(remoteVideoTrackStats)
                        .bytes(remoteVideoTrackStats.bytesReceived)
                        .dimensions(remoteVideoTrackStats.dimensions.toString())
                        .framerate(remoteVideoTrackStats.frameRate)
                        .trackName(trackName)
                        .isAudioTrack(false)
                        .isLocalTrack(false)
                        .build();
                statsListItemsBuilder.add(item);
                trackCount++;
            }
        }

        final ImmutableList immutableStatsListItems = statsListItemsBuilder.build();

        handler.post(new Runnable() {
            @Override
            public void run() {
                statsListItems.clear();
                statsListItems.addAll(immutableStatsListItems);
                notifyDataSetChanged();
            }
        });
    }

    private String getParticipantName(String trackSid, boolean isAudioTrack,
                                      List<RemoteParticipant> remoteParticipants) {
        for (RemoteParticipant remoteParticipant : remoteParticipants) {
            if (isAudioTrack) {
                RemoteAudioTrack remoteAudioTrack = getAudioTrack(remoteParticipant, trackSid);
                if (remoteAudioTrack != null) {
                    return remoteParticipant.getIdentity();
                }
            } else {
                RemoteVideoTrack remoteVideoTrack = getRemoteVideoTrack(remoteParticipant, trackSid);
                if (remoteVideoTrack != null) {
                    return remoteParticipant.getIdentity();
                }
            }
        }
        return "";
    }

    private RemoteAudioTrack getAudioTrack(RemoteParticipant remoteParticipant, String trackSid) {
        for (RemoteAudioTrackPublication remoteAudioTrackPublication :
                remoteParticipant.getRemoteAudioTracks()) {
            if (remoteAudioTrackPublication.getTrackSid().equals(trackSid)) {
                return remoteAudioTrackPublication.getRemoteAudioTrack();
            }
        }

        return null;
    }

    private RemoteVideoTrack getRemoteVideoTrack(RemoteParticipant remoteParticipant, String trackSid) {
        for (RemoteVideoTrackPublication remoteVideoTrackPublication :
                remoteParticipant.getRemoteVideoTracks()) {
            if (remoteVideoTrackPublication.getTrackSid().equals(trackSid)) {
                return remoteVideoTrackPublication.getRemoteVideoTrack();
            }
        }

        return null;
    }
}
