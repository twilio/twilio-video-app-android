package com.twilio.example.quickstart;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.graphics.Point;
import android.graphics.PixelFormat;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;

import com.twilio.signal.Participant;
import com.twilio.signal.Conversation;
import com.twilio.signal.Conversation.Status;
import com.twilio.signal.ConversationListener;

public class ConversationActivity extends Activity implements ConversationListener {

	private static final String TAG = "ConversationActivity";
	
	private SignalPhone phone;
	private Conversation conv;
	private ViewGroup localContainer;
	private ViewGroup participantContainer;
	private final Object syncObject = new Object();
	private String participantAddress;

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
			Participant participant, int error, String errorMessage) {

	}

	@Override
	public void onDisconnectParticipant(Conversation conversation,
			Participant participant) {

	}

	@Override
	public void onVideoAddedForParticipant(Conversation conversation,
			Participant participant) {
		if(!participant.getAddress().equals(participantAddress)) {
			participant.getMedia().attachContainerView(localContainer);
		} else {
			participant.getMedia().attachContainerView(participantContainer);
		}
	}

	@Override
	public void onVideoRemovedForParticipant(Conversation conversation, Participant participant) {

	}

	@Override
	public void onLocalStatusChanged(Conversation conversation, Status status) {

	}

	@Override
	public void onConversationEnded(Conversation conversation) {

	}

	@Override
	public void onConversationEnded(Conversation conversation, int error, String errorMessage) {

	}

}
