package com.twilio.video;

import java.util.List;

public interface StatsListener {
    void onStats(List<StatsReport> statsReports);
}
