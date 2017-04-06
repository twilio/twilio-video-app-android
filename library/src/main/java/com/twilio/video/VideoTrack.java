package com.twilio.video;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A video track represents a remote video source.
 */
public class VideoTrack implements Track {
    private static final String WARNING_NULL_RENDERER = "Attempted to add a null renderer.";
    private static final Logger logger = Logger.getLogger(VideoTrack.class);

    private final org.webrtc.VideoTrack webrtcVideoTrack;
    private final String trackId;
    private Map<VideoRenderer, org.webrtc.VideoRenderer> videoRenderersMap = new HashMap<>();
    private boolean isEnabled;
    private boolean isReleased = false;

    /*
     * NOTE
     * This class is a wrapper for org.webrtc.VideoTrack. In order to remove the dependency
     * on this class we would need to re-implement some JNI renderer boilerplate
     */
    VideoTrack(org.webrtc.VideoTrack webRtcVideoTrack) {
        this.webrtcVideoTrack = webRtcVideoTrack;
        this.trackId = webRtcVideoTrack.id();
        this.isEnabled = webRtcVideoTrack.enabled();
    }

    /**
     * Add a video renderer to get video from the video track.
     *
     * @param videoRenderer video renderer that receives video.
     */
    public synchronized void addRenderer(VideoRenderer videoRenderer) {
        if (isReleased) {
            logger.w("Cannot add renderer. Video track has been removed.");
            return;
        }
        if (videoRenderer != null) {
            org.webrtc.VideoRenderer webrtcVideoRenderer =
                    createWebRtcVideoRenderer(videoRenderer);
            videoRenderersMap.put(videoRenderer, webrtcVideoRenderer);
            webrtcVideoTrack.addRenderer(webrtcVideoRenderer);
        } else {
            logger.w(WARNING_NULL_RENDERER);
        }
    }

    /**
     * Remove a video renderer to stop receiving video from the video track.
     *
     * @param videoRenderer the video renderer that should no longer receives video.
     */
    public synchronized void removeRenderer(VideoRenderer videoRenderer) {
        if (!isReleased && videoRenderer != null) {
            org.webrtc.VideoRenderer webrtcVideoRenderer =
                    videoRenderersMap.remove(videoRenderer);
            if (webrtcVideoTrack != null && webrtcVideoRenderer != null) {
                webrtcVideoTrack.removeRenderer(webrtcVideoRenderer);
            }
        }
    }

    /**
     * The list of renderers receiving video from this video track.
     */
    public List<VideoRenderer> getRenderers() {
        return new ArrayList<>(videoRenderersMap.keySet());
    }

    /**
     * This video track id.
     *
     * @return track id.
     */
    @Override
    public String getTrackId() {
        return trackId;
    }

    /**
     * Check if this video track is enabled.
     *
     * @return true if track is enabled.
     */
    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    private org.webrtc.VideoRenderer createWebRtcVideoRenderer(VideoRenderer videoRenderer) {
        return new org.webrtc.VideoRenderer(new VideoRendererCallbackAdapter(videoRenderer));
    }

    void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    synchronized void release() {
        if (!isReleased) {
            isEnabled = false;
            for (org.webrtc.VideoRenderer videoRenderer : videoRenderersMap.values()) {
                webrtcVideoTrack.removeRenderer(videoRenderer);
            }
            videoRenderersMap.clear();
            isReleased = true;
        }
    }

    private class VideoRendererCallbackAdapter implements org.webrtc.VideoRenderer.Callbacks {
        private final VideoRenderer videoRenderer;

        public VideoRendererCallbackAdapter(VideoRenderer videoRenderer) {
            this.videoRenderer = videoRenderer;
        }

        @Override
        public void renderFrame(org.webrtc.VideoRenderer.I420Frame frame) {
            videoRenderer.renderFrame(new I420Frame(frame));
        }
    }
}

