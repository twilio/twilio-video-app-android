package com.twilio.conversations.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import android.os.Handler;

import com.twilio.conversations.AudioTrack;
import com.twilio.conversations.Conversation;
import com.twilio.conversations.StatsListener;
import com.twilio.conversations.MediaTrackStatsRecord;
import com.twilio.conversations.TwilioConversationsException;
import com.twilio.conversations.ConversationListener;
import com.twilio.conversations.LocalMedia;
import com.twilio.conversations.LocalVideoTrack;
import com.twilio.conversations.Media;
import com.twilio.conversations.MediaTrackState;
import com.twilio.conversations.Participant;
import com.twilio.conversations.TrackOrigin;
import com.twilio.conversations.TwilioConversations;
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
import com.twilio.conversations.impl.util.CallbackHandler;

public class ConversationImpl implements Conversation,
        NativeHandleInterface, SessionObserver, CoreSession {
    private static final String DISPOSED_MESSAGE = "The conversation has been destroyed. " +
            "This operation is no longer valid";
    private Set<String> invitedParticipants = new HashSet<>();
    private String inviter;
    private ConversationsClientImpl conversationsClient;
    private ConversationListener conversationListener;
    private ConversationStateObserver conversationStateObserver;
    private Map<String,ParticipantImpl> participantMap = new HashMap<>();
    private LocalMediaImpl localMediaImpl;
    private Handler handler;
    private IncomingInviteImpl incomingInviteImpl;
    private OutgoingInviteImpl outgoingInviteImpl;
    private StatsListener statsListener;
    private Handler statsHandler;

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

    private ConversationImpl(ConversationsClientImpl conversationsClient,
                             Set<String> participants,
                             LocalMedia localMedia,
                             ConversationListener conversationListener,
                             ConversationStateObserver conversationStateObserver) {
        this.conversationsClient = conversationsClient;
        this.invitedParticipants = participants;

        String[] participantIdentityArray = new String[participants.size()];
        int i = 0;
        for(String participant : participants) {
            participantIdentityArray[i++] = participant;
        }

        handler = CallbackHandler.create();
        if(handler == null) {
            throw new IllegalThreadStateException("This thread must be able to obtain a Looper");
        }

        localMediaImpl = (LocalMediaImpl)localMedia;
        localMediaImpl.setConversation(this);

        this.conversationListener = conversationListener;
        this.conversationStateObserver = conversationStateObserver;

        sessionObserverInternal = new SessionObserverInternal(this, this);

        nativeSession = wrapOutgoingSession(conversationsClient.getNativeHandle(),
                sessionObserverInternal.getNativeHandle(),
                participantIdentityArray);
        if(nativeSession != 0) {
            conversationSid = getConversationSid(nativeSession);
        }
    }

    private ConversationImpl(ConversationsClientImpl conversationsClient,
                             long nativeSession,
                             String[] participantsIdentities,
                             ConversationStateObserver conversationStateObserver) {
        this.conversationsClient = conversationsClient;
        this.conversationStateObserver = conversationStateObserver;
        this.nativeSession = nativeSession;

        conversationSid = getConversationSid(nativeSession);

        inviter = participantsIdentities[0];

        handler = CallbackHandler.create();
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

    public static ConversationImpl createOutgoingConversation(ConversationsClientImpl conversationsClient,
                                                              Set<String> participants,
                                                              LocalMedia localMedia,
                                                              ConversationListener listener,
                                                              ConversationStateObserver conversationStateObserver) {
        ConversationImpl conversationImpl = new ConversationImpl(conversationsClient, participants, localMedia, listener, conversationStateObserver);
        return conversationImpl;
    }

    public static ConversationImpl createIncomingConversation(ConversationsClientImpl conversationsClientImpl,
                                                              long nativeSession,
                                                              String[] participantIdentities,
                                                              ConversationStateObserver conversationStateObserver) {
        if (nativeSession == 0) {
            return null;
        }
        if (participantIdentities == null || participantIdentities.length == 0) {
            return null;
        }
        ConversationImpl conversationImpl = new ConversationImpl(conversationsClientImpl, nativeSession, participantIdentities, conversationStateObserver);
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
    public ConversationListener getConversationListener() {
        return conversationListener;
    }

    @Override
    public void setConversationListener(ConversationListener listener) {
        handler = CallbackHandler.create();
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
            statsHandler = CallbackHandler.create();
            if (sessionObserverInternal != null) {
                sessionObserverInternal.enableStats(nativeSession, true);
            }
        } else {
            statsHandler = null;
            if (sessionObserverInternal != null) {
                sessionObserverInternal.enableStats(nativeSession, false);
            }
        }
    }

    private ParticipantImpl findOrCreateParticipant(String participantIdentity, String participantSid) {
        ParticipantImpl participant = participantMap.get(participantIdentity);
        if(participant == null) {
            logger.d("Creating new participant" + participantIdentity);
            participant = new ParticipantImpl(participantIdentity, participantSid);
            participantMap.put(participantIdentity, participant);
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
            conversationsClient.removeConversation(this);
            participantMap.clear();

            final TwilioConversationsException e =
                    new TwilioConversationsException(error.getCode(), error.getMessage());
            if(conversationListener == null) {
                if(e.getErrorCode() == TwilioConversations.CONVERSATION_TERMINATED) {
                    conversationsClient.onConversationTerminated(this, e);
                }
            } else if(handler != null) {
                final CountDownLatch waitLatch = new CountDownLatch(1);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        conversationListener.onConversationEnded(ConversationImpl.this, e);
                        waitLatch.countDown();
                    }
                });
                try {
                    waitLatch.await();
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
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
        conversationsClient.removeConversation(this);
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
    public void onParticipantConnected(String participantIdentity, String participantSid, CoreError error) {
        log("onParticipantConnected",  participantIdentity, error);
        // Block this thread until the handler has completed its work.
        final CountDownLatch waitLatch = new CountDownLatch(1);
        final ParticipantImpl participantImpl = findOrCreateParticipant(participantIdentity, participantSid);
        if (error == null) {
            if(handler != null && conversationListener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        conversationListener.onParticipantConnected(ConversationImpl.this, participantImpl);
                        waitLatch.countDown();
                        /**
                         * Workaround for CSDK-225. MediaTracks are added before onParticipantConnected is called.
                         */
                        Media media = participantImpl.getMediaImpl();
                        for(final VideoTrack videoTrack : media.getVideoTracks()) {
                            final Handler participantHandler = participantImpl.getHandler();
                            if(participantHandler != null) {
                                participantHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (participantImpl.getParticipantListener() != null) {
                                            participantImpl.getParticipantListener().onVideoTrackAdded(
                                                    ConversationImpl.this, participantImpl, videoTrack);
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            } else {
                waitLatch.countDown();
            }
        } else {
            final TwilioConversationsException e = new TwilioConversationsException(error.getCode(),
                    error.getMessage());
            if (conversationListener == null) {
                if(e.getErrorCode() == TwilioConversations.CONVERSATION_TERMINATED) {
                    conversationsClient.onConversationTerminated(this, e);
                } else {
                    logger.e("onParticipantConnected -> received unexpected error code -> " +
                            e.getErrorCode());
                }
                waitLatch.countDown();
            } else if(handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        conversationListener.onFailedToConnectParticipant(ConversationImpl.this,
                                participantImpl, e);
                        waitLatch.countDown();
                    }
                });
            } else {
                waitLatch.countDown();
            }
        }
        try {
            waitLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onParticipantDisconnected(final String participantIdentity, String participantSid, final DisconnectReason reason) {
        log("onParticipantDisconnected", participantIdentity, reason);
        // Block this thread until the handler has completed its work.
        final CountDownLatch waitLatch = new CountDownLatch(1);
        final ParticipantImpl participant = participantMap.remove(participantIdentity);
        if(participant == null) {
            logger.i("participant removed but was never in list");
            waitLatch.countDown();
        } else {
            if(handler != null && conversationListener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        conversationListener.onParticipantDisconnected(ConversationImpl.this, participant);
                        waitLatch.countDown();
                    }
                });
            } else {
                waitLatch.countDown();
            }
        }
        try {
            waitLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        log("onVideoTrackAdded", trackInfo.getParticipantIdentity() + " " + trackInfo.getTrackId() + trackInfo.isEnabled());

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
            final Handler participantHandler = participantImpl.getHandler();
            if(participantHandler != null) {
                participantHandler.post(new Runnable() {
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
            final Handler participantHandler = participantImpl.getHandler();
            if(participantHandler != null) {
                participantHandler.post(new Runnable() {
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
                    final Handler participantHandler = participantImpl.getHandler();
                    if(participantHandler != null) {
                        participantHandler.post(new Runnable() {
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
            final Handler participantHandler = participantImpl.getHandler();
            if(participantHandler != null) {
                participantHandler.post(new Runnable() {
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
            final Handler participantHandler = participantImpl.getHandler();
            if(participantHandler != null) {
                participantHandler.post(new Runnable() {
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
                    final Handler participantHandler = participantImpl.getHandler();
                    if(participantHandler != null) {
                        participantHandler.post(new Runnable() {
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
        if (statsHandler != null && statsListener != null) {
            final MediaTrackStatsRecord stats = MediaTrackStatsRecordFactory.create(report);
            if (stats != null) {
                statsHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        statsListener.onMediaTrackStatsRecord(ConversationImpl.this, stats);
                    }
                });
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
        CameraCapturerImpl cameraCapturer = (CameraCapturerImpl)localVideoTrack.getCameraCapturer();
        cameraCapturer.startConversationCapturer(nativeSession);
        setExternalCapturer(nativeSession, cameraCapturer.getNativeVideoCapturer());
    }

    boolean mute(boolean on) {
        return mute(nativeSession, on);
    }

    boolean isMuted() {
        return isMuted(nativeSession);
    }

    /**
     * CoreSession
     */
    @Override
    public void start() {
        logger.d("starting call");

		/*
		 * Determine the media constraints
		 */
        LocalMedia localMedia = getLocalMedia();
        boolean enableVideo = !localMedia.getLocalVideoTracks().isEmpty();
        boolean pauseVideo = false;
        if (enableVideo) {
            setupExternalCapturer();
            pauseVideo = !localMedia.getLocalVideoTracks().get(0).isEnabled();
        }
        final CoreSessionMediaConstraints mediaConstraints =
                new CoreSessionMediaConstraints(localMedia.isMicrophoneAdded(),
                        localMedia.isMuted(), enableVideo, pauseVideo);

		/*
		 * Retain the session pointer since it can be reset before the
		 * new thread references it.
		 */
        final long retainedNativeSession = nativeSession;
        // Call start on a new thread to avoid blocking the calling thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                start(retainedNativeSession,
                        mediaConstraints.isAudioEnabled(),
                        mediaConstraints.isAudioMuted(),
                        mediaConstraints.isVideoEnabled(),
                        mediaConstraints.isVideoPaused());

            }
        }).start();

    }

    @Override
    public void stop() {
		/*
		 * Retain the session pointer since it can be reset before the
		 * new thread references it.
		 */
        final long retainNativeSession = nativeSession;
        // Call stop on a new thread to avoid blocking the calling thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                stop(retainNativeSession);
            }
        }).start();
    }

    @Override
    public boolean enableVideo(boolean enabled, boolean paused) {
        return enableVideo(nativeSession, enabled, paused);
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
        EglBaseProvider.releaseEglBase();
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
                              boolean pauseVideo);
    private native void setExternalCapturer(long nativeSession, long nativeCapturer);
    private native void stop(long nativeSession);
    private native void setSessionObserver(long nativeSession, long nativeSessionObserver);
    private native void freeNativeHandle(long nativeHandle);
    private native boolean enableVideo(long nativeHandle, boolean enabled, boolean paused);
    private native boolean mute(long nativeSession, boolean on);
    private native boolean isMuted(long nativeSession);
    private native void inviteParticipants(long nativeHandle, String[] participants);
    private native String getConversationSid(long nativeHandle);
    private native boolean enableAudio(long nativeHandle, boolean enabled, boolean muted);
}
