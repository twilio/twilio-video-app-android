package com.twilio.rooms;

public class MediaTrackStatsRecord {
    private final String trackId;
    private final int packetsLost;
    private final String direction;
    private final String codecName;
    private final String ssrc;
    private final String participantSid;
    private final long unixTimestamp;

    static MediaTrackStatsRecord create(CoreTrackStatsReport report) {
        MediaTrackStatsRecord record = null;
        if (report.direction.equalsIgnoreCase(CoreTrackStatsReport.DIRECTION_SENDING)) {
            if (report.mediaType.equalsIgnoreCase(CoreTrackStatsReport.MEDIA_OPTION_AUDIO_KEY)) {
                record = new LocalAudioTrackStatsRecord(report);
            } else if (report.mediaType.equalsIgnoreCase(CoreTrackStatsReport.MEDIA_OPTION_VIDEO_KEY)) {
                record = new LocalVideoTrackStatsRecord(report);
            }
        } else if (report.direction.equalsIgnoreCase(CoreTrackStatsReport.DIRECTION_RECEIVING)){
            if (report.mediaType.equalsIgnoreCase(CoreTrackStatsReport.MEDIA_OPTION_AUDIO_KEY)) {
                record = new RemoteAudioTrackStatsRecord(report);
            } else if (report.mediaType.equalsIgnoreCase(CoreTrackStatsReport.MEDIA_OPTION_VIDEO_KEY)) {
                record = new RemoteVideoTrackStatsRecord(report);
            }
        }
        return record;
    }

    MediaTrackStatsRecord(CoreTrackStatsReport report) {
        trackId = report.trackId;
        packetsLost = report.getIntValue(CoreTrackStatsReport.KeyEnum.PACKETS_LOST);
        direction = report.direction;
        codecName = report.codecName;
        ssrc = report.ssrc;
        participantSid = report.participantSid;
        unixTimestamp = report.timestamp;
    }

    public String getTrackId() {
        return trackId;
    }

    public int getPacketsLost() {
        return packetsLost;
    }

    public String getDirection() {
        return direction;
    }

    public String getCodecName() {
        return codecName;
    }

    public String getSsrc() {
        return ssrc;
    }

    public String getParticipantSid() {
        return participantSid;
    }

    public long getUnixTimestamp() {
        return unixTimestamp;
    }
}
