/*
 *  Copyright (c) 2011 by Twilio, Inc., all rights reserved.
 *
 *  Use of this software is subject to the terms and conditions of
 *  the Twilio Terms of Service located at http://www.twilio.com/legal/tos
 */

package com.twilio.example.quickstart;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ViewGroup;

import com.twilio.signal.CameraCapturer;
import com.twilio.signal.CameraCapturerFactory;
import com.twilio.signal.CapturerErrorListener;
import com.twilio.signal.CapturerException;
import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationException;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.Endpoint;
import com.twilio.signal.EndpointListener;
import com.twilio.signal.Invite;
import com.twilio.signal.LocalMedia;
import com.twilio.signal.LocalVideoTrack;
import com.twilio.signal.LocalVideoTrackFactory;
import com.twilio.signal.MediaFactory;
import com.twilio.signal.TwilioRTC;
import com.twilio.signal.impl.TwilioConstants;


public class SignalPhone implements EndpointListener
{
    private static final String TAG = "SIgnalPhone";

    // TODO: change this to point to the script on your public server
    private static final String ICE_TOKEN_URL_STRING = "http://client:chunder@chunder-interactive.appspot.com/iceToken?realm=prod";
    //private static final String CAPABILITY_TOKEN_URL_STRING = "https://sat-token-generator.herokuapp.com/sat-token?EndpointName=evan";
    private static final String CAPABILITY_TOKEN_URL_STRING =  "https://simple-signaling.appspot.com/token?realm=prod";

    private Endpoint alice = null;
    private String token = "";

    private Map<String, String> options = new HashMap<String, String>();
    private Map<String, Invite> invites = new HashMap<String, Invite>();

    private ExecutorService threadPool;

    public interface LoginListener
    {
        public void onLoginStarted();
        public void onLoginFinished();
        public void onLoginError(String errorMessage);
        public void onLogoutFinished();
        //TODO - !nn! - this is temporary callback
        //we need to figure out how invite will be sent in future
        //(intent, broadcast receiver or something else).
        public void onIncomingCall(String from);
    }

    private static SignalPhone instance;
    public static final SignalPhone getInstance(Context context)
    {
        if (instance == null)
            instance = new SignalPhone(context);
        return instance;
    }

    private static Context context;
    private LoginListener loginListener;

    private static boolean twilioSdkInited;
    private static boolean twilioSdkInitInProgress;
    private Map<String, Conversation> conversations;

    private boolean speakerEnabled;


    private SignalPhone(Context context)
    {
        this.context = context;
        threadPool = Executors.newFixedThreadPool(2);
        conversations = new HashMap<String, Conversation>();
    }

    public void setListeners(LoginListener loginListener)
    {
        this.loginListener = loginListener;
    }

    private void obtainCapabilityToken(String clientName)
    {
    	StringBuilder url = new StringBuilder();
    	url.append(SignalPhone.CAPABILITY_TOKEN_URL_STRING);
    	url.append("&&name=").append(clientName);

        // This runs asynchronously!
    	new GetAuthTokenAsyncTask().execute(url.toString());
    }

	private void obtainIceToken() {
		StringBuilder url = new StringBuilder();
		url.append(SignalPhone.ICE_TOKEN_URL_STRING);
		// This runs asynchronously!
		new GetIceTokenAsyncTask().execute(url.toString());
	}

    private void updateAudioRoute()
    {
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(speakerEnabled);
    }

    public void login(final String clientName)
    {
        if (loginListener != null)
            loginListener.onLoginStarted();

        if (!twilioSdkInited) {
            if (twilioSdkInitInProgress)
                return;

            twilioSdkInitInProgress = true;
            if (loginListener != null) {
				loginListener.onLoginStarted();
            }

            TwilioRTC.setLogLevel(Log.DEBUG);
            String versionText = TwilioRTC.getVersion();

            TwilioRTC.initialize(context, new TwilioRTC.InitListener() {
				@Override
				public void onInitialized() {
					twilioSdkInited = true;
					twilioSdkInitInProgress = false;
					obtainCapabilityToken(clientName);

				}

				@Override
				public void onError(Exception error) {
					twilioSdkInitInProgress = false;
					if (loginListener != null)
						loginListener.onLoginError(error.getMessage());
				}
			});
		} else {
			obtainCapabilityToken(clientName);
		}
	}

    public void setSpeakerEnabled(boolean speakerEnabled)
    {
        if (speakerEnabled != this.speakerEnabled) {
            this.speakerEnabled = speakerEnabled;
            updateAudioRoute();
        }
    }

    public void connect(Map<String, String> inParams)
    {

    }

    public Conversation call(Activity activity, String participant, ViewGroup localContainer, ConversationListener conversationListener) {
    	if (participant == null || participant == "") {
    		return null;
    	}
    	if (!twilioSdkInited || (SignalPhone.this.alice == null)) {
    		return null;
    	}
    	LocalMedia localMedia = MediaFactory.createLocalMedia();
    	Conversation conv = null;
    	CameraCapturer camera = CameraCapturerFactory.createCameraCapturer(
                activity,
                CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA,
                localContainer, capturerErrorListener());
    	if (camera != null) {
	    	LocalVideoTrack videoTrack = LocalVideoTrackFactory.createLocalVideoTrack(camera);
	    	localMedia.attachContainerView(localContainer);
	    	localMedia.addLocalVideoTrack(videoTrack);
	    	Set<String> participants = new HashSet<String>();
	    	participants.add(participant);
	    	conv = SignalPhone.this.alice.createConversation(
	    			participants, localMedia, conversationListener);
	    	if (conv != null) {
	    		conversations.put(conv.getConversationSid(), conv);
	    	}
    	}
    	
    	return conv;
    }

    public void disconnect()
    {

    }
    
