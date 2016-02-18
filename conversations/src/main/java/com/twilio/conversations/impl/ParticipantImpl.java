package com.twilio.conversations.impl;

import android.os.Handler;

import com.twilio.conversations.Media;
import com.twilio.conversations.Participant;
import com.twilio.conversations.ParticipantListener;
import com.twilio.conversations.impl.util.CallbackHandler;

public class ParticipantImpl implements Participant {
    private String identity;
    private String sid;
    private MediaImpl media;
    private ParticipantListener participantListener;
    private Handler handler;

    public ParticipantImpl(String identity, String sid) {
        this.identity = identity;
        this.sid = sid;
        this.media = new MediaImpl();
    }

    @Override
    public String getIdentity() {
        return identity;
    }

    @Override
    public Media getMedia() {
        return media;
    }

    @Override
    public void setParticipantListener(ParticipantListener participantListener) {
        this.handler = CallbackHandler.create();
        if(handler == null) {
            throw new IllegalThreadStateException("This thread must be able to obtain a Looper");
        }
        this.participantListener = participantListener;
    }

    @Override
    public ParticipantListener getParticipantListener() {
        return participantListener;
    }

    @Override
    public String getSid() {
        return sid;
    }

    MediaImpl getMediaImpl() {
        return media;
    }

    Handler getHandler() {
        return handler;
    }
}
