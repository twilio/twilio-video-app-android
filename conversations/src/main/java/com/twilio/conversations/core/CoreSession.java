package com.twilio.conversations.core;

import com.twilio.conversations.VideoConstraints;

import java.util.Set;

public interface CoreSession {
    void start(CoreSessionMediaConstraints mediaConstraints);

    void stop();

    boolean enableVideo(boolean enabled, boolean paused, VideoConstraints videoConstraints);

    void inviteParticipants(Set<String> participants);

    boolean enableAudio(boolean enabled, boolean muted);
}
