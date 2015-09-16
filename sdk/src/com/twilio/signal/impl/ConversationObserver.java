package com.twilio.signal.impl;

import com.twilio.signal.Conversation;

public interface ConversationObserver {

	void onConnectParticipant(String participant);

	void onFailToConnectParticipant(String participant, int error, String errorMessage);

	void onDisconnectParticipant(String participant);

	void onVideoAddedForParticipant(String participant);

	void onVideoRemovedForParticipant(String participant);

	void onLocalStatusChanged(Conversation.Status status);

	void onConversationEnded();

	void onConversationEnded(int error, String errorMessage);

}

