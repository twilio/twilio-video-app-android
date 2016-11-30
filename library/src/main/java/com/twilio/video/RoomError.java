package com.twilio.video;

import java.util.Locale;

/**
 * A class that provides information about a {@link Room} error.
 */
public class RoomError {
    public final int code;
    public final String message;

    RoomError(@Room.Error int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),"code:%d, message:%s", code, message);
    }
}