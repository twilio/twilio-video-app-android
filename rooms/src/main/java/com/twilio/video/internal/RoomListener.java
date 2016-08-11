package com.twilio.video.internal;


public interface RoomListener {
    void onConnected();
    void onDisconnected(int errorCode);
    void onConnectFailure(int errorCode);

    // TODO: maybe better idea would be to pass participant native handle and create
    // participant from Java layer
    void onParticipantConnected(long participantNativeDC);
    void onParticipantDisconnected(long participantNativeDC);
}
