package com.twilio.conversations.impl;

import com.twilio.conversations.TrackStatsReport;
import com.twilio.conversations.impl.core.CoreTrackStatsReport;

class MediaTrackStatsRecordFactory {

    public static TrackStatsReport create(CoreTrackStatsReport report) {
        TrackStatsReport record = null;
        if (report.direction.equalsIgnoreCase(CoreTrackStatsReport.DIRECTION_SENDING)) {
            if (report.mediaType.equalsIgnoreCase(CoreTrackStatsReport.MEDIA_OPTION_AUDIO_KEY)) {
                record = new LocalAudioStatsReportImpl(report);
            } else if (report.mediaType.equalsIgnoreCase(CoreTrackStatsReport.MEDIA_OPTION_VIDEO_KEY)) {
                record = new LocalVideoStatsReportImpl(report);
            }
        } else if (report.direction.equalsIgnoreCase(CoreTrackStatsReport.DIRECTION_RECEIVING)){
            if (report.mediaType.equalsIgnoreCase(CoreTrackStatsReport.MEDIA_OPTION_AUDIO_KEY)) {
                record = new RemoteAudioStatsReportImpl(report);
            } else if (report.mediaType.equalsIgnoreCase(CoreTrackStatsReport.MEDIA_OPTION_VIDEO_KEY)) {
                record = new RemoteVideoStatsReportImpl(report);
            }
        }
        return record;
    }
}
