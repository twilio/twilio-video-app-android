package com.twilio.conversations.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.twilio.conversations.I420Frame;
import com.twilio.conversations.MediaTrackState;
import com.twilio.conversations.VideoRenderer;
import com.twilio.conversations.VideoTrack;
import com.twilio.conversations.impl.core.TrackInfo;

public class VideoTrackImpl implements VideoTrack {
    private org.webrtc.VideoTrack videoTrack;
    private TrackInfo trackInfo;
    private MediaTrackState trackState;
    private Map<VideoRenderer, org.webrtc.VideoRenderer> videoRenderersMap =
            new HashMap<VideoRenderer, org.webrtc.VideoRenderer>();

    VideoTrackImpl() {
        trackState = MediaTrackState.IDLE;
    }

    VideoTrackImpl(org.webrtc.VideoTrack videoTrack, TrackInfo trackInfo) {
        this.videoTrack = videoTrack;
        this.trackInfo = trackInfo;

        trackState = MediaTrackState.STARTED;
    }

    void setWebrtcVideoTrack(org.webrtc.VideoTrack videoTrack) {
        this.videoTrack = videoTrack;

        trackState = MediaTrackState.STARTED;
    }

    void setTrackInfo(TrackInfo trackInfo) {
        this.trackInfo = trackInfo;
    }

    org.webrtc.VideoTrack getWebrtcVideoTrack() {
        return videoTrack;
    }

    TrackInfo getTrackInfo() {
        return trackInfo;
    }

    @Override
    public void addRenderer(VideoRenderer videoRenderer) {
        org.webrtc.VideoRenderer webrtcVideoRenderer =
                createWebRtcVideoRenderer(videoRenderer);
        videoRenderersMap.put(videoRenderer, webrtcVideoRenderer);
        videoTrack.addRenderer(webrtcVideoRenderer);
    }

    @Override
    public void removeRenderer(VideoRenderer videoRenderer) {
        org.webrtc.VideoRenderer webrtcVideoRenderer =
                videoRenderersMap.remove(videoRenderer);
        if (webrtcVideoRenderer != null) {
            videoTrack.removeRenderer(webrtcVideoRenderer);
        }
    }

    @Override
    public List<VideoRenderer> getRenderers() {
        return new ArrayList<VideoRenderer>(videoRenderersMap.keySet());
    }

    private org.webrtc.VideoRenderer createWebRtcVideoRenderer(VideoRenderer videoRenderer) {
        return new org.webrtc.VideoRenderer(new VideoRendererCallbackAdapter(videoRenderer));
    }

    @Override
    public String getTrackId() {
        return trackInfo != null ? trackInfo.getTrackId() : null;
    }

    @Override
    public MediaTrackState getState() {
        return trackState;
    }

    void updateTrackInfo(TrackInfo trackInfo) {
        this.trackInfo = trackInfo;
    }

    void setTrackState(MediaTrackState trackState) {
        this.trackState = trackState;
    }

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
