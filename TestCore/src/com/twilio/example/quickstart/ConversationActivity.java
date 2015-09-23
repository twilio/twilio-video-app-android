package com.twilio.example.quickstart;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.opengl.GLSurfaceView;

import com.twilio.signal.Participant;
import com.twilio.signal.Conversation;
import com.twilio.signal.Conversation.Status;
import com.twilio.signal.ConversationListener;

import org.webrtc.VideoTrack;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

public class ConversationActivity extends Activity implements ConversationListener {

	private static final String TAG = "ConversationActivity";
	
	private SignalPhone phone;
	private Conversation conv;
	private GLSurfaceView videoView;
	private String participantAddress;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.conversation);

		videoView = (GLSurfaceView)findViewById(R.id.surface);

		VideoRendererGui.setView(videoView, new Runnable() {
			@Override
			public void run() {
				callParticipant(participantAddress);
			}
		});

		participantAddress = getIntent().getStringExtra(SignalPhoneActivity.CONVERSATION_PARTICIPANT);
	}

	private void callParticipant(String participantAddress) {
		phone = SignalPhone.getInstance(getApplicationContext());
		conv = phone.call(this, participantAddress, null, this);
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
			Participant participant, VideoTrack videoTrack) {

		VideoRenderer renderer;
		try {
			if(!participant.getAddress().equals(participantAddress)) {
				// Host participant
				Log.i(TAG, "Adding local renderer");
				renderer = VideoRendererGui.createGui(70, 70, 28, 28, VideoRendererGui.ScalingType.SCALE_ASPECT_FIT, true);
				videoTrack.addRenderer(renderer);
			} else {
				// Remote participant
				Log.i(TAG, "Adding remote renderer");
				renderer = VideoRendererGui.createGui(0, 0, 100, 100, VideoRendererGui.ScalingType.SCALE_ASPECT_FIT, true);
				videoTrack.addRenderer(renderer);
			}
		} catch(Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	/*
	private VideoRenderer createVideoRenderer(ViewGroup container) {
		GLSurfaceView view = new GLSurfaceView(ConversationActivity.this);
		container.addView(view);

		final VideoRendererGui videoRendererGui = new VideoRendererGui(view, null);
		//return videoRendererGui.createRenderer(0, 0, 100, 100, VideoRendererGui.ScalingType.SCALE_ASPECT_FIT, true);
		return null;
	}
	*/


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
