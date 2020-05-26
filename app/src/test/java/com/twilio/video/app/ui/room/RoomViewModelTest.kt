package com.twilio.video.app.ui.room

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.twilio.video.VideoTrack
import com.twilio.video.app.participant.ParticipantManager
import com.twilio.video.app.participant.ParticipantViewState
import com.twilio.video.app.sdk.RoomManager
import com.twilio.video.app.sdk.VideoClient
import com.twilio.video.app.sdk.VideoTrackViewState
import com.twilio.video.app.ui.room.RoomEvent.ParticipantEvent.TrackSwitchOff
import io.reactivex.schedulers.TestScheduler
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test

class RoomViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `The TrackSwitchOff event should create a new VideoTrackViewState for an existing ParticipantViewState`() {
        val roomManager = RoomManager(mock(),
                VideoClient(mock(), mock(), mock()))
        val participantManager = ParticipantManager()
        val participantViewState = ParticipantViewState("123", "Test Participant")
        val expectedVideoTrack = mock<VideoTrack>()
        participantManager.addParticipant(participantViewState)
        val scheduler = TestScheduler()
        val viewModel = RoomViewModel(
                roomManager,
                mock(),
                participantManager,
                scheduler = scheduler)

        roomManager.sendParticipantEvent(TrackSwitchOff("123", expectedVideoTrack, true))
        scheduler.triggerActions()

        val expectedTrackViewState = VideoTrackViewState(expectedVideoTrack, true)
        val expectedParticipantViewState = participantViewState.copy(
                videoTrack = expectedTrackViewState)
        val updatedParticipant = viewModel.viewState.value?.participantThumbnails?.find {
            it.sid == "123"
        }
        assertThat(updatedParticipant, equalTo(expectedParticipantViewState))
    }
}