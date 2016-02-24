package com.twilio.conversations.impl;

import com.twilio.conversations.MediaTrackStatsRecord;
import com.twilio.conversations.impl.core.TrackStatsReport;

class MediaTrackStatsRecordFactory {

    public static MediaTrackStatsRecord create(TrackStatsReport report) {
        MediaTrackStatsRecord record = null;
        if (report.direction.equalsIgnoreCase(TrackStatsReport.DIRECTION_SENDING)) {
            if (report.mediaType.equalsIgnoreCase(TrackStatsReport.MEDIA_OPTION_AUDIO_KEY)) {
                record = new LocalAudioMediaStatsRecordImpl(report);
            } else if (report.mediaType.equalsIgnoreCase(TrackStatsReport.MEDIA_OPTION_VIDEO_KEY)) {
                record = new LocalVideoMediaStatsRecordImpl(report);
            }
        } else if (report.direction.equalsIgnoreCase(TrackStatsReport.DIRECTION_RECEIVING)){
            if (report.mediaType.equalsIgnoreCase(TrackStatsReport.MEDIA_OPTION_AUDIO_KEY)) {
                record = new RemoteAudioMediaStatsRecordImpl(report);
            } else if (report.mediaType.equalsIgnoreCase(TrackStatsReport.MEDIA_OPTION_VIDEO_KEY)) {
                record = new RemoteVideoMediaStatsRecordImpl(report);
            }
        }
        return record;
    }
}
