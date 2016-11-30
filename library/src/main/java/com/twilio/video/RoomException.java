package com.twilio.video;

/**
 * A class that provides information about a {@link Room} error.
 */
public class RoomException extends Exception {
    public final int code;
    public final String message;

    RoomException(@Room.Error int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}