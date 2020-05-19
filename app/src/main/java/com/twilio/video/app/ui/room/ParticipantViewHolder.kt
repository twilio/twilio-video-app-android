package com.twilio.video.app.ui.room

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.twilio.video.NetworkQualityLevel
import com.twilio.video.NetworkQualityLevel.NETWORK_QUALITY_LEVEL_FIVE
import com.twilio.video.NetworkQualityLevel.NETWORK_QUALITY_LEVEL_FOUR
import com.twilio.video.NetworkQualityLevel.NETWORK_QUALITY_LEVEL_ONE
import com.twilio.video.NetworkQualityLevel.NETWORK_QUALITY_LEVEL_THREE
import com.twilio.video.NetworkQualityLevel.NETWORK_QUALITY_LEVEL_TWO
import com.twilio.video.NetworkQualityLevel.NETWORK_QUALITY_LEVEL_ZERO
import com.twilio.video.VideoTrack
import com.twilio.video.app.R
import com.twilio.video.app.participant.ParticipantViewState
import com.twilio.video.app.ui.room.RoomViewEvent.PinParticipant
import timber.log.Timber

internal class ParticipantViewHolder(private val thumb: ParticipantThumbView) :
        RecyclerView.ViewHolder(thumb) {

    fun bind(participantViewState: ParticipantViewState, viewEventAction: (RoomViewEvent) -> Unit) {
        Timber.d("bind ParticipantViewHolder with data item: %s", participantViewState)
        Timber.d("thumb: %s", thumb)

        thumb.run {
            setOnClickListener {
                viewEventAction(PinParticipant(participantViewState.sid))
            }
            setIdentity(participantViewState.identity)
            setMuted(participantViewState.isMuted)
            setPinned(participantViewState.isPinned)

            updateVideoTrack(participantViewState)

            networkQualityLevelImg?.let {
                setNetworkQualityLevelImage(it, participantViewState.networkQualityLevel)
            }
        }
    }

    private fun updateVideoTrack(participantViewState: ParticipantViewState) {
        thumb.run {
            if (videoTrack !== participantViewState.videoTrack) {
                removeRender(videoTrack, this)
                videoTrack = participantViewState.videoTrack
                videoTrack?.let { videoTrack ->
                    setState(ParticipantView.State.VIDEO)
                    videoTrack.addRenderer(this)
                } ?: setState(ParticipantView.State.NO_VIDEO)
            }
        }
    }

    private fun removeRender(videoTrack: VideoTrack?, view: ParticipantView) {
        if (videoTrack == null || !videoTrack.renderers.contains(view)) return
        videoTrack.removeRenderer(view)
    }

    private fun setNetworkQualityLevelImage(
        networkQualityImage: ImageView,
        networkQualityLevel: NetworkQualityLevel?
    ) {
        when (networkQualityLevel) {
            NETWORK_QUALITY_LEVEL_ZERO -> R.drawable.network_quality_level_0
            NETWORK_QUALITY_LEVEL_ONE -> R.drawable.network_quality_level_1
            NETWORK_QUALITY_LEVEL_TWO -> R.drawable.network_quality_level_2
            NETWORK_QUALITY_LEVEL_THREE -> R.drawable.network_quality_level_3
            NETWORK_QUALITY_LEVEL_FOUR -> R.drawable.network_quality_level_4
            NETWORK_QUALITY_LEVEL_FIVE -> R.drawable.network_quality_level_5
            else -> null
        }?.let { image ->
            networkQualityImage.visibility = View.VISIBLE
            networkQualityImage.setImageResource(image)
        } ?: run { networkQualityImage.visibility = View.GONE }
    }
}
