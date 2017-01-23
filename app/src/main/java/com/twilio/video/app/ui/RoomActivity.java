package com.twilio.video.app.ui;

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
import android.support.v7.app.AppCompatActivity;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.twilio.accessmanager.AccessManager;
import com.twilio.video.AspectRatio;
import com.twilio.video.AudioTrack;
import com.twilio.video.CameraCapturer;
import com.twilio.video.ConnectOptions;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalMedia;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.Media;
import com.twilio.video.Participant;
import com.twilio.video.Room;
import com.twilio.video.RoomState;
import com.twilio.video.ScreenCapturer;
import com.twilio.video.StatsListener;
import com.twilio.video.StatsReport;
import com.twilio.video.TwilioException;
import com.twilio.video.VideoClient;
import com.twilio.video.VideoConstraints;
import com.twilio.video.VideoDimensions;
import com.twilio.video.VideoTrack;
import com.twilio.video.app.R;
import com.twilio.video.app.adapter.StatsListAdapter;
import com.twilio.video.app.data.Preferences;
import com.twilio.video.app.util.EnvUtil;
import com.twilio.video.app.util.InputUtils;
import com.twilio.video.app.util.StatsScheduler;
import com.twilio.video.env.Env;
import com.twilio.video.simplersignaling.SimplerSignalingUtils;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

import static com.twilio.video.app.R.drawable.ic_phonelink_ring_white_24dp;
import static com.twilio.video.app.R.drawable.ic_volume_up_white_24dp;

