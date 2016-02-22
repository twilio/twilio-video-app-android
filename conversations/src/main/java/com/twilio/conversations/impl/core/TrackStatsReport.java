package com.twilio.conversations.impl.core;

import java.util.HashMap;
import java.util.Map;

public class TrackStatsReport {

    public String participantAddress;
    public String participantSid;
    public String trackId;
    public String mediaType; // audio or video
    public String direction; // incoming or outgoing

    public String codecName;
    public String ssrc;

    public String activeConnectionId;

    public double timestamp;  // time since 1970-01-01T00:00:00Z in milliseconds

    public Map<String, String> data = new HashMap<String, String>();

    public TrackStatsReport() {}
    public TrackStatsReport(String participantAddress, String participantSid,
            String trackId, String mediaType, String direction, String codecName,
            String ssrc, String activeConnectionId, double timestamp,
            String[] keys, String[] values) {
        this.participantAddress = participantAddress;
        this.participantSid = participantSid;
        this.trackId = trackId;
        this.mediaType = mediaType;
        this.direction = direction;
        this.codecName = codecName;
        this.ssrc = ssrc;
        this.activeConnectionId = activeConnectionId;
        this.timestamp = timestamp;
        setMapValues(keys, values);
    }

    public boolean setMapValues(String[] keys, String[] values) {
        if (keys.length != 0 && keys.length != values.length) {
            return false;
        }
        data.clear();
        for (int i=0; i<keys.length; i++) {
            data.put(keys[i], values[i]);
        }
        return true;
    }

}
