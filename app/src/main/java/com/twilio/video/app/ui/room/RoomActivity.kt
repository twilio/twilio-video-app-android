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
package com.twilio.video.app.ui.room

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnTextChanged
import com.google.android.material.snackbar.Snackbar
import com.twilio.audioswitch.AudioDevice
import com.twilio.audioswitch.AudioDevice.BluetoothHeadset
import com.twilio.audioswitch.AudioDevice.Speakerphone
import com.twilio.audioswitch.AudioDevice.WiredHeadset
import com.twilio.audioswitch.AudioSwitch
import com.twilio.video.app.R
import com.twilio.video.app.adapter.StatsListAdapter
import com.twilio.video.app.base.BaseActivity
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.data.api.AuthServiceError
import com.twilio.video.app.data.api.TokenService
import com.twilio.video.app.participant.ParticipantViewState
import com.twilio.video.app.sdk.RoomManager
import com.twilio.video.app.ui.room.RoomViewConfiguration.Connecting
import com.twilio.video.app.ui.room.RoomViewConfiguration.Lobby
import com.twilio.video.app.ui.room.RoomViewEffect.Connected
import com.twilio.video.app.ui.room.RoomViewEffect.Disconnected
import com.twilio.video.app.ui.room.RoomViewEffect.PermissionsDenied
import com.twilio.video.app.ui.room.RoomViewEffect.ShowConnectFailureDialog
import com.twilio.video.app.ui.room.RoomViewEffect.ShowMaxParticipantFailureDialog
import com.twilio.video.app.ui.room.RoomViewEffect.ShowTokenErrorDialog
import com.twilio.video.app.ui.room.RoomViewEvent.ActivateAudioDevice
import com.twilio.video.app.ui.room.RoomViewEvent.Connect
import com.twilio.video.app.ui.room.RoomViewEvent.DeactivateAudioDevice
import com.twilio.video.app.ui.room.RoomViewEvent.DisableLocalAudio
import com.twilio.video.app.ui.room.RoomViewEvent.DisableLocalVideo
import com.twilio.video.app.ui.room.RoomViewEvent.Disconnect
import com.twilio.video.app.ui.room.RoomViewEvent.EnableLocalAudio
import com.twilio.video.app.ui.room.RoomViewEvent.EnableLocalVideo
import com.twilio.video.app.ui.room.RoomViewEvent.OnPause
import com.twilio.video.app.ui.room.RoomViewEvent.OnResume
import com.twilio.video.app.ui.room.RoomViewEvent.SelectAudioDevice
import com.twilio.video.app.ui.room.RoomViewEvent.StartScreenCapture
import com.twilio.video.app.ui.room.RoomViewEvent.StopScreenCapture
import com.twilio.video.app.ui.room.RoomViewEvent.SwitchCamera
import com.twilio.video.app.ui.room.RoomViewEvent.ToggleLocalAudio
import com.twilio.video.app.ui.room.RoomViewEvent.ToggleLocalVideo
import com.twilio.video.app.ui.room.RoomViewModel.RoomViewModelFactory
import com.twilio.video.app.ui.settings.SettingsActivity
import com.twilio.video.app.util.InputUtils
import com.twilio.video.app.util.PermissionUtil
import io.uniflow.androidx.flow.onEvents
import io.uniflow.androidx.flow.onStates
import javax.inject.Inject
import timber.log.Timber

