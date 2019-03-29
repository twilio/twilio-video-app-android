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

import android.support.annotation.NonNull;
import java.nio.ByteBuffer;

/** AudioSink consumes raw audio content from a AudioTrack. */
public interface AudioSink {

    /**
     * Render a single audio sample.
     *
     * @param audioSample A ByteBuffer which is being delivered to the sink.
     */
    void renderSample(@NonNull ByteBuffer audioSample, int encoding, int sampleRate, int channels);
}
