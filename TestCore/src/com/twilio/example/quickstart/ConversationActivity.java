package com.twilio.example.quickstart;

import android.app.Activity;
import android.app.Fragment;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.graphics.Point;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;

import com.twilio.signal.Conversation;

public class ConversationActivity extends Activity {

	private static final String TAG = "ConversationActivity";
	
	private SignalPhone phone;
	private Conversation conv;
	private TextureView localView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.conversation);

		localView = (TextureView)findViewById(R.id.localView);

		localView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {

			public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
				makeCall(surface);
			}

			public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

			}

			public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
				return true;	
			}

			public void onSurfaceTextureUpdated(SurfaceTexture surface) {
				
			}

		});

	}

	private void makeCall(SurfaceTexture localSurface) {
		String participant = getIntent().getStringExtra(SignalPhoneActivity.CONVERSATION_PARTICIPANT);

		phone = SignalPhone.getInstance(getApplicationContext());
		conv = phone.call(participant, localSurface);
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
