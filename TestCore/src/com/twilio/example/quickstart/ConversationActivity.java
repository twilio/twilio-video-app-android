package com.twilio.example.quickstart;

import android.app.Activity;
import android.app.Fragment;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class ConversationActivity extends Activity {

	// VideoView remoteVideo;
	// VideoView localVideo;

	private static final String TAG = "ConversationActivity";

	private GLSurfaceView videoView;

	// private View rootView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversation);
		// videoView = (GLSurfaceView) findViewById(R.id.videopane);

		((ImageButton) findViewById(R.id.cameratoggleIB))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Log.d(TAG, "Switching Camera");
					}
				});

		((ImageButton) findViewById(R.id.videotoggleIB))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Log.d(TAG, "Toggling Video");
					}
				});

		((ImageButton) findViewById(R.id.mutetoggleIB))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Log.d(TAG, "Toggling Mute");
					}
				});

		((ImageButton) findViewById(R.id.hangupIB))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Log.d(TAG, "Hanging up");
					}
				});

		// remoteVideo = (VideoView) findViewById(R.id.remote);
		// localVideo = (VideoView) findViewById(R.id.local);
		//
		// // MediaController mediaController = new MediaController(this);
		// // remoteVideo.setMediaController(mediaController);
		// remoteVideo.setVideoURI(Uri.parse("android.resource://"
		// + getPackageName() + "/" + R.raw.owl));
		// remoteVideo.requestFocus();
		// remoteVideo.start();
		//
		// // MediaController mediaController2 = new MediaController(this);
		// // localVideo.setMediaController(mediaController2);
		// localVideo.setVideoURI(Uri.parse("android.resource://"
		// + getPackageName() + "/" + R.raw.babe));
		// localVideo.requestFocus();
		// localVideo.start();

	}

	public static class MenuFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.menu, container, false);
		}
	}
}
