package com.twilio.conversations.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.os.Handler;

import com.twilio.conversations.AudioTrack;
import com.twilio.conversations.Conversation;
import com.twilio.conversations.IceOptions;
import com.twilio.conversations.IceServer;
import com.twilio.conversations.IceTransportPolicy;
import com.twilio.conversations.StatsListener;
import com.twilio.conversations.MediaTrackStatsRecord;
import com.twilio.conversations.TwilioConversationsClient;
import com.twilio.conversations.TwilioConversationsException;
import com.twilio.conversations.LocalMedia;
import com.twilio.conversations.LocalVideoTrack;
import com.twilio.conversations.MediaTrackState;
import com.twilio.conversations.Participant;
import com.twilio.conversations.TrackOrigin;
import com.twilio.conversations.VideoConstraints;
import com.twilio.conversations.VideoRenderer;
import com.twilio.conversations.VideoTrack;
import com.twilio.conversations.impl.core.ConversationStateObserver;
import com.twilio.conversations.impl.core.ConversationStatus;
import com.twilio.conversations.impl.core.CoreError;
import com.twilio.conversations.impl.core.CoreSession;
import com.twilio.conversations.impl.core.CoreSessionMediaConstraints;
import com.twilio.conversations.impl.core.DisconnectReason;
import com.twilio.conversations.impl.core.MediaStreamInfo;
import com.twilio.conversations.impl.core.SessionObserver;
import com.twilio.conversations.impl.core.SessionState;
import com.twilio.conversations.impl.core.TrackInfo;
import com.twilio.conversations.impl.core.CoreTrackStatsReport;
import com.twilio.conversations.impl.logging.Logger;

