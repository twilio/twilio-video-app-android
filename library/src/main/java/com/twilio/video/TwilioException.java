package com.twilio.video;

/**
 * A class that provides information about a {@link Room} error.
 */
public class TwilioException extends Exception {
    public final int code;
    public final String message;

    TwilioException(@Room.Error int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}