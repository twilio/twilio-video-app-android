package com.twilio.conversations;

import java.util.List;

/**
 * A video track represents a single local or remote video source
 */
public interface VideoTrack extends MediaTrack {
    /**
     * Add a video renderer to get video from the video track
     *
     * @param videoRenderer video renderer that receives video
     */
    void addRenderer(VideoRenderer videoRenderer);

    /**
     * Remove a video renderer to stop receiving video from the video track
     *
     * @param videoRenderer the video renderer that should no longer receives video
     */
    void removeRenderer(VideoRenderer videoRenderer);

    /**
     * The list of renderers receiving video from this video track
     * @return
     */
    List<VideoRenderer> getRenderers();
}

