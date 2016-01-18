package com.twilio.conversations.impl;

/**
 * A YUV frame in the I420 format
 *
 */
public class I420Frame {
    public final org.webrtc.VideoRenderer.I420Frame frame;

    I420Frame(org.webrtc.VideoRenderer.I420Frame frame) {
        this.frame = frame;
    }
}

