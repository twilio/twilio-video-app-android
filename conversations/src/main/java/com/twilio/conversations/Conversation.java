package com.twilio.conversations;

import android.os.Handler;

import com.twilio.conversations.internal.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Conversation represents communication between the client and one or more participants.
 */
public class Conversation {
    private static final String DISPOSED_MESSAGE = "The conversation has been destroyed. " +
            "This operation is no longer valid";
    private Set<String> invitedParticipants = new HashSet<>();
    private String inviter;
    private TwilioConversationsClientInternal conversationsClientInternal;
    private Conversation.Listener conversationListener;
    private ConversationStateObserver conversationStateObserver;
    private Map<String, Participant> participantMap = new HashMap<>();
    private LocalMedia localMedia;
    private CameraCapturer cameraCapturer;
    private Handler handler;
    private IncomingInvite incomingInvite;
    private OutgoingInvite outgoingInvite;
    private StatsListener statsListener;

    private static String TAG = "Conversation";

    static final Logger logger = Logger.getLogger(Conversation.class);
    private SessionState state;
    private ConversationStatus conversationStatus;
    private String conversationSid;

    class SessionObserverInternal implements NativeHandleInterface {
        private long nativeSessionObserver;
        private final Conversation conversation;
        private final SessionObserver sessionObserver = new SessionObserver() {
            @Override
            public void onSessionStateChanged(SessionState state) {
                logger.i("state changed to: " + state.name());

                ConversationStatus newConversationStatus = sessionStateToStatus(state,
                        conversationStatus);
                conversation.state = state;

                if(conversationStatus != newConversationStatus) {
                    conversationStatus = newConversationStatus;
                    conversationStateObserver.onConversationStatusChanged(conversation,
                            conversationStatus);

                    // TODO GSDK-492 multi-invite behavior
                }
            }

            @Override
            public void onStartCompleted(CoreError error) {
                log("onStartCompleted", error);

                if(error != null) {
                    // Remove this conversation from the client
                    conversationsClientInternal.removeConversation(conversation);
                    participantMap.clear();

                    final TwilioConversationsException e =
                            new TwilioConversationsException(error.getCode(), error.getMessage());
                    if(conversationListener == null) {
                        if(e.getErrorCode() == TwilioConversationsClient.CONVERSATION_TERMINATED) {
                            conversationsClientInternal.onConversationTerminated(conversation, e);
                        }
                    } else if(handler != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                conversationListener.onConversationEnded(conversation, e);
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
                conversationsClientInternal.removeConversation(conversation);
                // Conversations that are rejected do not have a listener
                if(conversationListener != null) {
                    participantMap.clear();
                    if (error == null) {
                        if (handler != null && conversationListener != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    conversationListener.onConversationEnded(conversation, null);
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
                                    conversationListener.onConversationEnded(conversation, e);
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
                final Participant participant = findOrCreateParticipant(participantIdentity,
                        participantSid);
                if (error == null) {
                    if(handler != null && conversationListener != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                conversationListener.onParticipantConnected(conversation,
                                        participant);
                            }
                        });
                    }
                } else {
                    final TwilioConversationsException e =
                            new TwilioConversationsException(error.getCode(), error.getMessage());
                    if (conversationListener == null) {
                        if(e.getErrorCode() == TwilioConversationsClient.CONVERSATION_TERMINATED) {
                            conversationsClientInternal.onConversationTerminated(conversation, e);
                        } else {
                            logger.e("onParticipantConnected -> received unexpected error code -> " +
                                    e.getErrorCode());
                        }
                    } else if(handler != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                conversationListener.onFailedToConnectParticipant(conversation,
                                        participant, e);
                            }
                        });
                    }
                }
            }

