package com.twilio.conversations;

import com.twilio.conversations.core.CoreTrackStatsReport;

class MediaTrackStatsRecordFactory {
    public static MediaTrackStatsRecord create(CoreTrackStatsReport report) {
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
}
