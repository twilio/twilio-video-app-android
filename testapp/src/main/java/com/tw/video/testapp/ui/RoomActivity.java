package com.tw.video.testapp.ui;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tw.video.testapp.R;
import com.tw.video.testapp.util.AccessManagerHelper;
import com.tw.video.testapp.util.SimpleSignalingUtils;
import com.twilio.common.AccessManager;
import com.twilio.video.AudioOptions;
import com.twilio.video.AudioTrack;
import com.twilio.video.CameraCapturer;
import com.twilio.video.ConnectOptions;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalMedia;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.Media;
import com.twilio.video.Participant;
import com.twilio.video.Room;
import com.twilio.video.VideoClient;
import com.twilio.video.VideoException;
import com.twilio.video.VideoRenderer;
import com.twilio.video.VideoScaleType;
import com.twilio.video.VideoTrack;
import com.twilio.video.VideoView;
import com.twilio.video.VideoViewRenderer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class RoomActivity extends AppCompatActivity {
    @BindView(R.id.exit_room_fab) FloatingActionButton exitRoomFab;
    @BindView(R.id.media_status_textview) TextView mediaStatusTextview;
    @BindView(R.id.room_status_textview) TextView roomStatusTextview;
    @BindView(R.id.primary_video_view) VideoView primaryVideoView;
    @BindView(R.id.thumbnail_video_view) VideoView thumbnailVideoView;
    @BindView(R.id.switch_camera_action_fab) FloatingActionButton switchCameraActionFab;
    @BindView(R.id.local_video_action_fab) FloatingActionButton localVideoActionFab;
    @BindView(R.id.local_video_pause_fab) FloatingActionButton localVideoPauseFab;
    @BindView(R.id.local_audio_action_fab) FloatingActionButton localAudioActionFab;
    @BindView(R.id.local_audio_enable_fab) FloatingActionButton localAudioMuteFab;
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
    private CameraCapturer cameraCapturer;

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
        localMedia = LocalMedia.create(this);
        localAudioTrack = localMedia.addAudioTrack(true);
        cameraCapturer = CameraCapturer.create(this,
                CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA, null);
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
        primaryVideoView.setMirror(true);
        localVideoTrack.addRenderer(primaryVideoView);
        createVideoClient();
        connectToRoom();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (primaryVideoView != null) {
            primaryVideoView.release();
            primaryVideoView = null;
        }
        if (thumbnailVideoView != null) {
            thumbnailVideoView.release();
            thumbnailVideoView = null;
        }
        if (localMedia != null) {
            localMedia.removeLocalVideoTrack(localVideoTrack);
            localMedia.removeAudioTrack(localAudioTrack);
            localMedia.release();
            localMedia = null;
        }
        if (accessManager != null) {
            accessManager.dispose();
            accessManager = null;
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

    private void connectToRoom() {
        roomStatusTextview.setText("Connecting to room...");
        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .name(roomName)
                .localMedia(localMedia)
                .build();

        room = videoClient.connect(connectOptions, createRoomListener());
    }

    @OnClick(R.id.exit_room_fab)
    public void exitRoom(View view) {
        if (room != null) {
            Timber.i("Exiting room");
            room.disconnect();
        } else {
            returnToVideoClientLogin();
        }
    }

    @OnClick(R.id.switch_camera_action_fab)
    public void switchCamera(View view) {
        if (cameraCapturer != null) {
            cameraCapturer.switchCamera();
        }
    }

    @OnClick(R.id.local_audio_enable_fab)
    public void enableLocalAudio(View view) {
        if (localAudioTrack != null) {
            boolean enable = !localAudioTrack.isEnabled();
            if (!localAudioTrack.enable(enable)) {
                Snackbar.make(roomStatusTextview,
                        "Audio track "+ (enable  ? "enable" : "diable") + " action failed",
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
                return;
            }
            int icon = enable ? R.drawable.ic_mic_green_24px : R.drawable.ic_mic_red_24px;
            localAudioMuteFab.setImageDrawable(ContextCompat.getDrawable(RoomActivity.this, icon));
        }
    }

    @OnClick(R.id.local_audio_action_fab)
    public void toggleLocalAudio(View view) {
        int icon = 0;
        if (localAudioTrack == null) {
            localAudioTrack = localMedia.addAudioTrack(true);
            icon = R.drawable.ic_mic_white_24px;
        } else {
            localMedia.removeAudioTrack(localAudioTrack);
            localAudioTrack = null;
            icon = R.drawable.ic_mic_off_gray_24px;
        }
        localAudioActionFab.setImageDrawable(ContextCompat.getDrawable(RoomActivity.this, icon));
    }

    @OnClick(R.id.local_video_pause_fab)
    public void pauseVideo(View view) {
        if (localVideoTrack != null) {
            boolean enable = !localVideoTrack.isEnabled();
            if (!localVideoTrack.enable(enable)) {
                Snackbar.make(roomStatusTextview,
                        "Video track "+ (enable  ? "enable" : "diable") + " action failed",
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
                return;
            }
            int icon = enable ? R.drawable.ic_pause_green_24px : R.drawable.ic_pause_red_24px;
            localVideoPauseFab.setImageDrawable(
                    ContextCompat.getDrawable(RoomActivity.this, icon));
        }
    }

    @OnClick(R.id.local_video_action_fab)
    public void toggleLocalVideo(View view) {
        int icon = 0;
        if (localVideoTrack == null) {
            localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
            if (room != null && room.getParticipants().size() > 0) {
                thumbnailVideoView.setVisibility(View.VISIBLE);
                localVideoTrack.addRenderer(thumbnailVideoView);
            } else {
                localVideoTrack.addRenderer(primaryVideoView);
            }
            icon = R.drawable.ic_videocam_white_24px;
        } else {
            localMedia.removeLocalVideoTrack(localVideoTrack);
            localVideoTrack = null;
            thumbnailVideoView.setVisibility(View.INVISIBLE);
            if (room != null && room.getParticipants().size() == 0) {
                // TODO: do something with primaryVideoView to show empty pic
            }
            icon = R.drawable.ic_videocam_off_gray_24px;
        }
        localVideoActionFab.setImageDrawable(
                ContextCompat.getDrawable(RoomActivity.this, icon));
    }

    private void returnToVideoClientLogin(){
        Intent registrationIntent = new Intent(RoomActivity.this, VideoClientLoginActivity.class);
        startActivity(registrationIntent);
        finish();
    }

    private Room.Listener createRoomListener() {
        return new Room.Listener() {
            @Override
            public void onConnected(Room room) {
                Timber.i("onConnected: "+room.getName() + " sid:"+
                        room.getSid()+" state:"+room.getState());
                roomStatusTextview.setText("Connected to "+room.getName());

            }

            @Override
            public void onConnectFailure(VideoException error) {
                Timber.i("onConnectFailure");
                roomStatusTextview.setText("Failed to connect to "+roomName);
            }

            @Override
            public void onDisconnected(Room room, VideoException error) {
                Timber.i("onDisconnected");
                roomStatusTextview.setText("Disconnected from "+roomName);
                RoomActivity.this.room = null;
                returnToVideoClientLogin();
            }

            @Override
            public void onParticipantConnected(Room room, Participant participant) {
                Timber.i("onParticipantConnected: "+participant.getIdentity());
                roomStatusTextview.setText("Participant "+participant.getIdentity()+ " joined");
                localVideoTrack.removeRenderer(primaryVideoView);
                thumbnailVideoView.setVisibility(View.VISIBLE);
                localVideoTrack.addRenderer(thumbnailVideoView);
                participant.getMedia().setListener(new ParticipantMediaListener(participant));
            }

            @Override
            public void onParticipantDisconnected(Room room, Participant participant) {
                Timber.i("onParticipantDisconnected "+participant.getIdentity());
                roomStatusTextview.setText("Participant "+participant.getIdentity()+ " left.");
                thumbnailVideoView.setVisibility(View.GONE);
                localVideoTrack.removeRenderer(thumbnailVideoView);
                primaryVideoView.setMirror(true);
                localVideoTrack.addRenderer(primaryVideoView);
            }
        };
    }

    private VideoViewRenderer createRendererForContainer(ViewGroup container) {
        VideoViewRenderer renderer = new VideoViewRenderer(this, container);
        renderer.setVideoScaleType(VideoScaleType.ASPECT_FILL);
        renderer.setListener(new VideoRenderer.Listener() {
            @Override
            public void onFirstFrame() {
                Timber.i("Participant onFirstFrame");
            }

            @Override
            public void onFrameDimensionsChanged(int width, int height, int rotation) {
                Timber.i("Participant onFrameDimensionsChanged [ width: " + width +
                        ", height: " + height +
                        ", rotation: " + rotation +
                        " ]");
            }
        });
        return renderer;
    }

    private class ParticipantMediaListener implements Media.Listener {
        private Participant participant;
        private VideoViewRenderer viewRenderer;

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
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            RelativeLayout layout = new RelativeLayout(RoomActivity.this);
            layout.setClickable(false);
            layout.setLayoutParams(layoutParams);
            primaryVideoView.setMirror(false);
            videoTrack.addRenderer(primaryVideoView);
        }

        @Override
        public void onVideoTrackRemoved(Media media, VideoTrack videoTrack) {
            Timber.i(participant.getIdentity() + ": onVideoTrackRemoved");
            videoTrack.removeRenderer(primaryVideoView);
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
