package com.twilio.conversations.impl;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.os.Handler;

import com.twilio.conversations.LocalMedia;
import com.twilio.conversations.LocalMediaListener;
import com.twilio.conversations.LocalVideoTrack;
import com.twilio.conversations.MediaTrackState;
import com.twilio.conversations.TwilioConversations;
import com.twilio.conversations.TwilioConversationsException;
import com.twilio.conversations.impl.core.TrackInfo;
import com.twilio.conversations.impl.logging.Logger;
import com.twilio.conversations.impl.util.CallbackHandler;

public class LocalMediaImpl implements LocalMedia {
    private List<LocalVideoTrackImpl> videoTracksImpl = new ArrayList<LocalVideoTrackImpl>();
    private WeakReference<ConversationImpl> convWeak;
    private boolean audioEnabled;
    private boolean audioMuted;
    private Handler handler;
    private LocalMediaListener localMediaListener;

    private static int MAX_LOCAL_VIDEO_TRACKS = 1;

    private static String TAG = "LocalMediaImpl";
    static final Logger logger = Logger.getLogger(LocalMediaImpl.class);

    public LocalMediaImpl(LocalMediaListener localMediaListener) {
        this.localMediaListener = localMediaListener;
        audioEnabled = true;
        audioMuted = false;

        handler = CallbackHandler.create();
        if(handler == null) {
            throw new IllegalThreadStateException("This thread must be able to obtain a Looper");
        }
    }

    Handler getHandler() {
        return handler;
    }

    @Override
    public LocalMediaListener getLocalMediaListener() {
        return localMediaListener;
    }

    @Override
    public void setLocalMediaListener(LocalMediaListener listener) {
        localMediaListener = listener;
    }

    @Override
    public boolean mute(boolean on) {
        if (convWeak != null && convWeak.get() != null && convWeak.get().getNativeHandle() != 0) {
            audioMuted = on;
            return convWeak.get().mute(on);
        } else if (audioMuted != on){
            audioMuted = on;
            return true;
        }
        return false;
    }

    @Override
    public boolean isMuted() {
        return audioMuted;
    }

    @Override
    public List<LocalVideoTrack> getLocalVideoTracks() {
        return new ArrayList<LocalVideoTrack>(videoTracksImpl);
    }

