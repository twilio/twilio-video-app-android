package com.twilio.video.app.ui.room;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import android.widget.Toast;

import com.twilio.video.AspectRatio;
import com.twilio.video.AudioTrack;
import com.twilio.video.CameraCapturer;
import com.twilio.video.ConnectOptions;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.Participant;
import com.twilio.video.Room;
import com.twilio.video.RoomState;
import com.twilio.video.ScreenCapturer;
import com.twilio.video.StatsListener;
import com.twilio.video.StatsReport;
import com.twilio.video.TwilioException;
import com.twilio.video.Video;
import com.twilio.video.VideoConstraints;
import com.twilio.video.VideoDimensions;
import com.twilio.video.VideoTrack;
import com.twilio.video.app.R;
import com.twilio.video.app.adapter.StatsListAdapter;
import com.twilio.video.app.base.BaseActivity;
import com.twilio.video.app.data.Preferences;
import com.twilio.video.app.data.api.TokenService;
import com.twilio.video.app.data.api.VideoAppService;
import com.twilio.video.app.data.api.model.Topology;
import com.twilio.video.app.ui.settings.SettingsActivity;
import com.twilio.video.app.util.EnvUtil;
import com.twilio.video.app.util.InputUtils;
import com.twilio.video.app.util.StatsScheduler;
import com.twilio.video.env.Env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import timber.log.Timber;

import static com.twilio.video.app.R.drawable.ic_phonelink_ring_white_24dp;
import static com.twilio.video.app.R.drawable.ic_volume_up_white_24dp;

public class RoomActivity extends BaseActivity {
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int MEDIA_PROJECTION_REQUEST_CODE = 101;
    private static final int STATS_DELAY = 1000; // milliseconds

    // This will be used instead of real local participant sid,
    // because that information is unknown until room connection is fully established
    private static final String LOCAL_PARTICIPANT_STUB_SID = "";

    private AspectRatio[] aspectRatios = new AspectRatio[]{
            VideoConstraints.ASPECT_RATIO_4_3,
            VideoConstraints.ASPECT_RATIO_16_9,
            VideoConstraints.ASPECT_RATIO_11_9
    };

