/*
 *  Copyright (c) 2011 by Twilio, Inc., all rights reserved.
 *
 *  Use of this software is subject to the terms and conditions of 
 *  the Twilio Terms of Service located at http://www.twilio.com/legal/tos
 */

package com.twilio.example.testcore;

import java.sql.Connection;
import java.util.Map;

import android.app.PendingIntent;
import android.bluetooth.BluetoothClass.Device;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.util.Log;

import com.twilio.signal.Capability;
import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.Endpoint;
import com.twilio.signal.EndpointListener;
import com.twilio.signal.Invite;
import com.twilio.signal.RemoteEndpoint;
import com.twilio.signal.Stream;
import com.twilio.signal.Track.TrackId;
import com.twilio.signal.TwilioSignal;


public class SignalPhone implements EndpointListener, ConversationListener
{
    private static final String TAG = "SIgnalPhone";

    // TODO: change this to point to the script on your public server
    private static final String AUTH_PHP_SCRIPT = "http://webrtc-phone.appspot.com/token?client=kumkum&realm=prod";
    private Endpoint endpoint;

    public interface LoginListener
    {
        public void onLoginStarted();
        public void onLoginFinished();
        public void onLoginError(Exception error);
    }

    private static SignalPhone instance;
    public static final SignalPhone getInstance(Context context)
    {
        if (instance == null)
            instance = new SignalPhone(context);
        return instance;
    }

    private final Context context;
    private LoginListener loginListener;

    private static boolean twilioSdkInited;
    private static boolean twilioSdkInitInProgress;
   
    private boolean speakerEnabled;
   

    private SignalPhone(Context context)
    {
        this.context = context;
    }

    public void setListeners(LoginListener loginListener)
    {
        this.loginListener = loginListener;
    }

    private void obtainCapabilityToken(String clientName, 
    								  boolean allowOutgoing, 
    								  boolean allowIncoming)
    {
    	StringBuilder url = new StringBuilder();
    	url.append(AUTH_PHP_SCRIPT);
    	url.append("?allowOutgoing=").append(allowOutgoing);
    	if (allowIncoming && (clientName != null)) {
    		url.append("&&client=").append(clientName);
    	}
    	
        // This runs asynchronously!
    	new GetAuthTokenAsyncTask().execute(url.toString());
    }

    private boolean isCapabilityTokenValid()
    {
       /* if (device == null || device.getCapabilities() == null)
            return false;
        long expTime = (Long)device.getCapabilities().get(Capability.EXPIRATION);
        return expTime - System.currentTimeMillis() / 1000 > 0;
        */
    	return true;
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
            TwilioSignal.setLogLevel(Log.DEBUG);
            String versionText = TwilioSignal.getVersion();
           
            TwilioSignal.initialize(context, new TwilioSignal.InitListener()
            {
                @Override
                public void onInitialized()
                {
                    twilioSdkInited = true;
                    twilioSdkInitInProgress = false;
                    SignalPhone.this.endpoint = TwilioSignal.createEndpoint("", SignalPhone.this);
                    if(SignalPhone.this.endpoint != null) {
                    	SignalPhone.this.endpoint.register();
                    }
                }

                @Override
                public void onError(Exception error)
                {
                    twilioSdkInitInProgress = false;
                    if (loginListener != null)
                        loginListener.onLoginError(error);
                }
            });
        } else {
        	//TODO
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

    public void disconnect()
    {
        
    }

    public void acceptConnection()
    {
       
    }

    public void ignoreIncomingConnection()
    {
        
    }
   
    
    private class GetAuthTokenAsyncTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			//BasicPhone.this.reallyLogin(result);
		}

		@Override
		protected String doInBackground(String... params) {
			String capabilityToken = null;
			try {
				capabilityToken = HttpHelper.httpGet(params[0]);;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return capabilityToken;
		}
    }


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

	@Override
	public void onEndpointStartListeningForInvites(Endpoint endpoint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEndpointStopListeningForInvites(Endpoint endpoint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFailedToStartListening(Endpoint endPoint, int errorCode,
			String errorMessage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCreatedConversation(Conversation conversation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCreateConversationFailedWithError(int errorCode,
			String errorMessage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceivedConversationInvite(Invite invite) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLeftConversation(Conversation conversation) {
		// TODO Auto-generated method stub
		
	}

}
