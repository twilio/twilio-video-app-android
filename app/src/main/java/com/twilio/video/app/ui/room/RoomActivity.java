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
import static com.twilio.video.app.R.drawable.ic_phonelink_ring_white_24dp;
import static com.twilio.video.app.R.drawable.ic_volume_up_white_24dp;
import static com.twilio.video.app.data.api.AuthServiceError.EXPIRED_PASSCODE_ERROR;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import com.google.android.material.snackbar.Snackbar;
import com.twilio.audio_manager.AudioDevice;
import com.twilio.audio_manager.AudioDeviceSelector;
import com.twilio.video.AspectRatio;
import com.twilio.video.CameraCapturer;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalAudioTrackPublication;
import com.twilio.video.LocalDataTrack;
import com.twilio.video.LocalDataTrackPublication;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.LocalVideoTrackPublication;
import com.twilio.video.NetworkQualityLevel;
import com.twilio.video.RemoteAudioTrack;
import com.twilio.video.RemoteAudioTrackPublication;
import com.twilio.video.RemoteDataTrack;
import com.twilio.video.RemoteDataTrackPublication;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.Room;
import com.twilio.video.Room.State;
import com.twilio.video.ScreenCapturer;
import com.twilio.video.StatsListener;
import com.twilio.video.TwilioException;
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
import com.twilio.video.app.ui.room.RoomEvent.ConnectFailure;
import com.twilio.video.app.ui.room.RoomEvent.Connecting;
import com.twilio.video.app.ui.room.RoomEvent.DominantSpeakerChanged;
import com.twilio.video.app.ui.room.RoomEvent.ParticipantConnected;
import com.twilio.video.app.ui.room.RoomEvent.ParticipantDisconnected;
import com.twilio.video.app.ui.room.RoomEvent.RoomState;
import com.twilio.video.app.ui.room.RoomEvent.TokenError;
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
    private static final String SCREEN_TRACK_NAME = "screen";
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

    private AudioManager audioManager;
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

        RoomViewModelFactory factory = new RoomViewModelFactory(roomManager);
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

        // Setup Audio
        audioDeviceSelector.start((audioDevices, selectedAudioDevice) -> {});
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);
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

        addParticipantViews();
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
        // Reset the speakerphone
        audioManager.setSpeakerphoneOn(false);
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
        removeAllParticipants();
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

        requestPermissions();
        roomViewModel.getRoomEvents().observe(this, this::bindRoomEvents);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_camera_menu_item:
                switchCamera();
                return true;
            case R.id.speaker_menu_item:
                if (audioManager.isSpeakerphoneOn()) {
                    audioManager.setSpeakerphoneOn(false);
                    item.setIcon(ic_phonelink_ring_white_24dp);
                } else {
                    audioManager.setSpeakerphoneOn(true);
                    item.setIcon(ic_volume_up_white_24dp);
                }
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
                setupAudioDevices();
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

            roomViewModel.connectToRoom(displayName, roomName, isNetworkQualityEnabled());
        }
    }

    @OnClick(R.id.disconnect)
    void disconnectButtonClick() {
        roomViewModel.disconnect();
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
            participantController.updateThumb(localParticipantSid, oldVideo, cameraVideoTrack);

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
                        localParticipantSid, cameraVideoTrack, ParticipantView.State.SELECTED);
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

    private void setupAudioDevices() {
        AudioDevice audioDevice = audioDeviceSelector.getSelectedAudioDevice();
        List<AudioDevice> audioDevices = audioDeviceSelector.getAudioDevices();

        int index = audioDevices.indexOf(audioDevice);

        ArrayList<String> audioDeviceNames = new ArrayList<>();
        for (AudioDevice a : audioDevices) {
            audioDeviceNames.add(a.name);
        }

        createAudioDeviceDialog(
                        this,
                        index,
                        audioDeviceNames,
                        (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            audioDeviceSelector.selectDevice(audioDevices.get(i));
                        })
                .show();
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

    private void updateUi(Room room, RoomEvent roomEvent) {
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

        if (roomEvent instanceof Connecting) {
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

        if (room != null) {
            switch (room.getState()) {
                case CONNECTED:
                    disconnectButtonState = View.VISIBLE;
                    joinRoomLayoutState = View.GONE;
                    joinStatusLayoutState = View.GONE;
                    settingsMenuItemState = false;
                    screenCaptureMenuItemState = true;

                    connectButtonEnabled = false;

                    roomName = room.getName();
                    toolbarTitle = roomName;
                    joinStatus = "";

                    break;
                case DISCONNECTED:
                    connectButtonEnabled = true;
                    screenCaptureMenuItemState = false;
                    break;
            }
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
                participantController.updateThumb(localParticipantSid, cameraVideoTrack, mirror);
            }
        }
    }

    private void setAudioFocus(boolean setFocus) {
        if (setFocus) {
            savedIsSpeakerPhoneOn = audioManager.isSpeakerphoneOn();
            savedIsMicrophoneMute = audioManager.isMicrophoneMute();
            setMicrophoneMute();
            savedAudioMode = audioManager.getMode();
            // Request audio focus before making any device switch.
            requestAudioFocus();
            /*
             * Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
             * required to be in this mode when playout and/or recording starts for
             * best possible VoIP performance.
             * Some devices have difficulties with speaker mode if this is not set.
             */
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            setVolumeControl(true);
        } else {
            audioManager.setMode(savedAudioMode);
            audioManager.abandonAudioFocus(null);
            audioManager.setMicrophoneMute(savedIsMicrophoneMute);
            audioManager.setSpeakerphoneOn(savedIsSpeakerPhoneOn);
            setVolumeControl(false);
        }
    }

    private void requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes playbackAttributes =
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build();
            AudioFocusRequest focusRequest =
                    new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                            .setAudioAttributes(playbackAttributes)
                            .setAcceptsDelayedFocusGain(true)
                            .setOnAudioFocusChangeListener(i -> {})
                            .build();
            audioManager.requestAudioFocus(focusRequest);
        } else {
            audioManager.requestAudioFocus(
                    null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
    }

    /** Sets the microphone mute state. */
    private void setMicrophoneMute() {
        boolean wasMuted = audioManager.isMicrophoneMute();
        if (!wasMuted) {
            return;
        }
        audioManager.setMicrophoneMute(false);
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
     * Provides remoteParticipant a listener for media events and add thumb.
     *
     * @param remoteParticipant newly joined room remoteParticipant
     */
    private void addParticipant(RemoteParticipant remoteParticipant, boolean renderAsPrimary) {
        boolean muted =
                remoteParticipant.getRemoteAudioTracks().size() <= 0
                        || !remoteParticipant.getRemoteAudioTracks().get(0).isTrackEnabled();
        List<RemoteVideoTrackPublication> remoteVideoTrackPublications =
                remoteParticipant.getRemoteVideoTracks();

        if (remoteVideoTrackPublications.isEmpty()) {
            /*
             * Add placeholder UI by passing null video track for a participant that is not
             * sharing any video tracks.
             */
            addParticipantVideoTrack(remoteParticipant, muted, null, renderAsPrimary);
        } else {
            for (RemoteVideoTrackPublication remoteVideoTrackPublication :
                    remoteVideoTrackPublications) {
                addParticipantVideoTrack(
                        remoteParticipant,
                        muted,
                        remoteVideoTrackPublication.getRemoteVideoTrack(),
                        renderAsPrimary);
                renderAsPrimary = false;
            }
        }
    }

    private void addParticipantVideoTrack(
            RemoteParticipant remoteParticipant,
            boolean muted,
            RemoteVideoTrack remoteVideoTrack,
            boolean renderAsPrimary) {
        if (renderAsPrimary) {
            ParticipantPrimaryView primaryView = participantController.getPrimaryView();

            renderItemAsPrimary(
                    new ParticipantController.Item(
                            remoteParticipant.getSid(),
                            remoteParticipant.getIdentity(),
                            remoteVideoTrack,
                            muted,
                            false));
            RemoteParticipantListener listener =
                    new RemoteParticipantListener(primaryView, remoteParticipant.getSid());
            remoteParticipant.setListener(listener);
        } else {
            participantController.addThumb(
                    remoteParticipant.getSid(),
                    remoteParticipant.getIdentity(),
                    remoteVideoTrack,
                    muted,
                    false);

            RemoteParticipantListener listener =
                    new RemoteParticipantListener(
                            participantController.getThumb(
                                    remoteParticipant.getSid(), remoteVideoTrack),
                            remoteParticipant.getSid());
            remoteParticipant.setListener(listener);
        }
    }

    /**
     * Sets new item to render as primary view and moves existing primary view item to thumbs view.
     *
     * @param item New item to be rendered in primary view
     */
    private void renderItemAsPrimary(ParticipantController.Item item) {
        // nothing to click while not in room
        if (room == null) return;

        // no need to renderer if same item clicked
        ParticipantController.Item old = participantController.getPrimaryItem();
        if (old != null && item.sid.equals(old.sid) && item.videoTrack == old.videoTrack) return;

        // add back old participant to thumbs
        if (old != null) {

            if (old.sid.equals(localParticipantSid)) {

                // toggle local participant state
                int state =
                        old.videoTrack == null
                                ? ParticipantView.State.NO_VIDEO
                                : ParticipantView.State.VIDEO;
                participantController.updateThumb(old.sid, old.videoTrack, state);
                participantController.updateThumb(old.sid, old.videoTrack, old.mirror);

            } else {

                // add thumb for remote participant
                RemoteParticipant remoteParticipant = getRemoteParticipant(old);
                if (remoteParticipant != null) {
                    participantController.addThumb(
                            old.sid, old.identity, old.videoTrack, old.muted, old.mirror);
                    RemoteParticipantListener listener =
                            new RemoteParticipantListener(
                                    participantController.getThumb(old.sid, old.videoTrack),
                                    remoteParticipant.getSid());
                    remoteParticipant.setListener(listener);
                }
            }
        }

        // handle new primary participant click
        participantController.renderAsPrimary(item);

        RemoteParticipant remoteParticipant = getRemoteParticipant(item);
        if (remoteParticipant != null) {
            ParticipantPrimaryView primaryView = participantController.getPrimaryView();
            RemoteParticipantListener listener =
                    new RemoteParticipantListener(primaryView, remoteParticipant.getSid());
            remoteParticipant.setListener(listener);
        }

        if (item.sid.equals(localParticipantSid)) {

            // toggle local participant state and hide his badge
            participantController.updateThumb(
                    item.sid, item.videoTrack, ParticipantView.State.SELECTED);
            participantController.getPrimaryView().showIdentityBadge(false);
        } else {

            // remove remote participant thumb
            participantController.removeThumb(item);
        }
    }

    private @Nullable RemoteParticipant getRemoteParticipant(ParticipantController.Item item) {
        RemoteParticipant remoteParticipant = null;

        for (RemoteParticipant temp : room.getRemoteParticipants()) {
            if (temp.getSid().equals(item.sid)) remoteParticipant = temp;
        }

        return remoteParticipant;
    }

    /** Removes all participant thumbs and push local camera as primary with empty sid. */
    private void removeAllParticipants() {
        if (room != null) {
            participantController.removeAllThumbs();
            participantController.removePrimary();

            renderLocalParticipantStub();
        }
    }

    /**
     * Remove single remoteParticipant thumbs and all it associated thumbs. If rendered as primary
     * remoteParticipant, primary view switches to local video track.
     *
     * @param remoteParticipant recently disconnected remoteParticipant.¬
     */
    private void removeParticipant(RemoteParticipant remoteParticipant) {

        if (participantController.getPrimaryItem().sid.equals(remoteParticipant.getSid())) {

            // render local video if primary remoteParticipant has gone
            participantController.getThumb(localParticipantSid, cameraVideoTrack).callOnClick();
        }

        participantController.removeThumbs(remoteParticipant.getSid());
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

    /**
     * Provides participant thumb click listener. On thumb click appropriate video track is being
     * send to primary view. If local camera track becomes primary, it should just change it state
     * to SELECTED state, if remote particpant track is going to be primary - thumb is removed.
     *
     * @return participant click listener.
     */
    private ParticipantController.ItemClickListener participantClickListener() {
        return this::renderItemAsPrimary;
    }

    private void initializeRoom() {
        if (room != null) {

            localParticipant = room.getLocalParticipant();

            publishLocalTracks();

            setAudioFocus(true);

            updateStats();

            addParticipantViews();
        }
    }

    private void publishLocalTracks() {
        if (localParticipant != null) {
            if (cameraVideoTrack != null) {
                Timber.d("Camera track: %s", cameraVideoTrack);
                localParticipant.publishTrack(cameraVideoTrack);
            }

            if (localAudioTrack != null) {
                localParticipant.publishTrack(localAudioTrack);
            }
        }
    }

    private void addParticipantViews() {
        if (room != null && localParticipant != null) {
            localParticipantSid = localParticipant.getSid();
            // remove primary view
            participantController.removePrimary();

            // add local thumb and "click" on it to make primary
            participantController.addThumb(
                    localParticipantSid,
                    getString(R.string.you),
                    cameraVideoTrack,
                    localAudioTrack == null,
                    cameraCapturer.getCameraSource() == CameraCapturer.CameraSource.FRONT_CAMERA);

            localParticipant.setListener(
                    new LocalParticipantListener(
                            participantController.getThumb(localParticipantSid, cameraVideoTrack)));
            participantController.getThumb(localParticipantSid, cameraVideoTrack).callOnClick();

            // add existing room participants thumbs
            boolean isFirstParticipant = true;
            for (RemoteParticipant remoteParticipant : room.getRemoteParticipants()) {
                addParticipant(remoteParticipant, isFirstParticipant);
                isFirstParticipant = false;
                if (room.getDominantSpeaker() != null) {
                    if (room.getDominantSpeaker().getSid().equals(remoteParticipant.getSid())) {
                        VideoTrack videoTrack =
                                (remoteParticipant.getRemoteVideoTracks().size() > 0)
                                        ? remoteParticipant
                                                .getRemoteVideoTracks()
                                                .get(0)
                                                .getRemoteVideoTrack()
                                        : null;
                        if (videoTrack != null) {
                            ParticipantView participantView =
                                    participantController.getThumb(
                                            remoteParticipant.getSid(), videoTrack);
                            participantController.setDominantSpeaker(participantView);
                        }
                    }
                }
            }
        }
    }

    private void bindRoomEvents(RoomEvent roomEvent) {
        if (roomEvent != null) {
            this.room = roomEvent.getRoom();
            if (room != null) {
                requestPermissions();
                if (roomEvent instanceof RoomState) {
                    State state = room.getState();
                    switch (state) {
                        case CONNECTED:
                            initializeRoom();
                            break;
                        case DISCONNECTED:
                            removeAllParticipants();
                            localParticipant = null;
                            room = null;
                            localParticipantSid = LOCAL_PARTICIPANT_STUB_SID;
                            updateStats();
                            setAudioFocus(false);
                            networkQualityLevels.clear();
                            break;
                    }
                }
                if (roomEvent instanceof ConnectFailure) {
                    new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                            .setTitle(getString(R.string.room_screen_connection_failure_title))
                            .setMessage(getString(R.string.room_screen_connection_failure_message))
                            .setNeutralButton("OK", null)
                            .show();
                    removeAllParticipants();
                    setAudioFocus(false);
                }
                if (roomEvent instanceof ParticipantConnected) {
                    boolean renderAsPrimary = room.getRemoteParticipants().size() == 1;
                    addParticipant(
                            ((ParticipantConnected) roomEvent).getRemoteParticipant(),
                            renderAsPrimary);

                    updateStatsUI(sharedPreferences.getBoolean(Preferences.ENABLE_STATS, false));
                }
                if (roomEvent instanceof ParticipantDisconnected) {
                    RemoteParticipant remoteParticipant =
                            ((ParticipantDisconnected) roomEvent).getRemoteParticipant();
                    networkQualityLevels.remove(remoteParticipant.getSid());
                    removeParticipant(remoteParticipant);

                    updateStatsUI(sharedPreferences.getBoolean(Preferences.ENABLE_STATS, false));
                }
                if (roomEvent instanceof DominantSpeakerChanged) {
                    RemoteParticipant remoteParticipant =
                            ((DominantSpeakerChanged) roomEvent).getRemoteParticipant();

                    if (remoteParticipant == null) {
                        participantController.setDominantSpeaker(null);
                        return;
                    }
                    VideoTrack videoTrack =
                            (remoteParticipant.getRemoteVideoTracks().size() > 0)
                                    ? remoteParticipant
                                            .getRemoteVideoTracks()
                                            .get(0)
                                            .getRemoteVideoTrack()
                                    : null;
                    if (videoTrack != null) {
                        ParticipantView participantView =
                                participantController.getThumb(
                                        remoteParticipant.getSid(), videoTrack);
                        if (participantView != null) {
                            participantController.setDominantSpeaker(participantView);
                        } else {
                            remoteParticipant.getIdentity();
                            ParticipantPrimaryView primaryParticipantView =
                                    participantController.getPrimaryView();
                            if (primaryParticipantView.identity.equals(
                                    remoteParticipant.getIdentity())) {
                                participantController.setDominantSpeaker(
                                        participantController.getPrimaryView());
                            } else {
                                participantController.setDominantSpeaker(null);
                            }
                        }
                    }
                }
            } else {
                if (roomEvent instanceof TokenError) {
                    AuthServiceError error = ((TokenError) roomEvent).getServiceError();
                    handleTokenError(error);
                }
            }
            updateUi(room, roomEvent);
        }
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

    private class LocalParticipantListener implements LocalParticipant.Listener {

        private ImageView networkQualityImage;

        LocalParticipantListener(ParticipantView primaryView) {
            networkQualityImage = primaryView.networkQualityLevelImg;
        }

        @Override
        public void onAudioTrackPublished(
                @NonNull LocalParticipant localParticipant,
                @NonNull LocalAudioTrackPublication localAudioTrackPublication) {}

        @Override
        public void onAudioTrackPublicationFailed(
                @NonNull LocalParticipant localParticipant,
                @NonNull LocalAudioTrack localAudioTrack,
                @NonNull TwilioException twilioException) {}

        @Override
        public void onVideoTrackPublished(
                @NonNull LocalParticipant localParticipant,
                @NonNull LocalVideoTrackPublication localVideoTrackPublication) {}

        @Override
        public void onVideoTrackPublicationFailed(
                @NonNull LocalParticipant localParticipant,
                @NonNull LocalVideoTrack localVideoTrack,
                @NonNull TwilioException twilioException) {}

        @Override
        public void onDataTrackPublished(
                @NonNull LocalParticipant localParticipant,
                @NonNull LocalDataTrackPublication localDataTrackPublication) {}

        @Override
        public void onDataTrackPublicationFailed(
                @NonNull LocalParticipant localParticipant,
                @NonNull LocalDataTrack localDataTrack,
                @NonNull TwilioException twilioException) {}

        @Override
        public void onNetworkQualityLevelChanged(
                @NonNull LocalParticipant localParticipant,
                @NonNull NetworkQualityLevel networkQualityLevel) {
            setNetworkQualityLevelImage(
                    networkQualityImage, networkQualityLevel, localParticipant.getSid());
        }
    }

    private class RemoteParticipantListener implements RemoteParticipant.Listener {

        private ImageView networkQualityImage;

        RemoteParticipantListener(ParticipantView primaryView, String sid) {
            networkQualityImage = primaryView.networkQualityLevelImg;
            setNetworkQualityLevelImage(networkQualityImage, networkQualityLevels.get(sid), sid);
        }

        @Override
        public void onNetworkQualityLevelChanged(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull NetworkQualityLevel networkQualityLevel) {
            setNetworkQualityLevelImage(
                    networkQualityImage, networkQualityLevel, remoteParticipant.getSid());
        }

        @Override
        public void onAudioTrackPublished(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
            Timber.i(
                    "onAudioTrackPublished: remoteParticipant: %s, audio: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.getIdentity(),
                    remoteAudioTrackPublication.getTrackSid(),
                    remoteAudioTrackPublication.isTrackEnabled(),
                    remoteAudioTrackPublication.isTrackSubscribed());

            // TODO: Need design
        }

        @Override
        public void onAudioTrackUnpublished(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
            Timber.i(
                    "onAudioTrackUnpublished: remoteParticipant: %s, audio: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.getIdentity(),
                    remoteAudioTrackPublication.getTrackSid(),
                    remoteAudioTrackPublication.isTrackEnabled(),
                    remoteAudioTrackPublication.isTrackSubscribed());
            // TODO: Need design
        }

        @Override
        public void onVideoTrackPublished(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
            Timber.i(
                    "onVideoTrackPublished: remoteParticipant: %s, video: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.getIdentity(),
                    remoteVideoTrackPublication.getTrackSid(),
                    remoteVideoTrackPublication.isTrackEnabled(),
                    remoteVideoTrackPublication.isTrackSubscribed());
            // TODO: Need design
        }

        @Override
        public void onVideoTrackUnpublished(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
            Timber.i(
                    "onVideoTrackUnpublished: remoteParticipant: %s, video: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.getIdentity(),
                    remoteVideoTrackPublication.getTrackSid(),
                    remoteVideoTrackPublication.isTrackEnabled(),
                    remoteVideoTrackPublication.isTrackSubscribed());
            // TODO: Need design
        }

        @Override
        public void onAudioTrackSubscribed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                @NonNull RemoteAudioTrack remoteAudioTrack) {
            Timber.i(
                    "onAudioTrackSubscribed: remoteParticipant: %s, audio: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.getIdentity(),
                    remoteAudioTrackPublication.getTrackSid(),
                    remoteAudioTrackPublication.isTrackEnabled(),
                    remoteAudioTrackPublication.isTrackSubscribed());
            boolean newAudioState = !remoteAudioTrackPublication.isTrackEnabled();

            if (participantController.getPrimaryItem().sid.equals(remoteParticipant.getSid())) {

                // update audio state for primary view
                participantController.getPrimaryItem().muted = newAudioState;
                participantController.getPrimaryView().setMuted(newAudioState);

            } else {

                // update thumbs with audio state
                participantController.updateThumbs(remoteParticipant.getSid(), newAudioState);
            }
        }

        @Override
        public void onAudioTrackSubscriptionFailed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                @NonNull TwilioException twilioException) {
            Timber.w(
                    "onAudioTrackSubscriptionFailed: remoteParticipant: %s, video: %s, exception: %s",
                    remoteParticipant.getIdentity(),
                    remoteAudioTrackPublication.getTrackSid(),
                    twilioException.getMessage());
            // TODO: Need design
            Snackbar.make(primaryVideoView, "onAudioTrackSubscriptionFailed", Snackbar.LENGTH_LONG)
                    .show();
        }

        @Override
        public void onAudioTrackUnsubscribed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                @NonNull RemoteAudioTrack remoteAudioTrack) {
            Timber.i(
                    "onAudioTrackUnsubscribed: remoteParticipant: %s, audio: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.getIdentity(),
                    remoteAudioTrackPublication.getTrackSid(),
                    remoteAudioTrackPublication.isTrackEnabled(),
                    remoteAudioTrackPublication.isTrackSubscribed());

            if (participantController.getPrimaryItem().sid.equals(remoteParticipant.getSid())) {

                // update audio state for primary view
                participantController.getPrimaryItem().muted = true;
                participantController.getPrimaryView().setMuted(true);

            } else {

                // update thumbs with audio state
                participantController.updateThumbs(remoteParticipant.getSid(), true);
            }
        }

        @Override
        public void onVideoTrackSubscribed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                @NonNull RemoteVideoTrack remoteVideoTrack) {
            Timber.i(
                    "onVideoTrackSubscribed: remoteParticipant: %s, video: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.getIdentity(),
                    remoteVideoTrackPublication.getTrackSid(),
                    remoteVideoTrackPublication.isTrackEnabled(),
                    remoteVideoTrackPublication.isTrackSubscribed());

            ParticipantController.Item primary = participantController.getPrimaryItem();

            if (primary != null
                    && primary.sid.equals(remoteParticipant.getSid())
                    && primary.videoTrack == null) {
                // no thumb needed - render as primary
                primary.videoTrack = remoteVideoTrack;
                participantController.renderAsPrimary(primary);
            } else {
                // not a primary remoteParticipant requires thumb
                participantController.addOrUpdateThumb(
                        remoteParticipant.getSid(),
                        remoteParticipant.getIdentity(),
                        null,
                        remoteVideoTrack);
            }
        }

        @Override
        public void onVideoTrackSubscriptionFailed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                @NonNull TwilioException twilioException) {
            Timber.w(
                    "onVideoTrackSubscriptionFailed: remoteParticipant: %s, video: %s, exception: %s",
                    remoteParticipant.getIdentity(),
                    remoteVideoTrackPublication.getTrackSid(),
                    twilioException.getMessage());
            // TODO: Need design
            Snackbar.make(primaryVideoView, "onVideoTrackSubscriptionFailed", Snackbar.LENGTH_LONG)
                    .show();
        }

        @Override
        public void onVideoTrackUnsubscribed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                @NonNull RemoteVideoTrack remoteVideoTrack) {
            Timber.i(
                    "onVideoTrackUnsubscribed: remoteParticipant: %s, video: %s, enabled: %b",
                    remoteParticipant.getIdentity(),
                    remoteVideoTrackPublication.getTrackSid(),
                    remoteVideoTrackPublication.isTrackEnabled());

            ParticipantController.Item primary = participantController.getPrimaryItem();

            if (primary != null
                    && primary.sid.equals(remoteParticipant.getSid())
                    && primary.videoTrack == remoteVideoTrack) {

                // Remove primary video track
                primary.videoTrack = null;

                // Try to find another video track to render as primary
                List<RemoteVideoTrackPublication> remoteVideoTracks =
                        remoteParticipant.getRemoteVideoTracks();
                for (RemoteVideoTrackPublication newRemoteVideoTrackPublication :
                        remoteVideoTracks) {
                    RemoteVideoTrack newRemoteVideoTrack =
                            newRemoteVideoTrackPublication.getRemoteVideoTrack();
                    if (newRemoteVideoTrack != remoteVideoTrack) {
                        participantController.removeThumb(
                                remoteParticipant.getSid(), newRemoteVideoTrack);
                        primary.videoTrack = newRemoteVideoTrack;
                        break;
                    }
                }
                participantController.renderAsPrimary(primary);
            } else {

                // remove thumb or leave empty video thumb
                participantController.removeOrEmptyThumb(
                        remoteParticipant.getSid(),
                        remoteParticipant.getIdentity(),
                        remoteVideoTrack);
            }
        }

        @Override
        public void onDataTrackPublished(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteDataTrackPublication remoteDataTrackPublication) {
            Timber.i(
                    "onDataTrackPublished: remoteParticipant: %s, data: %s, enabled: %b",
                    remoteParticipant.getIdentity(),
                    remoteDataTrackPublication.getTrackSid(),
                    remoteDataTrackPublication.isTrackEnabled());
        }

        @Override
        public void onDataTrackUnpublished(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteDataTrackPublication remoteDataTrackPublication) {
            Timber.i(
                    "onDataTrackUnpublished: remoteParticipant: %s, data: %s, enabled: %b",
                    remoteParticipant.getIdentity(),
                    remoteDataTrackPublication.getTrackSid(),
                    remoteDataTrackPublication.isTrackEnabled());
        }

        @Override
        public void onDataTrackSubscribed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                @NonNull RemoteDataTrack remoteDataTrack) {
            Timber.i(
                    "onDataTrackSubscribed: remoteParticipant: %s, data: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.getIdentity(),
                    remoteDataTrackPublication.getTrackSid(),
                    remoteDataTrackPublication.isTrackEnabled(),
                    remoteDataTrackPublication.isTrackSubscribed());
        }

        @Override
        public void onDataTrackSubscriptionFailed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                @NonNull TwilioException twilioException) {
            Timber.w(
                    "onDataTrackSubscriptionFailed: remoteParticipant: %s, video: %s, exception: %s",
                    remoteParticipant.getIdentity(),
                    remoteDataTrackPublication.getTrackSid(),
                    twilioException.getMessage());
            // TODO: Need design
            Snackbar.make(primaryVideoView, "onDataTrackSubscriptionFailed", Snackbar.LENGTH_LONG)
                    .show();
        }

        @Override
        public void onDataTrackUnsubscribed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                @NonNull RemoteDataTrack remoteDataTrack) {
            Timber.i(
                    "onDataTrackUnsubscribed: remoteParticipant: %s, data: %s, enabled: %b, subscribed: %b",
                    remoteParticipant.getIdentity(),
                    remoteDataTrackPublication.getTrackSid(),
                    remoteDataTrackPublication.isTrackEnabled(),
                    remoteDataTrackPublication.isTrackSubscribed());
        }

        @Override
        public void onAudioTrackEnabled(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
            Timber.i(
                    "onAudioTrackEnabled: remoteParticipant: %s, audio: %s, enabled: %b",
                    remoteParticipant.getIdentity(),
                    remoteAudioTrackPublication.getTrackSid(),
                    remoteAudioTrackPublication.isTrackEnabled());

            // TODO: need design
        }

        @Override
        public void onAudioTrackDisabled(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
            Timber.i(
                    "onAudioTrackDisabled: remoteParticipant: %s, audio: %s, enabled: %b",
                    remoteParticipant.getIdentity(),
                    remoteAudioTrackPublication.getTrackSid(),
                    remoteAudioTrackPublication.isTrackEnabled());

            // TODO: need design
        }

        @Override
        public void onVideoTrackEnabled(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
            Timber.i(
                    "onVideoTrackEnabled: remoteParticipant: %s, video: %s, enabled: %b",
                    remoteParticipant.getIdentity(),
                    remoteVideoTrackPublication.getTrackSid(),
                    remoteVideoTrackPublication.isTrackEnabled());

            // TODO: need design
        }

        @Override
        public void onVideoTrackDisabled(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
            Timber.i(
                    "onVideoTrackDisabled: remoteParticipant: %s, video: %s, enabled: %b",
                    remoteParticipant.getIdentity(),
                    remoteVideoTrackPublication.getTrackSid(),
                    remoteVideoTrackPublication.isTrackEnabled());

            // TODO: need design
        }
    }

    private void setNetworkQualityLevelImage(
            ImageView networkQualityImage, NetworkQualityLevel networkQualityLevel, String sid) {

        networkQualityLevels.put(sid, networkQualityLevel);
        if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_UNKNOWN) {
            networkQualityImage.setVisibility(View.GONE);
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_ZERO) {
            networkQualityImage.setVisibility(View.VISIBLE);
            networkQualityImage.setImageResource(R.drawable.network_quality_level_0);
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_ONE) {
            networkQualityImage.setVisibility(View.VISIBLE);
            networkQualityImage.setImageResource(R.drawable.network_quality_level_1);
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_TWO) {
            networkQualityImage.setVisibility(View.VISIBLE);
            networkQualityImage.setImageResource(R.drawable.network_quality_level_2);
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_THREE) {
            networkQualityImage.setVisibility(View.VISIBLE);
            networkQualityImage.setImageResource(R.drawable.network_quality_level_3);
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_FOUR) {
            networkQualityImage.setVisibility(View.VISIBLE);
            networkQualityImage.setImageResource(R.drawable.network_quality_level_4);
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_FIVE) {
            networkQualityImage.setVisibility(View.VISIBLE);
            networkQualityImage.setImageResource(R.drawable.network_quality_level_5);
        }
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