public class ConversationImpl implements Conversation,
        NativeHandleInterface, SessionObserver, CoreSession {
    private static final String DISPOSED_MESSAGE = "The conversation has been destroyed. " +
            "This operation is no longer valid";
    private Set<String> invitedParticipants = new HashSet<>();
    private String inviter;
    private TwilioConversationsClientInternal conversationsClientInternal;
    private Conversation.Listener conversationListener;
    private ConversationStateObserver conversationStateObserver;
    private Map<String,ParticipantImpl> participantMap = new HashMap<>();
    private LocalMediaImpl localMediaImpl;
    private CameraCapturerImpl cameraCapturer;
    private Handler handler;
    private IncomingInviteImpl incomingInviteImpl;
    private OutgoingInviteImpl outgoingInviteImpl;
    private StatsListener statsListener;

    private static String TAG = "ConversationImpl";

    static final Logger logger = Logger.getLogger(ConversationImpl.class);
    private SessionState state;
    private ConversationStatus conversationStatus;
    private String conversationSid;

    class SessionObserverInternal implements NativeHandleInterface {

        private long nativeSessionObserver;

        public SessionObserverInternal(SessionObserver sessionObserver,
                                       Conversation conversation) {
            this.nativeSessionObserver = wrapNativeObserver(sessionObserver, conversation);
        }

        public void enableStats(long nativeSession, boolean enable) {
            enableStats(nativeSessionObserver, nativeSession, enable);
        }

        private native long wrapNativeObserver(SessionObserver sessionObserver,
                                               Conversation conversation);
        private native void freeNativeObserver(long nativeSessionObserver);

        private native void enableStats(long nativeSessionObserver, long nativeSession, boolean enable);

        @Override
        public long getNativeHandle() {
            return nativeSessionObserver;
        }

        public void dispose() {
            if (nativeSessionObserver != 0) {
                freeNativeObserver(nativeSessionObserver);
                nativeSessionObserver = 0;
            }

        }

    }

    private SessionObserverInternal sessionObserverInternal;
    private long nativeSession;

    /*
     * Outgoing invite
     */
    private ConversationImpl(TwilioConversationsClientInternal conversationsClientInternal,
                             Set<String> participants,
                             LocalMedia localMedia,
                             Conversation.Listener conversationListener,
                             ConversationStateObserver conversationStateObserver,
                             Handler handler) {
        this.conversationsClientInternal = conversationsClientInternal;
        this.invitedParticipants = participants;

        String[] participantIdentityArray = new String[participants.size()];
        int i = 0;
        for(String participant : participants) {
            participantIdentityArray[i++] = participant;
        }

        this.handler = handler;
        if(handler == null) {
            throw new IllegalThreadStateException("This thread must be able to obtain a Looper");
        }

        localMediaImpl = (LocalMediaImpl)localMedia;
        localMediaImpl.setConversation(this);

        this.conversationListener = conversationListener;
        this.conversationStateObserver = conversationStateObserver;

        sessionObserverInternal = new SessionObserverInternal(this, this);

        nativeSession = wrapOutgoingSession(conversationsClientInternal.getNativeHandle(),
                sessionObserverInternal.getNativeHandle(),
                participantIdentityArray);
    }

    /*
     * The outgoing invite does not have a sid until it is accepted. As a result,
     * the ConversationClientImpl will call this to retain the conversation sid
     * when the conversation becomes valid.
     */
    void retainSid() {
        conversationSid = getConversationSid(nativeSession);
    }

    /*
     * Incoming invite
     */
    private ConversationImpl(TwilioConversationsClientInternal conversationsClientInternal,
                             long nativeSession,
                             String[] participantsIdentities,
                             ConversationStateObserver conversationStateObserver,
                             Handler handler) {
        this.conversationsClientInternal = conversationsClientInternal;
        this.conversationStateObserver = conversationStateObserver;
        this.nativeSession = nativeSession;

        // The sid is available from the session when an incoming invite is provided
        conversationSid = getConversationSid(nativeSession);

        inviter = participantsIdentities[0];

        this.handler = handler;
        if(handler == null) {
            throw new IllegalThreadStateException("This thread must be able to obtain a Looper");
        }

        for (String participantIdentity : participantsIdentities) {
            findOrCreateParticipant(participantIdentity, null);
            invitedParticipants.add(participantIdentity);


        }
        sessionObserverInternal = new SessionObserverInternal(this, this);
        setSessionObserver(nativeSession, sessionObserverInternal.getNativeHandle());
    }

    public static ConversationImpl createOutgoingConversation(
            TwilioConversationsClientInternal conversationsClient,
            Set<String> participants,
            LocalMedia localMedia,
            Conversation.Listener listener,
            ConversationStateObserver conversationStateObserver,
            Handler handler) {
        ConversationImpl conversationImpl = new ConversationImpl(conversationsClient,
                participants, localMedia, listener, conversationStateObserver, handler);
        return conversationImpl;
    }

    public static ConversationImpl createIncomingConversation(
            TwilioConversationsClientInternal conversationsClientInternal,
            long nativeSession,
            String[] participantIdentities,
            ConversationStateObserver conversationStateObserver,
            Handler handler) {

        if (nativeSession == 0) {
            return null;
        }
        if (participantIdentities == null || participantIdentities.length == 0) {
            return null;
        }
        ConversationImpl conversationImpl = new ConversationImpl(conversationsClientInternal,
                nativeSession, participantIdentities, conversationStateObserver, handler);
        return conversationImpl;
    }

    public boolean isActive() {
        return ((state == SessionState.STARTING) ||
                (state == SessionState.STOPPING) ||
                (state == SessionState.IN_PROGRESS) ||
                (state == SessionState.STOP_FAILED));
    }

    @Override
    public Set<Participant> getParticipants() {
        Set<Participant> participants =
                new HashSet<Participant>(participantMap.values());
        return participants;
    }

    Set<String> getInvitedParticipants() {
        return invitedParticipants;
    }

    String getInviter() {
        return inviter;
    }

    @Override
    public LocalMedia getLocalMedia() {
        checkDisposed();
        return localMediaImpl;
    }

    @Override
    public Conversation.Listener getConversationListener() {
        return conversationListener;
    }

    @Override
    public void setConversationListener(Conversation.Listener listener) {
        if(handler == null) {
            throw new IllegalThreadStateException("This thread must be able to obtain a Looper");
        }
        this.conversationListener = listener;
    }

    @Override
    public void invite(Set<String> participantIdentities) throws IllegalArgumentException {
        checkDisposed();
        if ((participantIdentities == null) || (participantIdentities.size() == 0)) {
            throw new IllegalArgumentException("participantIdentities cannot be null or empty");
        }
        inviteParticipants(participantIdentities);
    }

    @Override
    public void disconnect() {
        if(nativeSession != 0) {
            stop();
        }
    }

    @Override
    public String getSid() {
        return conversationSid;
    }

    @Override
    public StatsListener getStatsListener() {
        return statsListener;
    }

    @Override
    public void setStatsListener(StatsListener listener) {
        statsListener = listener;
        if (listener != null) {
            if (sessionObserverInternal != null) {
                sessionObserverInternal.enableStats(nativeSession, true);
            }
        } else {
            if (sessionObserverInternal != null) {
                sessionObserverInternal.enableStats(nativeSession, false);
            }
        }
    }

    private ParticipantImpl findOrCreateParticipant(String participantIdentity, String participantSid) {
        ParticipantImpl participant = participantMap.get(participantIdentity);
        if(participant == null) {
            logger.d("Creating new participant" + participantIdentity);
            if(participantSid == null) {
                logger.w("Participant sid was null");
            }
            participant = new ParticipantImpl(participantIdentity, participantSid);
            participantMap.put(participantIdentity, participant);
        } else if(participant != null && participant.getSid() == null) {
            /*
             * Incoming invites do not provide the participant sid. The sid becomes
             * available when the onParticipantConnect event is fired.
             */
            logger.d("Participant sid added for " + participantIdentity);
            participant.setSid(participantSid);
        }
        return participant;
    }

    /**
     * SessionObserver events
     */
    @Override
    public void onSessionStateChanged(SessionState state) {
        logger.i("state changed to: " + state.name());

        ConversationStatus newConversationStatus = sessionStateToStatus(state, conversationStatus);
        this.state = state;

        if(conversationStatus != newConversationStatus) {
            conversationStatus = newConversationStatus;
            conversationStateObserver.onConversationStatusChanged(ConversationImpl.this, conversationStatus);

            // TODO GSDK-492 multi-invite behavior
        }

    }

    SessionState getSessionState() {
        return state;
    }

    @Override
    public void onStartCompleted(CoreError error) {
        log("onStartCompleted", error);

        if(error != null) {
            // Remove this conversation from the client
            conversationsClientInternal.removeConversation(this);
            participantMap.clear();

            final TwilioConversationsException e =
                    new TwilioConversationsException(error.getCode(), error.getMessage());
            if(conversationListener == null) {
                if(e.getErrorCode() == TwilioConversationsClient.CONVERSATION_TERMINATED) {
                    conversationsClientInternal.onConversationTerminated(this, e);
                }
            } else if(handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        conversationListener.onConversationEnded(ConversationImpl.this, e);
                    }
                });
            }
            disposeConversation();
        }
    }

    @Override
    public void onStopCompleted(CoreError error) {
        /**
         * Note that we are not using a latch here because of deadlock situation revealed in
         * GSDK-598
         */
        log("onStopCompleted", error);

        // Remove this conversation from the client
        conversationsClientInternal.removeConversation(this);
        // Conversations that are rejected do not have a listener
        if(conversationListener != null) {
            participantMap.clear();
            if (error == null) {
                if (handler != null && conversationListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            conversationListener.onConversationEnded(ConversationImpl.this, null);
                        }
                    });
                }
            } else {
                final TwilioConversationsException e =
                        new TwilioConversationsException(error.getCode(),
                                error.getMessage());
                if (handler != null && conversationListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            conversationListener.onConversationEnded(ConversationImpl.this, e);
                        }
                    });
                }
            }
        }
        disposeConversation();
    }

    @Override
    public void onParticipantConnected(String participantIdentity,
                                       String participantSid,
                                       CoreError error) {
        log("onParticipantConnected",  participantIdentity, error);
        final ParticipantImpl participantImpl = findOrCreateParticipant(participantIdentity,
                participantSid);
        if (error == null) {
            if(handler != null && conversationListener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        conversationListener.onParticipantConnected(ConversationImpl.this,
                                participantImpl);
                    }
                });
            }
        } else {
            final TwilioConversationsException e = new TwilioConversationsException(error.getCode(),
                    error.getMessage());
            if (conversationListener == null) {
                if(e.getErrorCode() == TwilioConversationsClient.CONVERSATION_TERMINATED) {
                    conversationsClientInternal.onConversationTerminated(this, e);
                } else {
                    logger.e("onParticipantConnected -> received unexpected error code -> " +
                            e.getErrorCode());
                }
            } else if(handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        conversationListener.onFailedToConnectParticipant(ConversationImpl.this,
                                participantImpl, e);
                    }
                });
            }
        }
    }

    @Override
    public void onParticipantDisconnected(final String participantIdentity, String participantSid, final DisconnectReason reason) {
        log("onParticipantDisconnected", participantIdentity, reason);
        final ParticipantImpl participant = participantMap.remove(participantIdentity);
        if(participant == null) {
            logger.i("participant removed but was never in list");
        } else {
            if(handler != null && conversationListener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        conversationListener.onParticipantDisconnected(ConversationImpl.this, participant);
                    }
                });
            }
        }
    }

    @Override
    public void onMediaStreamAdded(MediaStreamInfo stream) {
        log("onMediaStreamAdded", stream.getParticipantAddress() + " " + stream.getStreamId());
    }

    @Override
    public void onMediaStreamRemoved(MediaStreamInfo stream) {
        log("onMediaStreamRemoved", stream.getParticipantAddress() + " " + stream.getStreamId());
    }

    @Override
    public void onVideoTrackAdded(final TrackInfo trackInfo, final org.webrtc.VideoTrack webRtcVideoTrack) {
        log("onVideoTrackAdded", trackInfo.getParticipantIdentity() + " " + trackInfo.getTrackId() + " " + trackInfo.isEnabled());

        if(trackInfo.getTrackOrigin() == TrackOrigin.LOCAL) {
            List<LocalVideoTrack> tracksList = localMediaImpl.getLocalVideoTracks();
            final LocalVideoTrackImpl videoTrackImpl = (LocalVideoTrackImpl)tracksList.get(0);
            videoTrackImpl.setWebrtcVideoTrack(webRtcVideoTrack);
            videoTrackImpl.setTrackInfo(trackInfo);
            if (localMediaImpl.getHandler() != null) {
                localMediaImpl.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (localMediaImpl.getLocalMediaListener() != null) {
                            localMediaImpl.getLocalMediaListener().onLocalVideoTrackAdded(
                                    localMediaImpl, videoTrackImpl);
                        }
                    }
                });
            }
        } else {
            final ParticipantImpl participantImpl = findOrCreateParticipant(trackInfo.getParticipantIdentity(), null);
            final VideoTrackImpl videoTrackImpl = new VideoTrackImpl(webRtcVideoTrack, trackInfo);
            participantImpl.getMediaImpl().addVideoTrack(videoTrackImpl);
            if(handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (participantImpl.getParticipantListener() != null) {
                            participantImpl.getParticipantListener().onVideoTrackAdded(
                                    ConversationImpl.this, participantImpl, videoTrackImpl);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onVideoTrackFailedToAdd(final TrackInfo trackInfo, CoreError error) {
        log("onVideoFailedToAdd", trackInfo.getParticipantIdentity() + " " + trackInfo.getTrackId() + trackInfo.isEnabled(), error);
        if(trackInfo.getTrackOrigin().equals(TrackOrigin.LOCAL)) {
            // Remove local video track
            final LocalVideoTrackImpl localVideoTrackImpl = (LocalVideoTrackImpl)localMediaImpl.getLocalVideoTracks().remove(0);
            List<VideoRenderer> renderers = new ArrayList<VideoRenderer>(localVideoTrackImpl.getRenderers());
            // Remove renderers
            for(VideoRenderer renderer: renderers) {
                localVideoTrackImpl.removeRenderer(renderer);
            }
            localVideoTrackImpl.removeCameraCapturer();

            final TwilioConversationsException e =
                    new TwilioConversationsException(error.getCode(), error.getMessage());

            if(localMediaImpl.getHandler() != null) {
                localMediaImpl.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        if(localMediaImpl.getLocalMediaListener() != null) {
                            localMediaImpl.getLocalMediaListener().onLocalVideoTrackError(localMediaImpl, localVideoTrackImpl, e);
                        }
                    }
                });
            }
        } else {
            logger.w("Remote track failed to add unexpectedly");
        }
    }

    @Override
    public void onVideoTrackRemoved(final TrackInfo trackInfo) {
        log("onVideoTrackRemoved", trackInfo.getParticipantIdentity() + " " + trackInfo.getTrackId() + " " + trackInfo.isEnabled());
        if (trackInfo.getTrackOrigin() == TrackOrigin.LOCAL) {
            final LocalVideoTrackImpl localVideoTrackImpl =
                    localMediaImpl.removeLocalVideoTrack(trackInfo);
            localVideoTrackImpl.removeCameraCapturer();
            localVideoTrackImpl.setTrackState(MediaTrackState.ENDED);
            if(localMediaImpl.getHandler() != null) {
                localMediaImpl.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (localMediaImpl.getLocalMediaListener() != null) {
                            localMediaImpl.getLocalMediaListener().onLocalVideoTrackRemoved(localMediaImpl, localVideoTrackImpl);
                        }
                    }
                });
            }
        } else {
            final ParticipantImpl participantImpl = findOrCreateParticipant(trackInfo.getParticipantIdentity(), null);
            final VideoTrackImpl videoTrackImpl = participantImpl.getMediaImpl().removeVideoTrack(trackInfo);
            videoTrackImpl.setTrackState(MediaTrackState.ENDED);
            if(handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (participantImpl.getParticipantListener() != null) {
                            participantImpl.getParticipantListener().onVideoTrackRemoved(ConversationImpl.this, participantImpl, videoTrackImpl);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onVideoTrackStateChanged(final TrackInfo trackInfo) {
        log("onVideoTrackStateChanged", trackInfo.getParticipantIdentity() + " " + trackInfo.getTrackId() + " " + trackInfo.isEnabled());
        if(trackInfo.getTrackOrigin() == TrackOrigin.LOCAL) {
            return;
        } else {
            final ParticipantImpl participantImpl = findOrCreateParticipant(trackInfo.getParticipantIdentity(), null);
            List<VideoTrack> videoTracks = participantImpl.getMediaImpl().getVideoTracks();
            for(final VideoTrack videoTrack: videoTracks) {
                if(trackInfo.getTrackId().equals(videoTrack.getTrackId())) {
                    ((VideoTrackImpl)videoTrack).updateTrackInfo(trackInfo);
                    if(handler != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (participantImpl.getParticipantListener() != null) {
                                    if(trackInfo.isEnabled()) {
                                        participantImpl.getParticipantListener().onTrackEnabled(ConversationImpl.this, participantImpl, videoTrack);
                                    } else {
                                        participantImpl.getParticipantListener().onTrackDisabled(ConversationImpl.this, participantImpl, videoTrack);
                                    }
                                }
                            }
                        });
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onAudioTrackAdded(TrackInfo trackInfo, final org.webrtc.AudioTrack webRtcAudioTrack) {
        log("onAudioTrackAdded", trackInfo.getParticipantIdentity() + " " + trackInfo.getTrackId() + " " + trackInfo.isEnabled());

        if(trackInfo.getTrackOrigin() == TrackOrigin.LOCAL) {
            // TODO: expose audio tracks in local media
        } else {
            final ParticipantImpl participantImpl = findOrCreateParticipant(trackInfo.getParticipantIdentity(), null);
            final AudioTrackImpl audioTrackImpl = new AudioTrackImpl(webRtcAudioTrack, trackInfo);
            participantImpl.getMediaImpl().addAudioTrack(audioTrackImpl);
            if(handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (participantImpl.getParticipantListener() != null) {
                            participantImpl.getParticipantListener().onAudioTrackAdded(
                                    ConversationImpl.this, participantImpl, audioTrackImpl);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onAudioTrackRemoved(TrackInfo trackInfo) {
        log("onAudioTrackRemoved", trackInfo.getParticipantIdentity() + " " + trackInfo.getTrackId() + " " + trackInfo.isEnabled());

        if(trackInfo.getTrackOrigin() == TrackOrigin.LOCAL) {
            // TODO: remove audio track from local media once audio tracks are exposed
        } else {
            final ParticipantImpl participantImpl = findOrCreateParticipant(trackInfo.getParticipantIdentity(), null);
            final AudioTrackImpl audioTrackImpl = participantImpl.getMediaImpl().removeAudioTrack(trackInfo);
            audioTrackImpl.setTrackState(MediaTrackState.ENDED);
            if(handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (participantImpl.getParticipantListener() != null) {
                            participantImpl.getParticipantListener().onAudioTrackRemoved(ConversationImpl.this, participantImpl, audioTrackImpl);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onAudioTrackStateChanged(final TrackInfo trackInfo) {
        log("onAudioTrackStateChanged", trackInfo.getParticipantIdentity() + " " + trackInfo.getTrackId() + " " + trackInfo.isEnabled());
        if(trackInfo.getTrackOrigin() == TrackOrigin.LOCAL) {
            return;
        } else {
            final ParticipantImpl participantImpl = findOrCreateParticipant(trackInfo.getParticipantIdentity(), null);
            List<AudioTrack> audioTracks = participantImpl.getMediaImpl().getAudioTracks();
            for(final AudioTrack audioTrack: audioTracks) {
                if(trackInfo.getTrackId().equals(audioTrack.getTrackId())) {
                    ((AudioTrackImpl)audioTrack).updateTrackInfo(trackInfo);
                    if(handler != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (participantImpl.getParticipantListener() != null) {
                                    if(trackInfo.isEnabled()) {
                                        participantImpl.getParticipantListener().onTrackEnabled(ConversationImpl.this, participantImpl, audioTrack);
                                    } else {
                                        participantImpl.getParticipantListener().onTrackDisabled(ConversationImpl.this, participantImpl, audioTrack);
                                    }
                                }
                            }
                        });
                    }
                    break;
                }
            }

        }
    }

    @Override
    public void onReceiveTrackStatistics(CoreTrackStatsReport report) {
        if (handler != null && statsListener != null) {
            final MediaTrackStatsRecord stats = MediaTrackStatsRecordFactory.create(report);

            if (stats != null) {
                /*
                 * Do not report stats until the participant sid of this participant is available
                 * on both the conversation and the media stats record.
                 * It will become available when the onParticipantConnected event is triggered.
                 */
                for(final Participant participant: getParticipants()) {
                    if(participant.getSid() != null && stats.getParticipantSid() != null &&
                            participant.getSid().equals(stats.getParticipantSid())) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                statsListener.onMediaTrackStatsRecord(
                                        ConversationImpl.this, participant, stats);
                            }
                        });
                        return;
                    }
                }
                logger.d("stats report skipped since the participant sid has not been set yet");
            }
        }
    }

    void log(String method, String message, CoreError coreError) {
        logger.d("session(" + method + ":" + getCoreError(coreError) + ")" + message);
    }

    void log(String method, String message, DisconnectReason reason) {
        logger.d("session(" + method + ":disconnect:" + String.valueOf(reason) + ")" + message);
    }

    void log(String method, String message) {
        logger.d("session(" + method + ")" + message);
    }

    void log(String method, CoreError coreError) {
        logger.d("session(" + method + ")" + getCoreError(coreError));
    }

    String getCoreError(CoreError coreError) {
        if(coreError != null) {
            return coreError.getDomain() + ":" + coreError.getCode() + ":" + coreError.getMessage();
        } else {
            return "";
        }
    }

    IncomingInviteImpl getIncomingInviteImpl() {
        return incomingInviteImpl;
    }

    OutgoingInviteImpl getOutgoingInviteImpl() {
        return outgoingInviteImpl;
    }

    void setIncomingInviteImpl(IncomingInviteImpl incomingInviteImpl) {
        this.incomingInviteImpl = incomingInviteImpl;
    }

    void setOutgoingInviteImpl(OutgoingInviteImpl outgoingInviteImpl) {
        this.outgoingInviteImpl = outgoingInviteImpl;
    }

    /**
     * NativeHandleInterface
     */
    @Override
    public long getNativeHandle() {
        return nativeSession;
    }

    public void setLocalMedia(LocalMedia media) {
        checkDisposed();
        localMediaImpl = (LocalMediaImpl)media;
        localMediaImpl.setConversation(this);
    }

    private ConversationStatus sessionStateToStatus(SessionState state,
                                                    ConversationStatus conversationStatus) {
        switch(state) {
            case INITIALIZED:
                return ConversationStatus.INITIALIZED;
            case STARTING:
                return ConversationStatus.CONNECTING;
            case IN_PROGRESS:
            case STOP_FAILED:
                return ConversationStatus.CONNECTED;
            case STOPPING:
                // Keep the existing status
                return conversationStatus;
            case STOPPED:
                return ConversationStatus.DISCONNECTED;
            case START_FAILED:
                return ConversationStatus.FAILED;
            default:
                return ConversationStatus.INITIALIZED;
        }
    }

    void setupExternalCapturer() {
        LocalVideoTrack localVideoTrack = localMediaImpl.getLocalVideoTracks().get(0);
        // TODO: Camera capture is the only supported local video stream for now.
        // Once we start supporting screen share or etc, we should modify this method.
        cameraCapturer = (CameraCapturerImpl)localVideoTrack.getCameraCapturer();
        cameraCapturer.startConversationCapturer(nativeSession);
        setExternalCapturer(nativeSession, cameraCapturer.getNativeVideoCapturer());
    }

    boolean mute(boolean on) {
        return mute(nativeSession, on);
    }

    boolean isMuted() {
        return isMuted(nativeSession);
    }


    CoreSessionMediaConstraints createMediaConstrains(IceOptions iceOptions) {
        LocalMedia localMedia = getLocalMedia();
        boolean enableVideo = !localMedia.getLocalVideoTracks().isEmpty();
        boolean pauseVideo = false;
        if (enableVideo) {
            pauseVideo = !localMedia.getLocalVideoTracks().get(0).isEnabled();
        }
        return new CoreSessionMediaConstraints(localMedia.isMicrophoneAdded(),
                        localMedia.isMuted(), enableVideo, pauseVideo, iceOptions);

    }

    /**
     * CoreSession
     */
    @Override
    public void start(final CoreSessionMediaConstraints mediaConstraints) {
        logger.d("starting call");

		/*
		 * Determine the media constraints
		 */
        final VideoConstraints videoConstraints;
        LocalMedia localMedia = getLocalMedia();
        if(mediaConstraints.isVideoEnabled()) {
            setupExternalCapturer();
            LocalVideoTrackImpl localVideoTrackImpl = (LocalVideoTrackImpl)localMedia.getLocalVideoTracks().get(0);
            videoConstraints = localVideoTrackImpl.getVideoConstraints();
        } else {
            videoConstraints = null;
        }

        IceOptions iceOptions = mediaConstraints.getIceOptions();
        final IceTransportPolicy policy = (iceOptions != null) ?
                iceOptions.iceTransportPolicy : IceTransportPolicy.ICE_TRANSPORT_POLICY_ALL;

		/*
		 * Retain the session pointer since it can be reset before the
		 * new thread references it.
		 */
        final long retainedNativeSession = nativeSession;
        start(retainedNativeSession,
                mediaConstraints.isAudioEnabled(),
                mediaConstraints.isAudioMuted(),
                mediaConstraints.isVideoEnabled(),
                mediaConstraints.isVideoPaused(),
                videoConstraints,
                mediaConstraints.getIceServersArray(),
                policy);

    }

    @Override
    public void stop() {
		/*
		 * Retain the session pointer since it can be reset before the
		 * new thread references it.
		 */
        final long retainNativeSession = nativeSession;
        stop(retainNativeSession);
    }

    @Override
    public boolean enableVideo(boolean enabled, boolean paused, VideoConstraints videoConstraints) {
        return enableVideo(nativeSession, enabled, paused, videoConstraints);
    }

    @Override
    public void inviteParticipants(Set<String> participants) {
        String[] participantIdentityArray =
                participants.toArray(new String[participants.size()]);
        inviteParticipants(nativeSession, participantIdentityArray);
    }

    @Override
    public boolean enableAudio(boolean enabled, boolean muted) {
        return enableAudio(nativeSession, enabled, muted);
    }

    private void disposeConversation() {
        if (sessionObserverInternal != null) {
            sessionObserverInternal.dispose();
            sessionObserverInternal = null;
        }
        if (nativeSession != 0) {
            freeNativeHandle(nativeSession);
            nativeSession = 0;
        }
        if(conversationsClientInternal.getActiveConversationsCount() == 0) {
            EglBaseProvider.releaseEglBase();
        }
        if (cameraCapturer != null) {
            cameraCapturer.dispose();
            cameraCapturer = null;
        }
    }

    private synchronized void checkDisposed() {
        if (nativeSession == 0) {
            throw new IllegalStateException(DISPOSED_MESSAGE);
        }
    }

    private native long wrapOutgoingSession(long nativeEndpoint,
                                            long nativeSessionObserver,
                                            String[] participants);
    private native void start(long nativeSession,
                              boolean enableAudio,
                              boolean muteAudio,
                              boolean enableVideo,
                              boolean pauseVideo,
                              VideoConstraints videoConstraints,
                              IceServer[] iceServers,
                              IceTransportPolicy iceTransportPolicy);

    private native void setExternalCapturer(long nativeSession, long nativeCapturer);
    private native void stop(long nativeSession);
    private native void setSessionObserver(long nativeSession, long nativeSessionObserver);
    private native void freeNativeHandle(long nativeHandle);
    private native boolean enableVideo(long nativeHandle, boolean enabled, boolean paused, VideoConstraints videoConstraints);
    private native boolean mute(long nativeSession, boolean on);
    private native boolean isMuted(long nativeSession);
    private native void inviteParticipants(long nativeHandle, String[] participants);
    private native String getConversationSid(long nativeHandle);
    private native boolean enableAudio(long nativeHandle, boolean enabled, boolean muted);
}
