/*
 * Copyright (C) 2019 Twilio, Inc.
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

package com.twilio.video.app.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import butterknife.BindView
import butterknife.ButterKnife
import com.twilio.video.RemoteAudioTrack
import com.twilio.video.RemoteParticipant
import com.twilio.video.RemoteVideoTrack
import com.twilio.video.StatsReport
import com.twilio.video.app.R
import com.twilio.video.app.model.StatsListItem
import java.util.ArrayList

class StatsListAdapter(private val context: Context) : RecyclerView.Adapter<StatsListAdapter.ViewHolder>() {

    private val statsListItems = ArrayList<StatsListItem>()
    private val handler: Handler

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.stats_track_name)
        internal var trackNameText: TextView? = null

        @BindView(R.id.stats_track_sid_value)
        internal var trackSidValueText: TextView? = null

        @BindView(R.id.stats_codec_value)
        internal var codecValueText: TextView? = null

        @BindView(R.id.stats_packets_value)
        internal var packetsValueText: TextView? = null

        @BindView(R.id.stats_bytes_title)
        internal var bytesTitleText: TextView? = null

        @BindView(R.id.stats_bytes_value)
        internal var bytesValueText: TextView? = null

        @BindView(R.id.stats_rtt_value)
        internal var rttValueText: TextView? = null

        @BindView(R.id.stats_jitter_value)
        internal var jitterValueText: TextView? = null

        @BindView(R.id.stats_audio_level_value)
        internal var audioLevelValueText: TextView? = null

        @BindView(R.id.stats_dimensions_value)
        internal var dimensionsValueText: TextView? = null

        @BindView(R.id.stats_framerate_value)
        internal var framerateValueText: TextView? = null

        @BindView(R.id.stats_rtt_row)
        internal var rttTableRow: TableRow? = null

        @BindView(R.id.stats_jitter_row)
        internal var jitterTableRow: TableRow? = null

        @BindView(R.id.stats_audio_level_row)
        internal var audioLevelTableRow: TableRow? = null

        @BindView(R.id.stats_dimensions_row)
        internal var dimensionsTableRow: TableRow? = null

        @BindView(R.id.stats_framerate_row)
        internal var framerateTableRow: TableRow? = null

        init {
            ButterKnife.bind(this, itemView)
        }
    }

    init {
        handler = Handler(Looper.getMainLooper())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.stats_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = statsListItems[position]
        holder.trackNameText!!.text = item.trackName
        holder.trackSidValueText!!.text = item.trackSid
        holder.codecValueText!!.text = item.codec
        holder.packetsValueText!!.text = item.packetsLost.toString()
        holder.bytesValueText!!.text = item.bytes.toString()
        if (item.isLocalTrack) {
            holder.bytesTitleText!!.text = context.getString(R.string.stats_bytes_sent)
            holder.rttValueText!!.text = item.rtt.toString()
            holder.rttTableRow!!.visibility = View.VISIBLE
        } else {
            holder.rttTableRow!!.visibility = View.GONE
            holder.bytesTitleText!!.text = context.getString(R.string.stats_bytes_received)
        }
        if (item.isAudioTrack) {
            holder.jitterValueText!!.text = item.jitter.toString()
            holder.audioLevelValueText!!.text = item.audioLevel.toString()
            holder.dimensionsTableRow!!.visibility = View.GONE
            holder.framerateTableRow!!.visibility = View.GONE
            holder.jitterTableRow!!.visibility = View.VISIBLE
            holder.audioLevelTableRow!!.visibility = View.VISIBLE
        } else {
            holder.dimensionsValueText!!.text = item.dimensions
            holder.framerateValueText!!.text = item.framerate.toString()
            holder.dimensionsTableRow!!.visibility = View.VISIBLE
            holder.framerateTableRow!!.visibility = View.VISIBLE
            holder.jitterTableRow!!.visibility = View.GONE
            holder.audioLevelTableRow!!.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return statsListItems.size
    }

    fun updateStatsData(
        statsReports: List<StatsReport>,
        remoteParticipants: List<RemoteParticipant>,
        localVideoTrackNames: Map<String, String>
    ) {
        /*
         * Generate new items on a separate list to ensure statsListItems changes are only
         * performed on the UI thread to meet the threading requirement of RecyclerView.Adapter.
         */
        val statsItemList = mutableListOf<StatsListItem>()

        // Generate stats items list from reports
        var localTracksAdded = false
        for (report in statsReports) {
            if (!localTracksAdded) {
                // go trough local tracks
                for (localAudioTrackStats in report.localAudioTrackStats) {
                    val item = StatsListItem.Builder()
                            .baseTrackInfo(localAudioTrackStats)
                            .bytes(localAudioTrackStats.bytesSent)
                            .rtt(localAudioTrackStats.roundTripTime)
                            .jitter(localAudioTrackStats.jitter)
                            .audioLevel(localAudioTrackStats.audioLevel)
                            .trackName(context.getString(R.string.local_audio_track))
                            .isAudioTrack(true)
                            .isLocalTrack(true)
                            .build()
                    statsItemList.add(item)
                }
                for (localVideoTrackStats in report.localVideoTrackStats) {
                    var localVideoTrackName = localVideoTrackNames[localVideoTrackStats.trackSid]
                    if (localVideoTrackName == null) {
                        localVideoTrackName = context.getString(R.string.local_video_track)
                    }
                    val item = StatsListItem.Builder()
                            .baseTrackInfo(localVideoTrackStats)
                            .bytes(localVideoTrackStats.bytesSent)
                            .rtt(localVideoTrackStats.roundTripTime)
                            .dimensions(localVideoTrackStats.dimensions.toString())
                            .framerate(localVideoTrackStats.frameRate)
                            .trackName(localVideoTrackName)
                            .isAudioTrack(false)
                            .isLocalTrack(true)
                            .build()
                    statsItemList.add(item)
                }
                localTracksAdded = true
            }
            var trackCount = 0
            for (remoteAudioTrackStats in report.remoteAudioTrackStats) {
                val trackName = (getParticipantName(remoteAudioTrackStats.trackSid, true, remoteParticipants) +
                        " " +
                        context.getString(R.string.audio_track) +
                        " " +
                        trackCount)
                val item = StatsListItem.Builder()
                        .baseTrackInfo(remoteAudioTrackStats)
                        .bytes(remoteAudioTrackStats.bytesReceived)
                        .jitter(remoteAudioTrackStats.jitter)
                        .audioLevel(remoteAudioTrackStats.audioLevel)
                        .trackName(trackName)
                        .isAudioTrack(true)
                        .isLocalTrack(false)
                        .build()
                statsItemList.add(item)
                trackCount++
            }
            trackCount = 0
            for (remoteVideoTrackStats in report.remoteVideoTrackStats) {
                val trackName = (getParticipantName(
                        remoteVideoTrackStats.trackSid, false, remoteParticipants) +
                        " " +
                        context.getString(R.string.video_track) +
                        " " +
                        trackCount)
                val item = StatsListItem.Builder()
                        .baseTrackInfo(remoteVideoTrackStats)
                        .bytes(remoteVideoTrackStats.bytesReceived)
                        .dimensions(remoteVideoTrackStats.dimensions.toString())
                        .framerate(remoteVideoTrackStats.frameRate)
                        .trackName(trackName)
                        .isAudioTrack(false)
                        .isLocalTrack(false)
                        .build()
                statsItemList.add(item)
                trackCount++
            }
        }

        handler.post {
            statsListItems.clear()
            statsListItems.addAll(statsListItems.toList())
            notifyDataSetChanged()
        }
    }

    private fun getParticipantName(
        trackSid: String,
        isAudioTrack: Boolean,
        remoteParticipants: List<RemoteParticipant>
    ): String {
        for (remoteParticipant in remoteParticipants) {
            if (isAudioTrack) {
                val remoteAudioTrack = getAudioTrack(remoteParticipant, trackSid)
                if (remoteAudioTrack != null) {
                    return remoteParticipant.identity
                }
            } else {
                val remoteVideoTrack = getRemoteVideoTrack(remoteParticipant, trackSid)
                if (remoteVideoTrack != null) {
                    return remoteParticipant.identity
                }
            }
        }
        return ""
    }

    private fun getAudioTrack(remoteParticipant: RemoteParticipant, trackSid: String): RemoteAudioTrack? {
        for (remoteAudioTrackPublication in remoteParticipant.remoteAudioTracks) {
            if (remoteAudioTrackPublication.trackSid == trackSid) {
                return remoteAudioTrackPublication.remoteAudioTrack
            }
        }

        return null
    }

    private fun getRemoteVideoTrack(
        remoteParticipant: RemoteParticipant,
        trackSid: String
    ): RemoteVideoTrack? {
        for (remoteVideoTrackPublication in remoteParticipant.remoteVideoTracks) {
            if (remoteVideoTrackPublication.trackSid == trackSid) {
                return remoteVideoTrackPublication.remoteVideoTrack
            }
        }

        return null
    }
}
