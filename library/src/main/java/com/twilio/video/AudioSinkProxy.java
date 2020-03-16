/*
 * Copyright (C) 2018 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
