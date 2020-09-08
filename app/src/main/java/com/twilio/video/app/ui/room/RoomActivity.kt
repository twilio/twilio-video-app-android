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
import androidx.core.content.PermissionChecker
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
import com.twilio.video.AspectRatio
import com.twilio.video.CameraCapturer
import com.twilio.video.LocalAudioTrack
import com.twilio.video.LocalParticipant
import com.twilio.video.LocalTrackPublicationOptions
import com.twilio.video.LocalVideoTrack
import com.twilio.video.Room
import com.twilio.video.ScreenCapturer
import com.twilio.video.StatsListener
import com.twilio.video.StatsReport
import com.twilio.video.TrackPriority
import com.twilio.video.VideoConstraints
import com.twilio.video.VideoDimensions
import com.twilio.video.app.R
import com.twilio.video.app.adapter.StatsListAdapter
import com.twilio.video.app.base.BaseActivity
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.data.api.AuthServiceError
import com.twilio.video.app.data.api.TokenService
import com.twilio.video.app.participant.ParticipantViewState
import com.twilio.video.app.sdk.CAMERA_TRACK_NAME
import com.twilio.video.app.sdk.MICROPHONE_TRACK_NAME
import com.twilio.video.app.sdk.RoomManager
import com.twilio.video.app.sdk.SCREEN_TRACK_NAME
import com.twilio.video.app.sdk.VideoTrackViewState
import com.twilio.video.app.udf.ViewEffect
import com.twilio.video.app.ui.room.RoomViewEffect.ShowConnectFailureDialog
import com.twilio.video.app.ui.room.RoomViewEffect.ShowTokenErrorDialog
import com.twilio.video.app.ui.room.RoomViewEvent.ActivateAudioDevice
import com.twilio.video.app.ui.room.RoomViewEvent.CheckPermissions
import com.twilio.video.app.ui.room.RoomViewEvent.Connect
import com.twilio.video.app.ui.room.RoomViewEvent.Disconnect
import com.twilio.video.app.ui.room.RoomViewEvent.RefreshViewState
import com.twilio.video.app.ui.room.RoomViewEvent.ScreenTrackRemoved
import com.twilio.video.app.ui.room.RoomViewEvent.SelectAudioDevice
import com.twilio.video.app.ui.room.RoomViewEvent.ToggleLocalVideo
import com.twilio.video.app.ui.room.RoomViewEvent.VideoTrackRemoved
import com.twilio.video.app.ui.room.RoomViewModel.RoomViewModelFactory
import com.twilio.video.app.ui.settings.SettingsActivity
import com.twilio.video.app.util.CameraCapturerCompat
import com.twilio.video.app.util.InputUtils
import com.twilio.video.app.util.PermissionUtil
import com.twilio.video.app.util.StatsScheduler
import javax.inject.Inject
import timber.log.Timber

class RoomActivity : BaseActivity() {
    private val aspectRatios = arrayOf(AspectRatio.ASPECT_RATIO_4_3, AspectRatio.ASPECT_RATIO_16_9, AspectRatio.ASPECT_RATIO_11_9)
    private val videoDimensions = arrayOf(
            VideoDimensions.CIF_VIDEO_DIMENSIONS,
            VideoDimensions.VGA_VIDEO_DIMENSIONS,
            VideoDimensions.WVGA_VIDEO_DIMENSIONS,
            VideoDimensions.HD_540P_VIDEO_DIMENSIONS,
            VideoDimensions.HD_720P_VIDEO_DIMENSIONS,
            VideoDimensions.HD_960P_VIDEO_DIMENSIONS,
            VideoDimensions.HD_S1080P_VIDEO_DIMENSIONS,
            VideoDimensions.HD_1080P_VIDEO_DIMENSIONS
    )

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
    private var localParticipant: LocalParticipant? = null
    private var localParticipantSid = LOCAL_PARTICIPANT_STUB_SID
    private var room: Room? = null
    private var videoConstraints: VideoConstraints? = null
    private var localAudioTrack: LocalAudioTrack? = null
    private var cameraVideoTrack: LocalVideoTrack? = null
    private var restoreLocalVideoCameraTrack = false
    private var screenVideoTrack: LocalVideoTrack? = null
    private var cameraCapturer: CameraCapturerCompat? = null
    private var screenCapturer: ScreenCapturer? = null
    private val screenCapturerListener: ScreenCapturer.Listener = object : ScreenCapturer.Listener {
        override fun onScreenCaptureError(errorDescription: String) {
            Timber.e("Screen capturer error: %s", errorDescription)
            stopScreenCapture()
            Snackbar.make(
                    primaryVideoView,
                    R.string.screen_capture_error,
                    Snackbar.LENGTH_LONG)
                    .show()
        }

        override fun onFirstFrameAvailable() {
            Timber.d("First frame from screen capturer available")
        }
    }
    private lateinit var statsScheduler: StatsScheduler
    private lateinit var statsListAdapter: StatsListAdapter
    private val localVideoTrackNames: MutableMap<String, String> = HashMap()

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
    private var isAudioMuted = false
    private var isVideoMuted = false
    private lateinit var participantAdapter: ParticipantAdapter
    private lateinit var roomViewModel: RoomViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = RoomViewModelFactory(roomManager, audioSwitch, PermissionUtil(this))
        roomViewModel = ViewModelProvider(this, factory).get(RoomViewModel::class.java)
        if (savedInstanceState != null) {
            isAudioMuted = savedInstanceState.getBoolean(IS_AUDIO_MUTED)
            isVideoMuted = savedInstanceState.getBoolean(IS_VIDEO_MUTED)
        }

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

