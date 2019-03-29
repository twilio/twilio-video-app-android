package com.twilio.video;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import java.nio.ByteBuffer;

class AudioSinkProxy implements AudioSink {
    private final Handler handler;
    private final HandlerThread handlerThread;
    private final AudioSink audioSink;
    private boolean isReleased = false;

    AudioSinkProxy(AudioSink audioSink) {
        this.audioSink = audioSink;
        this.handlerThread = new HandlerThread(audioSink.toString());
        this.handlerThread.start();
        this.handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public void renderSample(
            @NonNull ByteBuffer audioSample, int encoding, int sampleRate, int channels) {
        if (isReleased) return;
        handler.post(() -> audioSink.renderSample(audioSample, encoding, sampleRate, channels));
    }

    public void release() {
        isReleased = true;
        handlerThread.quit();
    }
}
