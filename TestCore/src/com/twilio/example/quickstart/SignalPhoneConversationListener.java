package com.twilio.example.quickstart;

import com.twilio.signal.ConversationListener;
import com.twilio.signal.RemoteEndpoint;
import com.twilio.signal.Stream;
import com.twilio.signal.Track.TrackId;

public class SignalPhoneConversationListener implements ConversationListener{

	@Override
	public void onRemoteEndpointJoined(RemoteEndpoint endpoint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRemoteEndpointLeftConversation(RemoteEndpoint endpoint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRemoteEndpointRejectedInvite(RemoteEndpoint endpoint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAddTrackWithId(RemoteEndpoint endpoint, TrackId trackId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRemoveTrackWithId(RemoteEndpoint endpoint, TrackId trackId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPauseVideo(RemoteEndpoint endpoint, Stream stream) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMuteAudio(RemoteEndpoint endpoint, Stream stream) {
		// TODO Auto-generated method stub
		
	}

}
