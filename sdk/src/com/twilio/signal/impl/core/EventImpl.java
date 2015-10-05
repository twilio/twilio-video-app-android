package com.twilio.signal.impl.core;

public class EventImpl implements Event {
	
	private EventType eventType;
	private String payload;
	
	public EventImpl(EventType eventType, String payload) {
		this.eventType = eventType;
		this.payload = payload;
	}

	@Override
	public EventType getType() {
		return eventType;
	}

	@Override
	public String getPayload() {
		return payload;
	}

}
