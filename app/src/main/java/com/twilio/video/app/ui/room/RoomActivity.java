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

import static com.twilio.video.Room.State.CONNECTED;
import static com.twilio.video.Room.State.DISCONNECTED;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import com.google.android.material.snackbar.Snackbar;
import com.twilio.video.RemoteAudioTrack;
import com.twilio.video.RemoteAudioTrackPublication;
import com.twilio.video.RemoteDataTrack;
import com.twilio.video.RemoteDataTrackPublication;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.Room;
import com.twilio.video.StatsListener;
import com.twilio.video.TwilioException;
import com.twilio.video.app.R;
import com.twilio.video.app.adapter.StatsListAdapter;
import com.twilio.video.app.base.BaseActivity;
import com.twilio.video.app.data.Preferences;
import com.twilio.video.app.ui.room.RoomViewEffect.RequestScreenSharePermission;
import com.twilio.video.app.ui.room.RoomViewEffect.ScreenShareError;
import com.twilio.video.app.ui.room.RoomViewEvent.ConnectToRoom;
import com.twilio.video.app.ui.room.RoomViewEvent.DisconnectFromRoom;
import com.twilio.video.app.ui.room.RoomViewEvent.SetupLocalMedia;
import com.twilio.video.app.ui.room.RoomViewEvent.StartScreenCapture;
import com.twilio.video.app.ui.room.RoomViewEvent.StopScreenCapture;
import com.twilio.video.app.ui.room.RoomViewEvent.TearDownLocalMedia;
import com.twilio.video.app.ui.room.RoomViewEvent.ToggleLocalAudio;
import com.twilio.video.app.ui.room.RoomViewEvent.ToggleSpeakerPhone;
import com.twilio.video.app.ui.settings.SettingsActivity;
import com.twilio.video.app.util.InputUtils;
import com.twilio.video.app.util.StatsScheduler;
import java.util.List;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

public class RoomActivity extends BaseActivity {
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int MEDIA_PROJECTION_REQUEST_CODE = 101;

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

    private StatsScheduler statsScheduler;
    private StatsListAdapter statsListAdapter;

    private String displayName;

    private ParticipantController participantController;

    @Inject SharedPreferences sharedPreferences;

    @Inject RoomManager roomManager;

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

        // setup participant controller
        participantController = new ParticipantController(thumbnailLinearLayout, primaryVideoView);
        participantController.setListener(participantClickListener());

        displayName = sharedPreferences.getString(Preferences.DISPLAY_NAME, "");

