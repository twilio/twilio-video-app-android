package com.twilio.video;

import java.util.Map;

public class Room {

    long nativeRoomContextHandle = 0;

    Room(long nativeRoomContextHandle) {
        this.nativeRoomContextHandle = nativeRoomContextHandle;
    }

    public String getName() {
        return nativeGetName(nativeRoomContextHandle);
    }

    public String getSid() {
        return nativeGetSid(nativeRoomContextHandle);
    }

    public RoomState getState() {
        return nativeGetState(nativeRoomContextHandle);
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
        nativeDisconnect(nativeRoomContextHandle);
        // TODO: Should we delete room_dc at this point ?
        //nativeRoomContextHandle = 0;
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