class RoomActivity : BaseActivity() {
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.connect)
    lateinit var connect: Button

    @BindView(R.id.disconnect)
    lateinit var disconnectButton: ImageButton

    @BindView(R.id.primary_video)
    lateinit var primaryVideoView: ParticipantPrimaryView

    @BindView(R.id.remote_video_thumbnails)
    lateinit var thumbnailRecyclerView: RecyclerView

    @BindView(R.id.local_video_image_button)
    lateinit var localVideoImageButton: ImageButton

    @BindView(R.id.local_audio_image_button)
    lateinit var localAudioImageButton: ImageButton

    @BindView(R.id.video_container)
    lateinit var frameLayout: FrameLayout

    @BindView(R.id.join_room_layout)
    lateinit var joinRoomLayout: LinearLayout

    @BindView(R.id.room_edit_text)
    lateinit var roomEditText: ClearableEditText

    @BindView(R.id.join_status_layout)
    lateinit var joinStatusLayout: LinearLayout

    @BindView(R.id.join_status)
    lateinit var joinStatusTextView: TextView

    @BindView(R.id.join_room_name)
    lateinit var joinRoomNameTextView: TextView

    @BindView(R.id.recording_notice)
    lateinit var recordingNoticeTextView: TextView

    @BindView(R.id.stats_recycler_view)
    lateinit var statsRecyclerView: RecyclerView

    @BindView(R.id.stats_disabled)
    lateinit var statsDisabledLayout: LinearLayout

    @BindView(R.id.stats_disabled_title)
    lateinit var statsDisabledTitleTextView: TextView

    @BindView(R.id.stats_disabled_description)
    lateinit var statsDisabledDescTextView: TextView
    private lateinit var switchCameraMenuItem: MenuItem
    private lateinit var pauseVideoMenuItem: MenuItem
    private lateinit var pauseAudioMenuItem: MenuItem
    private lateinit var screenCaptureMenuItem: MenuItem
    private lateinit var settingsMenuItem: MenuItem
    private lateinit var deviceMenuItem: MenuItem
    private var savedVolumeControlStream = 0
    private var displayName: String? = null
    private var localParticipantSid = LOCAL_PARTICIPANT_STUB_SID
    private lateinit var statsListAdapter: StatsListAdapter

    @Inject
    lateinit var tokenService: TokenService

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var roomManager: RoomManager

    @Inject
    lateinit var audioSwitch: AudioSwitch

    /** Coordinates participant thumbs and primary participant rendering.  */
    private lateinit var primaryParticipantController: PrimaryParticipantController
    private lateinit var participantAdapter: ParticipantAdapter
    private lateinit var roomViewModel: RoomViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = RoomViewModelFactory(roomManager, audioSwitch, PermissionUtil(this))
        roomViewModel = ViewModelProvider(this, factory).get(RoomViewModel::class.java)

        // So calls can be answered when screen is locked
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        // Grab views
        setContentView(R.layout.activity_room)
        ButterKnife.bind(this)
        setupThumbnailRecyclerView()

        // Setup toolbar
        setSupportActionBar(toolbar)

        // Cache volume control stream
        savedVolumeControlStream = volumeControlStream

        // setup participant controller
        primaryParticipantController = PrimaryParticipantController(primaryVideoView)
    }

    private fun setupThumbnailRecyclerView() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        thumbnailRecyclerView.layoutManager = layoutManager
        participantAdapter = ParticipantAdapter()
        participantAdapter
                .viewHolderEvents
                .observe(this, { viewEvent: RoomViewEvent -> roomViewModel.processInput(viewEvent) })
        thumbnailRecyclerView.adapter = participantAdapter
    }

    override fun onStart() {
        super.onStart()
        checkIntentURI()
    }

    override fun onResume() {
        super.onResume()
        displayName = sharedPreferences.getString(Preferences.DISPLAY_NAME, null)
        setTitle(displayName)
        roomViewModel.processInput(OnResume)
    }

    override fun onPause() {
        super.onPause()
        roomViewModel.processInput(OnPause)
    }

    private fun checkIntentURI(): Boolean {
        var isAppLinkProvided = false
        val uri = intent.data
        val roomName = UriRoomParser(UriWrapper(uri)).parseRoom()
        if (roomName != null) {
            roomEditText.setText(roomName)
            isAppLinkProvided = true
        }
        return isAppLinkProvided
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            val recordAudioPermissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
            val cameraPermissionGranted = grantResults[1] == PackageManager.PERMISSION_GRANTED
            val writeExternalStoragePermissionGranted = grantResults[2] == PackageManager.PERMISSION_GRANTED
            val permissionsGranted = (recordAudioPermissionGranted &&
                    cameraPermissionGranted &&
                    writeExternalStoragePermissionGranted)
            if (permissionsGranted) {
                roomViewModel.processInput(OnResume)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.room_menu, menu)
        settingsMenuItem = menu.findItem(R.id.settings_menu_item)
        // Grab menu items for updating later
        switchCameraMenuItem = menu.findItem(R.id.switch_camera_menu_item)
        pauseVideoMenuItem = menu.findItem(R.id.pause_video_menu_item)
        pauseAudioMenuItem = menu.findItem(R.id.pause_audio_menu_item)
        screenCaptureMenuItem = menu.findItem(R.id.share_screen_menu_item)
        deviceMenuItem = menu.findItem(R.id.device_menu_item)

        onStates(roomViewModel) { state ->
            if (state is RoomViewState) bindRoomViewState(state)
        }
        onEvents(roomViewModel) { eventWrapper ->
            eventWrapper.take()?.let { event ->
                if (event is RoomViewEffect) bindRoomViewEffects(event)
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.switch_camera_menu_item -> {
                roomViewModel.processInput(SwitchCamera)
                true
            }
            R.id.share_screen_menu_item -> {
                if (item.title == getString(R.string.share_screen)) {
                    requestScreenCapturePermission()
                } else {
                    roomViewModel.processInput(StopScreenCapture)
                }
                true
            }
            R.id.device_menu_item -> {
                displayAudioDeviceList()
                true
            }
            R.id.pause_audio_menu_item -> {
                if (item.title == getString(R.string.pause_audio))
                    roomViewModel.processInput(DisableLocalAudio)
                else
                    roomViewModel.processInput(EnableLocalAudio)
                true
            }
            R.id.pause_video_menu_item -> {
                if (item.title == getString(R.string.pause_video))
                    roomViewModel.processInput(DisableLocalVideo)
                else
                    roomViewModel.processInput(EnableLocalVideo)
                true
            }
            R.id.settings_menu_item -> {
                val intent = Intent(this@RoomActivity, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MEDIA_PROJECTION_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Snackbar.make(
                        primaryVideoView,
                        R.string.screen_capture_permission_not_granted,
                        Snackbar.LENGTH_LONG)
                        .show()
                return
            }
            data?.let { data ->
                roomViewModel.processInput(StartScreenCapture(resultCode, data))
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        roomViewModel.processInput(Disconnect)
    }

    @OnTextChanged(value = [R.id.room_edit_text], callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    fun onTextChanged(text: CharSequence?) {
        connect.isEnabled = !TextUtils.isEmpty(text)
    }

    @OnClick(R.id.connect)
    fun connectButtonClick() {
        InputUtils.hideKeyboard(this)
        connect.isEnabled = false
        // obtain room name
        val text = roomEditText.text
        if (text != null) {
            val roomName = text.toString()
            val viewEvent = Connect(displayName ?: "", roomName)
            roomViewModel.processInput(viewEvent)
        }
    }

    @OnClick(R.id.disconnect)
    fun disconnectButtonClick() {
        roomViewModel.processInput(Disconnect)
        // TODO Handle screen share
    }

    @OnClick(R.id.local_video_image_button)
    fun toggleLocalVideo() {
        roomViewModel.processInput(ToggleLocalVideo)
    }

    @OnClick(R.id.local_audio_image_button)
    fun toggleLocalAudio() {
        roomViewModel.processInput(ToggleLocalAudio)
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
                    PERMISSIONS_REQUEST_CODE)
        }
    }

    private fun updateLayout(roomViewState: RoomViewState) {
        var disconnectButtonState = View.GONE
        var joinRoomLayoutState = View.VISIBLE
        var joinStatusLayoutState = View.GONE
        var settingsMenuItemState = true
        var screenCaptureMenuItemState = false
        val roomEditable = roomEditText.text
        val isRoomTextNotEmpty = roomEditable != null && !roomEditable.toString().isEmpty()
        var connectButtonEnabled = isRoomTextNotEmpty
        var roomName = displayName
        var toolbarTitle = displayName
        var joinStatus = ""
        var recordingWarningVisibility = View.GONE
        when (roomViewState.configuration) {
            Connecting -> {
                disconnectButtonState = View.VISIBLE
                joinRoomLayoutState = View.GONE
                joinStatusLayoutState = View.VISIBLE
                recordingWarningVisibility = View.VISIBLE
                settingsMenuItemState = false
                connectButtonEnabled = false
                if (roomEditable != null) {
                    roomName = roomEditable.toString()
                }
                joinStatus = "Joining..."
            }
            RoomViewConfiguration.Connected -> {
                disconnectButtonState = View.VISIBLE
                joinRoomLayoutState = View.GONE
                joinStatusLayoutState = View.GONE
                settingsMenuItemState = false
                screenCaptureMenuItemState = true
                connectButtonEnabled = false
                roomName = roomViewState.title
                toolbarTitle = roomName
                joinStatus = ""
            }
            Lobby -> {
                connectButtonEnabled = isRoomTextNotEmpty
                screenCaptureMenuItemState = false
            }
        }
        val isMicEnabled = roomViewState.isMicEnabled
        val isCameraEnabled = roomViewState.isCameraEnabled
        val isLocalMediaEnabled = isMicEnabled && isCameraEnabled
        localAudioImageButton.isEnabled = isLocalMediaEnabled
        localVideoImageButton.isEnabled = isLocalMediaEnabled
        val micDrawable = if (roomViewState.isAudioMuted || !isLocalMediaEnabled) R.drawable.ic_mic_off_gray_24px else R.drawable.ic_mic_white_24px
        val videoDrawable = if (roomViewState.isVideoOff || !isLocalMediaEnabled) R.drawable.ic_videocam_off_gray_24px else R.drawable.ic_videocam_white_24px
        localAudioImageButton.setImageResource(micDrawable)
        localVideoImageButton.setImageResource(videoDrawable)
        statsListAdapter = StatsListAdapter(this)
        statsRecyclerView.adapter = statsListAdapter
        statsRecyclerView.layoutManager = LinearLayoutManager(this)
        disconnectButton.visibility = disconnectButtonState
        joinRoomLayout.visibility = joinRoomLayoutState
        joinStatusLayout.visibility = joinStatusLayoutState
        connect.isEnabled = connectButtonEnabled
        setTitle(toolbarTitle)
        joinStatusTextView.text = joinStatus
        joinRoomNameTextView.text = roomName
        recordingNoticeTextView.visibility = recordingWarningVisibility
        val pauseAudioTitle = getString(if (roomViewState.isAudioEnabled) R.string.pause_audio else R.string.resume_audio)
        val pauseVideoTitle = getString(if (roomViewState.isVideoEnabled) R.string.pause_video else R.string.resume_video)
        pauseAudioMenuItem.title = pauseAudioTitle
        pauseVideoMenuItem.title = pauseVideoTitle

        // TODO: Remove when we use a Service to obtainTokenAndConnect to a room
        settingsMenuItem.isVisible = settingsMenuItemState
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            screenCaptureMenuItem.isVisible = screenCaptureMenuItemState
            val screenCaptureResources = if (roomViewState.isScreenCaptureOn) {
                R.drawable.ic_stop_screen_share_white_24dp to getString(R.string.stop_screen_share)
            } else {
                R.drawable.ic_screen_share_white_24dp to getString(R.string.share_screen)
            }
            screenCaptureMenuItem.icon = ContextCompat.getDrawable(this,
                    screenCaptureResources.first)
            screenCaptureMenuItem.title = screenCaptureResources.second
        }
    }

    private fun setTitle(toolbarTitle: String?) {
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.title = toolbarTitle
        }
    }

    private fun setVolumeControl(setVolumeControl: Boolean) {
        volumeControlStream = if (setVolumeControl) {
            /*
             * Enable changing the volume using the up/down keys during a conversation
             */
            AudioManager.STREAM_VOICE_CALL
        } else {
            savedVolumeControlStream
        }
    }

    @TargetApi(21)
    private fun requestScreenCapturePermission() {
        Timber.d("Requesting permission to capture screen")
        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        // This initiates a prompt dialog for the user to confirm screen projection.
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(), MEDIA_PROJECTION_REQUEST_CODE)
    }

    private fun updateStatsUI(roomViewState: RoomViewState) {
        val enableStats = sharedPreferences.getBoolean(
                Preferences.ENABLE_STATS, Preferences.ENABLE_STATS_DEFAULT)
        if (enableStats) {
            when (roomViewState.configuration) {
                RoomViewConfiguration.Connected -> {
                    statsListAdapter.updateStatsData(roomViewState.roomStats)
                    statsRecyclerView.visibility = View.VISIBLE
                    statsDisabledLayout.visibility = View.GONE

                    // disable stats if there is room but no participants (no media)
                    val isStreamingMedia = roomViewState.participantThumbnails?.let { thumbnails ->
                        thumbnails.size > 1
                    } ?: false
                    if (!isStreamingMedia) {
                        statsDisabledTitleTextView.text = getString(R.string.stats_unavailable)
                        statsDisabledDescTextView.text = getString(R.string.stats_description_media_not_shared)
                        statsRecyclerView.visibility = View.GONE
                        statsDisabledLayout.visibility = View.VISIBLE
                    }
                }
                else -> {
                    statsDisabledTitleTextView.text = getString(R.string.stats_unavailable)
                    statsDisabledDescTextView.text = getString(R.string.stats_description_join_room)
                    statsRecyclerView.visibility = View.GONE
                    statsDisabledLayout.visibility = View.VISIBLE
                }
            }
        } else {
            statsDisabledTitleTextView.text = getString(R.string.stats_gathering_disabled)
            statsDisabledDescTextView.text = getString(R.string.stats_enable_in_settings)
            statsRecyclerView.visibility = View.GONE
            statsDisabledLayout.visibility = View.VISIBLE
        }
    }

    private fun toggleAudioDevice(enableAudioDevice: Boolean) {
        setVolumeControl(enableAudioDevice)
        val viewEvent = if (enableAudioDevice) ActivateAudioDevice else DeactivateAudioDevice
        roomViewModel.processInput(viewEvent)
    }

    private fun bindRoomViewState(roomViewState: RoomViewState) {
        deviceMenuItem.isVisible = roomViewState.availableAudioDevices?.isNotEmpty() ?: false
        renderPrimaryView(roomViewState.primaryParticipant)
        renderThumbnails(roomViewState)
        updateLayout(roomViewState)
        updateAudioDeviceIcon(roomViewState.selectedDevice)
        updateStatsUI(roomViewState)
    }

    private fun bindRoomViewEffects(roomViewEffect: RoomViewEffect) {
        when (roomViewEffect) {
            is Connected -> {
                toggleAudioDevice(true)
            }
            Disconnected -> {
                localParticipantSid = LOCAL_PARTICIPANT_STUB_SID
                // TODO Update stats
                toggleAudioDevice(false)
            }
            ShowConnectFailureDialog, ShowMaxParticipantFailureDialog -> {
                AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                        .setTitle(getString(R.string.room_screen_connection_failure_title))
                        .setMessage(getConnectFailureMessage(roomViewEffect))
                        .setNeutralButton(getString(android.R.string.ok), null)
                        .show()
                toggleAudioDevice(false)
            }
            is ShowTokenErrorDialog -> {
                val error = roomViewEffect.serviceError
                handleTokenError(error)
            }
            PermissionsDenied -> requestPermissions()
        }
    }

    private fun getConnectFailureMessage(roomViewEffect: RoomViewEffect) =
            getString(
                    when (roomViewEffect) {
                        ShowMaxParticipantFailureDialog -> R.string.room_screen_max_participant_failure_message
                        else -> R.string.room_screen_connection_failure_message
                    }
            )

    private fun updateAudioDeviceIcon(selectedAudioDevice: AudioDevice?) {
        val audioDeviceMenuIcon = when (selectedAudioDevice) {
            is BluetoothHeadset -> R.drawable.ic_bluetooth_white_24dp
            is WiredHeadset -> R.drawable.ic_headset_mic_white_24dp
            is Speakerphone -> R.drawable.ic_volume_up_white_24dp
            else -> R.drawable.ic_phonelink_ring_white_24dp
        }
        this.deviceMenuItem.setIcon(audioDeviceMenuIcon)
    }

    private fun renderPrimaryView(primaryParticipant: ParticipantViewState) {
        primaryParticipant.run {
            primaryParticipantController.renderAsPrimary(
                    if (isLocalParticipant) getString(R.string.you) else identity,
                    screenTrack,
                    videoTrack,
                    isMuted,
                    isMirrored)
            primaryVideoView.showIdentityBadge(!primaryParticipant.isLocalParticipant)
        }
    }

    private fun renderThumbnails(roomViewState: RoomViewState) {
        val newThumbnails = if (roomViewState.configuration is RoomViewConfiguration.Connected)
            roomViewState.participantThumbnails else null
        participantAdapter.submitList(newThumbnails)
    }

    private fun displayAudioDeviceList() {
        (roomViewModel.getCurrentState() as RoomViewState).let { viewState ->
            val selectedDevice = viewState.selectedDevice
            val audioDevices = viewState.availableAudioDevices
            if (selectedDevice != null && audioDevices != null) {
                val index = audioDevices.indexOf(selectedDevice)
                val audioDeviceNames = ArrayList<String>()
                for (a in audioDevices) {
                    audioDeviceNames.add(a.name)
                }
                createAudioDeviceDialog(
                        this,
                        index,
                        audioDeviceNames
                ) { dialogInterface: DialogInterface, i: Int ->
                    dialogInterface.dismiss()
                    val viewEvent = SelectAudioDevice(audioDevices[i])
                    roomViewModel.processInput(viewEvent)
                }
                        .show()
            }
        }
    }

    private fun createAudioDeviceDialog(
        activity: Activity,
        currentDevice: Int,
        availableDevices: ArrayList<String>,
        audioDeviceClickListener: DialogInterface.OnClickListener
    ): AlertDialog {
        val builder = AlertDialog.Builder(activity, R.style.AppTheme_Dialog)
        builder.setTitle(activity.getString(R.string.room_screen_select_device))
        builder.setSingleChoiceItems(
                availableDevices.toTypedArray<CharSequence>(),
                currentDevice,
                audioDeviceClickListener)
        return builder.create()
    }

    private fun handleTokenError(error: AuthServiceError?) {
        val errorMessage = if (error === AuthServiceError.EXPIRED_PASSCODE_ERROR) R.string.room_screen_token_expired_message else R.string.room_screen_token_retrieval_failure_message
        AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle(getString(R.string.room_screen_connection_failure_title))
                .setMessage(getString(errorMessage))
                .setNeutralButton(getString(android.R.string.ok), null)
                .show()
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
        private const val MEDIA_PROJECTION_REQUEST_CODE = 101

        // This will be used instead of real local participant sid,
        // because that information is unknown until room connection is fully established
        private const val LOCAL_PARTICIPANT_STUB_SID = ""
        fun startActivity(context: Context, appLink: Uri?) {
            val intent = Intent(context, RoomActivity::class.java)
            intent.data = appLink
            context.startActivity(intent)
        }
    }
}