    private CapturerErrorListener capturerErrorListener () {
    	return new CapturerErrorListener() {
			
			@Override
			public void onError(CapturerException e) {
				Log.e(TAG, e.getMessage());
				
			}
		};
    }

    public Conversation accept(Context context, String from, ViewGroup localContainer, ConversationListener listener) {
       Invite invite = invites.remove(from);
       if (!twilioSdkInited || invite == null || invite.to() == null) {
    	   return null;
       }
       LocalMedia localMedia = MediaFactory.createLocalMedia();
       Conversation conv = null;
       CameraCapturer camera = CameraCapturerFactory.createCameraCapturer(
               context,
               CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA,
               localContainer, capturerErrorListener());
       if (camera != null) {
	       LocalVideoTrack videoTrack = LocalVideoTrackFactory.createLocalVideoTrack(camera);
	       localMedia.addLocalVideoTrack(videoTrack);
	       localMedia.attachContainerView(localContainer);
	       conv = invite.accept(localMedia, listener);
	       return conv;
       }
       return null;
    }
    
    public void reject(String from) {
    	Invite invite = invites.remove(from);
    	if (invite != null) {
    		invite.reject();
    	}
    }
    
    public void ignore(String from) {
    	Invite invite = invites.remove(from);
    	if (invite != null) {
    		//TODO - !nn! - how do we ignore?
    	}
    }

    public void ignoreIncomingConnection()
    {

    }

    private void listen() {
    	if (SignalPhone.this.alice != null) {
			threadPool.execute(new Runnable() {

				@Override
				public void run() {
					SignalPhone.this.alice.listen();
				}
			});
		}

    }

    private void createEndpoint(String capabilityToken) {
    	if (loginListener != null) {
			loginListener.onLoginStarted();
		}
		SignalPhone.this.alice = TwilioRTC.createEndpoint(capabilityToken, SignalPhone.this);
		listen();
		Intent intent = new Intent(context, SignalPhoneActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //alice.setIncomingIntent(pendingIntent);
		/*} else {
			SignalPhone.this.alice.listen();
		}*/
		 Log.i(TAG, "Created Endpoint With Token");
    }


    private class GetAuthTokenAsyncTask extends AsyncTask<String, Void, String> {
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			//obtainIceToken();
			createEndpoint(result);
		}

		@Override
		protected String doInBackground(String... params) {
			String capabilityToken = null;
			try {
				capabilityToken = HttpHelper.httpGet(params[0]);
				options.put(TwilioConstants.EndpointOptionCapabilityTokenKey, capabilityToken);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return capabilityToken;
		}
    }

    private class GetIceTokenAsyncTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			//if (SignalPhone.this.alice == null) {
			/*
			if (loginListener != null) {
				loginListener.onLoginStarted();
			}
			SignalPhone.this.alice = TwilioSignal.createEndpointWithToken(
					options, token, SignalPhone.this);
			Intent intent = new Intent(context, SignalPhoneActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alice.setIncomingIntent(pendingIntent);
			/*} else {
				SignalPhone.this.alice.listen();
			}*/
			 Log.i(TAG, "Created Endpoint With Token");
		}

		@Override
		protected String doInBackground(String... params) {
			String capabilityToken = null;
			try {
				String response = HttpHelper.httpGet(params[0]).replace('[', ' ').replace(']', ' ');
				String [] tempBuf = response.split(",",2);
				JSONObject stunJson = new JSONObject(tempBuf[0]);
				JSONObject turnJson = new JSONObject(tempBuf[1]);
				String stunUrl = (String) stunJson.get("url");
				String credential = (String) turnJson.get("credential");
				String username = (String) turnJson.get("username");
				String turnUrl = (String) turnJson.get("url");

				options.put(TwilioConstants.EndpointOptionStunURLKey, stunUrl);
				options.put(TwilioConstants.EndpointOptionTurnURLKey, turnUrl);
				options.put(TwilioConstants.EndpointOptionUserNameKey, username);
				options.put(TwilioConstants.EndpointOptionPasswordKey, credential);

			} catch (Exception e) {
				e.printStackTrace();
			}
			return capabilityToken;
		}
    }



	public void onEndpointStartListeningForInvites(Endpoint endpoint) {
		if (loginListener != null) {
			loginListener.onLoginFinished();
		}
	}


	public void onEndpointStopListeningForInvites(Endpoint endpoint) {
		

	}

	@Override
	public void onFailedToStartListening(Endpoint endPoint, ConversationException e) {
		Log.d(TAG, "onFailedToStartListening msg:"+e.getMessage());
		if (loginListener != null)
			loginListener.onLoginError(e.getMessage());

	}


	public void logout() {
		SignalPhone.this.alice.unlisten();
	}

	@Override
	public void onStartListeningForInvites(Endpoint endpoint) {
		Log.d(TAG, "onStartListeningForInvites");
		if (loginListener != null) {
			loginListener.onLoginFinished();
		}
	}

	@Override
	public void onStopListeningForInvites(Endpoint endpoint) {
		if (loginListener != null) {
			loginListener.onLogoutFinished();
		}
		if (SignalPhone.this.alice == null) {
			Log.w(TAG, "Alice is null");
		}
		if (SignalPhone.this.alice != endpoint) {
			Log.w(TAG, "Alice is different then endpoing from callback");
		}
		//Get rid of endpoint
		SignalPhone.this.alice.dispose();
		SignalPhone.this.alice = null;
	}

	@Override
	public void onReceiveConversationInvite(Endpoint endpoint, Invite invite) {
		Log.d(TAG, "onReceiveConversationInvite");
		if (loginListener != null) {
			invites.put(invite.from(), invite);
			loginListener.onIncomingCall(invite.from());
		}
		
	}

}
