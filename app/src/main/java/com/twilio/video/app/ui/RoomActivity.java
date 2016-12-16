package com.twilio.video.app.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.twilio.accessmanager.AccessManager;
import com.twilio.video.AspectRatio;
import com.twilio.video.AudioTrack;
import com.twilio.video.CameraCapturer;
import com.twilio.video.ConnectOptions;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalMedia;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.Media;
import com.twilio.video.Participant;
import com.twilio.video.Room;
import com.twilio.video.ScreenCapturer;
import com.twilio.video.TwilioException;
import com.twilio.video.VideoClient;
import com.twilio.video.VideoConstraints;
import com.twilio.video.VideoDimensions;
import com.twilio.video.VideoTrack;
import com.twilio.video.VideoView;
import com.twilio.video.app.R;
import com.twilio.video.app.data.Preferences;
import com.twilio.video.app.util.EnvUtil;
import com.twilio.video.app.util.InputUtils;
import com.twilio.video.app.util.SimplerSignalingUtils;
import com.twilio.video.env.Env;

import java.net.HttpURLConnection;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class RoomActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int MEDIA_PROJECTION_REQUEST_CODE = 101;
    private static final int THUMBNAIL_DIMENSION = 96;

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
    @BindView(R.id.media_status_textview) TextView mediaStatusTextview;
    @BindView(R.id.room_status_textview) TextView roomStatusTextview;
    @BindView(R.id.primary_video) VideoView primaryVideoView;
    @BindView(R.id.video_thumbnails_container) RelativeLayout videoThumbnailRelativeLayout;
    @BindView(R.id.local_video_thumbnail) VideoView localThumbnailVideoView;
    @BindView(R.id.remote_video_thumbnails) LinearLayout thumbnailLinearLayout;
    @BindView(R.id.local_video_image_button) ImageButton localVideoImageButton;
    @BindView(R.id.local_audio_image_button) ImageButton localAudioImageButton;
    @BindView(R.id.video_container) FrameLayout frameLayout;

    @BindView(R.id.join_room_layout) LinearLayout joinRoomLayout;
    @BindView(R.id.room_edit_text) ClearableEditText roomEditText;

    @BindView(R.id.join_status_layout) LinearLayout joinStatusLayout;
    @BindView(R.id.join_status) TextView joinStatusTextView;
    @BindView(R.id.join_room_name) TextView joinRoomNameTextView;

    private MenuItem switchCameraMenuItem;
    private MenuItem pauseVideoMenuItem;
    private MenuItem pauseAudioMenuItem;
    private MenuItem screenCaptureMenuItem;
    private MenuItem settingsMenuItem;
    private MenuItem speakerMenuItem;

    private SharedPreferences sharedPreferences;

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
    private VideoTrack primaryVideoTrack;
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

    private final Multimap<Participant, VideoView> participantVideoViewMultimap =
            HashMultimap.create();
    private final BiMap<VideoTrack, VideoView> videoTrackVideoViewBiMap = HashBiMap.create();

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

        // Setup activity
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        username = sharedPreferences.getString(Preferences.IDENTITY, null);
        env = sharedPreferences.getString(Preferences.ENVIRONMENT,
                Preferences.ENVIRONMENT_DEFAULT);
        topology = sharedPreferences.getString(Preferences.TOPOLOGY,
                Preferences.TOPOLOGY_DEFAULT);
        localMedia = LocalMedia.create(this);
        obtainVideoConstraints();
        updateUi(room);
        requestPermissions();
    }

    @Override
    protected void onDestroy() {
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

            cameraVideoTrack = localMedia.addVideoTrack(true, cameraCapturer, videoConstraints);
            if (cameraVideoTrack != null) {
                localThumbnailVideoView.setMirror(cameraCapturer.getCameraSource() ==
                        CameraCapturer.CameraSource.FRONT_CAMERA);
                if (room != null && !videoTrackVideoViewBiMap.isEmpty()) {
                    cameraVideoTrack.addRenderer(localThumbnailVideoView);
                } else {
                    cameraVideoTrack.addRenderer(primaryVideoView);
                }
            } else {
                Snackbar.make(primaryVideoView, R.string.failed_to_add_camera_video_track,
                        Snackbar.LENGTH_SHORT).show();
            }
            restoreLocalVideoCameraTrack = false;
        }
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
        speakerMenuItem = menu.findItem(R.id.share_screen_menu_item);

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
                Snackbar.make(roomStatusTextview,
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
        int icon = 0;
        if (cameraVideoTrack == null) {
            // Add back local video from camera capturer
            Timber.d("Adding local video");
            cameraVideoTrack = localMedia.addVideoTrack(true, cameraCapturer, videoConstraints);

            if (cameraVideoTrack != null) {
                // If participants have video tracks we render in thumbnial
                if (room != null && !videoTrackVideoViewBiMap.isEmpty()) {
                    Timber.d("Participant video tracks are being rendered. Rendering local video in " +
                            "thumbnail");
                    localThumbnailVideoView.setMirror(cameraCapturer.getCameraSource() ==
                            CameraCapturer.CameraSource.FRONT_CAMERA);
                    cameraVideoTrack.addRenderer(localThumbnailVideoView);
                    localThumbnailVideoView.setVisibility(View.VISIBLE);
                } else {
                    // No remote tracks are being rendered so we render in primary view
                    Timber.d("No remote video is being rendered. Rendering local video in primary " +
                            "view");
                    primaryVideoView.setVisibility(View.VISIBLE);
                    cameraVideoTrack.addRenderer(primaryVideoView);
                }
                // Set and icon and menu items
                icon = R.drawable.ic_videocam_white_24px;
                switchCameraMenuItem.setVisible(cameraVideoTrack.isEnabled());
                pauseVideoMenuItem.setTitle(cameraVideoTrack.isEnabled() ?
                        R.string.pause_video : R.string.resume_video);
                pauseVideoMenuItem.setVisible(true);
            } else {
                Snackbar.make(primaryVideoView,
                        R.string.failed_to_add_camera_video_track,
                        Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        } else {
            Timber.d("Removing local video");
            if (primaryVideoTrack == null) {
                // TODO: Add UI for no video state in primary view
                primaryVideoView.setVisibility(View.GONE);
            } else {
                // TODO: Add UI for no video in thumbnail view
                localThumbnailVideoView.setVisibility(View.GONE);
            }

            // Remove renderer and track
            cameraVideoTrack.removeRenderer(localThumbnailVideoView);
            if (!localMedia.removeVideoTrack(cameraVideoTrack)) {
                Snackbar.make(roomStatusTextview,
                        "Video track remove action failed",
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }

            // Cleanup and set menu items accordingly
            cameraVideoTrack = null;
            icon = R.drawable.ic_videocam_off_gray_24px;
            switchCameraMenuItem.setVisible(false);
            pauseVideoMenuItem.setVisible(false);
        }
        localVideoImageButton.setImageResource(icon);
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

    private boolean permissionsGranted(){
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
            cameraVideoTrack.addRenderer(primaryVideoView);
        } else {
            Snackbar.make(primaryVideoView,
                    R.string.failed_to_add_camera_video_track,
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
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
            Timber.d("Switching camera");
            cameraCapturer.switchCamera();
            localThumbnailVideoView.setMirror(cameraCapturer.getCameraSource() ==
                    CameraCapturer.CameraSource.FRONT_CAMERA);
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
        } else {
            Snackbar.make(primaryVideoView,
                    R.string.failed_to_add_screen_video_track,
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void stopScreenCapture() {
        localMedia.removeVideoTrack(screenVideoTrack);
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

    private void addParticipant(Participant participant) {
        // Set listener
        participant.getMedia().setListener(new ParticipantMediaListener(participant));

        // Render each participant video track
        for (VideoTrack videoTrack : participant.getMedia().getVideoTracks()) {
            VideoView videoView = addParticipantVideo(videoTrack);

            // Maintain a relationship between a participant and its main rendered videos
            participantVideoViewMultimap.put(participant, videoView);
        }
    }

    private VideoView addParticipantVideo(VideoTrack videoTrack) {
        if (primaryVideoTrack == null) {
            Timber.d("Rendering participant video in primary view");
            moveLocalVideoToThumbnail();
            primaryVideoView.setMirror(false);
            primaryVideoView.setVisibility(View.VISIBLE);
            videoTrack.addRenderer(primaryVideoView);
            videoTrackVideoViewBiMap.put(videoTrack, primaryVideoView);
            primaryVideoTrack = videoTrack;

            return primaryVideoView;
        } else {
            Timber.d("Rendering participant video in thumbnail view");
            VideoView videoView = new VideoView(this);
            videoView.setMirror(false);
            videoView.applyZOrder(true);
            thumbnailLinearLayout.addView(videoView);
            videoView.getLayoutParams().width = (int)
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                            THUMBNAIL_DIMENSION, getResources().getDisplayMetrics());
            videoView.getLayoutParams().height = (int)
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                            THUMBNAIL_DIMENSION, getResources().getDisplayMetrics());
            videoTrack.addRenderer(videoView);
            videoTrackVideoViewBiMap.put(videoTrack, videoView);
            videoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Timber.d("Swapping clicked video with primary view");
                    VideoView clickedVideoView = (VideoView) v;
                    VideoTrack clickedVideoTrack = videoTrackVideoViewBiMap
                            .inverse().get(clickedVideoView);

                    // Swap track renderers
                    clickedVideoTrack.removeRenderer(clickedVideoView);
                    primaryVideoTrack.removeRenderer(primaryVideoView);
                    clickedVideoTrack.addRenderer(primaryVideoView);
                    primaryVideoTrack.addRenderer(clickedVideoView);

                    // Update bimap
                    videoTrackVideoViewBiMap.forcePut(clickedVideoTrack, primaryVideoView);
                    videoTrackVideoViewBiMap.forcePut(primaryVideoTrack, clickedVideoView);

                    // Swap references
                    primaryVideoTrack = clickedVideoTrack;
                }
            });

            return videoView;
        }
    }

    private void removeAllParticipants() {
        Timber.d("Cleaning out all participants");
        thumbnailLinearLayout.removeAllViews();
        videoTrackVideoViewBiMap.clear();
        participantVideoViewMultimap.clear();
        primaryVideoTrack = null;
        moveLocalVideoToPrimary();
    }

    private void removeParticipant(Participant participant) {
        roomStatusTextview.setText("Participant " + participant.getIdentity() + " left.");
        for (VideoView videoView : participantVideoViewMultimap.removeAll(participant)) {
            VideoTrack videoTrack = videoTrackVideoViewBiMap.inverse().get(videoView);
            if (videoTrack != null) {
                removeParticipantVideo(videoTrackVideoViewBiMap.inverse().get(videoView));
            }
        }
    }

    private void removeParticipantVideo(VideoTrack videoTrack) {
        if (videoTrack == primaryVideoTrack) {
            Timber.d("Removing participant video from primary view");
            primaryVideoTrack.removeRenderer(primaryVideoView);
            videoTrackVideoViewBiMap.remove(videoTrack);
            ViewGroup remoteVideoThumbnails = thumbnailLinearLayout;
            VideoView videoView = (VideoView) remoteVideoThumbnails.getChildAt(0);

            if (videoView != null) {
                Timber.d("Moving first remote thumbnail to primary view");
                VideoTrack newPrimaryTrack = videoTrackVideoViewBiMap.inverse().get(videoView);
                newPrimaryTrack.removeRenderer(videoView);
                newPrimaryTrack.addRenderer(primaryVideoView);
                videoTrackVideoViewBiMap.forcePut(newPrimaryTrack, primaryVideoView);
                primaryVideoTrack = newPrimaryTrack;
                remoteVideoThumbnails.removeView(videoView);
            } else {
                Timber.d("No remote thumbnail found.");
                moveLocalVideoToPrimary();
                primaryVideoTrack = null;
            }
        } else {
            Timber.d("Removing participant video from thumbnail");
            VideoView videoView = videoTrackVideoViewBiMap.remove(videoTrack);
            thumbnailLinearLayout.removeView(videoView);
        }
    }

    private void moveLocalVideoToThumbnail() {

        if (cameraVideoTrack != null) {
            boolean renderingToThumbnail = cameraVideoTrack.getRenderers().get(0) ==
                    localThumbnailVideoView;
            if (!renderingToThumbnail) {
                Timber.d("Moving camera video to thumbnail");
                cameraVideoTrack.removeRenderer(primaryVideoView);
                localThumbnailVideoView.setMirror(cameraCapturer.getCameraSource() ==
                        CameraCapturer.CameraSource.FRONT_CAMERA);
                videoThumbnailRelativeLayout.setVisibility(View.VISIBLE);
                localThumbnailVideoView.setVisibility(View.VISIBLE);
                cameraVideoTrack.addRenderer(localThumbnailVideoView);
            }
        } else {
            // TODO: Create thumbnail with name and icon in place of video
        }
    }

    private void moveLocalVideoToPrimary() {
        if(cameraVideoTrack != null) {
            boolean renderingToPrimary = cameraVideoTrack.getRenderers().get(0) == primaryVideoView;
            if (!renderingToPrimary) {
                Timber.d("Moving camera video to primary view");
                cameraVideoTrack.removeRenderer(localThumbnailVideoView);
                primaryVideoView.setVisibility(View.VISIBLE);
                cameraVideoTrack.addRenderer(primaryVideoView);
                primaryVideoView.setMirror(cameraCapturer.getCameraSource() ==
                        CameraCapturer.CameraSource.FRONT_CAMERA);
            }
        } else {
            // TODO: Show icon and name in place of video
            primaryVideoView.setVisibility(View.GONE);
        }
        localThumbnailVideoView.setVisibility(View.GONE);
    }

    private Room.Listener roomListener() {
        return new Room.Listener() {
            @Override
            public void onConnected(Room room) {
                Timber.i("onConnected: " + room.getName() + " sid:" +
                        room.getSid() + " state:" + room.getState());
                roomStatusTextview.setText("Connected to " + room.getName());
                updateUi(room);

                for (Map.Entry<String, Participant> entry : room.getParticipants().entrySet()) {
                    addParticipant(entry.getValue());
                }
            }

            @Override
            public void onConnectFailure(Room room, TwilioException twilioException) {
                Timber.i("onConnectFailure: " + twilioException.getMessage());
                roomStatusTextview.setText("Failed to connect to " + room.getName());
                RoomActivity.this.room = null;
                updateUi(room);            }

            @Override
            public void onDisconnected(Room room, TwilioException twilioException) {
                Timber.i("onDisconnected");
                roomStatusTextview.setText("Disconnected from " + room.getName());
                removeAllParticipants();
                updateUi(room);                RoomActivity.this.room = null;
            }

            @Override
            public void onParticipantConnected(Room room, Participant participant) {
                Timber.i("onParticipantConnected: " + participant.getIdentity());
                addParticipant(participant);
            }

            @Override
            public void onParticipantDisconnected(Room room, Participant participant) {
                Timber.i("onParticipantDisconnected " + participant.getIdentity());
                removeParticipant(participant);
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
            Timber.i("onAudioTrackAdded");
            mediaStatusTextview.setText(participant.getIdentity() + ": onAudioTrackAdded");
        }

        @Override
        public void onAudioTrackRemoved(Media media, AudioTrack audioTrack) {
            Timber.i(participant.getIdentity() + ": onAudioTrackRemoved for ");
        }

        @Override
        public void onVideoTrackAdded(Media media, VideoTrack videoTrack) {
            Timber.i(participant.getIdentity() + ": onVideoTrackAdded");
            participantVideoViewMultimap.put(participant, addParticipantVideo(videoTrack));
        }

        @Override
        public void onVideoTrackRemoved(Media media, VideoTrack videoTrack) {
            Timber.i(participant.getIdentity() + ": onVideoTrackRemoved");
            removeParticipantVideo(videoTrack);
        }

        @Override
        public void onAudioTrackEnabled(Media media, AudioTrack audioTrack) {
            Timber.i(participant.getIdentity() + ": onAudioTrackEnabled");
        }

        @Override
        public void onAudioTrackDisabled(Media media, AudioTrack audioTrack) {
            Timber.i(participant.getIdentity() + ": onAudioTrackDisabled");
        }

        @Override
        public void onVideoTrackEnabled(Media media, VideoTrack videoTrack) {
            Timber.i(participant.getIdentity() + ": onVideoTrackEnabled");
        }

        @Override
        public void onVideoTrackDisabled(Media media, VideoTrack videoTrack) {
            Timber.i(participant.getIdentity() + ": onVideoTrackDisabled");
        }
    }
}
