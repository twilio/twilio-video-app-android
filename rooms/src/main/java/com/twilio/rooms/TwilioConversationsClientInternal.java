package com.twilio.rooms;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;

import com.twilio.common.AccessManager;

import com.twilio.rooms.internal.Logger;
import com.twilio.rooms.internal.ClientOptionsInternal;

final class TwilioConversationsClientInternal implements
        NativeHandleInterface,
        EndpointObserver,
        CoreEndpoint,
        Conversation.Listener,
        ConversationStateObserver {

    private static final Logger logger = Logger.getLogger(TwilioConversationsClientInternal.class);

    private final UUID uuid = UUID.randomUUID();
    private final TwilioConversationsClient twilioConversationsClient;
    private Context context;
    private TwilioConversationsClient.Listener conversationsClientListener;
    private EndpointObserverInternal endpointObserver;
    private long nativeEndpointHandle;
    private AccessManager accessManager;
    private long nativeCore;
    private Handler handler;
    private EndpointState endpointState;
    private Set<Conversation> conversations = Collections
            .newSetFromMap(new ConcurrentHashMap<Conversation, Boolean>());
    private Map<Conversation, OutgoingInvite> pendingOutgoingInvites = new HashMap<>();
    private Map<Conversation, IncomingInvite> pendingIncomingInvites = new HashMap<>();
    private boolean listening = false;
    private IceOptions iceOptions;


    void removeConversation(Conversation conversation) {
        conversations.remove(conversation);
    }

    void onConversationTerminated(final Conversation conversation,
                                  TwilioConversationsException e) {
        conversations.remove(conversation);
        pendingIncomingInvites.remove(conversation.getIncomingInvite());
        conversation.getIncomingInvite().setStatus(InviteStatus.CANCELLED);

        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    conversationsClientListener.onIncomingInviteCancelled(twilioConversationsClient,
                            conversation.getIncomingInvite());
                }
            });
        }

    }

    class EndpointObserverInternal implements NativeHandleInterface {

        private long nativeEndpointObserver;

        public EndpointObserverInternal(EndpointObserver observer) {
            this.nativeEndpointObserver = nativeWrapObserver(observer,
                    TwilioConversationsClientInternal.this);
        }

        private native long nativeWrapObserver(EndpointObserver observer,
                                               TwilioConversationsClientInternal twilioConversationsClientInternal);
        private native void nativeFreeObserver(long nativeEndpointObserver);

        @Override
        public long getNativeHandle() {
            return nativeEndpointObserver;
        }

        public void dispose() {
            if (nativeEndpointObserver != 0) {
                nativeFreeObserver(nativeEndpointObserver);
                nativeEndpointObserver = 0;
            }
        }

    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public TwilioConversationsClientInternal(
                TwilioConversationsClient twilioConversationsClient,
                Context context,
                AccessManager accessManager,
                long nativeCore,
                TwilioConversationsClient.Listener listener,
                ClientOptions options,
                Handler handler) {
        this.twilioConversationsClient = twilioConversationsClient;
        this.context = context;
        this.conversationsClientListener = listener;
        this.accessManager = accessManager;
        this.nativeCore = nativeCore;

        if(handler == null) {
            throw new IllegalThreadStateException("This thread must be able to obtain a Looper");
        }
        this.handler = handler;
        if (options != null) {
            iceOptions = options.getIceOptions();
        }

        // Let's simplify options map by using array instead,
        // for easier passing data to jni layer
        String[] optionsArray = getOptionsArray(options);

        endpointObserver = new EndpointObserverInternal(this);
        nativeEndpointHandle = nativeCreateEndpoint(accessManager, optionsArray,
                nativeCore, endpointObserver.getNativeHandle());

        if(nativeEndpointHandle == 0) {
            throw new IllegalStateException("Native endpoint handle must not be null");
        }
    }

    private String[] getOptionsArray(ClientOptions options) {
        String[] optionsArray = new String[0];
        if (options != null && (options instanceof ClientOptionsInternal)) {
            Map<String, String> privateOptions =
                    ((ClientOptionsInternal)options).getPrivateOptions();
            if (privateOptions != null && privateOptions.size() >0) {
                optionsArray = new String[privateOptions.size() * 2];
                int i = 0;
                for (Map.Entry<String, String> entrySet : privateOptions.entrySet()) {
                    optionsArray[i++] = entrySet.getKey();
                    optionsArray[i++] = entrySet.getValue();
                }

            }
        }
        return optionsArray;
    }

    int getActiveConversationsCount() {
        int activeConversations = 0;
        for (Conversation conv : conversations) {
            if (conv.isActive()) {
                activeConversations++;
            }
        }
        return activeConversations;
    }

    public synchronized void listen() {
        if(nativeEndpointHandle != 0) {
            listening = true;
            nativeListen(nativeEndpointHandle);
        }
    }

    public synchronized void unlisten() {
        if(nativeEndpointHandle != 0) {
            nativeUnlisten(nativeEndpointHandle);
        }
    }

    public void setConversationsClientListener(TwilioConversationsClient.Listener listener) {
        if(handler == null) {
            throw new IllegalThreadStateException("This thread must be able to obtain a Looper");
        }
        this.conversationsClientListener = listener;
    }

    public String getIdentity() {
        return accessManager.getIdentity();
    }

    public boolean isListening() {
        return endpointState == EndpointState.REGISTERED;
    }

    public OutgoingInvite sendConversationInvite(Set<String> participants,
                                                 LocalMedia localMedia,
                                                 ConversationCallback conversationCallback) {
        return sendConversationInvite(participants, localMedia, null, conversationCallback);
    }

    public OutgoingInvite sendConversationInvite(Set<String> participants,
                                                 LocalMedia localMedia,
                                                 IceOptions iceOptions,
                                                 ConversationCallback conversationCallback) {
        if(participants == null || participants.size() == 0) {
            throw new IllegalStateException("Invite at least one participant");
        }
        if(localMedia == null) {
            throw new IllegalStateException("Local media is required to create a conversation");
        }
        if(conversationCallback == null) {
            throw new IllegalStateException("A ConversationCallback is required to retrieve the conversation");
        }
        for (String participant : participants) {
            if (participant == null || participant.isEmpty() ) {
                throw new IllegalArgumentException("Participant cannot be an empty string");
            }
        }
        if(endpointState != EndpointState.REGISTERED) {
            TwilioConversationsException exception =
                    new TwilioConversationsException(TwilioConversationsClient.CLIENT_DISCONNECTED,
                            "The TwilioConversationsClient must be listening to invite.");
            conversationCallback.onConversation(null, exception);
        }

        Conversation outgoingConversationImpl = Conversation.createOutgoingConversation(
                this, participants, localMedia, this, this, handler);

        if (outgoingConversationImpl == null ||
                outgoingConversationImpl.getNativeHandle() == 0) {
            TwilioConversationsException exception =
                    new TwilioConversationsException(TwilioConversationsClient.CLIENT_DISCONNECTED,
                            "Cannot create conversation while reconnecting. " +
                                    "Wait for conversations client to reconnect and try again.");
            conversationCallback.onConversation(null, exception);
            return null;
        } else {
            iceOptions = (iceOptions != null) ? iceOptions : this.iceOptions;
            outgoingConversationImpl.start(
                    outgoingConversationImpl.createMediaConstrains(iceOptions));
        }

        conversations.add(outgoingConversationImpl);
        logger.i("Conversations size is now " + conversations.size());

        OutgoingInvite outgoingInviteImpl = OutgoingInvite.create(this, outgoingConversationImpl, conversationCallback);
        pendingOutgoingInvites.put(outgoingConversationImpl, outgoingInviteImpl);
        outgoingConversationImpl.setOutgoingInvite(outgoingInviteImpl);

        return outgoingInviteImpl;
    }

    @Override
    public void onConversationStatusChanged(Conversation conversation,
                                            ConversationStatus conversationStatus) {
        if(conversationStatus.equals(ConversationStatus.CONNECTED) &&
                conversation.getSessionState().equals(SessionState.IN_PROGRESS)) {
            handleConversationStarted(conversation);
        }
    }

    private void handleConversationStarted(final Conversation conversationImpl) {
        final IncomingInvite incomingInviteImpl = pendingIncomingInvites.get(conversationImpl);
        if(incomingInviteImpl != null) {
            incomingInviteImpl.setStatus(InviteStatus.ACCEPTED);
            // Remove the invite since it has reached its final state
            pendingIncomingInvites.remove(conversationImpl);
            // Stop listening to ConversationListener. The developer should provide their own listener
            conversationImpl.setConversationListener(null);
            // Notify the developer that the conversation is active
            if (incomingInviteImpl.getConversationCallback() != null) {
                /**
                 * Block the thread to ensure no other callbacks are called until the developer
                 * handles this callback.
                 */
                final CountDownLatch waitLatch = new CountDownLatch(1);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        incomingInviteImpl.getConversationCallback().onConversation(conversationImpl, null);
                        waitLatch.countDown();
                    }
                });
                try {
                    waitLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        final OutgoingInvite outgoingInviteImpl = pendingOutgoingInvites.get(conversationImpl);
        if(outgoingInviteImpl != null) {
            outgoingInviteImpl.setStatus(InviteStatus.ACCEPTED);
            // Remove the invite since it has reached its final state
            pendingOutgoingInvites.remove(conversationImpl);
            // Stop listening to ConversationListener. The developer should provide their own listener
            conversationImpl.setConversationListener(null);
            conversationImpl.retainSid();
            if (outgoingInviteImpl.getConversationCallback() != null) {
                /**
                 * Block the thread to ensure no other callbacks are called until the developer handles
                 * this callback.
                 */
                final CountDownLatch waitLatch = new CountDownLatch(1);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        outgoingInviteImpl.getConversationCallback().onConversation(conversationImpl, null);
                        waitLatch.countDown();
                    }
                });
                try {
                    waitLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void handleConversationFailed(final Conversation conversationImpl,
                                          final TwilioConversationsException e) {
        final OutgoingInvite outgoingInviteImpl = pendingOutgoingInvites.get(conversationImpl);
        if(outgoingInviteImpl != null) {
            InviteStatus status = outgoingInviteImpl.getStatus() == InviteStatus.CANCELLED ?
                    InviteStatus.CANCELLED :
                    InviteStatus.FAILED;
            outgoingInviteImpl.setStatus(status);
            pendingOutgoingInvites.remove(conversationImpl);
            if (outgoingInviteImpl.getConversationCallback() != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // The call ended by the user
                        if(e != null) {
                            outgoingInviteImpl.getConversationCallback()
                                    .onConversation(conversationImpl, e);
                        } else {
                            outgoingInviteImpl.getConversationCallback()
                                    .onConversation(conversationImpl, e);
                            if (conversationImpl != null) {
                                conversationImpl.getConversationListener()
                                        .onConversationEnded(conversationImpl, null);
                            }
                        }
                    }
                });
            }
        }

        conversations.remove(conversationImpl);
    }

    @Override
    public void onParticipantConnected(Conversation conversation, Participant participant) {
        logger.w("Not expecting a connected participant " + participant + " while inviting.");
    }

    @Override
    public void onFailedToConnectParticipant(Conversation conversation, Participant participant, TwilioConversationsException e) {
        handleConversationStarting(conversation);
    }

    private void handleConversationStarting(Conversation conversation) {
        // Do nothing.
    }

    @Override
    public void onParticipantDisconnected(Conversation conversation, Participant participant) {
        // Do nothing.
    }

    @Override
    public void onConversationEnded(Conversation conversation, TwilioConversationsException e) {
        handleConversationFailed(conversation, e);
    }

    /**
     * NativeHandleInterface
     */
    @Override
    public long getNativeHandle() {
        return nativeEndpointHandle;
    }

    /**
     * EndpointObserver methods
     */
    @Override
    public void onRegistrationDidComplete(CoreError error) {
        logger.d("onRegistrationDidComplete");
        if (error != null) {
            listening = false;
            final TwilioConversationsException e =
                    new TwilioConversationsException(error.getCode(), error.getMessage());
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        conversationsClientListener
                                .onFailedToStartListening(twilioConversationsClient, e);
                    }
                });
            }
        } else {
            listening = true;
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        conversationsClientListener
                                .onStartListeningForInvites(twilioConversationsClient);
                    }
                });
            }
        }
    }

    @Override
    public void onUnregistrationDidComplete(CoreError error) {
        logger.d("onUnregistrationDidComplete");
        listening = false;
        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    conversationsClientListener
                            .onStopListeningForInvites(twilioConversationsClient);
                }
            });
        }
    }

    @Override
    public synchronized void onStateDidChange(EndpointState state) {
        logger.d("onStateDidChange " + state.toString());
        EndpointState oldEndpointState = endpointState;
        endpointState = state;
        if ((oldEndpointState == EndpointState.RECONNECTING) &&
                (endpointState == EndpointState.REGISTERED)) {
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        conversationsClientListener
                                .onStartListeningForInvites(twilioConversationsClient);
                    }
                });
            }

        } else if (endpointState == EndpointState.RECONNECTING) {
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        conversationsClientListener
                                .onStopListeningForInvites(twilioConversationsClient);
                    }
                });
            }
        }
    }

    @Override
    public void onIncomingCallDidReceive(long nativeSession, String[] participants) {
        logger.d("onIncomingCallDidReceive");

        Conversation incomingConversationImpl =
                Conversation.createIncomingConversation(this,
                        nativeSession,
                        participants,
                        this,
                        handler);
        if (incomingConversationImpl == null) {
            logger.e("Failed to create conversation");
            return;
        }

        conversations.add(incomingConversationImpl);

        final IncomingInvite incomingInviteImpl = IncomingInvite.create(this,
                incomingConversationImpl,
                handler);
        if (incomingInviteImpl == null) {
            logger.e("Failed to create IncomingInvite");
            return;
        }
        incomingConversationImpl.setIncomingInvite(incomingInviteImpl);

        pendingIncomingInvites.put(incomingConversationImpl, incomingInviteImpl);

        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    conversationsClientListener.onIncomingInvite(twilioConversationsClient,
                            incomingInviteImpl);
                }
            });
        }
    }

	/*
	 * CoreEndpoint methods
	 */


    /*
     * Accept the incoming invite.
     */
    @Override
    public void accept(Conversation conversationImpl, IceOptions options) {
        IceOptions iceOptions = (options != null) ? options : this.iceOptions;
        conversationImpl.start(conversationImpl.createMediaConstrains(iceOptions));
    }

    /*
     * Rejects the incoming invite. This removes the pending conversation and the invite.
     */
    @Override
    public void reject(Conversation conversationImpl) {
        nativeReject(getNativeHandle(), conversationImpl.getNativeHandle());
        pendingIncomingInvites.remove(conversationImpl);
        conversations.remove(conversationImpl);
    }

    @Override
    public void ignore(Conversation conversationImpl) {
        // We are intentionally not implementing ignore
    }

    public void setAudioOutput(AudioOutput audioOutput) {
        logger.d("setAudioOutput");
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioOutput == AudioOutput.SPEAKERPHONE) {
            audioManager.setSpeakerphoneOn(true);
        } else {
            audioManager.setSpeakerphoneOn(false);
        }

    }

    public AudioOutput getAudioOutput() {
        logger.d("getAudioOutput");
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.isSpeakerphoneOn() ? AudioOutput.SPEAKERPHONE : AudioOutput.HEADSET;
    }

    boolean hasTerminated() {
        return !listening;
    }

    public void disposeClient() {
        if (nativeEndpointHandle != 0) {
            nativeFreeHandle(nativeCore, nativeEndpointHandle);
            nativeEndpointHandle = 0;
        }
        if (endpointObserver != null) {
            endpointObserver.dispose();
            endpointObserver = null;
        }
    }

    private native long nativeCreateEndpoint(AccessManager accessManager,
                                             String[] optionsArray,
                                             long nativeCore,
                                             long nativeEndpointObserver);
    private native void nativeListen(long nativeEndpoint);
    private native void nativeUnlisten(long nativeEndpoint);
    private native void nativeReject(long nativeEndpoint, long nativeSession);
    private native void nativeFreeHandle(long nativeCore, long nativeEndpoint);
}
