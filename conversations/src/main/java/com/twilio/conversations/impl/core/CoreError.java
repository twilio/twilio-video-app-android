package com.twilio.conversations.impl.core;

public interface CoreError {
    int getCode();

    String getDomain();

    String getMessage();
}
