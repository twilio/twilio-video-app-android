package com.twilio.signal.impl;

import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.ConversationsClient;
import com.twilio.signal.Invite;
import com.twilio.signal.LocalMedia;
import com.twilio.signal.impl.core.CoreSessionMediaConstrains;

public class InviteImpl implements Invite {
	
	private ConversationImpl conversation;
	private ConversationsClientImpl conversationsClient;
	private String fromAddr;
	
	private InviteImpl(ConversationImpl conversation,
					ConversationsClientImpl conversationsClient,
					String[] participants) {
		this.conversation = conversation;
		this.conversationsClient = conversationsClient;
		if (participants.length > 0) {
			fromAddr = participants[0];
		}
	}
	
	public static Invite create(ConversationImpl conversation,
					ConversationsClientImpl conversationsClient,
					String[] participants) {
		if (conversation == null) {
			return null;
		}
		if (conversationsClient == null) {
			return null;
		}
		if (participants.length == 0) {
			return null;
		}
		return new InviteImpl(conversation, conversationsClient, participants);
	}

	@Override
	public String from() {
		return fromAddr;
	}

	@Override
	public ConversationsClient to() {
		return conversationsClient;
	}

	@Override
	public void reject() {
		if (conversation != null) {
			conversationsClient.reject(conversation);
			conversation = null;
		}
	}

	@Override
	public Conversation accept(LocalMedia localMedia,
			ConversationListener listener) throws IllegalArgumentException {
		if (localMedia == null) {
			throw new IllegalArgumentException("LocalMedia can't be null");
		}
		conversation.setLocalMedia(localMedia);
		conversation.setConversationListener(listener);
		boolean enableVideo = !localMedia.getLocalVideoTracks().isEmpty();
		boolean pauseVideo = false;
		if (enableVideo) {
			pauseVideo = localMedia.getLocalVideoTracks().get(0).isCameraEnabled();
		}
		CoreSessionMediaConstrains mediaContext =
				new CoreSessionMediaConstrains(localMedia.isMicrophoneAdded(),
							localMedia.isMuted(), enableVideo, pauseVideo);
		conversation.start(mediaContext);
		return conversation;
	}

}
