package com.twilio.signal.impl;

import com.twilio.signal.Participant;
import com.twilio.signal.Conversation;
import com.twilio.signal.Media;
import com.twilio.signal.LocalMediaImpl;

public class ParticipantImpl implements Participant {
	private String address;
	private Conversation conversation;
	private Media media;


	public ParticipantImpl(Conversation conversation, String address) {
		this.conversation = conversation;
		this.address = address;
		this.media = new LocalMediaImpl();
	}

	public String getAddress() {
		return address;
	}

	public Conversation getConversation() {
		return conversation;
	}

	public Media getMedia() {
		return media;
	}

}
