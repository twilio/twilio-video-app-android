package com.twilio.video;

/**
 * A class that provides information about a {@link Room} error.
 */
public class RoomException extends VideoException {
    RoomException(@Room.Error int code, String message) {
        super(code, message);
    }
}