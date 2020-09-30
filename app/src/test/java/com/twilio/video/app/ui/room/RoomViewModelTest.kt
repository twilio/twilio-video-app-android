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
import com.twilio.video.app.ui.room.RoomEvent.ConnectFailure
import com.twilio.video.app.ui.room.RoomEvent.MaxParticipantFailure
import com.twilio.video.app.ui.room.RoomEvent.ParticipantEvent.TrackSwitchOff
import com.twilio.video.app.ui.room.RoomViewEffect.CheckLocalMedia
import com.twilio.video.app.ui.room.RoomViewEffect.Disconnected
import com.twilio.video.app.ui.room.RoomViewEffect.ShowConnectFailureDialog
import com.twilio.video.app.ui.room.RoomViewEffect.ShowMaxParticipantFailureDialog
import com.twilio.video.app.util.PermissionUtil
import io.reactivex.schedulers.TestScheduler
import io.uniflow.android.test.TestViewObserver
import io.uniflow.android.test.createTestObserver
import io.uniflow.test.rule.TestDispatchersRule
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private const val PARTICIPANT_SID = "123"

class RoomViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineScope = TestDispatchersRule()

    private val roomManager = RoomManager(mock(),
            VideoClient(mock(), mock()))
    private val scheduler = TestScheduler()
    private val participantViewState = ParticipantViewState(PARTICIPANT_SID, "Test Participant")
    private val participantManager = ParticipantManager().apply {
        addParticipant(participantViewState)
    }
    val permissionUtil = mock<PermissionUtil>()
    private lateinit var testObserver: TestViewObserver
    private lateinit var viewModel: RoomViewModel

    @Before
    fun setUp() {
        viewModel = RoomViewModel(
                roomManager,
                mock(),
                permissionUtil,
                participantManager,
                scheduler = scheduler)
        testObserver = viewModel.createTestObserver()
    }

    @Test
    fun `The TrackSwitchOff event should create a new VideoTrackViewState for an existing ParticipantViewState`() {
        val expectedVideoTrack = mock<RemoteVideoTrack>()

        roomManager.sendRoomEvent(TrackSwitchOff(PARTICIPANT_SID, expectedVideoTrack, false))
        scheduler.triggerActions()

        val expectedTrackViewState = VideoTrackViewState(expectedVideoTrack)
        val expectedParticipantViewState = participantViewState.copy(
                videoTrack = expectedTrackViewState)
        val updatedParticipant = (viewModel.getCurrentState() as RoomViewState).participantThumbnails?.find {
            it.sid == PARTICIPANT_SID
        }
        assertThat(updatedParticipant, equalTo(expectedParticipantViewState))
    }

    @Test
    fun `The TrackSwitchOff event should create a new VideoTrackViewState for an existing ParticipantViewState with the switch off set to true`() {
        val expectedVideoTrack = mock<RemoteVideoTrack>()

        roomManager.sendRoomEvent(TrackSwitchOff(PARTICIPANT_SID, expectedVideoTrack, true))
        scheduler.triggerActions()

        val expectedTrackViewState = VideoTrackViewState(expectedVideoTrack, true)
        val expectedParticipantViewState = participantViewState.copy(
                videoTrack = expectedTrackViewState)
        val updatedParticipant = (viewModel.getCurrentState() as RoomViewState).participantThumbnails?.find {
            it.sid == PARTICIPANT_SID
        }
        assertThat(updatedParticipant, equalTo(expectedParticipantViewState))
    }

    @Test
    fun `The CheckLocalMedia event should set the isCameraEnabled view state property to true if camera permission is allowed`() {
        whenever(permissionUtil.isPermissionGranted(Manifest.permission.CAMERA))
                .thenReturn(true)
        val expectedViewState = RoomViewState(isCameraEnabled = true)

        viewModel.processInput(RoomViewEvent.CheckPermissions)

        testObserver.verifySequence(RoomViewState(), expectedViewState)
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

        viewModel.processInput(RoomViewEvent.CheckPermissions)

        testObserver.verifySequence(expectedViewState)

        assertThat(testObserver.lastEventOrNull, `is`(nullValue()))
    }

    @Test
    fun `The CheckLocalMedia event should set the isMicEnabled view state property to true if camera permission is allowed`() {
        whenever(permissionUtil.isPermissionGranted(Manifest.permission.RECORD_AUDIO))
                .thenReturn(true)
        val expectedViewState = RoomViewState(isMicEnabled = true)

        viewModel.processInput(RoomViewEvent.CheckPermissions)

        testObserver.verifySequence(RoomViewState(), expectedViewState)
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

        viewModel.processInput(RoomViewEvent.CheckPermissions)

        testObserver.verifySequence(expectedViewState)

        assertThat(testObserver.lastEventOrNull, `is`(nullValue()))
    }

    @Test
    fun `The CheckLocalMedia event should send a CheckLocalMedia ViewEffect if camera and mic permissions are allowed`() {
        whenever(permissionUtil.isPermissionGranted(Manifest.permission.CAMERA))
                .thenReturn(true)
        whenever(permissionUtil.isPermissionGranted(Manifest.permission.RECORD_AUDIO))
                .thenReturn(true)

        viewModel.processInput(RoomViewEvent.CheckPermissions)

        assertThat(testObserver.lastEventOrNull is CheckLocalMedia, equalTo(true))
    }

    @Test
    fun `The ConnectFailure event should send a ShowConnectFailureDialog ViewEffect`() {
        roomManager.sendRoomEvent(ConnectFailure)
        scheduler.triggerActions()

        testObserver.verifySequence(
                RoomViewState(),
                ShowConnectFailureDialog,
                Disconnected,
                lobbyState())
    }

    @Test
    fun `The MaxParticipantFailure event should send a ShowMaxParticipantFailureDialog ViewEffect`() {
        roomManager.sendRoomEvent(MaxParticipantFailure)
        scheduler.triggerActions()

        testObserver.verifySequence(
                RoomViewState(),
                ShowMaxParticipantFailureDialog,
                Disconnected,
                lobbyState())
    }

    private fun lobbyState() =
            RoomViewState(
                    isLobbyLayoutVisible = true,
                    isConnectingLayoutVisible = false,
                    isConnectedLayoutVisible = false
            )
}
