package com.twilio.video.app.ui.room

import android.Manifest
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.video.RemoteVideoTrack
import com.twilio.video.app.BaseUnitTest
import com.twilio.video.app.participant.ParticipantManager
import com.twilio.video.app.participant.ParticipantViewState
import com.twilio.video.app.sdk.LocalParticipantManager
import com.twilio.video.app.sdk.RoomManager
import com.twilio.video.app.sdk.VideoTrackViewState
import com.twilio.video.app.ui.room.RoomEvent.ConnectFailure
import com.twilio.video.app.ui.room.RoomEvent.MaxParticipantFailure
import com.twilio.video.app.ui.room.RoomEvent.RecordingStarted
import com.twilio.video.app.ui.room.RoomEvent.RecordingStopped
import com.twilio.video.app.ui.room.RoomEvent.RemoteParticipantEvent.TrackSwitchOff
import com.twilio.video.app.ui.room.RoomViewConfiguration.Lobby
import com.twilio.video.app.ui.room.RoomViewEffect.Disconnected
import com.twilio.video.app.ui.room.RoomViewEffect.PermissionsDenied
import com.twilio.video.app.ui.room.RoomViewEffect.ShowConnectFailureDialog
import com.twilio.video.app.ui.room.RoomViewEffect.ShowMaxParticipantFailureDialog
import com.twilio.video.app.ui.room.RoomViewEvent.Connect
import com.twilio.video.app.ui.room.RoomViewEvent.OnResume
import com.twilio.video.app.util.PermissionUtil
import io.uniflow.android.test.TestViewObserver
import io.uniflow.android.test.createTestObserver
import io.uniflow.test.rule.TestDispatchersRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private const val PARTICIPANT_SID = "123"

