package com.twilio.conversations.impl.core;

import com.twilio.conversations.impl.logging.Logger;

import java.util.HashMap;
import java.util.Map;

public class CoreTrackStatsReport {

    public static final String DIRECTION_SENDING = "sending";
    public static final String DIRECTION_RECEIVING = "receiving";
    public static final String MEDIA_OPTION_AUDIO_KEY = "audio";
    public static final String MEDIA_OPTION_VIDEO_KEY = "video";

    public final String participantAddress;
    public final String participantSid;
    public final String trackId;
    public final String mediaType; // audio or video
    public final String direction; // incoming or outgoing

    public final String codecName;
    public final String ssrc;

    public final String activeConnectionId;

    public final long timestamp;  // time since 1970-01-01T00:00:00Z in milliseconds

    private final Map<String, String> data = new HashMap<String, String>();

    static final Logger logger = Logger.getLogger(CoreTrackStatsReport.class);

    public enum KeyEnum {
        BYTES_SENT("bytesSent"),
        PACKETS_LOST("packetsLost"),
        PACKETS_SENT("packetsSent"),
        BYTES_RECEIVED("bytesReceived"),
        PACKETS_RECEIVED("packetsReceived"),
        FRAME_HEIGHT_INPUT("googFrameHeightInput"),
        FRAME_WIDTH_INPUT("googFrameWidthInput"),
        FRAME_HEIGHT_SENT("googFrameHeightSent"),
        FRAME_WIDTH_SENT("googFrameWidthSent"),
        RTT("googRtt"),
        FRAME_HEIGHT_RECEIVED("googFrameHeightReceived"),
        FRAME_WIDTH_RECEIVED("googFrameWidthReceived"),
        FRAME_RATE_RECEIVED("googFrameRateReceived"),
        FRAME_RATE_SENT("googFrameRateSent"),
        JITTER_BUFFER_MS("googJitterBufferMs"),
        JITTER("jitter"),
        AUDIO_INPUT_LEVEL("audioInputLevel"),
        AUDIO_OUTPUT_LEVEL("audioOutputLevel"),
        JITTER_RECEIVED("googJitterReceived");


        private String key;

        KeyEnum(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public CoreTrackStatsReport(String participantAddress, String participantSid,
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
        this.timestamp = (long)timestamp;
        setMapValues(keys, values);
    }

    public String getDataValueFromKey(KeyEnum k) {
        return data.get(k.getKey());
    }

    private void setMapValues(String[] keys, String[] values) {
        data.clear();
        if (keys != null && values != null && keys.length == values.length) {
            for (int i=0; i<keys.length; i++) {
                data.put(keys[i], values[i]);
            }
        }
    }

    public String getStringValue(KeyEnum key) {
        String result = data.get(key.getKey());
        if (result == null) {
            result = "";
        }
        return result;
    }

    public int getIntValue(KeyEnum key) {
        int result = 0;
        try {
            result = Integer.valueOf(data.get(key.getKey()));
        } catch (NumberFormatException e) {
            logger.w("Unexpected value type for: "+key.getKey() +
                    " value:"+data.get(key.getKey()));
        }
        return result;
    }

    public long getLongValue(KeyEnum key) {
        long result = 0;
        try {
            result = Long.valueOf(data.get(key.getKey()));
        } catch (NumberFormatException e) {
            logger.w("Unexpected value type for: "+key.getKey() +
                    " value:"+data.get(key.getKey()));
        }
        return result;
    }

}
