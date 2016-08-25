package com.tw.video.testapp.activity;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tw.video.testapp.R;
import com.tw.video.testapp.util.AccessManagerHelper;
import com.tw.video.testapp.util.SimpleSignalingUtils;
import com.twilio.common.AccessManager;
import com.twilio.video.AudioTrack;
import com.twilio.video.ConnectOptions;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalMedia;
import com.twilio.video.Media;
import com.twilio.video.Participant;
import com.twilio.video.Room;
import com.twilio.video.VideoClient;
import com.twilio.video.VideoException;
import com.twilio.video.VideoRenderer;
import com.twilio.video.VideoScaleType;
import com.twilio.video.VideoTrack;
import com.twilio.video.VideoViewRenderer;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class RoomActivity extends AppCompatActivity {

    @BindView(R.id.exit_room_fab) FloatingActionButton exitRoomFab;
    @BindView(R.id.media_status_textview) TextView mediaStatusTextview;
    @BindView(R.id.room_status_textview) TextView roomStatusTextview;
    @BindView(R.id.videoMainRelativeLayout) RelativeLayout videoMainRelativeLayout;

    private String username;
    private String capabilityToken;
    private String realm;
    private AccessManager accessManager;
    private VideoClient videoClient;
    private Room room;
    private String roomName;
    private LocalMedia localMedia;
    private LocalAudioTrack localAudioTrack;



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
        createVideoClient();
        connectToRoom();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (localMedia != null) {
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

    private Room.Listener createRoomListener() {
        return new Room.Listener() {
            @Override
            public void onConnected(Room room) {
                Timber.i("onConnected: "+room.getName() + " sid:"+
                        room.getSid()+" state:"+room.getState());
                //room.disconnect();
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
            }

            @Override
            public void onParticipantConnected(Room room, Participant participant) {
                Timber.i("onParticipantConnected: "+participant.getIdentity());
                roomStatusTextview.setText("Participant "+participant.getIdentity()+ " joined");
                participant.getMedia().setListener(new ParticipantMediaListener(participant));
            }

            @Override
            public void onParticipantDisconnected(Room room, Participant participant) {
                Timber.i("onParticipantDisconnected "+participant.getIdentity());
                roomStatusTextview.setText("Participant "+participant.getIdentity()+ " left.");
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
            viewRenderer = createRendererForContainer(layout);
            videoMainRelativeLayout.removeAllViews();
            videoMainRelativeLayout.addView(layout);
            videoTrack.addRenderer(viewRenderer);
        }

        @Override
        public void onVideoTrackRemoved(Media media, VideoTrack videoTrack) {
            Timber.i(participant.getIdentity() + ": onVideoTrackRemoved");
            videoTrack.removeRenderer(viewRenderer);
            viewRenderer = null;
            videoMainRelativeLayout.removeAllViews();
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