            @Override
            public void onParticipantDisconnected(String participantIdentity,
                                                  String participantSid,
                                                  DisconnectReason reason) {
                log("onParticipantDisconnected", participantIdentity, reason);
                final Participant participant = participantMap.remove(participantIdentity);
                if(participant == null) {
                    logger.i("participant removed but was never in list");
                } else {
                    if(handler != null && conversationListener != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                conversationListener.onParticipantDisconnected(conversation,
                                        participant);
                            }
                        });
                    }
                }
            }

            @Override
            public void onMediaStreamAdded(MediaStreamInfo stream) {
                log("onMediaStreamAdded", stream.getParticipantAddress() + " " +
                        stream.getStreamId());
            }

            @Override
            public void onMediaStreamRemoved(MediaStreamInfo stream) {
                log("onMediaStreamRemoved", stream.getParticipantAddress() + " " +
                        stream.getStreamId());
            }

            @Override
            public void onVideoTrackAdded(TrackInfo trackInfo,
                                          org.webrtc.VideoTrack webRtcVideoTrack) {
                log("onVideoTrackAdded", trackInfo.getParticipantIdentity() + " " +
                        trackInfo.getTrackId() + " " + trackInfo.isEnabled());

                if(trackInfo.getTrackOrigin() == TrackOrigin.LOCAL) {
                    List<LocalVideoTrack> tracksList = localMedia.getLocalVideoTracks();
                    final LocalVideoTrack videoTrack = tracksList.get(0);
                    videoTrack.setWebrtcVideoTrack(webRtcVideoTrack);
                    videoTrack.setTrackInfo(trackInfo);
                    if (localMedia.getHandler() != null) {
                        localMedia.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                if (localMedia.getLocalMediaListener() != null) {
                                    localMedia.getLocalMediaListener().onLocalVideoTrackAdded(localMedia,
                                            videoTrack);
                                }
                            }
                        });
                    }
                } else {
                    final Participant participant = findOrCreateParticipant(trackInfo
                            .getParticipantIdentity(), null);
                    final VideoTrack videoTrack = new VideoTrack(webRtcVideoTrack, trackInfo);
                    participant.getMedia().addVideoTrack(videoTrack);
                    if(handler != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (participant.getParticipantListener() != null) {
                                    participant.getParticipantListener().onVideoTrackAdded(
                                            conversation, participant, videoTrack);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onVideoTrackFailedToAdd(TrackInfo trackInfo, CoreError error) {
                log("onVideoFailedToAdd", trackInfo.getParticipantIdentity() + " " +
                        trackInfo.getTrackId() + trackInfo.isEnabled(), error);
                if(trackInfo.getTrackOrigin().equals(TrackOrigin.LOCAL)) {
                    // Remove local video track
                    final LocalVideoTrack localVideoTrack = localMedia.getLocalVideoTracks().remove(0);
                    List<VideoRenderer> renderers = new ArrayList<>(localVideoTrack.getRenderers());
                    // Remove renderers
                    for(VideoRenderer renderer: renderers) {
                        localVideoTrack.removeRenderer(renderer);
                    }
                    localVideoTrack.removeCameraCapturer();

                    final TwilioConversationsException e =
                            new TwilioConversationsException(error.getCode(), error.getMessage());

                    if(localMedia.getHandler() != null) {
                        localMedia.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                if(localMedia.getLocalMediaListener() != null) {
                                    localMedia.getLocalMediaListener().onLocalVideoTrackError(localMedia,
                                            localVideoTrack, e);
                                }
                            }
                        });
                    }
                } else {
                    logger.w("Remote track failed to add unexpectedly");
                }
            }

            @Override
            public void onVideoTrackRemoved(TrackInfo trackInfo) {
                log("onVideoTrackRemoved", trackInfo.getParticipantIdentity() + " " +
                        trackInfo.getTrackId() + " " + trackInfo.isEnabled());
                if (trackInfo.getTrackOrigin() == TrackOrigin.LOCAL) {
                    final LocalVideoTrack localVideoTrack =
                            localMedia.removeLocalVideoTrack(trackInfo);
                    localVideoTrack.removeCameraCapturer();
                    localVideoTrack.setTrackState(MediaTrackState.ENDED);
                    if(localMedia.getHandler() != null) {
                        localMedia.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                if (localMedia.getLocalMediaListener() != null) {
                                    localMedia.getLocalMediaListener()
                                            .onLocalVideoTrackRemoved(localMedia, localVideoTrack);
                                }
                            }
                        });
                    }
                } else {
                    final Participant participant = findOrCreateParticipant(trackInfo
                            .getParticipantIdentity(), null);
                    final VideoTrack videoTrack = participant.getMedia().removeVideoTrack(trackInfo);
                    videoTrack.setTrackState(MediaTrackState.ENDED);
                    if(handler != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (participant.getParticipantListener() != null) {
                                    participant.getParticipantListener()
                                            .onVideoTrackRemoved(conversation, participant,
                                                    videoTrack);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onVideoTrackStateChanged(final TrackInfo trackInfo) {
                log("onVideoTrackStateChanged", trackInfo.getParticipantIdentity() + " " +
                        trackInfo.getTrackId() + " " + trackInfo.isEnabled());
                if(trackInfo.getTrackOrigin() == TrackOrigin.LOCAL) {
                    return;
                } else {
                    final Participant participant = findOrCreateParticipant(trackInfo
                            .getParticipantIdentity(), null);
                    List<VideoTrack> videoTracks = participant.getMedia().getVideoTracks();
                    for(final VideoTrack videoTrack: videoTracks) {
                        if(trackInfo.getTrackId().equals(videoTrack.getTrackId())) {
                            videoTrack.updateTrackInfo(trackInfo);
                            if(handler != null) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (participant.getParticipantListener() != null) {
                                            if(trackInfo.isEnabled()) {
                                                participant.getParticipantListener()
                                                        .onTrackEnabled(conversation, participant,
                                                                videoTrack);
                                            } else {
                                                participant.getParticipantListener()
                                                        .onTrackDisabled(conversation, participant,
                                                                videoTrack);
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
            public void onAudioTrackAdded(TrackInfo trackInfo,
                                          org.webrtc.AudioTrack webRtcAudioTrack) {
                log("onAudioTrackAdded", trackInfo.getParticipantIdentity() + " " +
                        trackInfo.getTrackId() + " " + trackInfo.isEnabled());

                if(trackInfo.getTrackOrigin() == TrackOrigin.LOCAL) {
                    // TODO: expose audio tracks in local media
                } else {
                    final Participant participant = findOrCreateParticipant(trackInfo
                            .getParticipantIdentity(), null);
                    final AudioTrack audioTrack = new AudioTrack(webRtcAudioTrack, trackInfo);
                    participant.getMedia().addAudioTrack(audioTrack);
                    if(handler != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (participant.getParticipantListener() != null) {
                                    participant.getParticipantListener().onAudioTrackAdded(
                                            conversation, participant, audioTrack);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onAudioTrackRemoved(TrackInfo trackInfo) {
                log("onAudioTrackRemoved", trackInfo.getParticipantIdentity() + " " +
                        trackInfo.getTrackId() + " " + trackInfo.isEnabled());

                if(trackInfo.getTrackOrigin() == TrackOrigin.LOCAL) {
                    // TODO: remove audio track from local media once audio tracks are exposed
                } else {
                    final Participant participant = findOrCreateParticipant(trackInfo
                            .getParticipantIdentity(), null);
                    final AudioTrack audioTrack = participant.getMedia()
                            .removeAudioTrack(trackInfo);
                    audioTrack.setTrackState(MediaTrackState.ENDED);
                    if(handler != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (participant.getParticipantListener() != null) {
                                    participant.getParticipantListener()
                                            .onAudioTrackRemoved(conversation, participant,
                                                    audioTrack);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onAudioTrackStateChanged(final TrackInfo trackInfo) {
                log("onAudioTrackStateChanged", trackInfo.getParticipantIdentity() + " " +
                        trackInfo.getTrackId() + " " + trackInfo.isEnabled());
                if(trackInfo.getTrackOrigin() == TrackOrigin.LOCAL) {
                    return;
                } else {
                    final Participant participant = findOrCreateParticipant(trackInfo
                            .getParticipantIdentity(), null);
                    List<AudioTrack> audioTracks = participant.getMedia().getAudioTracks();
                    for(final AudioTrack audioTrack: audioTracks) {
                        if(trackInfo.getTrackId().equals(audioTrack.getTrackId())) {
                            audioTrack.updateTrackInfo(trackInfo);
                            if(handler != null) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (participant.getParticipantListener() != null) {
                                            if(trackInfo.isEnabled()) {
                                                participant.getParticipantListener()
                                                        .onTrackEnabled(conversation, participant,
                                                                audioTrack);
                                            } else {
                                                participant.getParticipantListener()
                                                        .onTrackDisabled(conversation, participant,
                                                                audioTrack);
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
                    final MediaTrackStatsRecord stats = MediaTrackStatsRecord.create(report);

                    if (stats != null) {
                        /*
                         * Do not report stats until the participant sid of this participant
                         * is available on both the conversation and the media stats record. It
                         * will become available when the onParticipantConnected event is triggered.
                         */
                        for(final Participant participant: getParticipants()) {
                            if(participant.getSid() != null && stats.getParticipantSid() != null &&
                                    participant.getSid().equals(stats.getParticipantSid())) {
                                postStatsToListener(participant, stats);
                                return;
                            }
                        }
                        logger.d("stats report skipped since the participant sid has not been set yet");
                    }
                }
            }
        };

        public SessionObserverInternal(Conversation conversation) {
            this.nativeSessionObserver = nativeWrapObserver(sessionObserver, conversation);
            this.conversation = conversation;
        }

        public void enableStats(long nativeSession, boolean enable) {
            nativeEnableStats(nativeSessionObserver, nativeSession, enable);
        }

        private native long nativeWrapObserver(SessionObserver sessionObserver,
                                               Conversation conversation);
        private native void nativeFreeObserver(long nativeSessionObserver);

        private native void nativeEnableStats(long nativeSessionObserver,
                                              long nativeSession,
                                              boolean enable);

        @Override
        public long getNativeHandle() {
            return nativeSessionObserver;
        }

        public void dispose() {
            if (nativeSessionObserver != 0) {
                nativeFreeObserver(nativeSessionObserver);
                nativeSessionObserver = 0;
            }

        }

    }

    // Made as package scope for OutgoingInvite so when we cancel we dispose our observer
    SessionObserverInternal sessionObserverInternal;
    private long nativeSession;

    /*
     * Outgoing invite
     */
    private Conversation(TwilioConversationsClientInternal conversationsClientInternal,
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

        this.localMedia = localMedia;
        localMedia.setConversation(this);

        this.conversationListener = conversationListener;
        this.conversationStateObserver = conversationStateObserver;

        sessionObserverInternal = new SessionObserverInternal(this);

        nativeSession = nativeWrapOutgoingSession(conversationsClientInternal.getNativeHandle(),
                sessionObserverInternal.getNativeHandle(),
                participantIdentityArray);
    }

    /*
     * The outgoing invite does not have a sid until it is accepted. As a result,
     * the ConversationClient will call this to retain the conversation sid
     * when the conversation becomes valid.
     */
    void retainSid() {
        conversationSid = nativeGetConversationSid(nativeSession);
    }

    /*
     * Incoming invite
     */
    private Conversation(TwilioConversationsClientInternal conversationsClientInternal,
                             long nativeSession,
                             String[] participantsIdentities,
                             ConversationStateObserver conversationStateObserver,
                             Handler handler) {
        this.conversationsClientInternal = conversationsClientInternal;
        this.conversationStateObserver = conversationStateObserver;
        this.nativeSession = nativeSession;

        // The sid is available from the session when an incoming invite is provided
        conversationSid = nativeGetConversationSid(nativeSession);

        inviter = participantsIdentities[0];

        this.handler = handler;
        if(handler == null) {
            throw new IllegalThreadStateException("This thread must be able to obtain a Looper");
        }

        for (String participantIdentity : participantsIdentities) {
            findOrCreateParticipant(participantIdentity, null);
            invitedParticipants.add(participantIdentity);


        }
        sessionObserverInternal = new SessionObserverInternal(this);
        nativeSetSessionObserver(nativeSession, sessionObserverInternal.getNativeHandle());
    }

    static Conversation createOutgoingConversation(
            TwilioConversationsClientInternal conversationsClient,
            Set<String> participants,
            LocalMedia localMedia,
            Conversation.Listener listener,
            ConversationStateObserver conversationStateObserver,
            Handler handler) {
        Conversation conversation = new Conversation(conversationsClient,
                participants, localMedia, listener, conversationStateObserver, handler);
        return conversation;
    }

    static Conversation createIncomingConversation(
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
        Conversation conversation = new Conversation(conversationsClientInternal,
                nativeSession, participantIdentities, conversationStateObserver, handler);
        return conversation;
    }

    public boolean isActive() {
        return ((state == SessionState.STARTING) ||
                (state == SessionState.STOPPING) ||
                (state == SessionState.IN_PROGRESS) ||
                (state == SessionState.STOP_FAILED));
    }

    /**
     * Returns the list of participants in this conversation.
     *
     * @return participants list of {@link Participant} in this conversation.
     */
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

    /**
     * Returns the {@link LocalMedia} for this conversation
     *
     * @return local media
     */
    public LocalMedia getLocalMedia() {
        checkDisposed();

        return localMedia;
    }

    /**
     * Gets the {@link Conversation.Listener} of this conversation
     *
     * @return listener of this conversation
     */
    public Conversation.Listener getConversationListener() {
        return conversationListener;
    }

    /**
     * Sets the {@link Conversation.Listener} of this conversation
     *
     * @param listener A listener of this conversation
     */
    public void setConversationListener(Conversation.Listener listener) {
        if(handler == null) {
            throw new IllegalThreadStateException("This thread must be able to obtain a Looper");
        }
        this.conversationListener = listener;
    }

    /**
     * Invites one or more participants to this conversation.
     *
     * <p>Results of this method will propagate up according to the following scenarios:
     * <ol>
     *     <li>{@link Conversation.Listener#onParticipantConnected(Conversation, Participant)} will
     *     be invoked if recipient accepts invite and is connected.</li>
     *     <li>{@link Conversation.Listener#onFailedToConnectParticipant(Conversation, Participant,
     *     TwilioConversationsException)} will be invoked with error code
     *     {@link TwilioConversationsClient#CONVERSATION_FAILED} if the recipient rejected the
     *     invite.</li>
     *     <li>{@link Conversation.Listener#onFailedToConnectParticipant(Conversation, Participant,
     *     TwilioConversationsException)} will be invoked with error code
     *     {@link TwilioConversationsClient#CONVERSATION_IGNORED} if the recipient ignored the
     *     invite.</li>
     * </ol>
     *
     * @param participantIdentities A set of strings representing the identities of these
     *                              participants.
     */
    public void invite(Set<String> participantIdentities) throws IllegalArgumentException {
        checkDisposed();
        if ((participantIdentities == null) || (participantIdentities.size() == 0)) {
            throw new IllegalArgumentException("participantIdentities cannot be null or empty");
        }
        inviteParticipants(participantIdentities);
    }

    /**
     * Disconnects from this conversation.
     *
     * <p>Results of this method will propagate up in the following order:
     * <ol>
     *     <li>{@link Conversation.Listener#onFailedToConnectParticipant(Conversation, Participant,
     *     TwilioConversationsException)} will be invoked with error code
     *     {@link TwilioConversationsClient#CONVERSATION_TERMINATED} for each participant of the
     *     {@link Conversation}</li>
     *     <li>{@link Conversation.Listener#onConversationEnded(Conversation,
     *     TwilioConversationsException)} will be invoked upon the completion of this process.</li>
     * </ol>
     */
    public void disconnect() {
        if(nativeSession != 0) {
            stop();
        }
    }

    /**
     * Gets the conversation SID
     *
     * @return conversation SID
     */
    public String getSid() {
        return conversationSid;
    }

    /**
     * Gets the {@link StatsListener} of this conversation
     *
     * @return listener of this conversation media tracks stats
     */
    public StatsListener getStatsListener() {
        return statsListener;
    }

    /**
     * Sets the {@link StatsListener} of this conversation
     *
     * @param listener Listens to media tracks stats from this conversation
     */
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

    private Participant findOrCreateParticipant(String participantIdentity, String participantSid) {
        Participant participant = participantMap.get(participantIdentity);
        if(participant == null) {
            logger.d("Creating new participant" + participantIdentity);
            if(participantSid == null) {
                logger.w("Participant sid was null");
            }
            participant = new Participant(participantIdentity, participantSid);
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

    SessionState getSessionState() {
        return state;
    }

    private void postStatsToListener(final Participant participant,
                                     final MediaTrackStatsRecord stats) {
        if (stats instanceof LocalAudioTrackStatsRecord) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    statsListener.onLocalAudioTrackStatsRecord(Conversation.this,
                            participant, (LocalAudioTrackStatsRecord) stats);
                }
            });
        } else if (stats instanceof LocalVideoTrackStatsRecord) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    statsListener.onLocalVideoTrackStatsRecord(Conversation.this,
                            participant, (LocalVideoTrackStatsRecord) stats);
                }
            });
        } else if (stats instanceof RemoteAudioTrackStatsRecord) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    statsListener.onRemoteAudioTrackStatsRecord(Conversation.this,
                            participant, (RemoteAudioTrackStatsRecord) stats);
                }
            });
        } else if (stats instanceof RemoteVideoTrackStatsRecord) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    statsListener.onRemoteVideoTrackStatsRecord(Conversation.this,
                            participant, (RemoteVideoTrackStatsRecord) stats);
                }
            });
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

    IncomingInvite getIncomingInvite() {
        return incomingInvite;
    }

    OutgoingInvite getOutgoingInvite() {
        return outgoingInvite;
    }

    void setIncomingInvite(IncomingInvite incomingInvite) {
        this.incomingInvite = incomingInvite;
    }

    void setOutgoingInvite(OutgoingInvite outgoingInvite) {
        this.outgoingInvite = outgoingInvite;
    }

    long getNativeHandle() {
        return nativeSession;
    }

    public void setLocalMedia(LocalMedia media) {
        checkDisposed();
        localMedia = media;
        localMedia.setConversation(this);
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
        LocalVideoTrack localVideoTrack = localMedia.getLocalVideoTracks().get(0);
        // TODO: Camera capture is the only supported local video stream for now.
        // Once we nativeStart supporting screen share or etc, we should modify this method.
        cameraCapturer = localVideoTrack.getCameraCapturer();
        cameraCapturer.startConversationCapturer(nativeSession);
        nativeSetExternalCapturer(nativeSession, cameraCapturer.getNativeVideoCapturer());
    }

    boolean mute(boolean on) {
        return nativeMute(nativeSession, on);
    }

    boolean isMuted() {
        return nativeIsMuted(nativeSession);
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

    void start(final CoreSessionMediaConstraints mediaConstraints) {
        logger.d("starting call");

		/*
		 * Determine the media constraints
		 */
        final VideoConstraints videoConstraints;
        LocalMedia localMedia = getLocalMedia();
        if(mediaConstraints.isVideoEnabled()) {
            setupExternalCapturer();
            LocalVideoTrack localVideoTrack = localMedia.getLocalVideoTracks().get(0);
            videoConstraints = localVideoTrack.getVideoConstraints();
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
        nativeStart(retainedNativeSession,
                mediaConstraints.isAudioEnabled(),
                mediaConstraints.isAudioMuted(),
                mediaConstraints.isVideoEnabled(),
                mediaConstraints.isVideoPaused(),
                videoConstraints,
                mediaConstraints.getIceServersArray(),
                policy);

    }

    void stop() {
		/*
		 * Retain the session pointer since it can be reset before the
		 * new thread references it.
		 */
        final long retainNativeSession = nativeSession;
        nativeStop(retainNativeSession);
    }

    boolean enableVideo(boolean enabled, boolean paused, VideoConstraints videoConstraints) {
        return nativeEnableVideo(nativeSession, enabled, paused, videoConstraints);
    }

    void inviteParticipants(Set<String> participants) {
        String[] participantIdentityArray =
                participants.toArray(new String[participants.size()]);
        nativeInviteParticipants(nativeSession, participantIdentityArray);
    }

    boolean enableAudio(boolean enabled, boolean muted) {
        return nativeEnableAudio(nativeSession, enabled, muted);
    }

    private void disposeConversation() {
        if (sessionObserverInternal != null) {
            sessionObserverInternal.dispose();
            sessionObserverInternal = null;
        }
        if (nativeSession != 0) {
            nativeFreeHandle(nativeSession);
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

    public interface Listener {
        /**
         * This method notifies the listener when participant has connected to the conversation.
         *
         * @param conversation The conversation.
         * @param participant The participant.
         */
        void onParticipantConnected(Conversation conversation, Participant participant);

        /**
         * This method notifies the listener when a participant was unable to connect to the
         * conversation.
         *
         * @param conversation The conversation.
         * @param participant The participant.
         * @param exception Exception encountered in adding participant to conversation.
         *                  <p>The error codes returned correspond to the following scenarios:
         *                  <ol>
         *                      <li>{@link TwilioConversationsClient#CONVERSATION_REJECTED} returned when
         *                      participant rejects an invite.</li>
         *                      <li>{@link TwilioConversationsClient#CONVERSATION_IGNORED} returned when
         *                      participant ignores an invite</li>
         *                      <li>{@link TwilioConversationsClient#CONVERSATION_FAILED} returned when
         *                      participant rejects an invite to an existing conversation</li>
         *                  </ol>
         */
        void onFailedToConnectParticipant(Conversation conversation,
                                          Participant participant,
                                          TwilioConversationsException exception);

        /**
         * This method notifies the listener when a participant has disconnected from a conversation
         * by request or due to an error.
         *
         * @param conversation The conversation.
         * @param participant The participant.
         */
        void onParticipantDisconnected(Conversation conversation, Participant participant);

        /**
         * This method notifies the listener when the conversation has ended.
         *
         * @param conversation The conversation
         * @param exception Exception (if any) encountered when conversation ends.
         */
        void onConversationEnded(Conversation conversation, TwilioConversationsException exception);
    }

    private native long nativeWrapOutgoingSession(long nativeEndpoint,
                                                  long nativeSessionObserver,
                                                  String[] participants);
    private native void nativeStart(long nativeSession,
                                    boolean enableAudio,
                                    boolean muteAudio,
                                    boolean enableVideo,
                                    boolean pauseVideo,
                                    VideoConstraints videoConstraints,
                                    IceServer[] iceServers,
                                    IceTransportPolicy iceTransportPolicy);

    private native void nativeSetExternalCapturer(long nativeSession, long nativeCapturer);
    private native void nativeStop(long nativeSession);
    private native void nativeSetSessionObserver(long nativeSession, long nativeSessionObserver);
    private native void nativeFreeHandle(long nativeHandle);
    private native boolean nativeEnableVideo(long nativeHandle, boolean enabled, boolean paused,
                                             VideoConstraints videoConstraints);
    private native boolean nativeMute(long nativeSession, boolean on);
    private native boolean nativeIsMuted(long nativeSession);
    private native void nativeInviteParticipants(long nativeHandle, String[] participants);
    private native String nativeGetConversationSid(long nativeHandle);
    private native boolean nativeEnableAudio(long nativeHandle, boolean enabled, boolean muted);
}
