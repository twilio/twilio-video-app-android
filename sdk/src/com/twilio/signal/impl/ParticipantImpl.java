package com.twilio.signal.impl;

import com.twilio.signal.Participant;
import com.twilio.signal.Media;

public class ParticipantImpl implements Participant {
	private String address;
	private MediaImpl media;

	public ParticipantImpl(String address) {
		this.address = address;
		this.media = new MediaImpl();
	}

	@Override
	public String getAddress() {
		return address;
	}

	@Override
	public Media getMedia() {
		return media;
	}

	public MediaImpl getMediaImpl() {
		return media;
	}

}
