package com.twilio.conversations.impl;

import com.twilio.conversations.MediaTrackStatsRecord;
import com.twilio.conversations.impl.core.CoreTrackStatsReport;

class TrackStatsRecordFactory {

    public static MediaTrackStatsRecord create(CoreTrackStatsReport report) {
        MediaTrackStatsRecord record = null;
        if (report.direction.equalsIgnoreCase(CoreTrackStatsReport.DIRECTION_SENDING)) {
            if (report.mediaType.equalsIgnoreCase(CoreTrackStatsReport.MEDIA_OPTION_AUDIO_KEY)) {
                record = new LocalAudioTrackStatsRecordImpl(report);
            } else if (report.mediaType.equalsIgnoreCase(CoreTrackStatsReport.MEDIA_OPTION_VIDEO_KEY)) {
                record = new LocalVideoTrackStatsRecordImpl(report);
            }
        } else if (report.direction.equalsIgnoreCase(CoreTrackStatsReport.DIRECTION_RECEIVING)){
            if (report.mediaType.equalsIgnoreCase(CoreTrackStatsReport.MEDIA_OPTION_AUDIO_KEY)) {
                record = new RemoteAudioTrackStatsRecordImpl(report);
            } else if (report.mediaType.equalsIgnoreCase(CoreTrackStatsReport.MEDIA_OPTION_VIDEO_KEY)) {
                record = new RemoteVideoTrackStatsRecordImpl(report);
            }
        }
        return record;
    }
}
