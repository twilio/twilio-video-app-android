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

package com.twilio.video.app.ui.room;

import static com.twilio.video.AspectRatio.ASPECT_RATIO_11_9;
import static com.twilio.video.AspectRatio.ASPECT_RATIO_16_9;
import static com.twilio.video.AspectRatio.ASPECT_RATIO_4_3;
import static com.twilio.video.Room.State.CONNECTED;
import static com.twilio.video.app.data.api.AuthServiceError.EXPIRED_PASSCODE_ERROR;
import static com.twilio.video.app.participant.ParticipantViewStateKt.buildLocalParticipantViewState;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import com.google.android.material.snackbar.Snackbar;
import com.twilio.audioswitch.selection.AudioDevice;
import com.twilio.audioswitch.selection.AudioDeviceSelector;
import com.twilio.video.AspectRatio;
import com.twilio.video.CameraCapturer;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.NetworkQualityLevel;
import com.twilio.video.Room;
import com.twilio.video.ScreenCapturer;
import com.twilio.video.StatsListener;
import com.twilio.video.VideoConstraints;
import com.twilio.video.VideoDimensions;
import com.twilio.video.VideoTrack;
import com.twilio.video.app.R;
import com.twilio.video.app.adapter.StatsListAdapter;
import com.twilio.video.app.base.BaseActivity;
import com.twilio.video.app.data.Preferences;
import com.twilio.video.app.data.api.AuthServiceError;
import com.twilio.video.app.data.api.TokenService;
import com.twilio.video.app.data.api.VideoAppService;
import com.twilio.video.app.participant.ParticipantViewState;
import com.twilio.video.app.udf.ViewEffect;
import com.twilio.video.app.ui.room.ParticipantController.Item;
import com.twilio.video.app.ui.room.RoomViewEffect.Connected;
import com.twilio.video.app.ui.room.RoomViewEffect.Disconnected;
import com.twilio.video.app.ui.room.RoomViewEffect.ShowConnectFailureDialog;
import com.twilio.video.app.ui.room.RoomViewEffect.ShowTokenErrorDialog;
import com.twilio.video.app.ui.room.RoomViewEvent.ActivateAudioDevice;
import com.twilio.video.app.ui.room.RoomViewEvent.Disconnect;
import com.twilio.video.app.ui.room.RoomViewEvent.LocalVideoTrackPublished;
import com.twilio.video.app.ui.room.RoomViewEvent.PinParticipant;
import com.twilio.video.app.ui.room.RoomViewEvent.SelectAudioDevice;
import com.twilio.video.app.ui.room.RoomViewModel.RoomViewModelFactory;
import com.twilio.video.app.ui.settings.SettingsActivity;
import com.twilio.video.app.util.CameraCapturerCompat;
import com.twilio.video.app.util.InputUtils;
import com.twilio.video.app.util.StatsScheduler;
import io.reactivex.disposables.CompositeDisposable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

public class RoomActivity extends BaseActivity {
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int MEDIA_PROJECTION_REQUEST_CODE = 101;
    private static final int STATS_DELAY = 1000; // milliseconds
    private static final String MICROPHONE_TRACK_NAME = "microphone";
    private static final String CAMERA_TRACK_NAME = "camera";
    static final String SCREEN_TRACK_NAME = "screen";
    private static final String IS_AUDIO_MUTED = "IS_AUDIO_MUTED";
    private static final String IS_VIDEO_MUTED = "IS_VIDEO_MUTED";

    // This will be used instead of real local participant sid,
    // because that information is unknown until room connection is fully established
    private static final String LOCAL_PARTICIPANT_STUB_SID = "";

    private AspectRatio[] aspectRatios =
            new AspectRatio[] {ASPECT_RATIO_4_3, ASPECT_RATIO_16_9, ASPECT_RATIO_11_9};

