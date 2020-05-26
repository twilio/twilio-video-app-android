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

private const val PARTICIPANT_SID = "123"

class RoomViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val roomManager = RoomManager(mock(),
            VideoClient(mock(), mock(), mock()))
    private val scheduler = TestScheduler()
    private val participantViewState = ParticipantViewState(PARTICIPANT_SID, "Test Participant")
    private val participantManager = ParticipantManager().apply {
        addParticipant(participantViewState)
    }
    private val viewModel = RoomViewModel(
            roomManager,
            mock(),
            participantManager,
            scheduler = scheduler)

    @Test
    fun `The TrackSwitchOff event should create a new VideoTrackViewState for an existing ParticipantViewState`() {
        val expectedVideoTrack = mock<VideoTrack>()

        roomManager.sendParticipantEvent(TrackSwitchOff(PARTICIPANT_SID, expectedVideoTrack, false))
        scheduler.triggerActions()

        val expectedTrackViewState = VideoTrackViewState(expectedVideoTrack)
        val expectedParticipantViewState = participantViewState.copy(
                videoTrack = expectedTrackViewState)
        val updatedParticipant = viewModel.viewState.value?.participantThumbnails?.find {
            it.sid == PARTICIPANT_SID
        }
        assertThat(updatedParticipant, equalTo(expectedParticipantViewState))
    }

    @Test
    fun `The TrackSwitchOff event should create a new VideoTrackViewState for an existing ParticipantViewState with the switch off set to true`() {
        val expectedVideoTrack = mock<VideoTrack>()

        roomManager.sendParticipantEvent(TrackSwitchOff(PARTICIPANT_SID, expectedVideoTrack, true))
        scheduler.triggerActions()

        val expectedTrackViewState = VideoTrackViewState(expectedVideoTrack, true)
        val expectedParticipantViewState = participantViewState.copy(
                videoTrack = expectedTrackViewState)
        val updatedParticipant = viewModel.viewState.value?.participantThumbnails?.find {
            it.sid == PARTICIPANT_SID
        }
        assertThat(updatedParticipant, equalTo(expectedParticipantViewState))
    }
}