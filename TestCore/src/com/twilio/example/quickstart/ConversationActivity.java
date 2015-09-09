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

import com.twilio.signal.Conversation;

public class ConversationActivity extends Activity {

	private static final String TAG = "ConversationActivity";
	
	private SignalPhone phone;
	private Conversation conv;
	private GLSurfaceView localView;
	private GLSurfaceView remoteView;
	private final Object syncObject = new Object();

	private GLSurfaceView[] views = new GLSurfaceView[2];

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.conversation);

		localView = (GLSurfaceView)findViewById(R.id.localView);
		//remoteView = (GLSurfaceView)findViewById(R.id.remoteView);

		views[0] = localView;
		views[1] = localView;

		attemptCall();
	}

	private void attemptCall() {
		if(views[0] != null && views[1] != null) {
			makeCall();
		}
	}

	private void makeCall() {
		String participant = getIntent().getStringExtra(SignalPhoneActivity.CONVERSATION_PARTICIPANT);

		phone = SignalPhone.getInstance(getApplicationContext());
		conv = phone.call(participant, views);
		if(conv != null) {
			// do stuff
		}
	}


	public static class MenuFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.menu, container, false);
		}
	}
}
