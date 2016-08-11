package com.twilio.video.internal;

import com.twilio.video.ClientError;
import com.twilio.video.Participant;

public interface RoomListener {
    void onConnected(String roomSid);
    void onDisconnected(String roomSid, ClientError errorCode);
    void onConnectFailure(String roomSid, ClientError errorCode);

    // TODO: maybe better idea would be to pass participant native handle and create
    // participant from Java layer
    void onParticipantConnected(String roomSid, long participantNativeDC);
    void onParticipantDisconnected(String roomSid, long participantNativeDC);
}
