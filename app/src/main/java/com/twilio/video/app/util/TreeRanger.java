package com.twilio.video.app.util;

public interface TreeRanger {
    void inform(String message);
    void caution(String message);
    void alert(Throwable throwable);
}
