package com.twilio.video.internal;

import com.twilio.video.ClientError;
import com.twilio.video.Participant;

public interface RoomListener {
    void onConnected();
    void onDisconnected(ClientError errorCode);
    void onConnectFailure(ClientError errorCode);

    // TODO: maybe better idea would be to pass participant native handle and create
    // participant from Java layer
    void onParticipantConnected(long participantNativeDC);
    void onParticipantDisconnected(long participantNativeDC);
}
