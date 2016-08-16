package com.twilio.video;

import java.util.HashMap;
import java.util.Map;

public class Room {

    private long nativeRoomContext;
    private String name;
    private String sid;
    private RoomState state;
    private Map<String, Participant> participantMap = new HashMap<>();

    Room(long nativeRoomContext, String name) {
        this.nativeRoomContext = nativeRoomContext;
        this.name = name;
        this.sid = "";
        state = RoomState.DISCONNECTED;
    }

    public String getName() {
        return name;
    }

    public String getSid() {
        if (sid.isEmpty()) {
            sid = nativeGetSid(nativeRoomContext);
        }
        return sid;
    }

    public RoomState getState() {
        return state;
    }

    public Map<String, Participant> getParticipants() {
        return new HashMap<>(participantMap);
    }

    public LocalMedia getLocalMedia() {
        // TODO: implement me
        return null;
    }

    public void disconnect() {
        nativeDisconnect(nativeRoomContext);
    }

    public interface Listener {
        void onConnected(Room room);

        void onConnectFailure(RoomsException error);

        void onDisconnected(Room room, RoomsException error);

        void onParticipantConnected(Room room, Participant participant);

        void onParticipantDisconnected(Room room, Participant participant);

    }

    // TODO: Once we move native listener inside room these methods might not be needed

    void release() {
        if (nativeRoomContext != 0) {
            nativeRelease(nativeRoomContext);
            nativeRoomContext = 0;
            // TODO: Once native video team makes decision about participant strategy
            // after disconnect, make sure it is properly implemented here. For now we are just
            // removing native participant context in order to prevent memory leak.
            for (Participant participant : participantMap.values()) {
                participant.release();
            }
        }
    }

    void setState(RoomState newState) {
        state = newState;
    }

    void addParticipant(Participant participant) {
        participantMap.put(participant.getSid(), participant);
    }

    Participant removeParticipant(String participantSid) {
        Participant participant = participantMap.remove(participantSid);
        participant.release();
        return participant;
    }

    private native String nativeGetSid(long nativeRoomContext);
    private native RoomState nativeGetState(long nativeRoomContext);
    private native void nativeDisconnect(long nativeRoomContext);
    private native void nativeRelease(long nativeRoomContext);

}