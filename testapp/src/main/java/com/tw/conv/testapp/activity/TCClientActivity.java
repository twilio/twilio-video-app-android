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
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.appyvet.rangebar.IRangeBarFormatter;
import com.appyvet.rangebar.RangeBar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tw.conv.testapp.adapter.IceServerAdapter;
import com.tw.conv.testapp.adapter.RemoteVideoTrackStatsAdapter;
import com.tw.conv.testapp.provider.TwilioIceServer;
import com.tw.conv.testapp.provider.TwilioIceServers;
import com.tw.conv.testapp.util.IceOptionsHelper;
import com.twilio.common.TwilioAccessManager;
import com.twilio.common.TwilioAccessManagerFactory;
import com.twilio.common.TwilioAccessManagerListener;
import com.twilio.conversations.IceOptions;
import com.twilio.conversations.IceServer;
import com.twilio.conversations.IceTransportPolicy;
import com.twilio.conversations.LocalAudioTrackStatsRecord;
import com.twilio.conversations.LocalVideoTrackStatsRecord;
import com.twilio.conversations.RemoteVideoTrackStatsRecord;
import com.twilio.conversations.StatsListener;
import com.twilio.conversations.MediaTrackStatsRecord;
import com.twilio.conversations.RemoteAudioTrackStatsRecord;
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
import com.twilio.conversations.VideoConstraints;
import com.twilio.conversations.VideoDimensions;
import com.twilio.conversations.VideoRendererObserver;
import com.twilio.conversations.VideoTrack;
import com.twilio.conversations.VideoViewRenderer;
import com.twilio.conversations.internal.ClientOptionsInternal;

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private static final String OPTION_DEV_REGISTRAR = "endpoint.dev.twilio.com";
    private static final String OPTION_DEV_STATS_URL = "https://eventgw.dev.twilio.com";
    private static final String OPTION_STAGE_REGISTRAR = "endpoint.stage.twilio.com";
    private static final String OPTION_STAGE_STATS_URL = "https://eventgw.stage.twilio.com";
    private static final String OPTION_REGISTRAR_KEY = "registrar";
    private static final String OPTION_STATS_KEY = "stats-server-url";

    private ConversationsClient conversationsClient;
    private OutgoingInvite outgoingInvite;
    private LocalMedia localMedia;
    private boolean wasPreviewing = false;
    private boolean wasLive = false;
    private boolean inBackground = false;
    private boolean loggingOut = false;
    private String realm;
    private CheckBox statsCheckBox;
    private LinearLayout statsLayout;
    private TextView localVideoTrackStatsTextView;
    private RemoteVideoTrackStatsAdapter remoteVideoTrackStatsAdapter;
    private LinkedHashMap<String, RemoteVideoTrackStatsRecord>
            remoteVideoTrackStatsRecordMap = new LinkedHashMap<>();
    private RecyclerView remoteStatsRecyclerView;

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
    private ExecutorService statsExecutorService;

    private VideoConstraints videoConstraints;

    private int minFps = 0;
    private int maxFps = 0;
    private VideoDimensions minVideoDimensions = null;
    private VideoDimensions maxVideoDimensions = null;

    private IceOptions iceOptions;
    private Spinner iceTransPolicySpinner;
    private ListView twilioIceServersListView;


    private static final Map<Integer, VideoDimensions> videoDimensionsMap;
    static {
        Map<Integer, VideoDimensions> vdMap = new HashMap<>();
        vdMap.put(0, new VideoDimensions(0,0));
        vdMap.put(1, VideoDimensions.CIF_VIDEO_DIMENSIONS);
        vdMap.put(2, VideoDimensions.VGA_VIDEO_DIMENSIONS);
        vdMap.put(3, VideoDimensions.WVGA_VIDEO_DIMENSIONS);
        vdMap.put(4, VideoDimensions.HD_540P_VIDEO_DIMENSIONS);
        vdMap.put(5, VideoDimensions.HD_720P_VIDEO_DIMENSIONS);
        vdMap.put(6, VideoDimensions.HD_960P_VIDEO_DIMENSIONS);
        vdMap.put(7, VideoDimensions.HD_S1080P_VIDEO_DIMENSIONS);
        vdMap.put(8, VideoDimensions.HD_1080P_VIDEO_DIMENSIONS);
        videoDimensionsMap = Collections.unmodifiableMap(vdMap);
    }

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
        // So calls can be answered when screen is locked
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

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

        statsCheckBox = (CheckBox)findViewById(R.id.enable_stats_checkbox);
        statsLayout = (LinearLayout)findViewById(R.id.stats_layout);
        statsLayout.setVisibility(View.INVISIBLE);

        remoteStatsRecyclerView = (RecyclerView) findViewById(R.id.stats_recycler_view);
        remoteVideoTrackStatsAdapter = new RemoteVideoTrackStatsAdapter(remoteVideoTrackStatsRecordMap);
        remoteStatsRecyclerView.setAdapter(remoteVideoTrackStatsAdapter);
        remoteStatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        remoteStatsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        localVideoTrackStatsTextView = (TextView)findViewById(R.id.local_video_track_stats_textview);

        statsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(conversation != null) {
                    if(isChecked) {
                        enableStats();
                    } else {
                        disableStats();
                    }
                }
            }
        });

        DrawerLayout drawerLayout = (DrawerLayout)findViewById(R.id.navigation_drawer);

        final RangeBar fpsRangeBar = (RangeBar)findViewById(R.id.fps_rangebar);
        final RangeBar videoDimensionsRangeBar = (RangeBar)findViewById(R.id.video_dimensions_rangebar);

        videoDimensionsRangeBar.setTickStart(1);
        videoDimensionsRangeBar.setTickEnd(videoDimensionsMap.size());

        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                minFps = Integer.valueOf(fpsRangeBar.getLeftPinValue());
                maxFps = Integer.valueOf(fpsRangeBar.getRightPinValue());
                minVideoDimensions = videoDimensionsMap.get(videoDimensionsRangeBar.getLeftIndex());
                maxVideoDimensions = videoDimensionsMap.get(videoDimensionsRangeBar.getRightIndex());
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                // Update video constraints
                try {
                    videoConstraints = new VideoConstraints.Builder()
                            .minFps(minFps)
                            .maxFps(maxFps)
                            .minVideoDimensions(minVideoDimensions)
                            .maxVideoDimensions(maxVideoDimensions)
                            .build();
                } catch(Exception e) {
                    Snackbar.make(
                            conversationStatusTextView,
                            e.getMessage(),
                            Snackbar.LENGTH_LONG)
                            .setAction("Action", null)
                            .show();
                    videoConstraints = null;
                }

                Timber.i("Video Constraints Fps " + minFps + " " + maxFps);
                Timber.i("Video Constraints MinVD " +
                        minVideoDimensions.width + " " + minVideoDimensions.height);
                Timber.i("Video Constraints MaxVD " +
                        maxVideoDimensions.width + " " + maxVideoDimensions.height);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        fpsRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,
                                              int rightPinIndex, String leftPinValue,
                                              String rightPinValue) {
                minFps = Integer.valueOf(leftPinValue);
                maxFps = Integer.valueOf(rightPinValue);
            }
        });

        videoDimensionsRangeBar.setOnRangeBarChangeListener(
                new RangeBar.OnRangeBarChangeListener() {
                    @Override
                    public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,
                                                      int rightPinIndex, String leftPinValue,
                                                      String rightPinValue) {
                        minVideoDimensions = videoDimensionsMap.get(leftPinIndex);
                        maxVideoDimensions = videoDimensionsMap.get(rightPinIndex);
                    }
                });

        videoDimensionsRangeBar.setFormatter(new IRangeBarFormatter() {
            @Override
            public String format(String value) {
                int position = Integer.decode(value) - 1;
                VideoDimensions videoDimensions = videoDimensionsMap.get(position);
                return String.valueOf(videoDimensions.width) + ":"  +
                        String.valueOf(videoDimensions.height);
            }
        });

        String username = getIntent().getExtras().getString(TCCapabilityTokenProvider.USERNAME);
        getSupportActionBar().setTitle(username);

        realm = getIntent().getExtras().getString(TCCapabilityTokenProvider.REALM);
        Map<String, String> privateOptions = createPrivateOptions(realm);
        IceOptions iceOptions = getIceOptionsFromIntent();
        ClientOptionsInternal options = new ClientOptionsInternal(iceOptions, privateOptions);

        // Get the capability token
        capabilityToken = getIntent().getExtras()
                .getString(TCCapabilityTokenProvider.CAPABILITY_TOKEN);
        if(savedInstanceState != null) {
            capabilityToken = savedInstanceState
                    .getString(TCCapabilityTokenProvider.CAPABILITY_TOKEN);
        }

        accessManager = TwilioAccessManagerFactory.createAccessManager(this, capabilityToken,
                accessManagerListener());

        conversationsClient = TwilioConversations.createConversationsClient(accessManager, options,
                conversationsClientListener());


        cameraCapturer = CameraCapturerFactory.createCameraCapturer(
                TCClientActivity.this,
                currentCameraSource,
                capturerErrorListener());

        switchCameraActionFab.setOnClickListener(switchCameraClickListener());

        audioState = AudioState.ENABLED;
        setAudioStateIcon();
        videoState = VideoState.ENABLED;
        setVideoStateIcon();
        setIceOptionsViews();
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        setSpeakerphoneOn(true);
        setCallAction();
        startPreview();
        registerRejectReceiver();
    }



    private IceOptions getIceOptionsFromIntent() {
        String selectedTwilioIceServersJson =
                getIntent().getExtras().getString(TwilioIceServers.ICE_SELECTED_SERVERS);

        //Transform twilio ice servers from json to Set<IceServer>
        List<TwilioIceServer> selectedIceServers =
                IceOptionsHelper.convertToTwilioIceServerList(selectedTwilioIceServersJson);
        Set<IceServer> iceServers = IceOptionsHelper.convertToIceServersSet(selectedIceServers);

        String transPolicyStr =
                getIntent().getExtras().getString(TwilioIceServers.ICE_TRANSPORT_POLICY);
        IceTransportPolicy transPolicy  = IceTransportPolicy.ICE_TRANSPORT_POLICY_ALL;;
        if (transPolicyStr.equalsIgnoreCase("relay") ) {
            transPolicy = IceTransportPolicy.ICE_TRANSPORT_POLICY_RELAY;
        }
        if (iceServers.size() > 0) {
            return new IceOptions(transPolicy, iceServers);
        }
        return new IceOptions(transPolicy);
    }

    private void setIceOptionsViews(){
        iceTransPolicySpinner = (Spinner)findViewById(R.id.ice_trans_policy_spinner);
        ArrayAdapter<CharSequence> iceTransPolicyArrayAdapter = ArrayAdapter.createFromResource(
                this, R.array.ice_trans_policy_array, android.R.layout.simple_spinner_item);
        iceTransPolicyArrayAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        iceTransPolicySpinner.setAdapter(iceTransPolicyArrayAdapter);
        String twilioIceServersJson =
                getIntent().getExtras().getString(TwilioIceServers.ICE_SERVERS);
        List<TwilioIceServer> twilioIceServers =
                IceOptionsHelper.convertToTwilioIceServerList(twilioIceServersJson);
        twilioIceServersListView = (ListView)findViewById(R.id.ice_servers_list_view);

        if (twilioIceServers.size() > 0) {
            IceServerAdapter iceServerAdapter =
                    new IceServerAdapter(this, twilioIceServers);
            twilioIceServersListView.setAdapter(iceServerAdapter);
        }
    }

    @Override
    public void onBackPressed() {
        /**
         * FIXME
         * Again another hack for the fact that there is so much state in this
         * activity. Need to have invites be parcelable so that activities and services
         * can use bundles to pass data around
         */
        moveTaskToBack(true);
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

    private Map<String, String> createPrivateOptions(String realm) {
        Map<String, String> options = new HashMap<>();
        if (realm.equalsIgnoreCase("dev")) {
            options.put(OPTION_REGISTRAR_KEY, OPTION_DEV_REGISTRAR);
            options.put(OPTION_STATS_KEY, OPTION_DEV_STATS_URL);
        } else if (realm.equalsIgnoreCase("stage")) {
            options.put(OPTION_REGISTRAR_KEY, OPTION_STAGE_REGISTRAR);
            options.put(OPTION_STATS_KEY, OPTION_STAGE_STATS_URL);
        }
        return options;
    }

    private void startPreview() {
        if (cameraCapturer != null) {
            cameraCapturer.startPreview(previewFrameLayout);
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

        // Lets unlisten first otherwise complete logout
        if (conversationsClient != null && conversationsClient.isListening()) {
            conversationsClient.unlisten();
        } else {
            completeLogout();
        }
    }

    private void completeLogout() {
        disposeConversationsClient();
        destroyConversationsSdk();
        disposeAccessManager();
        returnToRegistration();
        loggingOut = false;
    }


    private void disposeConversation() {
        if (conversation != null) {
            conversation = null;
        }
    }

    private void disposeConversationsClient() {
        if (conversationsClient != null) {
            conversationsClient = null;
        }
    }

    private void destroyConversationsSdk() {
        TwilioConversations.destroy();
    }

    private void disposeAccessManager() {
        if (accessManager != null) {
            accessManager.dispose();
            accessManager = null;
        }
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
                // If we are logging out let us finish the teardown process
                if (loggingOut) {
                    completeLogout();
                }
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
                            .setText("onIncomingInvite" + incomingInvite.getInviter());
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
                                    .setContentTitle(incomingInvite.getInviter())
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
                            incomingInvite.getInviter() + " terminated", Snackbar.LENGTH_LONG)
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
            /**
             *  The token expiration event notifies the developer 3 minutes before
             *  token actually expires to allow the developer to request a new token
             */
            @Override
            public void onTokenExpired(TwilioAccessManager twilioAccessManager) {
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
                    boolean cameraSwitchSucceeded = cameraCapturer.switchCamera();

                    if (cameraSwitchSucceeded) {
                        // Update the camera source
                        currentCameraSource =
                                (currentCameraSource ==
                                        CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA) ?
                                        (CameraCapturer.CameraSource.CAMERA_SOURCE_BACK_CAMERA) :
                                        (CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA);

                        // Update our local renderer to mirror or not
                        mirrorLocalRenderer = (currentCameraSource ==
                                CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA);

                        // Determine if our renderer is mirroring now
                        if (localRenderer != null) {
                            localRenderer.setMirror(mirrorLocalRenderer);
                        }
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
                    cameraCapturer.startPreview(previewFrameLayout);
                    if (localMedia != null) {
                        localContainer = (ViewGroup)findViewById(R.id.localContainer);
                        LocalVideoTrack videoTrack = createLocalVideoTrack(cameraCapturer);
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
                switchCameraActionFab.setEnabled(videoTrack.isEnabled());
                if (videoTrack.isEnabled()) {
                    pauseActionFab.setImageDrawable(
                            ContextCompat.getDrawable(TCClientActivity.this,
                                    R.drawable.ic_pause_green_24px));
                } else {
                    pauseActionFab.setImageDrawable(
                            ContextCompat.getDrawable(TCClientActivity.this,
                                    R.drawable.ic_pause_red_24px));
                }
            } else {
                Snackbar.make(conversationStatusTextView,
                        "Pause action failed",
                        Snackbar.LENGTH_LONG)
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
                                        Timber.i("Conversation SID " + conversation.getSid());
                                        TCClientActivity.this.conversation = conversation;
                                        conversation.setConversationListener(conversationListener());
                                        if(statsCheckBox.isChecked()) {
                                            enableStats();
                                        }
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

    private void enableStats() {
        if (statsExecutorService != null) {
            statsExecutorService.shutdown();
        }
        statsExecutorService = Executors.newFixedThreadPool(1);
        statsExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if(conversation != null) {
                    conversation.setStatsListener(statsListener());
                }
            }
        });
    }

    private void disableStats() {
        if(conversation != null) {
            conversation.setStatsListener(null);
            if (statsExecutorService != null) {
                statsExecutorService.shutdownNow();
                statsExecutorService = null;
            }
        }
        if(remoteVideoTrackStatsRecordMap != null) {
            remoteVideoTrackStatsRecordMap.clear();
        }
        statsLayout.setVisibility(View.GONE);
        remoteStatsRecyclerView.setVisibility(View.GONE);
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
        alertDialog = Dialog.createInviteDialog(
                incomingInvite.getInviter(),
                incomingInvite.getConversationSid(),
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
                Timber.i("onConversation");
                if (e == null) {
                    Timber.i("Conversation SID " + conversation.getSid());
                    TCClientActivity.this.conversation = conversation;
                    conversation.setConversationListener(conversationListener());
                    if(statsCheckBox.isChecked()) {
                        enableStats();
                    }
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
        if (!isConversationOngoing()) {
            this.incomingInvite = null;
            setCallAction();
        }
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
                        Snackbar.make(conversationStatusTextView, "Invite failed " + conversation.getSid(), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else if(e.getErrorCode() == TwilioConversations.CONVERSATION_REJECTED) {
                        Snackbar.make(conversationStatusTextView, "Invite was rejected " + conversation.getSid(), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }
                conversationStatusTextView.setText(status);
                disableStats();

                // If user is logging out we need to finish that process otherwise we just reset
                if (loggingOut) {
                    logout();
                } else {
                    reset();
                }
            }

        };
    }

    private StatsListener statsListener() {
        return new StatsListener() {
            @Override
            public void onMediaTrackStatsRecord(Conversation conversation, Participant participant, MediaTrackStatsRecord stats) {
                StringBuilder strBld = new StringBuilder();
                strBld.append(
                        String.format("Receiving stats for sid: %s, trackId: %s, direction: %s ",
                                stats.getParticipantSid(), stats.getTrackId(), stats.getDirection()));
                if (stats instanceof LocalAudioTrackStatsRecord) {
                    strBld.append(
                            String.format("media type: audio, bytes sent %d",
                                    ((LocalAudioTrackStatsRecord) stats).getBytesSent()));
                } else if (stats instanceof LocalVideoTrackStatsRecord) {
                    strBld.append(
                            String.format("media type: video, bytes sent %d",
                                    ((LocalVideoTrackStatsRecord) stats).getBytesSent()));
                } else if (stats instanceof RemoteAudioTrackStatsRecord) {
                    strBld.append(
                            String.format("media type: audio, bytes received %d",
                                    ((RemoteAudioTrackStatsRecord) stats).getBytesReceived()));
                } else if (stats instanceof RemoteVideoTrackStatsRecord) {
                    strBld.append(
                            String.format("media type: video, bytes received %d",
                                    ((RemoteVideoTrackStatsRecord) stats).getBytesReceived()));
                } else {
                    strBld.append("Unknown media type");
                }
                Timber.i(strBld.toString());

                if(stats instanceof LocalVideoTrackStatsRecord) {
                    if(statsLayout.getVisibility() != View.VISIBLE) {
                        statsLayout.setVisibility(View.VISIBLE);
                    }
                    if(conversation.getLocalMedia().getLocalVideoTracks().size() > 0) {
                        showLocalVideoTrackStats((LocalVideoTrackStatsRecord) stats);
                    } else {
                        // Latent stats callbacks can be triggered even after a local track is removed.
                        statsLayout.setVisibility(View.GONE);
                    }
                } else if(stats instanceof RemoteVideoTrackStatsRecord) {
                    if(remoteStatsRecyclerView.getVisibility() != View.VISIBLE) {
                        remoteStatsRecyclerView.setVisibility(View.VISIBLE);
                    }
                    if(participant.getMedia().getVideoTracks().size() > 0) {
                        showRemoteVideoTrackStats(conversation, (RemoteVideoTrackStatsRecord)stats);
                    } else {
                        // Latent stats callbacks can be triggered even after a remote track is removed.
                        remoteVideoTrackStatsRecordMap.clear();
                        remoteVideoTrackStatsAdapter.notifyDataSetChanged();
                    }
                }
            }
        };
    }

    private void showLocalVideoTrackStats(LocalVideoTrackStatsRecord localVideoTrackStatsRecord) {
        String localVideoStats =
                String.format("<b>SID</b> %s<br/>", localVideoTrackStatsRecord.getParticipantSid()) +
                        '\n' +
                        String.format("<b>Codec</b> %s<br/>", localVideoTrackStatsRecord.getCodecName()) +
                        '\n' +
                        String.format("<b>Capture Dimensions</b> %s<br/>", localVideoTrackStatsRecord.getCaptureDimensions().toString()) +
                        '\n' +
                        String.format("<b>Sent Dimensions</b> %s<br/>", localVideoTrackStatsRecord.getSentDimensions().toString()) +
                        '\n' +
                        String.format("<b>Fps</b> %d", localVideoTrackStatsRecord.getFrameRate());

        localVideoTrackStatsTextView.setText(Html.fromHtml(localVideoStats));
    }

    private void showRemoteVideoTrackStats(Conversation conversation, RemoteVideoTrackStatsRecord remoteVideoTrackStatsRecord) {
        for(Participant participant: conversation.getParticipants()) {
            if(participant.getSid().equals(remoteVideoTrackStatsRecord.getParticipantSid())) {
                remoteVideoTrackStatsRecordMap.put(participant.getIdentity(), remoteVideoTrackStatsRecord);
                remoteVideoTrackStatsAdapter.notifyDataSetChanged();
                break;
            }
        }
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
                remoteVideoTrackStatsRecordMap.remove(participant.getIdentity());
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
                statsLayout.setVisibility(View.GONE);
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

        disposeConversation();
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
                accessManager.getIdentity(), realm, new Callback<String>() {

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
            LocalVideoTrack videoTrack = createLocalVideoTrack(cameraCapturer);
            localMedia.addLocalVideoTrack(videoTrack);
        }
        if (audioState == AudioState.ENABLED) {
            localMedia.addMicrophone();
        } else {
            localMedia.removeMicrophone();
        }
        return localMedia;
    }

    private LocalVideoTrack createLocalVideoTrack(CameraCapturer cameraCapturer) {
        if(videoConstraints == null) {
            return LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer);
        } else {
            return LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer, videoConstraints);
        }
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
