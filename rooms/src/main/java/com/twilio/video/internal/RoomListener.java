package com.twilio.video.internal;


import com.twilio.video.Participant;

public interface RoomListener {
    void onConnected();
    void onDisconnected(int errorCode);
    void onConnectFailure(int errorCode);
    void onParticipantConnected(Participant participant);
    void onParticipantDisconnected(String participantSid);
}
