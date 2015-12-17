package com.twilio.example.quickstart;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.twilio.signal.AudioTrack;
import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationException;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.LocalMediaListener;
import com.twilio.signal.LocalVideoTrack;
import com.twilio.signal.MediaTrack;
import com.twilio.signal.Participant;
import com.twilio.signal.ParticipantListener;
import com.twilio.signal.VideoRendererObserver;
import com.twilio.signal.VideoTrack;
import com.twilio.signal.VideoViewRenderer;

public class ConversationActivity extends Activity implements
		ConversationListener,
		LocalMediaListener,
		ParticipantListener {

	private static final String TAG = "ConversationActivity";
	
	private SignalPhone phone;
	private Conversation conv;
	private ViewGroup localContainer;
	private ViewGroup participantContainer;
	private String participantAddress;
	private VideoViewRenderer participantVideoRenderer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.conversation);

		localContainer = (ViewGroup)findViewById(R.id.localContainer);
		participantContainer = (ViewGroup)findViewById(R.id.participantContainer);
		Intent intent = getIntent();
		phone = SignalPhone.getInstance(getApplicationContext());

		participantAddress = intent.getStringExtra(SignalPhoneActivity.CONVERSATION_PARTICIPANT);
		String action = intent.getStringExtra(SignalPhoneActivity.CONVERSATION_ACTION);
		if (action.equalsIgnoreCase(SignalPhoneActivity.CONVERSATION_ACTION_CALL)) {
			callParticipant(participantAddress);
		} else if (action.equalsIgnoreCase(SignalPhoneActivity.CONVERSATION_ACTION_ACCEPT_INCOMING)){
			acceptIncoming(participantAddress);
		} else {
			Log.e(TAG, "Unspecified action");
		}

		
	}

	private void callParticipant(String participantAddress) {
		conv = phone.call(this, participantAddress, localContainer, this, this);
	}
	
	private void acceptIncoming(String participantAddress) {
		conv = phone.accept(this, participantAddress, localContainer, this, this);
	}

	public static class MenuFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.menu, container, false);
		}
	}

	@Override
	public void onParticipantConnected(Conversation conversation,
			Participant participant) {
		Log.i(TAG, "Participant connected: "+participant.getIdentity());
		participant.setParticipantListener(this);
	}

	@Override
	public void onFailedToConnectParticipant(Conversation conversation,
			Participant participant, ConversationException e) {
		Log.w(TAG, "Failed to connect participant: "+e.getMessage());
	}

	@Override
	public void onParticipantDisconnected(Conversation conversation,
			Participant participant) {
		Log.i(TAG, "onParticipantDisconncted: "+participant.getIdentity());

	}

	private void releaseConversation(Conversation conversation) {
		if (conv == conversation) {
			conv.dispose();
			conv = null;
		} else {
			Log.w(TAG, "conversation local reference is different then the one from callback");
		}
		finish();
	}

	@Override
	public void onConversationEnded(Conversation conversation, ConversationException e) {
		if(e != null) {
			Log.i(TAG, "onConversationEnded error:"+e.getMessage());
		}
		releaseConversation(conversation);
	}

	@Override
	public void onResume() {
		super.onResume();
		if(participantVideoRenderer != null) {
			participantVideoRenderer.onResume();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if(participantVideoRenderer != null) {
			participantVideoRenderer.onPause();
		}

	}

	@Override
	public void onVideoTrackAdded(final Conversation conversation,
			final Participant participant, final VideoTrack videoTrack) {
		// Remote participant
		Log.i(TAG, "Participant adding video track");
		participantVideoRenderer = new VideoViewRenderer(ConversationActivity.this, participantContainer);
		participantVideoRenderer.setObserver(new VideoRendererObserver() {

			@Override
			public void onFirstFrame() {
				Log.i(TAG, "Participant onFirstFrame");
			}

			@Override
			public void onFrameDimensionsChanged(int width, int height) {
				Log.i(TAG, "Participant onFrameDimensionsChanged " + width + " " + height);
			}

		});
		videoTrack.addRenderer(participantVideoRenderer);
	}

	@Override
	public void onVideoTrackRemoved(Conversation conversation, Participant participant, VideoTrack videoTrack) {
		Log.i(TAG, "Participant removing video track");
		participantContainer.post(new Runnable() {
			@Override
			public void run() {
				participantContainer.removeAllViews();
			}

		});
	}

	@Override
	public void onAudioTrackAdded(Conversation conversation, Participant participant, AudioTrack audioTrack) {
		Log.i(TAG, "onAudioTrackAdded " + participant.getIdentity());
	}

	@Override
	public void onAudioTrackRemoved(Conversation conversation, Participant participant, AudioTrack audioTrack) {
		Log.i(TAG, "onAudioTrackRemoved " + participant.getIdentity());
	}

	@Override
	public void onTrackEnabled(Conversation conversation, Participant participant, MediaTrack mediaTrack) {
		Log.i(TAG, "onTrackEnabled " + participant.getIdentity());
	}

	@Override
	public void onTrackDisabled(Conversation conversation, Participant participant, MediaTrack mediaTrack) {
		Log.i(TAG, "onTrackDisabled " + participant.getIdentity());
	}


	@Override
	public void onLocalVideoTrackAdded(Conversation conversation,
			LocalVideoTrack localVideoTrack) {
		localVideoTrack.addRenderer(new VideoViewRenderer(this, localContainer));
	}

	@Override
	public void onLocalVideoTrackRemoved(Conversation conversation,
			LocalVideoTrack localVideoTrack) {
		localContainer.removeAllViews();
	}

}
