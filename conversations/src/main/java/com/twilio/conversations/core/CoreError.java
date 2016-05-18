package com.twilio.conversations.core;

public interface CoreError {
    int getCode();

    String getDomain();

    String getMessage();
}
