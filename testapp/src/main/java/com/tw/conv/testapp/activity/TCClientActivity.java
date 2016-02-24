package com.tw.conv.testapp.activity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.twilio.common.TwilioAccessManager;
import com.twilio.common.TwilioAccessManagerFactory;
import com.twilio.common.TwilioAccessManagerListener;
import com.twilio.conversations.LocalAudioMediaStatsRecord;
import com.twilio.conversations.LocalVideoMediaStatsRecord;
import com.twilio.conversations.MediaTrackStatsRecord;
import com.twilio.conversations.RemoteAudioMediaStatsRecord;
import com.twilio.conversations.RemoteVideoMediaStatsRecord;
import com.twilio.conversations.TwilioConversationsException;
import com.tw.conv.testapp.R;
import com.tw.conv.testapp.dialog.Dialog;
import com.tw.conv.testapp.provider.TCCapabilityTokenProvider;
import com.tw.conv.testapp.util.ParticipantParser;
import com.twilio.conversations.AudioTrack;
import com.twilio.conversations.AudioOutput;
import com.twilio.conversations.CameraCapturer;
import com.twilio.conversations.CameraCapturerFactory;
import com.twilio.conversations.CapturerErrorListener;
import com.twilio.conversations.CapturerException;
import com.twilio.conversations.Conversation;
import com.twilio.conversations.ConversationCallback;
import com.twilio.conversations.ConversationListener;
import com.twilio.conversations.ConversationsClient;
import com.twilio.conversations.ConversationsClientListener;
import com.twilio.conversations.IncomingInvite;
import com.twilio.conversations.LocalMedia;
import com.twilio.conversations.LocalMediaListener;
import com.twilio.conversations.LocalVideoTrack;
import com.twilio.conversations.LocalVideoTrackFactory;
import com.twilio.conversations.LocalMediaFactory;
import com.twilio.conversations.MediaTrack;
import com.twilio.conversations.OutgoingInvite;
import com.twilio.conversations.Participant;
import com.twilio.conversations.ParticipantListener;
import com.twilio.conversations.TwilioConversations;
import com.twilio.conversations.VideoRendererObserver;
import com.twilio.conversations.VideoTrack;
import com.twilio.conversations.VideoViewRenderer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class TCClientActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_REJECT_INCOMING_CALL = 1000;
    private static final int REQUEST_CODE_ACCEPT_INCOMING_CALL = 1001;
    private static final int INCOMING_CALL_NOTIFICATION_ID = 1002;
    private static final String ACTION_REJECT_INCOMING_CALL =
            "com.tw.conv.testapp.action.REJECT_INCOMING_CALL";
    private static final String ACTION_ACCEPT_INCOMING_CALL =
            "com.tw.conv.testapp.action.ACCEPT_INCOMING_CALL";

    private ConversationsClient conversationsClient;
    private OutgoingInvite outgoingInvite;
    private LocalMedia localMedia;
    private boolean wasPreviewing = false;
    private boolean wasLive = false;
    private boolean inBackground = false;
    private boolean loggingOut = false;

    private enum AudioState {
        ENABLED,
        DISABLED,
    }

    private enum VideoState {
        ENABLED,
        DISABLED,
    }

    // We will default to front facing for now but this could easily be a preference
    private CameraCapturer.CameraSource currentCameraSource =
            CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA;
    private CameraCapturer cameraCapturer;
    private EditText participantEditText;
    private AlertDialog alertDialog;
    private TextView conversationsClientStatusTextView;
    private TextView conversationStatusTextView;
    private VideoViewRenderer participantVideoRenderer;
    private FloatingActionButton callActionFab;
    private FloatingActionButton switchCameraActionFab;
    private FloatingActionButton localVideoActionFab;
    private FloatingActionButton pauseActionFab;
    private FloatingActionButton audioActionFab;
    private FloatingActionButton muteActionFab;
    private FloatingActionButton addParticipantActionFab;
    private FloatingActionButton speakerActionFab;
    private IncomingInvite incomingInvite;
    private Conversation conversation;
    private FrameLayout previewFrameLayout;
    private ViewGroup localContainer;
    private VideoViewRenderer localRenderer;
    private boolean mirrorLocalRenderer = true;
    private Queue<ViewGroup> availableContainers;
    private Map<Participant, ViewGroup> participantContainers;
    private LinearLayout videoLinearLayout;
    private VideoState videoState;
    private AudioState audioState;
    private String capabilityToken;
    private TwilioAccessManager accessManager;

    /**
     * FIXME
     * This is a result of not being able to use explicit intents with dynamically registered
     * receivers. So what we do is have this receiver rebroadcast via the LocalBroadcastManager
     * so that that explicit intent can reach our dynamically registered receiver that
     * performs the rejection. This is pretty bad and would be avoidable if the IncomingInvite
     * was parcelable. If this were true we could just pass the invite in a bundle.
     *
     * For documentation on this hack see...
     *
     * http://streamingcon.blogspot.com/2014/04/dynamic-broadcastreceiver-registration.html
     */
    public static class Rebroadcaster extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("Received broadcast from notification");
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
            if (manager == null)
                return;
            Intent modifiedIntent = new Intent(intent);
            modifiedIntent.setAction(ACTION_REJECT_INCOMING_CALL);
            modifiedIntent.setComponent(null);
            manager.sendBroadcast(modifiedIntent);
        }
    }

    /**
     * Here is the actual receiver that performs the reject when the app is in the background
     */
    public class RejectIncomingCallReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (incomingInvite != null) {
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                rejectInvite(incomingInvite);
                notificationManager.cancel(INCOMING_CALL_NOTIFICATION_ID);
            }
        }
    }
    private final RejectIncomingCallReceiver rejectIncomingInviteReceiver =
            new RejectIncomingCallReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        previewFrameLayout = (FrameLayout) findViewById(R.id.previewFrameLayout);
        localContainer = (ViewGroup)findViewById(R.id.localContainer);
        availableContainers = new ArrayDeque<>();
        availableContainers.add( (ViewGroup)findViewById(R.id.participantContainer1) );
        availableContainers.add( (ViewGroup)findViewById(R.id.participantContainer2) );
        availableContainers.add((ViewGroup) findViewById(R.id.participantContainer3));
        videoLinearLayout = (LinearLayout)findViewById(R.id.videoLinearLayout);
        videoLinearLayout.removeAllViews();
        videoLinearLayout.addView(localContainer);

        participantContainers = new HashMap<>();

        callActionFab = (FloatingActionButton)findViewById(R.id.call_action_fab);
        callActionFab.hide();
        conversationsClientStatusTextView = (TextView) findViewById(R.id.conversations_client_status_textview);
        conversationStatusTextView = (TextView) findViewById(R.id.conversation_status_textview);
        switchCameraActionFab = (FloatingActionButton)findViewById(R.id.switch_camera_action_fab);
        localVideoActionFab = (FloatingActionButton)findViewById(R.id.local_video_action_fab);
        pauseActionFab = (FloatingActionButton)findViewById(R.id.local_video_pause_fab);
        audioActionFab = (FloatingActionButton)findViewById(R.id.audio_action_fab);
        muteActionFab = (FloatingActionButton)findViewById(R.id.local_audio_mute_fab);

        addParticipantActionFab = (FloatingActionButton)findViewById(R.id.add_participant_action_fab);
        speakerActionFab = (FloatingActionButton)findViewById(R.id.speaker_action_fab);

        String username = getIntent().getExtras().getString(TCCapabilityTokenProvider.USERNAME);
        getSupportActionBar().setTitle(username);

        // Get the capability token
        capabilityToken = getIntent().getExtras().getString(TCCapabilityTokenProvider.CAPABILITY_TOKEN);
        if(savedInstanceState != null) {
            capabilityToken = savedInstanceState.getString(TCCapabilityTokenProvider.CAPABILITY_TOKEN);
        }

        accessManager = TwilioAccessManagerFactory.createAccessManager(capabilityToken,
                accessManagerListener());
        conversationsClient = TwilioConversations.createConversationsClient(accessManager,
                conversationsClientListener());


        cameraCapturer = CameraCapturerFactory.createCameraCapturer(
                TCClientActivity.this,
                currentCameraSource,
                previewFrameLayout,
                capturerErrorListener());

        switchCameraActionFab.setOnClickListener(switchCameraClickListener());

        audioState = AudioState.ENABLED;
        setAudioStateIcon();
        videoState = VideoState.ENABLED;
        setVideoStateIcon();
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        setSpeakerphoneOn(true);
        setCallAction();
        startPreview();
        registerRejectReceiver();
    }

    @Override
    public Window getWindow() {
        Window window = super.getWindow();

        // So calls can be answered when screen is locked
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        return window;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        NotificationManager notificationManager  =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        /**
         * FIXME
         * This will only occur when new invite has been accepted in the background.
         * However, this is a little bit of a hack. We need to make the IncomingInvite
         * Parcelable so the developer does not have to maintain so much state
         */
        localMedia = createLocalMedia();
        acceptInvite(incomingInvite);
        setHangupAction();
        notificationManager.cancel(INCOMING_CALL_NOTIFICATION_ID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.client_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_log_out:
                // Will continue logout once the conversation has ended
                loggingOut = true;

                // End any current call
                if (isConversationOngoing()) {
                    hangup();
                } else {
                    logout();
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(TCCapabilityTokenProvider.CAPABILITY_TOKEN, capabilityToken);
    }

    @Override
    protected void onStart() {
        super.onStart();

        inBackground = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (conversationsClient != null && !conversationsClient.isListening()) {
            conversationsClient.listen();
        }
        if(cameraCapturer != null && wasPreviewing) {
            wasPreviewing = false;
            startPreview();
        } else if(isConversationOngoing()) {
            LocalVideoTrack localVideoTrack = localMedia.getLocalVideoTracks().get(0);
            if(localVideoTrack != null && wasLive) {
                localVideoTrack.enable(true);
                wasLive = false;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraCapturer != null && cameraCapturer.isPreviewing()) {
            wasPreviewing = true;
            stopPreview();
        } else if(isConversationOngoing() && !localMedia.getLocalVideoTracks().isEmpty()) {
            LocalVideoTrack localVideoTrack = localMedia.getLocalVideoTracks().get(0);
            if(localVideoTrack != null && localVideoTrack.isEnabled()) {
                localVideoTrack.enable(false);
                wasLive = true;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        inBackground = true;
    }

    @Override
    protected void onDestroy() {
        unregisterRejectReceiver();
        super.onDestroy();
    }

    private void startPreview() {
        if (cameraCapturer != null) {
            cameraCapturer.startPreview();
        }
    }

    private void stopPreview() {
        if(cameraCapturer != null && cameraCapturer.isPreviewing()) {
            cameraCapturer.stopPreview();
        }
    }

    private boolean isConversationOngoing() {
        return conversation != null ||
                outgoingInvite != null;
    }

    private void logout() {
        // Teardown preview
        if (cameraCapturer != null && cameraCapturer.isPreviewing()) {
            stopPreview();
            cameraCapturer = null;
        }

        // Teardown our conversation, client, and sdk instance
        disposeConversation();
        disposeConversationsClient();
        destroyConversationsSdk();
        returnToRegistration();
        loggingOut = false;
    }

    private void disposeConversation() {
        if (conversation != null) {
            conversation.dispose();
            conversation = null;
        }
    }

    private void disposeConversationsClient() {
        if (conversationsClient != null) {
            conversationsClient.dispose();
            conversationsClient = null;
        }
    }

    private void destroyConversationsSdk() {
        TwilioConversations.destroy();
    }

    private void returnToRegistration() {
        startActivity(new Intent(TCClientActivity.this,
                TCRegistrationActivity.class));
        finish();
    }

    private void setCallAction() {
        callActionFab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_call_white_24px));
        callActionFab.show();
        callActionFab.setOnClickListener(callClickListener());
        switchCameraActionFab.show();
        switchCameraActionFab.setOnClickListener(switchCameraClickListener());
        localVideoActionFab.show();
        localVideoActionFab.setOnClickListener(localVideoClickListener());
        audioActionFab.show();
        audioActionFab.setOnClickListener(audioClickListener());
        addParticipantActionFab.hide();
        speakerActionFab.hide();
    }

    private void setHangupAction() {
        callActionFab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_call_end_white_24px));
        callActionFab.show();
        callActionFab.setOnClickListener(hangupClickListener());
        addParticipantActionFab.show();
        addParticipantActionFab.setOnClickListener(addClickListener());
        speakerActionFab.show();
        speakerActionFab.setOnClickListener(speakerClickListener());
    }

    private void hideAction() {
        callActionFab.hide();
        speakerActionFab.hide();
    }

    private ConversationsClientListener conversationsClientListener() {
        return new ConversationsClientListener() {
            @Override
            public void onStartListeningForInvites(ConversationsClient conversationsClient) {
                conversationsClientStatusTextView.setText("onStartListeningForInvites");
            }

            @Override
            public void onStopListeningForInvites(ConversationsClient conversationsClient) {
                conversationsClientStatusTextView.setText("onStopListeningForInvites");
            }

            @Override
            public void onFailedToStartListening(ConversationsClient conversationsClient,
                                                 TwilioConversationsException e) {
                Timber.e(e.getMessage());
                conversationsClientStatusTextView
                        .setText("onFailedToStartListening: " + e.getMessage());
            }

            @Override
            public void onIncomingInvite(ConversationsClient conversationsClient,
                                         IncomingInvite incomingInvite) {
                TCClientActivity.this.incomingInvite = incomingInvite;
                if (!inBackground) {
                    conversationsClientStatusTextView
                            .setText("onIncomingInvite" + incomingInvite.getInvitee());
                    showInviteDialog(incomingInvite);
                } else {
                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    /**
                     * Pending intents are often reused and this results in some not being
                     * triggered correctly so we explicitly cancel any existing intents first.
                     * This is a known bug and workaround seen at
                     *
                     * https://code.google.com/p/android/issues/detail?id=61850
                     */
                    getRejectPendingIntent().cancel();
                    getAcceptPendingIntent().cancel();
                    PendingIntent rejectPendingIntent = getRejectPendingIntent();

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(TCClientActivity.this)
                                    .setSmallIcon(R.drawable.ic_videocam_green_24px)
                                    .setDeleteIntent(rejectPendingIntent)
                                    .setContentTitle(incomingInvite.getInvitee())
                                    .setPriority(NotificationCompat.PRIORITY_MAX)
                                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                                    .setCategory(NotificationCompat.CATEGORY_CALL)
                                    .setShowWhen(true)
                                    .addAction(0, "Decline", rejectPendingIntent)
                                    .addAction(0, "Accept", getAcceptPendingIntent())
                                    .setContentText(getString(R.string.incoming_call));


                    notificationManager.notify(INCOMING_CALL_NOTIFICATION_ID,
                            mBuilder.build());
                }
            }

            @Override
            public void onIncomingInviteCancelled(ConversationsClient conversationsClient,
                                                  IncomingInvite incomingInvite) {
                TCClientActivity.this.incomingInvite = null;
                if (!inBackground) {
                    alertDialog.dismiss();
                    Snackbar.make(conversationStatusTextView, "Invite from " +
                            incomingInvite.getInvitee() + " terminated", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(INCOMING_CALL_NOTIFICATION_ID);
                }
            }
        };
    }

    private TwilioAccessManagerListener accessManagerListener() {
        return new TwilioAccessManagerListener() {
            /*
             *  The token expiration event notifies the developer 3 minutes before
             *  token actually expires to allow the developer to request a new token
             */
            @Override
            public void onAccessManagerTokenExpire(TwilioAccessManager twilioAccessManager) {
                Timber.d("onAccessManagerTokenExpire");
                conversationsClientStatusTextView.setText("onAccessManagerTokenExpire");
                obtainCapabilityToken();
            }

            @Override
            public void onTokenUpdated(TwilioAccessManager twilioAccessManager) {
                conversationsClientStatusTextView.setText("onAccessManagerTokenUpdated");
            }

            @Override
            public void onError(TwilioAccessManager twilioAccessManager, String s) {
                Timber.e(s);
                conversationsClientStatusTextView.setText("onAccessManagerTokenError: " + s);
            }
        };
    }

    private View.OnClickListener callClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideAction();
                showCallDialog();
            }
        };
    }

    private View.OnClickListener hangupClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hangup();
                reset();
            }
        };
    }

    private View.OnClickListener switchCameraClickListener() {
        return new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(cameraCapturer != null) {
                    // Switch our camera
                    cameraCapturer.switchCamera();

                    // Update the camera source
                    currentCameraSource = (currentCameraSource == CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA) ?
                            (CameraCapturer.CameraSource.CAMERA_SOURCE_BACK_CAMERA) :
                            (CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA);

                    // Update our local renderer to mirror or not
                    mirrorLocalRenderer = (currentCameraSource == CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA);

                    // Determine if our renderer is mirroring now
                    if (localRenderer != null) {
                        localRenderer.setMirror(mirrorLocalRenderer);
                    }
                }
            }
        };
    }

    private View.OnClickListener localVideoClickListener() {
        return new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (videoState == VideoState.DISABLED) {
                    cameraCapturer.startPreview();
                    if (localMedia != null) {
                        localContainer = (ViewGroup)findViewById(R.id.localContainer);
                        LocalVideoTrack videoTrack = LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer);
                        localMedia.addLocalVideoTrack(videoTrack);
                        switchCameraActionFab.hide();
                    } else {
                        videoState = VideoState.ENABLED;
                    }
                } else if (videoState == VideoState.ENABLED) {
                    cameraCapturer.stopPreview();
                    if (localMedia != null) {
                        if (localMedia.getLocalVideoTracks().size() > 0) {
                            localMedia.removeLocalVideoTrack(localMedia.getLocalVideoTracks().get(0));
                            pauseActionFab.hide();
                        }
                    } else {
                        videoState = VideoState.DISABLED;
                    }
                }
                setVideoStateIcon();
            }
        };
    }

    public void pauseVideo() {
        List<LocalVideoTrack> videoTracks =
                localMedia.getLocalVideoTracks();
        if (videoTracks.size() > 0) {
            LocalVideoTrack videoTrack = videoTracks.get(0);
            boolean enable = !videoTrack.isEnabled();
            boolean set = videoTrack.enable(enable);
            if(set) {
                if (videoTrack.isEnabled()) {
                    pauseActionFab.setImageDrawable(
                            ContextCompat.getDrawable(TCClientActivity.this, R.drawable.ic_pause_green_24px));
                } else {
                    pauseActionFab.setImageDrawable(
                            ContextCompat.getDrawable(TCClientActivity.this, R.drawable.ic_pause_red_24px));
                }
            } else {
                Snackbar.make(conversationStatusTextView, "Pause action failed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        } else {
            Timber.w("Camera is not present. Unable to pause");
        }
    }

    private void setVideoStateIcon() {
        if (videoState == VideoState.ENABLED) {
            switchCameraActionFab.show();
            localVideoActionFab.setImageDrawable(
                    ContextCompat.getDrawable(TCClientActivity.this,
                            R.drawable.ic_videocam_white_24px));
        } else {
            switchCameraActionFab.hide();
            localVideoActionFab.setImageDrawable(
                    ContextCompat.getDrawable(TCClientActivity.this,
                            R.drawable.ic_videocam_off_gray_24px));
        }
        if(localMedia != null && localMedia.getLocalVideoTracks().size() > 0 ) {
            if(localMedia.getLocalVideoTracks().get(0).isEnabled()) {
                pauseActionFab.setImageDrawable(
                        ContextCompat.getDrawable(TCClientActivity.this, R.drawable.ic_pause_green_24px));
            } else {
                pauseActionFab.setImageDrawable(
                        ContextCompat.getDrawable(TCClientActivity.this, R.drawable.ic_pause_red_24px));
            }
            pauseActionFab.show();
        } else {
            pauseActionFab.hide();
        }
    }

    private View.OnClickListener audioClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioState == AudioState.DISABLED)  {
                    if (localMedia != null) {
                        boolean microphoneAdded = localMedia.addMicrophone();
                        if(microphoneAdded) {
                            audioState = AudioState.ENABLED;
                        } else {
                            Snackbar.make(conversationStatusTextView, "Adding microphone failed", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    } else {
                        audioState = AudioState.ENABLED;
                    }
                } else {
                    if (localMedia != null) {
                        boolean microphoneRemoved = localMedia.removeMicrophone();
                        if(microphoneRemoved) {
                            audioState = AudioState.DISABLED;
                        } else {
                            Snackbar.make(conversationStatusTextView, "Removing microphone failed", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    } else {
                        audioState = AudioState.DISABLED;
                    }
                }
                setAudioStateIcon();
            }
        };
    }

    private void setAudioStateIcon() {
        if (audioState == AudioState.ENABLED) {
            audioActionFab.setImageDrawable(
                    ContextCompat.getDrawable(TCClientActivity.this, R.drawable.ic_mic_white_24px));
        } else if (audioState == AudioState.DISABLED) {
            audioActionFab.setImageDrawable(
                    ContextCompat.getDrawable(TCClientActivity.this, R.drawable.ic_mic_off_gray_24px));
        }
        if(audioState == AudioState.ENABLED && localMedia != null && localMedia.isMicrophoneAdded()) {
            muteActionFab.show();
        } else {
            muteActionFab.hide();
        }
    }

    private void muteAudio() {
        boolean enable = !localMedia.isMuted();
        boolean set = localMedia.mute(enable);
        if(set) {
            if (enable) {
                muteActionFab.setImageDrawable(
                        ContextCompat.getDrawable(TCClientActivity.this, R.drawable.ic_mic_red_24px));
            } else {
                muteActionFab.setImageDrawable(
                        ContextCompat.getDrawable(TCClientActivity.this, R.drawable.ic_mic_green_24px));
            }
        } else {
            Snackbar.make(conversationStatusTextView, "Mute action failed", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private View.OnClickListener addClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddParticipantsDialog();
            }
        };
    }

    private void showAddParticipantsDialog() {
        participantEditText = new EditText(this);
        alertDialog = Dialog.createAddParticipantsDialog(participantEditText,
                addParticipantsClickListener(participantEditText),
                cancelAddParticipantsClickListener(),
                this);
        alertDialog.show();
    }

    private void showCallDialog() {
        participantEditText = new EditText(this);
        alertDialog = Dialog.createCallParticipantsDialog(participantEditText,
                callParticipantClickListener(participantEditText),
                cancelCallClickListener(),
                this);
        alertDialog.show();
    }

    private void hangup() {
        if(conversation != null) {
            conversation.disconnect();
        } else if(outgoingInvite != null){
            outgoingInvite.cancel();
        }
    }

    private DialogInterface.OnClickListener callParticipantClickListener(final EditText participantEditText) {
        return new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                stopPreview();
                Set<String> participants = ParticipantParser.getParticipants(participantEditText.getText().toString());
                if(participants.size() > 0) {

                    localMedia = createLocalMedia();
                    outgoingInvite = conversationsClient.sendConversationInvite(participants,
                            localMedia, new ConversationCallback() {
                                @Override
                                public void onConversation(Conversation conversation, TwilioConversationsException e) {
                                    Timber.e("sendConversationInvite onConversation");
                                    if (e == null) {
                                        TCClientActivity.this.conversation = conversation;
                                        conversation.setConversationListener(conversationListener());
                                    } else {
                                        if (e.getErrorCode() == TwilioConversations.CONVERSATION_REJECTED) {
                                            Snackbar.make(conversationStatusTextView, "Invite rejected", Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        } else if (e.getErrorCode() == TwilioConversations.CONVERSATION_IGNORED) {
                                            Snackbar.make(conversationStatusTextView, "Invite ignored", Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        } else  {
                                            Snackbar.make(conversationStatusTextView, e.getMessage(), Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        }

                                        if (!loggingOut) {
                                            hangup();
                                            reset();
                                        } else {
                                            logout();
                                        }
                                    }
                                }
                            });
                    if (outgoingInvite != null) {
                        setHangupAction();
                    }

                } else {
                    participantEditText.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                }
            }
        };
    }

    private DialogInterface.OnClickListener cancelCallClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setCallAction();
                alertDialog.dismiss();
            }
        };
    }

    private DialogInterface.OnClickListener addParticipantsClickListener(final EditText participantEditText) {
        return new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (conversation == null) {
                    return;
                }
                Set<String> participants = ParticipantParser.getParticipants(participantEditText.getText().toString());
                if(participants.size() > 0) {
                    conversation.invite(participants);
                } else {
                    participantEditText.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                }
            }
        };
    }

    private DialogInterface.OnClickListener cancelAddParticipantsClickListener() {
        return new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        };
    }

    private void showInviteDialog(final IncomingInvite incomingInvite) {
        alertDialog = Dialog.createInviteDialog(incomingInvite.getInvitee(),
                acceptCallClickListener(incomingInvite),
                rejectCallClickListener(incomingInvite),
                this);
        alertDialog.show();
    }

    private DialogInterface.OnClickListener acceptCallClickListener(
            final IncomingInvite invite) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                localMedia = createLocalMedia();
                acceptInvite(incomingInvite);
                setHangupAction();
            }
        };
    }

    private void acceptInvite(IncomingInvite incomingInvite) {
        incomingInvite.accept(localMedia, new ConversationCallback() {
            @Override
            public void onConversation(Conversation conversation, TwilioConversationsException e) {
                Timber.e("onConversationInvite onConversation");
                if (e == null) {
                    TCClientActivity.this.conversation = conversation;
                    conversation.setConversationListener(conversationListener());
                } else if (e.getErrorCode() == TwilioConversations.TOO_MANY_ACTIVE_CONVERSATIONS) {
                    Timber.w(e.getMessage());
                    conversationsClientStatusTextView
                            .setText("Unable to accept call. Too many active conversations.");
                } else {
                    hangup();
                    reset();
                }
            }
        });
    }

    private DialogInterface.OnClickListener rejectCallClickListener(
            final IncomingInvite incomingInvite) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rejectInvite(incomingInvite);
            }
        };
    }

    private void rejectInvite(IncomingInvite incomingInvite) {
        incomingInvite.reject();
        this.incomingInvite = null;
        setCallAction();
    }

    private CapturerErrorListener capturerErrorListener() {
        return new CapturerErrorListener() {
            @Override
            public void onError(CapturerException e) {
                Timber.e(e.getMessage());
            }
        };
    }

    private void setSpeakerphoneOn(boolean on) {
        if (conversationsClient == null) {
            Timber.e("Unable to set audio output, conversation client is null");
            return;
        }
        conversationsClient.setAudioOutput(on ? AudioOutput.SPEAKERPHONE :
                AudioOutput.HEADSET);

        if (on == true) {
            Drawable drawable = ContextCompat.getDrawable(TCClientActivity.this, R.drawable.ic_volume_down_white_24px);
            speakerActionFab.setImageDrawable(drawable);
        } else {
            // route back to headset
            Drawable drawable = ContextCompat.getDrawable(TCClientActivity.this, R.drawable.ic_volume_down_gray_24px);
            speakerActionFab.setImageDrawable(drawable);
        }
    }

    private View.OnClickListener speakerClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conversationsClient == null) {
                    Timber.e("Unable to set audio output, conversation client is null");
                    return;
                }
                boolean speakerOn =
                        !(conversationsClient.getAudioOutput() ==  AudioOutput.SPEAKERPHONE) ?  true : false;
                setSpeakerphoneOn(speakerOn);
            }
        };
    }

    private View.OnClickListener pauseClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseVideo();
            }
        };
    }

    private View.OnClickListener muteClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                muteAudio();
            }
        };
    }

    private ConversationListener conversationListener() {
        return new ConversationListener() {
            @Override
            public void onParticipantConnected(Conversation conversation, Participant participant) {
                conversationStatusTextView.setText("onParticipantConnected " + participant.getIdentity());
                videoLinearLayout.invalidate();

                participant.setParticipantListener(participantListener());

            }

            @Override
            public void onFailedToConnectParticipant(Conversation conversation, Participant participant, TwilioConversationsException e) {
                Timber.e(e.getMessage());
                conversationStatusTextView.setText("onFailedToConnectParticipant " + participant.getIdentity());
            }

            @Override
            public void onParticipantDisconnected(Conversation conversation, Participant participant) {
                conversationStatusTextView.setText("onParticipantDisconnected " + participant.getIdentity());
            }

            @Override
            public void onConversationEnded(Conversation conversation, TwilioConversationsException e) {
                String status = "onConversationEnded";
                if (e != null) {
                    status += " " + e.getMessage();
                    if(e.getErrorCode() == TwilioConversations.CONVERSATION_FAILED) {
                        Snackbar.make(conversationStatusTextView, "Invite failed " + conversation.getConversationSid(), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else if(e.getErrorCode() == TwilioConversations.CONVERSATION_REJECTED) {
                        Snackbar.make(conversationStatusTextView, "Invite was rejected " + conversation.getConversationSid(), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }
                conversationStatusTextView.setText(status);

                // If user is logging out we need to finish that process otherwise we just reset
                if (loggingOut) {
                    logout();
                } else {
                    reset();
                }
            }

            @Override
            public void onReceiveTrackStatistics(MediaTrackStatsRecord stats) {
                StringBuilder strBld = new StringBuilder();
                strBld.append(
                        String.format("Receiving stats for sid: %s, trackId: %s, direction: %s ",
                        stats.getParticipantSid(), stats.getTrackId(), stats.getDirection()));
                if (stats instanceof LocalAudioMediaStatsRecord) {
                    strBld.append(
                            String.format("media type: audio, bytes sent %d",
                                    ((LocalAudioMediaStatsRecord) stats).getBytesSent()));
                } else if (stats instanceof LocalVideoMediaStatsRecord) {
                    strBld.append(
                            String.format("media type: video, bytes sent %d",
                                    ((LocalVideoMediaStatsRecord) stats).getBytesSent()));
                } else if (stats instanceof RemoteAudioMediaStatsRecord) {
                    strBld.append(
                            String.format("media type: audio, bytes received %d",
                                    ((RemoteAudioMediaStatsRecord) stats).getBytesReceived()));
                } else if (stats instanceof RemoteVideoMediaStatsRecord) {
                    strBld.append(
                            String.format("media type: video, bytes received %d",
                                    ((RemoteVideoMediaStatsRecord) stats).getBytesReceived()));
                } else {
                    strBld.append("Unknown media type");
                }
                Timber.i(strBld.toString());
            }
        };
    }

    private ParticipantListener participantListener() {
        return new ParticipantListener() {
            @Override
            public void onVideoTrackAdded(Conversation conversation, Participant participant, VideoTrack videoTrack) {
                Timber.i("onVideoTrackAdded " + participant.getIdentity());
                conversationStatusTextView.setText("onVideoTrackAdded " + participant.getIdentity());
                if (availableContainers.isEmpty()) {
                    // No available containers
                    return;
                }
                ViewGroup participantContainer = availableContainers.poll();
                videoLinearLayout.addView(participantContainer);
                participantContainers.put(participant, participantContainer);

                // Remote participant
                participantVideoRenderer = new VideoViewRenderer(TCClientActivity.this, participantContainer);
                participantVideoRenderer.setObserver(new VideoRendererObserver() {
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
                videoTrack.addRenderer(participantVideoRenderer);
            }

            @Override
            public void onVideoTrackRemoved(Conversation conversation, Participant participant, VideoTrack videoTrack) {
                Timber.i("onVideoTrackRemoved " + participant.getIdentity());
                conversationStatusTextView.setText("onVideoTrackRemoved " + participant.getIdentity());
                ViewGroup participantContainer = participantContainers.remove(participant);
                if (participantContainer != null) {
                    participantContainer.removeAllViews();
                    videoLinearLayout.removeView(participantContainer);
                    availableContainers.add(participantContainer);
                }
            }

            @Override
            public void onAudioTrackAdded(Conversation conversation, Participant participant, AudioTrack audioTrack) {
                Timber.i("onAudioTrackAdded " + participant.getIdentity());
            }

            @Override
            public void onAudioTrackRemoved(Conversation conversation, Participant participant, AudioTrack audioTrack) {
                Timber.i("onAudioTrackRemoved " + participant.getIdentity());
            }

            @Override
            public void onTrackEnabled(Conversation conversation, Participant participant, MediaTrack mediaTrack) {
                Timber.i("onTrackEnabled " + participant.getIdentity());

                for(VideoTrack videoTrack : participant.getMedia().getVideoTracks()) {
                    if(videoTrack.getTrackId().equals(mediaTrack.getTrackId())) {
                        ViewGroup participantContainer = participantContainers.get(participant);
                        List<View> trackStatusViews = getViewsByTag(participantContainer, mediaTrack.getTrackId());
                        for(View trackStatusView: trackStatusViews) {
                            participantContainer.removeView(trackStatusView);
                        }
                        break;
                    }
                }

                for(AudioTrack audioTrack: participant.getMedia().getAudioTracks()) {
                    if(audioTrack.getTrackId().equals(mediaTrack.getTrackId())) {
                        ViewGroup participantContainer = participantContainers.get(participant);
                        // If the conversation does not have a video track, there is no participant container available
                        if(participantContainer != null) {
                            List<View> trackStatusViews = getViewsByTag(participantContainer, mediaTrack.getTrackId());
                            for (View trackStatusView : trackStatusViews) {
                                participantContainer.removeView(trackStatusView);
                            }
                            break;
                        }
                    }
                }
            }

            private ArrayList<View> getViewsByTag(ViewGroup root, String tag){
                ArrayList<View> views = new ArrayList<View>();
                final int childCount = root.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    final View child = root.getChildAt(i);
                    if (child instanceof ViewGroup) {
                        views.addAll(getViewsByTag((ViewGroup) child, tag));
                    }

                    final Object tagObj = child.getTag();
                    if (tagObj != null && tagObj.equals(tag)) {
                        views.add(child);
                    }

                }
                return views;
            }

            @Override
            public void onTrackDisabled(Conversation conversation, Participant participant, MediaTrack mediaTrack) {
                Timber.i("onTrackDisabled " + participant.getIdentity());

                for(VideoTrack videoTrack : participant.getMedia().getVideoTracks()) {
                    if(videoTrack.getTrackId().equals(mediaTrack.getTrackId())) {
                        ViewGroup participantContainer = participantContainers.get(participant);
                        ImageView disabledView = new ImageView(TCClientActivity.this);
                        disabledView.setTag(mediaTrack.getTrackId());
                        disabledView.setBackgroundResource(R.drawable.ic_videocam_off_red_24px);
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                        disabledView.setLayoutParams(layoutParams);
                        participantContainer.addView(disabledView);
                        break;
                    }
                }

                for(AudioTrack audioTrack : participant.getMedia().getAudioTracks()) {
                    if(audioTrack.getTrackId().equals(mediaTrack.getTrackId())) {
                        ViewGroup participantContainer = participantContainers.get(participant);
                        // If the conversation does not have a video track, there is no participant container available
                        if(participantContainer != null) {
                            ImageView disabledView = new ImageView(TCClientActivity.this);
                            disabledView.setTag(mediaTrack.getTrackId());
                            disabledView.setBackgroundResource(R.drawable.ic_mic_off_red_24px);
                            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                            disabledView.setLayoutParams(layoutParams);
                            participantContainer.addView(disabledView);
                        }
                        break;
                    }
                }

            }
        };
    }

    private LocalMediaListener localMediaListener() {
        return new LocalMediaListener() {
            @Override
            public void onLocalVideoTrackAdded(LocalMedia localMedia, LocalVideoTrack localVideoTrack) {
                videoState = VideoState.ENABLED;
                conversationStatusTextView.setText("onLocalVideoTrackAdded");
                localRenderer = new VideoViewRenderer(TCClientActivity.this,
                        localContainer);
                localRenderer.setMirror(mirrorLocalRenderer);
                localVideoTrack.addRenderer(localRenderer);
                setVideoStateIcon();
                pauseActionFab.setOnClickListener(pauseClickListener());
                setAudioStateIcon();
                muteActionFab.setOnClickListener(muteClickListener());
            }

            @Override
            public void onLocalVideoTrackRemoved(LocalMedia localMedia, LocalVideoTrack localVideoTrack) {
                videoState = VideoState.DISABLED;
                conversationStatusTextView.setText("onLocalVideoTrackRemoved");
                localContainer.removeAllViews();
                setVideoStateIcon();
                setAudioStateIcon();
            }

            @Override
            public void onLocalVideoTrackError(LocalMedia localMedia, LocalVideoTrack localVideoTrack, TwilioConversationsException e) {
                setVideoStateIcon();
                Snackbar.make(conversationStatusTextView, e.getMessage(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

        };
    }

    private void reset() {
        if(participantVideoRenderer != null) {
            participantVideoRenderer = null;
        }
        localContainer.removeAllViews();
        for (ViewGroup participantContainer : participantContainers.values()) {
            availableContainers.add(participantContainer);
            participantContainer.removeAllViews();
            videoLinearLayout.removeView(participantContainer);
        }
        participantContainers.clear();
        localContainer = (ViewGroup)findViewById(R.id.localContainer);

        if(conversation != null) {
            conversation.dispose();
            conversation = null;
        }
        localMedia = null;
        outgoingInvite = null;

        audioState = AudioState.ENABLED;
        setAudioStateIcon();
        videoState = VideoState.ENABLED;
        setVideoStateIcon();
        pauseActionFab.setImageDrawable(
                ContextCompat.getDrawable(TCClientActivity.this, R.drawable.ic_pause_green_24px));
        muteActionFab.setImageDrawable(
                ContextCompat.getDrawable(TCClientActivity.this, R.drawable.ic_mic_green_24px));

        setSpeakerphoneOn(true);

        setCallAction();
        startPreview();
    }

    private void obtainCapabilityToken() {
        if (accessManager == null) {
            Timber.e("AccessManager is null");
            return;
        }
        TCCapabilityTokenProvider.obtainTwilioCapabilityToken(
                accessManager.getIdentity(), new Callback<String>() {

                    @Override
                    public void success(final String capabilityToken, Response response) {
                        if (accessManager != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    accessManager.updateToken(capabilityToken);
                                }
                            }).start();
                        } else {
                            Timber.e("AccessManager is null");
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Timber.e("Error fetching new capability token: " + error.getLocalizedMessage());
                        conversationsClientStatusTextView.setText("failure to obtain capability token");
                    }
                });

    }

    private LocalMedia createLocalMedia() {
        LocalMedia localMedia = LocalMediaFactory.createLocalMedia(localMediaListener());
        if (videoState != VideoState.DISABLED) {
            LocalVideoTrack videoTrack = LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer);
            localMedia.addLocalVideoTrack(videoTrack);
        }
        if (audioState == AudioState.ENABLED) {
            localMedia.addMicrophone();
        } else {
            localMedia.removeMicrophone();
        }
        return localMedia;
    }

    private PendingIntent getRejectPendingIntent() {
        Intent incomingCallRejectIntent = new Intent();
        incomingCallRejectIntent.setClass(this, Rebroadcaster.class);

        return PendingIntent.getBroadcast(this,
                REQUEST_CODE_REJECT_INCOMING_CALL,
                incomingCallRejectIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getAcceptPendingIntent() {
        Intent incomingCallAcceptIntent = new Intent(this, TCClientActivity.class);
        incomingCallAcceptIntent.setAction(ACTION_ACCEPT_INCOMING_CALL);
        incomingCallAcceptIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        return PendingIntent.getActivity(this,
                REQUEST_CODE_ACCEPT_INCOMING_CALL,
                incomingCallAcceptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void registerRejectReceiver() {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);

        if (manager != null) {
            manager.registerReceiver(rejectIncomingInviteReceiver,
                    new IntentFilter(ACTION_REJECT_INCOMING_CALL));
        }
    }

    private void unregisterRejectReceiver() {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);

        if (manager != null) {
            manager.unregisterReceiver(rejectIncomingInviteReceiver);
        }
    }
}
