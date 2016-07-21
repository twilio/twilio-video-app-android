package com.twilio.rooms;

import java.util.Map;

public class Room {

    public String getName() {
        // TODO: implement me
        return null;
    }

    public String getSid() {
        // TODO: implement me
        return null;
    }

    public boolean isConnected() {
        // TODO: implement me
        return false;
    }

    public Map<String, Participant> getParticipants() {
        // TODO: implement me
        return null;
    }

    public LocalMedia getLocalMedia() {
        // TODO: implement me
        return null;
    }

    public void setStatsListener(StatsListener statsListener) {
        // TODO: implement me
    }

    public void disconnect() {
        // TODO: implement me
    }

    public interface Listener {

        void onParticipantConnected(Room room, Participant participant);

        void onParticipantDisconnected(Room room, Participant participant);

    }
}