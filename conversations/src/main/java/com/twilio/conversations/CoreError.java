package com.twilio.conversations;

public interface CoreError {
    int getCode();

    String getDomain();

    String getMessage();
}