    private VideoDimensions[] videoDimensions =
            new VideoDimensions[] {
                VideoDimensions.CIF_VIDEO_DIMENSIONS,
                VideoDimensions.VGA_VIDEO_DIMENSIONS,
                VideoDimensions.WVGA_VIDEO_DIMENSIONS,
                VideoDimensions.HD_540P_VIDEO_DIMENSIONS,
                VideoDimensions.HD_720P_VIDEO_DIMENSIONS,
                VideoDimensions.HD_960P_VIDEO_DIMENSIONS,
                VideoDimensions.HD_S1080P_VIDEO_DIMENSIONS,
                VideoDimensions.HD_1080P_VIDEO_DIMENSIONS
            };

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.connect)
    Button connect;

    @BindView(R.id.disconnect)
    ImageButton disconnectButton;

    @BindView(R.id.primary_video)
    ParticipantPrimaryView primaryVideoView;

    @BindView(R.id.remote_video_thumbnails)
    LinearLayout thumbnailLinearLayout;

    @BindView(R.id.local_video_image_button)
    ImageButton localVideoImageButton;

    @BindView(R.id.local_audio_image_button)
    ImageButton localAudioImageButton;

    @BindView(R.id.video_container)
    FrameLayout frameLayout;

    @BindView(R.id.join_room_layout)
    LinearLayout joinRoomLayout;

    @BindView(R.id.room_edit_text)
    ClearableEditText roomEditText;

    @BindView(R.id.join_status_layout)
    LinearLayout joinStatusLayout;

    @BindView(R.id.join_status)
    TextView joinStatusTextView;

    @BindView(R.id.join_room_name)
    TextView joinRoomNameTextView;

    @BindView(R.id.recording_notice)
    TextView recordingNoticeTextView;

    @BindView(R.id.stats_recycler_view)
    RecyclerView statsRecyclerView;

    @BindView(R.id.stats_disabled)
    LinearLayout statsDisabledLayout;

    @BindView(R.id.stats_disabled_title)
    TextView statsDisabledTitleTextView;

    @BindView(R.id.stats_disabled_description)
    TextView statsDisabledDescTextView;

    private MenuItem switchCameraMenuItem;
    private MenuItem pauseVideoMenuItem;
    private MenuItem pauseAudioMenuItem;
    private MenuItem screenCaptureMenuItem;
    private MenuItem settingsMenuItem;
    private MenuItem deviceMenuItem;

    private int savedAudioMode = AudioManager.MODE_INVALID;
    private int savedVolumeControlStream;
    private boolean savedIsMicrophoneMute = false;
    private boolean savedIsSpeakerPhoneOn = false;

    private String displayName;
    private LocalParticipant localParticipant;
    private String localParticipantSid = LOCAL_PARTICIPANT_STUB_SID;
    private Room room;
    private VideoConstraints videoConstraints;
    private LocalAudioTrack localAudioTrack;
    private LocalVideoTrack cameraVideoTrack;
    private boolean restoreLocalVideoCameraTrack = false;
    private LocalVideoTrack screenVideoTrack;
    private CameraCapturerCompat cameraCapturer;
    private ScreenCapturer screenCapturer;
    private final ScreenCapturer.Listener screenCapturerListener =
            new ScreenCapturer.Listener() {
                @Override
                public void onScreenCaptureError(@NonNull String errorDescription) {
                    Timber.e("Screen capturer error: %s", errorDescription);
                    stopScreenCapture();
                    Snackbar.make(
                                    primaryVideoView,
                                    R.string.screen_capture_error,
                                    Snackbar.LENGTH_LONG)
                            .show();
                }

                @Override
                public void onFirstFrameAvailable() {
                    Timber.d("First frame from screen capturer available");
                }
            };

    private StatsScheduler statsScheduler;
    private StatsListAdapter statsListAdapter;
    private Map<String, String> localVideoTrackNames = new HashMap<>();
    // TODO This should be decoupled from this Activity as part of
    // https://issues.corp.twilio.com/browse/AHOYAPPS-473
    private Map<String, NetworkQualityLevel> networkQualityLevels = new HashMap<>();

    @Inject TokenService tokenService;

    @Inject SharedPreferences sharedPreferences;

    @Inject RoomManager roomManager;

    @Inject AudioDeviceSelector audioDeviceSelector;

    /** Coordinates participant thumbs and primary participant rendering. */
    private ParticipantController participantController;

    /** Disposes {@link VideoAppService} requests when activity is destroyed. */
    private final CompositeDisposable rxDisposables = new CompositeDisposable();

    private Boolean isAudioMuted = false;
    private Boolean isVideoMuted = false;

    public static void startActivity(Context context, Uri appLink) {
        Intent intent = new Intent(context, RoomActivity.class);
        intent.setData(appLink);

        context.startActivity(intent);
    }

    private RoomViewModel roomViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RoomViewModelFactory factory = new RoomViewModelFactory(roomManager, audioDeviceSelector);
        roomViewModel = new ViewModelProvider(this, factory).get(RoomViewModel.class);

        if (savedInstanceState != null) {
            isAudioMuted = savedInstanceState.getBoolean(IS_AUDIO_MUTED);
            isVideoMuted = savedInstanceState.getBoolean(IS_VIDEO_MUTED);
        }

        // So calls can be answered when screen is locked
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        // Grab views
        setContentView(R.layout.activity_room);
        ButterKnife.bind(this);

        // Setup toolbar
        setSupportActionBar(toolbar);

        // Cache volume control stream
        savedVolumeControlStream = getVolumeControlStream();

        // setup participant controller
        participantController = new ParticipantController(thumbnailLinearLayout, primaryVideoView);
        participantController.setListener(participantClickListener());

        // Setup Activity
        statsScheduler = new StatsScheduler();
        obtainVideoConstraints();
    }

    @Override
    protected void onStart() {
        super.onStart();

        checkIntentURI();

        restoreCameraTrack();

        publishLocalTracks();

        updateStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayName = sharedPreferences.getString(Preferences.DISPLAY_NAME, null);
        setTitle(displayName);
    }

    private boolean checkIntentURI() {
        boolean isAppLinkProvided = false;
        Uri uri = getIntent().getData();
        String roomName = new UriRoomParser(new UriWrapper(uri)).parseRoom();
        if (roomName != null) {
            roomEditText.setText(roomName);
            isAppLinkProvided = true;
        }
        return isAppLinkProvided;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(IS_AUDIO_MUTED, isAudioMuted);
        outState.putBoolean(IS_VIDEO_MUTED, isVideoMuted);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        // Teardown tracks
        if (localAudioTrack != null) {
            localAudioTrack.release();
            localAudioTrack = null;
        }
        if (cameraVideoTrack != null) {
            cameraVideoTrack.release();
            cameraVideoTrack = null;
        }
        if (screenVideoTrack != null) {
            screenVideoTrack.release();
            screenVideoTrack = null;
        }

        // dispose any token requests if needed
        rxDisposables.clear();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean recordAudioPermissionGranted =
                    grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean cameraPermissionGranted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
            boolean writeExternalStoragePermissionGranted =
                    grantResults[2] == PackageManager.PERMISSION_GRANTED;
            boolean permissionsGranted =
                    recordAudioPermissionGranted
                            && cameraPermissionGranted
                            && writeExternalStoragePermissionGranted;

            if (permissionsGranted) {
                setupLocalMedia();
            } else {
                Snackbar.make(primaryVideoView, R.string.permissions_required, Snackbar.LENGTH_LONG)
                        .show();
            }
        }
    }

    @Override
    protected void onStop() {
        removeCameraTrack();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.room_menu, menu);
        settingsMenuItem = menu.findItem(R.id.settings_menu_item);
        // Grab menu items for updating later
        switchCameraMenuItem = menu.findItem(R.id.switch_camera_menu_item);
        pauseVideoMenuItem = menu.findItem(R.id.pause_video_menu_item);
        pauseAudioMenuItem = menu.findItem(R.id.pause_audio_menu_item);
        screenCaptureMenuItem = menu.findItem(R.id.share_screen_menu_item);
        deviceMenuItem = menu.findItem(R.id.device_menu_item);

        requestPermissions();
        roomViewModel.getRoomEvents().observe(this, (Observer) o -> {});
        roomViewModel.getViewState().observe(this, this::bindRoomViewState);
        roomViewModel.getViewEffects().observe(this, this::bindRoomViewEffects);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_camera_menu_item:
                switchCamera();
                return true;
            case R.id.share_screen_menu_item:
                String shareScreen = getString(R.string.share_screen);

                if (item.getTitle().equals(shareScreen)) {
                    if (screenCapturer == null) {
                        requestScreenCapturePermission();
                    } else {
                        startScreenCapture();
                    }
                } else {
                    stopScreenCapture();
                }
                return true;
            case R.id.device_menu_item:
                displayAudioDeviceList();
                return true;
            case R.id.pause_audio_menu_item:
                toggleLocalAudioTrackState();
                return true;
            case R.id.pause_video_menu_item:
                toggleLocalVideoTrackState();
                return true;
            case R.id.settings_menu_item:
                removeCameraTrack();

                Intent intent = new Intent(RoomActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MEDIA_PROJECTION_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                Snackbar.make(
                                primaryVideoView,
                                R.string.screen_capture_permission_not_granted,
                                Snackbar.LENGTH_LONG)
                        .show();
                return;
            }
            screenCapturer = new ScreenCapturer(this, resultCode, data, screenCapturerListener);
            startScreenCapture();
        }
    }

    @OnTextChanged(
        value = R.id.room_edit_text,
        callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED
    )
    void onTextChanged(CharSequence text) {
        connect.setEnabled(!TextUtils.isEmpty(text));
    }

    @OnClick(R.id.connect)
    void connectButtonClick() {
        InputUtils.hideKeyboard(this);
        if (!didAcceptPermissions()) {
            Snackbar.make(primaryVideoView, R.string.permissions_required, Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }
        connect.setEnabled(false);
        // obtain room name

        Editable text = roomEditText.getText();
        if (text != null) {
            final String roomName = text.toString();

            RoomViewEvent.Connect viewEvent =
                    new RoomViewEvent.Connect(displayName, roomName, isNetworkQualityEnabled());
            roomViewModel.processInput(viewEvent);
        }
    }

    @OnClick(R.id.disconnect)
    void disconnectButtonClick() {
        roomViewModel.processInput(Disconnect.INSTANCE);
        stopScreenCapture();
    }

    @OnClick(R.id.local_audio_image_button)
    void toggleLocalAudio() {
        int icon;
        if (localAudioTrack == null) {
            isAudioMuted = false;
            localAudioTrack = LocalAudioTrack.create(this, true, MICROPHONE_TRACK_NAME);
            if (localParticipant != null && localAudioTrack != null) {
                localParticipant.publishTrack(localAudioTrack);
            }
            icon = R.drawable.ic_mic_white_24px;
            pauseAudioMenuItem.setVisible(true);
            pauseAudioMenuItem.setTitle(
                    localAudioTrack.isEnabled() ? R.string.pause_audio : R.string.resume_audio);
        } else {
            isAudioMuted = true;
            if (localParticipant != null) {
                localParticipant.unpublishTrack(localAudioTrack);
            }
            localAudioTrack.release();
            localAudioTrack = null;
            icon = R.drawable.ic_mic_off_gray_24px;
            pauseAudioMenuItem.setVisible(false);
        }
        localAudioImageButton.setImageResource(icon);
    }

    @OnClick(R.id.local_video_image_button)
    void toggleLocalVideo() {

        // remember old video reference for updating thumb in room
        VideoTrack oldVideo = cameraVideoTrack;

        if (cameraVideoTrack == null) {
            isVideoMuted = false;

            // add local camera track
            cameraVideoTrack =
                    LocalVideoTrack.create(
                            this,
                            true,
                            cameraCapturer.getVideoCapturer(),
                            videoConstraints,
                            CAMERA_TRACK_NAME);
            if (localParticipant != null && cameraVideoTrack != null) {
                localParticipant.publishTrack(cameraVideoTrack);

                // enable video settings
                switchCameraMenuItem.setVisible(cameraVideoTrack.isEnabled());
                pauseVideoMenuItem.setTitle(
                        cameraVideoTrack.isEnabled()
                                ? R.string.pause_video
                                : R.string.resume_video);
                pauseVideoMenuItem.setVisible(true);
            }
        } else {
            isVideoMuted = true;
            // remove local camera track
            cameraVideoTrack.removeRenderer(primaryVideoView);

            if (localParticipant != null) {
                localParticipant.unpublishTrack(cameraVideoTrack);
            }
            cameraVideoTrack.release();
            cameraVideoTrack = null;

            // disable video settings
            switchCameraMenuItem.setVisible(false);
            pauseVideoMenuItem.setVisible(false);
        }

        if (room != null && room.getState() == CONNECTED) {

            // update local participant thumb
            participantController.updateThumb(
                    buildLocalParticipantViewState(
                            localParticipant, getString(R.string.you), cameraVideoTrack));

            if (participantController.getPrimaryItem().sid.equals(localParticipantSid)) {

                // local video was rendered as primary view - refreshing
                participantController.renderAsPrimary(
                        localParticipantSid,
                        getString(R.string.you),
                        cameraVideoTrack,
                        localAudioTrack == null,
                        cameraCapturer.getCameraSource()
                                == CameraCapturer.CameraSource.FRONT_CAMERA);

                participantController.getPrimaryView().showIdentityBadge(false);

                // update thumb state
                participantController.updateThumb(
                        localParticipantSid, ParticipantView.State.SELECTED);
            }

        } else {

            renderLocalParticipantStub();
        }

        // update toggle button icon
        localVideoImageButton.setImageResource(
                cameraVideoTrack != null
                        ? R.drawable.ic_videocam_white_24px
                        : R.drawable.ic_videocam_off_gray_24px);
    }

    private boolean isNetworkQualityEnabled() {
        return sharedPreferences.getBoolean(
                Preferences.ENABLE_NETWORK_QUALITY_LEVEL,
                Preferences.ENABLE_NETWORK_QUALITY_LEVEL_DEFAULT);
    }

    private void obtainVideoConstraints() {
        Timber.d("Collecting video constraints...");

        VideoConstraints.Builder builder = new VideoConstraints.Builder();

        // setup aspect ratio
        String aspectRatio = sharedPreferences.getString(Preferences.ASPECT_RATIO, "0");
        if (aspectRatio != null) {
            int aspectRatioIndex = Integer.parseInt(aspectRatio);
            builder.aspectRatio(aspectRatios[aspectRatioIndex]);
            Timber.d(
                    "Aspect ratio : %s",
                    getResources()
                            .getStringArray(R.array.settings_screen_aspect_ratio_array)[
                            aspectRatioIndex]);
        }

        // setup video dimensions
        int minVideoDim = sharedPreferences.getInt(Preferences.MIN_VIDEO_DIMENSIONS, 0);
        int maxVideoDim =
                sharedPreferences.getInt(
                        Preferences.MAX_VIDEO_DIMENSIONS, videoDimensions.length - 1);

        if (maxVideoDim != -1 && minVideoDim != -1) {
            builder.minVideoDimensions(videoDimensions[minVideoDim]);
            builder.maxVideoDimensions(videoDimensions[maxVideoDim]);
        }

        Timber.d(
                "Video dimensions: %s - %s",
                getResources()
                        .getStringArray(R.array.settings_screen_video_dimensions_array)[
                        minVideoDim],
                getResources()
                        .getStringArray(R.array.settings_screen_video_dimensions_array)[
                        maxVideoDim]);

        // setup fps
        int minFps = sharedPreferences.getInt(Preferences.MIN_FPS, 0);
        int maxFps = sharedPreferences.getInt(Preferences.MAX_FPS, 30);

        if (maxFps != -1 && minFps != -1) {
            builder.minFps(minFps);
            builder.maxFps(maxFps);
        }

        Timber.d("Frames per second: %d - %d", minFps, maxFps);

        videoConstraints = builder.build();
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!permissionsGranted()) {
                requestPermissions(
                        new String[] {
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        PERMISSIONS_REQUEST_CODE);
            } else {
                setupLocalMedia();
            }
        } else {
            setupLocalMedia();
        }
    }

    private boolean permissionsGranted() {
        int resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int resultStorage =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return ((resultCamera == PackageManager.PERMISSION_GRANTED)
                && (resultMic == PackageManager.PERMISSION_GRANTED)
                && (resultStorage == PackageManager.PERMISSION_GRANTED));
    }

    /** Initialize local media and provide stub participant for primary view. */
    private void setupLocalMedia() {
        if (localAudioTrack == null && !isAudioMuted) {
            localAudioTrack = LocalAudioTrack.create(this, true, MICROPHONE_TRACK_NAME);
            if (room != null && localParticipant != null)
                localParticipant.publishTrack(localAudioTrack);
        }
        if (cameraVideoTrack == null && !isVideoMuted) {
            setupLocalVideoTrack();
            renderLocalParticipantStub();
            if (room != null && localParticipant != null)
                localParticipant.publishTrack(cameraVideoTrack);
        }
    }

    /** Create local video track */
    private void setupLocalVideoTrack() {

        // initialize capturer only once if needed
        if (cameraCapturer == null) {
            cameraCapturer =
                    new CameraCapturerCompat(this, CameraCapturer.CameraSource.FRONT_CAMERA);
        }

        cameraVideoTrack =
                LocalVideoTrack.create(
                        this,
                        true,
                        cameraCapturer.getVideoCapturer(),
                        videoConstraints,
                        CAMERA_TRACK_NAME);
        if (cameraVideoTrack != null) {
            localVideoTrackNames.put(
                    cameraVideoTrack.getName(), getString(R.string.camera_video_track));
        } else {
            Snackbar.make(
                            primaryVideoView,
                            R.string.failed_to_add_camera_video_track,
                            Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * Render local video track.
     *
     * <p>NOTE: Stub participant is created in controller. Make sure to remove it when connected to
     * room.
     */
    private void renderLocalParticipantStub() {
        participantController.renderAsPrimary(
                localParticipantSid,
                getString(R.string.you),
                cameraVideoTrack,
                localAudioTrack == null,
                cameraCapturer.getCameraSource() == CameraCapturer.CameraSource.FRONT_CAMERA);

        primaryVideoView.showIdentityBadge(false);
    }

    private void updateLayout(RoomViewState roomViewState) {
        int disconnectButtonState = View.GONE;
        int joinRoomLayoutState = View.VISIBLE;
        int joinStatusLayoutState = View.GONE;

        boolean settingsMenuItemState = true;
        boolean screenCaptureMenuItemState = false;

        Editable roomEditable = roomEditText.getText();
        boolean connectButtonEnabled = roomEditable != null && !roomEditable.toString().isEmpty();

        String roomName = displayName;
        String toolbarTitle = displayName;
        String joinStatus = "";
        int recordingWarningVisibility = View.GONE;

        if (roomViewState.isConnectingLayoutVisible()) {
            disconnectButtonState = View.VISIBLE;
            joinRoomLayoutState = View.GONE;
            joinStatusLayoutState = View.VISIBLE;
            recordingWarningVisibility = View.VISIBLE;
            settingsMenuItemState = false;

            connectButtonEnabled = false;

            if (roomEditable != null) {
                roomName = roomEditable.toString();
            }
            joinStatus = "Joining...";
        }
        if (roomViewState.isConnectedLayoutVisible()) {
            disconnectButtonState = View.VISIBLE;
            joinRoomLayoutState = View.GONE;
            joinStatusLayoutState = View.GONE;
            settingsMenuItemState = false;
            screenCaptureMenuItemState = true;

            connectButtonEnabled = false;

            roomName = roomViewState.getTitle();
            toolbarTitle = roomName;
            joinStatus = "";
        }
        if (roomViewState.isLobbyLayoutVisible()) {
            connectButtonEnabled = true;
            screenCaptureMenuItemState = false;
        }

        // Check mute state
        if (isAudioMuted) {
            localAudioImageButton.setImageResource(R.drawable.ic_mic_off_gray_24px);
        }
        if (isVideoMuted) {
            localVideoImageButton.setImageResource(R.drawable.ic_videocam_off_gray_24px);
        }

        statsListAdapter = new StatsListAdapter(this);
        statsRecyclerView.setAdapter(statsListAdapter);
        statsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        disconnectButton.setVisibility(disconnectButtonState);
        joinRoomLayout.setVisibility(joinRoomLayoutState);
        joinStatusLayout.setVisibility(joinStatusLayoutState);
        connect.setEnabled(connectButtonEnabled);

        setTitle(toolbarTitle);

        joinStatusTextView.setText(joinStatus);
        joinRoomNameTextView.setText(roomName);
        recordingNoticeTextView.setVisibility(recordingWarningVisibility);

        // TODO: Remove when we use a Service to obtainTokenAndConnect to a room
        if (settingsMenuItem != null) {
            settingsMenuItem.setVisible(settingsMenuItemState);
        }

        if (screenCaptureMenuItem != null
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            screenCaptureMenuItem.setVisible(screenCaptureMenuItemState);
        }
    }

    private void setTitle(String toolbarTitle) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(toolbarTitle);
        }
    }

    private void switchCamera() {
        if (cameraCapturer != null) {

            boolean mirror =
                    cameraCapturer.getCameraSource() == CameraCapturer.CameraSource.BACK_CAMERA;

            cameraCapturer.switchCamera();

            if (participantController.getPrimaryItem().sid.equals(localParticipantSid)) {
                participantController.updatePrimaryThumb(mirror);
            } else {
                participantController.updateThumb(
                        buildLocalParticipantViewState(localParticipant, "", null));
            }
        }
    }

    private void setVolumeControl(boolean setVolumeControl) {
        if (setVolumeControl) {
            /*
             * Enable changing the volume using the up/down keys during a conversation
             */
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        } else {
            setVolumeControlStream(savedVolumeControlStream);
        }
    }

    @TargetApi(21)
    private void requestScreenCapturePermission() {
        Timber.d("Requesting permission to capture screen");
        MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        // This initiates a prompt dialog for the user to confirm screen projection.
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(), MEDIA_PROJECTION_REQUEST_CODE);
    }

    private void startScreenCapture() {
        screenVideoTrack = LocalVideoTrack.create(this, true, screenCapturer, SCREEN_TRACK_NAME);

        if (screenVideoTrack != null) {
            screenCaptureMenuItem.setIcon(R.drawable.ic_stop_screen_share_white_24dp);
            screenCaptureMenuItem.setTitle(R.string.stop_screen_share);
            localVideoTrackNames.put(
                    screenVideoTrack.getName(), getString(R.string.screen_video_track));

            if (localParticipant != null) {
                localParticipant.publishTrack(screenVideoTrack);
            }
        } else {
            Snackbar.make(
                            primaryVideoView,
                            R.string.failed_to_add_screen_video_track,
                            Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show();
        }
    }

    private void stopScreenCapture() {
        if (screenVideoTrack != null) {
            if (localParticipant != null) {
                localParticipant.unpublishTrack(screenVideoTrack);
            }
            screenVideoTrack.release();
            localVideoTrackNames.remove(screenVideoTrack.getName());
            screenVideoTrack = null;
            screenCaptureMenuItem.setIcon(R.drawable.ic_screen_share_white_24dp);
            screenCaptureMenuItem.setTitle(R.string.share_screen);
        }
    }

    private void toggleLocalAudioTrackState() {
        if (localAudioTrack != null) {
            boolean enable = !localAudioTrack.isEnabled();
            localAudioTrack.enable(enable);
            pauseAudioMenuItem.setTitle(
                    localAudioTrack.isEnabled() ? R.string.pause_audio : R.string.resume_audio);
        }
    }

    private void toggleLocalVideoTrackState() {
        if (cameraVideoTrack != null) {
            boolean enable = !cameraVideoTrack.isEnabled();
            cameraVideoTrack.enable(enable);
            pauseVideoMenuItem.setTitle(
                    cameraVideoTrack.isEnabled() ? R.string.pause_video : R.string.resume_video);
        }
    }

    /**
     * Remove the video track and mark the track to be restored when going to the settings screen or
     * going to the background
     */
    private void removeCameraTrack() {
        if (cameraVideoTrack != null) {
            if (localParticipant != null) {
                localParticipant.unpublishTrack(cameraVideoTrack);
            }
            cameraVideoTrack.release();
            restoreLocalVideoCameraTrack = true;
            cameraVideoTrack = null;
        }
    }

    /** Try to restore camera video track after going to the settings screen or background */
    private void restoreCameraTrack() {
        if (restoreLocalVideoCameraTrack) {
            obtainVideoConstraints();
            setupLocalVideoTrack();
            renderLocalParticipantStub();
            restoreLocalVideoCameraTrack = false;
        }
    }

    private void updateStatsUI(boolean enabled) {
        if (enabled) {
            if (room != null && room.getRemoteParticipants().size() > 0) {
                // show stats
                statsRecyclerView.setVisibility(View.VISIBLE);
                statsDisabledLayout.setVisibility(View.GONE);
            } else if (room != null) {
                // disable stats when there is no room
                statsDisabledTitleTextView.setText(getString(R.string.stats_unavailable));
                statsDisabledDescTextView.setText(
                        getString(R.string.stats_description_media_not_shared));
                statsRecyclerView.setVisibility(View.GONE);
                statsDisabledLayout.setVisibility(View.VISIBLE);
            } else {
                // disable stats if there is room but no participants (no media)
                statsDisabledTitleTextView.setText(getString(R.string.stats_unavailable));
                statsDisabledDescTextView.setText(getString(R.string.stats_description_join_room));
                statsRecyclerView.setVisibility(View.GONE);
                statsDisabledLayout.setVisibility(View.VISIBLE);
            }
        } else {
            statsDisabledTitleTextView.setText(getString(R.string.stats_gathering_disabled));
            statsDisabledDescTextView.setText(getString(R.string.stats_enable_in_settings));
            statsRecyclerView.setVisibility(View.GONE);
            statsDisabledLayout.setVisibility(View.VISIBLE);
        }
    }

    private void updateStats() {
        if (statsScheduler.isRunning()) {
            statsScheduler.cancelStatsGathering();
        }
        boolean enableStats = sharedPreferences.getBoolean(Preferences.ENABLE_STATS, false);
        if (enableStats && (room != null) && (room.getState() == CONNECTED)) {
            statsScheduler.scheduleStatsGathering(room, statsListener(), STATS_DELAY);
        }
        updateStatsUI(enableStats);
    }

    private StatsListener statsListener() {
        return statsReports -> {
            // Running on StatsScheduler thread
            if (room != null) {
                statsListAdapter.updateStatsData(
                        statsReports, room.getRemoteParticipants(), localVideoTrackNames);
            }
        };
    }

    private ParticipantController.ItemClickListener participantClickListener() {
        return (Item item) -> roomViewModel.processInput(new PinParticipant(item.sid));
    }

    private void initializeRoom() {
        if (room != null) {

            setupLocalParticipant(room);

            publishLocalTracks();

            updateStats();
        }
    }

    private void setupLocalParticipant(Room room) {
        localParticipant = room.getLocalParticipant();
        if (localParticipant != null) {
            localParticipantSid = localParticipant.getSid();
        }
    }

    private void publishLocalTracks() {
        if (localParticipant != null) {
            if (cameraVideoTrack != null) {
                Timber.d("Camera track: %s", cameraVideoTrack);
                localParticipant.publishTrack(cameraVideoTrack);
                roomViewModel.processInput(
                        new LocalVideoTrackPublished(localParticipant.getSid(), cameraVideoTrack));
            }

            if (localAudioTrack != null) {
                localParticipant.publishTrack(localAudioTrack);
            }
        }
    }

    private void toggleAudioDevice(boolean enableAudioDevice) {
        setVolumeControl(enableAudioDevice);
        RoomViewEvent viewEvent =
                enableAudioDevice
                        ? ActivateAudioDevice.INSTANCE
                        : RoomViewEvent.DeactivateAudioDevice.INSTANCE;
        roomViewModel.processInput(viewEvent);
    }

    private void bindRoomViewState(RoomViewState roomViewState) {
        Timber.d("RoomViewState: %s", roomViewState);
        deviceMenuItem.setVisible(!roomViewState.getAvailableAudioDevices().isEmpty());
        renderPrimaryView(roomViewState.getPrimaryParticipant());
        renderThumbnails(roomViewState);
        updateLayout(roomViewState);
    }

    private void bindRoomViewEffects(ViewEffect<RoomViewEffect> roomViewEffectWrapper) {
        RoomViewEffect roomViewEffect = roomViewEffectWrapper.getContentIfNotHandled();
        if (roomViewEffect != null) {
            Timber.d("RoomViewEffect: %s", roomViewEffect);
            requestPermissions();
            if (roomViewEffect instanceof Connected) {
                room = ((Connected) roomViewEffect).getRoom();
                toggleAudioDevice(true);
                initializeRoom();
            }
            if (roomViewEffect instanceof Disconnected) {
                localParticipant = null;
                room = null;
                localParticipantSid = LOCAL_PARTICIPANT_STUB_SID;
                updateStats();
                toggleAudioDevice(false);
                networkQualityLevels.clear();
            }
            if (roomViewEffect instanceof ShowConnectFailureDialog) {
                new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                        .setTitle(getString(R.string.room_screen_connection_failure_title))
                        .setMessage(getString(R.string.room_screen_connection_failure_message))
                        .setNeutralButton("OK", null)
                        .show();
                toggleAudioDevice(false);
            }
            if (roomViewEffect instanceof ShowTokenErrorDialog) {
                AuthServiceError error = ((ShowTokenErrorDialog) roomViewEffect).getServiceError();
                handleTokenError(error);
            }
        }
    }

    private void renderPrimaryView(ParticipantViewState primaryParticipant) {
        if (primaryParticipant != null) {
            participantController.renderAsPrimary(
                    primaryParticipant.getSid(),
                    primaryParticipant.getIdentity(),
                    primaryParticipant.getVideoTrack(),
                    primaryParticipant.isMuted(),
                    primaryParticipant.isMirrored());
        } else {
            renderLocalParticipantStub();
        }
    }

    private void renderThumbnails(RoomViewState roomViewState) {
        List<ParticipantViewState> thumbnails = roomViewState.getParticipantThumbnails();
        ParticipantControllerExtensionsKt.updateThumbnails(participantController, thumbnails);
    }

    private void displayAudioDeviceList() {
        RoomViewState viewState = roomViewModel.getViewState().getValue();
        AudioDevice selectedDevice = viewState.getSelectedDevice();
        List<AudioDevice> audioDevices = viewState.getAvailableAudioDevices();

        if (selectedDevice != null && audioDevices != null) {
            int index = audioDevices.indexOf(selectedDevice);

            ArrayList<String> audioDeviceNames = new ArrayList<>();
            for (AudioDevice a : audioDevices) {
                audioDeviceNames.add(a.getName());
            }

            createAudioDeviceDialog(
                            this,
                            index,
                            audioDeviceNames,
                            (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                                SelectAudioDevice viewEvent =
                                        new SelectAudioDevice(audioDevices.get(i));
                                roomViewModel.processInput(viewEvent);
                            })
                    .show();
        }
    }

    private AlertDialog createAudioDeviceDialog(
            final Activity activity,
            int currentDevice,
            ArrayList<String> availableDevices,
            DialogInterface.OnClickListener audioDeviceClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppTheme_Dialog);
        builder.setTitle(activity.getString(R.string.room_screen_select_device));

        builder.setSingleChoiceItems(
                availableDevices.toArray(new CharSequence[0]),
                currentDevice,
                audioDeviceClickListener);

        return builder.create();
    }

    private void handleTokenError(AuthServiceError error) {
        int errorMessage =
                error == EXPIRED_PASSCODE_ERROR
                        ? R.string.room_screen_token_expired_message
                        : R.string.room_screen_token_retrieval_failure_message;

        new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle(getString(R.string.room_screen_connection_failure_title))
                .setMessage(getString(errorMessage))
                .setNeutralButton("OK", null)
                .show();
    }

    private boolean didAcceptPermissions() {
        return PermissionChecker.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        == PermissionChecker.PERMISSION_GRANTED
                && PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA)
                        == PermissionChecker.PERMISSION_GRANTED
                && PermissionChecker.checkSelfPermission(
                                this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PermissionChecker.PERMISSION_GRANTED;
    }
}
