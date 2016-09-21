package com.tw.video.testapp.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tw.video.testapp.R;
import com.tw.video.testapp.dialog.Dialog;
import com.tw.video.testapp.util.AccessManagerHelper;
import com.tw.video.testapp.util.SimpleSignalingUtils;
import com.twilio.common.AccessManager;
import com.twilio.video.AudioTrack;
import com.twilio.video.CameraCapturer;
import com.twilio.video.ConnectOptions;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalMedia;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.Media;
import com.twilio.video.Participant;
import com.twilio.video.Room;
import com.twilio.video.RoomState;
import com.twilio.video.VideoClient;
import com.twilio.video.VideoException;
import com.twilio.video.VideoTrack;
import com.twilio.video.VideoView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class RoomActivity extends AppCompatActivity {
    @BindView(R.id.connect_fab) FloatingActionButton connectFab;
    @BindView(R.id.media_status_textview) TextView mediaStatusTextview;
    @BindView(R.id.room_status_textview) TextView roomStatusTextview;
    @BindView(R.id.primary_video_view) VideoView primaryVideoView;
    @BindView(R.id.thumbnail_linear_layout) LinearLayout thumbnailLinearLayout;
    @BindView(R.id.local_video_action_fab) FloatingActionButton localVideoActionFab;
    @BindView(R.id.local_video_pause_fab) FloatingActionButton localVideoPauseFab;
    @BindView(R.id.local_audio_action_fab) FloatingActionButton localAudioActionFab;
    @BindView(R.id.local_audio_enable_fab) FloatingActionButton localAudioEnableFab;
    @BindView(R.id.speaker_action_fab) FloatingActionButton speakerActionFab;

    private String username;
    private String capabilityToken;
    private String realm;
    private AccessManager accessManager;
    private VideoClient videoClient;
    private Room room;
    private String roomName;
    private LocalMedia localMedia;
    private LocalAudioTrack localAudioTrack;
    private LocalVideoTrack localVideoTrack;
    private VideoView localVideoView;
    private CameraCapturer cameraCapturer;
    private AlertDialog alertDialog;
    boolean loggingOut;
    private Map<Participant, VideoView> videoViewMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // So calls can be answered when screen is locked
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_room);

        ButterKnife.bind(this);

        processActivityIntent(savedInstanceState);
        updateUI(RoomState.DISCONNECTED);
        loggingOut = false;
        localMedia = LocalMedia.create(this);
        localAudioTrack = localMedia.addAudioTrack(true);
        cameraCapturer = new CameraCapturer(this,
                CameraCapturer.CameraSource.FRONT_CAMERA, null);
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
        primaryVideoView.setMirror(true);
        localVideoTrack.addRenderer(primaryVideoView);
        localVideoView = primaryVideoView;
        createVideoClient();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (primaryVideoView != null) {
            primaryVideoView.release();
            primaryVideoView = null;
        }
        if (thumbnailLinearLayout != null) {
            thumbnailLinearLayout.removeAllViews();
            for (Map.Entry<Participant, VideoView> entry : videoViewMap.entrySet()) {
                entry.getValue().release();
            }
            videoViewMap.clear();
        }
        if (localMedia != null) {
            localMedia.removeVideoTrack(localVideoTrack);
            localMedia.removeAudioTrack(localAudioTrack);
            localMedia.release();
            localMedia = null;
        }
        if (accessManager != null) {
            accessManager.dispose();
            accessManager = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.room_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem switchCameraMenuItem = menu.findItem(R.id.switch_camera_menu_item);
        MenuItem pauseVideoMenuItem = menu.findItem(R.id.pause_video_menu_item);
        if (localVideoTrack != null) {
            switchCameraMenuItem.setVisible(localVideoTrack.isEnabled());
            pauseVideoMenuItem.setTitle(localVideoTrack.isEnabled() ?
                    R.string.pause_video : R.string.resume_video);
        } else {
            switchCameraMenuItem.setVisible(false);
            pauseVideoMenuItem.setVisible(false);
        }
        MenuItem pauseAudioMenuItem = menu.findItem(R.id.pause_audio_menu_item);
        if (localAudioTrack != null) {
            pauseAudioMenuItem.setVisible(true);
            pauseAudioMenuItem.setTitle(localAudioTrack.isEnabled() ?
                    R.string.pause_audio : R.string.resume_audio);
        } else {
            pauseAudioMenuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out_menu_item:
                // Will continue logout once the conversation has ended
                loggingOut = true;
                // End any current call
                if (room != null && room.getState() != RoomState.DISCONNECTED) {
                    room.disconnect();
                } else {
                    returnToVideoClientLogin();
                }
                return true;
            case R.id.switch_camera_menu_item:
                switchCamera();
                return true;
            case R.id.pause_audio_menu_item:
                toggleLocalAudioTrackState();
                return true;
            case R.id.pause_video_menu_item:
                toggleLocalVideoTrackState();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void processActivityIntent(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Timber.d("Restoring client activity state");
            username = savedInstanceState.getString(SimpleSignalingUtils.USERNAME);
            capabilityToken = savedInstanceState.getString(SimpleSignalingUtils.CAPABILITY_TOKEN);
            realm = savedInstanceState.getString(SimpleSignalingUtils.REALM);
            roomName = savedInstanceState.getString(SimpleSignalingUtils.ROOM_NAME);
        } else {
            Bundle extras = getIntent().getExtras();
            username = extras.getString(SimpleSignalingUtils.USERNAME);
            capabilityToken = extras.getString(SimpleSignalingUtils.CAPABILITY_TOKEN);
            realm = extras.getString(SimpleSignalingUtils.REALM);
            roomName = extras.getString(SimpleSignalingUtils.ROOM_NAME);
        }
    }

    private void createVideoClient() {
        accessManager = AccessManagerHelper.createAccessManager(this, capabilityToken);
        videoClient = new VideoClient(this, accessManager);
    }

    private void connectToRoom(String roomName) {
        roomStatusTextview.setText("Connecting to room "+roomName);
        this.roomName = roomName;
        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .roomName(roomName)
                .localMedia(localMedia)
                .build();

        room = videoClient.connect(connectOptions, roomListener());
        updateUI(RoomState.CONNECTING);
    }

    @OnClick(R.id.connect_fab)
    public void connect(View view) {
        if (room != null) {
            Timber.i("Exiting room");
            room.disconnect();
        } else {
            EditText connectEditText = new EditText(this);
            alertDialog = Dialog.createConnectDialog(connectEditText,
                    connectClickListener(connectEditText),
                    cancelRoomClickListener(),
                    this);
            alertDialog.show();
        }
    }

    private void switchCamera() {
        if (cameraCapturer != null) {
            cameraCapturer.switchCamera();
            localVideoView.setMirror(
                    cameraCapturer.getCameraSource() == CameraCapturer.CameraSource.FRONT_CAMERA);
        }
    }

    private void toggleLocalAudioTrackState() {
        if (localAudioTrack != null) {
            boolean enable = !localAudioTrack.isEnabled();
            localAudioTrack.enable(enable);
            invalidateOptionsMenu();
        }
    }

    @OnClick(R.id.local_audio_action_fab)
    public void toggleLocalAudio(View view) {
        int icon = 0;
        if (localAudioTrack == null) {
            localAudioTrack = localMedia.addAudioTrack(true);
            icon = R.drawable.ic_mic_white_24px;
            localAudioEnableFab.show();
        } else {
            if (!localMedia.removeAudioTrack(localAudioTrack)) {
                Snackbar.make(roomStatusTextview,
                        "Audio track remove action failed",
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
            localAudioTrack = null;
            icon = R.drawable.ic_mic_off_gray_24px;
            localAudioEnableFab.hide();
        }
        localAudioActionFab.setImageDrawable(ContextCompat.getDrawable(RoomActivity.this, icon));
        invalidateOptionsMenu();
    }

    public void toggleLocalVideoTrackState() {
        if (localVideoTrack != null) {
            boolean enable = !localVideoTrack.isEnabled();
            localVideoTrack.enable(enable);
            invalidateOptionsMenu();
        }
    }

    @OnClick(R.id.local_video_action_fab)
    public void toggleLocalVideo(View view) {
        int icon = 0;
        if (localVideoTrack == null) {
            localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
            if (room != null && !videoViewMap.isEmpty()) {
                localVideoView = createVideoView();
                localVideoTrack.addRenderer(localVideoView);
                thumbnailLinearLayout.addView(localVideoView);
                thumbnailLinearLayout.setVisibility(View.VISIBLE);
            } else {
                // Set as primary video view
                localVideoView = primaryVideoView;
                localVideoTrack.addRenderer(primaryVideoView);
            }
            localVideoPauseFab.show();
            icon = R.drawable.ic_videocam_white_24px;
        } else {
            if (localVideoView == primaryVideoView) {
                // TODO: do something with primaryVideoView to show empty pic
            } else {
                thumbnailLinearLayout.removeView(localVideoView);
                if (videoViewMap.isEmpty()) {
                    thumbnailLinearLayout.setVisibility(View.GONE);
                }
            }
            localVideoTrack.removeRenderer(localVideoView);
            if (!localMedia.removeVideoTrack(localVideoTrack)) {
                Snackbar.make(roomStatusTextview,
                        "Video track remove action failed",
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
            localVideoTrack = null;
            localVideoView = null;
            localVideoPauseFab.hide();
            icon = R.drawable.ic_videocam_off_gray_24px;

        }
        localVideoActionFab.setImageDrawable(
                ContextCompat.getDrawable(RoomActivity.this, icon));
        invalidateOptionsMenu();
    }

    private void updateUI(RoomState roomState) {
        int joinIcon = 0;
        if (roomState == RoomState.CONNECTING) {
            joinIcon = R.drawable.ic_call_end_white_24px;
        } else if (roomState == RoomState.CONNECTED) {
            getSupportActionBar().setTitle(room.getName());
            joinIcon = R.drawable.ic_call_end_white_24px;
        } else { // disconnected
            getSupportActionBar().setTitle(username);
            joinIcon = R.drawable.ic_add_circle_white_24px;
        }
        connectFab.setImageDrawable(
                ContextCompat.getDrawable(RoomActivity.this, joinIcon));
    }

    private DialogInterface.OnClickListener connectClickListener(final EditText connectEditText) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                connectToRoom(connectEditText.getText().toString());
            }
        };
    }

    private DialogInterface.OnClickListener cancelRoomClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // set proper action
                alertDialog.dismiss();
            }
        };
    }

    private void returnToVideoClientLogin(){
        Intent registrationIntent = new Intent(RoomActivity.this, LoginActivity.class);
        startActivity(registrationIntent);
        finish();
    }

    private VideoView createVideoView() {
        VideoView videoView = new VideoView(this);
        videoView.setMirror(true);
        videoView.setZOrderMediaOverlay(true);
        return videoView;
    }

    private synchronized void addParticipant(Participant participant) {
        if (videoViewMap.size() > 3) {
            Toast.makeText(this, "Do not support more then 3 participants", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        roomStatusTextview.setText("Participant "+participant.getIdentity()+ " joined");
        VideoView videoView = null;
        if (videoViewMap.isEmpty()) {
            // Move local renderer from main view to thumbnail and
            // set participant renderer to main view
            if (localVideoTrack != null) {
                localVideoTrack.removeRenderer(primaryVideoView);
                localVideoView = createVideoView();
                localVideoTrack.addRenderer(localVideoView);
                thumbnailLinearLayout.addView(localVideoView);
                thumbnailLinearLayout.setVisibility(View.VISIBLE);
            }
            videoView = primaryVideoView;
            primaryVideoView.setMirror(false);
        } else {
            videoView = createVideoView();
            thumbnailLinearLayout.addView(videoView);
        }
        videoViewMap.put(participant, videoView);
        List<VideoTrack> videoTracks = participant.getMedia().getVideoTracks();
        if (!videoTracks.isEmpty()) {
            videoTracks.get(0).addRenderer(videoView);
        } else {
            // TODO: set blank picture for the participant on primaryVideoView,
            // because we don't have participant video track yet
        }
        participant.getMedia().setListener(new ParticipantMediaListener(participant));
    }

    private synchronized void removeAllParticipants() {
        thumbnailLinearLayout.removeAllViews();
        videoViewMap.clear();
        // set local view as primary view
        localVideoTrack.removeRenderer(localVideoView);
        primaryVideoView.setMirror(true);
        localVideoTrack.addRenderer(primaryVideoView);
        localVideoView = primaryVideoView;
        thumbnailLinearLayout.setVisibility(View.GONE);
    }

    private synchronized void removeParticipant(Participant participant) {
        roomStatusTextview.setText("Participant "+participant.getIdentity()+ " left.");
        VideoView videoView = videoViewMap.remove(participant);
        if (videoView == null) {
            // TODO: handle error
            return;
        }
        List<VideoTrack> participantVideoTracks = participant.getMedia().getVideoTracks();
        if (!participantVideoTracks.isEmpty()) {
            participantVideoTracks.get(0).removeRenderer(videoView);
        }
        if (videoView == primaryVideoView) {
            if (videoViewMap.size() != 0) {
                // Pick next view from thumbnail layout
                Map.Entry<Participant, VideoView> entry = videoViewMap.entrySet().iterator().next();
                thumbnailLinearLayout.removeView(entry.getValue());
                Participant nextParticipant = entry.getKey();
                participantVideoTracks = nextParticipant.getMedia().getVideoTracks();
                if (!participantVideoTracks.isEmpty()) {
                    participantVideoTracks.get(0).removeRenderer(entry.getValue());
                    participantVideoTracks.get(0).addRenderer(primaryVideoView);
                }
                videoViewMap.put(nextParticipant, primaryVideoView);
            } else {
                if (localVideoTrack != null) {
                    // set local view as primary view
                    localVideoTrack.removeRenderer(localVideoView);
                    primaryVideoView.setMirror(true);
                    localVideoTrack.addRenderer(primaryVideoView);
                    localVideoView = primaryVideoView;
                }
                thumbnailLinearLayout.setVisibility(View.GONE);
            }
        } else {
            thumbnailLinearLayout.removeView(videoView);
        }
        if (videoViewMap.isEmpty()) {
            thumbnailLinearLayout.removeAllViews();
            thumbnailLinearLayout.setVisibility(View.GONE);
        }
    }

    private Room.Listener roomListener() {
        return new Room.Listener() {
            @Override
            public void onConnected(Room room) {
                Timber.i("onConnected: "+room.getName() + " sid:"+
                        room.getSid()+" state:"+room.getState());
                roomStatusTextview.setText("Connected to "+room.getName());
                updateUI(RoomState.CONNECTED);

                for (Map.Entry<String, Participant> entry : room.getParticipants().entrySet()) {
                    addParticipant(entry.getValue());
                }
            }

            @Override
            public void onConnectFailure(Room room, VideoException error) {
                Timber.i("onConnectFailure");
                roomStatusTextview.setText("Failed to connect to "+roomName);
                RoomActivity.this.room = null;
                updateUI(RoomState.DISCONNECTED);
            }

            @Override
            public void onDisconnected(Room room, VideoException error) {
                Timber.i("onDisconnected");
                roomStatusTextview.setText("Disconnected from "+roomName);
                removeAllParticipants();
                updateUI(RoomState.DISCONNECTED);
                RoomActivity.this.room = null;
                if (loggingOut) {
                    returnToVideoClientLogin();
                }
            }

            @Override
            public void onParticipantConnected(Room room, Participant participant) {
                Timber.i("onParticipantConnected: "+participant.getIdentity());
                addParticipant(participant);
            }

            @Override
            public void onParticipantDisconnected(Room room, Participant participant) {
                Timber.i("onParticipantDisconnected "+participant.getIdentity());
                removeParticipant(participant);
            }
        };
    }

    private class ParticipantMediaListener implements Media.Listener {
        private Participant participant;

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
            VideoView videoView = videoViewMap.get(participant);
            videoTrack.addRenderer(videoView);
        }

        @Override
        public void onVideoTrackRemoved(Media media, VideoTrack videoTrack) {
            Timber.i(participant.getIdentity() + ": onVideoTrackRemoved");
            videoTrack.removeRenderer(videoViewMap.get(participant));
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