        // Setup Activity
        statsScheduler = new StatsScheduler();
        requestPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //        boolean isAppLinkProvided = checkIntentURI();
        //        updateUi(isAppLinkProvided);
        restoreCameraTrack();
        updateStats();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        roomManager.processViewEvent(TearDownLocalMedia.INSTANCE);
    }

    private void bindViewState(RoomViewState viewState) {
        updateUi(viewState);
    }

    private void bindViewEffects(@Nullable RoomViewEffect viewEffect) {
        if (viewEffect != null) {
            if (viewEffect instanceof ScreenShareError) {
                Snackbar.make(primaryVideoView, R.string.screen_capture_error, Snackbar.LENGTH_LONG)
                        .show();
            }
            if (viewEffect instanceof RequestScreenSharePermission) {
                //                requestScreenCapturePermission();
            }
        }
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
    protected void onPause() {
        removeCameraTrack();
        removeAllParticipants();
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
        screenCaptureMenuItem = menu.findItem(R.id.share_screen_menu_item);
        pauseVideoMenuItem = menu.findItem(R.id.pause_video_menu_item);
        pauseAudioMenuItem = menu.findItem(R.id.pause_audio_menu_item);

        // Screen sharing only available on lollipop and up
        screenCaptureMenuItem.setVisible(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);

        roomManager.getViewState().observe(this, this::bindViewState);
        roomManager.getViewEffects().observe(this, this::bindViewEffects);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_camera_menu_item:
                //                switchCamera();
                return true;
            case R.id.speaker_menu_item:
                roomManager.processViewEvent(ToggleSpeakerPhone.INSTANCE);
                return true;
            case R.id.share_screen_menu_item:
                String shareScreen = getString(R.string.share_screen);

                if (item.getTitle().equals(shareScreen)) {
                    roomManager.processViewEvent(StartScreenCapture.INSTANCE);
                } else {
                    roomManager.processViewEvent(StopScreenCapture.INSTANCE);
                }

                return true;
            case R.id.pause_audio_menu_item:
                roomManager.processViewEvent(ToggleLocalAudio.INSTANCE);
                return true;
            case R.id.pause_video_menu_item:
                //                toggleLocalVideoTrackState();
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
            roomManager.processViewEvent(new RoomViewEvent.SetupScreenCapture(data));
            roomManager.processViewEvent(StartScreenCapture.INSTANCE);
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
        InputUtils.hideKeyboard(RoomActivity.this);
        Editable text = roomEditText.getText();
        String roomName = "";
        if (text != null) {
            roomName = text.toString();
        }
        roomManager.processViewEvent(new ConnectToRoom(roomName, displayName));
    }

    @OnClick(R.id.disconnect)
    void disconnectButtonClick() {
        roomManager.processViewEvent(DisconnectFromRoom.INSTANCE);
    }

    @OnClick(R.id.local_audio_image_button)
    void toggleLocalAudio() {
        roomManager.processViewEvent(ToggleLocalAudio.INSTANCE);
    }

    @OnClick(R.id.local_video_image_button)
    void toggleLocalVideo() {
        //
        //        // remember old video reference for updating thumb in room
        //        VideoTrack oldVideo = cameraVideoTrack;
        //
        //        if (cameraVideoTrack == null) {
        //
        //            // add local camera track
        //            cameraVideoTrack =
        //                    LocalVideoTrack.create(
        //                            this,
        //                            true,
        //                            cameraCapturer.getVideoCapturer(),
        //                            videoConstraints,
        //                            CAMERA_TRACK_NAME);
        //            if (localParticipant != null && cameraVideoTrack != null) {
        //                localParticipant.publishTrack(cameraVideoTrack);
        //
        //                // enable video settings
        //                switchCameraMenuItem.setVisible(cameraVideoTrack.isEnabled());
        //                pauseVideoMenuItem.setTitle(
        //                        cameraVideoTrack.isEnabled()
        //                                ? R.string.pause_video
        //                                : R.string.resume_video);
        //                pauseVideoMenuItem.setVisible(true);
        //            }
        //        } else {
        //            // remove local camera track
        //            cameraVideoTrack.removeRenderer(primaryVideoView);
        //
        //            if (localParticipant != null) {
        //                localParticipant.unpublishTrack(cameraVideoTrack);
        //            }
        //            cameraVideoTrack.release();
        //            cameraVideoTrack = null;
        //
        //            // disable video settings
        //            switchCameraMenuItem.setVisible(false);
        //            pauseVideoMenuItem.setVisible(false);
        //        }
        //
        //        if (room != null && room.getState() == Room.State.CONNECTED) {
        //
        //            // update local participant thumb
        //            participantController.updateThumb(localParticipantSid, oldVideo,
        // cameraVideoTrack);
        //
        //            if (participantController.getPrimaryItem().sid.equals(localParticipantSid)) {
        //
        //                // local video was rendered as primary view - refreshing
        //                participantController.renderAsPrimary(
        //                        localParticipantSid,
        //                        getString(R.string.you),
        //                        cameraVideoTrack,
        //                        localAudioTrack == null,
        //                        cameraCapturer.getCameraSource()
        //                                == CameraCapturer.CameraSource.FRONT_CAMERA);
        //
        //                participantController.getPrimaryView().showIdentityBadge(false);
        //
        //                // update thumb state
        //                participantController.updateThumb(
        //                        localParticipantSid, cameraVideoTrack,
        // ParticipantView.State.SELECTED);
        //            }
        //
        //        } else {
        //
        //            renderLocalParticipantStub();
        //        }
        //
        //        // update toggle button icon
        //        localVideoImageButton.setImageResource(
        //                cameraVideoTrack != null
        //                        ? R.drawable.ic_videocam_white_24px
        //                        : R.drawable.ic_videocam_off_gray_24px);
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

    private void setupLocalMedia() {
        roomManager.processViewEvent(new SetupLocalMedia(getVolumeControlStream()));
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

    private void updateUi(RoomViewState viewState) {
        updateUi(viewState, false);
    }

    private void updateUi(RoomViewState viewState, boolean isAppLinkProvided) {
        int disconnectButtonState = View.GONE;
        int joinRoomLayoutState = View.VISIBLE;
        int joinStatusLayoutState = View.GONE;

        boolean settingsMenuItemState = true;

        boolean connectButtonEnabled = isAppLinkProvided;

        String roomName = displayName;
        String toolbarTitle = displayName;
        String joinStatus = "";
        int recordingWarningVisibility = View.GONE;

        ParticipantViewState participantViewState = viewState.getPrimaryParticipant();
        if (participantViewState != null) {
            renderPrimaryParticipant(participantViewState, false);
        }
        if (viewState.isConnecting()) {
            disconnectButtonState = View.VISIBLE;
            joinRoomLayoutState = View.GONE;
            joinStatusLayoutState = View.VISIBLE;
            recordingWarningVisibility = View.VISIBLE;
            settingsMenuItemState = false;
            connectButtonEnabled = false;
            joinStatus = "Joining...";
        }
        Room.State connectionState = viewState.getConnectionState();
        if (connectionState != null) {
            if (connectionState == CONNECTED) {
                Room room = viewState.getRoom();
                if (room != null) {
                    roomName = room.getName();
                }
                disconnectButtonState = View.VISIBLE;
                joinRoomLayoutState = View.GONE;
                joinStatusLayoutState = View.GONE;
                recordingWarningVisibility = View.GONE;
                settingsMenuItemState = false;
                connectButtonEnabled = false;
                toolbarTitle = roomName;
                joinStatus = "";
                setVolumeControl(viewState.getVolumeControl(), viewState.getVolumeControlStream());
            } else if (connectionState == DISCONNECTED) {
                connectButtonEnabled = true;
                removeAllParticipants();
                //                room = null
                //                localParticipant = null
                //                localParticipantSid = LOCAL_PARTICIPANT_STUB_SID
                //                updateStats()
                //                setAudioFocus(false)
            }
        }
        if (viewState.isConnectFailure()) {
            Snackbar.make(
                            primaryVideoView,
                            getString(R.string.room_activity_failed_to_connect_to_room),
                            Snackbar.LENGTH_LONG)
                    .show();
        }
        int audioIconButton = R.drawable.ic_mic_white_24px;
        int pauseAudioMenuItemTitle = R.string.pause_audio;
        if (viewState.isLocalAudioMuted()) {
            audioIconButton = R.drawable.ic_mic_off_gray_24px;
            pauseAudioMenuItemTitle = R.string.resume_audio;
        }
        List<ParticipantViewState> participants = viewState.getParticipants();
        if (participants != null && !participants.isEmpty()) {
            for (ParticipantViewState participant : participants) {
                participantController.addThumb(
                        participant.getSid(),
                        participant.getIdentity(),
                        participant.getVideoTrack(),
                        participant.isMuted(),
                        participant.isMirrored(),
                        false);
            }
        } else {
            participantController.removeAllThumbs();
        }
        //            if (viewState.isSpeakerPhoneMuted()) {
        //                ((MenuItem)
        // findViewById(R.id.speaker_menu_item)).setIcon(ic_volume_up_white_24dp);
        //            } else {
        //                ((MenuItem)
        // findViewById(R.id.speaker_menu_item)).setIcon(ic_phonelink_ring_white_24dp);
        //            }
        //            int icon = R.drawable.ic_screen_share_white_24dp;
        //            int title = R.string.share_screen;
        //            if (viewState.isScreenShared()) {
        //                icon = R.drawable.ic_stop_screen_share_white_24dp;
        //                title = R.string.stop_screen_share;
        //            }
        //            screenCaptureMenuItem.setIcon(icon);
        //            screenCaptureMenuItem.setTitle(title);

        localAudioImageButton.setImageResource(audioIconButton);
        pauseAudioMenuItem.setTitle(pauseAudioMenuItemTitle);
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
        recordingNoticeTextView.setVisibility(recordingWarningVisibility);

        // TODO: Remove when we use a Service to obtainTokenAndConnect to a room
        if (settingsMenuItem != null) {
            settingsMenuItem.setVisible(settingsMenuItemState);
        }
    }

    private void renderPrimaryParticipant(ParticipantViewState viewState) {
        renderPrimaryParticipant(viewState, true);
    }

    private void renderPrimaryParticipant(
            ParticipantViewState viewState, boolean showIdentityBadge) {
        participantController.renderAsPrimary(
                viewState.getSid(),
                viewState.getIdentity(),
                viewState.getVideoTrack(),
                viewState.getAudioTrack() == null,
                viewState.isMuted());
        primaryVideoView.showIdentityBadge(showIdentityBadge);
    }

    //    private void switchCamera() {
    //        if (cameraCapturer != null) {
    //
    //            boolean mirror =
    //                    cameraCapturer.getCameraSource() ==
    // CameraCapturer.CameraSource.BACK_CAMERA;
    //
    //            cameraCapturer.switchCamera();
    //
    //            if (participantController.getPrimaryItem().sid.equals(localParticipantSid)) {
    //                participantController.updatePrimaryThumb(mirror);
    //            } else {
    //                participantController.updateThumb(localParticipantSid, cameraVideoTrack,
    // mirror);
    //            }
    //        }
    //    }
    //
    //
    //
    //
    //    @TargetApi(21)
    //    private void requestScreenCapturePermission() {
    //        Timber.d("Requesting permission to capture screen");
    //        MediaProjectionManager mediaProjectionManager =
    //                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    //
    //        // This initiates a prompt dialog for the user to confirm screen projection.
    //        startActivityForResult(
    //                mediaProjectionManager.createScreenCaptureIntent(),
    // MEDIA_PROJECTION_REQUEST_CODE);
    //    }
    //
    //    private void toggleLocalAudioTrackState() {
    //        if (localAudioTrack != null) {
    //            boolean enable = !localAudioTrack.isEnabled();
    //            localAudioTrack.enable(enable);
    //            pauseAudioMenuItem.setTitle(
    //                    localAudioTrack.isEnabled() ? R.string.pause_audio :
    // R.string.resume_audio);
    //        }
    //    }
    //
    //    private void toggleLocalVideoTrackState() {
    //        if (cameraVideoTrack != null) {
    //            boolean enable = !cameraVideoTrack.isEnabled();
    //            cameraVideoTrack.enable(enable);
    //            pauseVideoMenuItem.setTitle(
    //                    cameraVideoTrack.isEnabled() ? R.string.pause_video :
    // R.string.resume_video);
    //        }
    //    }

    private void setVolumeControl(boolean setVolumeControl, int volumeControlStream) {
        if (setVolumeControl) {
            /*
             * Enable changing the volume using the up/down keys during a conversation
             */
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        } else {
            setVolumeControlStream(volumeControlStream);
        }
    }

    /**
     * Provides remoteParticipant a listener for media events and add thumb.
     *
     * @param remoteParticipant newly joined room remoteParticipant
     */
    private void addParticipant(RemoteParticipant remoteParticipant, boolean renderAsPrimary) {
        ParticipantListener listener = new ParticipantListener();
        remoteParticipant.setListener(listener);
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
            addParticipantVideoTrack(
                    remoteParticipant.getSid(),
                    remoteParticipant.getIdentity(),
                    null,
                    muted,
                    renderAsPrimary);
        } else {
            for (RemoteVideoTrackPublication remoteVideoTrackPublication :
                    remoteVideoTrackPublications) {
                addParticipantVideoTrack(
                        remoteParticipant.getSid(),
                        remoteParticipant.getIdentity(),
                        remoteVideoTrackPublication.getRemoteVideoTrack(),
                        muted,
                        renderAsPrimary);
                renderAsPrimary = false;
            }
        }
    }

    private void addParticipantVideoTrack(
            String participantSid,
            String participantIdentity,
            RemoteVideoTrack remoteVideoTrack,
            boolean muted,
            boolean renderAsPrimary) {
        if (renderAsPrimary) {
            renderItemAsPrimary(
                    new ParticipantController.Item(
                            participantSid, participantIdentity, remoteVideoTrack, muted, false));
        } else {
            participantController.addThumb(
                    participantSid, participantIdentity, remoteVideoTrack, muted, false, false);
        }
    }

    /**
     * Sets new item to render as primary view and moves existing primary view item to thumbs view.
     *
     * @param item New item to be rendered in primary view
     */
    private void renderItemAsPrimary(ParticipantController.Item item) {
        // nothing to click while not in room
        //        if (room == null) return;
        //
        //        // no need to renderer if same item clicked
        //        ParticipantController.Item old = participantController.getPrimaryItem();
        //        if (old != null && item.sid.equals(old.sid) && item.videoTrack == old.videoTrack)
        // return;
        //
        //        // add back old participant to thumbs
        //        if (old != null) {
        //
        //            if (old.sid.equals(localParticipantSid)) {
        //
        //                // toggle local participant state
        //                int state =
        //                        old.videoTrack == null
        //                                ? ParticipantView.State.NO_VIDEO
        //                                : ParticipantView.State.VIDEO;
        //                participantController.updateThumb(old.sid, old.videoTrack, state);
        //                participantController.updateThumb(old.sid, old.videoTrack, old.mirror);
        //
        //            } else {
        //
        //                // add thumb for remote participant
        //                participantController.addThumb(old);
        //            }
        //        }
        //
        //        // handle new primary participant click
        //        participantController.renderAsPrimary(item);
        //
        //        if (item.sid.equals(localParticipantSid)) {
        //
        //            // toggle local participant state and hide his badge
        //            participantController.updateThumb(
        //                    item.sid, item.videoTrack, ParticipantView.State.SELECTED);
        //            participantController.getPrimaryView().showIdentityBadge(false);
        //        } else {
        //
        //            // remove remote participant thumb
        //            participantController.removeThumb(item);
        //        }
    }

    /** Removes all participant thumbs and push local camera as primary with empty sid. */
    private void removeAllParticipants() {
        //        if (room != null) {
        //            participantController.removeAllThumbs();
        //            participantController.removePrimary();
        //
        //            renderLocalParticipantStub();
        //        }
    }

    /**
     * Remove single remoteParticipant thumbs and all it associated thumbs. If rendered as primary
     * remoteParticipant, primary view switches to local video track.
     *
     * @param remoteParticipant recently disconnected remoteParticipant.¬
     */
    private void removeParticipant(RemoteParticipant remoteParticipant) {

        //        if (participantController.getPrimaryItem().sid.equals(remoteParticipant.getSid()))
        // {
        //
        //            // render local video if primary remoteParticipant has gone
        //            participantController.getThumb(localParticipantSid,
        // cameraVideoTrack).callOnClick();
        //        }
        //
        //        participantController.removeThumbs(remoteParticipant.getSid());
    }

    /**
     * Remove the video track and mark the track to be restored when going to the settings screen or
     * going to the background
     */
    private void removeCameraTrack() {
        //        if (cameraVideoTrack != null) {
        //            if (localParticipant != null) {
        //                localParticipant.unpublishTrack(cameraVideoTrack);
        //            }
        //            cameraVideoTrack.release();
        //            restoreLocalVideoCameraTrack = true;
        //            cameraVideoTrack = null;
        //        }
    }

    /** Try to restore camera video track after going to the settings screen or background */
    private void restoreCameraTrack() {
        //        if (restoreLocalVideoCameraTrack) {
        //            obtainVideoConstraints();
        //            setupLocalVideoTrack();
        //            renderLocalParticipantStub();
        //            restoreLocalVideoCameraTrack = false;
        //        }
    }

    private void updateStatsUI(boolean enabled) {
        //        if (enabled) {
        //            if (room != null && room.getRemoteParticipants().size() > 0) {
        //                // show stats
        //                statsRecyclerView.setVisibility(View.VISIBLE);
        //                statsDisabledLayout.setVisibility(View.GONE);
        //            } else if (room != null) {
        //                // disable stats when there is no room
        //                statsDisabledTitleTextView.setText(getString(R.string.stats_unavailable));
        //                statsDisabledDescTextView.setText(
        //                        getString(R.string.stats_description_media_not_shared));
        //                statsRecyclerView.setVisibility(View.GONE);
        //                statsDisabledLayout.setVisibility(View.VISIBLE);
        //            } else {
        //                // disable stats if there is room but no participants (no media)
        //                statsDisabledTitleTextView.setText(getString(R.string.stats_unavailable));
        //
        // statsDisabledDescTextView.setText(getString(R.string.stats_description_join_room));
        //                statsRecyclerView.setVisibility(View.GONE);
        //                statsDisabledLayout.setVisibility(View.VISIBLE);
        //            }
        //        } else {
        //
        // statsDisabledTitleTextView.setText(getString(R.string.stats_gathering_disabled));
        //
        // statsDisabledDescTextView.setText(getString(R.string.stats_enable_in_settings));
        //            statsRecyclerView.setVisibility(View.GONE);
        //            statsDisabledLayout.setVisibility(View.VISIBLE);
        //        }
    }

    private void updateStats() {
        //        if (statsScheduler.isRunning()) {
        //            statsScheduler.cancelStatsGathering();
        //        }
        //        boolean enableStats = sharedPreferences.getBoolean(Preferences.ENABLE_STATS,
        // false);
        //        if (enableStats && (room != null) && (room.getState() == Room.State.CONNECTED)) {
        //            statsScheduler.scheduleStatsGathering(room, statsListener(), STATS_DELAY);
        //        }
        //        updateStatsUI(enableStats);
    }

    private StatsListener statsListener() {
        return statsReports -> {
            // Running on StatsScheduler thread
            //            if (room != null) {
            //                statsListAdapter.updateStatsData(
            //                        statsReports, room.getRemoteParticipants(),
            // localVideoTrackNames);
            //            }
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

    private class ParticipantListener implements RemoteParticipant.Listener {
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
}
