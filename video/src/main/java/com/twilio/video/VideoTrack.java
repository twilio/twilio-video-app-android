package com.twilio.video;

import com.twilio.video.internal.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A video track represents a single local or remote video source
 */
public class VideoTrack {
    private static final String WARNING_NULL_RENDERER = "Attempted to add a null renderer.";
    private static final Logger logger = Logger.getLogger(VideoTrack.class);

    private org.webrtc.VideoTrack webrtcVideoTrack;
    private String trackId;
    //private MediaTrackState trackState;
    private Map<VideoRenderer, org.webrtc.VideoRenderer> videoRenderersMap = new HashMap<>();
    private boolean isEnabled;
    private long nativeVideoTrackContext;

    /**
     * Add a video renderer to get video from the video track
     *
     * @param videoRenderer video renderer that receives video
     */
    public void addRenderer(VideoRenderer videoRenderer) {
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
     * Remove a video renderer to stop receiving video from the video track
     *
     * @param videoRenderer the video renderer that should no longer receives video
     */
    public void removeRenderer(VideoRenderer videoRenderer) {
        if (videoRenderer != null) {
            org.webrtc.VideoRenderer webrtcVideoRenderer =
                    videoRenderersMap.remove(videoRenderer);
            if (webrtcVideoRenderer != null) {
                webrtcVideoTrack.removeRenderer(webrtcVideoRenderer);
            }
        }
    }

    /**
     * The list of renderers receiving video from this video track
     */
    public List<VideoRenderer> getRenderers() {
        return new ArrayList<>(videoRenderersMap.keySet());
    }

    private org.webrtc.VideoRenderer createWebRtcVideoRenderer(VideoRenderer videoRenderer) {
        return new org.webrtc.VideoRenderer(new VideoRendererCallbackAdapter(videoRenderer));
    }

    public String getTrackId() {
        return trackId;
    }

//    @Override
//    public MediaTrackState getState() {
//        return trackState;
//    }


    public boolean isEnabled() {
        return isEnabled;
    }

//    void updateTrackInfo(TrackInfo trackInfo) {
//        this.trackInfo = trackInfo;
//    }

//    void setTrackState(MediaTrackState trackState) {
//        this.trackState = trackState;
//    }

//    VideoTrack() {
//        trackState = MediaTrackState.IDLE;
//    }
//
//    VideoTrack(org.webrtc.VideoTrack videoTrack, TrackInfo trackInfo) {
//        this.videoTrack = videoTrack;
//        this.trackInfo = trackInfo;
//
//        trackState = MediaTrackState.STARTED;
//    }

    VideoTrack(long nativeVideoTrackContext, String trackId,
               boolean isEnabled, long nativeWebrtcTrack) {
        this.nativeVideoTrackContext = nativeVideoTrackContext;
        this.trackId = trackId;
        this.isEnabled = isEnabled;
        this.webrtcVideoTrack = new org.webrtc.VideoTrack(nativeWebrtcTrack);
    }

//    void setWebrtcVideoTrack(org.webrtc.VideoTrack videoTrack) {
//        this.videoTrack = videoTrack;
//
//        trackState = MediaTrackState.STARTED;
//    }

//    void setTrackInfo(TrackInfo trackInfo) {
//        this.trackInfo = trackInfo;
//    }

//    org.webrtc.VideoTrack getWebrtcVideoTrack() {
//        return videoTrack;
//    }

//    TrackInfo getTrackInfo() {
//        return trackInfo;
//    }

    private class VideoRendererCallbackAdapter implements org.webrtc.VideoRenderer.Callbacks {
        private final VideoRenderer videoRenderer;

        public VideoRendererCallbackAdapter(VideoRenderer videoRenderer) {
            this.videoRenderer = videoRenderer;
        }

        @Override
        public void renderFrame(org.webrtc.VideoRenderer.I420Frame frame) {
            videoRenderer.renderFrame(transformWebRtcFrame(frame));
        }

        private I420Frame transformWebRtcFrame(org.webrtc.VideoRenderer.I420Frame frame) {
            long frameNativePointer;
            try {
                Field nativeFramePointField = frame.getClass().getDeclaredField("nativeFramePointer");
                nativeFramePointField.setAccessible(true);
                frameNativePointer = nativeFramePointField.getLong(frame);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Unable to retrieve I420 Frame native pointer");
            } catch( IllegalAccessException e) {
                throw new RuntimeException("Unable to retrieve I420 Frame native pointer");
            }
            return new I420Frame(frame.width,
                    frame.height,
                    frame.rotationDegree,
                    frame.yuvStrides,
                    frame.yuvPlanes,
                    frameNativePointer);
        }
    }
}

