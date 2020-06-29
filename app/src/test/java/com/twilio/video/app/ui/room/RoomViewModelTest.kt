package com.twilio.video.app.ui.room

import android.Manifest
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.video.RemoteVideoTrack
import com.twilio.video.app.participant.ParticipantManager
import com.twilio.video.app.participant.ParticipantViewState
import com.twilio.video.app.sdk.RoomManager
import com.twilio.video.app.sdk.VideoClient
import com.twilio.video.app.sdk.VideoTrackViewState
import com.twilio.video.app.ui.room.RoomEvent.ParticipantEvent.TrackSwitchOff
import com.twilio.video.app.util.PermissionUtil
import io.reactivex.schedulers.TestScheduler
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test

private const val PARTICIPANT_SID = "123"

class RoomViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val roomManager = RoomManager(mock(),
            VideoClient(mock(), mock()))
    private val scheduler = TestScheduler()
    private val participantViewState = ParticipantViewState(PARTICIPANT_SID, "Test Participant")
    private val participantManager = ParticipantManager().apply {
        addParticipant(participantViewState)
    }
    val permissionUtil = mock<PermissionUtil>()
    private var viewModel = RoomViewModel(
            roomManager,
            mock(),
            permissionUtil,
            participantManager,
            scheduler = scheduler)

    @Test
    fun `The TrackSwitchOff event should create a new VideoTrackViewState for an existing ParticipantViewState`() {
        val expectedVideoTrack = mock<RemoteVideoTrack>()

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
        val expectedVideoTrack = mock<RemoteVideoTrack>()

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

    @Test
    fun `The CheckLocalMedia event should set the isCameraEnabled view state property to true if camera permission is allowed`() {
        whenever(permissionUtil.isPermissionGranted(Manifest.permission.CAMERA))
                .thenReturn(true)
        val expectedViewState = RoomViewState(isCameraEnabled = true)

        viewModel.processInput(RoomViewEvent.CheckLocalMedia)

        assertThat(viewModel.viewState.value, equalTo(expectedViewState))
    }

    @Test
    fun `The CheckLocalMedia event should set the isCameraEnabled view state property to false if camera permission is denied`() {
        viewModel = RoomViewModel(
                roomManager,
                mock(),
                permissionUtil,
                participantManager,
                scheduler = scheduler,
                initialViewState = RoomViewState(isCameraEnabled = true))
        whenever(permissionUtil.isPermissionGranted(Manifest.permission.CAMERA))
                .thenReturn(false)
        val expectedViewState = RoomViewState(isCameraEnabled = false)

        viewModel.processInput(RoomViewEvent.CheckLocalMedia)

        assertThat(viewModel.viewState.value, equalTo(expectedViewState))
        assertThat(viewModel.viewEffects.value, `is`(nullValue()))
    }

    @Test
    fun `The CheckLocalMedia event should set the isMicEnabled view state property to true if camera permission is allowed`() {
        whenever(permissionUtil.isPermissionGranted(Manifest.permission.RECORD_AUDIO))
                .thenReturn(true)
        val expectedViewState = RoomViewState(isMicEnabled = true)

        viewModel.processInput(RoomViewEvent.CheckLocalMedia)

        assertThat(viewModel.viewState.value, equalTo(expectedViewState))
    }

    @Test
    fun `The CheckLocalMedia event should set the isMicEnabled view state property to false if camera permission is denied`() {
        viewModel = RoomViewModel(
                roomManager,
                mock(),
                permissionUtil,
                participantManager,
                scheduler = scheduler,
                initialViewState = RoomViewState(isMicEnabled = true))
        whenever(permissionUtil.isPermissionGranted(Manifest.permission.RECORD_AUDIO))
                .thenReturn(false)
        val expectedViewState = RoomViewState(isMicEnabled = false)

        viewModel.processInput(RoomViewEvent.CheckLocalMedia)

        assertThat(viewModel.viewState.value, equalTo(expectedViewState))
        assertThat(viewModel.viewEffects.value, `is`(nullValue()))
    }

    @Test
    fun `The CheckLocalMedia event should send a CheckLocalMedia ViewEffect if camera and mic permissions are allowed`() {
        whenever(permissionUtil.isPermissionGranted(Manifest.permission.CAMERA))
                .thenReturn(true)
        whenever(permissionUtil.isPermissionGranted(Manifest.permission.RECORD_AUDIO))
                .thenReturn(true)

        viewModel.processInput(RoomViewEvent.CheckLocalMedia)

        val expectedViewEffect = viewModel.viewEffects.value!!
                .getContentIfNotHandled() is RoomViewEffect.CheckLocalMedia
        assertThat(expectedViewEffect, equalTo(true))
    }
}
