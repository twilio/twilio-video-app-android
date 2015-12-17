package com.twilio.signal.impl;

import android.os.Handler;

import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationCallback;
import com.twilio.signal.IncomingInvite;
import com.twilio.signal.InviteStatus;
import com.twilio.signal.LocalMedia;
import com.twilio.signal.impl.core.CoreSessionMediaConstraints;
import com.twilio.signal.impl.logging.Logger;
import com.twilio.signal.impl.util.CallbackHandler;

import java.util.Set;

public class IncomingInviteImpl implements IncomingInvite {

	static final Logger logger = Logger.getLogger(IncomingInviteImpl.class);
	private final Handler handler;

	private ConversationsClientImpl conversationsClientImpl;
	private ConversationImpl conversationImpl;
	private ConversationCallback conversationCallback;
	private InviteStatus inviteStatus;

	private IncomingInviteImpl(ConversationsClientImpl conversationsClientImpl,
							   ConversationImpl conversationImpl) {
		this.conversationImpl = conversationImpl;
		this.conversationsClientImpl = conversationsClientImpl;
		this.inviteStatus = InviteStatus.PENDING;
		this.handler = CallbackHandler.create();
		if(handler == null) {
			throw new IllegalThreadStateException("This thread must be able to obtain a Looper");
		}
	}

	static IncomingInviteImpl create(ConversationsClientImpl conversationsClientImpl,
								 ConversationImpl conversationImpl) {
		if(conversationsClientImpl == null) {
			return null;
		}
		if(conversationImpl == null) {
			return null;
		}
		return new IncomingInviteImpl(conversationsClientImpl, conversationImpl);
	}

	Handler getHandler() {
		return handler;
	}

	void setStatus(InviteStatus inviteStatus) {
		this.inviteStatus = inviteStatus;
	}

	Conversation getConversation() {
		return conversationImpl;
	}

	void setConversationCallback(ConversationCallback conversationCallback) {
		this.conversationCallback = conversationCallback;
	}

	ConversationCallback getConversationCallback() {
		return conversationCallback;
	}

	@Override
	public void accept(LocalMedia localMedia, ConversationCallback conversationCallback) {
		inviteStatus = InviteStatus.ACCEPTING;
		this.conversationCallback = conversationCallback;
		conversationImpl.setLocalMedia(localMedia);
		boolean enableVideo = !localMedia.getLocalVideoTracks().isEmpty();
		boolean pauseVideo = false;
		if (enableVideo) {
			pauseVideo = !localMedia.getLocalVideoTracks().get(0).isCameraEnabled();
		}
		CoreSessionMediaConstraints mediaContext =
				new CoreSessionMediaConstraints(localMedia.isMicrophoneAdded(),
							localMedia.isMuted(), enableVideo, pauseVideo);
		conversationImpl.start(mediaContext);
	}

	@Override
	public void reject() {
		inviteStatus = InviteStatus.REJECTED;
		conversationsClientImpl.reject(conversationImpl);
		conversationsClientImpl.removeConversation(conversationImpl);
		conversationsClientImpl.clearIncoming();
	}

	@Override
	public String getConversationSid() {
		return conversationImpl.getConversationSid();
	}

	@Override
	public String getInvitee() {
		return conversationImpl.getInvitee();
	}

	@Override
	public Set<String> getParticipants() {
		return conversationImpl.getInvitedParticipants();
	}

	@Override
	public InviteStatus getInviteStatus() {
		return inviteStatus;
	}

	InviteStatus getStatus() {
		return inviteStatus;
	}
}
