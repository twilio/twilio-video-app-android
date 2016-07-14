package com.twilio.rooms;

import java.util.Locale;

/**
 * A class that provides information about a {@link Room} error.
 *
 */
public class RoomsException extends Exception {
    private static final long serialVersionUID = 8881301848720707153L;

    private int errorCode;
    private String errorMessage;

    public RoomsException(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public String getMessage() {
        return String.format(Locale.getDefault(),"code:%d, message:%s", errorCode, errorMessage);
    }

    public int getErrorCode() {
        return errorCode;
    }
}