@ExperimentalCoroutinesApi
class RoomViewModelTest : BaseUnitTest() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
    @get:Rule
    val coroutineScope = TestDispatchersRule(testDispatcher)

    val localParticipantManager = mock<LocalParticipantManager>()
    private val roomManager = RoomManager(mock(), mock(), mock(), testDispatcher).apply {
        localParticipantManager = this@RoomViewModelTest.localParticipantManager
    }
    private val participantViewState = ParticipantViewState(PARTICIPANT_SID, "Test Participant")
    private val participantManager = ParticipantManager().apply {
        addParticipant(participantViewState)
    }
    private val permissionUtil = mock<PermissionUtil>()
    private lateinit var testObserver: TestViewObserver
    private lateinit var viewModel: RoomViewModel
    private val localParticipantViewState = ParticipantViewState(isLocalParticipant = true)
    private val initialRoomViewState = RoomViewState(participantManager.primaryParticipant)

    @Before
    fun setUp() {
        viewModel = RoomViewModel(
                roomManager,
                mock(),
                permissionUtil,
                participantManager)
        testObserver = viewModel.createTestObserver()
    }

    @Test
    fun `The TrackSwitchOff event should create a new VideoTrackViewState for an existing ParticipantViewState`() {
        connect()
        val expectedVideoTrack = mock<RemoteVideoTrack>()
        roomManager.sendRoomEvent(TrackSwitchOff(PARTICIPANT_SID, expectedVideoTrack, false))

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
        connect()
        val expectedVideoTrack = mock<RemoteVideoTrack>()
        roomManager.sendRoomEvent(TrackSwitchOff(PARTICIPANT_SID, expectedVideoTrack, true))

        val expectedTrackViewState = VideoTrackViewState(expectedVideoTrack, true)
        val expectedParticipantViewState = participantViewState.copy(
                videoTrack = expectedTrackViewState)
        val updatedParticipant = (viewModel.getCurrentState() as RoomViewState).participantThumbnails?.find {
            it.sid == PARTICIPANT_SID
        }
        assertThat(updatedParticipant, equalTo(expectedParticipantViewState))
    }

    @Test
    fun `The OnResume event should set the isCameraEnabled view state property to true if camera permission is allowed`() {
        whenever(permissionUtil.isPermissionGranted(Manifest.permission.CAMERA))
                .thenReturn(true)
        val expectedViewState = initialRoomViewState.copy(isCameraEnabled = true)
        viewModel.processInput(OnResume)

        testObserver.verifySequence(initialRoomViewState, expectedViewState, PermissionsDenied)
    }

    @Test
    fun `The OnResume event should set the isCameraEnabled view state property to false if camera permission is denied`() {
        viewModel = RoomViewModel(
                roomManager,
                mock(),
                permissionUtil,
                participantManager,
                initialViewState = initialRoomViewState.copy(isCameraEnabled = true))
        whenever(permissionUtil.isPermissionGranted(Manifest.permission.CAMERA))
                .thenReturn(false)
        val expectedViewState = initialRoomViewState.copy(isCameraEnabled = false)

        viewModel.processInput(OnResume)

        testObserver.verifySequence(expectedViewState)

        assertThat(testObserver.lastEventOrNull, `is`(nullValue()))
    }

    @Test
    fun `The OnResume event should set the isMicEnabled view state property to true if camera permission is allowed`() {
        whenever(permissionUtil.isPermissionGranted(Manifest.permission.RECORD_AUDIO))
                .thenReturn(true)
        val expectedViewState = initialRoomViewState.copy(isMicEnabled = true)

        viewModel.processInput(OnResume)

        testObserver.verifySequence(initialRoomViewState, expectedViewState, PermissionsDenied)
    }

    @Test
    fun `The OnResume event should set the isMicEnabled view state property to false if camera permission is denied`() {
        viewModel = RoomViewModel(
                roomManager,
                mock(),
                permissionUtil,
                participantManager,
                initialViewState = initialRoomViewState.copy(isCameraEnabled = true))
        whenever(permissionUtil.isPermissionGranted(Manifest.permission.RECORD_AUDIO))
                .thenReturn(false)
        val expectedViewState = initialRoomViewState.copy(isMicEnabled = false)

        viewModel.processInput(OnResume)

        testObserver.verifySequence(expectedViewState)

        assertThat(testObserver.lastEventOrNull, `is`(nullValue()))
    }

    @Test
    fun `The OnResume event should invoke RoomManager onResume if camera and mic permissions are allowed`() {
        whenever(permissionUtil.isPermissionGranted(Manifest.permission.CAMERA))
                .thenReturn(true)
        whenever(permissionUtil.isPermissionGranted(Manifest.permission.RECORD_AUDIO))
                .thenReturn(true)

        viewModel.processInput(OnResume)

        verify(localParticipantManager).onResume()
    }

    @Test
    fun `The ConnectFailure event should send a ShowConnectFailureDialog ViewEffect`() {
        connect()
        roomManager.sendRoomEvent(ConnectFailure)

        testObserver.verifySequence(
                initialRoomViewState,
                initialRoomViewState.copy(configuration = RoomViewConfiguration.Connecting),
                ShowConnectFailureDialog,
                Disconnected,
                initialRoomViewState.copy(configuration = Lobby),
                initialRoomViewState.copy(configuration = Lobby,
                primaryParticipant = localParticipantViewState,
                participantThumbnails = listOf(localParticipantViewState)))
    }

    @Test
    fun `The MaxParticipantFailure event should send a ShowMaxParticipantFailureDialog ViewEffect`() {
        connect()
        roomManager.sendRoomEvent(MaxParticipantFailure)

        testObserver.verifySequence(
                initialRoomViewState,
                initialRoomViewState.copy(configuration = RoomViewConfiguration.Connecting),
                ShowMaxParticipantFailureDialog,
                Disconnected,
                initialRoomViewState.copy(configuration = Lobby),
                initialRoomViewState.copy(configuration = Lobby,
                        primaryParticipant = localParticipantViewState,
                        participantThumbnails = listOf(localParticipantViewState)))
    }

    @Test
    fun `The RecordingStarted event should set the isRecording property to true`() {
        connect()
        roomManager.sendRoomEvent(RecordingStarted)

        testObserver.verifySequence(
                initialRoomViewState,
                initialRoomViewState.copy(configuration = RoomViewConfiguration.Connecting),
                initialRoomViewState.copy(configuration = RoomViewConfiguration.Connecting,
                        isRecording = true))
    }

    @Test
    fun `The RecordingStopped event should set the isRecording property to false`() {
        connect()
        roomManager.sendRoomEvent(RecordingStopped)

        testObserver.verifySequence(
                initialRoomViewState,
                initialRoomViewState.copy(configuration = RoomViewConfiguration.Connecting),
                initialRoomViewState.copy(configuration = RoomViewConfiguration.Connecting,
                        isRecording = false))
    }

    private fun connect() =
        viewModel.processInput(Connect("Test", "Test Room"))
}
