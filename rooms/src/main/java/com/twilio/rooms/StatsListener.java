package com.twilio.rooms;

/**
 * Listener for media tracks statistics
 *
 * @note: Each media track will report a statistics record once per second.
 *        We suggest using a separate thread to obtain stats
 */
public interface StatsListener {


    /**
     * This method notifies the listener when statistics are received for a local audio track.
     *
     * @param conversation The conversation
     * @param participant The participant
     * @param trackStatsRecord Local audio track statistics.
     */
    void onLocalAudioTrackStatsRecord(Conversation conversation, Participant participant,
                                      LocalAudioTrackStatsRecord trackStatsRecord);

    /**
     * This method notifies the listener when statistics are received for a local video track.
     *
     * @param conversation The conversation
     * @param participant The participant
     * @param trackStatsRecord Local video track statistics.
     */
    void onLocalVideoTrackStatsRecord(Conversation conversation, Participant participant,
                                      LocalVideoTrackStatsRecord trackStatsRecord);

    /**
     * This method notifies the listener when statistics are received for a remote audio track.
     *
     * @param conversation The conversation
     * @param participant The participant
     * @param trackStatsRecord Remote audio track statistics.
     */
    void onRemoteAudioTrackStatsRecord(Conversation conversation, Participant participant,
                                       RemoteAudioTrackStatsRecord trackStatsRecord);

    /**
     * This method notifies the listener when statistics are received for a remote video track.
     *
     * @param conversation The conversation
     * @param participant The participant
     * @param trackStatsRecord Remote video track statistics.
     */
    void onRemoteVideoTrackStatsRecord(Conversation conversation, Participant participant,
                                       RemoteVideoTrackStatsRecord trackStatsRecord);

}
