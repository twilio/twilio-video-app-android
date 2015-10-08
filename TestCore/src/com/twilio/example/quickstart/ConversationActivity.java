package com.twilio.example.quickstart;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.twilio.signal.Conversation;
import com.twilio.signal.Conversation.Status;
import com.twilio.signal.ConversationException;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.Participant;
import com.twilio.signal.VideoRendererObserver;
import com.twilio.signal.VideoTrack;
import com.twilio.signal.VideoViewRenderer;

public class ConversationActivity extends Activity implements ConversationListener {

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

		participantAddress = getIntent().getStringExtra(SignalPhoneActivity.CONVERSATION_PARTICIPANT);

		callParticipant(participantAddress);
	}

	private void callParticipant(String participantAddress) {
		phone = SignalPhone.getInstance(getApplicationContext());
		conv = phone.call(this, participantAddress, localContainer, this);
	}

	public static class MenuFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.menu, container, false);
		}
	}

	@Override
	public void onConnectParticipant(Conversation conversation,
			Participant participant) {
		
	}

	@Override
	public void onFailToConnectParticipant(Conversation conversation,
			Participant participant, ConversationException e) {
		Log.w(TAG, "Failed to connect participant: "+e.getMessage());
	}

	@Override
	public void onDisconnectParticipant(Conversation conversation,
			Participant participant) {
		Log.i(TAG, "onDisconnectedParticipant:"+participant.getAddress());

	}

	@Override
	public void onVideoAddedForParticipant(final Conversation conversation,
				final Participant participant, final VideoTrack videoTrack) {
		runOnUiThread(new Runnable() {

			public void run() {
				// Remote participant
				Log.i(TAG, "Participant adding video track");
				participantVideoRenderer = new VideoViewRenderer(ConversationActivity.this, participantContainer);
				participantVideoRenderer.setObserver(new VideoRendererObserver() {

					@Override
					public void onFirstFrame() {
						Log.i(TAG, "Participant onFirstFrame");
					}

					@Override
					public void onFrameSizeChanged(int width, int height) {
						Log.i(TAG, "Participant onFrameSizeChanged " + width + " " + height);
					}

				});
				videoTrack.addRenderer(participantVideoRenderer);
			}
		});
	}

	@Override
	public void onVideoRemovedForParticipant(Conversation conversation, Participant participant, VideoTrack videoTrack) {
		Log.i(TAG, "Participant removing video track");
		participantContainer.post(new Runnable() {
			@Override
			public void run() {
				participantContainer.removeAllViews();
			}

		});
	}

	@Override
	public void onLocalStatusChanged(Conversation conversation, Status status) {
		Log.i(TAG, "onLocalStatusChanged "+status.name());
	}

	@Override
	public void onConversationEnded(Conversation conversation) {
		Log.i(TAG, "onConversationEnded");
	}

	@Override
	public void onConversationEnded(Conversation conversation, ConversationException e) {
		Log.i(TAG, "onConversationEnded error:"+e.getMessage());
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

}
