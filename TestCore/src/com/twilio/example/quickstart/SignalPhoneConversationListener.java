package com.twilio.example.quickstart;

import com.twilio.signal.Conversation;
import com.twilio.signal.Conversation.Status;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.Participant;


public class SignalPhoneConversationListener implements ConversationListener{

	@Override
	public void onConnectParticipant(Conversation conversation,
			Participant participant) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFailToConnectParticipant(Conversation conversation,
			Participant participant, int error, String errorMessage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnectParticipant(Conversation conversation,
			Participant participant) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onVideoAddedForParticipant(Conversation conversation,
			Participant participant) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onVideoRemovedForParticipant(Conversation conversation,
			Participant participant) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLocalStatusChanged(Conversation conversation, Status status) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConversationEndedt(Conversation conversation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConversationEndedt(Conversation conversation, int error,
			String errorMessage) {
		// TODO Auto-generated method stub
		
	}



}
