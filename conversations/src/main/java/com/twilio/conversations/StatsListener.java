package com.twilio.conversations;

public interface StatsListener {

    /**
     * This method notifies the listener when statistics are received for a track.
     *
     * @param conversation The conversation
     * @param stats Media track statistics.
     */
    void onTrackStatsRecord(Conversation conversation, TrackStatsRecord stats);

}