        // Setup Activity
        statsScheduler = StatsScheduler()
        obtainVideoConstraints()
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
        roomViewModel.processInput(RefreshViewState)
        roomViewModel.processInput(CheckPermissions)
        updateStats()
    }

    override fun onResume() {
        super.onResume()
        displayName = sharedPreferences.getString(Preferences.DISPLAY_NAME, null)
        setTitle(displayName)
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
        outState.putBoolean(IS_AUDIO_MUTED, isAudioMuted)
        outState.putBoolean(IS_VIDEO_MUTED, isVideoMuted)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        // Teardown tracks
        localAudioTrack?.let {
            it.release()
            localAudioTrack = null
        }
        cameraVideoTrack?.let {
            it.release()
            cameraVideoTrack = null
        }
        screenVideoTrack?.let {
            it.release()
            screenVideoTrack = null
        }
        super.onDestroy()
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
                roomViewModel.processInput(CheckPermissions)
                setupLocalMedia()
            } else {
                Snackbar.make(primaryVideoView, R.string.permissions_required, Snackbar.LENGTH_LONG)
                        .show()
            }
        }
    }

    override fun onStop() {
        removeCameraTrack()
        super.onStop()
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
        requestPermissions()
        roomViewModel.viewState.observe(this, { roomViewState: RoomViewState -> bindRoomViewState(roomViewState) })
        roomViewModel.viewEffects.observe(this, { roomViewEffectWrapper: ViewEffect<RoomViewEffect> -> bindRoomViewEffects(roomViewEffectWrapper) })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.switch_camera_menu_item -> {
                cameraCapturer?.let {
                    it.switchCamera()
                }
                true
            }
            R.id.share_screen_menu_item -> {
                val shareScreen = getString(R.string.share_screen)
                if (item.title == shareScreen) {
                    if (screenCapturer == null) {
                        requestScreenCapturePermission()
                    } else {
                        startScreenCapture()
                    }
                } else {
                    stopScreenCapture()
                }
                true
            }
            R.id.device_menu_item -> {
                displayAudioDeviceList()
                true
            }
            R.id.pause_audio_menu_item -> {
                toggleLocalAudioTrackState()
                true
            }
            R.id.pause_video_menu_item -> {
                toggleLocalVideoTrackState()
                true
            }
            R.id.settings_menu_item -> {
                removeCameraTrack()
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
                screenCapturer = ScreenCapturer(this, resultCode, data, screenCapturerListener)
                startScreenCapture()
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
        if (!didAcceptPermissions()) {
            Snackbar.make(primaryVideoView, R.string.permissions_required, Snackbar.LENGTH_SHORT)
                    .show()
            return
        }
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
        stopScreenCapture()
    }

    @OnClick(R.id.local_audio_image_button)
    fun toggleLocalAudio() {
        val icon: Int
        if (localAudioTrack == null) {
            isAudioMuted = false
            LocalAudioTrack.create(this, true, MICROPHONE_TRACK_NAME)?.let { localAudioTrack ->
                this.localAudioTrack = localAudioTrack
                localParticipant?.publishTrack(localAudioTrack)
            }
            icon = R.drawable.ic_mic_white_24px
            pauseAudioMenuItem.isVisible = true
            pauseAudioMenuItem.setTitle(
                    if (localAudioTrack?.isEnabled == true) R.string.pause_audio else R.string.resume_audio)
        } else {
            isAudioMuted = true

            localAudioTrack?.let { localAudioTrack ->
                localParticipant?.unpublishTrack(localAudioTrack)
                localAudioTrack.release()
                this.localAudioTrack = null
            }
            icon = R.drawable.ic_mic_off_gray_24px
            pauseAudioMenuItem.isVisible = false
        }
        localAudioImageButton.setImageResource(icon)
    }

    @OnClick(R.id.local_video_image_button)
    fun toggleLocalVideo() {
        localParticipant?.let { roomViewModel.processInput(ToggleLocalVideo(it.sid)) }
        if (cameraVideoTrack == null) {
            isVideoMuted = false

            // add local camera track
            cameraCapturer?.let {
                cameraVideoTrack = LocalVideoTrack.create(
                        this,
                        true,
                        it.videoCapturer,
                        videoConstraints,
                        CAMERA_TRACK_NAME)
            }
            if (localParticipant != null) {
                cameraVideoTrack?.let { publishVideoTrack(it, TrackPriority.LOW) }

                // enable video settings
                val isCameraVideoTrackEnabled = cameraVideoTrack?.isEnabled == true
                switchCameraMenuItem.isVisible = isCameraVideoTrackEnabled
                pauseVideoMenuItem.setTitle(
                        if (isCameraVideoTrackEnabled) R.string.pause_video else R.string.resume_video)
                pauseVideoMenuItem.isVisible = true
            }
        } else {
            isVideoMuted = true
            // remove local camera track
            cameraVideoTrack?.let { cameraVideoTrack ->
                cameraVideoTrack.removeRenderer(primaryVideoView)
                localParticipant?.unpublishTrack(cameraVideoTrack)
                cameraVideoTrack.release()
                this.cameraVideoTrack = null
            }

            // disable video settings
            switchCameraMenuItem.isVisible = false
            pauseVideoMenuItem.isVisible = false
        }

        // update toggle button icon
        localVideoImageButton.setImageResource(
                if (cameraVideoTrack != null) R.drawable.ic_videocam_white_24px else R.drawable.ic_videocam_off_gray_24px)
        roomViewModel.processInput(RefreshViewState)
    }

    private fun publishVideoTrack(videoTrack: LocalVideoTrack, trackPriority: TrackPriority) {
        val localTrackPublicationOptions = LocalTrackPublicationOptions(trackPriority)
        localParticipant?.publishTrack(videoTrack, localTrackPublicationOptions)
    }

    private fun obtainVideoConstraints() {
        Timber.d("Collecting video constraints...")
        val builder = VideoConstraints.Builder()

        // setup aspect ratio
        val aspectRatio = sharedPreferences.getString(Preferences.ASPECT_RATIO, "0")
        if (aspectRatio != null) {
            val aspectRatioIndex = aspectRatio.toInt()
            builder.aspectRatio(aspectRatios[aspectRatioIndex])
            Timber.d(
                    "Aspect ratio : %s",
                    resources
                            .getStringArray(R.array.settings_screen_aspect_ratio_array)[aspectRatioIndex])
        }

        // setup video dimensions
        val minVideoDim = sharedPreferences.getInt(Preferences.MIN_VIDEO_DIMENSIONS, 0)
        val maxVideoDim = sharedPreferences.getInt(Preferences.MAX_VIDEO_DIMENSIONS, 1)
        if (maxVideoDim != -1 && minVideoDim != -1) {
            builder.minVideoDimensions(videoDimensions[minVideoDim])
            builder.maxVideoDimensions(videoDimensions[maxVideoDim])
        }
        Timber.d(
                "Video dimensions: %s - %s",
                resources
                        .getStringArray(R.array.settings_screen_video_dimensions_array)[minVideoDim],
                resources
                        .getStringArray(R.array.settings_screen_video_dimensions_array)[maxVideoDim])

        // setup fps
        val minFps = sharedPreferences.getInt(Preferences.MIN_FPS, 0)
        val maxFps = sharedPreferences.getInt(Preferences.MAX_FPS, 24)
        if (maxFps != -1 && minFps != -1) {
            builder.minFps(minFps)
            builder.maxFps(maxFps)
        }
        Timber.d("Frames per second: %d - %d", minFps, maxFps)
        videoConstraints = builder.build()
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!permissionsGranted()) {
                requestPermissions(arrayOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                        PERMISSIONS_REQUEST_CODE)
            } else {
                setupLocalMedia()
            }
        } else {
            setupLocalMedia()
        }
    }

    private fun permissionsGranted(): Boolean {
        val resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        val resultStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return (resultCamera == PackageManager.PERMISSION_GRANTED &&
                resultMic == PackageManager.PERMISSION_GRANTED &&
                resultStorage == PackageManager.PERMISSION_GRANTED)
    }

    /** Initialize local media and provide stub participant for primary view.  */
    private fun setupLocalMedia() {
        if (localAudioTrack == null && !isAudioMuted) {
            localAudioTrack = LocalAudioTrack.create(this, true, MICROPHONE_TRACK_NAME)
            if (room != null && localParticipant != null) {
                localAudioTrack?.let { localParticipant?.publishTrack(it) }
            }
        }
        if (!isVideoMuted) {
            setupLocalVideoTrack()
            if (room != null && localParticipant != null) {
                cameraVideoTrack?.let { publishVideoTrack(it, TrackPriority.LOW) }
            }
        }
        roomViewModel.processInput(RefreshViewState)
    }

    /** Create local video track  */
    private fun setupLocalVideoTrack() {

        // initialize capturer only once if needed
        if (cameraCapturer == null) {
            cameraCapturer = CameraCapturerCompat(this, CameraCapturer.CameraSource.FRONT_CAMERA)
        }
        cameraCapturer?.let {
            cameraVideoTrack = LocalVideoTrack.create(
                    this,
                    true,
                    it.videoCapturer,
                    videoConstraints,
                    CAMERA_TRACK_NAME)
        }
        cameraVideoTrack?.let {
            localVideoTrackNames[it.name] = getString(R.string.camera_video_track)
        } ?: run {
            Snackbar.make(
                    primaryVideoView,
                    R.string.failed_to_add_camera_video_track,
                    Snackbar.LENGTH_LONG)
                    .show()
        }
    }

    /**
     * Render local video track.
     *
     *
     * NOTE: Stub participant is created in controller. Make sure to remove it when connected to
     * room.
     */
    private fun renderLocalParticipantStub() {
        val cameraTrackViewState = cameraVideoTrack?.let { VideoTrackViewState(it, false) }
        cameraCapturer?.let { cameraCapturer ->
            primaryParticipantController.renderAsPrimary(
                    localParticipantSid,
                    getString(R.string.you),
                    null,
                    cameraTrackViewState,
                    localAudioTrack == null,
                    cameraCapturer.cameraSource == CameraCapturer.CameraSource.FRONT_CAMERA)
        }
        primaryVideoView.showIdentityBadge(false)
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
        if (roomViewState.isConnectingLayoutVisible) {
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
        if (roomViewState.isConnectedLayoutVisible) {
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
        if (roomViewState.isLobbyLayoutVisible) {
            connectButtonEnabled = isRoomTextNotEmpty
            screenCaptureMenuItemState = false
        }
        val isMicEnabled = roomViewState.isMicEnabled
        val isCameraEnabled = roomViewState.isCameraEnabled
        val isLocalMediaEnabled = isMicEnabled && isCameraEnabled
        localAudioImageButton.isEnabled = isLocalMediaEnabled
        localVideoImageButton.isEnabled = isLocalMediaEnabled
        val micDrawable = if (isAudioMuted || !isLocalMediaEnabled) R.drawable.ic_mic_off_gray_24px else R.drawable.ic_mic_white_24px
        val videoDrawable = if (isVideoMuted || !isLocalMediaEnabled) R.drawable.ic_videocam_off_gray_24px else R.drawable.ic_videocam_white_24px
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

        // TODO: Remove when we use a Service to obtainTokenAndConnect to a room
        settingsMenuItem.isVisible = settingsMenuItemState
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            screenCaptureMenuItem.isVisible = screenCaptureMenuItemState
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

    private fun startScreenCapture() {
        screenCapturer?.let { screenCapturer ->
            screenVideoTrack = LocalVideoTrack.create(this, true, screenCapturer,
                    SCREEN_TRACK_NAME)
            screenVideoTrack?.let { screenVideoTrack ->
                screenCaptureMenuItem.setIcon(R.drawable.ic_stop_screen_share_white_24dp)
                screenCaptureMenuItem.setTitle(R.string.stop_screen_share)
                localVideoTrackNames[screenVideoTrack.name] = getString(R.string.screen_video_track)
                if (localParticipant != null) {
                    publishVideoTrack(screenVideoTrack, TrackPriority.HIGH)
                }
            } ?: run {
                Snackbar.make(
                        primaryVideoView,
                        R.string.failed_to_add_screen_video_track,
                        Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show()
            }
        }
    }

    private fun stopScreenCapture() {
        screenVideoTrack?.let { screenVideoTrack ->
            localParticipant?.let { localParticipant ->
                roomViewModel.processInput(ScreenTrackRemoved(localParticipant.sid))
                localParticipant.unpublishTrack(screenVideoTrack)
            }
            screenVideoTrack.release()
            localVideoTrackNames.remove(screenVideoTrack.name)
            this.screenVideoTrack = null
            screenCaptureMenuItem.setIcon(R.drawable.ic_screen_share_white_24dp)
            screenCaptureMenuItem.setTitle(R.string.share_screen)
        }
    }

    private fun toggleLocalAudioTrackState() {
        localAudioTrack?.let { localAudioTrack ->
            val enable = !localAudioTrack.isEnabled
            localAudioTrack.enable(enable)
            pauseAudioMenuItem.setTitle(
                    if (localAudioTrack.isEnabled) R.string.pause_audio else R.string.resume_audio)
        }
    }

    private fun toggleLocalVideoTrackState() {
        cameraVideoTrack?.let { cameraVideoTrack ->
            val enable = !cameraVideoTrack.isEnabled
            cameraVideoTrack.enable(enable)
            pauseVideoMenuItem.setTitle(
                    if (cameraVideoTrack.isEnabled) R.string.pause_video else R.string.resume_video)
        }
    }

    /**
     * Remove the video track and mark the track to be restored when going to the settings screen or
     * going to the background
     */
    private fun removeCameraTrack() {
        cameraVideoTrack?.let { cameraVideoTrack ->
            localParticipant?.let { localParticipant ->
                roomViewModel.processInput(VideoTrackRemoved(localParticipant.sid))
                localParticipant.unpublishTrack(cameraVideoTrack)
            }
            cameraVideoTrack.release()
            restoreLocalVideoCameraTrack = true
            this.cameraVideoTrack = null
        }
    }

    /** Try to restore camera video track after going to the settings screen or background  */
    private fun restoreCameraTrack() {
        if (restoreLocalVideoCameraTrack) {
            obtainVideoConstraints()
            setupLocalVideoTrack()
            restoreLocalVideoCameraTrack = false
        }
    }

    private fun updateStatsUI(enabled: Boolean) {
        if (enabled) {
            if (room != null && room!!.remoteParticipants.size > 0) {
                // show stats
                statsRecyclerView.visibility = View.VISIBLE
                statsDisabledLayout.visibility = View.GONE
            } else if (room != null) {
                // disable stats when there is no room
                statsDisabledTitleTextView.text = getString(R.string.stats_unavailable)
                statsDisabledDescTextView.text = getString(R.string.stats_description_media_not_shared)
                statsRecyclerView.visibility = View.GONE
                statsDisabledLayout.visibility = View.VISIBLE
            } else {
                // disable stats if there is room but no participants (no media)
                statsDisabledTitleTextView.text = getString(R.string.stats_unavailable)
                statsDisabledDescTextView.text = getString(R.string.stats_description_join_room)
                statsRecyclerView.visibility = View.GONE
                statsDisabledLayout.visibility = View.VISIBLE
            }
        } else {
            statsDisabledTitleTextView.text = getString(R.string.stats_gathering_disabled)
            statsDisabledDescTextView.text = getString(R.string.stats_enable_in_settings)
            statsRecyclerView.visibility = View.GONE
            statsDisabledLayout.visibility = View.VISIBLE
        }
    }

    private fun updateStats() {
        if (statsScheduler.isRunning) {
            statsScheduler.cancelStatsGathering()
        }
        val enableStats = sharedPreferences.getBoolean(
                Preferences.ENABLE_STATS, Preferences.ENABLE_STATS_DEFAULT)
        if (enableStats && room != null && room!!.state == Room.State.CONNECTED) {
            statsScheduler.scheduleStatsGathering(room!!, statsListener(), STATS_DELAY.toLong())
        }
        updateStatsUI(enableStats)
    }

    private fun statsListener(): StatsListener {
        return StatsListener { statsReports: List<StatsReport> ->
            // Running on StatsScheduler thread
            room?.let { room ->
                statsListAdapter.updateStatsData(statsReports, room.remoteParticipants,
                        localVideoTrackNames)
            }
        }
    }

    private fun initializeRoom() {
        room?.let {
            setupLocalParticipant(it)
            publishLocalTracks()
            updateStats()
        }
    }

    private fun setupLocalParticipant(room: Room) {
        localParticipant = room.localParticipant
        localParticipant?.let {
            localParticipantSid = it.sid
        }
    }

    private fun publishLocalTracks() {
        if (localParticipant != null) {
            cameraVideoTrack?.let { cameraVideoTrack ->
                Timber.d("Camera track: %s", cameraVideoTrack)
                publishVideoTrack(cameraVideoTrack, TrackPriority.LOW)
            }
            localAudioTrack?.let { localParticipant?.publishTrack(it) }
        }
    }

    private fun toggleAudioDevice(enableAudioDevice: Boolean) {
        setVolumeControl(enableAudioDevice)
        val viewEvent = if (enableAudioDevice) ActivateAudioDevice else RoomViewEvent.DeactivateAudioDevice
        roomViewModel.processInput(viewEvent)
    }

    private fun bindRoomViewState(roomViewState: RoomViewState) {
        Timber.d("RoomViewState: %s", roomViewState)
        deviceMenuItem.isVisible = roomViewState.availableAudioDevices?.isNotEmpty() ?: false
        renderPrimaryView(roomViewState.primaryParticipant)
        renderThumbnails(roomViewState)
        updateLayout(roomViewState)
        updateAudioDeviceIcon(roomViewState.selectedDevice)
    }

    private fun bindRoomViewEffects(roomViewEffectWrapper: ViewEffect<RoomViewEffect>) {
        val roomViewEffect = roomViewEffectWrapper.getContentIfNotHandled()
        if (roomViewEffect != null) {
            Timber.d("RoomViewEffect: %s", roomViewEffect)
            requestPermissions()
            if (roomViewEffect is RoomViewEffect.Connected) {
                room = roomViewEffect.room
                toggleAudioDevice(true)
                initializeRoom()
            }
            if (roomViewEffect is RoomViewEffect.Disconnected) {
                localParticipant = null
                room = null
                localParticipantSid = LOCAL_PARTICIPANT_STUB_SID
                updateStats()
                toggleAudioDevice(false)
            }
            if (roomViewEffect is ShowConnectFailureDialog) {
                AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                        .setTitle(getString(R.string.room_screen_connection_failure_title))
                        .setMessage(getString(R.string.room_screen_connection_failure_message))
                        .setNeutralButton("OK", null)
                        .show()
                toggleAudioDevice(false)
            }
            if (roomViewEffect is ShowTokenErrorDialog) {
                val error = roomViewEffect.serviceError
                handleTokenError(error)
            }
        }
    }

    private fun updateAudioDeviceIcon(selectedAudioDevice: AudioDevice?) {
        val audioDeviceMenuIcon = when (selectedAudioDevice) {
            is BluetoothHeadset -> R.drawable.ic_bluetooth_white_24dp
            is WiredHeadset -> R.drawable.ic_headset_mic_white_24dp
            is Speakerphone -> R.drawable.ic_volume_up_white_24dp
            else -> R.drawable.ic_phonelink_ring_white_24dp
        }
        this.deviceMenuItem.setIcon(audioDeviceMenuIcon)
    }

    private fun renderPrimaryView(primaryParticipant: ParticipantViewState?) {
        if (primaryParticipant != null) {
            primaryParticipantController.renderAsPrimary(
                    primaryParticipant.sid,
                    primaryParticipant.identity,
                    primaryParticipant.screenTrack,
                    primaryParticipant.videoTrack,
                    primaryParticipant.isMuted,
                    primaryParticipant.isMirrored)
        } else {
            renderLocalParticipantStub()
        }
    }

    private fun renderThumbnails(roomViewState: RoomViewState) {
        participantAdapter.submitList(roomViewState.participantThumbnails)
    }

    private fun displayAudioDeviceList() {
        roomViewModel.viewState.value?.let { viewState ->
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
                .setNeutralButton("OK", null)
                .show()
    }

    private fun didAcceptPermissions(): Boolean {
        return (PermissionChecker.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PermissionChecker.PERMISSION_GRANTED) && (PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PermissionChecker.PERMISSION_GRANTED) && (PermissionChecker.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PermissionChecker.PERMISSION_GRANTED)
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
        private const val MEDIA_PROJECTION_REQUEST_CODE = 101
        private const val STATS_DELAY = 1000 // milliseconds
        private const val IS_AUDIO_MUTED = "IS_AUDIO_MUTED"
        private const val IS_VIDEO_MUTED = "IS_VIDEO_MUTED"

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