    @Override
    public void addLocalVideoTrack(final LocalVideoTrack track)
            throws IllegalArgumentException, UnsupportedOperationException {
        if (track == null) {
            throw new NullPointerException("LocalVideoTrack can't be null");
        }
        LocalVideoTrackImpl localVideoTrackImpl = (LocalVideoTrackImpl)track;
        if(!localVideoTrackImpl.getState().equals(MediaTrackState.IDLE)) {
            postVideoTrackException(localVideoTrackImpl, new TwilioConversationsException(TwilioConversations.TRACK_OPERATION_IN_PROGRESS, " A track operation is already in progress."));
        }
        if (videoTracksImpl.size() >= MAX_LOCAL_VIDEO_TRACKS) {
            postVideoTrackException(localVideoTrackImpl, new TwilioConversationsException(TwilioConversations.TOO_MANY_TRACKS, "Unable to add the local video track. Only " + MAX_LOCAL_VIDEO_TRACKS + " local video track is supported."));
        }
        if (localVideoTrackImpl.getCameraCapturer() == null) {
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (localMediaListener != null) {
                            localMediaListener.onLocalVideoTrackError(
                                    LocalMediaImpl.this, track, new TwilioConversationsException(TwilioConversations.INVALID_VIDEO_CAPTURER, "The LocalVideoTrack must be associated with a camera capturer"));
                        }
                    }
                });
            }
        }
        videoTracksImpl.add(localVideoTrackImpl);
        if ((convWeak != null) &&  (convWeak.get() != null) ) {
            // LocalVideoTrack is added during conversation
            // TODO: we should use localVideoTrackImpl.isEnabled() as second param here,
            // it is hard coded as false for now until we resolve issue with CameraCapturer starting in disabled mode.
            // This leaves responsibility to a user to unpause the capturer, which user doesn't have to do
            // during initial creation. This is inconsistent behavior and it should be more investigated.
            CameraCapturerImpl cameraCapturerImpl = (CameraCapturerImpl)localVideoTrackImpl.getCameraCapturer();
            if(cameraCapturerImpl.getCapturerState() !=
                    CameraCapturerImpl.CapturerState.BROADCASTING) {
                logger.d("Create a new external capturer since the nativeVideoCapturer is no longer valid");
                convWeak.get().setupExternalCapturer();
            }
            boolean enabledVideo = convWeak.get().enableVideo(true, !localVideoTrackImpl.isEnabled(), localVideoTrackImpl.getVideoConstraints());
            if(!enabledVideo) {
                // Remove the video track since it failed to be added
                videoTracksImpl.remove(localVideoTrackImpl);
                postVideoTrackException(localVideoTrackImpl, new TwilioConversationsException(TwilioConversations.TRACK_OPERATION_IN_PROGRESS, " A track operation is already in progress."));
            } else {
                localVideoTrackImpl.setTrackState(MediaTrackState.STARTING);
            }
        }
    }

    @Override
    public void removeLocalVideoTrack(LocalVideoTrack track) throws IllegalArgumentException {
        if (videoTracksImpl.size() == 0) {
            logger.w("There are no local video tracks in the list");
            return;
        } else if (!videoTracksImpl.contains(track)) {
            logger.w("The specified local video track was not found");
            return;
        }
        if(track.getState().equals(MediaTrackState.ENDED)) {
            postVideoTrackException(track, new TwilioConversationsException(TwilioConversations.INVALID_VIDEO_TRACK_STATE, "The provided video track is not in a valid state"));
        } else if(!track.getState().equals(MediaTrackState.STARTED)) {
            postVideoTrackException(track, new TwilioConversationsException(TwilioConversations.TRACK_OPERATION_IN_PROGRESS, " A track operation is already in progress."));
        }
        if (convWeak == null || convWeak.get() == null) {
            logger.d("Conversation is null");
            return;
        }
        ConversationImpl conv = convWeak.get();
        boolean enabled = conv.enableVideo(false, !track.isEnabled(), null);
        if(enabled) {
            ((LocalVideoTrackImpl) track).setTrackState(MediaTrackState.ENDING);
        }
    }

    private void postVideoTrackException(final LocalVideoTrack localVideoTrack, final TwilioConversationsException trackException) {
        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (localMediaListener != null) {
                        localMediaListener.onLocalVideoTrackError(
                                LocalMediaImpl.this, localVideoTrack, trackException);
                    }
                }
            });
        }
    }

    LocalVideoTrackImpl removeLocalVideoTrack(TrackInfo trackInfo) {
        for(LocalVideoTrackImpl videoTrackImpl : new ArrayList<LocalVideoTrackImpl>(videoTracksImpl)) {
            if(trackInfo.getTrackId().equals(videoTrackImpl.getTrackInfo().getTrackId())) {
                videoTracksImpl.remove(videoTrackImpl);
                return videoTrackImpl;
            }
        }
        return null;
    }

    void setConversation(ConversationImpl conversation) {
        this.convWeak = new WeakReference<>(conversation);
    }

    @Override
    public boolean addMicrophone() {
        if (!audioEnabled) {
            audioEnabled = enableAudio(true);
            return audioEnabled;
        } else {
            return false;
        }
    }

    @Override
    public boolean removeMicrophone() {
        if (audioEnabled) {
            audioEnabled = !enableAudio(false);
            return !audioEnabled;
        } else {
            return false;
        }
    }

    private boolean enableAudio(boolean enable) {
        if (convWeak != null && convWeak.get() != null) {
            return convWeak.get().enableAudio(enable, audioMuted);
        } else {
            // The conversation is not ongoing. Always return true
            return true;
        }
    }

    @Override
    public boolean isMicrophoneAdded() {
        return audioEnabled;
    }
}
