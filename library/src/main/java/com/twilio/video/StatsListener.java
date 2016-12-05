package com.twilio.video;

import java.util.List;

/**
 * Interface that provides event related to {@link Room#getStats(StatsListener)}
 */
public interface StatsListener {
    /**
     * Notifies when stats reports for all media tracks are ready.
     * @param statsReports Reports for all media tracks.
     */
    void onStats(List<StatsReport> statsReports);
}
