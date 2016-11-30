package com.twilio.video;

import java.util.Locale;

/**
 * A class that provides information about a {@link Room} error.
 */
public class RoomError {
    public final int errorCode;
    public final String errorMessage;

    RoomError(@Room.Error int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),"code:%d, message:%s", errorCode, errorMessage);
    }
}