package com.twilio.video;

/**
 * A remote video track represents a remote video source.
 */
public class RemoteVideoTrack extends VideoTrack {
    private final String sid;

    RemoteVideoTrack(org.webrtc.VideoTrack webRtcVideoTrack, boolean enabled, String sid) {
        super(webRtcVideoTrack, enabled);
        this.sid = sid;
    }

    /**
     * Returns a string that uniquely identifies the remote video track within the scope
     * of a {@link Room}.
     *
     * @return sid
     */
    public String getSid() {
        // TODO: Remove once proper SID is used to build object
        throw new UnsupportedOperationException();
    }
}

