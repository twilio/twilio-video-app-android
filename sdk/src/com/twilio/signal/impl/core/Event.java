package com.twilio.signal.impl.core;

public interface Event {
	
	public enum EventType {
		ICE_CANDIDATE_FOUND(1);
		
		private int type;
		
		private EventType(int type) {
			this.type = type;
		}
		
		public int getType() { return type; }
	}

	public EventType getType();
	
	public String getPayload();

}
