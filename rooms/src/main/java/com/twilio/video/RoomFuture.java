package com.twilio.video;

public class RoomFuture {
    private long nativeRoomFutureHandle;

    RoomFuture(long nativeRoomFutureHandle) {
        this.nativeRoomFutureHandle = nativeRoomFutureHandle;
    }

    public void cancel() {
        // TODO: implement me
    }

}