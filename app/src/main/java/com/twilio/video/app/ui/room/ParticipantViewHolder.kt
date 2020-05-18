package com.twilio.video.app.ui.room

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.twilio.video.NetworkQualityLevel
import com.twilio.video.VideoTrack
import com.twilio.video.app.R
import com.twilio.video.app.participant.ParticipantViewState
import timber.log.Timber

internal class ParticipantViewHolder(private val thumb: ParticipantThumbView) :
        RecyclerView.ViewHolder(thumb) {

    fun bind(participantViewState: ParticipantViewState, viewEventAction: (RoomViewEvent) -> Unit) {
        Timber.d("bind ParticipantViewHolder with data item: %s", participantViewState)
        Timber.d("thumb: %s", thumb)

        thumb.setOnClickListener {
            viewEventAction(RoomViewEvent.PinParticipant(participantViewState.sid))
        }
        thumb.setIdentity(participantViewState.identity)
        thumb.setMuted(participantViewState.isMuted)
        thumb.setPinned(participantViewState.isPinned)

        updateVideoTrack(participantViewState)

        thumb.networkQualityLevelImg?.let {
            setNetworkQualityLevelImage(it, participantViewState.networkQualityLevel)
        }
    }

    private fun updateVideoTrack(participantViewState: ParticipantViewState) {

        if (thumb.videoTrack !== participantViewState.videoTrack) {
            removeRender(thumb.videoTrack, thumb)
            thumb.videoTrack = participantViewState.videoTrack
            if (thumb.videoTrack != null) {
                thumb.setState(ParticipantView.State.VIDEO)
                thumb.videoTrack.addRenderer(thumb)
            } else {
                thumb.setState(ParticipantView.State.NO_VIDEO)
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
        if (networkQualityLevel == null ||
                networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_UNKNOWN) {
            networkQualityImage.visibility = View.GONE
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_ZERO) {
            networkQualityImage.visibility = View.VISIBLE
            networkQualityImage.setImageResource(R.drawable.network_quality_level_0)
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_ONE) {
            networkQualityImage.visibility = View.VISIBLE
            networkQualityImage.setImageResource(R.drawable.network_quality_level_1)
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_TWO) {
            networkQualityImage.visibility = View.VISIBLE
            networkQualityImage.setImageResource(R.drawable.network_quality_level_2)
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_THREE) {
            networkQualityImage.visibility = View.VISIBLE
            networkQualityImage.setImageResource(R.drawable.network_quality_level_3)
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_FOUR) {
            networkQualityImage.visibility = View.VISIBLE
            networkQualityImage.setImageResource(R.drawable.network_quality_level_4)
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_FIVE) {
            networkQualityImage.visibility = View.VISIBLE
            networkQualityImage.setImageResource(R.drawable.network_quality_level_5)
        }
    }
}
