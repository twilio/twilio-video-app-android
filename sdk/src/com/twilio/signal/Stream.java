package com.twilio.signal;

import java.util.Set;

import android.view.View;

import com.twilio.signal.Track.TrackId;

public interface Stream {
	/**
	 * This method gets a stream from the local hardware based on a dictionary of media constraints.

	 * @param mediaConstraints Set of media constraints such as front camera, rear camera etc.
	 * @return a Stream object containing the tracks as specified in the media constraints.
	 */
	public Stream getStreamWithConstraints(Set<String> mediaConstraints);
	
	
	/**
	 * This method gets a video from the local hardware’s front facing camera.
	 * 
	 * @return a Stream object containing the video track from the front facing camera.
	 */
	public Stream getVideoFromFrontFacingCamera();
	
	/**
	 * This method gets a video from the local hardware’s rear facing camera.
	 * 
	 * @return  a Stream object containing the video track from the rear facing camera.
	 */
	public Stream getVideoFromRearFacingCamera();
	
	/**
	 * This method gets a screen capture video from the local hardware.
	 * 
	 * @return a Stream object containing the screen capture as a video track.
	 */
	public Stream getScreenCapture();
	
	/**
	 * This method merges all the tracks from another Stream object.
	 * 
	 * @param stream the Stream object whose tracks need to be merged.
	 */
	public void mergeStream(Stream stream);
	
	/**
	 * This method adds a track to the Stream object. 
	 * 
	 * @param track the Track object that needs to be added.
	 */
	public void addTrack(Track track);
	
	/**
	 * This method removes a track from the Stream object.
	 * 
	 * @param track the Track object that needs to be add
	 */
	public void removeTrack(Track track);
	
	/**
	 * This method returns an array of tracks that are present in the Stream object.
	 * 
	 * @return an Set of tracks, each of type Track.
	 */
	public Set<Track> getTracks();
	
	
	/**
	 * This method returns the track with the id specified as an argument.
	 * 
	 * @param trackId
	 * @return A track of type Track.
	 */
	public Track getTrackById(TrackId trackId);
	
	
	/**
	 * 
	 * This method mutes all audio tracks in the stream.
	 * 
	 * @param muted A boolean value: True implies mute and false implies resume.
	 */
	public void muteAudio(boolean muted);
	
	
	/**
	 * This method pauses all video tracks in the stream.
	 * 
	 * @param paused A boolean value: True implies pause and false implies resume.
	 */
	public void pauseVideo(boolean paused);
	
	/**
	 * This method attaches a video track to a view.
	 * 
	 * @param track The video track of type Track to be attached.
	 * @param view The view to which the track needs to be attached.
	 */
	
	public void attachVideoTrack(Track track, View view);
	
	/**
	 * This method attaches an audio track to the device’s audio output.
	 * 
	 * @param audio The audio track of type Track to be attached.
	 */
	public void attachAudioTrack(Track audio);
	
	/**
	 * This method detaches a video track from the specified view.
	 * 
	 * @param track The track of type Track to be detached.
	 * @param view The view from which the track needs to be detached.
	 */
	
	public void detchVideoTrack(Track track, View view);
	
	
	/**
	 * This method detaches a track. If it is a video track, it is detached from all views to which it was previously attached. If it is an audio track, 
	 * it is detached from system’s audio output.
	 *
	 * @param track The track of type Track to be detached.
	 */
	public void detachTrack(Track track);

}
