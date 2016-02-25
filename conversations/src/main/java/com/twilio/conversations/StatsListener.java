package com.twilio.conversations;

public interface StatsListener {

    /**
     * This method notifies the listener when statistics are received for a track.
     *
     * @param conversation The conversation
     * @param stats Media track statistics.
     *
     * @note: Each media track will report a statistics record once per second.
     *        We suggest using a separate thread to obtain stats
     */
    void onMediaTrackStatsRecord(Conversation conversation, MediaTrackStatsRecord stats);

}