public class RoomActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int MEDIA_PROJECTION_REQUEST_CODE = 101;
    private static final int STATS_DELAY = 1000; // milliseconds

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

    private SharedPreferences sharedPreferences;
    private AudioManager audioManager;
    private int savedAudioMode = AudioManager.MODE_INVALID;
    private int savedVolumeControlStream;

    private String username;
    private String env;
    private String topology;
    private AccessManager accessManager;
    private VideoClient videoClient;
    private Room room;
    private LocalMedia localMedia;
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

    /**
     * Coordinates participant thumbs and primary participant rendering.
     */
    private ParticipantController participantController;

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
        username = sharedPreferences.getString(Preferences.IDENTITY, null);
        env = sharedPreferences.getString(Preferences.ENVIRONMENT,
                Preferences.ENVIRONMENT_DEFAULT);
        topology = sharedPreferences.getString(Preferences.TOPOLOGY,
                Preferences.TOPOLOGY_DEFAULT);
        localMedia = LocalMedia.create(this);
        statsScheduler = new StatsScheduler();
        obtainVideoConstraints();
        updateUi(room);
        requestPermissions();
    }

    @Override
    protected void onDestroy() {
        // Reset the speakerphone
        audioManager.setSpeakerphoneOn(false);
        // Teardown local media
        if (localMedia != null) {
            localMedia.removeVideoTrack(cameraVideoTrack);
            localMedia.removeAudioTrack(localAudioTrack);
            localMedia.release();
            localMedia = null;
        }
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

        // try to restore camera video track after setting screen
        if (restoreLocalVideoCameraTrack) {
            obtainVideoConstraints();

//            // add local local camera track
//            cameraVideoTrack = localMedia.addVideoTrack(true, cameraCapturer, videoConstraints);
//
//            // enable video settings
//            switchCameraMenuItem.setVisible(cameraVideoTrack.isEnabled());
//            pauseVideoMenuItem.setTitle(cameraVideoTrack.isEnabled() ?
//                    R.string.pause_video : R.string.resume_video);
//            pauseVideoMenuItem.setVisible(true);
//
//            // local video was rendered as primary view - refreshing
//            participantController.renderAsPrimary("", getString(R.string.you), cameraVideoTrack,
//                    localAudioTrack == null);
//
//            primaryVideoView.setMirror(cameraCapturer.getCameraSource() ==
//                    CameraCapturer.CameraSource.BACK_CAMERA);

            restoreLocalVideoCameraTrack = false;
        }
        updateStats();
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

                /*
                 * Remove video tracks before going to setting screen
                 * and mark track to be restored after settings applied
                 */
                if (cameraVideoTrack != null) {
                    localMedia.removeVideoTrack(cameraVideoTrack);
                    restoreLocalVideoCameraTrack = true;
                    cameraVideoTrack = null;
                }

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
        String roomOrSid = roomEditText.getText().toString();
        obtainTokenAndConnect(roomOrSid);
        InputUtils.hideKeyboard(this);
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
            localAudioTrack = localMedia.addAudioTrack(true);
            icon = R.drawable.ic_mic_white_24px;
            pauseAudioMenuItem.setVisible(true);
            pauseAudioMenuItem.setTitle(localAudioTrack.isEnabled() ?
                    R.string.pause_audio : R.string.resume_audio);
        } else {
            if (!localMedia.removeAudioTrack(localAudioTrack)) {
                Snackbar.make(primaryVideoView,
                        "Audio track remove action failed",
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
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

            // add local local camera track
            cameraVideoTrack = localMedia.addVideoTrack(true, cameraCapturer, videoConstraints);

            // enable video settings
            switchCameraMenuItem.setVisible(cameraVideoTrack.isEnabled());
            pauseVideoMenuItem.setTitle(cameraVideoTrack.isEnabled() ?
                    R.string.pause_video : R.string.resume_video);
            pauseVideoMenuItem.setVisible(true);

        } else {

            // remove local camera track
            cameraVideoTrack.removeRenderer(primaryVideoView);

            // show snack if failed to remove track
            if (!localMedia.removeVideoTrack(cameraVideoTrack)) {
                Snackbar.make(primaryVideoView, "Failed to remove video track",
                        Snackbar.LENGTH_LONG).show();
            }
            cameraVideoTrack = null;

            // disable video settings
            switchCameraMenuItem.setVisible(false);
            pauseVideoMenuItem.setVisible(false);
        }

        if (room != null) {

            LocalParticipant localParticipant = room.getLocalParticipant();

            // update local participant thumb
            participantController.updateThumb(room.getLocalParticipant().getSid(), oldVideo,
                    cameraVideoTrack);

            if (participantController.getPrimaryItem().sid.equals(localParticipant.getSid())) {

                // local video was rendered as primary view - refreshing
                participantController.renderAsPrimary(localParticipant.getSid(), getString(R.string.you),
                        cameraVideoTrack, localAudioTrack == null);
                participantController.getPrimaryView().showIdentityBadge(false);

                // update mirror
                primaryVideoView.setMirror(cameraCapturer.getCameraSource() ==
                        CameraCapturer.CameraSource.BACK_CAMERA);

                // update thumb state
                participantController.updateThumb(localParticipant.getSid(), cameraVideoTrack,
                        ParticipantView.State.SELECTED);
            }

        } else {

            // local video was rendered as primary view - refreshing
            participantController.renderAsPrimary("", getString(R.string.you), cameraVideoTrack,
                    localAudioTrack == null);
            participantController.getPrimaryView().showIdentityBadge(false);

            // update mirror
            primaryVideoView.setMirror(cameraCapturer.getCameraSource() ==
                    CameraCapturer.CameraSource.BACK_CAMERA);
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

    private void setupLocalMedia() {
        localAudioTrack = localMedia.addAudioTrack(true);
        cameraCapturer = new CameraCapturer(this, CameraCapturer.CameraSource.FRONT_CAMERA);
        cameraVideoTrack = localMedia.addVideoTrack(true, cameraCapturer, videoConstraints);
        primaryVideoView.setMirror(true);

        if (cameraVideoTrack != null) {
            localVideoTrackNames.put(cameraVideoTrack.getTrackId(),
                    getString(R.string.camera_video_track));
        } else {
            Snackbar.make(primaryVideoView, R.string.failed_to_add_camera_video_track,
                    Snackbar.LENGTH_LONG).show();
        }

        // TODO: render local with empty sid
        participantController.renderAsPrimary("", getString(R.string.you), cameraVideoTrack,
                localAudioTrack == null);
        participantController.getPrimaryView().showIdentityBadge(false);
    }

    private void updateUi(Room room) {
        int disconnectButtonState = View.GONE;
        int joinRoomLayoutState = View.VISIBLE;
        int joinStatusLayoutState = View.GONE;

        boolean settingsMenuItemState = true;

        boolean connectButtonEnabled = false;

        String roomName = username;
        String toolbarTitle = username;
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
            cameraCapturer.switchCamera();

            if (room != null) {

                if (participantController.getPrimaryItem().sid.equals(room.getLocalParticipant().getSid())) {

                    // local video was rendered as primary view - refreshing
                    primaryVideoView.setMirror(cameraCapturer.getCameraSource() ==
                            CameraCapturer.CameraSource.BACK_CAMERA);

                } else {

                    // local video was rendered as thumb - refreshing thumb state
                    ParticipantView thumb = participantController.getThumb(
                            room.getLocalParticipant().getSid(), null);
                    thumb.setMirror(cameraCapturer.getCameraSource() ==
                            CameraCapturer.CameraSource.BACK_CAMERA);
                }
            } else {

                // local video was rendered as primary view - refreshing
                primaryVideoView.setMirror(cameraCapturer.getCameraSource() ==
                        CameraCapturer.CameraSource.BACK_CAMERA);
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
        screenVideoTrack = localMedia.addVideoTrack(true, screenCapturer);

        if (screenVideoTrack != null) {
            screenCaptureMenuItem.setIcon(R.drawable.ic_stop_screen_share_white_24dp);
            screenCaptureMenuItem.setTitle(R.string.stop_screen_share);
            localVideoTrackNames.put(
                    screenVideoTrack.getTrackId(), getString(R.string.screen_video_track));
        } else {
            Snackbar.make(primaryVideoView,
                    R.string.failed_to_add_screen_video_track,
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void stopScreenCapture() {
        localMedia.removeVideoTrack(screenVideoTrack);
        localVideoTrackNames.remove(screenVideoTrack.getTrackId());
        screenVideoTrack = null;
        screenCaptureMenuItem.setIcon(R.drawable.ic_screen_share_white_24dp);
        screenCaptureMenuItem.setTitle(R.string.share_screen);
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

    private void obtainTokenAndConnect(final String roomName) {
        String currentEnv = sharedPreferences.getString(Preferences.ENVIRONMENT,
                Preferences.ENVIRONMENT_DEFAULT);
        String currentTopology = sharedPreferences.getString(Preferences.TOPOLOGY,
                Preferences.TOPOLOGY_DEFAULT);
        if(env != currentEnv) {
            // Reset the client to ensure that the client is created with the new environment
            videoClient = null;
        }
        if (newTokenNeeded(currentEnv, currentTopology)) {
            Timber.d("Retrieving access token");
            env = currentEnv;
            topology = currentTopology;
            SimplerSignalingUtils.getAccessToken(username, env, topology,
                    new Callback<String>() {
                        @Override
                        public void success(String token, Response response) {
                            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                                Timber.d("Access token retrieved");
                                updateToken(token);
                                connect(roomName);
                            } else {
                                Snackbar.make(primaryVideoView,
                                        "Retrieving access token failed. Status: " +
                                                response.getStatus(),
                                        Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                                updateUi(room);
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Snackbar.make(primaryVideoView,
                                    "Retrieving access token failed. Error: " + error.getMessage(),
                                    Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            updateUi(room);
                        }
                    });
        } else {
            connect(roomName);
        }
    }

    private boolean newTokenNeeded(String currentEnv, String currentTopology) {
        return !env.equals(currentEnv) ||
                !topology.equals(currentTopology) ||
                accessManager == null ||
                accessManager.isTokenExpired();
    }

    private void updateToken(String token) {
        if (accessManager == null) {
            accessManager = new AccessManager(token);
        } else {
            accessManager.updateToken(token);
        }
        if (videoClient == null) {
            String nativeEnvironmentVariableValue = EnvUtil.getNativeEnvironmentVariableValue(env);
            Env.set(this, EnvUtil.TWILIO_ENV_KEY, nativeEnvironmentVariableValue, true);
            videoClient = new VideoClient(this, token);
        } else {
            videoClient.updateToken(token);
        }
    }

    private void connect(String roomName) {
        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .roomName(roomName)
                .localMedia(localMedia)
                .build();

        room = videoClient.connect(connectOptions, roomListener());
        updateUi(room);
    }

    /**
     * Provides participant a listener for media events and add thumb.
     *
     * @param participant newly joined room participant
     */
    private void addParticipant(Participant participant) {
        ParticipantMediaListener listener = new ParticipantMediaListener(participant);
        participant.getMedia().setListener(listener);
        participantController.addThumb(participant.getSid(), participant.getIdentity());
    }

    /**
     * Removes all participant thumbs and push local camera as primary with empty sid.
     */
    private void removeAllParticipants() {
        participantController.removeAllThumbs();
        participantController.removePrimary();

        // TODO: render local with empty sid
        participantController.renderAsPrimary("", getString(R.string.you), cameraVideoTrack,
                localAudioTrack == null);
        participantController.getPrimaryView().showIdentityBadge(false);
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
            LocalParticipant localParticipant = room.getLocalParticipant();
            participantController
                    .getThumb(localParticipant.getSid(), cameraVideoTrack)
                    .callOnClick();
        }

        participantController.removeThumbs(participant.getSid());
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

                    if (old.sid.equals(room.getLocalParticipant().getSid())) {

                        // toggle local participant state
                        int state = old.videoTrack == null ?
                                ParticipantView.State.NO_VIDEO : ParticipantView.State.VIDEO;
                        participantController.updateThumb(old.sid, old.videoTrack, state);

                    } else {

                        // add thumb for remote participant
                        participantController.addThumb(old);
                    }
                }

                // handle new primary participant click
                participantController.renderAsPrimary(item);

                if (item.sid.equals(room.getLocalParticipant().getSid())) {

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

                setAudioFocus(true);
                setVolumeControl(true);
                updateStats();
                updateUi(room);

                // remove primary view
                participantController.removePrimary();

                // add local thumb and "click" on it to make primary
                String localSid = room.getLocalParticipant().getSid();
                participantController.addThumb(localSid, getString(R.string.you), cameraVideoTrack,
                        localAudioTrack == null);
                participantController.getThumb(room.getLocalParticipant().getSid(),
                        cameraVideoTrack).callOnClick();

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
                        twilioException.code,
                        twilioException.message);

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

    private class ParticipantMediaListener implements Media.Listener {

        private final Participant participant;

        ParticipantMediaListener(Participant participant) {
            this.participant = participant;
        }

        @Override
        public void onAudioTrackAdded(Media media, AudioTrack audioTrack) {
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
        public void onAudioTrackRemoved(Media media, AudioTrack audioTrack) {
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
        public void onVideoTrackAdded(Media media, VideoTrack videoTrack) {
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
        public void onVideoTrackRemoved(Media media, VideoTrack videoTrack) {
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
        public void onAudioTrackEnabled(Media media, AudioTrack audioTrack) {
            Timber.i("Participant enabled audio track -> participant: %s, audio: %s, enabled: %b",
                    participant.getSid(),
                    audioTrack.getTrackId(),
                    audioTrack.isEnabled());

            // TODO: need design
        }

        @Override
        public void onAudioTrackDisabled(Media media, AudioTrack audioTrack) {
            Timber.i("Participant disabled audio track -> participant: %s, audio: %s, enabled: %b",
                    participant.getSid(),
                    audioTrack.getTrackId(),
                    audioTrack.isEnabled());

            // TODO: need design
        }

        @Override
        public void onVideoTrackEnabled(Media media, VideoTrack videoTrack) {
            Timber.i("Participant enabled video track -> participant: %s, video: %s, enabled: %b",
                    participant.getSid(),
                    videoTrack.getTrackId(),
                    videoTrack.isEnabled());

            // TODO: need design
        }

        @Override
        public void onVideoTrackDisabled(Media media, VideoTrack videoTrack) {
            Timber.i("Participant disabled video track -> participant: %s, video: %s, enabled: %b",
                    participant.getSid(),
                    videoTrack.getTrackId(),
                    videoTrack.isEnabled());

            // TODO: need design
        }
    }
}
