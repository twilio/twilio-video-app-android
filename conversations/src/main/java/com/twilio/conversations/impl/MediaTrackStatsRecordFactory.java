package com.twilio.conversations.impl;

import com.twilio.conversations.TrackStatsRecord;
import com.twilio.conversations.impl.core.CoreTrackStatsReport;

class MediaTrackStatsRecordFactory {

    public static TrackStatsRecord create(CoreTrackStatsReport report) {
        TrackStatsRecord record = null;
        if (report.direction.equalsIgnoreCase(CoreTrackStatsReport.DIRECTION_SENDING)) {
            if (report.mediaType.equalsIgnoreCase(CoreTrackStatsReport.MEDIA_OPTION_AUDIO_KEY)) {
                record = new LocalAudioStatsRecordImpl(report);
            } else if (report.mediaType.equalsIgnoreCase(CoreTrackStatsReport.MEDIA_OPTION_VIDEO_KEY)) {
                record = new LocalVideoStatsRecordImpl(report);
            }
        } else if (report.direction.equalsIgnoreCase(CoreTrackStatsReport.DIRECTION_RECEIVING)){
            if (report.mediaType.equalsIgnoreCase(CoreTrackStatsReport.MEDIA_OPTION_AUDIO_KEY)) {
                record = new RemoteAudioStatsRecordImpl(report);
            } else if (report.mediaType.equalsIgnoreCase(CoreTrackStatsReport.MEDIA_OPTION_VIDEO_KEY)) {
                record = new RemoteVideoStatsRecordImpl(report);
            }
        }
        return record;
    }
}