    private VideoDimensions[] videoDimensions = new VideoDimensions[]{
            VideoDimensions.CIF_VIDEO_DIMENSIONS,
            VideoDimensions.VGA_VIDEO_DIMENSIONS,
            VideoDimensions.WVGA_VIDEO_DIMENSIONS,
            VideoDimensions.HD_540P_VIDEO_DIMENSIONS,
            VideoDimensions.HD_720P_VIDEO_DIMENSIONS,
            VideoDimensions.HD_960P_VIDEO_DIMENSIONS,
            VideoDimensions.HD_S1080P_VIDEO_DIMENSIONS,
            VideoDimensions.HD_1080P_VIDEO_DIMENSIONS
    };

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.connect) Button connect;
    @BindView(R.id.disconnect) ImageButton disconnectButton;
    @BindView(R.id.primary_video) ParticipantPrimaryView primaryVideoView;
    @BindView(R.id.remote_video_thumbnails) LinearLayout thumbnailLinearLayout;
    @BindView(R.id.local_video_image_button) ImageButton localVideoImageButton;
    @BindView(R.id.local_audio_image_button) ImageButton localAudioImageButton;
    @BindView(R.id.video_container) FrameLayout frameLayout;

    @BindView(R.id.join_room_layout) LinearLayout joinRoomLayout;
    @BindView(R.id.room_edit_text) ClearableEditText roomEditText;

    @BindView(R.id.join_status_layout) LinearLayout joinStatusLayout;
    @BindView(R.id.join_status) TextView joinStatusTextView;
    @BindView(R.id.join_room_name) TextView joinRoomNameTextView;

    @BindView(R.id.stats_recycler_view) RecyclerView statsRecyclerView;
    @BindView(R.id.stats_disabled) LinearLayout statsDisabledLayout;
    @BindView(R.id.stats_disabled_title) TextView statsDisabledTitleTextView;
    @BindView(R.id.stats_disabled_description) TextView statsDisabledDescTextView;

    private MenuItem switchCameraMenuItem;
    private MenuItem pauseVideoMenuItem;
    private MenuItem pauseAudioMenuItem;
    private MenuItem screenCaptureMenuItem;
    private MenuItem settingsMenuItem;

    private AudioManager audioManager;
    private int savedAudioMode = AudioManager.MODE_INVALID;
    private int savedVolumeControlStream;

    private String displayName;
    private LocalParticipant localParticipant;
    private String localParticipantSid = LOCAL_PARTICIPANT_STUB_SID;
    private Room room;
    private VideoConstraints videoConstraints;
    private LocalAudioTrack localAudioTrack;
    private LocalVideoTrack cameraVideoTrack;
    private boolean restoreLocalVideoCameraTrack = false;
    private LocalVideoTrack screenVideoTrack;
    private CameraCapturer cameraCapturer;
    private ScreenCapturer screenCapturer;
    private final ScreenCapturer.Listener screenCapturerListener = new ScreenCapturer.Listener() {
        @Override
        public void onScreenCaptureError(String errorDescription) {
            Timber.e("Screen capturer error: " + errorDescription);
            stopScreenCapture();
            Toast.makeText(RoomActivity.this, R.string.screen_capture_error,
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFirstFrameAvailable() {
            Timber.d("First frame from screen capturer available");
        }
    };

    private StatsScheduler statsScheduler;
    private StatsListAdapter statsListAdapter;
    private Map<String, String> localVideoTrackNames = new HashMap<>();

    @Inject TokenService tokenService;
    @Inject SharedPreferences sharedPreferences;

    /**
     * Coordinates participant thumbs and primary participant rendering.
     */
    private ParticipantController participantController;

    /**
     * Disposes {@link VideoAppService} requests when activity is destroyed.
     */
    private final CompositeDisposable rxDisposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);
        savedVolumeControlStream = getVolumeControlStream();

        // setup participant controller
        participantController = new ParticipantController(thumbnailLinearLayout, primaryVideoView);
        participantController.setListener(participantClickListener());

        // Setup Activity
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        displayName = sharedPreferences.getString(Preferences.DISPLAY_NAME, null);
        statsScheduler = new StatsScheduler();
        obtainVideoConstraints();
        updateUi(room);
        requestPermissions();
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
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean recordAudioPermissionGranted = grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED;
            boolean cameraPermissionGranted = grantResults[1] ==
                    PackageManager.PERMISSION_GRANTED;
            boolean writeExternalStoragePermissionGranted = grantResults[2] ==
                    PackageManager.PERMISSION_GRANTED;
            boolean permissionsGranted = recordAudioPermissionGranted &&
                    cameraPermissionGranted &&
                    writeExternalStoragePermissionGranted;

            if (permissionsGranted) {
                setupLocalMedia();
            } else {
                Toast.makeText(this, R.string.permissions_required, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreCameraTrack();
        updateStats();
    }

    @Override
    protected void onPause() {
        removeCameraTrack();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.room_menu, menu);
        settingsMenuItem = menu.findItem(R.id.settings_menu_item);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Grab menu items for updating later
        switchCameraMenuItem = menu.findItem(R.id.switch_camera_menu_item);
        pauseVideoMenuItem = menu.findItem(R.id.pause_video_menu_item);
        pauseAudioMenuItem = menu.findItem(R.id.pause_audio_menu_item);
        screenCaptureMenuItem = menu.findItem(R.id.share_screen_menu_item);

        // Screen sharing only available on lollipop and up
        screenCaptureMenuItem.setVisible(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);

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
        if (requestCode == MEDIA_PROJECTION_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(this, R.string.screen_capture_permission_not_granted,
                        Toast.LENGTH_LONG).show();

                return;
            }
            screenCapturer = new ScreenCapturer(this, resultCode, data, screenCapturerListener);
            startScreenCapture();
        }
    }

    @OnTextChanged(value = R.id.room_edit_text, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onTextChanged(CharSequence text) {
        connect.setEnabled(!TextUtils.isEmpty(text));
    }

    @OnClick(R.id.connect)
    void connectButtonClick() {
        connect.setEnabled(false);
        // obtain room name
        final String roomName = roomEditText.getText().toString();

        // obtain latest environment preferences
        Topology topology = Topology.fromString(sharedPreferences.getString(Preferences.TOPOLOGY,
                Preferences.TOPOLOGY_DEFAULT));

        Single<Room> connection = updateEnv()
                .andThen(tokenService.getToken(displayName, topology))
                .flatMap(new Function<String, SingleSource<? extends Room>>() {
                    @Override
                    public SingleSource<? extends Room> apply(@NonNull String token)
                            throws Exception {
                        return connect(token, roomName);
                    }
                });

        connection.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Room>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        InputUtils.hideKeyboard(RoomActivity.this);
                        rxDisposables.add(disposable);
                    }

                    @Override
                    public void onSuccess(Room room) {
                        RoomActivity.this.room = room;
                        updateUi(room);
                    }

                    @Override
                    public void onError(Throwable e) {
                        final String message = "Failed to retrieve access token";
                        Timber.e("%s -> reason: %s", message, e.getMessage());
                        Snackbar.make(primaryVideoView, message, Snackbar.LENGTH_LONG).show();
                        connect.setEnabled(true);
                    }
                });

    }

    @OnClick(R.id.disconnect)
    void disconnectButtonClick() {
        if (room != null) {
            Timber.i("Exiting room");
            room.disconnect();
        }
    }

    @OnClick(R.id.local_audio_image_button)
    void toggleLocalAudio() {
        int icon = 0;
        if (localAudioTrack == null) {
            localAudioTrack = LocalAudioTrack.create(this, true);
            if (localParticipant != null) {
                localParticipant.addAudioTrack(localAudioTrack);
            }
            icon = R.drawable.ic_mic_white_24px;
            pauseAudioMenuItem.setVisible(true);
            pauseAudioMenuItem.setTitle(localAudioTrack.isEnabled() ?
                    R.string.pause_audio : R.string.resume_audio);
        } else {
            if (localParticipant != null) {
                localParticipant.removeAudioTrack(localAudioTrack);
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

            // add local camera track
            cameraVideoTrack = LocalVideoTrack.create(this, true, cameraCapturer, videoConstraints);
            if (localParticipant != null) {
                localParticipant.addVideoTrack(cameraVideoTrack);
            }

            // enable video settings
            switchCameraMenuItem.setVisible(cameraVideoTrack.isEnabled());
            pauseVideoMenuItem.setTitle(cameraVideoTrack.isEnabled() ?
                    R.string.pause_video : R.string.resume_video);
            pauseVideoMenuItem.setVisible(true);

        } else {
            // remove local camera track
            cameraVideoTrack.removeRenderer(primaryVideoView);

            if (localParticipant != null) {
                localParticipant.removeVideoTrack(cameraVideoTrack);
            }
            cameraVideoTrack.release();
            cameraVideoTrack = null;

            // disable video settings
            switchCameraMenuItem.setVisible(false);
            pauseVideoMenuItem.setVisible(false);
        }

        if (room != null && room.getState() == RoomState.CONNECTED) {

            // update local participant thumb
            participantController.updateThumb(localParticipantSid, oldVideo, cameraVideoTrack);

            if (participantController.getPrimaryItem().sid.equals(localParticipantSid)) {

                // local video was rendered as primary view - refreshing
                participantController.renderAsPrimary(localParticipantSid,
                        getString(R.string.you),
                        cameraVideoTrack,
                        localAudioTrack == null,
                        cameraCapturer.getCameraSource() == CameraCapturer.CameraSource.FRONT_CAMERA);

                participantController.getPrimaryView().showIdentityBadge(false);

                // update thumb state
                participantController.updateThumb(localParticipantSid,
                        cameraVideoTrack,
                        ParticipantView.State.SELECTED);
            }

        } else {

            renderLocalParticipantStub();
        }

        // update toggle button icon
        localVideoImageButton.setImageResource(cameraVideoTrack != null ?
                R.drawable.ic_videocam_white_24px : R.drawable.ic_videocam_off_gray_24px);
    }

    private void obtainVideoConstraints() {
        Timber.d("Collecting video constraints...");

        VideoConstraints.Builder builder = new VideoConstraints.Builder();

        // setup aspect ratio
        String aspectRatio = sharedPreferences.getString(Preferences.ASPECT_RATIO, "0");
        int aspectRatioIndex = Integer.parseInt(aspectRatio);
        builder.aspectRatio(aspectRatios[aspectRatioIndex]);

        Timber.d("Aspect ratio : %s",
                getResources().getStringArray(R.array.aspect_ratio_array)[aspectRatioIndex]);

        // setup video dimensions
        int minVideoDim = sharedPreferences.getInt(Preferences.MIN_VIDEO_DIMENSIONS, 0);
        int maxVideoDim = sharedPreferences.getInt(Preferences.MAX_VIDEO_DIMENSIONS,
                videoDimensions.length - 1);

        if (maxVideoDim != -1 && minVideoDim != -1) {
            builder.minVideoDimensions(videoDimensions[minVideoDim]);
            builder.maxVideoDimensions(videoDimensions[maxVideoDim]);
        }

        Timber.d("Video dimensions: %s - %s",
                getResources().getStringArray(R.array.video_dimensions_array)[minVideoDim],
                getResources().getStringArray(R.array.video_dimensions_array)[maxVideoDim]);

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
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
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
        int resultStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return ((resultCamera == PackageManager.PERMISSION_GRANTED) &&
                (resultMic == PackageManager.PERMISSION_GRANTED) &&
                (resultStorage == PackageManager.PERMISSION_GRANTED));
    }

    /**
     * Initialize local media and provide stub participant for primary view.
     */
    private void setupLocalMedia() {
        localAudioTrack = LocalAudioTrack.create(this, true);
        setupLocalVideoTrack();
        renderLocalParticipantStub();
    }

    /**
     * Create local video track
     */
    private void setupLocalVideoTrack() {

        // initialize capturer only once if needed
        if (cameraCapturer == null) {
            cameraCapturer = new CameraCapturer(this, CameraCapturer.CameraSource.FRONT_CAMERA);
        }

        cameraVideoTrack = LocalVideoTrack.create(this, true, cameraCapturer, videoConstraints);

        if (cameraVideoTrack != null) {
            localVideoTrackNames.put(cameraVideoTrack.getTrackId(),
                    getString(R.string.camera_video_track));
        } else {
            Snackbar.make(primaryVideoView, R.string.failed_to_add_camera_video_track,
                    Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Render local video track.
     *
     * NOTE: Stub participant is created in controller. Make sure to remove it
     * when connected to room.
     */
    private void renderLocalParticipantStub() {
        participantController.renderAsPrimary(localParticipantSid,
                getString(R.string.you),
                cameraVideoTrack,
                localAudioTrack == null,
                cameraCapturer.getCameraSource() == CameraCapturer.CameraSource.FRONT_CAMERA);

        primaryVideoView.showIdentityBadge(false);
    }

    private void updateUi(Room room) {
        int disconnectButtonState = View.GONE;
        int joinRoomLayoutState = View.VISIBLE;
        int joinStatusLayoutState = View.GONE;

        boolean settingsMenuItemState = true;

        boolean connectButtonEnabled = false;

        String roomName = displayName;
        String toolbarTitle = displayName;
        String joinStatus = "";

        if (room != null) {
            switch (room.getState()) {
                case CONNECTING:
                    disconnectButtonState = View.VISIBLE;
                    joinRoomLayoutState = View.GONE;
                    joinStatusLayoutState = View.VISIBLE;
                    settingsMenuItemState = false;

                    connectButtonEnabled = false;

                    roomName = room.getName();
                    joinStatus = "Joining...";

                    break;
                case CONNECTED:
                    disconnectButtonState = View.VISIBLE;
                    joinRoomLayoutState = View.GONE;
                    joinStatusLayoutState = View.GONE;
                    settingsMenuItemState = false;

                    connectButtonEnabled = false;

                    roomName = room.getName();
                    toolbarTitle = roomName;
                    joinStatus = "";

                    break;
                case DISCONNECTED:
                    connectButtonEnabled = true;
                    break;
            }
        }

        statsListAdapter = new StatsListAdapter(this);
        statsRecyclerView.setAdapter(statsListAdapter);
        statsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        disconnectButton.setVisibility(disconnectButtonState);
        joinRoomLayout.setVisibility(joinRoomLayoutState);
        joinStatusLayout.setVisibility(joinStatusLayoutState);
        connect.setEnabled(connectButtonEnabled);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(toolbarTitle);
        }

        joinStatusTextView.setText(joinStatus);
        joinRoomNameTextView.setText(roomName);

        // TODO: Remove when we use a Service to obtainTokenAndConnect to a room
        if (settingsMenuItem != null) {
            settingsMenuItem.setVisible(settingsMenuItemState);
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
                    localParticipantSid, cameraVideoTrack, mirror);
            }

        }
    }

    private void setAudioFocus(boolean setFocus) {
        if (setFocus) {
            savedAudioMode = audioManager.getMode();
            // Request audio focus before making any device switch.
            audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            /*
             * Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
             * required to be in this mode when playout and/or recording starts for
             * best possible VoIP performance.
             * Some devices have difficulties with speaker mode if this is not set.
             */
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } else {
            audioManager.setMode(savedAudioMode);
            audioManager.abandonAudioFocus(null);
        }
    }

    private void setVolumeControl(boolean setVolumeControl) {
        if(setVolumeControl) {
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
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        // This initiates a prompt dialog for the user to confirm screen projection.
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(),
                MEDIA_PROJECTION_REQUEST_CODE);
    }

    private void startScreenCapture() {
        screenVideoTrack = LocalVideoTrack.create(this, true, screenCapturer);

        if (screenVideoTrack != null) {
            screenCaptureMenuItem.setIcon(R.drawable.ic_stop_screen_share_white_24dp);
            screenCaptureMenuItem.setTitle(R.string.stop_screen_share);
            localVideoTrackNames.put(
                    screenVideoTrack.getTrackId(), getString(R.string.screen_video_track));

            if (localParticipant != null) {
                localParticipant.addVideoTrack(screenVideoTrack);
            }
        } else {
            Snackbar.make(primaryVideoView,
                    R.string.failed_to_add_screen_video_track,
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void stopScreenCapture() {
        if (screenVideoTrack != null) {
            if (localParticipant != null) {
                localParticipant.removeVideoTrack(screenVideoTrack);
            }
            screenVideoTrack.release();
            localVideoTrackNames.remove(screenVideoTrack.getTrackId());
            screenVideoTrack = null;
            screenCaptureMenuItem.setIcon(R.drawable.ic_screen_share_white_24dp);
            screenCaptureMenuItem.setTitle(R.string.share_screen);
        }
    }

    private void toggleLocalAudioTrackState() {
        if (localAudioTrack != null) {
            boolean enable = !localAudioTrack.isEnabled();
            localAudioTrack.enable(enable);
            pauseAudioMenuItem.setTitle(localAudioTrack.isEnabled() ?
                    R.string.pause_audio : R.string.resume_audio);
        }
    }

    private void toggleLocalVideoTrackState() {
        if (cameraVideoTrack != null) {
            boolean enable = !cameraVideoTrack.isEnabled();
            cameraVideoTrack.enable(enable);
            pauseVideoMenuItem.setTitle(cameraVideoTrack.isEnabled() ?
                    R.string.pause_video : R.string.resume_video);
        }
    }

    /**
     * Update {@link com.twilio.video.Video} environment.
     *
     * @return Completable
     */
    private Completable updateEnv() {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                String env = sharedPreferences.getString(Preferences.ENVIRONMENT,
                        Preferences.ENVIRONMENT_DEFAULT);
                String nativeEnvironmentVariableValue = EnvUtil
                        .getNativeEnvironmentVariableValue(env);
                Env.set(RoomActivity.this,
                        EnvUtil.TWILIO_ENV_KEY,
                        nativeEnvironmentVariableValue,
                        true);
            }
        });
    }

    /**
     * Connect to room with specified parameters.
     *
     * @param roomName room name or sid
     * @return Single with room reference
     */
    private Single<Room> connect(final String token, final String roomName) {
        return Single.fromCallable(new Callable<Room>() {
            @Override
            public Room call() throws Exception {
                String env = sharedPreferences.getString(Preferences.ENVIRONMENT,
                    Preferences.ENVIRONMENT_DEFAULT);

                ConnectOptions.Builder connectOptionsBuilder = new ConnectOptions.Builder(token)
                        .roomName(roomName);

                if (localAudioTrack != null) {
                    connectOptionsBuilder
                            .audioTracks(Collections.singletonList(localAudioTrack));
                }

                List<LocalVideoTrack> localVideoTracks = new ArrayList<>();
                if (cameraVideoTrack != null) {
                    localVideoTracks.add(cameraVideoTrack);
                }

                if (screenVideoTrack != null) {
                    localVideoTracks.add(screenVideoTrack);
                }

                if (!localVideoTracks.isEmpty()) {
                    connectOptionsBuilder.videoTracks(localVideoTracks);
                }

                room = Video.connect(RoomActivity.this,
                        connectOptionsBuilder.build(),
                        roomListener());
                return room;
            }
        });
    }

    /**
     * Provides participant a listener for media events and add thumb.
     *
     * @param participant newly joined room participant
     */
    private void addParticipant(Participant participant) {
        ParticipantListener listener = new ParticipantListener();
        participant.setListener(listener);
        participantController.addThumb(participant.getSid(), participant.getIdentity());
    }

    /**
     * Removes all participant thumbs and push local camera as primary with empty sid.
     */
    private void removeAllParticipants() {
        participantController.removeAllThumbs();
        participantController.removePrimary();

        renderLocalParticipantStub();
    }

    /**
     * Remove single participant thumbs and all it associated thumbs.
     * If rendered as primary participant, primary view switches to local video track.
     *
     * @param participant recently disconnected participant.¬
     */
    private void removeParticipant(Participant participant) {

        if (participantController.getPrimaryItem().sid.equals(participant.getSid())) {

            // render local video if primary participant has gone
            participantController
                    .getThumb(localParticipantSid, cameraVideoTrack)
                    .callOnClick();
        }

        participantController.removeThumbs(participant.getSid());
    }

    /**
     * Remove the video track and mark the track to be restored
     * when going to the settings screen or going to the background
     */
    private void removeCameraTrack() {
        if (cameraVideoTrack != null) {
            if (localParticipant != null) {
                localParticipant.removeVideoTrack(cameraVideoTrack);
            }
            cameraVideoTrack.release();
            restoreLocalVideoCameraTrack = true;
            cameraVideoTrack = null;
        }

    }

    /**
     * Try to restore camera video track after going to the settings screen or background
     */
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
            if (room != null && room.getParticipants().size() > 0) {
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
                statsDisabledDescTextView.setText(
                        getString(R.string.stats_description_join_room));
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
        if (enableStats && (room != null) && (room.getState() == RoomState.CONNECTED)) {
            statsScheduler.scheduleStatsGathering(room, statsListener(), STATS_DELAY);
        }
        updateStatsUI(enableStats);
    }

    private StatsListener statsListener() {
        return new StatsListener() {
            @Override
            public void onStats(List<StatsReport> statsReports) {
                // Running on StatsScheduler thread
                if (room != null) {
                    statsListAdapter.updateStatsData(
                            statsReports, room.getParticipants(), localVideoTrackNames);
                }
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
        return new ParticipantController.ItemClickListener() {
            @Override
            public void onThumbClick(ParticipantController.Item item) {

                // nothing to click while not in room
                if (room == null) return;

                // no need to renderer if same item clicked
                ParticipantController.Item old = participantController.getPrimaryItem();
                if (old != null && item.sid.equals(old.sid)) return;

                // add back old participant to thumbs
                if (old != null) {

                    if (old.sid.equals(localParticipantSid)) {

                        // toggle local participant state
                        int state = old.videoTrack == null ?
                                ParticipantView.State.NO_VIDEO : ParticipantView.State.VIDEO;
                        participantController.updateThumb(old.sid, old.videoTrack, state);
                        participantController.updateThumb(old.sid, old.videoTrack, old.mirror);

                    } else {

                        // add thumb for remote participant
                        participantController.addThumb(old);
                    }
                }

                // handle new primary participant click
                participantController.renderAsPrimary(item);

                if (item.sid.equals(localParticipantSid)) {

                    // toggle local participant state and hide his badge
                    participantController.updateThumb(item.sid, item.videoTrack, ParticipantView.State.SELECTED);
                    participantController.getPrimaryView().showIdentityBadge(false);
                } else {

                    // remove remote participant thumb
                    participantController.removeThumb(item);
                }
            }
        };
    }

    private Room.Listener roomListener() {
        return new Room.Listener() {
            @Override
            public void onConnected(final Room room) {
                Timber.i("Connected to room -> name: %s, sid: %s, state: %s",
                        room.getName(),
                        room.getSid(),
                        room.getState());
                localParticipant = room.getLocalParticipant();
                localParticipantSid = localParticipant.getSid();

                setAudioFocus(true);
                setVolumeControl(true);
                updateStats();
                updateUi(room);

                // remove primary view
                participantController.removePrimary();

                // add local thumb and "click" on it to make primary
                participantController.addThumb(localParticipantSid,
                        getString(R.string.you),
                        cameraVideoTrack,
                        localAudioTrack == null,
                        cameraCapturer.getCameraSource() == CameraCapturer.CameraSource.FRONT_CAMERA);

                participantController.getThumb(
                    localParticipantSid, cameraVideoTrack).callOnClick();

                // add existing room participants thumbs
                for (Participant participant : room.getParticipants().values()) {
                    addParticipant(participant);
                }
            }

            @Override
            public void onConnectFailure(Room room, TwilioException twilioException) {
                Timber.e("Failed to connect to room -> sid: %s, state: %s, code: %d, error: %s",
                        room.getSid(),
                        room.getState(),
                        twilioException.getCode(),
                        twilioException.getMessage());

                removeAllParticipants();

                RoomActivity.this.room = null;
                updateUi(room);
            }

            @Override
            public void onDisconnected(Room room, TwilioException twilioException) {
                Timber.i("Disconnected from room -> sid: %s, state: %s",
                        room.getSid(),
                        room.getState());

                removeAllParticipants();
                RoomActivity.this.room = null;
                RoomActivity.this.localParticipant = null;
                RoomActivity.this.localParticipantSid = LOCAL_PARTICIPANT_STUB_SID;

                updateUi(room);
                updateStats();

                setAudioFocus(false);
                setVolumeControl(false);
            }

            @Override
            public void onParticipantConnected(Room room, Participant participant) {
                Timber.i("Participant connected -> room sid: %s, participant: %s",
                        room.getSid(),
                        participant.getSid());

                addParticipant(participant);

                updateStatsUI(sharedPreferences.getBoolean(Preferences.ENABLE_STATS, false));
            }

            @Override
            public void onParticipantDisconnected(Room room, Participant participant) {
                Timber.i("Participant disconnected -> room sid: %s, participant: %s",
                        room.getSid(),
                        participant.getSid());

                removeParticipant(participant);

                updateStatsUI(sharedPreferences.getBoolean(Preferences.ENABLE_STATS, false));
            }

            @Override
            public void onRecordingStarted(Room room) {
                Timber.i("onRecordingStarted: " + room.getName());
            }

            @Override
            public void onRecordingStopped(Room room) {
                Timber.i("onRecordingStopped: " + room.getName());
            }
        };
    }

    private class ParticipantListener implements Participant.Listener {
        @Override
        public void onAudioTrackAdded(Participant participant, AudioTrack audioTrack) {
            Timber.i("Participant added audio track -> participant: %s, audio: %s, enabled: %b",
                    participant.getSid(),
                    audioTrack.getTrackId(),
                    audioTrack.isEnabled());

            boolean newAudioState = !audioTrack.isEnabled();

            if (participantController.getPrimaryItem().sid.equals(participant.getSid())) {

                // update audio state for primary view
                participantController.getPrimaryItem().muted = newAudioState;
                participantController.getPrimaryView().setMuted(newAudioState);

            } else {

                // update thumbs with audio state
                participantController.updateThumbs(participant.getSid(), newAudioState);
            }
        }

        @Override
        public void onAudioTrackRemoved(Participant participant, AudioTrack audioTrack) {
            Timber.i("Participant removed audio track -> participant: %s, audio: %s, enabled: %b",
                    participant.getSid(),
                    audioTrack.getTrackId(),
                    audioTrack.isEnabled());

            boolean newAudioState = true;

            if (participantController.getPrimaryItem().sid.equals(participant.getSid())) {

                // update audio state for primary view
                participantController.getPrimaryItem().muted = newAudioState;
                participantController.getPrimaryView().setMuted(newAudioState);

            } else {

                // update thumbs with audio state
                participantController.updateThumbs(participant.getSid(), newAudioState);
            }
        }

        @Override
        public void onVideoTrackAdded(Participant participant, VideoTrack videoTrack) {
            Timber.i("Participant added video track -> participant: %s, id: %s, enabled: %b",
                    participant.getSid(),
                    videoTrack.getTrackId(),
                    videoTrack.isEnabled());

            ParticipantController.Item primary = participantController.getPrimaryItem();

            if (primary != null && primary.sid.equals(participant.getSid())) {

                // no thumb needed - render as primary
                primary.videoTrack = videoTrack;
                participantController.renderAsPrimary(primary);
            } else {

                // not a primary participant requires thumb
                participantController.addOrUpdateThumb(participant.getSid(),
                        participant.getIdentity(), null, videoTrack);
            }
        }

        @Override
        public void onVideoTrackRemoved(Participant participant, VideoTrack videoTrack) {
            Timber.i("Participant removed video track -> participant: %s, id: %s, enabled: %b",
                    participant.getSid(),
                    videoTrack.getTrackId(),
                    videoTrack.isEnabled());

            ParticipantController.Item primary = participantController.getPrimaryItem();

            if (primary != null && primary.sid.equals(participant.getSid())) {

                // no thumb needed - render as primary
                primary.videoTrack = null;
                participantController.renderAsPrimary(primary);
            } else {

                // remove thumb or leave empty video thumb
                participantController.removeOrEmptyThumb(participant.getSid(),
                        participant.getIdentity(), videoTrack);
            }
        }

        @Override
        public void onAudioTrackEnabled(Participant participant, AudioTrack audioTrack) {
            Timber.i("Participant enabled audio track -> participant: %s, audio: %s, enabled: %b",
                    participant.getSid(),
                    audioTrack.getTrackId(),
                    audioTrack.isEnabled());

            // TODO: need design
        }

        @Override
        public void onAudioTrackDisabled(Participant participant, AudioTrack audioTrack) {
            Timber.i("Participant disabled audio track -> participant: %s, audio: %s, enabled: %b",
                    participant.getSid(),
                    audioTrack.getTrackId(),
                    audioTrack.isEnabled());

            // TODO: need design
        }

        @Override
        public void onVideoTrackEnabled(Participant participant, VideoTrack videoTrack) {
            Timber.i("Participant enabled video track -> participant: %s, video: %s, enabled: %b",
                    participant.getSid(),
                    videoTrack.getTrackId(),
                    videoTrack.isEnabled());

            // TODO: need design
        }

        @Override
        public void onVideoTrackDisabled(Participant participant, VideoTrack videoTrack) {
            Timber.i("Participant disabled video track -> participant: %s, video: %s, enabled: %b",
                    participant.getSid(),
                    videoTrack.getTrackId(),
                    videoTrack.isEnabled());

            // TODO: need design
        }
    }
}
