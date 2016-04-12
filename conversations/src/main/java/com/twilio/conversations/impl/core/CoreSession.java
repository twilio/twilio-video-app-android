package com.twilio.conversations.impl.core;

import com.twilio.conversations.IceOptions;

import java.util.Set;

public interface CoreSession {
    void start(CoreSessionMediaConstraints mediaConstraints);

    void stop();

    boolean enableVideo(boolean enabled, boolean paused);

    void inviteParticipants(Set<String> participants);

    boolean enableAudio(boolean enabled, boolean muted);
}
