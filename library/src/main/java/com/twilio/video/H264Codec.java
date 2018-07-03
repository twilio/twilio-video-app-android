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

/**
 * Block-oriented motion-compensation-based video compression standard.
 *
 * @see <a href="https://en.wikipedia.org/wiki/H.264/MPEG-4_AVC">H.264</a>
 */
public class H264Codec extends VideoCodec {
    @NonNull public static final String NAME = "H264";

    public H264Codec() {
        super(NAME);
    }
}
