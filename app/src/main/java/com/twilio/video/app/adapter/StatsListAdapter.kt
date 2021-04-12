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
import androidx.recyclerview.widget.RecyclerView
import com.twilio.video.RemoteAudioTrack
import com.twilio.video.RemoteParticipant
import com.twilio.video.RemoteVideoTrack
import com.twilio.video.app.R
import com.twilio.video.app.databinding.StatsViewBinding
import com.twilio.video.app.model.StatsListItem
import com.twilio.video.app.sdk.RoomStats

class StatsListAdapter(private val context: Context) : RecyclerView.Adapter<StatsListAdapter.ViewHolder>() {

    private val statsListItems = ArrayList<StatsListItem>()
    private val handler: Handler = Handler(Looper.getMainLooper())

    class ViewHolder(internal val binding: StatsViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = StatsViewBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = statsListItems[position]
        val binding = holder.binding
        binding.trackName.text = item.trackName
        binding.trackSid.text = item.trackSid
        binding.codec.text = item.codec
        binding.packetsLost.text = item.packetsLost.toString()
        binding.bytes.text = item.bytes.toString()
        if (item.isLocalTrack) {
            binding.bytesTitle.text = context.getString(R.string.stats_bytes_sent)
            binding.rtt.text = item.rtt.toString()
            binding.rttRow.visibility = View.VISIBLE
        } else {
            binding.rttRow.visibility = View.GONE
            binding.bytesTitle.text = context.getString(R.string.stats_bytes_received)
        }
        if (item.isAudioTrack) {
            binding.jitter.text = item.jitter.toString()
            binding.audioLevel.text = item.audioLevel.toString()
            binding.dimensionsRow.visibility = View.GONE
            binding.framerateRow.visibility = View.GONE
            binding.jitterRow.visibility = View.VISIBLE
            binding.audioLevelRow.visibility = View.VISIBLE
        } else {
            binding.dimensions.text = item.dimensions
            binding.framerate.text = item.framerate.toString()
            binding.dimensionsRow.visibility = View.VISIBLE
            binding.framerateRow.visibility = View.VISIBLE
            binding.jitterRow.visibility = View.GONE
            binding.audioLevelRow.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return statsListItems.size
    }

    fun updateStatsData(roomStats: RoomStats?) {
        /*
         * Generate new items on a separate list to ensure statsListItems changes are only
         * performed on the UI thread to meet the threading requirement of RecyclerView.Adapter.
         */
        val statsItemList = mutableListOf<StatsListItem>()

        // Generate stats items list from reports
        var localTracksAdded = false
        roomStats?.statsReports?.let { statsReports ->
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
                        var localVideoTrackName = roomStats.localVideoTrackNames[localVideoTrackStats.trackSid]
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
                    val trackName = (getParticipantName(remoteAudioTrackStats.trackSid, true, roomStats.remoteParticipants) +
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
                            remoteVideoTrackStats.trackSid, false, roomStats.remoteParticipants) +
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
        }

        handler.post {
            statsListItems.clear()
            statsListItems.addAll(statsItemList.toList())
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
