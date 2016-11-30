package com.twilio.video;

/*
 * Base class for all exception and errors throughout the SDK.
 */
abstract class VideoException extends Exception {
    public final int code;
    public final String message;

    protected VideoException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
