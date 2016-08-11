package com.twilio.video;

import java.util.Map;

public class Room {

    long nativeRoomDCHandle = 0;

    Room(long nativeRoomDCHandle) {
        this.nativeRoomDCHandle = nativeRoomDCHandle;
    }

    public String getName() {
        return nativeGetName(nativeRoomDCHandle);
    }

    public String getSid() {
        return nativeGetSid(nativeRoomDCHandle);
    }

    public RoomState getState() {
        return nativeGetState(nativeRoomDCHandle);
    }

    public Map<String, Participant> getParticipants() {
        // TODO: implement me
        return null;
    }

    public LocalMedia getLocalMedia() {
        // TODO: implement me
        return null;
    }

    public void disconnect() {
        nativeDisconnect(nativeRoomDCHandle);
        // TODO: Should we delete room_dc at this point ?
        //nativeRoomDCHandle = 0;
    }

    public interface Listener {
        void onConnected(Room room);

        void onConnectFailure(RoomsException error);

        void onDisconnected(Room room, RoomsException error);

        void onParticipantConnected(Room room, Participant participant);

        void onParticipantDisconnected(Room room, Participant participant);

    }

    private native String nativeGetName(long nativeRoomDCHandle);
    private native String nativeGetSid(long nativeRoomDCHandle);
    private native RoomState nativeGetState(long nativeRoomDCHandle);
    private native void nativeDisconnect(long nativeRoomDCHandle);

}